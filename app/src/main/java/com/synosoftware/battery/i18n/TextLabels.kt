package com.synosoftware.battery.i18n

import androidx.compose.runtime.Composable
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.ui.model.HealthTrendState
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.StressLevel

fun confidenceText(confidence: ConfidenceLevel): UiText {
    return when (confidence) {
        ConfidenceLevel.HIGH -> T("confidence.high")
        ConfidenceLevel.MEDIUM -> T("confidence.medium")
        ConfidenceLevel.LOW -> T("confidence.low")
    }
}

fun confidenceReasonText(confidence: ConfidenceLevel): UiText {
    return when (confidence) {
        ConfidenceLevel.HIGH -> T("confidence.reason.high")
        ConfidenceLevel.MEDIUM -> T("confidence.reason.medium")
        ConfidenceLevel.LOW -> T("confidence.reason.low")
    }
}

@Composable
fun healthTrendText(trend: HealthTrendState): UiText {
    return when (trend) {
        HealthTrendState.COLLECTING -> T("health.trend.collecting")
        HealthTrendState.STABLE -> T("health.trend.stable")
        HealthTrendState.DECLINING -> T("health.trend.declining")
        HealthTrendState.NOISY -> T("health.trend.noisy")
    }
}

@Composable
fun healthApproxPercentText(percent: Int): UiText {
    return T("health.approx.percent", percent)
}

@Composable
fun healthPercentRangeText(range: IntRange): UiText {
    return T(
        "health.likely.rangePercent",
        T("value.percent", range.first).asString(),
        T("value.percent", range.last).asString(),
    )
}

@Composable
fun healthCapacityRangeText(range: IntRange): UiText {
    return T(
        "health.likely.range",
        T("value.mah", range.first).asString(),
        T("value.mah", range.last).asString(),
    )
}

fun stressText(stress: StressLevel): UiText {
    return when (stress) {
        StressLevel.EXCELLENT -> T("stress.excellent")
        StressLevel.GOOD -> T("stress.good")
        StressLevel.NORMAL -> T("stress.normal")
        StressLevel.HIGH_STRESS -> T("stress.high")
        StressLevel.SEVERE_STRESS -> T("stress.severe")
    }
}

fun chargingSourceText(source: ChargingSource): UiText {
    return when (source) {
        ChargingSource.UNKNOWN -> T("charging.source.unknown")
        ChargingSource.USB -> T("charging.source.usb")
        ChargingSource.AC -> T("charging.source.ac")
        ChargingSource.WIRELESS -> T("charging.source.wireless")
        ChargingSource.DOCK -> T("charging.source.dock")
    }
}

fun chargingStateText(state: ChargingState): UiText {
    return when (state) {
        ChargingState.UNKNOWN -> T("charging.state.unknown")
        ChargingState.CHARGING -> T("charging.state.charging")
        ChargingState.DISCHARGING -> T("charging.state.discharging")
        ChargingState.FULL -> T("charging.state.full")
    }
}

fun sessionSourceText(source: ChargingSource): UiText {
    return when (source) {
        ChargingSource.UNKNOWN -> T("session.source.unknown")
        ChargingSource.USB -> T("session.source.usb")
        ChargingSource.AC -> T("session.source.ac")
        ChargingSource.WIRELESS -> T("session.source.wireless")
        ChargingSource.DOCK -> T("session.source.dock")
    }
}

fun sessionAssessmentText(quality: SessionQuality): UiText {
    return when (quality) {
        SessionQuality.USEFUL -> T("session.assessment.useful")
        SessionQuality.WEAK -> T("session.assessment.weak")
        SessionQuality.INCOMPLETE -> T("session.assessment.incomplete")
    }
}
