package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ClashDataIdRegistry
import com.example.model.ClashAccount
import com.example.model.ClashVillageItem
import com.example.model.UpgradeCategory
import com.example.model.WidgetDisplayPreferences
import com.example.ui.ClashTrackerViewModel
import com.example.ui.SortOption
import com.example.ui.components.LiveWidgetPreviewCard
import com.example.ui.components.UpgradeCard
import com.example.ui.dialogs.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClashTrackerScreen(
    viewModel: ClashTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackBar()
        }
    }

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = Color(0xFF0B0E17).copy(alpha = 0.96f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand & Village Tag Identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.openEditPlayerDialog() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF283252), Color(0xFF171D30))
                                    )
                                )
                                .border(1.dp, ClashGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_widget_th_iso),
                                contentDescription = "Town Hall",
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "CLASH TRACKER",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.8.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ClashGold.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "TH${uiState.account.townHallLevel}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClashGold
                                    )
                                }
                            }
                            Text(
                                text = "${uiState.account.playerTag} • Tap to edit profile",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Sleek Top Action Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color(0xFF171D30))
                                .border(1.dp, ClashGold.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
                                .clickable { viewModel.openImportJsonDialog() }
                                .padding(horizontal = 11.dp)
                                .testTag("top_bar_import_json_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.FileUpload,
                                    contentDescription = "Import JSON",
                                    tint = ClashGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Import JSON",
                                    color = ClashGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color(0xFF171D30))
                                .border(1.dp, Color(0xFF2B3554), RoundedCornerShape(11.dp))
                                .clickable { viewModel.openWidgetPinDialog() }
                                .testTag("app_bar_widgets_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Widgets,
                                contentDescription = "Widgets",
                                tint = ClashGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddUpgradeDialog() },
                containerColor = ClashGold,
                contentColor = Color(0xFF0B0E17),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_upgrade")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Upgrade", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Upgrade", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ClashDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Sleek Bento Telemetry Summary Strip
            item {
                BentoTelemetryStrip(
                    account = uiState.account,
                    currentTimeEpochMs = uiState.currentTimeEpochMs,
                    onGemClick = { viewModel.openGemCalculatorDialog() },
                    onBuildersClick = { viewModel.openEditPlayerDialog() },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // 2. Interactive Widget Data Selector Bar (Select sections AND specific Heroes/Pets)
            item {
                WidgetDataFilterBar(
                    account = uiState.account,
                    widgetPrefs = uiState.widgetPrefs,
                    onUpdatePrefs = { viewModel.updateWidgetPrefs(it) },
                    onToggleSpecificItem = { viewModel.toggleItemNameWidgetVisibility(it) },
                    onOpenFullSelector = { viewModel.openWidgetDataDialog() },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // 3. Live Scrollable Home Screen Widget Replica
            item {
                LiveWidgetPreviewCard(
                    account = uiState.account,
                    currentTimeEpochMs = uiState.currentTimeEpochMs,
                    widgetPrefs = uiState.widgetPrefs,
                    onRefreshClick = { viewModel.forceRefreshWidgets() },
                    onCustomizeWidgetClick = { viewModel.openWidgetDataDialog() },
                    onItemClick = { upgrade -> viewModel.openEditUpgradeDialog(upgrade) },
                    onVillageItemClick = { item -> viewModel.startUpgradeFromVillageItem(item) },
                    onOpenVillageClick = { viewModel.openEditPlayerDialog() },
                    viewportMaxHeight = 430.dp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // 4. Modern Quick Action Dock (Widget Data, Pin Widget, Import JSON, Gem Calc)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.openWidgetDataDialog() },
                        modifier = Modifier
                            .weight(1.25f)
                            .height(42.dp)
                            .testTag("open_widget_data_selector_button"),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClashGold,
                            contentColor = Color(0xFF0B0E17)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Widget Data", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Widget Data", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openWidgetPinDialog() },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(42.dp)
                            .testTag("open_widget_manager_button"),
                        shape = RoundedCornerShape(13.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.horizontalGradient(listOf(ClashIndigo, ClashCyan))
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = "Pin Widget",
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFF818CF8)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Pin Widget", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openImportJsonDialog() },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(42.dp)
                            .testTag("open_import_json_button"),
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = "JSON",
                            modifier = Modifier.size(15.dp),
                            tint = ClashGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openGemCalculatorDialog() },
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Gem Calculator",
                            modifier = Modifier.size(16.dp),
                            tint = ClashGreen
                        )
                    }
                }
            }

            // 5. Sleek Segmented Switcher: Active Timers vs Village Inventory
            if (uiState.account.villageItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF131828))
                            .border(1.dp, Color(0xFF252F4A), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isTimersSelected = !uiState.showVillageInventory
                        Button(
                            onClick = { viewModel.setShowVillageInventory(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("tab_active_timers"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimersSelected) ClashGold else Color.Transparent,
                                contentColor = if (isTimersSelected) Color(0xFF0B0E17) else Color(0xFF94A3B8)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Active Timers (${uiState.account.activeUpgrades.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Button(
                            onClick = { viewModel.setShowVillageInventory(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("tab_village_inventory"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isTimersSelected) ClashGold else Color.Transparent,
                                contentColor = if (!isTimersSelected) Color(0xFF0B0E17) else Color(0xFF94A3B8)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Heroes, Pets & Village (${uiState.account.villageItems.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 6. Modern Search Bar & Sort Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search Archer Queen, Monolith, Spirit Fox...",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("search_upgrade_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF131828),
                            unfocusedContainerColor = Color(0xFF131828),
                            focusedBorderColor = ClashGold,
                            unfocusedBorderColor = Color(0xFF252F4A)
                        )
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF131828)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = ClashGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.title,
                                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortOption == option) ClashGold else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 7. Category Filter Chips
            item {
                val baseListSize = if (uiState.showVillageInventory) uiState.account.villageItems.size else uiState.account.activeUpgrades.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All ($baseListSize)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClashGold.copy(alpha = 0.2f),
                            selectedLabelColor = ClashGold
                        )
                    )

                    UpgradeCategory.entries.forEach { category ->
                        val count = if (uiState.showVillageInventory) {
                            uiState.account.villageItems.count { it.category == category }
                        } else {
                            uiState.account.activeUpgrades.count { it.category == category }
                        }
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = {
                                if (uiState.selectedCategory == category) viewModel.setCategoryFilter(null)
                                else viewModel.setCategoryFilter(category)
                            },
                            label = {
                                Text(
                                    "${category.iconLabel} ${category.displayName} ($count)",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClashGold.copy(alpha = 0.2f),
                                selectedLabelColor = ClashGold
                            )
                        )
                    }
                }
            }

            // 8. Main Content List (Village Inventory or Active Upgrades)
            if (uiState.showVillageInventory) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPECIFIC HEROES, PETS & STRUCTURES (${uiState.filteredVillageItems.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClashGold,
                            letterSpacing = 0.7.sp
                        )

                        Text(
                            text = "Tap '+Lv' to start timer",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (uiState.account.activeUpgrades.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151B2C)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "💡 Your village JSON was imported (${uiState.account.villageItems.size} items), and 0 timers were actively ticking at export time. Tap '+Lv' on any hero, pet, or building below to track it on your widget!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                items(
                    items = uiState.filteredVillageItems,
                    key = { it.id }
                ) { item ->
                    val isShownInWidget = uiState.widgetPrefs.isVillageItemVisible(item)
                    VillageItemCard(
                        item = item,
                        isShownInWidget = isShownInWidget,
                        onToggleWidgetVisibility = {
                            viewModel.toggleItemNameWidgetVisibility(item.name)
                        },
                        onStartUpgrade = { viewModel.startUpgradeFromVillageItem(item) },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE UPGRADES (${uiState.filteredUpgrades.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClashGold,
                            letterSpacing = 0.7.sp
                        )

                        Text(
                            text = "${uiState.sortOption.title} • Tap 'In Widget' to toggle",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (uiState.filteredUpgrades.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151B2C)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "No matching upgrades",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Import your Clash of Clans JSON or add an upgrade to track it on your home screen widget.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.openImportJsonDialog() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ClashGold,
                                            contentColor = Color(0xFF0B0E17)
                                        )
                                    ) {
                                        Text("Import JSON", fontWeight = FontWeight.Bold)
                                    }
                                    if (uiState.account.villageItems.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { viewModel.setShowVillageInventory(true) }
                                        ) {
                                            Text("View Village (${uiState.account.villageItems.size})")
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { viewModel.openAddUpgradeDialog() }
                                        ) {
                                            Text("Add Manually")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.filteredUpgrades,
                        key = { it.id }
                    ) { upgrade ->
                        val isShownInWidget = uiState.widgetPrefs.isUpgradeVisible(upgrade)
                        UpgradeCard(
                            upgrade = upgrade,
                            currentTimeEpochMs = uiState.currentTimeEpochMs,
                            isShownInWidget = isShownInWidget,
                            onToggleWidgetVisibility = {
                                viewModel.toggleUpgradeWidgetVisibility(upgrade.id)
                            },
                            onEdit = { viewModel.openEditUpgradeDialog(upgrade) },
                            onDelete = { viewModel.deleteUpgrade(upgrade.id) },
                            onComplete = { viewModel.completeUpgrade(upgrade.id) },
                            onSpeedUp = { hours -> viewModel.speedUpUpgrade(upgrade.id, hours) },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.isWidgetDataDialogOpen) {
        WidgetDataSelectorDialog(
            account = uiState.account,
            widgetPrefs = uiState.widgetPrefs,
            currentTimeEpochMs = uiState.currentTimeEpochMs,
            onUpdatePrefs = { viewModel.updateWidgetPrefs(it) },
            onApplyPreset = { viewModel.applyWidgetPreset(it) },
            onToggleUpgradeVisibility = { viewModel.toggleUpgradeWidgetVisibility(it) },
            onToggleItemNameVisibility = { viewModel.toggleItemNameWidgetVisibility(it) },
            onShowAllUpgrades = { viewModel.showAllUpgradesInWidget() },
            onDismiss = { viewModel.closeWidgetDataDialog() }
        )
    }

    if (uiState.isAddEditDialogOpen) {
        AddEditUpgradeDialog(
            upgradeToEdit = uiState.upgradeToEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, name, targetLevel, category, builderName, remainingSeconds, totalDurationSeconds, cost, resource ->
                viewModel.saveUpgrade(
                    id,
                    name,
                    targetLevel,
                    category,
                    builderName,
                    remainingSeconds,
                    totalDurationSeconds,
                    cost,
                    resource
                )
            }
        )
    }

    if (uiState.isImportJsonDialogOpen) {
        ImportJsonDialog(
            currentJsonExport = uiState.rawJsonExport,
            onDismiss = { viewModel.closeImportJsonDialog() },
            onImportJson = { json -> viewModel.importJson(json) },
            onLoadPreset = { presetKey -> viewModel.loadPreset(presetKey) }
        )
    }

    if (uiState.isGemCalculatorDialogOpen) {
        GemCalculatorDialog(
            onDismiss = { viewModel.closeGemCalculatorDialog() }
        )
    }

    if (uiState.isWidgetPinDialogOpen) {
        WidgetPinDialog(
            account = uiState.account,
            onDismiss = { viewModel.closeWidgetPinDialog() },
            onPinWidget = { isMulti -> viewModel.requestPinWidget(isMulti) }
        )
    }

    if (uiState.isEditPlayerDialogOpen) {
        EditPlayerDialog(
            account = uiState.account,
            onDismiss = { viewModel.closeEditPlayerDialog() },
            onSave = { name, tag, th, builders ->
                viewModel.updatePlayerInfo(name, tag, th, builders)
            }
        )
    }
}

@Composable
private fun BentoTelemetryStrip(
    account: ClashAccount,
    currentTimeEpochMs: Long,
    onGemClick: () -> Unit,
    onBuildersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextUpgrade = account.activeUpgrades
        .filter { !it.isFinished(currentTimeEpochMs) }
        .minByOrNull { it.endTimeMs }
    val totalGems = account.activeUpgrades
        .filter { !it.isFinished(currentTimeEpochMs) }
        .sumOf { it.gemCost(currentTimeEpochMs) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Card 1: Builders Status
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF192036), Color(0xFF131828))
                    )
                )
                .border(1.dp, Color(0xFF283250), RoundedCornerShape(16.dp))
                .clickable(onClick = onBuildersClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (account.availableBuildersCount == 0) ClashGold else ClashGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BUILDERS",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${account.busyBuildersCount}/${account.totalBuilders}" +
                        if (account.hasGoblinBuilder) " + G" else "",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = if (account.availableBuildersCount == 0) "All working" else "${account.availableBuildersCount} free",
                color = ClashGold,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        // Card 2: Next Finishing
        Column(
            modifier = Modifier
                .weight(1.25f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF192036), Color(0xFF131828))
                    )
                )
                .border(1.dp, Color(0xFF283250), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "NEXT FINISH",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = nextUpgrade?.formattedRemainingTime(currentTimeEpochMs) ?: "Idle",
                color = ClashGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = nextUpgrade?.name ?: "No active timers",
                color = Color(0xFFCBD5E1),
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Card 3: Gem Skip Cost
        Column(
            modifier = Modifier
                .weight(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF192036), Color(0xFF131828))
                    )
                )
                .border(1.dp, Color(0xFF283250), RoundedCornerShape(16.dp))
                .clickable(onClick = onGemClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "SKIP ALL",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "💎 $totalGems",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Gem calc ↗",
                color = ClashIndigoLight,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WidgetDataFilterBar(
    account: ClashAccount,
    widgetPrefs: WidgetDisplayPreferences,
    onUpdatePrefs: (WidgetDisplayPreferences) -> Unit,
    onToggleSpecificItem: (String) -> Unit,
    onOpenFullSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val specificHeroes = (
        account.villageItems.filter { it.category == UpgradeCategory.HERO }.map { it.name } +
        account.activeUpgrades.filter { it.category == UpgradeCategory.HERO }.map { it.name }
    ).distinct().ifEmpty {
        listOf("Barbarian King", "Archer Queen", "Grand Warden", "Royal Champion", "Minion Prince", "Dragon Duke")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131828))
            .border(1.dp, Color(0xFF252F4A), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ClashGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WIDGET DATA SELECTION",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
            }

            Text(
                text = "Customize All (${widgetPrefs.enabledSectionCount}/6) ↗",
                color = ClashGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onOpenFullSelector)
                    .testTag("widget_filter_more_options")
            )
        }

        // Row 1: Category Section Toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            WidgetDataChip(
                label = "🔨 Defenses & Buildings",
                selected = widgetPrefs.showBuilders,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showBuilders = !widgetPrefs.showBuilders)) },
                testTag = "chip_widget_builders"
            )
            WidgetDataChip(
                label = "👑 All Heroes (${specificHeroes.size})",
                selected = widgetPrefs.showHeroes,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showHeroes = !widgetPrefs.showHeroes)) },
                testTag = "chip_widget_heroes"
            )
            WidgetDataChip(
                label = "🧪 Laboratory",
                selected = widgetPrefs.showLaboratory,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showLaboratory = !widgetPrefs.showLaboratory)) },
                testTag = "chip_widget_lab"
            )
            WidgetDataChip(
                label = "🐾 Pets & Equipment",
                selected = widgetPrefs.showPetsAndEquipment,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showPetsAndEquipment = !widgetPrefs.showPetsAndEquipment)) },
                testTag = "chip_widget_pets"
            )
            WidgetDataChip(
                label = "⚙️ Builder Base",
                selected = widgetPrefs.showBuilderBase,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showBuilderBase = !widgetPrefs.showBuilderBase)) },
                testTag = "chip_widget_builder_base"
            )
            WidgetDataChip(
                label = "🏰 Village Roster",
                selected = widgetPrefs.showVillageInventory,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showVillageInventory = !widgetPrefs.showVillageInventory)) },
                testTag = "chip_widget_village"
            )
            WidgetDataChip(
                label = "🧪 Boost Bar",
                selected = widgetPrefs.showBoostsBanner,
                onClick = { onUpdatePrefs(widgetPrefs.copy(showBoostsBanner = !widgetPrefs.showBoostsBanner)) },
                testTag = "chip_widget_boosts"
            )
        }

        // Row 2: Specific Heroes Quick-Select Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HEROES:",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(end = 2.dp)
            )
            specificHeroes.forEach { heroName ->
                val isShown = widgetPrefs.showHeroes && heroName !in widgetPrefs.hiddenItemNames
                WidgetDataChip(
                    label = "👑 $heroName",
                    selected = isShown,
                    onClick = {
                        if (!widgetPrefs.showHeroes) {
                            onUpdatePrefs(widgetPrefs.copy(showHeroes = true))
                        }
                        onToggleSpecificItem(heroName)
                    },
                    testTag = "chip_hero_${heroName.lowercase().replace(" ", "_")}"
                )
            }
        }
    }
}

