package com.synosoftware.battery.data.session

import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.SessionAssessment
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.TR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargeSessionMappersTest {
    @Test
    fun sessionUiUsesCurrentAssessmentAndIgnoresLegacyStorageFields() {
        val entity = ChargeSessionEntity(
            id = 42L,
            startedAtMs = 1_000L,
            lastSeenAtMs = 61_000L,
            endedAtMs = 61_000L,
            startLevelPercent = 20,
            currentLevelPercent = 65,
            startChargeCounterUah = 1_200_000,
            currentChargeCounterUah = 2_100_000,
            startTemperatureC = 30f,
            currentTemperatureC = 32f,
            maxTemperatureC = 33f,
            averageTemperatureC = 31f,
            chargingSource = "AC",
            chargingState = "CHARGING",
            status = "COMPLETED",
            sampleCount = 6,
            timeAbove85Sec = 0L,
            timeAbove90Sec = 0L,
            lastNotifiedTargetPercent = null,
            gainPercent = 45,
        )
        val assessment = SessionAssessment(
            quality = SessionQuality.USEFUL,
            confidence = ConfidenceLevel.HIGH,
            evidenceGrade = EvidenceGrade.EXPERIMENTAL,
            reason = TR(R.string.confidence_reason_high),
            usefulForHealth = true,
            thermalStress = StressLevel.GOOD,
            chargeLevelStress = StressLevel.NORMAL,
            combinedStress = StressLevel.NORMAL,
        )

        val ui = entity.toUi(assessment, TemperatureUnit.FAHRENHEIT)

        assertEquals(TR(R.string.sessions_useful), ui.qualityLabel)
        assertEquals(EvidenceGrade.EXPERIMENTAL, ui.qualityEvidence)
        assertEquals(ConfidenceLevel.HIGH, ui.confidence)
        assertEquals(TR(R.string.confidence_reason_high), ui.confidenceReason)
        assertEquals(sessionTemperatureText(33f, 31f, TemperatureUnit.FAHRENHEIT), ui.temperatureLabel)
        assertTrue(ui.usefulForHealth)
        assertEquals(StressLevel.GOOD, ui.thermalStress)
        assertEquals(StressLevel.NORMAL, ui.chargeLevelStress)
        assertEquals(StressLevel.NORMAL, ui.combinedStress)
    }
}
