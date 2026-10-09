package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation
import com.example.model.ConversationType
import com.example.model.Profile
import com.example.ui.components.BangAvatar
import com.example.ui.theme.BangBadge
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangEmeraldDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    conversations: List<Conversation>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onConversationClick: (String) -> Unit,
    onPinToggle: (String) -> Unit,
    onMuteToggle: (String) -> Unit,
    showCreateGroupDialog: Boolean,
    onSetShowCreateGroupDialog: (Boolean) -> Unit,
    onCreateGroup: (String, List<String>) -> Unit,
    availableContacts: List<Profile>,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: "Bang Chat" + Search + Menu
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bang Chat",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BangEmeraldDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BangEmerald.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "⚡ Realtime",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BangEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSetShowCreateGroupDialog(true) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupAdd,
                                    contentDescription = "สร้างกลุ่ม",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "เมนู",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("สร้างกลุ่มใหม่") },
                                        onClick = {
                                            showMenu = false
                                            onSetShowCreateGroupDialog(true)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("ทำเครื่องหมายว่าอ่านทั้งหมด") },
                                        onClick = { showMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("ค้นหาแชทและข้อความ...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "ค้นหา",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("chat_search_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                }
            }

            // Conversation List
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "💬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "ไม่พบข้อความที่ค้นหา" else "ยังไม่มีบทสนทนา",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "แตะแท็บ 'รายชื่อ' เพื่อเริ่มคุยกับเพื่อนใน Bang Chat",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            onClick = { onConversationClick(conv.id) },
                            onPinToggle = { onPinToggle(conv.id) },
                            onMuteToggle = { onMuteToggle(conv.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 74.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // FAB to start new chat
        FloatingActionButton(
            onClick = { onSetShowCreateGroupDialog(true) },
            containerColor = BangEmeraldDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_new_chat")
        ) {
            Icon(Icons.Default.Add, contentDescription = "สร้างการสนทนาใหม่")
        }

        // Create Group Dialog
        if (showCreateGroupDialog) {
            CreateGroupDialog(
                availableContacts = availableContacts,
                onDismiss = { onSetShowCreateGroupDialog(false) },
                onConfirm = onCreateGroup
            )
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGroup = conversation.type == ConversationType.GROUP
    val title = conversation.title ?: conversation.otherMember?.displayName ?: "Chat"
    val isOnline = conversation.otherMember?.isOnline

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = timeFormat.format(Date(conversation.lastMessageTime))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_item_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BangAvatar(
            name = title,
            size = 52.dp,
            isOnline = isOnline,
            isGroup = isGroup
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = timeText,
                    fontSize = 11.sp,
                    color = if (conversation.unreadCount > 0) BangEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessageContent ?: "เริ่มการสนทนา...",
                    fontSize = 13.sp,
                    color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "ปักหมุด",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (conversation.isMuted) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "ปิดการแจ้งเตือน",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BangBadge),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
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
}

@Composable
fun CreateGroupDialog(
    availableContacts: List<Profile>,
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("สร้างกลุ่มแชทใหม่ (New Group)", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("ชื่อกลุ่ม") },
                    placeholder = { Text("เช่น ทีม ABBTG-A, ครอบครัว") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "เลือกสมาชิก (${selectedIds.size} คน)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.height(180.dp)) {
                    items(availableContacts) { contact ->
                        val isSelected = selectedIds.contains(contact.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (isSelected) {
                                        selectedIds - contact.id
                                    } else {
                                        selectedIds + contact.id
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BangAvatar(name = contact.displayName, size = 36.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = contact.displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = "@${contact.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = if (isSelected) "✓ เลือกแล้ว" else "+ เลือก",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BangEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onConfirm(groupName, selectedIds.toList())
                    }
                },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BangEmeraldDark)
            ) {
                Text("สร้างกลุ่ม", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}
