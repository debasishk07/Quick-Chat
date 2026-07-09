package com.quickchat.core.network.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.quickchat.core.crypto.*
import com.quickchat.core.database.dao.ChatDao
import com.quickchat.core.database.dao.MessageDao
import com.quickchat.core.database.dao.OutboxDao
import com.quickchat.core.database.dao.SessionDao
import com.quickchat.core.database.entities.ChatEntity
import com.quickchat.core.database.entities.MessageEntity
import com.quickchat.core.database.entities.OutboxEntity
import com.quickchat.core.database.entities.SessionEntity
import com.quickchat.core.model.Chat
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageStatus
import com.quickchat.core.model.MessageType
import com.quickchat.core.network.api.QuickChatApi
import com.quickchat.core.network.websocket.SocketManager
import com.quickchat.core.network.websocket.SocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.KeyPair
import java.security.PublicKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    fun getChatsFlow(): Flow<List<Chat>>
    fun getMessagesFlow(recipientPhone: String): Flow<List<Message>>
    suspend fun sendMessage(recipientPhone: String, messageText: String, type: MessageType): String
    suspend fun sendTyping(recipientPhone: String, isTyping: Boolean)
    suspend fun searchMessages(query: String): List<Message>
    fun initSocketConnection(phone: String)
    fun closeSocketConnection()
    val activeTypingState: StateFlow<Map<String, Boolean>> // phone -> isTyping
    suspend fun getChat(phone: String): Chat?
    suspend fun clearUnreadCount(phone: String)
    val incomingCallSignals: SharedFlow<Pair<String, String>>
    suspend fun sendCallSignal(recipientPhone: String, signalJson: String): Boolean
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: QuickChatApi,
    private val socketManager: SocketManager,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val sessionDao: SessionDao,
    private val outboxDao: OutboxDao,
    private val userRepository: UserRepository
) : ChatRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    
    private val _activeTypingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val activeTypingState: StateFlow<Map<String, Boolean>> = _activeTypingState

    private val _incomingCallSignals = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    override val incomingCallSignals: SharedFlow<Pair<String, String>> = _incomingCallSignals.asSharedFlow()

    init {
        observeSocketEvents()
    }

    override fun initSocketConnection(phone: String) {
        // Points to host system loopback (standard Android emulator mapping for localhost)
        socketManager.connect("http://10.0.2.2:3000", phone)
        repositoryScope.launch {
            syncOfflineOutbox()
        }
    }

    override fun closeSocketConnection() {
        socketManager.disconnect()
    }

    override fun getChatsFlow(): Flow<List<Chat>> {
        return chatDao.getAllChatsFlow().map { entities ->
            entities.map { entity ->
                val lastMsg = entity.lastMessageId?.let { id ->
                    messageDao.getMessage(id)?.toDomain()
                }
                entity.toDomain(lastMsg)
            }
        }
    }

    override fun getMessagesFlow(recipientPhone: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(recipientPhone).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChat(phone: String): Chat? {
        val entity = chatDao.getChat(phone) ?: return null
        val lastMsg = entity.lastMessageId?.let { id ->
            messageDao.getMessage(id)?.toDomain()
        }
        return entity.toDomain(lastMsg)
    }

    override suspend fun clearUnreadCount(phone: String) {
        chatDao.clearUnreadCount(phone)
    }

    override suspend fun sendMessage(recipientPhone: String, messageText: String, type: MessageType): String {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return ""

        // 1. Create a sending message entity in SENDING state locally
        val pendingMsg = Message(
            id = messageId,
            senderPhone = currentUserPhone,
            recipientPhone = recipientPhone,
            isGroup = false,
            ciphertext = "",
            iv = "",
            messageType = type,
            timestamp = timestamp,
            status = MessageStatus.SENDING,
            plainText = messageText
        )
        messageDao.insertMessage(MessageEntity.fromDomain(pendingMsg))
        updateChatLastMessage(recipientPhone, pendingMsg)

        // 2. Perform E2E Encryption and relay
        repositoryScope.launch {
            try {
                // Get or establish E2E session
                val session = getOrCreateSession(recipientPhone)
                
                // Encrypt payload
                val encrypted = DoubleRatchetEngine.encrypt(session, messageText.toByteArray(Charsets.UTF_8))
                
                // Save updated session state
                saveSession(recipientPhone, session)

                // Dispatch via socket
                val socketMsg = SocketMessage(
                    id = messageId,
                    sender = currentUserPhone,
                    recipient = recipientPhone,
                    isGroup = 0,
                    ciphertext = encrypted.ciphertext,
                    iv = encrypted.iv,
                    ephemeralPublicKey = encrypted.ephemeralPublicKey,
                    messageType = type.name,
                    timestamp = timestamp,
                    status = "SENDING"
                )

                // Update local Room database with ciphertext info
                val finalMsg = pendingMsg.copy(
                    ciphertext = encrypted.ciphertext,
                    iv = encrypted.iv,
                    ephemeralPublicKey = encrypted.ephemeralPublicKey,
                    status = MessageStatus.SENT
                )
                messageDao.insertMessage(MessageEntity.fromDomain(finalMsg))

                socketManager.sendMessage(socketMsg)

            } catch (e: Exception) {
                Log.e("ChatRepository", "E2EE failed, queuing message offline", e)
                // Queue message in outbox to retry when connection is stable
                outboxDao.enqueueMessage(
                    OutboxEntity(
                        id = messageId,
                        recipientPhone = recipientPhone,
                        isGroup = false,
                        messageType = type.name,
                        plainText = messageText,
                        mediaPath = null,
                        timestamp = timestamp
                    )
                )
            }
        }

        return messageId
    }

    override suspend fun sendTyping(recipientPhone: String, isTyping: Boolean) {
        socketManager.sendTyping(recipientPhone, isTyping)
    }

    override suspend fun sendCallSignal(recipientPhone: String, signalJson: String): Boolean {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return false
        
        return try {
            val session = getOrCreateSession(recipientPhone)
            val encrypted = DoubleRatchetEngine.encrypt(session, signalJson.toByteArray(Charsets.UTF_8))
            saveSession(recipientPhone, session)
            
            val socketMsg = SocketMessage(
                id = messageId,
                sender = currentUserPhone,
                recipient = recipientPhone,
                isGroup = 0,
                ciphertext = encrypted.ciphertext,
                iv = encrypted.iv,
                ephemeralPublicKey = encrypted.ephemeralPublicKey,
                messageType = MessageType.CALL_SIGNAL.name,
                timestamp = timestamp,
                status = "SENDING"
            )
            socketManager.sendMessage(socketMsg)
            true
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to send call signal", e)
            false
        }
    }

    override suspend fun searchMessages(query: String): List<Message> {
        return messageDao.searchMessages(query).map { it.toDomain() }
    }

    // Process queued offline messages
    private suspend fun syncOfflineOutbox() {
        val queued = outboxDao.getAllQueuedMessages()
        if (queued.isEmpty()) return
        Log.d("ChatRepository", "Syncing ${queued.size} queued offline messages...")
        
        for (q in queued) {
            outboxDao.dequeueMessage(q.id)
            sendMessage(q.recipientPhone, q.plainText ?: "", MessageType.valueOf(q.messageType))
        }
    }

    // WebSocket events observer
    private fun observeSocketEvents() {
        repositoryScope.launch {
            socketManager.incomingMessages.collect { socketMsg ->
                handleIncomingMessage(socketMsg)
            }
        }

        repositoryScope.launch {
            socketManager.messageReceipts.collect { receipt ->
                val status = MessageStatus.valueOf(receipt.status)
                messageDao.updateMessageStatus(receipt.messageId, status.name)
            }
        }

        repositoryScope.launch {
            socketManager.typingNotifications.collect { typing ->
                val updated = _activeTypingState.value.toMutableMap()
                updated[typing.sender] = typing.isTyping
                _activeTypingState.value = updated
            }
        }
    }

    private suspend fun handleIncomingMessage(socketMsg: SocketMessage) {
        val myPhone = userRepository.currentUser.value?.phone ?: return
        
        try {
            // Rebuild E2E Session
            val session = getOrCreateSession(socketMsg.sender, socketMsg.ephemeralPublicKey)
            
            // Decrypt ciphertext
            val decryptedBytes = DoubleRatchetEngine.decrypt(
                session,
                EncryptedPayload(socketMsg.ciphertext, socketMsg.iv, socketMsg.ephemeralPublicKey!!)
            )
            val decryptedText = String(decryptedBytes, Charsets.UTF_8)
            
            // Save updated session state
            saveSession(socketMsg.sender, session)

            if (socketMsg.messageType == MessageType.CALL_SIGNAL.name) {
                _incomingCallSignals.emit(Pair(socketMsg.sender, decryptedText))
                socketManager.sendReceipt(socketMsg.id, socketMsg.sender, "DELIVERED")
                return
            }

            // Save decrypted message to database
            val domainMsg = Message(
                id = socketMsg.id,
                senderPhone = socketMsg.sender,
                recipientPhone = socketMsg.recipient,
                isGroup = socketMsg.isGroup == 1,
                ciphertext = socketMsg.ciphertext,
                iv = socketMsg.iv,
                ephemeralPublicKey = socketMsg.ephemeralPublicKey,
                messageType = MessageType.valueOf(socketMsg.messageType),
                timestamp = socketMsg.timestamp,
                status = MessageStatus.READ, // Read locally
                plainText = decryptedText
            )
            messageDao.insertMessage(MessageEntity.fromDomain(domainMsg))
            updateChatLastMessage(socketMsg.sender, domainMsg)

            // Reply with delivery receipt
            socketManager.sendReceipt(socketMsg.id, socketMsg.sender, "DELIVERED")

        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to decrypt incoming message from ${socketMsg.sender}", e)
        }
    }

    private suspend fun updateChatLastMessage(recipientPhone: String, msg: Message) {
        val existing = chatDao.getChat(recipientPhone)
        val chat = ChatEntity(
            recipientPhone = recipientPhone,
            displayName = existing?.displayName ?: "Contact $recipientPhone",
            avatarUrl = existing?.avatarUrl,
            isGroup = msg.isGroup,
            lastMessageId = msg.id,
            unreadCount = if (userRepository.currentUser.value?.phone == msg.senderPhone) 0 else (existing?.unreadCount ?: 0) + 1,
            isPinned = existing?.isPinned ?: false,
            isMuted = existing?.isMuted ?: false,
            isArchived = existing?.isArchived ?: false
        )
        chatDao.insertOrUpdateChat(chat)
    }

    // E2E Session Serialization & Orchestration Helpers
    private suspend fun getOrCreateSession(partnerPhone: String, incomingEphemeralKey: String? = null): DoubleRatchetSession {
        val entity = sessionDao.getSession(partnerPhone)
        if (entity != null) {
            return deserializeSession(entity.sessionJson)
        }

        // Alice initiator flow
        if (incomingEphemeralKey == null) {
            Log.d("ChatRepository", "No E2EE session with $partnerPhone. Initiating X3DH Key Agreement...")
            val bundle = api.getPreKeyBundle(partnerPhone)
            
            val partnerIdentityKey = SignalKeys.decodePublicKey(bundle.identityKey)
            val partnerSignedPreKey = SignalKeys.decodePublicKey(bundle.signedPreKey)
            val partnerOneTimePreKey = bundle.oneTimePreKey?.let { SignalKeys.decodePublicKey(it) }

            val ourIdentityKey = userRepository.getLocalIdentityKey() 
                ?: throw IllegalStateException("Local Identity Key not generated")

            val session = DoubleRatchetEngine.initAlice(
                ourIdentityKey = ourIdentityKey,
                recipientIdentityKey = partnerIdentityKey,
                recipientSignedPreKey = partnerSignedPreKey,
                recipientOneTimePreKey = partnerOneTimePreKey
            )
            return session
        } else {
            // Bob receiver flow
            Log.d("ChatRepository", "No E2EE session with $partnerPhone. Rebuilding X3DH from incoming message...")
            val ourIdentityKey = userRepository.getLocalIdentityKey()!!
            val ourSignedPreKey = userRepository.getLocalSignedPreKey()!!
            
            // Rebuild Bob's session using the ephemeral key Alice sent in the message header
            val partnerIdentityKey = api.getPreKeyBundle(partnerPhone).identityKey.let { SignalKeys.decodePublicKey(it) }
            val partnerEphemeralKey = SignalKeys.decodePublicKey(incomingEphemeralKey)
            
            // Try to find the OTPK used (simplified: use our local one-time prekey if active, or fall back to main key)
            val ourOneTimePreKey = userRepository.getLocalOneTimePreKey(incomingEphemeralKey) // if ephemeral maps directly to one of our keys

            val session = DoubleRatchetEngine.initBob(
                ourIdentityKey = ourIdentityKey,
                ourSignedPreKey = ourSignedPreKey,
                ourOneTimePreKey = ourOneTimePreKey,
                senderIdentityKey = partnerIdentityKey,
                senderEphemeralKey = partnerEphemeralKey
            )
            return session
        }
    }

    private suspend fun saveSession(partnerPhone: String, session: DoubleRatchetSession) {
        val jsonStr = serializeSession(session)
        sessionDao.insertSession(SessionEntity(partnerPhone, jsonStr))
    }

    private fun serializeSession(session: DoubleRatchetSession): String {
        val json = JSONObject().apply {
            put("rootKey", Base64.encodeToString(session.rootKey, Base64.NO_WRAP))
            put("sendingChainKey", session.sendingChainKey?.let { Base64.encodeToString(it, Base64.NO_WRAP) })
            put("receivingChainKey", session.receivingChainKey?.let { Base64.encodeToString(it, Base64.NO_WRAP) })
            
            // Local key pair
            put("localPrivate", Base64.encodeToString(session.localKeyPair.private.encoded, Base64.NO_WRAP))
            put("localPublic", Base64.encodeToString(session.localKeyPair.public.encoded, Base64.NO_WRAP))
            
            // Remote key
            put("remotePublic", session.remotePublicKey?.let { SignalKeys.encodePublicKey(it) })
            
            // Skipped keys
            val skipped = JSONObject()
            session.skippedMessageKeys.forEach { (k, v) ->
                skipped.put(k, Base64.encodeToString(v, Base64.NO_WRAP))
            }
            put("skipped", skipped)
            
            // Seq numbers
            put("seqSending", session.sequenceNumberSending)
            put("seqReceiving", session.sequenceNumberReceiving)
        }
        return json.toString()
    }

    private fun deserializeSession(jsonStr: String): DoubleRatchetSession {
        val json = JSONObject(jsonStr)
        val rootKey = Base64.decode(json.getString("rootKey"), Base64.DEFAULT)
        val sendingChainKey = json.optString("sendingChainKey", null)?.let { Base64.decode(it, Base64.DEFAULT) }
        val receivingChainKey = json.optString("receivingChainKey", null)?.let { Base64.decode(it, Base64.DEFAULT) }
        
        // Rebuild local keypair
        val privBytes = Base64.decode(json.getString("localPrivate"), Base64.DEFAULT)
        val pubBytes = Base64.decode(json.getString("localPublic"), Base64.DEFAULT)
        val kf = java.security.KeyFactory.getInstance("EC")
        val privateKey = kf.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(privBytes))
        val publicKey = kf.generatePublic(java.security.spec.X509EncodedKeySpec(pubBytes))
        val localKeyPair = KeyPair(publicKey, privateKey)

        // Rebuild remote pub key
        val remotePublicKey = json.optString("remotePublic", null)?.let { SignalKeys.decodePublicKey(it) }
        
        // Skipped keys
        val skippedObj = json.getJSONObject("skipped")
        val skipped = mutableMapOf<String, ByteArray>()
        val keys = skippedObj.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            skipped[key] = Base64.decode(skippedObj.getString(key), Base64.DEFAULT)
        }

        return DoubleRatchetSession(
            rootKey = rootKey,
            sendingChainKey = sendingChainKey,
            receivingChainKey = receivingChainKey,
            localKeyPair = localKeyPair,
            remotePublicKey = remotePublicKey,
            skippedMessageKeys = skipped,
            sequenceNumberSending = json.getInt("seqSending"),
            sequenceNumberReceiving = json.getInt("seqReceiving")
        )
    }
}
