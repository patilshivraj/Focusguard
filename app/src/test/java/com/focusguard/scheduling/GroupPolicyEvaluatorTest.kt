package com.focusguard.scheduling

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class GroupPolicyEvaluatorTest {
    private val evaluator = GroupPolicyEvaluator(ZoneId.of("UTC"))

    @Test
    fun weeklyBlock_activeInsideSlot() {
        val group = BlockGroup(
            name = "Social",
            packageNames = setOf("com.instagram.android"),
            policyType = GroupPolicyType.WEEKLY_BLOCK,
            weeklySlots = listOf(
                WeeklyTimeSlot(
                    daysOfWeekBitmask = ScheduleRule.SUNDAY or ScheduleRule.MONDAY or ScheduleRule.TUESDAY,
                    startMinutes = 10 * 60,
                    endMinutes = 13 * 60,
                ),
            ),
        )
        val monday11 = ZonedDateTime.of(2025, 10, 6, 11, 0, 0, 0, ZoneId.of("UTC")).toInstant()
        val result = evaluator.evaluateGroup(group, null, monday11)
        assertTrue(result.shouldBlock)
    }

    @Test
    fun weeklyBlock_inactiveOutsideSlot() {
        val group = BlockGroup(
            name = "Social",
            packageNames = setOf("com.instagram.android"),
            policyType = GroupPolicyType.WEEKLY_BLOCK,
            weeklySlots = listOf(
                WeeklyTimeSlot(
                    daysOfWeekBitmask = ScheduleRule.MONDAY,
                    startMinutes = 10 * 60,
                    endMinutes = 13 * 60,
                ),
            ),
        )
        val monday15 = ZonedDateTime.of(2025, 10, 6, 15, 0, 0, 0, ZoneId.of("UTC")).toInstant()
        val result = evaluator.evaluateGroup(group, null, monday15)
        assertFalse(result.shouldBlock)
    }
}
