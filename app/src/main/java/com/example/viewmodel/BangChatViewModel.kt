package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Conversation
import com.example.model.ConversationType
import com.example.model.Message
import com.example.model.MessageAttachment
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.Profile
import com.example.service.BangChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    CHATS,
    CONTACTS,
    SETTINGS
}

data class AuthUiState(
    val isAuthenticated: Boolean = true,
    val currentProfile: Profile? = null,
    val error: String? = null
)

class BangChatViewModel : ViewModel() {

    private val repository = BangChatRepository(viewModelScope)

    // Current Navigation State
    private val _currentTab = MutableStateFlow(AppTab.CHATS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    // Search query in Home & Contacts
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Replying message state
    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    // Editing message state
    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    // Typing debounce job for current user
    private var typingJob: Job? = null

    // Create group dialog state
    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    // Profiles & Auth
    val currentProfile = repository.currentProfile
    val profiles = repository.profiles
    val blockedUserIds = repository.blockedUserIds

    // Conversations filtered by search query
    val conversations: StateFlow<List<Conversation>> = combine(
        repository.conversations,
        _searchQuery
    ) { convList, query ->
        if (query.isBlank()) {
            convList
        } else {
            val q = query.trim().lowercase()
            convList.filter {
                (it.title?.lowercase()?.contains(q) == true) ||
                (it.lastMessageContent?.lowercase()?.contains(q) == true) ||
                (it.otherMember?.displayName?.lowercase()?.contains(q) == true) ||
                (it.otherMember?.username?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation object
    val activeConversation: StateFlow<Conversation?> = combine(
        repository.conversations,
        _activeConversationId
    ) { convList, activeId ->
        convList.find { it.id == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Messages for active conversation
    val activeMessages: StateFlow<List<Message>> = combine(
        repository.messages,
        _activeConversationId
    ) { messagesMap, activeId ->
        if (activeId == null) emptyList()
        else messagesMap[activeId] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Typing indicator for active conversation
    val isOtherUserTyping: StateFlow<Boolean> = combine(
        repository.typingUsers,
        _activeConversationId,
        currentProfile
    ) { typingMap, activeId, me ->
        if (activeId == null || me == null) false
        else {
            val typingSet = typingMap[activeId] ?: emptySet()
            typingSet.any { it != me.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        _replyingToMessage.value = null
        _editingMessage.value = null
        repository.markConversationAsRead(conversationId)
    }

    fun closeConversation() {
        val currentId = _activeConversationId.value
        val me = currentProfile.value
        if (currentId != null && me != null) {
            repository.setTyping(currentId, me.id, false)
        }
        _activeConversationId.value = null
        _replyingToMessage.value = null
        _editingMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startChatWithUser(user: Profile) {
        val conv = repository.startOrGetDirectConversation(user)
        openConversation(conv.id)
    }

    fun sendMessage(text: String, attachment: MessageAttachment? = null) {
        val convId = _activeConversationId.value ?: return
        if (text.isBlank() && attachment == null) return

        val editing = _editingMessage.value
        if (editing != null) {
            repository.editMessage(convId, editing.id, text.trim())
            _editingMessage.value = null
            return
        }

        val type = if (attachment != null) {
            when {
                attachment.mimeType.startsWith("image/") -> MessageType.IMAGE
                attachment.mimeType.startsWith("audio/") -> MessageType.AUDIO
                else -> MessageType.FILE
            }
        } else MessageType.TEXT

        repository.sendMessage(
            conversationId = convId,
            content = text.trim(),
            type = type,
            replyTo = _replyingToMessage.value,
            attachment = attachment
        )
        _replyingToMessage.value = null

        // Stop typing
        val me = currentProfile.value
        if (me != null) {
            repository.setTyping(convId, me.id, false)
        }
    }

    fun onUserTyping() {
        val convId = _activeConversationId.value ?: return
        val me = currentProfile.value ?: return

        repository.setTyping(convId, me.id, true)
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2500)
            repository.setTyping(convId, me.id, false)
        }
    }

    fun setReplyTo(message: Message?) {
        _replyingToMessage.value = message
        _editingMessage.value = null
    }

    fun setEditing(message: Message?) {
        _editingMessage.value = message
        _replyingToMessage.value = null
    }

    fun addReaction(message: Message, reaction: String) {
        val convId = _activeConversationId.value ?: return
        repository.addReaction(convId, message.id, reaction)
    }

    fun deleteMessage(message: Message) {
        val convId = _activeConversationId.value ?: return
        repository.deleteMessage(convId, message.id)
    }

    fun togglePin(conversationId: String) {
        repository.togglePinConversation(conversationId)
    }

    fun toggleMute(conversationId: String) {
        repository.toggleMuteConversation(conversationId)
    }

    fun setShowCreateGroupDialog(show: Boolean) {
        _showCreateGroupDialog.value = show
    }

    fun createGroup(title: String, memberIds: List<String>) {
        if (title.isBlank()) return
        val newConv = repository.createGroupConversation(title.trim(), memberIds)
        _showCreateGroupDialog.value = false
        openConversation(newConv.id)
    }

    fun login(username: String, displayName: String) {
        repository.login(username, displayName)
    }

    fun logout() {
        closeConversation()
        repository.logout()
    }

    fun updateProfile(displayName: String, bio: String) {
        repository.updateProfile(displayName, bio)
    }

    fun blockUser(userId: String) {
        repository.blockUser(userId)
    }

    fun unblockUser(userId: String) {
        repository.unblockUser(userId)
    }

    fun searchContacts(query: String): List<Profile> {
        return repository.searchUsers(query)
    }

    fun getSenderName(senderId: String): String {
        return repository.getSenderName(senderId)
    }
}
