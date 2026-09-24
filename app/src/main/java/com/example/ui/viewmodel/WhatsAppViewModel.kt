package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.ChannelPostEntity
import com.example.data.model.ChatSummary
import com.example.data.model.ContactEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import com.example.data.model.UserAccount
import com.example.data.repository.WhatsAppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    WELCOME,
    PHONE_INPUT,
    VERIFY_OTP,
    PROFILE_SETUP,
    MAIN,
    CHAT_CONVERSATION,
    STATUS_VIEWER,
    CALL_SCREEN,
    SETTINGS,
    MEETING_ROOM,
    CHANNEL_FEED
}

data class ActiveCall(
    val contactName: String,
    val phoneNumber: String,
    val avatarColor: Long,
    val callType: String, // "voice" or "video"
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true, // Video ON by default for video calls!
    val isFrontCamera: Boolean = true
)

data class MeetingParticipant(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val isHost: Boolean = false,
    val isMuted: Boolean = false,
    val isVideoOff: Boolean = false,
    val isSpeaking: Boolean = false,
    val isHandRaised: Boolean = false
)

data class MeetingChatMessage(
    val senderName: String,
    val text: String,
    val time: String,
    val isMe: Boolean
)

data class ActiveMeetingSession(
    val meeting: MeetingEntity,
    val isMuted: Boolean = false,
    val isVideoOff: Boolean = false,
    val isHandRaised: Boolean = false,
    val isFrontCamera: Boolean = true,
    val participants: List<MeetingParticipant> = emptyList(),
    val messages: List<MeetingChatMessage> = emptyList(),
    val elapsedSeconds: Int = 0
)

class WhatsAppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = WhatsAppRepository(
        database.userDao(),
        database.contactDao(),
        database.messageDao(),
        database.statusDao(),
        database.callDao(),
        database.meetingDao(),
        database.channelDao()
    )

    private val _currentScreen = MutableStateFlow(AppScreen.WELCOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Temporary registration state
    private val _tempCountryCode = MutableStateFlow("+91")
    val tempCountryCode: StateFlow<String> = _tempCountryCode.asStateFlow()

    private val _tempCountryName = MutableStateFlow("India")
    val tempCountryName: StateFlow<String> = _tempCountryName.asStateFlow()

    private val _tempPhoneNumber = MutableStateFlow("")
    val tempPhoneNumber: StateFlow<String> = _tempPhoneNumber.asStateFlow()

    private val _generatedOtp = MutableStateFlow("782419")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    private val _isVerifyingOtp = MutableStateFlow(false)
    val isVerifyingOtp: StateFlow<Boolean> = _isVerifyingOtp.asStateFlow()

    private val _isInitializingProfile = MutableStateFlow(false)
    val isInitializingProfile: StateFlow<Boolean> = _isInitializingProfile.asStateFlow()

    val userAccount: StateFlow<UserAccount?> = repository.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Chat search & filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedChatFilter = MutableStateFlow("All")
    val selectedChatFilter: StateFlow<String> = _selectedChatFilter.asStateFlow()

    val chatsSummary: StateFlow<List<ChatSummary>> = combine(
        repository.chatsSummaryFlow,
        _searchQuery,
        _selectedChatFilter
    ) { chats, query, filter ->
        var list = chats
        if (query.isNotBlank()) {
            list = list.filter {
                it.contact.name.contains(query, ignoreCase = true) ||
                (it.lastMessage?.text?.contains(query, ignoreCase = true) == true)
            }
        }
        when (filter) {
            "Unread" -> list.filter { it.contact.unreadCount > 0 }
            "Favourites" -> list.filter { it.contact.isPinned }
            "Groups" -> list.filter { it.contact.isGroup }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.allContactsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val statuses: StateFlow<List<StatusUpdateEntity>> = repository.allStatusesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Status Filter: "All", "Top Score", "Recent", "Unviewed", "My Status"
    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    val filteredStatuses: StateFlow<List<StatusUpdateEntity>> = combine(
        repository.allStatusesFlow,
        _statusFilter
    ) { list, filter ->
        when (filter) {
            "Top Score" -> list.sortedByDescending { it.score }
            "Recent" -> list.sortedByDescending { it.timestamp }
            "Unviewed" -> list.filter { !it.isViewed && !it.isMine }
            "My Status" -> list.filter { it.isMine }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallLogEntity>> = repository.allCallsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val meetings: StateFlow<List<MeetingEntity>> = repository.allMeetingsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Channels
    val channels: StateFlow<List<ChannelEntity>> = repository.allChannelsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _selectedChannel = MutableStateFlow<ChannelEntity?>(null)
    val selectedChannel: StateFlow<ChannelEntity?> = _selectedChannel.asStateFlow()

    val channelPosts: StateFlow<List<ChannelPostEntity>> = _selectedChannel.flatMapLatest { channel ->
        if (channel == null) flowOf(emptyList())
        else repository.getChannelPosts(channel.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Meeting Session
    private val _activeMeeting = MutableStateFlow<ActiveMeetingSession?>(null)
    val activeMeeting: StateFlow<ActiveMeetingSession?> = _activeMeeting.asStateFlow()
    private var meetingTimerJob: Job? = null

    // Active Chat Selection
    private val _selectedContact = MutableStateFlow<ContactEntity?>(null)
    val selectedContact: StateFlow<ContactEntity?> = _selectedContact.asStateFlow()

    val currentChatMessages: StateFlow<List<MessageEntity>> = _selectedContact.flatMapLatest { contact ->
        if (contact == null) flowOf(emptyList())
        else repository.getMessagesForChat(contact.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Status Viewer
    private val _activeStatus = MutableStateFlow<StatusUpdateEntity?>(null)
    val activeStatus: StateFlow<StatusUpdateEntity?> = _activeStatus.asStateFlow()

    // Active Call
    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            val user = repository.getUser()
            if (user != null && user.isLoggedIn) {
                _currentScreen.value = AppScreen.MAIN
            } else {
                _currentScreen.value = AppScreen.WELCOME
            }
        }
    }

    // --- Onboarding & Auth actions ---
    fun onAgreeAndContinue() {
        _currentScreen.value = AppScreen.PHONE_INPUT
    }

    fun setPhoneDetails(countryCode: String, countryName: String, phone: String) {
        _tempCountryCode.value = countryCode
        _tempCountryName.value = countryName
        _tempPhoneNumber.value = phone
    }

    fun submitPhoneNumber() {
        val code = (100000..999999).random().toString()
        _generatedOtp.value = code
        viewModelScope.launch {
            repository.savePhoneNumber(
                countryCode = _tempCountryCode.value,
                countryName = _tempCountryName.value,
                phone = _tempPhoneNumber.value
            )
            _currentScreen.value = AppScreen.VERIFY_OTP
        }
    }

    fun verifyOtp(inputOtp: String, onInvalid: () -> Unit = {}) {
        if (inputOtp.length == 6) {
            viewModelScope.launch {
                _isVerifyingOtp.value = true
                delay(700)
                _isVerifyingOtp.value = false
                _currentScreen.value = AppScreen.PROFILE_SETUP
            }
        } else {
            onInvalid()
        }
    }

    fun editPhoneNumber() {
        _currentScreen.value = AppScreen.PHONE_INPUT
    }

    fun completeProfile(name: String, about: String, avatarColor: Long, photoUri: String? = null) {
        viewModelScope.launch {
            _isInitializingProfile.value = true
            repository.completeProfile(name, about, avatarColor, photoUri)
            delay(1000)
            _isInitializingProfile.value = false
            _currentScreen.value = AppScreen.MAIN
        }
    }

    fun updateUserProfile(name: String, about: String, photoUri: String?, avatarColor: Long) {
        viewModelScope.launch {
            repository.updateUserProfile(name, about, photoUri, avatarColor)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _selectedContact.value = null
            _activeCall.value = null
            _activeStatus.value = null
            _activeMeeting.value = null
            _selectedChannel.value = null
            _currentScreen.value = AppScreen.WELCOME
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // --- Channel actions ---
    fun openChannel(channel: ChannelEntity) {
        _selectedChannel.value = channel
        _currentScreen.value = AppScreen.CHANNEL_FEED
    }

    fun closeChannel() {
        _selectedChannel.value = null
        _currentScreen.value = AppScreen.MAIN
    }

    fun createChannel(name: String, description: String, category: String = "Community") {
        viewModelScope.launch {
            val newCh = repository.createChannel(name, description, category)
            _selectedChannel.value = newCh
            _currentScreen.value = AppScreen.CHANNEL_FEED
        }
    }

    fun toggleFollowChannel(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFollowChannel(channel.id, channel.isFollowed)
            _selectedChannel.value?.let {
                if (it.id == channel.id) {
                    val newFollowed = !it.isFollowed
                    _selectedChannel.value = it.copy(
                        isFollowed = newFollowed,
                        followerCount = if (newFollowed) it.followerCount + 1 else (it.followerCount - 1).coerceAtLeast(0)
                    )
                }
            }
        }
    }

    fun postChannelUpdate(channelId: String, text: String, mediaUri: String? = null) {
        viewModelScope.launch {
            repository.postChannelUpdate(channelId, text, mediaUri)
        }
    }

    fun likeChannelPost(postId: Long) {
        viewModelScope.launch {
            repository.likeChannelPost(postId)
        }
    }

    // --- Chat actions ---
    fun openChat(contact: ContactEntity) {
        _selectedContact.value = contact
        _currentScreen.value = AppScreen.CHAT_CONVERSATION
        viewModelScope.launch {
            repository.clearUnread(contact.id)
        }
    }

    fun closeChat() {
        _selectedContact.value = null
        _currentScreen.value = AppScreen.MAIN
    }

    fun sendMessage(text: String) {
        val contact = _selectedContact.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(contact.id, text.trim())
        }
    }

    fun sendVoiceNote() {
        val contact = _selectedContact.value ?: return
        val duration = "0:0${(3..8).random()}"
        viewModelScope.launch {
            repository.sendVoiceMessage(contact.id, duration)
        }
    }

    fun sendImageMessage(uri: String, caption: String = "") {
        val contact = _selectedContact.value ?: return
        viewModelScope.launch {
            repository.sendImageMessage(contact.id, uri, caption)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setChatFilter(filter: String) {
        _selectedChatFilter.value = filter
    }

    // --- Status actions ---
    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun openStatusViewer(status: StatusUpdateEntity) {
        _activeStatus.value = status
        viewModelScope.launch {
            repository.markStatusViewed(status.id)
        }
        _currentScreen.value = AppScreen.STATUS_VIEWER
    }

    fun reactToStatus(statusId: String) {
        viewModelScope.launch {
            repository.addStatusLike(statusId)
            _activeStatus.value?.let {
                if (it.id == statusId) {
                    _activeStatus.value = it.copy(
                        likesCount = it.likesCount + 1,
                        score = if (it.score < 95) it.score + 5 else 100
                    )
                }
            }
        }
    }

    fun closeStatusViewer() {
        _activeStatus.value = null
        _currentScreen.value = AppScreen.MAIN
    }

    fun addNewStatus(
        text: String,
        colorHex: Long,
        filterName: String = "Natural",
        mediaType: String = "text",
        mediaUri: String? = null,
        songTitle: String? = null,
        songArtist: String? = null
    ) {
        viewModelScope.launch {
            repository.postStatus(text, colorHex, filterName, mediaType, mediaUri, songTitle, songArtist)
        }
    }

    // --- Call actions ---
    fun startCall(contactName: String, phone: String, avatarColor: Long, type: String) {
        _activeCall.value = ActiveCall(
            contactName = contactName,
            phoneNumber = phone,
            avatarColor = avatarColor,
            callType = type,
            isVideoEnabled = (type == "video") // Video ON when call starts!
        )
        viewModelScope.launch {
            repository.logCall(contactName, phone, avatarColor, type, "outgoing")
        }
        _currentScreen.value = AppScreen.CALL_SCREEN
    }

    fun toggleCallMute() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleCallSpeaker() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isSpeakerOn = !it.isSpeakerOn)
        }
    }

    fun toggleCallVideo() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isVideoEnabled = !it.isVideoEnabled)
        }
    }

    fun switchCallCamera() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isFrontCamera = !it.isFrontCamera)
        }
    }

    fun endCall() {
        _activeCall.value = null
        _currentScreen.value = if (_selectedContact.value != null) AppScreen.CHAT_CONVERSATION else AppScreen.MAIN
    }

    fun addNewContact(name: String, phone: String, about: String) {
        viewModelScope.launch {
            repository.addContact(name, phone, about)
        }
    }

    // --- Meeting actions ---
    fun startInstantMeeting(title: String = "MeChat Quick Meeting") {
        viewModelScope.launch {
            val user = repository.getUser()
            val hostName = user?.displayName?.ifBlank { "You" } ?: "You"
            val meeting = repository.createMeeting(title, durationMinutes = 45, isInstant = true)
            setupActiveMeetingSession(meeting, hostName)
        }
    }

    fun joinMeetingWithCode(code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUser()
            val myName = user?.displayName?.ifBlank { "You" } ?: "You"
            val meeting = repository.getMeetingByCode(code)
            if (meeting != null) {
                setupActiveMeetingSession(meeting, myName)
                onResult(true)
            } else {
                val newMeeting = repository.createMeeting("Room: ${code.uppercase()}", durationMinutes = 30, isInstant = true)
                setupActiveMeetingSession(newMeeting.copy(meetingCode = code.trim().lowercase()), myName)
                onResult(true)
            }
        }
    }

    fun scheduleMeeting(title: String, durationMinutes: Int, scheduledTime: Long) {
        viewModelScope.launch {
            repository.createMeeting(title, durationMinutes = durationMinutes, isInstant = false)
        }
    }

    private fun setupActiveMeetingSession(meeting: MeetingEntity, hostOrMyName: String) {
        val initialParticipants = listOf(
            MeetingParticipant(
                id = "self",
                name = "$hostOrMyName (You)",
                avatarColor = 0xFF008069,
                isHost = true,
                isMuted = false,
                isVideoOff = false,
                isSpeaking = false
            ),
            MeetingParticipant(
                id = "p1",
                name = "Aman Kumar",
                avatarColor = 0xFF128C7E,
                isHost = false,
                isMuted = false,
                isVideoOff = false,
                isSpeaking = true
            ),
            MeetingParticipant(
                id = "p2",
                name = "Neha Sharma",
                avatarColor = 0xFFE91E63,
                isHost = false,
                isMuted = true,
                isVideoOff = false,
                isSpeaking = false
            )
        )

        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val initialMessages = listOf(
            MeetingChatMessage(
                senderName = "Aman Kumar",
                text = "Hello everyone! Sound and video clear here.",
                time = timeStr,
                isMe = false
            )
        )

        _activeMeeting.value = ActiveMeetingSession(
            meeting = meeting,
            isMuted = false,
            isVideoOff = false,
            isHandRaised = false,
            isFrontCamera = true,
            participants = initialParticipants,
            messages = initialMessages,
            elapsedSeconds = 0
        )

        meetingTimerJob?.cancel()
        meetingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeMeeting.value = _activeMeeting.value?.let {
                    it.copy(elapsedSeconds = it.elapsedSeconds + 1)
                }
            }
        }

        _currentScreen.value = AppScreen.MEETING_ROOM
    }

    fun toggleMeetingMute() {
        _activeMeeting.value = _activeMeeting.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleMeetingVideo() {
        _activeMeeting.value = _activeMeeting.value?.let {
            it.copy(isVideoOff = !it.isVideoOff)
        }
    }

    fun toggleMeetingHand() {
        _activeMeeting.value = _activeMeeting.value?.let {
            it.copy(isHandRaised = !it.isHandRaised)
        }
    }

    fun switchMeetingCamera() {
        _activeMeeting.value = _activeMeeting.value?.let {
            it.copy(isFrontCamera = !it.isFrontCamera)
        }
    }

    fun sendMeetingChatMessage(text: String) {
        if (text.isBlank()) return
        val current = _activeMeeting.value ?: return
        val user = userAccount.value
        val myName = user?.displayName?.ifBlank { "You" } ?: "You"
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newMsg = MeetingChatMessage(
            senderName = myName,
            text = text.trim(),
            time = timeStr,
            isMe = true
        )
        _activeMeeting.value = current.copy(
            messages = current.messages + newMsg
        )
    }

    fun leaveMeeting() {
        meetingTimerJob?.cancel()
        val current = _activeMeeting.value
        if (current != null) {
            viewModelScope.launch {
                repository.endMeeting(current.meeting.id)
            }
        }
        _activeMeeting.value = null
        _currentScreen.value = AppScreen.MAIN
    }
}
