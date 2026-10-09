package com.focusguard.accessibility

import android.view.accessibility.AccessibilityWindowInfo
import com.focusguard.util.DisplaySize
import com.focusguard.accessibility.model.WindowBounds
import com.focusguard.accessibility.model.WindowFrame
import com.focusguard.accessibility.model.WindowPresentation
import com.focusguard.accessibility.model.WindowSnapshot

interface WindowStateDetector {
    fun classify(
        bounds: WindowBounds,
        displaySize: DisplaySize,
        isInPictureInPictureMode: Boolean,
        hasSplitScreenDivider: Boolean,
        applicationWindowCount: Int,
    ): WindowPresentation
}

class DefaultWindowStateDetector : WindowStateDetector {
    override fun classify(
        bounds: WindowBounds,
        displaySize: DisplaySize,
        isInPictureInPictureMode: Boolean,
        hasSplitScreenDivider: Boolean,
        applicationWindowCount: Int,
    ): WindowPresentation {
        if (isInPictureInPictureMode) return WindowPresentation.Pip
        if (hasSplitScreenDivider && applicationWindowCount >= 2) return WindowPresentation.Split
        if (!bounds.isNonZero()) return WindowPresentation.Unknown

        val widthRatio = bounds.width().toFloat() / displaySize.width.coerceAtLeast(1)
        val heightRatio = bounds.height().toFloat() / displaySize.height.coerceAtLeast(1)
        val isNearlyFullScreen = widthRatio >= 0.92f && heightRatio >= 0.85f
        if (isNearlyFullScreen) return WindowPresentation.FullScreen

        val isFloatingSized = widthRatio < 0.92f || heightRatio < 0.85f
        if (isFloatingSized && bounds.width() > 0 && bounds.height() > 0) {
            return WindowPresentation.Floating
        }
        return WindowPresentation.Unknown
    }
}

object AccessibilityWindowTypeNames {
    fun name(type: Int): String =
        when (type) {
            AccessibilityWindowInfo.TYPE_APPLICATION -> "TYPE_APPLICATION"
            AccessibilityWindowInfo.TYPE_INPUT_METHOD -> "TYPE_INPUT_METHOD"
            AccessibilityWindowInfo.TYPE_SYSTEM -> "TYPE_SYSTEM"
            AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY -> "TYPE_ACCESSIBILITY_OVERLAY"
            AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER -> "TYPE_SPLIT_SCREEN_DIVIDER"
            AccessibilityWindowInfo.TYPE_MAGNIFICATION_OVERLAY -> "TYPE_MAGNIFICATION_OVERLAY"
            else -> "TYPE_UNKNOWN($type)"
        }
}

fun WindowSnapshot.presentationLabel(): String = presentation.name

fun WindowFrame.describeShort(): String {
    val pkg = packageName ?: "?"
    return "$pkg ${presentation.name} active=$isActive focused=$isFocused bounds=${bounds.left},${bounds.top}-${bounds.right},${bounds.bottom}"
}
