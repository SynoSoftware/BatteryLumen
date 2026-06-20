package com.synosoftware.battery.ui

import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import com.synosoftware.battery.ui.model.HealthTrendState
import com.synosoftware.battery.ui.model.MIN_USEFUL_SESSION_COUNT
import kotlin.math.roundToInt

internal fun buildHealthEstimate(
    points: List<HealthTrendPointUi>,
    designCapacityMah: Int?,
): BatteryHealthEstimateUi {
    val capacities = points.map { it.estimatedCapacityMah }.filter { it.isFinite() && it > 0f }
    if (capacities.size < MIN_USEFUL_SESSION_COUNT) {
        return BatteryHealthEstimateUi(
            estimatedCapacityMah = null,
            likelyRangeMah = null,
            healthPercent = null,
            healthRangePercent = null,
            confidence = ConfidenceLevel.LOW,
            usefulSessionCount = capacities.size,
            trend = if (capacities.isEmpty()) HealthTrendState.COLLECTING else HealthTrendState.NOISY,
        )
    }

    val sorted = capacities.sorted()
    val median = sorted[sorted.size / 2]
    val low = sorted[(sorted.size * 0.25f).toInt().coerceIn(0, sorted.lastIndex)]
    val high = sorted[(sorted.size * 0.75f).toInt().coerceIn(0, sorted.lastIndex)]
    val spreadRatio = if (median > 0f) (high - low) / median else Float.POSITIVE_INFINITY
    val confidence = when {
        capacities.size >= 10 && spreadRatio <= 0.06f -> ConfidenceLevel.HIGH
        capacities.size >= 5 && spreadRatio <= 0.12f -> ConfidenceLevel.MEDIUM
        else -> ConfidenceLevel.LOW
    }
    val trend = when {
        capacities.size < 3 -> HealthTrendState.COLLECTING
        isDeclining(capacities) -> HealthTrendState.DECLINING
        spreadRatio <= 0.10f -> HealthTrendState.STABLE
        else -> HealthTrendState.NOISY
    }

    val percentReference = designCapacityMah?.takeIf { it > 0 }?.toFloat()
    val healthPercent = percentReference?.let { reference ->
        (median / reference * 100f).roundToInt()
    }
    val healthRangePercent = percentReference?.let { reference ->
        (low / reference * 100f).roundToInt()..(high / reference * 100f).roundToInt()
    }

    return BatteryHealthEstimateUi(
        estimatedCapacityMah = median.roundToInt(),
        likelyRangeMah = low.roundToInt()..high.roundToInt(),
        healthPercent = healthPercent,
        healthRangePercent = healthRangePercent,
        confidence = confidence,
        usefulSessionCount = capacities.size,
        trend = trend,
    )
}

internal fun isDeclining(capacities: List<Float>): Boolean {
    if (capacities.size < 3) return false
    val firstHalf = capacities.take(capacities.size / 2)
    val secondHalf = capacities.takeLast(capacities.size / 2)
    if (firstHalf.isEmpty() || secondHalf.isEmpty()) return false
    val firstAverage = firstHalf.average()
    val secondAverage = secondHalf.average()
    if (firstAverage <= 0.0) return false
    val deltaRatio = (secondAverage - firstAverage) / firstAverage
    return deltaRatio < -0.04
}
