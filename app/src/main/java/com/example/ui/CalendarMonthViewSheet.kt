package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.calendar.LunarCalendar
import com.example.data.EventEntity
import com.example.i18n.i18n
import com.example.ui.theme.EditorialVermillion
import com.example.ui.theme.FengShiTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 逢时 · 岁时吉日全屏/弹窗历表 (Calendar Month View)
 * 沉浸式公历/农历双轨月历视图，实时映射每个吉日纪念日，支持点击日期查看详情与快速添加。
 */
@Composable
fun CalendarMonthViewDialog(
    events: List<EventEntity>,
    onDismiss: () -> Unit,
    onEventClick: (EventEntity) -> Unit,
    onAddEventForDate: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    var currentYearMonth by remember { mutableStateOf(YearMonth.of(today.year, today.monthValue)) }
    var selectedDate by remember { mutableStateOf(today) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value // 1 (Mon) .. 7 (Sun)
    val startOffset = firstDayOfWeek - 1 // Monday-based offset (0..6)

    // 计算当月每一天的农历信息和对应事件列表
    val monthEventsMap = remember(events, currentYearMonth) {
        val map = mutableMapOf<LocalDate, MutableList<EventEntity>>()
        for (day in 1..daysInMonth) {
            val date = currentYearMonth.atDay(day)
            map[date] = mutableListOf()
        }
        for (event in events) {
            val target = event.getTargetSolarDate(today)
            if (target.year == currentYearMonth.year && target.monthValue == currentYearMonth.monthValue) {
                map[target]?.add(event)
            }
        }
        map
    }

    val cyclicalYearText = remember(currentYearMonth.year) {
        val cyclical = LunarCalendar.getCyclicalYear(currentYearMonth.year)
        val zodiac = LunarCalendar.getZodiac(currentYearMonth.year)
        "$cyclical ($zodiac)年"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(20.dp))
                .testTag("calendar_month_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = i18n("CALENDAR 岁时吉历", "CALENDAR 歲時吉曆"),
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = "${currentYearMonth.year}年 ${currentYearMonth.monthValue}月",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                        Text(
                            text = cyclicalYearText,
                            fontSize = 11.sp,
                            color = FengShiTheme.colors.accentGold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 回到今天
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FengShiTheme.colors.surfaceVariant)
                                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    currentYearMonth = YearMonth.of(today.year, today.monthValue)
                                    selectedDate = today
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(i18n("今日", "今日"), fontSize = 11.sp, color = FengShiTheme.colors.textPrimary)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp).testTag("close_calendar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = i18n("关闭", "關閉"),
                                tint = FengShiTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month Navigation Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentYearMonth = currentYearMonth.minusMonths(1)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = i18n("上一月", "上一月"),
                            tint = FengShiTheme.colors.textPrimary
                        )
                    }

                    Text(
                        text = "${currentYearMonth.year}年 ${currentYearMonth.monthValue}月",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FengShiTheme.colors.textPrimary
                    )

                    IconButton(
                        onClick = {
                            currentYearMonth = currentYearMonth.plusMonths(1)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = i18n("下一月", "下一月"),
                            tint = FengShiTheme.colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekday Headers (一 .. 日)
                val weekdays = listOf(
                    i18n("一", "一"),
                    i18n("二", "二"),
                    i18n("三", "三"),
                    i18n("四", "四"),
                    i18n("五", "五"),
                    i18n("六", "六"),
                    i18n("日", "日")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEachIndexed { idx, dayName ->
                        Text(
                            text = dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (idx >= 5) FengShiTheme.colors.accentGold else FengShiTheme.colors.textMuted,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Month Grid (Rows of 7 days)
                val totalCells = startOffset + daysInMonth
                val totalRows = (totalCells + 6) / 7

                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startOffset + 1

                            if (cellIndex < startOffset || dayNumber > daysInMonth) {
                                // Empty cell
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .padding(1.5.dp)
                                )
                            } else {
                                val cellDate = currentYearMonth.atDay(dayNumber)
                                val isCellToday = cellDate == today
                                val isCellSelected = cellDate == selectedDate
                                val dayEvents = monthEventsMap[cellDate] ?: emptyList()
                                val lunarInfo = remember(cellDate) {
                                    LunarCalendar.solarToLunar(cellDate)
                                }

                                val lunarLabel = if (lunarInfo.day == 1) {
                                    lunarInfo.monthChinese
                                } else {
                                    lunarInfo.dayChinese
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .padding(1.5.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isCellSelected -> FengShiTheme.colors.accentGold
                                                isCellToday -> FengShiTheme.colors.surfaceVariant
                                                else -> FengShiTheme.colors.surface
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                isCellSelected -> FengShiTheme.colors.accentGold
                                                isCellToday -> FengShiTheme.colors.accentGold.copy(alpha = 0.6f)
                                                else -> FengShiTheme.colors.border.copy(alpha = 0.5f)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            selectedDate = cellDate
                                        }
                                        .padding(horizontal = 1.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 11.sp,
                                            lineHeight = 12.sp,
                                            fontWeight = if (isCellSelected || isCellToday) FontWeight.Bold else FontWeight.Medium,
                                            color = when {
                                                isCellSelected -> if (FengShiTheme.colors.isDark) Color.Black else Color.White
                                                isCellToday -> FengShiTheme.colors.accentGold
                                                else -> FengShiTheme.colors.textPrimary
                                            }
                                        )
                                        Text(
                                            text = lunarLabel,
                                            fontSize = 8.sp,
                                            lineHeight = 9.sp,
                                            maxLines = 1,
                                            color = when {
                                                isCellSelected -> if (FengShiTheme.colors.isDark) Color.Black.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f)
                                                lunarInfo.day == 1 -> FengShiTheme.colors.accentGold
                                                else -> FengShiTheme.colors.textMuted
                                            }
                                        )

                                        // Event Indicator Dot
                                        if (dayEvents.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(3.5.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCellSelected) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.accentVermillion
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected Day Dossier
                val selectedDayEvents = monthEventsMap[selectedDate] ?: emptyList()
                val selectedLunar = remember(selectedDate) {
                    LunarCalendar.solarToLunar(selectedDate)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedDate.year}年${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FengShiTheme.colors.textPrimary
                                )
                                Text(
                                    text = "${i18n("农历", "農曆")} ${selectedLunar.cyclicalYear} (${selectedLunar.zodiac}) · ${selectedLunar.monthChinese}${selectedLunar.dayChinese}",
                                    fontSize = 11.sp,
                                    color = FengShiTheme.colors.accentGold
                                )
                            }

                            // 快速为此日期添加吉日按钮
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary)
                                    .clickable {
                                        onDismiss()
                                        onAddEventForDate(selectedDate)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("add_event_for_date_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = i18n("添加", "添加"),
                                        tint = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = i18n("添录此日", "添錄此日"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedDayEvents.isEmpty()) {
                            Text(
                                text = i18n("此日清平，暂无特定吉日或生日纪事。", "此日清平，暫無特定吉日或生日紀事。"),
                                fontSize = 12.sp,
                                color = FengShiTheme.colors.textMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedDayEvents.forEach { ev ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(FengShiTheme.colors.surface)
                                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                                            .clickable {
                                                onDismiss()
                                                onEventClick(ev)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(ev.categoryColorHex))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = ev.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = FengShiTheme.colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "[${ev.category}]",
                                                fontSize = 10.sp,
                                                color = FengShiTheme.colors.textMuted
                                            )
                                        }

                                        Text(
                                            text = ev.getStatusDescription(today),
                                            fontSize = 11.sp,
                                            color = FengShiTheme.colors.accentGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
