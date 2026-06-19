package com.synosoftware.battery.domain

import com.synosoftware.battery.i18n.UiText
import kotlinx.serialization.Serializable

@Serializable
enum class EvidenceGrade {
    MEASURED,
    ESTIMATED,
    INFERRED,
    EXPERIMENTAL,
}

@Serializable
enum class ConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
enum class StressLevel(val severity: Int) {
    EXCELLENT(0),
    GOOD(1),
    NORMAL(2),
    HIGH_STRESS(3),
    SEVERE_STRESS(4),
}

@Serializable
enum class ChargingSource {
    UNKNOWN,
    USB,
    AC,
    WIRELESS,
    DOCK,
}

@Serializable
enum class ChargingState {
    UNKNOWN,
    CHARGING,
    DISCHARGING,
    FULL,
}

@Serializable
enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    INCOMPLETE,
}

@Serializable
enum class SessionQuality {
    USEFUL,
    WEAK,
    INCOMPLETE,
}

@Serializable
data class BatterySnapshot(
    val timestampMs: Long,
    val levelPercent: Int,
    val scale: Int,
    val temperatureC: Float?,
    val voltageMv: Int?,
    val currentUa: Int?,
    val averageCurrentUa: Int?,
    val chargeCounterUah: Int?,
    val chargingSource: ChargingSource,
    val chargingState: ChargingState,
    val healthLabel: String?,
    val technology: String?,
)

@Serializable
data class DeviceCapability(
    val key: String,
    val label: UiText,
    val source: UiText,
    val unit: UiText,
    val availability: UiText,
    val reliabilityRule: UiText,
    val evidenceGrade: EvidenceGrade,
    val fallback: UiText,
)

@Serializable
data class ChargeSessionMetrics(
    val startedAtMs: Long,
    val lastSeenAtMs: Long,
    val startLevelPercent: Int,
    val currentLevelPercent: Int,
    val maxTemperatureC: Float?,
    val averageTemperatureC: Float?,
    val sampleCount: Int,
    val timeAbove85Sec: Long,
    val timeAbove90Sec: Long,
    val chargingSource: ChargingSource,
    val chargingState: ChargingState,
    val status: SessionStatus,
    val usefulForHealth: Boolean,
    val quality: SessionQuality,
    val lastNotifiedTargetPercent: Int?,
)

val ChargeSessionMetrics.durationMs: Long
    get() = (lastSeenAtMs - startedAtMs).coerceAtLeast(0L)

val ChargeSessionMetrics.durationMinutes: Double
    get() = durationMs / 60_000.0

val ChargeSessionMetrics.gainPercent: Int
    get() = (currentLevelPercent - startLevelPercent).coerceAtLeast(0)

@Serializable
data class SessionAssessment(
    val quality: SessionQuality,
    val confidence: ConfidenceLevel,
    val evidenceGrade: EvidenceGrade,
    val reason: UiText,
    val usefulForHealth: Boolean,
    val thermalStress: StressLevel,
    val chargeLevelStress: StressLevel,
    val combinedStress: StressLevel,
)

@Serializable
data class BatteryDecision(
    val stress: StressLevel,
    val thermalStress: StressLevel,
    val chargeLevelStress: StressLevel,
    val reason: UiText,
    val action: UiText,
    val confidence: ConfidenceLevel,
    val confidenceReason: UiText,
    val evidenceGrade: EvidenceGrade,
    val targetPercent: Int,
    val bestStopPercent: Int,
    val timeToTargetMinutes: Int?,
    val timeToFullMinutes: Int?,
)
