package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.MeChatGreen
import com.example.ui.theme.MeChatLightGreen
import com.example.ui.theme.MeChatTeal

@Composable
fun ProfileSetupScreen(
    isInitializing: Boolean,
    onCompleteProfile: (name: String, about: String, avatarColor: Long, profilePhotoUri: String?) -> Unit
) {
    val avatarColors = listOf(
        0xFF008069, 0xFF128C7E, 0xFFE91E63, 0xFF3F51B5, 0xFFE65100, 0xFF9C27B0
    )
    var selectedColor by remember { mutableStateOf(avatarColors[0]) }
    var name by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Hey there! I am using MeChat.") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profilePhotoUri = uri.toString()
        }
    }

    val presetAvatars = listOf(
        "Modern 1" to "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
        "Modern 2" to "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
        "Modern 3" to "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isInitializing) {
            // MeChat Initializing screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MeChatGreen,
                    modifier = Modifier.size(54.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Initializing...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please wait a moment",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Profile info",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MeChatTeal
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Please provide your name and an optional profile picture",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Avatar Selector
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (name.isNotBlank()) {
                                Text(
                                    text = name.take(1).uppercase(),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        // Camera overlay badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color palette for avatar
                    Text("Select Avatar Color:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        avatarColors.forEach { colorVal ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .clickable { selectedColor = colorVal }
                                    .then(
                                        if (selectedColor == colorVal) {
                                            Modifier.border(2.dp, MeChatLightGreen, CircleShape)
                                        } else Modifier
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Name Input Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            if (it.length <= 25) name = it
                        },
                        placeholder = { Text("Type your name here") },
                        singleLine = true,
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "${25 - name.length}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Mood,
                                    contentDescription = "Emoji",
                                    tint = Color.Gray
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("profile_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeChatGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // About field
                    OutlinedTextField(
                        value = about,
                        onValueChange = { about = it },
                        label = { Text("About") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("profile_about_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeChatGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }

                // Next Button
                Button(
                    onClick = {
                        val finalName = if (name.isBlank()) "User" else name.trim()
                        onCompleteProfile(finalName, about.trim(), selectedColor)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(44.dp)
                        .testTag("profile_next_button"),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen)
                ) {
                    Text(
                        text = "NEXT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
