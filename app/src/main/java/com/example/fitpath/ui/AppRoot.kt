// File: app/src/main/java/com/fitpath/ui/AppRoot.kt
package com.example.fitpath.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitpath.R
import com.example.fitpath.ui.nav.FitPathNavGraph
import com.example.fitpath.ui.nav.Route
import com.example.fitpath.ui.theme.FitPathTheme
import com.example.fitpath.ui.vm.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppViewModel) {
    val prefs by vm.prefs.collectAsState()
    FitPathTheme(themeMode = prefs.themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val current = backStackEntry?.destination?.route
        val showBottom = current != Route.Onboarding.path

        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("FitPath") }) },
            bottomBar = {
                if (showBottom) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = current == Route.Today.path,
                            onClick = {
                                navController.navigate(Route.Today.path) {
                                    popUpTo(Route.Today.path) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Today, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_today)) }
                        )
                        NavigationBarItem(
                            selected = current == Route.WeightLog.path,
                            onClick = {
                                navController.navigate(Route.WeightLog.path) {
                                    popUpTo(Route.Today.path) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Scale, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_weight)) }
                        )
                        NavigationBarItem(
                            selected = current == Route.Trends.path,
                            onClick = {
                                navController.navigate(Route.Trends.path) {
                                    popUpTo(Route.Today.path) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Insights, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_trends)) }
                        )
                        NavigationBarItem(
                            selected = current == Route.Settings.path,
                            onClick = {
                                navController.navigate(Route.Settings.path) {
                                    popUpTo(Route.Today.path) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_settings)) }
                        )
                    }
                }
            }
        ) { inner ->
            FitPathNavGraph(
                modifier = Modifier.padding(inner),
                navController = navController,
                vm = vm
            )
        }
    }
}
