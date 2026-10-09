package com.focusguard.data

import com.focusguard.blocking.BlockingPolicyState
import com.focusguard.data.proto.BlockGroupProto
import com.focusguard.data.proto.FocusGuardSettings
import com.focusguard.data.proto.ScheduleRuleProto
import com.focusguard.data.proto.ScreenTimeLimitProto
import com.focusguard.data.proto.WeeklyTimeSlotProto
import com.focusguard.scheduling.BlockGroup
import com.focusguard.scheduling.GroupPolicyType
import com.focusguard.scheduling.ScheduleRule
import com.focusguard.scheduling.ScreenTimeLimitConfig
import com.focusguard.scheduling.ScreenTimeLimitMode
import com.focusguard.scheduling.WeeklyTimeSlot

object SettingsMapper {
    fun toPolicyState(settings: FocusGuardSettings): BlockingPolicyState {
        val groups = settings.blockGroupsList.map { it.toDomain() }
        val groupPackages = groups.flatMap { it.packageNames }.toSet()
        val blocked = settings.blockedPackagesList.toSet() + groupPackages
        return BlockingPolicyState(
            blockingEnabled = isBlockingEnabled(settings),
            blockNow = settings.blockNow,
            blockedPackages = blocked,
            schedules = settings.schedulesList.map { it.toDomain() },
            blockGroups = groups,
        )
    }

    fun scheduleToProto(rule: ScheduleRule): ScheduleRuleProto =
        ScheduleRuleProto.newBuilder()
            .setId(rule.id)
            .setPackageName(rule.packageName)
            .setDaysOfWeek(rule.daysOfWeekBitmask)
            .setStartMinutes(rule.startMinutes)
            .setEndMinutes(rule.endMinutes)
            .setAlwaysBlocked(rule.alwaysBlocked)
            .build()

    fun blockGroupToProto(group: BlockGroup): BlockGroupProto {
        val limit = group.screenTimeLimit
        val limitProto = ScreenTimeLimitProto.newBuilder()
            .setLimitMode(if (limit.mode == ScreenTimeLimitMode.HOURLY) 1 else 0)
            .addAllDailyLimitsByDay(limit.dailyLimitsByDay)
            .setHourlyLimitMinutes(limit.hourlyLimitMinutes)
            .build()
        return BlockGroupProto.newBuilder()
            .setId(group.id)
            .setName(group.name)
            .addAllPackageNames(group.packageNames)
            .setPolicyType(group.policyType.ordinal)
            .setAlwaysBlocked(group.alwaysBlocked)
            .addAllWeeklySlots(group.weeklySlots.map { slotToProto(it) })
            .setScreenTimeLimit(limitProto)
            .build()
    }

    private fun slotToProto(slot: WeeklyTimeSlot): WeeklyTimeSlotProto =
        WeeklyTimeSlotProto.newBuilder()
            .setId(slot.id)
            .setDaysOfWeek(slot.daysOfWeekBitmask)
            .setStartMinutes(slot.startMinutes)
            .setEndMinutes(slot.endMinutes)
            .build()

    fun isBlockingEnabled(settings: FocusGuardSettings): Boolean {
        if (!settings.hasBlockingEnabled()) return true
        return settings.blockingEnabled
    }

    private fun ScheduleRuleProto.toDomain(): ScheduleRule =
        ScheduleRule(
            id = id.ifBlank { java.util.UUID.randomUUID().toString() },
            packageName = packageName,
            daysOfWeekBitmask = daysOfWeek,
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            alwaysBlocked = alwaysBlocked,
        )

    private fun BlockGroupProto.toDomain(): BlockGroup {
        val limit = screenTimeLimit
        val daily = if (limit.dailyLimitsByDayList.size == 7) {
            limit.dailyLimitsByDayList
        } else {
            List(7) { -1 }
        }
        return BlockGroup(
            id = id.ifBlank { java.util.UUID.randomUUID().toString() },
            name = name.ifBlank { "Block group" },
            packageNames = packageNamesList.toSet(),
            policyType = GroupPolicyType.entries.getOrElse(policyType) { GroupPolicyType.MANUAL_ONLY },
            alwaysBlocked = alwaysBlocked,
            weeklySlots = weeklySlotsList.map { slot ->
                WeeklyTimeSlot(
                    id = slot.id.ifBlank { java.util.UUID.randomUUID().toString() },
                    daysOfWeekBitmask = slot.daysOfWeek,
                    startMinutes = slot.startMinutes,
                    endMinutes = slot.endMinutes,
                )
            },
            screenTimeLimit = ScreenTimeLimitConfig(
                mode = if (limit.limitMode == 1) ScreenTimeLimitMode.HOURLY else ScreenTimeLimitMode.DAILY,
                dailyLimitsByDay = daily,
                hourlyLimitMinutes = limit.hourlyLimitMinutes,
            ),
        )
    }
}
