package com.synosoftware.battery.data.session

import androidx.room.Room
import com.synosoftware.battery.domain.BatterySnapshot
import com.synosoftware.battery.domain.ChargingSource
import com.synosoftware.battery.domain.ChargingState
import com.synosoftware.battery.domain.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry

class ChargeSessionRepositoryTest {
    @Test
    fun staleChargingSessionStartsFreshSessionWithoutCountingTheGap() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, BatteryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repository = ChargeSessionRepository(database, database.chargeSessionDao())
            val now = 1_000_000L
            val stale = seedActiveSession(
                startedAtMs = now - 2 * 60 * 60 * 1000L,
                lastSeenAtMs = now - 31 * 60 * 1000L,
                currentLevelPercent = 92,
                timeAbove85Sec = 600L,
                timeAbove90Sec = 120L,
            )
            val staleId = database.chargeSessionDao().insert(stale)

            val result = repository.recordSnapshot(
                snapshot = snapshot(
                    timestampMs = now,
                    levelPercent = 93,
                    temperatureC = 41f,
                    chargingState = ChargingState.CHARGING,
                ),
                targetPercent = 90,
            )

            val sessions = database.chargeSessionDao().observeSessions().first()
            assertEquals(2, sessions.size)
            val staleRow = sessions.first { it.id == staleId }
            assertEquals(SessionStatus.INCOMPLETE.name, staleRow.status)
            assertEquals(staleRow.lastSeenAtMs, staleRow.endedAtMs)
            assertEquals(600L, staleRow.timeAbove85Sec)
            assertEquals(120L, staleRow.timeAbove90Sec)
            val activeRow = sessions.first { it.status == SessionStatus.ACTIVE.name }
            assertEquals(now, activeRow.startedAtMs)
            assertEquals(now, activeRow.lastSeenAtMs)
            assertEquals(0L, activeRow.timeAbove85Sec)
            assertEquals(0L, activeRow.timeAbove90Sec)
            assertNotNull(result.activeSession)
            assertEquals(now, result.activeSession?.startedAtMs)
        } finally {
            database.close()
        }
    }

    @Test
    fun staleUnpluggedSessionClosesWithoutCountingTheGap() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, BatteryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repository = ChargeSessionRepository(database, database.chargeSessionDao())
            val now = 2_000_000L
            val stale = seedActiveSession(
                startedAtMs = now - 90 * 60 * 1000L,
                lastSeenAtMs = now - 35 * 60 * 1000L,
                currentLevelPercent = 88,
                timeAbove85Sec = 900L,
                timeAbove90Sec = 0L,
            )
            database.chargeSessionDao().insert(stale)

            val result = repository.recordSnapshot(
                snapshot = snapshot(
                    timestampMs = now,
                    levelPercent = 87,
                    temperatureC = 34f,
                    chargingState = ChargingState.DISCHARGING,
                ),
                targetPercent = 90,
            )

            val sessions = database.chargeSessionDao().observeSessions().first()
            assertEquals(1, sessions.size)
            val staleRow = sessions.first()
            assertEquals(SessionStatus.INCOMPLETE.name, staleRow.status)
            assertEquals(staleRow.lastSeenAtMs, staleRow.endedAtMs)
            assertEquals(900L, staleRow.timeAbove85Sec)
            assertEquals(0L, staleRow.timeAbove90Sec)
            assertNull(result.activeSession)
            assertEquals(false, result.targetCrossed)
        } finally {
            database.close()
        }
    }

    @Test
    fun activeSessionAccumulatesRawExposureFromPreviousState() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, BatteryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repository = ChargeSessionRepository(database, database.chargeSessionDao())
            val now = 3_000_000L
            val active = seedActiveSession(
                startedAtMs = now - 5 * 60 * 1000L,
                lastSeenAtMs = now,
                currentLevelPercent = 84,
                currentTemperatureC = 39f,
                maxTemperatureC = 39f,
                timeAbove35Sec = 0L,
                timeAbove40Sec = 0L,
                timeAbove43Sec = 0L,
                timeAbove45Sec = 0L,
                timeAbove80Sec = 0L,
                timeAbove85Sec = 0L,
                timeAbove90Sec = 0L,
                timeAbove95Sec = 0L,
            )
            database.chargeSessionDao().insert(active)

            repository.recordSnapshot(
                snapshot = snapshot(
                    timestampMs = now + 60_000L,
                    levelPercent = 86,
                    temperatureC = 41f,
                    chargingState = ChargingState.CHARGING,
                ),
                targetPercent = 90,
            )

            repository.recordSnapshot(
                snapshot = snapshot(
                    timestampMs = now + 120_000L,
                    levelPercent = 91,
                    temperatureC = 44f,
                    chargingState = ChargingState.CHARGING,
                ),
                targetPercent = 90,
            )

            val sessions = database.chargeSessionDao().observeSessions().first()
            val row = sessions.single()
            assertEquals(120L, row.timeAbove35Sec)
            assertEquals(60L, row.timeAbove40Sec)
            assertEquals(0L, row.timeAbove43Sec)
            assertEquals(0L, row.timeAbove45Sec)
            assertEquals(120L, row.timeAbove80Sec)
            assertEquals(60L, row.timeAbove85Sec)
            assertEquals(0L, row.timeAbove90Sec)
            assertEquals(0L, row.timeAbove95Sec)
        } finally {
            database.close()
        }
    }

    private fun seedActiveSession(
        startedAtMs: Long,
        lastSeenAtMs: Long,
        currentLevelPercent: Int,
        timeAbove85Sec: Long,
        timeAbove90Sec: Long,
        currentTemperatureC: Float = 41f,
        maxTemperatureC: Float = 41f,
        timeAbove35Sec: Long = 0L,
        timeAbove40Sec: Long = 0L,
        timeAbove43Sec: Long = 0L,
        timeAbove45Sec: Long = 0L,
        timeAbove80Sec: Long = 0L,
        timeAbove95Sec: Long = 0L,
    ): ChargeSessionEntity {
        return ChargeSessionEntity(
            startedAtMs = startedAtMs,
            lastSeenAtMs = lastSeenAtMs,
            endedAtMs = null,
            startLevelPercent = 60,
            currentLevelPercent = currentLevelPercent,
            startChargeCounterUah = 1_000_000,
            currentChargeCounterUah = 2_000_000,
            startTemperatureC = 33f,
            currentTemperatureC = currentTemperatureC,
            maxTemperatureC = maxTemperatureC,
            averageTemperatureC = 39f,
            chargingSource = ChargingSource.USB.name,
            chargingState = ChargingState.CHARGING.name,
            status = SessionStatus.ACTIVE.name,
            sampleCount = 3,
            timeAbove85Sec = timeAbove85Sec,
            timeAbove90Sec = timeAbove90Sec,
            lastNotifiedTargetPercent = null,
            gainPercent = 32,
            timeAbove35Sec = timeAbove35Sec,
            timeAbove40Sec = timeAbove40Sec,
            timeAbove43Sec = timeAbove43Sec,
            timeAbove45Sec = timeAbove45Sec,
            timeAbove80Sec = timeAbove80Sec,
            timeAbove95Sec = timeAbove95Sec,
        )
    }

    private fun snapshot(
        timestampMs: Long,
        levelPercent: Int,
        temperatureC: Float,
        chargingState: ChargingState,
    ): BatterySnapshot {
        return BatterySnapshot(
            timestampMs = timestampMs,
            levelPercent = levelPercent,
            scale = 100,
            temperatureC = temperatureC,
            voltageMv = 4100,
            currentUa = 900_000,
            averageCurrentUa = 850_000,
            chargeCounterUah = 2_100_000,
            chargingSource = ChargingSource.USB,
            chargingState = chargingState,
            healthLabel = null,
            technology = null,
        )
    }
}
