package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.MotionPhotosOn
import androidx.compose.material.icons.outlined.VideoCameraFront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatSummary
import com.example.data.model.ContactEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.StatusUpdateEntity
import com.example.data.model.UserAccount
import com.example.ui.components.AddContactDialog
import com.example.ui.components.ContactAvatar
import com.example.ui.components.CreateChannelDialog
import com.example.ui.components.CreateStatusDialog
import com.example.ui.components.MessageStatusTicks
import com.example.ui.theme.MeChatDarkGreen
import com.example.ui.theme.MeChatGreen
import com.example.ui.theme.MeChatLightGreen
import com.example.ui.theme.MeChatLightMint
import com.example.ui.theme.UnreadBadgeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: UserAccount?,
    chats: List<ChatSummary>,
    statuses: List<StatusUpdateEntity>,
    channels: List<ChannelEntity>,
    calls: List<CallLogEntity>,
    meetings: List<MeetingEntity>,
    searchQuery: String,
    selectedFilter: String,
    statusFilter: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onOpenChat: (ContactEntity) -> Unit,
    onOpenStatusViewer: (StatusUpdateEntity) -> Unit,
    onStartCall: (name: String, phone: String, color: Long, type: String) -> Unit,
    onPostStatus: (text: String, color: Long, filterName: String, mediaType: String, mediaUri: String?, songTitle: String?, songArtist: String?) -> Unit,
    onAddNewContact: (name: String, phone: String, about: String) -> Unit,
    onStartInstantMeeting: (String) -> Unit,
    onJoinMeetingWithCode: (String) -> Unit,
    onScheduleMeeting: (title: String, duration: Int, time: Long) -> Unit,
    onOpenChannel: (ChannelEntity) -> Unit,
    onCreateChannel: (name: String, description: String, category: String) -> Unit,
    onToggleFollowChannel: (ChannelEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Updates, 2: Meetings, 3: Calls
    var isSearchActive by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showCreateStatusDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }

    val totalUnread = chats.sumOf { it.contact.unreadCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search...", color = Color.Gray, fontSize = 16.sp) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    onSearchQueryChange("")
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.Gray)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("chat_search_input")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.mechat_logo),
                                contentDescription = "MeChat Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MeChat",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeChatGreen
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(
                            onClick = { /* Camera feature */ },
                            modifier = Modifier.testTag("main_camera_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("main_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { isMenuExpanded = true },
                                modifier = Modifier.testTag("main_overflow_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("New group") },
                                    onClick = {
                                        isMenuExpanded = false
                                        showAddContactDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Start instant meeting") },
                                    onClick = {
                                        isMenuExpanded = false
                                        onStartInstantMeeting("MeChat Quick Meeting")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Starred messages") },
                                    onClick = { isMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        isMenuExpanded = false
                                        onOpenSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout", color = Color.Red) },
                                    onClick = {
                                        isMenuExpanded = false
                                        onLogout()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                // Chats
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        if (totalUnread > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = UnreadBadgeGreen,
                                        contentColor = Color.White
                                    ) {
                                        Text("$totalUnread")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Chat else Icons.Outlined.Chat,
                                contentDescription = "Chats"
                            )
                        }
                    },
                    label = { Text("Chats", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD8FDD2),
                        selectedIconColor = MeChatGreen
                    ),
                    modifier = Modifier.testTag("nav_chats_tab")
                )

                // Updates
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.MotionPhotosOn,
                            contentDescription = "Updates"
                        )
                    },
                    label = { Text("Updates", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD8FDD2),
                        selectedIconColor = MeChatGreen
                    ),
                    modifier = Modifier.testTag("nav_updates_tab")
                )

                // Meetings
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.VideoCameraFront else Icons.Outlined.VideoCameraFront,
                            contentDescription = "Meetings"
                        )
                    },
                    label = { Text("Meetings", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD8FDD2),
                        selectedIconColor = MeChatGreen
                    ),
                    modifier = Modifier.testTag("nav_meetings_tab")
                )

                // Calls
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Call else Icons.Outlined.Call,
                            contentDescription = "Calls"
                        )
                    },
                    label = { Text("Calls", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD8FDD2),
                        selectedIconColor = MeChatGreen
                    ),
                    modifier = Modifier.testTag("nav_calls_tab")
                )
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = { showAddContactDialog = true },
                        containerColor = MeChatLightGreen,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("new_chat_fab")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "New Chat")
                    }
                }
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { showCreateStatusDialog = true },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            elevation = FloatingActionButtonDefaults.elevation(2.dp),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp).testTag("text_status_fab")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Text Status", modifier = Modifier.size(20.dp))
                        }

                        FloatingActionButton(
                            onClick = { showCreateStatusDialog = true },
                            containerColor = MeChatLightGreen,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("camera_status_fab")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Add Status")
                        }
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { onStartInstantMeeting("MeChat Instant Room") },
                        containerColor = MeChatGreen,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("new_meeting_fab")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Instant Meeting")
                    }
                }
                3 -> {
                    FloatingActionButton(
                        onClick = { showAddContactDialog = true },
                        containerColor = MeChatLightGreen,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("new_call_fab")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "New Call")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> ChatsTabContent(
                    chats = chats,
                    selectedFilter = selectedFilter,
                    onFilterChange = onFilterChange,
                    onOpenChat = onOpenChat
                )
                1 -> UpdatesTabContent(
                    user = user,
                    statuses = statuses,
                    channels = channels,
                    statusFilter = statusFilter,
                    onStatusFilterChange = onStatusFilterChange,
                    onOpenStatusViewer = onOpenStatusViewer,
                    onCreateStatus = { showCreateStatusDialog = true },
                    onOpenChannel = onOpenChannel,
                    onCreateChannelClick = { showCreateChannelDialog = true },
                    onToggleFollowChannel = onToggleFollowChannel
                )
                2 -> MeetingsTabContent(
                    user = user,
                    meetings = meetings,
                    onStartInstantMeeting = onStartInstantMeeting,
                    onJoinMeetingWithCode = onJoinMeetingWithCode,
                    onScheduleMeeting = onScheduleMeeting
                )
                3 -> CallsTabContent(
                    calls = calls,
                    onStartCall = onStartCall
                )
            }
        }
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onSave = onAddNewContact
        )
    }

    if (showCreateStatusDialog) {
        CreateStatusDialog(
            onDismiss = { showCreateStatusDialog = false },
            onPost = onPostStatus
        )
    }

    if (showCreateChannelDialog) {
        CreateChannelDialog(
            onDismiss = { showCreateChannelDialog = false },
            onCreate = onCreateChannel
        )
    }
}

