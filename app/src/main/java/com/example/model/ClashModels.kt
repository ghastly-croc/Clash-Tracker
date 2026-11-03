package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class UpgradeCategory(val displayName: String, val iconLabel: String) {
    BUILDING("Defenses & Buildings", "🔨"),
    LABORATORY("Troops & Spells", "🧪"),
    HERO("Village Heroes", "👑"),
    PET("Hero Pets", "🐾"),
    EQUIPMENT("Hero Equipment", "⚔️"),
    BUILDER_BASE("Builder Base", "⚙️")
}

enum class UpgradeResource(val displayName: String, val symbol: String) {
    GOLD("Gold", "🪙"),
    ELIXIR("Elixir", "💧"),
    DARK_ELIXIR("Dark Elixir", "🟣"),
    BUILDER_GOLD("Builder Gold", "🪙"),
    BUILDER_ELIXIR("Builder Elixir", "💧"),
    ORE("Ores", "💎"),
    GEMS("Gems", "💎")
}

enum class WidgetPreset(val label: String, val subtitle: String) {
    ALL_DATA("Everything", "All timers, heroes, pets & village items"),
    TIMERS_ONLY("Active Timers", "Active Builders, Heroes, Lab & Pets"),
    BUILDERS_ONLY("Builders & Heroes", "Home Village Builders & Specific Heroes"),
    COMPACT("Compact Clean", "Timers without finish dates or notes")
}

data class WidgetDisplayPreferences(
    val showBuilders: Boolean = true,
    val showHeroes: Boolean = true,
    val showLaboratory: Boolean = true,
    val showPetsAndEquipment: Boolean = true,
    val showBuilderBase: Boolean = true,
    val showVillageInventory: Boolean = true,
    val showBoostsBanner: Boolean = true,
    val showProgressBars: Boolean = true,
    val showFinishTimestamp: Boolean = true,
    val showHelperNotes: Boolean = true,
    val hiddenUpgradeIds: Set<String> = emptySet(),
    val hiddenItemNames: Set<String> = emptySet()
) {
    fun isUpgradeVisible(upgrade: ClashUpgrade): Boolean {
        if (upgrade.id in hiddenUpgradeIds || upgrade.name in hiddenItemNames) return false
        return when (upgrade.category) {
            UpgradeCategory.BUILDING -> showBuilders
            UpgradeCategory.HERO -> showHeroes
            UpgradeCategory.LABORATORY -> showLaboratory
            UpgradeCategory.PET, UpgradeCategory.EQUIPMENT -> showPetsAndEquipment
            UpgradeCategory.BUILDER_BASE -> showBuilderBase
        }
    }

    fun isVillageItemVisible(item: ClashVillageItem): Boolean {
        if (!showVillageInventory) return false
        if (item.name in hiddenItemNames) return false
        return when (item.category) {
            UpgradeCategory.HERO -> showHeroes
            UpgradeCategory.PET, UpgradeCategory.EQUIPMENT -> showPetsAndEquipment
            UpgradeCategory.LABORATORY -> showLaboratory
            UpgradeCategory.BUILDER_BASE -> showBuilderBase
            UpgradeCategory.BUILDING -> showBuilders
        }
    }

    val enabledSectionCount: Int
        get() = listOf(
            showBuilders,
            showHeroes,
            showLaboratory,
            showPetsAndEquipment,
            showBuilderBase,
            showVillageInventory
        ).count { it }
}

data class ClashVillageItem(
    val id: String = UUID.randomUUID().toString(),
    val dataId: Int = 0,
    val name: String,
    val currentLevel: Int,
    val count: Int = 1,
    val category: UpgradeCategory = UpgradeCategory.BUILDING,
    val resource: UpgradeResource = UpgradeResource.GOLD,
    val isUpgrading: Boolean = false
)

