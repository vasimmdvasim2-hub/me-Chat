package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CallDao
import com.example.data.dao.ChannelDao
import com.example.data.dao.ContactDao
import com.example.data.dao.MeetingDao
import com.example.data.dao.MessageDao
import com.example.data.dao.StatusDao
import com.example.data.dao.UserDao
import com.example.data.model.CallLogEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.ChannelPostEntity
import com.example.data.model.ContactEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import com.example.data.model.UserAccount

@Database(
    entities = [
        UserAccount::class,
        ContactEntity::class,
        MessageEntity::class,
        StatusUpdateEntity::class,
        CallLogEntity::class,
        MeetingEntity::class,
        ChannelEntity::class,
        ChannelPostEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun statusDao(): StatusDao
    abstract fun callDao(): CallDao
    abstract fun meetingDao(): MeetingDao
    abstract fun channelDao(): ChannelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mechat_app.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
