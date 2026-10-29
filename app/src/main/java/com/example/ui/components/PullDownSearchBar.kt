package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.i18n
import com.example.ui.theme.FengShiTheme

/**
 * 逢时 · 下拉呼出搜索栏 (PullDownSearchBar)
 *
 * 东方极简美学规范：
 * 1. 默认状态隐藏，不侵占首屏视觉与空间。
 * 2. 容器高度 40dp，圆角 12dp，无厚重外阴影或强对比粗边框。
 * 3. 背景自适应当前主题：
 *    - 宣纸水墨 (浅色)：浅灰温润底色 (#EFECE6)，字色深灰，光标墨黑
 *    - 玄曜纯黑 (深色)：深灰微光底色 (#1C1C1E)，字色浅白，光标鎏金或纯白
 * 4. 内嵌极细放大镜图标与占位文本："搜索吉日、事件、生辰..."
 * 5. 右侧极简【取消】文本按钮，点击后平滑折叠并清空输入。
 */
@Composable
fun PullDownSearchBar(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    // 展开时轻微延迟自动聚焦输入法
    LaunchedEffect(visible) {
        if (visible) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 260),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 220),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
        modifier = modifier
    ) {
        val isDark = FengShiTheme.colors.isDark
        // 浅色模式：浅灰温润底色 #EFECE6；深色模式：深灰微光底色 #1C1C1E
        val containerBackground = if (isDark) Color(0xFF1C1C1E) else Color(0xFFEFECE6)
        val inputTextColor = if (isDark) Color(0xFFF4F4F5) else Color(0xFF27272A)
        val cursorColor = if (isDark) FengShiTheme.colors.accentGold else Color(0xFF18181B)
        val placeholderColor = if (isDark) Color(0xFF71717A) else Color(0xFF8E8E93)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .testTag("pull_down_search_bar_container"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 极简圆角搜索框主体容器
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerBackground)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = i18n("搜索", "搜尋"),
                    tint = placeholderColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = i18n("搜索吉日、事件、生辰...", "搜尋吉日、事件、生辰..."),
                            color = placeholderColor,
                            fontSize = 13.sp,
                            letterSpacing = 0.3.sp,
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("search_input_field"),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = inputTextColor,
                            fontSize = 13.sp,
                            letterSpacing = 0.3.sp,
                            fontFamily = FontFamily.Default
                        ),
                        cursorBrush = SolidColor(cursorColor)
                    )
                }

                // 清空输入小按钮
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onQueryChange("") }
                            .testTag("search_clear_input_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("清空", "清空"),
                            tint = placeholderColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 极简【取消】文本按钮
            Text(
                text = i18n("取消", "取消"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FengShiTheme.colors.textSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        onQueryChange("")
                        onDismiss()
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .testTag("cancel_search_button")
            )
        }
    }
}
