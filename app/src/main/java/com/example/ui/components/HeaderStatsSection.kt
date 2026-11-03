package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClashAccount
import com.example.ui.theme.*

@Composable
fun HeaderStatsSection(
    account: ClashAccount,
    currentTimeEpochMs: Long,
    onEditProfile: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenImportJson: () -> Unit,
    onOpenGemCalculator: () -> Unit,
    onForceRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextUpgrade = account.nextFinishingUpgrade
    val totalGems = account.totalGemCost

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Player Identity Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ClashDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ClashBorder)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Town Hall Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClashGoldDark)
                            .border(1.5.dp, ClashGold, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "${account.townHallLevel}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ClashGoldLight
                            )
                        }
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = account.playerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClashTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onEditProfile() },
                                tint = ClashTextSecondary
                            )
                        }
                        Text(
                            text = "${account.playerTag} • BH ${account.builderHallLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClashTextSecondary
                        )
                    }
                }

                // Builder Free / Busy Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF232D42))
                        .border(1.dp, ClashGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "🔨 ${account.busyBuildersCount}/${account.totalBuilders} Busy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClashGold
                        )
                        Text(
                            text = if (account.availableBuildersCount > 0)
                                "${account.availableBuildersCount} Builder Free!"
                            else "All Busy",
                            fontSize = 10.sp,
                            color = if (account.availableBuildersCount > 0) ClashGreen else ClashCyan
                        )
                    }
                }
            }
        }

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Next Finishing Upgrade Card
            Card(
                modifier = Modifier.weight(1.3f),
                colors = CardDefaults.cardColors(containerColor = ClashDarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("⚡ Next Free Upgrade", fontSize = 11.sp, color = ClashTextSecondary)
                    Text(
                        text = nextUpgrade?.name ?: "All Free",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClashTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = nextUpgrade?.formattedRemainingTime(currentTimeEpochMs) ?: "No upgrades",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClashCyan
                    )
                }
            }

            // Total Gems to Finish All Card
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = ClashDarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("💎 Finish All", fontSize = 11.sp, color = ClashTextSecondary)
                    Text(
                        text = "$totalGems",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClashGoldLight
                    )
                    Text("Gems Total", fontSize = 10.sp, color = ClashTextMuted)
                }
            }
        }

        // Action Buttons Row (Widget, Import JSON, Gem Calc, Refresh)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onOpenWidgets,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("open_widget_manager_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ClashGold),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Widgets, contentDescription = "Widgets", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Widgets", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenImportJson,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("open_import_json_button"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = "Import", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import JSON", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onOpenGemCalculator,
                modifier = Modifier.weight(0.9f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Calc", fontSize = 12.sp)
            }

            IconButton(
                onClick = onForceRefresh,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ClashDarkCard)
                    .border(1.dp, ClashBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Widgets", modifier = Modifier.size(18.dp), tint = ClashCyan)
            }
        }
    }
}
