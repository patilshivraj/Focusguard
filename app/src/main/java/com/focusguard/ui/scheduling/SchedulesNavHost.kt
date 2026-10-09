package com.focusguard.ui.scheduling

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusguard.FocusGuardApp
import com.focusguard.scheduling.BlockGroup
import com.focusguard.scheduling.GroupPolicyType
import com.focusguard.scheduling.ScheduleRule
import com.focusguard.scheduling.ScheduleTemplates
import com.focusguard.scheduling.ScreenTimeLimitConfig
import com.focusguard.scheduling.ScreenTimeLimitMode
import com.focusguard.scheduling.WeeklyTimeSlot
import com.focusguard.ui.components.AppIcon
import com.focusguard.ui.components.FocusGuardCard
import com.focusguard.ui.components.ScreenHeader
import com.focusguard.util.LaunchableApps
import com.focusguard.util.PackageLabelResolver
import com.focusguard.util.TimeFormatting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SchedulesNavHost(app: FocusGuardApp) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ScheduleNavRoutes.GROUP_LIST) {
        composable(ScheduleNavRoutes.GROUP_LIST) {
            GroupListScreen(app, onOpenGroup = { id ->
                navController.navigate(ScheduleNavRoutes.whenToBlock(id))
            }, onCreateGroup = { id ->
                navController.navigate(ScheduleNavRoutes.whenToBlock(id))
            })
        }
        composable(
            ScheduleNavRoutes.WHEN_TO_BLOCK,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        ) { entry ->
            val groupId = entry.arguments?.getString("groupId") ?: return@composable
            WhenToBlockScreen(
                app = app,
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onWeekly = { navController.navigate(ScheduleNavRoutes.weeklyEditor(groupId)) },
                onScreenTime = { navController.navigate(ScheduleNavRoutes.screenTimeEditor(groupId)) },
                onEditApps = { navController.navigate(ScheduleNavRoutes.groupApps(groupId)) },
            )
        }
        composable(
            ScheduleNavRoutes.WEEKLY_EDITOR,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        ) { entry ->
            val groupId = entry.arguments?.getString("groupId") ?: return@composable
            WeeklyEditorScreen(app, groupId, onBack = { navController.popBackStack() })
        }
        composable(
            ScheduleNavRoutes.SCREEN_TIME_EDITOR,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        ) { entry ->
            val groupId = entry.arguments?.getString("groupId") ?: return@composable
            ScreenTimeEditorScreen(app, groupId, onBack = { navController.popBackStack() })
        }
        composable(
            ScheduleNavRoutes.GROUP_APPS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        ) { entry ->
            val groupId = entry.arguments?.getString("groupId") ?: return@composable
            GroupAppsScreen(app, groupId, onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupListScreen(
    app: FocusGuardApp,
    onOpenGroup: (String) -> Unit,
    onCreateGroup: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<BlockGroup>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository) {
        app.blockRepository.blockGroups.collect { groups = it }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newGroupName = ""
                    showCreateDialog = true
                },
            ) { Icon(Icons.Default.Add, contentDescription = "Add group") }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item {
                ScreenHeader(
                    title = "When to block",
                    subtitle = "Group apps and set weekly schedules or screen time limits.",
                )
            }
            items(groups, key = { it.id }) { group ->
                FocusGuardCard(
                    modifier = Modifier.padding(vertical = 6.dp),
                    onClick = { onOpenGroup(group.id) },
                ) {
                    Text(group.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${group.packageNames.size} apps · ${policyLabel(group.policyType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Name block group") },
            text = {
                Column {
                    Text(
                        "Give this group a name (e.g. Social, Work distractions).",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Group name") },
                        placeholder = { Text("Social media") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val name = newGroupName.trim().ifEmpty { "Unnamed group" }
                            val group = BlockGroup(name = name)
                            app.blockRepository.upsertBlockGroup(group)
                            showCreateDialog = false
                            onCreateGroup(group.id)
                        }
                    },
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhenToBlockScreen(
    app: FocusGuardApp,
    groupId: String,
    onBack: () -> Unit,
    onWeekly: () -> Unit,
    onScreenTime: () -> Unit,
    onEditApps: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var group by remember { mutableStateOf<BlockGroup?>(null) }
    var groupName by remember { mutableStateOf("") }
    var showChooser by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    androidx.compose.runtime.LaunchedEffect(groupId) {
        groupName = ""
        group = null
    }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository, groupId) {
        app.blockRepository.blockGroups.collect { list ->
            val g = list.find { it.id == groupId }
            group = g
            if (g != null && groupName.isEmpty()) {
                groupName = g.name
            }
        }
    }
    androidx.compose.runtime.LaunchedEffect(groupName) {
        val g = group ?: return@LaunchedEffect
        val trimmed = groupName.trim()
        if (trimmed.isEmpty() || trimmed == g.name) return@LaunchedEffect
        delay(400)
        val updated = g.copy(name = trimmed)
        scope.launch {
            app.blockRepository.upsertBlockGroup(updated)
            group = updated
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(group?.name?.ifEmpty { "When to block" } ?: "When to block") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Group name") },
                singleLine = true,
            )
            Text(
                "Set a schedule or limit to control when blocking is active.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onEditApps) { Text("Edit apps in group (${group?.packageNames?.size ?: 0})") }
            FocusGuardCard(onClick = { showChooser = true }) {
                Text("Add schedule or limit", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Weekly schedule, screen time limits, or block manually from the dashboard.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (group?.policyType == GroupPolicyType.WEEKLY_BLOCK) {
                OutlinedButton(onClick = onWeekly, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit weekly schedule (${group?.weeklySlots?.size ?: 0} slots)")
                }
            }
            if (group?.policyType == GroupPolicyType.SCREEN_TIME_DAILY ||
                group?.policyType == GroupPolicyType.SCREEN_TIME_HOURLY
            ) {
                OutlinedButton(onClick = onScreenTime, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit screen time limit")
                }
            }
        }
    }
    if (showChooser) {
        ModalBottomSheet(onDismissRequest = { showChooser = false }, sheetState = sheetState) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add schedule or limit", style = MaterialTheme.typography.titleLarge)
                ScheduleOptionCard(
                    title = "Block on a weekly schedule",
                    subtitle = "Set time intervals when apps should be blocked.",
                    icon = Icons.Default.Schedule,
                    onClick = {
                        scope.launch {
                            val g = group ?: return@launch
                            app.blockRepository.upsertBlockGroup(
                                g.copy(policyType = GroupPolicyType.WEEKLY_BLOCK, alwaysBlocked = false),
                            )
                            showChooser = false
                            onWeekly()
                        }
                    },
                )
                ScheduleOptionCard(
                    title = "Screen time limit",
                    subtitle = "Limit daily or hourly usage (requires usage access).",
                    icon = Icons.Default.Timer,
                    onClick = {
                        scope.launch {
                            val g = group ?: return@launch
                            app.blockRepository.upsertBlockGroup(
                                g.copy(policyType = GroupPolicyType.SCREEN_TIME_DAILY, alwaysBlocked = false),
                            )
                            showChooser = false
                            onScreenTime()
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyEditorScreen(app: FocusGuardApp, groupId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var group by remember { mutableStateOf<BlockGroup?>(null) }
    var slots by remember { mutableStateOf<List<WeeklyTimeSlot>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dayMask by remember { mutableIntStateOf(ScheduleRule.ALL_DAYS) }
    var startMin by remember { mutableIntStateOf(10 * 60) }
    var endMin by remember { mutableIntStateOf(13 * 60) }

    androidx.compose.runtime.LaunchedEffect(groupId) {
        val g = app.blockRepository.getBlockGroup(groupId)
        group = g
        slots = g?.weeklySlots ?: emptyList()
    }

    fun saveSlots(updated: List<WeeklyTimeSlot>) {
        slots = updated
        val g = group ?: return
        scope.launch {
            app.blockRepository.upsertBlockGroup(
                g.copy(weeklySlots = updated, policyType = GroupPolicyType.WEEKLY_BLOCK),
            )
        }
    }

    fun pickTime(current: Int, onPicked: (Int) -> Unit) {
        val h = current / 60
        val m = current % 60
        TimePickerDialog(context, { _, hour, minute -> onPicked(hour * 60 + minute) }, h, m, false).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block on a weekly schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Check, contentDescription = "Done") }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Select time to activate blocking.") }
            item { WeekHeatmap(slots) }
            item { Text("Quick templates", style = MaterialTheme.typography.titleMedium) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TemplateChip("Work", Icons.Default.Work) {
                            saveSlots(slots + ScheduleTemplates.workHours())
                        }
                    }
                    item {
                        TemplateChip("Evening", Icons.Default.NightsStay) {
                            saveSlots(slots + ScheduleTemplates.evening())
                        }
                    }
                    item {
                        TemplateChip("Weekend", Icons.Default.DateRange) {
                            saveSlots(slots + ScheduleTemplates.weekend())
                        }
                    }
                    item {
                        TemplateChip("Sleep", Icons.Default.Bedtime) {
                            saveSlots(slots + ScheduleTemplates.sleepTime())
                        }
                    }
                    item {
                        TemplateChip("Always", Icons.Default.AllInclusive) {
                            scope.launch {
                                val g = group ?: return@launch
                                app.blockRepository.upsertBlockGroup(
                                    g.copy(
                                        alwaysBlocked = true,
                                        policyType = GroupPolicyType.ALWAYS_BLOCKED,
                                    ),
                                )
                                onBack()
                            }
                        }
                    }
                }
            }
            items(slots, key = { it.id }) { slot ->
                FocusGuardCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(formatSlotRange(slot.startMinutes, slot.endMinutes))
                            Text(formatDayMask(slot.daysOfWeekBitmask), style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { saveSlots(slots.filter { it.id != slot.id }) }) {
                            Text("Remove")
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Add time") }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Set time to block apps") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Blocked on days")
                    DayOfWeekSelector(
                        dayMask = dayMask,
                        onDayMaskChange = { dayMask = it },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickTime(startMin) { startMin = it } }) {
                            Text(TimeFormatting.formatMinutesFromMidnight(startMin))
                        }
                        Text("–")
                        OutlinedButton(onClick = { pickTime(endMin) { endMin = it } }) {
                            Text(TimeFormatting.formatMinutesFromMidnight(endMin))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    saveSlots(
                        slots + WeeklyTimeSlot(
                            daysOfWeekBitmask = dayMask,
                            startMinutes = startMin,
                            endMinutes = endMin,
                        ),
                    )
                    showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTimeEditorScreen(app: FocusGuardApp, groupId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val tracker = (LocalContext.current.applicationContext as FocusGuardApp).screenTimeUsageTracker
    var group by remember { mutableStateOf<BlockGroup?>(null) }
    var hourly by remember { mutableStateOf(false) }
    var hourlyLimit by remember { mutableIntStateOf(30) }
    var dailyLimits by remember { mutableStateOf(List(7) { -1 }) }
    var limitEditRequest by remember { mutableStateOf<ScreenTimeLimitEditRequest?>(null) }

    androidx.compose.runtime.LaunchedEffect(groupId) {
        val g = app.blockRepository.getBlockGroup(groupId)
        group = g
        hourly = g?.screenTimeLimit?.mode == ScreenTimeLimitMode.HOURLY
        hourlyLimit = g?.screenTimeLimit?.hourlyLimitMinutes ?: 30
        dailyLimits = g?.screenTimeLimit?.dailyLimitsByDay ?: List(7) { -1 }
    }

    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen time limit") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            item {
                if (!tracker.hasUsageAccess()) {
                    Text("Usage access required for screen time limits.", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { tracker.openUsageAccessSettings() }) {
                        Text("Grant usage access")
                    }
                }
            }
            item {
                Row {
                    OutlinedButton(onClick = { hourly = false }) { Text(if (!hourly) "Daily ✓" else "Daily") }
                    OutlinedButton(onClick = { hourly = true }) { Text(if (hourly) "Hourly ✓" else "Hourly") }
                }
            }
            if (hourly) {
                item {
                    Text("Maximum screen time per hour: ${hourlyLimit} mins")
                    Slider(
                        value = hourlyLimit.toFloat(),
                        onValueChange = { hourlyLimit = it.toInt() },
                        valueRange = 0f..60f,
                    )
                }
            } else {
                item {
                    Button(
                        onClick = {
                            limitEditRequest = ScreenTimeLimitEditRequest(
                                dayMask = ScheduleRule.ALL_DAYS,
                                initialMinutes = 60,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Set limit for multiple days")
                    }
                }
                items(dayNames.size) { index ->
                    val limit = dailyLimits[index]
                    FocusGuardCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(dayNames[index], style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (limit < 0) "No limit" else "$limit mins",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(
                                onClick = {
                                    limitEditRequest = ScreenTimeLimitEditRequest(
                                        dayMask = dayBitsOrdered[index],
                                        initialMinutes = limit,
                                    )
                                },
                            ) { Text("Edit") }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val g = group ?: return@Button
                        scope.launch {
                            val policy = if (hourly) GroupPolicyType.SCREEN_TIME_HOURLY else GroupPolicyType.SCREEN_TIME_DAILY
                            app.blockRepository.upsertBlockGroup(
                                g.copy(
                                    policyType = policy,
                                    screenTimeLimit = ScreenTimeLimitConfig(
                                        mode = if (hourly) ScreenTimeLimitMode.HOURLY else ScreenTimeLimitMode.DAILY,
                                        dailyLimitsByDay = dailyLimits,
                                        hourlyLimitMinutes = hourlyLimit,
                                    ),
                                ),
                            )
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) { Text("Save") }
            }
        }
    }

    limitEditRequest?.let { request ->
        ScreenTimeLimitDialog(
            request = request,
            onDismiss = { limitEditRequest = null },
            onConfirm = { mask, limitMinutes ->
                dailyLimits = applyScreenTimeLimitToMask(dailyLimits, mask, limitMinutes)
                limitEditRequest = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupAppsScreen(app: FocusGuardApp, groupId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var group by remember { mutableStateOf<BlockGroup?>(null) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var query by remember { mutableStateOf("") }
    val ensurePackages = remember(selected, group?.packageNames) {
        selected + (group?.packageNames ?: emptySet())
    }
    val installed = remember(context.packageName, ensurePackages) {
        LaunchableApps.list(
            context,
            excludePackage = context.packageName,
            ensurePackages = ensurePackages,
        )
    }
    androidx.compose.runtime.LaunchedEffect(groupId) {
        val g = app.blockRepository.getBlockGroup(groupId)
        group = g
        selected = g?.packageNames ?: emptySet()
    }
    androidx.compose.runtime.LaunchedEffect(app.blockRepository, groupId) {
        app.blockRepository.blockGroups.collect { list ->
            group = list.find { it.id == groupId }
        }
    }
    val filtered = installed.filter {
        val label = PackageLabelResolver.label(context, it.packageName)
        label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(group?.name?.ifEmpty { "Apps in group" } ?: "Apps in group")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = {
                        val g = group ?: return@IconButton
                        scope.launch {
                            app.blockRepository.upsertBlockGroup(g.copy(packageNames = selected))
                            onBack()
                        }
                    }) { Icon(Icons.Default.Check, null) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Search apps") },
                    singleLine = true,
                )
            }
            items(filtered, key = { it.packageName }) { info ->
                val checked = selected.contains(info.packageName)
                FocusGuardCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { on ->
                                selected = if (on) selected + info.packageName else selected - info.packageName
                            },
                        )
                        AppIcon(
                            packageName = info.packageName,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Text(
                            PackageLabelResolver.label(context, info.packageName),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    FocusGuardCard(onClick = onClick, modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TemplateChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = androidx.compose.material3.ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

private fun policyLabel(type: GroupPolicyType): String =
    when (type) {
        GroupPolicyType.MANUAL_ONLY -> "Block now / manual"
        GroupPolicyType.WEEKLY_BLOCK -> "Weekly schedule"
        GroupPolicyType.SCREEN_TIME_DAILY -> "Daily screen time"
        GroupPolicyType.SCREEN_TIME_HOURLY -> "Hourly screen time"
        GroupPolicyType.ALWAYS_BLOCKED -> "Always blocked"
    }
