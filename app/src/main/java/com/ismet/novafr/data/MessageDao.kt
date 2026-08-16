package com.ismet.novafr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<Message>>

    @Insert
    suspend fun insert(message: Message): Long

    @Query("DELETE FROM messages")
    suspend fun clearHistory()
}
