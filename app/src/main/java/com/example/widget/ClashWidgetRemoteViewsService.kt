package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.data.ClashRepository

class ClashWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ClashWidgetRemoteViewsFactory(applicationContext)
    }
}

class ClashWidgetRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var rows: List<ClashWidgetUtils.ScrollableWidgetRow> = emptyList()

    override fun onCreate() {
        reloadData()
    }

    override fun onDataSetChanged() {
        reloadData()
    }

    private fun reloadData() {
        val repo = ClashRepository.getInstance(context)
        val account = repo.accountState.value
        val widgetPrefs = repo.widgetPrefsState.value
        val now = System.currentTimeMillis()
        rows = ClashWidgetUtils.buildScrollableWidgetRows(context, account, widgetPrefs, now)
    }

    override fun onDestroy() {
        rows = emptyList()
    }

    override fun getCount(): Int = rows.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in rows.indices) return null
        return rows[position].remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 3

    override fun getItemId(position: Int): Long {
        if (position !in rows.indices) return position.toLong()
        return rows[position].id
    }

    override fun hasStableIds(): Boolean = true
}
