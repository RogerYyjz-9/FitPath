// File: app/src/main/java/com/fitpath/domain/profile/UserProfileRepository.kt
package com.example.fitpath.domain.profile

import com.example.fitpath.domain.model.ActivityLevel
import com.example.fitpath.domain.model.FoodPreference
import com.example.fitpath.domain.model.Sex
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val foodPreference: FoodPreference = FoodPreference.NONE,
    val sex: Sex = Sex.UNSPECIFIED,
    val ageYears: Int? = null,
) {
    fun isReadyForPlan(): Boolean =
        (currentWeightKg != null && currentWeightKg > 0.0) &&
                (targetWeightKg != null && targetWeightKg > 0.0)
}

interface UserProfileRepository {
    val profile: StateFlow<UserProfile>
    suspend fun update(profile: UserProfile)
    suspend fun clearAll()
}
