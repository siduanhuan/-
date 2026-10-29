package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.StringReader

/**
 * 逢时 - 本地优先数据备份与批量导入导出工具
 * 支持标准全量 JSON、Excel 友好型 CSV 以及面向 AI 智能识别的标准提示词模板
 */
object BackupRestoreManager {

    /**
     * 供用户复制给任意外部 LLM (如 ChatGPT, Gemini, Claude, DeepSeek) 的标准化提示词
     */
    const val AI_SYSTEM_PROMPT_TEMPLATE = """# 角色设定
你是一个专业的日期转换与结构化数据提取助手，专门为「逢时」App 批量生成符合导入规范的吉日与倒数日数据。

# 任务目标
请仔细阅读用户提供的杂乱文本或事件清单，精确识别每个事件的：
1. 事件名称（title）
2. 日期（公历 YYYY-MM-DD 或农历年月日）
3. 录入历法（calendarType: SOLAR 公历 / LUNAR 农历）
4. 庆生或纪念方式（celebrateBy: SOLAR 按公历过 / LUNAR 按农历过）
5. 循环规则（repeatRule: ANNUAL 每年循环 / NONE 仅倒数一次）
6. 事件类型（eventType: BIRTHDAY 生日 / ANNIVERSARY 纪念日 / COUNTDOWN 倒数节日 / TARGET 长远目标）
7. 分类（category）及备注（notes）

# 输出格式规范 (JSON 格式)
请直接输出纯 JSON 代码块（不要包含其他废话）：
{
  "app": "逢时",
  "version": "2.0",
  "events": [
    {
      "title": "妈妈生日",
      "dateString": "1972-10-15",
      "calendarType": "LUNAR",
      "celebrateBy": "LUNAR",
      "repeatRule": "ANNUAL",
      "eventType": "BIRTHDAY",
      "category": "生日",
      "lunarParams": {
        "lunarYear": 1972,
        "lunarMonth": 9,
        "lunarDay": 9,
        "isLeapMonth": false
      },
      "reminderMode": "BEFORE_1_DAY",
      "notes": "每年九月初九重阳节生日"
    },
    {
      "title": "结婚纪念日",
      "dateString": "2020-05-20",
      "calendarType": "SOLAR",
      "celebrateBy": "SOLAR",
      "repeatRule": "ANNUAL",
      "eventType": "ANNIVERSARY",
      "category": "纪念日",
      "reminderMode": "DAY_OF",
      "notes": "相伴到老"
    }
  ]
}"""

    const val SAMPLE_JSON_TEMPLATE = """{
  "app": "逢时",
  "version": "2.0",
  "events": [
    {
      "title": "好友生辰",
      "dateString": "2000-08-15",
      "calendarType": "LUNAR",
      "celebrateBy": "LUNAR",
      "repeatRule": "ANNUAL",
      "eventType": "BIRTHDAY",
      "category": "生日",
      "lunarParams": {
        "lunarYear": 2000,
        "lunarMonth": 7,
        "lunarDay": 16,
        "isLeapMonth": false
      },
      "reminderMode": "BEFORE_1_DAY",
      "reminderTime": "09:00",
      "isPinned": false,
      "notes": "岁岁常欢愉，万事皆胜意"
    },
    {
      "title": "恋爱纪念日",
      "dateString": "2022-09-20",
      "calendarType": "SOLAR",
      "celebrateBy": "SOLAR",
      "repeatRule": "ANNUAL",
      "eventType": "ANNIVERSARY",
      "category": "纪念日",
      "reminderMode": "DAY_OF",
      "reminderTime": "09:00",
      "isPinned": true,
      "notes": "与君初相识，犹如故人归"
    }
  ]
}"""

    const val SAMPLE_CSV_TEMPLATE = """标题,日期,录入历法,庆生规则,重复规则,事件类型,分类,是否置顶,提醒设置,备注
好友生辰,2000-08-15,农历,按农历过,每年循环,生日,生日,否,BEFORE_1_DAY,岁岁常欢愉
恋爱纪念日,2022-09-20,公历,按公历过,每年循环,纪念日,纪念日,是,DAY_OF,与君初相识
2026年高考,2026-06-07,公历,按公历过,仅倒数一次,长远目标,学业,是,BEFORE_7_DAYS,直挂云帆济沧海"""

