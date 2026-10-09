package com.focusguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguard.FocusGuardApp
import com.focusguard.accessibility.AccessibilityConnectionState
import com.focusguard.accessibility.ServiceBridge
import com.focusguard.permissions.AccessibilityServiceHelper
import com.focusguard.ui.components.FocusGuardCard
import com.focusguard.ui.components.ScreenHeader
import com.focusguard.util.PackageLabelResolver
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DashboardScreen(app: FocusGuardApp) {
    var blockingEnabled by remember { mutableStateOf(true) }
    var blockNow by remember { mutableStateOf(false) }
    var blockedCount by remember { mutableStateOf(0) }
    var groupCount by remember { mutableStateOf(0) }
    var blockedPackagesPreview by remember { mutableStateOf<List<String>>(emptyList()) }
    val connection = ServiceBridge.connectionState.collectAsStateCompat(ServiceBridge.connectionState.value)
    var a11yEnabled by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val protectionOk = connection.value == AccessibilityConnectionState.CONNECTED && a11yEnabled

    fun refreshA11yEnabled() {
        a11yEnabled = AccessibilityServiceHelper.isServiceEnabled(context)
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        refreshA11yEnabled()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshA11yEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.settings.collect { settings ->
            blockingEnabled = com.focusguard.data.SettingsMapper.isBlockingEnabled(settings)
            blockNow = settings.blockNow
            blockedCount = settings.blockedPackagesList.size
            blockedPackagesPreview = settings.blockedPackagesList
            groupCount = settings.blockGroupsList.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = "FocusGuard",
            subtitle = "Block distractions across normal, floating, and split-screen modes.",
            showLogo = true,
        )

        FocusGuardCard {
            Text("Protection", style = MaterialTheme.typography.titleMedium)
            Text(
                text = when {
                    !blockingEnabled -> "Blocking is turned off"
                    blockNow -> "Block now is active"
                    blockedCount == 0 -> "No apps selected"
                    else -> "$blockedCount app(s) · $groupCount schedule group(s)"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            Text(
                text = if (blockingEnabled && blockedCount > 0) "ACTIVE" else "INACTIVE",
                color = if (blockingEnabled && blockedCount > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold,
            )
        }

        AccessibilityStatusCard(
            protectionOk = protectionOk,
            a11yEnabled = a11yEnabled,
            serviceConnected = connection.value == AccessibilityConnectionState.CONNECTED,
            onOpenSettings = { AccessibilityServiceHelper.openAccessibilitySettings(context) },
        )

        if (blockedPackagesPreview.isNotEmpty()) {
            FocusGuardCard {
                Text("Blocked apps", style = MaterialTheme.typography.titleMedium)
                blockedPackagesPreview.take(5).forEach { pkg ->
                    Text(
                        PackageLabelResolver.label(context, pkg),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (blockedPackagesPreview.size > 5) {
                    Text(
                        "+${blockedPackagesPreview.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        if (blockedCount == 0) {
            Text(
                "Add apps under Blocked apps or create a schedule group.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        FocusGuardCard {
            Text(
                "Change blocking controls in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccessibilityStatusCard(
    protectionOk: Boolean,
    a11yEnabled: Boolean,
    serviceConnected: Boolean,
    onOpenSettings: () -> Unit,
) {
    val statusMessage = when {
        protectionOk -> "FocusGuard can detect and block app windows."
        !a11yEnabled -> "Turn on FocusGuard in Android Accessibility settings to block apps."
        !serviceConnected -> "Accessibility is on but the service is not connected. Re-enable FocusGuard or reopen the app."
        else -> "Service disconnected."
    }
    val setupSteps = when {
        protectionOk -> null
        !a11yEnabled -> "Settings → Accessibility → Installed apps → FocusGuard → Allow"
        else -> "Open Accessibility settings and confirm FocusGuard is enabled."
    }

    FocusGuardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = when {
                    protectionOk -> Icons.Default.CheckCircle
                    !a11yEnabled -> Icons.Default.AccessibilityNew
                    else -> Icons.Default.Warning
                },
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = when {
                    protectionOk -> MaterialTheme.colorScheme.primary
                    !a11yEnabled -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.tertiary
                },
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Accessibility", style = MaterialTheme.typography.titleMedium)
                Text(
                    statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = when {
                        protectionOk -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = if (protectionOk) "Connected" else "Action required",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            protectionOk -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        },
                    )
                }
            }
        }

        if (!protectionOk) {
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    Icons.Default.AccessibilityNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Open Accessibility settings")
            }
            if (setupSteps != null) {
                Text(
                    setupSteps,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun <T> StateFlow<T>.collectAsStateCompat(initial: T): androidx.compose.runtime.State<T> {
    val state = remember { mutableStateOf(initial) }
    androidx.compose.runtime.LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}
