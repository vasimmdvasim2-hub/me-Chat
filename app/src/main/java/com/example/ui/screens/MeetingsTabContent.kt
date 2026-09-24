package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeetingEntity
import com.example.data.model.UserAccount
import com.example.ui.theme.MeChatDarkGreen
import com.example.ui.theme.MeChatGreen
import com.example.ui.theme.MeChatLightMint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeetingsTabContent(
    user: UserAccount?,
    meetings: List<MeetingEntity>,
    onStartInstantMeeting: (String) -> Unit,
    onJoinMeetingWithCode: (String) -> Unit,
    onScheduleMeeting: (title: String, duration: Int, time: Long) -> Unit
) {
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showInstantDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("meetings_tab_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MeChatGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Meeting",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MeChat Meetings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MeChatDarkGreen
                        )
                        Text(
                            text = "Host or join HD video meetings with encrypted voice and screen sharing.",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Action Buttons: New Meeting, Join with Code, Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // New Meeting Button
                Button(
                    onClick = { showInstantDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("new_meeting_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VideoCameraFront, contentDescription = "New Meeting", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Join Button
                Button(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("join_meeting_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = "Join with Code", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Join", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Schedule Button
                Button(
                    onClick = { showScheduleDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("schedule_meeting_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Personal Meeting Link Card
        item {
            val personalCode = remember(user) {
                val clean = user?.displayName?.filter { it.isLetterOrDigit() }?.lowercase()?.take(6) ?: "user"
                "mct-$clean-room"
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MeChatGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Link, contentDescription = "Link", tint = MeChatGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Your Personal Room", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Code: $personalCode", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Row {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("MeChat Meeting", personalCode))
                            Toast.makeText(context, "Copied code: $personalCode", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MeChatGreen, modifier = Modifier.size(20.dp))
                        }

                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("MeChat Invite", "Join my personal MeChat room: https://mechat.app/join/$personalCode"))
                            Toast.makeText(context, "Invite link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MeChatGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Section Title: Active & Upcoming Meetings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meetings & Conferences",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${meetings.size} rooms",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // List of Meetings
        if (meetings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "No meetings",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active meetings", color = Color.Gray, fontSize = 14.sp)
                        Text("Tap 'New' or 'Schedule' to create one", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(meetings) { meeting ->
                MeetingItemCard(
                    meeting = meeting,
                    onJoin = { onJoinMeetingWithCode(meeting.meetingCode) },
                    onCopyLink = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MeChat Meeting", meeting.meetingCode))
                        Toast.makeText(context, "Copied meeting code: ${meeting.meetingCode}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // --- Dialogs ---

    // 1. Instant Meeting Dialog
    if (showInstantDialog) {
        var title by remember { mutableStateOf("Quick Team Sync") }
        AlertDialog(
            onDismissRequest = { showInstantDialog = false },
            title = { Text("Start Instant Meeting", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter meeting topic or title:", fontSize = 13.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth().testTag("instant_meeting_title_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInstantDialog = false
                        onStartInstantMeeting(title)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen)
                ) {
                    Text("Start Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstantDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // 2. Join with Code Dialog
    if (showJoinDialog) {
        var inputCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join Meeting with Code", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the 9-character code (e.g. mct-489-215):", fontSize = 13.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        placeholder = { Text("e.g. mct-712-904") },
                        modifier = Modifier.fillMaxWidth().testTag("meeting_code_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            showJoinDialog = false
                            onJoinMeetingWithCode(inputCode.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                    modifier = Modifier.testTag("confirm_join_code_btn")
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // 3. Schedule Meeting Dialog
    if (showScheduleDialog) {
        var title by remember { mutableStateOf("Weekly Project Review") }
        var duration by remember { mutableIntStateOf(30) }
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule a Meeting", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Meeting Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Duration:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (duration == mins) MeChatGreen else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { duration = mins }
                                    .padding(vertical = 4.dp),
                                contentColor = if (duration == mins) Color.White else MaterialTheme.colorScheme.onSurface
                            ) {
                                Text(
                                    text = "$mins m",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            showScheduleDialog = false
                            val scheduledTime = System.currentTimeMillis() + (duration * 60 * 1000)
                            onScheduleMeeting(title.trim(), duration, scheduledTime)
                            Toast.makeText(context, "Meeting '$title' scheduled!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun MeetingItemCard(
    meeting: MeetingEntity,
    onJoin: () -> Unit,
    onCopyLink: () -> Unit
) {
    val dateStr = remember(meeting.startTime) {
        SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.getDefault()).format(Date(meeting.startTime))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meeting.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Host: ${meeting.hostName} • $dateStr",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Status Pill
                if (meeting.status == "active") {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🔴 LIVE",
                            color = Color(0xFF15803D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "📅 ${meeting.durationMinutes}m",
                            color = Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onCopyLink() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = meeting.meetingCode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MeChatDarkGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = MeChatDarkGreen,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("join_meeting_card_${meeting.id}")
                ) {
                    Text("Join", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
