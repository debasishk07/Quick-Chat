package com.quickchat.app.scheduling

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.quickchat.core.model.MessageType
import com.quickchat.core.network.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatRepository: ChatRepository

    override fun onReceive(context: Context, intent: Intent) {
        val senderPhone = intent.getStringExtra("sender_phone") ?: return
        val notificationId = intent.getIntExtra("notification_id", -1)
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = results.getCharSequence("key_text_reply")?.toString() ?: return

        Log.d("NotificationReplyRcvr", "Direct reply received for $senderPhone: $replyText")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                chatRepository.sendMessage(senderPhone, replyText, MessageType.TEXT)

                val channelId = "chat_messages_channel"
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val repliedNotification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle("Quick Chat")
                    .setContentText("Reply sent")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .build()

                manager.notify(notificationId, repliedNotification)
                
                kotlinx.coroutines.delay(1000)
                manager.cancel(notificationId)
            } catch (e: Exception) {
                Log.e("NotificationReplyRcvr", "Failed to send inline reply", e)
            }
        }
    }
}
