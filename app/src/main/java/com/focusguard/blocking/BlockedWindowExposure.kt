package com.focusguard.blocking

import android.view.accessibility.AccessibilityWindowInfo
import com.focusguard.accessibility.model.WindowFrame
import com.focusguard.accessibility.model.WindowPresentation

data class ExposedPackage(
    val packageName: String,
    val presentation: WindowPresentation,
    val isActive: Boolean,
    val isFocused: Boolean,
    val isPictureInPicture: Boolean,
    val firstSeenAtMillis: Long,
)

class BlockedWindowExposure {
    private val firstSeen = mutableMapOf<String, Long>()

    fun findExposedPackages(
        frames: List<WindowFrame>,
        focusGuardPackage: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): List<ExposedPackage> {
        val exposed = linkedSetOf<String>()
        frames
            .filter { it.windowType == AccessibilityWindowInfo.TYPE_APPLICATION }
            .filter { it.packageName != null && it.packageName != focusGuardPackage }
            .forEach { frame ->
                val pkg = frame.packageName!!
                val visible = frame.bounds.isNonZero()
                val interactive = frame.isActive || frame.isFocused || frame.isAccessibilityFocused
                val floatingVisible = frame.presentation == WindowPresentation.Floating && visible
                val pip = frame.isInPictureInPictureMode
                if (interactive || floatingVisible || pip || (visible && frame.presentation != WindowPresentation.Unknown)) {
                    if (!firstSeen.containsKey(pkg)) {
                        firstSeen[pkg] = nowMillis
                    }
                    exposed += pkg
                }
            }

        return exposed.map { pkg ->
            val frame = frames.first { it.packageName == pkg }
            ExposedPackage(
                packageName = pkg,
                presentation = frame.presentation,
                isActive = frame.isActive,
                isFocused = frame.isFocused,
                isPictureInPicture = frame.isInPictureInPictureMode,
                firstSeenAtMillis = firstSeen[pkg] ?: nowMillis,
            )
        }
    }

    fun clearPackage(packageName: String) {
        firstSeen.remove(packageName)
    }

    fun firstSeenMillis(packageName: String): Long? = firstSeen[packageName]
}
