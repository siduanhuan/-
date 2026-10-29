package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY isPinned DESC, createdAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE isPinned = 1 ORDER BY createdAt DESC")
    fun getPinnedEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getAllEventsList(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE events SET category = :newCategory WHERE id IN (:ids)")
    suspend fun updateCategoryForIds(ids: List<Long>, newCategory: String)

    @Query("UPDATE events SET categoryColorHex = :colorHex WHERE category = :category")
    suspend fun updateColorForCategory(category: String, colorHex: Long)

    @Query("DELETE FROM events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM events WHERE category IN ('节日', '節日')")
    suspend fun deleteFestivalEvents()

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
