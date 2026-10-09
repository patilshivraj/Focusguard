package com.focusguard.data

import android.content.Context
import com.focusguard.blocking.BlockingPolicyState
import com.focusguard.blocking.LauncherEntryDisableHelper
import com.focusguard.data.proto.FocusGuardSettings
import com.focusguard.scheduling.BlockGroup
import com.focusguard.scheduling.ScheduleRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BlockRepository(private val context: Context) {
    val settings: Flow<FocusGuardSettings> = context.focusGuardDataStore.data

    val policyState: Flow<BlockingPolicyState> = settings.map { SettingsMapper.toPolicyState(it) }

    val blockedPackages: Flow<Set<String>> = policyState.map { it.blockedPackages }

    val blockGroups: Flow<List<BlockGroup>> = policyState.map { it.blockGroups }

    val schedules: Flow<List<ScheduleRule>> = settings.map { list ->
        list.schedulesList.map { proto ->
            ScheduleRule(
                id = proto.id,
                packageName = proto.packageName,
                daysOfWeekBitmask = proto.daysOfWeek,
                startMinutes = proto.startMinutes,
                endMinutes = proto.endMinutes,
                alwaysBlocked = proto.alwaysBlocked,
            )
        }
    }

    val diagnosticRecording: Flow<Boolean> = settings.map { it.diagnosticRecording }
    val diagnosticVerboseEvents: Flow<Boolean> = settings.map { it.diagnosticVerboseEvents }
    val blockingEnabled: Flow<Boolean> = settings.map { SettingsMapper.isBlockingEnabled(it) }
    val blockNow: Flow<Boolean> = settings.map { it.blockNow }

    val blockingOverlayEnabled: Flow<Boolean> = settings.map { settings ->
        if (!settings.hasBlockingOverlayEnabled()) true else settings.blockingOverlayEnabled
    }

    val experimentalDisableLauncherEntry: Flow<Boolean> = settings.map { settings ->
        settings.experimentalDisableLauncherEntry
    }

    suspend fun setBlockingEnabled(enabled: Boolean) = update { it.toBuilder().setBlockingEnabled(enabled).build() }

    suspend fun setBlockingOverlayEnabled(enabled: Boolean) =
        update { it.toBuilder().setBlockingOverlayEnabled(enabled).build() }

    suspend fun setExperimentalDisableLauncherEntry(enabled: Boolean) =
        update { it.toBuilder().setExperimentalDisableLauncherEntry(enabled).build() }

    suspend fun setBlockNow(enabled: Boolean) = update { it.toBuilder().setBlockNow(enabled).build() }

    suspend fun setBlockedPackages(packages: Set<String>) =
        update { it.toBuilder().clearBlockedPackages().addAllBlockedPackages(packages).build() }

    suspend fun upsertBlockGroup(group: BlockGroup) {
        update { current ->
            val others = current.blockGroupsList.filter { it.id != group.id }
            val builder = current.toBuilder().clearBlockGroups()
            others.forEach { builder.addBlockGroups(it) }
            builder.addBlockGroups(SettingsMapper.blockGroupToProto(group))
            syncBlockedPackages(builder)
            builder.build()
        }
    }

    suspend fun deleteBlockGroup(groupId: String) {
        update { current ->
            val builder = current.toBuilder().clearBlockGroups()
            current.blockGroupsList.filter { it.id != groupId }.forEach { builder.addBlockGroups(it) }
            syncBlockedPackages(builder)
            builder.build()
        }
    }

    suspend fun getBlockGroup(groupId: String): BlockGroup? {
        val state = settings.first()
        return SettingsMapper.toPolicyState(state).blockGroups.firstOrNull { it.id == groupId }
    }

    suspend fun toggleBlockedPackage(packageName: String, blocked: Boolean) {
        val experimentalLauncher = settings.first().experimentalDisableLauncherEntry
        update { current ->
            val builder = current.toBuilder()
            val list = current.blockedPackagesList.toMutableSet()
            if (blocked) list.add(packageName) else list.remove(packageName)
            builder.clearBlockedPackages().addAllBlockedPackages(list)
            if (blocked && list.isNotEmpty()) {
                builder.setBlockingEnabled(true)
            }
            builder.build()
        }
        if (experimentalLauncher) {
            if (blocked) {
                LauncherEntryDisableHelper.tryDisableLauncherEntry(context, packageName)
            } else {
                LauncherEntryDisableHelper.tryEnableLauncherEntry(context, packageName)
            }
        }
    }

    suspend fun upsertSchedule(rule: ScheduleRule) {
        update { current ->
            val others = current.schedulesList.filter { it.id != rule.id }
            val builder = current.toBuilder().clearSchedules()
            others.forEach { builder.addSchedules(it) }
            builder.addSchedules(SettingsMapper.scheduleToProto(rule))
            builder.build()
        }
    }

    suspend fun deleteSchedule(scheduleId: String) {
        update { current ->
            val builder = current.toBuilder().clearSchedules()
            current.schedulesList.filter { it.id != scheduleId }.forEach { builder.addSchedules(it) }
            builder.build()
        }
    }

    suspend fun setDiagnosticRecording(enabled: Boolean) =
        update { it.toBuilder().setDiagnosticRecording(enabled).build() }

    suspend fun setDiagnosticVerboseEvents(enabled: Boolean) =
        update { it.toBuilder().setDiagnosticVerboseEvents(enabled).build() }

    private fun syncBlockedPackages(builder: FocusGuardSettings.Builder) {
        val groupPackages = builder.blockGroupsList.flatMap { it.packageNamesList }.toSet()
        val merged = builder.blockedPackagesList.toSet() + groupPackages
        builder.clearBlockedPackages().addAllBlockedPackages(merged)
    }

    private suspend fun update(transform: (FocusGuardSettings) -> FocusGuardSettings) {
        context.focusGuardDataStore.updateData(transform)
    }
}
