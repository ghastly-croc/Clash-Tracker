package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClashJsonParser
import com.example.data.ClashRepository
import com.example.model.ClashAccount
import com.example.model.ClashUpgrade
import com.example.model.ClashVillageItem
import com.example.model.UpgradeCategory
import com.example.model.UpgradeResource
import com.example.model.WidgetDisplayPreferences
import com.example.model.WidgetPreset
import com.example.widget.ClashWidgetUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SortOption(val title: String) {
    TIME_REMAINING("Finishing Soonest"),
    CATEGORY("Category"),
    NAME("Name (A-Z)"),
    LEVEL("Highest Level")
}

data class ClashUiState(
    val account: ClashAccount = ClashAccount(),
    val widgetPrefs: WidgetDisplayPreferences = WidgetDisplayPreferences(),
    val filteredUpgrades: List<ClashUpgrade> = emptyList(),
    val filteredVillageItems: List<ClashVillageItem> = emptyList(),
    val showVillageInventory: Boolean = false,
    val selectedCategory: UpgradeCategory? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.TIME_REMAINING,
    val currentTimeEpochMs: Long = System.currentTimeMillis(),
    val isAddEditDialogOpen: Boolean = false,
    val upgradeToEdit: ClashUpgrade? = null,
    val isImportJsonDialogOpen: Boolean = false,
    val isGemCalculatorDialogOpen: Boolean = false,
    val isWidgetPinDialogOpen: Boolean = false,
    val isWidgetDataDialogOpen: Boolean = false,
    val isEditPlayerDialogOpen: Boolean = false,
    val rawJsonExport: String = "",
    val snackBarMessage: String? = null
)

class ClashTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClashRepository.getInstance(application)

    private val _selectedCategory = MutableStateFlow<UpgradeCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.TIME_REMAINING)
    private val _showVillageInventory = MutableStateFlow(false)
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())

    private val _isAddEditDialogOpen = MutableStateFlow(false)
    private val _upgradeToEdit = MutableStateFlow<ClashUpgrade?>(null)
    private val _isImportJsonDialogOpen = MutableStateFlow(false)
    private val _isGemCalculatorDialogOpen = MutableStateFlow(false)
    private val _isWidgetPinDialogOpen = MutableStateFlow(false)
    private val _isWidgetDataDialogOpen = MutableStateFlow(false)
    private val _isEditPlayerDialogOpen = MutableStateFlow(false)
    private val _snackBarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ClashUiState> = combine(
        repository.accountState,
        repository.widgetPrefsState,
        _selectedCategory,
        _searchQuery,
        _sortOption,
        _showVillageInventory,
        _currentTime,
        _isAddEditDialogOpen,
        _upgradeToEdit,
        _isImportJsonDialogOpen,
        _isGemCalculatorDialogOpen,
        _isWidgetPinDialogOpen,
        _isWidgetDataDialogOpen,
        _isEditPlayerDialogOpen,
        _snackBarMessage
    ) { params ->
        val account = params[0] as ClashAccount
        val widgetPrefs = params[1] as WidgetDisplayPreferences
        val category = params[2] as? UpgradeCategory
        val query = params[3] as String
        val sort = params[4] as SortOption
        val showInventory = params[5] as Boolean
        val now = params[6] as Long
        val isAddEdit = params[7] as Boolean
        val upgToEdit = params[8] as? ClashUpgrade
        val isImport = params[9] as Boolean
        val isGem = params[10] as Boolean
        val isPin = params[11] as Boolean
        val isWidgetData = params[12] as Boolean
        val isPlayer = params[13] as Boolean
        val snack = params[14] as? String

        val filtered = account.activeUpgrades
            .filter { upg ->
                (category == null || upg.category == category) &&
                        (query.isBlank() || upg.name.contains(query, ignoreCase = true) || upg.builderName.contains(query, ignoreCase = true))
            }
            .let { list ->
                when (sort) {
                    SortOption.TIME_REMAINING -> list.sortedBy { it.endTimeMs }
                    SortOption.CATEGORY -> list.sortedBy { it.category.name }
                    SortOption.NAME -> list.sortedBy { it.name }
                    SortOption.LEVEL -> list.sortedByDescending { it.targetLevel }
                }
            }

        val filteredVillage = account.villageItems
            .filter { item ->
                (category == null || item.category == category) &&
                        (query.isBlank() || item.name.contains(query, ignoreCase = true))
            }
            .let { list ->
                when (sort) {
                    SortOption.TIME_REMAINING -> list
                    SortOption.CATEGORY -> list.sortedBy { it.category.name }
                    SortOption.NAME -> list.sortedBy { it.name }
                    SortOption.LEVEL -> list.sortedByDescending { it.currentLevel }
                }
            }

        ClashUiState(
            account = account,
            widgetPrefs = widgetPrefs,
            filteredUpgrades = filtered,
            filteredVillageItems = filteredVillage,
            showVillageInventory = showInventory,
            selectedCategory = category,
            searchQuery = query,
            sortOption = sort,
            currentTimeEpochMs = now,
            isAddEditDialogOpen = isAddEdit,
            upgradeToEdit = upgToEdit,
            isImportJsonDialogOpen = isImport,
            isGemCalculatorDialogOpen = isGem,
            isWidgetPinDialogOpen = isPin,
            isWidgetDataDialogOpen = isWidgetData,
            isEditPlayerDialogOpen = isPlayer,
            rawJsonExport = ClashJsonParser.serialize(account),
            snackBarMessage = snack
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClashUiState()
    )

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    fun setCategoryFilter(category: UpgradeCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun setShowVillageInventory(show: Boolean) {
        _showVillageInventory.value = show
    }

    fun updateWidgetPrefs(newPrefs: WidgetDisplayPreferences) {
        repository.updateWidgetPrefs(newPrefs)
    }

    fun applyWidgetPreset(preset: WidgetPreset) {
        repository.applyWidgetPreset(preset)
        _snackBarMessage.value = "Applied '${preset.label}' widget preset"
    }

    fun toggleUpgradeWidgetVisibility(upgradeId: String) {
        repository.toggleUpgradeWidgetVisibility(upgradeId)
    }

    fun toggleItemNameWidgetVisibility(itemName: String) {
        repository.toggleItemNameWidgetVisibility(itemName)
    }

    fun showAllUpgradesInWidget() {
        repository.showAllUpgradesInWidget()
    }

    fun openWidgetDataDialog() {
        _isWidgetDataDialogOpen.value = true
    }

    fun closeWidgetDataDialog() {
        _isWidgetDataDialogOpen.value = false
    }

    fun openAddUpgradeDialog() {
        _upgradeToEdit.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditUpgradeDialog(upgrade: ClashUpgrade) {
        _upgradeToEdit.value = upgrade
        _isAddEditDialogOpen.value = true
    }

    fun startUpgradeFromVillageItem(item: ClashVillageItem) {
        val now = System.currentTimeMillis()
        val defaultDurationSec = 86400L * 2L // 2 days default
        val draftUpgrade = ClashUpgrade(
            id = "", // Empty ID signals new upgrade pre-filled in dialog
            name = item.name,
            targetLevel = item.currentLevel + 1,
            category = item.category,
            builderName = when (item.category) {
                UpgradeCategory.LABORATORY -> "Lab Researcher"
                UpgradeCategory.PET -> "Pet Trainer"
                UpgradeCategory.EQUIPMENT -> "Blacksmith"
                UpgradeCategory.BUILDER_BASE -> "Master Builder"
                else -> "Builder #${(repository.accountState.value.busyBuildersCount + 1).coerceAtMost(6)}"
            },
            startTimeMs = now,
            endTimeMs = now + defaultDurationSec * 1000L,
            totalDurationSeconds = defaultDurationSec,
            cost = 0L,
            resource = item.resource
        )
        _upgradeToEdit.value = draftUpgrade
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _upgradeToEdit.value = null
    }

    fun openImportJsonDialog() {
        _isImportJsonDialogOpen.value = true
    }

    fun closeImportJsonDialog() {
        _isImportJsonDialogOpen.value = false
    }

    fun openGemCalculatorDialog() {
        _isGemCalculatorDialogOpen.value = true
    }

    fun closeGemCalculatorDialog() {
        _isGemCalculatorDialogOpen.value = false
    }

    fun openWidgetPinDialog() {
        _isWidgetPinDialogOpen.value = true
    }

    fun closeWidgetPinDialog() {
        _isWidgetPinDialogOpen.value = false
    }

    fun openEditPlayerDialog() {
        _isEditPlayerDialogOpen.value = true
    }

    fun closeEditPlayerDialog() {
        _isEditPlayerDialogOpen.value = false
    }

    fun saveUpgrade(
        id: String?,
        name: String,
        targetLevel: Int,
        category: UpgradeCategory,
        builderName: String,
        remainingSeconds: Long,
        totalDurationSeconds: Long,
        cost: Long,
        resource: UpgradeResource
    ) {
        val now = System.currentTimeMillis()
        val endTime = now + (remainingSeconds * 1000)
        val startTime = now - ((totalDurationSeconds - remainingSeconds) * 1000).coerceAtLeast(0)

        if (id.isNullOrBlank()) {
            // New upgrade
            val newUpg = ClashUpgrade(
                name = name,
                targetLevel = targetLevel,
                category = category,
                builderName = builderName,
                startTimeMs = startTime,
                endTimeMs = endTime,
                totalDurationSeconds = totalDurationSeconds,
                cost = cost,
                resource = resource
            )
            repository.addUpgrade(newUpg)
            _showVillageInventory.value = false
            _snackBarMessage.value = "Added $name (Lv $targetLevel)"
        } else {
            // Update existing
            val existing = _upgradeToEdit.value
            val updatedUpg = ClashUpgrade(
                id = id,
                name = name,
                targetLevel = targetLevel,
                category = category,
                builderName = builderName,
                startTimeMs = existing?.startTimeMs ?: startTime,
                endTimeMs = endTime,
                totalDurationSeconds = totalDurationSeconds,
                cost = cost,
                resource = resource
            )
            repository.updateUpgrade(updatedUpg)
            _snackBarMessage.value = "Updated $name (Lv $targetLevel)"
        }
        closeAddEditDialog()
    }

    fun deleteUpgrade(id: String) {
        repository.deleteUpgrade(id)
        _snackBarMessage.value = "Upgrade removed."
    }

    fun completeUpgrade(id: String) {
        repository.completeUpgrade(id)
        _snackBarMessage.value = "Upgrade marked completed!"
    }

    fun speedUpUpgrade(id: String, hours: Int) {
        val seconds = hours * 3600L
        repository.speedUpUpgrade(id, seconds)
        _snackBarMessage.value = "Sped up by $hours hours!"
    }

    fun importJson(jsonContent: String): Boolean {
        val result = repository.importJson(jsonContent)
        return if (result.isSuccess) {
            val acc = result.getOrNull()
            val activeCount = acc?.activeUpgrades?.size ?: 0
            val villageCount = acc?.villageItems?.size ?: 0
            _selectedCategory.value = null
            _searchQuery.value = ""

            if (activeCount > 0) {
                _showVillageInventory.value = false
                _snackBarMessage.value = "Imported ${acc?.playerName} (TH${acc?.townHallLevel}) • $activeCount active upgrades!"
            } else if (villageCount > 0) {
                _showVillageInventory.value = true
                _snackBarMessage.value = "Imported ${acc?.playerName} (TH${acc?.townHallLevel}) • $villageCount village items (0 active timers in JSON)."
            } else {
                _showVillageInventory.value = false
                _snackBarMessage.value = "Imported ${acc?.playerName} (TH${acc?.townHallLevel})."
            }
            closeImportJsonDialog()
            true
        } else {
            _snackBarMessage.value = "Error parsing JSON: ${result.exceptionOrNull()?.localizedMessage}"
            false
        }
    }

    fun loadPreset(presetKey: String) {
        val account = repository.loadPreset(presetKey)
        _selectedCategory.value = null
        _searchQuery.value = ""
        _showVillageInventory.value = false
        _snackBarMessage.value = "Loaded ${account.playerName} (${account.activeUpgrades.size} active upgrades)"
        closeImportJsonDialog()
    }

    fun updatePlayerInfo(name: String, tag: String, thLevel: Int, builders: Int) {
        repository.updateAccountDetails(name, tag, thLevel, builders)
        _snackBarMessage.value = "Account profile updated"
        closeEditPlayerDialog()
    }

    fun requestPinWidget(isMulti: Boolean) {
        val pinned = ClashWidgetUtils.requestPinWidget(getApplication(), isMulti)
        if (pinned) {
            _snackBarMessage.value = "Adding widget to your home screen..."
        } else {
            _snackBarMessage.value = "Long-press your home screen to add the Clash Tracker widget!"
        }
    }

    fun forceRefreshWidgets() {
        ClashWidgetUtils.updateAllWidgets(getApplication())
        _snackBarMessage.value = "Widgets refreshed with latest timers!"
    }

    fun clearSnackBar() {
        _snackBarMessage.value = null
    }
}
