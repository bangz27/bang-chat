package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Message
import com.example.model.MessageAttachment
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangEmeraldDark

@Composable
fun MessageComposer(
    replyingTo: Message?,
    editingMessage: Message?,
    onCancelReplyOrEdit: () -> Unit,
    onSendMessage: (String, MessageAttachment?) -> Unit,
    onTyping: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(editingMessage) { mutableStateOf(editingMessage?.content ?: "") }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            // Replying or Editing Banner
            AnimatedVisibility(
                visible = replyingTo != null || editingMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(26.dp)
                                .background(BangEmerald, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingMessage != null) "แก้ไขข้อความ" else "ตอบกลับ ${replyingTo?.replyToSenderName ?: "ข้อความ"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BangEmerald
                            )
                            Text(
                                text = editingMessage?.content ?: replyingTo?.content ?: "",
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                onCancelReplyOrEdit()
                                if (editingMessage != null) text = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "ยกเลิก",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick attachment options bar if menu opened
            AnimatedVisibility(visible = showAttachmentMenu) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                showAttachmentMenu = false
                                // Send photo attachment
                                onSendMessage(
                                    "ส่งรูปภาพแนบ 📷",
                                    MessageAttachment(
                                        messageId = "",
                                        storagePath = "chat-media/img_photo.jpg",
                                        fileName = "chat_photo.jpg",
                                        mimeType = "image/jpeg"
                                    )
                                )
                            }
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "รูปภาพ",
                                tint = BangEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ส่งรูปภาพ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            showAttachmentMenu = false
                            onSendMessage(
                                "บันทึกเสียงเรียบร้อย 🎤",
                                MessageAttachment(
                                    messageId = "",
                                    storagePath = "voice/audio_rec.m4a",
                                    fileName = "voice_message.m4a",
                                    mimeType = "audio/mp4",
                                    duration = 14
                                )
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "เสียง",
                                tint = BangEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ข้อความเสียง", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach button (+)
                IconButton(
                    onClick = { showAttachmentMenu = !showAttachmentMenu },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "แนบไฟล์",
                        tint = if (showAttachmentMenu) BangEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text Field
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (it.isNotEmpty()) onTyping()
                    },
                    placeholder = {
                        Text(
                            text = "พิมพ์ข้อความ...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BangEmerald,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send or Voice Button
                if (text.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BangEmeraldDark)
                            .clickable {
                                val msgText = text
                                text = ""
                                onSendMessage(msgText, null)
                            }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ส่งข้อความ",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            onSendMessage(
                                "ส่งข้อความเสียงสั้น 🎤",
                                MessageAttachment(
                                    messageId = "",
                                    storagePath = "voice/quick_audio.m4a",
                                    fileName = "quick_voice.m4a",
                                    mimeType = "audio/mp4",
                                    duration = 8
                                )
                            )
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "ส่งเสียง",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
