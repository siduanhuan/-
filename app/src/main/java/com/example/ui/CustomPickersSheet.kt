package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.calendar.LunarCalendar
import com.example.i18n.i18n
import com.example.ui.theme.EditorialVermillion
import com.example.ui.theme.FengShiTheme
import com.example.ui.theme.ImperialGold
import java.time.LocalDate

/**
 * 历法日期选择对话框 (支持公历/农历一键切换，采用平滑列表滚轮与快捷定位)
 */
@Composable
fun DatePickerDialogSheet(
    initialDate: LocalDate,
    isLunar: Boolean,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.monthValue) }
    var selectedDay by remember { mutableStateOf(initialDate.dayOfMonth) }

    // 当选中的月份或模式变动时，纠偏天数
    val maxDays = remember(selectedYear, selectedMonth, isLunar) {
        if (isLunar) {
            LunarCalendar.getLunarMonthDays(selectedYear, selectedMonth, false)
        } else {
            try {
                val ym = java.time.YearMonth.of(selectedYear, selectedMonth)
                ym.lengthOfMonth()
            } catch (e: Exception) {
                30
            }
        }
    }

    LaunchedEffect(maxDays) {
        if (selectedDay > maxDays) {
            selectedDay = maxDays
        }
    }

    val years = remember { (1920..2080).toList() }
    val months = remember { (1..12).toList() }
    val days = remember(maxDays) { (1..maxDays).toList() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("date_picker_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isLunar) "CHINESE LUNAR CALENDAR" else "GREGORIAN CALENDAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = if (isLunar) i18n("选择农历日期", "選擇農曆日期") else i18n("选择公历日期", "選擇公曆日期"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.padding(0.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 选中实时中文雅称大预览
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FengShiTheme.colors.surfaceVariant)
                        .border(1.dp, FengShiTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isLunar) {
                                val cyclical = LunarCalendar.getCyclicalYear(selectedYear)
                                val zodiac = LunarCalendar.getZodiac(selectedYear)
                                val mStr = LunarCalendar.LUNAR_MONTHS.getOrElse(selectedMonth - 1) { "${selectedMonth}月" }
                                val dStr = LunarCalendar.LUNAR_DAYS.getOrElse(selectedDay - 1) { "${selectedDay}日" }
                                "$cyclical($zodiac)年 · $mStr$dStr"
                            } else {
                                "${selectedYear}年 ${selectedMonth}月 ${selectedDay}日"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.accentGold
                        )

                        if (!isLunar) {
                            val targetDate = try {
                                LocalDate.of(selectedYear, selectedMonth, selectedDay)
                            } catch (e: Exception) {
                                LocalDate.now()
                            }
                            val lunarInfo = LunarCalendar.solarToLunar(targetDate)
                            Text(
                                text = "对应农历：${lunarInfo.cyclicalYear} (${lunarInfo.zodiac}) · ${lunarInfo.monthChinese}${lunarInfo.dayChinese}",
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else {
                            val solar = LunarCalendar.lunarToSolar(selectedYear, selectedMonth, selectedDay, false)
                            Text(
                                text = "对应公历：${solar.year}年${solar.monthValue}月${solar.dayOfMonth}日",
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 三列滚轮选择区域 (年 / 月 / 日)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 年份滚轮
                    WheelPickerColumn(
                        items = years,
                        selectedItem = selectedYear,
                        onItemSelected = { selectedYear = it },
                        itemLabel = { year ->
                            if (isLunar) {
                                val cyclical = LunarCalendar.getCyclicalYear(year).replace("年", "")
                                val zodiac = LunarCalendar.getZodiac(year)
                                "$cyclical($zodiac)"
                            } else {
                                "${year}年"
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        testTagPrefix = "year"
                    )

                    // 月份滚轮
                    WheelPickerColumn(
                        items = months,
                        selectedItem = selectedMonth,
                        onItemSelected = { selectedMonth = it },
                        itemLabel = { month ->
                            if (isLunar) {
                                LunarCalendar.LUNAR_MONTHS.getOrElse(month - 1) { "${month}月" }
                            } else {
                                "${month}月"
                            }
                        },
                        modifier = Modifier.weight(1.0f),
                        testTagPrefix = "month"
                    )

                    // 日期滚轮
                    WheelPickerColumn(
                        items = days,
                        selectedItem = selectedDay,
                        onItemSelected = { selectedDay = it },
                        itemLabel = { day ->
                            if (isLunar) {
                                LunarCalendar.LUNAR_DAYS.getOrElse(day - 1) { "${day}日" }
                            } else {
                                "${day}日"
                            }
                        },
                        modifier = Modifier.weight(1.0f),
                        testTagPrefix = "day"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(i18n("取消", "取消"), color = FengShiTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onDateSelected(selectedYear, selectedMonth, selectedDay)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                            contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(i18n("确定", "確定"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 细致的时间选择器对话框 (安卓系统标准风格，提供滚轮选择与快速整点半点微调)
 */
@Composable
fun TimePickerDialogSheet(
    initialTime: String, // "HH:mm"
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = remember(initialTime) {
        val split = initialTime.split(":")
        val h = split.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
        val m = split.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        h to m
    }

    var selectedHour by remember { mutableStateOf(parts.first) }
    var selectedMinute by remember { mutableStateOf(parts.second) }

    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("time_picker_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TIME SETTINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = i18n("设置提醒时间", "設置提醒時間"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 当前时间大屏显示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FengShiTheme.colors.surfaceVariant)
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d : %02d", selectedHour, selectedMinute),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = FengShiTheme.colors.accentGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 快捷预设胶囊
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("08:00", "09:00", "12:00", "18:00", "20:00").forEach { preset ->
                        val pHour = preset.substringBefore(":").toInt()
                        val pMin = preset.substringAfter(":").toInt()
                        val isCurrent = selectedHour == pHour && selectedMinute == pMin
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) FengShiTheme.colors.accentGold else FengShiTheme.colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isCurrent) FengShiTheme.colors.accentGold else FengShiTheme.colors.border,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedHour = pHour
                                    selectedMinute = pMin
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) Color.Black else FengShiTheme.colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 时分滚轮双列选择
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WheelPickerColumn(
                        items = hours,
                        selectedItem = selectedHour,
                        onItemSelected = { selectedHour = it },
                        itemLabel = { String.format("%02d 时", it) },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "hour"
                    )

                    WheelPickerColumn(
                        items = minutes,
                        selectedItem = selectedMinute,
                        onItemSelected = { selectedMinute = it },
                        itemLabel = { String.format("%02d 分", it) },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "minute"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(i18n("取消", "取消"), color = FengShiTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val formatted = String.format("%02d:%02d", selectedHour, selectedMinute)
                            onTimeSelected(formatted)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                            contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(i18n("确定", "確定"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 竖向平滑滚轮挑选列组件
 */
@Composable
private fun <T> WheelPickerColumn(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "picker"
) {
    val listState = rememberLazyListState()
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    LaunchedEffect(selectedIndex) {
        val target = (selectedIndex - 2).coerceAtLeast(0)
        listState.scrollToItem(target)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FengShiTheme.colors.surfaceVariant)
            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items) { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) FengShiTheme.colors.surfaceElevated else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) FengShiTheme.colors.accentGold.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        fontSize = if (isSelected) 14.sp else 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) FengShiTheme.colors.accentGold else FengShiTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
