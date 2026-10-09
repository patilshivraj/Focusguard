package com.focusguard.util

import android.content.Context
import android.content.pm.PackageManager

object PackageLabelResolver {
    fun label(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
