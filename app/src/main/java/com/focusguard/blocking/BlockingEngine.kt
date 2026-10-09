package com.focusguard.blocking

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import android.view.accessibility.AccessibilityEvent
import com.focusguard.accessibility.ScanResult
import com.focusguard.accessibility.ServiceBridge
import com.focusguard.data.BlockRepository
import com.focusguard.scheduling.ScreenTimeUsageTracker
import com.focusguard.util.FocusGuardLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BlockingEngine(
    private val appContext: Context,
    private val blockRepository: BlockRepository,
    private val blockingPolicy: BlockingPolicy,
    private val exposure: BlockedWindowExposure,
    private val scope: CoroutineScope,
    private val screenTimeUsageTracker: ScreenTimeUsageTracker,
    private val focusGuardPackage: String = appContext.packageName,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lastBlockAt = mutableMapOf<String, Long>()
    private val activityCooldownMs = 1500L
    private var pendingRescan = false

    fun onWindowScan(scanResult: ScanResult, triggerSource: String) {
        val exposed = exposure.findExposedPackages(scanResult.frames, focusGuardPackage)
        if (exposed.isEmpty()) {
            ServiceBridge.enforcementHost?.hideOverlay()
            return
        }

        scope.launch {
            val settings = blockRepository.settings.first()
            val state = com.focusguard.data.SettingsMapper.toPolicyState(settings)
            val overlayEnabled =
                if (!settings.hasBlockingOverlayEnabled()) true else settings.blockingOverlayEnabled

            val blockedExposed = exposed.mapNotNull { pkg ->
                val decision = blockingPolicy.shouldBlock(pkg.packageName, state, screenTimeUsageTracker)
                if (decision.shouldBlock) pkg else {
                    FocusGuardLogger.d(
                        "Skip ${pkg.packageName} source=$triggerSource policy=${decision.reason}",
                    )
                    null
                }
            }
            if (blockedExposed.isEmpty()) {
                ServiceBridge.enforcementHost?.hideOverlay()
                return@launch
            }

            val primary = blockedExposed.maxByOrNull { it.isFocused } ?: blockedExposed.first()
            val decision = blockingPolicy.shouldBlock(primary.packageName, state, screenTimeUsageTracker)
            val host = ServiceBridge.enforcementHost

            val eventDelayMs = System.currentTimeMillis() - primary.firstSeenAtMillis
            FocusGuardLogger.d(
                "Block exposure ${primary.packageName} source=$triggerSource " +
                    "presentation=${primary.presentation} sinceFirstSeenMs=$eventDelayMs " +
                    "overlay=$overlayEnabled host=${host != null}",
            )

            if (overlayEnabled && host != null) {
                runOnMainSync {
                    host.showOverlay(primary.packageName, decision.availableAgainAt)
                }
                if (host.isOverlayShowing()) {
                    return@launch
                }
                FocusGuardLogger.w("Overlay failed to attach; falling back to BlockingActivity")
            }

            if (isInActivityCooldown(primary.packageName)) return@launch
            if (isBlockingActivityForeground(scanResult)) return@launch

            launchBlockingActivity(
                packageName = primary.packageName,
                availableAgainAt = decision.availableAgainAt,
                triggerSource = triggerSource,
                detectionDelayMs = eventDelayMs,
            )
        }
    }

    fun onAccessibilityEvent(eventType: Int, scanResult: ScanResult) {
        val source = AccessibilityEvent.eventTypeToString(eventType)
        onWindowScan(scanResult, source)
    }

    fun requestDebouncedRescan(rescan: () -> Unit) {
        if (pendingRescan) return
        pendingRescan = true
        mainHandler.postDelayed({
            pendingRescan = false
            rescan()
        }, 75L)
    }

    private fun isInActivityCooldown(packageName: String): Boolean {
        val last = lastBlockAt[packageName] ?: return false
        return System.currentTimeMillis() - last < activityCooldownMs
    }

    private fun isBlockingActivityForeground(scanResult: ScanResult): Boolean {
        return scanResult.frames.any {
            it.packageName == focusGuardPackage &&
                it.className?.contains("BlockingActivity") == true &&
                (it.isActive || it.isFocused)
        }
    }

    private fun runOnMainSync(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
            return
        }
        val latch = CountDownLatch(1)
        mainHandler.post {
            try {
                block()
            } finally {
                latch.countDown()
            }
        }
        latch.await(500, TimeUnit.MILLISECONDS)
    }

    private fun launchBlockingActivity(
        packageName: String,
        availableAgainAt: java.time.Instant?,
        triggerSource: String,
        detectionDelayMs: Long,
    ) {
        lastBlockAt[packageName] = System.currentTimeMillis()
        val intent = BlockingActivity.intent(
            context = appContext,
            blockedPackage = packageName,
            availableAgainAtEpochMillis = availableAgainAt?.toEpochMilli(),
            triggerSource = triggerSource,
            detectionDelayMs = detectionDelayMs,
        )
        mainHandler.post {
            appContext.startActivity(intent)
        }
    }
}
