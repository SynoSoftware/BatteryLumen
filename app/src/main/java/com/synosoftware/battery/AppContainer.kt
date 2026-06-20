package com.synosoftware.battery

import android.content.Context
import androidx.room.Room
import com.synosoftware.battery.data.battery.BatteryMonitor
import com.synosoftware.battery.data.notification.ChargingNotificationManager
import com.synosoftware.battery.data.preferences.SettingsRepository
import com.synosoftware.battery.data.session.BatteryDatabase
import com.synosoftware.battery.data.session.MIGRATION_1_2
import com.synosoftware.battery.data.session.MIGRATION_2_3
import com.synosoftware.battery.data.session.ChargeSessionRepository
import com.synosoftware.battery.domain.BatteryDecisionEngine
import com.synosoftware.battery.ui.BatteryViewModelFactory

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: BatteryDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            BatteryDatabase::class.java,
            "open_battery.db",
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    private val decisionEngine: BatteryDecisionEngine by lazy {
        BatteryDecisionEngine()
    }

    val batteryMonitor: BatteryMonitor by lazy {
        BatteryMonitor(appContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext)
    }

    val notificationManager: ChargingNotificationManager by lazy {
        ChargingNotificationManager(appContext)
    }

    val chargeSessionRepository: ChargeSessionRepository by lazy {
        ChargeSessionRepository(
            database = database,
            dao = database.chargeSessionDao(),
        )
    }

    val batteryViewModelFactory: BatteryViewModelFactory by lazy {
        BatteryViewModelFactory(
            batteryMonitor = batteryMonitor,
            sessionRepository = chargeSessionRepository,
            settingsRepository = settingsRepository,
            decisionEngine = decisionEngine,
            notificationManager = notificationManager,
        )
    }
}
