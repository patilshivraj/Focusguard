package com.focusguard.blocking

import android.view.accessibility.AccessibilityWindowInfo
import com.focusguard.accessibility.model.WindowBounds
import com.focusguard.accessibility.model.WindowFrame
import com.focusguard.accessibility.model.WindowPresentation
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockedWindowExposureTest {
    private val exposure = BlockedWindowExposure()

    @Test
    fun detectsFloatingWindowWithoutFocus() {
        val frames = listOf(
            WindowFrame(
                windowId = 1,
                windowType = AccessibilityWindowInfo.TYPE_APPLICATION,
                windowTypeName = "TYPE_APPLICATION",
                displayId = 0,
                bounds = WindowBounds(50, 100, 600, 800),
                packageName = "com.blocked.app",
                className = ".Main",
                title = null,
                isActive = false,
                isFocused = false,
                isAccessibilityFocused = false,
                isInPictureInPictureMode = false,
                presentation = WindowPresentation.Floating,
                childWindowCount = 0,
            ),
        )
        val exposed = exposure.findExposedPackages(frames, "com.focusguard")
        assertEquals(1, exposed.size)
        assertEquals("com.blocked.app", exposed.first().packageName)
    }
}
