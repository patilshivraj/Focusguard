package com.focusguard.scheduling

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class ScheduleEvaluation(
    val isBlocked: Boolean,
    val activeRule: ScheduleRule?,
    val availableAgainAt: Instant?,
)

class ScheduleEvaluator(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun evaluate(
        packageName: String,
        schedules: List<ScheduleRule>,
        now: Instant = Instant.now(),
    ): ScheduleEvaluation {
        val rules = schedules.filter { it.packageName == packageName }
        if (rules.isEmpty()) {
            return ScheduleEvaluation(isBlocked = false, activeRule = null, availableAgainAt = null)
        }

        val zoned = now.atZone(zoneId)
        val date = zoned.toLocalDate()
        val time = zoned.toLocalTime()
        val dayBit = dayBitFor(date)

        val active = rules.firstOrNull { rule ->
            rule.alwaysBlocked || isWithinSchedule(rule, date, time, dayBit)
        }

        if (active == null) {
            val nextStart = nextBlockStart(rules, date, time, dayBit)
            return ScheduleEvaluation(isBlocked = false, activeRule = null, availableAgainAt = nextStart)
        }

        if (active.alwaysBlocked) {
            return ScheduleEvaluation(isBlocked = true, activeRule = active, availableAgainAt = null)
        }

        val endInstant = endOfActiveWindow(active, date, time, dayBit)
        return ScheduleEvaluation(isBlocked = true, activeRule = active, availableAgainAt = endInstant)
    }

    fun nextScheduleBoundary(
        schedules: List<ScheduleRule>,
        now: Instant = Instant.now(),
    ): Instant? {
        if (schedules.isEmpty()) return null
        val zoned = now.atZone(zoneId)
        val date = zoned.toLocalDate()
        val time = zoned.toLocalTime()
        val dayBit = dayBitFor(date)
        return schedules.mapNotNull { rule ->
            if (rule.alwaysBlocked) null else nextBlockStart(listOf(rule), date, time, dayBit)
        }.minOrNull()
    }

    private fun isWithinSchedule(
        rule: ScheduleRule,
        date: LocalDate,
        time: LocalTime,
        dayBit: Int,
    ): Boolean {
        if ((rule.daysOfWeekBitmask and dayBit) == 0) return false
        val start = LocalTime.ofSecondOfDay(rule.startMinutes * 60L)
        val end = LocalTime.ofSecondOfDay(rule.endMinutes * 60L)
        val minutes = time.hour * 60 + time.minute
        if (rule.startMinutes == rule.endMinutes) return false
        if (rule.startMinutes < rule.endMinutes) {
            return minutes in rule.startMinutes until rule.endMinutes
        }
        // Overnight window e.g. 22:00 -> 08:00
        return minutes >= rule.startMinutes || minutes < rule.endMinutes
    }

    private fun endOfActiveWindow(
        rule: ScheduleRule,
        date: LocalDate,
        time: LocalTime,
        dayBit: Int,
    ): Instant? {
        if (rule.alwaysBlocked) return null
        val endDate = if (rule.startMinutes < rule.endMinutes) {
            date
        } else {
            val minutes = time.hour * 60 + time.minute
            if (minutes >= rule.startMinutes) date.plusDays(1) else date
        }
        val endTime = LocalTime.ofSecondOfDay(rule.endMinutes * 60L)
        return LocalDateTime.of(endDate, endTime).atZone(zoneId).toInstant()
    }

    private fun nextBlockStart(
        rules: List<ScheduleRule>,
        date: LocalDate,
        time: LocalTime,
        dayBit: Int,
    ): Instant? {
        val nowMinutes = time.hour * 60 + time.minute
        var candidate: Instant? = null
        for (offset in 0..7) {
            val d = date.plusDays(offset.toLong())
            val bit = dayBitFor(d)
            rules.filter { !it.alwaysBlocked && (it.daysOfWeekBitmask and bit) != 0 }.forEach { rule ->
                val startToday = LocalDateTime.of(d, LocalTime.ofSecondOfDay(rule.startMinutes * 60L))
                val instant = startToday.atZone(zoneId).toInstant()
                if (offset == 0 && rule.startMinutes <= nowMinutes) return@forEach
                if (candidate == null || instant.isBefore(candidate)) candidate = instant
            }
        }
        return candidate
    }

    private fun dayBitFor(date: LocalDate): Int {
        return when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> ScheduleRule.MONDAY
            java.time.DayOfWeek.TUESDAY -> ScheduleRule.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> ScheduleRule.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> ScheduleRule.THURSDAY
            java.time.DayOfWeek.FRIDAY -> ScheduleRule.FRIDAY
            java.time.DayOfWeek.SATURDAY -> ScheduleRule.SATURDAY
            java.time.DayOfWeek.SUNDAY -> ScheduleRule.SUNDAY
        }
    }
}
