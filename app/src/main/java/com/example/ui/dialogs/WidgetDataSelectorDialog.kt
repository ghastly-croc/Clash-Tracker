package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ClashDataIdRegistry
import com.example.model.ClashAccount
import com.example.model.UpgradeCategory
import com.example.model.WidgetDisplayPreferences
import com.example.model.WidgetPreset

@Composable
fun WidgetDataSelectorDialog(
    account: ClashAccount,
    widgetPrefs: WidgetDisplayPreferences,
    currentTimeEpochMs: Long,
    onUpdatePrefs: (WidgetDisplayPreferences) -> Unit,
    onApplyPreset: (WidgetPreset) -> Unit,
    onToggleUpgradeVisibility: (String) -> Unit,
    onToggleItemNameVisibility: (String) -> Unit = {},
    onShowAllUpgrades: () -> Unit,
    onDismiss: () -> Unit
) {
    val activeUpgrades = account.activeUpgrades.filter { !it.isFinished(currentTimeEpochMs) }
    val buildingCount = activeUpgrades.count { it.category == UpgradeCategory.BUILDING }
    val heroCount = activeUpgrades.count { it.category == UpgradeCategory.HERO }
    val labCount = activeUpgrades.count { it.category == UpgradeCategory.LABORATORY }
    val petEquipCount = activeUpgrades.count {
        it.category == UpgradeCategory.PET || it.category == UpgradeCategory.EQUIPMENT
    }
    val builderBaseCount = activeUpgrades.count { it.category == UpgradeCategory.BUILDER_BASE }
    val villageCount = account.villageItems.size

    val specificHeroNames = (
        account.villageItems.filter { it.category == UpgradeCategory.HERO }.map { it.name } +
        activeUpgrades.filter { it.category == UpgradeCategory.HERO }.map { it.name }
    ).distinct().ifEmpty {
        listOf("Barbarian King", "Archer Queen", "Grand Warden", "Royal Champion", "Minion Prince", "Dragon Duke")
    }

    val specificPetAndEquipNames = (
        account.villageItems.filter {
            it.category == UpgradeCategory.PET || it.category == UpgradeCategory.EQUIPMENT
        }.map { it.name } +
        activeUpgrades.filter {
            it.category == UpgradeCategory.PET || it.category == UpgradeCategory.EQUIPMENT
        }.map { it.name }
    ).distinct().ifEmpty {
        listOf("Spirit Fox", "Phoenix", "Diggy", "Sneezy", "Giant Gauntlet", "Electro Boots")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 690.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF5C451).copy(alpha = 0.55f), Color(0xFF6366F1).copy(alpha = 0.45f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF111522)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5C451).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFFF5C451),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Widget Data Studio",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Select specific heroes, buildings, pets & timers",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Quick Presets
                    Text(
                        text = "QUICK PRESETS",
                        color = Color(0xFFF5C451),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetPillCard(
                                preset = WidgetPreset.ALL_DATA,
                                onClick = { onApplyPreset(WidgetPreset.ALL_DATA) },
                                modifier = Modifier.weight(1f)
                            )
                            PresetPillCard(
                                preset = WidgetPreset.TIMERS_ONLY,
                                onClick = { onApplyPreset(WidgetPreset.TIMERS_ONLY) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetPillCard(
                                preset = WidgetPreset.BUILDERS_ONLY,
                                onClick = { onApplyPreset(WidgetPreset.BUILDERS_ONLY) },
                                modifier = Modifier.weight(1f)
                            )
                            PresetPillCard(
                                preset = WidgetPreset.COMPACT,
                                onClick = { onApplyPreset(WidgetPreset.COMPACT) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 2. Data Sections in Widget
                    Text(
                        text = "WIDGET DATA SECTIONS",
                        color = Color(0xFFF5C451),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF181D2E))
                            .border(1.dp, Color(0xFF283149), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetToggleRow(
                            title = "🔨 Home Village Defenses & Buildings",
                            subtitle = "Monolith, Multi-Gear Tower, Super Wizard Tower, Hero Hall ($buildingCount active)",
                            checked = widgetPrefs.showBuilders,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showBuilders = it)) },
                            testTag = "toggle_widget_builders"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "👑 Specific Heroes (${specificHeroNames.size})",
                            subtitle = "${specificHeroNames.joinToString(", ")} ($heroCount upgrading)",
                            checked = widgetPrefs.showHeroes,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showHeroes = it)) },
                            testTag = "toggle_widget_heroes"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "🧪 Laboratory Troops & Spells",
                            subtitle = "Root Rider, Electro Titan, Thrower, Overgrowth Spell ($labCount active)",
                            checked = widgetPrefs.showLaboratory,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showLaboratory = it)) },
                            testTag = "toggle_widget_lab"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "🐾 Hero Pets & Blacksmith Equipment",
                            subtitle = "${specificPetAndEquipNames.joinToString(", ")} ($petEquipCount active)",
                            checked = widgetPrefs.showPetsAndEquipment,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showPetsAndEquipment = it)) },
                            testTag = "toggle_widget_pets"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "⚙️ Builder Base (Master Builder & O.T.T.O)",
                            subtitle = "Mega Tesla, Battle Machine, Battle Copter ($builderBaseCount active)",
                            checked = widgetPrefs.showBuilderBase,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showBuilderBase = it)) },
                            testTag = "toggle_widget_builder_base"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "🏰 Full Village Roster & Levels",
                            subtitle = "Include all imported Heroes, Pets, Equipment & Defenses ($villageCount items)",
                            checked = widgetPrefs.showVillageInventory,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showVillageInventory = it)) },
                            testTag = "toggle_widget_village_inventory"
                        )
                    }

                    // 3. Specific Hero & Pet Selection Chips
                    Text(
                        text = "SELECT SPECIFIC HEROES & PETS",
                        color = Color(0xFFF5C451),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF181D2E))
                            .border(1.dp, Color(0xFF283149), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tap any Hero, Pet, or Equipment to show or hide it in the widget:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            specificHeroNames.forEach { heroName ->
                                val isShown = heroName !in widgetPrefs.hiddenItemNames && widgetPrefs.showHeroes
                                SpecificEntityChip(
                                    label = "👑 $heroName",
                                    selected = isShown,
                                    onClick = { onToggleItemNameVisibility(heroName) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            specificPetAndEquipNames.forEach { itemName ->
                                val isShown = itemName !in widgetPrefs.hiddenItemNames && widgetPrefs.showPetsAndEquipment
                                SpecificEntityChip(
                                    label = "🐾 $itemName",
                                    selected = isShown,
                                    onClick = { onToggleItemNameVisibility(itemName) }
                                )
                            }
                        }
                    }

                    // 4. Widget Layout & Details
                    Text(
                        text = "CARD APPEARANCE & METADATA",
                        color = Color(0xFFF5C451),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF181D2E))
                            .border(1.dp, Color(0xFF283149), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetToggleRow(
                            title = "Boosts Potion Banner",
                            subtitle = "Show 'No active boosts' bar at the top of the widget",
                            checked = widgetPrefs.showBoostsBanner,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showBoostsBanner = it)) },
                            testTag = "toggle_widget_boosts_banner"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "Indigo Progress Bars",
                            subtitle = "Show completion bar under each remaining timer",
                            checked = widgetPrefs.showProgressBars,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showProgressBars = it)) },
                            testTag = "toggle_widget_progress_bars"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "Exact Finish Date & Time",
                            subtitle = "Show 'Finishes dd/MM/yyyy h:mm a' on each item",
                            checked = widgetPrefs.showFinishTimestamp,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showFinishTimestamp = it)) },
                            testTag = "toggle_widget_finish_time"
                        )
                        HorizontalDivider(color = Color(0xFF252D44))
                        WidgetToggleRow(
                            title = "Builder's Apprentice & Lab Assistant Notes",
                            subtitle = "Show 'Apprentice · repeat forecast off' status line",
                            checked = widgetPrefs.showHelperNotes,
                            onCheckedChange = { onUpdatePrefs(widgetPrefs.copy(showHelperNotes = it)) },
                            testTag = "toggle_widget_helper_notes"
                        )
                    }

                    // 5. Individual Active Upgrade Selection
                    if (activeUpgrades.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INDIVIDUAL ACTIVE TIMERS (${activeUpgrades.count { widgetPrefs.isUpgradeVisible(it) }}/${activeUpgrades.size})",
                                color = Color(0xFFF5C451),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            if (widgetPrefs.hiddenUpgradeIds.isNotEmpty() || widgetPrefs.hiddenItemNames.isNotEmpty()) {
                                TextButton(
                                    onClick = onShowAllUpgrades,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Show All",
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF181D2E))
                                .border(1.dp, Color(0xFF283149), RoundedCornerShape(16.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeUpgrades.forEach { upgrade ->
                                val isShown = widgetPrefs.isUpgradeVisible(upgrade)
                                val specificRole = ClashDataIdRegistry.getSpecificSubtitleForName(upgrade.name, upgrade.category)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isShown) Color(0xFF21283E) else Color(0xFF141826))
                                        .clickable { onToggleUpgradeVisibility(upgrade.id) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = ClashDataIdRegistry.getIconDrawableRes(upgrade)),
                                        contentDescription = upgrade.name,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${upgrade.displayTitle} → ${upgrade.targetLevel}",
                                            color = if (isShown) Color.White else Color(0xFF64748B),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$specificRole • ${upgrade.formattedRemainingTime(currentTimeEpochMs)}",
                                            color = if (isShown) Color(0xFF34D399) else Color(0xFF475569),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isShown) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isShown) "Visible in Widget" else "Hidden from Widget",
                                        tint = if (isShown) Color(0xFFF5C451) else Color(0xFF475569),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("widget_data_done_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5C451),
                        contentColor = Color(0xFF111522)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply to Widget",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecificEntityChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFF5C451).copy(alpha = 0.18f) else Color(0xFF121624))
            .border(
                1.dp,
                if (selected) Color(0xFFF5C451) else Color(0xFF28324E),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFFF5C451),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (selected) Color(0xFFF5C451) else Color(0xFF64748B),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PresetPillCard(
    preset: WidgetPreset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C2236))
            .border(1.dp, Color(0xFF2E3856), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = preset.label,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = preset.subtitle,
            color = Color(0xFF94A3B8),
            fontSize = 10.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WidgetToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF111522),
                checkedTrackColor = Color(0xFFF5C451),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF22293E)
            )
        )
    }
}
