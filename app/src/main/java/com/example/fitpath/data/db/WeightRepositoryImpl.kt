// File: app/src/main/java/com/fitpath/data/repo/WeightRepositoryImpl.kt
package com.example.fitpath.data.repo

import com.example.fitpath.data.db.WeightDao
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.domain.weight.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WeightRepositoryImpl(
    private val dao: WeightDao
) : WeightRepository {

    override fun entriesFlow(): Flow<List<WeightEntryModel>> =
        dao.observeAll().map { list ->
            list.map {
                WeightEntryModel(
                    id = it.id,
                    date = LocalDate.ofEpochDay(it.dateEpochDay),
                    weightKg = it.weightKg
                )
            }
        }

    override suspend fun upsert(date: LocalDate, weightKg: Double) {
        // Data hygiene boundary (non-medical): avoid nonsense polluting trend.
        require(weightKg in 20.0..400.0) { "weightKg out of range" }
        dao.upsertByDate(date.toEpochDay(), weightKg)
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun clearAll() = dao.clear()
}
