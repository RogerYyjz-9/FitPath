// File: app/src/main/java/com/example/fitpath/FitPathApplication.kt
package com.example.fitpath

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.fitpath.core.AppContainer
import com.example.fitpath.work.ReminderWorker

class FitPathApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(appContext = this)

        // Step5: notifications channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                "FitPath reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle daily reminders to log weight or review the plan."
            }
            nm.createNotificationChannel(channel)
        }
    }
}
