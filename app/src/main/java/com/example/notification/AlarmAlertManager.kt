package com.example.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 逢时 - 独立强力闹钟唤醒控制器
 * 支持播放系统闹钟铃声、高优先级震动模式与亮屏唤醒，与常驻状态栏通知互不干扰。
 */
object AlarmAlertManager {
    private var currentRingtone: Ringtone? = null
    private var autoStopJob: Job? = null
    private var isAlarmPlaying: Boolean = false

    fun startAlarm(context: Context) {
        stopAlarm(context)
        isAlarmPlaying = true

        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "FengShi:AlarmWakeLock"
            )
            wakeLock?.acquire(10000L)
        } catch (e: Exception) {
            // Screen wake lock fallback
        }

        // 播放系统闹铃音频
        try {
            var alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            if (alertUri != null) {
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
                currentRingtone = ringtone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 触发强力震动
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 800, 400, 800, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // Ignore vibrator error
        }

        // 60秒无操作自动停止闹钟，防止持续耗电
        autoStopJob = CoroutineScope(Dispatchers.Main).launch {
            delay(60000L)
            stopAlarm(context)
        }
    }

    fun stopAlarm(context: Context) {
        autoStopJob?.cancel()
        autoStopJob = null
        try {
            currentRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        currentRingtone = null

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
        isAlarmPlaying = false
    }

    fun isPlaying(): Boolean = isAlarmPlaying
}
