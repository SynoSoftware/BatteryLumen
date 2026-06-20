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
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.HealthEvolutionUi
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import com.synosoftware.battery.ui.model.HealthTrendState
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
                state = BatteryUiState(),
                contentPadding = PaddingValues(),
            )
        }

        composeRule.onNodeWithText("Insufficient data").assertIsDisplayed()
        composeRule.onNodeWithText("Battery health needs 5 useful charging sessions before it can be estimated.").assertIsDisplayed()
        composeRule.onNodeWithText("0 of 5 useful sessions collected").assertIsDisplayed()
    }

    @Test
    fun healthScreenShowsEstimateSummaryAndTrend() {
        composeRule.setContent {
            HealthScreen(
                state = BatteryUiState(
                    healthEstimate = BatteryHealthEstimateUi(
                        estimatedCapacityMah = 3358,
                        likelyRangeMah = 3220..3490,
                        confidence = ConfidenceLevel.MEDIUM,
                        usefulSessionCount = 7,
                        trend = HealthTrendState.STABLE,
                    ),
                    healthEvolution = HealthEvolutionUi(
                        points = listOf(
                            HealthTrendPointUi(label = "Jun 11", estimatedCapacityMah = 3390f),
                            HealthTrendPointUi(label = "Jun 14", estimatedCapacityMah = 3360f),
                            HealthTrendPointUi(label = "Jun 17", estimatedCapacityMah = 3358f),
                        ),
                    ),
                ),
                contentPadding = PaddingValues(),
            )
        }

        composeRule.onNodeWithText("Battery Health").assertIsDisplayed()
        composeRule.onNodeWithText("Estimated capacity").assertIsDisplayed()
        composeRule.onNodeWithText("3,358 mAh").assertIsDisplayed()
        composeRule.onNodeWithText("Based on 7 useful sessions").assertIsDisplayed()
        composeRule.onNodeWithText("Battery health trend").assertIsDisplayed()
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
