package com.synosoftware.battery.data.session

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChargeSessionEntity::class],
    version = 5,
    exportSchema = true,
)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun chargeSessionDao(): ChargeSessionDao
}
