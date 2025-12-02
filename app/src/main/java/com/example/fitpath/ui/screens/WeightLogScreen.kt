// File: app/src/main/java/com/fitpath/ui/screens/WeightLogScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.fitpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.ui.vm.AppViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WeightLogScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<WeightEntryModel?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.weight_log_title), style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { editing = null; showDialog = true }) {
                Text(stringResource(R.string.add_entry))
            }
        }

        if (ui.weightEntries.isEmpty()) {
            Text("No entries yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ui.weightEntries, key = { it.id }) { e ->
                    WeightRow(
                        entry = e,
                        onEdit = { editing = e; showDialog = true },
                        onDelete = { vm.deleteWeight(e.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        WeightEntryDialog(
            initialDate = editing?.date ?: LocalDate.now(),
            initialWeight = editing?.weightKg?.toString() ?: "",
            onDismiss = { showDialog = false },
            onSave = { date, weightStr ->
                val w = weightStr.toDoubleOrNull()
                if (w != null && w in 20.0..400.0) {
                    vm.upsertWeight(date, w)
                    showDialog = false
                }
            }
        )
    }
}

@Composable
private fun WeightRow(entry: WeightEntryModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(entry.date.toString(), style = MaterialTheme.typography.titleSmall)
                Text("${"%.1f".format(entry.weightKg)} kg", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                OutlinedButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun WeightEntryDialog(
    initialDate: LocalDate,
    initialWeight: String,
    onDismiss: () -> Unit,
    onSave: (LocalDate, String) -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    var selectedDate by remember { mutableStateOf(initialDate) }
    var weightText by remember { mutableStateOf(initialWeight) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isFutureDate = selectedDate.isAfter(today)
    val weightOk = weightText.toDoubleOrNull()?.let { it in 20.0..400.0 } == true

    if (showDatePicker) {
        val initialMillis = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val d = Instant.ofEpochMilli(utcTimeMillis).atZone(zoneId).toLocalDate()
                    return !d.isAfter(today) // 禁选未来
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val d = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                        if (!d.isAfter(today)) selectedDate = d
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState, showModeToggle = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = weightOk && !isFutureDate,
                onClick = { onSave(selectedDate, weightText) }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.add_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ✅ 日历选择日期（替代手输）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.date), style = MaterialTheme.typography.titleMedium)
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = { Text(selectedDate.format(formatter)) }
                    )
                }
                if (isFutureDate) {
                    Text(
                        text = "Date cannot be in the future.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = weightText.isNotBlank() && !weightOk,
                    supportingText = {
                        if (weightText.isNotBlank() && !weightOk) Text("Range: 20–400 kg")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
