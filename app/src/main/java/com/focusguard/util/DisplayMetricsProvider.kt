package com.focusguard.util

import android.content.Context
import android.view.WindowManager

object DisplayMetricsProvider {
    fun defaultDisplaySize(context: Context): DisplaySize {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            DisplaySize(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            DisplaySize(metrics.widthPixels, metrics.heightPixels)
        }
    }
}
