package com.synosoftware.battery.data.session

import com.synosoftware.battery.R
import com.synosoftware.battery.data.formatDuration
import com.synosoftware.battery.data.formatTimeRange
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.SessionAssessment
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.i18n.TR
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
        timeHotAndAbove85Sec = timeHotAndAbove85Sec,
        timeVeryHotAndAbove90Sec = timeVeryHotAndAbove90Sec,
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
        headline = TR(R.string.session_headline_delta, TR(R.string.value_delta_percent, (currentLevelPercent - startLevelPercent).coerceAtLeast(0))),
        timeRange = TR(R.string.session_time_range, formatTimeRange(start, endedAtMs)),
        deltaLabel = TR(R.string.session_delta_label, startLevelPercent, currentLevelPercent),
        temperatureLabel = sessionTemperatureText(maxTemperatureC, averageTemperatureC, temperatureUnit),
        maxTemperatureC = maxTemperatureC,
        averageTemperatureC = averageTemperatureC,
        currentTemperatureC = currentTemperatureC,
        sourceLabel = sessionSourceText(source),
        qualityLabel = when {
            assessment.quality == SessionQuality.USEFUL -> TR(R.string.sessions_useful)
            status == SessionStatus.ACTIVE -> TR(R.string.sessions_active)
            status == SessionStatus.INCOMPLETE -> TR(R.string.sessions_incomplete)
            else -> TR(R.string.sessions_weak)
        },
        qualityEvidence = assessment.evidenceGrade,
        confidence = assessment.confidence,
        confidenceReason = assessment.reason,
        usefulForHealth = assessment.usefulForHealth,
        active = status == SessionStatus.ACTIVE,
        thermalStress = assessment.thermalStress,
        chargeLevelStress = assessment.chargeLevelStress,
        combinedStress = assessment.combinedStress,
        timeAbove85Label = TR(R.string.session_time_above_85, formatDuration(timeAbove85Sec * 1000L)),
        timeAbove90Label = TR(R.string.session_time_above_90, formatDuration(timeAbove90Sec * 1000L)),
    )
}
