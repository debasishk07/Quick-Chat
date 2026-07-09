package com.quickchat.feature.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageStatus
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.sketchyBorder
import java.text.SimpleDateFormat
import java.util.*
import com.quickchat.core.model.MessageType
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam

// Simple Helper import to override mutableStateOf
import androidx.compose.runtime.mutableStateOf as mutableStateFlowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: ChatRoomViewModel,
    chatWallpaper: String,
    chatFontSize: Float,
    onNavigateBack: () -> Unit,
    onNavigateToVerify: (phone: String) -> Unit,
    onStartCall: (phone: String, isVideo: Boolean) -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val partnerName by viewModel.recipientName.collectAsState()
    val partnerPhone by viewModel.recipientPhone.collectAsState()
    val isOnline by viewModel.isPartnerOnline.collectAsState()
    val lastSeen by viewModel.partnerLastSeen.collectAsState()
    val isTyping by viewModel.partnerTyping.collectAsState()

    var textInput by remember { mutableStateFlowOf("") }
    val listState = rememberLazyListState()
    val colors = LocalSketchyColors.current

    var showPermissionRationale by remember { mutableStateFlowOf(false) }
    var isVideoCallTrigger by remember { mutableStateFlowOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted && audioGranted) {
            onStartCall(partnerPhone, isVideoCallTrigger)
        }
    }

    val context = LocalContext.current
    val checkAndStartCall = { isVideo: Boolean ->
        isVideoCallTrigger = isVideo
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        
        if (hasCamera && hasAudio) {
            onStartCall(partnerPhone, isVideo)
        } else {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                title = {
                    Column {
                        Text(
                            partnerName,
                            color = colors.text,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = when {
                                isTyping -> "typing..."
                                isOnline -> "online"
                                lastSeen > 0 -> {
                                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    "last seen at ${sdf.format(Date(lastSeen))}"
                                }
                                else -> "offline"
                            },
                            color = if (isTyping || isOnline) colors.accent else colors.text.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { checkAndStartCall(false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = colors.accent)
                    }
                    IconButton(onClick = { checkAndStartCall(true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = colors.accent)
                    }
                    IconButton(onClick = { onNavigateToVerify(partnerPhone) }) {
                        Icon(Icons.Outlined.Lock, contentDescription = "Verify Encryption", tint = colors.accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background Wallpaper layer
            ChatRoomBackground(wallpaperStyle = chatWallpaper, colors.text.copy(alpha = 0.05f))

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val isMe = message.recipientPhone == partnerPhone
                        MessageBubble(
                            message = message,
                            isMe = isMe,
                            fontSize = chatFontSize
                        )
                    }
                    
                    if (isTyping) {
                        item {
                            TypingBubble(partnerName)
                        }
                    }
                }

                // Input Bar
                Surface(
                    color = colors.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SketchyDivider(color = colors.border)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = {
                                    textInput = it
                                    viewModel.sendTyping(it.isNotEmpty())
                                },
                                placeholder = { Text("Write a message...", color = colors.text.copy(alpha = 0.5f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.text,
                                    unfocusedTextColor = colors.text,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                maxLines = 4
                            )

                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.sendMessage(textInput)
                                        textInput = ""
                                        viewModel.sendTyping(false)
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent)
                                    .sketchyBorder(1.dp, colors.text, 20.dp)
                            ) {
                                Icon(Icons.Outlined.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (showPermissionRationale) {
                AlertDialog(
                    onDismissRequest = { showPermissionRationale = false },
                    title = {
                        Text(
                            text = "Permission Required",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                    },
                    text = {
                        Text(
                            text = "Quick Chat needs Camera and Audio permissions to establish secure E2E encrypted voice and video calls.",
                            color = colors.text
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPermissionRationale = false
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("Continue", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPermissionRationale = false }) {
                            Text("Cancel", color = colors.text)
                        }
                    },
                    containerColor = colors.surface,
                    modifier = Modifier.sketchyBorder(2.dp, colors.border, 16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatRoomBackground(wallpaperStyle: String, strokeColor: Color) {
    val colors = LocalSketchyColors.current
    val bg = when (wallpaperStyle) {
        "warm_cream" -> Color(0xFFF5F0E8)
        "charcoal_dark" -> Color(0xFF161513)
        "terracotta_sunset" -> Color(0xFFE5B09E)
        else -> colors.background
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        if (wallpaperStyle == "default" || wallpaperStyle == "warm_cream") {
            // Hand-drawn sketchy horizontal ledger lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 32.dp.toPx()
                var y = step
                while (y < size.height) {
                    // Draw a slightly wavy ledger line
                    drawLine(
                        color = strokeColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y + (Math.sin(y.toDouble()) * 2).toFloat()),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += step
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean, fontSize: Float) {
    var showReactionMenu by remember { mutableStateFlowOf(false) }
    var selectedReaction by remember { mutableStateFlowOf<String?>(null) }
    val colors = LocalSketchyColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        )
                    )
                    .background(if (isMe) colors.accent else colors.surface)
                    .sketchyBorder(
                        width = 1.dp,
                        color = colors.text,
                        cornerRadius = 16.dp
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showReactionMenu = true }
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (message.messageType == MessageType.CALL_LOG) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isVideoCall = message.plainText?.contains("Video Call", ignoreCase = true) == true
                            val icon = if (isVideoCall) Icons.Default.Videocam else Icons.Default.Call
                            Icon(
                                imageVector = icon,
                                contentDescription = "Call Log",
                                tint = if (isMe) Color.White else colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = message.plainText ?: "Call",
                                color = if (isMe) Color.White else colors.text,
                                fontSize = fontSize.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = message.plainText ?: "[Decryption Error]",
                            color = if (isMe) Color.White else colors.text,
                            fontSize = fontSize.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(message.timestamp)),
                            color = if (isMe) Color.White.copy(alpha = 0.7f) else colors.text.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )

                        if (isMe) {
                            val ticks = when (message.status) {
                                MessageStatus.SENDING -> "⌛"
                                MessageStatus.SENT -> "✓"
                                MessageStatus.DELIVERED -> "✓✓"
                                MessageStatus.READ -> "✓✓"
                            }
                            val tickColor = if (message.status == MessageStatus.READ) Color(0xFF81C784) else Color.White.copy(alpha = 0.8f)
                            Text(ticks, color = tickColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            selectedReaction?.let { reaction ->
                Box(
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .background(colors.surface, RoundedCornerShape(8.dp))
                        .sketchyBorder(0.5.dp, colors.text, 8.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(reaction, fontSize = 12.sp)
                }
            }
        }

        if (showReactionMenu) {
            AlertDialog(
                onDismissRequest = { showReactionMenu = false },
                title = { Text("React to Message", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.text) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
                            Text(
                                emoji,
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .clickable {
                                        selectedReaction = emoji
                                        showReactionMenu = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                },
                confirmButton = {},
                containerColor = colors.surface
            )
        }
    }
}

@Composable
fun TypingBubble(partnerName: String) {
    val colors = LocalSketchyColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp))
                .background(colors.surface)
                .sketchyBorder(0.5.dp, colors.text, 16.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("$partnerName is typing...", color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
