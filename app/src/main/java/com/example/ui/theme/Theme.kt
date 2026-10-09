package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BangEmerald,
    onPrimary = Color(0xFF003828),
    primaryContainer = BangEmeraldDark,
    onPrimaryContainer = Color(0xFFA6F5D0),
    secondary = BangElectricBlue,
    onSecondary = Color.White,
    background = BangDarkBackground,
    onBackground = BangDarkTextPrimary,
    surface = BangDarkSurface,
    onSurface = BangDarkTextPrimary,
    surfaceVariant = BangDarkSurfaceVariant,
    onSurfaceVariant = BangDarkTextSecondary,
    outline = BangDarkBorder,
    error = BangError
)

private val LightColorScheme = lightColorScheme(
    primary = BangEmeraldDark,
    onPrimary = Color.White,
    primaryContainer = BangEmeraldLight,
    onPrimaryContainer = Color(0xFF003828),
    secondary = BangElectricBlue,
    onSecondary = Color.White,
    background = BangLightBackground,
    onBackground = BangLightTextPrimary,
    surface = BangLightSurface,
    onSurface = BangLightTextPrimary,
    surfaceVariant = BangLightSurfaceVariant,
    onSurfaceVariant = BangLightTextSecondary,
    outline = BangLightBorder,
    error = BangError
)

@Composable
fun BangChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
