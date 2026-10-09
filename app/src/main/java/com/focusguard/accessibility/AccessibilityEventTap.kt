package com.focusguard.accessibility

import android.view.accessibility.AccessibilityEvent
import com.focusguard.accessibility.model.WindowSnapshot
import com.focusguard.diagnostics.DiagnosticEntry
import com.focusguard.diagnostics.DiagnosticLogRepository
import com.focusguard.diagnostics.DiagnosticTransitionDetector

class AccessibilityEventTap(
    private val diagnosticLogRepository: DiagnosticLogRepository,
    private val transitionDetector: DiagnosticTransitionDetector = DiagnosticTransitionDetector(),
) {
    fun onEvent(
        event: AccessibilityEvent,
        scanResult: ScanResult,
        recordingEnabled: Boolean,
    ) {
        val transition = transitionDetector.detect(scanResult)
        val snapshots = scanResult.snapshots.map { snapshot ->
            if (transition != null && snapshot.packageName == transition.packageName) {
                snapshot.copy(transitionNote = transition.note)
            } else {
                snapshot
            }
        }

        snapshots.forEach { snapshot ->
            val entry = DiagnosticEntry.fromSnapshot(snapshot)
            ServiceBridge.appendDiagnostic(entry)
            if (recordingEnabled) {
                diagnosticLogRepository.append(entry)
            }
        }

        if (snapshots.isEmpty()) {
            val fallback = DiagnosticEntry(
                timestampMillis = System.currentTimeMillis(),
                eventType = event.eventType,
                eventTypeName = AccessibilityEvent.eventTypeToString(event.eventType),
                eventPackageName = event.packageName?.toString(),
                eventClassName = event.className?.toString(),
                windowId = -1,
                windowType = -1,
                windowTypeName = "N/A",
                displayId = -1,
                boundsLeft = 0,
                boundsTop = 0,
                boundsRight = 0,
                boundsBottom = 0,
                packageName = event.packageName?.toString(),
                className = event.className?.toString(),
                title = null,
                isActive = false,
                isFocused = false,
                isAccessibilityFocused = false,
                isInPictureInPictureMode = false,
                visibleWindowCount = scanResult.visibleWindowCount,
                foregroundPackageHeuristic = scanResult.foregroundPackage,
                presentation = "Unknown",
                transitionNote = transition?.note,
            )
            ServiceBridge.appendDiagnostic(fallback)
            if (recordingEnabled) diagnosticLogRepository.append(fallback)
        }
    }
}
