// File: app/src/main/java/com/fitpath/domain/weight/WeightRepository.kt
package com.example.fitpath.domain.weight

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class WeightEntryModel(
    val id: Long,
    val date: LocalDate,
    val weightKg: Double
)

interface WeightRepository {
    fun entriesFlow(): Flow<List<WeightEntryModel>>
    suspend fun upsert(date: LocalDate, weightKg: Double)
    suspend fun delete(id: Long)
    suspend fun clearAll()
}
