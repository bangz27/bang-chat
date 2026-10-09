package com.example.service

import com.example.model.Conversation
import com.example.model.ConversationMember
import com.example.model.ConversationType
import com.example.model.Message
import com.example.model.MessageAttachment
import com.example.model.MessageReaction
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class RealtimeEvent {
    data class NewMessage(val message: Message) : RealtimeEvent()
    data class MessageStatusChanged(val messageId: String, val status: MessageStatus) : RealtimeEvent()
    data class TypingChanged(val conversationId: String, val userId: String, val isTyping: Boolean) : RealtimeEvent()
    data class PresenceChanged(val userId: String, val isOnline: Boolean, val lastSeenAt: Long) : RealtimeEvent()
    data class ReactionAdded(val reaction: MessageReaction) : RealtimeEvent()
    data class ReactionRemoved(val messageId: String, val userId: String, val reaction: String) : RealtimeEvent()
    data class MessageDeleted(val conversationId: String, val messageId: String) : RealtimeEvent()
    data class MessageEdited(val conversationId: String, val messageId: String, val newContent: String) : RealtimeEvent()
}

/**
 * BangChatRepository implements the full Supabase-aligned data store.
 * Handles profiles, conversations, messages, read states, reactions, typing broadcasts,
 * and online presence with real-time reactive flows.
 */
class BangChatRepository(private val scope: CoroutineScope) {

    // Registered Profiles in Bang Chat database
    private val _profiles = MutableStateFlow<Map<String, Profile>>(emptyMap())
    val profiles: StateFlow<Map<String, Profile>> = _profiles.asStateFlow()

    // Currently authenticated user profile
    private val _currentProfile = MutableStateFlow<Profile?>(null)
    val currentProfile: StateFlow<Profile?> = _currentProfile.asStateFlow()

    // Conversations state
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Messages per conversation ID
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Typing state: conversationId -> Set of userIds currently typing
    private val _typingUsers = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val typingUsers: StateFlow<Map<String, Set<String>>> = _typingUsers.asStateFlow()

    // Realtime events channel
    private val _realtimeEvents = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    val realtimeEvents: SharedFlow<RealtimeEvent> = _realtimeEvents.asSharedFlow()

    // Blocked users set: set of blocked user IDs by current user
    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    init {
        seedInitialAccounts()
    }

