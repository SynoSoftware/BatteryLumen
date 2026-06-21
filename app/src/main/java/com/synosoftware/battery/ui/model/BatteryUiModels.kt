package com.synosoftware.battery.ui.model

import androidx.annotation.StringRes
import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.AppLanguage
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.DeviceCapability
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.TR
import com.synosoftware.battery.i18n.UiText

const val MIN_USEFUL_SESSION_COUNT = 5

enum class BatteryTab(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val navLabelRes: Int,
    val iconRes: Int,
) {
    NOW("now", R.string.navigation_now, R.string.navigation_now, R.drawable.lucide_battery_charging),
    HEALTH("health", R.string.health_title, R.string.navigation_health, R.drawable.lucide_heart),
    LEDGER("sessions", R.string.sessions_title, R.string.navigation_sessions, R.drawable.lucide_history),
    HOW_IT_WORKS("info", R.string.info_title, R.string.navigation_info, R.drawable.lucide_info),
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
    val healthPercent: Int? = null,
    val healthRangePercent: IntRange? = null,
    val confidence: ConfidenceLevel = ConfidenceLevel.LOW,
    val usefulSessionCount: Int = 0,
    val trend: HealthTrendState = HealthTrendState.COLLECTING,
) {
    val hasEstimate: Boolean
        get() = estimatedCapacityMah != null

    val hasHealthPercent: Boolean
        get() = healthPercent != null
}

data class DailyChargingSummaryUi(
    val headline: UiText = TR(R.string.daily_summary_collecting),
    val detail: UiText = TR(R.string.daily_summary_waiting),
    val advice: UiText? = null,
    val confidence: ConfidenceLevel = ConfidenceLevel.LOW,
    val evidenceGrade: EvidenceGrade = EvidenceGrade.INFERRED,
    val sessionCount: Int = 0,
) {
    val hasData: Boolean
        get() = sessionCount > 0
}

data class BatteryUiState(
    val targetChargePercent: Int = 85,
    val designCapacityMah: Int? = null,
    val experimentalMetricsEnabled: Boolean = false,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val currentSnapshot: BatterySnapshot? = null,
    val decision: BatteryDecision? = null,
    val activeSession: BatterySessionUi? = null,
    val sessions: List<BatterySessionUi> = emptyList(),
    val healthEstimate: BatteryHealthEstimateUi = BatteryHealthEstimateUi(),
    val healthEvolution: HealthEvolutionUi = HealthEvolutionUi(),
    val dailySummary: DailyChargingSummaryUi = DailyChargingSummaryUi(),
    val capabilities: List<DeviceCapability> = emptyList(),
)

sealed interface BatteryEvent {
    data class TargetReached(val targetPercent: Int) : BatteryEvent
}
