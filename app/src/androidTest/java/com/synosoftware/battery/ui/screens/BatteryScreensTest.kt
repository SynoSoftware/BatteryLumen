package com.synosoftware.battery.ui.screens

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.resolveText
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.BatteryUiState
import com.synosoftware.battery.ui.model.DailyChargingSummaryUi
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(),
            )
        }

        composeRule.onNodeWithText(text("now.stress.label")).assertIsDisplayed()
        composeRule.onNodeWithText(text("stress.high")).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.summary.title")).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.approx.percent", 80)).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.capacity.reference", text("value.mah", 5000))).assertIsDisplayed()
        composeRule.onNodeWithText(text("daily.summary.title")).assertIsDisplayed()
        composeRule.onNodeWithText(text("now.details.title")).assertIsDisplayed()
        composeRule.onNodeWithText(text("now.details.show")).performClick()
        composeRule.onNodeWithText(text("decision.thermal.label")).assertIsDisplayed()
        composeRule.onNodeWithText(text("value.percent", 90)).performClick()

        assertEquals(90, selectedTarget)
    }

    @Test
    fun healthScreenShowsEmptyStateWhenNoEstimateIsAvailable() {
        composeRule.setContent {
            HealthScreen(
                state = BatteryUiState(),
                onSeedDemoData = {},
                contentPadding = androidx.compose.foundation.layout.PaddingValues(),
            )
        }

        composeRule.onNodeWithText(text("health.insufficient.title")).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.insufficient.body", 5)).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.sessions.collected", 0, 5)).assertIsDisplayed()
    }

    @Test
    fun healthScreenShowsEstimateSummaryAndTrend() {
        composeRule.setContent {
            HealthScreen(
                state = BatteryUiState(
                    designCapacityMah = 4000,
                    healthEstimate = BatteryHealthEstimateUi(
                        estimatedCapacityMah = 3358,
                        likelyRangeMah = 3220..3490,
                        healthPercent = 84,
                        healthRangePercent = 81..87,
                        confidence = ConfidenceLevel.MEDIUM,
                        usefulSessionCount = 7,
                        trend = HealthTrendState.STABLE,
                    ),
                    dailySummary = DailyChargingSummaryUi(
                        headline = T("daily.summary.good"),
                        detail = T("daily.summary.no.issue"),
                        confidence = ConfidenceLevel.HIGH,
                        sessionCount = 2,
                    ),
                    healthEvolution = HealthEvolutionUi(
                        points = listOf(
                            HealthTrendPointUi(label = "Jun 11", estimatedCapacityMah = 3390f),
                            HealthTrendPointUi(label = "Jun 14", estimatedCapacityMah = 3360f),
                            HealthTrendPointUi(label = "Jun 17", estimatedCapacityMah = 3358f),
                        ),
                    ),
                ),
                onSeedDemoData = {},
                contentPadding = androidx.compose.foundation.layout.PaddingValues(),
            )
        }

        composeRule.onNodeWithText(text("health.current.title")).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.estimated.health")).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.approx.percent", 84)).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.capacity.reference", text("value.mah", 4000))).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.based.on.sessions", 7)).assertIsDisplayed()
        composeRule.onNodeWithText(text("health.trend.title")).assertIsDisplayed()
    }

    @Test
    fun howItWorksScreenShowsEvidenceLabelsAndCapabilityMatrix() {
        composeRule.setContent {
            HowItWorksScreen(
                state = BatteryUiState(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(),
            )
        }

        composeRule.onNodeWithText(text("evidence.measured")).assertIsDisplayed()
        composeRule.onNodeWithText(text("evidence.estimated")).assertIsDisplayed()
        composeRule.onNodeWithText(text("info.capability.title")).assertIsDisplayed()
    }

    @Test
    fun notificationPermissionCardRefreshesOnResume() {
        val lifecycleOwner = TestLifecycleOwner()
        var permissionGranted = false

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                NotificationPermissionCard(permissionChecker = { permissionGranted })
            }
        }

        composeRule.onNodeWithText(text("notification.channel.charge.target.title")).assertIsDisplayed()

        composeRule.runOnIdle {
            permissionGranted = true
            lifecycleOwner.resume()
        }

        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(text("notification.channel.charge.target.title")).assertCountEquals(0)
    }

    private fun sampleNowState(): BatteryUiState {
        return BatteryUiState(
            targetChargePercent = 85,
            designCapacityMah = 5000,
            healthEstimate = BatteryHealthEstimateUi(
                estimatedCapacityMah = 4000,
                likelyRangeMah = 3900..4100,
                healthPercent = 80,
                healthRangePercent = 78..82,
                confidence = ConfidenceLevel.MEDIUM,
                usefulSessionCount = 7,
                trend = HealthTrendState.STABLE,
            ),
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
                reason = T("decision.reason.hot.charging", "42.0"),
                action = T("decision.action.cool"),
                confidence = ConfidenceLevel.HIGH,
                confidenceReason = T("confidence.reason.high"),
                evidenceGrade = EvidenceGrade.INFERRED,
                targetPercent = 85,
                bestStopPercent = 85,
                timeToTargetMinutes = 12,
                timeToFullMinutes = 42,
            ),
        )
    }

    private fun text(key: String, vararg args: Any?): String {
        return context.resolveText(T(key, *args))
    }

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this).apply {
            currentState = Lifecycle.State.CREATED
        }

        override val lifecycle: Lifecycle
            get() = registry

        fun resume() {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }
}
