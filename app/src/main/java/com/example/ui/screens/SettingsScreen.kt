package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Profile
import com.example.ui.components.BangAvatar
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangEmeraldDark
import com.example.ui.theme.BangError

@Composable
fun SettingsScreen(
    currentProfile: Profile?,
    blockedCount: Int,
    onUpdateProfile: (String, String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var readReceiptsEnabled by remember { mutableStateOf(true) }
    var onlineStatusEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "การตั้งค่า (Settings)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Profile Card
        if (currentProfile != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showEditProfileDialog = true }
                    .testTag("settings_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BangAvatar(
                        name = currentProfile.displayName,
                        size = 56.dp,
                        isOnline = currentProfile.isOnline
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentProfile.displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@${currentProfile.username}",
                            fontSize = 13.sp,
                            color = BangEmeraldDark,
                            fontWeight = FontWeight.Medium
                        )
                        if (!currentProfile.bio.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentProfile.bio,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "แก้ไข",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Backend & Infrastructure badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BangEmerald.copy(alpha = 0.12f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Backend",
                    tint = BangEmeraldDark,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Supabase Production Ready",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BangEmeraldDark
                    )
                    Text(
                        text = "PostgreSQL with Row Level Security, Realtime Channels & Storage",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Section
        SettingsSectionHeader(title = "ความเป็นส่วนตัวและความปลอดภัย (Privacy & Security)")

        SettingsSwitchRow(
            icon = Icons.Default.Security,
            title = "ใบตอบรับการอ่าน (Read Receipts)",
            subtitle = "แสดงสถานะเครื่องหมายถูกสีฟ้าเมื่ออ่านข้อความแล้ว",
            checked = readReceiptsEnabled,
            onCheckedChange = { readReceiptsEnabled = it }
        )

        SettingsSwitchRow(
            icon = Icons.Default.Security,
            title = "สถานะออนไลน์ (Online Presence)",
            subtitle = "แสดงจุดสีเขียวเมื่อกำลังใช้งานแอป",
            checked = onlineStatusEnabled,
            onCheckedChange = { onlineStatusEnabled = it }
        )

        SettingsItemRow(
            icon = Icons.Default.Block,
            title = "ผู้ใช้ที่ถูกบล็อก (Blocked Users)",
            subtitle = "$blockedCount บัญชี",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications & Appearance
        SettingsSectionHeader(title = "การแจ้งเตือนและการแสดงผล (Notifications & App)")

        SettingsSwitchRow(
            icon = Icons.Default.Notifications,
            title = "การแจ้งเตือนข้อความ (Push Notifications)",
            subtitle = "รับการแจ้งเตือนเมื่อมีข้อความใหม่เข้ามา",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it }
        )

        SettingsItemRow(
            icon = Icons.Default.Palette,
            title = "ธีมของแอปพลิเคชัน (Theme & Colors)",
            subtitle = "Bang Chat Electric Mint & Navy Identity",
            onClick = {}
        )

        SettingsItemRow(
            icon = Icons.Default.Info,
            title = "เกี่ยวกับ Bang Chat (About)",
            subtitle = "เวอร์ชัน 1.0.0 (Realtime Messaging Architecture)",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BangError.copy(alpha = 0.1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onLogout() }
                .testTag("settings_logout_button")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "ออกจากระบบ",
                    tint = BangError,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ออกจากระบบ (Logout)",
                    color = BangError,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog && currentProfile != null) {
        var editName by remember { mutableStateOf(currentProfile.displayName) }
        var editBio by remember { mutableStateOf(currentProfile.bio ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("แก้ไขโปรไฟล์ (Edit Profile)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("ชื่อที่แสดง (Display Name)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("สถานะ / Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName.trim(), editBio.trim())
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangEmeraldDark)
                ) {
                    Text("บันทึก", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = BangEmeraldDark,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = BangEmeraldDark,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = BangEmeraldDark,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BangEmeraldDark
            )
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}