    private fun seedInitialAccounts() {
        val somchai = Profile(
            id = "user_somchai_01",
            username = "somchai.bang",
            displayName = "Somchai Prasert (สมชาย)",
            bio = "Bang Chat Early Adopter 🚀 | Product Engineer",
            isOnline = true,
            lastSeenAt = System.currentTimeMillis()
        )
        val teamLead = Profile(
            id = "user_kanya_02",
            username = "kanya.abbtg",
            displayName = "Kanya (กัญญา)",
            bio = "Lead Developer @ ABBTG 💻",
            isOnline = true,
            lastSeenAt = System.currentTimeMillis() - 1000 * 60 * 3
        )
        val alex = Profile(
            id = "user_alex_03",
            username = "alex.bang",
            displayName = "Alex Wong",
            bio = "Coffee & Code ☕",
            isOnline = false,
            lastSeenAt = System.currentTimeMillis() - 1000 * 60 * 25
        )
        val me = Profile(
            id = "user_my_account_00",
            username = "bangz.daengnie",
            displayName = "Bangz Daengnie",
            bio = "Creator of Bang Chat ⚡ Realtime first",
            isOnline = true,
            lastSeenAt = System.currentTimeMillis()
        )

        _profiles.value = mapOf(
            somchai.id to somchai,
            teamLead.id to teamLead,
            alex.id to alex,
            me.id to me
        )

        // Set default authenticated profile
        _currentProfile.value = me

        // Seed initial conversations & messages
        val convSomchaiId = "conv_somchai_direct"
        val convGroupId = "conv_abbtg_group"
        val convAlexId = "conv_alex_direct"

        val somchaiConv = Conversation(
            id = convSomchaiId,
            type = ConversationType.DIRECT,
            title = somchai.displayName,
            createdBy = somchai.id,
            lastMessageContent = "สวัสดีครับ วันนี้มีอัปเดตระบบ Realtime ไหมครับ?",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 12,
            lastMessageSenderName = somchai.displayName,
            unreadCount = 1,
            otherMember = somchai,
            memberCount = 2,
            isPinned = true
        )

        val groupConv = Conversation(
            id = convGroupId,
            type = ConversationType.GROUP,
            title = "ทีม ABBTG-A 🚀",
            createdBy = teamLead.id,
            lastMessageContent = "เดี๋ยวส่งสรุปสเปกให้บ่ายสองครับ",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 45,
            lastMessageSenderName = "Kanya",
            unreadCount = 2,
            memberCount = 4,
            isPinned = false
        )

        val alexConv = Conversation(
            id = convAlexId,
            type = ConversationType.DIRECT,
            title = alex.displayName,
            createdBy = alex.id,
            lastMessageContent = "Let's connect tomorrow morning!",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 180,
            lastMessageSenderName = alex.displayName,
            unreadCount = 0,
            otherMember = alex,
            memberCount = 2,
            isPinned = false
        )

        _conversations.value = listOf(somchaiConv, groupConv, alexConv)

        // Seed messages
        val now = System.currentTimeMillis()
        val somchaiMessages = listOf(
            Message(
                id = "msg_s_1",
                conversationId = convSomchaiId,
                senderId = somchai.id,
                content = "ยินดีต้อนรับสู่ Bang Chat ครับ! 👋",
                status = MessageStatus.READ,
                createdAt = now - 1000 * 60 * 60
            ),
            Message(
                id = "msg_s_2",
                conversationId = convSomchaiId,
                senderId = me.id,
                content = "ขอบคุณครับ somchai! ระบบ RLS กับ Realtime Channel ทำงานพร้อมแล้ว",
                status = MessageStatus.READ,
                createdAt = now - 1000 * 60 * 50
            ),
            Message(
                id = "msg_s_3",
                conversationId = convSomchaiId,
                senderId = somchai.id,
                content = "สวัสดีครับ วันนี้มีอัปเดตระบบ Realtime ไหมครับ?",
                status = MessageStatus.DELIVERED,
                createdAt = now - 1000 * 60 * 12
            )
        )

        val groupMessages = listOf(
            Message(
                id = "msg_g_1",
                conversationId = convGroupId,
                senderId = teamLead.id,
                content = "ทุกคนครับ เตรียมพร้อมสำหรับการ Deploy ระบบข้อความของ Bang Chat นะครับ",
                status = MessageStatus.READ,
                createdAt = now - 1000 * 60 * 120
            ),
            Message(
                id = "msg_g_2",
                conversationId = convGroupId,
                senderId = somchai.id,
                content = "รับทราบครับ ทดสอบ RLS Policy ผ่านหมดแล้ว",
                status = MessageStatus.READ,
                createdAt = now - 1000 * 60 * 80
            ),
            Message(
                id = "msg_g_3",
                conversationId = convGroupId,
                senderId = teamLead.id,
                content = "เดี๋ยวส่งสรุปสเปกให้บ่ายสองครับ",
                status = MessageStatus.DELIVERED,
                createdAt = now - 1000 * 60 * 45
            )
        )

        val alexMessages = listOf(
            Message(
                id = "msg_a_1",
                conversationId = convAlexId,
                senderId = me.id,
                content = "Hey Alex, are we still meeting this week?",
                status = MessageStatus.READ,
                createdAt = now - 1000 * 60 * 240
            ),
            Message(
                id = "msg_a_2",
                conversationId = convAlexId,
                senderId = alex.id,
                content = "Let's connect tomorrow morning!",
                status = MessageStatus.READ,
                createdAt = now - 1000 * 60 * 180
            )
        )

        _messages.value = mapOf(
            convSomchaiId to somchaiMessages,
            convGroupId to groupMessages,
            convAlexId to alexMessages
        )
    }

    // --- Authentication & Profiles ---
    fun login(usernameOrEmail: String, displayName: String) {
        val existing = _profiles.value.values.find {
            it.username.equals(usernameOrEmail, ignoreCase = true)
        }
        val current = existing ?: Profile(
            id = "user_" + UUID.randomUUID().toString().take(8),
            username = usernameOrEmail.replace("@", "_").replace(".", "_").lowercase(),
            displayName = displayName.ifBlank { usernameOrEmail },
            bio = "Bang Chat Member ⚡",
            isOnline = true
        )
        _profiles.value = _profiles.value + (current.id to current)
        _currentProfile.value = current
    }

    fun logout() {
        val current = _currentProfile.value ?: return
        val updated = current.copy(isOnline = false, lastSeenAt = System.currentTimeMillis())
        _profiles.value = _profiles.value + (current.id to updated)
        _currentProfile.value = null
    }

    fun updateProfile(displayName: String, bio: String) {
        val current = _currentProfile.value ?: return
        val updated = current.copy(displayName = displayName, bio = bio, updatedAt = System.currentTimeMillis())
        _currentProfile.value = updated
        _profiles.value = _profiles.value + (updated.id to updated)
    }

