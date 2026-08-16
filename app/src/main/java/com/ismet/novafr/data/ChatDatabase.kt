package com.ismet.novafr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromSender(sender: Sender): String = sender.name

    @TypeConverter
    fun toSender(value: String): Sender = Sender.valueOf(value)
}

@Database(entities = [Message::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "nova_fr_chat_history.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
