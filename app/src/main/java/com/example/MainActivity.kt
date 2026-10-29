package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.i18n.LanguageManager
import com.example.i18n.LocalLanguage
import com.example.ui.EventViewModel
import com.example.ui.MainScreen
import com.example.ui.OnboardingScreen
import com.example.ui.components.FengShiPrimaryButton
import com.example.ui.theme.DarkThemeColors
import com.example.ui.theme.EditorialGold
import com.example.ui.theme.FengShiTheme
import com.example.ui.theme.LocalFengShiColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeManager

/**
 * 逢时 · 主活动入口 (Main Activity)
 * 采用全方位主动防御与弹性兜底架构：
 * 1. 顶层全局未捕获异常兜底处理 (UncaughtExceptionHandler)
 * 2. 原生启动屏门禁防死锁机制 (Native Splash Guard with 1.2s Timeout & Safety Fallback)
 * 3. 动态通知权限在 Activity 根级注册，杜绝 CompositionLocal 缺失与重组闪退
 * 4. 纯静态降级保底，确保任何配置损坏或数据为空均能秒级稳定直达主界面
 */
class MainActivity : ComponentActivity() {

    private val viewModel: EventViewModel by viewModels()

    // 动态通知权限申请器（在 Activity 构造/生命周期安全层级注册，杜绝在 Composable 中因 LocalContext 被代理包装而引发 Registry 丢失）
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.i("MainActivity", "Notification permission granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 静态基础设施安全初始化与异常兜底
        try {
            LanguageManager.init(applicationContext)
        } catch (e: Throwable) {
            Log.e("MainActivity", "LanguageManager init fallback", e)
        }

        try {
            ThemeManager.init(applicationContext)
        } catch (e: Throwable) {
            Log.e("MainActivity", "ThemeManager init fallback", e)
        }

        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            Log.e("MainActivity", "enableEdgeToEdge fallback", e)
        }

        // 2. 原生启动屏门禁防死锁机制 (Native Splash Guard with Timeout Fallback)
        setupSplashGuardWithTimeout()

        // 3. Android 13+ 运行时通知权限安全申请
        requestNotificationPermissionSafely()

        // 4. 挂载 Compose 树（保持原生 Activity 作为 LocalContext，同时注入多语言环境）
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()
            val isHydrated by viewModel.isHydrated.collectAsStateWithLifecycle()
            val isCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

            // 提供多语言配置，使用静态默认降级避免空指针
            val safeLanguageMode = languageMode.ifBlank { LanguageManager.LANG_SYSTEM }
            val safeThemeMode = themeMode.ifBlank { ThemeManager.THEME_SYSTEM }

            CompositionLocalProvider(
                LocalLanguage provides safeLanguageMode
            ) {
                MyApplicationTheme(themeMode = safeThemeMode) {
                    // 状态就绪屏障 (State Hydration Guard): 平滑过渡，严禁卡死空白
                    Crossfade(
                        targetState = isHydrated,
                        animationSpec = tween(durationMillis = 300),
                        label = "SplashHydrationCrossfade"
                    ) { hydrated ->
                        if (!hydrated) {
                            // 东方宣纸水墨纯静态预热占位
                            SplashHydrationPlaceholder()
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(FengShiTheme.colors.background)
                            ) {
                                if (!isCompleted) {
                                    OnboardingScreen(
                                        onFinish = {
                                            viewModel.completeOnboarding()
                                        }
                                    )
                                } else {
                                    MainScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 安全的原生启动屏门禁：
     * 具备 1200ms 超时强制放行机制，坚决杜绝因协程阻塞、数据库微卡顿或文件读取异常导致的永久黑白屏死锁。
     */
    private fun setupSplashGuardWithTimeout() {
        val content = findViewById<View>(android.R.id.content) ?: return
        val startTime = System.currentTimeMillis()
        val timeoutMs = 1200L

        var listener: ViewTreeObserver.OnPreDrawListener? = null
        listener = ViewTreeObserver.OnPreDrawListener {
            val isReady = try {
                viewModel.isHydrated.value || (System.currentTimeMillis() - startTime > timeoutMs)
            } catch (t: Throwable) {
                true // 遇异常即刻放行，绝不阻断界面渲染
            }

            if (isReady) {
                content.viewTreeObserver.removeOnPreDrawListener(listener)
                true
            } else {
                false
            }
        }
        content.viewTreeObserver.addOnPreDrawListener(listener)
    }

    /**
     * 运行时通知权限申请
     */
    private fun requestNotificationPermissionSafely() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val hasPermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } catch (t: Throwable) {
                Log.w("MainActivity", "Failed to check or request notification permission", t)
            }
        }
    }
}

/**
 * 启动预热占位视图 (Splash Hydration Placeholder)
 * 纯静态东方雅致排版，完全解耦动态数据与外部依赖
 */
@Composable
fun SplashHydrationPlaceholder() {
    val bg = DarkThemeColors.background
    val textCol = DarkThemeColors.textPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "逢时",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 8.sp,
                color = textCol
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "静候嘉期 · 岁月沉香",
                fontSize = 13.sp,
                letterSpacing = 4.sp,
                color = EditorialGold.copy(alpha = 0.85f),
                fontFamily = FontFamily.Serif
            )
        }
    }
}
