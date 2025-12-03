// File: app/src/main/java/com/example/fitpath/data/db/FitPathDatabase.kt
package com.example.fitpath.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WeightEntry::class],
    version = 3,
    exportSchema = true
)
abstract class FitPathDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao

    companion object {
        fun build(context: Context): FitPathDatabase =
            Room.databaseBuilder(context, FitPathDatabase::class.java, "fitpath.db")
                // v1 -> v2: enforce "one row per day"
                // v2 -> v3: deduplicate again and keep UNIQUE index without destructive fallback
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}

/**
 * Migration 1->2:
 * 1) If duplicates exist, keep latest row (max id) for each day.
 * 2) Create UNIQUE index on dateEpochDay.
 */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM weight_entries
            WHERE id NOT IN (
              SELECT MAX(id) FROM weight_entries GROUP BY dateEpochDay
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_weight_entries_dateEpochDay
            ON weight_entries(dateEpochDay)
            """.trimIndent()
        )
    }
}

/**
 * Migration 2->3: ensure UNIQUE constraint holds without destructive migration.
 */
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM weight_entries
            WHERE id NOT IN (
              SELECT MAX(id) FROM weight_entries GROUP BY dateEpochDay
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_weight_entries_dateEpochDay
            ON weight_entries(dateEpochDay)
            """.trimIndent()
        )
    }
}
