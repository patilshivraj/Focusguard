package com.focusguard.ui.screens

import androidx.compose.runtime.Composable
import com.focusguard.FocusGuardApp
import com.focusguard.ui.scheduling.SchedulesNavHost

@Composable
fun SchedulesScreen(app: FocusGuardApp) {
    SchedulesNavHost(app)
}
