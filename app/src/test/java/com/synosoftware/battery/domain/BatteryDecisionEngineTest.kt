package com.synosoftware.battery.domain

import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.TR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryDecisionEngineTest {
    private val engine = BatteryDecisionEngine()

    @Test
    fun hotChargingAboveTargetIsHighStressAndSuggestsCooling() {
        val snapshot = snapshot(
            levelPercent = 86,
            temperatureC = 42f,
            chargingState = ChargingState.CHARGING,
        )

        val decision = engine.analyze(snapshot, null, 85)

        assertEquals(StressLevel.HIGH_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_action_cool), decision.action)
    }

    @Test
    fun veryHotChargingIsSevereAndUnplugsNow() {
        val snapshot = snapshot(
            levelPercent = 91,
            temperatureC = 45f,
            chargingState = ChargingState.CHARGING,
        )

        val decision = engine.analyze(snapshot, null, 85)

        assertEquals(StressLevel.SEVERE_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_action_unplug_now), decision.action)
    }

    @Test
    fun fullCoolChargingStaysHighStressAndSuggestsUnpluggingWhenNotNeeded() {
        val snapshot = snapshot(
            levelPercent = 100,
            temperatureC = 31f,
            chargingState = ChargingState.CHARGING,
        )

        val decision = engine.analyze(snapshot, null, 85)

        assertEquals(StressLevel.HIGH_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_action_unplug_if_not_needed), decision.action)
    }

    @Test
    fun coolModerateChargingStaysNormalOrBetter() {
        val snapshot = snapshot(
            levelPercent = 55,
            temperatureC = 31f,
            chargingState = ChargingState.CHARGING,
        )

        val decision = engine.analyze(snapshot, null, 85)

        assertTrue(decision.stress == StressLevel.GOOD || decision.stress == StressLevel.NORMAL)
        assertEquals(TR(R.string.decision_action_continue), decision.action)
    }

    @Test
    fun longTimeNearFullRaisesStress() {
        val session = sessionMetrics(
            startedAtMs = 1_000L,
            lastSeenAtMs = 61 * 60_000L,
            startLevelPercent = 50,
            currentLevelPercent = 96,
            maxTemperatureC = 36f,
            averageTemperatureC = 35f,
            sampleCount = 6,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.CHARGING,
            status = SessionStatus.COMPLETED,
            timeAbove85Sec = 35 * 60L,
            timeAbove90Sec = 20 * 60L,
            timeAbove95Sec = 65 * 60L,
        )

        val assessment = engine.assessSession(session)

        assertEquals(SessionQuality.USEFUL, assessment.quality)
        assertEquals(StressLevel.HIGH_STRESS, assessment.chargeLevelStress)
        assertEquals(StressLevel.HIGH_STRESS, assessment.combinedStress)
    }

    @Test
    fun shortSessionIsStoredButMarkedWeak() {
        val session = sessionMetrics(
            startedAtMs = 1_000L,
            lastSeenAtMs = 4 * 60_000L,
            startLevelPercent = 40,
            currentLevelPercent = 45,
            maxTemperatureC = 33f,
            averageTemperatureC = 32f,
            sampleCount = 2,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.CHARGING,
            status = SessionStatus.COMPLETED,
        )

        val assessment = engine.assessSession(session)

        assertEquals(SessionQuality.WEAK, assessment.quality)
        assertTrue(!assessment.usefulForHealth)
    }

    @Test
    fun largeStableSessionIsUseful() {
        val session = sessionMetrics(
            startedAtMs = 1_000L,
            lastSeenAtMs = 46 * 60_000L,
            startLevelPercent = 30,
            currentLevelPercent = 70,
            maxTemperatureC = 34f,
            averageTemperatureC = 33f,
            sampleCount = 6,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.CHARGING,
            status = SessionStatus.COMPLETED,
        )

        val assessment = engine.assessSession(session)

        assertEquals(SessionQuality.USEFUL, assessment.quality)
        assertTrue(assessment.usefulForHealth)
        assertEquals(ConfidenceLevel.HIGH, assessment.confidence)
    }

    @Test
    fun hotChargingAtLowSocStaysHighStressNotSevere() {
        // Regression test: 44C alone (without high SOC) must not be escalated all the way
        // to SEVERE - the spec only allows that when temp>=43 AND soc>=90 happen together.
        val snapshot = snapshot(
            levelPercent = 20,
            temperatureC = 44f,
            chargingState = ChargingState.CHARGING,
        )

        val decision = engine.analyze(snapshot, null, 85)

        assertEquals(StressLevel.HIGH_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_action_cool), decision.action)
    }

    @Test
    fun veryHotNearFullIsSevereOnlyWithHighSoc() {
        val snapshot = snapshot(
            levelPercent = 92,
            temperatureC = 44f,
            chargingState = ChargingState.CHARGING,
        )

        val decision = engine.analyze(snapshot, null, 85)

        assertEquals(StressLevel.SEVERE_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_action_unplug_now), decision.action)
    }

    @Test
    fun sustainedHotAboveEightyFiveLedgerEscalatesToHighStress() {
        val snapshot = snapshot(
            levelPercent = 50,
            temperatureC = 25f,
            chargingState = ChargingState.CHARGING,
        )
        val session = sessionMetrics(
            startedAtMs = 1_000L,
            lastSeenAtMs = 700_000L,
            startLevelPercent = 50,
            currentLevelPercent = 50,
            maxTemperatureC = 30f,
            averageTemperatureC = 28f,
            sampleCount = 6,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.CHARGING,
            status = SessionStatus.ACTIVE,
            timeHotAndAbove85Sec = 700L,
        )

        val decision = engine.analyze(snapshot, session, 85)

        assertEquals(StressLevel.HIGH_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_reason_hot_above_85_long), decision.reason)
    }

    @Test
    fun sustainedVeryHotNearFullLedgerEscalatesToSevere() {
        val snapshot = snapshot(
            levelPercent = 50,
            temperatureC = 25f,
            chargingState = ChargingState.CHARGING,
        )
        val session = sessionMetrics(
            startedAtMs = 1_000L,
            lastSeenAtMs = 300_000L,
            startLevelPercent = 50,
            currentLevelPercent = 50,
            maxTemperatureC = 30f,
            averageTemperatureC = 28f,
            sampleCount = 6,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.CHARGING,
            status = SessionStatus.ACTIVE,
            timeVeryHotAndAbove90Sec = 200L,
        )

        val decision = engine.analyze(snapshot, session, 85)

        assertEquals(StressLevel.SEVERE_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_reason_very_hot_near_full_long), decision.reason)
    }

    @Test
    fun coldFastChargingIsHighStressWithWarmUpAction() {
        val snapshot = snapshot(
            levelPercent = 50,
            temperatureC = 5f,
            chargingState = ChargingState.CHARGING,
            currentUa = -3_000_000,
            chargeCounterUah = 2_000_000,
        )

        val decision = engine.analyze(snapshot, null, 85, estimatedCapacityMah = 3_000.0)

        assertEquals(StressLevel.HIGH_STRESS, decision.stress)
        assertEquals(TR(R.string.decision_reason_cold_fast_charge, "5.0"), decision.reason)
        assertEquals(TR(R.string.decision_action_slow_charge_cold), decision.action)
    }

    @Test
    fun timeToTargetFallsBackToHistoricalBucketsWhenNoActiveSession() {
        val snapshot = snapshot(
            levelPercent = 80,
            temperatureC = 32f,
            chargingState = ChargingState.CHARGING,
        )
        val buckets = listOf(
            ChargeRateBucket(
                band = SocBand(80, 85),
                plugType = ChargingSource.AC,
                tempBand = TempBand.NORMAL,
                medianPctPerMinute = 0.5,
                sampleCount = 5,
            ),
        )

        val decision = engine.analyze(snapshot, null, 85, historicalBuckets = buckets)

        assertEquals(10, decision.timeToTargetMinutes)
        assertEquals(ConfidenceLevel.MEDIUM, decision.timeToTargetConfidence)
    }

    @Test
    fun incompleteSessionIsMarkedIncomplete() {
        val session = sessionMetrics(
            startedAtMs = 1_000L,
            lastSeenAtMs = 8 * 60_000L,
            startLevelPercent = 50,
            currentLevelPercent = 54,
            maxTemperatureC = 34f,
            averageTemperatureC = 33f,
            sampleCount = 2,
            chargingSource = ChargingSource.AC,
            chargingState = ChargingState.DISCHARGING,
            status = SessionStatus.INCOMPLETE,
        )

        val assessment = engine.assessSession(session)

        assertEquals(SessionQuality.INCOMPLETE, assessment.quality)
        assertNotNull(assessment.reason)
    }

    private fun snapshot(
        levelPercent: Int,
        temperatureC: Float?,
        chargingState: ChargingState,
        currentUa: Int? = null,
        chargeCounterUah: Int? = null,
    ): BatterySnapshot {
        return BatterySnapshot(
            timestampMs = 1_000L,
            levelPercent = levelPercent,
            scale = 100,
            temperatureC = temperatureC,
            voltageMv = null,
            currentUa = currentUa,
            averageCurrentUa = currentUa,
            chargeCounterUah = chargeCounterUah,
            chargingSource = ChargingSource.AC,
            chargingState = chargingState,
            healthLabel = "good",
            technology = "Li-ion",
        )
    }

    private fun sessionMetrics(
        startedAtMs: Long,
        lastSeenAtMs: Long,
        startLevelPercent: Int,
        currentLevelPercent: Int,
        maxTemperatureC: Float?,
        averageTemperatureC: Float?,
        sampleCount: Int,
        chargingSource: ChargingSource,
        chargingState: ChargingState,
        status: SessionStatus,
        timeAbove85Sec: Long = 0L,
        timeAbove90Sec: Long = 0L,
        timeAbove35Sec: Long = 0L,
        timeAbove40Sec: Long = 0L,
        timeAbove43Sec: Long = 0L,
        timeAbove45Sec: Long = 0L,
        timeAbove80Sec: Long = 0L,
        timeAbove95Sec: Long = 0L,
        timeHotAndAbove85Sec: Long = 0L,
        timeVeryHotAndAbove90Sec: Long = 0L,
    ): ChargeSessionMetrics {
        return ChargeSessionMetrics(
            startedAtMs = startedAtMs,
            lastSeenAtMs = lastSeenAtMs,
            startLevelPercent = startLevelPercent,
            currentLevelPercent = currentLevelPercent,
            startChargeCounterUah = null,
            currentChargeCounterUah = null,
            maxTemperatureC = maxTemperatureC,
            averageTemperatureC = averageTemperatureC,
            sampleCount = sampleCount,
            chargingSource = chargingSource,
            chargingState = chargingState,
            status = status,
            usefulForHealth = false,
            quality = SessionQuality.INCOMPLETE,
            lastNotifiedTargetPercent = null,
            timeAbove85Sec = timeAbove85Sec,
            timeAbove90Sec = timeAbove90Sec,
            timeAbove35Sec = timeAbove35Sec,
            timeAbove40Sec = timeAbove40Sec,
            timeAbove43Sec = timeAbove43Sec,
            timeAbove45Sec = timeAbove45Sec,
            timeAbove80Sec = timeAbove80Sec,
            timeAbove95Sec = timeAbove95Sec,
            timeHotAndAbove85Sec = timeHotAndAbove85Sec,
            timeVeryHotAndAbove90Sec = timeVeryHotAndAbove90Sec,
        )
    }
}
