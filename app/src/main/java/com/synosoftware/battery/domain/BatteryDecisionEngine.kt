package com.synosoftware.battery.domain

import kotlin.math.ceil
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.confidenceReasonText
import com.synosoftware.battery.i18n.sessionAssessmentText

class BatteryDecisionEngine {
    fun analyze(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        targetPercent: Int,
    ): BatteryDecision {
        val sessionAssessment = session?.let { assessSession(it) }
        val thermalStress = thermalStress(snapshot, session)
        val chargeStress = chargeLevelStress(snapshot, session)
        val combinedStress = maxBySeverity(thermalStress, chargeStress)
        val confidence = confidence(snapshot, session, sessionAssessment)
        val reason = buildReason(snapshot, thermalStress, chargeStress, session, targetPercent)
        val action = buildAction(snapshot, session, combinedStress, targetPercent)
        val confidenceReason = buildConfidenceReason(confidence)
        val timeToTarget = estimateMinutes(snapshot, session, targetPercent)
        val timeToFull = estimateMinutes(snapshot, session, 100)

        return BatteryDecision(
            stress = combinedStress,
            thermalStress = thermalStress,
            chargeLevelStress = chargeStress,
            reason = reason,
            action = action,
            confidence = confidence,
            confidenceReason = confidenceReason,
            evidenceGrade = EvidenceGrade.INFERRED,
            targetPercent = targetPercent,
            bestStopPercent = targetPercent,
            timeToTargetMinutes = timeToTarget,
            timeToFullMinutes = timeToFull,
        )
    }

    fun assessSession(session: ChargeSessionMetrics): SessionAssessment {
        val thermalStress = sessionThermalStress(session)
        val chargeStress = sessionChargeLevelStress(session)
        val combinedStress = maxBySeverity(thermalStress, chargeStress)
        val quality = classifySessionQuality(session)
        val confidence = when (quality) {
            SessionQuality.USEFUL -> ConfidenceLevel.HIGH
            SessionQuality.WEAK -> ConfidenceLevel.MEDIUM
            SessionQuality.INCOMPLETE -> ConfidenceLevel.LOW
        }
        val reason = when (quality) {
            SessionQuality.USEFUL -> sessionAssessmentText(SessionQuality.USEFUL)
            SessionQuality.WEAK -> sessionAssessmentText(SessionQuality.WEAK)
            SessionQuality.INCOMPLETE -> sessionAssessmentText(SessionQuality.INCOMPLETE)
        }
        return SessionAssessment(
            quality = quality,
            confidence = confidence,
            evidenceGrade = EvidenceGrade.INFERRED,
            reason = reason,
            usefulForHealth = quality == SessionQuality.USEFUL,
            thermalStress = thermalStress,
            chargeLevelStress = chargeStress,
            combinedStress = combinedStress,
        )
    }

    fun healthTrend(session: ChargeSessionMetrics): SessionHealthTrend {
        val measuredPercent = session.currentLevelPercent.toFloat().coerceIn(0f, 100f)
        val sessionAssessment = assessSession(session)
        val temperatureEstimatePercent = (measuredPercent - temperaturePenalty(session, sessionAssessment)).coerceIn(0f, 100f)
        val percentOnlyEstimatePercent = (measuredPercent - percentPenalty(session)).coerceIn(0f, 100f)
        return SessionHealthTrend(
            usefulForHealth = sessionAssessment.usefulForHealth,
            measuredPercent = measuredPercent,
            temperatureEstimatePercent = temperatureEstimatePercent,
            percentOnlyEstimatePercent = percentOnlyEstimatePercent,
        )
    }

    private fun thermalStress(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
    ): StressLevel {
        val temp = snapshot.temperatureC ?: session?.maxTemperatureC
        var stress = when {
            temp == null -> StressLevel.NORMAL
            temp < 35f -> StressLevel.GOOD
            temp < 40f -> StressLevel.NORMAL
            temp < 43f -> StressLevel.HIGH_STRESS
            temp < 45f -> StressLevel.HIGH_STRESS
            else -> StressLevel.SEVERE_STRESS
        }
        if (snapshot.chargingState == ChargingState.CHARGING && temp != null && temp >= 43f) {
            stress = stress.escalate()
        }
        return stress
    }

