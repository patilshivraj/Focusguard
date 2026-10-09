package com.focusguard.ui.scheduling

import com.focusguard.scheduling.ScheduleRule
import com.focusguard.util.TimeFormatting

fun formatSlotRange(startMinutes: Int, endMinutes: Int): String {
    return "${TimeFormatting.formatMinutesFromMidnight(startMinutes)} – " +
        TimeFormatting.formatMinutesFromMidnight(endMinutes)
}

fun formatDayMask(mask: Int): String {
    val labels = mutableListOf<String>()
    if (mask and ScheduleRule.SUNDAY != 0) labels += "SUN"
    if (mask and ScheduleRule.MONDAY != 0) labels += "MON"
    if (mask and ScheduleRule.TUESDAY != 0) labels += "TUE"
    if (mask and ScheduleRule.WEDNESDAY != 0) labels += "WED"
    if (mask and ScheduleRule.THURSDAY != 0) labels += "THU"
    if (mask and ScheduleRule.FRIDAY != 0) labels += "FRI"
    if (mask and ScheduleRule.SATURDAY != 0) labels += "SAT"
    return labels.joinToString(" ")
}

fun toggleDay(mask: Int, dayBit: Int): Int =
    if ((mask and dayBit) != 0) mask and dayBit.inv() else mask or dayBit

val dayBitsOrdered = listOf(
    ScheduleRule.SUNDAY,
    ScheduleRule.MONDAY,
    ScheduleRule.TUESDAY,
    ScheduleRule.WEDNESDAY,
    ScheduleRule.THURSDAY,
    ScheduleRule.FRIDAY,
    ScheduleRule.SATURDAY,
)

val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
