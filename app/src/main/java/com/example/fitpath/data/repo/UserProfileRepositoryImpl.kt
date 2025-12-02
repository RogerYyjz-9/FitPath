// File: app/src/main/java/com/fitpath/data/repo/UserProfileRepositoryImpl.kt
package com.example.fitpath.data.repo

import com.example.fitpath.data.prefs.UserDataStore
import com.example.fitpath.domain.profile.UserProfile
import com.example.fitpath.domain.profile.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UserProfileRepositoryImpl(
    private val store: UserDataStore
) : UserProfileRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _profile = MutableStateFlow(UserProfile())
    override val profile: StateFlow<UserProfile> = _profile

    init {
        store.prefsFlow()
            .onEach { snap ->
                _profile.value = UserProfile(
                    currentWeightKg = snap.currentWeightKg,
                    targetWeightKg = snap.targetWeightKg,
                    activityLevel = snap.activityLevel,
                    foodPreference = snap.foodPreference,
                    sex = snap.sex,
                    ageYears = snap.ageYears
                )
            }
            .launchIn(scope)
    }

    override suspend fun update(profile: UserProfile) {
        store.setProfile(
            currentWeightKg = profile.currentWeightKg,
            targetWeightKg = profile.targetWeightKg,
            activityLevel = profile.activityLevel,
            foodPreference = profile.foodPreference,
            sex = profile.sex,
            ageYears = profile.ageYears
        )
    }

    override suspend fun clearAll() = store.clearAll()
}
