// File: app/src/main/java/com/example/fitpath/ui/AppRoot.kt
package com.example.fitpath.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitpath.R
import com.example.fitpath.ui.components.WeightEntryDialog
import com.example.fitpath.ui.nav.FitPathNavGraph
import com.example.fitpath.ui.nav.Route
import com.example.fitpath.ui.theme.FitPathTheme
import com.example.fitpath.ui.vm.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppViewModel) {
    val prefs by vm.prefs.collectAsState()
    FitPathTheme(themeMode = prefs.themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val current = backStackEntry?.destination?.route
        val showBottom = current != Route.Onboarding.path
        var showWeightDialog by remember { mutableStateOf(false) }
        var showQuickSheet by remember { mutableStateOf(false) }

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
            },
            floatingActionButton = {
                if (showBottom) {
                    when (current) {
                        Route.Today.path -> {
                            SmallFloatingActionButton(onClick = { showQuickSheet = true }) {
                                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_quick_add))
                            }
                        }
                        Route.WeightLog.path -> {
                            SmallFloatingActionButton(onClick = { showWeightDialog = true }) {
                                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_entry))
                            }
                        }
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

        if (showQuickSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQuickSheet = false }
            ) {
                ListItem(
                    overlineContent = { Text(stringResource(R.string.action_quick_add)) },
                    headlineContent = { Text(stringResource(R.string.add_weight_action)) },
                    supportingContent = { Text(stringResource(R.string.add_weight_action_desc)) },
                    leadingContent = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    modifier = Modifier
                        .clickable {
                            showQuickSheet = false
                            showWeightDialog = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.show_meals)) },
                    supportingContent = { Text(stringResource(R.string.show_meals_desc)) },
                    leadingContent = { Icon(Icons.Outlined.Today, contentDescription = null) },
                    modifier = Modifier
                        .clickable {
                            showQuickSheet = false
                            vm.requestScrollToMeals()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.open_weight_log)) },
                    supportingContent = { Text(stringResource(R.string.open_weight_log_desc)) },
                    leadingContent = { Icon(Icons.Outlined.Scale, contentDescription = null) },
                    modifier = Modifier
                        .clickable {
                            showQuickSheet = false
                            navController.navigate(Route.WeightLog.path) {
                                popUpTo(Route.Today.path) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.edit_profile_action)) },
                    supportingContent = { Text(stringResource(R.string.edit_profile_desc)) },
                    leadingContent = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    modifier = Modifier
                        .clickable {
                            showQuickSheet = false
                            navController.navigate(Route.Onboarding.path) {
                                launchSingleTop = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (showWeightDialog) {
            WeightEntryDialog(
                initialDate = LocalDate.now(),
                initialWeight = "",
                onDismiss = { showWeightDialog = false },
                onSave = { date, weightStr ->
                    val w = weightStr.toDoubleOrNull()
                    if (w != null && w in 20.0..400.0) {
                        vm.upsertWeight(date, w)
                        showWeightDialog = false
                    }
                }
            )
        }
    }
}
