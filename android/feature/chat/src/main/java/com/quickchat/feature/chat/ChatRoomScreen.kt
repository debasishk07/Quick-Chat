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
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Block
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageStatus
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder
import com.quickchat.core.model.theme.SketchyBottomSheet
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
import android.view.WindowManager
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.lazy.LazyRow
import org.json.JSONObject
import org.json.JSONArray
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.mutableStateOf as mutableStateFlowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: ChatRoomViewModel,
    chatWallpaper: String,
    chatFontSize: Float,
    highlightMessageId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToVerify: (phone: String) -> Unit,
    onNavigateToContactInfo: (phone: String) -> Unit,
    onStartCall: (phone: String, isVideo: Boolean) -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val partnerName by viewModel.recipientName.collectAsState()
    val partnerPhone by viewModel.recipientPhone.collectAsState()
    val partnerAvatar by viewModel.recipientAvatar.collectAsState()
    val isProfileLoaded by viewModel.isProfileLoaded.collectAsState()
    val isOnline by viewModel.isPartnerOnline.collectAsState()
    val lastSeen by viewModel.partnerLastSeen.collectAsState()
    val isTyping by viewModel.partnerTyping.collectAsState()

    val context = LocalContext.current
    var textInput by remember { mutableStateFlowOf("") }
    val listState = rememberLazyListState()
    val colors = LocalSketchyColors.current

    val voicePlayer = remember { VoiceMessagePlayer(context) }
    DisposableEffect(partnerPhone) {
        onDispose {
            viewModel.stopTyping()
            voicePlayer.stop()
        }
    }

    var isRecording by remember { mutableStateFlowOf(false) }
    var isRecordingLocked by remember { mutableStateFlowOf(false) }
    var recordingDuration by remember { mutableStateFlowOf(0L) }
    var recordAmplitudes by remember { mutableStateFlowOf(listOf<Int>()) }
    val recordTimerScope = rememberCoroutineScope()
    var recordJob by remember { mutableStateFlowOf<Job?>(null) }
    val recorder = remember { VoiceMessageRecorder(context) }

    var editingMessage by remember { mutableStateFlowOf<Message?>(null) }
    var openViewOnceMessage by remember { mutableStateFlowOf<Message?>(null) }

    var flashedMessageId by remember { mutableStateFlowOf<String?>(null) }
    var replyingToMessage by remember { mutableStateFlowOf<Message?>(null) }
    LaunchedEffect(highlightMessageId, messages) {
        if (highlightMessageId != null && messages.isNotEmpty()) {
            val index = messages.indexOfFirst { it.id == highlightMessageId }
            if (index >= 0) {
                listState.scrollToItem(index)
                flashedMessageId = highlightMessageId
            }
        }
    }

    LaunchedEffect(flashedMessageId) {
        if (flashedMessageId != null) {
            kotlinx.coroutines.delay(1500)
            flashedMessageId = null
        }
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                        }
                        if (!isProfileLoaded) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.text.copy(alpha = 0.15f))
                            )
                        } else {
                            UserAvatar(
                                avatarUrl = partnerAvatar,
                                displayName = partnerName,
                                size = 36.dp,
                                modifier = Modifier.clickable { onNavigateToContactInfo(partnerPhone) }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                },
                title = {
                    Column(modifier = Modifier.clickable { onNavigateToContactInfo(partnerPhone) }) {
                        if (!isProfileLoaded) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(16.dp)
                                    .background(colors.text.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(10.dp)
                                    .background(colors.text.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    partnerName,
                                    color = colors.text,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                val disappearingDuration by viewModel.recipientDisappearingDuration.collectAsState()
                                if (disappearingDuration > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = "Disappearing messages active",
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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

            var showAttachmentMenu by remember { mutableStateFlowOf(false) }
            val haptic = LocalHapticFeedback.current

            Column(modifier = Modifier.fillMaxSize()) {
                val pinnedMessages = messages.filter { it.pinnedAt != null }.sortedByDescending { it.pinnedAt }
                if (pinnedMessages.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                            .sketchyBorder(0.5.dp, colors.text, 0.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned Messages",
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            pinnedMessages.take(3).forEach { pm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val index = messages.indexOfFirst { it.id == pm.id }
                                            if (index >= 0) {
                                                recordTimerScope.launch {
                                                    listState.animateScrollToItem(index)
                                                }
                                            }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pinned: " + (pm.plainText ?: "Attachment"),
                                        fontSize = 12.sp,
                                        color = colors.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.pinMessage(pm.id, false) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Unpin",
                                            tint = colors.text.copy(alpha = 0.5f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    SketchyDivider(color = colors.border)
                }

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
                        SwipeToReplyContainer(
                            onSwipeTriggered = {
                                replyingToMessage = message
                            }
                        ) {
                            MessageBubble(
                                message = message,
                                isMe = isMe,
                                fontSize = chatFontSize,
                                isHighlighted = (message.id == flashedMessageId),
                                viewModel = viewModel,
                                voicePlayer = voicePlayer,
                                onViewOnceOpened = { openViewOnceMessage = it },
                                onEditMessage = { msg ->
                                    editingMessage = msg
                                    textInput = msg.plainText ?: ""
                                }
                            )
                        }
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
                        
                        if (editingMessage != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.accent.copy(alpha = 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editing", tint = colors.accent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editing Message", fontSize = 12.sp, color = colors.accent, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                IconButton(onClick = { editingMessage = null; textInput = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel Edit", tint = colors.text.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                            SketchyDivider(color = colors.border.copy(alpha = 0.5f))
                        }

                        if (replyingToMessage != null) {
                            val replyMsg = replyingToMessage!!
                            val replySenderName = if (replyMsg.recipientPhone == partnerPhone) "You" else partnerName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.text.copy(alpha = 0.05f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                        .background(colors.accent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Replying to $replySenderName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.accent
                                    )
                                    Text(
                                        text = replyMsg.plainText ?: "[Encrypted Media]",
                                        fontSize = 12.sp,
                                        color = colors.text.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { replyingToMessage = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel Reply",
                                        tint = colors.text.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            SketchyDivider(color = colors.border.copy(alpha = 0.5f))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isRecording) {
                                IconButton(onClick = { showAttachmentMenu = true }) {
                                    Icon(Icons.Default.Attachment, contentDescription = "Attach File", tint = colors.accent)
                                }
                                
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = {
                                        textInput = it
                                        viewModel.onInputTextChanged(it)
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

                                if (textInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            if (editingMessage != null) {
                                                viewModel.editMessage(editingMessage!!.id, textInput)
                                                editingMessage = null
                                            } else {
                                                val finalMsg = if (replyingToMessage != null) {
                                                    val replyMsg = replyingToMessage!!
                                                    val replySenderName = if (replyMsg.recipientPhone == partnerPhone) "You" else partnerName
                                                    val cleanText = (replyMsg.plainText ?: "[Encrypted Media]").replace("\n", " ")
                                                    "> $replySenderName: $cleanText\n\n$textInput"
                                                } else {
                                                    textInput
                                                }
                                                viewModel.sendMessage(finalMsg)
                                            }
                                            textInput = ""
                                            replyingToMessage = null
                                            viewModel.stopTyping()
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                            .sketchyBorder(1.dp, colors.text, 20.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    // WhatsApp mic button with touch drag gestures
                                    var dragAmountX by remember { mutableStateFlowOf(0f) }
                                    var dragAmountY by remember { mutableStateFlowOf(0f) }

                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                            .sketchyBorder(1.dp, colors.text, 20.dp)
                                            .pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        recorder.start()
                                                        isRecording = true
                                                        isRecordingLocked = false
                                                        recordAmplitudes = emptyList()
                                                        recordJob = recordTimerScope.launch {
                                                            while (isRecording) {
                                                                delay(100)
                                                                recordingDuration = recorder.getDuration()
                                                                val amp = recorder.getAmplitude()
                                                                val scaled = (amp / 327.67f).toInt().coerceIn(0, 100)
                                                                recordAmplitudes = recordAmplitudes + scaled
                                                            }
                                                        }
                                                        dragAmountX = 0f
                                                        dragAmountY = 0f
                                                    },
                                                    onDragEnd = {
                                                        if (isRecording) {
                                                            if (!isRecordingLocked) {
                                                                val (file, amps) = recorder.stop()
                                                                isRecording = false
                                                                recordJob?.cancel()
                                                                if (file != null) {
                                                                    viewModel.sendVoiceMessage(file.absolutePath, amps)
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onDragCancel = {
                                                        if (isRecording) {
                                                            recorder.cancel()
                                                            isRecording = false
                                                            recordJob?.cancel()
                                                        }
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAmountX += dragAmount.x
                                                        dragAmountY += dragAmount.y
                                                        if (dragAmountX < -150f && !isRecordingLocked) {
                                                            recorder.cancel()
                                                            isRecording = false
                                                            recordJob?.cancel()
                                                        } else if (dragAmountY < -150f && !isRecordingLocked) {
                                                            isRecordingLocked = true
                                                        }
                                                    }
                                                )
                                            }
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            } else {
                                // Live recording preview row
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(colors.border.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val minutes = (recordingDuration / 1000) / 60
                                    val seconds = (recordingDuration / 1000) % 60
                                    Text(
                                        text = String.format("%02d:%02d", minutes, seconds),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.text
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    // Live amplitudes building up visually
                                    Canvas(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                    ) {
                                        val step = 4.dp.toPx()
                                        val midY = size.height / 2f
                                        val maxBars = (size.width / step).toInt()
                                        val subList = recordAmplitudes.takeLast(maxBars)
                                        subList.forEachIndexed { idx, amp ->
                                            val x = idx * step
                                            val barHeight = (amp / 100f) * size.height
                                            drawLine(
                                                color = colors.accent,
                                                start = Offset(x, midY - barHeight / 2f),
                                                end = Offset(x, midY + barHeight / 2f),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    }
                                }

                                if (isRecordingLocked) {
                                    // Cancel trash button
                                    IconButton(
                                        onClick = {
                                            recorder.cancel()
                                            isRecording = false
                                            recordJob?.cancel()
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Cancel Recording", tint = Color.Red)
                                    }
                                    // Send voice button
                                    IconButton(
                                        onClick = {
                                            val (file, amps) = recorder.stop()
                                            isRecording = false
                                            recordJob?.cancel()
                                            if (file != null) {
                                                viewModel.sendVoiceMessage(file.absolutePath, amps)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    Text(
                                        text = "<- Slide to Cancel / Drag Up to Lock",
                                        fontSize = 11.sp,
                                        color = colors.text.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (openViewOnceMessage != null) {
                ViewOnceMediaViewerDialog(
                    message = openViewOnceMessage!!,
                    viewModel = viewModel,
                    onDismiss = { openViewOnceMessage = null }
                )
            }

            if (showAttachmentMenu) {
                SketchyBottomSheet(
                    onDismissRequest = { showAttachmentMenu = false }
                ) {
                    Text(
                        text = "Send Secure View-Once Media",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.text,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    // Mock pick image: create file in cache
                                    val mockImg = java.io.File(context.cacheDir, "mock_image.jpg")
                                    mockImg.writeBytes(ByteArray(1024)) // 1kb mock jpg bytes
                                    viewModel.sendViewOnceMedia(mockImg.absolutePath, isVideo = false)
                                }
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "View-Once Photo", tint = colors.accent, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("View Once Photo", fontSize = 12.sp, color = colors.text)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    // Mock pick video: create file in cache
                                    val mockVid = java.io.File(context.cacheDir, "mock_video.mp4")
                                    mockVid.writeBytes(ByteArray(2048)) // 2kb mock mp4 bytes
                                    viewModel.sendViewOnceMedia(mockVid.absolutePath, isVideo = true)
                                }
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "View-Once Video", tint = colors.accent, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("View Once Video", fontSize = 12.sp, color = colors.text)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    fontSize: Float,
    isHighlighted: Boolean = false,
    viewModel: ChatRoomViewModel,
    voicePlayer: VoiceMessagePlayer,
    onViewOnceOpened: (Message) -> Unit,
    onEditMessage: (Message) -> Unit
) {
    val colors = LocalSketchyColors.current
    
    if (message.messageType == MessageType.SYSTEM) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(colors.border.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .sketchyBorder(0.5.dp, colors.text, 8.dp)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = message.plainText ?: "",
                    fontSize = 11.sp,
                    color = colors.text.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    if (message.plainText?.matches(Regex("^delete:.+:(me|everyone)$")) == true || message.plainText?.startsWith("delete:") == true) {
        return
    }

    var showReactionMenu by remember { mutableStateFlowOf(false) }
    var showDeleteDialog by remember { mutableStateFlowOf(false) }
    var deleteMode by remember { mutableStateFlowOf("me") }
    val isDeletedMsg = message.isDeleted || message.plainText == "This message was deleted"
    val context = LocalContext.current
    val copyableText: String? = remember(message.plainText, message.isDeleted, message.messageType) {
        if (isDeletedMsg) null
        else {
            val replyInfo = parseMessageReply(message.plainText)
            val text = replyInfo?.messageBody ?: message.plainText
            if (text.isNullOrBlank()) null
            else {
                val isPureMedia = message.messageType == MessageType.VOICE ||
                        message.messageType == MessageType.CALL_LOG ||
                        text == "view_once" || text.startsWith("view_once:") || text == "Opened" ||
                        (message.messageType == MessageType.IMAGE && (text.startsWith("http") || text.startsWith("/") || text.startsWith("content:"))) ||
                        (message.messageType == MessageType.VIDEO && (text.startsWith("http") || text.startsWith("/") || text.startsWith("content:")))
                if (isPureMedia) null else text
            }
        }
    }

    val bubbleBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isHighlighted) colors.accent.copy(alpha = 0.5f) else (if (isMe) colors.accent else colors.surface),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400),
        label = "MessageHighlightPulse"
    )

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
                    .background(bubbleBgColor)
                    .sketchyBorder(
                        width = 1.dp,
                        color = colors.text,
                        cornerRadius = 16.dp
                    )
                    .combinedClickable(
                        onClick = { },
                        onDoubleClick = {
                            if (!isDeletedMsg) viewModel.reactToMessage(message.id, "❤️")
                        },
                        onLongClick = { if (!isDeletedMsg) showReactionMenu = true }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                val replyInfo = remember(message.plainText) { parseMessageReply(message.plainText) }
                Column(horizontalAlignment = Alignment.Start) {
                    if (replyInfo != null && !isDeletedMsg) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.text.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(colors.accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = replyInfo.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = colors.accent
                                )
                                Text(
                                    text = replyInfo.text,
                                    fontSize = 11.sp,
                                    color = colors.text.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (isDeletedMsg) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Deleted",
                                tint = if (isMe) Color.White.copy(alpha = 0.7f) else colors.text.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "This message was deleted",
                                fontStyle = FontStyle.Italic,
                                color = if (isMe) Color.White.copy(alpha = 0.8f) else colors.text.copy(alpha = 0.6f),
                                fontSize = fontSize.sp
                            )
                        }
                    } else {
                        when (message.messageType) {
                            MessageType.CALL_LOG -> {
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
                            }
                            MessageType.VOICE -> {
                                VoiceBubble(
                                    message = message,
                                    isMe = isMe,
                                    voicePlayer = voicePlayer,
                                    viewModel = viewModel,
                                    colors = colors
                                )
                            }
                            MessageType.IMAGE, MessageType.VIDEO -> {
                                val isViewOnce = message.plainText == "view_once" || message.plainText?.startsWith("view_once:") == true || message.plainText == "Opened"
                                if (isViewOnce) {
                                    ViewOnceBubble(
                                        message = message,
                                        isMe = isMe,
                                        onOpen = { onViewOnceOpened(message) },
                                        colors = colors
                                    )
                                } else {
                                    val rawUrl = (message.plainText ?: "").split("#")[0]
                                    val displayUrl = remember(rawUrl, message.messageType, message.publicId) {
                                        if (rawUrl.contains("cloudinary.com")) {
                                            if (message.messageType == MessageType.IMAGE) {
                                                rawUrl.replace("/image/upload/", "/image/upload/f_auto,q_auto,w_400/")
                                            } else {
                                                rawUrl.replace("/video/upload/", "/video/upload/so_0,w_400,h_400,c_fill/").replace(Regex("\\.(mp4|mov|avi|mkv)$", RegexOption.IGNORE_CASE), ".jpg")
                                            }
                                        } else {
                                            rawUrl
                                        }
                                    }
                                    Box(contentAlignment = Alignment.Center) {
                                        AsyncImage(
                                            model = displayUrl,
                                            contentDescription = "Cloudinary Media",
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (message.messageType == MessageType.VIDEO) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play Video",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                val preview = remember(message.plainText) { parsePreviewPayload(message.plainText) }
                                if (preview != null) {
                                    Text(
                                        text = preview.text,
                                        color = if (isMe) Color.White else colors.text,
                                        fontSize = fontSize.sp
                                    )
                                    LinkPreviewCard(preview = preview, colors = colors)
                                } else {
                                    val bodyText = replyInfo?.messageBody ?: (message.plainText ?: "[Decryption Error]")
                                    Text(
                                        text = bodyText,
                                        color = if (isMe) Color.White else colors.text,
                                        fontSize = fontSize.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(message.timestamp)) + if (message.isEdited && !isDeletedMsg) " (edited)" else "",
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
                            val tickColor = when (message.status) {
                                MessageStatus.READ -> Color(0xFF34B7F1)
                                MessageStatus.DELIVERED -> Color.White.copy(alpha = 0.85f)
                                MessageStatus.SENT -> Color.White.copy(alpha = 0.85f)
                                MessageStatus.SENDING -> Color.White.copy(alpha = 0.5f)
                            }
                            Text(ticks, color = tickColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (!isDeletedMsg) {
                message.reaction?.let { reaction ->
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
        }

        if (showReactionMenu) {
            SketchyBottomSheet(
                onDismissRequest = { showReactionMenu = false }
            ) {
                Text(
                    text = "React & Options",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.text,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
                        Text(
                            emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.reactToMessage(message.id, emoji)
                                    showReactionMenu = false
                                }
                                .padding(8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                SketchyDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                val isPinned = message.pinnedAt != null
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(if (isPinned) "Unpin Message" else "Pin Message", color = colors.text) },
                    onClick = {
                        viewModel.pinMessage(message.id, !isPinned)
                        showReactionMenu = false
                    }
                )
                
                if (isMe) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Edit Message", color = colors.text) },
                        onClick = {
                            onEditMessage(message)
                            showReactionMenu = false
                        }
                    )
                }

                copyableText?.let { textToCopy ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Copy", color = colors.text) },
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Message", textToCopy)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            showReactionMenu = false
                        }
                    )
                }

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("Delete for Me", color = colors.text) },
                    onClick = {
                        deleteMode = "me"
                        showDeleteDialog = true
                        showReactionMenu = false
                    }
                )

                if (isMe) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Delete for Everyone", color = Color(0xFFE53935)) },
                        onClick = {
                            deleteMode = "everyone"
                            showDeleteDialog = true
                            showReactionMenu = false
                        }
                    )
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = if (deleteMode == "everyone") "Delete for Everyone?" else "Delete for Me?",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                },
                text = {
                    Text(
                        text = if (deleteMode == "everyone")
                            "This message will be deleted for all participants in the chat."
                        else
                            "This message will be deleted from your device only.",
                        color = colors.text
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMessage(message.id, deleteMode)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (deleteMode == "everyone") Color(0xFFE53935) else colors.accent
                        )
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = colors.text)
                    }
                }
            )
        }
    }
}

@Composable
fun TypingBubble(partnerName: String) {
    val colors = LocalSketchyColors.current
    val infiniteTransition = rememberInfiniteTransition()
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp))
                .background(colors.surface)
                .sketchyBorder(0.5.dp, colors.text, 16.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$partnerName is typing", color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(4.dp).alpha(alpha1).background(colors.accent, CircleShape))
                Box(modifier = Modifier.size(4.dp).alpha(alpha2).background(colors.accent, CircleShape))
                Box(modifier = Modifier.size(4.dp).alpha(alpha3).background(colors.accent, CircleShape))
            }
        }
    }
}

@Composable
fun SwipeToReplyContainer(
    onSwipeTriggered: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalSketchyColors.current
    var dragAmountX by remember { mutableStateFlowOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = dragAmountX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SwipeDrag"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmountX < -150f) {
                            onSwipeTriggered()
                        }
                        dragAmountX = 0f
                    },
                    onDragCancel = {
                        dragAmountX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountX = (dragAmountX + dragAmount).coerceIn(-250f, 0f)
                    }
                )
            }
    ) {
        if (animatedDragX < 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val scale = (abs(animatedDragX) / 150f).coerceIn(0f, 1f)
                val tint = if (abs(animatedDragX) >= 150f) colors.accent else colors.text.copy(alpha = 0.4f)
                Icon(
                    imageVector = Icons.Outlined.Reply,
                    contentDescription = "Reply",
                    tint = tint,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = scale
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedDragX.roundToInt(), 0) }
        ) {
            content()
        }
    }
}

data class ReplyInfo(
    val senderName: String,
    val text: String,
    val messageBody: String
)

fun parseMessageReply(text: String?): ReplyInfo? {
    if (text == null || !text.startsWith("> ")) return null
    try {
        val firstDoubleNewline = text.indexOf("\n\n")
        if (firstDoubleNewline < 0) return null
        
        val replyHeader = text.substring(2, firstDoubleNewline)
        val colonIndex = replyHeader.indexOf(": ")
        if (colonIndex < 0) return null
        
        val sender = replyHeader.substring(0, colonIndex)
        val replyText = replyHeader.substring(colonIndex + 2)
        val body = text.substring(firstDoubleNewline + 2)
        
        return ReplyInfo(sender, replyText, body)
    } catch (e: Exception) {
        return null
    }
}

@Composable
fun LinkPreviewCard(preview: PreviewPayload, colors: com.quickchat.core.model.theme.SketchyColors) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(preview.url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Ignore
                }
            },
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (preview.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = preview.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = preview.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (preview.description.isNotEmpty()) {
                Text(
                    text = preview.description,
                    fontSize = 11.sp,
                    color = colors.text.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VoiceBubble(
    message: Message,
    isMe: Boolean,
    voicePlayer: VoiceMessagePlayer,
    viewModel: ChatRoomViewModel,
    colors: com.quickchat.core.model.theme.SketchyColors
) {
    val payload = remember(message.plainText) { parseVoicePayload(message.plainText) } ?: return
    val context = LocalContext.current

    var isDownloaded by remember { mutableStateFlowOf(false) }
    var voiceFile by remember { mutableStateFlowOf<java.io.File?>(null) }

    LaunchedEffect(payload) {
        val file = E2EeMediaCache.getDecryptedFile(context, payload.url, payload.key, payload.iv, message.id)
        if (file != null) {
            voiceFile = file
            isDownloaded = true
        }
    }

    val isPlaying = voicePlayer.isCurrentlyPlaying(message.id)
    val progress = voicePlayer.getProgress(message.id)
    val speed = message.playbackSpeed

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        if (!isDownloaded) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (isMe) Color.White else colors.accent
            )
        } else {
            IconButton(
                onClick = {
                    voiceFile?.let { file ->
                        voicePlayer.play(message.id, file, speed) {
                            // Finished
                        }
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = if (isMe) Color.White else colors.accent
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform visualization canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = offset.x / size.width.toFloat()
                        voicePlayer.seekTo(ratio.coerceIn(0f, 1f))
                    }
                }
        ) {
            val amps = payload.amplitudes
            if (amps.isNotEmpty()) {
                val step = size.width / amps.size
                val midY = size.height / 2f
                amps.forEachIndexed { i, amp ->
                    val x = i * step
                    val barHeight = (amp / 100f) * size.height
                    val isPast = (i.toFloat() / amps.size) <= progress
                    val color = if (isPast) (if (isMe) Color.Yellow else colors.accent) else (if (isMe) Color.White.copy(alpha = 0.5f) else colors.text.copy(alpha = 0.3f))
                    
                    drawLine(
                        color = color,
                        start = Offset(x, midY - barHeight / 2f),
                        end = Offset(x, midY + barHeight / 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Playback Speed control chip
        Text(
            text = "${if (speed == 1.0f) "1" else if (speed == 1.5f) "1.5" else "2"}x",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMe) Color.White else colors.text,
            modifier = Modifier
                .background(if (isMe) Color.White.copy(alpha = 0.2f) else colors.border.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                .clickable {
                    val nextSpeed = when (speed) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                    viewModel.setVoicePlaybackSpeed(message.id, nextSpeed)
                }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ViewOnceBubble(
    message: Message,
    isMe: Boolean,
    onOpen: () -> Unit,
    colors: com.quickchat.core.model.theme.SketchyColors
) {
    val isOpened = message.plainText == "Opened" || message.plainText?.startsWith("view_once_opened:") == true
    val isVideo = message.messageType == MessageType.VIDEO

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(enabled = !isOpened) { onOpen() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (isOpened) Icons.Default.VolumeUp else Icons.Default.MoreHoriz,
            contentDescription = "View Once",
            tint = if (isMe) Color.White else colors.accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isOpened) "Opened" else (if (isVideo) "View Once Video" else "View Once Photo"),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMe) Color.White else colors.text
        )
        if (!isOpened) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(if (isMe) Color.White else colors.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMe) colors.accent else Color.White)
            }
        }
    }
}

@Composable
fun ViewOnceMediaViewerDialog(
    message: Message,
    viewModel: ChatRoomViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val payload = remember(message.plainText) {
        val raw = message.plainText ?: ""
        if (raw.startsWith("view_once:")) {
            val link = raw.substringAfter("view_once:")
            val parts = link.split("#")
            if (parts.size >= 2) {
                val url = parts[0]
                val keyIv = parts[1].split(",")
                if (keyIv.size >= 2) {
                    Triple(url, keyIv[0], keyIv[1])
                } else null
            } else null
        } else null
    }

    var decryptedFile by remember { mutableStateFlowOf<java.io.File?>(null) }
    var isLoading by remember { mutableStateFlowOf(true) }

    LaunchedEffect(payload) {
        if (payload != null) {
            val file = E2EeMediaCache.getDecryptedFile(context, payload.first, payload.second, payload.third, message.id)
            decryptedFile = file
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = {
            decryptedFile?.delete()
            viewModel.notifyViewOnceOpened(message.id)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val window = (LocalContext.current as? Activity)?.window
        DisposableEffect(Unit) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            onDispose {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(color = Color.White)
            } else if (decryptedFile != null) {
                val file = decryptedFile!!
                val isVideo = message.messageType == MessageType.VIDEO
                if (isVideo) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                setVideoPath(file.absolutePath)
                                setOnPreparedListener { start() }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f)
                    )
                } else {
                    AsyncImage(
                        model = file,
                        contentDescription = "View once media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Text("Failed to load view once media.", color = Color.White)
            }

            IconButton(
                onClick = {
                    decryptedFile?.delete()
                    viewModel.notifyViewOnceOpened(message.id)
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

// --- Delight Feature Helpers & Payloads ---

data class VoicePayload(
    val url: String,
    val key: String,
    val iv: String,
    val amplitudes: List<Float>
)

fun parseVoicePayload(text: String?): VoicePayload? {
    if (text == null || !text.startsWith("{")) return null
    return try {
        val obj = JSONObject(text)
        if (!obj.has("amplitudes")) return null
        val url = obj.getString("url")
        val key = obj.getString("key")
        val iv = obj.getString("iv")
        val arr = obj.getJSONArray("amplitudes")
        val amps = mutableListOf<Float>()
        for (i in 0 until arr.length()) {
            amps.add(arr.getDouble(i).toFloat())
        }
        VoicePayload(url, key, iv, amps)
    } catch (e: Exception) {
        null
    }
}

data class PreviewPayload(
    val text: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val url: String
)

fun parsePreviewPayload(text: String?): PreviewPayload? {
    if (text == null || !text.startsWith("{")) return null
    return try {
        val obj = JSONObject(text)
        if (!obj.has("linkPreview")) return null
        val bodyText = obj.getString("text")
        val preview = obj.getJSONObject("linkPreview")
        PreviewPayload(
            text = bodyText,
            title = preview.optString("title", ""),
            description = preview.optString("description", ""),
            imageUrl = preview.optString("imageUrl", ""),
            url = preview.optString("url", "")
        )
    } catch (e: Exception) {
        null
    }
}

object E2EeMediaCache {
    private val client = okhttp3.OkHttpClient()

    suspend fun getDecryptedFile(context: android.content.Context, url: String, keyBase64: String, ivBase64: String, msgId: String): java.io.File? = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val cacheFile = java.io.File(context.cacheDir, "decrypted_voice_$msgId.m4a")
        if (cacheFile.exists()) return@withContext cacheFile

        try {
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val encryptedBytes = response.body?.bytes() ?: return@withContext null

            val key = android.util.Base64.decode(keyBase64, android.util.Base64.NO_WRAP)
            val iv = android.util.Base64.decode(ivBase64, android.util.Base64.NO_WRAP)
            val decryptedBytes = com.quickchat.core.crypto.SignalKeys.decryptAES(encryptedBytes, key, iv)

            cacheFile.writeBytes(decryptedBytes)
            return@withContext cacheFile
        } catch (e: Exception) {
            android.util.Log.e("E2EeMediaCache", "Failed to download/decrypt voice message", e)
            null
        }
    }
}

class VoiceMessagePlayer(private val context: android.content.Context) {
    private var mediaPlayer: android.media.MediaPlayer? = null
    val currentPlayingId = mutableStateFlowOf<String?>(null)
    val currentProgress = mutableStateFlowOf(0f)
    val isPlaying = mutableStateFlowOf(false)
    val currentDuration = mutableStateFlowOf(0L)
    private var progressJob: kotlinx.coroutines.Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun play(msgId: String, file: java.io.File, speed: Float, onFinished: () -> Unit) {
        scope.launch {
            try {
                if (currentPlayingId.value == msgId) {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        isPlaying.value = false
                        progressJob?.cancel()
                    } else {
                        mediaPlayer?.start()
                        isPlaying.value = true
                        startProgressTracker(msgId, onFinished)
                    }
                    return@launch
                }

                stop()

                mediaPlayer = android.media.MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        playbackParams = playbackParams.setSpeed(speed)
                    }
                    setOnCompletionListener {
                        stop()
                        onFinished()
                    }
                }
                mediaPlayer?.start()
                isPlaying.value = true
                currentPlayingId.value = msgId
                currentDuration.value = mediaPlayer?.duration?.toLong() ?: 0L
                startProgressTracker(msgId, onFinished)

            } catch (e: Exception) {
                android.util.Log.e("VoicePlayer", "Failed to play voice note", e)
            }
        }
    }

    fun seekTo(progress: Float) {
        mediaPlayer?.let { player ->
            val position = (player.duration * progress).toInt()
            player.seekTo(position)
            currentProgress.value = progress
        }
    }

    fun stop() {
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingId.value = null
        currentProgress.value = 0f
        isPlaying.value = false
        currentDuration.value = 0L
    }

    fun isCurrentlyPlaying(msgId: String): Boolean = currentPlayingId.value == msgId && isPlaying.value
    fun getProgress(msgId: String): Float = if (currentPlayingId.value == msgId) currentProgress.value else 0f
    fun getCurrentPosition(msgId: String): Long = if (currentPlayingId.value == msgId) (currentProgress.value * currentDuration.value).toLong() else 0L

    private fun startProgressTracker(msgId: String, onFinished: () -> Unit) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (currentPlayingId.value == msgId && mediaPlayer?.isPlaying == true) {
                val player = mediaPlayer ?: break
                val pos = player.currentPosition.toFloat()
                val dur = player.duration.toFloat()
                if (dur > 0) {
                    currentProgress.value = pos / dur
                }
                kotlinx.coroutines.delay(50)
            }
        }
    }
}

class VoiceMessageRecorder(private val context: android.content.Context) {
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var recordFile: java.io.File? = null
    private var isRecording = false
    private var amplitudes = mutableListOf<Int>()
    private var recordStartTime = 0L

    fun start() {
        amplitudes.clear()
        recordFile = java.io.File(context.cacheDir, "recorded_voice_${System.currentTimeMillis()}.m4a")
        
        mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.media.MediaRecorder(context)
        } else {
            android.media.MediaRecorder()
        }.apply {
            setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
            setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordFile?.absolutePath)
            prepare()
            start()
        }
        isRecording = true
        recordStartTime = System.currentTimeMillis()
    }

    fun getDuration(): Long = if (isRecording) System.currentTimeMillis() - recordStartTime else 0L

    fun getAmplitude(): Int {
        if (!isRecording) return 0
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun stop(): Pair<java.io.File?, List<Int>> {
        if (!isRecording) return Pair(null, emptyList())
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        return Pair(recordFile, amplitudes)
    }

    fun cancel() {
        if (!isRecording) return
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        recordFile?.delete()
        recordFile = null
        amplitudes.clear()
    }
}
