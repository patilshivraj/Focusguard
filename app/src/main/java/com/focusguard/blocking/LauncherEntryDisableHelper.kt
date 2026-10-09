package com.focusguard.blocking

import android.content.Context
import android.content.pm.PackageManager
import com.focusguard.util.FocusGuardLogger

/**
 * Attempts to disable launcher entry for a third-party package.
 * On consumer devices this almost always fails without Device Owner / privileged shell.
 */
object LauncherEntryDisableHelper {
    sealed class Result {
        data object Success : Result()
        data class NotSupported(val reason: String) : Result()
    }

    fun tryDisableLauncherEntry(context: Context, packageName: String): Result {
        return try {
            val pm = context.packageManager
            val launch = pm.getLaunchIntentForPackage(packageName)
            if (launch?.component == null) {
                return Result.NotSupported("No launcher activity for $packageName")
            }
            pm.setApplicationEnabledSetting(
                packageName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                PackageManager.DONT_KILL_APP,
            )
            FocusGuardLogger.d("Disabled launcher entry for $packageName")
            Result.Success
        } catch (e: SecurityException) {
            Result.NotSupported(
                "Android does not allow third-party apps to hide other apps' launcher icons. " +
                    "Use accessibility blocking instead.",
            )
        } catch (e: Exception) {
            Result.NotSupported(e.message ?: "Unknown error")
        }
    }

    fun tryEnableLauncherEntry(context: Context, packageName: String): Result {
        return try {
            context.packageManager.setApplicationEnabledSetting(
                packageName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            Result.Success
        } catch (e: SecurityException) {
            Result.NotSupported("Cannot re-enable launcher entry without system permission.")
        } catch (e: Exception) {
            Result.NotSupported(e.message ?: "Unknown error")
        }
    }
}
