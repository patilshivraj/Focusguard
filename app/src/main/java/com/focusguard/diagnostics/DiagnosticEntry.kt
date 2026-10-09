package com.focusguard.diagnostics

import com.focusguard.accessibility.model.WindowSnapshot
import org.json.JSONObject

data class DiagnosticEntry(
    val timestampMillis: Long,
    val eventType: Int,
    val eventTypeName: String,
    val eventPackageName: String?,
    val eventClassName: String?,
    val windowId: Int,
    val windowType: Int,
    val windowTypeName: String,
    val displayId: Int,
    val boundsLeft: Int,
    val boundsTop: Int,
    val boundsRight: Int,
    val boundsBottom: Int,
    val packageName: String?,
    val className: String?,
    val title: String?,
    val isActive: Boolean,
    val isFocused: Boolean,
    val isAccessibilityFocused: Boolean,
    val isInPictureInPictureMode: Boolean,
    val visibleWindowCount: Int,
    val foregroundPackageHeuristic: String?,
    val presentation: String,
    val transitionNote: String? = null,
) {
    fun toJsonLine(schemaVersion: Int, deviceInfo: JSONObject): String {
        val json = JSONObject()
        json.put("schemaVersion", schemaVersion)
        json.put("device", deviceInfo)
        json.put("timestampMillis", timestampMillis)
        json.put("eventType", eventType)
        json.put("eventTypeName", eventTypeName)
        json.put("eventPackageName", eventPackageName)
        json.put("eventClassName", eventClassName)
        json.put("windowId", windowId)
        json.put("windowType", windowType)
        json.put("windowTypeName", windowTypeName)
        json.put("displayId", displayId)
        json.put("bounds", JSONObject().apply {
            put("left", boundsLeft)
            put("top", boundsTop)
            put("right", boundsRight)
            put("bottom", boundsBottom)
        })
        json.put("packageName", packageName)
        json.put("className", className)
        json.put("title", title)
        json.put("isActive", isActive)
        json.put("isFocused", isFocused)
        json.put("isAccessibilityFocused", isAccessibilityFocused)
        json.put("isInPictureInPictureMode", isInPictureInPictureMode)
        json.put("visibleWindowCount", visibleWindowCount)
        json.put("foregroundPackageHeuristic", foregroundPackageHeuristic)
        json.put("presentation", presentation)
        json.put("transitionNote", transitionNote)
        return json.toString()
    }

    companion object {
        fun fromSnapshot(snapshot: WindowSnapshot): DiagnosticEntry =
            DiagnosticEntry(
                timestampMillis = snapshot.timestampMillis,
                eventType = snapshot.eventType,
                eventTypeName = snapshot.eventTypeName,
                eventPackageName = snapshot.eventPackageName,
                eventClassName = snapshot.eventClassName,
                windowId = snapshot.windowId,
                windowType = snapshot.windowType,
                windowTypeName = snapshot.windowTypeName,
                displayId = snapshot.displayId,
                boundsLeft = snapshot.bounds.left,
                boundsTop = snapshot.bounds.top,
                boundsRight = snapshot.bounds.right,
                boundsBottom = snapshot.bounds.bottom,
                packageName = snapshot.packageName,
                className = snapshot.className,
                title = snapshot.title,
                isActive = snapshot.isActive,
                isFocused = snapshot.isFocused,
                isAccessibilityFocused = snapshot.isAccessibilityFocused,
                isInPictureInPictureMode = snapshot.isInPictureInPictureMode,
                visibleWindowCount = snapshot.visibleWindowCount,
                foregroundPackageHeuristic = snapshot.foregroundPackageHeuristic,
                presentation = snapshot.presentation.name,
                transitionNote = snapshot.transitionNote,
            )
    }
}
