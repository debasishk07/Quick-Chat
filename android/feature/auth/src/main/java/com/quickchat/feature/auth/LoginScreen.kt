package com.quickchat.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyCard
import com.quickchat.core.model.theme.SketchyDoodle
import com.quickchat.core.model.theme.sketchyBorder

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToProfileSetup: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val flowState by viewModel.loginFlowState.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val linkPhone by viewModel.linkPhone.collectAsState()
    val linkPhoneCode by viewModel.linkPhoneCode.collectAsState()
    val linkPhoneCodeSent by viewModel.linkPhoneCodeSent.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showMockChooser by viewModel.showMockGoogleChooser.collectAsState()
    
    val colors = LocalSketchyColors.current

    // Handles navigation outcome
    val handleNavigationOutcome: (Boolean) -> Unit = { needsUsernameSetup ->
        if (needsUsernameSetup) onNavigateToProfileSetup() else onNavigateToHome()
    }

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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Doodle / Header
                SketchyDoodle(name = "security", color = colors.accent, modifier = Modifier.size(90.dp))

                Text(
                    text = "Quick Chat",
                    fontSize = 32.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                when (flowState) {
                    LoginFlowState.CHOICE -> {
                        Text(
                            text = "Connect with friends securely and instantly.",
                            fontSize = 14.sp,
                            color = colors.text.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Google Login Button
                        Button(
                            onClick = {
                                viewModel.signInWithGoogle(context, handleNavigationOutcome)
                            },
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.background,
                                contentColor = colors.text
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .sketchyBorder(2.dp, colors.text, 25.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SketchyDoodle(
                                    name = "google",
                                    color = colors.accent,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 8.dp)
                                )
                                Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        // Phone Login Button
                        Button(
                            onClick = {
                                viewModel.setLoginFlowState(LoginFlowState.PHONE_OTP)
                            },
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .sketchyBorder(1.dp, colors.text, 25.dp)
                        ) {
                            Text("Continue with Phone", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    LoginFlowState.PHONE_OTP -> {
                        Text(
                            text = if (otpSent) "Enter the 6-digit OTP sent to your phone" else "Verify your phone number to get started",
                            fontSize = 14.sp,
                            color = colors.text.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        if (!otpSent) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { viewModel.updatePhone(it) },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                                onClick = { viewModel.sendOtp() },
                                enabled = !loading,
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
                                    Text("Send Verification Code", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = otp,
                                onValueChange = { viewModel.updateOtp(it) },
                                label = { Text("6-Digit OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                    viewModel.verifyOtp(handleNavigationOutcome)
                                },
                                enabled = !loading,
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
                                    Text("Verify Code", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        // Back to selection
                        Text(
                            text = "Back to Sign In options",
                            color = colors.accent,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { viewModel.resetStates() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    LoginFlowState.GOOGLE_LINK_PHONE -> {
                        Text(
                            text = "Link a phone number (optional) to allow friends to discover you in their contacts sync.",
                            fontSize = 13.sp,
                            color = colors.text.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        if (!linkPhoneCodeSent) {
                            OutlinedTextField(
                                value = linkPhone,
                                onValueChange = { viewModel.updateLinkPhone(it) },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                                onClick = { viewModel.sendLinkPhoneOtp() },
                                enabled = !loading,
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
                                    Text("Send Link OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = linkPhoneCode,
                                onValueChange = { viewModel.updateLinkPhoneCode(it) },
                                label = { Text("6-Digit Code") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                    viewModel.verifyLinkPhoneOtp(handleNavigationOutcome)
                                },
                                enabled = !loading,
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
                                    Text("Verify and Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        // Skip linking
                        Text(
                            text = "Skip Linking & Continue",
                            color = colors.accent,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { viewModel.skipLinkPhone(handleNavigationOutcome) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Fallback Mock Google Account Dialog
    if (showMockChooser) {
        var customEmail by remember { mutableStateOf("") }
        var customName by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { viewModel.dismissMockGoogleChooser() }) {
            SketchyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Select Google Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )

                    // Mock accounts list
                    val mockAccounts = listOf(
                        Triple("alice@gmail.com", "Alice Smith", "https://i.pravatar.cc/150?img=10"),
                        Triple("bob@gmail.com", "Bob Jones", "https://i.pravatar.cc/150?img=11"),
                        Triple("charlie@gmail.com", "Charlie Miller", "https://i.pravatar.cc/150?img=12")
                    )

                    mockAccounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.signInWithGoogleMock(acc.first, acc.second, acc.third, handleNavigationOutcome)
                                }
                                .sketchyBorder(1.dp, colors.text.copy(alpha = 0.2f), 8.dp)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SketchyDoodle(name = "profile", color = colors.accent, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = acc.second, fontWeight = FontWeight.Bold, color = colors.text)
                                Text(text = acc.first, fontSize = 12.sp, color = colors.text.copy(alpha = 0.6f))
                            }
                        }
                    }

                    Divider(color = colors.text.copy(alpha = 0.2f))

                    Text(
                        text = "Or Sign In with Custom Custom Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text.copy(alpha = 0.6f)
                    )

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.text.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.text.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissMockGoogleChooser() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (customEmail.contains("@") && customName.isNotBlank()) {
                                    viewModel.signInWithGoogleMock(customEmail, customName, null, handleNavigationOutcome)
                                }
                            },
                            enabled = customEmail.contains("@") && customName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mock Login")
                        }
                    }
                }
            }
        }
    }
}
