package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.ui.model.BatteryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BatteryScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nowScreenShowsStressEvidenceAndTargetOptions() {
        var selectedTarget = 85

        composeRule.setContent {
            NowScreen(
                state = sampleNowState(),
                onTargetSelected = { selectedTarget = it },
                contentPadding = PaddingValues(),
            )
        }

        composeRule.onNodeWithText("Battery stress").assertIsDisplayed()
        composeRule.onNodeWithText("High stress").assertIsDisplayed()
        composeRule.onNodeWithText("Inferred · INFERRED").assertIsDisplayed()
        composeRule.onNodeWithText("90%").performClick()

        assertEquals(90, selectedTarget)
    }

    @Test
    fun healthScreenShowsEmptyStateWhenNoEstimateIsAvailable() {
        composeRule.setContent {
            HealthScreen(
                state = BatteryUiState(
                    healthMessage = "No battery-health estimate is shown in v0.",
                    usefulSessionCount = 0,
                ),
                contentPadding = PaddingValues(),
            )
        }

        composeRule.onNodeWithText("No battery-health estimate is shown in v0.").assertIsDisplayed()
        composeRule.onNodeWithText("Not enough useful sessions yet").assertIsDisplayed()
    }

    @Test
    fun howItWorksScreenShowsEvidenceLabelsAndCapabilityMatrix() {
        composeRule.setContent {
            HowItWorksScreen(
                state = BatteryUiState(),
                contentPadding = PaddingValues(),
            )
        }

        composeRule.onNodeWithText("Measured").assertIsDisplayed()
        composeRule.onNodeWithText("Estimated").assertIsDisplayed()
        composeRule.onNodeWithText("Capability matrix").assertIsDisplayed()
    }

    private fun sampleNowState(): BatteryUiState {
        return BatteryUiState(
            targetChargePercent = 85,
            currentSnapshot = BatterySnapshot(
                timestampMs = 1_000L,
                levelPercent = 86,
                scale = 100,
                temperatureC = 42f,
                voltageMv = 4100,
                currentUa = 980_000,
                averageCurrentUa = 950_000,
                chargeCounterUah = 2_800_000,
                chargingSource = ChargingSource.AC,
                chargingState = ChargingState.CHARGING,
                healthLabel = "good",
                technology = "Li-ion",
            ),
            decision = BatteryDecision(
                stress = StressLevel.HIGH_STRESS,
                thermalStress = StressLevel.HIGH_STRESS,
                chargeLevelStress = StressLevel.HIGH_STRESS,
                reason = "Battery is 42.0°C while charging.",
                action = "Unplug now or let the phone cool.",
                confidence = ConfidenceLevel.HIGH,
                confidenceReason = "temperature is direct; battery level is direct; current is available",
                evidenceGrade = EvidenceGrade.INFERRED,
                targetPercent = 85,
                bestStopPercent = 85,
                timeToTargetMinutes = 12,
                timeToFullMinutes = 42,
            ),
        )
    }
}
