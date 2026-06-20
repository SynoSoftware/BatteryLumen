package com.synosoftware.battery.data.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeSessionDao {
    @Query("SELECT * FROM charge_sessions ORDER BY startedAtMs DESC")
    fun observeSessions(): Flow<List<ChargeSessionEntity>>

    @Query("SELECT * FROM charge_sessions WHERE status = 'ACTIVE' ORDER BY startedAtMs DESC LIMIT 1")
    suspend fun getActiveSession(): ChargeSessionEntity?

    @Query("DELETE FROM charge_sessions")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(session: ChargeSessionEntity): Long

    @Insert
    suspend fun insertAll(sessions: List<ChargeSessionEntity>)

    @Update
    suspend fun update(session: ChargeSessionEntity)
}
