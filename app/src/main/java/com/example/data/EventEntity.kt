package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.calendar.LunarCalendar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 逢时 - 核心实体模型 (Room Entity)
 * 深度解耦：支持录入历法 (SOLAR/LUNAR)、庆生/纪念规则 (SOLAR/LUNAR)、重复规则 (NONE/ANNUAL) 及原生农历存储。
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dateString: String = "", // 原始公历日期字符串 "YYYY-MM-DD"
    val isLunar: Boolean = false, // 兼容旧版标记 (true 等价于 calendarType == CALENDAR_LUNAR)
    val calendarType: String = CALENDAR_SOLAR, // 录入历法：CALENDAR_SOLAR 或 CALENDAR_LUNAR
    val celebrateBy: String = CELEBRATE_SOLAR,  // 庆生/纪念依照：CELEBRATE_SOLAR 或 CELEBRATE_LUNAR
    val lunarYear: Int = 0,                    // 原生农历年份 (若录入农历，非0)
    val lunarMonth: Int = 0,                   // 原生农历月份 1..12
    val lunarDay: Int = 0,                     // 原生农历日期 1..30
    val isLeapMonth: Boolean = false,          // 是否为闰月
    val repeatRule: String = REPEAT_ANNUAL,    // 重复规则：REPEAT_ANNUAL (每年循环) 或 REPEAT_NONE (单次目标)
    val eventType: String = TYPE_COUNTDOWN,    // BIRTHDAY, COUNTDOWN, ANNIVERSARY, TARGET
    val category: String = "节日",
    val categoryColorHex: Long = 0xFFD4AF37,
    val categoryIcon: String = "calendar",
    val isPinned: Boolean = false,
    val reminderMode: String = REMINDER_NONE,  // NONE, DAY_OF, BEFORE_1_DAY, BEFORE_2_DAYS, etc.
    val reminderTime: String = "09:00",        // "HH:mm" e.g. "09:00", "08:30"
    val isPersistentNotification: Boolean = false, // 是否未手动确认前常驻通知栏
    val isAlarmEnabled: Boolean = false,       // 是否开启独立闹钟响铃系统
    val notes: String = "",
    val customBgUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_BIRTHDAY = "BIRTHDAY"
        const val TYPE_COUNTDOWN = "COUNTDOWN"
        const val TYPE_ANNIVERSARY = "ANNIVERSARY"
        const val TYPE_TARGET = "TARGET"

        const val CALENDAR_SOLAR = "SOLAR"
        const val CALENDAR_LUNAR = "LUNAR"

        const val CELEBRATE_SOLAR = "SOLAR"
        const val CELEBRATE_LUNAR = "LUNAR"

        const val REPEAT_ANNUAL = "ANNUAL"
        const val REPEAT_NONE = "NONE"

        const val REMINDER_NONE = "NONE"
        const val REMINDER_DAY_OF = "DAY_OF"
        const val REMINDER_BEFORE_1 = "BEFORE_1_DAY"
        const val REMINDER_BEFORE_2 = "BEFORE_2_DAYS"
        const val REMINDER_BEFORE_3 = "BEFORE_3_DAYS"
        const val REMINDER_BEFORE_7 = "BEFORE_7_DAYS"
        const val REMINDER_BEFORE_15 = "BEFORE_15_DAYS"
        const val REMINDER_BEFORE_30 = "BEFORE_30_DAYS"
    }

    /**
     * 判断是否属于农历录入
     */
    fun isRecordedLunar(): Boolean {
        return calendarType == CALENDAR_LUNAR || isLunar
    }

    /**
     * 获取事件起点的真实公历日期
     */
    fun getOriginSolarDate(): LocalDate {
        return if (isRecordedLunar() && lunarYear > 0 && lunarMonth > 0 && lunarDay > 0) {
            try {
                LunarCalendar.lunarToSolar(lunarYear, lunarMonth, lunarDay, isLeapMonth)
            } catch (e: Exception) {
                parseDateStringOrNow()
            }
        } else {
            parseDateStringOrNow()
        }
    }

    /**
     * 获取事件起点的农历信息 (包含干支生肖)
     */
    fun getOriginLunarDate(): LunarCalendar.LunarDate {
        return if (isRecordedLunar() && lunarYear > 0 && lunarMonth > 0 && lunarDay > 0) {
            val cyclical = LunarCalendar.getCyclicalYear(lunarYear)
            val zodiac = LunarCalendar.getZodiac(lunarYear)
            val mStr = if (isLeapMonth) "闰${LunarCalendar.LUNAR_MONTHS.getOrElse(lunarMonth - 1) { "${lunarMonth}月" }}"
                       else LunarCalendar.LUNAR_MONTHS.getOrElse(lunarMonth - 1) { "${lunarMonth}月" }
            val dStr = LunarCalendar.LUNAR_DAYS.getOrElse(lunarDay - 1) { "${lunarDay}日" }
            LunarCalendar.LunarDate(
                year = lunarYear,
                month = lunarMonth,
                day = lunarDay,
                isLeapMonth = isLeapMonth,
                cyclicalYear = cyclical,
                zodiac = zodiac,
                monthChinese = mStr,
                dayChinese = dStr
            )
        } else {
            LunarCalendar.solarToLunar(getOriginSolarDate())
        }
    }

    private fun parseDateStringOrNow(): LocalDate {
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    /**
     * 计算该事件下一次发生或目标基准的公历日期（基于解耦规则）
     */
    fun getTargetSolarDate(today: LocalDate = LocalDate.now()): LocalDate {
        val origSolar = getOriginSolarDate()

        // 单次目标事件 (非每年重复)
        if (repeatRule == REPEAT_NONE) {
            return origSolar
        }

        // 每年循环类事件 (节日、生日、纪念日)
        return if (celebrateBy == CELEBRATE_LUNAR) {
            // 按农历规则追踪下一次对应的公历日期
            val origLunar = getOriginLunarDate()
            val curLunarYear = LunarCalendar.solarToLunar(today).year
            val thisYearSolar = try {
                LunarCalendar.lunarToSolar(curLunarYear, origLunar.month, origLunar.day, false)
            } catch (e: Exception) {
                today
            }

            if (thisYearSolar.isBefore(today)) {
                try {
                    LunarCalendar.lunarToSolar(curLunarYear + 1, origLunar.month, origLunar.day, false)
                } catch (e: Exception) {
                    thisYearSolar.plusYears(1)
                }
            } else {
                thisYearSolar
            }
        } else {
            // 按公历月日追踪下一次发生日期
            val thisYearSolar = try {
                LocalDate.of(today.year, origSolar.monthValue, origSolar.dayOfMonth)
            } catch (e: Exception) {
                LocalDate.of(today.year, 2, 28) // 处理闰年2月29日
            }

            if (thisYearSolar.isBefore(today)) {
                try {
                    LocalDate.of(today.year + 1, origSolar.monthValue, origSolar.dayOfMonth)
                } catch (e: Exception) {
                    LocalDate.of(today.year + 1, 2, 28)
                }
            } else {
                thisYearSolar
            }
        }
    }

    /**
     * 计算动态天数：
     * - 每年重复事件 (生日/节日/纪念日)：距离下一次发生日的剩余天数 (>= 0)
     * - 单次目标 (REPEAT_NONE)：未来事件倒数 (>= 0)，过去事件已过天数 (>= 0)
     * - 纪念日已过累计天数可由 getAnniversaryTotalDays() 获取
     */
    fun computeDays(today: LocalDate = LocalDate.now()): Int {
        val origSolar = getOriginSolarDate()

        if (repeatRule == REPEAT_NONE) {
            return if (today.isBefore(origSolar)) {
                ChronoUnit.DAYS.between(today, origSolar).toInt()
            } else if (today.isEqual(origSolar)) {
                0
            } else {
                ChronoUnit.DAYS.between(origSolar, today).toInt()
            }
        }

        val targetSolar = getTargetSolarDate(today)
        val diff = ChronoUnit.DAYS.between(today, targetSolar).toInt()
        return if (diff < 0) 0 else diff
    }

    /**
     * 纪念日：计算自起始日起累计度过的总天数
     */
    fun getAnniversaryTotalDays(today: LocalDate = LocalDate.now()): Int {
        val origSolar = getOriginSolarDate()
        val diff = ChronoUnit.DAYS.between(origSolar, today).toInt()
        return diff.coerceAtLeast(0)
    }

    /**
     * 周年 / 华诞轮次描述 (如 "距 5 周年纪念日", "将迎来 22 周岁华诞")
     */
    fun getAnniversaryRoundText(today: LocalDate = LocalDate.now()): String? {
        val origSolar = getOriginSolarDate()
        val nextTarget = getTargetSolarDate(today)

        return when (eventType) {
            TYPE_ANNIVERSARY -> {
                val years = nextTarget.year - origSolar.year
                if (years > 0) "距 $years 周年纪念" else "距首次周年"
            }
            TYPE_BIRTHDAY -> {
                val turningAge = (nextTarget.year - origSolar.year).coerceAtLeast(1)
                val currentAge = (turningAge - (if (today.isEqual(nextTarget)) 0 else 1)).coerceAtLeast(0)
                if (today.isEqual(nextTarget)) {
                    "今日满 $currentAge 周岁华诞"
                } else {
                    "将满 $turningAge 周岁华诞"
                }
            }
            else -> null
        }
    }

    /**
     * 极简状态主描述文案
     */
    fun getStatusDescription(today: LocalDate = LocalDate.now()): String {
        val origSolar = getOriginSolarDate()

        if (repeatRule == REPEAT_NONE) {
            return when {
                today.isBefore(origSolar) -> "距目标吉日还有"
                today.isEqual(origSolar) -> "正是良辰今日"
                else -> "目标吉日已过去"
            }
        }

        val days = computeDays(today)
        if (days == 0) return "正是良辰今日"

        return when (eventType) {
            TYPE_ANNIVERSARY -> {
                val totalDays = getAnniversaryTotalDays(today)
                "已携手相伴 $totalDays 天"
            }
            TYPE_BIRTHDAY -> {
                val origLunar = getOriginLunarDate()
                if (celebrateBy == CELEBRATE_LUNAR) {
                    "距下个农历生辰"
                } else {
                    "距下个公历生辰"
                }
            }
            else -> "距今还有"
        }
    }

    /**
     * 庆生方式与日期展示辅助文案 (如 "农历七月二十 · 按农历过")
     */
    fun getCelebrationSubText(): String? {
        val origLunar = getOriginLunarDate()
        val origSolar = getOriginSolarDate()

        return when (eventType) {
            TYPE_BIRTHDAY -> {
                if (celebrateBy == CELEBRATE_LUNAR) {
                    "依农历庆生 · ${origLunar.monthChinese}${origLunar.dayChinese}"
                } else {
                    "依公历庆生 · ${origSolar.monthValue}月${origSolar.dayOfMonth}日"
                }
            }
            TYPE_ANNIVERSARY -> {
                if (celebrateBy == CELEBRATE_LUNAR) {
                    "每年农历 ${origLunar.monthChinese}${origLunar.dayChinese} 纪念"
                } else {
                    "每年公历 ${origSolar.monthValue}月${origSolar.dayOfMonth}日 纪念"
                }
            }
            else -> null
        }
    }

    /**
     * 原始日期的传统/公历格式化展示
     */
    fun getDisplayDateText(): String {
        val origSolar = getOriginSolarDate()
        val origLunar = getOriginLunarDate()

        return if (isRecordedLunar()) {
            "农历 ${origLunar.cyclicalYear}(${origLunar.zodiac}) ${origLunar.monthChinese}${origLunar.dayChinese}"
        } else {
            "公历 ${origSolar.year}年${origSolar.monthValue}月${origSolar.dayOfMonth}日"
        }
    }
}

