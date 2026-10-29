package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FengShiTheme

/**
 * 逢时 · 全局按钮尺寸与触控反馈规范化组件系统 (Editorial Design System Buttons)
 * 
 * 1. Primary Button (大按钮): 高度 48dp, 圆角 12dp, 用于主确认/保存等核心操作
 * 2. Secondary Button (中按钮): 高度 38dp, 圆角 8dp, 用于副操作/换诗/选图等
 * 3. Tag / Option Capsule (小胶囊): 高度 30dp, 圆角 15dp 胶囊形, 用于切换选择/分类
 * 
 * 全局统一按下微缩放触控反馈 (scale 0.97f)，触控热区保证 >= 48dp
 */

@Composable
fun FengShiPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
    contentColor: Color = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
    disabledContainerColor: Color = FengShiTheme.colors.surfaceVariant,
    disabledContentColor: Color = FengShiTheme.colors.textMuted,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "PrimaryButtonScale"
    )

    val actualContainerColor = if (enabled) containerColor else disabledContainerColor
    val actualContentColor = if (enabled) contentColor else disabledContentColor

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .scale(scale)
            .clip(shape)
            .background(actualContainerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 自定义微缩放动画
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .height(48.dp)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides actualContentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

@Composable
fun FengShiSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = FengShiTheme.colors.surfaceVariant,
    contentColor: Color = FengShiTheme.colors.textPrimary,
    borderColor: Color = FengShiTheme.colors.border,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "SecondaryButtonScale"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp, minWidth = 44.dp)
            .scale(scale)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .height(38.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

@Composable
fun FengShiCapsuleButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedContainerColor: Color = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
    selectedContentColor: Color = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
    unselectedContainerColor: Color = Color.Transparent,
    unselectedContentColor: Color = FengShiTheme.colors.textMuted,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(15.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "CapsuleButtonScale"
    )

    val actualBg = if (selected) selectedContainerColor else unselectedContainerColor
    val actualText = if (selected) selectedContentColor else unselectedContentColor

    val borderModifier = if (borderColor != null) {
        Modifier.border(1.dp, borderColor, shape)
    } else Modifier

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp, minWidth = 44.dp) // 保证系统无障碍最小触控热区
            .scale(scale)
            .clip(shape)
            .background(actualBg)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .height(30.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides actualText) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

/**
 * 通用按下微缩放触控反馈修饰符 (active: scale(0.97))
 */
fun Modifier.fengShiClickScale(scaleFactor: Float = 0.97f): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleFactor else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "FengShiClickScale"
    )
    this.scale(scale)
}
