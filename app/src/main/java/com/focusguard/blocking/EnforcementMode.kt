package com.focusguard.blocking

/**
 * Optional advanced enforcement (Device Owner / DPM) is not available to normal consumer apps.
 * This interface exists for future experimental modes without coupling to the accessibility engine.
 */
interface EnforcementMode {
    val name: String
    val isAvailable: Boolean
    val setupInstructions: String
}

object AccessibilityEnforcementMode : EnforcementMode {
    override val name: String = "Accessibility window detection"
    override val isAvailable: Boolean = true
    override val setupInstructions: String =
        "Enable FocusGuard in system Accessibility settings."
}

object DeviceOwnerEnforcementMode : EnforcementMode {
    override val name: String = "Device Owner (optional, not enabled)"
    override val isAvailable: Boolean = false
    override val setupInstructions: String =
        "Requires provisioning via adb dpm set-device-owner on a managed device. Not supported in standard installs."
}
