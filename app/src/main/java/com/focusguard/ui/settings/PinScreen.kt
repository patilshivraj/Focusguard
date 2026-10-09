package com.focusguard.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusguard.FocusGuardApp
import com.focusguard.ui.components.ScreenHeader
import com.focusguard.ui.components.SettingsSwitchRow
import kotlinx.coroutines.launch

@Composable
fun PinScreen(
    app: FocusGuardApp,
    modifier: Modifier = Modifier,
    showScreenHeader: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    var pinEnabled by remember { mutableStateOf(false) }
    var hasPin by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        app.pinRepository.clearOrphanedPinEnabled()
        launch { app.pinRepository.pinEnabled.collect { pinEnabled = it } }
        launch { app.pinRepository.hasPin.collect { hasPin = it } }
    }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showScreenHeader) {
            ScreenHeader(
                title = "PIN",
                subtitle = "PIN is required for Settings, Apps, and Schedules when enabled.",
            )
        }
        SettingsSwitchRow(
            title = "Require PIN",
            subtitle = if (hasPin) {
                "When enabled, Settings, Apps, and Schedules require your PIN."
            } else {
                "Set a PIN below before you can turn this on."
            },
            checked = pinEnabled,
            enabled = hasPin,
            onCheckedChange = { enabled ->
                scope.launch {
                    val ok = app.pinRepository.setPinEnabled(enabled)
                    if (!ok) {
                        message = "Set a PIN first, then enable Require PIN."
                    } else {
                        message = null
                    }
                }
            },
        )
        OutlinedTextField(
            value = newPin,
            onValueChange = { newPin = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New PIN") },
            singleLine = true,
        )
        Button(
            onClick = {
                scope.launch {
                    if (app.pinRepository.setPin(newPin)) {
                        newPin = ""
                        message = "PIN set. Require PIN is now on."
                    } else {
                        message = "Enter a PIN before saving."
                    }
                }
            },
        ) {
            Text("Set PIN")
        }
        Button(
            onClick = {
                scope.launch {
                    app.pinRepository.clearPin()
                    message = "PIN cleared."
                }
            },
            enabled = hasPin,
        ) {
            Text("Clear PIN")
        }
        if (message != null) {
            Text(
                message!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            "PIN is stored only on this device as a hash.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
