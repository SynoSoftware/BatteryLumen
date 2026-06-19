package com.synosoftware.battery.data.session

import androidx.room.withTransaction
import com.synosoftware.battery.domain.BatteryDecisionEngine
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ConfidenceLevel
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.SessionQuality
import com.synosoftware.battery.domain.SessionStatus
import com.synosoftware.battery.i18n.T

data class SessionProcessResult(
    val activeSession: ChargeSessionMetrics?,
    val completedSession: ChargeSessionMetrics?,
    val targetCrossed: Boolean,
    val targetPercent: Int?,
)

class ChargeSessionRepository(
    private val database: BatteryDatabase,
    private val dao: ChargeSessionDao,
    private val decisionEngine: BatteryDecisionEngine,
) {
    fun observeSessions() = dao.observeSessions()

    fun observeActiveSession() = dao.observeActiveSession()

    suspend fun recordSnapshot(
        snapshot: BatterySnapshot,
        targetPercent: Int,
    ): SessionProcessResult = database.withTransaction {
        val isChargingLike = snapshot.chargingState == ChargingState.CHARGING || snapshot.chargingState == ChargingState.FULL
        val existingActive = dao.getActiveSession()

        if (isChargingLike) {
            if (existingActive == null) {
                val baseEntity = baseSessionEntity(snapshot, targetPercent)
                val assessed = applyAssessment(baseEntity)
                val newId = dao.insert(assessed)
                val inserted = assessed.copy(id = newId)
                return@withTransaction SessionProcessResult(
                    activeSession = inserted.toMetrics(),
                    completedSession = null,
                    targetCrossed = snapshot.levelPercent >= targetPercent,
                    targetPercent = if (snapshot.levelPercent >= targetPercent) targetPercent else null,
                )
            }

            val updated = updateActiveSession(existingActive, snapshot, targetPercent)
            dao.update(updated.entity)
            val activeMetrics = updated.entity.toMetrics()
            val targetCrossed = updated.targetCrossed
            return@withTransaction SessionProcessResult(
                activeSession = activeMetrics,
                completedSession = null,
                targetCrossed = targetCrossed,
                targetPercent = if (targetCrossed) targetPercent else null,
            )
        }

        if (existingActive != null) {
            val finalized = finalizeSession(existingActive, snapshot)
            dao.update(finalized.entity)
            return@withTransaction SessionProcessResult(
                activeSession = null,
                completedSession = finalized.entity.toMetrics(),
                targetCrossed = false,
                targetPercent = null,
            )
        }

        SessionProcessResult(
            activeSession = null,
            completedSession = null,
            targetCrossed = false,
            targetPercent = null,
        )
    }

    private data class UpdateResult(
        val entity: ChargeSessionEntity,
        val targetCrossed: Boolean,
    )

    private fun baseSessionEntity(
        snapshot: BatterySnapshot,
        targetPercent: Int,
    ): ChargeSessionEntity {
        return ChargeSessionEntity(
            startedAtMs = snapshot.timestampMs,
            lastSeenAtMs = snapshot.timestampMs,
            endedAtMs = null,
            startLevelPercent = snapshot.levelPercent,
            currentLevelPercent = snapshot.levelPercent,
            startTemperatureC = snapshot.temperatureC,
            currentTemperatureC = snapshot.temperatureC,
            maxTemperatureC = snapshot.temperatureC,
            averageTemperatureC = snapshot.temperatureC,
            chargingSource = snapshot.chargingSource.name,
            chargingState = snapshot.chargingState.name,
            status = SessionStatus.ACTIVE.name,
            sampleCount = 1,
            timeAbove85Sec = 0L,
            timeAbove90Sec = 0L,
            lastNotifiedTargetPercent = if (snapshot.levelPercent >= targetPercent) targetPercent else null,
            gainPercent = 0,
            quality = SessionQuality.INCOMPLETE.name,
            usefulForHealth = false,
            evidenceGrade = EvidenceGrade.INFERRED.name,
            confidenceLevel = ConfidenceLevel.LOW.name,
            confidenceReason = T("session_started").key,
            thermalStress = "NORMAL",
            chargeLevelStress = "NORMAL",
            combinedStress = "NORMAL",
        )
    }

    private fun applyAssessment(entity: ChargeSessionEntity): ChargeSessionEntity {
        val assessment = decisionEngine.assessSession(entity.toMetrics())
        return entity.copy(
            quality = assessment.quality.name,
            usefulForHealth = assessment.usefulForHealth,
            evidenceGrade = assessment.evidenceGrade.name,
            confidenceLevel = assessment.confidence.name,
            confidenceReason = assessment.reason.key,
            thermalStress = assessment.thermalStress.name,
            chargeLevelStress = assessment.chargeLevelStress.name,
            combinedStress = assessment.combinedStress.name,
        )
    }

    private fun updateActiveSession(
        existing: ChargeSessionEntity,
        snapshot: BatterySnapshot,
        targetPercent: Int,
    ): UpdateResult {
        val deltaSec = ((snapshot.timestampMs - existing.lastSeenAtMs).coerceAtLeast(0L)) / 1000L
        val timeAbove85 = existing.timeAbove85Sec + if (existing.currentLevelPercent >= 85) deltaSec else 0L
        val timeAbove90 = existing.timeAbove90Sec + if (existing.currentLevelPercent >= 90) deltaSec else 0L
        val sampleCount = existing.sampleCount + 1
        val averageTemperature = when {
            existing.averageTemperatureC == null -> snapshot.temperatureC
            snapshot.temperatureC == null -> existing.averageTemperatureC
            else -> ((existing.averageTemperatureC * existing.sampleCount) + snapshot.temperatureC) / sampleCount
        }
        val newGain = (snapshot.levelPercent - existing.startLevelPercent).coerceAtLeast(0)
        val crossedTarget = snapshot.levelPercent >= targetPercent &&
            existing.lastNotifiedTargetPercent != targetPercent

        val provisionalEntity = existing.copy(
            lastSeenAtMs = snapshot.timestampMs,
            currentLevelPercent = snapshot.levelPercent,
            currentTemperatureC = snapshot.temperatureC,
            maxTemperatureC = maxOfNullable(existing.maxTemperatureC, snapshot.temperatureC),
            averageTemperatureC = averageTemperature,
            chargingSource = snapshot.chargingSource.name,
            chargingState = snapshot.chargingState.name,
            sampleCount = sampleCount,
            timeAbove85Sec = timeAbove85,
            timeAbove90Sec = timeAbove90,
            lastNotifiedTargetPercent = if (crossedTarget) targetPercent else existing.lastNotifiedTargetPercent,
            gainPercent = newGain,
        )

        val assessment = decisionEngine.assessSession(provisionalEntity.toMetrics())
        val finalEntity = provisionalEntity.copy(
            quality = assessment.quality.name,
            usefulForHealth = assessment.usefulForHealth,
            evidenceGrade = assessment.evidenceGrade.name,
            confidenceLevel = assessment.confidence.name,
            confidenceReason = assessment.reason.key,
            thermalStress = assessment.thermalStress.name,
            chargeLevelStress = assessment.chargeLevelStress.name,
            combinedStress = assessment.combinedStress.name,
        )

        return UpdateResult(entity = finalEntity, targetCrossed = crossedTarget)
    }

    private fun finalizeSession(
        existing: ChargeSessionEntity,
        snapshot: BatterySnapshot,
    ): UpdateResult {
        val updated = updateActiveSession(existing, snapshot, Int.MAX_VALUE).entity
        val assessment = decisionEngine.assessSession(updated.toMetrics())
        val finalEntity = updated.copy(
            endedAtMs = snapshot.timestampMs,
            chargingState = snapshot.chargingState.name,
            currentLevelPercent = snapshot.levelPercent,
            currentTemperatureC = snapshot.temperatureC,
            status = if (assessment.quality == SessionQuality.USEFUL) SessionStatus.COMPLETED.name else SessionStatus.INCOMPLETE.name,
            quality = assessment.quality.name,
            usefulForHealth = assessment.usefulForHealth,
            evidenceGrade = assessment.evidenceGrade.name,
            confidenceLevel = assessment.confidence.name,
            confidenceReason = assessment.reason.key,
            thermalStress = assessment.thermalStress.name,
            chargeLevelStress = assessment.chargeLevelStress.name,
            combinedStress = assessment.combinedStress.name,
        )
        return UpdateResult(entity = finalEntity, targetCrossed = false)
    }

    private fun ChargeSessionEntity.toMetrics(): ChargeSessionMetrics {
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
            chargingState = runCatching { ChargingState.valueOf(chargingState) }.getOrDefault(ChargingState.UNKNOWN),
            status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.INCOMPLETE),
            usefulForHealth = usefulForHealth,
            quality = runCatching { SessionQuality.valueOf(quality) }.getOrDefault(SessionQuality.WEAK),
            lastNotifiedTargetPercent = lastNotifiedTargetPercent,
        )
    }

    private fun maxOfNullable(first: Float?, second: Float?): Float? {
        return when {
            first == null -> second
            second == null -> first
            else -> maxOf(first, second)
        }
    }
}
