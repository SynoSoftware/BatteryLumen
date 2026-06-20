package com.synosoftware.battery.domain

import kotlin.math.roundToInt

data class CapacityPoint(
    val timestampMs: Long,
    val estimatedFullCapacityMah: Double,
    val quality: DataQuality,
)

enum class DataQuality {
    USEFUL,
    WEAK,
    REJECTED,
}

enum class CapacityTrend {
    COLLECTING,
    STABLE,
    DECLINING,
    NOISY,
}

data class CapacityEstimate(
    val estimatedCapacityMah: Int?,
    val likelyRangeMah: IntRange?,
    val confidence: ConfidenceLevel,
    val usefulSessionCount: Int,
    val trend: CapacityTrend,
)

fun isCurrentReliable(snapshot: BatterySnapshot): Boolean {
    val currentUa = snapshot.currentUa
    val averageUa = snapshot.averageCurrentUa

    return when {
        currentUa == null && averageUa == null ->
            false

        currentUa == null || currentUa == 0 ->
            false

        snapshot.chargeCounterUah == null || snapshot.levelPercent <= 0 ->
            false

        else ->
            true
    }
}

fun buildCapacityPoints(sessions: List<ChargeSessionMetrics>): List<CapacityPoint> {
    return sessions.asSequence()
        .filter { it.status == SessionStatus.COMPLETED }
        .mapNotNull(::buildCapacityPoint)
        .sortedBy { it.timestampMs }
        .toList()
}

fun estimateCapacity(points: List<CapacityPoint>): CapacityEstimate {
    val useful = points
        .filter { it.quality == DataQuality.USEFUL }
        .map { it.estimatedFullCapacityMah }
        .filter { it.isFinite() && it > 0.0 }

    if (useful.size < MIN_USEFUL_CAPACITY_SESSION_COUNT) {
        return CapacityEstimate(
            estimatedCapacityMah = null,
            likelyRangeMah = null,
            confidence = ConfidenceLevel.LOW,
            usefulSessionCount = useful.size,
            trend = if (useful.isEmpty()) CapacityTrend.COLLECTING else CapacityTrend.NOISY,
        )
    }

    val sorted = useful.sorted()
    val median = sorted[sorted.size / 2]
    val low = sorted[(sorted.size * 0.25).toInt()]
    val high = sorted[(sorted.size * 0.75).toInt().coerceAtMost(sorted.lastIndex)]
    val spreadRatio = if (median > 0.0) (high - low) / median else Double.POSITIVE_INFINITY
    val confidence = when {
        useful.size >= 10 && spreadRatio <= 0.06 -> ConfidenceLevel.HIGH
        useful.size >= 5 && spreadRatio <= 0.12 -> ConfidenceLevel.MEDIUM
        else -> ConfidenceLevel.LOW
    }
    val trend = when {
        useful.size < 3 -> CapacityTrend.COLLECTING
        isDeclining(useful) -> CapacityTrend.DECLINING
        spreadRatio <= 0.10 -> CapacityTrend.STABLE
        else -> CapacityTrend.NOISY
    }

    return CapacityEstimate(
        estimatedCapacityMah = median.roundToInt(),
        likelyRangeMah = low.roundToInt()..high.roundToInt(),
        confidence = confidence,
        usefulSessionCount = useful.size,
        trend = trend,
    )
}

private fun buildCapacityPoint(session: ChargeSessionMetrics): CapacityPoint? {
    val startCounter = session.startChargeCounterUah ?: return null
    val currentCounter = session.currentChargeCounterUah ?: return null
    val deltaSocPct = session.gainPercent
    if (deltaSocPct < 30) return null

    val deltaUah = currentCounter - startCounter
    if (deltaUah <= 0) return null

    val estimatedFullCapacityMah = (deltaUah / (deltaSocPct / 100.0)) / 1000.0
    if (!estimatedFullCapacityMah.isFinite() || estimatedFullCapacityMah <= 0.0) {
        return null
    }

    val quality = when {
        estimatedFullCapacityMah !in PLAUSIBLE_CAPACITY_MAH ->
            DataQuality.REJECTED

        session.chargingSource == ChargingSource.WIRELESS ->
            DataQuality.WEAK

        session.sampleCount < 2 ->
            DataQuality.WEAK

        session.maxTemperatureC?.let { it >= 45f } == true ->
            DataQuality.WEAK

        session.averageTemperatureC?.let { it >= 43f } == true ->
            DataQuality.WEAK

        else ->
            DataQuality.USEFUL
    }

    return CapacityPoint(
        timestampMs = session.lastSeenAtMs,
        estimatedFullCapacityMah = estimatedFullCapacityMah,
        quality = quality,
    )
}

private fun isDeclining(capacities: List<Double>): Boolean {
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

private val PLAUSIBLE_CAPACITY_MAH = 1_000.0..20_000.0
const val MIN_USEFUL_CAPACITY_SESSION_COUNT = 5
