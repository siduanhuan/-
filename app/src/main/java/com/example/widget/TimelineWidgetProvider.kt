package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calendar.LunarCalendar
import com.example.data.AppDatabase
import com.example.data.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * 逢时 · 临近佳期时间轴桌面微件 (Timeline Widget Provider)
 * 支持多行吉日排版、跨进程轻量缓存降级与绝对崩溃免疫
 */
class TimelineWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 先快速应用轻量持久化缓存或静态兜底视图，杜绝桌面“无法加载微件”或白屏
                withContext(Dispatchers.Main) {
                    for (widgetId in appWidgetIds) {
                        val initialViews = buildCurrentOrFallbackViews(context, widgetId)
                        appWidgetManager.updateAppWidget(widgetId, initialViews)
                    }
                }
                // 2. 尝试从数据库读取最新数据并刷新持久化缓存
                updateTimelineWidgetsInternal(context, appWidgetManager, appWidgetIds)
            } catch (t: Throwable) {
                Log.e(TAG, "Exception during onUpdate in TimelineWidgetProvider", t)
                try {
                    for (widgetId in appWidgetIds) {
                        val fallback = buildFallbackViews(context, widgetId)
                        appWidgetManager.updateAppWidget(widgetId, fallback)
                    }
                } catch (ignored: Throwable) {}
            } finally {
                try {
                    pendingResult.finish()
                } catch (ignored: Throwable) {}
            }
        }
    }

    companion object {
        private const val TAG = "TimelineWidgetProvider"

        fun buildFallbackViews(context: Context, widgetId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_timeline_events)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_timeline_root, pendingIntent)
            views.setTextViewText(R.id.widget_timeline_date, "逢时 · 今日")
            views.setViewVisibility(R.id.timeline_item_1, View.VISIBLE)
            views.setTextViewText(R.id.item1_title, "逢时 · 记录吉日")
            views.setTextViewText(R.id.item1_days, "点击添录")
            views.setViewVisibility(R.id.timeline_item_2, View.GONE)
            views.setViewVisibility(R.id.timeline_item_3, View.GONE)
            views.setViewVisibility(R.id.timeline_item_4, View.GONE)
            return views
        }

        fun buildCurrentOrFallbackViews(context: Context, widgetId: Int): RemoteViews {
            val cached = WidgetCacheManager.getTimelineSnapshot(context)
            if (cached.items.isEmpty()) {
                return buildFallbackViews(context, widgetId)
            }

            val views = RemoteViews(context.packageName, R.layout.widget_timeline_events)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_timeline_root, pendingIntent)
            views.setTextViewText(R.id.widget_timeline_date, cached.dateText)

            val rowLayouts = intArrayOf(
                R.id.timeline_item_1,
                R.id.timeline_item_2,
                R.id.timeline_item_3,
                R.id.timeline_item_4
            )
            val rowTitles = intArrayOf(
                R.id.item1_title,
                R.id.item2_title,
                R.id.item3_title,
                R.id.item4_title
            )
            val rowDays = intArrayOf(
                R.id.item1_days,
                R.id.item2_days,
                R.id.item3_days,
                R.id.item4_days
            )

            for (i in 0 until 4) {
                if (i < cached.items.size) {
                    val item = cached.items[i]
                    views.setViewVisibility(rowLayouts[i], View.VISIBLE)
                    views.setTextViewText(rowTitles[i], (if (item.isPinned) "★ " else "") + item.title)
                    views.setTextViewText(rowDays[i], item.daysText)
                } else {
                    views.setViewVisibility(rowLayouts[i], View.GONE)
                }
            }
            return views
        }

        fun updateTimelineWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            CoroutineScope(Dispatchers.IO).launch {
                updateTimelineWidgetsInternal(context, appWidgetManager, appWidgetIds)
            }
        }

        private suspend fun updateTimelineWidgetsInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            try {
                val dao = AppDatabase.getDatabase(context).eventDao()
                val events = dao.getAllEventsList()
                // 同步刷新轻量跨进程缓存
                WidgetCacheManager.saveSnapshot(context, events)

                val today = LocalDate.now()
                val lunarToday = LunarCalendar.solarToLunar(today)

                val sorted = events.sortedWith(
                    compareByDescending<EventEntity> { it.isPinned }
                        .thenBy { it.computeDays(today) }
                ).take(4)

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_timeline_events)

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_timeline_root, pendingIntent)

                    views.setTextViewText(
                        R.id.widget_timeline_date,
                        "${lunarToday.cyclicalYear} ${lunarToday.monthChinese}${lunarToday.dayChinese}"
                    )

                    val rowLayouts = intArrayOf(
                        R.id.timeline_item_1,
                        R.id.timeline_item_2,
                        R.id.timeline_item_3,
                        R.id.timeline_item_4
                    )
                    val rowTitles = intArrayOf(
                        R.id.item1_title,
                        R.id.item2_title,
                        R.id.item3_title,
                        R.id.item4_title
                    )
                    val rowDays = intArrayOf(
                        R.id.item1_days,
                        R.id.item2_days,
                        R.id.item3_days,
                        R.id.item4_days
                    )

                    if (sorted.isEmpty()) {
                        views.setViewVisibility(rowLayouts[0], View.VISIBLE)
                        views.setTextViewText(rowTitles[0], "暂无吉日，点击添录")
                        views.setTextViewText(rowDays[0], "0 天")
                        for (i in 1 until 4) {
                            views.setViewVisibility(rowLayouts[i], View.GONE)
                        }
                    } else {
                        for (i in 0 until 4) {
                            if (i < sorted.size) {
                                val item = sorted[i]
                                val days = item.computeDays(today)
                                val dayText = if (item.eventType == EventEntity.TYPE_ANNIVERSARY) {
                                    "已 $days 天"
                                } else {
                                    "余 $days 天"
                                }
                                views.setViewVisibility(rowLayouts[i], View.VISIBLE)
                                views.setTextViewText(rowTitles[i], (if (item.isPinned) "★ " else "") + item.title)
                                views.setTextViewText(rowDays[i], dayText)
                            } else {
                                views.setViewVisibility(rowLayouts[i], View.GONE)
                            }
                        }
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to query database or update timeline widget, falling back to cache", t)
                for (widgetId in appWidgetIds) {
                    val fallback = buildCurrentOrFallbackViews(context, widgetId)
                    appWidgetManager.updateAppWidget(widgetId, fallback)
                }
            }
        }
    }
}
