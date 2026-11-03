package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClashJsonParser
import com.example.ui.theme.ClashCyan
import com.example.ui.theme.ClashDarkSurfaceVariant
import com.example.ui.theme.ClashGold
import com.example.ui.theme.ClashGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportJsonDialog(
    currentJsonExport: String,
    onDismiss: () -> Unit,
    onImportJson: (String) -> Boolean,
    onLoadPreset: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var jsonInputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var copiedToClipboard by remember { mutableStateOf(false) }

    val livePreviewAccount = remember(jsonInputText) {
        if (jsonInputText.isBlank()) null
        else runCatching { ClashJsonParser.parse(jsonInputText) }.getOrNull()
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    jsonInputText = content
                    val success = onImportJson(content)
                    if (!success) {
                        errorMessage = "Could not parse imported file as Clash of Clans JSON."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to read file: ${e.localizedMessage}"
            }
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
                    text = "Clash JSON Data",
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
                    .heightIn(max = 500.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ClashDarkSurfaceVariant,
                    contentColor = ClashGold
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Import JSON") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Presets") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Export") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Import JSON tab
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Supports Clash of Clans In-Game Village Export (Settings > More Settings > Export Data), Supercell API dumps, and tracker JSONs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pick_json_file_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ClashCyan),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick .json File", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clip = clipboardManager.getText()?.text
                                        if (!clip.isNullOrBlank()) {
                                            jsonInputText = clip
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Clipboard is empty."
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("paste_clipboard_button"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paste Clipboard", fontSize = 12.sp)
                                }
                            }

                            if (livePreviewAccount != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ClashDarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "✅ Ready: ${livePreviewAccount.playerName} (${livePreviewAccount.playerTag}) • TH ${livePreviewAccount.townHallLevel}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ClashGreen
                                        )
                                        Text(
                                            text = "Found ${livePreviewAccount.activeUpgrades.size} active timer(s) and ${livePreviewAccount.villageItems.size} village structures/troops.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = jsonInputText,
                                onValueChange = {
                                    jsonInputText = it
                                    errorMessage = null
                                },
                                label = { Text("Paste Clash of Clans JSON Here") },
                                placeholder = {
                                    Text(
                                        "{\n  \"tag\": \"#GU2QV0Y8Q\",\n  \"buildings\": [\n    { \"data\": 1000001, \"lvl\": 16 },\n    { \"data\": 1000031, \"lvl\": 6, \"timer\": 24973 }\n  ],\n  \"heroes\": [\n    { \"data\": 28000001, \"lvl\": 92, \"timer\": 48600 }\n  ]\n}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .testTag("paste_json_input"),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                ),
                                maxLines = 12
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    1 -> {
                        // Presets Tab
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Select a realistic Clash of Clans JSON preset to load or test:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            PresetCard(
                                title = "Official In-Game Village Export (TH 18)",
                                description = "Raw Supercell Settings > Data Export format ('data' IDs, 'lvl', 'timer', Goblin 'extra', Apprentice 'helpers')",
                                badge = "TH 18 • 6/6 + G",
                                onClick = { onLoadPreset("IN_GAME_EXPORT") }
                            )

                            PresetCard(
                                title = "TH 18 Full Builders + Goblin (#QUJGYLGOP)",
                                description = "Dark Elixir Storage → 13, Multi-Gear Tower · G → 3, Monolith → 5, Hero Hall · H8 → 12, Laboratory → 16, Super Wizard Tower → 2",
                                badge = "TH 18 • 6/6 + G",
                                onClick = { onLoadPreset("TH16_MAX") }
                            )

                            PresetCard(
                                title = "TH15 Spell Tower & Warden",
                                description = "Spell Tower, Grand Warden, Electro Dragon Lab research, Diggy Pet training",
                                badge = "TH 15 • 6 Builders",
                                onClick = { onLoadPreset("TH15_RUSH") }
                            )

                            PresetCard(
                                title = "TH14 Hero & Troop Focus",
                                description = "Barbarian King Lv 78, Healer Lab research, defensive traps",
                                badge = "TH 14 • 5 Builders",
                                onClick = { onLoadPreset("TH14_LAB") }
                            )
                        }
                    }

                    2 -> {
                        // Export Tab
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Current account JSON representation with updated remaining timers:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(currentJsonExport))
                                        copiedToClipboard = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (copiedToClipboard) "Copied!" else "Copy JSON")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_TEXT, currentJsonExport)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Clash JSON"))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share")
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                colors = CardDefaults.cardColors(containerColor = ClashDarkSurfaceVariant)
                            ) {
                                Text(
                                    text = currentJsonExport,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .verticalScroll(rememberScrollState())
                                        .horizontalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = {
                        if (jsonInputText.isBlank()) {
                            errorMessage = "Please paste JSON or pick a .json file before importing."
                        } else {
                            val success = onImportJson(jsonInputText)
                            if (!success) {
                                errorMessage = "Invalid JSON format. Check keys and syntax."
                            }
                        }
                    },
                    modifier = Modifier.testTag("submit_import_json_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ClashGold)
                ) {
                    Text("Import & Update Widgets", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PresetCard(
    title: String,
    description: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ClashDarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClashGold)
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(badge, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
