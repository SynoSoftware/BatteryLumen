package com.synosoftware.battery.data.session

import com.synosoftware.battery.data.formatDuration
import com.synosoftware.battery.data.formatTimeRange
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.SessionAssessment
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.ui.model.BatterySessionUi

fun ChargeSessionEntity.toMetrics(): ChargeSessionMetrics {
    return ChargeSessionMetrics(
        startedAtMs = startedAtMs,
        lastSeenAtMs = lastSeenAtMs,
        startLevelPercent = startLevelPercent,
        currentLevelPercent = currentLevelPercent,
        startChargeCounterUah = startChargeCounterUah,
        currentChargeCounterUah = currentChargeCounterUah,
        maxTemperatureC = maxTemperatureC,
        averageTemperatureC = averageTemperatureC,
        sampleCount = sampleCount,
        timeAbove85Sec = timeAbove85Sec,
        timeAbove90Sec = timeAbove90Sec,
        chargingSource = runCatching { ChargingSource.valueOf(chargingSource) }.getOrDefault(ChargingSource.UNKNOWN),
        chargingState = runCatching { com.synosoftware.battery.domain.ChargingState.valueOf(chargingState) }.getOrDefault(com.synosoftware.battery.domain.ChargingState.UNKNOWN),
        status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.INCOMPLETE),
        usefulForHealth = false,
        quality = SessionQuality.INCOMPLETE,
        lastNotifiedTargetPercent = lastNotifiedTargetPercent,
    )
}

fun ChargeSessionEntity.toUi(assessment: SessionAssessment): BatterySessionUi {
    val start = startedAtMs
    val status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.INCOMPLETE)
    return BatterySessionUi(
        id = id,
        headline = T("session_headline_delta", T("value_delta_percent", (currentLevelPercent - startLevelPercent).coerceAtLeast(0))),
        timeRange = T("session_time_range", formatTimeRange(start, endedAtMs)),
        deltaLabel = T("session_delta_label", startLevelPercent, currentLevelPercent),
        temperatureLabel = if (averageTemperatureC != null) {
            T(
                "session_temperature_with_average",
                maxTemperatureC?.let { String.format("%.1f", it) } ?: T("value_na"),
                String.format("%.1f", averageTemperatureC),
            )
        } else {
            T("session_temperature", maxTemperatureC?.let { String.format("%.1f", it) } ?: T("value_na"))
        },
        maxTemperatureC = maxTemperatureC,
        averageTemperatureC = averageTemperatureC,
        currentTemperatureC = currentTemperatureC,
        sourceLabel = T("session_source_${chargingSource.lowercase()}"),
        qualityLabel = when {
            assessment.quality == SessionQuality.USEFUL -> T("sessions.useful")
            status == SessionStatus.ACTIVE -> T("sessions.active")
            assessment.quality == SessionQuality.INCOMPLETE -> T("sessions.incomplete")
            else -> T("sessions.weak")
        },
        qualityEvidence = assessment.evidenceGrade,
        confidence = assessment.confidence,
        confidenceReason = assessment.reason,
        usefulForHealth = assessment.usefulForHealth,
        active = status == SessionStatus.ACTIVE,
        thermalStress = assessment.thermalStress,
        chargeLevelStress = assessment.chargeLevelStress,
        combinedStress = assessment.combinedStress,
        timeAbove85Label = T("session_time_above_85", formatDuration(timeAbove85Sec * 1000L)),
        timeAbove90Label = T("session_time_above_90", formatDuration(timeAbove90Sec * 1000L)),
    )
}
