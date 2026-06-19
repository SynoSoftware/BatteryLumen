package com.synosoftware.battery.ui.model

import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.DeviceCapability
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.UiText

enum class BatteryTab(
    val route: String,
    val label: String,
    val iconRes: Int,
) {
    NOW("now", "battery_tab_now", R.drawable.lucide_battery_charging),
    HEALTH("health", "battery_tab_health", R.drawable.lucide_heart),
    LEDGER("ledger", "battery_tab_ledger", R.drawable.lucide_history),
    HOW_IT_WORKS("how_it_works", "battery_tab_how_it_works", R.drawable.lucide_info),
}

data class BatterySessionUi(
    val id: Long,
    val headline: UiText,
    val timeRange: UiText,
    val deltaLabel: UiText,
    val temperatureLabel: UiText,
    val maxTemperatureC: Float? = null,
    val averageTemperatureC: Float? = null,
    val currentTemperatureC: Float? = null,
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

data class HealthTrendPointUi(
    val label: String,
    val measuredPercent: Float,
    val temperatureEstimatePercent: Float,
    val percentOnlyEstimatePercent: Float,
    val isUsefulSession: Boolean,
)

data class HealthEvolutionUi(
    val points: List<HealthTrendPointUi> = emptyList(),
) {
    val hasData: Boolean
        get() = points.isNotEmpty()
}

data class BatteryUiState(
    val targetChargePercent: Int = 85,
    val experimentalMetricsEnabled: Boolean = false,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentSnapshot: BatterySnapshot? = null,
    val decision: BatteryDecision? = null,
    val activeSession: BatterySessionUi? = null,
    val sessions: List<BatterySessionUi> = emptyList(),
    val healthEvolution: HealthEvolutionUi = HealthEvolutionUi(),
    val usefulSessionCount: Int = 0,
    val capabilities: List<DeviceCapability> = emptyList(),
    val healthMessage: UiText = T("health_no_estimate_v0"),
    val batteryHealthVisible: Boolean = false,
    val selectedTab: BatteryTab = BatteryTab.NOW,
)

sealed interface BatteryEvent {
    data class TargetReached(val targetPercent: Int) : BatteryEvent
    data class Message(val text: UiText) : BatteryEvent
}
