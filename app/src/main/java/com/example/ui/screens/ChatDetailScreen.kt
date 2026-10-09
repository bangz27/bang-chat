package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation
import com.example.model.ConversationType
import com.example.model.Message
import com.example.model.MessageAttachment
import com.example.model.Profile
import com.example.ui.components.BangAvatar
import com.example.ui.components.MessageBubble
import com.example.ui.components.MessageComposer
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangEmeraldDark
import com.example.ui.theme.BangError
import com.example.ui.theme.StatusOnline

@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    currentProfile: Profile?,
    messages: List<Message>,
    isOtherTyping: Boolean,
    replyingTo: Message?,
    editingMessage: Message?,
    onBack: () -> Unit,
    onSendMessage: (String, MessageAttachment?) -> Unit,
    onTyping: () -> Unit,
    onReply: (Message) -> Unit,
    onEdit: (Message) -> Unit,
    onDelete: (Message) -> Unit,
    onReact: (Message, String) -> Unit,
    onCancelReplyOrEdit: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    getSenderName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware or gesture back navigation
    BackHandler {
        onBack()
    }

    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    val isGroup = conversation.type == ConversationType.GROUP
    val title = conversation.title ?: conversation.otherMember?.displayName ?: "Chat"
    val otherProfile = conversation.otherMember

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "กลับ",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    BangAvatar(
                        name = title,
                        size = 40.dp,
                        isOnline = otherProfile?.isOnline,
                        isGroup = isGroup
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* Could show profile/group info */ }
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isOtherTyping) {
                            Text(
                                text = "กำลังพิมพ์... ⚡",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BangEmeraldDark
                            )
                        } else if (isGroup) {
                            Text(
                                text = "สมาชิก ${conversation.memberCount} คน",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (otherProfile != null) {
                            Text(
                                text = if (otherProfile.isOnline) "🟢 ออนไลน์" else "ออฟไลน์",
                                fontSize = 11.sp,
                                color = if (otherProfile.isOnline) StatusOnline else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "ตัวเลือก",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (conversation.isPinned) "ยกเลิกปักหมุด" else "ปักหมุดแชท") },
                                onClick = {
                                    showMenu = false
                                    onTogglePin(conversation.id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (conversation.isMuted) "เปิดการแจ้งเตือน" else "ปิดการแจ้งเตือน") },
                                onClick = {
                                    showMenu = false
                                    onToggleMute(conversation.id)
                                }
                            )
                            if (!isGroup && otherProfile != null) {
                                DropdownMenuItem(
                                    text = { Text("บล็อกผู้ใช้นี้", color = BangError) },
                                    onClick = {
                                        showMenu = false
                                        onBlockUser(otherProfile.id)
                                        onBack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            MessageComposer(
                replyingTo = replyingTo,
                editingMessage = editingMessage,
                onCancelReplyOrEdit = onCancelReplyOrEdit,
                onSendMessage = onSendMessage,
                onTyping = onTyping
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                // Encryption & RLS banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "🔒 ข้อความใน Bang Chat ได้รับการคุ้มครองด้วย Supabase RLS และ Private Channel",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == currentProfile?.id
                    val senderName = if (isGroup && !isMe) {
                        if (msg.senderId == "user_somchai_01") "Somchai"
                        else if (msg.senderId == "user_kanya_02") "Kanya"
                        else "User"
                    } else ""

                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        senderName = senderName,
                        onReply = onReply,
                        onReact = onReact,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}
