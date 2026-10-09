package com.focusguard.util

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchableAppsTest {
    @Test
    fun nonSystemApp_isIncludedByFlags() {
        val info = ApplicationInfo().apply {
            packageName = "com.instagram.android"
            flags = 0
        }
        assertTrue(isNonSystemOrUpdatedSystem(info))
    }

    @Test
    fun updatedSystemApp_isIncludedByFlags() {
        val info = ApplicationInfo().apply {
            packageName = "com.google.android.youtube"
            flags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        }
        assertTrue(isNonSystemOrUpdatedSystem(info))
    }

    @Test
    fun pureSystemApp_requiresLauncherFallback() {
        val info = ApplicationInfo().apply {
            packageName = "com.android.settings"
            flags = ApplicationInfo.FLAG_SYSTEM
        }
        assertFalse(isNonSystemOrUpdatedSystem(info))
    }

    private fun isNonSystemOrUpdatedSystem(info: ApplicationInfo): Boolean {
        val system = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val updatedSystem = (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return !system || updatedSystem
    }
}
