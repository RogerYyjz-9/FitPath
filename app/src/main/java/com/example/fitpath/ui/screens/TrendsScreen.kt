// File: app/src/main/java/com/example/fitpath/ui/screens/TrendsScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.fitpath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.ui.vm.AppViewModel
import java.time.LocalDate
import kotlin.math.max
import java.time.temporal.ChronoUnit

@Composable
fun TrendsScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()
    val profile by vm.profile.collectAsState()
    var days by remember { mutableStateOf(7) }

    val series = remember(ui.weightEntries, days) {
        val cutoff = LocalDate.now().minusDays(days.toLong() - 1)
        ui.weightEntries.filter { it.date >= cutoff }.sortedBy { it.date }
    }

    val movingAverage = remember(series) { movingAverage(series, window = 7) }

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
                    Text(stringResource(R.string.trends_not_enough))
                } else {
                    LineChart(
                        actualSeries = series,
                        averageSeries = movingAverage,
                        targetWeight = profile.targetWeightKg,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                    LegendRow(targetVisible = profile.targetWeightKg != null)
                    Text(
                        text = stringResource(R.string.trends_hint_moving_avg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    actualSeries: List<WeightEntryModel>,
    averageSeries: List<WeightEntryModel>,
    targetWeight: Double?,
    modifier: Modifier = Modifier
) {
    val dates = actualSeries.map { it.date }
    val startDate = dates.minOrNull() ?: return
    val endDate = dates.maxOrNull() ?: return
    val totalDays = max(1, ChronoUnit.DAYS.between(startDate, endDate).toInt())

    val values = buildList {
        addAll(actualSeries.map { it.weightKg })
        addAll(averageSeries.map { it.weightKg })
        targetWeight?.let { add(it) }
    }
    val minValue = (values.minOrNull() ?: 0.0) - 1.0
    val maxValue = (values.maxOrNull() ?: 0.0) + 1.0
    val range = max(0.1, maxValue - minValue)

    val actualColor = MaterialTheme.colorScheme.primary
    val avgColor = MaterialTheme.colorScheme.tertiary
    val targetColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padding = 16.dp.toPx()
        val chartWidth = w - padding * 2
        val chartHeight = h - padding * 2

        fun xFor(date: LocalDate): Float {
            val delta = ChronoUnit.DAYS.between(startDate, date).toFloat()
            val fraction = delta / totalDays.toFloat()
            return padding + chartWidth * fraction
        }

        fun yFor(value: Double): Float {
            val norm = ((value - minValue) / range).toFloat()
            return (h - padding) - norm * chartHeight
        }

        fun drawLine(series: List<WeightEntryModel>, color: androidx.compose.ui.graphics.Color, stroke: Float, dotted: Boolean = false) {
            if (series.size < 2) return
            val path = Path()
            series.forEachIndexed { index, entry ->
                val x = xFor(entry.date)
                val y = yFor(entry.weightKg)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = stroke,
                    pathEffect = if (dotted) PathEffect.dashPathEffect(floatArrayOf(12f, 12f)) else null
                )
            )
        }

        drawLine(series = actualSeries, color = actualColor, stroke = 6f)
        drawLine(series = averageSeries, color = avgColor, stroke = 4f)

        if (targetWeight != null) {
            drawLine(
                series = listOf(
                    WeightEntryModel(id = -1, date = startDate, weightKg = targetWeight),
                    WeightEntryModel(id = -2, date = endDate, weightKg = targetWeight)
                ),
                color = targetColor,
                stroke = 3f,
                dotted = true
            )
        }

        actualSeries.forEach { entry ->
            drawCircle(
                color = actualColor,
                radius = 6f,
                center = Offset(xFor(entry.date), yFor(entry.weightKg))
            )
        }
    }
}

private fun movingAverage(series: List<WeightEntryModel>, window: Int): List<WeightEntryModel> {
    if (series.isEmpty()) return emptyList()
    val sorted = series.sortedBy { it.date }
    val result = mutableListOf<WeightEntryModel>()
    val weights = mutableListOf<Double>()
    sorted.forEachIndexed { index, entry ->
        weights.add(entry.weightKg)
        if (weights.size > window) weights.removeAt(0)
        val avg = weights.average()
        result.add(entry.copy(weightKg = avg, id = entry.id))
    }
    return result
}

@Composable
private fun LegendRow(targetVisible: Boolean) {
    val spacing = 12.dp
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
        LegendItem(
            color = MaterialTheme.colorScheme.primary,
            label = stringResource(R.string.legend_actual),
            dotted = false,
            showDot = true
        )
        LegendItem(
            color = MaterialTheme.colorScheme.tertiary,
            label = stringResource(R.string.legend_average),
            dotted = false,
            showDot = false
        )
        if (targetVisible) {
            LegendItem(
                color = MaterialTheme.colorScheme.secondary,
                label = stringResource(R.string.legend_target),
                dotted = true,
                showDot = false
            )
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String, dotted: Boolean, showDot: Boolean) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Canvas(modifier = Modifier.height(16.dp).width(32.dp)) {
            val y = size.height / 2
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 4.dp.toPx(),
                pathEffect = if (dotted) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
            )
        }
        if (showDot) {
            Spacer(modifier = Modifier.width(4.dp))
            Canvas(modifier = Modifier.height(10.dp).width(10.dp)) {
                drawCircle(color = color, radius = size.minDimension / 2)
            }
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
