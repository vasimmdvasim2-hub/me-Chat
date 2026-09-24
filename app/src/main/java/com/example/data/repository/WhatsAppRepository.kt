package com.example.data.repository

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
import com.example.data.model.ChatSummary
import com.example.data.model.ContactEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import com.example.data.model.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WhatsAppRepository(
    private val userDao: UserDao,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val statusDao: StatusDao,
    private val callDao: CallDao,
    private val meetingDao: MeetingDao,
    private val channelDao: ChannelDao
) {
    val userFlow: Flow<UserAccount?> = userDao.getUserFlow()
    val allContactsFlow: Flow<List<ContactEntity>> = contactDao.getAllContactsFlow()
    val allCallsFlow: Flow<List<CallLogEntity>> = callDao.getAllCallsFlow()
    val allStatusesFlow: Flow<List<StatusUpdateEntity>> = statusDao.getAllStatusesFlow()
    val allMeetingsFlow: Flow<List<MeetingEntity>> = meetingDao.getAllMeetingsFlow()
    val allChannelsFlow: Flow<List<ChannelEntity>> = channelDao.getAllChannelsFlow()

    // Chats list combined with latest message
    val chatsSummaryFlow: Flow<List<ChatSummary>> = combine(
        contactDao.getAllContactsFlow(),
        messageDao.getAllMessagesFlow()
    ) { contacts, allMessages ->
        val latestByChat = allMessages.groupBy { it.chatId }
            .mapValues { (_, msgs) -> msgs.maxByOrNull { it.timestamp } }

        contacts.map { contact ->
            ChatSummary(
                contact = contact,
                lastMessage = latestByChat[contact.id]
            )
        }.sortedByDescending { it.lastMessage?.timestamp ?: 0L }
    }

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForChatFlow(chatId)
    }

    suspend fun getUser(): UserAccount? = userDao.getUser()

    suspend fun savePhoneNumber(countryCode: String, countryName: String, phone: String) {
        val current = userDao.getUser() ?: UserAccount()
        userDao.insertUser(
            current.copy(
                countryCode = countryCode,
                countryName = countryName,
                phoneNumber = phone,
                isLoggedIn = false
            )
        )
    }

    suspend fun completeProfile(name: String, about: String, avatarColor: Long, profilePhotoUri: String? = null) {
        val current = userDao.getUser() ?: UserAccount()
        userDao.insertUser(
            current.copy(
                displayName = name,
                aboutStatus = about,
                avatarColor = avatarColor,
                profilePhotoUri = profilePhotoUri,
                isLoggedIn = true
            )
        )
    }

    suspend fun updateUserProfile(name: String, about: String, profilePhotoUri: String?, avatarColor: Long) {
        userDao.updateProfile(name, about, profilePhotoUri, avatarColor)
    }

    suspend fun logout() {
        userDao.clearUser()
    }

    suspend fun clearUnread(chatId: String) {
        contactDao.clearUnread(chatId)
    }

    suspend fun sendMessage(chatId: String, text: String, mediaType: String = "text") {
        val user = userDao.getUser()
        val senderName = user?.displayName?.ifBlank { "You" } ?: "You"

        val myMsg = MessageEntity(
            chatId = chatId,
            senderId = "me",
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            status = "read",
            mediaType = mediaType
        )
        messageDao.insertMessage(myMsg)

        // Trigger realistic WhatsApp auto-response from contact
        val contact = contactDao.getContactById(chatId)
        if (contact != null) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1200)
                val replyText = generateAutoReply(contact.name, text)
                val replyMsg = MessageEntity(
                    chatId = chatId,
                    senderId = contact.id,
                    senderName = contact.name,
                    text = replyText,
                    timestamp = System.currentTimeMillis(),
                    isOutgoing = false,
                    status = "read"
                )
                messageDao.insertMessage(replyMsg)
            }
        }
    }

    suspend fun sendVoiceMessage(chatId: String, duration: String = "0:06") {
        sendMessage(chatId, "Voice message ($duration)", mediaType = "voice")
    }

    suspend fun addContact(name: String, phone: String, about: String = "Hey there! I am using MeChat.") {
        val id = "contact_${System.currentTimeMillis()}"
        val colors = listOf(0xFF008069, 0xFF128C7E, 0xFF00A884, 0xFFE05638, 0xFF3F51B5, 0xFF9C27B0)
        val color = colors.random()
        val newContact = ContactEntity(
            id = id,
            name = name,
            phoneNumber = phone,
            about = about,
            avatarColor = color,
            isOnline = true,
            lastSeenText = "online"
        )
        contactDao.insertContact(newContact)
    }

    suspend fun postStatus(
        text: String,
        backgroundHex: Long,
        filterName: String = "Natural",
        mediaType: String = "text",
        mediaUri: String? = null,
        songTitle: String? = null,
        songArtist: String? = null
    ) {
        val user = userDao.getUser()
        val author = user?.displayName?.ifBlank { "My Status" } ?: "My Status"
        val status = StatusUpdateEntity(
            id = "status_mine_${System.currentTimeMillis()}",
            contactId = "me",
            authorName = author,
            avatarColor = user?.avatarColor ?: 0xFF008069,
            text = text,
            backgroundHex = backgroundHex,
            timestamp = System.currentTimeMillis(),
            isViewed = false,
            isMine = true,
            score = 94,
            viewsCount = 0,
            likesCount = 0,
            filterName = filterName,
            mediaType = mediaType,
            mediaUri = mediaUri,
            songTitle = songTitle,
            songArtist = songArtist
        )
        statusDao.insertStatus(status)
    }

    // --- Channel Operations ---
    suspend fun createChannel(
        name: String,
        description: String,
        category: String = "Community"
    ): ChannelEntity {
        val colors = listOf(0xFF008069, 0xFF128C7E, 0xFF0288D1, 0xFF7B1FA2, 0xFFE65100, 0xFFC2185B)
        val channelId = "channel_${System.currentTimeMillis()}"
        val channel = ChannelEntity(
            id = channelId,
            name = name,
            description = description,
            iconColor = colors.random(),
            followerCount = 1,
            isFollowed = true,
            isOwner = true,
            category = category
        )
        channelDao.insertChannel(channel)

        val user = userDao.getUser()
        val author = user?.displayName?.ifBlank { "Admin" } ?: "Admin"
        channelDao.insertChannelPost(
            ChannelPostEntity(
                channelId = channelId,
                authorName = author,
                text = "Welcome to $name! Here we will share updates, news, and exciting announcements with everyone.",
                mediaType = "text",
                timestamp = System.currentTimeMillis()
            )
        )
        return channel
    }

    suspend fun toggleFollowChannel(channelId: String, currentFollowed: Boolean) {
        val newFollow = !currentFollowed
        val delta = if (newFollow) 1 else -1
        channelDao.updateChannelFollow(channelId, newFollow, delta)
    }

    fun getChannelPosts(channelId: String): Flow<List<ChannelPostEntity>> =
        channelDao.getPostsForChannelFlow(channelId)

    suspend fun postChannelUpdate(
        channelId: String,
        text: String,
        mediaUri: String? = null,
        mediaType: String = "text"
    ) {
        val user = userDao.getUser()
        val author = user?.displayName?.ifBlank { "Admin" } ?: "Admin"
        channelDao.insertChannelPost(
            ChannelPostEntity(
                channelId = channelId,
                authorName = author,
                text = text,
                mediaUri = mediaUri,
                mediaType = mediaType,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun likeChannelPost(postId: Long) {
        channelDao.likeChannelPost(postId)
    }

    suspend fun markStatusViewed(statusId: String) {
        statusDao.markStatusViewed(statusId)
    }

    suspend fun addStatusLike(statusId: String) {
        statusDao.addStatusLike(statusId)
    }

    suspend fun sendImageMessage(chatId: String, mediaUri: String, caption: String = "") {
        val user = userDao.getUser()
        val senderName = user?.displayName?.ifBlank { "You" } ?: "You"
        val msg = MessageEntity(
            chatId = chatId,
            senderId = "me",
            senderName = senderName,
            text = caption.ifBlank { "📷 Photo" },
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            status = "read",
            mediaType = "image",
            mediaUri = mediaUri
        )
        messageDao.insertMessage(msg)
    }

    suspend fun createMeeting(title: String, durationMinutes: Int = 30, isInstant: Boolean = true): MeetingEntity {
        val user = userDao.getUser()
        val host = user?.displayName?.ifBlank { "You" } ?: "You"
        val code1 = (100..999).random()
        val code2 = (100..999).random()
        val code = "mct-$code1-$code2"
        val meeting = MeetingEntity(
            id = "meet_${System.currentTimeMillis()}",
            meetingCode = code,
            title = title.ifBlank { "MeChat Video Meeting" },
            hostName = host,
            startTime = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            isInstant = isInstant,
            status = "active",
            participantCount = 1
        )
        meetingDao.insertMeeting(meeting)
        return meeting
    }

    suspend fun getMeetingByCode(code: String): MeetingEntity? {
        val cleanCode = code.trim().lowercase()
        return meetingDao.getMeetingByCode(cleanCode)
    }

    suspend fun endMeeting(id: String) {
        meetingDao.updateMeetingStatus(id, "ended")
    }

    suspend fun deleteMeeting(id: String) {
        meetingDao.deleteMeeting(id)
    }

    suspend fun logCall(contactName: String, phone: String, avatarColor: Long, type: String, direction: String) {
        val call = CallLogEntity(
            contactName = contactName,
            contactPhone = phone,
            avatarColor = avatarColor,
            callType = type,
            direction = direction,
            timestamp = System.currentTimeMillis()
        )
        callDao.insertCall(call)
    }

    private fun generateAutoReply(contactName: String, userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") ->
                "Hey! Kese ho? MeChat pe baat karke accha laga 😊"
            lower.contains("kaha ho") || lower.contains("where") ->
                "Main ghar pe hu, thodi der me call karta hu!"
            lower.contains("kya kar rahe") || lower.contains("doing") ->
                "Bas coding aur coffee chal rahi he ☕ tum batao?"
            lower.contains("call") || lower.contains("baat") ->
                "Haan sure, abhi free ho to voice call karein?"
            lower.contains("ok") || lower.contains("theek") ->
                "Done deal! 👍 Milte hain sham ko."
            lower.contains("photo") || lower.contains("pic") ->
                "Superb click! 👌 Bahut badiya lag rahi hai."
            else ->
                "Han bilkul sahi kaha! Let me know if you need anything else! 🙌"
        }
    }

    suspend fun seedInitialDataIfNeeded() {
        val existing = contactDao.getAllContactsFlow().firstOrNull()
        if (!existing.isNullOrEmpty()) return

        val now = System.currentTimeMillis()
        val hour = 3600_000L

        // Default contacts
        val contacts = listOf(
            ContactEntity(
                id = "c1",
                name = "Rahul Sharma",
                phoneNumber = "+91 98234 56789",
                about = "Busy coding... 💻",
                avatarColor = 0xFF008069,
                isOnline = true,
                lastSeenText = "online",
                unreadCount = 2,
                isPinned = true
            ),
            ContactEntity(
                id = "c2",
                name = "Priya Verma",
                phoneNumber = "+91 94123 45678",
                about = "Urgent calls only 📵",
                avatarColor = 0xFFE91E63,
                isOnline = false,
                lastSeenText = "today at 10:15 AM",
                unreadCount = 0,
                isPinned = false
            ),
            ContactEntity(
                id = "c3",
                name = "Family Group 👨‍👩‍👧‍👦",
                phoneNumber = "12 participants",
                about = "Family first ❤️",
                avatarColor = 0xFF3F51B5,
                isOnline = false,
                lastSeenText = "yesterday",
                isGroup = true,
                unreadCount = 5,
                isPinned = true
            ),
            ContactEntity(
                id = "c4",
                name = "Aman Khan",
                phoneNumber = "+91 87654 32109",
                about = "Can't talk, MeChat only",
                avatarColor = 0xFFFF5722,
                isOnline = true,
                lastSeenText = "online",
                unreadCount = 0,
                isPinned = false
            ),
            ContactEntity(
                id = "c5",
                name = "Neha Gupta",
                phoneNumber = "+91 99887 76655",
                about = "Live, Laugh, Love ✨",
                avatarColor = 0xFF9C27B0,
                isOnline = false,
                lastSeenText = "today at 09:30 AM",
                unreadCount = 0,
                isPinned = false
            )
        )
        contactDao.insertContacts(contacts)

        // Default messages
        val messages = listOf(
            MessageEntity(
                chatId = "c1",
                senderId = "c1",
                senderName = "Rahul Sharma",
                text = "Bhai, kal ka project submit kar diya?",
                timestamp = now - (hour * 2),
                isOutgoing = false,
                status = "read"
            ),
            MessageEntity(
                chatId = "c1",
                senderId = "me",
                senderName = "You",
                text = "Haan bhai, kal raat ko hi bhej diya tha. Check kar lo!",
                timestamp = now - (hour),
                isOutgoing = true,
                status = "read"
            ),
            MessageEntity(
                chatId = "c1",
                senderId = "c1",
                senderName = "Rahul Sharma",
                text = "Great! Ek baar call pe discuss kar lete hain 👍",
                timestamp = now - 15 * 60_000L,
                isOutgoing = false,
                status = "read"
            ),
            MessageEntity(
                chatId = "c2",
                senderId = "c2",
                senderName = "Priya Verma",
                text = "Hey! Are we still meeting for coffee this weekend?",
                timestamp = now - (hour * 4),
                isOutgoing = false,
                status = "read"
            ),
            MessageEntity(
                chatId = "c2",
                senderId = "me",
                senderName = "You",
                text = "Yes absolutely! 5 PM at Starbucks.",
                timestamp = now - (hour * 3),
                isOutgoing = true,
                status = "read"
            ),
            MessageEntity(
                chatId = "c3",
                senderId = "c3",
                senderName = "Mom ❤️",
                text = "Sab log dinner ke liye 8 baje ghar pe rehna 🍲",
                timestamp = now - (hour * 5),
                isOutgoing = false,
                status = "read"
            ),
            MessageEntity(
                chatId = "c4",
                senderId = "c4",
                senderName = "Aman Khan",
                text = "Check out this new Kotlin feature! 🔥",
                timestamp = now - (hour * 8),
                isOutgoing = false,
                status = "read"
            ),
            MessageEntity(
                chatId = "c5",
                senderId = "c5",
                senderName = "Neha Gupta",
                text = "Thanks for sharing the notes! 🙏",
                timestamp = now - (hour * 24),
                isOutgoing = false,
                status = "read"
            )
        )
        messageDao.insertMessages(messages)

        // Default Statuses with Status Score, Filters, Photo, Video & Gana (Song)
        val statuses = listOf(
            StatusUpdateEntity(
                id = "s1",
                contactId = "c1",
                authorName = "Rahul Sharma",
                avatarColor = 0xFF008069,
                text = "Sunrise morning vibes by the holy Ganga ghats! 🌅🙏🌊",
                backgroundHex = 0xFF005C4B,
                timestamp = now - (hour * 2),
                isViewed = false,
                score = 98,
                viewsCount = 45,
                likesCount = 21,
                filterName = "Emerald",
                mediaType = "photo",
                songTitle = "Ganga Kinare (Ganga Aarti Dhun)",
                songArtist = "Traditional / Devotional"
            ),
            StatusUpdateEntity(
                id = "s2",
                contactId = "c2",
                authorName = "Priya Verma",
                avatarColor = 0xFFE91E63,
                text = "Live music rehearsal & guitar jam session! 🎸🎬✨",
                backgroundHex = 0xFFD81B60,
                timestamp = now - (hour * 5),
                isViewed = false,
                score = 92,
                viewsCount = 31,
                likesCount = 14,
                filterName = "Sunset",
                mediaType = "video",
                songTitle = "Kesariya (Brahmāstra)",
                songArtist = "Arijit Singh"
            ),
            StatusUpdateEntity(
                id = "s3",
                contactId = "c4",
                authorName = "Aman Khan",
                avatarColor = 0xFFFF5722,
                text = "Weekend road trip begins through the mountain valleys 🚗💨⛰️",
                backgroundHex = 0xFFE64A19,
                timestamp = now - (hour * 7),
                isViewed = true,
                score = 79,
                viewsCount = 20,
                likesCount = 8,
                filterName = "Vibrant",
                mediaType = "text",
                songTitle = "Pehle Bhi Main (Animal)",
                songArtist = "Vishal Mishra"
            )
        )
        statusDao.insertStatuses(statuses)

        // Default Channels
        val channels = listOf(
            ChannelEntity(
                id = "ch_mechat",
                name = "MeChat Official",
                description = "Official news, features, announcements, and tips from the MeChat team.",
                iconColor = 0xFF008069,
                followerCount = 1250000,
                isFollowed = true,
                isOwner = false,
                category = "Official"
            ),
            ChannelEntity(
                id = "ch_tech",
                name = "TechRadar India",
                description = "Daily gadgets, smartphone launches, AI news, and tech updates.",
                iconColor = 0xFF0288D1,
                followerCount = 840000,
                isFollowed = true,
                isOwner = false,
                category = "Technology"
            ),
            ChannelEntity(
                id = "ch_bollywood",
                name = "Bollywood Buzz",
                description = "Latest movie trailers, celebrity gossips, music releases, and box office.",
                iconColor = 0xFFC2185B,
                followerCount = 980000,
                isFollowed = false,
                isOwner = false,
                category = "Entertainment"
            ),
            ChannelEntity(
                id = "ch_cricket",
                name = "Cricket 24x7",
                description = "Ball-by-ball updates, match schedules, stats, and Team India highlights.",
                iconColor = 0xFF1976D2,
                followerCount = 2100000,
                isFollowed = false,
                isOwner = false,
                category = "Sports"
            ),
            ChannelEntity(
                id = "ch_motivation",
                name = "Daily Motivation & Quotes",
                description = "Start your day with positive thoughts, inspiring quotes, and success wisdom.",
                iconColor = 0xFFE65100,
                followerCount = 450000,
                isFollowed = false,
                isOwner = false,
                category = "Lifestyle"
            )
        )
        channelDao.insertChannels(channels)

        // Default Channel Posts
        val channelPosts = listOf(
            ChannelPostEntity(
                channelId = "ch_mechat",
                authorName = "MeChat Team",
                text = "🎉 Welcome to MeChat! Now you can post Photo, Video & Song statuses, enjoy HD Video Calls, and create your own Channels!",
                mediaType = "text",
                timestamp = now - (hour * 3),
                likesCount = 1542
            ),
            ChannelPostEntity(
                channelId = "ch_tech",
                authorName = "TechRadar",
                text = "⚡ New flagship processors launched with cutting-edge on-device AI performance!",
                mediaType = "text",
                timestamp = now - (hour * 6),
                likesCount = 421
            ),
            ChannelPostEntity(
                channelId = "ch_cricket",
                authorName = "Cricket Desk",
                text = "🏏 Spectacular victory in the final over! What a match!",
                mediaType = "text",
                timestamp = now - (hour * 10),
                likesCount = 2890
            )
        )
        channelDao.insertChannelPosts(channelPosts)

        // Default Calls
        val calls = listOf(
            CallLogEntity(
                contactName = "Rahul Sharma",
                contactPhone = "+91 98234 56789",
                avatarColor = 0xFF008069,
                callType = "voice",
                direction = "incoming",
                timestamp = now - (hour * 3)
            ),
            CallLogEntity(
                contactName = "Priya Verma",
                contactPhone = "+91 94123 45678",
                avatarColor = 0xFFE91E63,
                callType = "video",
                direction = "outgoing",
                timestamp = now - (hour * 6)
            ),
            CallLogEntity(
                contactName = "Aman Khan",
                contactPhone = "+91 87654 32109",
                avatarColor = 0xFFFF5722,
                callType = "voice",
                direction = "missed",
                timestamp = now - (hour * 20)
            )
        )
        callDao.insertCalls(calls)

        // Default Meetings
        val meetings = listOf(
            MeetingEntity(
                id = "meet_demo_1",
                meetingCode = "mct-489-215",
                title = "MeChat Product Launch Sync",
                hostName = "Rahul Sharma",
                startTime = now + (hour * 2),
                durationMinutes = 45,
                isInstant = false,
                status = "scheduled",
                participantCount = 4
            ),
            MeetingEntity(
                id = "meet_demo_2",
                meetingCode = "mct-712-904",
                title = "UI/UX Review & Feedback",
                hostName = "Priya Verma",
                startTime = now - (hour * 4),
                durationMinutes = 30,
                isInstant = true,
                status = "active",
                participantCount = 2
            )
        )
        meetings.forEach { meetingDao.insertMeeting(it) }
    }
}
