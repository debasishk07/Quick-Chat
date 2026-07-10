package com.quickchat.app.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.quickchat.app.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var callManager: WebRtcCallManager

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var callStateJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "call_service_channel"
        private const val NOTIFICATION_ID = 2004

        fun start(context: Context) {
            val intent = Intent(context, CallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundWithNotification()

        // Observe call state to stop service when call ends
        callStateJob = serviceScope.launch {
            callManager.callState.collectLatest { state ->
                if (state == CallState.IDLE) {
                    stopSelf()
                } else {
                    updateNotification(state)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        callStateJob?.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Call",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows notifications for active E2EE calls"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("Connecting E2EE Call...", callManager.partnerName.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(state: CallState) {
        val partnerName = callManager.partnerName.value
        val isVideo = callManager.isVideo.value
        val mediaType = if (isVideo) "Video Call" else "Voice Call"
        
        val statusText = when (state) {
            CallState.OUTGOING_RINGING -> "Ringing $partnerName..."
            CallState.INCOMING_RINGING -> "Incoming call from $partnerName"
            CallState.CONNECTING -> "Connecting E2EE $mediaType with $partnerName..."
            CallState.CONNECTED -> "Active E2EE $mediaType with $partnerName"
            else -> "Ending Call..."
        }

        val notification = buildNotification(statusText, partnerName)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(text: String, partnerName: String): Notification {
        val intent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // End Call action button
        val endCallIntent = Intent(this, CallReceiver::class.java).apply {
            action = "ACTION_END_CALL"
        }
        val endCallPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val initialsBitmap = getAvatarBitmap(this, partnerName)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Quick Chat")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setLargeIcon(initialsBitmap)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Call",
                endCallPendingIntent
            )
            .build()
    }

    private fun getAvatarBitmap(context: Context, displayName: String): android.graphics.Bitmap {
        val size = 128
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        val colors = listOf(
            0xFFE57373.toInt(), 0xFFF06292.toInt(), 0xFFBA68C8.toInt(), 0xFF9575CD.toInt(),
            0xFF7986CB.toInt(), 0xFF64B5F6.toInt(), 0xFF4FC3F7.toInt(), 0xFF4DB6AC.toInt(),
            0xFF81C784.toInt(), 0xFFD4E157.toInt(), 0xFFFFD54F.toInt(), 0xFFFFB74D.toInt(),
            0xFFFF8A65.toInt(), 0xFFA1887F.toInt()
        )
        val nameToHash = if (displayName.isBlank()) "Guest" else displayName
        val colorIndex = Math.abs(nameToHash.hashCode()) % colors.size
        val bgColor = colors[colorIndex]
        
        val paint = android.graphics.Paint().apply {
            color = bgColor
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            textSize = 54f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val initial = nameToHash.trim().firstOrNull()?.uppercaseChar() ?: '?'
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(initial.toString(), xPos, yPos, textPaint)
        
        return bitmap
    }
}
