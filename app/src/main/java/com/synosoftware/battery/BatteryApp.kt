package com.synosoftware.battery

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.synosoftware.battery.data.work.SessionReconciliationWorker
import javax.inject.Inject
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BatteryApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        scheduleReconciliation()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()
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
