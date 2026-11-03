package com.example.ui.dialogs

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
import androidx.compose.ui.unit.sp
import com.example.model.calculateClashGems
import com.example.ui.theme.ClashDarkSurfaceVariant
import com.example.ui.theme.ClashGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemCalculatorDialog(
    onDismiss: () -> Unit
) {
    var daysText by remember { mutableStateOf("2") }
    var hoursText by remember { mutableStateOf("12") }
    var minutesText by remember { mutableStateOf("0") }

    val totalSeconds = remember(daysText, hoursText, minutesText) {
        val d = daysText.toLongOrNull() ?: 0L
        val h = hoursText.toLongOrNull() ?: 0L
        val m = minutesText.toLongOrNull() ?: 0L
        (d * 86400) + (h * 3600) + (m * 60)
    }

    val calculatedGems = remember(totalSeconds) {
        calculateClashGems(totalSeconds)
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
                    text = "💎 Gem & Magic Items Calculator",
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
                Text(
                    text = "Calculate exact Clash of Clans gem skip cost and compare with Magic Item boost efficiency.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Input row
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

                // Gem Result Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Official Supercell Gem Skip Cost", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💎 $calculatedGems Gems",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClashGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Time: ${daysText.ifBlank { "0" }}d ${hoursText.ifBlank { "0" }}h ${minutesText.ifBlank { "0" }}m ($totalSeconds seconds)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Magic Items Comparison
                Text("Magic Item Value Guide", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ClashDarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MagicItemRow(
                            icon = "🧪",
                            name = "Builder Potion",
                            effect = "10x speed for 1 hr",
                            saves = "Saves 9 hrs per builder (up to 54 hrs across 6 builders!)"
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        MagicItemRow(
                            icon = "🔬",
                            name = "Research Potion",
                            effect = "24x speed for 1 hr",
                            saves = "Saves 23 hours in the Laboratory instantly"
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        MagicItemRow(
                            icon = "🐾",
                            name = "Pet Potion",
                            effect = "24x speed for 1 hr",
                            saves = "Saves 23 hours in the Pet House"
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        MagicItemRow(
                            icon = "📖",
                            name = "Book of Building / Heroes",
                            effect = "Instant completion",
                            saves = "Best used on upgrades longer than 6 days (saves 1,000+ gems!)"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ClashGold)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun MagicItemRow(
    icon: String,
    name: String,
    effect: String,
    saves: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 20.sp)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(effect, style = MaterialTheme.typography.labelSmall, color = ClashGold)
            }
            Text(saves, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
