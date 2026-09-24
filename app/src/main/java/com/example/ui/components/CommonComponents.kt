package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CheckBlue
import com.example.ui.theme.CheckGrey
import com.example.ui.theme.MeChatGreen
import com.example.ui.theme.MeChatLightGreen

@Composable
fun ContactAvatar(
    name: String,
    avatarColor: Long,
    size: Dp = 48.dp,
    imageUri: String? = null,
    isGroup: Boolean = false,
    hasStatusUpdate: Boolean = false,
    isStatusViewed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val color = Color(avatarColor)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .then(
                if (hasStatusUpdate) {
                    Modifier
                        .padding(2.dp)
                        .border(
                            width = 2.dp,
                            color = if (isStatusViewed) Color.LightGray else MeChatLightGreen,
                            shape = CircleShape
                        )
                        .padding(3.dp)
                } else Modifier
            )
            .clip(CircleShape)
            .background(color)
    ) {
        if (!imageUri.isNullOrBlank()) {
            AsyncImage(
                model = imageUri,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(size)
                    .clip(CircleShape)
            )
        } else if (isGroup) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Group Avatar",
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}

@Composable
fun MessageStatusTicks(
    status: String,
    isOutgoing: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isOutgoing) return

    val icon = when (status) {
        "sent" -> Icons.Default.Check
        else -> Icons.Default.DoneAll
    }
    val tint = when (status) {
        "read" -> CheckBlue
        else -> CheckGrey
    }

    Icon(
        imageVector = icon,
        contentDescription = "Status",
        tint = tint,
        modifier = modifier.size(16.dp)
    )
}

@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MeChatGreen)
            }
        },
        title = {
            Text("Share Content", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentItem(
                        icon = Icons.Default.InsertDriveFile,
                        label = "Document",
                        color = Color(0xFF7F66FF)
                    ) { onOptionSelected("Document"); onDismiss() }

                    AttachmentItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Camera",
                        color = Color(0xFFD3396D)
                    ) { onOptionSelected("Photo"); onDismiss() }

                    AttachmentItem(
                        icon = Icons.Default.Photo,
                        label = "Gallery",
                        color = Color(0xFFAC44CF)
                    ) { onOptionSelected("Gallery Image"); onDismiss() }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentItem(
                        icon = Icons.Default.Audiotrack,
                        label = "Audio",
                        color = Color(0xFFE07A2F)
                    ) { onOptionSelected("Audio Recording"); onDismiss() }

                    AttachmentItem(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        color = Color(0xFF1EA75C)
                    ) { onOptionSelected("Live Location"); onDismiss() }

                    AttachmentItem(
                        icon = Icons.Default.Person,
                        label = "Contact",
                        color = Color(0xFF009DE2)
                    ) { onOptionSelected("Contact Card"); onDismiss() }
                }
            }
        }
    )
}

