package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MeChatAccent,
    onPrimary = Color.White,
    primaryContainer = MeChatDarkGreen,
    onPrimaryContainer = Color.White,
    secondary = MeChatLightGreen,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF2A3942),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF374248)
)

private val LightColorScheme = lightColorScheme(
    primary = MeChatGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8FDD2),
    onPrimaryContainer = MeChatDarkGreen,
    secondary = MeChatLightGreen,
    onSecondary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFE9EDEF)
)

@Composable
fun MeChatTheme(
    darkTheme: Boolean = false, // Always enforce crisp, clean light theme as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun WhatsAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = MeChatTheme(darkTheme = false, content = content)
