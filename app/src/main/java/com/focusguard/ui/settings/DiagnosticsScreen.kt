package com.focusguard.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.focusguard.FocusGuardApp
import com.focusguard.accessibility.ServiceBridge
import com.focusguard.accessibility.describeShort
import com.focusguard.diagnostics.DiagnosticExporter
import com.focusguard.ui.components.FocusGuardCard
import com.focusguard.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    app: FocusGuardApp,
    modifier: Modifier = Modifier,
    showScreenHeader: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val foreground = ServiceBridge.foregroundState.collectAsStateFlowCompat()
    val entries = ServiceBridge.diagnosticEntries.collectAsStateFlowCompat()
    var recording by remember { mutableStateOf(false) }
    var verboseEvents by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.diagnosticRecording.collect { recording = it }
    }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.diagnosticVerboseEvents.collect { verboseEvents = it }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    val payload = DiagnosticExporter.toJsonLines(context, entries.value)
                    stream.write(payload.toByteArray())
                }
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showScreenHeader) {
            ScreenHeader(
                title = "Diagnostics",
                subtitle = "Live window snapshots and event stream from the accessibility service.",
            )
        }
        FocusGuardCard {
            Text("Foreground: ${foreground.value.packageName ?: "—"}")
            Text("Activity: ${foreground.value.className ?: "—"}")
            Text("Visible windows: ${foreground.value.visibleWindowCount}")
        }
        Button(
            onClick = {
                scope.launch {
                    app.blockRepository.setDiagnosticRecording(!recording)
                }
            },
        ) {
            Text(if (recording) "STOP RECORDING" else "START RECORDING")
        }
        Button(onClick = { exportLauncher.launch("focusguard-diagnostics.jsonl") }) {
            Text("Export diagnostic log")
        }
        Button(onClick = { ServiceBridge.clearDiagnostics() }) { Text("Clear stream") }
        Button(
            onClick = {
                scope.launch {
                    app.blockRepository.setDiagnosticVerboseEvents(!verboseEvents)
                }
            },
        ) {
            Text(if (verboseEvents) "Verbose events: ON (re-enable a11y)" else "Verbose events: OFF")
        }
        Text("Windows")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(foreground.value.windows) { window ->
                Text(window.describeShort())
            }
        }
        Text("Event stream (latest ${entries.value.size})")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(entries.value.takeLast(100).reversed()) { entry ->
                Text("${entry.timestampMillis} ${entry.eventTypeName} ${entry.packageName} ${entry.presentation} ${entry.transitionNote ?: ""}")
            }
        }
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateFlowCompat(): androidx.compose.runtime.State<T> {
    val state = remember { mutableStateOf(value) }
    androidx.compose.runtime.LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}
