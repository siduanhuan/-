package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(entities = [EventEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN reminderTime TEXT NOT NULL DEFAULT '09:00'")
                db.execSQL("ALTER TABLE events ADD COLUMN isPersistentNotification INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN isAlarmEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN calendarType TEXT NOT NULL DEFAULT 'SOLAR'")
                db.execSQL("ALTER TABLE events ADD COLUMN celebrateBy TEXT NOT NULL DEFAULT 'SOLAR'")
                db.execSQL("ALTER TABLE events ADD COLUMN lunarYear INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN lunarMonth INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN lunarDay INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN isLeapMonth INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN repeatRule TEXT NOT NULL DEFAULT 'ANNUAL'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fengshi_local.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 初次建库初始化示例数据
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).eventDao()
                            val now = LocalDate.now()
                            dao.insertAll(
                                listOf(
                                    EventEntity(
                                        title = "逢时 · 春风如期",
                                        dateString = "${now.year}-02-04",
                                        isLunar = false,
                                        calendarType = EventEntity.CALENDAR_SOLAR,
                                        celebrateBy = EventEntity.CELEBRATE_SOLAR,
                                        repeatRule = EventEntity.REPEAT_ANNUAL,
                                        eventType = EventEntity.TYPE_COUNTDOWN,
                                        category = "节日",
                                        categoryColorHex = 0xFFD4AF37,
                                        isPinned = true,
                                        reminderMode = EventEntity.REMINDER_DAY_OF,
                                        notes = "东风解冻，春山可望。愿君所遇皆良辰。"
                                    ),
                                    EventEntity(
                                        title = "知己生辰",
                                        dateString = "${now.year - 24}-09-15",
                                        isLunar = true,
                                        calendarType = EventEntity.CALENDAR_LUNAR,
                                        celebrateBy = EventEntity.CELEBRATE_LUNAR,
                                        lunarYear = now.year - 24,
                                        lunarMonth = 8,
                                        lunarDay = 15,
                                        repeatRule = EventEntity.REPEAT_ANNUAL,
                                        eventType = EventEntity.TYPE_BIRTHDAY,
                                        category = "生日",
                                        categoryColorHex = 0xFFC24D43,
                                        isPinned = false,
                                        reminderMode = EventEntity.REMINDER_BEFORE_1,
                                        notes = "农历八月十五月圆之期，岁岁常欢愉，万事皆胜意。"
                                    ),
                                    EventEntity(
                                        title = "相识相守",
                                        dateString = "${now.year - 1}-05-20",
                                        isLunar = false,
                                        calendarType = EventEntity.CALENDAR_SOLAR,
                                        celebrateBy = EventEntity.CELEBRATE_SOLAR,
                                        repeatRule = EventEntity.REPEAT_ANNUAL,
                                        eventType = EventEntity.TYPE_ANNIVERSARY,
                                        category = "纪念日",
                                        categoryColorHex = 0xFF8A9A5B,
                                        isPinned = false,
                                        reminderMode = EventEntity.REMINDER_NONE,
                                        notes = "与君初相识，犹如故人归。"
                                    )
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
