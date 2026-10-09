package com.focusguard.accessibility.model

import android.graphics.Rect

enum class WindowPresentation {
    FullScreen,
    Split,
    Floating,
    Pip,
    Unknown,
}

data class WindowBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun toRect(): Rect = Rect(left, top, right, bottom)

    fun width(): Int = right - left

    fun height(): Int = bottom - top

    fun isNonZero(): Boolean = width() > 0 && height() > 0
}

data class WindowFrame(
    val windowId: Int,
    val windowType: Int,
    val windowTypeName: String,
    val displayId: Int,
    val bounds: WindowBounds,
    val packageName: String?,
    val className: String?,
    val title: String?,
    val isActive: Boolean,
    val isFocused: Boolean,
    val isAccessibilityFocused: Boolean,
    val isInPictureInPictureMode: Boolean,
    val presentation: WindowPresentation,
    val childWindowCount: Int,
)

data class WindowSnapshot(
    val timestampMillis: Long,
    val eventType: Int,
    val eventTypeName: String,
    val eventPackageName: String?,
    val eventClassName: String?,
    val windowId: Int,
    val windowType: Int,
    val windowTypeName: String,
    val displayId: Int,
    val bounds: WindowBounds,
    val packageName: String?,
    val className: String?,
    val title: String?,
    val isActive: Boolean,
    val isFocused: Boolean,
    val isAccessibilityFocused: Boolean,
    val isInPictureInPictureMode: Boolean,
    val visibleWindowCount: Int,
    val foregroundPackageHeuristic: String?,
    val presentation: WindowPresentation,
    val transitionNote: String? = null,
)
