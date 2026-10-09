package com.focusguard.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focusguard.FocusGuardApp
import com.focusguard.ui.components.FocusGuardCard
import com.focusguard.ui.components.ScreenHeader
import com.focusguard.ui.components.SettingsSwitchRow
import kotlinx.coroutines.launch

@Composable
fun SettingsNavHost(app: FocusGuardApp) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SettingsRoutes.HOME,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(SettingsRoutes.HOME) {
            SettingsHomeScreen(
                app = app,
                onPin = { navController.navigate(SettingsRoutes.PIN) },
                onDiagnostics = { navController.navigate(SettingsRoutes.DIAGNOSTICS) },
                onAbout = { navController.navigate(SettingsRoutes.ABOUT) },
            )
        }
        composable(SettingsRoutes.PIN) {
            SettingsDetailScaffold(title = "PIN", onBack = { navController.popBackStack() }) { padding ->
                PinScreen(app, modifier = Modifier.padding(padding), showScreenHeader = false)
            }
        }
        composable(SettingsRoutes.DIAGNOSTICS) {
            SettingsDetailScaffold(title = "Diagnostics", onBack = { navController.popBackStack() }) { padding ->
                DiagnosticsScreen(app, modifier = Modifier.padding(padding), showScreenHeader = false)
            }
        }
        composable(SettingsRoutes.ABOUT) {
            SettingsDetailScaffold(title = "About", onBack = { navController.popBackStack() }) { padding ->
                AboutScreen(modifier = Modifier.padding(padding), showScreenHeader = false)
            }
        }
    }
}

@Composable
private fun SettingsHomeScreen(
    app: FocusGuardApp,
    onPin: () -> Unit,
    onDiagnostics: () -> Unit,
    onAbout: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var blockingEnabled by remember { mutableStateOf(true) }
    var blockNow by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.blockingEnabled.collect { blockingEnabled = it }
    }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.blockNow.collect { blockNow = it }
    }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.blockingOverlayEnabled.collect { overlayEnabled = it }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Settings",
                subtitle = "Blocking controls, security, diagnostics, and app information.",
            )
        }
        item {
            SettingsSwitchRow(
                title = "Enable blocking",
                subtitle = "Master switch for all rules",
                checked = blockingEnabled,
                onCheckedChange = { checked ->
                    scope.launch { app.blockRepository.setBlockingEnabled(checked) }
                },
            )
        }
        item {
            SettingsSwitchRow(
                title = "Block now",
                subtitle = "Immediately block all listed apps",
                checked = blockNow,
                onCheckedChange = { checked ->
                    scope.launch { app.blockRepository.setBlockNow(checked) }
                },
            )
        }
        item {
            SettingsSwitchRow(
                title = "Floating window overlay",
                subtitle = "Recommended for Motorola pop-up / freeform windows",
                checked = overlayEnabled,
                onCheckedChange = { checked ->
                    scope.launch { app.blockRepository.setBlockingOverlayEnabled(checked) }
                },
            )
        }
        item {
            SettingsNavRow(
                title = "PIN",
                subtitle = "Require PIN for Settings, Apps, and Schedules",
                icon = Icons.Default.Lock,
                onClick = onPin,
            )
        }
        item {
            SettingsNavRow(
                title = "Diagnostics",
                subtitle = "Window snapshots, event stream, export logs",
                icon = Icons.Default.BugReport,
                onClick = onDiagnostics,
            )
        }
        item {
            SettingsNavRow(
                title = "About",
                subtitle = "Version, permissions, limitations",
                icon = Icons.Default.Info,
                onClick = onAbout,
            )
        }
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    FocusGuardCard(
        modifier = Modifier.padding(vertical = 6.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (padding: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        content = content,
    )
}
