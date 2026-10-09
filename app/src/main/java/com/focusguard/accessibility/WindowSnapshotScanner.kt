package com.focusguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import com.focusguard.util.DisplaySize
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.focusguard.accessibility.model.WindowBounds
import com.focusguard.accessibility.model.WindowFrame
import com.focusguard.accessibility.model.WindowSnapshot
import com.focusguard.util.FocusGuardLogger

class WindowSnapshotScanner(
    private val windowStateDetector: WindowStateDetector = DefaultWindowStateDetector(),
) {
    fun scan(
        service: AccessibilityService,
        event: AccessibilityEvent?,
        displaySize: DisplaySize,
    ): ScanResult {
        val rawWindows = service.windows?.filterNotNull() ?: emptyList()
        val hasSplitDivider = rawWindows.any { it.type == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER }
        val applicationWindowCount = rawWindows.count { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }

        val frames = mutableListOf<WindowFrame>()
        val snapshots = mutableListOf<WindowSnapshot>()
        val eventType = event?.eventType ?: 0
        val eventTypeName = if (event != null) AccessibilityEvent.eventTypeToString(eventType) else "MANUAL_SCAN"

        rawWindows.forEach { window ->
            try {
                val bounds = boundsOf(window)
                val rootPkg = safeRootPackage(window)
                val rootClass = safeRootClassName(window)
                val presentation = windowStateDetector.classify(
                    bounds = bounds,
                    displaySize = displaySize,
                    isInPictureInPictureMode = window.isInPictureInPictureMode,
                    hasSplitScreenDivider = hasSplitDivider,
                    applicationWindowCount = applicationWindowCount,
                )
                frames += WindowFrame(
                    windowId = window.id,
                    windowType = window.type,
                    windowTypeName = AccessibilityWindowTypeNames.name(window.type),
                    displayId = window.displayId,
                    bounds = bounds,
                    packageName = rootPkg,
                    className = rootClass,
                    title = window.title?.toString(),
                    isActive = window.isActive,
                    isFocused = window.isFocused,
                    isAccessibilityFocused = window.isAccessibilityFocused,
                    isInPictureInPictureMode = window.isInPictureInPictureMode,
                    presentation = presentation,
                    childWindowCount = window.childCount,
                )
                snapshots += WindowSnapshot(
                    timestampMillis = System.currentTimeMillis(),
                    eventType = eventType,
                    eventTypeName = eventTypeName,
                    eventPackageName = event?.packageName?.toString(),
                    eventClassName = event?.className?.toString(),
                    windowId = window.id,
                    windowType = window.type,
                    windowTypeName = AccessibilityWindowTypeNames.name(window.type),
                    displayId = window.displayId,
                    bounds = bounds,
                    packageName = rootPkg,
                    className = rootClass,
                    title = window.title?.toString(),
                    isActive = window.isActive,
                    isFocused = window.isFocused,
                    isAccessibilityFocused = window.isAccessibilityFocused,
                    isInPictureInPictureMode = window.isInPictureInPictureMode,
                    visibleWindowCount = rawWindows.size,
                    foregroundPackageHeuristic = null,
                    presentation = presentation,
                )
            } catch (e: Exception) {
                FocusGuardLogger.e("Failed window snapshot", e)
            } finally {
                window.recycle()
            }
        }

        val foreground = frames
            .filter { it.windowType == AccessibilityWindowInfo.TYPE_APPLICATION }
            .sortedWith(
                compareByDescending<WindowFrame> { it.isFocused }
                    .thenByDescending { it.isActive }
                    .thenByDescending { it.bounds.width() * it.bounds.height() },
            )
            .firstOrNull()

        val foregroundPackage = foreground?.packageName
        val enrichedSnapshots = snapshots.map { it.copy(foregroundPackageHeuristic = foregroundPackage) }

        return ScanResult(
            frames = frames,
            snapshots = enrichedSnapshots,
            foregroundPackage = foregroundPackage,
            foregroundClassName = foreground?.className,
            visibleWindowCount = rawWindows.size,
        )
    }

    private fun boundsOf(window: AccessibilityWindowInfo): WindowBounds {
        val rect = Rect()
        window.getBoundsInScreen(rect)
        return WindowBounds(rect.left, rect.top, rect.right, rect.bottom)
    }

    private fun safeRootPackage(window: AccessibilityWindowInfo): String? {
        val root = window.root ?: return null
        return try {
            root.packageName?.toString()
        } finally {
            root.recycle()
        }
    }

    private fun safeRootClassName(window: AccessibilityWindowInfo): String? {
        val root = window.root ?: return null
        return try {
            root.className?.toString()
        } finally {
            root.recycle()
        }
    }
}

data class ScanResult(
    val frames: List<WindowFrame>,
    val snapshots: List<WindowSnapshot>,
    val foregroundPackage: String?,
    val foregroundClassName: String?,
    val visibleWindowCount: Int,
)
