package com.focusguard.scheduling

import java.util.UUID

enum class GroupPolicyType {
    MANUAL_ONLY,
    WEEKLY_BLOCK,
    SCREEN_TIME_DAILY,
    SCREEN_TIME_HOURLY,
    ALWAYS_BLOCKED,
}

enum class ScreenTimeLimitMode {
    DAILY,
    HOURLY,
}

data class WeeklyTimeSlot(
    val id: String = UUID.randomUUID().toString(),
    val daysOfWeekBitmask: Int,
    val startMinutes: Int,
    val endMinutes: Int,
)

data class ScreenTimeLimitConfig(
    val mode: ScreenTimeLimitMode = ScreenTimeLimitMode.DAILY,
    /** Index 0 = Sunday … 6 = Saturday. -1 = no limit that day. */
    val dailyLimitsByDay: List<Int> = List(7) { -1 },
    val hourlyLimitMinutes: Int = 30,
)

data class BlockGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageNames: Set<String> = emptySet(),
    val policyType: GroupPolicyType = GroupPolicyType.MANUAL_ONLY,
    val alwaysBlocked: Boolean = false,
    val weeklySlots: List<WeeklyTimeSlot> = emptyList(),
    val screenTimeLimit: ScreenTimeLimitConfig = ScreenTimeLimitConfig(),
) {
    fun effectivePolicyType(): GroupPolicyType {
        if (alwaysBlocked || policyType == GroupPolicyType.ALWAYS_BLOCKED) return GroupPolicyType.ALWAYS_BLOCKED
        return policyType
    }
}
