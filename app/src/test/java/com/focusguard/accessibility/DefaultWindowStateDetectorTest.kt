package com.focusguard.accessibility

import com.focusguard.accessibility.model.WindowBounds
import com.focusguard.util.DisplaySize
import com.focusguard.accessibility.model.WindowPresentation
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultWindowStateDetectorTest {
    private val detector = DefaultWindowStateDetector()
    private val display = DisplaySize(1080, 2400)

    @Test
    fun fullScreenClassification() {
        val presentation = detector.classify(
            bounds = WindowBounds(0, 0, 1080, 2400),
            displaySize = display,
            isInPictureInPictureMode = false,
            hasSplitScreenDivider = false,
            applicationWindowCount = 1,
        )
        assertEquals(WindowPresentation.FullScreen, presentation)
    }

    @Test
    fun floatingClassification() {
        val presentation = detector.classify(
            bounds = WindowBounds(100, 200, 700, 900),
            displaySize = display,
            isInPictureInPictureMode = false,
            hasSplitScreenDivider = false,
            applicationWindowCount = 1,
        )
        assertEquals(WindowPresentation.Floating, presentation)
    }
}
