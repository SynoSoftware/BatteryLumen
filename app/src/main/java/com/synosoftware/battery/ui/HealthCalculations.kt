package com.synosoftware.battery.ui

import com.synosoftware.battery.domain.CapacityEstimate
import com.synosoftware.battery.domain.CapacityTrend
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.HealthTrendState
import kotlin.math.roundToInt

internal fun CapacityEstimate.toUi(designCapacityMah: Int?): BatteryHealthEstimateUi {
    val healthPercent = estimatedCapacityMah?.let { capacityMah ->
        designCapacityMah?.takeIf { it > 0 }?.let { designCapacity ->
            (capacityMah.toFloat() / designCapacity.toFloat() * 100f).roundToInt()
        }
    }
    val healthRangePercent = likelyRangeMah?.let { range ->
        designCapacityMah?.takeIf { it > 0 }?.let { designCapacity ->
            (range.first.toFloat() / designCapacity.toFloat() * 100f).roundToInt()..(range.last.toFloat() / designCapacity.toFloat() * 100f).roundToInt()
        }
    }

    return BatteryHealthEstimateUi(
        estimatedCapacityMah = estimatedCapacityMah,
        likelyRangeMah = likelyRangeMah,
        healthPercent = healthPercent,
        healthRangePercent = healthRangePercent,
        confidence = confidence,
        usefulSessionCount = usefulSessionCount,
        trend = trend.toUi(),
    )
}

private fun CapacityTrend.toUi(): HealthTrendState {
    return when (this) {
        CapacityTrend.COLLECTING -> HealthTrendState.COLLECTING
        CapacityTrend.STABLE -> HealthTrendState.STABLE
        CapacityTrend.DECLINING -> HealthTrendState.DECLINING
        CapacityTrend.NOISY -> HealthTrendState.NOISY
    }
}
