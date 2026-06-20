package com.synosoftware.battery.domain

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
        assertEquals("decision_action_cool", decision.action.key)
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
        assertEquals("decision_action_unplug_if_not_needed", decision.action.key)
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
        assertEquals("decision_action_continue", decision.action.key)
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
    fun healthTrendUsesSessionAssessment() {
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

        val trend = engine.healthTrend(session)

        assertTrue(trend.usefulForHealth)
        assertTrue(trend.measuredPercent > trend.percentOnlyEstimatePercent)
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
    ): BatterySnapshot {
        return BatterySnapshot(
            timestampMs = 1_000L,
            levelPercent = levelPercent,
            scale = 100,
            temperatureC = temperatureC,
            voltageMv = null,
            currentUa = null,
            averageCurrentUa = null,
            chargeCounterUah = null,
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
            timeAbove85Sec = 0L,
            timeAbove90Sec = 0L,
            chargingSource = chargingSource,
            chargingState = chargingState,
            status = status,
            usefulForHealth = false,
            quality = SessionQuality.INCOMPLETE,
            lastNotifiedTargetPercent = null,
        )
    }
}
