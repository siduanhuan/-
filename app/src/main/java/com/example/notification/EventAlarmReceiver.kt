package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity

/**
 * 逢时 - 双轨独立提醒与闹钟广播接收器
 * 1. 常驻通知系统：若开启常驻，通知无法被划除，必须手动点击「✔ 确认已阅并消除」或在应用内确认。
 * 2. 独立闹钟系统：若开启响铃闹钟，准点播放系统闹钟声音并触发强震动，提供「🔕 停止闹钟」按钮。
 * 两套系统互不干扰，协同保障吉日重要时刻零遗漏。
 */
class EventAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_TRIGGER_REMINDER
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "吉日提醒"

        when (action) {
            ACTION_CONFIRM_NOTIFICATION -> {
                // 用户点击了“确认已阅并消除”
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(eventId.toInt())
                AlarmAlertManager.stopAlarm(context)
                Toast.makeText(context, "已确认「$title」吉日提醒", Toast.LENGTH_SHORT).show()
                return
            }
            ACTION_STOP_ALARM -> {
                // 用户点击了“停止闹钟响铃”
                AlarmAlertManager.stopAlarm(context)
                Toast.makeText(context, "已停止闹钟响铃", Toast.LENGTH_SHORT).show()
                return
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // 开机自启重新注册闹钟 (可在 ViewModel 或后台调度，此处做安全防守)
                return
            }
            else -> {
                // 触发提醒通知与闹钟
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "逢时佳期已至。"
                val isPersistent = intent.getBooleanExtra(EXTRA_IS_PERSISTENT, false)
                val isAlarm = intent.getBooleanExtra(EXTRA_IS_ALARM, false)

                createNotificationChannel(context)

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("EXTRA_TARGET_EVENT_ID", eventId)
                }

                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    eventId.toInt(),
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 确认并消除通知 Action
                val confirmIntent = Intent(context, EventAlarmReceiver::class.java).apply {
                    this.action = ACTION_CONFIRM_NOTIFICATION
                    putExtra(EXTRA_EVENT_ID, eventId)
                    putExtra(EXTRA_TITLE, title)
                }
                val confirmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (eventId + 100000).toInt(),
                    confirmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 停止闹钟 Action
                val stopAlarmIntent = Intent(context, EventAlarmReceiver::class.java).apply {
                    this.action = ACTION_STOP_ALARM
                    putExtra(EXTRA_EVENT_ID, eventId)
                }
                val stopAlarmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (eventId + 200000).toInt(),
                    stopAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val subText = buildString {
                    if (isPersistent) append("【未确认常驻通知】 ")
                    if (isAlarm) append("【闹钟唤醒】")
                }

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSubText(if (subText.isNotBlank()) subText else null)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(openPendingIntent)

                if (isPersistent) {
                    // 未确认的话就一直显示在通知中，必须手动确认才可以消除通知
                    builder.setOngoing(true)
                    builder.setAutoCancel(false)
                    builder.addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "✔ 确认已阅并消除",
                        confirmPendingIntent
                    )
                } else {
                    builder.setAutoCancel(true)
                }

                if (isAlarm) {
                    builder.addAction(
                        android.R.drawable.ic_lock_idle_alarm,
                        "🔕 停止闹钟",
                        stopAlarmPendingIntent
                    )
                    // 触发独立强力闹钟播放
                    AlarmAlertManager.startAlarm(context)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(eventId.toInt(), builder.build())
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "逢时 · 吉日纪念提醒"
            val descriptionText = "系统级精准节点提醒与独立强力闹钟，保障吉日零遗漏"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "fengshi_reminders"
        const val ACTION_TRIGGER_REMINDER = "com.example.action.TRIGGER_REMINDER"
        const val ACTION_CONFIRM_NOTIFICATION = "com.example.action.CONFIRM_NOTIFICATION"
        const val ACTION_STOP_ALARM = "com.example.action.STOP_ALARM"

        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_IS_PERSISTENT = "extra_is_persistent"
        const val EXTRA_IS_ALARM = "extra_is_alarm"
    }
}
