package com.synosoftware.battery.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargeRateModelTest {
    @Test
    fun buildsBucketFromThreeOverlappingSessionsOfSamePlugAndTempBand() {
        val sessions = listOf(
            completedSession(startLevelPercent = 78, currentLevelPercent = 88, durationMinutes = 20.0, avgTempC = 32f),
            completedSession(startLevelPercent = 79, currentLevelPercent = 87, durationMinutes = 20.0, avgTempC = 31f),
            completedSession(startLevelPercent = 80, currentLevelPercent = 90, durationMinutes = 20.0, avgTempC = 33f),
        )

        val buckets = buildChargeRateBuckets(sessions)
        val bucket = buckets.single { it.band == SocBand(80, 85) }

        assertEquals(ChargingSource.AC, bucket.plugType)
        assertEquals(TempBand.NORMAL, bucket.tempBand)
        assertEquals(3, bucket.sampleCount)
        assertEquals(0.5, bucket.medianPctPerMinute, 1e-9)
    }

    @Test
    fun dropsBucketsBelowMinimumSampleCount() {
        val sessions = listOf(
            completedSession(startLevelPercent = 78, currentLevelPercent = 86, durationMinutes = 16.0, avgTempC = 32f),
            completedSession(startLevelPercent = 79, currentLevelPercent = 87, durationMinutes = 20.0, avgTempC = 31f),
        )

        val buckets = buildChargeRateBuckets(sessions)

        assertTrue(buckets.none { it.band == SocBand(80, 85) })
    }

    @Test
    fun ignoresSessionsThatDoNotOverlapTheRequestedBand() {
        val sessions = listOf(
            completedSession(startLevelPercent = 10, currentLevelPercent = 40, durationMinutes = 30.0, avgTempC = 32f),
            completedSession(startLevelPercent = 12, currentLevelPercent = 45, durationMinutes = 33.0, avgTempC = 31f),
            completedSession(startLevelPercent = 15, currentLevelPercent = 50, durationMinutes = 35.0, avgTempC = 33f),
        )

        val buckets = buildChargeRateBuckets(sessions)

        assertTrue(buckets.none { it.band == SocBand(80, 85) })
    }

    @Test
    fun tempBandForCoversAllBoundaries() {
        assertEquals(TempBand.UNKNOWN, tempBandFor(null))
        assertEquals(TempBand.COOL, tempBandFor(20f))
        assertEquals(TempBand.NORMAL, tempBandFor(32f))
        assertEquals(TempBand.WARM, tempBandFor(37f))
        assertEquals(TempBand.HOT, tempBandFor(42f))
    }

    private fun completedSession(
        startLevelPercent: Int,
        currentLevelPercent: Int,
        durationMinutes: Double,
        avgTempC: Float,
    ): ChargeSessionMetrics {
        val startedAtMs = 0L
        val lastSeenAtMs = (durationMinutes * 60_000.0).toLong()
        return ChargeSessionMetrics(
            startedAtMs = startedAtMs,
            lastSeenAtMs = lastSeenAtMs,
            startLevelPercent = startLevelPercent,
            currentLevelPercent = currentLevelPercent,
            startChargeCounterUah = null,
            currentChargeCounterUah = null,
            maxTemperatureC = avgTempC,
            averageTemperatureC = avgTempC,
            sampleCount = 4,
            timeAbove85Sec = 0L,
            timeAbove90Sec = 0L,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.DISCHARGING,
            status = SessionStatus.COMPLETED,
            usefulForHealth = true,
            quality = SessionQuality.USEFUL,
            lastNotifiedTargetPercent = null,
        )
    }
}
