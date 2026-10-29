package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FengShiTheme
import com.example.i18n.LanguageManager
import com.example.i18n.i18n
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val poemSnippet: String
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = i18n("阴阳和合 · 双历精准", "陰陽和合 · 雙曆精准"),
            subtitle = i18n("百年离线农历与公历转换", "百年離線農曆與公曆轉換"),
            description = i18n("内置1900至2100年天文基准历表。自动计算闰月、天干地支、生肖与动态周岁，不差毫厘。", "內置1900至2100年天文基準曆表。自動計算閏月、天干地支、生肖與動態周歲，不差毫厘。"),
            icon = Icons.Default.CalendarMonth,
            poemSnippet = i18n("天道运转，四时更迭。逢其时也，如期而至。", "天道運轉，四時更迭。逢其時也，如期而至。")
        ),
        OnboardingPage(
            title = i18n("本地为本 · 纯净无痕", "本地為本 · 純淨無痕"),
            subtitle = i18n("零账号体系，数据归于己", "零賬號體系，數據歸於己"),
            description = i18n("无需注册登录，绝无云端上传与追踪广告。所有记录安居设备本地，能效极高，秒启轻盈。", "無需註冊登錄，絕無雲端上傳與追蹤廣告。所有記錄安居設備本地，能效極高，秒啟輕盈。"),
            icon = Icons.Default.Security,
            poemSnippet = i18n("心远地自偏，无事小神仙。记录于心，安之若素。", "心遠地自偏，無事小神仙。記錄於心，安之若素。")
        ),
        OnboardingPage(
            title = i18n("桌面小件 · 静水流深", "桌面小件 · 靜水流深"),
            subtitle = i18n("原生小组件与系统精准提醒", "原生小組件與系統精准提醒"),
            description = i18n("支持 1×1 焦点倒数与 2×4 临近时间流桌面小组件。依托系统底层闹钟，无常驻后台，静默守护。", "支持 1×1 焦點倒數與 2×4 臨近時間流桌面小組件。依託系統底層鬧鐘，無常駐後台，靜默守護。"),
            icon = Icons.Default.NotificationsActive,
            poemSnippet = i18n("吉日良辰，待君相逢。", "吉日良辰，待君相逢。")
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FengShiTheme.colors.background)
            .statusBarsPadding()
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        // Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("skip_onboarding_button")
            ) {
                Text(
                    text = i18n("跳过", "跳過"),
                    color = FengShiTheme.colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp)
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circular Zen Icon Container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FengShiTheme.colors.surfaceVariant)
                        .border(1.dp, FengShiTheme.colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = page.title,
                        tint = FengShiTheme.colors.textPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = page.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = FengShiTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.subtitle,
                    fontSize = 13.sp,
                    color = FengShiTheme.colors.textMuted,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = page.description,
                    fontSize = 14.sp,
                    color = FengShiTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Poetic footer snippet
                Box(
                    modifier = Modifier
                        .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(4.dp))
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "「 ${page.poemSnippet} 」",
                        fontSize = 12.sp,
                        color = FengShiTheme.colors.textMuted,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) FengShiTheme.colors.textPrimary else FengShiTheme.colors.border)
                            .size(if (isSelected) 8.dp else 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            if (pagerState.currentPage == pages.size - 1) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_app_button"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                        contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                    )
                ) {
                    Text(
                        text = i18n("开启逢时之旅", "開啟逢時之旅"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_page_button"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FengShiTheme.colors.surfaceVariant,
                        contentColor = FengShiTheme.colors.textPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = i18n("下一步", "下一步"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = i18n("下一步", "下一步"),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
