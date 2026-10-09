package com.focusguard.diagnostics

import android.content.Context
import android.os.Build
import com.focusguard.BuildConfig
import org.json.JSONObject

object DiagnosticExporter {
    const val SCHEMA_VERSION = 1

    fun toJsonLines(context: Context, entries: List<DiagnosticEntry>): String {
        val device = JSONObject()
            .put("manufacturer", Build.MANUFACTURER)
            .put("model", Build.MODEL)
            .put("sdkInt", Build.VERSION.SDK_INT)
            .put("release", Build.VERSION.RELEASE)
            .put("buildType", if (BuildConfig.DEBUG) "debug" else "release")

        return buildString {
            entries.forEach { entry ->
                append(entry.toJsonLine(SCHEMA_VERSION, device))
                append('\n')
            }
            append(
                JSONObject()
                    .put("footer", "See docs/FLOATING_WINDOW_TEST.md for Motorola floating-window procedure.")
                    .toString(),
            )
            append('\n')
        }
    }
}
