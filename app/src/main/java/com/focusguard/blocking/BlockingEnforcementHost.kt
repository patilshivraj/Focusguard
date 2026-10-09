package com.focusguard.blocking

import java.time.Instant

interface BlockingEnforcementHost {
    fun showOverlay(blockedPackage: String, availableAgainAt: Instant?)
    fun hideOverlay()
    fun isOverlayShowing(): Boolean
    fun overlayBlockedPackage(): String?
}
