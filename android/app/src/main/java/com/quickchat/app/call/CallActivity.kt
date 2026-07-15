package com.quickchat.app.call

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyTheme
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity : ComponentActivity() {

    @Inject
    lateinit var callManager: WebRtcCallManager

    private var isPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                com.quickchat.app.R.anim.fade_scale_in,
                com.quickchat.app.R.anim.fade_scale_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(
                com.quickchat.app.R.anim.fade_scale_in,
                com.quickchat.app.R.anim.fade_scale_out
            )
        }
        super.onCreate(savedInstanceState)

        // Show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val settingsViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.quickchat.feature.auth.SettingsViewModel>()
            val themePreference by settingsViewModel.theme.collectAsState("system")
            val isDark = when(themePreference) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            SketchyTheme(darkTheme = isDark) {
                val state by callManager.callState.collectAsState()
                
                // Automatically close activity when call returns to IDLE
                LaunchedEffect(state) {
                    if (state == CallState.IDLE) {
                        finish()
                    } else if (state == CallState.CONNECTING || state == CallState.CONNECTED) {
                        // Start CallService foreground service to hold connection alive
                        CallService.start(this@CallActivity)
                    }
                }

                CallScreenContent(
                    state = state,
                    isPip = isPipMode
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                com.quickchat.app.R.anim.fade_scale_in,
                com.quickchat.app.R.anim.fade_scale_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(
                com.quickchat.app.R.anim.fade_scale_in,
                com.quickchat.app.R.anim.fade_scale_out
            )
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Minimize to picture-in-picture if call is connected
        if (callManager.callState.value == CallState.CONNECTED) {
            val aspectRatio = if (callManager.isVideo.value) Rational(9, 16) else Rational(1, 1)
            val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
            } else null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && params != null) {
                enterPictureInPictureMode(params)
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @Composable
    fun CallScreenContent(state: CallState, isPip: Boolean) {
        val colors = LocalSketchyColors.current
        val partnerName by callManager.partnerName.collectAsState()
        val partnerPhone by callManager.partnerPhone.collectAsState()
        val isVideo by callManager.isVideo.collectAsState()
        val isMuted by callManager.isMuted.collectAsState()
        val isSpeakerOn by callManager.isSpeakerOn.collectAsState()
        val localVideoTrack by callManager.localVideoTrack.collectAsState()
        val remoteVideoTrack by callManager.remoteVideoTrack.collectAsState()
        val duration by callManager.callDuration.collectAsState()

        val formattedDuration = remember(duration) {
            val mins = duration / 60
            val secs = duration % 60
            String.format("%d:%02d", mins, secs)
        }

        if (isPip) {
            // Minimal render for PIP mode
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (isVideo && remoteVideoTrack != null) {
                    VideoRenderer(videoTrack = remoteVideoTrack, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        UserAvatar(avatarUrl = null, displayName = partnerName, size = 64.dp)
                    }
                }
            }
            return
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            if (isVideo && state == CallState.CONNECTED) {
                // Video Call Renderer Area
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (remoteVideoTrack != null) {
                        VideoRenderer(videoTrack = remoteVideoTrack, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.accent)
                        }
                    }

                    // Floating Local Video Preview
                    if (localVideoTrack != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .width(90.dp)
                                .height(160.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(colors.surface)
                                .sketchyBorder(1.5.dp, colors.border, 12.dp)
                        ) {
                            VideoRenderer(videoTrack = localVideoTrack, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // Standard UI elements on top of the Video (or full screen for Voice Calls/Ringing states)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header (Partner details & Security info)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Encryption Badge
                    Row(
                        modifier = Modifier
                            .background(colors.surface, MaterialTheme.shapes.small)
                            .sketchyBorder(1.dp, colors.border, 4.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔒 E2E Encrypted",
                            color = colors.text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isVideo || state != CallState.CONNECTED) {
                        UserAvatar(avatarUrl = null, displayName = partnerName, size = 100.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = partnerName,
                        color = if (isVideo && state == CallState.CONNECTED) Color.White else colors.text,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when (state) {
                            CallState.OUTGOING_RINGING -> "ringing..."
                            CallState.INCOMING_RINGING -> "incoming call..."
                            CallState.CONNECTING -> "connecting..."
                            CallState.CONNECTED -> formattedDuration
                            CallState.ENDED -> "call ended"
                            else -> ""
                        },
                        color = if (isVideo && state == CallState.CONNECTED) Color.White.copy(alpha = 0.8f) else colors.text.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Controls area
                if (state == CallState.INCOMING_RINGING) {
                    // Answer/Decline Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Button (Terracotta/Red theme style)
                        IconButtonWithLabel(
                            icon = Icons.Default.CallEnd,
                            label = "Decline",
                            containerColor = colors.accent,
                            iconColor = Color.White
                        ) {
                            callManager.rejectCall()
                        }

                        // Answer Button (Green)
                        IconButtonWithLabel(
                            icon = Icons.Default.Call,
                            label = "Accept",
                            containerColor = Color(0xFF4CAF50),
                            iconColor = Color.White
                        ) {
                            callManager.acceptCall()
                        }
                    }
                } else {
                    // Active call / Outgoing ringing Control Buttons
                    Row(
                        modifier = Modifier
                            .background(
                                if (isVideo && state == CallState.CONNECTED) Color.Black.copy(alpha = 0.5f) else colors.surface,
                                MaterialTheme.shapes.medium
                            )
                            .sketchyBorder(
                                1.5.dp,
                                if (isVideo && state == CallState.CONNECTED) Color.White.copy(alpha = 0.5f) else colors.border,
                                16.dp
                            )
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute button
                        IconButton(onClick = { callManager.toggleMute() }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isVideo && state == CallState.CONNECTED) Color.White else colors.text
                            )
                        }

                        // Speaker button
                        IconButton(onClick = { callManager.toggleSpeaker() }) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Speaker",
                                tint = if (isVideo && state == CallState.CONNECTED) Color.White else colors.text
                            )
                        }

                        // Camera Toggle (only for video calls)
                        if (isVideo) {
                            IconButton(onClick = { callManager.switchCamera() }) {
                                Icon(
                                    imageVector = Icons.Default.SwitchCamera,
                                    contentDescription = "Switch Camera",
                                    tint = if (isVideo && state == CallState.CONNECTED) Color.White else colors.text
                                )
                            }
                        }

                        // End Call
                        IconButton(
                            onClick = { callManager.endCall() },
                            modifier = Modifier
                                .background(colors.accent, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun IconButtonWithLabel(
        icon: ImageVector,
        label: String,
        containerColor: Color,
        iconColor: Color,
        onClick: () -> Unit
    ) {
        val colors = LocalSketchyColors.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(containerColor, CircleShape)
                    .sketchyBorder(2.dp, colors.border, 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = colors.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    fun VideoRenderer(
        videoTrack: VideoTrack?,
        modifier: Modifier = Modifier
    ) {
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    init(callManager.rootEglBase.eglBaseContext, null)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    setEnableHardwareScaler(true)
                }
            },
            update = { view ->
                videoTrack?.addSink(view)
            },
            onRelease = { view ->
                view.release()
            },
            modifier = modifier
        )
    }
}
