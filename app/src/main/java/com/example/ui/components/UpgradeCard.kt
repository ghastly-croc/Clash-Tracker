package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.data.ClashDataIdRegistry
import com.example.model.ClashUpgrade
import com.example.ui.theme.*

@Composable
fun UpgradeCard(
    upgrade: ClashUpgrade,
    currentTimeEpochMs: Long,
    isShownInWidget: Boolean = true,
    onToggleWidgetVisibility: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onSpeedUp: (hours: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinished = upgrade.isFinished(currentTimeEpochMs)
    val remainingFormatted = upgrade.formattedRemainingTime(currentTimeEpochMs)
    val progressFraction = upgrade.progressFraction(currentTimeEpochMs)
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "progress")
    val gemCost = upgrade.gemCost(currentTimeEpochMs)

    var showSpeedUpMenu by remember { mutableStateOf(false) }

    val cardBorderBrush = when {
        isFinished -> Brush.linearGradient(listOf(ClashGreen, ClashGreen.copy(alpha = 0.5f)))
        isShownInWidget -> Brush.linearGradient(
            listOf(
                Color(0xFF2E3A5C),
                Color(0xFF202840)
            )
        )
        else -> Brush.linearGradient(listOf(Color(0xFF1E2436), Color(0xFF181D2C)))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp))
            .testTag("upgrade_card_${upgrade.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isShownInWidget) Color(0xFF151B2C) else Color(0xFF101422)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Squircle Isometric Icon Badge
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF283352),
                                    Color(0xFF1A2136)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF334168), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = ClashDataIdRegistry.getIconDrawableRes(upgrade)),
                        contentDescription = upgrade.name,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Line 1: Building Name + · G / · H8 on left, → Level pill on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = upgrade.displayTitle,
                            color = if (isShownInWidget) Color(0xFFF8FAFC) else Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ClashGold.copy(alpha = 0.15f))
                                .border(0.8.dp, ClashGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "→ Lv ${upgrade.targetLevel}",
                                color = ClashGold,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Line 2: Remaining time in mint green + gem skip cost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = remainingFormatted,
                                color = ClashGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "• ${ClashDataIdRegistry.getSpecificSubtitleForName(upgrade.name, upgrade.category)}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!isFinished && gemCost > 0) {
                            Text(
                                text = "💎 $gemCost",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // Line 3: Sleek luminous progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0D111C))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.coerceIn(0.05f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF6366F1), Color(0xFF38BDF8))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Line 4: Finish timestamp & optional helper note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = upgrade.formattedFinishTime(),
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            color = Color(0xFF818CF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (upgrade.helperNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = upgrade.helperNote,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF232C46).copy(alpha = 0.7f), thickness = 0.8.dp)

            // Action Buttons Row with Widget Visibility Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle whether this upgrade is shown in the widget
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isShownInWidget) ClashGold.copy(alpha = 0.14f)
                                else Color(0xFF1B2234)
                            )
                            .border(
                                1.dp,
                                if (isShownInWidget) ClashGold.copy(alpha = 0.45f) else Color(0xFF2B3550),
                                RoundedCornerShape(9.dp)
                            )
                            .clickable(onClick = onToggleWidgetVisibility)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("toggle_upgrade_widget_${upgrade.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isShownInWidget) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isShownInWidget) "In Widget" else "Hidden from Widget",
                                tint = if (isShownInWidget) ClashGold else Color(0xFF64748B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isShownInWidget) "In Widget" else "Hidden",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isShownInWidget) ClashGold else Color(0xFF64748B)
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showSpeedUpMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.height(29.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Speed Up", modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Boost", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        DropdownMenu(
                            expanded = showSpeedUpMenu,
                            onDismissRequest = { showSpeedUpMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("+1 Hour (Apprentice Lv 8)") },
                                onClick = {
                                    onSpeedUp(1)
                                    showSpeedUpMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("+9 Hours (Builder Potion 🧪)") },
                                onClick = {
                                    onSpeedUp(9)
                                    showSpeedUpMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("+24 Hours (1 Day ⏱️)") },
                                onClick = {
                                    onSpeedUp(24)
                                    showSpeedUpMenu = false
                                }
                            )
                        }
                    }

                    if (!isFinished) {
                        TextButton(
                            onClick = onComplete,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(29.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                modifier = Modifier.size(13.dp),
                                tint = ClashGreen
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Done", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ClashGreen)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(15.dp),
                            tint = ClashTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
