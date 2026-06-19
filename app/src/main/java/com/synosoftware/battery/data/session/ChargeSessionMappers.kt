package com.synosoftware.battery.data.session

import com.synosoftware.battery.data.formatDeltaPercent
import com.synosoftware.battery.data.formatDuration
import com.synosoftware.battery.data.formatTimeRange
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.text
import com.synosoftware.battery.ui.model.BatterySessionUi

fun ChargeSessionEntity.toMetrics(): ChargeSessionMetrics {
    return ChargeSessionMetrics(
        startedAtMs = startedAtMs,
        lastSeenAtMs = lastSeenAtMs,
        startLevelPercent = startLevelPercent,
        currentLevelPercent = currentLevelPercent,
        maxTemperatureC = maxTemperatureC,
        averageTemperatureC = averageTemperatureC,
        sampleCount = sampleCount,
        timeAbove85Sec = timeAbove85Sec,
        timeAbove90Sec = timeAbove90Sec,
        chargingSource = runCatching { ChargingSource.valueOf(chargingSource) }.getOrDefault(ChargingSource.UNKNOWN),
        chargingState = runCatching { com.synosoftware.battery.domain.ChargingState.valueOf(chargingState) }.getOrDefault(com.synosoftware.battery.domain.ChargingState.UNKNOWN),
        status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.INCOMPLETE),
        usefulForHealth = usefulForHealth,
        quality = runCatching { SessionQuality.valueOf(quality) }.getOrDefault(SessionQuality.WEAK),
        lastNotifiedTargetPercent = lastNotifiedTargetPercent,
    )
}

fun ChargeSessionEntity.toUi(): BatterySessionUi {
    val start = startedAtMs
    return BatterySessionUi(
        id = id,
        headline = text("session_headline_delta", formatDeltaPercent(startLevelPercent, currentLevelPercent)),
        timeRange = text("session_time_range", formatTimeRange(start, endedAtMs)),
        deltaLabel = text("session_delta_label", startLevelPercent, currentLevelPercent),
        temperatureLabel = if (averageTemperatureC != null) {
            text(
                "session_temperature_with_average",
                maxTemperatureC?.let { String.format("%.1f", it) } ?: "n/a",
                String.format("%.1f", averageTemperatureC),
            )
        } else {
            text("session_temperature", maxTemperatureC?.let { String.format("%.1f", it) } ?: "n/a")
        },
        sourceLabel = text("session_source_${chargingSource.lowercase()}"),
        qualityLabel = when {
            usefulForHealth -> text("session_quality_useful")
            status == SessionStatus.ACTIVE.name -> text("session_quality_active")
            quality == SessionQuality.INCOMPLETE.name -> text("session_quality_incomplete")
            else -> text("session_quality_weak")
        },
        qualityEvidence = runCatching { EvidenceGrade.valueOf(evidenceGrade) }.getOrDefault(EvidenceGrade.INFERRED),
        confidence = runCatching { ConfidenceLevel.valueOf(confidenceLevel) }.getOrDefault(ConfidenceLevel.LOW),
        confidenceReason = text(confidenceReason),
        usefulForHealth = usefulForHealth,
        active = status == SessionStatus.ACTIVE.name,
        thermalStress = runCatching { StressLevel.valueOf(thermalStress) }.getOrDefault(StressLevel.NORMAL),
        chargeLevelStress = runCatching { StressLevel.valueOf(chargeLevelStress) }.getOrDefault(StressLevel.NORMAL),
        combinedStress = runCatching { StressLevel.valueOf(combinedStress) }.getOrDefault(StressLevel.NORMAL),
        timeAbove85Label = text("session_time_above_85", formatDuration(timeAbove85Sec * 1000L)),
        timeAbove90Label = text("session_time_above_90", formatDuration(timeAbove90Sec * 1000L)),
    )
}
