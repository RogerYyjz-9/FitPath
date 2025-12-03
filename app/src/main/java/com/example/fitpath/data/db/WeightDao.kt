// File: app/src/main/java/com/example/fitpath/data/db/WeightDao.kt
package com.example.fitpath.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    // 恢复原有的方法名 observeAll 和正确的表名 weight_entries
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

    // 恢复 deleteById
    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 恢复 clear
    @Query("DELETE FROM weight_entries")
    suspend fun clear()

    // [保留] 批量插入，供数据库预填充使用
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<WeightEntry>)

    /**
     * 恢复 upsertByDate 逻辑，WeightRepositoryImpl 依赖此方法
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