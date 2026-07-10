package com.quickchat.feature.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageType
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyCard
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    viewModel: ChatRoomViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVerify: (phone: String) -> Unit,
    onNavigateToMediaGallery: (phone: String) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalSketchyColors.current

    val partnerName by viewModel.recipientName.collectAsState()
    val partnerPhone by viewModel.recipientPhone.collectAsState()
    val partnerAvatar by viewModel.recipientAvatar.collectAsState()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val disappearingDuration by viewModel.recipientDisappearingDuration.collectAsState()
    val messages by viewModel.messages.collectAsState()

    // Dialog state controllers
    var showFullPhoto by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Muted state simulated locally (read/write logic matching Firebase Cloud Messaging)
    var isMuted by remember { mutableStateOf(false) }
    var muteDurationText by remember { mutableStateOf("Off") }

    // Check system Accessibility reduced motion setting
    val isReducedMotion = remember {
        try {
            val scale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }

    // Filter shared media thumbnail strips
    val mediaMessages = remember(messages) {
        messages.filter { it.messageType == MessageType.IMAGE || it.messageType == MessageType.VIDEO }
    }
    val starredCount = remember(messages) {
        messages.count { it.isStarred }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isReducedMotion) {
                        Text(
                            text = partnerName,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.text
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        val lazyListState = rememberLazyListState()

        // Calculate Collapsing Header size factor based on scroll offset (disable if reduced motion)
        val collapsingHeaderHeight = if (isReducedMotion) {
            0.dp
        } else {
            val offset = lazyListState.firstVisibleItemScrollOffset
            val firstIndex = lazyListState.firstVisibleItemIndex
            if (firstIndex > 0) 0.dp else (240.dp - (offset / 2f).dp).coerceAtLeast(80.dp)
        }

        val headerAlpha = if (isReducedMotion) {
            0f
        } else {
            val heightFraction = if (collapsingHeaderHeight > 80.dp) {
                ((collapsingHeaderHeight - 80.dp) / 160.dp)
            } else {
                0f
            }
            heightFraction.coerceIn(0f, 1f)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Collapsing Header
            if (!isReducedMotion && collapsingHeaderHeight > 80.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsingHeaderHeight)
                        .background(colors.background)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((collapsingHeaderHeight.value * 0.45f).dp)
                            .clip(CircleShape)
                            .background(colors.border.copy(alpha = 0.1f))
                            .sketchyBorder(1.5.dp, colors.text, (collapsingHeaderHeight.value * 0.45f).dp)
                            .clickable { showFullPhoto = true }
                    ) {
                        UserAvatar(
                            avatarUrl = partnerAvatar,
                            displayName = partnerName,
                            size = (collapsingHeaderHeight.value * 0.45f).dp
                        )
                    }

                    if (headerAlpha > 0.3f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = partnerName,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = (18 + (6 * headerAlpha)).sp,
                            color = colors.text.copy(alpha = headerAlpha)
                        )
                        Text(
                            text = partnerPhone,
                            fontSize = 14.sp,
                            color = colors.text.copy(alpha = headerAlpha * 0.6f)
                        )
                    }
                }
                SketchyDivider()
            }

            // Scrollable Content
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Render Static Header if reduced motion is enabled
                if (isReducedMotion) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .sketchyBorder(1.5.dp, colors.text, 100.dp)
                                    .clickable { showFullPhoto = true }
                            ) {
                                UserAvatar(
                                    avatarUrl = partnerAvatar,
                                    displayName = partnerName,
                                    size = 100.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = partnerName,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = colors.text
                            )
                            Text(
                                text = partnerPhone,
                                fontSize = 14.sp,
                                color = colors.text.copy(alpha = 0.6f)
                            )
                        }
                        SketchyDivider()
                    }
                }

                // E2EE verification row
                item {
                    SketchyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToVerify(partnerPhone) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Security",
                                tint = colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Encryption",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text
                                )
                                Text(
                                    text = "Messages are end-to-end encrypted. Tap to verify security code.",
                                    fontSize = 13.sp,
                                    color = colors.text.copy(alpha = 0.7f)
                                )
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.text)
                        }
                    }
                }

                // Shared Media Horizontal Strip
                item {
                    SketchyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMediaGallery(partnerPhone) }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = "Media",
                                        tint = colors.text,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Media, Links & Docs",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.text
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mediaMessages.size.toString(),
                                        fontSize = 14.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.text)
                                }
                            }

                            if (mediaMessages.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    mediaMessages.take(6).forEach { msg ->
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.border.copy(alpha = 0.1f))
                                                .sketchyBorder(1.dp, colors.text, 8.dp)
                                        ) {
                                            // Since plainText stores local path or key base64 for media, Coil loads it if it is a local path
                                            AsyncImage(
                                                model = msg.plainText?.substringBefore("#"),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Settings section (Starred, Disappearing, Mute)
                item {
                    SketchyCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            // Starred Messages
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast
                                            .makeText(
                                                context,
                                                "Use message search on chat room to filter starred messages",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }
                                    .padding(14.dp)
                            ) {
                                Icon(Icons.Outlined.Star, contentDescription = null, tint = colors.text)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Starred Messages",
                                    modifier = Modifier.weight(1f),
                                    color = colors.text
                                )
                                Text(
                                    text = starredCount.toString(),
                                    fontSize = 14.sp,
                                    color = colors.text.copy(alpha = 0.5f)
                                )
                            }
                            SketchyDivider()

                            // Disappearing Messages
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDisappearingDialog = true }
                                    .padding(14.dp)
                            ) {
                                Icon(Icons.Outlined.History, contentDescription = null, tint = colors.text)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Disappearing Messages", color = colors.text)
                                    Text(
                                        text = when (disappearingDuration) {
                                            0L -> "Off"
                                            24 * 3600 * 1000L -> "24 Hours"
                                            7 * 24 * 3600 * 1000L -> "7 Days"
                                            90 * 24 * 3600 * 1000L -> "90 Days"
                                            else -> "Active"
                                        },
                                        fontSize = 12.sp,
                                        color = colors.text.copy(alpha = 0.6f)
                                    )
                                }
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.text)
                            }
                            SketchyDivider()

                            // Mute notifications
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMuteDialog = true }
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = colors.text
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Mute Notifications", color = colors.text)
                                    Text(
                                        text = if (isMuted) "Muted ($muteDurationText)" else "Off",
                                        fontSize = 12.sp,
                                        color = colors.text.copy(alpha = 0.6f)
                                    )
                                }
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.text)
                            }
                        }
                    }
                }

                // Danger zone section
                item {
                    Text(
                        text = "Danger Zone",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SketchyCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            // Clear history
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClearHistoryDialog = true }
                                    .padding(14.dp)
                            ) {
                                Icon(Icons.Outlined.DeleteSweep, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Clear Chat History", color = Color.Red)
                            }
                            SketchyDivider()

                            // Delete chat
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDeleteChatDialog = true }
                                    .padding(14.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Delete Conversation", color = Color.Red)
                            }
                            SketchyDivider()

                            // Block
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showBlockDialog = true }
                                    .padding(14.dp)
                            ) {
                                Icon(Icons.Outlined.Block, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = if (isBlocked) "Unblock $partnerName" else "Block $partnerName",
                                    color = Color.Red
                                )
                            }
                            SketchyDivider()

                            // Report
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showReportDialog = true }
                                    .padding(14.dp)
                            ) {
                                Icon(Icons.Outlined.Report, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Report $partnerName", color = Color.Red)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // 1. Full Photo Viewer Dialog
    if (showFullPhoto) {
        Dialog(onDismissRequest = { showFullPhoto = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black, RoundedCornerShape(16.dp))
                    .sketchyBorder(2.dp, colors.text, 16.dp),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    avatarUrl = partnerAvatar,
                    displayName = partnerName,
                    size = 300.dp
                )
            }
        }
    }

    // 2. Disappearing messages duration dialog
    if (showDisappearingDialog) {
        AlertDialog(
            onDismissRequest = { showDisappearingDialog = false },
            title = { Text("Disappearing Messages", fontFamily = FontFamily.Serif) },
            text = {
                Column {
                    Text("New messages sent in this chat will disappear for both sides after the chosen timer.")
                    Spacer(modifier = Modifier.height(16.dp))
                    val options = listOf(
                        "Off" to 0L,
                        "24 Hours" to 24 * 3600 * 1000L,
                        "7 Days" to 7 * 24 * 3600 * 1000L,
                        "90 Days" to 90 * 24 * 3600 * 1000L
                    )
                    options.forEach { (label, duration) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDisappearingDuration(duration)
                                    showDisappearingDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = disappearingDuration == duration,
                                onClick = {
                                    viewModel.setDisappearingDuration(duration)
                                    showDisappearingDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, color = colors.text)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDisappearingDialog = false }) {
                    Text("Close", color = colors.accent)
                }
            },
            containerColor = colors.background,
            titleContentColor = colors.text,
            textContentColor = colors.text
        )
    }

    // 3. Mute notification dialog
    if (showMuteDialog) {
        AlertDialog(
            onDismissRequest = { showMuteDialog = false },
            title = { Text("Mute Notifications", fontFamily = FontFamily.Serif) },
            text = {
                Column {
                    Text("Mute alerts for incoming messages in this chat.")
                    Spacer(modifier = Modifier.height(16.dp))
                    val options = listOf(
                        "8 Hours",
                        "1 Week",
                        "Always",
                        "Unmute"
                    )
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (option == "Unmute") {
                                        isMuted = false
                                        muteDurationText = "Off"
                                    } else {
                                        isMuted = true
                                        muteDurationText = option
                                    }
                                    showMuteDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(option, color = colors.text, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMuteDialog = false }) {
                    Text("Cancel", color = colors.accent)
                }
            },
            containerColor = colors.background,
            titleContentColor = colors.text,
            textContentColor = colors.text
        )
    }

    // 4. Clear chat history dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Chat History?", fontFamily = FontFamily.Serif, color = Color.Red) },
            text = { Text("This will permanently delete all messages in this conversation. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearHistoryDialog = false
                    Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = colors.accent)
                }
            },
            containerColor = colors.background,
            titleContentColor = colors.text,
            textContentColor = colors.text
        )
    }

    // 5. Delete Chat dialog
    if (showDeleteChatDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteChatDialog = false },
            title = { Text("Delete Conversation?", fontFamily = FontFamily.Serif, color = Color.Red) },
            text = { Text("This will delete all message logs and remove the conversation from your chat list.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChat {
                        showDeleteChatDialog = false
                        onNavigateBack()
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChatDialog = false }) {
                    Text("Cancel", color = colors.accent)
                }
            },
            containerColor = colors.background,
            titleContentColor = colors.text,
            textContentColor = colors.text
        )
    }

    // 6. Block contact confirmation dialog
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = {
                Text(
                    text = if (isBlocked) "Unblock $partnerName?" else "Block $partnerName?",
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Text(
                    text = if (isBlocked) {
                        "You will now be able to receive messages, voice calls, and status updates from this contact."
                    } else {
                        "Blocked contacts will no longer be able to message or call you. Your last seen, online, and status updates will be completely hidden from them."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isBlocked) viewModel.unblockRecipient() else viewModel.blockRecipient()
                    showBlockDialog = false
                    Toast.makeText(
                        context,
                        if (isBlocked) "Contact unblocked" else "Contact blocked",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text(text = if (isBlocked) "Unblock" else "Block", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancel", color = colors.accent)
                }
            },
            containerColor = colors.background,
            titleContentColor = colors.text,
            textContentColor = colors.text
        )
    }

    // 7. Report contact dialog
    if (showReportDialog) {
        var reasonSelected by remember { mutableStateOf("Spam") }
        var descriptionText by remember { mutableStateOf("") }
        var attachHistory by remember { mutableStateOf(true) }
        var reportAndBlock by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report $partnerName", fontFamily = FontFamily.Serif) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "If you report this user, the safety team will review their activity. Messages will only be shared if you check the attachment option below.",
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select a Reason:", fontWeight = FontWeight.Bold)
                    val reasons = listOf("Spam", "Harassment or abuse", "Inappropriate content", "Impersonation", "Other")
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reasonSelected = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = reasonSelected == r, onClick = { reasonSelected = r })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(r, color = colors.text)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Additional details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.text.copy(alpha = 0.5f),
                            focusedLabelColor = colors.accent,
                            unfocusedLabelColor = colors.text.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = attachHistory, onCheckedChange = { attachHistory = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Attach last 5 messages in plaintext for context",
                            fontSize = 12.sp,
                            color = colors.text
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = reportAndBlock, onCheckedChange = { reportAndBlock = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Block contact and delete message history",
                            fontSize = 12.sp,
                            color = colors.text
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reportRecipient(
                        reason = reasonSelected,
                        description = descriptionText.takeIf { it.isNotBlank() },
                        attachMessages = attachHistory
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_LONG).show()
                            if (reportAndBlock) {
                                viewModel.blockRecipient()
                                viewModel.clearHistory()
                            }
                        } else {
                            Toast.makeText(context, "Failed to submit report", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showReportDialog = false
                }) {
                    Text("Submit Report", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = colors.accent)
                }
            },
            containerColor = colors.background,
            titleContentColor = colors.text,
            textContentColor = colors.text
        )
    }
}
