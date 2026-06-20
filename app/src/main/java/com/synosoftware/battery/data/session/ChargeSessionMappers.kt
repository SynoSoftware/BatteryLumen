package com.synosoftware.battery.data.session

import com.synosoftware.battery.data.formatDuration
import com.synosoftware.battery.data.formatTimeRange
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.SessionAssessment
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.sessionSourceText
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
        timeAbove35Sec = timeAbove35Sec,
        timeAbove40Sec = timeAbove40Sec,
        timeAbove43Sec = timeAbove43Sec,
        timeAbove45Sec = timeAbove45Sec,
        timeAbove80Sec = timeAbove80Sec,
        timeAbove95Sec = timeAbove95Sec,
    )
}

fun ChargeSessionEntity.toUi(
    assessment: SessionAssessment,
    temperatureUnit: TemperatureUnit,
): BatterySessionUi {
    val start = startedAtMs
    val status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.INCOMPLETE)
    val source = runCatching { ChargingSource.valueOf(chargingSource) }.getOrDefault(ChargingSource.UNKNOWN)
    return BatterySessionUi(
        id = id,
        headline = T("session.headline.delta", T("value.delta.percent", (currentLevelPercent - startLevelPercent).coerceAtLeast(0))),
        timeRange = T("session.time.range", formatTimeRange(start, endedAtMs)),
        deltaLabel = T("session.delta.label", startLevelPercent, currentLevelPercent),
        temperatureLabel = sessionTemperatureText(maxTemperatureC, averageTemperatureC, temperatureUnit),
        maxTemperatureC = maxTemperatureC,
        averageTemperatureC = averageTemperatureC,
        currentTemperatureC = currentTemperatureC,
        sourceLabel = sessionSourceText(source),
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
        timeAbove85Label = T("session.time.above.85", formatDuration(timeAbove85Sec * 1000L)),
        timeAbove90Label = T("session.time.above.90", formatDuration(timeAbove90Sec * 1000L)),
    )
}
