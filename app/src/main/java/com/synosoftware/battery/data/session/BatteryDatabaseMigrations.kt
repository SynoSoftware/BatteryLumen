package com.synosoftware.battery.data.session

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `charge_sessions_new` (
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
        db.execSQL(
            """
            INSERT INTO `charge_sessions_new` (
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
            )
            SELECT
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
            FROM `charge_sessions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `charge_sessions`")
        db.execSQL("ALTER TABLE `charge_sessions_new` RENAME TO `charge_sessions`")
        db.execSQL(
            """
            INSERT OR REPLACE INTO `sqlite_sequence` (`name`, `seq`)
            SELECT 'charge_sessions', IFNULL(MAX(`id`), 0)
            FROM `charge_sessions`
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `charge_sessions` ADD COLUMN `startChargeCounterUah` INTEGER")
        db.execSQL("ALTER TABLE `charge_sessions` ADD COLUMN `currentChargeCounterUah` INTEGER")
    }
}
