package com.synosoftware.battery.data.session

import androidx.room.withTransaction
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargeSessionMetrics
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.SessionStatus
import kotlin.random.Random

data class SessionProcessResult(
    val activeSession: ChargeSessionMetrics?,
    val targetCrossed: Boolean,
    val targetPercent: Int?,
)

class ChargeSessionRepository(
    private val database: BatteryDatabase,
    private val dao: ChargeSessionDao,
) {
    fun observeSessions() = dao.observeSessions()

    suspend fun seedDebugSessions() = database.withTransaction {
        val sessions = buildDebugSessions()
        dao.deleteAll()
        dao.insertAll(sessions)
    }

    suspend fun recordSnapshot(
        snapshot: BatterySnapshot,
        targetPercent: Int,
    ): SessionProcessResult = database.withTransaction {
        val isChargingLike = snapshot.chargingState == ChargingState.CHARGING || snapshot.chargingState == ChargingState.FULL
        val existingActive = dao.getActiveSession()
        val staleActive = existingActive?.takeIf { snapshot.timestampMs - it.lastSeenAtMs > STALE_ACTIVE_SESSION_MS }

        if (staleActive != null) {
            dao.update(closeStaleSession(staleActive))

            if (!isChargingLike) {
                return@withTransaction SessionProcessResult(
                    activeSession = null,
                    targetCrossed = false,
                    targetPercent = null,
                )
            }

            val fresh = baseSessionEntity(snapshot, targetPercent)
            val newId = dao.insert(fresh)
            val inserted = fresh.copy(id = newId)
            return@withTransaction SessionProcessResult(
                activeSession = inserted.toMetrics(),
                targetCrossed = snapshot.levelPercent >= targetPercent,
                targetPercent = if (snapshot.levelPercent >= targetPercent) targetPercent else null,
            )
        }

        if (isChargingLike) {
            if (existingActive == null) {
                val baseEntity = baseSessionEntity(snapshot, targetPercent)
                val newId = dao.insert(baseEntity)
                val inserted = baseEntity.copy(id = newId)
                return@withTransaction SessionProcessResult(
                    activeSession = inserted.toMetrics(),
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
                targetCrossed = targetCrossed,
                targetPercent = if (targetCrossed) targetPercent else null,
            )
        }

        if (existingActive != null) {
            val finalized = finalizeSession(existingActive, snapshot)
            dao.update(finalized.entity)
            return@withTransaction SessionProcessResult(
                activeSession = null,
                targetCrossed = false,
                targetPercent = null,
            )
        }

        SessionProcessResult(
            activeSession = null,
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
            startChargeCounterUah = snapshot.chargeCounterUah,
            currentChargeCounterUah = snapshot.chargeCounterUah,
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
            timeAbove35Sec = 0L,
            timeAbove40Sec = 0L,
            timeAbove43Sec = 0L,
            timeAbove45Sec = 0L,
            timeAbove80Sec = 0L,
            timeAbove95Sec = 0L,
            timeHotAndAbove85Sec = 0L,
            timeVeryHotAndAbove90Sec = 0L,
        )
    }

    private fun updateActiveSession(
        existing: ChargeSessionEntity,
        snapshot: BatterySnapshot,
        targetPercent: Int,
    ): UpdateResult {
        val deltaSec = ((snapshot.timestampMs - existing.lastSeenAtMs).coerceAtLeast(0L)) / 1000L
        val exposure = accumulateExposure(existing, deltaSec)
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
            currentChargeCounterUah = snapshot.chargeCounterUah ?: existing.currentChargeCounterUah,
            currentTemperatureC = snapshot.temperatureC,
            maxTemperatureC = maxOfNullable(existing.maxTemperatureC, snapshot.temperatureC),
            averageTemperatureC = averageTemperature,
            chargingSource = snapshot.chargingSource.name,
            chargingState = snapshot.chargingState.name,
            sampleCount = sampleCount,
            timeAbove35Sec = exposure.timeAbove35Sec,
            timeAbove40Sec = exposure.timeAbove40Sec,
            timeAbove43Sec = exposure.timeAbove43Sec,
            timeAbove45Sec = exposure.timeAbove45Sec,
            timeAbove80Sec = exposure.timeAbove80Sec,
            timeAbove85Sec = exposure.timeAbove85Sec,
            timeAbove90Sec = exposure.timeAbove90Sec,
            timeAbove95Sec = exposure.timeAbove95Sec,
            timeHotAndAbove85Sec = exposure.timeHotAndAbove85Sec,
            timeVeryHotAndAbove90Sec = exposure.timeVeryHotAndAbove90Sec,
            lastNotifiedTargetPercent = if (crossedTarget) targetPercent else existing.lastNotifiedTargetPercent,
            gainPercent = newGain,
        )

        return UpdateResult(entity = provisionalEntity, targetCrossed = crossedTarget)
    }

    private fun finalizeSession(
        existing: ChargeSessionEntity,
        snapshot: BatterySnapshot,
    ): UpdateResult {
        val updated = updateActiveSession(existing, snapshot, Int.MAX_VALUE).entity
        val finalEntity = updated.copy(
            endedAtMs = snapshot.timestampMs,
            chargingState = snapshot.chargingState.name,
            currentLevelPercent = snapshot.levelPercent,
            currentTemperatureC = snapshot.temperatureC,
            status = SessionStatus.COMPLETED.name,
        )
        return UpdateResult(entity = finalEntity, targetCrossed = false)
    }

    private fun closeStaleSession(existing: ChargeSessionEntity): ChargeSessionEntity {
        return existing.copy(
            endedAtMs = existing.lastSeenAtMs,
            status = SessionStatus.INCOMPLETE.name,
        )
    }

    private fun maxOfNullable(first: Float?, second: Float?): Float? {
        return when {
            first == null -> second
            second == null -> first
            else -> maxOf(first, second)
        }
    }

    private fun buildDebugSessions(): List<ChargeSessionEntity> {
        val nowMs = System.currentTimeMillis()
        val random = Random(20260619L)
        return (364 downTo 1).map { day ->
            buildCompletedSession(
                dayIndex = day,
                nowMs = nowMs,
                random = random,
            )
        } + buildActiveSession(nowMs, random)
    }

    private fun buildCompletedSession(
        dayIndex: Int,
        nowMs: Long,
        random: Random,
    ): ChargeSessionEntity {
        val dayStart = nowMs - dayIndex * DAY_MS
        val startOffsetMinutes = random.nextInt(7 * 60, 19 * 60)
        val startLevel = random.nextInt(18, 56)
        val useful = dayIndex % 4 != 0
        val source = when {
            useful && dayIndex % 11 == 0 -> ChargingSource.DOCK
            useful && dayIndex % 7 == 0 -> ChargingSource.AC
            !useful && dayIndex % 3 == 0 -> ChargingSource.WIRELESS
            else -> ChargingSource.USB
        }
        val durationMinutes = when {
            useful -> random.nextInt(55, 175)
            source == ChargingSource.WIRELESS -> random.nextInt(20, 70)
            else -> random.nextInt(25, 105)
        }
        val gain = when {
            useful -> random.nextInt(28, 52)
            source == ChargingSource.WIRELESS -> random.nextInt(6, 20)
            else -> random.nextInt(10, 28)
        }
        val endLevel = (startLevel + gain).coerceAtMost(100)
        val highTemp = when {
            useful -> random.nextFloat(31.5f, 42.0f)
            source == ChargingSource.WIRELESS -> random.nextFloat(41.5f, 45.5f)
            else -> random.nextFloat(38.5f, 44.5f)
        }
        val estimatedCapacityUah = random.nextInt(3_400_000, 4_900_000)
        val startChargeCounter = random.nextInt(1_100_000, 2_600_000)
        val endChargeCounter = startChargeCounter + ((estimatedCapacityUah * gain) / 100)
        val averageTemp = (highTemp - random.nextFloat(0.2f, 1.6f)).coerceAtLeast(28f)
        val currentTemp = (averageTemp - random.nextFloat(0.1f, 0.9f)).coerceAtLeast(27.5f)
        val startedAtMs = dayStart + startOffsetMinutes * MINUTE_MS
        val endedAtMs = startedAtMs + durationMinutes * MINUTE_MS
        val timeAbove85Sec = if (endLevel >= 85) ((durationMinutes - 15).coerceAtLeast(0) * 60L) else 0L
        val timeAbove90Sec = if (endLevel >= 90) ((durationMinutes - 35).coerceAtLeast(0) * 60L) else 0L

        return ChargeSessionEntity(
            startedAtMs = startedAtMs,
            lastSeenAtMs = endedAtMs,
            endedAtMs = endedAtMs,
            startLevelPercent = startLevel,
            currentLevelPercent = endLevel,
            startChargeCounterUah = startChargeCounter,
            currentChargeCounterUah = endChargeCounter,
            startTemperatureC = currentTemp,
            currentTemperatureC = currentTemp,
            maxTemperatureC = highTemp,
            averageTemperatureC = averageTemp,
            chargingSource = source.name,
            chargingState = ChargingState.DISCHARGING.name,
            status = SessionStatus.COMPLETED.name,
            sampleCount = random.nextInt(3, 9),
            timeAbove35Sec = if (highTemp >= 35f) durationMinutes * 60L else 0L,
            timeAbove40Sec = if (highTemp >= 40f) ((durationMinutes - 10).coerceAtLeast(0) * 60L) else 0L,
            timeAbove43Sec = if (highTemp >= 43f) ((durationMinutes - 20).coerceAtLeast(0) * 60L) else 0L,
            timeAbove45Sec = if (highTemp >= 45f) ((durationMinutes - 30).coerceAtLeast(0) * 60L) else 0L,
            timeAbove80Sec = if (endLevel >= 80) ((durationMinutes - 5).coerceAtLeast(0) * 60L) else 0L,
            timeAbove85Sec = timeAbove85Sec,
            timeAbove90Sec = timeAbove90Sec,
            timeAbove95Sec = if (endLevel >= 95) ((durationMinutes - 45).coerceAtLeast(0) * 60L) else 0L,
            timeHotAndAbove85Sec = if (highTemp >= 40f && endLevel >= 85) ((durationMinutes - 25).coerceAtLeast(0) * 60L) else 0L,
            timeVeryHotAndAbove90Sec = if (highTemp >= 43f && endLevel >= 90) ((durationMinutes - 40).coerceAtLeast(0) * 60L) else 0L,
            lastNotifiedTargetPercent = null,
            gainPercent = (endLevel - startLevel).coerceAtLeast(0),
        )
    }

    private fun buildActiveSession(
        nowMs: Long,
        random: Random,
    ): ChargeSessionEntity {
        val startedAtMs = nowMs - random.nextInt(85, 240) * MINUTE_MS
        val startLevel = random.nextInt(44, 68)
        val currentLevel = (startLevel + random.nextInt(8, 24)).coerceAtMost(84)
        val estimatedCapacityUah = random.nextInt(3_400_000, 4_900_000)
        val startChargeCounter = random.nextInt(1_250_000, 2_700_000)
        val currentChargeCounter = startChargeCounter + ((estimatedCapacityUah * (currentLevel - startLevel)) / 100)
        val temperature = random.nextFloat(33.5f, 39.5f)
        val averageTemperature = (temperature - random.nextFloat(0.1f, 0.7f)).coerceAtLeast(30f)

        return ChargeSessionEntity(
            startedAtMs = startedAtMs,
            lastSeenAtMs = nowMs,
            endedAtMs = null,
            startLevelPercent = startLevel,
            currentLevelPercent = currentLevel,
            startChargeCounterUah = startChargeCounter,
            currentChargeCounterUah = currentChargeCounter,
            startTemperatureC = temperature,
            currentTemperatureC = temperature,
            maxTemperatureC = temperature,
            averageTemperatureC = averageTemperature,
            chargingSource = ChargingSource.USB.name,
            chargingState = ChargingState.CHARGING.name,
            status = SessionStatus.ACTIVE.name,
            sampleCount = random.nextInt(2, 6),
            timeAbove35Sec = 0L,
            timeAbove40Sec = 0L,
            timeAbove43Sec = 0L,
            timeAbove45Sec = 0L,
            timeAbove80Sec = 0L,
            timeAbove85Sec = 0L,
            timeAbove90Sec = 0L,
            timeAbove95Sec = 0L,
            timeHotAndAbove85Sec = 0L,
            timeVeryHotAndAbove90Sec = 0L,
            lastNotifiedTargetPercent = null,
            gainPercent = (currentLevel - startLevel).coerceAtLeast(0),
        )
    }

    private data class ExposureTotals(
        val timeAbove35Sec: Long,
        val timeAbove40Sec: Long,
        val timeAbove43Sec: Long,
        val timeAbove45Sec: Long,
        val timeAbove80Sec: Long,
        val timeAbove85Sec: Long,
        val timeAbove90Sec: Long,
        val timeAbove95Sec: Long,
        val timeHotAndAbove85Sec: Long,
        val timeVeryHotAndAbove90Sec: Long,
    )

    private fun accumulateExposure(
        existing: ChargeSessionEntity,
        deltaSec: Long,
    ): ExposureTotals {
        if (deltaSec <= 0L || deltaSec > 30 * MINUTE_MS / 1000L) {
            return ExposureTotals(
                timeAbove35Sec = existing.timeAbove35Sec,
                timeAbove40Sec = existing.timeAbove40Sec,
                timeAbove43Sec = existing.timeAbove43Sec,
                timeAbove45Sec = existing.timeAbove45Sec,
                timeAbove80Sec = existing.timeAbove80Sec,
                timeAbove85Sec = existing.timeAbove85Sec,
                timeAbove90Sec = existing.timeAbove90Sec,
                timeAbove95Sec = existing.timeAbove95Sec,
                timeHotAndAbove85Sec = existing.timeHotAndAbove85Sec,
                timeVeryHotAndAbove90Sec = existing.timeVeryHotAndAbove90Sec,
            )
        }

        val previousTemperature = existing.currentTemperatureC ?: existing.averageTemperatureC ?: existing.maxTemperatureC
        val previousLevel = existing.currentLevelPercent
        // Sessions only exist while charging-like, so no separate isCharging gate is needed here.
        val hotAndAbove85 = previousTemperature != null && previousTemperature >= 40f && previousLevel >= 85
        val veryHotAndAbove90 = previousTemperature != null && previousTemperature >= 43f && previousLevel >= 90

        return ExposureTotals(
            timeAbove35Sec = existing.timeAbove35Sec + if (previousTemperature != null && previousTemperature >= 35f) deltaSec else 0L,
            timeAbove40Sec = existing.timeAbove40Sec + if (previousTemperature != null && previousTemperature >= 40f) deltaSec else 0L,
            timeAbove43Sec = existing.timeAbove43Sec + if (previousTemperature != null && previousTemperature >= 43f) deltaSec else 0L,
            timeAbove45Sec = existing.timeAbove45Sec + if (previousTemperature != null && previousTemperature >= 45f) deltaSec else 0L,
            timeAbove80Sec = existing.timeAbove80Sec + if (previousLevel >= 80) deltaSec else 0L,
            timeAbove85Sec = existing.timeAbove85Sec + if (previousLevel >= 85) deltaSec else 0L,
            timeAbove90Sec = existing.timeAbove90Sec + if (previousLevel >= 90) deltaSec else 0L,
            timeAbove95Sec = existing.timeAbove95Sec + if (previousLevel >= 95) deltaSec else 0L,
            timeHotAndAbove85Sec = existing.timeHotAndAbove85Sec + if (hotAndAbove85) deltaSec else 0L,
            timeVeryHotAndAbove90Sec = existing.timeVeryHotAndAbove90Sec + if (veryHotAndAbove90) deltaSec else 0L,
        )
    }

    private fun Random.nextFloat(min: Float, max: Float): Float {
        return min + nextFloat() * (max - min)
    }

    private fun Random.nextInt(min: Int, max: Int): Int {
        return min + nextInt(max - min)
    }

    private companion object {
        const val DAY_MS = 86_400_000L
        const val MINUTE_MS = 60_000L
        const val STALE_ACTIVE_SESSION_MS = 30 * MINUTE_MS
    }
}
