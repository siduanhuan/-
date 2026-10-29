package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.EventEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {

    fun scheduleEventAlarm(context: Context, event: EventEntity) {
        if (event.reminderMode == EventEntity.REMINDER_NONE) {
            cancelEventAlarm(context, event.id)
            return
        }

        val today = LocalDate.now()
        val targetSolarDate = event.getTargetSolarDate(today)

        val daysOffset = when (event.reminderMode) {
            EventEntity.REMINDER_BEFORE_1 -> 1L
            EventEntity.REMINDER_BEFORE_2 -> 2L
            EventEntity.REMINDER_BEFORE_3 -> 3L
            EventEntity.REMINDER_BEFORE_7 -> 7L
            EventEntity.REMINDER_BEFORE_15 -> 15L
            EventEntity.REMINDER_BEFORE_30 -> 30L
            else -> 0L
        }

        val triggerDate = targetSolarDate.minusDays(daysOffset)
        // 解析用户自定义提醒时分 (如 "09:00", "08:30", "20:00")
        val timeParts = event.reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        val triggerDateTime = triggerDate.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59), 0)
        val now = LocalDateTime.now()

        if (triggerDateTime.isBefore(now)) {
            // 如果计算出来的提醒时间已经过去了，无需设定
            return
        }

        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, EventAlarmReceiver::class.java).apply {
            action = EventAlarmReceiver.ACTION_TRIGGER_REMINDER
            putExtra(EventAlarmReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(EventAlarmReceiver.EXTRA_TITLE, "逢时 · ${event.title}")
            val message = when (event.reminderMode) {
                EventEntity.REMINDER_DAY_OF -> "${event.title} 正是今日！"
                EventEntity.REMINDER_BEFORE_1 -> "明日即是 ${event.title}，请留心备办。"
                EventEntity.REMINDER_BEFORE_2 -> "距 ${event.title} 还有 2 天。"
                EventEntity.REMINDER_BEFORE_3 -> "距 ${event.title} 还有 3 天。"
                EventEntity.REMINDER_BEFORE_7 -> "距 ${event.title} 还有 7 天。"
                EventEntity.REMINDER_BEFORE_15 -> "距 ${event.title} 还有 15 天。"
                EventEntity.REMINDER_BEFORE_30 -> "距 ${event.title} 还有 1 个月。"
                else -> "${event.title} 佳期临近。"
            }
            putExtra(EventAlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(EventAlarmReceiver.EXTRA_IS_PERSISTENT, event.isPersistentNotification)
            putExtra(EventAlarmReceiver.EXTRA_IS_ALARM, event.isAlarmEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for permissions if any
        }
    }

    fun cancelEventAlarm(context: Context, eventId: Long) {
        val intent = Intent(context, EventAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
