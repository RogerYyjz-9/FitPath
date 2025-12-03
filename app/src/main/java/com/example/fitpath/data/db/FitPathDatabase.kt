// File: app/src/main/java/com/example/fitpath/data/db/FitPathDatabase.kt
package com.example.fitpath.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [WeightEntry::class],
    version = 1, // 保持版本一致，使用破坏性迁移重置数据
    exportSchema = false
)
abstract class FitPathDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var Instance: FitPathDatabase? = null

        // 方法名必须是 build，因为 AppContainer 中是这样调用的
        fun build(context: Context): FitPathDatabase =
            Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FitPathDatabase::class.java, "fitpath.db")
                    .addCallback(PrePopulateCallback(context)) // 添加预填充回调
                    .fallbackToDestructiveMigration() // 允许破坏性迁移（会清空旧数据）
                    .build()
                    .also { Instance = it }
            }
    }
}

// 预填充数据的回调
private class PrePopulateCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            // 使用 build 获取实例
            val database = FitPathDatabase.build(context)
            prePopulateWeightData(database.weightDao())
        }
    }

    private fun prePopulateWeightData(dao: WeightDao) {
        val today = LocalDate.now()
        val entries = mutableListOf<WeightEntry>()
        for (i in 30 downTo 0) {
            val date = today.minusDays(i.toLong())
            val trend = i * 0.15
            val wave = kotlin.math.sin(i.toDouble() * 0.5) * 0.5
            val weight = (70.0 + trend + wave).let { Math.round(it * 100.0) / 100.0 }

            entries.add(
                WeightEntry(
                    // id 自动生成
                    dateEpochDay = date.toEpochDay(), // [修复] 使用 dateEpochDay 而不是 date string
                    weightKg = weight
                )
            )
        }
        dao.insertAll(entries)
    }
}