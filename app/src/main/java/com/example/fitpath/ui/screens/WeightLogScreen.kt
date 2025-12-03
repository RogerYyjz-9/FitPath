// File: app/src/main/java/com/example/fitpath/ui/screens/WeightLogScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.fitpath.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.ui.components.WeightEntryDialog
import com.example.fitpath.ui.vm.AppViewModel
import java.time.LocalDate
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
            Text(stringResource(R.string.weight_log_empty))
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
                Text(entry.date.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.titleSmall)
                Text("${"%.1f".format(entry.weightKg)} kg", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                OutlinedButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
