package com.quickchat.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyCard
import com.quickchat.core.model.theme.SketchyDoodle
import com.quickchat.core.model.theme.sketchyBorder

@Composable
fun ProfileSetupScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit
) {
    val displayName by viewModel.displayName.collectAsState()
    val about by viewModel.about.collectAsState()
    val username by viewModel.username.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val colors = LocalSketchyColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        SketchyCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SketchyDoodle(name = "profile", color = colors.accent, modifier = Modifier.size(90.dp))

                Text(
                    text = "Profile Info",
                    fontSize = 28.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please provide your name, bio, and a unique search username.",
                    fontSize = 14.sp,
                    color = colors.text.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { viewModel.updateDisplayName(it) },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.text.copy(alpha = 0.4f),
                        cursorColor = colors.accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text("Unique Username (Optional)") },
                    singleLine = true,
                    supportingText = {
                        if (usernameError != null) {
                            Text(usernameError!!, color = Color(0xFFD32F2F))
                        } else {
                            Text("e.g. alice_smith123 (3-20 lowercase alphanumeric characters)")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = if (usernameError != null) Color(0xFFD32F2F) else colors.accent,
                        unfocusedBorderColor = colors.text.copy(alpha = 0.4f),
                        cursorColor = colors.accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = about,
                    onValueChange = { viewModel.updateAbout(it) },
                    label = { Text("About Status") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.text.copy(alpha = 0.4f),
                        cursorColor = colors.accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.completeProfile {
                            onNavigateToHome()
                        }
                    },
                    enabled = !loading && usernameError == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .sketchyBorder(1.dp, colors.text, 25.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
