package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.MeetingRoomScreen
import com.example.ui.screens.PhoneInputScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatusViewerScreen
import com.example.ui.screens.VerifyOtpScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MeChatTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.WhatsAppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MeChatAppRoot()
                }
            }
        }
    }
}

@Composable
fun MeChatAppRoot(
    viewModel: WhatsAppViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val user by viewModel.userAccount.collectAsStateWithLifecycle()
    val chats by viewModel.chatsSummary.collectAsStateWithLifecycle()
    val filteredStatuses by viewModel.filteredStatuses.collectAsStateWithLifecycle()
    val calls by viewModel.calls.collectAsStateWithLifecycle()
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val currentChatMessages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
    val activeStatus by viewModel.activeStatus.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedChatFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    val tempCountryCode by viewModel.tempCountryCode.collectAsStateWithLifecycle()
    val tempCountryName by viewModel.tempCountryName.collectAsStateWithLifecycle()
    val tempPhoneNumber by viewModel.tempPhoneNumber.collectAsStateWithLifecycle()
    val generatedOtp by viewModel.generatedOtp.collectAsStateWithLifecycle()
    val isVerifyingOtp by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val isInitializingProfile by viewModel.isInitializingProfile.collectAsStateWithLifecycle()

    when (currentScreen) {
        AppScreen.WELCOME -> {
            WelcomeScreen(
                onAgreeAndContinue = { viewModel.onAgreeAndContinue() }
            )
        }
        AppScreen.PHONE_INPUT -> {
            PhoneInputScreen(
                initialCountryCode = tempCountryCode,
                initialCountryName = tempCountryName,
                onProceedToOtp = { code, country, phone ->
                    viewModel.setPhoneDetails(code, country, phone)
                    viewModel.submitPhoneNumber()
                }
            )
        }
        AppScreen.VERIFY_OTP -> {
            VerifyOtpScreen(
                fullPhoneNumber = "$tempCountryCode $tempPhoneNumber",
                generatedOtp = generatedOtp,
                isVerifying = isVerifyingOtp,
                onVerifyOtp = { otp ->
                    viewModel.verifyOtp(otp)
                },
                onEditNumber = {
                    viewModel.editPhoneNumber()
                }
            )
        }
        AppScreen.PROFILE_SETUP -> {
            ProfileSetupScreen(
                isInitializing = isInitializingProfile,
                onCompleteProfile = { name, about, avatarColor ->
                    viewModel.completeProfile(name, about, avatarColor)
                }
            )
        }
        AppScreen.MAIN -> {
            MainScreen(
                user = user,
                chats = chats,
                statuses = filteredStatuses,
                calls = calls,
                meetings = meetings,
                searchQuery = searchQuery,
                selectedFilter = selectedFilter,
                statusFilter = statusFilter,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onFilterChange = { viewModel.setChatFilter(it) },
                onStatusFilterChange = { viewModel.setStatusFilter(it) },
                onOpenChat = { contact -> viewModel.openChat(contact) },
                onOpenStatusViewer = { status -> viewModel.openStatusViewer(status) },
                onStartCall = { name, phone, color, type ->
                    viewModel.startCall(name, phone, color, type)
                },
                onPostStatus = { text, color, filterName -> viewModel.addNewStatus(text, color, filterName) },
                onAddNewContact = { name, phone, about -> viewModel.addNewContact(name, phone, about) },
                onStartInstantMeeting = { title -> viewModel.startInstantMeeting(title) },
                onJoinMeetingWithCode = { code -> viewModel.joinMeetingWithCode(code) {} },
                onScheduleMeeting = { title, duration, time -> viewModel.scheduleMeeting(title, duration, time) },
                onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                onLogout = { viewModel.logout() }
            )
        }
        AppScreen.CHAT_CONVERSATION -> {
            val contact = selectedContact
            if (contact != null) {
                ChatScreen(
                    contact = contact,
                    messages = currentChatMessages,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onSendVoiceNote = { viewModel.sendVoiceNote() },
                    onSendImage = { uri, caption -> viewModel.sendImageMessage(uri, caption) },
                    onStartCall = { name, phone, color, type ->
                        viewModel.startCall(name, phone, color, type)
                    },
                    onBack = { viewModel.closeChat() }
                )
            } else {
                viewModel.navigateTo(AppScreen.MAIN)
            }
        }
        AppScreen.STATUS_VIEWER -> {
            val status = activeStatus
            if (status != null) {
                StatusViewerScreen(
                    status = status,
                    onReact = { statusId -> viewModel.reactToStatus(statusId) },
                    onClose = { viewModel.closeStatusViewer() }
                )
            } else {
                viewModel.navigateTo(AppScreen.MAIN)
            }
        }
        AppScreen.CALL_SCREEN -> {
            val call = activeCall
            if (call != null) {
                CallScreen(
                    call = call,
                    onToggleMute = { viewModel.toggleCallMute() },
                    onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                    onEndCall = { viewModel.endCall() }
                )
            } else {
                viewModel.navigateTo(AppScreen.MAIN)
            }
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                user = user,
                onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                onLogout = { viewModel.logout() }
            )
        }
        AppScreen.MEETING_ROOM -> {
            MeetingRoomScreen(
                viewModel = viewModel
            )
        }
    }
}
