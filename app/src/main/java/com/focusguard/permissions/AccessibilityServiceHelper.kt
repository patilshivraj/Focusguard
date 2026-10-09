package com.focusguard.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import com.focusguard.accessibility.FocusGuardAccessibilityService

object AccessibilityServiceHelper {
    fun isServiceEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabled)
        val expected = ComponentName(context, FocusGuardAccessibilityService::class.java).flattenToString()
        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    fun openAccessibilitySettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }
}
