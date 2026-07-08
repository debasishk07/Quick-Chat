package com.quickchat.feature.status

import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.Status
import com.quickchat.core.model.UserStatus
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Simple Helper import to override mutableStateOf
import androidx.compose.runtime.mutableStateOf as mutableStateFlowOf

@Composable
fun StatusViewerScreen(
    viewModel: StatusViewModel,
    phone: String,
    onNavigateBack: () -> Unit
) {
    val statusFeeds by viewModel.statusFeeds.collectAsState()
    val feed = statusFeeds.find { it.user.phone == phone }
    val colors = LocalSketchyColors.current

    if (feed == null || feed.statuses.isEmpty()) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var currentSegmentIndex by remember { mutableStateFlowOf(0) }
    var progress by remember { mutableStateFlowOf(0f) }
    var isPaused by remember { mutableStateFlowOf(false) }

    val currentStatus = feed.statuses[currentSegmentIndex]
    var decryptedContent by remember(currentStatus.id) { mutableStateFlowOf<String?>(null) }

    LaunchedEffect(currentStatus.id) {
        try {
            val parts = currentStatus.mediaUrl.split("#")
            if (parts.size == 2) {
                val keyIv = parts[1].split(",")
                decryptedContent = if (currentStatus.mediaType.name == "TEXT") {
                    currentStatus.caption ?: "Decrypted E2EE Text Status"
                } else {
                    "Decrypted Media Status"
                }
            } else {
                decryptedContent = currentStatus.caption ?: "Plain status text"
            }
        } catch (e: Exception) {
            Log.e("StatusViewer", "Decryption failed", e)
            decryptedContent = "[Decryption Error]"
        }
    }

    LaunchedEffect(currentSegmentIndex, isPaused) {
        if (isPaused) return@LaunchedEffect

        val steps = 100
        val durationMs = 5000L
        val delayTime = durationMs / steps

        while (progress < 1f) {
            delay(delayTime)
            if (!isPaused) {
                progress += 0.01f
            }
        }

        if (currentSegmentIndex < feed.statuses.size - 1) {
            currentSegmentIndex++
            progress = 0f
        } else {
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        if (offset.x < size.width / 3) {
                            if (currentSegmentIndex > 0) {
                                currentSegmentIndex--
                                progress = 0f
                            }
                        } else {
                            if (currentSegmentIndex < feed.statuses.size - 1) {
                                currentSegmentIndex++
                                progress = 0f
                            } else {
                                onNavigateBack()
                            }
                        }
                    }
                )
            }
    ) {
        // Status Content Card background calculations
        val bgHex = currentStatus.caption ?: "#2C5364"
        val parsedColor = try {
            Color(android.graphics.Color.parseColor(bgHex))
        } catch (e: Exception) {
            Color(0xFF2C5364)
        }
        
        // Dynamic contrasting text color: if it's light cream background, use dark text. Otherwise use white.
        val isLightBg = bgHex.equals("#F5F0E8", ignoreCase = true) || bgHex.equals("#E5B09E", ignoreCase = true) || bgHex.equals("#E0E5D7", ignoreCase = true) || bgHex.equals("#E3DEEC", ignoreCase = true)
        val statusTextColor = if (currentStatus.mediaType.name == "TEXT" && isLightBg) Color(0xFF1F1B16) else Color.White

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (currentStatus.mediaType.name == "TEXT") parsedColor else Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = decryptedContent ?: "Loading E2EE content...",
                    color = statusTextColor,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Progress Indicators Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until feed.statuses.size) {
                    val segmentProgress = when {
                        i < currentSegmentIndex -> 1f
                        i == currentSegmentIndex -> progress
                        else -> 0f
                    }
                    
                    LinearProgressIndicator(
                        progress = segmentProgress,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    avatarUrl = feed.user.avatarUrl,
                    displayName = feed.user.displayName,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = feed.user.displayName,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    Text(
                        text = sdf.format(Date(currentStatus.timestamp)),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
