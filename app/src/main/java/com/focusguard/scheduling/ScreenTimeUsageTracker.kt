package com.focusguard.scheduling

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class ScreenTimeUsageTracker(private val context: Context) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings() {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    fun totalForegroundMinutes(
        packageNames: Set<String>,
        startMillis: Long,
        endMillis: Long = System.currentTimeMillis(),
    ): Int {
        if (!hasUsageAccess() || packageNames.isEmpty()) return 0
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            endMillis,
        ) ?: return 0
        val totalMs = stats
            .filter { packageNames.contains(it.packageName) }
            .sumOf { it.totalTimeInForeground }
        return TimeUnit.MILLISECONDS.toMinutes(totalMs).toInt()
    }

    fun startOfDayMillis(now: Instant = Instant.now(), zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val zdt = now.atZone(zoneId)
        return zdt.toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun startOfHourMillis(now: Instant = Instant.now(), zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val zdt = now.atZone(zoneId)
        return zdt.withMinute(0).withSecond(0).withNano(0).toInstant().toEpochMilli()
    }

    fun dayIndexSundayFirst(now: Instant = Instant.now(), zoneId: ZoneId = ZoneId.systemDefault()): Int {
        return when (now.atZone(zoneId).dayOfWeek) {
            java.time.DayOfWeek.SUNDAY -> 0
            java.time.DayOfWeek.MONDAY -> 1
            java.time.DayOfWeek.TUESDAY -> 2
            java.time.DayOfWeek.WEDNESDAY -> 3
            java.time.DayOfWeek.THURSDAY -> 4
            java.time.DayOfWeek.FRIDAY -> 5
            java.time.DayOfWeek.SATURDAY -> 6
        }
    }
}
