// File: app/src/main/java/com/example/fitpath/data/db/WeightEntry.kt
package com.example.fitpath.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per day enforced by UNIQUE index on dateEpochDay.
 * dateEpochDay = LocalDate.toEpochDay()
 */
@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["dateEpochDay"], unique = true)]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val weightKg: Double
)