    // --- Search Users ---
    fun searchUsers(query: String): List<Profile> {
        val currentId = _currentProfile.value?.id ?: ""
        if (query.isBlank()) {
            return _profiles.value.values.filter { it.id != currentId }
        }
        val q = query.trim().lowercase()
        return _profiles.value.values.filter {
            it.id != currentId && (it.username.lowercase().contains(q) || it.displayName.lowercase().contains(q))
        }
    }

    // --- Conversations ---
    fun startOrGetDirectConversation(targetUser: Profile): Conversation {
        val current = _currentProfile.value ?: throw IllegalStateException("Not logged in")
        val existing = _conversations.value.find {
            it.type == ConversationType.DIRECT && it.otherMember?.id == targetUser.id
        }
        if (existing != null) return existing

        val newConv = Conversation(
            id = "conv_" + UUID.randomUUID().toString(),
            type = ConversationType.DIRECT,
            title = targetUser.displayName,
            createdBy = current.id,
            lastMessageContent = null,
            lastMessageTime = System.currentTimeMillis(),
            otherMember = targetUser,
            memberCount = 2
        )
        _conversations.value = listOf(newConv) + _conversations.value
        return newConv
    }

    fun createGroupConversation(title: String, memberIds: List<String>): Conversation {
        val current = _currentProfile.value ?: throw IllegalStateException("Not logged in")
        val newConv = Conversation(
            id = "conv_group_" + UUID.randomUUID().toString(),
            type = ConversationType.GROUP,
            title = title,
            createdBy = current.id,
            lastMessageContent = "กลุ่มถูกสร้างขึ้นแล้ว",
            lastMessageTime = System.currentTimeMillis(),
            lastMessageSenderName = current.displayName,
            memberCount = memberIds.size + 1
        )
        _conversations.value = listOf(newConv) + _conversations.value

        val initMsg = Message(
            id = UUID.randomUUID().toString(),
            conversationId = newConv.id,
            senderId = current.id,
            type = MessageType.SYSTEM,
            content = "${current.displayName} สร้างกลุ่ม \"$title\"",
            status = MessageStatus.READ,
            createdAt = System.currentTimeMillis()
        )
        _messages.value = _messages.value + (newConv.id to listOf(initMsg))
        return newConv
    }

    // --- Messages Flow ---
    fun sendMessage(
        conversationId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        replyTo: Message? = null,
        attachment: MessageAttachment? = null
    ) {
        val current = _currentProfile.value ?: return
        val messageId = UUID.randomUUID().toString()

        val optimisticMessage = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = current.id,
            type = type,
            content = content,
            replyToMessageId = replyTo?.id,
            replyToContent = replyTo?.content,
            replyToSenderName = replyTo?.let { getSenderName(it.senderId) },
            status = MessageStatus.SENDING,
            createdAt = System.currentTimeMillis(),
            attachments = if (attachment != null) listOf(attachment) else emptyList()
        )

