package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.model.ClashAccount
import com.example.ui.theme.ClashGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlayerDialog(
    account: ClashAccount,
    onDismiss: () -> Unit,
    onSave: (name: String, tag: String, thLevel: Int, builders: Int) -> Unit
) {
    var name by remember { mutableStateOf(account.playerName) }
    var tag by remember { mutableStateOf(account.playerTag) }
    var thText by remember { mutableStateOf(account.townHallLevel.toString()) }
    var buildersText by remember { mutableStateOf(account.totalBuilders.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Clash Profile",
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player / Village Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Player Tag") },
                    placeholder = { Text("#92L0UV8") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = thText,
                        onValueChange = { thText = it.filter { c -> c.isDigit() } },
                        label = { Text("Town Hall Level") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = buildersText,
                        onValueChange = { buildersText = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Builders") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val th = thText.toIntOrNull()?.coerceIn(1, 17) ?: 16
                    val b = buildersText.toIntOrNull()?.coerceIn(1, 10) ?: 6
                    val n = if (name.isBlank()) "Chief Clash" else name.trim()
                    val t = if (tag.isBlank()) "#92L0UV8" else tag.trim()
                    onSave(n, t, th, b)
                },
                modifier = Modifier.testTag("save_player_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ClashGold)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
