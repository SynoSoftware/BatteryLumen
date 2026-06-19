package com.synosoftware.battery.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class SessionReconciliationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as com.synosoftware.battery.BatteryApp
        val batteryMonitor = app.container.batteryMonitor
        val sessionRepository = app.container.chargeSessionRepository
        val settingsRepository = app.container.settingsRepository
        val notificationManager = app.container.notificationManager
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
