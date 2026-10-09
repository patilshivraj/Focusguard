package com.focusguard.blocking

import com.focusguard.scheduling.ScheduleRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class BlockingPolicyTest {
    private val policy = BlockingPolicy()

    @Test
    fun blockNowOverridesSchedule() {
        val state = BlockingPolicyState(
            blockingEnabled = true,
            blockNow = true,
            blockedPackages = setOf("com.test"),
            schedules = emptyList(),
        )
        val decision = policy.shouldBlock("com.test", state)
        assertTrue(decision.shouldBlock)
    }

    @Test
    fun blockedListDefaultWhenNoSchedule() {
        val state = BlockingPolicyState(
            blockingEnabled = true,
            blockNow = false,
            blockedPackages = setOf("com.test"),
            schedules = emptyList(),
        )
        val decision = policy.shouldBlock("com.test", state)
        assertTrue(decision.shouldBlock)
    }

    @Test
    fun outsideScheduleNotBlocked() {
        val rule = ScheduleRule(
            packageName = "com.test",
            daysOfWeekBitmask = ScheduleRule.WEEKDAYS,
            startMinutes = 22 * 60,
            endMinutes = 8 * 60,
        )
        val state = BlockingPolicyState(
            blockingEnabled = true,
            blockNow = false,
            blockedPackages = setOf("com.test"),
            schedules = listOf(rule),
        )
        val mondayNoon = ZonedDateTime.of(2025, 10, 6, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant()
        val decision = policy.shouldBlock("com.test", state, null, mondayNoon)
        assertFalse(decision.shouldBlock)
    }
}
