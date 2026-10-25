package com.example.insulia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.DateFormats
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.GlucoseStatus

/**
 * Trend chart for weekly glucose readings.
 *
 * The chart shows one reading per week (at least 4 times per month) so the
 * patient tracks progress across weeks rather than day by day.
 */
@Composable
fun GlucoseTrendCard(
    readings: List<GlucoseReading>,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outline
    val trendColor = MaterialTheme.colorScheme.primary
    val targetColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    InsuliaCard(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.glucose_trend_title, readings.size),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (readings.isEmpty()) {
                BoxedMessage(
                    text = stringResource(R.string.glucose_levels_empty),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    drawTrend(
                        readings = readings,
                        gridColor = gridColor,
                        trendColor = trendColor,
                        targetColor = targetColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                readings.forEach { reading ->
                    Text(
                        text = DateFormats.weekLabel(reading.recordedAt),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.glucose_trend_target_range,
                    GlucoseStatus.LOW_LIMIT,
                    GlucoseStatus.HIGH_LIMIT
                ),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Draws the target band, the grid lines, the trend line and the data points.
 */
private fun DrawScope.drawTrend(
    readings: List<GlucoseReading>,
    gridColor: Color,
    trendColor: Color,
    targetColor: Color
) {
    val values = readings.map { it.valueMgDl.toFloat() }
    val minValue = minOf(values.min(), GlucoseStatus.LOW_LIMIT.toFloat()) - 20f
    val maxValue = maxOf(values.max(), GlucoseStatus.HIGH_LIMIT.toFloat()) + 20f
    val range = (maxValue - minValue).coerceAtLeast(1f)

    fun yFor(value: Float): Float =
        size.height - ((value - minValue) / range * size.height)

    // Target range band (70 - 140 mg/dL).
    val bandTop = yFor(GlucoseStatus.HIGH_LIMIT.toFloat())
    val bandBottom = yFor(GlucoseStatus.LOW_LIMIT.toFloat())
    drawRect(
        color = targetColor,
        topLeft = Offset(0f, bandTop),
        size = Size(size.width, (bandBottom - bandTop).coerceAtLeast(0f))
    )

    // Horizontal grid lines.
    for (i in 0..4) {
        val y = size.height * i / 4f
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.5f
        )
    }

    if (values.size == 1) {
        drawCircle(
            color = trendColor,
            radius = 5f,
            center = Offset(size.width / 2f, yFor(values.first()))
        )
        return
    }

    val dx = size.width / (values.size - 1)

    // Trend line.
    for (i in 0 until values.size - 1) {
        drawLine(
            color = trendColor,
            start = Offset(i * dx, yFor(values[i])),
            end = Offset((i + 1) * dx, yFor(values[i + 1])),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    // Data points.
    values.forEachIndexed { index, value ->
        drawCircle(
            color = trendColor,
            radius = 5f,
            center = Offset(index * dx, yFor(value))
        )
    }
}

@Composable
private fun BoxedMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
