package com.quickchat.app.scheduling

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.quickchat.core.database.dao.MessageDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var messageDao: MessageDao

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra("message_id") ?: return
        Log.d("ReminderReceiver", "Alarm triggered for message reminder ID: $messageId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entity = messageDao.getMessage(messageId) ?: return@launch
                val senderPhone = entity.senderPhone
                val plainText = entity.plainText ?: "Attachment"

                val channelId = "chat_reminders_channel"
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Message Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    manager.createNotificationChannel(channel)
                }

                val openIntent = Intent().apply {
                    setClassName(context.packageName, "com.quickchat.app.MainActivity")
                    putExtra("navigate_to_chat", senderPhone)
                    putExtra("highlight_message_id", messageId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    messageId.hashCode(),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle("Quick Chat Reminder")
                    .setContentText("You asked to reply to: $plainText")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                manager.notify(messageId.hashCode(), builder.build())
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Failed to show reminder notification", e)
            }
        }
    }
}
