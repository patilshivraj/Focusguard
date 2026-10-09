package com.focusguard.ui.screens

import androidx.compose.runtime.Composable
import com.focusguard.FocusGuardApp
import com.focusguard.ui.settings.SettingsNavHost

@Composable
fun SettingsScreen(app: FocusGuardApp) {
    SettingsNavHost(app)
}
