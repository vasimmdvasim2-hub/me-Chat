package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallLogEntity
import com.example.data.model.ContactEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    fun getUserFlow(): Flow<UserAccount?>

    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    suspend fun getUser(): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE user_account SET displayName = :name, aboutStatus = :about, profilePhotoUri = :photoUri, avatarColor = :color WHERE id = 1")
    suspend fun updateProfile(name: String, about: String, photoUri: String?, color: Long)

    @Query("DELETE FROM user_account")
    suspend fun clearUser()
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isPinned DESC, name ASC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnread(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(chatId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearChatHistory(chatId: String)
}

@Dao
interface StatusDao {
    @Query("SELECT * FROM status_updates ORDER BY timestamp DESC")
    fun getAllStatusesFlow(): Flow<List<StatusUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: StatusUpdateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatuses(statuses: List<StatusUpdateEntity>)

    @Query("UPDATE status_updates SET isViewed = 1, viewsCount = viewsCount + 1, score = CASE WHEN score < 98 THEN score + 2 ELSE 100 END WHERE id = :id")
    suspend fun markStatusViewed(id: String)

    @Query("UPDATE status_updates SET likesCount = likesCount + 1, score = CASE WHEN score < 95 THEN score + 5 ELSE 100 END WHERE id = :id")
    suspend fun addStatusLike(id: String)
}

@Dao
interface CallDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallsFlow(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallLogEntity>)

    @Query("DELETE FROM call_logs")
    suspend fun clearCallLogs()
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY startTime DESC")
    fun getAllMeetingsFlow(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE meetingCode = :code LIMIT 1")
    suspend fun getMeetingByCode(code: String): MeetingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity)

    @Query("UPDATE meetings SET status = :status WHERE id = :id")
    suspend fun updateMeetingStatus(id: String, status: String)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeeting(id: String)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY isOwner DESC, followerCount DESC, createdAt DESC")
    fun getAllChannelsFlow(): Flow<List<com.example.data.model.ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): com.example.data.model.ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: com.example.data.model.ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<com.example.data.model.ChannelEntity>)

    @Query("UPDATE channels SET isFollowed = :isFollowed, followerCount = followerCount + :delta WHERE id = :id")
    suspend fun updateChannelFollow(id: String, isFollowed: Boolean, delta: Int)

    @Query("SELECT * FROM channel_posts WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getPostsForChannelFlow(channelId: String): Flow<List<com.example.data.model.ChannelPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelPost(post: com.example.data.model.ChannelPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelPosts(posts: List<com.example.data.model.ChannelPostEntity>)

    @Query("UPDATE channel_posts SET likesCount = likesCount + 1 WHERE id = :postId")
    suspend fun likeChannelPost(postId: Long)
}
