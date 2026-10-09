package com.focusguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.focusguard.FocusGuardApp
import com.focusguard.blocking.BlockingOverlayController
import com.focusguard.util.DisplayMetricsProvider
import com.focusguard.util.FocusGuardLogger

class FocusGuardAccessibilityService : AccessibilityService() {
    private val windowSnapshotScanner = WindowSnapshotScanner()
    private lateinit var eventTap: AccessibilityEventTap
    private var overlayController: BlockingOverlayController? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val app = application as FocusGuardApp
        eventTap = AccessibilityEventTap(app.diagnosticLogRepository)
        applyServiceFlags(app.diagnosticVerboseEvents)
        overlayController = BlockingOverlayController(this)
        ServiceBridge.enforcementHost = overlayController
        ServiceBridge.setConnected(this)
        FocusGuardLogger.d("Accessibility service connected")
        performScan(null)
    }

    override fun onDestroy() {
        overlayController?.hideOverlay()
        overlayController = null
        ServiceBridge.setConnected(null)
        super.onDestroy()
    }

    override fun onInterrupt() = Unit

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!isWindowRelated(event.eventType)) return

        val app = application as FocusGuardApp
        val scanResult = performScan(event)
        eventTap.onEvent(event, scanResult, app.diagnosticRecordingEnabled)
        app.blockingEngine.onAccessibilityEvent(event.eventType, scanResult)

        if (
            event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            app.blockingEngine.requestDebouncedRescan {
                val rescan = performScan(event)
                app.blockingEngine.onWindowScan(rescan, "debounced_rescan")
            }
        }
    }

    private fun performScan(event: AccessibilityEvent?): ScanResult {
        val displaySize = DisplayMetricsProvider.defaultDisplaySize(this)
        val scanResult = windowSnapshotScanner.scan(this, event, displaySize)
        ServiceBridge.updateForeground(
            ForegroundState(
                packageName = scanResult.foregroundPackage,
                className = scanResult.foregroundClassName,
                windows = scanResult.frames,
                visibleWindowCount = scanResult.visibleWindowCount,
            ),
        )
        return scanResult
    }

    private fun applyServiceFlags(verboseEvents: Boolean) {
        val info = serviceInfo ?: return
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
            AccessibilityEvent.TYPE_WINDOWS_CHANGED or
            if (verboseEvents) AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED else 0
        info.flags = info.flags or
            AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
            AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 100
        serviceInfo = info
    }

    private fun isWindowRelated(eventType: Int): Boolean {
        return eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    }
}
