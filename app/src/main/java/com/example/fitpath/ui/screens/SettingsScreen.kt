// File: app/src/main/java/com/fitpath/ui/screens/SettingsScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.fitpath.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.example.fitpath.R
import com.example.fitpath.domain.model.LanguageMode
import com.example.fitpath.domain.model.ThemeMode
import com.example.fitpath.ui.vm.AppViewModel


@Composable
fun SettingsScreen(vm: AppViewModel) {
    val prefs by vm.prefs.collectAsState()
    val ctx = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    val requestNotifPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (granted) vm.setReminderEnabled(true) }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)

        SettingSegment(
            title = stringResource(R.string.theme),
            options = listOf(
                stringResource(R.string.theme_system) to ThemeMode.SYSTEM,
                stringResource(R.string.theme_light) to ThemeMode.LIGHT,
                stringResource(R.string.theme_dark) to ThemeMode.DARK,
            ),
            selected = prefs.themeMode,
            onSelect = vm::setTheme
        )

        SettingSegment(
            title = stringResource(R.string.language),
            options = listOf(
                stringResource(R.string.language_system) to LanguageMode.SYSTEM,
                stringResource(R.string.language_en) to LanguageMode.EN,
                stringResource(R.string.language_zh) to LanguageMode.ZH,
            ),
            selected = prefs.languageMode,
            onSelect = vm::setLanguage
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.reminders_desc), style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = prefs.reminderEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                vm.setReminderEnabled(false)
                            } else {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    val has = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
                                    if (has) vm.setReminderEnabled(true)
                                    else requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    vm.setReminderEnabled(true)
                                }
                            }
                        }
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { vm.exportData(ctx) }
                ) { Text(stringResource(R.string.export_data)) }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { confirmDelete = true }
                ) { Text(stringResource(R.string.delete_data)) }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_data)) },
            text = { Text(stringResource(R.string.delete_data_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.deleteAllData(); confirmDelete = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun <T> SettingSegment(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (label, value) ->
                SegmentedButton(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size)
                ) { Text(label) }
            }
        }
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
