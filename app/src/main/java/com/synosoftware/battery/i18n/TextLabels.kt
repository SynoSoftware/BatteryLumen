package com.synosoftware.battery.i18n

import com.synosoftware.battery.R
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.ui.model.HealthTrendState

fun confidenceText(confidence: ConfidenceLevel): UiText = when (confidence) {
    ConfidenceLevel.HIGH -> TR(R.string.confidence_high)
    ConfidenceLevel.MEDIUM -> TR(R.string.confidence_medium)
    ConfidenceLevel.LOW -> TR(R.string.confidence_low)
}

fun confidenceReasonText(confidence: ConfidenceLevel): UiText = when (confidence) {
    ConfidenceLevel.HIGH -> TR(R.string.confidence_reason_high)
    ConfidenceLevel.MEDIUM -> TR(R.string.confidence_reason_medium)
    ConfidenceLevel.LOW -> TR(R.string.confidence_reason_low)
}

fun healthTrendText(trend: HealthTrendState): UiText = when (trend) {
    HealthTrendState.COLLECTING -> TR(R.string.health_trend_collecting)
    HealthTrendState.STABLE -> TR(R.string.health_trend_stable)
    HealthTrendState.DECLINING -> TR(R.string.health_trend_declining)
    HealthTrendState.NOISY -> TR(R.string.health_trend_noisy)
}

fun healthApproxPercentText(percent: Int): UiText = TR(R.string.health_approx_percent, percent)

fun healthPercentRangeText(range: IntRange): UiText = TR(
    R.string.health_likely_range_percent,
    TR(R.string.value_percent, range.first),
    TR(R.string.value_percent, range.last),
)

fun healthCapacityRangeText(range: IntRange): UiText = TR(
    R.string.health_likely_range,
    TR(R.string.value_mah, range.first),
    TR(R.string.value_mah, range.last),
)

fun stressText(stress: StressLevel): UiText = when (stress) {
    StressLevel.EXCELLENT -> TR(R.string.stress_excellent)
    StressLevel.GOOD -> TR(R.string.stress_good)
    StressLevel.NORMAL -> TR(R.string.stress_normal)
    StressLevel.HIGH_STRESS -> TR(R.string.stress_high)
    StressLevel.SEVERE_STRESS -> TR(R.string.stress_severe)
}

fun chargingSourceText(source: ChargingSource): UiText = when (source) {
    ChargingSource.UNKNOWN -> TR(R.string.charging_source_unknown)
    ChargingSource.USB -> TR(R.string.charging_source_usb)
    ChargingSource.AC -> TR(R.string.charging_source_ac)
    ChargingSource.WIRELESS -> TR(R.string.charging_source_wireless)
    ChargingSource.DOCK -> TR(R.string.charging_source_dock)
}

fun chargingStateText(state: ChargingState): UiText = when (state) {
    ChargingState.UNKNOWN -> TR(R.string.charging_state_unknown)
    ChargingState.CHARGING -> TR(R.string.charging_state_charging)
    ChargingState.DISCHARGING -> TR(R.string.charging_state_discharging)
    ChargingState.FULL -> TR(R.string.charging_state_full)
}

fun sessionSourceText(source: ChargingSource): UiText = when (source) {
    ChargingSource.UNKNOWN -> TR(R.string.session_source_unknown)
    ChargingSource.USB -> TR(R.string.session_source_usb)
    ChargingSource.AC -> TR(R.string.session_source_ac)
    ChargingSource.WIRELESS -> TR(R.string.session_source_wireless)
    ChargingSource.DOCK -> TR(R.string.session_source_dock)
}

fun sessionAssessmentText(quality: SessionQuality): UiText = when (quality) {
    SessionQuality.USEFUL -> TR(R.string.session_assessment_useful)
    SessionQuality.WEAK -> TR(R.string.session_assessment_weak)
    SessionQuality.INCOMPLETE -> TR(R.string.session_assessment_incomplete)
}
