package com.synosoftware.battery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synosoftware.battery.data.battery.BatteryMonitor
import com.synosoftware.battery.data.notification.ChargingNotificationManager
import com.synosoftware.battery.data.preferences.SettingsRepository
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.data.preferences.UserPreferences
import com.synosoftware.battery.data.session.ChargeSessionEntity
import com.synosoftware.battery.data.session.ChargeSessionRepository
import com.synosoftware.battery.data.session.toMetrics
import com.synosoftware.battery.data.session.toUi
import com.synosoftware.battery.domain.BatteryDecisionEngine
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.DeviceCapabilityMatrix
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.domain.SessionAssessment
import com.synosoftware.battery.ui.model.BatteryEvent
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.MIN_USEFUL_SESSION_COUNT
import com.synosoftware.battery.ui.model.HealthEvolutionUi
import com.synosoftware.battery.ui.model.HealthTrendState
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import com.synosoftware.battery.ui.model.BatteryUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatteryViewModel(
    private val batteryMonitor: BatteryMonitor,
    private val sessionRepository: ChargeSessionRepository,
    private val settingsRepository: SettingsRepository,
    private val decisionEngine: BatteryDecisionEngine,
    private val notificationManager: ChargingNotificationManager,
) : ViewModel() {
    private val latestSnapshot = MutableStateFlow<BatterySnapshot?>(null)
    private val preferencesState = settingsRepository.preferences.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        UserPreferences(),
    )
    val themeMode: StateFlow<ThemeMode> = preferencesState
        .map { it.themeMode }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ThemeMode.SYSTEM,
        )
    private val _events = MutableSharedFlow<BatteryEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<BatteryUiState> = combine(
        preferencesState,
        sessionRepository.observeSessions(),
        latestSnapshot,
    ) { preferences, sessions, snapshot ->
        val sessionViews = sessions.map { session ->
            val metrics = session.toMetrics()
            val assessment = decisionEngine.assessSession(metrics)
            SessionView(
                entity = session,
                metrics = metrics,
                assessment = assessment,
                ui = session.toUi(assessment),
            )
        }
        val activeView = sessionViews.firstOrNull { it.metrics.status == SessionStatus.ACTIVE }
        val activeSession = activeView?.ui
        val decision = snapshot?.let { current ->
            val activeMetrics = activeView?.metrics
            decisionEngine.analyze(current, activeMetrics, preferences.targetChargePercent)
        }
        val healthEvolution = buildHealthEvolution(sessionViews)
        val healthEstimate = buildHealthEstimate(healthEvolution.points)
        BatteryUiState(
            targetChargePercent = preferences.targetChargePercent,
            experimentalMetricsEnabled = preferences.experimentalMetricsEnabled,
            temperatureUnit = preferences.temperatureUnit,
            themeMode = preferences.themeMode,
            currentSnapshot = snapshot,
            decision = decision,
            activeSession = activeSession,
            sessions = sessionViews.map { it.ui },
            healthEstimate = healthEstimate,
            healthEvolution = healthEvolution,
            capabilities = DeviceCapabilityMatrix.defaultCapabilities(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        BatteryUiState(
            capabilities = DeviceCapabilityMatrix.defaultCapabilities(),
        ),
    )

    init {
        viewModelScope.launch {
            batteryMonitor.snapshots().collect { snapshot ->
                latestSnapshot.value = snapshot
                val preferences = preferencesState.value
                val result = sessionRepository.recordSnapshot(snapshot, preferences.targetChargePercent)
                if (result.targetCrossed) {
                    val target = result.targetPercent ?: preferences.targetChargePercent
                    notificationManager.notifyTargetReached(target, snapshot.levelPercent)
                    _events.tryEmit(BatteryEvent.TargetReached(target))
                }
            }
        }
    }

    fun setTargetChargePercent(targetPercent: Int) {
        viewModelScope.launch {
            settingsRepository.setTargetChargePercent(targetPercent)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(unit)
        }
    }

    fun setExperimentalMetricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setExperimentalMetricsEnabled(enabled)
        }
    }

    fun seedDebugSessions() {
        viewModelScope.launch {
            sessionRepository.seedDebugSessions()
        }
    }

    private fun buildHealthEvolution(sessions: List<SessionView>): HealthEvolutionUi {
        val ordered = sessions
            .sortedBy { it.entity.endedAtMs ?: it.entity.lastSeenAtMs }
            .filter { it.assessment.usefulForHealth && it.metrics.status == SessionStatus.COMPLETED }
            .mapNotNull { session ->
                val metrics = session.metrics
                val startCounter = metrics.startChargeCounterUah ?: return@mapNotNull null
                val currentCounter = metrics.currentChargeCounterUah ?: return@mapNotNull null
                val deltaCounter = currentCounter - startCounter
                val gainPercent = (metrics.currentLevelPercent - metrics.startLevelPercent).coerceAtLeast(0)
                if (gainPercent < 30 || deltaCounter <= 0) {
                    return@mapNotNull null
                }
                val estimatedCapacityMah = (deltaCounter / (gainPercent / 100f)) / 1000f
                if (estimatedCapacityMah <= 0f) {
                    return@mapNotNull null
                }
                val pointTimeMs = session.entity.endedAtMs ?: session.entity.lastSeenAtMs
                HealthTrendPointUi(
                    label = dateFormatter.format(Date(pointTimeMs)),
                    estimatedCapacityMah = estimatedCapacityMah,
                )
            }
            .takeLast(12)

        return HealthEvolutionUi(points = ordered)
    }

    private fun buildHealthEstimate(points: List<HealthTrendPointUi>): BatteryHealthEstimateUi {
        val capacities = points.map { it.estimatedCapacityMah }.filter { it.isFinite() && it > 0f }
        if (capacities.size < MIN_USEFUL_SESSION_COUNT) {
            return BatteryHealthEstimateUi(
                estimatedCapacityMah = null,
                likelyRangeMah = null,
                confidence = com.synosoftware.battery.domain.ConfidenceLevel.LOW,
                usefulSessionCount = capacities.size,
                trend = if (capacities.isEmpty()) HealthTrendState.COLLECTING else HealthTrendState.NOISY,
            )
        }

        val sorted = capacities.sorted()
        val median = sorted[sorted.size / 2]
        val low = sorted[(sorted.size * 0.25f).toInt().coerceIn(0, sorted.lastIndex)]
        val high = sorted[(sorted.size * 0.75f).toInt().coerceIn(0, sorted.lastIndex)]
        val spreadRatio = if (median > 0f) (high - low) / median else Float.POSITIVE_INFINITY
        val confidence = when {
            capacities.size >= 10 && spreadRatio <= 0.06f -> com.synosoftware.battery.domain.ConfidenceLevel.HIGH
            capacities.size >= 5 && spreadRatio <= 0.12f -> com.synosoftware.battery.domain.ConfidenceLevel.MEDIUM
            else -> com.synosoftware.battery.domain.ConfidenceLevel.LOW
        }
        val trend = when {
            capacities.size < 3 -> HealthTrendState.COLLECTING
            isDeclining(sorted) -> HealthTrendState.DECLINING
            spreadRatio <= 0.10f -> HealthTrendState.STABLE
            else -> HealthTrendState.NOISY
        }

        return BatteryHealthEstimateUi(
            estimatedCapacityMah = median.roundToInt(),
            likelyRangeMah = low.roundToInt()..high.roundToInt(),
            confidence = confidence,
            usefulSessionCount = capacities.size,
            trend = trend,
        )
    }

    private fun isDeclining(capacities: List<Float>): Boolean {
        if (capacities.size < 3) return false
        val firstHalf = capacities.take(capacities.size / 2)
        val secondHalf = capacities.takeLast(capacities.size / 2)
        if (firstHalf.isEmpty() || secondHalf.isEmpty()) return false
        val firstAverage = firstHalf.average()
        val secondAverage = secondHalf.average()
        if (firstAverage <= 0.0) return false
        val deltaRatio = (secondAverage - firstAverage) / firstAverage
        return deltaRatio < -0.04
    }

    private data class SessionView(
        val entity: ChargeSessionEntity,
        val metrics: ChargeSessionMetrics,
        val assessment: SessionAssessment,
        val ui: com.synosoftware.battery.ui.model.BatterySessionUi,
    )

    private companion object {
        private val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
    }
}
