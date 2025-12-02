// File: app/src/main/java/com/example/fitpath/core/AppContainer.kt
package com.example.fitpath.core

import android.content.Context
import com.example.fitpath.data.db.FitPathDatabase
import com.example.fitpath.data.prefs.UserDataStore
import com.example.fitpath.data.repo.UserPrefsRepositoryImpl
import com.example.fitpath.data.repo.UserProfileRepositoryImpl
import com.example.fitpath.data.repo.WeightRepositoryImpl
import com.example.fitpath.domain.prefs.UserPrefsRepository
import com.example.fitpath.domain.profile.UserProfileRepository
import com.example.fitpath.domain.weight.WeightRepository
import com.example.fitpath.work.ReminderScheduler

class AppContainer(appContext: Context) {
    private val database: FitPathDatabase = FitPathDatabase.build(appContext)
    private val dataStore = UserDataStore(appContext)

    val userPrefsRepository: UserPrefsRepository = UserPrefsRepositoryImpl(dataStore)
    val userProfileRepository: UserProfileRepository = UserProfileRepositoryImpl(dataStore)
    val weightRepository: WeightRepository = WeightRepositoryImpl(database.weightDao())

    val reminderScheduler: ReminderScheduler = ReminderScheduler(appContext)
}
