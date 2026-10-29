package com.example

import com.example.calendar.LunarCalendar
import com.example.calendar.PoetryProvider
import com.example.data.BackupRestoreManager
import com.example.data.EventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testLunarCalendarConversions() {
        // 2026-02-17 is Chinese New Year (正月初一) of 丙午年 (马年)
        val date1 = LocalDate.of(2026, 2, 17)
        val lunar1 = LunarCalendar.solarToLunar(date1)
        assertEquals("丙午年", lunar1.cyclicalYear)
        assertEquals("马", lunar1.zodiac)
        assertEquals("正月", lunar1.monthChinese)
        assertEquals("初一", lunar1.dayChinese)

        // 2024-02-10 was Year of the Dragon (甲辰) 正月初一
        val date2 = LocalDate.of(2024, 2, 10)
        val lunar2 = LunarCalendar.solarToLunar(date2)
        assertEquals("甲辰年", lunar2.cyclicalYear)
        assertEquals("龙", lunar2.zodiac)
        assertEquals("正月", lunar2.monthChinese)
        assertEquals("初一", lunar2.dayChinese)
    }

    @Test
    fun testEventAgeAndCountdownCalculations() {
        val today = LocalDate.of(2026, 6, 1)
        val birthdayEvent = EventEntity(
            title = "张三生日",
            dateString = "1998-08-15",
            isLunar = false,
            eventType = EventEntity.TYPE_BIRTHDAY
        )

        // Next birthday in 2026 is 2026-08-15 (75 days away from 2026-06-01)
        val daysUntil = birthdayEvent.computeDays(today)
        assertEquals(75, daysUntil)

        val ageInfo = LunarCalendar.calculateAgeInfo(birthdayEvent.dateString, false, today)
        assertEquals(27, ageInfo.currentAge)
        assertEquals(28, ageInfo.turningAge)
    }

    @Test
    fun testBackupRestoreJsonAndCsv() {
        val sampleEvents = listOf(
            EventEntity(
                id = 1,
                title = "立春",
                dateString = "2026-02-04",
                isLunar = false,
                eventType = EventEntity.TYPE_COUNTDOWN,
                category = "节日"
            )
        )

        val json = BackupRestoreManager.exportToJson(sampleEvents)
        val parsedJson = BackupRestoreManager.parseFromJson(json)
        assertEquals(1, parsedJson.size)
        assertEquals("立春", parsedJson[0].title)

        val csv = BackupRestoreManager.exportToCsv(sampleEvents)
        val parsedCsv = BackupRestoreManager.parseFromCsv(csv)
        assertEquals(1, parsedCsv.size)
        assertEquals("立春", parsedCsv[0].title)
    }

    @Test
    fun testPoetryProvider() {
        val poem = PoetryProvider.getRandomPoem(EventEntity.TYPE_BIRTHDAY)
        assertNotNull(poem.content)
        assertTrue(poem.content.isNotEmpty())
        assertNotNull(poem.source)
    }

    @Test
    fun testDualReminderSystemFields() {
        val eventWithReminders = EventEntity(
            id = 42,
            title = "中秋节",
            dateString = "2026-09-25",
            isLunar = true,
            eventType = EventEntity.TYPE_COUNTDOWN,
            category = "节日",
            reminderMode = EventEntity.REMINDER_BEFORE_1,
            reminderTime = "08:30",
            isPersistentNotification = true,
            isAlarmEnabled = true
        )

        // Verify JSON serialization includes new reminder settings
        val json = BackupRestoreManager.exportToJson(listOf(eventWithReminders))
        val parsed = BackupRestoreManager.parseFromJson(json)
        assertEquals(1, parsed.size)
        assertEquals("08:30", parsed[0].reminderTime)
        assertTrue(parsed[0].isPersistentNotification)
        assertTrue(parsed[0].isAlarmEnabled)
        assertTrue(parsed[0].isLunar)

        // Verify traditional Chinese lunar day naming (no Roman numerals)
        for (i in 1..30) {
            val dayName = LunarCalendar.LUNAR_DAYS.getOrNull(i - 1)
            assertNotNull(dayName)
            // Ensure pure Chinese characters: 初一~初十, 十一~十九, 二十, 廿一~廿九, 三十
            assertTrue(dayName!!.matches(Regex("^(初[一二三四五六七八九十]|十[一二三四五六七八九]|二十|廿[一二三四五六七八九]|三十)$")))
        }
    }
}
