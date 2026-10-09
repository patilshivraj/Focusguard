package com.focusguard.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.focusguard.data.BlockRepository
import com.focusguard.util.FocusGuardLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant

class ScheduleAlarmScheduler(
    private val context: Context,
    private val blockRepository: BlockRepository,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNextBoundary(schedules: List<ScheduleRule>) {
        if (schedules.isEmpty()) return
        val evaluator = ScheduleEvaluator()
        val next = evaluator.nextScheduleBoundary(schedules) ?: return
        if (!next.isAfter(Instant.now())) return
        val intent = Intent(context, ScheduleAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.toEpochMilli(),
                pending,
            )
        } catch (e: SecurityException) {
            FocusGuardLogger.w("Exact alarm not allowed; schedule boundary alarm skipped.")
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.toEpochMilli(),
                    pending,
                )
            } catch (e2: Exception) {
                FocusGuardLogger.e("Failed to schedule boundary alarm", e2)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 1001
    }
}

class ScheduleAlarmReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as com.focusguard.FocusGuardApp
        runBlocking {
            val schedules = app.blockRepository.schedules.first()
            app.scheduleAlarmScheduler.scheduleNextBoundary(schedules)
        }
    }
}

class BootReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as com.focusguard.FocusGuardApp
        runBlocking {
            val schedules = app.blockRepository.schedules.first()
            app.scheduleAlarmScheduler.scheduleNextBoundary(schedules)
        }
    }
}
