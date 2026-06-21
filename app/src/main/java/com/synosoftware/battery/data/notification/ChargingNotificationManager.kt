package com.synosoftware.battery.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.T

class ChargingNotificationManager(
    private val context: Context,
) {
    private val channelId = "charging_target"

    @SuppressLint("MissingPermission")
    fun notifyTargetReached(targetPercent: Int, currentPercent: Int) {
        if (!hasNotificationPermission()) return
        ensureChannel()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.lucide_bell)
            .setContentTitle(context.T(R.string.target_reached_title, targetPercent))
            .setContentText(context.T(R.string.target_reached_body, currentPercent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(targetPercent, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId,
            context.T(R.string.notification_channel_charge_target_title),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.T(R.string.notification_channel_charge_target_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
