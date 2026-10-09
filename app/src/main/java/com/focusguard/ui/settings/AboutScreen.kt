package com.focusguard.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusguard.BuildConfig
import com.focusguard.ui.components.FocusGuardCard
import com.focusguard.ui.components.ScreenHeader

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    showScreenHeader: Boolean = true,
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        if (showScreenHeader) {
            ScreenHeader(
                title = "About FocusGuard",
                subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            )
        } else {
            Text(
                "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        FocusGuardCard {
            Text("Permissions", style = MaterialTheme.typography.titleMedium)
            Text(
                "Accessibility service is required to detect and block app windows.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        FocusGuardCard(modifier = Modifier.padding(top = 12.dp)) {
            Text("Limitations", style = MaterialTheme.typography.titleMedium)
            Text(
                "Android may still create floating windows; FocusGuard blocks when they become visible. " +
                    "Device Owner mode is not used in normal installs.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
