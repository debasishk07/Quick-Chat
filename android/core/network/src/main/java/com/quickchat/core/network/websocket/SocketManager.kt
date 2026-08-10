package com.quickchat.core.network.websocket

import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {
    private var socket: Socket? = null
    private val gson = Gson()

    private val _incomingMessages = MutableSharedFlow<SocketMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<SocketMessage> = _incomingMessages.asSharedFlow()

    private val _messageReceipts = MutableSharedFlow<SocketReceipt>(extraBufferCapacity = 64)
    val messageReceipts: SharedFlow<SocketReceipt> = _messageReceipts.asSharedFlow()

    private val _presenceChanges = MutableSharedFlow<SocketPresence>(extraBufferCapacity = 64)
    val presenceChanges: SharedFlow<SocketPresence> = _presenceChanges.asSharedFlow()

    private val _typingNotifications = MutableSharedFlow<SocketTyping>(extraBufferCapacity = 64)
    val typingNotifications: SharedFlow<SocketTyping> = _typingNotifications.asSharedFlow()

    private val _messageDeletedEvents = MutableSharedFlow<SocketDeleteMessage>(extraBufferCapacity = 64)
    val messageDeletedEvents: SharedFlow<SocketDeleteMessage> = _messageDeletedEvents.asSharedFlow()

    fun connect(baseUrl: String, phone: String) {
        if (socket?.connected() == true) return

        try {
            val opts = IO.Options().apply {
                query = "phone=$phone"
            }
            socket = IO.socket(baseUrl, opts)
            
            setupEventListeners()
            socket?.connect()
            Log.d("SocketManager", "Connecting socket for user $phone...")
        } catch (e: Exception) {
            Log.e("SocketManager", "Failed to connect socket", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d("SocketManager", "Socket disconnected.")
    }

    private fun setupEventListeners() {
        val s = socket ?: return

        s.on("receive-message") { args ->
            val json = args[0] as JSONObject
            val msg = gson.fromJson(json.toString(), SocketMessage::class.java)
            _incomingMessages.tryEmit(msg)
        }

        s.on("offline-messages") { args ->
            val jsonArray = args[0] as org.json.JSONArray
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val msg = gson.fromJson(json.toString(), SocketMessage::class.java)
                _incomingMessages.tryEmit(msg)
            }
        }

        s.on("message-receipt") { args ->
            val json = args[0] as JSONObject
            val receipt = gson.fromJson(json.toString(), SocketReceipt::class.java)
            _messageReceipts.tryEmit(receipt)
        }

        s.on("presence-change") { args ->
            val json = args[0] as JSONObject
            val presence = gson.fromJson(json.toString(), SocketPresence::class.java)
            _presenceChanges.tryEmit(presence)
        }

        s.on("typing") { args ->
            val json = args[0] as JSONObject
            val typing = gson.fromJson(json.toString(), SocketTyping::class.java)
            _typingNotifications.tryEmit(typing)
        }

        s.on("message-deleted") { args ->
            val json = args[0] as JSONObject
            val del = gson.fromJson(json.toString(), SocketDeleteMessage::class.java)
            _messageDeletedEvents.tryEmit(del)
        }
    }

    fun sendMessage(msg: SocketMessage) {
        val jsonStr = gson.toJson(msg)
        socket?.emit("send-message", JSONObject(jsonStr))
    }

    fun sendReceipt(messageId: String, recipientPhone: String, status: String) {
        val json = JSONObject().apply {
            put("messageId", messageId)
            put("recipient", recipientPhone)
            put("status", status)
            put("timestamp", System.currentTimeMillis())
        }
        socket?.emit("message-receipt", json)
    }

    fun sendBatchReceipts(messageIds: List<String>, recipientPhone: String, status: String) {
        if (messageIds.isEmpty()) return
        val jsonArray = org.json.JSONArray(messageIds)
        val json = JSONObject().apply {
            put("messageIds", jsonArray)
            put("recipient", recipientPhone)
            put("status", status)
            put("timestamp", System.currentTimeMillis())
        }
        socket?.emit("message-receipt", json)
    }

    fun sendTyping(recipientPhone: String, isTyping: Boolean) {
        val json = JSONObject().apply {
            put("recipient", recipientPhone)
            put("isTyping", isTyping)
        }
        socket?.emit("typing", json)
    }

    fun sendDeleteMessage(messageId: String, recipientPhone: String, mode: String, publicId: String? = null) {
        val json = JSONObject().apply {
            put("messageId", messageId)
            put("recipient", recipientPhone)
            put("mode", mode)
            if (publicId != null) put("publicId", publicId)
        }
        socket?.emit("delete-message", json)
    }
}

// Socket transfer objects
data class SocketMessage(
    val id: String,
    val sender: String,
    val recipient: String,
    val isGroup: Int,
    val ciphertext: String,
    val iv: String,
    val ephemeralPublicKey: String?,
    val messageType: String,
    val timestamp: Long,
    val status: String,
    val isDeleted: Int? = 0
)

data class SocketReceipt(
    val messageId: String? = null,
    val messageIds: List<String>? = null,
    val recipient: String,
    val status: String,
    val timestamp: Long
)

data class SocketPresence(
    val phone: String,
    val isOnline: Boolean,
    val lastSeen: Long
)

data class SocketTyping(
    val sender: String,
    val isTyping: Boolean
)

data class SocketDeleteMessage(
    val messageId: String,
    val deletedBy: String,
    val mode: String
)
