package com.example.widget

import android.content.Context
import com.example.calendar.LunarCalendar
import com.example.data.EventEntity
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * 跨进程桌面小组件轻量高速缓存管理器 (Shared Preferences Snapshot)
 * 彻底杜绝桌面微件在独立进程访问 Room 数据库时的锁等待、空指针或跨进程崩溃
 */
object WidgetCacheManager {

    private const val PREF_NAME = "fengshi_widget_cache"
    private const val KEY_SINGLE_EVENT = "cache_single_event"
    private const val KEY_TIMELINE_EVENTS = "cache_timeline_events"
    private const val KEY_TIMELINE_DATE = "cache_timeline_date"

    data class SingleEventData(
        val id: Long,
        val title: String,
        val category: String,
        val days: Int,
        val statusDesc: String,
        val dateText: String,
        val isAnniversary: Boolean,
        val isPinned: Boolean
    )

    data class TimelineItemData(
        val id: Long,
        val title: String,
        val daysText: String,
        val isPinned: Boolean
    )

    data class TimelineData(
        val dateText: String,
        val items: List<TimelineItemData>
    )

    fun saveSnapshot(context: Context, events: List<EventEntity>) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val today = LocalDate.now()
            val lunarToday = LunarCalendar.solarToLunar(today)

            // 1. 计算单个核心吉日 (置顶优先，其次倒数最临近)
            val targetEvent = events.firstOrNull { it.isPinned }
                ?: events.minByOrNull {
                    val d = it.computeDays(today)
                    if (it.eventType == EventEntity.TYPE_ANNIVERSARY) 99999 else d
                } ?: events.firstOrNull()

            val editor = prefs.edit()

            if (targetEvent != null) {
                val days = targetEvent.computeDays(today)
                val json = JSONObject().apply {
                    put("id", targetEvent.id)
                    put("title", targetEvent.title)
                    put("category", targetEvent.category)
                    put("days", days)
                    put("statusDesc", targetEvent.getStatusDescription(today))
                    put("dateText", targetEvent.getDisplayDateText())
                    put("isAnniversary", targetEvent.eventType == EventEntity.TYPE_ANNIVERSARY)
                    put("isPinned", targetEvent.isPinned)
                }
                editor.putString(KEY_SINGLE_EVENT, json.toString())
            } else {
                editor.remove(KEY_SINGLE_EVENT)
            }

            // 2. 计算时间轴 4 件临近吉日
            val sorted = events.sortedWith(
                compareByDescending<EventEntity> { it.isPinned }
                    .thenBy { it.computeDays(today) }
            ).take(4)

            val timelineArray = JSONArray()
            for (item in sorted) {
                val days = item.computeDays(today)
                val dayText = if (item.eventType == EventEntity.TYPE_ANNIVERSARY) {
                    "已 $days 天"
                } else {
                    "余 $days 天"
                }
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("daysText", dayText)
                    put("isPinned", item.isPinned)
                }
                timelineArray.put(obj)
            }

            editor.putString(
                KEY_TIMELINE_DATE,
                "${lunarToday.cyclicalYear} ${lunarToday.monthChinese}${lunarToday.dayChinese}"
            )
            editor.putString(KEY_TIMELINE_EVENTS, timelineArray.toString())
            editor.apply()
        } catch (t: Throwable) {
            // 保护性静默，防止影响主流程
        }
    }

    fun getSingleEventSnapshot(context: Context): SingleEventData? {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_SINGLE_EVENT, null) ?: return null
            val obj = JSONObject(jsonStr)
            SingleEventData(
                id = obj.optLong("id", 0L),
                title = obj.optString("title", "逢时 · 记录吉日"),
                category = obj.optString("category", "吉日"),
                days = obj.optInt("days", 0),
                statusDesc = obj.optString("statusDesc", "临近吉日"),
                dateText = obj.optString("dateText", ""),
                isAnniversary = obj.optBoolean("isAnniversary", false),
                isPinned = obj.optBoolean("isPinned", false)
            )
        } catch (t: Throwable) {
            null
        }
    }

    fun getTimelineSnapshot(context: Context): TimelineData {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val dateText = prefs.getString(KEY_TIMELINE_DATE, "逢时 · 今日") ?: "逢时 · 今日"
            val arrayStr = prefs.getString(KEY_TIMELINE_EVENTS, null)
            val items = mutableListOf<TimelineItemData>()

            if (!arrayStr.isNullOrBlank()) {
                val array = JSONArray(arrayStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(
                        TimelineItemData(
                            id = obj.optLong("id", 0L),
                            title = obj.optString("title", ""),
                            daysText = obj.optString("daysText", "-- 天"),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
            }
            TimelineData(dateText, items)
        } catch (t: Throwable) {
            TimelineData("逢时 · 今日", emptyList())
        }
    }
}
