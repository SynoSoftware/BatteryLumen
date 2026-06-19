package com.synosoftware.battery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synosoftware.battery.data.battery.BatteryMonitor
import com.synosoftware.battery.data.notification.ChargingNotificationManager
import com.synosoftware.battery.data.preferences.SettingsRepository
import com.synosoftware.battery.data.preferences.UserPreferences
import com.synosoftware.battery.data.session.ChargeSessionRepository
import com.synosoftware.battery.data.session.toMetrics
import com.synosoftware.battery.data.session.toUi
import com.synosoftware.battery.domain.BatteryDecisionEngine
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.DeviceCapabilityMatrix
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.i18n.text
import com.synosoftware.battery.ui.model.BatteryEvent
import com.synosoftware.battery.ui.model.BatteryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BatteryViewModel @Inject constructor(
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
        BatteryUiState(
            targetChargePercent = preferences.targetChargePercent,
            currentSnapshot = snapshot,
            decision = decision,
            activeSession = activeSession,
            sessions = sessionUi,
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
            healthMessage = text("health_no_estimate_v0"),
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

    private fun buildHealthMessage(usefulSessionCount: Int) =
        if (usefulSessionCount < 5) {
            text("health_no_estimate_count", usefulSessionCount)
        } else {
            text("health_estimation_backlog")
        }
}
