// File: app/src/main/java/com/fitpath/ui/nav/Routes.kt
package com.example.fitpath.ui.nav

sealed class Route(val path: String) {
    data object Onboarding : Route("onboarding")
    data object Today : Route("today")
    data object WeightLog : Route("weight_log")
    data object Trends : Route("trends")
    data object Settings : Route("settings")
}
