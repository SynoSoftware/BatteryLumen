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
import com.synosoftware.battery.domain.DeviceCapabilityMatrix
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.ui.model.BatteryEvent
import com.synosoftware.battery.ui.model.HealthEvolutionUi
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import com.synosoftware.battery.ui.model.BatteryUiState
import java.text.SimpleDateFormat
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
        val activeSessionEntity = sessions.firstOrNull { it.status == SessionStatus.ACTIVE.name }
        val activeSession = activeSessionEntity?.toUi()
        val sessionMetrics = activeSessionEntity?.toMetrics()
        val decision = snapshot?.let { decisionEngine.analyze(it, sessionMetrics, preferences.targetChargePercent) }
        val sessionUi = sessions.map { it.toUi() }
        val healthEvolution = buildHealthEvolution(sessions)
        BatteryUiState(
            targetChargePercent = preferences.targetChargePercent,
            experimentalMetricsEnabled = preferences.experimentalMetricsEnabled,
            temperatureUnit = preferences.temperatureUnit,
            themeMode = preferences.themeMode,
            currentSnapshot = snapshot,
            decision = decision,
            activeSession = activeSession,
            sessions = sessionUi,
            healthEvolution = healthEvolution,
            usefulSessionCount = sessions.count { it.usefulForHealth },
            capabilities = DeviceCapabilityMatrix.defaultCapabilities(),
            healthMessage = buildHealthMessage(sessions.count { it.usefulForHealth }),
            batteryHealthVisible = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        BatteryUiState(
            capabilities = DeviceCapabilityMatrix.defaultCapabilities(),
            healthMessage = T("health_no_estimate_v0"),
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
            latestSnapshot.value?.let { snapshot ->
                val result = sessionRepository.recordSnapshot(snapshot, targetPercent)
                if (result.targetCrossed) {
                    val notifiedTarget = result.targetPercent ?: targetPercent
                    notificationManager.notifyTargetReached(notifiedTarget, snapshot.levelPercent)
                    _events.tryEmit(BatteryEvent.TargetReached(notifiedTarget))
                }
            }
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

    private fun buildHealthMessage(usefulSessionCount: Int) =
        if (usefulSessionCount < 5) {
            T("health_no_estimate_count", usefulSessionCount)
        } else {
            T("health_estimation_backlog")
        }

    private fun buildHealthEvolution(sessions: List<ChargeSessionEntity>): HealthEvolutionUi {
        val ordered = sessions
            .sortedBy { it.endedAtMs ?: it.lastSeenAtMs }
            .takeLast(10)

        if (ordered.isEmpty()) {
            return HealthEvolutionUi()
        }

        val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())

        val points = ordered.map { session ->
            val pointTimeMs = session.endedAtMs ?: session.lastSeenAtMs
            val measuredValue = session.currentLevelPercent.toFloat()
            val temperatureValue = (measuredValue - temperaturePenalty(session)).coerceIn(0f, 100f)
            val percentOnlyValue = (measuredValue - percentPenalty(session)).coerceIn(0f, 100f)

            HealthTrendPointUi(
                label = dateFormatter.format(Date(pointTimeMs)),
                measuredPercent = measuredValue.coerceIn(0f, 100f),
                temperatureEstimatePercent = temperatureValue,
                percentOnlyEstimatePercent = percentOnlyValue,
                isUsefulSession = session.usefulForHealth,
            )
        }

        return HealthEvolutionUi(points = points)
    }

    private fun temperaturePenalty(session: ChargeSessionEntity): Float {
        val maxTemperature = session.maxTemperatureC ?: session.averageTemperatureC ?: session.currentTemperatureC ?: 0f
        val base = when {
            maxTemperature >= 45f -> 9f
            maxTemperature >= 43f -> 7f
            maxTemperature >= 40f -> 4.5f
            maxTemperature >= 35f -> 1.5f
            else -> 0f
        }
        val dwellPenalty = (session.timeAbove85Sec / 1_800f) * 1.2f + (session.timeAbove90Sec / 900f) * 1.8f
        val qualityPenalty = if (session.usefulForHealth) -1f else 0.5f
        return base + dwellPenalty + qualityPenalty
    }

    private fun percentPenalty(session: ChargeSessionEntity): Float {
        val chargePenalty = when {
            session.currentLevelPercent >= 95 -> 8f
            session.currentLevelPercent >= 90 -> 6f
            session.currentLevelPercent >= 85 -> 4f
            session.currentLevelPercent >= 80 -> 2f
            else -> 0.5f
        }
        val durationPenalty = (session.sampleCount.coerceAtLeast(1) * 0.25f) + (session.gainPercent.coerceAtLeast(0) / 20f)
        return chargePenalty + durationPenalty
    }
}
