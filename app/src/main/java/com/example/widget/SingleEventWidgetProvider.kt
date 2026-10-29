package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * 逢时 · 单个吉日桌面微件 (Single Event Widget Provider)
 * 支持跨进程数据安全访问、异步安全兜底与极端异常隔离
 */
class SingleEventWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // 使用 goAsync 保障跨进程异步广播生命周期安全
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
                // 2. 尝试从数据库拉取最新数据并刷新持久化缓存
                updateSingleWidgetsInternal(context, appWidgetManager, appWidgetIds)
            } catch (t: Throwable) {
                Log.e(TAG, "Exception during onUpdate in SingleEventWidgetProvider", t)
                // 异常兜底，再次确保每个微件都有合法的静态视图
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
        private const val TAG = "SingleEventWidget"

        /**
         * 静态兜底视图 (Fallback UI)
         */
        fun buildFallbackViews(context: Context, widgetId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_single_event)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_single_root, pendingIntent)
            views.setTextViewText(R.id.widget_category_badge, "逢时")
            views.setTextViewText(R.id.widget_days_number, "--")
            views.setTextViewText(R.id.widget_days_unit, "天")
            views.setTextViewText(R.id.widget_event_title, "逢时 · 记录吉日")
            views.setTextViewText(R.id.widget_event_desc, "点击进入添录生活纪念日")
            return views
        }

        /**
         * 优先使用轻量持久化缓存构建视图，无缓存则回退至纯静态兜底
         */
        fun buildCurrentOrFallbackViews(context: Context, widgetId: Int): RemoteViews {
            val cached = WidgetCacheManager.getSingleEventSnapshot(context) ?: return buildFallbackViews(context, widgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_single_event)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_TARGET_EVENT_ID", cached.id)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_single_root, pendingIntent)
            views.setTextViewText(R.id.widget_category_badge, cached.category)
            views.setTextViewText(R.id.widget_days_number, cached.days.toString())
            views.setTextViewText(
                R.id.widget_days_unit,
                if (cached.isAnniversary) "天 (已过)" else "天"
            )
            views.setTextViewText(
                R.id.widget_event_title,
                (if (cached.isPinned) "★ " else "") + cached.title
            )
            views.setTextViewText(R.id.widget_event_desc, "${cached.statusDesc} · ${cached.dateText}")
            return views
        }

        /**
         * 供外部调用的全量安全刷新
         */
        fun updateSingleWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            CoroutineScope(Dispatchers.IO).launch {
                updateSingleWidgetsInternal(context, appWidgetManager, appWidgetIds)
            }
        }

        private suspend fun updateSingleWidgetsInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            try {
                val db = AppDatabase.getDatabase(context)
                val events = db.eventDao().getAllEventsList()
                // 同步刷新轻量跨进程缓存
                WidgetCacheManager.saveSnapshot(context, events)

                val today = LocalDate.now()
                val targetEvent = events.firstOrNull { it.isPinned }
                    ?: events.minByOrNull {
                        val d = it.computeDays(today)
                        if (it.eventType == EventEntity.TYPE_ANNIVERSARY) 99999 else d
                    } ?: events.firstOrNull()

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_single_event)

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        if (targetEvent != null) {
                            putExtra("EXTRA_TARGET_EVENT_ID", targetEvent.id)
                        }
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_single_root, pendingIntent)

                    if (targetEvent != null) {
                        val days = targetEvent.computeDays(today)
                        val desc = targetEvent.getStatusDescription(today)

                        views.setTextViewText(R.id.widget_category_badge, targetEvent.category)
                        views.setTextViewText(R.id.widget_days_number, days.toString())
                        views.setTextViewText(
                            R.id.widget_days_unit,
                            if (targetEvent.eventType == EventEntity.TYPE_ANNIVERSARY) "天 (已过)" else "天"
                        )
                        views.setTextViewText(
                            R.id.widget_event_title,
                            (if (targetEvent.isPinned) "★ " else "") + targetEvent.title
                        )
                        views.setTextViewText(R.id.widget_event_desc, "$desc · ${targetEvent.getDisplayDateText()}")
                    } else {
                        views.setTextViewText(R.id.widget_category_badge, "吉日")
                        views.setTextViewText(R.id.widget_days_number, "0")
                        views.setTextViewText(R.id.widget_days_unit, "天")
                        views.setTextViewText(R.id.widget_event_title, "逢时 · 记录吉日")
                        views.setTextViewText(R.id.widget_event_desc, "点击进入添录生活纪念日")
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to query database or update widget, falling back to cache", t)
                for (widgetId in appWidgetIds) {
                    val fallback = buildCurrentOrFallbackViews(context, widgetId)
                    appWidgetManager.updateAppWidget(widgetId, fallback)
                }
            }
        }
    }
}
