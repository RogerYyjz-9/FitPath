// File: app/src/main/java/com/example/fitpath/work/ReminderScheduler.kt
package com.example.fitpath.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    fun scheduleDaily() {
        val initialDelay = computeInitialDelayToNext(LocalTime.of(9, 0))
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay)
            .addTag(TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }

    private fun computeInitialDelayToNext(target: LocalTime): Duration {
        val now = LocalDateTime.now()
        val todayTarget = now.toLocalDate().atTime(target)
        val next = if (now.isBefore(todayTarget)) todayTarget else todayTarget.plusDays(1)
        return Duration.between(now, next).coerceAtLeast(Duration.ofMinutes(1))
    }

    companion object {
        const val UNIQUE_NAME = "fitpath_daily_reminder"
        const val TAG = "fitpath_reminder"
    }
}
