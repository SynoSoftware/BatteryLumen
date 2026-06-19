package com.synosoftware.battery.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ChargingState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@Singleton
class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun snapshots(): Flow<BatterySnapshot> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                trySend(intent.toSnapshot())
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        sticky?.toSnapshot()?.let { trySend(it) }

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }.conflate()

    fun readSnapshot(): BatterySnapshot? {
        val stickyIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return stickyIntent?.toSnapshot()
    }

    private fun Intent.toSnapshot(): BatterySnapshot {
        val batteryManager = context.getSystemService(BatteryManager::class.java)
        val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val temperatureRaw = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val voltageRaw = getIntExtra(BatteryManager.EXTRA_VOLTAGE, Int.MIN_VALUE)
        val plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val healthRaw = getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)

        val currentUa = batteryManager.getIntPropertyOrNull(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val averageCurrentUa = batteryManager.getIntPropertyOrNull(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val chargeCounterUah = batteryManager.getIntPropertyOrNull(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

        return BatterySnapshot(
            timestampMs = System.currentTimeMillis(),
            levelPercent = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 0,
            scale = scale,
            temperatureC = temperatureRaw.takeUnless { it == Int.MIN_VALUE }?.div(10f),
            voltageMv = voltageRaw.takeUnless { it == Int.MIN_VALUE },
            currentUa = currentUa,
            averageCurrentUa = averageCurrentUa,
            chargeCounterUah = chargeCounterUah,
            chargingSource = plugged.toChargingSource(),
            chargingState = status.toChargingState(),
            healthLabel = healthRaw.toHealthLabel(),
            technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
        )
    }

    private fun BatteryManager.getIntPropertyOrNull(property: Int): Int? {
        val value = getIntProperty(property)
        return value.takeUnless { it == Int.MIN_VALUE }
    }

    private fun Int.toChargingSource(): ChargingSource {
        return when (this) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargingSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.WIRELESS
            BatteryManager.BATTERY_PLUGGED_DOCK -> ChargingSource.DOCK
            else -> ChargingSource.UNKNOWN
        }
    }

    private fun Int.toChargingState(): ChargingState {
        return when (this) {
            BatteryManager.BATTERY_STATUS_CHARGING -> ChargingState.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> ChargingState.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> ChargingState.FULL
            else -> ChargingState.UNKNOWN
        }
    }

    private fun Int.toHealthLabel(): String? {
        return when (this) {
            BatteryManager.BATTERY_HEALTH_COLD -> "cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> "good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "overheat"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "over-voltage"
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> null
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "failure"
            else -> null
        }
    }
}
