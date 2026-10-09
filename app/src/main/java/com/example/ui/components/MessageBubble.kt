package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.ui.theme.BangElectricBlue
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    senderName: String,
    onReply: (Message) -> Unit,
    onReact: (Message, String) -> Unit,
    onEdit: (Message) -> Unit,
    onDelete: (Message) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    val bubbleBg = if (isMe) {
        Color(0xFF005C4B) // Bang dark emerald bubble
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = timeFormat.format(Date(message.createdAt))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Group Chat sender display
        if (!isMe && senderName.isNotBlank()) {
            Text(
                text = senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BangEmerald,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box {
            Surface(
                shape = bubbleShape,
                color = bubbleBg,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    )
                    .testTag("message_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    // Replying banner preview
                    if (message.replyToContent != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMe) Color(0x33000000) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .background(BangEmerald, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = message.replyToSenderName ?: "ข้อความ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BangEmerald
                                    )
                                    Text(
                                        text = message.replyToContent,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Content rendering based on MessageType
                    when (message.type) {
                        MessageType.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🖼️", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = message.content.ifEmpty { "รูปภาพที่แชร์" },
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        MessageType.AUDIO -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(text = "🎤", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ข้อความเสียง (0:14)",
                                    fontSize = 13.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        MessageType.SYSTEM -> {
                            Text(
                                text = message.content,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        else -> {
                            Text(
                                text = message.content,
                                fontSize = 14.5.sp,
                                color = textColor,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Footer: Timestamp + Status ticks + Edited indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (message.editedAt != null) {
                            Text(
                                text = "แก้ไขแล้ว",
                                fontSize = 9.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = timeText,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )

                        if (isMe) {
                            when (message.status) {
                                MessageStatus.SENDING -> {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "กำลังส่ง",
                                        modifier = Modifier.size(12.dp),
                                        tint = textColor.copy(alpha = 0.6f)
                                    )
                                }
                                MessageStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "ส่งแล้ว",
                                        modifier = Modifier.size(13.dp),
                                        tint = textColor.copy(alpha = 0.7f)
                                    )
                                }
                                MessageStatus.DELIVERED -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "ส่งถึงแล้ว",
                                        modifier = Modifier.size(13.dp),
                                        tint = textColor.copy(alpha = 0.7f)
                                    )
                                }
                                MessageStatus.READ -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "อ่านแล้ว",
                                        modifier = Modifier.size(13.dp),
                                        tint = BangElectricBlue
                                    )
                                }
                                MessageStatus.FAILED -> {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "ส่งไม่สำเร็จ",
                                        modifier = Modifier.size(12.dp),
                                        tint = BangError
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Context Action Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("ตอบกลับ (Reply)") },
                    onClick = {
                        showMenu = false
                        onReply(message)
                    }
                )
                DropdownMenuItem(
                    text = { Text("ใส่ความรู้สึก (React)") },
                    onClick = {
                        showMenu = false
                        showReactionPicker = true
                    }
                )
                if (isMe && message.type == MessageType.TEXT) {
                    DropdownMenuItem(
                        text = { Text("แก้ไขข้อความ (Edit)") },
                        onClick = {
                            showMenu = false
                            onEdit(message)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("ลบข้อความ (Delete)", color = BangError) },
                    onClick = {
                        showMenu = false
                        onDelete(message)
                    }
                )
            }

            // Reaction Picker overlay
            if (showReactionPicker) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    modifier = Modifier.offset(y = (-40).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .combinedClickable(
                                        onClick = {
                                            onReact(message, emoji)
                                            showReactionPicker = false
                                        }
                                    )
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reaction chips display underneath bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val grouped = message.reactions.groupBy { it.reaction }
                grouped.forEach { (emoji, list) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 11.sp)
                            if (list.size > 1) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${list.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
