package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val singleIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, SingleEventWidgetProvider::class.java)
        )
        if (singleIds.isNotEmpty()) {
            SingleEventWidgetProvider.updateSingleWidgets(context, appWidgetManager, singleIds)
        }

        val timelineIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, TimelineWidgetProvider::class.java)
        )
        if (timelineIds.isNotEmpty()) {
            TimelineWidgetProvider.updateTimelineWidgets(context, appWidgetManager, timelineIds)
        }
    }
}
