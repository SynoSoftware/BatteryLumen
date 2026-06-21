package com.synosoftware.battery.domain

import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.TR
import com.synosoftware.battery.i18n.UiText
import com.synosoftware.battery.i18n.confidenceReasonText
import com.synosoftware.battery.i18n.sessionAssessmentText
import java.util.Locale
import kotlin.math.ceil

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
        val confidenceReason = confidenceReasonText(confidence)
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
        val reason = sessionAssessmentText(quality)
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

    private fun thermalStress(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
    ): StressLevel {
        val temp = snapshot.temperatureC ?: session?.maxTemperatureC ?: session?.averageTemperatureC
        var stress = thermalSeverity(temp)

        if (session != null) {
            stress = maxBySeverity(stress, sessionThermalStress(session))
        }

        if (isChargingLike(snapshot) && temp != null && temp >= 43f) {
            stress = stress.escalate()
        }

        return stress
    }

    private fun chargeLevelStress(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
    ): StressLevel {
        var stress = chargeSeverity(snapshot.levelPercent, isChargingLike(snapshot))

        if (session != null) {
            stress = maxBySeverity(stress, sessionChargeLevelStress(session))
        }

        return stress
    }

    private fun thermalSeverity(tempC: Float?): StressLevel {
        return when {
            tempC == null -> StressLevel.GOOD
            tempC < 35f -> StressLevel.GOOD
            tempC < 40f -> StressLevel.NORMAL
            tempC < 45f -> StressLevel.HIGH_STRESS
            else -> StressLevel.SEVERE_STRESS
        }
    }

    private fun chargeSeverity(levelPercent: Int, chargingLike: Boolean): StressLevel {
        return if (chargingLike) {
            when {
                levelPercent < 80 -> StressLevel.GOOD
                levelPercent < 85 -> StressLevel.NORMAL
                levelPercent < 90 -> StressLevel.NORMAL
                levelPercent < 95 -> StressLevel.HIGH_STRESS
                else -> StressLevel.HIGH_STRESS
            }
        } else {
            when {
                levelPercent >= 95 -> StressLevel.NORMAL
                levelPercent >= 90 -> StressLevel.GOOD
                else -> StressLevel.GOOD
            }
        }
    }

    private fun sessionThermalStress(session: ChargeSessionMetrics): StressLevel {
        var stress = thermalSeverity(session.maxTemperatureC ?: session.averageTemperatureC)

        if (session.timeAbove45Sec >= 3 * 60) {
            return StressLevel.SEVERE_STRESS
        }
        if (session.timeAbove43Sec >= 10 * 60) {
            stress = maxBySeverity(stress, StressLevel.HIGH_STRESS)
        }
        if (session.timeAbove40Sec >= 30 * 60) {
            stress = maxBySeverity(stress, StressLevel.NORMAL)
        }
        if (session.timeAbove35Sec >= 60 * 60) {
            stress = maxBySeverity(stress, StressLevel.GOOD)
        }

        return stress
    }

    private fun sessionChargeLevelStress(session: ChargeSessionMetrics): StressLevel {
        var stress = when {
            session.currentLevelPercent >= 95 -> StressLevel.HIGH_STRESS
            session.currentLevelPercent >= 90 -> StressLevel.HIGH_STRESS
            session.currentLevelPercent >= 85 -> StressLevel.NORMAL
            session.currentLevelPercent >= 80 -> StressLevel.NORMAL
            else -> StressLevel.GOOD
        }

        if (session.timeAbove95Sec >= 60 * 60) {
            stress = maxBySeverity(stress, StressLevel.HIGH_STRESS)
        }
        if (session.timeAbove90Sec >= 15 * 60) {
            stress = maxBySeverity(stress, StressLevel.HIGH_STRESS)
        }
        if (session.timeAbove85Sec >= 30 * 60) {
            stress = maxBySeverity(stress, StressLevel.NORMAL)
        }
        if (session.timeAbove80Sec >= 60 * 60) {
            stress = maxBySeverity(stress, StressLevel.NORMAL)
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

    private fun confidence(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        sessionAssessment: SessionAssessment?,
    ): ConfidenceLevel {
        var score = 0
        if (snapshot.temperatureC != null) score += 1
        if (snapshot.levelPercent in 0..100) score += 1
        if (isChargingLike(snapshot)) score += 1
        if (isCurrentReliable(snapshot)) score += 1
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
    ): UiText {
        val temperature = snapshot.temperatureC ?: session?.maxTemperatureC ?: session?.averageTemperatureC
        return when {
            isChargingLike(snapshot) && temperature != null && temperature >= 45f ->
                TR(R.string.decision_reason_hot_charging, temperature.roundOne())

            isChargingLike(snapshot) && temperature != null && temperature >= 43f && snapshot.levelPercent >= 90 ->
                TR(R.string.decision_reason_hot_charging, temperature.roundOne())

            isChargingLike(snapshot) && temperature != null && temperature >= 40f && snapshot.levelPercent >= 85 ->
                TR(R.string.decision_reason_hot_charging, temperature.roundOne())

            session != null && session.timeAbove95Sec >= 60 * 60 ->
                TR(R.string.decision_reason_near_full)

            chargeStress.severity >= StressLevel.HIGH_STRESS.severity && isChargingLike(snapshot) ->
                TR(R.string.decision_reason_near_full)

            isChargingLike(snapshot) && snapshot.levelPercent >= targetPercent ->
                TR(R.string.decision_reason_at_target)

            thermalStress.severity >= StressLevel.HIGH_STRESS.severity ->
                TR(R.string.decision_reason_hot_label)

            isChargingLike(snapshot) ->
                TR(R.string.decision_reason_reasonable)

            else ->
                TR(R.string.decision_reason_not_charging)
        }
    }

    private fun buildAction(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        combinedStress: StressLevel,
        targetPercent: Int,
    ): UiText {
        val temperature = snapshot.temperatureC ?: session?.maxTemperatureC ?: session?.averageTemperatureC
        return when {
            !isChargingLike(snapshot) ->
                TR(R.string.decision_action_not_charging)

            combinedStress == StressLevel.SEVERE_STRESS ->
                TR(R.string.decision_action_unplug_now)

            temperature != null && temperature >= 45f ->
                TR(R.string.decision_action_unplug_now)

            temperature != null && temperature >= 40f ->
                TR(R.string.decision_action_cool)

            snapshot.levelPercent >= targetPercent && targetPercent < 100 ->
                TR(R.string.decision_action_unplug_if_not_needed)

            combinedStress.severity >= StressLevel.HIGH_STRESS.severity ->
                TR(R.string.decision_action_avoid_full)

            else ->
                TR(R.string.decision_action_continue)
        }
    }

    private fun estimateMinutes(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        targetPercent: Int,
    ): Int? {
        if (!isChargingLike(snapshot)) {
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

    private fun isChargingLike(snapshot: BatterySnapshot): Boolean {
        return snapshot.chargingState == ChargingState.CHARGING || snapshot.chargingState == ChargingState.FULL
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

    private fun Float.roundOne(): String = String.format(Locale.ROOT, "%.1f", this)

}
