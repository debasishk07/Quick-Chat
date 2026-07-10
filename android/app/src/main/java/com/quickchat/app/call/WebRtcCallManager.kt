package com.quickchat.app.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.content.Intent
import com.quickchat.core.model.MessageType
import com.quickchat.core.network.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class CallState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTING,
    CONNECTED,
    ENDED
}

@Singleton
class WebRtcCallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _isVideo = MutableStateFlow(false)
    val isVideo: StateFlow<Boolean> = _isVideo.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private val _callDuration = MutableStateFlow(0L)
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()

    private val _partnerPhone = MutableStateFlow("")
    val partnerPhone: StateFlow<String> = _partnerPhone.asStateFlow()

    private val _partnerName = MutableStateFlow("")
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    private val _partnerAvatar = MutableStateFlow<String?>(null)
    val partnerAvatar: StateFlow<String?> = _partnerAvatar.asStateFlow()

    // WebRTC Core components
    val rootEglBase: EglBase = EglBase.create()
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoCapturer: VideoCapturer? = null
    
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrackObj: VideoTrack? = null
    
    // Audio / Ringing components
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Call state variables
    private var callId: String = ""
    private var timerJob: Job? = null
    private var isCaller = false
    private var pendingIceCandidates = mutableListOf<IceCandidate>()

    init {
        initWebRTC()
        listenForIncomingCallSignals()
    }

    private fun initWebRTC() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext, true, true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()
    }

    private fun listenForIncomingCallSignals() {
        scope.launch {
            chatRepository.incomingCallSignals.collect { (sender, payloadJson) ->
                try {
                    val obj = JSONObject(payloadJson)
                    val signalCallId = obj.optString("callId")
                    val type = obj.optString("type")

                    when (type) {
                        "offer" -> {
                            if (_callState.value == CallState.IDLE) {
                                callId = signalCallId
                                _partnerPhone.value = sender
                                val chat = chatRepository.getChat(sender)
                                _partnerName.value = chat?.displayName ?: "Contact $sender"
                                _partnerAvatar.value = chat?.avatarUrl
                                _isVideo.value = obj.optBoolean("isVideo", false)
                                isCaller = false
                                _callState.value = CallState.INCOMING_RINGING
                                startRinging()
                                
                                // Auto-launch CallActivity for incoming call
                                val intent = Intent(context, CallActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)

                                // Store the remote SDP offer to set later upon acceptance
                                val sdp = obj.optString("sdp")
                                pendingRemoteSdp = sdp
                            } else {
                                // Busy, decline automatically
                                sendCallSignalMessage(sender, signalCallId, "busy")
                            }
                        }
                        "answer" -> {
                            if (signalCallId == callId && _callState.value == CallState.CONNECTING) {
                                val sdp = obj.optString("sdp")
                                setRemoteDescription(sdp, SessionDescription.Type.ANSWER)
                            }
                        }
                        "ice-candidate" -> {
                            if (signalCallId == callId) {
                                val candidate = IceCandidate(
                                    obj.optString("sdpMid"),
                                    obj.optInt("sdpMLineIndex"),
                                    obj.optString("candidate")
                                )
                                if (peerConnection != null && peerConnection?.remoteDescription != null) {
                                    peerConnection?.addIceCandidate(candidate)
                                } else {
                                    pendingIceCandidates.add(candidate)
                                }
                            }
                        }
                        "decline" -> {
                            if (signalCallId == callId) {
                                cleanUpCallState("Declined")
                            }
                        }
                        "busy" -> {
                            if (signalCallId == callId) {
                                cleanUpCallState("Busy")
                            }
                        }
                        "end" -> {
                            if (signalCallId == callId) {
                                cleanUpCallState("Ended")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebRtcCallManager", "Error parsing call signal", e)
                }
            }
        }
    }

    private var pendingRemoteSdp: String? = null

    fun initiateCall(recipientPhone: String, isVideo: Boolean) {
        if (_callState.value != CallState.IDLE) return
        
        callId = UUID.randomUUID().toString()
        _partnerPhone.value = recipientPhone
        _isVideo.value = isVideo
        isCaller = true
        _callState.value = CallState.OUTGOING_RINGING

        // Auto-launch CallActivity for outgoing call
        val intent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
        
        scope.launch {
            val chat = chatRepository.getChat(recipientPhone)
            _partnerName.value = chat?.displayName ?: "Contact $recipientPhone"
            _partnerAvatar.value = chat?.avatarUrl
        }

        startOutgoingRingBack()
        setupPeerConnection()

        // Create Offer
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        // Send Offer signal E2E encrypted
                        val signal = JSONObject().apply {
                            put("callId", callId)
                            put("type", "offer")
                            put("isVideo", isVideo)
                            put("sdp", desc.description)
                        }
                        scope.launch {
                            chatRepository.sendCallSignal(recipientPhone, signal.toString())
                        }
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e("WebRtcCallManager", "Failed to create SDP offer: $error")
                endCall()
            }
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    fun acceptCall() {
        if (_callState.value != CallState.INCOMING_RINGING) return
        stopRinging()
        _callState.value = CallState.CONNECTING

        setupPeerConnection()

        // Set Remote Offer
        pendingRemoteSdp?.let { sdp ->
            setRemoteDescription(sdp, SessionDescription.Type.OFFER) {
                // Create Answer
                peerConnection?.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription) {
                        peerConnection?.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() {
                                // Send Answer signal E2E encrypted
                                val signal = JSONObject().apply {
                                    put("callId", callId)
                                    put("type", "answer")
                                    put("sdp", desc.description)
                                }
                                scope.launch {
                                    chatRepository.sendCallSignal(_partnerPhone.value, signal.toString())
                                }
                            }
                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        }, desc)
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, MediaConstraints())
            }
        }
    }

    fun rejectCall() {
        if (_callState.value == CallState.INCOMING_RINGING) {
            sendCallSignalMessage(_partnerPhone.value, callId, "decline")
            cleanUpCallState("Rejected")
        }
    }

    fun endCall() {
        if (_callState.value != CallState.IDLE) {
            sendCallSignalMessage(_partnerPhone.value, callId, "end")
            cleanUpCallState("Ended")
        }
    }

    private fun sendCallSignalMessage(recipient: String, targetCallId: String, type: String) {
        val signal = JSONObject().apply {
            put("callId", targetCallId)
            put("type", type)
        }
        scope.launch {
            chatRepository.sendCallSignal(recipient, signal.toString())
        }
    }

    private fun setupPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            // Placeholder: Add your TURN credentials here
            // PeerConnection.IceServer.builder("turn:yourturn.server:3478").setUsername("user").setPassword("pass").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                Log.d("WebRtcCallManager", "Ice Connection State Change: $newState")
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    mainHandler.post {
                        stopRinging()
                        if (_callState.value != CallState.CONNECTED) {
                            _callState.value = CallState.CONNECTED
                            startCallTimer()
                        }
                    }
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED || newState == PeerConnection.IceConnectionState.FAILED) {
                    mainHandler.post {
                        cleanUpCallState("Disconnected")
                    }
                }
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            
            override fun onIceCandidate(candidate: IceCandidate) {
                // Send candidate
                val signal = JSONObject().apply {
                    put("callId", callId)
                    put("type", "ice-candidate")
                    put("sdpMid", candidate.sdpMid)
                    put("sdpMLineIndex", candidate.sdpMLineIndex)
                    put("candidate", candidate.sdp)
                }
                scope.launch {
                    chatRepository.sendCallSignal(_partnerPhone.value, signal.toString())
                }
            }
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            
            override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<out MediaStream>?) {
                val track = receiver.track()
                if (track is VideoTrack) {
                    Log.d("WebRtcCallManager", "Remote Video Track Added")
                    _remoteVideoTrack.value = track
                }
            }
            override fun onRemoveTrack(p0: RtpReceiver?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })

        // Request Audio Focus
        requestAudioFocus()

        // Create Local Audio Track
        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("echoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("noiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("autoGainControl", "true"))
        }
        val audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("ARDAMSa0", audioSource)
        peerConnection?.addTrack(localAudioTrack)

        // Create Local Video Track if video call
        if (_isVideo.value) {
            setupVideoCapturer()
        }
    }

    private fun setupVideoCapturer() {
        val enumerator = if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }

        val deviceNames = enumerator.deviceNames
        var targetDevice: String? = null
        // Pick front camera by default
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                targetDevice = deviceName
                break
            }
        }
        if (targetDevice == null && deviceNames.isNotEmpty()) {
            targetDevice = deviceNames[0]
        }

        if (targetDevice != null) {
            videoCapturer = enumerator.createCapturer(targetDevice, null)
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
            val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer!!.isScreencast)
            
            videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
            videoCapturer?.startCapture(1280, 720, 30)

            localVideoTrackObj = peerConnectionFactory?.createVideoTrack("ARDAMSv0", videoSource)
            _localVideoTrack.value = localVideoTrackObj
            peerConnection?.addTrack(localVideoTrackObj)
        }
    }

    private fun setRemoteDescription(sdp: String, type: SessionDescription.Type, onSetComplete: (() -> Unit)? = null) {
        val sdpObj = SessionDescription(type, sdp)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                mainHandler.post {
                    onSetComplete?.invoke()
                    // Process ICE candidates gathered before setting description
                    for (cand in pendingIceCandidates) {
                        peerConnection?.addIceCandidate(cand)
                    }
                    pendingIceCandidates.clear()
                }
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e("WebRtcCallManager", "Failed to set remote description: $error")
            }
        }, sdpObj)
    }

    // Call state helper to log calls
    private fun writeCallLog(logDetails: String) {
        val partner = _partnerPhone.value
        if (partner.isBlank()) return
        scope.launch {
            chatRepository.sendMessage(partner, logDetails, MessageType.CALL_LOG)
        }
    }

    private fun cleanUpCallState(endReason: String) {
        if (_callState.value == CallState.IDLE) return
        
        val durationStr = if (_callDuration.value > 0) {
            val mins = _callDuration.value / 60
            val secs = _callDuration.value % 60
            String.format("%d:%02d", mins, secs)
        } else {
            ""
        }

        // Write Call Log
        val callTypePrefix = if (_isVideo.value) "Video Call" else "Voice Call"
        val isIncoming = !isCaller
        val logDetails = when {
            endReason == "Declined" && isIncoming -> "Declined incoming $callTypePrefix"
            endReason == "Declined" && !isIncoming -> "Declined outgoing $callTypePrefix"
            endReason == "Rejected" -> "Rejected incoming $callTypePrefix"
            endReason == "Busy" -> "$callTypePrefix - Line Busy"
            _callDuration.value > 0 -> "$callTypePrefix ended ($durationStr)"
            isIncoming -> "Missed $callTypePrefix"
            else -> "Unanswered $callTypePrefix"
        }
        writeCallLog(logDetails)

        _callState.value = CallState.ENDED
        stopCallTimer()
        stopRinging()
        abandonAudioFocus()

        try {
            videoCapturer?.stopCapture()
        } catch (e: Exception) {
            // Ignore
        }
        videoCapturer?.dispose()
        videoCapturer = null

        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null

        peerConnection?.dispose()
        peerConnection = null

        _localVideoTrack.value = null
        _remoteVideoTrack.value = null
        localVideoTrackObj = null
        localAudioTrack = null
        pendingRemoteSdp = null
        pendingIceCandidates.clear()

        _isMuted.value = false
        _isSpeakerOn.value = false
        
        mainHandler.postDelayed({
            if (_callState.value == CallState.ENDED) {
                _callState.value = CallState.IDLE
                _callDuration.value = 0L
                _partnerPhone.value = ""
                _partnerName.value = ""
            }
        }, 1500)
    }

    // Controls
    fun toggleMute() {
        val newMute = !_isMuted.value
        localAudioTrack?.setEnabled(!newMute)
        _isMuted.value = newMute
    }

    fun toggleSpeaker() {
        val newSpeaker = !_isSpeakerOn.value
        audioManager.isSpeakerphoneOn = newSpeaker
        _isSpeakerOn.value = newSpeaker
    }

    fun switchCamera() {
        val capturer = videoCapturer as? CameraVideoCapturer ?: return
        capturer.switchCamera(null)
    }

    // Audio Focus and routing management
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { },
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = _isSpeakerOn.value
    }

    private fun abandonAudioFocus() {
        audioManager.mode = AudioManager.MODE_NORMAL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus { }
        }
    }

    // Ringing management
    private fun startRinging() {
        // Vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)

        // Ringtone player
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("WebRtcCallManager", "Failed to start ringtone", e)
        }
    }

    private fun startOutgoingRingBack() {
        // Simple mock of ring-back tone (playing a beep loop or simple system ringback)
        try {
            mediaPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("WebRtcCallManager", "Failed to start ringback", e)
        }
    }

    private fun stopRinging() {
        vibrator?.cancel()
        vibrator = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
    }

    // Timer management
    private fun startCallTimer() {
        timerJob?.cancel()
        _callDuration.value = 0L
        timerJob = scope.launch {
            while (_callState.value == CallState.CONNECTED) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    private fun stopCallTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
