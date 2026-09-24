package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MeChatDarkGreen
import com.example.ui.theme.MeChatGreen
import com.example.ui.theme.MeChatLightMint
import com.example.ui.viewmodel.ActiveMeetingSession
import com.example.ui.viewmodel.MeetingChatMessage
import com.example.ui.viewmodel.MeetingParticipant
import com.example.ui.viewmodel.WhatsAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRoomScreen(
    viewModel: WhatsAppViewModel
) {
    val activeSession by viewModel.activeMeeting.collectAsState()
    val context = LocalContext.current
    var showChatSheet by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.leaveMeeting()
    }

    val session = activeSession
    if (session == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Text("Meeting ended", color = Color.White, fontSize = 18.sp)
        }
        return
    }

    val minutes = session.elapsedSeconds / 60
    val seconds = session.elapsedSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121B22))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("meeting_room_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Meeting Header
            MeetingTopBar(
                title = session.meeting.title,
                meetingCode = session.meeting.meetingCode,
                timer = timerFormatted,
                participantCount = session.participants.size,
                onCopyCode = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("MeChat Meeting", session.meeting.meetingCode))
                    Toast.makeText(context, "Meeting Code copied: ${session.meeting.meetingCode}", Toast.LENGTH_SHORT).show()
                },
                onSwitchCamera = {
                    viewModel.switchMeetingCamera()
                    val mode = if (session.isFrontCamera) "Rear Camera" else "Front Camera"
                    Toast.makeText(context, "Switched to $mode", Toast.LENGTH_SHORT).show()
                },
                onLeave = {
                    viewModel.leaveMeeting()
                }
            )

            // Video Grid Tiles
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                MeetingParticipantGrid(
                    participants = session.participants,
                    isSelfMuted = session.isMuted,
                    isSelfVideoOff = session.isVideoOff,
                    isSelfHandRaised = session.isHandRaised,
                    isFrontCamera = session.isFrontCamera
                )
            }

            // Bottom Control Bar
            MeetingControlsBar(
                isMuted = session.isMuted,
                isVideoOff = session.isVideoOff,
                isHandRaised = session.isHandRaised,
                unreadChatCount = session.messages.size,
                onToggleMute = { viewModel.toggleMeetingMute() },
                onToggleVideo = { viewModel.toggleMeetingVideo() },
                onToggleHand = {
                    viewModel.toggleMeetingHand()
                    val handMsg = if (!session.isHandRaised) "Hand raised ✋" else "Hand lowered"
                    Toast.makeText(context, handMsg, Toast.LENGTH_SHORT).show()
                },
                onOpenChat = { showChatSheet = true },
                onShareInvite = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val invite = "Join my MeChat Meeting!\nCode: ${session.meeting.meetingCode}\nLink: https://mechat.app/join/${session.meeting.meetingCode}"
                    clipboard.setPrimaryClip(ClipData.newPlainText("MeChat Invite", invite))
                    Toast.makeText(context, "Invite link copied to clipboard!", Toast.LENGTH_LONG).show()
                },
                onEndMeeting = { viewModel.leaveMeeting() }
            )
        }

        // In-Meeting Chat BottomSheet
        if (showChatSheet) {
            MeetingChatSheet(
                messages = session.messages,
                onSendMessage = { text -> viewModel.sendMeetingChatMessage(text) },
                onDismiss = { showChatSheet = false }
            )
        }
    }
}

