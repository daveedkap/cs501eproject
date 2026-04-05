package com.pulsify.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val activityType: String,
    val latitude: Double?,
    val longitude: Double?,
    val playlistSummary: String?,
    val trackCount: Int,
)

@Entity(tableName = "context_music_rules")
data class ContextMusicRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String,
    val associationNote: String,
    val useCount: Int = 0,
)
