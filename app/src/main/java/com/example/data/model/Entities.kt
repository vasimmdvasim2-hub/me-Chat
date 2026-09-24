package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean = false,
    val countryCode: String = "+91",
    val countryName: String = "India",
    val phoneNumber: String = "",
    val displayName: String = "",
    val aboutStatus: String = "Hey there! I am using MeChat.",
    val avatarColor: Long = 0xFF008069,
    val profilePhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val about: String = "Hey there! I am using MeChat.",
    val avatarColor: Long = 0xFF128C7E,
    val isOnline: Boolean = false,
    val lastSeenText: String = "online",
    val isGroup: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val senderId: String, // "me" or contact id
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val status: String = "read", // "sent", "delivered", "read"
    val mediaType: String = "text", // "text", "voice", "image"
    val voiceDuration: String = "",
    val mediaUri: String? = null
)

@Entity(tableName = "status_updates")
data class StatusUpdateEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val authorName: String,
    val avatarColor: Long,
    val text: String,
    val backgroundHex: Long = 0xFF008069,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false,
    val isMine: Boolean = false,
    val score: Int = 85, // Status Score (0-100)
    val viewsCount: Int = 12,
    val likesCount: Int = 4,
    val filterName: String = "Natural", // Filter effect
    val mediaType: String = "text", // "text", "photo", "video"
    val mediaUri: String? = null,
    val songTitle: String? = null, // Gana / song title
    val songArtist: String? = null // Gana / artist
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconColor: Long = 0xFF008069,
    val iconUri: String? = null,
    val followerCount: Int = 0,
    val isFollowed: Boolean = false,
    val isOwner: Boolean = false,
    val category: String = "Community",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_posts")
data class ChannelPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val authorName: String,
    val text: String,
    val mediaUri: String? = null,
    val mediaType: String = "text", // "text", "image"
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val contactPhone: String,
    val avatarColor: Long,
    val callType: String, // "voice", "video"
    val direction: String, // "incoming", "outgoing", "missed"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val meetingCode: String,
    val title: String,
    val hostName: String,
    val startTime: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 30,
    val isInstant: Boolean = true,
    val status: String = "active", // "active", "scheduled", "ended"
    val participantCount: Int = 1
)

data class ChatSummary(
    val contact: ContactEntity,
    val lastMessage: MessageEntity?
)
