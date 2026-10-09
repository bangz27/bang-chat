package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BangElectricBlue
import com.example.ui.theme.BangEmerald
import com.example.ui.theme.BangEmeraldDark

@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("bangz.daengnie") }
    var displayName by remember { mutableStateOf("Bangz Daengnie") }
    var password by remember { mutableStateOf("secret123") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bang Chat Logo Graphic
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BangEmeraldDark, BangElectricBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BANG CHAT",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = BangEmeraldDark,
                letterSpacing = 1.sp
            )

            Text(
                text = "แอปแชทเรียลไทม์ที่เร็วและปลอดภัยที่สุด",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isSignUp) "สร้างบัญชีใหม่ (Sign Up)" else "เข้าสู่ระบบ (Sign In)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSignUp) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("ชื่อที่แสดง (Display Name)") },
                            placeholder = { Text("เช่น สมชาย หรือ Alex") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BangEmeraldDark)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_display_name_field")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("ชื่อผู้ใช้ / อีเมล (Username / Email)") },
                        placeholder = { Text("username หรือ user@email.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BangEmeraldDark)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_username_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("รหัสผ่าน (Password)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BangEmeraldDark)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (username.isNotBlank()) {
                                onLogin(username.trim(), displayName.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BangEmeraldDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isSignUp) "เริ่มต้นใช้งานทันที" else "เข้าสู่ระบบ Bang Chat",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { isSignUp = !isSignUp },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isSignUp) "มีบัญชีอยู่แล้ว? เข้าสู่ระบบ" else "ยังไม่มีบัญชี? สมัครสมาชิกใหม่",
                            color = BangEmeraldDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
