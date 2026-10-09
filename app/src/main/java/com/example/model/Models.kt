package com.example.model

import java.util.UUID

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    AUDIO,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class ConversationType {
    DIRECT,
    GROUP
}

data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
)

data class MessageAttachment(
    val id: String = UUID.randomUUID().toString(),
    val messageId: String,
    val storagePath: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long = 0L,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class MessageReaction(
    val messageId: String,
    val userId: String,
    val reaction: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val type: MessageType = MessageType.TEXT,
    val content: String,
    val replyToMessageId: String? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val deletedAt: Long? = null,
    val attachments: List<MessageAttachment> = emptyList(),
    val reactions: List<MessageReaction> = emptyList()
)

data class ConversationMember(
    val conversationId: String,
    val userId: String,
    val role: String = "member", // member, admin, owner
    val joinedAt: Long = System.currentTimeMillis(),
    val lastReadMessageId: String? = null,
    val muted: Boolean = false,
    val pinned: Boolean = false
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val type: ConversationType = ConversationType.DIRECT,
    val title: String? = null,
    val avatarUrl: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessageId: String? = null,
    val lastMessageContent: String? = null,
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageSenderName: String? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val otherMember: Profile? = null,
    val memberCount: Int = 2
)