    private fun chargeLevelStress(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
    ): StressLevel {
        val level = snapshot.levelPercent
        var stress = when {
            level >= 95 -> StressLevel.HIGH_STRESS
            level >= 90 -> StressLevel.HIGH_STRESS
            level >= 85 -> StressLevel.HIGH_STRESS
            level >= 80 -> StressLevel.NORMAL
            level >= 50 -> StressLevel.GOOD
            else -> StressLevel.NORMAL
        }

        val highChargeMinutes = session?.timeAbove85Sec?.div(60) ?: 0L
        val veryHighChargeMinutes = session?.timeAbove90Sec?.div(60) ?: 0L

        if (snapshot.chargingState == ChargingState.CHARGING) {
            if (highChargeMinutes >= 30) {
                stress = stress.escalate()
            }
            if (veryHighChargeMinutes >= 15) {
                stress = stress.escalate()
            }
        }

        return stress
    }

    private fun sessionThermalStress(session: ChargeSessionMetrics): StressLevel {
        val temp = session.maxTemperatureC
        return when {
            temp == null -> StressLevel.NORMAL
            temp < 35f -> StressLevel.GOOD
            temp < 40f -> StressLevel.NORMAL
            temp < 43f -> StressLevel.HIGH_STRESS
            else -> StressLevel.SEVERE_STRESS
        }
    }

    private fun sessionChargeLevelStress(session: ChargeSessionMetrics): StressLevel {
        val level = session.currentLevelPercent
        var stress = when {
            level >= 95 -> StressLevel.HIGH_STRESS
            level >= 90 -> StressLevel.HIGH_STRESS
            level >= 85 -> StressLevel.HIGH_STRESS
            level >= 80 -> StressLevel.NORMAL
            level >= 50 -> StressLevel.GOOD
            else -> StressLevel.NORMAL
        }
        if (session.timeAbove85Sec >= 30 * 60) {
            stress = stress.escalate()
        }
        if (session.timeAbove90Sec >= 15 * 60) {
            stress = stress.escalate()
        }
        return stress
    }

    private fun classifySessionQuality(session: ChargeSessionMetrics): SessionQuality {
        if (session.status == SessionStatus.INCOMPLETE) {
            return SessionQuality.INCOMPLETE
        }
        val gain = session.gainPercent
        val durationMinutes = session.durationMinutes
        val maxTemp = session.maxTemperatureC ?: 0f
        return when {
            gain >= 30 &&
                durationMinutes >= 10.0 &&
                session.sampleCount >= 2 &&
                session.chargingSource != ChargingSource.WIRELESS &&
                maxTemp < 45f -> SessionQuality.USEFUL

            gain < 30 ||
                durationMinutes < 10.0 ||
                session.sampleCount < 2 ||
                session.chargingSource == ChargingSource.WIRELESS ||
                maxTemp >= 45f -> SessionQuality.WEAK

            else -> SessionQuality.WEAK
        }
    }

    private fun temperaturePenalty(
        session: ChargeSessionMetrics,
        sessionAssessment: SessionAssessment,
    ): Float {
        val maxTemperature = session.maxTemperatureC ?: session.averageTemperatureC ?: 0f
        val base = when {
            maxTemperature >= 45f -> 9f
            maxTemperature >= 43f -> 7f
            maxTemperature >= 40f -> 4.5f
            maxTemperature >= 35f -> 1.5f
            else -> 0f
        }
        val dwellPenalty = (session.timeAbove85Sec / 1_800f) * 1.2f + (session.timeAbove90Sec / 900f) * 1.8f
        val qualityPenalty = if (sessionAssessment.usefulForHealth) -1f else 0.5f
        return base + dwellPenalty + qualityPenalty
    }

    private fun percentPenalty(session: ChargeSessionMetrics): Float {
        val chargePenalty = when {
            session.currentLevelPercent >= 95 -> 8f
            session.currentLevelPercent >= 90 -> 6f
            session.currentLevelPercent >= 85 -> 4f
            session.currentLevelPercent >= 80 -> 2f
            else -> 0.5f
        }
        val durationPenalty = (session.sampleCount.coerceAtLeast(1) * 0.25f) + (session.gainPercent.coerceAtLeast(0) / 20f)
        return chargePenalty + durationPenalty
    }