@Composable
private fun AttachmentItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, about: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Hey there! I am using MeChat.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_contact_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_contact_phone_input")
                )
                OutlinedTextField(
                    value = about,
                    onValueChange = { about = it },
                    label = { Text("About") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name.trim(), phone.trim(), about.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                modifier = Modifier.testTag("save_contact_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun CreateStatusDialog(
    onDismiss: () -> Unit,
    onPost: (text: String, colorHex: Long, filterName: String, mediaType: String, mediaUri: String?, songTitle: String?, songArtist: String?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("text") } // "text", "photo", "video"
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var selectedMediaLabel by remember { mutableStateOf<String?>(null) }

    var selectedSongTitle by remember { mutableStateOf<String?>(null) }
    var selectedSongArtist by remember { mutableStateOf<String?>(null) }
    var customSongInput by remember { mutableStateOf("") }
    var isAddingSong by remember { mutableStateOf(false) }

    val colors = listOf(
        0xFF008069, 0xFFE91E63, 0xFF9C27B0, 0xFF3F51B5,
        0xFFE65100, 0xFF009688, 0xFF455A64
    )
    val filters = listOf("Natural", "Emerald", "Sunset", "Vibrant", "Vintage", "Ganga Dawn", "Midnight")
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            selectedMediaLabel = "Device Photo"
            mediaType = "photo"
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            selectedMediaLabel = "Device Video"
            mediaType = "video"
        }
    }

    val samplePhotos = listOf(
        "Ganga Ghats 🌊" to "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?w=800",
        "Mountain Sunset 🌅" to "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
        "Coffee Coding ☕" to "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800"
    )

    val popularSongs = listOf(
        "Ganga Kinare (Ganga Dhun)" to "Traditional / Aarti",
        "Kesariya" to "Arijit Singh",
        "Tum Hi Ho" to "Arijit Singh",
        "Pehle Bhi Main" to "Vishal Mishra",
        "Apna Bana Le" to "Arijit Singh"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Status Update", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Media Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mediaType == "text",
                        onClick = { mediaType = "text"; selectedMediaUri = null; selectedMediaLabel = null },
                        label = { Text("✍️ Text", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = mediaType == "photo",
                        onClick = {
                            mediaType = "photo"
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        label = { Text("📷 Photo", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = mediaType == "video",
                        onClick = {
                            mediaType = "video"
                            videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        label = { Text("🎬 Video", fontSize = 12.sp) }
                    )
                }

                // Status Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(selectedColor))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (mediaType == "photo") {
                            Text("📷 Photo Status: ${selectedMediaLabel ?: "Photo Attached"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else if (mediaType == "video") {
                            Text("🎬 Video Status: ${selectedMediaLabel ?: "Video Clip"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Text(
                            text = text.ifBlank { "Type a status message..." },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "🎨 $selectedFilter",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (selectedSongTitle != null) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "🎵 $selectedSongTitle",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Sample photo presets if in photo mode
                if (mediaType == "photo") {
                    Text("Or choose sample photo:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        samplePhotos.forEach { (label, uri) ->
                            FilterChip(
                                selected = selectedMediaLabel == label,
                                onClick = {
                                    selectedMediaUri = uri
                                    selectedMediaLabel = label
                                },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Text caption
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(if (mediaType == "text") "Status text..." else "Add a caption...") },
                    modifier = Modifier.fillMaxWidth().testTag("status_text_input"),
                    singleLine = false,
                    maxLines = 2
                )

                // 🎵 GANA / SONG PICKER SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎵 Gana / Music Track:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MeChatGreen)
                    if (selectedSongTitle != null) {
                        TextButton(
                            onClick = {
                                selectedSongTitle = null
                                selectedSongArtist = null
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("Remove Song", fontSize = 11.sp, color = Color.Red)
                        }
                    }
                }

                // Quick Song Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularSongs.take(3).forEach { (song, artist) ->
                        FilterChip(
                            selected = selectedSongTitle == song,
                            onClick = {
                                selectedSongTitle = song
                                selectedSongArtist = artist
                            },
                            label = { Text("🎵 $song", fontSize = 10.sp) }
                        )
                    }
                }

                // Custom Song Input toggle
                if (isAddingSong) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customSongInput,
                            onValueChange = { customSongInput = it },
                            placeholder = { Text("Song name / Singer", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (customSongInput.isNotBlank()) {
                                    selectedSongTitle = customSongInput.trim()
                                    selectedSongArtist = "User Selected"
                                    isAddingSong = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen)
                        ) {
                            Text("Add", fontSize = 11.sp)
                        }
                    }
                } else {
                    TextButton(onClick = { isAddingSong = true }) {
                        Text("+ Custom Gana / Song name", fontSize = 11.sp, color = MeChatGreen)
                    }
                }

                // Filters
                Text("🎨 Filter Effect:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filters.take(4).forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) }
                        )
                    }
                }

                // Background Color
                Text("Background Color:", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .then(
                                    if (selectedColor == colorVal) {
                                        Modifier.border(2.dp, Color.Black, CircleShape)
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPostText = if (text.isNotBlank()) text.trim() else if (mediaType == "photo") "📷 New photo status" else if (mediaType == "video") "🎬 New video status" else "New status"
                    onPost(
                        finalPostText,
                        selectedColor,
                        selectedFilter,
                        mediaType,
                        selectedMediaUri,
                        selectedSongTitle,
                        selectedSongArtist
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                modifier = Modifier.testTag("post_status_button")
            ) {
                Text("Post Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Community") }
    val categories = listOf("Community", "Tech", "News", "Sports", "Music", "Lifestyle")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = "Channel", tint = MeChatGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create a Channel", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Anyone can discover your channel. Channels are public, so anyone can see your posts and updates.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Channel Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("channel_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Channel Description") },
                    placeholder = { Text("Describe what updates you will share...") },
                    modifier = Modifier.fillMaxWidth().testTag("channel_desc_input"),
                    maxLines = 3
                )

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(
                            name.trim(),
                            description.ifBlank { "Welcome to $name updates channel!" },
                            selectedCategory
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                modifier = Modifier.testTag("create_channel_confirm_button")
            ) {
                Text("Create Channel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EditProfileDialog(
    user: com.example.data.model.UserAccount?,
    onDismiss: () -> Unit,
    onSave: (name: String, about: String, photoUri: String?, color: Long) -> Unit
) {
    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var about by remember { mutableStateOf(user?.aboutStatus ?: "Hey there! I am using MeChat.") }
    var photoUri by remember { mutableStateOf(user?.profilePhotoUri) }
    var avatarColor by remember { mutableStateOf(user?.avatarColor ?: 0xFF008069) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val sampleAvatars = listOf(
        "Avatar 1" to "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
        "Avatar 2" to "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
        "Avatar 3" to "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile & Photo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Photo with Camera badge
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(90.dp)
                        .clickable {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    ContactAvatar(
                        name = name.ifBlank { "User" },
                        avatarColor = avatarColor,
                        imageUri = photoUri,
                        size = 90.dp
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MeChatGreen)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen)
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery Photo", fontSize = 12.sp)
                    }

                    if (photoUri != null) {
                        TextButton(onClick = { photoUri = null }) {
                            Text("Remove", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                Text("Or choose preset photo:", fontSize = 11.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sampleAvatars.forEach { (lbl, uri) ->
                        FilterChip(
                            selected = photoUri == uri,
                            onClick = { photoUri = uri },
                            label = { Text(lbl, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_edit_input")
                )

                OutlinedTextField(
                    value = about,
                    onValueChange = { about = it },
                    label = { Text("About Status") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_about_edit_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), about.trim(), photoUri, avatarColor)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

