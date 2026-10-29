package com.example.data

import android.content.Context
import com.example.notification.AlarmScheduler
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao,
    private val context: Context
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val pinnedEvents: Flow<List<EventEntity>> = eventDao.getPinnedEvents()

    suspend fun getEventById(id: Long): EventEntity? = eventDao.getEventById(id)

    suspend fun getAllEventsList(): List<EventEntity> = eventDao.getAllEventsList()

    suspend fun insertEvent(event: EventEntity): Long {
        val id = eventDao.insertEvent(event)
        val savedEvent = event.copy(id = id)
        AlarmScheduler.scheduleEventAlarm(context, savedEvent)
        WidgetUpdateHelper.updateAllWidgets(context)
        return id
    }

    suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
        AlarmScheduler.scheduleEventAlarm(context, event)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun deleteEvent(event: EventEntity) {
        AlarmScheduler.cancelEventAlarm(context, event.id)
        eventDao.deleteEvent(event)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun deleteById(id: Long) {
        AlarmScheduler.cancelEventAlarm(context, id)
        eventDao.deleteById(id)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun updateCategoryForIds(ids: List<Long>, newCategory: String) {
        eventDao.updateCategoryForIds(ids, newCategory)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun updateColorForCategory(category: String, colorHex: Long) {
        eventDao.updateColorForCategory(category, colorHex)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun deleteByIds(ids: List<Long>) {
        for (id in ids) {
            AlarmScheduler.cancelEventAlarm(context, id)
        }
        eventDao.deleteByIds(ids)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun bulkInsert(events: List<EventEntity>) {
        eventDao.insertAll(events)
        for (e in events) {
            AlarmScheduler.scheduleEventAlarm(context, e)
        }
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun deleteFestivalEvents() {
        val list = eventDao.getAllEventsList().filter { it.category in listOf("节日", "節日") }
        for (e in list) {
            AlarmScheduler.cancelEventAlarm(context, e.id)
        }
        eventDao.deleteFestivalEvents()
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun deleteAll() {
        val all = eventDao.getAllEventsList()
        for (e in all) {
            AlarmScheduler.cancelEventAlarm(context, e.id)
        }
        eventDao.deleteAll()
        WidgetUpdateHelper.updateAllWidgets(context)
    }
}
