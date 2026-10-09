package com.focusguard.blocking

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import com.focusguard.R
import com.focusguard.util.FocusGuardLogger
import com.focusguard.util.PackageLabelResolver
import com.focusguard.util.TimeFormatting
import java.time.Instant

class BlockingOverlayController(
    private val service: AccessibilityService,
) : BlockingEnforcementHost {
    private val windowManager = service.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var showingPackage: String? = null

    override fun showOverlay(blockedPackage: String, availableAgainAt: Instant?) {
        val label = PackageLabelResolver.label(service, blockedPackage)
        if (overlayView != null && showingPackage == blockedPackage) {
            updateTexts(label, availableAgainAt)
            return
        }
        hideOverlay()

        val view = LayoutInflater.from(service).inflate(R.layout.blocking_overlay, null)
        view.setOnClickListener { /* consume */ }
        view.findViewById<Button>(R.id.overlay_home_button).setOnClickListener {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            hideOverlay()
        }
        updateTextsOnView(view, label, availableAgainAt)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            showingPackage = blockedPackage
            view.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            FocusGuardLogger.d("Overlay shown for $blockedPackage")
        } catch (e: Exception) {
            FocusGuardLogger.e("Failed to show blocking overlay", e)
            overlayView = null
            showingPackage = null
        }
    }

    override fun hideOverlay() {
        val view = overlayView ?: return
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            FocusGuardLogger.e("Failed to hide blocking overlay", e)
        } finally {
            overlayView = null
            showingPackage = null
        }
    }

    override fun isOverlayShowing(): Boolean = overlayView != null

    override fun overlayBlockedPackage(): String? = showingPackage

    private fun updateTexts(label: String, availableAgainAt: Instant?) {
        overlayView?.let { updateTextsOnView(it, label, availableAgainAt) }
    }

    private fun updateTextsOnView(view: View, label: String, availableAgainAt: Instant?) {
        view.findViewById<TextView>(R.id.overlay_message).text = "$label is blocked."
        val schedule = view.findViewById<TextView>(R.id.overlay_schedule)
        if (availableAgainAt != null) {
            schedule.visibility = View.VISIBLE
            schedule.text = "Available again at ${TimeFormatting.formatInstant(availableAgainAt)}"
        } else {
            schedule.visibility = View.GONE
        }
    }
}