@Composable
private fun ChatsTabContent(
    chats: List<ChatSummary>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onOpenChat: (ContactEntity) -> Unit
) {
    val filters = listOf("All", "Unread", "Favourites", "Groups")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Filter chips row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD8FDD2),
                            selectedLabelColor = MeChatGreen,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MeChatGreen else Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }
        }

        // Archived row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Archived chats */ }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = "Archived",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "Archived",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Chat list
        if (chats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "No chats",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No chats found",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(chats, key = { it.contact.id }) { item ->
                ChatItemRow(
                    summary = item,
                    onClick = { onOpenChat(item.contact) }
                )
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    summary: ChatSummary,
    onClick: () -> Unit
) {
    val contact = summary.contact
    val lastMsg = summary.lastMessage

    val timeFormatted = remember(lastMsg?.timestamp) {
        if (lastMsg != null) {
            val date = Date(lastMsg.timestamp)
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        } else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("chat_row_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = contact.name,
            avatarColor = contact.avatarColor,
            isGroup = contact.isGroup,
            size = 52.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (timeFormatted.isNotEmpty()) {
                    Text(
                        text = timeFormatted,
                        fontSize = 12.sp,
                        color = if (contact.unreadCount > 0) UnreadBadgeGreen else Color.Gray,
                        fontWeight = if (contact.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (lastMsg != null && lastMsg.isOutgoing) {
                        MessageStatusTicks(status = lastMsg.status)
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = lastMsg?.text ?: contact.about,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (contact.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(UnreadBadgeGreen)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${contact.unreadCount}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdatesTabContent(
    user: UserAccount?,
    statuses: List<StatusUpdateEntity>,
    channels: List<ChannelEntity>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onOpenStatusViewer: (StatusUpdateEntity) -> Unit,
    onCreateStatus: () -> Unit,
    onOpenChannel: (ChannelEntity) -> Unit,
    onCreateChannelClick: () -> Unit,
    onToggleFollowChannel: (ChannelEntity) -> Unit
) {
    val myStatuses = statuses.filter { it.isMine }
    val otherStatuses = statuses.filter { !it.isMine }

    val filterOptions = listOf("All", "Top Score", "Recent", "Unviewed", "My Status")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Status header
        item {
            Text(
                text = "Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Status Filter Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == statusFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusFilterChange(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    "Top Score" -> "⭐ $filter"
                                    "Recent" -> "🕒 $filter"
                                    "Unviewed" -> "👀 $filter"
                                    "My Status" -> "👤 $filter"
                                    else -> filter
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD8FDD2),
                            selectedLabelColor = MeChatGreen
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("status_filter_$filter")
                    )
                }
            }
        }

        // My Status item
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (myStatuses.isNotEmpty()) onOpenStatusViewer(myStatuses.first())
                        else onCreateStatus()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ContactAvatar(
                        name = user?.displayName ?: "Me",
                        avatarColor = user?.avatarColor ?: 0xFF008069,
                        imageUri = user?.profilePhotoUri,
                        hasStatusUpdate = myStatuses.isNotEmpty(),
                        size = 54.dp
                    )
                    if (myStatuses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MeChatLightGreen)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Status", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "My Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (myStatuses.isNotEmpty()) "Tap to view • ${myStatuses.first().viewsCount} views • ⭐ Score ${myStatuses.first().score}" else "Tap to add photo, video or gana status",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Recent / Filtered updates header
        if (otherStatuses.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (statusFilter == "All") "Recent updates" else "$statusFilter updates",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "${otherStatuses.size} updates",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(otherStatuses) { status ->
                val timeStr = remember(status.timestamp) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(status.timestamp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStatusViewer(status) }
                        .padding(vertical = 8.dp)
                        .testTag("status_item_${status.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        name = status.authorName,
                        avatarColor = status.avatarColor,
                        hasStatusUpdate = true,
                        isStatusViewed = status.isViewed,
                        size = 54.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = status.authorName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            // Status Score Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "⭐ Score: ${status.score}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Today, $timeStr",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            // Media indicator tag (Photo / Video)
                            if (status.mediaType == "photo") {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE0F2FE)
                                ) {
                                    Text(
                                        text = "📷 Photo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else if (status.mediaType == "video") {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFCE7F3)
                                ) {
                                    Text(
                                        text = "🎬 Video",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBE185D),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Song / Gana tag
                            if (!status.songTitle.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEDE9FE)
                                ) {
                                    Text(
                                        text = "🎵 ${status.songTitle}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9),
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Channels section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Channels",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Stay updated on topics you care about",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // + Create Channel Button
                Button(
                    onClick = onCreateChannelClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD8FDD2)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("create_channel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Channel",
                        tint = MeChatGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Create",
                        color = MeChatGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Channels List
        items(channels, key = { it.id }) { channel ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChannel(channel) }
                    .padding(vertical = 8.dp)
                    .testTag("channel_item_${channel.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ContactAvatar(name = channel.name, avatarColor = channel.iconColor, size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (channel.isOwner) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Owner",
                                        fontSize = 10.sp,
                                        color = Color(0xFF92400E),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = channel.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatFollowers(channel.followerCount)} followers",
                            fontSize = 11.sp,
                            color = MeChatGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (channel.isFollowed) Color(0xFFF3F4F6) else Color(0xFFD8FDD2),
                    modifier = Modifier.clickable { onToggleFollowChannel(channel) }
                ) {
                    Text(
                        text = if (channel.isFollowed) "Following" else "Follow",
                        color = if (channel.isFollowed) Color.DarkGray else MeChatGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

private fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", count / 1_000.0)
        else -> "$count"
    }
}

@Composable
private fun CallsTabContent(
    calls: List<CallLogEntity>,
    onStartCall: (name: String, phone: String, color: Long, type: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Create call link
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MeChatLightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Call Link",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Create call link",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Share a link for your MeChat call",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Recent",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(calls) { call ->
            val timeStr = remember(call.timestamp) {
                SimpleDateFormat("MMMM dd, hh:mm a", Locale.getDefault()).format(Date(call.timestamp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ContactAvatar(name = call.contactName, avatarColor = call.avatarColor, size = 50.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = call.contactName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (call.direction == "missed") Color.Red else MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (dirIcon, dirTint) = when (call.direction) {
                                "incoming" -> Icons.Default.CallReceived to MeChatGreen
                                "outgoing" -> Icons.Default.CallMade to MeChatGreen
                                else -> Icons.Default.CallReceived to Color.Red
                            }
                            Icon(
                                imageVector = dirIcon,
                                contentDescription = call.direction,
                                tint = dirTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = timeStr, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                IconButton(
                    onClick = {
                        onStartCall(call.contactName, call.contactPhone, call.avatarColor, call.callType)
                    }
                ) {
                    Icon(
                        imageVector = if (call.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call",
                        tint = MeChatGreen
                    )
                }
            }
        }
    }
}