    private fun confidence(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        sessionAssessment: SessionAssessment?,
    ): ConfidenceLevel {
        var score = 0
        if (snapshot.temperatureC != null) score += 1
        if (snapshot.levelPercent in 0..100) score += 1
        if (snapshot.voltageMv != null || snapshot.currentUa != null || snapshot.chargeCounterUah != null) {
            score += 1
        }
        if (snapshot.chargingState != ChargingState.UNKNOWN) score += 1
        if (session != null && session.sampleCount >= 2) score += 1
        if (sessionAssessment != null && sessionAssessment.usefulForHealth) score += 1

        return when {
            score >= 5 -> ConfidenceLevel.HIGH
            score >= 3 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }

    private fun buildReason(
        snapshot: BatterySnapshot,
        thermalStress: StressLevel,
        chargeStress: StressLevel,
        session: ChargeSessionMetrics?,
        targetPercent: Int,
    ): com.synosoftware.battery.i18n.UiText {
        val temperature = snapshot.temperatureC ?: session?.maxTemperatureC
        return when {
            temperature != null && temperature >= 43f ->
                T("decision.reason.hot.charging", temperature.roundOne())
            snapshot.levelPercent >= targetPercent && snapshot.chargingState == ChargingState.CHARGING ->
                T("decision.reason.at.target")
            chargeStress.severity >= StressLevel.HIGH_STRESS.severity && snapshot.chargingState == ChargingState.CHARGING ->
                T("decision.reason.near.full")
            thermalStress.severity >= StressLevel.HIGH_STRESS.severity ->
                T("decision.reason.hot.label")
            snapshot.chargingState == ChargingState.CHARGING ->
                T("decision.reason.reasonable")
            else ->
                T("decision.reason.not.charging")
        }
    }

    private fun buildAction(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        combinedStress: StressLevel,
        targetPercent: Int,
    ): com.synosoftware.battery.i18n.UiText {
        val temperature = snapshot.temperatureC ?: session?.maxTemperatureC
        return when {
            snapshot.chargingState != ChargingState.CHARGING ->
                T("decision.action.not.charging")
            combinedStress == StressLevel.SEVERE_STRESS ->
                T("decision.action.unplug.now")
            temperature != null && temperature >= 40f ->
                T("decision.action.cool")
            snapshot.levelPercent >= targetPercent && targetPercent < 100 ->
                T("decision.action.unplug.if.not.needed")
            combinedStress.severity >= StressLevel.HIGH_STRESS.severity ->
                T("decision.action.avoid.full")
            else ->
                T("decision.action.continue")
        }
    }

    private fun buildConfidenceReason(
        confidence: ConfidenceLevel,
    ): com.synosoftware.battery.i18n.UiText {
        return when (confidence) {
            ConfidenceLevel.HIGH -> confidenceReasonText(ConfidenceLevel.HIGH)
            ConfidenceLevel.MEDIUM -> confidenceReasonText(ConfidenceLevel.MEDIUM)
            ConfidenceLevel.LOW -> confidenceReasonText(ConfidenceLevel.LOW)
        }
    }

    private fun estimateMinutes(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        targetPercent: Int,
    ): Int? {
        if (snapshot.chargingState != ChargingState.CHARGING) {
            return null
        }
        if (snapshot.levelPercent >= targetPercent) {
            return 0
        }

        val sessionData = session ?: return null
        val gain = sessionData.gainPercent
        val durationMinutes = sessionData.durationMinutes
        if (gain < 5 || durationMinutes < 5.0) {
            return null
        }
        val rate = gain / durationMinutes
        if (rate <= 0.01) {
            return null
        }
        val remaining = targetPercent - snapshot.levelPercent
        return ceil(remaining / rate).toInt().coerceAtLeast(1)
    }

    private fun maxBySeverity(first: StressLevel, second: StressLevel): StressLevel {
        return if (first.severity >= second.severity) first else second
    }

    private fun StressLevel.escalate(): StressLevel {
        return when (this) {
            StressLevel.EXCELLENT -> StressLevel.GOOD
            StressLevel.GOOD -> StressLevel.NORMAL
            StressLevel.NORMAL -> StressLevel.HIGH_STRESS
            StressLevel.HIGH_STRESS -> StressLevel.SEVERE_STRESS
            StressLevel.SEVERE_STRESS -> StressLevel.SEVERE_STRESS
        }
    }

    private fun Float.roundOne(): String = String.format("%.1f", this)
}
