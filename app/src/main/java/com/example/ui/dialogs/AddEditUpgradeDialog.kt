package com.example.ui.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.ClashUpgrade
import com.example.model.UpgradeCategory
import com.example.model.UpgradeResource
import com.example.ui.theme.ClashGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUpgradeDialog(
    upgradeToEdit: ClashUpgrade?,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        name: String,
        targetLevel: Int,
        category: UpgradeCategory,
        builderName: String,
        remainingSeconds: Long,
        totalDurationSeconds: Long,
        cost: Long,
        resource: UpgradeResource
    ) -> Unit
) {
    val isEditing = upgradeToEdit != null && upgradeToEdit.id.isNotBlank()

    var name by remember { mutableStateOf(upgradeToEdit?.name ?: "") }
    var targetLevelText by remember { mutableStateOf((upgradeToEdit?.targetLevel ?: 1).toString()) }
    var category by remember { mutableStateOf(upgradeToEdit?.category ?: UpgradeCategory.BUILDING) }
    var builderName by remember { mutableStateOf(upgradeToEdit?.builderName ?: "Builder #1") }

    val initialSeconds = upgradeToEdit?.remainingSeconds() ?: (3600 * 12)
    var daysText by remember { mutableStateOf((initialSeconds / 86400).toString()) }
    var hoursText by remember { mutableStateOf(((initialSeconds % 86400) / 3600).toString()) }
    var minutesText by remember { mutableStateOf(((initialSeconds % 3600) / 60).toString()) }

    var costText by remember { mutableStateOf((upgradeToEdit?.cost ?: 0L).toString()) }
    var resource by remember { mutableStateOf(upgradeToEdit?.resource ?: UpgradeResource.GOLD) }

    val popularSuggestions = remember(category) {
        when (category) {
            UpgradeCategory.BUILDING -> listOf(
                "Town Hall", "Multi-Gear Tower", "Monolith", "Super Wizard Tower",
                "Firespitter", "Hero Hall", "Laboratory", "Ricochet Cannon",
                "Multi-Archer Tower", "Eagle Artillery", "Scattershot", "Inferno Tower", "X-Bow"
            )
            UpgradeCategory.LABORATORY -> listOf(
                "Root Rider", "Thrower", "Druid", "Furnace", "Meteor Golem",
                "Electro Titan", "Electro Dragon", "Dragon Rider", "Overgrowth Spell",
                "Revive Spell", "Recall Spell", "Rage Spell", "Healing Spell"
            )
            UpgradeCategory.HERO -> listOf(
                "Barbarian King", "Archer Queen", "Grand Warden",
                "Royal Champion", "Minion Prince", "Dragon Duke",
                "Longshot (Guardian)", "Smasher (Guardian)"
            )
            UpgradeCategory.PET -> listOf(
                "Spirit Fox", "Sneezy", "Angry Jelly", "Greedy Raven",
                "Phoenix", "Diggy", "Frosty", "Poison Lizard",
                "Unicorn", "Electro Owl", "Mighty Yak", "L.A.S.S.I"
            )
            UpgradeCategory.EQUIPMENT -> listOf(
                "Electro Boots", "Magic Mirror", "Spiky Ball", "Rocket Spear",
                "Fireball", "Giant Gauntlet", "Frozen Arrow", "Lavaloon Puppet",
                "Dark Orb", "Henchmen Puppet", "Snake Bracelet", "Action Figure",
                "Dark Crown", "Metal Pants", "Noble Iron"
            )
            UpgradeCategory.BUILDER_BASE -> listOf(
                "Builder Hall", "Mega Tesla", "Battle Machine", "Battle Copter",
                "Lava Launcher", "Roaster", "Multi Mortar", "Giant Cannon", "O.T.T.O's Outpost"
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Upgrade" else "Add Active Upgrade",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClashGold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selector
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UpgradeCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("${cat.iconLabel} ${cat.displayName}") }
                        )
                    }
                }

                // Name with Quick suggestions
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Specific Hero / Building / Troop Name") },
                    placeholder = { Text("e.g. Archer Queen, Monolith, Spirit Fox") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upgrade_name_input"),
                    singleLine = true
                )

                // Quick suggestions chips (all specific items in category)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularSuggestions.forEach { suggestion ->
                        AssistChip(
                            onClick = { name = suggestion },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Target Level and Builder Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetLevelText,
                        onValueChange = { targetLevelText = it.filter { c -> c.isDigit() } },
                        label = { Text("Target Level") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("target_level_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = builderName,
                        onValueChange = { builderName = it },
                        label = { Text("Builder / Slot") },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("builder_name_input"),
                        singleLine = true
                    )
                }

                // Time Duration (Days, Hours, Minutes)
                Text("Time Remaining", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it.filter { c -> c.isDigit() } },
                        label = { Text("Days") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter { c -> c.isDigit() } },
                        label = { Text("Hours") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Mins") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Cost & Resource Type
                Text("Upgrade Cost", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it.filter { c -> c.isDigit() } },
                        label = { Text("Cost Amount") },
                        modifier = Modifier.weight(1.3f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Type", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(UpgradeResource.GOLD, UpgradeResource.ELIXIR, UpgradeResource.DARK_ELIXIR, UpgradeResource.ORE).forEach { res ->
                                FilterChip(
                                    selected = resource == res,
                                    onClick = { resource = res },
                                    label = { Text("${res.symbol} ${res.displayName}") }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysText.toLongOrNull() ?: 0L
                    val hours = hoursText.toLongOrNull() ?: 0L
                    val mins = minutesText.toLongOrNull() ?: 0L
                    val totalSec = (days * 86400) + (hours * 3600) + (mins * 60)
                    val targetLevel = targetLevelText.toIntOrNull() ?: 1
                    val cost = costText.toLongOrNull() ?: 0L

                    val durationSeconds = if (isEditing && upgradeToEdit != null) {
                        upgradeToEdit.totalDurationSeconds.coerceAtLeast(totalSec)
                    } else {
                        (totalSec * 1.5).toLong().coerceAtLeast(totalSec)
                    }

                    val finalName = if (name.isBlank()) popularSuggestions.first() else name.trim()

                    onSave(
                        upgradeToEdit?.id,
                        finalName,
                        targetLevel,
                        category,
                        builderName,
                        totalSec,
                        durationSeconds,
                        cost,
                        resource
                    )
                },
                modifier = Modifier.testTag("save_upgrade_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ClashGold)
            ) {
                Text(if (isEditing) "Save Changes" else "Add Upgrade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