    /**
     * 导出全量数据为格式化 JSON 字符串
     */
    fun exportToJson(events: List<EventEntity>): String {
        val root = JSONObject()
        root.put("app", "逢时")
        root.put("version", "2.0")
        root.put("exportTimestamp", System.currentTimeMillis())

        val array = JSONArray()
        for (e in events) {
            val item = JSONObject()
            item.put("title", e.title)
            item.put("dateString", e.dateString)
            val isLunarFlag = e.isRecordedLunar() || e.isLunar
            item.put("isLunar", isLunarFlag)
            item.put("calendarType", if (isLunarFlag && e.calendarType == EventEntity.CALENDAR_SOLAR) EventEntity.CALENDAR_LUNAR else e.calendarType)
            item.put("celebrateBy", e.celebrateBy)
            item.put("repeatRule", e.repeatRule)
            item.put("eventType", e.eventType)
            item.put("category", e.category)
            item.put("categoryColorHex", e.categoryColorHex)
            item.put("categoryIcon", e.categoryIcon)
            item.put("isPinned", e.isPinned)
            item.put("reminderMode", e.reminderMode)
            item.put("reminderTime", e.reminderTime)
            item.put("isPersistentNotification", e.isPersistentNotification)
            item.put("isAlarmEnabled", e.isAlarmEnabled)
            item.put("notes", e.notes)
            item.put("createdAt", e.createdAt)

            if (e.isRecordedLunar() || e.lunarMonth > 0) {
                val lunarObj = JSONObject()
                lunarObj.put("lunarYear", e.lunarYear)
                lunarObj.put("lunarMonth", e.lunarMonth)
                lunarObj.put("lunarDay", e.lunarDay)
                lunarObj.put("isLeapMonth", e.isLeapMonth)
                item.put("lunarParams", lunarObj)
            }

            array.put(item)
        }
        root.put("events", array)
        return root.toString(2)
    }

    /**
     * 从 JSON 字符串解析事件列表
     */
    fun parseFromJson(jsonStr: String): List<EventEntity> {
        val result = mutableListOf<EventEntity>()
        val trimmed = jsonStr.trim()
        val array = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            val root = JSONObject(trimmed)
            if (root.has("events")) root.getJSONArray("events") else JSONArray().put(root)
        }

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val isLunarLegacy = item.optBoolean("isLunar", false)
            val calType = item.optString("calendarType", if (isLunarLegacy) EventEntity.CALENDAR_LUNAR else EventEntity.CALENDAR_SOLAR)
            val celBy = item.optString("celebrateBy", if (calType == EventEntity.CALENDAR_LUNAR) EventEntity.CELEBRATE_LUNAR else EventEntity.CELEBRATE_SOLAR)
            val repRule = item.optString("repeatRule", EventEntity.REPEAT_ANNUAL)

            var lYear = 0
            var lMonth = 0
            var lDay = 0
            var isLeap = false

            if (item.has("lunarParams")) {
                val lp = item.getJSONObject("lunarParams")
                lYear = lp.optInt("lunarYear", 0)
                lMonth = lp.optInt("lunarMonth", 0)
                lDay = lp.optInt("lunarDay", 0)
                isLeap = lp.optBoolean("isLeapMonth", false)
            } else {
                lYear = item.optInt("lunarYear", 0)
                lMonth = item.optInt("lunarMonth", 0)
                lDay = item.optInt("lunarDay", 0)
                isLeap = item.optBoolean("isLeapMonth", false)
            }

