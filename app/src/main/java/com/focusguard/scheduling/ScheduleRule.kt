package com.focusguard.scheduling

import java.util.UUID

data class ScheduleRule(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val daysOfWeekBitmask: Int,
    val startMinutes: Int,
    val endMinutes: Int,
    val alwaysBlocked: Boolean = false,
) {
    companion object {
        const val MONDAY = 1
        const val TUESDAY = 2
        const val WEDNESDAY = 4
        const val THURSDAY = 8
        const val FRIDAY = 16
        const val SATURDAY = 32
        const val SUNDAY = 64
        const val WEEKDAYS = MONDAY or TUESDAY or WEDNESDAY or THURSDAY or FRIDAY
        const val ALL_DAYS = WEEKDAYS or SATURDAY or SUNDAY
    }
}