        // Optimistic UI insert
        val list = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (list + optimisticMessage))
        updateConversationLastMessage(conversationId, optimisticMessage, current.displayName)

        // Realtime simulate server ack & delivery flow
        scope.launch(Dispatchers.Default) {
            delay(350)
            // Change SENDING -> SENT (saved in PostgreSQL)
            updateMessageStatus(conversationId, messageId, MessageStatus.SENT)

            delay(400)
            // Recipient delivered (Supabase Realtime push delivered)
            updateMessageStatus(conversationId, messageId, MessageStatus.DELIVERED)

            // If it's a direct conversation with someone online (like Somchai), simulate realistic reply or read
            val conv = _conversations.value.find { it.id == conversationId }
            if (conv?.type == ConversationType.DIRECT && conv.otherMember?.id == "user_somchai_01") {
                delay(800)
                updateMessageStatus(conversationId, messageId, MessageStatus.READ)
                simulateSomchaiReply(conversationId, content)
            }
        }
    }

    private fun simulateSomchaiReply(conversationId: String, userText: String) {
        scope.launch(Dispatchers.Default) {
            delay(1000)
            // Broadcast typing indicator
            setTyping(conversationId, "user_somchai_01", true)
            delay(1800)
            setTyping(conversationId, "user_somchai_01", false)

            val replyContent = when {
                userText.contains("สวัสดี", ignoreCase = true) || userText.contains("hello", ignoreCase = true) ->
                    "สวัสดีครับ! ดีใจที่ได้คุยกันใน Bang Chat ระบบเรียลไทม์เร็วและปลอดภัยมากครับ 👍"
                userText.contains("ภาพ", ignoreCase = true) || userText.contains("รูป", ignoreCase = true) ->
                    "ส่งรูปมาได้เลยครับ Supabase Storage จัดเก็บรูปภาพให้อย่างปลอดภัยครับ 🖼️"
                userText.contains("อัปเดต", ignoreCase = true) || userText.contains("test", ignoreCase = true) ->
                    "ทดสอบสถานะ Sent, Delivered และ Read Receipts ครบถ้วนครับผม!"
                else -> "รับทราบข้อความ \"$userText\" เรียบร้อยครับ ตอบกลับผ่าน Realtime Channel ทันที ⚡"
            }

            val replyMsg = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId = "user_somchai_01",
                content = replyContent,
                status = MessageStatus.DELIVERED,
                createdAt = System.currentTimeMillis()
            )

            val list = _messages.value[conversationId] ?: emptyList()
            _messages.value = _messages.value + (conversationId to (list + replyMsg))
            updateConversationLastMessage(conversationId, replyMsg, "Somchai Prasert")
            _realtimeEvents.emit(RealtimeEvent.NewMessage(replyMsg))
        }
    }

    fun markConversationAsRead(conversationId: String) {
        val current = _currentProfile.value ?: return
        // Mark all messages from other senders as READ
        val list = _messages.value[conversationId] ?: return
        val updatedList = list.map { msg ->
            if (msg.senderId != current.id && msg.status != MessageStatus.READ) {
                msg.copy(status = MessageStatus.READ)
            } else msg
        }
        _messages.value = _messages.value + (conversationId to updatedList)

        // Clear unread count on conversation
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) conv.copy(unreadCount = 0) else conv
        }
    }

    fun addReaction(conversationId: String, messageId: String, reaction: String) {
        val current = _currentProfile.value ?: return
        val list = _messages.value[conversationId] ?: return
        val updatedList = list.map { msg ->
            if (msg.id == messageId) {
                val existing = msg.reactions.find { it.userId == current.id && it.reaction == reaction }
                val newReactions = if (existing != null) {
                    msg.reactions.filterNot { it.userId == current.id && it.reaction == reaction }
                } else {
                    msg.reactions + MessageReaction(messageId = messageId, userId = current.id, reaction = reaction)
                }
                msg.copy(reactions = newReactions)
            } else msg
        }
        _messages.value = _messages.value + (conversationId to updatedList)
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        val list = _messages.value[conversationId] ?: return
        val updatedList = list.map { msg ->
            if (msg.id == messageId) {
                msg.copy(deletedAt = System.currentTimeMillis(), content = "ข้อความนี้ถูกลบแล้ว")
            } else msg
        }
        _messages.value = _messages.value + (conversationId to updatedList)
    }

    fun editMessage(conversationId: String, messageId: String, newContent: String) {
        val list = _messages.value[conversationId] ?: return
        val updatedList = list.map { msg ->
            if (msg.id == messageId) {
                msg.copy(content = newContent, editedAt = System.currentTimeMillis())
            } else msg
        }
        _messages.value = _messages.value + (conversationId to updatedList)
    }

    fun setTyping(conversationId: String, userId: String, isTyping: Boolean) {
        val currentMap = _typingUsers.value
        val currentSet = currentMap[conversationId] ?: emptySet()
        val newSet = if (isTyping) currentSet + userId else currentSet - userId
        _typingUsers.value = currentMap + (conversationId to newSet)
    }

    fun togglePinConversation(conversationId: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) conv.copy(isPinned = !conv.isPinned) else conv
        }.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.lastMessageTime })
    }

    fun toggleMuteConversation(conversationId: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) conv.copy(isMuted = !conv.isMuted) else conv
        }
    }

    fun blockUser(userId: String) {
        _blockedUserIds.value = _blockedUserIds.value + userId
    }

    fun unblockUser(userId: String) {
        _blockedUserIds.value = _blockedUserIds.value - userId
    }

    fun getSenderName(senderId: String): String {
        return _profiles.value[senderId]?.displayName ?: "User"
    }

    private fun updateMessageStatus(conversationId: String, messageId: String, status: MessageStatus) {
        val list = _messages.value[conversationId] ?: return
        val updatedList = list.map { msg ->
            if (msg.id == messageId) msg.copy(status = status) else msg
        }
        _messages.value = _messages.value + (conversationId to updatedList)
    }

    private fun updateConversationLastMessage(
        conversationId: String,
        message: Message,
        senderName: String
    ) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val preview = when (message.type) {
                    MessageType.IMAGE -> "📷 รูปภาพ"
                    MessageType.FILE -> "📎 ไฟล์แนบ"
                    MessageType.AUDIO -> "🎤 ข้อความเสียง"
                    else -> message.content
                }
                conv.copy(
                    lastMessageContent = preview,
                    lastMessageTime = message.createdAt,
                    lastMessageSenderName = senderName
                )
            } else conv
        }.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.lastMessageTime })
    }
}
