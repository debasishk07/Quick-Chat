package com.quickchat.feature.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyCard
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    loginViewModel: LoginViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalSketchyColors.current
    val currentUser by loginViewModel.currentUser.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    val currentWallpaper by viewModel.chatWallpaper.collectAsState()
    val currentFontSize by viewModel.chatFontSize.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    // Privacy options states
    var lastSeen by remember { mutableStateOf(viewModel.getLastSeenVisibility()) }
    var readReceipts by remember { mutableStateOf(viewModel.getReadReceiptsEnabled()) }
    var photoVisibility by remember { mutableStateOf(viewModel.getProfilePhotoVisibility()) }
    var statusVisibility by remember { mutableStateOf(viewModel.getStatusVisibility()) }

    // Notifications options states
    var soundEnabled by remember { mutableStateOf(viewModel.getSoundEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(viewModel.getVibrationEnabled()) }
    var previewEnabled by remember { mutableStateOf(viewModel.getNotificationPreviewEnabled()) }

    // Chat options states
    var enterToSend by remember { mutableStateOf(viewModel.getEnterToSend()) }
    var clearingCache by remember { mutableStateOf(false) }

    // Help & Security Dialog
    var showHelpSecurityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteState()
            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
            onLoggedOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.text,
                    navigationIconContentColor = colors.text
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Profile Shortcut Header
            SketchyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToProfile)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserAvatar(
                        avatarUrl = currentUser?.avatarUrl,
                        displayName = currentUser?.displayName ?: "Me",
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            currentUser?.displayName ?: "Set Name",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Text(
                            currentUser?.about ?: "Hey there! I am using Quick Chat.",
                            fontSize = 14.sp,
                            color = colors.text.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = "Edit Profile",
                        tint = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SketchyDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // 2. Account Category
            Text("Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Change Phone Number") },
                    supportingContent = { Text("Update registered phone number") },
                    leadingContent = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Contact administrator to change phone number", Toast.LENGTH_LONG).show()
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
                ListItem(
                    headlineContent = { Text("Delete Account", color = Color.Red, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Permanently erase account information and messages") },
                    leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable { showDeleteConfirmation = true },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, supportingColor = colors.text.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Privacy Category
            Text("Privacy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Allow search by Username") },
                    supportingContent = { Text("Let others find you by your unique username") },
                    leadingContent = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.text) },
                    trailingContent = {
                        Switch(
                            checked = currentUser?.usernameSearchEnabled == true,
                            onCheckedChange = { checked ->
                                currentUser?.phone?.let { uid ->
                                    viewModel.setUsernameSearchEnabled(uid, checked) { success ->
                                        if (!success) {
                                            Toast.makeText(context, "Failed to update search privacy", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.accent.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
                ListItem(
                    headlineContent = { Text("Last Seen") },
                    supportingContent = { Text(lastSeen.replaceFirstChar { it.uppercase() }) },
                    leadingContent = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable {
                        val items = arrayOf("everyone", "contacts", "nobody")
                        val next = items[(items.indexOf(lastSeen) + 1) % items.size]
                        viewModel.setLastSeenVisibility(next)
                        lastSeen = next
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
                ListItem(
                    headlineContent = { Text("Profile Photo Visibility") },
                    supportingContent = { Text(photoVisibility.replaceFirstChar { it.uppercase() }) },
                    leadingContent = { Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable {
                        val items = arrayOf("everyone", "contacts", "nobody")
                        val next = items[(items.indexOf(photoVisibility) + 1) % items.size]
                        viewModel.setProfilePhotoVisibility(next)
                        photoVisibility = next
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
                ListItem(
                    headlineContent = { Text("Read Receipts") },
                    supportingContent = { Text("Show when you've viewed incoming chats") },
                    leadingContent = { Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.text) },
                    trailingContent = {
                        Switch(
                            checked = readReceipts,
                            onCheckedChange = {
                                viewModel.setReadReceiptsEnabled(it)
                                readReceipts = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.accent.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Notifications Category
            Text("Notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Notification Sounds") },
                    leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null, tint = colors.text) },
                    trailingContent = {
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                viewModel.setSoundEnabled(it)
                                soundEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.accent.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text)
                )
                ListItem(
                    headlineContent = { Text("Vibration Alert") },
                    leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null, tint = colors.text) },
                    trailingContent = {
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                viewModel.setVibrationEnabled(it)
                                vibrationEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.accent.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Chats Category
            Text("Chats", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Wallpaper") },
                    supportingContent = { Text("Active theme: $currentWallpaper") },
                    leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable { showWallpaperDialog = true },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
                ListItem(
                    headlineContent = { Text("Font Size") },
                    supportingContent = { Text("Current size: ${currentFontSize.toInt()}sp") },
                    leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable { showFontSizeDialog = true },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
                ListItem(
                    headlineContent = { Text("Enter to Send") },
                    supportingContent = { Text("Press enter key to send message") },
                    leadingContent = { Icon(Icons.Outlined.Send, contentDescription = null, tint = colors.text) },
                    trailingContent = {
                        Switch(
                            checked = enterToSend,
                            onCheckedChange = {
                                viewModel.setEnterToSend(it)
                                enterToSend = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.accent.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Storage & Cache Category
            Text("Storage & Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Clear Cache") },
                    supportingContent = { Text("Free space from media downloads") },
                    leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.text) },
                    trailingContent = {
                        if (clearingCache) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.accent)
                        } else {
                            TextButton(onClick = {
                                clearingCache = true
                                viewModel.clearCache {
                                    clearingCache = false
                                    Toast.makeText(context, "Local cache cleared", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Clear", color = colors.accent, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. System & Theme Category
            Text("App Theme", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Theme Settings") },
                    supportingContent = { Text(currentTheme.replaceFirstChar { it.uppercase() }) },
                    leadingContent = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable { showThemeDialog = true },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Help Category
            Text("About & Security", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent, modifier = Modifier.padding(vertical = 8.dp))
            SketchyCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Help & Security") },
                    supportingContent = { Text("App version 1.0.0 • Security policies") },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null, tint = colors.text) },
                    modifier = Modifier.clickable { showHelpSecurityDialog = true },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = colors.text, supportingColor = colors.text.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    loginViewModel.logout()
                    onLoggedOut()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.background,
                    contentColor = Color.Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .sketchyBorder(1.dp, Color.Red, 25.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialogs Setup
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account?", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("This will permanently clear your cryptographic verification keys, delete all cached chat databases locally, and log you out. This action is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAccount()
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = colors.text)
                }
            },
            containerColor = colors.surface
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("light", "dark", "system").forEach { themeOption ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(themeOption)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = currentTheme == themeOption,
                                onClick = {
                                    viewModel.setTheme(themeOption)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(themeOption.replaceFirstChar { it.uppercase() }, color = colors.text)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.surface
        )
    }

    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = { Text("Chat Font Size", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(14f to "Small", 16f to "Medium", 18f to "Large").forEach { (size, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setChatFontSize(size)
                                    showFontSizeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = currentFontSize == size,
                                onClick = {
                                    viewModel.setChatFontSize(size)
                                    showFontSizeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = colors.text)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.surface
        )
    }

    if (showWallpaperDialog) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = { Text("Choose Wallpaper Style", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("default", "warm_cream", "charcoal_dark", "terracotta_sunset").forEach { style ->
                        val circleBg = when(style) {
                            "warm_cream" -> Color(0xFFF5F0E8)
                            "charcoal_dark" -> Color(0xFF22201D)
                            "terracotta_sunset" -> Color(0xFFE5B09E)
                            else -> colors.surface
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.setChatWallpaper(style)
                                showWallpaperDialog = false
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(circleBg)
                                    .sketchyBorder(
                                        width = if (currentWallpaper == style) 2.dp else 1.dp,
                                        color = if (currentWallpaper == style) colors.accent else colors.text.copy(alpha = 0.5f),
                                        cornerRadius = 24.dp
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = style.replace("_", " ").substringBefore(" ").replaceFirstChar { it.uppercase() },
                                fontSize = 10.sp,
                                color = colors.text,
                                fontWeight = if (currentWallpaper == style) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.surface
        )
    }

    if (showHelpSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showHelpSecurityDialog = false },
            title = {
                Text(
                    text = "End-to-End Encryption (E2EE)",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Quick Chat provides end-to-end encryption for all conversations. Here is how it works under the hood:",
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Keys Exchange: When you register, your device generates a Master Identity Key, Signed PreKey, and ephemeral One-Time PreKeys. These are uploaded to the secure keys server.\n\n" +
                                "2. Session Establishment: When you start a chat, your device requests the recipient's prekey bundle to perform an Extended Triple Diffie-Hellman (X3DH) handshake. This derives a shared root key.\n\n" +
                                "3. Double Ratchet: Every message sent utilizes a ratcheted sequence of keys. This ensures Forward Secrecy and Post-Compromise Security (if a single key is leaked, past and future messages remain completely safe).\n\n" +
                                "4. Zero Knowledge: The server has absolutely no access to the keys or the plain text. It only routes encrypted ciphertext envelopes.",
                        fontSize = 12.sp,
                        color = colors.text.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpSecurityDialog = false }) {
                    Text("Understood", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.surface
        )
    }
}
