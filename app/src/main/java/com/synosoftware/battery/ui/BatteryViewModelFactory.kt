package com.synosoftware.battery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.synosoftware.battery.data.battery.BatteryMonitor
import com.synosoftware.battery.data.notification.ChargingNotificationManager
import com.synosoftware.battery.data.preferences.SettingsRepository
import com.synosoftware.battery.data.session.ChargeSessionRepository
import com.synosoftware.battery.domain.BatteryDecisionEngine

class BatteryViewModelFactory(
    private val batteryMonitor: BatteryMonitor,
    private val sessionRepository: ChargeSessionRepository,
    private val settingsRepository: SettingsRepository,
    private val decisionEngine: BatteryDecisionEngine,
    private val notificationManager: ChargingNotificationManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return create(modelClass)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatteryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BatteryViewModel(
                batteryMonitor = batteryMonitor,
                sessionRepository = sessionRepository,
                settingsRepository = settingsRepository,
                decisionEngine = decisionEngine,
                notificationManager = notificationManager,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
