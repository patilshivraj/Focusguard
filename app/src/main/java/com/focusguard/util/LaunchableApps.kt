package com.focusguard.util

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Apps shown in block pickers: user-installed apps, updated system apps (YouTube), and
 * other launcher-visible packages. Merges several sources so OEM/package-visibility quirks
 * do not hide apps like Instagram or YouTube.
 */
object LaunchableApps {
    fun list(
        context: android.content.Context,
        excludePackage: String? = null,
        ensurePackages: Set<String> = emptySet(),
    ): List<ApplicationInfo> {
        val pm = context.packageManager
        val packageNames = linkedSetOf<String>()

        fun add(pkg: String?) {
            if (pkg.isNullOrBlank()) return
            if (excludePackage != null && pkg == excludePackage) return
            packageNames += pkg
        }

        launcherPackageNames(pm).forEach(::add)

        installedApplications(pm).forEach { info ->
            if (shouldIncludeInstalledApp(info, pm)) {
                add(info.packageName)
            }
        }

        ensurePackages.forEach(::add)

        return packageNames
            .mapNotNull { packageName -> applicationInfo(pm, packageName) }
            .sortedBy { PackageLabelResolver.label(context, it.packageName) }
    }

    private fun launcherPackageNames(pm: PackageManager): List<String> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
        }
        return resolveInfos.map { it.activityInfo.packageName }
    }

    private fun installedApplications(pm: PackageManager): List<ApplicationInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

    /** User apps (Instagram) + Play-updated system apps (YouTube) + launcher-only system apps. */
    internal fun shouldIncludeInstalledApp(info: ApplicationInfo, pm: PackageManager): Boolean {
        val system = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val updatedSystem = (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        if (!system) return true
        if (updatedSystem) return true
        return pm.getLaunchIntentForPackage(info.packageName) != null
    }

    private fun applicationInfo(pm: PackageManager, packageName: String): ApplicationInfo? =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
}
