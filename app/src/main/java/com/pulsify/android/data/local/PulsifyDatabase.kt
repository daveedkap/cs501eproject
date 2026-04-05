package com.pulsify.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActivitySessionEntity::class, ContextMusicRuleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PulsifyDatabase : RoomDatabase() {
    abstract fun pulsifyDao(): PulsifyDao

    companion object {
        fun build(context: Context): PulsifyDatabase =
            Room.databaseBuilder(context, PulsifyDatabase::class.java, "pulsify.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
