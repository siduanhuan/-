package com.example.calendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 逢时 - 百年公历/农历离线精准转换引擎 (1900 - 2100)
 * 采用国家天文台基准农历历表二进制编码数据，支持闰月、天干地支、生肖与动态周岁计算。
 */
object LunarCalendar {

    // 1900 - 2100 百年农历基准数据 (201年)
    // 编码结构：
    // bit 16: 闰月大小月（1为大月30天，0为小月29天）
    // bit 15..4: 1~12月大小月（1为大月30天，0为小月29天，bit 15为正月）
    // bit 3..0: 闰月月份（0表示无闰月，1..12表示闰几月）
    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d260,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252,
        0x0d520
    )

    // 天干
    val HEAVENLY_STEMS = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    // 地支
    val EARTHLY_BRANCHES = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    // 生肖
    val ZODIAC = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    val LUNAR_MONTHS = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    val LUNAR_DAYS = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean,
        val cyclicalYear: String, // 干支年，如 乙巳年
        val zodiac: String,       // 生肖，如 蛇
        val monthChinese: String, // 如 润六月 或 正月
        val dayChinese: String    // 如 初五
    ) {
        fun toDisplayString(): String = "$cyclicalYear($zodiac) $monthChinese$dayChinese"
    }

    /**
     * 获取农历年总天数
     */
    fun getLunarYearDays(year: Int): Int {
        if (year < 1900 || year > 2100) return 365
        val info = LUNAR_INFO[year - 1900]
        var total = 0
        for (m in 1..12) {
            val bit = 0x8000 shr (m - 1)
            total += if ((info and bit) != 0) 30 else 29
        }
        val leapMonth = getLeapMonth(year)
        if (leapMonth > 0) {
            total += if ((info and 0x10000) != 0) 30 else 29
        }
        return total
    }

    /**
     * 获取农历年闰月月份（0为无闰月，1..12）
     */
    fun getLeapMonth(year: Int): Int {
        if (year < 1900 || year > 2100) return 0
        return LUNAR_INFO[year - 1900] and 0x0f
    }

    /**
     * 获取某年某月农历天数 (month: 1..12, isLeap: 是否闰月)
     */
    fun getLunarMonthDays(year: Int, month: Int, isLeap: Boolean = false): Int {
        if (year < 1900 || year > 2100) return 30
        val info = LUNAR_INFO[year - 1900]
        return if (isLeap) {
            if ((info and 0x10000) != 0) 30 else 29
        } else {
            val bit = 0x8000 shr (month - 1)
            if ((info and bit) != 0) 30 else 29
        }
    }

    /**
     * 公历转农历
     */
    fun solarToLunar(solarDate: LocalDate): LunarDate {
        val baseDate = LocalDate.of(1900, 1, 31) // 1900年正月初一公历基准
        var offset = ChronoUnit.DAYS.between(baseDate, solarDate).toInt()

        if (offset < 0) {
            // 早于基准日时兜底返回
            return LunarDate(
                year = solarDate.year,
                month = solarDate.monthValue,
                day = solarDate.dayOfMonth,
                isLeapMonth = false,
                cyclicalYear = getCyclicalYear(solarDate.year),
                zodiac = getZodiac(solarDate.year),
                monthChinese = LUNAR_MONTHS[solarDate.monthValue - 1],
                dayChinese = LUNAR_DAYS[solarDate.dayOfMonth - 1]
            )
        }

        var year = 1900
        var daysInYear = 0
        while (year <= 2100 && offset > 0) {
            daysInYear = getLunarYearDays(year)
            if (offset >= daysInYear) {
                offset -= daysInYear
                year++
            } else {
                break
            }
        }

        val leapMonth = getLeapMonth(year)
        var isLeap = false
        var month = 1

        for (m in 1..12) {
            // 常规月份
            val daysInMonth = getLunarMonthDays(year, m, false)
            if (offset < daysInMonth) {
                month = m
                isLeap = false
                break
            }
            offset -= daysInMonth

            // 闰月
            if (leapMonth > 0 && leapMonth == m) {
                val daysInLeap = getLunarMonthDays(year, m, true)
                if (offset < daysInLeap) {
                    month = m
                    isLeap = true
                    break
                }
                offset -= daysInLeap
            }
        }

        val day = offset + 1
        val cyclicalYear = getCyclicalYear(year)
        val zodiac = getZodiac(year)
        val monthChinese = (if (isLeap) "闰" else "") + LUNAR_MONTHS.getOrElse(month - 1) { "${month}月" }
        val dayChinese = LUNAR_DAYS.getOrElse(day - 1) { "${day}日" }

        return LunarDate(
            year = year,
            month = month,
            day = day,
            isLeapMonth = isLeap,
            cyclicalYear = cyclicalYear,
            zodiac = zodiac,
            monthChinese = monthChinese,
            dayChinese = dayChinese
        )
    }

    /**
     * 农历转公历
     */
    fun lunarToSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int, isLeap: Boolean = false): LocalDate {
        if (lunarYear < 1900 || lunarYear > 2100) return LocalDate.of(lunarYear, lunarMonth.coerceIn(1, 12), lunarDay.coerceIn(1, 28))

        var days = 0
        // 累加1900到目标年份前一年的天数
        for (y in 1900 until lunarYear) {
            days += getLunarYearDays(y)
        }

        val leapMonth = getLeapMonth(lunarYear)
        for (m in 1 until lunarMonth) {
            days += getLunarMonthDays(lunarYear, m, false)
            if (leapMonth > 0 && leapMonth == m) {
                days += getLunarMonthDays(lunarYear, m, true)
            }
        }

        if (isLeap && leapMonth == lunarMonth) {
            // 如果计算的是闰月，还需加上当前正常月份天数
            days += getLunarMonthDays(lunarYear, lunarMonth, false)
        }

        days += (lunarDay - 1)
        val baseDate = LocalDate.of(1900, 1, 31)
        return baseDate.plusDays(days.toLong())
    }

    /**
     * 计算天干地支年 (如 丙午年)
     */
    fun getCyclicalYear(year: Int): String {
        val num = year - 1900 + 36
        val stem = HEAVENLY_STEMS[num % 10]
        val branch = EARTHLY_BRANCHES[num % 12]
        return "${stem}${branch}年"
    }

    /**
     * 计算生肖 (如 蛇)
     */
    fun getZodiac(year: Int): String {
        return ZODIAC[(year - 4) % 12]
    }

    /**
     * 计算下一次发生日期（支持公历/农历重复事件）
     */
    fun getNextTargetDate(
        dateString: String,
        isLunar: Boolean,
        today: LocalDate = LocalDate.now()
    ): LocalDate {
        val originalDate = try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            return today
        }

        if (!isLunar) {
            // 公历重复事件（如生日、节日）
            val thisYearTarget = try {
                LocalDate.of(today.year, originalDate.monthValue, originalDate.dayOfMonth)
            } catch (e: Exception) {
                // 针对2月29日闰日特殊处理
                LocalDate.of(today.year, 2, 28)
            }

            return if (thisYearTarget.isBefore(today)) {
                try {
                    LocalDate.of(today.year + 1, originalDate.monthValue, originalDate.dayOfMonth)
                } catch (e: Exception) {
                    LocalDate.of(today.year + 1, 2, 28)
                }
            } else {
                thisYearTarget
            }
        } else {
            // 农历生日或节日
            val originalLunar = solarToLunar(originalDate)
            val thisYearSolar = lunarToSolar(
                lunarYear = today.year,
                lunarMonth = originalLunar.month,
                lunarDay = originalLunar.day,
                isLeap = false
            )

            return if (thisYearSolar.isBefore(today)) {
                lunarToSolar(
                    lunarYear = today.year + 1,
                    lunarMonth = originalLunar.month,
                    lunarDay = originalLunar.day,
                    isLeap = false
                )
            } else {
                thisYearSolar
            }
        }
    }

    /**
     * 生日年龄分析结构
     */
    data class AgeInfo(
        val currentAge: Int,      // 当前周岁
        val turningAge: Int,      // 即将迎来的周岁
        val daysUntilBirthday: Int, // 距下一次生日天数
        val nextBirthdayDate: LocalDate
    )

    /**
     * 计算周岁与下一个生日
     */
    fun calculateAgeInfo(
        birthDateString: String,
        isLunar: Boolean,
        today: LocalDate = LocalDate.now()
    ): AgeInfo {
        val birthDate = try {
            LocalDate.parse(birthDateString)
        } catch (e: Exception) {
            today
        }

        val nextBirthday = getNextTargetDate(birthDateString, isLunar, today)
        val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday).toInt()

        val currentAge = if (today.monthValue > birthDate.monthValue ||
            (today.monthValue == birthDate.monthValue && today.dayOfMonth >= birthDate.dayOfMonth)) {
            today.year - birthDate.year
        } else {
            (today.year - birthDate.year - 1).coerceAtLeast(0)
        }

        val turningAge = currentAge + (if (daysUntil == 0) 0 else 1)

        return AgeInfo(
            currentAge = currentAge,
            turningAge = turningAge,
            daysUntilBirthday = daysUntil,
            nextBirthdayDate = nextBirthday
        )
    }
}
