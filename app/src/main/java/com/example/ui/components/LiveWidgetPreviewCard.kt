package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ClashDataIdRegistry
import com.example.model.ClashAccount
import com.example.model.ClashUpgrade
import com.example.model.ClashVillageItem
import com.example.model.UpgradeCategory
import com.example.model.WidgetDisplayPreferences

private val WidgetCardBg = Color(0xFF1C202E)
private val WidgetCardBorder = Color(0xFF5C5343)
private val WidgetRefreshBtnBg = Color(0xFF252A3D)
private val WidgetBoostBg = Color(0xFF212234)
private val WidgetBoostBorder = Color(0xFF5E4768)
private val WidgetBoostText = Color(0xFFD8D4E6)
private val WidgetItemBg = Color(0xFF24293A)
private val WidgetGold = Color(0xFFE2B959)
private val WidgetMintGreen = Color(0xFF8CE99A)
private val WidgetTrackDark = Color(0xFF191D2B)
private val WidgetProgressStart = Color(0xFF5865F2)
private val WidgetProgressEnd = Color(0xFF818CF8)
private val WidgetFinishSubtext = Color(0xFF94A3B8)
private val WidgetFooterLink = Color(0xFFCBD5E1)
private val WidgetFooterUpdated = Color(0xFF8492A6)

