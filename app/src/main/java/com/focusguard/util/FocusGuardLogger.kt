package com.focusguard.util

import android.util.Log
import com.focusguard.BuildConfig

object FocusGuardLogger {
    private const val TAG = "FocusGuard"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun w(message: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.e(TAG, message, throwable) else Log.e(TAG, message)
        }
    }
}
