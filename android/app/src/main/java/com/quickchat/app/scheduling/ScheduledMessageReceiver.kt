package com.quickchat.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.quickchat.core.database.dao.ScheduledMessageDao
import com.quickchat.core.model.MessageType
import com.quickchat.core.network.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledMessageReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var scheduledMessageDao: ScheduledMessageDao

    override fun onReceive(context: Context, intent: Intent) {
        val scheduledId = intent.getLongExtra("scheduled_id", -1L)
        if (scheduledId == -1L) return

        Log.d("ScheduledMsgReceiver", "Alarm triggered for scheduled message ID: $scheduledId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val list = scheduledMessageDao.getAllScheduledMessages()
                val msg = list.firstOrNull { it.id == scheduledId }
                if (msg != null) {
                    chatRepository.sendMessage(
                        recipientPhone = msg.recipientPhone,
                        messageText = msg.plainText,
                        type = MessageType.valueOf(msg.messageType)
                    )
                    scheduledMessageDao.deleteScheduled(scheduledId)
                }
            } catch (e: Exception) {
                Log.e("ScheduledMsgReceiver", "Failed to process scheduled message", e)
            }
        }
    }
}
