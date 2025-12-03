// File: app/src/main/java/com/example/fitpath/ui/components/WeightEntryDialog.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.fitpath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WeightEntryDialog(
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
                }) { Text(androidx.compose.ui.res.stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(androidx.compose.ui.res.stringResource(R.string.cancel))
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
            ) { Text(androidx.compose.ui.res.stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(androidx.compose.ui.res.stringResource(R.string.cancel)) } },
        title = { Text(androidx.compose.ui.res.stringResource(R.string.add_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.date), style = MaterialTheme.typography.titleMedium)
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = { Text(selectedDate.format(formatter)) }
                    )
                }
                if (isFutureDate) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.weight_future_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(androidx.compose.ui.res.stringResource(R.string.weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = weightText.isNotBlank() && !weightOk,
                    supportingText = {
                        if (weightText.isNotBlank() && !weightOk) Text(androidx.compose.ui.res.stringResource(R.string.weight_range_error))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