@Composable
fun MeetingTopBar(
    title: String,
    meetingCode: String,
    timer: String,
    participantCount: Int,
    onCopyCode: () -> Unit,
    onSwitchCamera: () -> Unit,
    onLeave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onLeave,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Leave Meeting",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Code badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF263238))
                        .clickable { onCopyCode() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = meetingCode,
                        color = MeChatLightMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = MeChatLightMint,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Timer badge
                Text(
                    text = "• $timer",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Participant count
                Text(
                    text = "• 👥 $participantCount",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        IconButton(
            onClick = onSwitchCamera,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = Color.White
            )
        }
    }
}

@Composable
fun MeetingParticipantGrid(
    participants: List<MeetingParticipant>,
    isSelfMuted: Boolean,
    isSelfVideoOff: Boolean,
    isSelfHandRaised: Boolean,
    isFrontCamera: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Row: Self + Peer 1
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Self tile
            ParticipantTile(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                name = "You",
                avatarColor = 0xFF008069,
                isMuted = isSelfMuted,
                isVideoOff = isSelfVideoOff,
                isSpeaking = false,
                isHandRaised = isSelfHandRaised,
                isSelf = true
            )

            // Participant 1
            val p1 = participants.getOrNull(1)
            if (p1 != null) {
                ParticipantTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    name = p1.name,
                    avatarColor = p1.avatarColor,
                    isMuted = p1.isMuted,
                    isVideoOff = p1.isVideoOff,
                    isSpeaking = p1.isSpeaking,
                    isHandRaised = p1.isHandRaised,
                    isSelf = false
                )
            }
        }

        // Bottom Row: Participant 2 + Participant 3 (or full tile if 1)
        val p2 = participants.getOrNull(2)
        if (p2 != null) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ParticipantTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    name = p2.name,
                    avatarColor = p2.avatarColor,
                    isMuted = p2.isMuted,
                    isVideoOff = p2.isVideoOff,
                    isSpeaking = p2.isSpeaking,
                    isHandRaised = p2.isHandRaised,
                    isSelf = false
                )

                // Placeholder / Meeting Link tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2A38))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F766E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Invite More",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Share link with friends",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantTile(
    modifier: Modifier = Modifier,
    name: String,
    avatarColor: Long,
    isMuted: Boolean,
    isVideoOff: Boolean,
    isSpeaking: Boolean,
    isHandRaised: Boolean,
    isSelf: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    val borderModifier = if (isSpeaking) {
        Modifier.border(pulseBorder.dp, MeChatGreen, RoundedCornerShape(16.dp))
    } else {
        Modifier.border(1.dp, Color(0xFF263544), RoundedCornerShape(16.dp))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E2833))
            .then(borderModifier)
    ) {
        if (!isVideoOff) {
            // Simulated live video stream with ambient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(avatarColor).copy(alpha = 0.55f),
                                Color(0xFF0F172A)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(avatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isSpeaking) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MeChatGreen.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Speaking...", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Video turned off
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF161F28)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VideocamOff,
                            contentDescription = "Video off",
                            tint = Color.LightGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Camera Off", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // Top badges: Hand raised
        if (isHandRaised) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFC107))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✋ Hand Raised", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom label: Name + Mic state
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 100.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isMuted) "Muted" else "Unmuted",
                tint = if (isMuted) Color(0xFFEF4444) else MeChatGreen,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun MeetingControlsBar(
    isMuted: Boolean,
    isVideoOff: Boolean,
    isHandRaised: Boolean,
    unreadChatCount: Int,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleHand: () -> Unit,
    onOpenChat: () -> Unit,
    onShareInvite: () -> Unit,
    onEndMeeting: () -> Unit
) {
    Surface(
        color = Color(0xFF1E2833),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute
            MeetingActionButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute",
                isActive = !isMuted,
                activeColor = MeChatGreen,
                inactiveColor = Color(0xFFEF4444),
                onClick = onToggleMute
            )

            // Video On / Off
            MeetingActionButton(
                icon = if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                label = if (isVideoOff) "Start Video" else "Stop Video",
                isActive = !isVideoOff,
                activeColor = MeChatGreen,
                inactiveColor = Color(0xFFEF4444),
                onClick = onToggleVideo
            )

            // Raise Hand
            MeetingActionButton(
                icon = Icons.Default.PanTool,
                label = if (isHandRaised) "Lower" else "Raise Hand",
                isActive = isHandRaised,
                activeColor = Color(0xFFFFC107),
                inactiveColor = Color(0xFF334155),
                tint = if (isHandRaised) Color.Black else Color.White,
                onClick = onToggleHand
            )

            // In-Meeting Chat
            MeetingActionButton(
                icon = Icons.Default.Chat,
                label = "Chat",
                badge = if (unreadChatCount > 0) "$unreadChatCount" else null,
                isActive = false,
                activeColor = MeChatGreen,
                inactiveColor = Color(0xFF334155),
                onClick = onOpenChat
            )

            // Share link
            MeetingActionButton(
                icon = Icons.Default.Share,
                label = "Invite",
                isActive = false,
                activeColor = MeChatGreen,
                inactiveColor = Color(0xFF334155),
                onClick = onShareInvite
            )

            // End Meeting
            IconButton(
                onClick = onEndMeeting,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .testTag("end_meeting_button")
            ) {
                Icon(
                    Icons.Default.CallEnd,
                    contentDescription = "Leave Meeting",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MeetingActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    tint: Color = Color.White,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else inactiveColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MeChatGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingChatSheet(
    messages: List<MeetingChatMessage>,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var textInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E2833),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "In-Meeting Messages",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            Text(
                text = "Messages are visible only to participants in this call.",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (msg.isMe) Color(0xFF005C4B) else Color(0xFF263544))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg.senderName,
                                color = if (msg.isMe) MeChatLightMint else Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = msg.time,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = msg.text,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Chat input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Send a message to everyone...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MeChatGreen,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF161F28),
                        unfocusedContainerColor = Color(0xFF161F28)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MeChatGreen)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