@Composable
private fun WidgetDataChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) ClashGold.copy(alpha = 0.18f) else Color(0xFF1A2136)
            )
            .border(
                width = 1.dp,
                color = if (selected) ClashGold else Color(0xFF2B3554),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ClashGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (selected) ClashGold else Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun VillageItemCard(
    item: ClashVillageItem,
    isShownInWidget: Boolean = true,
    onToggleWidgetVisibility: () -> Unit = {},
    onStartUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val specificSubtitle = ClashDataIdRegistry.getSpecificSubtitleForName(item.name, item.category)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isShownInWidget) Color(0xFF151B2C) else Color(0xFF101422)
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (item.isUpgrading) ClashGold else Color(0xFF26314E)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF1E263D)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = ClashDataIdRegistry.getIconDrawableResForName(item.name, item.category)
                        ),
                        contentDescription = item.name,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isShownInWidget) Color(0xFFF8FAFC) else Color(0xFF94A3B8)
                        )

                        if (item.count > 1) {
                            Text(
                                text = "×${item.count}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClashGold
                            )
                        }

                        if (item.isUpgrading) {
                            Text(
                                text = "• Upgrading",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClashGreen
                            )
                        }
                    }

                    Text(
                        text = "$specificSubtitle • Level ${item.currentLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isShownInWidget) ClashGold.copy(alpha = 0.14f) else Color(0xFF1A2134)
                        )
                        .border(
                            1.dp,
                            if (isShownInWidget) ClashGold.copy(alpha = 0.45f) else Color(0xFF28324E),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onToggleWidgetVisibility),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isShownInWidget) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isShownInWidget) "Visible in Widget" else "Hidden from Widget",
                        tint = if (isShownInWidget) ClashGold else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onStartUpgrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClashGold,
                        contentColor = Color(0xFF0B0E17)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "+Lv ${item.currentLevel + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
