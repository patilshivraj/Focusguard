package com.focusguard.ui.scheduling

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.focusguard.scheduling.WeeklyTimeSlot
import com.focusguard.ui.theme.HeatmapBlock
import com.focusguard.ui.theme.HeatmapTrack

@Composable
fun WeekHeatmap(
    slots: List<WeeklyTimeSlot>,
    modifier: Modifier = Modifier,
) {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    val timeLabels = listOf("12a", "6a", "12p", "6p")
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier
                    .width(28.dp)
                    .height(180.dp)
                    .padding(top = 20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                timeLabels.forEach { label ->
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                ) {
                    dayLabels.forEach { label ->
                        Text(label, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 8.dp),
                ) {
                    val chartTop = 4f
                    val chartH = size.height - chartTop
                    val colW = size.width / 7f
                    for (i in 0 until 7) {
                        val left = i * colW + 4f
                        drawRoundRect(
                            color = HeatmapTrack,
                            topLeft = Offset(left, chartTop),
                            size = Size(colW - 8f, chartH),
                            cornerRadius = CornerRadius(16f, 16f),
                        )
                    }
                    slots.forEach { slot ->
                        for (day in 0 until 7) {
                            val bit = dayBitForIndex(day)
                            if ((slot.daysOfWeekBitmask and bit) == 0) continue
                            val startFrac = slot.startMinutes.coerceIn(0, 24 * 60) / (24f * 60f)
                            val endFrac = slot.endMinutes.coerceIn(0, 24 * 60) / (24f * 60f)
                            val top = if (startFrac < endFrac) startFrac else 0f
                            val bottom = if (startFrac < endFrac) endFrac else 1f
                            val left = day * colW + 6f
                            val y1 = chartTop + chartH * top
                            val y2 = chartTop + chartH * bottom
                            drawRoundRect(
                                color = HeatmapBlock,
                                topLeft = Offset(left, y1),
                                size = Size(colW - 12f, (y2 - y1).coerceAtLeast(10f)),
                                cornerRadius = CornerRadius(10f, 10f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dayBitForIndex(index: Int): Int =
    when (index) {
        0 -> com.focusguard.scheduling.ScheduleRule.SUNDAY
        1 -> com.focusguard.scheduling.ScheduleRule.MONDAY
        2 -> com.focusguard.scheduling.ScheduleRule.TUESDAY
        3 -> com.focusguard.scheduling.ScheduleRule.WEDNESDAY
        4 -> com.focusguard.scheduling.ScheduleRule.THURSDAY
        5 -> com.focusguard.scheduling.ScheduleRule.FRIDAY
        6 -> com.focusguard.scheduling.ScheduleRule.SATURDAY
        else -> 0
    }
