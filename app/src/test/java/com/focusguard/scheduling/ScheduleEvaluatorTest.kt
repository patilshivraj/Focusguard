package com.focusguard.scheduling

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleEvaluatorTest {
    private val zone = ZoneId.of("America/New_York")
    private val evaluator = ScheduleEvaluator(zone)

    @Test
    fun overnightWeekdayBlock_activeAtNight() {
        val rule = ScheduleRule(
            packageName = "com.instagram.android",
            daysOfWeekBitmask = ScheduleRule.WEEKDAYS,
            startMinutes = 22 * 60,
            endMinutes = 8 * 60,
        )
        val monday2300 = zdt(2025, 10, 6, 23, 0) // Monday
        val result = evaluator.evaluate("com.instagram.android", listOf(rule), monday2300.toInstant())
        assertTrue(result.isBlocked)
    }

    @Test
    fun overnightWeekdayBlock_inactiveMidday() {
        val rule = ScheduleRule(
            packageName = "com.instagram.android",
            daysOfWeekBitmask = ScheduleRule.WEEKDAYS,
            startMinutes = 22 * 60,
            endMinutes = 8 * 60,
        )
        val monday1200 = zdt(2025, 10, 6, 12, 0)
        val result = evaluator.evaluate("com.instagram.android", listOf(rule), monday1200.toInstant())
        assertFalse(result.isBlocked)
    }

    @Test
    fun alwaysBlocked() {
        val rule = ScheduleRule(
            packageName = "com.example",
            daysOfWeekBitmask = ScheduleRule.ALL_DAYS,
            startMinutes = 0,
            endMinutes = 0,
            alwaysBlocked = true,
        )
        val result = evaluator.evaluate("com.example", listOf(rule), Instant.now())
        assertTrue(result.isBlocked)
    }

    private fun zdt(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime {
        return ZonedDateTime.of(
            LocalDate.of(year, month, day),
            java.time.LocalTime.of(hour, minute),
            zone,
        )
    }
}
