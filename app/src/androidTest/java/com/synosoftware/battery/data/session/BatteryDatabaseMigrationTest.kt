package com.synosoftware.battery.data.session

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry

class BatteryDatabaseMigrationTest {
    @Test
    fun migrateV1ToV3KeepsSessionRow() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-v1-to-v3"
        context.deleteDatabase(name)
        createLegacyDatabaseV1(context, name)

        val database = Room.databaseBuilder(context, BatteryDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
        try {
            val session = database.chargeSessionDao().observeSessions().first().single()
            assertEquals(1L, session.id)
            assertEquals(1_000L, session.startedAtMs)
            assertEquals(2_000L, session.lastSeenAtMs)
            assertEquals(2_000L, session.endedAtMs)
            assertEquals(55, session.startLevelPercent)
            assertEquals(92, session.currentLevelPercent)
            assertNull(session.startChargeCounterUah)
            assertNull(session.currentChargeCounterUah)
            assertEquals(3, session.sampleCount)
            assertEquals(600L, session.timeAbove85Sec)
            assertEquals(120L, session.timeAbove90Sec)
        } finally {
            database.close()
        }
    }

    @Test
    fun migrateV2ToV3KeepsSessionRow() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-v2-to-v3"
        context.deleteDatabase(name)
        createLegacyDatabaseV2(context, name)

        val database = Room.databaseBuilder(context, BatteryDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
        try {
            val session = database.chargeSessionDao().observeSessions().first().single()
            assertEquals(1L, session.id)
            assertEquals(5_000L, session.startedAtMs)
            assertEquals(6_000L, session.lastSeenAtMs)
            assertEquals(6_000L, session.endedAtMs)
            assertEquals(40, session.startLevelPercent)
            assertEquals(81, session.currentLevelPercent)
            assertNull(session.startChargeCounterUah)
            assertNull(session.currentChargeCounterUah)
            assertEquals(4, session.sampleCount)
            assertEquals(300L, session.timeAbove85Sec)
            assertEquals(0L, session.timeAbove90Sec)
        } finally {
            database.close()
        }
    }

    private fun createLegacyDatabaseV1(context: Context, name: String) {
        val database = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
        try {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `charge_sessions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `startedAtMs` INTEGER NOT NULL,
                    `lastSeenAtMs` INTEGER NOT NULL,
                    `endedAtMs` INTEGER,
                    `startLevelPercent` INTEGER NOT NULL,
                    `currentLevelPercent` INTEGER NOT NULL,
                    `startTemperatureC` REAL,
                    `currentTemperatureC` REAL,
                    `maxTemperatureC` REAL,
                    `averageTemperatureC` REAL,
                    `chargingSource` TEXT NOT NULL,
                    `chargingState` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `sampleCount` INTEGER NOT NULL,
                    `timeAbove85Sec` INTEGER NOT NULL,
                    `timeAbove90Sec` INTEGER NOT NULL,
                    `lastNotifiedTargetPercent` INTEGER,
                    `gainPercent` INTEGER NOT NULL,
                    `quality` TEXT NOT NULL,
                    `usefulForHealth` INTEGER NOT NULL,
                    `evidenceGrade` TEXT NOT NULL,
                    `confidenceLevel` TEXT NOT NULL,
                    `confidenceReason` TEXT NOT NULL,
                    `thermalStress` TEXT NOT NULL,
                    `chargeLevelStress` TEXT NOT NULL,
                    `combinedStress` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `charge_sessions` (
                    `id`,
                    `startedAtMs`,
                    `lastSeenAtMs`,
                    `endedAtMs`,
                    `startLevelPercent`,
                    `currentLevelPercent`,
                    `startTemperatureC`,
                    `currentTemperatureC`,
                    `maxTemperatureC`,
                    `averageTemperatureC`,
                    `chargingSource`,
                    `chargingState`,
                    `status`,
                    `sampleCount`,
                    `timeAbove85Sec`,
                    `timeAbove90Sec`,
                    `lastNotifiedTargetPercent`,
                    `gainPercent`,
                    `quality`,
                    `usefulForHealth`,
                    `evidenceGrade`,
                    `confidenceLevel`,
                    `confidenceReason`,
                    `thermalStress`,
                    `chargeLevelStress`,
                    `combinedStress`
                ) VALUES (
                    1,
                    1000,
                    2000,
                    2000,
                    55,
                    92,
                    31.5,
                    32.5,
                    33.5,
                    32.0,
                    'USB',
                    'DISCHARGING',
                    'COMPLETED',
                    3,
                    600,
                    120,
                    90,
                    37,
                    'USEFUL',
                    1,
                    'ESTIMATED',
                    'HIGH',
                    'confidence reason',
                    'NORMAL',
                    'HIGH_STRESS',
                    'HIGH_STRESS'
                )
                """.trimIndent(),
            )
            database.version = 1
        } finally {
            database.close()
        }
    }

    private fun createLegacyDatabaseV2(context: Context, name: String) {
        val database = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
        try {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `charge_sessions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `startedAtMs` INTEGER NOT NULL,
                    `lastSeenAtMs` INTEGER NOT NULL,
                    `endedAtMs` INTEGER,
                    `startLevelPercent` INTEGER NOT NULL,
                    `currentLevelPercent` INTEGER NOT NULL,
                    `startTemperatureC` REAL,
                    `currentTemperatureC` REAL,
                    `maxTemperatureC` REAL,
                    `averageTemperatureC` REAL,
                    `chargingSource` TEXT NOT NULL,
                    `chargingState` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `sampleCount` INTEGER NOT NULL,
                    `timeAbove85Sec` INTEGER NOT NULL,
                    `timeAbove90Sec` INTEGER NOT NULL,
                    `lastNotifiedTargetPercent` INTEGER,
                    `gainPercent` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `charge_sessions` (
                    `id`,
                    `startedAtMs`,
                    `lastSeenAtMs`,
                    `endedAtMs`,
                    `startLevelPercent`,
                    `currentLevelPercent`,
                    `startTemperatureC`,
                    `currentTemperatureC`,
                    `maxTemperatureC`,
                    `averageTemperatureC`,
                    `chargingSource`,
                    `chargingState`,
                    `status`,
                    `sampleCount`,
                    `timeAbove85Sec`,
                    `timeAbove90Sec`,
                    `lastNotifiedTargetPercent`,
                    `gainPercent`
                ) VALUES (
                    1,
                    5000,
                    6000,
                    6000,
                    40,
                    81,
                    29.0,
                    30.0,
                    31.0,
                    30.5,
                    'AC',
                    'DISCHARGING',
                    'COMPLETED',
                    4,
                    300,
                    0,
                    85,
                    41
                )
                """.trimIndent(),
            )
            database.version = 2
        } finally {
            database.close()
        }
    }
}
