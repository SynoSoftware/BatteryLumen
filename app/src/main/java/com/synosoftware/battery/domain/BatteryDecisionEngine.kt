package com.synosoftware.battery.domain

import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.TR
import com.synosoftware.battery.i18n.UiText
import com.synosoftware.battery.i18n.confidenceReasonText
import com.synosoftware.battery.i18n.sessionAssessmentText
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil

private enum class ReasonKind {
    HOT_CHARGING_SEVERE,
    VERY_HOT_NEAR_FULL,
    HOT_ABOVE_85,
    WARM_NEAR_FULL,
    NEAR_FULL_LONG,
    HOT_ABOVE_85_LONG,
    VERY_HOT_NEAR_FULL_LONG,
    COLD_FAST_CHARGE,
    FAST_CHARGE_HEAT,
    FAST_CHARGE_NEAR_FULL,
}

private data class CurrentRiskAssessment(
    val severity: StressLevel,
    val reason: ReasonKind?,
)

private data class TimeEstimate(
    val minutes: Int?,
    val confidence: ConfidenceLevel,
)

class BatteryDecisionEngine {
    fun analyze(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        targetPercent: Int,
        estimatedCapacityMah: Double? = null,
        historicalBuckets: List<ChargeRateBucket> = emptyList(),
    ): BatteryDecision {
        val sessionAssessment = session?.let { assessSession(it) }
        val temperature = resolveTemperature(snapshot, session)
        val charging = isChargingLike(snapshot)

        val baseThermal = thermalSeverity(temperature)
        val baseCharge = chargeSeverity(snapshot.levelPercent, charging)
        val thermalStress = if (session != null) maxBySeverity(baseThermal, sessionThermalStress(session)) else baseThermal
        val chargeStress = if (session != null) maxBySeverity(baseCharge, sessionChargeLevelStress(session)) else baseCharge

        var combined = maxBySeverity(thermalStress, chargeStress)
        val reasons = mutableListOf<ReasonKind>()

        if (charging && temperature != null) {
            when {
                temperature >= 45f -> {
                    combined = StressLevel.SEVERE_STRESS
                    reasons += ReasonKind.HOT_CHARGING_SEVERE
                }
                temperature >= 43f && snapshot.levelPercent >= 90 -> {
                    combined = StressLevel.SEVERE_STRESS
                    reasons += ReasonKind.VERY_HOT_NEAR_FULL
                }
                temperature >= 40f && snapshot.levelPercent >= 85 -> {
                    combined = maxBySeverity(combined, StressLevel.HIGH_STRESS)
                    reasons += ReasonKind.HOT_ABOVE_85
                }
                temperature >= 35f && snapshot.levelPercent >= 90 -> {
                    combined = maxBySeverity(combined, StressLevel.HIGH_STRESS)
                    reasons += ReasonKind.WARM_NEAR_FULL
                }
            }
        }

        if (session != null) {
            if (session.timeAbove95Sec >= 60 * 60) {
                combined = maxBySeverity(combined, StressLevel.HIGH_STRESS)
                reasons += ReasonKind.NEAR_FULL_LONG
            }
            if (session.timeHotAndAbove85Sec >= 10 * 60) {
                combined = maxBySeverity(combined, StressLevel.HIGH_STRESS)
                reasons += ReasonKind.HOT_ABOVE_85_LONG
            }
            if (session.timeVeryHotAndAbove90Sec >= 3 * 60) {
                combined = StressLevel.SEVERE_STRESS
                reasons += ReasonKind.VERY_HOT_NEAR_FULL_LONG
            }
        }

        val currentUa = snapshot.currentUa
        var currentReason: ReasonKind? = null
        if (charging && currentUa != null && estimatedCapacityMah != null && isCurrentReliable(snapshot)) {
            val currentAssessment = currentRisk(currentUa, estimatedCapacityMah, temperature, snapshot.levelPercent)
            combined = maxBySeverity(combined, currentAssessment.severity)
            currentAssessment.reason?.let {
                currentReason = it
                reasons += it
            }
        }

        val confidence = confidence(snapshot, session, sessionAssessment)
        val mainReason = reasons.firstOrNull()
        val reason = mainReason?.let { reasonText(it, temperature) }
            ?: defaultReason(charging, temperature, thermalStress, chargeStress)
        val action = buildAction(snapshot, combined, temperature, targetPercent, currentReason)
        val confidenceReason = confidenceReasonText(confidence)
        val timeToTarget = estimateMinutes(snapshot, session, targetPercent, historicalBuckets)
        val timeToFull = estimateMinutes(snapshot, session, 100, historicalBuckets)

        return BatteryDecision(
            stress = combined,
            thermalStress = thermalStress,
            chargeLevelStress = chargeStress,
            reason = reason,
            action = action,
            confidence = confidence,
            confidenceReason = confidenceReason,
            evidenceGrade = EvidenceGrade.INFERRED,
            targetPercent = targetPercent,
            bestStopPercent = targetPercent,
            timeToTargetMinutes = timeToTarget.minutes,
            timeToTargetConfidence = timeToTarget.confidence,
            timeToFullMinutes = timeToFull.minutes,
            timeToFullConfidence = timeToFull.confidence,
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

    /**
     * Secondary risk factor from charge current: high current combined with a cold battery,
     * a cold battery near full, or sustained heat near full are when fast charging actually
     * matters (lithium-plating / heat risk), not the current alone.
     */
    private fun currentRisk(
        currentUa: Int,
        estimatedCapacityMah: Double,
        tempC: Float?,
        socPct: Int,
    ): CurrentRiskAssessment {
        val capacityAh = estimatedCapacityMah / 1000.0
        if (capacityAh <= 0.0) return CurrentRiskAssessment(StressLevel.GOOD, null)
        val amps = abs(currentUa) / 1_000_000.0
        val cRate = amps / capacityAh

        return when {
            tempC != null && tempC < 10f && cRate >= 0.5 ->
                CurrentRiskAssessment(StressLevel.HIGH_STRESS, ReasonKind.COLD_FAST_CHARGE)
            tempC != null && tempC < 15f && socPct >= 80 && cRate >= 0.5 ->
                CurrentRiskAssessment(StressLevel.HIGH_STRESS, ReasonKind.COLD_FAST_CHARGE)
            cRate >= 1.5 && tempC != null && tempC >= 35f ->
                CurrentRiskAssessment(StressLevel.HIGH_STRESS, ReasonKind.FAST_CHARGE_HEAT)
            cRate >= 1.0 && socPct >= 85 ->
                CurrentRiskAssessment(StressLevel.NORMAL, ReasonKind.FAST_CHARGE_NEAR_FULL)
            else ->
                CurrentRiskAssessment(StressLevel.GOOD, null)
        }
    }

    private fun reasonText(kind: ReasonKind, temperature: Float?): UiText {
        val temp = temperature?.roundOne() ?: ""
        return when (kind) {
            ReasonKind.HOT_CHARGING_SEVERE -> TR(R.string.decision_reason_hot_charging, temp)
            ReasonKind.VERY_HOT_NEAR_FULL -> TR(R.string.decision_reason_hot_charging, temp)
            ReasonKind.HOT_ABOVE_85 -> TR(R.string.decision_reason_hot_charging, temp)
            ReasonKind.WARM_NEAR_FULL -> TR(R.string.decision_reason_warm_near_full, temp)
            ReasonKind.NEAR_FULL_LONG -> TR(R.string.decision_reason_near_full)
            ReasonKind.HOT_ABOVE_85_LONG -> TR(R.string.decision_reason_hot_above_85_long)
            ReasonKind.VERY_HOT_NEAR_FULL_LONG -> TR(R.string.decision_reason_very_hot_near_full_long)
            ReasonKind.COLD_FAST_CHARGE -> TR(R.string.decision_reason_cold_fast_charge, temp)
            ReasonKind.FAST_CHARGE_HEAT -> TR(R.string.decision_reason_fast_charge_heat, temp)
            ReasonKind.FAST_CHARGE_NEAR_FULL -> TR(R.string.decision_reason_fast_charge_near_full)
        }
    }

    private fun defaultReason(
        charging: Boolean,
        temperature: Float?,
        thermalStress: StressLevel,
        chargeStress: StressLevel,
    ): UiText {
        return when {
            !charging -> TR(R.string.decision_reason_not_charging)
            temperature == null -> TR(R.string.decision_reason_temperature_unavailable)
            thermalStress.severity > chargeStress.severity -> TR(R.string.decision_reason_temperature_dominant)
            chargeStress.severity > thermalStress.severity -> TR(R.string.decision_reason_charge_dominant)
            else -> TR(R.string.decision_reason_reasonable)
        }
    }

    private fun buildAction(
        snapshot: BatterySnapshot,
        combinedStress: StressLevel,
        temperature: Float?,
        targetPercent: Int,
        currentReason: ReasonKind?,
    ): UiText {
        if (!isChargingLike(snapshot)) {
            return TR(R.string.decision_action_not_charging)
        }

        return when (combinedStress) {
            StressLevel.SEVERE_STRESS ->
                TR(R.string.decision_action_unplug_now)

            StressLevel.HIGH_STRESS ->
                when {
                    currentReason == ReasonKind.COLD_FAST_CHARGE ->
                        TR(R.string.decision_action_slow_charge_cold)
                    temperature != null && temperature >= 40f ->
                        TR(R.string.decision_action_cool)
                    snapshot.levelPercent >= targetPercent ->
                        TR(R.string.decision_action_unplug_if_not_needed)
                    else ->
                        TR(R.string.decision_action_avoid_full)
                }

            StressLevel.NORMAL ->
                if (snapshot.levelPercent >= targetPercent) {
                    TR(R.string.decision_action_unplug_if_not_needed)
                } else {
                    TR(R.string.decision_action_continue)
                }

            else ->
                TR(R.string.decision_action_continue)
        }
    }

    private fun estimateMinutes(
        snapshot: BatterySnapshot,
        session: ChargeSessionMetrics?,
        targetPercent: Int,
        historicalBuckets: List<ChargeRateBucket>,
    ): TimeEstimate {
        if (!isChargingLike(snapshot)) {
            return TimeEstimate(null, ConfidenceLevel.LOW)
        }
        if (snapshot.levelPercent >= targetPercent) {
            return TimeEstimate(0, ConfidenceLevel.HIGH)
        }

        val sessionRate = session?.let {
            val gain = it.gainPercent
            val durationMinutes = it.durationMinutes
            if (gain < 5 || durationMinutes < 5.0) return@let null
            (gain / durationMinutes).takeIf { rate -> rate > 0.01 }
        }

        val plugType = snapshot.chargingSource
        val tempBand = tempBandFor(snapshot.temperatureC ?: session?.maxTemperatureC ?: session?.averageTemperatureC)

        var minutes = 0.0
        var usedHistorical = false

        for (soc in snapshot.levelPercent until targetPercent) {
            val historicalRate = historicalBuckets
                .filter {
                    soc >= it.band.fromInclusive &&
                        soc < it.band.toExclusive &&
                        it.plugType == plugType &&
                        it.tempBand == tempBand
                }
                .maxByOrNull { it.sampleCount }
                ?.medianPctPerMinute

            val rate = sessionRate ?: historicalRate ?: return TimeEstimate(null, ConfidenceLevel.LOW)
            if (sessionRate == null) usedHistorical = true
            minutes += 1.0 / rate
        }

        val confidence = if (usedHistorical) ConfidenceLevel.MEDIUM else ConfidenceLevel.HIGH
        return TimeEstimate(ceil(minutes).toInt().coerceAtLeast(1), confidence)
    }

    private fun resolveTemperature(snapshot: BatterySnapshot, session: ChargeSessionMetrics?): Float? {
        return snapshot.temperatureC ?: session?.maxTemperatureC ?: session?.averageTemperatureC
    }

    private fun isChargingLike(snapshot: BatterySnapshot): Boolean {
        return snapshot.chargingState == ChargingState.CHARGING || snapshot.chargingState == ChargingState.FULL
    }

    private fun maxBySeverity(first: StressLevel, second: StressLevel): StressLevel {
        return if (first.severity >= second.severity) first else second
    }

    private fun Float.roundOne(): String = String.format(Locale.ROOT, "%.1f", this)
}
