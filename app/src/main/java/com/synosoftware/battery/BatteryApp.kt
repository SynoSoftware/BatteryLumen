package com.synosoftware.battery

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.synosoftware.battery.data.work.SessionReconciliationWorker

class BatteryApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        scheduleReconciliation()
    }

    private fun scheduleReconciliation() {
        val request = OneTimeWorkRequestBuilder<SessionReconciliationWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            RECONCILIATION_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val RECONCILIATION_WORK_NAME = "session_reconciliation"
    }
}
