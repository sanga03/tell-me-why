package com.tellmewhy.data.local.dao

import com.tellmewhy.domain.model.JustificationEntry

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow // For observing data changes (optional but good)

@Dao
interface JustificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if conflict (e.g., same primary key, though unlikely with autoGenerate)
    suspend fun insertJustification(entry: JustificationEntry) // Use suspend for coroutines

    @Query("SELECT * FROM justification_log ORDER BY timestamp DESC")
    fun getAllJustifications(): Flow<List<JustificationEntry>> // Observe changes as a Flow

    @Query("SELECT * FROM justification_log WHERE appName = :appName ORDER BY timestamp DESC")
    fun getJustificationsForApp(appName: String): Flow<List<JustificationEntry>>

    // You can add more queries as needed, e.g., delete, update, get by ID, etc.
    @Query("DELETE FROM justification_log WHERE id = :entryId")
    suspend fun deleteJustificationById(entryId: Int)

    @Query("DELETE FROM justification_log")
    suspend fun clearAllJustifications()
}
