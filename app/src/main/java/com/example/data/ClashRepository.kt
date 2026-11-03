package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ClashAccount
import com.example.model.ClashUpgrade
import com.example.model.WidgetDisplayPreferences
import com.example.model.WidgetPreset
import com.example.widget.ClashWidgetUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ClashRepository private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("clash_tracker_prefs", Context.MODE_PRIVATE)

    private val _accountState = MutableStateFlow(loadInitialAccount())
    val accountState: StateFlow<ClashAccount> = _accountState.asStateFlow()

    private val _widgetPrefsState = MutableStateFlow(loadWidgetPrefs())
    val widgetPrefsState: StateFlow<WidgetDisplayPreferences> = _widgetPrefsState.asStateFlow()

    private fun loadInitialAccount(): ClashAccount {
        val savedJson = prefs.getString(KEY_ACCOUNT_JSON, null)
        return if (!savedJson.isNullOrEmpty()) {
            try {
                // Re-parsing through ClashJsonParser automatically upgrades any previously generic names
                // (e.g., "Hero #7" -> "Dragon Duke", "Hero Pet #16" -> "Sneezy") via ClashDataIdRegistry.resolve
                val parsed = ClashJsonParser.parse(savedJson)
                prefs.edit().putString(KEY_ACCOUNT_JSON, ClashJsonParser.serialize(parsed)).apply()
                parsed
            } catch (e: Exception) {
                loadDefaultPreset()
            }
        } else {
            loadDefaultPreset()
        }
    }

    private fun loadDefaultPreset(): ClashAccount {
        val defaultJson = ClashJsonParser.getPresetJson("TH16_MAX")
        return ClashJsonParser.parse(defaultJson)
    }

    private fun loadWidgetPrefs(): WidgetDisplayPreferences {
        val raw = prefs.getString(KEY_WIDGET_PREFS_JSON, null) ?: return WidgetDisplayPreferences()
        return try {
            val obj = JSONObject(raw)
            val hiddenSet = mutableSetOf<String>()
            val hiddenArr = obj.optJSONArray("hiddenUpgradeIds")
            if (hiddenArr != null) {
                for (i in 0 until hiddenArr.length()) {
                    val id = hiddenArr.optString(i)
                    if (id.isNotBlank()) hiddenSet.add(id)
                }
            }
            val hiddenNamesSet = mutableSetOf<String>()
            val hiddenNamesArr = obj.optJSONArray("hiddenItemNames")
            if (hiddenNamesArr != null) {
                for (i in 0 until hiddenNamesArr.length()) {
                    val name = hiddenNamesArr.optString(i)
                    if (name.isNotBlank()) hiddenNamesSet.add(name)
                }
            }
            WidgetDisplayPreferences(
                showBuilders = obj.optBoolean("showBuilders", true),
                showHeroes = obj.optBoolean("showHeroes", true),
                showLaboratory = obj.optBoolean("showLaboratory", true),
                showPetsAndEquipment = obj.optBoolean("showPetsAndEquipment", true),
                showBuilderBase = obj.optBoolean("showBuilderBase", true),
                showVillageInventory = obj.optBoolean("showVillageInventory", true),
                showBoostsBanner = obj.optBoolean("showBoostsBanner", true),
                showProgressBars = obj.optBoolean("showProgressBars", true),
                showFinishTimestamp = obj.optBoolean("showFinishTimestamp", true),
                showHelperNotes = obj.optBoolean("showHelperNotes", true),
                hiddenUpgradeIds = hiddenSet,
                hiddenItemNames = hiddenNamesSet
            )
        } catch (e: Exception) {
            WidgetDisplayPreferences()
        }
    }

    fun updateWidgetPrefs(newPrefs: WidgetDisplayPreferences) {
        _widgetPrefsState.value = newPrefs
        val obj = JSONObject().apply {
            put("showBuilders", newPrefs.showBuilders)
            put("showHeroes", newPrefs.showHeroes)
            put("showLaboratory", newPrefs.showLaboratory)
            put("showPetsAndEquipment", newPrefs.showPetsAndEquipment)
            put("showBuilderBase", newPrefs.showBuilderBase)
            put("showVillageInventory", newPrefs.showVillageInventory)
            put("showBoostsBanner", newPrefs.showBoostsBanner)
            put("showProgressBars", newPrefs.showProgressBars)
            put("showFinishTimestamp", newPrefs.showFinishTimestamp)
            put("showHelperNotes", newPrefs.showHelperNotes)
            val arr = JSONArray()
            newPrefs.hiddenUpgradeIds.forEach { arr.put(it) }
            put("hiddenUpgradeIds", arr)
            val namesArr = JSONArray()
            newPrefs.hiddenItemNames.forEach { namesArr.put(it) }
            put("hiddenItemNames", namesArr)
        }
        prefs.edit().putString(KEY_WIDGET_PREFS_JSON, obj.toString()).apply()
        ClashWidgetUtils.updateAllWidgets(context)
    }

    fun applyWidgetPreset(preset: WidgetPreset) {
        val updated = when (preset) {
            WidgetPreset.ALL_DATA -> WidgetDisplayPreferences(
                showBuilders = true,
                showHeroes = true,
                showLaboratory = true,
                showPetsAndEquipment = true,
                showBuilderBase = true,
                showVillageInventory = true,
                showBoostsBanner = true,
                showProgressBars = true,
                showFinishTimestamp = true,
                showHelperNotes = true,
                hiddenUpgradeIds = emptySet(),
                hiddenItemNames = emptySet()
            )
            WidgetPreset.TIMERS_ONLY -> _widgetPrefsState.value.copy(
                showBuilders = true,
                showHeroes = true,
                showLaboratory = true,
                showPetsAndEquipment = true,
                showBuilderBase = true,
                showVillageInventory = false,
                hiddenUpgradeIds = emptySet(),
                hiddenItemNames = emptySet()
            )
            WidgetPreset.BUILDERS_ONLY -> _widgetPrefsState.value.copy(
                showBuilders = true,
                showHeroes = true,
                showLaboratory = false,
                showPetsAndEquipment = false,
                showBuilderBase = false,
                showVillageInventory = false,
                hiddenUpgradeIds = emptySet(),
                hiddenItemNames = emptySet()
            )
            WidgetPreset.COMPACT -> _widgetPrefsState.value.copy(
                showBoostsBanner = false,
                showProgressBars = true,
                showFinishTimestamp = false,
                showHelperNotes = false,
                showVillageInventory = false
            )
        }
        updateWidgetPrefs(updated)
    }

    fun toggleUpgradeWidgetVisibility(upgradeId: String) {
        val current = _widgetPrefsState.value
        val updatedHidden = current.hiddenUpgradeIds.toMutableSet()
        if (upgradeId in updatedHidden) {
            updatedHidden.remove(upgradeId)
        } else {
            updatedHidden.add(upgradeId)
        }
        updateWidgetPrefs(current.copy(hiddenUpgradeIds = updatedHidden))
    }

    fun toggleItemNameWidgetVisibility(itemName: String) {
        val current = _widgetPrefsState.value
        val updatedHidden = current.hiddenItemNames.toMutableSet()
        if (itemName in updatedHidden) {
            updatedHidden.remove(itemName)
        } else {
            updatedHidden.add(itemName)
        }
        updateWidgetPrefs(current.copy(hiddenItemNames = updatedHidden))
    }

    fun showAllUpgradesInWidget() {
        val current = _widgetPrefsState.value
        updateWidgetPrefs(
            current.copy(
                hiddenUpgradeIds = emptySet(),
                hiddenItemNames = emptySet()
            )
        )
    }

    fun importJson(jsonString: String): Result<ClashAccount> {
        return try {
            val account = ClashJsonParser.parse(jsonString)
            saveAccount(account)
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadPreset(presetKey: String): ClashAccount {
        val json = ClashJsonParser.getPresetJson(presetKey)
        val account = ClashJsonParser.parse(json)
        saveAccount(account)
        return account
    }

    fun addUpgrade(upgrade: ClashUpgrade) {
        val current = _accountState.value
        val updatedList = current.activeUpgrades.toMutableList().apply {
            add(0, upgrade)
        }
        val updatedAccount = current.copy(
            activeUpgrades = updatedList,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        saveAccount(updatedAccount)
    }

    fun updateUpgrade(upgrade: ClashUpgrade) {
        val current = _accountState.value
        val updatedList = current.activeUpgrades.map {
            if (it.id == upgrade.id) upgrade else it
        }
        val updatedAccount = current.copy(
            activeUpgrades = updatedList,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        saveAccount(updatedAccount)
    }

    fun deleteUpgrade(upgradeId: String) {
        val current = _accountState.value
        val updatedList = current.activeUpgrades.filter { it.id != upgradeId }
        val updatedAccount = current.copy(
            activeUpgrades = updatedList,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        saveAccount(updatedAccount)
    }

    fun completeUpgrade(upgradeId: String) {
        val current = _accountState.value
        val updatedList = current.activeUpgrades.map {
            if (it.id == upgradeId) {
                it.copy(endTimeMs = System.currentTimeMillis())
            } else {
                it
            }
        }
        val updatedAccount = current.copy(
            activeUpgrades = updatedList,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        saveAccount(updatedAccount)
    }

    fun speedUpUpgrade(upgradeId: String, secondsReduced: Long) {
        val current = _accountState.value
        val updatedList = current.activeUpgrades.map {
            if (it.id == upgradeId) {
                val newEnd = (it.endTimeMs - (secondsReduced * 1000)).coerceAtLeast(System.currentTimeMillis())
                it.copy(endTimeMs = newEnd)
            } else {
                it
            }
        }
        val updatedAccount = current.copy(
            activeUpgrades = updatedList,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        saveAccount(updatedAccount)
    }

    fun updateAccountDetails(playerName: String, playerTag: String, townHallLevel: Int, totalBuilders: Int) {
        val current = _accountState.value
        val updated = current.copy(
            playerName = playerName,
            playerTag = playerTag,
            townHallLevel = townHallLevel,
            totalBuilders = totalBuilders,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        saveAccount(updated)
    }

    private fun saveAccount(account: ClashAccount) {
        _accountState.value = account
        val serialized = ClashJsonParser.serialize(account)
        prefs.edit().putString(KEY_ACCOUNT_JSON, serialized).apply()
        ClashWidgetUtils.updateAllWidgets(context)
    }

    companion object {
        private const val KEY_ACCOUNT_JSON = "saved_clash_account_json_v3"
        private const val KEY_WIDGET_PREFS_JSON = "widget_display_prefs_v1"

        @Volatile
        private var instance: ClashRepository? = null

        fun getInstance(context: Context): ClashRepository {
            return instance ?: synchronized(this) {
                instance ?: ClashRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
