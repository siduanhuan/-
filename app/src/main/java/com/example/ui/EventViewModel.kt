package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BackupRestoreManager
import com.example.data.EventEntity
import com.example.data.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.example.i18n.LanguageManager
import com.example.ui.theme.ThemeManager

/**
 * 列表展示专用视图模型预计算项 (只读轻量缓存，彻底剥离列表渲染主线程换算)
 */
data class EventUiItem(
    val event: EventEntity,
    val daysLeft: Int,
    val statusDescription: String,
    val displayDateText: String,
    val celebrationSubText: String?,
    val anniversaryRoundText: String?,
    val isToday: Boolean,
    val isPastOneOff: Boolean
)

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    private val prefs = application.getSharedPreferences("fengshi_prefs", Context.MODE_PRIVATE)

    val selectedCategory = MutableStateFlow("全部")
    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(prefs.getString("key_sort_order", SORT_DAYS) ?: SORT_DAYS) // SORT_DAYS, SORT_CREATED, SORT_PINNED

    fun setSortOrder(order: String) {
        prefs.edit().putString("key_sort_order", order).apply()
        sortOrder.value = order
    }

    // 外观风格：SYSTEM(跟随系统), LIGHT(宣纸水墨), DARK(玄曜纯黑)
    val themeMode: StateFlow<String> = ThemeManager.themeMode
    // 语言偏好：SYSTEM(跟随系统), ZH_CN(简体中文), ZH_TW(繁體中文)
    val languageMode: StateFlow<String> = LanguageManager.currentLanguage

    fun setThemeMode(mode: String) {
        ThemeManager.setThemeMode(getApplication(), mode)
    }

    fun setLanguageMode(mode: String) {
        LanguageManager.setLanguage(getApplication(), mode)
    }

    // 批量管理与多选状态 (Selection State Management)
    val isSelectionMode = MutableStateFlow(false)
    val selectedItemIds = MutableStateFlow<Set<Long>>(emptySet())

    // 兼容原引用别名
    val isBatchMode: MutableStateFlow<Boolean> get() = isSelectionMode
    val selectedIds: MutableStateFlow<Set<Long>> get() = selectedItemIds

    val isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("key_onboarding_completed", false)
    )

    // 数据初始拉取状态（用于控制骨架屏，防止初次空白闪烁）
    val isInitialLoading = MutableStateFlow(true)
    // 状态就绪屏障 (State Hydration Guard)：严格指示本地持久化数据已彻底从磁盘加载完毕
    val isHydrated = MutableStateFlow(false)

    private val defaultCategories = listOf("生日", "节日", "纪念日", "目标", "生活", "亲友")
    val userCategories = MutableStateFlow<List<String>>(loadCategories())

    val isSampleBannerDismissed = MutableStateFlow(
        prefs.getBoolean("key_dismiss_sample_banner", false)
    )

    private fun loadCategories(): List<String> {
        val saved = prefs.getString("key_custom_categories", null)
        if (saved.isNullOrBlank()) {
            return defaultCategories
        }
        val list = saved.split(",,,,").map { it.trim() }.filter { it.isNotBlank() }
        return if (list.isNotEmpty()) list else defaultCategories
    }

    private fun saveCategories(list: List<String>) {
        prefs.edit().putString("key_custom_categories", list.joinToString(",,,,")).apply()
        userCategories.value = list
    }

    fun addCategory(categoryName: String): Boolean {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank() || userCategories.value.contains(trimmed)) return false
        val updated = userCategories.value + trimmed
        saveCategories(updated)
        return true
    }

    fun renameCategory(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed == oldName) return
        val updated = userCategories.value.map { if (it == oldName) trimmed else it }
        saveCategories(updated)
        // 级联更新数据库中属于该分类的所有记录
        viewModelScope.launch {
            val all = repository.getAllEventsList()
            for (event in all) {
                if (event.category == oldName) {
                    repository.updateEvent(event.copy(category = trimmed))
                }
            }
        }
        if (selectedCategory.value == oldName) {
            selectedCategory.value = trimmed
        }
    }

    fun deleteCategory(categoryName: String) {
        if (userCategories.value.size <= 1) return
        val updated = userCategories.value.filter { it != categoryName }
        saveCategories(updated)
        if (selectedCategory.value == categoryName) {
            selectedCategory.value = "全部"
        }
    }

    fun updateCategoryColor(categoryName: String, colorHex: Long) {
        viewModelScope.launch {
            repository.updateColorForCategory(categoryName, colorHex)
        }
    }

    fun resetCategories() {
        saveCategories(defaultCategories)
    }

    // ================= 批量操作与多选管理 (Selection & Batch Operations) =================
    fun enterSelectionMode(initialId: Long? = null) {
        isSelectionMode.value = true
        selectedItemIds.value = if (initialId != null) setOf(initialId) else emptySet()
    }

    fun exitSelectionMode() {
        isSelectionMode.value = false
        selectedItemIds.value = emptySet()
    }

    fun toggleSelect(id: Long) {
        val current = selectedItemIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        selectedItemIds.value = current
    }

    fun selectAll(allIds: List<Long>) {
        selectedItemIds.value = allIds.toSet()
    }

    fun deselectAll() {
        selectedItemIds.value = emptySet()
    }

    // 兼容原有用法
    fun enterBatchMode(initialId: Long? = null) = enterSelectionMode(initialId)
    fun exitBatchMode() = exitSelectionMode()
    fun toggleSelection(id: Long) = toggleSelect(id)
    fun clearSelection() = deselectAll()

    fun batchMoveCategory(targetCategory: String, onComplete: () -> Unit) {
        val ids = selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.updateCategoryForIds(ids, targetCategory)
            exitSelectionMode()
            onComplete()
        }
    }

    fun batchDelete(onComplete: (Int) -> Unit) {
        val ids = selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteByIds(ids)
            val count = ids.size
            exitSelectionMode()
            onComplete(count)
        }
    }

    fun batchDelete(onComplete: () -> Unit) {
        batchDelete { _ -> onComplete() }
    }

    suspend fun batchExportJson(ids: List<Long>? = null): String {
        val all = repository.getAllEventsList()
        val targets = if (ids != null && ids.isNotEmpty()) {
            all.filter { ids.contains(it.id) }
        } else {
            all
        }
        return BackupRestoreManager.exportToJson(targets)
    }

    suspend fun batchExportCsv(ids: List<Long>? = null): String {
        val all = repository.getAllEventsList()
        val targets = if (ids != null && ids.isNotEmpty()) {
            all.filter { ids.contains(it.id) }
        } else {
            all
        }
        return BackupRestoreManager.exportToCsv(targets)
    }

    fun isSampleEvent(event: EventEntity): Boolean {
        return event.title in listOf("逢时 · 春风如期", "知己生辰", "相识相守") ||
               event.notes.contains("东风解冻，春山可望") ||
               event.notes.contains("岁岁常欢愉，万事皆胜意") ||
               event.notes.contains("与君初相识，犹如故人归")
    }

    fun hasSampleEvents(events: List<EventEntity>): Boolean {
        return events.any { isSampleEvent(it) }
    }

    fun countSampleEvents(events: List<EventEntity>): Int {
        return events.count { isSampleEvent(it) }
    }

    fun dismissSampleBanner() {
        prefs.edit().putBoolean("key_dismiss_sample_banner", true).apply()
        isSampleBannerDismissed.value = true
    }

    fun deleteSampleEvents(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val all = repository.getAllEventsList()
            val sampleEvents = all.filter { isSampleEvent(it) }
            for (sample in sampleEvents) {
                repository.deleteEvent(sample)
            }
            onComplete?.invoke()
        }
    }

    companion object {
        const val SORT_DAYS = "SORT_DAYS"
        const val SORT_CREATED = "SORT_CREATED"
        const val SORT_PINNED = "SORT_PINNED"
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = EventRepository(db.eventDao(), application)
        ThemeManager.init(application)
        LanguageManager.init(application)

        // 异步预加载持久化数据并标记完成，阻断冷启动白屏/闪烁
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val initialList = repository.getAllEventsList()
                com.example.widget.WidgetCacheManager.saveSnapshot(application, initialList)
            } catch (e: Throwable) {
                android.util.Log.e("EventViewModel", "Failed to load initial events or save widget snapshot", e)
            } finally {
                isInitialLoading.value = false
                isHydrated.value = true
            }
        }
    }

    val allEventsFlow = repository.allEvents

    /**
     * 核心视图模型流：在 Dispatchers.Default 后台预先换算天数与文本，列表项直接只读渲染
     */
    val uiEvents: StateFlow<List<EventUiItem>> = combine(
        allEventsFlow,
        selectedCategory,
        searchQuery,
        sortOrder
    ) { events, category, query, sort ->
        val today = LocalDate.now()
        isInitialLoading.value = false

        var filtered = events

        // 搜索过滤：多维度即时匹配（标题、备注/诗词、分类标签、公历/农历日期）
        if (query.isNotBlank()) {
            val q = query.trim()
            filtered = filtered.filter { event ->
                event.title.contains(q, ignoreCase = true) ||
                event.notes.contains(q, ignoreCase = true) ||
                event.category.contains(q, ignoreCase = true) ||
                event.getDisplayDateText().contains(q, ignoreCase = true) ||
                event.dateString.contains(q, ignoreCase = true) ||
                run {
                    val lunar = event.getOriginLunarDate()
                    lunar.monthChinese.contains(q, ignoreCase = true) ||
                    lunar.dayChinese.contains(q, ignoreCase = true) ||
                    "${lunar.monthChinese}${lunar.dayChinese}".contains(q, ignoreCase = true) ||
                    lunar.zodiac.contains(q, ignoreCase = true) ||
                    lunar.cyclicalYear.contains(q, ignoreCase = true)
                }
            }
        }

        // 分类过滤
        if (category != "全部") {
            filtered = if (category == "置顶") {
                filtered.filter { it.isPinned }
            } else {
                filtered.filter { it.category == category }
            }
        }

        // 预计算所有项的数据
        val precomputed = filtered.map { event ->
            val days = event.computeDays(today)
            val isToday = (days == 0 && event.repeatRule != EventEntity.REPEAT_NONE) ||
                    (event.repeatRule == EventEntity.REPEAT_NONE && event.getOriginSolarDate().isEqual(today))
            val isPastOneOff = event.repeatRule == EventEntity.REPEAT_NONE && today.isAfter(event.getOriginSolarDate())

            EventUiItem(
                event = event,
                daysLeft = days,
                statusDescription = event.getStatusDescription(today),
                displayDateText = event.getDisplayDateText(),
                celebrationSubText = event.getCelebrationSubText(),
                anniversaryRoundText = event.getAnniversaryRoundText(today),
                isToday = isToday,
                isPastOneOff = isPastOneOff
            )
        }

        // 严格遵循排序：默认 SORT_DAYS 统一按“临近剩余天数升序”排列，当天或最近的在最前
        when (sort) {
            SORT_DAYS -> {
                precomputed.sortedWith(
                    compareBy<EventUiItem> {
                        // 倒数中/临近的事件（含今天0天）排在最前 (0)；已过期且不重复的单次事件排在最后 (1)
                        if (it.isPastOneOff) 1 else 0
                    }.thenBy { it.daysLeft }
                )
            }
            SORT_CREATED -> {
                precomputed.sortedByDescending { it.event.createdAt }
            }
            SORT_PINNED -> {
                precomputed.sortedWith(
                    compareByDescending<EventUiItem> { it.event.isPinned }
                        .thenBy { if (it.isPastOneOff) 1 else 0 }
                        .thenBy { it.daysLeft }
                )
            }
            else -> precomputed.sortedWith(
                compareBy<EventUiItem> { if (it.isPastOneOff) 1 else 0 }.thenBy { it.daysLeft }
            )
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredEvents: StateFlow<List<EventEntity>> = uiEvents.map { list ->
        list.map { it.event }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun completeOnboarding() {
        prefs.edit().putBoolean("key_onboarding_completed", true).apply()
        isOnboardingCompleted.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("key_onboarding_completed", false).apply()
        isOnboardingCompleted.value = false
    }

    fun addEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun togglePin(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(isPinned = !event.isPinned))
        }
    }

    suspend fun getJsonExport(selectedList: List<EventEntity>? = null): String {
        val list = selectedList ?: repository.getAllEventsList()
        return BackupRestoreManager.exportToJson(list)
    }

    suspend fun getCsvExport(selectedList: List<EventEntity>? = null): String {
        val list = selectedList ?: repository.getAllEventsList()
        return BackupRestoreManager.exportToCsv(list)
    }

    fun importFromJson(jsonStr: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val parsed = BackupRestoreManager.parseFromJson(jsonStr)
                if (parsed.isNotEmpty()) {
                    repository.bulkInsert(parsed)
                    onSuccess(parsed.size)
                } else {
                    onError("未能解析出有效事件数据")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "JSON 格式解析失败")
            }
        }
    }

    fun importFromCsv(csvStr: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val parsed = BackupRestoreManager.parseFromCsv(csvStr)
                if (parsed.isNotEmpty()) {
                    repository.bulkInsert(parsed)
                    onSuccess(parsed.size)
                } else {
                    onError("未能从 CSV 解析出有效事件")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "CSV 格式解析失败")
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAll()
            onComplete()
        }
    }
}

