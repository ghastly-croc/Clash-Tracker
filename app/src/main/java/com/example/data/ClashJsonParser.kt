package com.example.data

import com.example.model.ClashAccount
import com.example.model.ClashUpgrade
import com.example.model.ClashVillageItem
import com.example.model.UpgradeCategory
import com.example.model.UpgradeResource
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ClashJsonParser {

    private val VILLAGE_SECTION_KEYS = listOf(
        "buildings",
        "th_buildings",
        "traps",
        "heroes",
        "units",
        "troops",
        "siege_machines",
        "spells",
        "pets",
        "equipment",
        "heroEquipment",
        "buildings2",
        "bh_buildings",
        "traps2",
        "units2",
        "heroes2",
        "guardians"
    )

    private val IGNORED_VILLAGE_SECTIONS = setOf(
        "helpers",
        "decos",
        "obstacles",
        "obstacles2",
        "skins",
        "sceneries",
        "house_parts",
        "boosts",
        "achievements",
        "labels",
        "badges"
    )

    /**
     * Parses any Clash of Clans JSON string into a ClashAccount:
     * 1. Official Clash of Clans In-Game Village Export JSON ("tag", "timestamp", "buildings", "heroes", "units", "pets", "spells", "traps", "buildings2", "helpers", "boosts")
     * 2. Official Supercell REST API Player JSON ("tag", "name", "townHallLevel", "heroes", "troops", "spells", "heroEquipment")
     * 3. Custom Tracker / Widget Export JSON ("activeUpgrades", "upgrades", "timers", "queue")
     * 4. Raw JSONArray of upgrade/building objects
     */
    fun parse(jsonString: String): ClashAccount {
        val cleaned = extractJsonBody(jsonString)
        if (cleaned.isEmpty()) {
            throw IllegalArgumentException("JSON content is empty.")
        }

        val now = System.currentTimeMillis()

        if (cleaned.startsWith("[")) {
            val jsonArray = JSONArray(cleaned)
            val upgrades = mutableListOf<ClashUpgrade>()
            val villageItems = mutableListOf<ClashVillageItem>()
            parseMixedArray(
                array = jsonArray,
                sectionName = "upgrades",
                now = now,
                treatAllAsUpgrades = false,
                apprenticeLevel = 8,
                upgradesOut = upgrades,
                villageOut = villageItems
            )
            if (upgrades.isEmpty() && jsonArray.length() > 0) {
                parseMixedArray(
                    array = jsonArray,
                    sectionName = "upgrades",
                    now = now,
                    treatAllAsUpgrades = true,
                    apprenticeLevel = 8,
                    upgradesOut = upgrades,
                    villageOut = mutableListOf()
                )
            }
            return ClashAccount(
                playerTag = "#QUJGYLGOP",
                playerName = "#QUJGYLGOP",
                activeUpgrades = assignBuilderSlots(upgrades),
                villageItems = sortVillageItems(villageItems),
                rawJson = cleaned
            )
        }

        if (!cleaned.startsWith("{")) {
            throw IllegalArgumentException("Invalid JSON format. Expected JSON Object '{...}' or Array '[...]'.")
        }

        val rawRoot = JSONObject(cleaned)
        val root = unwrapRootObject(rawRoot)

        // 1. Extract Town Hall, Builder Hall, Total Builders, Helpers, and Boosts
        val detectedTh = detectTownHallLevel(root)
        val detectedBh = detectBuilderHallLevel(root)
        val detectedBuilders = detectTotalBuilders(root)
        val apprenticeLevel = detectApprenticeLevel(root)
        val activeBoostsText = detectActiveBoostsText(root)

        val playerTag = root.optString(
            "playerTag",
            root.optString("tag", rawRoot.optString("tag", "#QUJGYLGOP"))
        ).ifBlank { "#QUJGYLGOP" }

        val explicitName = root.optString(
            "playerName",
            root.optString("name", rawRoot.optString("name", ""))
        ).trim()

        val playerName = if (explicitName.isNotEmpty()) {
            explicitName
        } else {
            playerTag
        }

        val townHallLevel = root.optInt("townHallLevel", root.optInt("townHall", detectedTh ?: 18))
        val builderHallLevel = root.optInt("builderHallLevel", root.optInt("builderHall", detectedBh ?: 10))
        val totalBuilders = root.optInt("totalBuilders", root.optInt("builders", detectedBuilders ?: 6)).coerceIn(1, 10)

        val upgrades = mutableListOf<ClashUpgrade>()
        val villageItems = mutableListOf<ClashVillageItem>()

        // 2. Check explicit upgrade arrays first ("activeUpgrades", "upgrades", "timers", "queue")
        val explicitUpgradeKeys = listOf("activeUpgrades", "upgrades", "timers", "queue")
        for (key in explicitUpgradeKeys) {
            val arr = root.optJSONArray(key)
            if (arr != null && arr.length() > 0) {
                parseMixedArray(
                    array = arr,
                    sectionName = key,
                    now = now,
                    treatAllAsUpgrades = true,
                    apprenticeLevel = apprenticeLevel,
                    upgradesOut = upgrades,
                    villageOut = villageItems
                )
            }
        }

        // 3. Check explicit "villageItems" array if re-importing our serialized JSON
        val savedVillageArr = root.optJSONArray("villageItems")
        if (savedVillageArr != null && savedVillageArr.length() > 0) {
            for (i in 0 until savedVillageArr.length()) {
                val obj = savedVillageArr.optJSONObject(i) ?: continue
                villageItems.add(parseSavedVillageItem(obj))
            }
        }

        // 4. Scan all Clash of Clans Village Export & Supercell API sections
        val visitedKeys = mutableSetOf<String>().apply {
            addAll(explicitUpgradeKeys)
            add("villageItems")
        }

        for (sectionKey in VILLAGE_SECTION_KEYS) {
            val arr = root.optJSONArray(sectionKey) ?: continue
            visitedKeys.add(sectionKey)
            parseMixedArray(
                array = arr,
                sectionName = sectionKey,
                now = now,
                treatAllAsUpgrades = false,
                apprenticeLevel = apprenticeLevel,
                upgradesOut = upgrades,
                villageOut = villageItems
            )
        }

        // 5. Scan any remaining JSONArrays in root
        val keysIterator = root.keys()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            if (key in visitedKeys || key.lowercase() in IGNORED_VILLAGE_SECTIONS) continue
            val value = root.opt(key)
            if (value is JSONArray && value.length() > 0) {
                parseMixedArray(
                    array = value,
                    sectionName = key,
                    now = now,
                    treatAllAsUpgrades = false,
                    apprenticeLevel = apprenticeLevel,
                    upgradesOut = upgrades,
                    villageOut = villageItems
                )
            } else if (value is JSONObject) {
                val subKeys = value.keys()
                while (subKeys.hasNext()) {
                    val subKey = subKeys.next()
                    if (subKey.lowercase() in IGNORED_VILLAGE_SECTIONS) continue
                    val subArr = value.optJSONArray(subKey) ?: continue
                    parseMixedArray(
                        array = subArr,
                        sectionName = subKey,
                        now = now,
                        treatAllAsUpgrades = false,
                        apprenticeLevel = apprenticeLevel,
                        upgradesOut = upgrades,
                        villageOut = villageItems
                    )
                }
            }
        }

        return ClashAccount(
            playerTag = playerTag,
            playerName = playerName,
            townHallLevel = townHallLevel,
            builderHallLevel = builderHallLevel,
            totalBuilders = totalBuilders,
            activeBoostsText = activeBoostsText,
            activeUpgrades = assignBuilderSlots(upgrades),
            villageItems = sortVillageItems(villageItems),
            lastUpdatedEpochMs = now,
            rawJson = cleaned
        )
    }

    private fun extractJsonBody(raw: String): String {
        val noBom = raw.replace("\uFEFF", "").trim()
        val firstBrace = noBom.indexOf('{')
        val firstBracket = noBom.indexOf('[')

        val startIndex = when {
            firstBrace == -1 && firstBracket == -1 -> return noBom
            firstBrace == -1 -> firstBracket
            firstBracket == -1 -> firstBrace
            else -> minOf(firstBrace, firstBracket)
        }

        val isObject = noBom[startIndex] == '{'
        val endIndex = if (isObject) noBom.lastIndexOf('}') else noBom.lastIndexOf(']')
        return if (endIndex > startIndex) {
            noBom.substring(startIndex, endIndex + 1)
        } else {
            noBom.substring(startIndex)
        }
    }

    private fun unwrapRootObject(root: JSONObject): JSONObject {
        val wrapperKeys = listOf("village", "player", "account", "data", "export")
        for (key in wrapperKeys) {
            val child = root.optJSONObject(key)
            if (child != null && (child.has("buildings") || child.has("activeUpgrades") || child.has("heroes") || child.has("tag"))) {
                return child
            }
        }
        return root
    }

    private fun detectTownHallLevel(root: JSONObject): Int? {
        val buildings = root.optJSONArray("buildings") ?: root.optJSONArray("th_buildings") ?: return null
        for (i in 0 until buildings.length()) {
            val item = buildings.optJSONObject(i) ?: continue
            val dataId = item.optInt("data", item.optInt("dataId", -1))
            if (dataId == 1000001) {
                val lvl = item.optInt("lvl", item.optInt("level", 0))
                if (lvl > 0) return lvl
            }
        }
        return null
    }

    private fun detectBuilderHallLevel(root: JSONObject): Int? {
        val buildings2 = root.optJSONArray("buildings2") ?: root.optJSONArray("bh_buildings") ?: return null
        for (i in 0 until buildings2.length()) {
            val item = buildings2.optJSONObject(i) ?: continue
            val dataId = item.optInt("data", item.optInt("dataId", -1))
            if (dataId == 1000034) {
                val lvl = item.optInt("lvl", item.optInt("level", 0))
                if (lvl > 0) return lvl
            }
        }
        return null
    }

    private fun detectTotalBuilders(root: JSONObject): Int? {
        val buildings = root.optJSONArray("buildings") ?: root.optJSONArray("th_buildings") ?: return null
        var builderHuts = 0
        var hasSixthBuilder = false
        for (i in 0 until buildings.length()) {
            val item = buildings.optJSONObject(i) ?: continue
            val dataId = item.optInt("data", item.optInt("dataId", -1))
            if (dataId == 1000015) {
                builderHuts += item.optInt("cnt", 1).coerceAtLeast(1)
            } else if (dataId == 1000080 || dataId == 1000064) {
                hasSixthBuilder = true
            }
        }
        if (builderHuts == 0) return null
        return (builderHuts + if (hasSixthBuilder) 1 else 0).coerceIn(1, 6)
    }

    private fun detectApprenticeLevel(root: JSONObject): Int {
        val helpers = root.optJSONArray("helpers") ?: return 8
        for (i in 0 until helpers.length()) {
            val h = helpers.optJSONObject(i) ?: continue
            val dataId = h.optInt("data", -1)
            if (dataId == 93000000) {
                return h.optInt("lvl", 8).coerceAtLeast(1)
            }
        }
        return 8
    }

    private fun detectActiveBoostsText(root: JSONObject): String {
        val explicit = root.optString("activeBoostsText", "").trim()
        if (explicit.isNotEmpty()) return explicit

        val boostsObj = root.optJSONObject("boosts") ?: return "No active boosts"
        val builderBoost = boostsObj.optLong("builder_boost", boostsObj.optLong("builderPotion", 0L))
        val researchBoost = boostsObj.optLong("lab_boost", boostsObj.optLong("researchPotion", 0L))
        val clockTower = boostsObj.optLong("clock_tower", 0L)

        val activeList = mutableListOf<String>()
        if (builderBoost > 0) activeList.add("Builder Potion (${builderBoost / 60}m)")
        if (researchBoost > 0) activeList.add("Research Potion (${researchBoost / 60}m)")
        if (clockTower > 0) activeList.add("Clock Tower (${clockTower / 60}m)")

        return if (activeList.isEmpty()) "No active boosts" else activeList.joinToString(" · ")
    }

    private fun parseMixedArray(
        array: JSONArray,
        sectionName: String,
        now: Long,
        treatAllAsUpgrades: Boolean,
        apprenticeLevel: Int,
        upgradesOut: MutableList<ClashUpgrade>,
        villageOut: MutableList<ClashVillageItem>
    ) {
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue

            // Ignore helper cooldowns if scanning a generic array
            if (item.has("helper_cooldown") && !item.has("timer")) continue

            val timerSeconds = extractRemainingSeconds(item, now)
            val isExplicitUpgrade = treatAllAsUpgrades || timerSeconds > 0L

            val dataId = item.optInt("data", item.optInt("dataId", -1))
            val rawName = item.optString("name", item.optString("building", item.optString("title", ""))).trim()
            val meta = ClashDataIdRegistry.resolve(dataId, sectionName, rawName)

            // Determine category
            val explicitCategoryStr = item.optString("category", item.optString("type", "")).uppercase()
            val villageStr = item.optString("village", "").lowercase()
            val category = when {
                explicitCategoryStr.contains("LAB") || explicitCategoryStr.contains("RESEARCH") || explicitCategoryStr.contains("TROOP") || explicitCategoryStr.contains("SPELL") -> UpgradeCategory.LABORATORY
                explicitCategoryStr.contains("HERO") -> if (villageStr == "builderbase") UpgradeCategory.BUILDER_BASE else UpgradeCategory.HERO
                explicitCategoryStr.contains("PET") -> UpgradeCategory.PET
                explicitCategoryStr.contains("EQUIP") || explicitCategoryStr.contains("BLACKSMITH") -> UpgradeCategory.EQUIPMENT
                explicitCategoryStr.contains("BUILDER_BASE") || explicitCategoryStr.contains("BUILDERHALL") -> UpgradeCategory.BUILDER_BASE
                villageStr == "builderbase" -> UpgradeCategory.BUILDER_BASE
                else -> meta.category
            }

            // Determine resource
            val resourceStr = item.optString("resource", item.optString("costType", "")).uppercase()
            val resource = when {
                resourceStr.contains("DARK") -> UpgradeResource.DARK_ELIXIR
                resourceStr.contains("BUILDER_ELIXIR") -> UpgradeResource.BUILDER_ELIXIR
                resourceStr.contains("BUILDER_GOLD") -> UpgradeResource.BUILDER_GOLD
                resourceStr.contains("ELIXIR") -> UpgradeResource.ELIXIR
                resourceStr.contains("ORE") -> UpgradeResource.ORE
                resourceStr.contains("GEM") -> UpgradeResource.GEMS
                resourceStr.contains("GOLD") -> UpgradeResource.GOLD
                else -> meta.defaultResource
            }

            val currentLvl = item.optInt("lvl", item.optInt("level", 0))
            val count = item.optInt("cnt", item.optInt("count", 1)).coerceAtLeast(1)

            // Display name adjustments for special upgrades
            val displayName = when {
                item.has("supercharge") && timerSeconds > 0 -> "${meta.name} (Supercharge)"
                item.optBoolean("gear_up_upgrading", false) -> "${meta.name} (Gear Up)"
                dataId == 1000001 && item.has("weapon") && timerSeconds > 0 && currentLvl >= 12 -> {
                    val weaponLvl = item.optInt("weapon", 1)
                    if (weaponLvl < 5) "Town Hall Weapon" else meta.name
                }
                else -> meta.name
            }

            if (isExplicitUpgrade) {
                val targetLevel = when {
                    item.has("targetLevel") -> item.optInt("targetLevel", 1)
                    item.has("supercharge") && timerSeconds > 0 -> item.optInt("supercharge", 0) + 1
                    item.has("timer") -> currentLvl + 1
                    currentLvl > 0 -> currentLvl
                    else -> 1
                }

                val finalRemainingSeconds = if (timerSeconds > 0L) timerSeconds else 3600L * 12L
                val estimatedTotal = estimateTotalDurationSeconds(category, targetLevel, finalRemainingSeconds)
                val totalDurationSeconds = item.optLong("totalDurationSeconds", estimatedTotal)
                    .coerceAtLeast(finalRemainingSeconds)

                val startTimeMs = now - ((totalDurationSeconds - finalRemainingSeconds) * 1000L).coerceAtLeast(0L)
                val endTimeMs = now + (finalRemainingSeconds * 1000L)
                val cost = item.optLong("cost", 0L)
                val builderName = item.optString("builderName", item.optString("builder", ""))
                val id = item.optString("id", UUID.randomUUID().toString())

                val isGoblin = item.optBoolean("extra", item.optBoolean("isGoblin", false)) ||
                        builderName.contains("Goblin", ignoreCase = true)

                val explicitHelperTag = item.optString("helperTag", "").trim()
                val explicitHelperNote = item.optString("helperNote", "").trim()

                val hasHelperAssigned = item.has("helper_recurrent") || item.has("helper_timer") || item.optBoolean("helper_assigned", false)
                val helperRecurrent = item.optBoolean("helper_recurrent", false)

                val helperTag = when {
                    explicitHelperTag.isNotEmpty() -> explicitHelperTag
                    hasHelperAssigned -> "H$apprenticeLevel"
                    else -> ""
                }

                val helperNote = when {
                    explicitHelperNote.isNotEmpty() -> explicitHelperNote
                    hasHelperAssigned -> if (helperRecurrent) {
                        "Apprentice · repeat forecast on"
                    } else {
                        "Apprentice · repeat forecast off"
                    }
                    else -> ""
                }

                upgradesOut.add(
                    ClashUpgrade(
                        id = id,
                        dataId = if (dataId > 0) dataId else 0,
                        name = displayName,
                        targetLevel = targetLevel,
                        category = category,
                        builderName = if (isGoblin && builderName.isBlank()) "Goblin Builder" else builderName,
                        startTimeMs = startTimeMs,
                        endTimeMs = endTimeMs,
                        totalDurationSeconds = totalDurationSeconds,
                        cost = cost,
                        resource = resource,
                        isGoblin = isGoblin,
                        helperTag = helperTag,
                        helperNote = helperNote
                    )
                )
            }

            if (!treatAllAsUpgrades && dataId != 1000010 && dataId != 1000043) {
                villageOut.add(
                    ClashVillageItem(
                        dataId = if (dataId > 0) dataId else 0,
                        name = meta.name,
                        currentLevel = currentLvl.coerceAtLeast(1),
                        count = count,
                        category = category,
                        resource = resource,
                        isUpgrading = timerSeconds > 0L
                    )
                )
            }
        }
    }

    private fun extractRemainingSeconds(item: JSONObject, now: Long): Long {
        return when {
            item.has("timer") -> item.optLong("timer", 0L)
            item.has("remainingSeconds") -> item.optLong("remainingSeconds", 0L)
            item.has("durationSeconds") -> item.optLong("durationSeconds", 0L)
            item.has("time") -> item.optLong("time", 0L)
            item.has("upgrade_time") -> item.optLong("upgrade_time", 0L)
            item.has("remainingHours") -> (item.optDouble("remainingHours", 0.0) * 3600.0).toLong()
            item.has("remainingMinutes") -> (item.optDouble("remainingMinutes", 0.0) * 60.0).toLong()
            item.has("endTimeMs") -> ((item.optLong("endTimeMs") - now) / 1000L).coerceAtLeast(0L)
            item.has("endTime") -> {
                val end = item.optLong("endTime")
                if (end > 2_000_000_000L) ((end - now) / 1000L).coerceAtLeast(0L)
                else (end - (now / 1000L)).coerceAtLeast(0L)
            }
            else -> 0L
        }
    }

    private fun estimateTotalDurationSeconds(
        category: UpgradeCategory,
        targetLevel: Int,
        remainingSeconds: Long
    ): Long {
        val standardDays = when (category) {
            UpgradeCategory.HERO -> when {
                targetLevel >= 80 -> 8L
                targetLevel >= 50 -> 6L
                targetLevel >= 25 -> 4L
                else -> 2L
            }
            UpgradeCategory.PET -> when {
                targetLevel >= 8 -> 7L
                targetLevel >= 4 -> 5L
                else -> 3L
            }
            UpgradeCategory.LABORATORY -> when {
                targetLevel >= 10 -> 10L
                targetLevel >= 6 -> 6L
                else -> 3L
            }
            UpgradeCategory.BUILDING -> when {
                targetLevel >= 15 -> 12L
                targetLevel >= 10 -> 9L
                targetLevel >= 5 -> 5L
                else -> 6L
            }
            UpgradeCategory.BUILDER_BASE -> 5L
            UpgradeCategory.EQUIPMENT -> 1L
        }
        val standardSec = standardDays * 86400L
        return if (remainingSeconds > standardSec) {
            (remainingSeconds * 1.25).toLong()
        } else {
            standardSec.coerceAtLeast((remainingSeconds * 1.35).toLong())
        }
    }

    private fun assignBuilderSlots(upgrades: List<ClashUpgrade>): List<ClashUpgrade> {
        var homeBuilderIndex = 1
        var bbBuilderIndex = 1
        return upgrades.sortedBy { it.endTimeMs }.map { upg ->
            val assignedBuilder = when {
                upg.isGoblin -> "Goblin Builder"
                upg.builderName.isNotBlank() -> upg.builderName
                else -> when (upg.category) {
                    UpgradeCategory.BUILDING, UpgradeCategory.HERO -> "Builder #${homeBuilderIndex++}"
                    UpgradeCategory.LABORATORY -> "Lab Researcher"
                    UpgradeCategory.PET -> "Pet Trainer"
                    UpgradeCategory.EQUIPMENT -> "Blacksmith"
                    UpgradeCategory.BUILDER_BASE -> if (bbBuilderIndex++ == 1) "Master Builder" else "O.T.T.O Bot"
                }
            }
            upg.copy(builderName = assignedBuilder)
        }
    }

    private fun sortVillageItems(items: List<ClashVillageItem>): List<ClashVillageItem> {
        if (items.isEmpty()) return emptyList()
        val grouped = items.groupBy { "${it.name}_${it.currentLevel}_${it.category}_${it.isUpgrading}" }
            .map { (_, group) ->
                val first = group.first()
                first.copy(count = group.sumOf { it.count })
            }

        val categoryPriority = mapOf(
            UpgradeCategory.HERO to 0,
            UpgradeCategory.BUILDING to 1,
            UpgradeCategory.PET to 2,
            UpgradeCategory.LABORATORY to 3,
            UpgradeCategory.EQUIPMENT to 4,
            UpgradeCategory.BUILDER_BASE to 5
        )

        return grouped.sortedWith(
            compareBy<ClashVillageItem> { if (it.isUpgrading) 0 else 1 }
                .thenBy { categoryPriority[it.category] ?: 9 }
                .thenByDescending { it.currentLevel }
                .thenBy { it.name }
        )
    }

    private fun parseSavedVillageItem(obj: JSONObject): ClashVillageItem {
        val catStr = obj.optString("category", "BUILDING")
        val rawDataId = obj.optInt("dataId", 0)
        val rawName = obj.optString("name", "")
        val meta = ClashDataIdRegistry.resolve(rawDataId, catStr, rawName)
        val cat = runCatching { UpgradeCategory.valueOf(catStr) }.getOrDefault(meta.category)
        val resStr = obj.optString("resource", meta.defaultResource.name)
        val res = runCatching { UpgradeResource.valueOf(resStr) }.getOrDefault(meta.defaultResource)
        return ClashVillageItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            dataId = meta.id,
            name = meta.name,
            currentLevel = obj.optInt("currentLevel", 1),
            count = obj.optInt("count", 1),
            category = cat,
            resource = res,
            isUpgrading = obj.optBoolean("isUpgrading", false)
        )
    }

    /**
     * Serializes a ClashAccount to a clean, well-indented JSON string.
     */
    fun serialize(account: ClashAccount): String {
        val root = JSONObject()
        root.put("playerName", account.playerName)
        root.put("playerTag", account.playerTag)
        root.put("townHallLevel", account.townHallLevel)
        root.put("builderHallLevel", account.builderHallLevel)
        root.put("totalBuilders", account.totalBuilders)
        root.put("activeBoostsText", account.activeBoostsText)
        root.put("lastUpdated", account.lastUpdatedEpochMs)

        val upgradesArray = JSONArray()
        val now = System.currentTimeMillis()
        for (upg in account.activeUpgrades) {
            val item = JSONObject()
            item.put("id", upg.id)
            item.put("dataId", upg.dataId)
            item.put("name", upg.name)
            item.put("targetLevel", upg.targetLevel)
            item.put("category", upg.category.name)
            item.put("builderName", upg.builderName)
            item.put("remainingSeconds", upg.remainingSeconds(now))
            item.put("totalDurationSeconds", upg.totalDurationSeconds)
            item.put("cost", upg.cost)
            item.put("resource", upg.resource.name)
            item.put("startTimeMs", upg.startTimeMs)
            item.put("endTimeMs", upg.endTimeMs)
            item.put("isGoblin", upg.isGoblin)
            item.put("helperTag", upg.helperTag)
            item.put("helperNote", upg.helperNote)
            upgradesArray.put(item)
        }
        root.put("activeUpgrades", upgradesArray)

        if (account.villageItems.isNotEmpty()) {
            val villageArray = JSONArray()
            for (v in account.villageItems) {
                val obj = JSONObject()
                obj.put("id", v.id)
                obj.put("dataId", v.dataId)
                obj.put("name", v.name)
                obj.put("currentLevel", v.currentLevel)
                obj.put("count", v.count)
                obj.put("category", v.category.name)
                obj.put("resource", v.resource.name)
                obj.put("isUpgrading", v.isUpgrading)
                villageArray.put(obj)
            }
            root.put("villageItems", villageArray)
        }

        return root.toString(2)
    }

    /**
     * Sample Clash JSON Presets (including raw official In-Game Village Export format matching screenshot)
     */
    fun getPresetJson(presetType: String): String {
        return when (presetType) {
            "IN_GAME_EXPORT" -> """
{
  "tag": "#QUJGYLGOP",
  "timestamp": 1762164660,
  "helpers": [
    { "data": 93000000, "lvl": 8 },
    { "data": 93000001, "lvl": 6 }
  ],
  "buildings": [
    { "data": 1000001, "lvl": 18 },
    { "data": 1000015, "lvl": 6, "cnt": 5 },
    { "data": 1000080, "lvl": 1 },
    { "data": 1000024, "lvl": 12, "timer": 18060 },
    { "data": 1000079, "lvl": 2, "timer": 66840, "extra": true },
    { "data": 1000077, "lvl": 4, "timer": 104460 },
    { "data": 1000071, "lvl": 11, "timer": 365040, "helper_recurrent": false },
    { "data": 1000007, "lvl": 15, "timer": 365760 },
    { "data": 1000090, "lvl": 1, "timer": 417240 },
    { "data": 1000031, "lvl": 7, "cnt": 1 },
    { "data": 1000084, "lvl": 3, "cnt": 2 },
    { "data": 1000085, "lvl": 3, "cnt": 2 },
    { "data": 1000089, "lvl": 2, "cnt": 2 },
    { "data": 1000067, "lvl": 6, "cnt": 2 },
    { "data": 1000027, "lvl": 11, "cnt": 3 },
    { "data": 1000021, "lvl": 12, "cnt": 4 }
  ],
  "heroes": [
    { "data": 28000000, "lvl": 100 },
    { "data": 28000001, "lvl": 100 },
    { "data": 28000002, "lvl": 75 },
    { "data": 28000004, "lvl": 50 },
    { "data": 28000006, "lvl": 80 },
    { "data": 28000007, "lvl": 40 }
  ],
  "units": [
    { "data": 4000110, "lvl": 3 },
    { "data": 4000095, "lvl": 4 },
    { "data": 4000059, "lvl": 7 }
  ],
  "pets": [
    { "data": 73000010, "lvl": 10 },
    { "data": 73000009, "lvl": 10 },
    { "data": 73000016, "lvl": 10 }
  ],
  "equipment": [
    { "data": 90000018, "lvl": 27 },
    { "data": 90000019, "lvl": 27 },
    { "data": 90000034, "lvl": 27 },
    { "data": 90000042, "lvl": 18 }
  ],
  "buildings2": [
    { "data": 1000034, "lvl": 10 }
  ]
}
""".trimIndent()

            "TH16_MAX" -> """
{
  "playerName": "#QUJGYLGOP",
  "playerTag": "#QUJGYLGOP",
  "townHallLevel": 18,
  "builderHallLevel": 10,
  "totalBuilders": 6,
  "activeBoostsText": "No active boosts",
  "activeUpgrades": [
    {
      "dataId": 1000024,
      "name": "Dark Elixir Storage",
      "targetLevel": 13,
      "category": "BUILDING",
      "builderName": "Builder #1",
      "remainingSeconds": 18060,
      "totalDurationSeconds": 691200,
      "cost": 22000000,
      "resource": "ELIXIR",
      "isGoblin": false
    },
    {
      "dataId": 1000079,
      "name": "Multi-Gear Tower",
      "targetLevel": 3,
      "category": "BUILDING",
      "builderName": "Goblin Builder",
      "remainingSeconds": 66840,
      "totalDurationSeconds": 777600,
      "cost": 23500000,
      "resource": "GOLD",
      "isGoblin": true
    },
    {
      "dataId": 1000077,
      "name": "Monolith",
      "targetLevel": 5,
      "category": "BUILDING",
      "builderName": "Builder #2",
      "remainingSeconds": 104460,
      "totalDurationSeconds": 864000,
      "cost": 380000,
      "resource": "DARK_ELIXIR",
      "isGoblin": false
    },
    {
      "dataId": 1000071,
      "name": "Hero Hall",
      "targetLevel": 12,
      "category": "BUILDING",
      "builderName": "Builder #3",
      "remainingSeconds": 365040,
      "totalDurationSeconds": 950400,
      "cost": 24000000,
      "resource": "ELIXIR",
      "isGoblin": false,
      "helperTag": "H8",
      "helperNote": "Apprentice · repeat forecast off"
    },
    {
      "dataId": 1000007,
      "name": "Laboratory",
      "targetLevel": 16,
      "category": "BUILDING",
      "builderName": "Builder #4",
      "remainingSeconds": 365760,
      "totalDurationSeconds": 950400,
      "cost": 23000000,
      "resource": "ELIXIR",
      "isGoblin": false
    },
    {
      "dataId": 1000090,
      "name": "Super Wizard Tower",
      "targetLevel": 2,
      "category": "BUILDING",
      "builderName": "Builder #5",
      "remainingSeconds": 417240,
      "totalDurationSeconds": 1036800,
      "cost": 25000000,
      "resource": "GOLD",
      "isGoblin": false
    },
    {
      "dataId": 28000006,
      "name": "Minion Prince",
      "targetLevel": 81,
      "category": "HERO",
      "builderName": "Builder #6",
      "remainingSeconds": 518400,
      "totalDurationSeconds": 691200,
      "cost": 310000,
      "resource": "DARK_ELIXIR",
      "isGoblin": false
    },
    {
      "dataId": 4000110,
      "name": "Root Rider",
      "targetLevel": 3,
      "category": "LABORATORY",
      "builderName": "Lab Researcher",
      "remainingSeconds": 259200,
      "totalDurationSeconds": 691200,
      "cost": 21500000,
      "resource": "ELIXIR",
      "isGoblin": false
    },
    {
      "dataId": 73000010,
      "name": "Spirit Fox",
      "targetLevel": 10,
      "category": "PET",
      "builderName": "Pet Trainer",
      "remainingSeconds": 302400,
      "totalDurationSeconds": 604800,
      "cost": 290000,
      "resource": "DARK_ELIXIR",
      "isGoblin": false
    },
    {
      "dataId": 1000053,
      "name": "Mega Tesla",
      "targetLevel": 10,
      "category": "BUILDER_BASE",
      "builderName": "Master Builder",
      "remainingSeconds": 172800,
      "totalDurationSeconds": 518400,
      "cost": 5200000,
      "resource": "BUILDER_GOLD",
      "isGoblin": false
    }
  ],
  "buildings": [
    { "data": 1000001, "lvl": 18 },
    { "data": 1000031, "lvl": 7, "cnt": 1 },
    { "data": 1000084, "lvl": 3, "cnt": 2 },
    { "data": 1000085, "lvl": 3, "cnt": 2 },
    { "data": 1000089, "lvl": 2, "cnt": 2 },
    { "data": 1000067, "lvl": 6, "cnt": 2 },
    { "data": 1000027, "lvl": 11, "cnt": 3 },
    { "data": 1000021, "lvl": 12, "cnt": 4 }
  ],
  "heroes": [
    { "data": 28000000, "lvl": 100 },
    { "data": 28000001, "lvl": 100 },
    { "data": 28000002, "lvl": 75 },
    { "data": 28000004, "lvl": 50 },
    { "data": 28000006, "lvl": 80 },
    { "data": 28000007, "lvl": 40 }
  ],
  "pets": [
    { "data": 73000010, "lvl": 10 },
    { "data": 73000004, "lvl": 10 },
    { "data": 73000008, "lvl": 10 },
    { "data": 73000016, "lvl": 10 }
  ],
  "equipment": [
    { "data": 90000018, "lvl": 27 },
    { "data": 90000019, "lvl": 27 },
    { "data": 90000034, "lvl": 27 }
  ]
}
""".trimIndent()

            "TH15_RUSH" -> """
{
  "playerName": "#2P08VJQR",
  "playerTag": "#2P08VJQR",
  "townHallLevel": 16,
  "builderHallLevel": 10,
  "totalBuilders": 6,
  "activeBoostsText": "Builder Potion (45m)",
  "activeUpgrades": [
    {
      "dataId": 1000072,
      "name": "Spell Tower",
      "targetLevel": 3,
      "category": "BUILDING",
      "builderName": "Builder #1",
      "remainingSeconds": 7200,
      "totalDurationSeconds": 432000,
      "cost": 17000000,
      "resource": "GOLD"
    },
    {
      "dataId": 28000002,
      "name": "Grand Warden",
      "targetLevel": 64,
      "category": "HERO",
      "builderName": "Builder #2",
      "remainingSeconds": 61200,
      "totalDurationSeconds": 432000,
      "cost": 18000000,
      "resource": "ELIXIR",
      "helperTag": "H8",
      "helperNote": "Apprentice · repeat forecast on"
    },
    {
      "dataId": 4000059,
      "name": "Electro Dragon",
      "targetLevel": 7,
      "category": "LABORATORY",
      "builderName": "Lab Researcher",
      "remainingSeconds": 86400,
      "totalDurationSeconds": 518400,
      "cost": 18500000,
      "resource": "ELIXIR"
    },
    {
      "dataId": 73000007,
      "name": "Diggy",
      "targetLevel": 10,
      "category": "PET",
      "builderName": "Pet Trainer",
      "remainingSeconds": 144000,
      "totalDurationSeconds": 432000,
      "cost": 220000,
      "resource": "DARK_ELIXIR"
    }
  ]
}
""".trimIndent()

            else -> """
{
  "playerName": "#L820YYQ",
  "playerTag": "#L820YYQ",
  "townHallLevel": 15,
  "totalBuilders": 5,
  "activeBoostsText": "No active boosts",
  "activeUpgrades": [
    {
      "dataId": 28000000,
      "name": "Barbarian King",
      "targetLevel": 85,
      "category": "HERO",
      "builderName": "Builder #1",
      "remainingSeconds": 18000,
      "totalDurationSeconds": 345600,
      "cost": 280000,
      "resource": "DARK_ELIXIR"
    },
    {
      "dataId": 4000007,
      "name": "Healer",
      "targetLevel": 9,
      "category": "LABORATORY",
      "builderName": "Lab Researcher",
      "remainingSeconds": 93600,
      "totalDurationSeconds": 432000,
      "cost": 15000000,
      "resource": "ELIXIR"
    }
  ]
}
""".trimIndent()
        }
    }
}
