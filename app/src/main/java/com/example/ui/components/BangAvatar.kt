package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangEmeraldDark
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline

@Composable
fun BangAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isOnline: Boolean? = null,
    isGroup: Boolean = false
) {
    // Determine dynamic avatar background colors from name hash
    val colors = listOf(
        Pair(Color(0xFF00C896), Color(0xFF007A5A)),
        Pair(Color(0xFF0EA5E9), Color(0xFF0369A1)),
        Pair(Color(0xFF8B5CF6), Color(0xFF5B21B6)),
        Pair(Color(0xFFF59E0B), Color(0xFFB45309)),
        Pair(Color(0xFFEC4899), Color(0xFF9D174D))
    )
    val colorIndex = Math.abs(name.hashCode()) % colors.size
    val (c1, c2) = colors[colorIndex]

    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "BC" }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(c1, c2))),
            contentAlignment = Alignment.Center
        ) {
            if (isGroup) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            } else if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = name,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        if (isOnline != null && !isGroup) {
            val indicatorSize = size * 0.28f
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(if (isOnline) StatusOnline else StatusOffline)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
