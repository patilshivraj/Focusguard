package com.focusguard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusguard.FocusGuardApp
import com.focusguard.ui.navigation.Routes
import com.focusguard.ui.screens.BlockedAppsScreen
import com.focusguard.ui.screens.DashboardScreen
import com.focusguard.ui.screens.SchedulesScreen
import com.focusguard.ui.screens.SettingsScreen
import com.focusguard.ui.theme.FocusGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FocusGuardApp
        setContent {
            FocusGuardTheme {
                FocusGuardRoot(app)
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun FocusGuardRoot(app: FocusGuardApp) {
    val navController = rememberNavController()
    val items = listOf(
        NavItem(Routes.Dashboard, "Home", Icons.Default.Home),
        NavItem(Routes.BlockedApps, "Apps", Icons.Default.Apps),
        NavItem(Routes.Schedules, "Schedule", Icons.Default.Schedule),
        NavItem(Routes.Settings, "Settings", Icons.Default.Settings),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.Dashboard
    var pinUnlockedRoutes by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = current == item.route,
                        onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Dashboard) { DashboardScreen(app) }
            composable(Routes.BlockedApps) {
                ProtectedRoute(app, route = Routes.BlockedApps, pinUnlockedRoutes, onUnlock = {
                    pinUnlockedRoutes = pinUnlockedRoutes + Routes.BlockedApps
                }) { BlockedAppsScreen(app) }
            }
            composable(Routes.Schedules) {
                ProtectedRoute(app, route = Routes.Schedules, pinUnlockedRoutes, onUnlock = {
                    pinUnlockedRoutes = pinUnlockedRoutes + Routes.Schedules
                }) { SchedulesScreen(app) }
            }
            composable(Routes.Settings) {
                ProtectedRoute(app, route = Routes.Settings, pinUnlockedRoutes, onUnlock = {
                    pinUnlockedRoutes = pinUnlockedRoutes + Routes.Settings
                }) { SettingsScreen(app) }
            }
        }
    }
}

@Composable
private fun ProtectedRoute(
    app: FocusGuardApp,
    route: String,
    unlocked: Set<String>,
    onUnlock: () -> Unit,
    content: @Composable () -> Unit,
) {
    // Gate only when PIN is enabled AND a hash exists (avoids lockout if toggle was on with no PIN).
    val pinRequired by app.pinRepository.pinRequired.collectAsStateWithLifecycleCompat(false)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        app.pinRepository.clearOrphanedPinEnabled()
    }
    if (pinRequired && !unlocked.contains(route)) {
        com.focusguard.ui.pin.PinGate(app, pinEnabled = true, onUnlocked = onUnlock) {}
    } else {
        content()
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.Flow<T>.collectAsStateWithLifecycleCompat(initial: T): androidx.compose.runtime.State<T> {
    val state = remember { mutableStateOf(initial) }
    androidx.compose.runtime.LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}
