package com.synosoftware.battery.ui.model

import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.DeviceCapability
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.UiText

const val MIN_USEFUL_SESSION_COUNT = 5

enum class BatteryTab(
    val route: String,
    val titleKey: String,
    val navLabelKey: String,
    val iconRes: Int,
) {
    NOW("now", "navigation.now", "navigation.now", R.drawable.lucide_battery_charging),
    HEALTH("health", "health.title", "navigation.health", R.drawable.lucide_heart),
    LEDGER("sessions", "sessions.title", "navigation.sessions", R.drawable.lucide_history),
    HOW_IT_WORKS("info", "info.title", "navigation.info", R.drawable.lucide_info),
}

enum class HealthTrendState {
    COLLECTING,
    STABLE,
    DECLINING,
    NOISY,
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
    val estimatedCapacityMah: Float,
)

data class HealthEvolutionUi(
    val points: List<HealthTrendPointUi> = emptyList(),
) {
    val hasData: Boolean
        get() = points.isNotEmpty()
}

data class BatteryHealthEstimateUi(
    val estimatedCapacityMah: Int? = null,
    val likelyRangeMah: IntRange? = null,
    val confidence: ConfidenceLevel = ConfidenceLevel.LOW,
    val usefulSessionCount: Int = 0,
    val trend: HealthTrendState = HealthTrendState.COLLECTING,
) {
    val hasEstimate: Boolean
        get() = estimatedCapacityMah != null
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
    val healthEstimate: BatteryHealthEstimateUi = BatteryHealthEstimateUi(),
    val healthEvolution: HealthEvolutionUi = HealthEvolutionUi(),
    val capabilities: List<DeviceCapability> = emptyList(),
)

sealed interface BatteryEvent {
    data class TargetReached(val targetPercent: Int) : BatteryEvent
}
