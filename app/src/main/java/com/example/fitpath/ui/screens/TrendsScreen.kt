// File: app/src/main/java/com/example/fitpath/ui/screens/TrendsScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.fitpath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.ui.vm.AppViewModel
import java.time.LocalDate

@Composable
fun TrendsScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()
    val profile by vm.profile.collectAsState() // 获取用户配置以读取目标体重
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
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // --- 新增：图例 (Legend) ---
                // 在这里“单股给出”说明，分别解释实线和虚线代表什么
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = MaterialTheme.colorScheme.primary, text = stringResource(R.string.weight_kg))

                    // 如果设置了目标体重，才显示目标体重的图例
                    if (profile.targetWeightKg != null) {
                        Spacer(Modifier.width(24.dp))
                        LegendItem(color = MaterialTheme.colorScheme.tertiary, text = stringResource(R.string.target_weight))
                    }
                }

                if (series.size < 2) {
                    // 数据不足时显示提示
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Text("Not enough data to plot yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LineChart(
                        series = series,
                        targetWeight = profile.targetWeightKg,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
            }
        }
    }
}

// 独立的图例组件
@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LineChart(
    series: List<WeightEntryModel>,
    targetWeight: Double?,
    modifier: Modifier = Modifier
) {
    // 1. 计算数值范围，必须包含目标体重，以免目标线画到图表外面去
    val weights = series.map { it.weightKg }
    val allValues = if (targetWeight != null) weights + targetWeight else weights

    val min = allValues.minOrNull() ?: 0.0
    val max = allValues.maxOrNull() ?: 1.0
    val range = (max - min).takeIf { it > 0.001 } ?: 1.0

    // 定义颜色
    val lineColor = MaterialTheme.colorScheme.primary
    val targetColor = MaterialTheme.colorScheme.tertiary
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val dotColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // 留白，防止点画在边缘被切掉
        val padding = 16.dp.toPx()
        val xStep = (w - padding * 2) / (series.size - 1).toFloat()

        // Y轴坐标转换函数
        fun yFor(value: Double): Float {
            val norm = ((value - min) / range).toFloat()
            // (h - padding) 是底部起点，norm * (h - 2*padding) 是高度偏移
            return (h - padding) - norm * (h - padding * 2)
        }

        // 2. 画 X 轴底线
        drawLine(
            color = axisColor,
            start = Offset(padding, h - padding),
            end = Offset(w - padding, h - padding),
            strokeWidth = 2f
        )

        // 3. 画目标体重线 (虚线)
        if (targetWeight != null) {
            val yTarget = yFor(targetWeight)
            drawLine(
                color = targetColor,
                start = Offset(padding, yTarget),
                end = Offset(w - padding, yTarget),
                strokeWidth = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f) // 虚线效果
            )
        }

        // 4. 画实际体重曲线 (实线)
        val path = Path()
        series.forEachIndexed { i, e ->
            val x = padding + xStep * i
            val y = yFor(e.weightKg)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 5f)
        )

        // 5. 画数据点圆圈
        series.forEachIndexed { i, e ->
            val x = padding + xStep * i
            val y = yFor(e.weightKg)
            drawCircle(
                color = dotColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)