data class ClashUpgrade(
    val id: String = UUID.randomUUID().toString(),
    val dataId: Int = 0,
    val name: String,
    val targetLevel: Int,
    val category: UpgradeCategory = UpgradeCategory.BUILDING,
    val builderName: String = "Builder #1",
    val startTimeMs: Long = System.currentTimeMillis(),
    val endTimeMs: Long,
    val totalDurationSeconds: Long = (endTimeMs - startTimeMs).coerceAtLeast(1000) / 1000,
    val cost: Long = 0L,
    val resource: UpgradeResource = UpgradeResource.GOLD,
    val isGoblin: Boolean = false,
    val helperTag: String = "",
    val helperNote: String = ""
) {
    val displayTitle: String
        get() {
            var base = name
            if (isGoblin && !base.contains("· G")) {
                base = "$base · G"
            }
            if (helperTag.isNotBlank() && !base.contains("· $helperTag")) {
                base = "$base · $helperTag"
            }
            return base
        }

    fun remainingSeconds(now: Long = System.currentTimeMillis()): Long {
        return ((endTimeMs - now) / 1000).coerceAtLeast(0)
    }

    fun isFinished(now: Long = System.currentTimeMillis()): Boolean {
        return now >= endTimeMs
    }

    fun progressFraction(now: Long = System.currentTimeMillis()): Float {
        if (totalDurationSeconds <= 0) return 1f
        val elapsed = ((now - startTimeMs) / 1000).coerceAtLeast(0)
        return (elapsed.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0.06f, 1f)
    }

    fun formattedRemainingTime(now: Long = System.currentTimeMillis()): String {
        val sec = remainingSeconds(now)
        if (sec <= 0) return "Ready!"
        val days = sec / 86400
        val hours = (sec % 86400) / 3600
        val minutes = (sec % 3600) / 60
        val seconds = sec % 60

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun formattedFinishTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.US)
        val formatted = sdf.format(Date(endTimeMs))
            .replace("AM", "am")
            .replace("PM", "pm")
        return "Finishes $formatted"
    }

    fun gemCost(now: Long = System.currentTimeMillis()): Int {
        return calculateClashGems(remainingSeconds(now))
    }
}

data class ClashAccount(
    val playerTag: String = "#QUJGYLGOP",
    val playerName: String = "#QUJGYLGOP",
    val townHallLevel: Int = 18,
    val builderHallLevel: Int = 10,
    val totalBuilders: Int = 6,
    val activeBoostsText: String = "No active boosts",
    val activeUpgrades: List<ClashUpgrade> = emptyList(),
    val villageItems: List<ClashVillageItem> = emptyList(),
    val lastUpdatedEpochMs: Long = System.currentTimeMillis(),
    val rawJson: String = ""
) {
    private val builderUpgrades: List<ClashUpgrade>
        get() = activeUpgrades.filter {
            !it.isFinished() && (it.category == UpgradeCategory.BUILDING || it.category == UpgradeCategory.HERO)
        }

    val hasGoblinBuilder: Boolean
        get() = activeUpgrades.any { !it.isFinished() && (it.isGoblin || it.name.contains("· G") || it.builderName.contains("Goblin", ignoreCase = true)) } ||
                builderUpgrades.size > totalBuilders

    val busyBuildersCount: Int
        get() {
            val regularBusy = builderUpgrades.count { !it.isGoblin && !it.name.contains("· G") }
            return regularBusy.coerceAtMost(totalBuilders)
        }

    val availableBuildersCount: Int
        get() = (totalBuilders - busyBuildersCount).coerceAtLeast(0)

    val headerSubtitleText: String
        get() {
            val goblinSuffix = if (hasGoblinBuilder) " + G" else ""
            return "TH $townHallLevel · $busyBuildersCount/$totalBuilders BUILDERS$goblinSuffix"
        }

    val sectionHeaderText: String
        get() {
            val goblinSuffix = if (hasGoblinBuilder) " + Goblin" else ""
            return "BUILDERS  $busyBuildersCount/$totalBuilders$goblinSuffix"
        }

    val formattedUpdatedTime: String
        get() {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val timeStr = sdf.format(Date(lastUpdatedEpochMs))
                .replace("AM", "am")
                .replace("PM", "pm")
            return "UPDATED $timeStr"
        }

    val nextFinishingUpgrade: ClashUpgrade?
        get() = activeUpgrades
            .filter { !it.isFinished() }
            .minByOrNull { it.endTimeMs }

    val totalGemCost: Int
        get() = activeUpgrades.filter { !it.isFinished() }.sumOf { it.gemCost() }
}

/**
 * Supercell official Clash of Clans gem calculation formula
 */
fun calculateClashGems(seconds: Long): Int {
    if (seconds <= 0) return 0
    if (seconds <= 60) return 1
    return when {
        seconds <= 3600 -> {
            (1 + (seconds.toDouble() / 60.0) * (19.0 / 60.0)).toInt()
        }
        seconds <= 86400 -> {
            (20 + ((seconds - 3600).toDouble() * 240.0 / 82800.0)).toInt()
        }
        seconds <= 604800 -> {
            (260 + ((seconds - 86400).toDouble() * 740.0 / 518400.0)).toInt()
        }
        else -> {
            (1000 + ((seconds - 604800).toDouble() * 2750.0 / 1209600.0)).toInt()
        }
    }
}