            result.add(
                EventEntity(
                    id = 0,
                    title = item.optString("title", "未命名"),
                    dateString = item.optString("dateString", "2026-01-01"),
                    isLunar = calType == EventEntity.CALENDAR_LUNAR || isLunarLegacy,
                    calendarType = calType,
                    celebrateBy = celBy,
                    lunarYear = lYear,
                    lunarMonth = lMonth,
                    lunarDay = lDay,
                    isLeapMonth = isLeap,
                    repeatRule = repRule,
                    eventType = item.optString("eventType", EventEntity.TYPE_COUNTDOWN),
                    category = item.optString("category", "节日"),
                    categoryColorHex = item.optLong("categoryColorHex", 0xFFD4AF37),
                    categoryIcon = item.optString("categoryIcon", "calendar"),
                    isPinned = item.optBoolean("isPinned", false),
                    reminderMode = item.optString("reminderMode", EventEntity.REMINDER_NONE),
                    reminderTime = item.optString("reminderTime", "09:00"),
                    isPersistentNotification = item.optBoolean("isPersistentNotification", false),
                    isAlarmEnabled = item.optBoolean("isAlarmEnabled", false),
                    notes = item.optString("notes", ""),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return result
    }

    /**
     * 导出为标准 CSV 格式 (UTF-8)
     */
    fun exportToCsv(events: List<EventEntity>): String {
        val sb = StringBuilder()
        sb.append("标题,日期,录入历法,庆生规则,重复规则,事件类型,分类,是否置顶,提醒设置,备注\n")
        for (e in events) {
            val safeTitle = escapeCsv(e.title)
            val calType = if (e.isRecordedLunar()) "农历" else "公历"
            val celBy = if (e.celebrateBy == EventEntity.CELEBRATE_LUNAR) "按农历过" else "按公历过"
            val repRule = if (e.repeatRule == EventEntity.REPEAT_ANNUAL) "每年循环" else "仅倒数一次"
            val typeDesc = when (e.eventType) {
                EventEntity.TYPE_BIRTHDAY -> "生日"
                EventEntity.TYPE_ANNIVERSARY -> "纪念日"
                EventEntity.TYPE_TARGET -> "长远目标"
                else -> "倒数节日"
            }
            val pinned = if (e.isPinned) "是" else "否"
            val safeNotes = escapeCsv(e.notes)
            sb.append("$safeTitle,${e.dateString},$calType,$celBy,$repRule,$typeDesc,${e.category},$pinned,${e.reminderMode},$safeNotes\n")
        }
        return sb.toString()
    }

    /**
     * 解析 CSV 内容为实体列表
     */
    fun parseFromCsv(csvContent: String): List<EventEntity> {
        val result = mutableListOf<EventEntity>()
        val reader = BufferedReader(StringReader(csvContent))
        var lineIndex = 0
        var line = reader.readLine()
        while (line != null) {
            lineIndex++
            if (lineIndex == 1 && (line.contains("标题") || line.contains("title"))) {
                line = reader.readLine()
                continue
            }
            val tokens = parseCsvLine(line)
            if (tokens.isNotEmpty()) {
                val title = tokens.getOrElse(0) { "" }.trim()
                if (title.isNotEmpty()) {
                    val date = tokens.getOrElse(1) { "2026-01-01" }.trim()
                    val calStr = tokens.getOrElse(2) { "公历" }
                    val isLunar = calStr.contains("农历") || calStr.contains("阴历")
                    val calType = if (isLunar) EventEntity.CALENDAR_LUNAR else EventEntity.CALENDAR_SOLAR
                    val celStr = tokens.getOrElse(3) { if (isLunar) "按农历过" else "按公历过" }
                    val celBy = if (celStr.contains("农历")) EventEntity.CELEBRATE_LUNAR else EventEntity.CELEBRATE_SOLAR
                    val repStr = tokens.getOrElse(4) { "每年循环" }
                    val repRule = if (repStr.contains("一次") || repStr.contains("NONE")) EventEntity.REPEAT_NONE else EventEntity.REPEAT_ANNUAL
                    val typeStr = tokens.getOrElse(5) { "倒数" }
                    val eventType = when {
                        typeStr.contains("生") -> EventEntity.TYPE_BIRTHDAY
                        typeStr.contains("纪念") -> EventEntity.TYPE_ANNIVERSARY
                        typeStr.contains("目标") -> EventEntity.TYPE_TARGET
                        else -> EventEntity.TYPE_COUNTDOWN
                    }
                    val category = tokens.getOrElse(6) { "节日" }.trim().ifEmpty { "节日" }
                    val pinned = tokens.getOrElse(7) { "否" }.contains("是")
                    val reminder = tokens.getOrElse(8) { EventEntity.REMINDER_NONE }.trim()
                    val notes = tokens.getOrElse(9) { "" }.trim()

                    result.add(
                        EventEntity(
                            id = 0,
                            title = title,
                            dateString = date,
                            isLunar = isLunar,
                            calendarType = calType,
                            celebrateBy = celBy,
                            repeatRule = repRule,
                            eventType = eventType,
                            category = category,
                            isPinned = pinned,
                            reminderMode = reminder,
                            notes = notes
                        )
                    )
                }
            }
            line = reader.readLine()
        }
        return result
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString())
        return tokens
    }
}

