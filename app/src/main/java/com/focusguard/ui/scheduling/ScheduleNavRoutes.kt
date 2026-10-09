package com.focusguard.ui.scheduling

object ScheduleNavRoutes {
    const val GROUP_LIST = "group_list"
    const val WHEN_TO_BLOCK = "when_to_block/{groupId}"
    const val WEEKLY_EDITOR = "weekly_editor/{groupId}"
    const val SCREEN_TIME_EDITOR = "screen_time_editor/{groupId}"
    const val GROUP_APPS = "group_apps/{groupId}"

    fun whenToBlock(groupId: String) = "when_to_block/$groupId"
    fun weeklyEditor(groupId: String) = "weekly_editor/$groupId"
    fun screenTimeEditor(groupId: String) = "screen_time_editor/$groupId"
    fun groupApps(groupId: String) = "group_apps/$groupId"
}
