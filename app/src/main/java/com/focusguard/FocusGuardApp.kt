package com.focusguard

import android.app.Application
import com.focusguard.blocking.BlockedWindowExposure
import com.focusguard.blocking.BlockingEngine
import com.focusguard.blocking.BlockingPolicy
import com.focusguard.data.BlockRepository
import com.focusguard.data.PinRepository
import com.focusguard.diagnostics.DiagnosticLogRepository
import com.focusguard.scheduling.ScheduleAlarmScheduler
import com.focusguard.scheduling.ScreenTimeUsageTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class FocusGuardApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var blockRepository: BlockRepository
    lateinit var pinRepository: PinRepository
    lateinit var blockingEngine: BlockingEngine
    lateinit var scheduleAlarmScheduler: ScheduleAlarmScheduler
    lateinit var screenTimeUsageTracker: ScreenTimeUsageTracker

    val diagnosticLogRepository = DiagnosticLogRepository()

    @Volatile
    var diagnosticRecordingEnabled: Boolean = false

    @Volatile
    var diagnosticVerboseEvents: Boolean = false

    override fun onCreate() {
        super.onCreate()
        blockRepository = BlockRepository(this)
        pinRepository = PinRepository(this)
        scheduleAlarmScheduler = ScheduleAlarmScheduler(this, blockRepository)
        screenTimeUsageTracker = ScreenTimeUsageTracker(this)
        blockingEngine = BlockingEngine(
            appContext = applicationContext,
            blockRepository = blockRepository,
            blockingPolicy = BlockingPolicy(),
            exposure = BlockedWindowExposure(),
            scope = appScope,
            screenTimeUsageTracker = screenTimeUsageTracker,
        )

        appScope.launch {
            blockRepository.diagnosticRecording
                .distinctUntilChanged()
                .collect { diagnosticRecordingEnabled = it }
        }
        appScope.launch {
            blockRepository.diagnosticVerboseEvents
                .distinctUntilChanged()
                .collect { diagnosticVerboseEvents = it }
        }
        appScope.launch {
            blockRepository.schedules.collect { schedules ->
                scheduleAlarmScheduler.scheduleNextBoundary(schedules)
            }
        }
    }
}
