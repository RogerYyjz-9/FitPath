// File: app/src/main/java/com/fitpath/ui/screens/TrendsScreen.kt
// File: app/src/main/java/com/example/fitpath/ui/screens/TrendsScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.fitpath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.ui.vm.AppViewModel
import java.time.LocalDate

@Composable
fun TrendsScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()
    var days by remember { mutableStateOf(30) }

    val series = remember(ui.weightEntries, days) {
        val cutoff = LocalDate.now().minusDays(days.toLong() - 1)
        ui.weightEntries.filter { it.date >= cutoff }.sortedBy { it.date }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.trends_title), style = MaterialTheme.typography.headlineSmall)

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = days == 7,
                onClick = { days = 7 },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text(stringResource(R.string.last_7_days)) }
            SegmentedButton(
                selected = days == 30,
                onClick = { days = 30 },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text(stringResource(R.string.last_30_days)) }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (series.size < 2) {
                    Text("Not enough data to plot yet.")
                } else {
                    LineChart(series = series, modifier = Modifier.fillMaxWidth().height(180.dp))
                }
            }
        }
    }
}

@Composable
private fun LineChart(series: List<WeightEntryModel>, modifier: Modifier = Modifier) {
    val min = series.minOf { it.weightKg }
    val max = series.maxOf { it.weightKg }
    val range = (max - min).takeIf { it > 0.001 } ?: 1.0

    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    val dotColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padding = 16.dp.toPx()
        val xStep = (w - padding * 2) / (series.size - 1).toFloat()

        fun yFor(value: Double): Float {
            val norm = ((value - min) / range).toFloat()
            return (h - padding) - norm * (h - padding * 2)
        }

        val path = Path()
        series.forEachIndexed { i, e ->
            val x = padding + xStep * i
            val y = yFor(e.weightKg)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawLine(
            color = axisColor,
            start = Offset(padding, h - padding),
            end = Offset(w - padding, h - padding),
            strokeWidth = 2f
        )

        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )

        series.forEachIndexed { i, e ->
            val x = padding + xStep * i
            val y = yFor(e.weightKg)
            drawCircle(color = dotColor, radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
