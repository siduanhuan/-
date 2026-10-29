package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.LunarCalendar
import com.example.data.EventEntity
import com.example.ui.theme.FengShiTheme
import com.example.i18n.LanguageManager
import com.example.i18n.i18n
import com.example.i18n.tr
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: EventEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FengShiTheme.colors.surface,
        dragHandle = null
    ) {
        EventDetailContent(
            event = event,
            onDismiss = onDismiss,
            onEdit = onEdit,
            onShare = onShare,
            onTogglePin = onTogglePin,
            onDelete = onDelete
        )
    }
}

@Composable
fun EventDetailContent(
    event: EventEntity,
    onDismiss: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val days = remember(event) { event.computeDays(today) }
    val statusDesc = remember(event) { event.getStatusDescription(today) }

    val origDate = remember(event) {
        try { LocalDate.parse(event.dateString) } catch (e: Exception) { today }
    }
    val lunar = remember(event) { LunarCalendar.solarToLunar(origDate) }
    val ageInfo = remember(event) {
        if (event.eventType == EventEntity.TYPE_BIRTHDAY) {
            LunarCalendar.calculateAgeInfo(event.dateString, event.isLunar, today)
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("event_detail_sheet")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(4.dp))
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = event.category,
                        fontSize = 11.sp,
                        color = FengShiTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
                if (event.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(4.dp))
                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = i18n("置顶", "置頂"),
                            fontSize = 11.sp,
                            color = FengShiTheme.colors.accentVermillion,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(32.dp).testTag("detail_pin_button")
                ) {
                    Icon(
                        imageVector = if (event.isPinned) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = i18n("置顶", "置頂"),
                        tint = if (event.isPinned) FengShiTheme.colors.accentVermillion else FengShiTheme.colors.textMuted
                    )
                }
                if (onDismiss != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = event.title,
            fontSize = 26.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            color = FengShiTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Status Description + Big Number Display
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = days.toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = FengShiTheme.colors.textPrimary,
                fontFamily = FontFamily.Serif,
                letterSpacing = (-3).sp,
                lineHeight = 64.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = if (event.eventType == EventEntity.TYPE_ANNIVERSARY) "DAYS PAST" else "DAYS LEFT",
                    fontSize = 11.sp,
                    color = FengShiTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
                Text(
                    text = statusDesc,
                    fontSize = 12.sp,
                    color = FengShiTheme.colors.textSecondary
                )
            }
        }

        // Traditional Calendar Details Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(8.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            DetailRow(label = i18n("历法模式", "曆法模式"), value = if (event.isLunar) i18n("农历（阴历）", "農曆（陰曆）") else i18n("公历（阳历）", "公曆（陽曆）"))
            DetailRow(label = i18n("设定日期", "設定日期"), value = event.getDisplayDateText())
            DetailRow(label = i18n("农历干支", "農曆干支"), value = "${lunar.cyclicalYear} (${lunar.zodiac}年) ${lunar.monthChinese}${lunar.dayChinese}")

            if (ageInfo != null) {
                DetailRow(
                    label = i18n("年岁计算", "年歲計算"),
                    value = "${i18n("现年", "現年")} ${ageInfo.currentAge} ${i18n("周岁，即将迎来", "週歲，即將迎來")} ${ageInfo.turningAge} ${i18n("岁", "歲")}"
                )
            }

            val reminderLabel = buildString {
                if (event.reminderMode == EventEntity.REMINDER_NONE) {
                    append(i18n("未开启提醒", "未開啟提醒"))
                } else {
                    val modeStr = when (event.reminderMode) {
                        EventEntity.REMINDER_DAY_OF -> i18n("当天", "當天")
                        EventEntity.REMINDER_BEFORE_1 -> i18n("提前 1 天", "提前 1 天")
                        EventEntity.REMINDER_BEFORE_2 -> i18n("提前 2 天", "提前 2 天")
                        EventEntity.REMINDER_BEFORE_3 -> i18n("提前 3 天", "提前 3 天")
                        EventEntity.REMINDER_BEFORE_7 -> i18n("提前 7 天", "提前 7 天")
                        EventEntity.REMINDER_BEFORE_15 -> i18n("提前 15 天", "提前 15 天")
                        EventEntity.REMINDER_BEFORE_30 -> i18n("提前 30 天", "提前 30 天")
                        else -> i18n("设定提醒", "設定提醒")
                    }
                    append("$modeStr ${event.reminderTime}")
                    if (event.isPersistentNotification) append(i18n(" (常驻通知)", " (常駐通知)"))
                    if (event.isAlarmEnabled) append(i18n(" [响铃闹钟]", " [響鈴鬧鐘]"))
                }
            }
            DetailRow(label = i18n("提醒设定", "提醒設定"), value = reminderLabel)

            if (event.notes.isNotBlank()) {
                DetailRow(label = i18n("备忘寄语", "備忘寄語"), value = event.notes)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1.4f).height(48.dp).testTag("detail_share_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                    contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = i18n("生成分享卡片", "生成分享卡片"),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(i18n("诗意卡片", "詩意卡片"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f).height(48.dp).testTag("detail_edit_button"),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FengShiTheme.colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = FengShiTheme.colors.surfaceVariant,
                    contentColor = FengShiTheme.colors.textPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = i18n("编辑", "編輯"),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(i18n("修改", "修改"), fontSize = 14.sp)
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .background(FengShiTheme.colors.surfaceVariant, CircleShape)
                    .border(1.dp, FengShiTheme.colors.border, CircleShape)
                    .testTag("detail_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = i18n("删除", "刪除"),
                    tint = FengShiTheme.colors.accentVermillion,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = FengShiTheme.colors.textMuted)
        Text(text = value, fontSize = 12.sp, color = FengShiTheme.colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}
