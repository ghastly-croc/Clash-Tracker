package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.ClashDataIdRegistry
import com.example.data.ClashRepository
import com.example.model.ClashAccount
import com.example.model.ClashUpgrade
import com.example.model.ClashVillageItem
import com.example.model.UpgradeCategory
import com.example.model.WidgetDisplayPreferences

object ClashWidgetUtils {

    const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_WIDGET"

    data class ScrollableWidgetRow(
        val id: Long,
        val remoteViews: RemoteViews
    )

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Update multi widgets
        val multiComponentName = ComponentName(context, ClashMultiWidgetProvider::class.java)
        val multiIds = appWidgetManager.getAppWidgetIds(multiComponentName)
        for (id in multiIds) {
            updateMultiWidget(context, appWidgetManager, id)
        }
        if (multiIds.isNotEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            appWidgetManager.notifyAppWidgetViewDataChanged(multiIds, R.id.widget_list_view)
        }

        // Update single widgets
        val singleComponentName = ComponentName(context, ClashSingleWidgetProvider::class.java)
        val singleIds = appWidgetManager.getAppWidgetIds(singleComponentName)
        for (id in singleIds) {
            updateSingleWidget(context, appWidgetManager, id)
        }
    }

    fun updateMultiWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val repository = ClashRepository.getInstance(context)
        val account = repository.accountState.value
        val widgetPrefs = repository.widgetPrefsState.value
        val now = System.currentTimeMillis()

        val views = RemoteViews(context.packageName, R.layout.widget_clash_multi)

        // 1. Header: Player Tag / Name & Subtitle ("TH 18 · 6/6 BUILDERS + G")
        val headerTitle = if (account.playerTag.isNotBlank() && account.playerName.startsWith("Village ")) {
            account.playerTag
        } else {
            account.playerName.ifBlank { account.playerTag }
        }
        views.setTextViewText(R.id.widget_player_name, headerTitle)
        views.setTextViewText(R.id.widget_th_badge, account.headerSubtitleText)

        // 2. Boosts Banner ("No active boosts") - toggleable via WidgetDisplayPreferences
        if (widgetPrefs.showBoostsBanner) {
            views.setViewVisibility(R.id.widget_boosts_box, View.VISIBLE)
            views.setTextViewText(R.id.widget_next_banner, account.activeBoostsText)
        } else {
            views.setViewVisibility(R.id.widget_boosts_box, View.GONE)
        }

        // 3. Footer ("UPDATED 10:11 am")
        views.setTextViewText(R.id.widget_updated_time, account.formattedUpdatedTime)

        // 4. Scrollable ListView Adapter containing filtered active upgrades & specific village sections
        val rows = buildScrollableWidgetRows(context, account, widgetPrefs, now)
        views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val builder = RemoteViews.RemoteCollectionItems.Builder()
                .setHasStableIds(true)
                .setViewTypeCount(3)
            for (row in rows) {
                builder.addItem(row.id, row.remoteViews)
            }
            views.setRemoteAdapter(R.id.widget_list_view, builder.build())
        } else {
            val serviceIntent = Intent(context, ClashWidgetRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            @Suppress("DEPRECATION")
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
        }

        // PendingIntent to launch app
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchPendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_header_row, launchPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_open_village, launchPendingIntent)
        views.setPendingIntentTemplate(R.id.widget_list_view, launchPendingIntent)

        // PendingIntent for refresh button
        val refreshIntent = Intent(context, ClashMultiWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun buildScrollableWidgetRows(
        context: Context,
        account: ClashAccount,
        widgetPrefs: WidgetDisplayPreferences = WidgetDisplayPreferences(),
        now: Long = System.currentTimeMillis()
    ): List<ScrollableWidgetRow> {
        val rows = mutableListOf<ScrollableWidgetRow>()
        var nextId = 1L

        val visibleUpgrades = account.activeUpgrades
            .filter { !it.isFinished(now) && widgetPrefs.isUpgradeVisible(it) }
            .sortedBy { it.endTimeMs }

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

        // Section 1: Builders (Home Village Buildings & Heroes)
        if ((widgetPrefs.showBuilders || widgetPrefs.showHeroes) &&
            (builderUpgrades.isNotEmpty() || visibleUpgrades.isEmpty())
        ) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(context, account.sectionHeaderText)
                )
            )
            for (upg in builderUpgrades) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createUpgradeItemRemoteView(context, upg, widgetPrefs, now)
                    )
                )
            }
        }

        // Section 2: Laboratory, Pets & Blacksmith
        if ((widgetPrefs.showLaboratory || widgetPrefs.showPetsAndEquipment) && labAndPetUpgrades.isNotEmpty()) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "LABORATORY & PETS  ${labAndPetUpgrades.size} ACTIVE"
                    )
                )
            )
            for (upg in labAndPetUpgrades) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createUpgradeItemRemoteView(context, upg, widgetPrefs, now)
                    )
                )
            }
        }

        // Section 3: Builder Base Active Upgrades
        if (widgetPrefs.showBuilderBase && builderBaseUpgrades.isNotEmpty()) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "BUILDER BASE  ${builderBaseUpgrades.size} ACTIVE"
                    )
                )
            )
            for (upg in builderBaseUpgrades) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createUpgradeItemRemoteView(context, upg, widgetPrefs, now)
                    )
                )
            }
        }

        // Section 4A: Specific Village Heroes (Barbarian King, Archer Queen, Grand Warden, Royal Champion, Minion Prince, Dragon Duke)
        if (villageHeroes.isNotEmpty()) {
            val heroNamesSummary = villageHeroes.joinToString(", ") { it.name.substringBefore(" (") }
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "HEROES  ($heroNamesSummary)"
                    )
                )
            )
            for (hero in villageHeroes) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createVillageItemRemoteView(context, hero)
                    )
                )
            }
        }

        // Section 4B: Specific Hero Pets & Blacksmith Equipment
        if (villagePetsAndEquip.isNotEmpty()) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "HERO PETS & EQUIPMENT  (${villagePetsAndEquip.size})"
                    )
                )
            )
            for (item in villagePetsAndEquip) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createVillageItemRemoteView(context, item)
                    )
                )
            }
        }

        // Section 4C: Home Village Defenses & Buildings
        if (villageBuildings.isNotEmpty()) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "HOME VILLAGE DEFENSES & STRUCTURES  (${villageBuildings.size})"
                    )
                )
            )
            for (item in villageBuildings) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createVillageItemRemoteView(context, item)
                    )
                )
            }
        }

        // Section 4D: Laboratory Troops & Spells
        if (villageLab.isNotEmpty()) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "LABORATORY TROOPS & SPELLS  (${villageLab.size})"
                    )
                )
            )
            for (item in villageLab) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createVillageItemRemoteView(context, item)
                    )
                )
            }
        }

        // Section 4E: Builder Base Structures & Units
        if (villageBuilderBase.isNotEmpty()) {
            rows.add(
                ScrollableWidgetRow(
                    id = nextId++,
                    remoteViews = createSectionHeaderRemoteView(
                        context,
                        "BUILDER BASE STRUCTURES  (${villageBuilderBase.size})"
                    )
                )
            )
            for (item in villageBuilderBase) {
                rows.add(
                    ScrollableWidgetRow(
                        id = nextId++,
                        remoteViews = createVillageItemRemoteView(context, item)
                    )
                )
            }
        }

        return rows
    }

    private fun createSectionHeaderRemoteView(context: Context, title: String): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item_section_header)
        rv.setTextViewText(R.id.widget_section_header_text, title)
        rv.setOnClickFillInIntent(R.id.widget_section_root, Intent())
        return rv
    }

    private fun createUpgradeItemRemoteView(
        context: Context,
        upg: ClashUpgrade,
        widgetPrefs: WidgetDisplayPreferences,
        now: Long
    ): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item_upgrade)
        rv.setImageViewResource(R.id.widget_item_icon, ClashDataIdRegistry.getIconDrawableRes(upg))
        rv.setTextViewText(R.id.widget_item_title, upg.displayTitle)
        rv.setTextViewText(R.id.widget_item_level, "→ ${upg.targetLevel}")
        rv.setTextViewText(R.id.widget_item_time, upg.formattedRemainingTime(now))

        if (widgetPrefs.showProgressBars) {
            rv.setViewVisibility(R.id.widget_item_progress, View.VISIBLE)
            val progressPercent = (upg.progressFraction(now) * 100).toInt().coerceIn(4, 100)
            rv.setProgressBar(R.id.widget_item_progress, 100, progressPercent, false)
        } else {
            rv.setViewVisibility(R.id.widget_item_progress, View.GONE)
        }

        if (widgetPrefs.showFinishTimestamp) {
            rv.setViewVisibility(R.id.widget_item_finish, View.VISIBLE)
            rv.setTextViewText(R.id.widget_item_finish, upg.formattedFinishTime())
        } else {
            rv.setViewVisibility(R.id.widget_item_finish, View.GONE)
        }

        if (widgetPrefs.showHelperNotes && upg.helperNote.isNotBlank()) {
            rv.setViewVisibility(R.id.widget_item_helper, View.VISIBLE)
            rv.setTextViewText(R.id.widget_item_helper, upg.helperNote)
        } else {
            rv.setViewVisibility(R.id.widget_item_helper, View.GONE)
        }

        rv.setOnClickFillInIntent(R.id.widget_row_root, Intent())
        return rv
    }

    private fun createVillageItemRemoteView(
        context: Context,
        item: ClashVillageItem
    ): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item_village)
        rv.setImageViewResource(
            R.id.widget_village_icon,
            ClashDataIdRegistry.getIconDrawableResForName(item.name, item.category)
        )
        val countSuffix = if (item.count > 1) " ×${item.count}" else ""
        val statusSuffix = if (item.isUpgrading) " · Upgrading" else ""
        val specificRole = ClashDataIdRegistry.getSpecificSubtitleForName(item.name, item.category)
        rv.setTextViewText(R.id.widget_village_title, "${item.name}$countSuffix")
        rv.setTextViewText(
            R.id.widget_village_subtitle,
            "$specificRole · Level ${item.currentLevel}$statusSuffix"
        )
        rv.setTextViewText(R.id.widget_village_level, "Lv ${item.currentLevel}")
        rv.setOnClickFillInIntent(R.id.widget_village_row_root, Intent())
        return rv
    }

    fun updateSingleWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val repository = ClashRepository.getInstance(context)
        val account = repository.accountState.value
        val widgetPrefs = repository.widgetPrefsState.value
        val now = System.currentTimeMillis()

        val views = RemoteViews(context.packageName, R.layout.widget_clash_single)

        val next = account.activeUpgrades
            .filter { !it.isFinished(now) && widgetPrefs.isUpgradeVisible(it) }
            .minByOrNull { it.endTimeMs }
            ?: account.nextFinishingUpgrade

        if (next != null) {
            views.setTextViewText(R.id.widget_single_timer, next.formattedRemainingTime(now))
            views.setTextViewText(R.id.widget_single_name, "${next.displayTitle} → ${next.targetLevel}")
            views.setTextViewText(R.id.widget_single_builder, next.formattedFinishTime())

            val progressPercent = (next.progressFraction(now) * 100).toInt().coerceIn(4, 100)
            views.setProgressBar(R.id.widget_single_progress, 100, progressPercent, false)

            val gems = next.gemCost(now)
            views.setTextViewText(R.id.widget_single_gem_cost, "💎 $gems Gems to finish")
            views.setImageViewResource(R.id.widget_single_icon, ClashDataIdRegistry.getIconDrawableRes(next))
        } else {
            views.setTextViewText(R.id.widget_single_timer, "FREE!")
            views.setTextViewText(R.id.widget_single_name, "All Upgrades Complete")
            views.setTextViewText(R.id.widget_single_builder, "Ready for builder")
            views.setProgressBar(R.id.widget_single_progress, 100, 100, false)
            views.setTextViewText(R.id.widget_single_gem_cost, "Tap to open tracker")
            views.setImageViewResource(R.id.widget_single_icon, R.drawable.ic_widget_th_iso)
        }

        // Tap to open app
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchPendingIntent = PendingIntent.getActivity(
            context,
            2,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_single_root, launchPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * Pins widget directly to home screen launcher on Android 8.0+ (API 26+)
     */
    fun requestPinWidget(context: Context, isMulti: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val providerClass = if (isMulti) {
                    ClashMultiWidgetProvider::class.java
                } else {
                    ClashSingleWidgetProvider::class.java
                }
                val provider = ComponentName(context, providerClass)
                return appWidgetManager.requestPinAppWidget(provider, null, null)
            }
        }
        return false
    }
}
