package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BangChatTheme
import com.example.ui.theme.BangEmeraldDark
import com.example.viewmodel.AppTab
import com.example.viewmodel.BangChatViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BangChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BangChatTheme {
                BangChatApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BangChatApp(viewModel: BangChatViewModel) {
    val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeConversation by viewModel.activeConversation.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val isOtherTyping by viewModel.isOtherUserTyping.collectAsStateWithLifecycle()
    val replyingTo by viewModel.replyingToMessage.collectAsStateWithLifecycle()
    val editingMessage by viewModel.editingMessage.collectAsStateWithLifecycle()

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showCreateGroupDialog by viewModel.showCreateGroupDialog.collectAsStateWithLifecycle()
    val allProfilesMap by viewModel.profiles.collectAsStateWithLifecycle()
    val blockedUserIds by viewModel.blockedUserIds.collectAsStateWithLifecycle()

    val contactsList = allProfilesMap.values.filter { it.id != (currentProfile?.id ?: "") }
    val totalUnreadCount = conversations.sumOf { it.unreadCount }

    // If not authenticated, display login/sign-up screen
    if (currentProfile == null) {
        AuthScreen(
            onLogin = { username, displayName ->
                viewModel.login(username, displayName)
            }
        )
        return
    }

    // If inside an active conversation, show direct chat detail screen
    if (activeConversationId != null && activeConversation != null) {
        ChatDetailScreen(
            conversation = activeConversation!!,
            currentProfile = currentProfile,
            messages = activeMessages,
            isOtherTyping = isOtherTyping,
            replyingTo = replyingTo,
            editingMessage = editingMessage,
            onBack = { viewModel.closeConversation() },
            onSendMessage = { text, attachment -> viewModel.sendMessage(text, attachment) },
            onTyping = { viewModel.onUserTyping() },
            onReply = { viewModel.setReplyTo(it) },
            onEdit = { viewModel.setEditing(it) },
            onDelete = { viewModel.deleteMessage(it) },
            onReact = { msg, emoji -> viewModel.addReaction(msg, emoji) },
            onCancelReplyOrEdit = {
                viewModel.setReplyTo(null)
                viewModel.setEditing(null)
            },
            onTogglePin = { viewModel.togglePin(it) },
            onToggleMute = { viewModel.toggleMute(it) },
            onBlockUser = { viewModel.blockUser(it) },
            getSenderName = { viewModel.getSenderName(it) }
        )
        return
    }

    // Main App with Bottom Navigation
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bang_bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Chats Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CHATS,
                    onClick = { viewModel.selectTab(AppTab.CHATS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (totalUnreadCount > 0) {
                                    Badge(containerColor = BangEmeraldDark) {
                                        Text("$totalUnreadCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.CHATS) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = "แชท"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "แชท (Chats)",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.CHATS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BangEmeraldDark,
                        selectedTextColor = BangEmeraldDark,
                        indicatorColor = BangEmeraldDark.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_chats")
                )

                // Contacts Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CONTACTS,
                    onClick = { viewModel.selectTab(AppTab.CONTACTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CONTACTS) Icons.Default.People else Icons.Outlined.People,
                            contentDescription = "รายชื่อ"
                        )
                    },
                    label = {
                        Text(
                            text = "รายชื่อ (Contacts)",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.CONTACTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BangEmeraldDark,
                        selectedTextColor = BangEmeraldDark,
                        indicatorColor = BangEmeraldDark.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_contacts")
                )

                // Settings Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "การตั้งค่า"
                        )
                    },
                    label = {
                        Text(
                            text = "ตั้งค่า (Settings)",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BangEmeraldDark,
                        selectedTextColor = BangEmeraldDark,
                        indicatorColor = BangEmeraldDark.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    AppTab.CHATS -> {
                        ChatListScreen(
                            conversations = conversations,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onConversationClick = { viewModel.openConversation(it) },
                            onPinToggle = { viewModel.togglePin(it) },
                            onMuteToggle = { viewModel.toggleMute(it) },
                            showCreateGroupDialog = showCreateGroupDialog,
                            onSetShowCreateGroupDialog = { viewModel.setShowCreateGroupDialog(it) },
                            onCreateGroup = { title, members -> viewModel.createGroup(title, members) },
                            availableContacts = contactsList
                        )
                    }
                    AppTab.CONTACTS -> {
                        ContactsScreen(
                            contacts = contactsList,
                            blockedUserIds = blockedUserIds,
                            onStartChat = { contact -> viewModel.startChatWithUser(contact) }
                        )
                    }
                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            currentProfile = currentProfile,
                            blockedCount = blockedUserIds.size,
                            onUpdateProfile = { name, bio -> viewModel.updateProfile(name, bio) },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}
