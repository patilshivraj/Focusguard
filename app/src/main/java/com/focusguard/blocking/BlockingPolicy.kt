package com.focusguard.blocking

import com.focusguard.scheduling.BlockGroup
import com.focusguard.scheduling.GroupPolicyEvaluator
import com.focusguard.scheduling.ScheduleEvaluator
import com.focusguard.scheduling.ScheduleRule
import com.focusguard.scheduling.ScreenTimeUsageTracker
import java.time.Instant

data class BlockingPolicyState(
    val blockingEnabled: Boolean,
    val blockNow: Boolean,
    val blockedPackages: Set<String>,
    val schedules: List<ScheduleRule>,
    val blockGroups: List<BlockGroup> = emptyList(),
)

class BlockingPolicy(
    private val scheduleEvaluator: ScheduleEvaluator = ScheduleEvaluator(),
    private val groupPolicyEvaluator: GroupPolicyEvaluator = GroupPolicyEvaluator(),
) {
    fun shouldBlock(
        packageName: String,
        state: BlockingPolicyState,
        usageTracker: ScreenTimeUsageTracker? = null,
        now: Instant = Instant.now(),
    ): BlockDecision {
        if (!state.blockingEnabled) {
            return BlockDecision(false, "blocking_disabled")
        }
        if (!state.blockedPackages.contains(packageName)) {
            return BlockDecision(false, "not_in_block_list")
        }
        if (state.blockNow) {
            return BlockDecision(true, "block_now", null)
        }

        val groupResult = groupPolicyEvaluator.evaluatePackage(
            packageName = packageName,
            groups = state.blockGroups,
            usageTracker = usageTracker,
            now = now,
        )
        if (groupResult != null) {
            return BlockDecision(
                shouldBlock = groupResult.shouldBlock,
                reason = groupResult.reason,
                availableAgainAt = groupResult.availableAgainAt,
            )
        }

        val packageSchedules = state.schedules.filter { it.packageName == packageName }
        if (packageSchedules.any { it.alwaysBlocked }) {
            return BlockDecision(true, "always_blocked", null)
        }
        if (packageSchedules.isEmpty()) {
            return BlockDecision(true, "blocked_list_default", null)
        }

        val scheduleEval = scheduleEvaluator.evaluate(packageName, state.schedules, now)
        if (scheduleEval.isBlocked) {
            return BlockDecision(
                shouldBlock = true,
                reason = "schedule_active",
                availableAgainAt = scheduleEval.availableAgainAt,
            )
        }

        return BlockDecision(false, "outside_schedule", scheduleEval.availableAgainAt)
    }
}
