package com.tellmewhy.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tellmewhy.data.local.dao.JustificationDao
import com.tellmewhy.domain.model.JustificationEntry

@Database(entities = [JustificationEntry::class], version = 1, exportSchema = false) // Increment version on schema changes
abstract class AppDatabase : RoomDatabase() {

    abstract fun justificationDao(): JustificationDao

    companion object {
        @Volatile // Ensures visibility of changes to this variable across threads
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // synchronized to prevent multiple instances
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "justification_database" // Name of your database file
                )
                    // .fallbackToDestructiveMigration() // Use this only during development if you don't want to provide migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}