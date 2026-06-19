package com.synosoftware.battery.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.synosoftware.battery.data.battery.BatteryMonitor
import com.synosoftware.battery.data.notification.ChargingNotificationManager
import com.synosoftware.battery.data.preferences.SettingsRepository
import com.synosoftware.battery.data.session.ChargeSessionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SessionReconciliationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val batteryMonitor: BatteryMonitor,
    private val sessionRepository: ChargeSessionRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationManager: ChargingNotificationManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val snapshot = batteryMonitor.readSnapshot() ?: return Result.success()
        val preferences = settingsRepository.preferences.first()
        val result = sessionRepository.recordSnapshot(snapshot, preferences.targetChargePercent)
        if (result.targetCrossed) {
            val target = result.targetPercent ?: preferences.targetChargePercent
            notificationManager.notifyTargetReached(target, snapshot.levelPercent)
        }
        return Result.success()
    }
}
