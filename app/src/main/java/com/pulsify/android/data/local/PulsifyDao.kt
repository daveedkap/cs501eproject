package com.pulsify.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PulsifyDao {
    @Query("SELECT * FROM activity_sessions ORDER BY timestampMillis DESC")
    fun observeSessions(): Flow<List<ActivitySessionEntity>>

    @Insert
    suspend fun insertSession(session: ActivitySessionEntity): Long

    @Query("SELECT * FROM context_music_rules ORDER BY useCount DESC")
    fun observeRules(): Flow<List<ContextMusicRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: ContextMusicRuleEntity)
}
