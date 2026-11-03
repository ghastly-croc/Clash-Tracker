package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClashAccount
import com.example.ui.components.LiveWidgetPreviewCard
import com.example.ui.theme.ClashCyan
import com.example.ui.theme.ClashDarkBg
import com.example.ui.theme.ClashDarkSurfaceVariant
import com.example.ui.theme.ClashGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPinDialog(
    account: ClashAccount,
    onDismiss: () -> Unit,
    onPinWidget: (isMulti: Boolean) -> Unit
) {
    val now = System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📲 Home Screen Widgets",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pin your village upgrade tracker widget directly to your Android home screen:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Full Village Builders Widget (4x5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ClashDarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Village Builders Widget (4x5)", fontWeight = FontWeight.Bold, color = ClashGold)
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text("Recommended", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Live Preview using LiveWidgetPreviewCard (scrollable inside dialog)
                        LiveWidgetPreviewCard(
                            account = account,
                            currentTimeEpochMs = now,
                            onRefreshClick = {},
                            viewportMaxHeight = 240.dp
                        )

                        Button(
                            onClick = { onPinWidget(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_multi_widget_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ClashGold)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1E1303))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pin Village Widget to Home Screen", fontWeight = FontWeight.Bold, color = Color(0xFF1E1303))
                        }
                    }
                }

                // Option 2: Single Next-Up Widget (2x2)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ClashDarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Compact Next-Up Widget (2x2)", fontWeight = FontWeight.Bold, color = ClashCyan)
                        Text(
                            text = "Compact 2x2 widget displaying the next upgrade completing with countdown timer and gem skip cost.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ClashDarkBg, RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF5C5343), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NEXT UPGRADE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2B959))
                                val next = account.nextFinishingUpgrade
                                Text(
                                    text = next?.formattedRemainingTime(now) ?: "READY!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8CE99A)
                                )
                                Text(
                                    text = next?.let { "${it.displayTitle} → ${it.targetLevel}" } ?: "All Builders Free",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { onPinWidget(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_single_widget_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ClashCyan)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pin Compact Widget to Home Screen", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
