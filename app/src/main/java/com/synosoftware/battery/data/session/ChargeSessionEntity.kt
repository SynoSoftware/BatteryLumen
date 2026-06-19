package com.synosoftware.battery.data.session

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charge_sessions")
data class ChargeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startedAtMs: Long,
    val lastSeenAtMs: Long,
    val endedAtMs: Long? = null,
    val startLevelPercent: Int,
    val currentLevelPercent: Int,
    val startTemperatureC: Float? = null,
    val currentTemperatureC: Float? = null,
    val maxTemperatureC: Float? = null,
    val averageTemperatureC: Float? = null,
    val chargingSource: String,
    val chargingState: String,
    val status: String,
    val sampleCount: Int,
    val timeAbove85Sec: Long,
    val timeAbove90Sec: Long,
    val lastNotifiedTargetPercent: Int? = null,
    val gainPercent: Int,
    val quality: String,
    val usefulForHealth: Boolean,
    val evidenceGrade: String,
    val confidenceLevel: String,
    val confidenceReason: String,
    val thermalStress: String,
    val chargeLevelStress: String,
    val combinedStress: String,
)

