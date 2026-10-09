package com.focusguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.focusguard.FocusGuardApp
import com.focusguard.ui.components.FocusGuardCard
import com.focusguard.ui.components.ScreenHeader
import com.focusguard.util.LaunchableApps
import com.focusguard.util.PackageLabelResolver
import kotlinx.coroutines.launch

@Composable
fun BlockedAppsScreen(app: FocusGuardApp) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var blocked by remember { mutableStateOf(setOf<String>()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        app.blockRepository.blockedPackages.collect { blocked = it }
    }

    val installed = remember(context.packageName, blocked) {
        LaunchableApps.list(
            context,
            excludePackage = context.packageName,
            ensurePackages = blocked,
        )
    }

    val filtered = installed.filter {
        val label = PackageLabelResolver.label(context, it.packageName)
        label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader(
            title = "Blocked apps",
            subtitle = "Checked apps are intercepted when opened, including floating windows when overlay is on.",
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            label = { Text("Search") },
            singleLine = true,
        )
        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.packageName }) { appInfo ->
                val checked = blocked.contains(appInfo.packageName)
                val label = PackageLabelResolver.label(context, appInfo.packageName)
                val icon = remember(appInfo.packageName) {
                    try {
                        context.packageManager.getApplicationIcon(appInfo.packageName)
                            .toBitmap(64, 64)
                            .asImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                }
                FocusGuardCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                scope.launch {
                                    app.blockRepository.toggleBlockedPackage(appInfo.packageName, isChecked)
                                }
                            },
                        )
                        if (icon != null) {
                            Image(
                                bitmap = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .alpha(if (checked) 0.35f else 1f),
                            )
                        }
                        Column(Modifier.padding(start = 8.dp).weight(1f)) {
                            Text(
                                label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (checked) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            if (checked) {
                                Text(
                                    "Blocked — launches intercepted",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