@Composable
fun LiveWidgetPreviewCard(
    account: ClashAccount,
    currentTimeEpochMs: Long,
    onRefreshClick: () -> Unit,
    widgetPrefs: WidgetDisplayPreferences = WidgetDisplayPreferences(),
    onCustomizeWidgetClick: () -> Unit = {},
    onItemClick: (ClashUpgrade) -> Unit = {},
    onVillageItemClick: (ClashVillageItem) -> Unit = {},
    onOpenVillageClick: () -> Unit = {},
    viewportMaxHeight: Dp = 430.dp,
    modifier: Modifier = Modifier
) {
    val allActiveUpgrades = account.activeUpgrades
        .filter { !it.isFinished(currentTimeEpochMs) }
        .sortedBy { it.endTimeMs }

    val visibleUpgrades = allActiveUpgrades.filter { widgetPrefs.isUpgradeVisible(it) }

    val builderUpgrades = visibleUpgrades.filter {
        it.category == UpgradeCategory.BUILDING || it.category == UpgradeCategory.HERO
    }
    val labAndPetUpgrades = visibleUpgrades.filter {
        it.category == UpgradeCategory.LABORATORY ||
                it.category == UpgradeCategory.PET ||
                it.category == UpgradeCategory.EQUIPMENT
    }
    val builderBaseUpgrades = visibleUpgrades.filter {
        it.category == UpgradeCategory.BUILDER_BASE
    }

    val visibleVillageItems = account.villageItems.filter { widgetPrefs.isVillageItemVisible(it) }
    val villageHeroes = visibleVillageItems.filter { it.category == UpgradeCategory.HERO }
    val villagePetsAndEquip = visibleVillageItems.filter {
        it.category == UpgradeCategory.PET || it.category == UpgradeCategory.EQUIPMENT
    }
    val villageBuildings = visibleVillageItems.filter { it.category == UpgradeCategory.BUILDING }
    val villageLab = visibleVillageItems.filter { it.category == UpgradeCategory.LABORATORY }
    val villageBuilderBase = visibleVillageItems.filter { it.category == UpgradeCategory.BUILDER_BASE }

    val headerTitle = if (account.playerTag.isNotBlank() && account.playerName.startsWith("Village ")) {
        account.playerTag
    } else {
        account.playerName.ifBlank { account.playerTag }
    }

    val widgetScrollState = rememberScrollState()
    val totalScrollableCount = visibleUpgrades.size + visibleVillageItems.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WidgetCardBg)
            .border(1.5.dp, WidgetCardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("live_widget_card")
    ) {
        // 1. Fixed Header Row: Isometric Town Hall + Player Tag + TH/Builders Subtitle + Customize & Refresh Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_widget_th_iso),
                contentDescription = "Town Hall",
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headerTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = account.headerSubtitleText,
                    color = WidgetGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Customize Widget Data button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WidgetRefreshBtnBg)
                    .border(1.dp, WidgetGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onCustomizeWidgetClick)
                    .testTag("widget_preview_customize_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Select Widget Data",
                    tint = WidgetGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sync / Refresh button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WidgetRefreshBtnBg)
                    .clickable(onClick = onRefreshClick)
                    .testTag("widget_preview_refresh_button"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_widget_sync_gold),
                    contentDescription = "Refresh Widget",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Optional Fixed Boosts Banner ("No active boosts")
        if (widgetPrefs.showBoostsBanner) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WidgetBoostBg)
                    .border(1.dp, WidgetBoostBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_widget_potion_iso),
                    contentDescription = "Builder Potion",
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = account.activeBoostsText,
                    color = WidgetBoostText,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "↕ Scroll ($totalScrollableCount)",
                    color = WidgetGold.copy(alpha = 0.85f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Scrollable Interior Viewport containing selected Widget Data
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = viewportMaxHeight)
                .verticalScrollWithScrollbar(widgetScrollState)
                .verticalScroll(widgetScrollState)
                .padding(end = 6.dp)
                .testTag("widget_scrollable_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val anySectionHasRows =
                ((widgetPrefs.showBuilders || widgetPrefs.showHeroes) && builderUpgrades.isNotEmpty()) ||
                ((widgetPrefs.showLaboratory || widgetPrefs.showPetsAndEquipment) && labAndPetUpgrades.isNotEmpty()) ||
                (widgetPrefs.showBuilderBase && builderBaseUpgrades.isNotEmpty()) ||
                visibleVillageItems.isNotEmpty()

            // Section A: Home Village Builders (Buildings & Heroes)
            if (widgetPrefs.showBuilders || widgetPrefs.showHeroes) {
                WidgetSectionHeader(title = account.sectionHeaderText)

                if (builderUpgrades.isEmpty() && !anySectionHasRows) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WidgetItemBg)
                            .padding(vertical = 18.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (allActiveUpgrades.isNotEmpty()) {
                                "Selected filters hid ${allActiveUpgrades.size} active upgrades • Tap Customize Data"
                            } else {
                                "All builders idle • Import JSON or tap + to add an upgrade"
                            },
                            color = WidgetFinishSubtext,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    builderUpgrades.forEach { upgrade ->
                        WidgetUpgradeRowItem(
                            upgrade = upgrade,
                            currentTimeEpochMs = currentTimeEpochMs,
                            showProgressBar = widgetPrefs.showProgressBars,
                            showFinishTimestamp = widgetPrefs.showFinishTimestamp,
                            showHelperNote = widgetPrefs.showHelperNotes,
                            onClick = { onItemClick(upgrade) }
                        )
                    }
                }
            }

            // Section B: Laboratory, Pet House & Blacksmith
            if ((widgetPrefs.showLaboratory || widgetPrefs.showPetsAndEquipment) && labAndPetUpgrades.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "LABORATORY & PETS  ${labAndPetUpgrades.size} ACTIVE")
                labAndPetUpgrades.forEach { upgrade ->
                    WidgetUpgradeRowItem(
                        upgrade = upgrade,
                        currentTimeEpochMs = currentTimeEpochMs,
                        showProgressBar = widgetPrefs.showProgressBars,
                        showFinishTimestamp = widgetPrefs.showFinishTimestamp,
                        showHelperNote = widgetPrefs.showHelperNotes,
                        onClick = { onItemClick(upgrade) }
                    )
                }
            }

            // Section C: Builder Base Active Upgrades
            if (widgetPrefs.showBuilderBase && builderBaseUpgrades.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "BUILDER BASE  ${builderBaseUpgrades.size} ACTIVE")
                builderBaseUpgrades.forEach { upgrade ->
                    WidgetUpgradeRowItem(
                        upgrade = upgrade,
                        currentTimeEpochMs = currentTimeEpochMs,
                        showProgressBar = widgetPrefs.showProgressBars,
                        showFinishTimestamp = widgetPrefs.showFinishTimestamp,
                        showHelperNote = widgetPrefs.showHelperNotes,
                        onClick = { onItemClick(upgrade) }
                    )
                }
            }

            // Section D1: Specific Village Heroes (Barbarian King, Archer Queen, Grand Warden, Royal Champion, Minion Prince, Dragon Duke)
            if (villageHeroes.isNotEmpty()) {
                val heroNamesSummary = villageHeroes.joinToString(", ") { it.name.substringBefore(" (") }
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "HEROES  ($heroNamesSummary)")
                villageHeroes.forEach { hero ->
                    WidgetVillageRowItem(
                        item = hero,
                        onClick = { onVillageItemClick(hero) }
                    )
                }
            }

            // Section D2: Specific Hero Pets & Blacksmith Equipment
            if (villagePetsAndEquip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "HERO PETS & EQUIPMENT  (${villagePetsAndEquip.size})")
                villagePetsAndEquip.forEach { item ->
                    WidgetVillageRowItem(
                        item = item,
                        onClick = { onVillageItemClick(item) }
                    )
                }
            }

            // Section D3: Home Village Defenses & Structures
            if (villageBuildings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "HOME VILLAGE DEFENSES & STRUCTURES  (${villageBuildings.size})")
                villageBuildings.forEach { item ->
                    WidgetVillageRowItem(
                        item = item,
                        onClick = { onVillageItemClick(item) }
                    )
                }
            }

            // Section D4: Laboratory Troops & Spells
            if (villageLab.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "LABORATORY TROOPS & SPELLS  (${villageLab.size})")
                villageLab.forEach { item ->
                    WidgetVillageRowItem(
                        item = item,
                        onClick = { onVillageItemClick(item) }
                    )
                }
            }

            // Section D5: Builder Base Structures
            if (villageBuilderBase.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                WidgetSectionHeader(title = "BUILDER BASE STRUCTURES  (${villageBuilderBase.size})")
                villageBuilderBase.forEach { item ->
                    WidgetVillageRowItem(
                        item = item,
                        onClick = { onVillageItemClick(item) }
                    )
                }
            }

            if (!anySectionHasRows && !(widgetPrefs.showBuilders || widgetPrefs.showHeroes)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WidgetItemBg)
                        .clickable(onClick = onCustomizeWidgetClick)
                        .padding(vertical = 22.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sections enabled for this widget • Tap to select widget data",
                        color = WidgetGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Fixed Footer ("OPEN VILLAGE  ↗     UPDATED 10:11 am")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "OPEN VILLAGE  ↗",
                    color = WidgetFooterLink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.clickable(onClick = onOpenVillageClick)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = account.formattedUpdatedTime,
                    color = WidgetFooterUpdated,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }

            Text(
                text = "${visibleUpgrades.size}/${allActiveUpgrades.size} TIMERS",
                color = WidgetMintGreen,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onCustomizeWidgetClick)
            )
        }
    }
}

