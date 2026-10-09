package com.focusguard.scheduling

import java.util.UUID

object ScheduleTemplates {
    fun workHours(): List<WeeklyTimeSlot> = listOf(
        WeeklyTimeSlot(
            id = UUID.randomUUID().toString(),
            daysOfWeekBitmask = ScheduleRule.WEEKDAYS,
            startMinutes = 9 * 60,
            endMinutes = 17 * 60,
        ),
    )

    fun evening(): List<WeeklyTimeSlot> = listOf(
        WeeklyTimeSlot(
            id = UUID.randomUUID().toString(),
            daysOfWeekBitmask = ScheduleRule.WEEKDAYS,
            startMinutes = 18 * 60,
            endMinutes = 22 * 60,
        ),
    )

    fun weekend(): List<WeeklyTimeSlot> = listOf(
        WeeklyTimeSlot(
            id = UUID.randomUUID().toString(),
            daysOfWeekBitmask = ScheduleRule.SATURDAY or ScheduleRule.SUNDAY,
            startMinutes = 0,
            endMinutes = 24 * 60,
        ),
    )

    fun sleepTime(): List<WeeklyTimeSlot> = listOf(
        WeeklyTimeSlot(
            id = UUID.randomUUID().toString(),
            daysOfWeekBitmask = ScheduleRule.ALL_DAYS,
            startMinutes = 22 * 60,
            endMinutes = 7 * 60,
        ),
    )
}
