package com.synosoftware.battery.domain

enum class TempBand {
    COOL,
    NORMAL,
    WARM,
    HOT,
    UNKNOWN,
}

fun tempBandFor(tempC: Float?): TempBand {
    return when {
        tempC == null -> TempBand.UNKNOWN
        tempC < 30f -> TempBand.COOL
        tempC < 35f -> TempBand.NORMAL
        tempC < 40f -> TempBand.WARM
        else -> TempBand.HOT
    }
}

data class SocBand(
    val fromInclusive: Int,
    val toExclusive: Int,
)

data class ChargeRateBucket(
    val band: SocBand,
    val plugType: ChargingSource,
    val tempBand: TempBand,
    val medianPctPerMinute: Double,
    val sampleCount: Int,
)

const val MIN_BUCKET_SAMPLE_COUNT = 3

// Finer-grained near the top of the curve, where charge rate actually changes the most.
private val SOC_BANDS = listOf(
    SocBand(0, 50),
    SocBand(50, 80),
    SocBand(80, 85),
    SocBand(85, 90),
    SocBand(90, 95),
    SocBand(95, 101),
)

private data class RateSample(
    val rate: Double,
    val plugType: ChargingSource,
    val tempBand: TempBand,
    val startLevelPercent: Int,
    val currentLevelPercent: Int,
)

/**
 * Builds median charge-rate buckets from completed sessions, segmented by SOC range,
 * plug type, and temperature band, so estimateTimeToTarget can fall back to a device's
 * own charging history instead of guessing.
 */
fun buildChargeRateBuckets(history: List<ChargeSessionMetrics>): List<ChargeRateBucket> {
    val samples = history
        .filter { it.status == SessionStatus.COMPLETED }
        .mapNotNull { session ->
            val duration = session.durationMinutes
            val gain = session.gainPercent
            if (duration < 5.0 || gain < 5) return@mapNotNull null
            val rate = gain / duration
            if (!rate.isFinite() || rate <= 0.0) return@mapNotNull null
            RateSample(
                rate = rate,
                plugType = session.chargingSource,
                tempBand = tempBandFor(session.averageTemperatureC ?: session.maxTemperatureC),
                startLevelPercent = session.startLevelPercent,
                currentLevelPercent = session.currentLevelPercent,
            )
        }

    val grouped = mutableMapOf<Triple<SocBand, ChargingSource, TempBand>, MutableList<Double>>()
    samples.forEach { sample ->
        SOC_BANDS.forEach { band ->
            val overlaps = sample.startLevelPercent < band.toExclusive && sample.currentLevelPercent > band.fromInclusive
            if (overlaps) {
                grouped.getOrPut(Triple(band, sample.plugType, sample.tempBand)) { mutableListOf() }.add(sample.rate)
            }
        }
    }

    return grouped.mapNotNull { (key, rates) ->
        if (rates.size < MIN_BUCKET_SAMPLE_COUNT) return@mapNotNull null
        val sorted = rates.sorted()
        ChargeRateBucket(
            band = key.first,
            plugType = key.second,
            tempBand = key.third,
            medianPctPerMinute = sorted[sorted.size / 2],
            sampleCount = rates.size,
        )
    }
}
