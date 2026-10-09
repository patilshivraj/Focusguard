package com.focusguard.ui.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import kotlinx.coroutines.launch

@Composable
fun PinGate(
    app: FocusGuardApp,
    pinEnabled: Boolean,
    onUnlocked: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (!pinEnabled) {
        content()
        return
    }

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Enter PIN to continue")
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("PIN") },
            singleLine = true,
        )
        if (error != null) Text(error!!)
        Button(
            onClick = {
                scope.launch {
                    if (app.pinRepository.verifyPin(pin)) {
                        error = null
                        onUnlocked()
                    } else {
                        error = "Incorrect PIN"
                    }
                }
            },
        ) {
            Text("Unlock")
        }
    }
}
