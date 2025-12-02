// File: app/src/main/java/com/example/fitpath/data/db/WeightDao.kt
package com.example.fitpath.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Query("SELECT * FROM weight_entries ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun findByDate(dateEpochDay: Long): WeightEntry?

    @Query("SELECT id FROM weight_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun findIdByDate(dateEpochDay: Long): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: WeightEntry): Long

    @Update
    suspend fun update(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM weight_entries")
    suspend fun clear()

    /**
     * "One day one row": insert if absent, otherwise overwrite weight for that day.
     * Uses a single transaction to avoid race conditions.
     */
    @Transaction
    suspend fun upsertByDate(dateEpochDay: Long, weightKg: Double) {
        val existingId = findIdByDate(dateEpochDay)
        if (existingId == null) {
            insert(WeightEntry(dateEpochDay = dateEpochDay, weightKg = weightKg))
        } else {
            update(WeightEntry(id = existingId, dateEpochDay = dateEpochDay, weightKg = weightKg))
        }
    }
}