@Composable
private fun WidgetSectionHeader(title: String) {
    Text(
        text = title,
        color = WidgetGold,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
    )
}

@Composable
fun WidgetUpgradeRowItem(
    upgrade: ClashUpgrade,
    currentTimeEpochMs: Long,
    onClick: () -> Unit,
    showProgressBar: Boolean = true,
    showFinishTimestamp: Boolean = true,
    showHelperNote: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = upgrade.progressFraction(currentTimeEpochMs).coerceIn(0.05f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WidgetItemBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = ClashDataIdRegistry.getIconDrawableRes(upgrade)),
            contentDescription = upgrade.name,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Line 1: Building/Hero Name + optional · G / · H8 on left, → Level on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = upgrade.displayTitle,
                    color = Color(0xFFF8FAFC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "→ ${upgrade.targetLevel}",
                    color = WidgetGold,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Line 2: Remaining time in mint green ("5h 1m", "18h 34m", "1d 5h 1m")
            Text(
                text = upgrade.formattedRemainingTime(currentTimeEpochMs),
                color = WidgetMintGreen,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )

            // Line 3: Thin 3dp indigo-blue progress bar on #191D2B track
            if (showProgressBar) {
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(WidgetTrackDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(WidgetProgressStart, WidgetProgressEnd)
                                )
                            )
                    )
                }
            }

            // Line 4: Exact finish timestamp ("Finishes 03/11/2026 3:11 pm")
            if (showFinishTimestamp) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = upgrade.formattedFinishTime(),
                    color = WidgetFinishSubtext,
                    fontSize = 11.sp
                )
            }

            // Line 5 (optional): Helper status ("Apprentice · repeat forecast off")
            if (showHelperNote && upgrade.helperNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = upgrade.helperNote,
                    color = WidgetFinishSubtext,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun WidgetVillageRowItem(
    item: ClashVillageItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val specificRole = ClashDataIdRegistry.getSpecificSubtitleForName(item.name, item.category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WidgetItemBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = ClashDataIdRegistry.getIconDrawableResForName(item.name, item.category)
            ),
            contentDescription = item.name,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    color = Color(0xFFF8FAFC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.count > 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "×${item.count}",
                        color = WidgetGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "$specificRole · Level ${item.currentLevel}" +
                        if (item.isUpgrading) " · Upgrading" else "",
                color = if (item.isUpgrading) WidgetMintGreen else WidgetFinishSubtext,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "Lv ${item.currentLevel} → ${item.currentLevel + 1}",
            color = WidgetGold,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun Modifier.verticalScrollWithScrollbar(
    scrollState: ScrollState,
    trackColor: Color = Color(0xFF151824),
    thumbColor: Color = Color(0xFFE2B959).copy(alpha = 0.65f)
): Modifier = this.drawWithContent {
    drawContent()
    val maxScroll = scrollState.maxValue
    if (maxScroll > 0) {
        val barWidth = 3.dp.toPx()
        val barX = size.width - barWidth
        val viewportHeight = size.height
        val totalContentHeight = viewportHeight + maxScroll
        val thumbHeight = (viewportHeight * (viewportHeight / totalContentHeight)).coerceAtLeast(28.dp.toPx())
        val scrollFraction = scrollState.value.toFloat() / maxScroll.toFloat()
        val thumbOffsetY = scrollFraction * (viewportHeight - thumbHeight)

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(barX, 0f),
            size = Size(barWidth, viewportHeight),
            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
        )
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(barX, thumbOffsetY),
            size = Size(barWidth, thumbHeight),
            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
        )
    }
}
