package com.focusguard.ui.scheduling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusguard.scheduling.ScheduleRule

data class ScreenTimeLimitEditRequest(
    val dayMask: Int,
    val initialMinutes: Int,
)

@Composable
fun ScreenTimeLimitDialog(
    request: ScreenTimeLimitEditRequest,
    onDismiss: () -> Unit,
    onConfirm: (dayMask: Int, limitMinutes: Int) -> Unit,
) {
    var dayMask by remember(request) { mutableIntStateOf(request.dayMask) }
    val initialNoLimit = request.initialMinutes < 0
    var noLimit by remember(request) { mutableStateOf(initialNoLimit) }
    var mins by remember(request) {
        mutableIntStateOf(if (request.initialMinutes < 0) 60 else request.initialMinutes)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set screen time limit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Choose one or more days, then set a daily limit.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { dayMask = ScheduleRule.WEEKDAYS },
                        modifier = Modifier.weight(1f),
                    ) { Text("Weekdays") }
                    OutlinedButton(
                        onClick = { dayMask = ScheduleRule.SATURDAY or ScheduleRule.SUNDAY },
                        modifier = Modifier.weight(1f),
                    ) { Text("Weekend") }
                    OutlinedButton(
                        onClick = { dayMask = ScheduleRule.ALL_DAYS },
                        modifier = Modifier.weight(1f),
                    ) { Text("All") }
                }
                DayOfWeekSelector(
                    dayMask = dayMask,
                    onDayMaskChange = { dayMask = it },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("No limit on selected days")
                    Switch(checked = noLimit, onCheckedChange = { noLimit = it })
                }
                if (!noLimit) {
                    Text("Maximum screen time: $mins mins")
                    Slider(
                        value = mins.toFloat(),
                        onValueChange = { mins = it.toInt() },
                        valueRange = 0f..180f,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dayMask == 0) return@TextButton
                    onConfirm(dayMask, if (noLimit) -1 else mins)
                },
                enabled = dayMask != 0,
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

fun applyScreenTimeLimitToMask(
    dailyLimits: List<Int>,
    dayMask: Int,
    limitMinutes: Int,
): List<Int> {
    val updated = dailyLimits.toMutableList()
    dayBitsOrdered.forEachIndexed { index, bit ->
        if ((dayMask and bit) != 0) {
            updated[index] = limitMinutes
        }
    }
    return updated
}
