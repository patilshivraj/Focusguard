package com.focusguard.scheduling

import java.time.Instant
import java.time.ZoneId

data class GroupPolicyResult(
    val shouldBlock: Boolean,
    val reason: String,
    val availableAgainAt: Instant? = null,
    val group: BlockGroup? = null,
)

class GroupPolicyEvaluator(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun evaluatePackage(
        packageName: String,
        groups: List<BlockGroup>,
        usageTracker: ScreenTimeUsageTracker?,
        now: Instant = Instant.now(),
    ): GroupPolicyResult? {
        val memberGroups = groups.filter { it.packageNames.contains(packageName) }
        if (memberGroups.isEmpty()) return null

        val results = memberGroups.map { evaluateGroup(it, usageTracker, now) }
        val blocking = results.filter { it.shouldBlock }
        if (blocking.isNotEmpty()) {
            return blocking.maxByOrNull { priority(it.reason) }!!
        }
        return results.firstOrNull { !it.shouldBlock }
    }

    private fun priority(reason: String): Int =
        when (reason) {
            "group_always_blocked" -> 100
            "weekly_block_active" -> 90
            "screen_time_exceeded" -> 80
            else -> 0
        }

    fun evaluateGroup(
        group: BlockGroup,
        usageTracker: ScreenTimeUsageTracker?,
        now: Instant = Instant.now(),
    ): GroupPolicyResult {
        val type = group.effectivePolicyType()
        if (type == GroupPolicyType.ALWAYS_BLOCKED) {
            return GroupPolicyResult(true, "group_always_blocked", null, group)
        }
        when (type) {
            GroupPolicyType.WEEKLY_BLOCK -> {
                if (group.weeklySlots.isEmpty()) {
                    return GroupPolicyResult(false, "weekly_not_configured", null, group)
                }
                val active = WeeklySlotEvaluator.isWithinAnySlot(group.weeklySlots, now, zoneId)
                return if (active) {
                    GroupPolicyResult(
                        shouldBlock = true,
                        reason = "weekly_block_active",
                        availableAgainAt = WeeklySlotEvaluator.nextSlotEnd(group.weeklySlots, now, zoneId),
                        group = group,
                    )
                } else {
                    GroupPolicyResult(false, "outside_weekly_block", null, group)
                }
            }
            GroupPolicyType.SCREEN_TIME_DAILY -> {
                val limit = dailyLimitForToday(group, usageTracker, now)
                if (limit < 0) return GroupPolicyResult(false, "no_daily_limit", null, group)
                val used = usageForGroup(group, usageTracker, daily = true, now)
                return if (used >= limit) {
                    GroupPolicyResult(true, "screen_time_exceeded", null, group)
                } else {
                    GroupPolicyResult(false, "within_daily_limit", null, group)
                }
            }
            GroupPolicyType.SCREEN_TIME_HOURLY -> {
                val limit = group.screenTimeLimit.hourlyLimitMinutes
                if (limit <= 0) return GroupPolicyResult(false, "no_hourly_limit", null, group)
                val used = usageForGroup(group, usageTracker, daily = false, now)
                return if (used >= limit) {
                    GroupPolicyResult(true, "screen_time_exceeded", null, group)
                } else {
                    GroupPolicyResult(false, "within_hourly_limit", null, group)
                }
            }
            GroupPolicyType.MANUAL_ONLY -> {
                return GroupPolicyResult(false, "manual_only", null, group)
            }
            else -> return GroupPolicyResult(false, "unknown", null, group)
        }
    }

    private fun dailyLimitForToday(
        group: BlockGroup,
        usageTracker: ScreenTimeUsageTracker?,
        now: Instant,
    ): Int {
        val limits = group.screenTimeLimit.dailyLimitsByDay
        if (limits.size != 7) return -1
        val index = usageTracker?.dayIndexSundayFirst(now, zoneId) ?: WeeklySlotEvaluator.dayBitFor(
            now.atZone(zoneId).toLocalDate(),
        ).let { bit ->
            when (bit) {
                ScheduleRule.SUNDAY -> 0
                ScheduleRule.MONDAY -> 1
                ScheduleRule.TUESDAY -> 2
                ScheduleRule.WEDNESDAY -> 3
                ScheduleRule.THURSDAY -> 4
                ScheduleRule.FRIDAY -> 5
                ScheduleRule.SATURDAY -> 6
                else -> 0
            }
        }
        return limits.getOrElse(index) { -1 }
    }

    private fun usageForGroup(
        group: BlockGroup,
        usageTracker: ScreenTimeUsageTracker?,
        daily: Boolean,
        now: Instant,
    ): Int {
        if (usageTracker == null || !usageTracker.hasUsageAccess()) return 0
        val start = if (daily) {
            usageTracker.startOfDayMillis(now, zoneId)
        } else {
            usageTracker.startOfHourMillis(now, zoneId)
        }
        return usageTracker.totalForegroundMinutes(group.packageNames, start)
    }
}
