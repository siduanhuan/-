package com.example.ui

import com.example.ui.components.FengShiCapsuleButton
import com.example.ui.components.FengShiPrimaryButton
import com.example.ui.components.FengShiSecondaryButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.calendar.LunarCalendar
import com.example.data.EventEntity
import com.example.ui.theme.BambooGreen
import com.example.ui.theme.BorderDark
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.IndigoNight
import com.example.ui.theme.OledBlack
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VermillionRed
import com.example.i18n.LanguageManager
import com.example.i18n.i18n
import com.example.i18n.tr
import com.example.ui.theme.FengShiTheme
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditEventDialog(
    initialEvent: EventEntity? = null,
    presetDate: LocalDate? = null,
    categoriesList: List<String> = listOf("生日", "节日", "纪念日", "目标", "生活", "亲友"),
    onAddNewCategory: ((String) -> Unit)? = null,
    onOpenCategoryManagement: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit
) {
    val isEdit = initialEvent != null

    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var isLunar by remember { mutableStateOf(initialEvent?.isLunar ?: false) }

    // 日期处理
    val initialDate = remember(initialEvent, presetDate) {
        try {
            when {
                initialEvent != null -> LocalDate.parse(initialEvent.dateString)
                presetDate != null -> presetDate
                else -> LocalDate.now()
            }
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.monthValue) }
    var selectedDay by remember { mutableStateOf(initialDate.dayOfMonth) }

    var eventType by remember {
        mutableStateOf(initialEvent?.eventType ?: EventEntity.TYPE_COUNTDOWN)
    }
    var category by remember { mutableStateOf(initialEvent?.category ?: "节日") }
    var isPinned by remember { mutableStateOf(initialEvent?.isPinned ?: false) }
    var reminderMode by remember {
        mutableStateOf(initialEvent?.reminderMode ?: EventEntity.REMINDER_NONE)
    }
    var reminderTime by remember {
        mutableStateOf(initialEvent?.reminderTime ?: "09:00")
    }
    var isPersistentNotification by remember {
        mutableStateOf(initialEvent?.isPersistentNotification ?: false)
    }
    var isAlarmEnabled by remember {
        mutableStateOf(initialEvent?.isAlarmEnabled ?: false)
    }
    var notes by remember { mutableStateOf(initialEvent?.notes ?: "") }
    var colorHex by remember { mutableStateOf(initialEvent?.categoryColorHex ?: 0xFFD4AF37L) }

    var showMoreOptions by remember { mutableStateOf(isEdit) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAddingCustomCategory by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }
    var showDatePickerSheet by remember { mutableStateOf(false) }
    var showTimePickerSheet by remember { mutableStateOf(false) }

    // 计算当前所选日期的对应农历/公历预览
    val currentSelectedDate = remember(selectedYear, selectedMonth, selectedDay) {
        try {
            LocalDate.of(selectedYear, selectedMonth, selectedDay.coerceAtMost(28))
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    val previewLunar = remember(currentSelectedDate, isLunar) {
        if (isLunar) {
            "${selectedYear}年 农历${LunarCalendar.LUNAR_MONTHS.getOrElse(selectedMonth - 1) { "${selectedMonth}月" }}${LunarCalendar.LUNAR_DAYS.getOrElse(selectedDay - 1) { "${selectedDay}日" }}"
        } else {
            val l = LunarCalendar.solarToLunar(currentSelectedDate)
            "对应农历：${l.cyclicalYear} ${l.monthChinese}${l.dayChinese}"
        }
    }

    val orientalColors: List<Pair<Long, Pair<String, String>>> = listOf(
        0xFFD4AF37L to ("鎏金" to "鎏金"),
        0xFFC24D43L to ("绯红" to "緋紅"),
        0xFF7A8B7BL to ("竹青" to "竹青"),
        0xFF4A6B82L to ("黛蓝" to "黛藍"),
        0xFF9E8166L to ("沉香" to "沉香")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("add_edit_event_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) "EDIT OCCASION" else "NEW OCCASION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = if (isEdit) i18n("修改吉日", "修改吉日") else i18n("添录吉日", "添錄吉日"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 第一步：标题 (Step 1)
                Text(
                    text = i18n("TITLE 事件标题", "TITLE 事件標題"),
                    fontSize = 11.sp,
                    color = FengShiTheme.colors.textMuted,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = { Text(i18n("如：立春、母亲生辰、结婚十载...", "如：立春、母親生辰、結婚十載..."), color = FengShiTheme.colors.textMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FengShiTheme.colors.textPrimary,
                        unfocusedBorderColor = FengShiTheme.colors.border,
                        focusedTextColor = FengShiTheme.colors.textPrimary,
                        unfocusedTextColor = FengShiTheme.colors.textPrimary,
                        cursorColor = FengShiTheme.colors.textPrimary,
                        focusedContainerColor = FengShiTheme.colors.surfaceVariant,
                        unfocusedContainerColor = FengShiTheme.colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = FengShiTheme.colors.accentVermillion,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 第二步：日期与历法切换 (Step 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = i18n("CALENDAR 历法与日期", "CALENDAR 曆法與日期"),
                        fontSize = 11.sp,
                        color = FengShiTheme.colors.textMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // 公历 / 农历 Toggle
                    Row(
                        modifier = Modifier
                            .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(14.dp))
                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(14.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isLunar) FengShiTheme.colors.accentGold else Color.Transparent)
                                .clickable { isLunar = false }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("toggle_solar")
                        ) {
                            Text(
                                text = i18n("公历", "公曆"),
                                fontSize = 12.sp,
                                color = if (!isLunar) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLunar) FengShiTheme.colors.accentGold else Color.Transparent)
                                .clickable { isLunar = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("toggle_lunar")
                        ) {
                            Text(
                                text = i18n("农历", "農曆"),
                                fontSize = 12.sp,
                                color = if (isLunar) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val lunarYearCyclical = remember(selectedYear) {
                    LunarCalendar.getCyclicalYear(selectedYear)
                }
                val lunarZodiac = remember(selectedYear) {
                    LunarCalendar.getZodiac(selectedYear)
                }

                // 日期设定卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FengShiTheme.colors.surfaceVariant)
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(10.dp))
                        .clickable { showDatePickerSheet = true }
                        .padding(14.dp)
                        .testTag("date_picker_trigger_box")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isLunar) i18n("农历日期设定", "農曆日期設定") else i18n("公历日期设定", "公曆日期設定"),
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isLunar) {
                                    val cyclical = lunarYearCyclical.replace("年", "")
                                    val mStr = LunarCalendar.LUNAR_MONTHS.getOrElse(selectedMonth - 1) { "${selectedMonth}月" }
                                    val dStr = LunarCalendar.LUNAR_DAYS.getOrElse(selectedDay - 1) { "${selectedDay}日" }
                                    "$cyclical($lunarZodiac)年 · $mStr$dStr"
                                } else {
                                    "${selectedYear}年 ${selectedMonth}月 ${selectedDay}日"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = FengShiTheme.colors.accentGold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isLunar) {
                                    val solar = LunarCalendar.lunarToSolar(selectedYear, selectedMonth, selectedDay, false)
                                    "${i18n("对应公历：", "對應公曆：")}${solar.year}年${solar.monthValue}月${solar.dayOfMonth}日"
                                } else {
                                    previewLunar
                                },
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(FengShiTheme.colors.surface)
                                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = i18n("选择日期", "選擇日期"),
                                tint = FengShiTheme.colors.accentGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = i18n("修改", "修改"),
                                fontSize = 12.sp,
                                color = FengShiTheme.colors.textPrimary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 展开/折叠更多选项 (事件类型、提醒、置顶、配色)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showMoreOptions = !showMoreOptions }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showMoreOptions) i18n("收起附加设置", "收起附加設置") else i18n("展开更多设置 (类型、提醒、置顶、备注)", "展開更多設置 (類型、提醒、置頂、備註)"),
                        fontSize = 13.sp,
                        color = FengShiTheme.colors.accentGold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showMoreOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = i18n("切换", "切換"),
                        tint = FengShiTheme.colors.accentGold
                    )
                }

                AnimatedVisibility(visible = showMoreOptions) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        // 模式类型
                        Text(
                            text = i18n("模式类型", "模式類型"),
                            fontSize = 11.sp,
                            color = FengShiTheme.colors.textMuted,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val types = listOf(
                                EventEntity.TYPE_COUNTDOWN to (i18n("节日节气", "節日節氣")),
                                EventEntity.TYPE_BIRTHDAY to (i18n("生日寿辰", "生日壽辰")),
                                EventEntity.TYPE_ANNIVERSARY to (i18n("纪念日", "紀念日")),
                                EventEntity.TYPE_TARGET to (i18n("目标规划", "目標規劃"))
                            )
                            for ((tKey, tName) in types) {
                                val isSelected = eventType == tKey
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) (if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary)
                                            else FengShiTheme.colors.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) FengShiTheme.colors.border else FengShiTheme.colors.border.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { eventType = tKey }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = tName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        // 模式说明文案
                        val modeDescription = when (eventType) {
                            EventEntity.TYPE_COUNTDOWN -> i18n("倒数计算距离下一个传统节日/现代节日的剩余天数。", "倒數計算距離下一個傳統節日/現代節日的剩餘天數。")
                            EventEntity.TYPE_BIRTHDAY -> i18n("按生辰八字/出生日期计算，支持周岁、虚岁与下次生日倒计时。", "按生辰八字/出生日期計算，支持週歲、虛歲與下次生日倒計時。")
                            EventEntity.TYPE_ANNIVERSARY -> i18n("累计已发生天数（如相恋、开业等已过去多少天）。", "累計已發生天數（如相戀、開業等已過去多少天）。")
                            EventEntity.TYPE_TARGET -> i18n("针对考试、项目或长期目标的截止日期倒数与阶段规划。", "針對考試、項目或長期目標的截止日期倒數與階段規劃。")
                            else -> ""
                        }
                        if (modeDescription.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = modeDescription,
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textMuted,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 分类标签
                        val availableCategories = remember(categoriesList, category) {
                            if (category.isNotBlank() && !categoriesList.contains(category)) {
                                categoriesList + category
                            } else {
                                categoriesList
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = i18n("TAG 分类标签", "TAG 分類標籤"),
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textMuted,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (onOpenCategoryManagement != null) {
                                Text(
                                    text = i18n("管理分类 ⚙", "管理分類 ⚙"),
                                    fontSize = 11.sp,
                                    color = FengShiTheme.colors.accentGold,
                                    modifier = Modifier
                                        .clickable { onOpenCategoryManagement() }
                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (cat in availableCategories) {
                                val isSelected = category == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) (if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary)
                                            else FengShiTheme.colors.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) FengShiTheme.colors.border else FengShiTheme.colors.border.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { category = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textSecondary
                                    )
                                }
                            }

                            // 添新分类按钮
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isAddingCustomCategory) FengShiTheme.colors.border else FengShiTheme.colors.surfaceVariant)
                                    .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(4.dp))
                                    .clickable { isAddingCustomCategory = !isAddingCustomCategory }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = i18n("自定义分类", "自定義分類"),
                                        tint = FengShiTheme.colors.textPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = i18n("新分类", "新分類"),
                                        fontSize = 11.sp,
                                        color = FengShiTheme.colors.textPrimary
                                    )
                                }
                            }
                        }

                        // Inline Custom Category Input
                        if (isAddingCustomCategory) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customCategoryInput,
                                    onValueChange = { customCategoryInput = it },
                                    placeholder = { Text(i18n("输入新标签 (如：考研、还贷)...", "輸入新標籤 (如：考研、還貸)..."), color = FengShiTheme.colors.textMuted, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FengShiTheme.colors.textPrimary,
                                        unfocusedBorderColor = FengShiTheme.colors.border,
                                        focusedTextColor = FengShiTheme.colors.textPrimary,
                                        unfocusedTextColor = FengShiTheme.colors.textPrimary,
                                        cursorColor = FengShiTheme.colors.textPrimary,
                                        focusedContainerColor = FengShiTheme.colors.surfaceVariant,
                                        unfocusedContainerColor = FengShiTheme.colors.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        val trimmed = customCategoryInput.trim()
                                        if (trimmed.isNotBlank()) {
                                            category = trimmed
                                            onAddNewCategory?.invoke(trimmed)
                                            customCategoryInput = ""
                                            isAddingCustomCategory = false
                                        }
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                                        contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                                    )
                                ) {
                                    Text(i18n("保存", "保存"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 东方雅致色彩 (COLOR 标记色彩)
                        val selectedColorName = orientalColors.find { it.first == colorHex }?.second?.let {
                            if (LanguageManager.isTraditional()) it.second else it.first
                        } ?: ""

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = i18n("COLOR 标记色彩", "COLOR 標記色彩"),
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textMuted,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (selectedColorName.isNotEmpty()) {
                                Text(
                                    text = "${i18n("已选：", "已選：")}$selectedColorName",
                                    fontSize = 11.sp,
                                    color = Color(colorHex),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for ((hex, namePair) in orientalColors) {
                                val isSelected = colorHex == hex
                                val name = if (LanguageManager.isTraditional()) namePair.second else namePair.first

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            colorHex = hex
                                        }
                                        .padding(4.dp)
                                        .testTag("color_item_$name")
                                ) {
                                    // 外圈高亮环反馈 (Concentric Selection Ring)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) Color(hex) else FengShiTheme.colors.border,
                                                shape = CircleShape
                                            )
                                            .padding(3.5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // 内圈实体色彩 (Solid Inner Color)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color(hex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = name,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(hex) else FengShiTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 系统级双轨提醒系统设置
                        Text(
                            text = i18n("REMINDER 提醒与响铃设置", "REMINDER 提醒與響鈴設置"),
                            fontSize = 11.sp,
                            color = FengShiTheme.colors.textMuted,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val reminders = listOf(
                                EventEntity.REMINDER_NONE to i18n("不提醒", "不提醒"),
                                EventEntity.REMINDER_DAY_OF to i18n("当天", "當天"),
                                EventEntity.REMINDER_BEFORE_1 to i18n("提前 1 天", "提前 1 天"),
                                EventEntity.REMINDER_BEFORE_2 to i18n("提前 2 天", "提前 2 天"),
                                EventEntity.REMINDER_BEFORE_3 to i18n("提前 3 天", "提前 3 天"),
                                EventEntity.REMINDER_BEFORE_7 to i18n("提前 7 天", "提前 7 天"),
                                EventEntity.REMINDER_BEFORE_15 to i18n("提前 15 天", "提前 15 天"),
                                EventEntity.REMINDER_BEFORE_30 to i18n("提前 30 天", "提前 30 天")
                            )
                            for ((rKey, rName) in reminders) {
                                val isSelected = reminderMode == rKey
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) (if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary)
                                            else FengShiTheme.colors.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) FengShiTheme.colors.border else FengShiTheme.colors.border.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { reminderMode = rKey }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = rName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        if (reminderMode != EventEntity.REMINDER_NONE) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // 自定义具体提醒时刻设置卡片
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FengShiTheme.colors.surfaceVariant)
                                    .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                                    .clickable { showTimePickerSheet = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("reminder_time_picker_trigger")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = i18n("准点触发时刻", "準點觸發時刻"),
                                            fontSize = 11.sp,
                                            color = FengShiTheme.colors.textMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = reminderTime,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = FengShiTheme.colors.accentGold
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FengShiTheme.colors.surface)
                                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = i18n("选择时刻", "選擇時刻"),
                                            tint = FengShiTheme.colors.accentGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = i18n("调整时刻", "調整時刻"),
                                            fontSize = 12.sp,
                                            color = FengShiTheme.colors.textPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 双轨提醒机制设置卡片
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(8.dp))
                                    .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // 模式一：未确认常驻通知
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(
                                                text = i18n("📌 常驻通知 (未确认不消除)", "📌 常駐通知 (未確認不消除)"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FengShiTheme.colors.textPrimary
                                            )
                                            Text(
                                                text = i18n("持续常驻通知栏无法滑除，必须点击「确认已阅」才消除，防手滑遗漏。", "持續常駐通知欄無法滑除，必須點擊「確認已閱」才消除，防手滑遺漏。"),
                                                fontSize = 10.sp,
                                                color = FengShiTheme.colors.textMuted,
                                                lineHeight = 14.sp
                                            )
                                        }
                                        Switch(
                                            checked = isPersistentNotification,
                                            onCheckedChange = { isPersistentNotification = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
                                                checkedTrackColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                                                uncheckedThumbColor = FengShiTheme.colors.textMuted,
                                                uncheckedTrackColor = FengShiTheme.colors.surfaceVariant
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(FengShiTheme.colors.border)
                                    )

                                    // 模式二：独立响铃闹钟
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(
                                                text = i18n("⏰ 独立响铃闹钟", "⏰ 獨立響鈴鬧鐘"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FengShiTheme.colors.textPrimary
                                            )
                                            Text(
                                                text = i18n("准点播放闹钟铃声并强力震动唤醒，与常驻通知互不干扰，双轨保障。", "準點播放鬧鐘鈴聲並強力震動喚醒，與常駐通知互不干擾，雙軌保障。"),
                                                fontSize = 10.sp,
                                                color = FengShiTheme.colors.textMuted,
                                                lineHeight = 14.sp
                                            )
                                        }
                                        Switch(
                                            checked = isAlarmEnabled,
                                            onCheckedChange = { isAlarmEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
                                                checkedTrackColor = FengShiTheme.colors.accentGold,
                                                uncheckedThumbColor = FengShiTheme.colors.textMuted,
                                                uncheckedTrackColor = FengShiTheme.colors.surfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 重要置顶
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = i18n("置顶", "置頂"),
                                    tint = if (isPinned) FengShiTheme.colors.accentGold else FengShiTheme.colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = i18n("置顶于首页与桌面小组件", "置頂於首頁與桌面小組件"),
                                    fontSize = 13.sp,
                                    color = FengShiTheme.colors.textPrimary
                                )
                            }
                            Switch(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
                                    checkedTrackColor = FengShiTheme.colors.accentGold,
                                    uncheckedThumbColor = FengShiTheme.colors.textMuted,
                                    uncheckedTrackColor = FengShiTheme.colors.surfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 寄语备注
                        Text(text = i18n("寄语与备忘", "寄語與備忘"), fontSize = 12.sp, color = FengShiTheme.colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text(i18n("写下此时此刻的期许与备忘...", "寫下此時此刻的期許與備忘..."), color = FengShiTheme.colors.textMuted, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FengShiTheme.colors.textPrimary,
                                unfocusedBorderColor = FengShiTheme.colors.border,
                                focusedTextColor = FengShiTheme.colors.textPrimary,
                                unfocusedTextColor = FengShiTheme.colors.textPrimary,
                                cursorColor = FengShiTheme.colors.textPrimary,
                                focusedContainerColor = FengShiTheme.colors.surfaceVariant,
                                unfocusedContainerColor = FengShiTheme.colors.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FengShiSecondaryButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text(i18n("取消", "取消"), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FengShiPrimaryButton(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = tr("请输入吉日/纪念日标题", "請輸入吉日/紀念日標題")
                                return@FengShiPrimaryButton
                            }
                            val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                            val calType = if (isLunar) EventEntity.CALENDAR_LUNAR else EventEntity.CALENDAR_SOLAR
                            val celBy = if (isLunar) EventEntity.CELEBRATE_LUNAR else EventEntity.CELEBRATE_SOLAR
                            val repRule = if (eventType == EventEntity.TYPE_ANNIVERSARY || eventType == EventEntity.TYPE_TARGET) {
                                EventEntity.REPEAT_NONE
                            } else {
                                EventEntity.REPEAT_ANNUAL
                            }

                            val lYear: Int
                            val lMonth: Int
                            val lDay: Int
                            val isLeap: Boolean
                            if (isLunar) {
                                lYear = selectedYear
                                lMonth = selectedMonth
                                lDay = selectedDay
                                isLeap = false
                            } else {
                                val l = LunarCalendar.solarToLunar(LocalDate.of(selectedYear, selectedMonth, selectedDay))
                                lYear = l.year
                                lMonth = l.month
                                lDay = l.day
                                isLeap = l.isLeapMonth
                            }

                            val newEvent = (initialEvent ?: EventEntity(title = "", dateString = dateStr)).copy(
                                title = title.trim(),
                                dateString = dateStr,
                                isLunar = isLunar,
                                calendarType = calType,
                                celebrateBy = celBy,
                                repeatRule = repRule,
                                lunarYear = lYear,
                                lunarMonth = lMonth,
                                lunarDay = lDay,
                                isLeapMonth = isLeap,
                                eventType = eventType,
                                category = category,
                                categoryColorHex = colorHex,
                                isPinned = isPinned,
                                reminderMode = reminderMode,
                                reminderTime = reminderTime,
                                isPersistentNotification = isPersistentNotification,
                                isAlarmEnabled = isAlarmEnabled,
                                notes = notes.trim()
                            )
                            onSave(newEvent)
                        },
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text(i18n("保存吉日", "保存吉日"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // 细致的历法日期滚轮挑选弹窗
    if (showDatePickerSheet) {
        val initialPickerDate = remember(selectedYear, selectedMonth, selectedDay) {
            try {
                LocalDate.of(selectedYear, selectedMonth, selectedDay.coerceAtMost(28))
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
        DatePickerDialogSheet(
            initialDate = initialPickerDate,
            isLunar = isLunar,
            onDateSelected = { y, m, d ->
                selectedYear = y
                selectedMonth = m
                selectedDay = d
            },
            onDismiss = { showDatePickerSheet = false }
        )
    }

    // 细致的安卓系统级时间设定弹窗
    if (showTimePickerSheet) {
        TimePickerDialogSheet(
            initialTime = reminderTime,
            onTimeSelected = { newTime ->
                reminderTime = newTime
            },
            onDismiss = { showTimePickerSheet = false }
        )
    }
}

@Composable
fun NumberPickerBox(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    displayText: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "-",
                color = FengShiTheme.colors.textSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        val next = (value - 1).coerceAtLeast(min)
                        onValueChange(next)
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )

            Text(
                text = displayText ?: "$value$label",
                color = FengShiTheme.colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = "+",
                color = FengShiTheme.colors.textSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        val next = (value + 1).coerceAtMost(max)
                        onValueChange(next)
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
