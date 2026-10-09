package com.focusguard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.focusguard.data.proto.FocusGuardSettings

val Context.focusGuardDataStore: DataStore<FocusGuardSettings> by dataStore(
    fileName = "focus_guard_settings.pb",
    serializer = FocusGuardSettingsSerializer,
)
