// File: app/src/main/java/com/fitpath/ui/nav/FitPathNavGraph.kt
package com.example.fitpath.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitpath.ui.screens.*
import com.example.fitpath.ui.vm.AppViewModel

@Composable
fun FitPathNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    vm: AppViewModel
) {
    val start = if (vm.isOnboardingCompleted()) Route.Today.path else Route.Onboarding.path

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = start
    ) {
        composable(Route.Onboarding.path) {
            OnboardingScreen(
                vm = vm,
                onContinue = {
                    vm.completeOnboarding()
                    navController.navigate(Route.Today.path) {
                        popUpTo(Route.Onboarding.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Today.path) { TodayPlanScreen(vm = vm, onEditProfile = { navController.navigate(Route.Onboarding.path) }) }
        composable(Route.WeightLog.path) { WeightLogScreen(vm = vm) }
        composable(Route.Trends.path) { TrendsScreen(vm = vm) }
        composable(Route.Settings.path) { SettingsScreen(vm = vm) }
    }
}
