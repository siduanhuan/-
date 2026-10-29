package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.example.ui.components.FengShiCapsuleButton
import com.example.ui.components.FengShiPrimaryButton
import com.example.ui.components.FengShiSecondaryButton
import com.example.ui.components.PullDownSearchBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calendar.LunarCalendar
import com.example.data.EventEntity
import com.example.ui.theme.EditorialVermillion
import com.example.ui.theme.FengShiTheme
import com.example.i18n.LanguageManager
import com.example.i18n.i18n
import com.example.i18n.tr
import com.example.ui.theme.OledBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * 逢时 · 主界面 (Editorial Aesthetic 东方画报风格)
 */
@Composable
fun MainScreen(viewModel: EventViewModel) {
    val context = LocalContext.current
    val events by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val userCategories by viewModel.userCategories.collectAsStateWithLifecycle()
    val isSampleBannerDismissed by viewModel.isSampleBannerDismissed.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedItemIds.collectAsStateWithLifecycle()
    val isHydrated by viewModel.isHydrated.collectAsStateWithLifecycle()
    var showBatchDeleteConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isSelectionMode) {
        viewModel.exitSelectionMode()
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var viewingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var sharingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showCalendarView by remember { mutableStateOf(false) }
    var presetAddDate by remember { mutableStateOf<LocalDate?>(null) }

    val sampleCount = remember(allEvents) { viewModel.countSampleEvents(allEvents) }
    val showSampleBanner = sampleCount > 0 && !isSampleBannerDismissed

    val today = remember { LocalDate.now() }
    val todayLunar = remember { LunarCalendar.solarToLunar(today) }

    val monthNames = remember { arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec") }
    val englishDate = remember(today) { "${monthNames[today.monthValue - 1]} ${today.dayOfMonth}, ${today.year}" }

    // Spotlight/Pinned event
    val spotlightEvent = remember(allEvents) {
        allEvents.firstOrNull { it.isPinned } ?: allEvents.minByOrNull {
            val d = it.computeDays(today)
            if (it.eventType == EventEntity.TYPE_ANNIVERSARY) 99999 else d
        }
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(FengShiTheme.colors.background)
            .statusBarsPadding()
            .testTag("main_screen"),
        containerColor = FengShiTheme.colors.background,
        bottomBar = {
            if (isSelectionMode) {
                // Bottom Batch Action Bar (批量操作常驻底栏)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("batch_action_bar"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(FengShiTheme.colors.surfaceElevated)
                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (selectedItemIds.isEmpty()) i18n("请勾选需删除的佳期", "請勾選需刪除的佳期") else i18n("已选中 ${selectedItemIds.size} 项", "已選中 ${selectedItemIds.size} 項"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedItemIds.isEmpty()) FengShiTheme.colors.textMuted else FengShiTheme.colors.textPrimary
                            )
                            if (selectedItemIds.isNotEmpty()) {
                                Text(
                                    text = i18n("删除将同步注销定时提醒与微件", "刪除將同步註銷定時提醒與微件"),
                                    fontSize = 11.sp,
                                    color = FengShiTheme.colors.textMuted
                                )
                            }
                        }

                        val isDeleteEnabled = selectedItemIds.isNotEmpty()
                        FengShiSecondaryButton(
                            onClick = {
                                if (isDeleteEnabled) {
                                    showBatchDeleteConfirmDialog = true
                                }
                            },
                            enabled = isDeleteEnabled,
                            containerColor = if (isDeleteEnabled) FengShiTheme.colors.accentVermillion else FengShiTheme.colors.surfaceVariant,
                            contentColor = if (isDeleteEnabled) Color.White else FengShiTheme.colors.textMuted,
                            borderColor = if (isDeleteEnabled) FengShiTheme.colors.accentVermillion else Color.Transparent,
                            modifier = Modifier.testTag("batch_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = i18n("批量删除", "批量刪除"),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedItemIds.isEmpty()) i18n("批量删除", "批量刪除") else "${i18n("删除", "刪除")} (${selectedItemIds.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Editorial Aesthetic Floating Nav Dock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Calendar Button (打开岁时吉历全景)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(FengShiTheme.colors.surfaceElevated)
                                .border(1.dp, FengShiTheme.colors.border, CircleShape)
                                .clickable {
                                    showCalendarView = true
                                }
                                .testTag("open_calendar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = i18n("岁时吉历", "歲時吉曆"),
                                tint = FengShiTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center: Dramatic Editorial White Action FAB with Black Border
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary)
                                .border(4.dp, FengShiTheme.colors.background, CircleShape)
                            .clickable {
                                editingEvent = null
                                showAddEditDialog = true
                            }
                            .testTag("add_event_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = i18n("添录吉日", "添錄吉日"),
                            tint = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Right: Settings & Backup Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FengShiTheme.colors.surfaceElevated)
                            .border(1.dp, FengShiTheme.colors.border, CircleShape)
                            .clickable {
                                showSettingsDialog = true
                            }
                            .testTag("open_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = i18n("设置与备份", "設置與備份"),
                            tint = FengShiTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    ) { innerPadding ->
        if (isTablet) {
            // 平板横屏/大屏专属 Master-Detail 双栏分栏布局
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 左侧主栏：事件浏览与筛选
                Box(
                    modifier = Modifier
                        .widthIn(min = 360.dp, max = 460.dp)
                        .fillMaxHeight()
                ) {
                    MainEventListView(
                        events = events,
                        isHydrated = isHydrated,
                        spotlightEvent = null,
                        today = today,
                        todayLunar = todayLunar,
                        englishDate = englishDate,
                        searchQuery = searchQuery,
                        showSearchBar = showSearchBar,
                        selectedCategory = selectedCategory,
                        userCategories = userCategories,
                        sortOrder = sortOrder,
                        showSampleBanner = showSampleBanner,
                        sampleCount = sampleCount,
                        isSelectionMode = isSelectionMode,
                        selectedItemIds = selectedItemIds,
                        onToggleSearch = { showSearchBar = !showSearchBar },
                        onSearchChange = { viewModel.searchQuery.value = it },
                        onCategorySelect = { viewModel.selectedCategory.value = it },
                        onOpenCategoryDialog = { showCategoryDialog = true },
                        onToggleSort = {
                            val nextSort = when (sortOrder) {
                                EventViewModel.SORT_DAYS -> EventViewModel.SORT_CREATED
                                EventViewModel.SORT_CREATED -> EventViewModel.SORT_PINNED
                                else -> EventViewModel.SORT_DAYS
                            }
                            viewModel.sortOrder.value = nextSort
                        },
                        onClearSampleEvents = {
                            viewModel.deleteSampleEvents {
                                Toast.makeText(context, tr("已一键清空预置示例", "已一鍵清空預置示例"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDismissSampleBanner = { viewModel.dismissSampleBanner() },
                        onEnterSelectionMode = { initialId -> viewModel.enterSelectionMode(initialId) },
                        onExitSelectionMode = { viewModel.exitSelectionMode() },
                        onToggleSelect = { id -> viewModel.toggleSelect(id) },
                        onSelectAll = { ids -> viewModel.selectAll(ids) },
                        onDeselectAll = { viewModel.deselectAll() },
                        onEventClick = { event -> viewingEvent = event },
                        onTogglePin = { event -> viewModel.togglePin(event) },
                        onAdd = {
                            editingEvent = null
                            showAddEditDialog = true
                        }
                    )
                }

                // 雅致细分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(FengShiTheme.colors.divider)
                )

                // 右侧副栏：选中的事件详情或诗意吉日全景卡片
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    val activeDetailEvent = viewingEvent ?: spotlightEvent
                    if (activeDetailEvent != null) {
                        EventDetailContent(
                            event = activeDetailEvent,
                            onDismiss = if (viewingEvent != null) { { viewingEvent = null } } else null,
                            onEdit = {
                                editingEvent = activeDetailEvent
                                showAddEditDialog = true
                            },
                            onShare = {
                                sharingEvent = activeDetailEvent
                            },
                            onTogglePin = {
                                viewModel.togglePin(activeDetailEvent)
                                if (viewingEvent?.id == activeDetailEvent.id) {
                                    viewingEvent = viewingEvent?.copy(isPinned = !activeDetailEvent.isPinned)
                                }
                            },
                            onDelete = {
                                viewModel.deleteEvent(activeDetailEvent)
                                if (viewingEvent?.id == activeDetailEvent.id) {
                                    viewingEvent = null
                                }
                            }
                        )
                    } else {
                        TabletEmptyOverview(
                            todayLunar = todayLunar,
                            englishDate = englishDate,
                            onAdd = {
                                editingEvent = null
                                showAddEditDialog = true
                            }
                        )
                    }
                }
            }
        } else {
            // 手机单栏竖屏流线型布局
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MainEventListView(
                    events = events,
                    isHydrated = isHydrated,
                    spotlightEvent = spotlightEvent,
                    today = today,
                    todayLunar = todayLunar,
                    englishDate = englishDate,
                    searchQuery = searchQuery,
                    showSearchBar = showSearchBar,
                    selectedCategory = selectedCategory,
                    userCategories = userCategories,
                    sortOrder = sortOrder,
                    showSampleBanner = showSampleBanner,
                    sampleCount = sampleCount,
                    isSelectionMode = isSelectionMode,
                    selectedItemIds = selectedItemIds,
                    onToggleSearch = { showSearchBar = !showSearchBar },
                    onSearchChange = { viewModel.searchQuery.value = it },
                    onCategorySelect = { viewModel.selectedCategory.value = it },
                    onOpenCategoryDialog = { showCategoryDialog = true },
                    onToggleSort = {
                        val nextSort = when (sortOrder) {
                            EventViewModel.SORT_DAYS -> EventViewModel.SORT_CREATED
                            EventViewModel.SORT_CREATED -> EventViewModel.SORT_PINNED
                            else -> EventViewModel.SORT_DAYS
                        }
                        viewModel.sortOrder.value = nextSort
                    },
                    onClearSampleEvents = {
                        viewModel.deleteSampleEvents {
                            Toast.makeText(context, tr("已一键清空预置示例", "已一鍵清空預置示例"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDismissSampleBanner = { viewModel.dismissSampleBanner() },
                    onEnterSelectionMode = { initialId -> viewModel.enterSelectionMode(initialId) },
                    onExitSelectionMode = { viewModel.exitSelectionMode() },
                    onToggleSelect = { id -> viewModel.toggleSelect(id) },
                    onSelectAll = { ids -> viewModel.selectAll(ids) },
                    onDeselectAll = { viewModel.deselectAll() },
                    onEventClick = { event -> viewingEvent = event },
                    onTogglePin = { event -> viewModel.togglePin(event) },
                    onAdd = {
                        editingEvent = null
                        showAddEditDialog = true
                    }
                )
            }
        }
    }

    // 批量删除二次确认警示框 (Batch Delete Confirmation Dialog)
    if (showBatchDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirmDialog = false },
            title = {
                Text(
                    text = i18n("确认批量删除", "確認批量刪除"),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = FengShiTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "${i18n("确定要彻底删除选中的", "確定要徹底刪除選中的")} ${selectedItemIds.size} ${i18n("项吉日吗？此操作无法撤销，对应的系统定时提醒与微件展示也将同步移除。", "項吉日嗎？此操作無法撤銷，對應的系統定時提醒與微件展示也將同步移除。")}",
                    color = FengShiTheme.colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = selectedItemIds.size
                        viewModel.batchDelete { deletedCount ->
                            Toast.makeText(context, "${tr("已成功批量删除", "已成功批量刪除")} $deletedCount ${tr("项吉日", "項吉日")}", Toast.LENGTH_SHORT).show()
                        }
                        showBatchDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FengShiTheme.colors.accentVermillion,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("confirm_batch_delete_button")
                ) {
                    Text(i18n("确认删除", "確認刪除"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBatchDeleteConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_batch_delete_button")
                ) {
                    Text(i18n("取消", "取消"), color = FengShiTheme.colors.textMuted)
                }
            },
            containerColor = FengShiTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialogs
    if (showCalendarView) {
        CalendarMonthViewDialog(
            events = allEvents,
            onDismiss = { showCalendarView = false },
            onEventClick = { ev ->
                viewingEvent = ev
            },
            onAddEventForDate = { date ->
                editingEvent = null
                presetAddDate = date
                showAddEditDialog = true
            }
        )
    }

    if (showAddEditDialog) {
        AddEditEventDialog(
            initialEvent = editingEvent,
            presetDate = presetAddDate,
            categoriesList = userCategories,
            onAddNewCategory = { newCat ->
                viewModel.addCategory(newCat)
            },
            onOpenCategoryManagement = {
                showCategoryDialog = true
            },
            onDismiss = {
                showAddEditDialog = false
                editingEvent = null
                presetAddDate = null
            },
            onSave = { saved ->
                if (editingEvent != null) {
                    viewModel.updateEvent(saved)
                } else {
                    viewModel.addEvent(saved)
                }
                showAddEditDialog = false
                editingEvent = null
                presetAddDate = null
            }
        )
    }

    if (showCategoryDialog) {
        CategoryManagementDialog(
            viewModel = viewModel,
            onDismiss = { showCategoryDialog = false }
        )
    }

    // 手机端底部弹窗；平板端在右侧主界面直接响应展示
    if (!isTablet && viewingEvent != null) {
        EventDetailSheet(
            event = viewingEvent!!,
            onDismiss = { viewingEvent = null },
            onEdit = {
                editingEvent = viewingEvent
                viewingEvent = null
                showAddEditDialog = true
            },
            onShare = {
                sharingEvent = viewingEvent
            },
            onTogglePin = {
                viewModel.togglePin(viewingEvent!!)
                viewingEvent = viewingEvent!!.copy(isPinned = !viewingEvent!!.isPinned)
            },
            onDelete = {
                viewModel.deleteEvent(viewingEvent!!)
                viewingEvent = null
            }
        )
    }

    if (sharingEvent != null) {
        ShareCardDialog(
            event = sharingEvent!!,
            onDismiss = { sharingEvent = null }
        )
    }

    if (showSettingsDialog) {
        SettingsAboutDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false },
            onReplayOnboarding = { viewModel.resetOnboarding() }
        )
    }
}

/**
 * 首页聚焦 Spotlight 视觉卡片 (Editorial Aesthetic)
 */
@Composable
fun SpotlightCard(
    event: EventEntity,
    today: LocalDate,
    onClick: () -> Unit
) {
    val days = remember(event, today) { event.computeDays(today) }
    val status = remember(event, today) { event.getStatusDescription(today) }
    val todayLunar = remember(today) { LunarCalendar.solarToLunar(today) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FengShiTheme.colors.surface)
            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
            .testTag("spotlight_card")
    ) {
        // Subtle Watermark Zodiac Glyphs in Top-Right
        Text(
            text = todayLunar.zodiac,
            fontSize = 110.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = FengShiTheme.colors.textPrimary.copy(alpha = 0.04f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 12.dp, y = (-20).dp)
        )

        Column {
            // Header with Vermilion Seal Tag & Light Serif Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 标记色彩圆点 (Oriental Color Dot Indicator)
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(event.categoryColorHex))
                    )

                    Box(
                        modifier = Modifier
                            .background(EditorialVermillion.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (event.isPinned) i18n("置顶", "置頂") else event.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EditorialVermillion,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = event.title,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        color = FengShiTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Italic poetry quote / note
            val quoteText = if (event.notes.isNotBlank()) {
                "“${event.notes}”"
            } else {
                "“$status”"
            }
            Text(
                text = quoteText,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                color = FengShiTheme.colors.textSecondary,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Big Editorial Numeral with Tracked Uppercase Suffix
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = days.toString(),
                    fontSize = 92.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = FengShiTheme.colors.textPrimary,
                    letterSpacing = (-4).sp,
                    lineHeight = 92.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "DAYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = FengShiTheme.colors.textMuted
                    )
                    Text(
                        text = if (event.eventType == EventEntity.TYPE_ANNIVERSARY) "PAST" else "LEFT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = FengShiTheme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date meta
            Text(
                text = "${event.getDisplayDateText()} · $status",
                fontSize = 12.sp,
                color = FengShiTheme.colors.textMuted
            )
        }
    }
}

/**
 * 列表单条卡片 (Editorial Magazine Row with Hairline Dividers & Multi-Selection Support)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventItemCard(
    event: EventEntity,
    today: LocalDate,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onTogglePin: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val days = remember(event, today) { event.computeDays(today) }
    val isAnniversary = event.eventType == EventEntity.TYPE_ANNIVERSARY

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isSelectionMode) 16.dp else 0.dp,
                vertical = if (isSelectionMode) 3.dp else 0.dp
            )
            .clip(if (isSelectionMode) RoundedCornerShape(10.dp) else RoundedCornerShape(0.dp))
            .background(
                if (isSelected) EditorialVermillion.copy(alpha = 0.12f)
                else if (isSelectionMode) FengShiTheme.colors.surface
                else Color.Transparent
            )
            .border(
                width = if (isSelected) 1.5.dp else if (isSelectionMode) 1.dp else 0.dp,
                color = if (isSelected) EditorialVermillion else if (isSelectionMode) FengShiTheme.colors.border else Color.Transparent,
                shape = if (isSelectionMode) RoundedCornerShape(10.dp) else RoundedCornerShape(0.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .testTag("event_item_${event.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isSelectionMode) 16.dp else 24.dp,
                        vertical = 14.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 多选态专属复选框 (Active Selection Checkbox Indicator)
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) EditorialVermillion else FengShiTheme.colors.surfaceVariant)
                            .border(
                                1.5.dp,
                                if (isSelected) EditorialVermillion else FengShiTheme.colors.border,
                                CircleShape
                            )
                            .testTag("event_checkbox_${event.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = i18n("已勾选", "已勾選"),
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                // 卡片左侧色彩指示条 (Card Vertical Oriental Color Indicator Bar)
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(event.categoryColorHex))
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Top Meta: Category & Date with oriental color dot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(event.categoryColorHex))
                        )
                        Spacer(modifier = Modifier.width(5.dp))

                        if (event.isPinned && !isSelectionMode) {
                            Box(
                                modifier = Modifier
                                    .background(EditorialVermillion.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = i18n("置顶", "置頂"),
                                    fontSize = 10.sp,
                                    color = EditorialVermillion,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "${event.category} · ${event.getDisplayDateText()}",
                            fontSize = 11.sp,
                            color = FengShiTheme.colors.textMuted,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Title
                    Text(
                        text = event.title,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        color = FengShiTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (event.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = event.notes,
                            fontSize = 12.sp,
                            color = FengShiTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Numeral & Suffix
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = days.toString(),
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        color = if (isAnniversary) FengShiTheme.colors.textSecondary else FengShiTheme.colors.textPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isAnniversary) i18n("已过", "已過") else i18n("天后", "天後"),
                        fontSize = 10.sp,
                        color = FengShiTheme.colors.textMuted,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(bottom = 3.dp),
                        maxLines = 1
                    )

                    if (!isSelectionMode) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (event.isPinned) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = i18n("置顶切换", "置頂切換"),
                                tint = if (event.isPinned) EditorialVermillion else FengShiTheme.colors.border,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 非多选态下保留雅致发丝分隔线
            if (!isSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(1.dp)
                        .background(FengShiTheme.colors.divider)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    searchQuery: String = "",
    onClearSearch: () -> Unit = {},
    onAdd: () -> Unit
) {
    val isSearchEmpty = searchQuery.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(FengShiTheme.colors.surfaceVariant)
                .border(1.dp, FengShiTheme.colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearchEmpty) Icons.Default.Search else Icons.Default.CalendarMonth,
                contentDescription = if (isSearchEmpty) i18n("无匹配吉日", "無匹配吉日") else i18n("暂无吉日", "暫無吉日"),
                tint = FengShiTheme.colors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isSearchEmpty) i18n("未寻得相关吉日", "未尋得相關吉日") else i18n("良辰吉日，静候记录", "良辰吉日，靜候記錄"),
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = FengShiTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearchEmpty)
                "${i18n("未检索到包含「", "未檢索到包含「")}$searchQuery${i18n("」的纪念日、分类或诗词", "」的紀念日、分類或詩詞")}"
            else
                i18n("人生忽如寄，逢时倍相惜。\n点击下方添录生活中的纪念日与生日。", "人生忽如寄，逢時倍相惜。\n點擊下方添錄生活中的紀念日與生日。"),
            fontSize = 13.sp,
            color = FengShiTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isSearchEmpty) {
            FengShiCapsuleButton(
                selected = true,
                onClick = onClearSearch,
                modifier = Modifier.testTag("empty_search_clear_button")
            ) {
                Text(
                    text = i18n("清空搜索条件", "清空搜尋條件"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            FengShiPrimaryButton(
                onClick = onAdd,
                modifier = Modifier.testTag("empty_state_add_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = i18n("添录首个吉日", "添錄首個吉日"),
                    tint = if (FengShiTheme.colors.isDark) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = i18n("添录第一条佳期", "添錄第一條佳期"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                )
            }
        }
    }
}

/**
 * 主事件流列表视图 (手机主界面与平板左侧 Master 栏复用)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainEventListView(
    events: List<EventEntity>,
    isHydrated: Boolean = true,
    spotlightEvent: EventEntity?,
    today: LocalDate,
    todayLunar: LunarCalendar.LunarDate,
    englishDate: String,
    searchQuery: String,
    showSearchBar: Boolean,
    selectedCategory: String,
    userCategories: List<String>,
    sortOrder: String,
    showSampleBanner: Boolean,
    sampleCount: Int,
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onOpenCategoryDialog: () -> Unit,
    onToggleSort: () -> Unit,
    onClearSampleEvents: () -> Unit,
    onDismissSampleBanner: () -> Unit,
    onEnterSelectionMode: (Long?) -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onToggleSelect: (Long) -> Unit = {},
    onSelectAll: (List<Long>) -> Unit = {},
    onDeselectAll: () -> Unit = {},
    onEventClick: (EventEntity) -> Unit,
    onTogglePin: (EventEntity) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var pullDistance by remember { mutableFloatStateOf(0f) }
    val pullThresholdPx = 180f

    val nestedScrollConnection = remember(showSearchBar, isSelectionMode) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If search bar is shown and user scrolls down to browse, we can let normal scroll happen
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Only detect pull down when at the top of the list, not in selection mode, and not already showing search bar
                if (!showSearchBar && !isSelectionMode && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    if (available.y > 0) {
                        pullDistance += available.y * 0.5f // Damping effect
                        if (pullDistance >= pullThresholdPx) {
                            pullDistance = 0f
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleSearch()
                        }
                        return Offset(0f, available.y)
                    }
                }
                if (available.y < 0 && pullDistance > 0f) {
                    pullDistance = (pullDistance + available.y).coerceAtLeast(0f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        if (isSelectionMode) {
            // Active Selection Top Bar (多选管理专属顶栏)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExitSelectionMode,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("exit_selection_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("取消退出多选", "取消退出多選"),
                            tint = FengShiTheme.colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedItemIds.isEmpty()) i18n("选择吉日", "選擇吉日") else "${i18n("已选择", "已選擇")} ${selectedItemIds.size} ${i18n("项", "項")}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = FengShiTheme.colors.textPrimary
                    )
                }

                val currentVisibleIds = remember(events) { events.map { it.id } }
                val isAllSelected = currentVisibleIds.isNotEmpty() && currentVisibleIds.all { selectedItemIds.contains(it) }

                Button(
                    onClick = {
                        if (isAllSelected) {
                            onDeselectAll()
                        } else {
                            onSelectAll(currentVisibleIds)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAllSelected) (if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary) else FengShiTheme.colors.surfaceVariant,
                        contentColor = if (isAllSelected) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("toggle_select_all_button")
                ) {
                    Text(
                        text = if (isAllSelected) i18n("取消全选", "取消全選") else i18n("全选", "全選"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Editorial Aesthetic Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "FENG SHI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp,
                        color = FengShiTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = i18n("逢时", "逢時"),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = FengShiTheme.colors.textPrimary,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = (-0.5).sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${todayLunar.cyclicalYear} · ${todayLunar.monthChinese}${todayLunar.dayChinese}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FengShiTheme.colors.textSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = englishDate,
                        fontSize = 11.sp,
                        color = FengShiTheme.colors.textMuted,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // 下拉呼出搜索栏组件（高度 40dp、12dp 倒角、清空与取消功能齐全）
        PullDownSearchBar(
            visible = showSearchBar,
            query = searchQuery,
            onQueryChange = onSearchChange,
            onDismiss = {
                onSearchChange("")
                if (showSearchBar) onToggleSearch()
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (isSelectionMode) 120.dp else 96.dp)
        ) {
            // Editorial Spotlight Card
            if (!isSelectionMode && spotlightEvent != null && searchQuery.isBlank() && selectedCategory == "全部") {
                item {
                    SpotlightCard(
                        event = spotlightEvent,
                        today = today,
                        onClick = { onEventClick(spotlightEvent) }
                    )
                }
            }

            // 预置示例一键清理横幅 (Editorial System Samples Banner)
            if (!isSelectionMode && showSampleBanner && searchQuery.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FengShiTheme.colors.surfaceVariant)
                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                            .testTag("sample_events_banner")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(FengShiTheme.colors.accentVermillion)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${i18n("预置示例项目", "預置示例項目")} · $sampleCount ${i18n("件", "件")}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = FengShiTheme.colors.textPrimary
                                    )
                                }

                                Text(
                                    text = "${i18n("保留并隐藏", "保留並隱藏")} ✕",
                                    fontSize = 11.sp,
                                    color = FengShiTheme.colors.textMuted,
                                    modifier = Modifier
                                        .clickable { onDismissSampleBanner() }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = i18n("初次探索内置的节日与生辰示例。您可随时一键清空它们，开始录入属于您的时光。", "初次探索內置的節日與生辰示例。您可隨時一鍵清空它們，開始錄入屬於您的時光。"),
                                fontSize = 11.sp,
                                color = FengShiTheme.colors.textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = onClearSampleEvents,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FengShiTheme.colors.accentVermillion.copy(alpha = 0.15f),
                                        contentColor = FengShiTheme.colors.accentVermillion
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(i18n("一键清空示例", "一鍵清空示例"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Category Chips Selector (Clean Minimal Editorial Pills)
            // 支持点击筛选；长按自定义分类可直接触发分类管理
            item {
                val categories = listOf("全部", "置顶") + userCategories
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val displayCat = when (cat) {
                            "全部" -> i18n("全部", "全部")
                            "置顶" -> i18n("置顶", "置頂")
                            else -> cat
                        }
                        FengShiCapsuleButton(
                            selected = isSelected,
                            onClick = { onCategorySelect(cat) },
                            modifier = Modifier.testTag("category_chip_$cat")
                        ) {
                            Text(
                                text = displayCat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Summary & Sort Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelectionMode) "${i18n("共收录", "共收錄")} ${events.size} ${i18n("件佳期 · 已选中", "件佳期 · 已選中")} ${selectedItemIds.size} ${i18n("项", "項")}" else "${i18n("收录", "收錄")} ${events.size} ${i18n("件佳期", "件佳期")}",
                        fontSize = 11.sp,
                        color = FengShiTheme.colors.textMuted,
                        letterSpacing = 0.5.sp
                    )

                    // Sort toggle button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onToggleSort() }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = i18n("排序", "排序"),
                            tint = FengShiTheme.colors.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val sortLabel = when (sortOrder) {
                            EventViewModel.SORT_DAYS -> i18n("临近天数", "臨近天數")
                            EventViewModel.SORT_CREATED -> i18n("创建时间", "創建時間")
                            else -> i18n("置顶优先", "置頂優先")
                        }
                        Text(
                            text = sortLabel,
                            fontSize = 11.sp,
                            color = FengShiTheme.colors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Event List
            if (events.isEmpty() && isHydrated) {
                item {
                    EmptyStateView(
                        searchQuery = searchQuery,
                        onClearSearch = { onSearchChange("") },
                        onAdd = onAdd
                    )
                }
            } else if (events.isNotEmpty()) {
                items(events, key = { it.id }) { event ->
                    EventItemCard(
                        event = event,
                        today = today,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedItemIds.contains(event.id),
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelect(event.id)
                            } else {
                                onEventClick(event)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                onEnterSelectionMode(event.id)
                            } else {
                                onToggleSelect(event.id)
                            }
                        },
                        onTogglePin = { onTogglePin(event) }
                    )
                }
            }
        }
    }
}

/**
 * 平板右侧默认展示的吉日全景与诗意卡片
 */
@Composable
fun TabletEmptyOverview(
    todayLunar: LunarCalendar.LunarDate,
    englishDate: String,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(12.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(12.dp))
                .padding(36.dp)
        ) {
            Text(
                text = "FENG SHI · TABLET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = FengShiTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = i18n("岁时如诗 · 吉日相伴", "歲時如詩 · 吉日相伴"),
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                color = FengShiTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${todayLunar.cyclicalYear} (${todayLunar.zodiac}${i18n("年", "年")}) · ${todayLunar.monthChinese}${todayLunar.dayChinese} · $englishDate",
                fontSize = 13.sp,
                color = FengShiTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "「浮云一别后，流水十几年。\n欢笑情如旧，萧萧鬓已斑。」",
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                color = FengShiTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                    contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(i18n("添录新佳期", "添錄新佳期"), fontWeight = FontWeight.Bold)
            }
        }
    }
}
