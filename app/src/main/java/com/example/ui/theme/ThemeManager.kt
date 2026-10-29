package com.example.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 逢时 · 东方美学色彩模型 (Theme Token Contract)
 */
data class FengShiColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentVermillion: Color,
    val accentGold: Color,
    val tagBg: Color,
    val tagText: Color,
    val isDark: Boolean
)

// 玄曜纯黑 (Dark / OLED Black) 调色板
val DarkThemeColors = FengShiColors(
    background = Color(0xFF000000),      // OLED 纯黑
    surface = Color(0xFF0E0E10),         // 卡片深灰微光
    surfaceElevated = Color(0xFF18181B), // 悬浮交互底座
    surfaceVariant = Color(0xFF141416),  // 输入框/次级容器
    border = Color(0x1FFFFFFF),          // 边框微光线
    divider = Color(0x0FFFFFFF),         // 发丝分割线
    textPrimary = Color(0xFFFFFFFF),     // 高对比纯白
    textSecondary = Color(0xFFA1A1AA),   // 浅灰次要文字
    textMuted = Color(0xFF71717A),       // 暗灰注释文字
    accentVermillion = Color(0xFFC34048),// 印章朱砂
    accentGold = Color(0xFFD4AF37),      // 雅致鎏金
    tagBg = Color(0xFF18181B),           // 标签默认底色
    tagText = Color(0xFFE4E4E7),         // 标签文本色
    isDark = true
)

// 宣纸水墨 (Light / Paper & Ink) 调色板
val LightThemeColors = FengShiColors(
    background = Color(0xFFF7F6F3),      // 柔和宣纸白/象牙底色
    surface = Color(0xFFFFFFFF),         // 纯白卡片
    surfaceElevated = Color(0xFFECEBE6), // 悬浮交互底座
    surfaceVariant = Color(0xFFEFEFEA),  // 输入框/次级容器
    border = Color(0x14000000),          // 柔和黑墨边框
    divider = Color(0x0A000000),         // 淡墨分割线
    textPrimary = Color(0xFF18181B),     // 墨黑文字
    textSecondary = Color(0xFF52525B),   // 次浓水墨文字
    textMuted = Color(0xFF8E8E93),       // 淡墨辅助文字
    accentVermillion = Color(0xFFC34048),// 印章朱砂
    accentGold = Color(0xFFB8860B),      // 暗金/赭石
    tagBg = Color(0xFFECEBE6),           // 标签浅宣纸底色
    tagText = Color(0xFF27272A),         // 标签墨黑文本
    isDark = false
)

val LocalFengShiColors = staticCompositionLocalOf { DarkThemeColors }

object FengShiTheme {
    val colors: FengShiColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFengShiColors.current
}

/**
 * 全局外观风格管理类 (ThemeManager)
 * 负责外观偏好持久化与全局即时响应
 */
object ThemeManager {
    const val THEME_SYSTEM = "SYSTEM"
    const val THEME_DARK = "DARK"
    const val THEME_LIGHT = "LIGHT"

    private const val PREFS_NAME = "fengshi_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"

    private val _themeMode = MutableStateFlow(THEME_SYSTEM)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
        _themeMode.value = saved
        initialized = true
    }

    fun setThemeMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    @Composable
    fun isDark(mode: String = _themeMode.value): Boolean {
        return when (mode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> isSystemInDarkTheme()
        }
    }
}
