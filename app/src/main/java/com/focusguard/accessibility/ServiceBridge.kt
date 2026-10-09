package com.focusguard.accessibility

import com.focusguard.accessibility.model.WindowFrame
import com.focusguard.blocking.BlockingEnforcementHost
import com.focusguard.diagnostics.DiagnosticEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AccessibilityConnectionState {
    DISCONNECTED,
    CONNECTED,
}

data class ForegroundState(
    val packageName: String? = null,
    val className: String? = null,
    val windows: List<WindowFrame> = emptyList(),
    val visibleWindowCount: Int = 0,
)

object ServiceBridge {
    private val _connectionState = MutableStateFlow(AccessibilityConnectionState.DISCONNECTED)
    val connectionState: StateFlow<AccessibilityConnectionState> = _connectionState.asStateFlow()

    private val _foregroundState = MutableStateFlow(ForegroundState())
    val foregroundState: StateFlow<ForegroundState> = _foregroundState.asStateFlow()

    private val _diagnosticEntries = MutableStateFlow<List<DiagnosticEntry>>(emptyList())
    val diagnosticEntries: StateFlow<List<DiagnosticEntry>> = _diagnosticEntries.asStateFlow()

    @Volatile
    var accessibilityService: FocusGuardAccessibilityService? = null

    @Volatile
    var enforcementHost: BlockingEnforcementHost? = null

    fun setConnected(service: FocusGuardAccessibilityService?) {
        accessibilityService = service
        if (service == null) {
            enforcementHost?.hideOverlay()
            enforcementHost = null
        }
        _connectionState.value =
            if (service != null) AccessibilityConnectionState.CONNECTED
            else AccessibilityConnectionState.DISCONNECTED
    }

    fun updateForeground(state: ForegroundState) {
        _foregroundState.value = state
    }

    fun appendDiagnostic(entry: DiagnosticEntry) {
        val current = _diagnosticEntries.value
        val next = (current + entry).takeLast(1000)
        _diagnosticEntries.value = next
    }

    fun clearDiagnostics() {
        _diagnosticEntries.value = emptyList()
    }

    fun replaceDiagnostics(entries: List<DiagnosticEntry>) {
        _diagnosticEntries.value = entries.takeLast(1000)
    }
}
