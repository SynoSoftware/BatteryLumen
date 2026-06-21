package com.synosoftware.battery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synosoftware.battery.R
import com.synosoftware.battery.data.battery.BatteryMonitor
import com.synosoftware.battery.data.formatDuration
import com.synosoftware.battery.data.notification.ChargingNotificationManager
import com.synosoftware.battery.data.preferences.AppLanguage
import com.synosoftware.battery.data.preferences.SettingsRepository
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.data.preferences.UserPreferences
import com.synosoftware.battery.data.session.ChargeSessionEntity
import com.synosoftware.battery.data.session.ChargeSessionRepository
import com.synosoftware.battery.data.session.toMetrics
import com.synosoftware.battery.data.session.toUi
import com.synosoftware.battery.domain.CapacityPoint
import com.synosoftware.battery.domain.DataQuality
import com.synosoftware.battery.domain.buildCapacityPoints
import com.synosoftware.battery.domain.buildChargeRateBuckets
import com.synosoftware.battery.domain.estimateCapacity
import com.synosoftware.battery.domain.BatteryDecisionEngine
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.DeviceCapabilityMatrix
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.domain.SessionAssessment
import com.synosoftware.battery.ui.model.BatteryEvent
import com.synosoftware.battery.ui.model.DailyChargingSummaryUi
import com.synosoftware.battery.ui.model.HealthEvolutionUi
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import com.synosoftware.battery.ui.model.BatteryUiState
import com.synosoftware.battery.ui.model.MIN_USEFUL_SESSION_COUNT
import com.synosoftware.battery.ui.toUi
import com.synosoftware.battery.i18n.TR
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
                ui = session.toUi(assessment, preferences.temperatureUnit),
            )
        }
        val activeView = sessionViews.firstOrNull { it.metrics.status == SessionStatus.ACTIVE }
        val activeSession = activeView?.ui
        val allMetrics = sessionViews.map { it.metrics }
        val capacityPoints = buildCapacityPoints(allMetrics)
        val capacityEstimate = estimateCapacity(capacityPoints, preferences.designCapacityMah)
        val estimatedCapacityForRisk = capacityEstimate.estimatedCapacityMah?.toDouble()
            ?: preferences.designCapacityMah?.toDouble()
        val historicalBuckets = buildChargeRateBuckets(allMetrics)
        val decision = snapshot?.let { current ->
            val activeMetrics = activeView?.metrics
            decisionEngine.analyze(
                snapshot = current,
                session = activeMetrics,
                targetPercent = preferences.targetChargePercent,
                estimatedCapacityMah = estimatedCapacityForRisk,
                historicalBuckets = historicalBuckets,
            )
        }
        val healthEvolution = buildHealthEvolution(capacityPoints)
        val healthEstimate = capacityEstimate.toUi(preferences.designCapacityMah)
        val dailySummary = buildDailySummary(sessionViews)
        BatteryUiState(
            targetChargePercent = preferences.targetChargePercent,
            designCapacityMah = preferences.designCapacityMah,
            experimentalMetricsEnabled = preferences.experimentalMetricsEnabled,
            temperatureUnit = preferences.temperatureUnit,
            themeMode = preferences.themeMode,
            language = preferences.language,
            currentSnapshot = snapshot,
            decision = decision,
            activeSession = activeSession,
            sessions = sessionViews.map { it.ui },
            healthEstimate = healthEstimate,
            healthEvolution = healthEvolution,
            dailySummary = dailySummary,
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
                    notificationManager.notifyTargetReached(target, snapshot.levelPercent, preferences.language)
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

    fun setDesignCapacityMah(capacityMah: Int?) {
        viewModelScope.launch {
            settingsRepository.setDesignCapacityMah(capacityMah)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
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

    private fun buildHealthEvolution(points: List<CapacityPoint>): HealthEvolutionUi {
        val ordered = points
            .filter { it.quality == DataQuality.USEFUL }
            .map {
                HealthTrendPointUi(
                    label = dateFormatter.format(Date(it.timestampMs)),
                    estimatedCapacityMah = it.estimatedFullCapacityMah.toFloat(),
                )
            }
            .takeLast(12)

        return HealthEvolutionUi(points = ordered)
    }

    private fun buildDailySummary(
        sessions: List<SessionView>,
    ): DailyChargingSummaryUi {
        val todayStartMs = startOfDayMs()
        val todaySessions = sessions.filter { it.entity.lastSeenAtMs >= todayStartMs }
        if (todaySessions.isEmpty()) {
            return DailyChargingSummaryUi(
                headline = TR(R.string.daily_summary_collecting),
                detail = TR(R.string.daily_summary_waiting),
                confidence = ConfidenceLevel.LOW,
                evidenceGrade = EvidenceGrade.INFERRED,
                sessionCount = 0,
            )
        }

        // Mirrors the spec's summarizeDay(): hot+near-full time together is the strongest
        // signal, then raw hot-while-charging time, then plain time-near-full as fallbacks.
        val totalHotAbove85Sec = todaySessions.sumOf { it.metrics.timeHotAndAbove85Sec }
        val totalAbove40Sec = todaySessions.sumOf { it.metrics.timeAbove40Sec }
        val totalAbove90Sec = todaySessions.sumOf { it.metrics.timeAbove90Sec }
        val totalAbove85Sec = todaySessions.sumOf { it.metrics.timeAbove85Sec }

        val headline = when {
            totalHotAbove85Sec >= 20 * 60L -> TR(R.string.daily_summary_risky)
            totalAbove40Sec >= 30 * 60L -> TR(R.string.daily_summary_risky)
            totalAbove90Sec >= 120 * 60L -> TR(R.string.daily_summary_normal)
            totalAbove85Sec >= 240 * 60L -> TR(R.string.daily_summary_normal)
            else -> TR(R.string.daily_summary_good)
        }
        val detail = when {
            totalHotAbove85Sec >= 20 * 60L -> TR(R.string.daily_summary_issue_hot_above_85)
            totalAbove40Sec >= 30 * 60L -> TR(R.string.daily_summary_issue_hot)
            totalAbove90Sec >= 120 * 60L -> TR(R.string.daily_summary_above_90_issue, formatDuration(totalAbove90Sec * 1000L))
            totalAbove85Sec >= 240 * 60L -> TR(R.string.daily_summary_above_85_issue, formatDuration(totalAbove85Sec * 1000L))
            else -> TR(R.string.daily_summary_no_issue)
        }
        val advice = when {
            totalHotAbove85Sec >= 20 * 60L -> TR(R.string.daily_summary_advice_hot_above_85)
            totalAbove40Sec >= 30 * 60L -> TR(R.string.daily_summary_advice_hot)
            totalAbove90Sec >= 120 * 60L -> TR(R.string.daily_summary_advice_above_90)
            totalAbove85Sec >= 240 * 60L -> TR(R.string.daily_summary_advice_above_85)
            else -> TR(R.string.daily_summary_advice_none)
        }
        val confidence = when {
            todaySessions.size >= MIN_USEFUL_SESSION_COUNT -> ConfidenceLevel.HIGH
            todaySessions.size >= 2 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        return DailyChargingSummaryUi(
            headline = headline,
            advice = advice,
            detail = detail,
            confidence = confidence,
            evidenceGrade = EvidenceGrade.INFERRED,
            sessionCount = todaySessions.size,
        )
    }

    private fun startOfDayMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
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
