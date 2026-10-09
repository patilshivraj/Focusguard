package com.focusguard.diagnostics

import com.focusguard.accessibility.ScanResult
import com.focusguard.accessibility.model.WindowPresentation

data class DetectedTransition(
    val packageName: String?,
    val note: String,
)

class DiagnosticTransitionDetector {
    private var lastForeground: String? = null
    private var lastPresentations: Map<String, WindowPresentation> = emptyMap()

    fun detect(scanResult: ScanResult): DetectedTransition? {
        val fg = scanResult.foregroundPackage
        var transition: DetectedTransition? = null
        if (fg != null && fg != lastForeground) {
            transition = DetectedTransition(fg, "foreground_package_changed:$lastForeground->$fg")
        }

        scanResult.frames
            .filter { it.windowType == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION }
            .forEach { frame ->
                val pkg = frame.packageName ?: return@forEach
                val prev = lastPresentations[pkg]
                if (prev != null && prev != frame.presentation) {
                    transition = DetectedTransition(
                        pkg,
                        "presentation_changed:$prev->${frame.presentation}",
                    )
                }
                if (frame.presentation == WindowPresentation.Floating && prev != WindowPresentation.Floating) {
                    transition = DetectedTransition(pkg, "floating_window_detected")
                }
            }

        lastForeground = fg
        lastPresentations = scanResult.frames
            .filter { it.packageName != null }
            .associate { it.packageName!! to it.presentation }

        return transition
    }

    fun reset() {
        lastForeground = null
        lastPresentations = emptyMap()
    }
}
