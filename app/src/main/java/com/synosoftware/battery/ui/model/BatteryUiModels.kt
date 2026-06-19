package com.synosoftware.battery.ui.model

import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.DeviceCapability
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.UiText

enum class BatteryTab(val route: String, val label: String) {
    NOW("now", "battery_tab_now"),
    HEALTH("health", "battery_tab_health"),
    LEDGER("ledger", "battery_tab_ledger"),
    HOW_IT_WORKS("how_it_works", "battery_tab_how_it_works"),
}

data class BatterySessionUi(
    val id: Long,
    val headline: UiText,
    val timeRange: UiText,
    val deltaLabel: UiText,
    val temperatureLabel: UiText,
    val sourceLabel: UiText,
    val qualityLabel: UiText,
    val qualityEvidence: EvidenceGrade,
    val confidence: ConfidenceLevel,
    val confidenceReason: UiText,
    val usefulForHealth: Boolean,
    val active: Boolean,
    val thermalStress: StressLevel,
    val chargeLevelStress: StressLevel,
    val combinedStress: StressLevel,
    val timeAbove85Label: UiText,
    val timeAbove90Label: UiText,
)

data class BatteryUiState(
    val targetChargePercent: Int = 85,
    val currentSnapshot: BatterySnapshot? = null,
    val decision: BatteryDecision? = null,
    val activeSession: BatterySessionUi? = null,
    val sessions: List<BatterySessionUi> = emptyList(),
    val usefulSessionCount: Int = 0,
    val capabilities: List<DeviceCapability> = emptyList(),
    val healthMessage: UiText = UiText("health_no_estimate_v0"),
    val batteryHealthVisible: Boolean = false,
    val selectedTab: BatteryTab = BatteryTab.NOW,
)

sealed interface BatteryEvent {
    data class TargetReached(val targetPercent: Int) : BatteryEvent
    data class Message(val text: UiText) : BatteryEvent
}
