package com.quickchat.core.network.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.quickchat.core.crypto.*
import com.quickchat.core.database.dao.*
import com.quickchat.core.database.entities.*
import com.quickchat.core.model.Chat
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageStatus
import com.quickchat.core.model.BlockedContact
import com.quickchat.core.model.PhoneContact
import com.quickchat.core.model.MessageType
import com.quickchat.core.network.api.*
import com.quickchat.core.network.websocket.SocketManager
import com.quickchat.core.network.websocket.SocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.security.KeyPair
import java.security.PublicKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    fun getChatsFlow(): Flow<List<Chat>>
    fun getMessagesFlow(recipientPhone: String): Flow<List<Message>>
    suspend fun sendMessage(recipientPhone: String, messageText: String, type: MessageType, existingMessageId: String? = null, mediaPath: String? = null): String
    suspend fun sendTyping(recipientPhone: String, isTyping: Boolean)
    suspend fun searchMessages(query: String): List<Message>
    fun initSocketConnection(phone: String)
    fun closeSocketConnection()
    val activeTypingState: StateFlow<Map<String, Boolean>> // phone -> isTyping
    suspend fun getChat(phone: String): Chat?
    suspend fun clearUnreadCount(phone: String)
    suspend fun markMessagesAsRead(senderPhone: String)
    val incomingCallSignals: SharedFlow<Pair<String, String>>
    suspend fun sendCallSignal(recipientPhone: String, signalJson: String): Boolean

    suspend fun savePhoneContact(phone: String, contactName: String)
    suspend fun syncUserProfile(phone: String)
    suspend fun syncAllChatProfiles()
    suspend fun ensureChatExists(phone: String, displayName: String)
    fun getChatFlow(phone: String): Flow<Chat?>
    fun getStarredMessagesFlow(recipientPhone: String): Flow<List<Message>>
    suspend fun setMessageStarred(messageId: String, isStarred: Boolean)
    suspend fun setDisappearingDuration(phone: String, durationMs: Long)
    fun getBlockedContactsFlow(): Flow<List<BlockedContact>>
    fun getPhoneContactsFlow(): Flow<List<PhoneContact>>
    suspend fun blockUser(phone: String, displayName: String)
    suspend fun unblockUser(phone: String)
    fun isBlockedFlow(phone: String): Flow<Boolean>
    suspend fun clearChatHistory(phone: String)
    suspend fun deleteChat(phone: String)
    suspend fun searchLocalMessages(query: String): List<Message>
    suspend fun reportUser(reportedPhone: String, reason: String, description: String?, attachMessages: Boolean, lastNMessages: List<Message>): Boolean

    // Delight Features APIs
    suspend fun setChatPinned(phone: String, isPinned: Boolean)
    fun getPinnedMessagesFlow(recipientPhone: String): Flow<List<Message>>
    suspend fun setPinMessage(messageId: String, isPinned: Boolean)
    suspend fun setMessageReaction(messageId: String, reaction: String?)
    suspend fun setVoiceMessagePlaybackSpeed(messageId: String, speed: Float)
    suspend fun scheduleMessage(recipientPhone: String, messageText: String, scheduledTime: Long): Long
    suspend fun getPendingScheduledMessages(): List<com.quickchat.core.database.entities.ScheduledMessageEntity>
    suspend fun deleteScheduledMessage(id: Long)
    fun getScheduledMessagesFlow(phone: String): Flow<List<com.quickchat.core.database.entities.ScheduledMessageEntity>>
    suspend fun editMessage(messageId: String, newText: String)
    suspend fun deleteMessage(messageId: String, mode: String)
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: QuickChatApi,
    private val socketManager: SocketManager,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val sessionDao: SessionDao,
    private val outboxDao: OutboxDao,
    private val userDao: UserDao,
    private val phoneContactDao: PhoneContactDao,
    private val blockedContactDao: BlockedContactDao,
    private val userRepository: UserRepository,
    private val scheduledMessageDao: ScheduledMessageDao,
    @ApplicationContext private val context: Context
) : ChatRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    
    private val _activeTypingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val activeTypingState: StateFlow<Map<String, Boolean>> = _activeTypingState

    private val _incomingCallSignals = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    override val incomingCallSignals: SharedFlow<Pair<String, String>> = _incomingCallSignals.asSharedFlow()

    init {
        observeSocketEvents()
        
        // Start background cleaner for E2EE disappearing messages and leaked delete commands
        repositoryScope.launch {
            try {
                messageDao.purgeLeakedDeleteCommands()
            } catch (e: Exception) {
                Log.e("ChatRepository", "Failed to purge leaked delete commands", e)
            }
            while (true) {
                try {
                    val now = System.currentTimeMillis()
                    messageDao.deleteExpiredMessagesFts(now)
                    messageDao.deleteExpiredMessages(now)
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Failed to clear expired messages", e)
                }
                kotlinx.coroutines.delay(5000) // check every 5 seconds
            }
        }
    }

    override fun initSocketConnection(phone: String) {
        // Points to host system loopback (standard Android emulator mapping for localhost)
        socketManager.connect("http://10.0.2.2:3000", phone)
        repositoryScope.launch {
            syncOfflineOutbox()
        }
        // Sync blocked users
        repositoryScope.launch {
            try {
                val response = api.getBlockedUsers(phone)
                if (response.success) {
                    val existing = blockedContactDao.getAllBlockedContacts().map { it.phone }.toSet()
                    val serverList = response.blocked.toSet()
                    for (p in serverList - existing) {
                        val contactName = phoneContactDao.getPhoneContact(p)?.contactName ?: userDao.getUser(p)?.displayName ?: p
                        blockedContactDao.insertBlockedContact(BlockedContactEntity(p, contactName))
                    }
                    for (p in existing - serverList) {
                        blockedContactDao.deleteBlockedContact(p)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Failed to sync blocked list", e)
            }
        }
    }

    override fun closeSocketConnection() {
        socketManager.disconnect()
    }

    override fun getChatsFlow(): Flow<List<Chat>> {
        return combine(
            chatDao.getAllChatsFlow(),
            userDao.getAllUsersFlow(),
            phoneContactDao.getAllPhoneContactsFlow()
        ) { chatsList, usersList, contactsList ->
            val usersMap = usersList.associateBy { it.phone }
            val contactsMap = contactsList.associateBy { it.phone }
            
            chatsList.map { entity ->
                val lastMsg = entity.lastMessageId?.let { id ->
                    messageDao.getMessage(id)?.toDomain()
                }
                
                val (resolvedName, resolvedAvatar) = if (entity.isGroup) {
                    entity.displayName to entity.avatarUrl
                } else {
                    val phone = entity.recipientPhone
                    val localContactName = contactsMap[phone]?.contactName
                    val cachedUser = usersMap[phone]
                    
                    val name = localContactName ?: cachedUser?.displayName ?: phone
                    val avatar = cachedUser?.avatarUrl
                    name to avatar
                }
                
                val isLoaded = entity.isGroup || contactsMap[entity.recipientPhone] != null || usersMap[entity.recipientPhone] != null
                Chat(
                    recipientPhone = entity.recipientPhone,
                    displayName = resolvedName,
                    avatarUrl = resolvedAvatar,
                    isGroup = entity.isGroup,
                    lastMessage = lastMsg,
                    unreadCount = entity.unreadCount,
                    isPinned = entity.isPinned,
                    isMuted = entity.isMuted,
                    isArchived = entity.isArchived,
                    isProfileLoaded = isLoaded
                )
            }
        }
    }

    override fun getMessagesFlow(recipientPhone: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(recipientPhone).map { entities ->
            val list = entities.map { it.toDomain() }
            Log.d("DEBUG_DELETE_FLOW", "getMessagesFlow for $recipientPhone fetched ${list.size} messages: ${list.map { "id=${it.id}, isDeleted=${it.isDeleted}, text=${it.plainText}" }}")
            list
        }
    }

    override suspend fun getChat(phone: String): Chat? {
        val entity = chatDao.getChat(phone) ?: return null
        val lastMsg = entity.lastMessageId?.let { id ->
            messageDao.getMessage(id)?.toDomain()
        }
        val (resolvedName, resolvedAvatar) = if (entity.isGroup) {
            entity.displayName to entity.avatarUrl
        } else {
            val localContactName = phoneContactDao.getPhoneContact(phone)?.contactName
            val cachedUser = userDao.getUser(phone)
            
            val name = localContactName ?: cachedUser?.displayName ?: phone
            val avatar = cachedUser?.avatarUrl
            name to avatar
        }
        
        val isLoaded = entity.isGroup || phoneContactDao.getPhoneContact(phone) != null || userDao.getUser(phone) != null
        return Chat(
            recipientPhone = entity.recipientPhone,
            displayName = resolvedName,
            avatarUrl = resolvedAvatar,
            isGroup = entity.isGroup,
            lastMessage = lastMsg,
            unreadCount = entity.unreadCount,
            isPinned = entity.isPinned,
            isMuted = entity.isMuted,
            isArchived = entity.isArchived,
            isProfileLoaded = isLoaded
        )
    }

    override suspend fun clearUnreadCount(phone: String) {
        chatDao.clearUnreadCount(phone)
    }

    override suspend fun markMessagesAsRead(senderPhone: String) {
        val unreadList = messageDao.getUnreadReceivedMessages(senderPhone)
        if (unreadList.isNotEmpty()) {
            val ids = unreadList.map { it.id }
            messageDao.updateMessagesStatus(ids, MessageStatus.READ.name)
            socketManager.sendBatchReceipts(ids, senderPhone, MessageStatus.READ.name)
        }
    }

    override suspend fun sendMessage(
        recipientPhone: String,
        messageText: String,
        type: MessageType,
        existingMessageId: String?,
        mediaPath: String?
    ): String {
        val messageId = existingMessageId ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return ""

        val chat = chatDao.getChat(recipientPhone)
        val disappearingDuration = chat?.disappearingDuration ?: 0L
        val expireAt = if (disappearingDuration > 0) timestamp + disappearingDuration else null

        val isEdit = existingMessageId != null

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
            plainText = messageText,
            expireAt = expireAt,
            isEdited = isEdit
        )
        messageDao.insertMessage(MessageEntity.fromDomain(pendingMsg))
        if (messageText.isNotBlank()) {
            messageDao.insertMessageFts(
                MessageFtsEntity(
                    messageId = messageId,
                    chatPhone = recipientPhone,
                    plainText = messageText
                )
            )
        }
        updateChatLastMessage(recipientPhone, pendingMsg)

        // 2. Perform E2E Encryption and relay in background
        repositoryScope.launch {
            var finalMessageText = messageText
            var finalMediaPath = mediaPath

            var publicId: String? = null
            var duration: Double? = null
            var format: String? = null
            var bytes: Long? = null

            try {
                if (finalMediaPath != null) {
                    val cResult = uploadAndEncryptMedia(finalMediaPath)
                        ?: throw IllegalStateException("Failed to upload/encrypt media")
                    val mediaUrlWithKey = cResult.mediaUrlWithKey
                    publicId = cResult.publicId
                    duration = cResult.duration
                    format = cResult.format
                    bytes = cResult.bytes

                    if (type == MessageType.VOICE) {
                        val parts = mediaUrlWithKey.split("#")
                        val url = parts[0]
                        val keyIv = parts[1].split(",")
                        val key = keyIv[0]
                        val iv = keyIv[1]
                        val amplitudes = messageText.split(",").map { it.toIntOrNull() ?: 0 }
                        val voicePayload = JSONObject().apply {
                            put("url", url)
                            put("key", key)
                            put("iv", iv)
                            put("amplitudes", JSONArray(amplitudes))
                            if (duration != null) put("duration", duration)
                        }.toString()
                        finalMessageText = voicePayload
                    } else if (type == MessageType.IMAGE || type == MessageType.VIDEO) {
                        if (messageText == "view_once") {
                            finalMessageText = "view_once:$mediaUrlWithKey"
                        } else {
                            finalMessageText = mediaUrlWithKey
                        }
                    }
                    finalMediaPath = null
                } else {
                    if (type == MessageType.TEXT) {
                        finalMessageText = kotlinx.coroutines.withContext(Dispatchers.IO) {
                            fetchLinkPreviewIfUrl(messageText)
                        }
                    }
                }

                // Update local Room database plainText with final plaintext details and Cloudinary metadata
                val pendingUpdated = pendingMsg.copy(
                    plainText = finalMessageText,
                    publicId = publicId,
                    mediaDuration = duration,
                    mediaFormat = format,
                    fileSize = bytes
                )
                messageDao.insertMessage(MessageEntity.fromDomain(pendingUpdated))

                // Get or establish E2E session
                val session = getOrCreateSession(recipientPhone)
                
                // Encrypt payload
                val encrypted = DoubleRatchetEngine.encrypt(session, finalMessageText.toByteArray(Charsets.UTF_8))
                
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
                val finalMsg = pendingUpdated.copy(
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
                        mediaPath = finalMediaPath,
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
            sendMessage(
                recipientPhone = q.recipientPhone,
                messageText = q.plainText ?: "",
                type = MessageType.valueOf(q.messageType),
                existingMessageId = q.id,
                mediaPath = q.mediaPath
            )
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
                val ids = receipt.messageIds ?: if (receipt.messageId != null) listOf(receipt.messageId) else emptyList()
                if (ids.isNotEmpty()) {
                    messageDao.updateMessagesStatus(ids, status.name)
                }
            }
        }

        repositoryScope.launch {
            socketManager.typingNotifications.collect { typing ->
                val updated = _activeTypingState.value.toMutableMap()
                updated[typing.sender] = typing.isTyping
                _activeTypingState.value = updated
            }
        }

        repositoryScope.launch {
            socketManager.presenceChanges.collect { presence ->
                syncUserProfile(presence.phone)
            }
        }

        repositoryScope.launch {
            socketManager.messageDeletedEvents.collect { del ->
                if (del.mode == "everyone") {
                    messageDao.markMessageAsDeleted(del.messageId)
                    messageDao.deleteMessageFts(del.messageId)
                }
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

            // 1. Intercept Special Command Messages (Reactions, Pin Updates, Deletions, View-Once Acknowledgements)
            if (decryptedText.startsWith("delete:")) {
                val parts = decryptedText.split(":")
                if (parts.size >= 3) {
                    val targetMsgId = parts[1]
                    val mode = parts[2]
                    if (mode == "everyone") {
                        messageDao.markMessageAsDeleted(targetMsgId)
                        messageDao.deleteMessageFts(targetMsgId)
                    }
                }
                socketManager.sendReceipt(socketMsg.id, socketMsg.sender, "DELIVERED")
                return
            }

            if (decryptedText.startsWith("react:")) {
                val parts = decryptedText.split(":")
                if (parts.size >= 3) {
                    val targetMsgId = parts[1]
                    val reaction = if (parts[2] == "null") null else parts[2]
                    val existing = messageDao.getMessage(targetMsgId)
                    if (existing != null) {
                        messageDao.insertMessage(existing.copy(reaction = reaction))
                    }
                }
                socketManager.sendReceipt(socketMsg.id, socketMsg.sender, "DELIVERED")
                return
            }

            if (decryptedText.startsWith("pin:")) {
                val parts = decryptedText.split(":")
                if (parts.size >= 4) {
                    val targetMsgId = parts[1]
                    val action = parts[2]
                    val sysText = parts.drop(3).joinToString(":")
                    val existing = messageDao.getMessage(targetMsgId)
                    if (existing != null) {
                        messageDao.insertMessage(existing.copy(pinnedAt = if (action == "pin") System.currentTimeMillis() else null))
                    }
                    
                    // Insert a local SYSTEM log message
                    val chat = chatDao.getChat(socketMsg.sender)
                    val disappearingDuration = chat?.disappearingDuration ?: 0L
                    val expireAt = if (disappearingDuration > 0) socketMsg.timestamp + disappearingDuration else null
                    val sysMsg = Message(
                        id = UUID.randomUUID().toString(),
                        senderPhone = socketMsg.sender,
                        recipientPhone = socketMsg.recipient,
                        isGroup = socketMsg.isGroup == 1,
                        ciphertext = "",
                        iv = "",
                        messageType = MessageType.SYSTEM,
                        timestamp = socketMsg.timestamp,
                        status = MessageStatus.READ,
                        plainText = sysText,
                        expireAt = expireAt
                    )
                    messageDao.insertMessage(MessageEntity.fromDomain(sysMsg))
                    updateChatLastMessage(socketMsg.sender, sysMsg)
                }
                socketManager.sendReceipt(socketMsg.id, socketMsg.sender, "DELIVERED")
                return
            }

            if (decryptedText.startsWith("view_once_opened:")) {
                val parts = decryptedText.split(":")
                if (parts.size >= 2) {
                    val targetMsgId = parts[1]
                    val existing = messageDao.getMessage(targetMsgId)
                    if (existing != null) {
                        messageDao.insertMessage(existing.copy(plainText = "Opened"))
                    }
                }
                socketManager.sendReceipt(socketMsg.id, socketMsg.sender, "DELIVERED")
                return
            }

            val chat = chatDao.getChat(socketMsg.sender)
            val disappearingDuration = chat?.disappearingDuration ?: 0L
            val expireAt = if (disappearingDuration > 0) socketMsg.timestamp + disappearingDuration else null

            // Check if this is an edit of an existing message or if it's marked as deleted
            val existingMsg = messageDao.getMessage(socketMsg.id)
            val isEdit = existingMsg != null
            val isDeleted = (socketMsg.isDeleted == 1) || (existingMsg?.isDeleted == true)
            val textToSave = if (isDeleted) "This message was deleted" else decryptedText

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
                plainText = textToSave,
                expireAt = expireAt,
                isEdited = isEdit,
                reaction = existingMsg?.reaction,
                playbackSpeed = existingMsg?.playbackSpeed ?: 1.0f,
                pinnedAt = existingMsg?.pinnedAt,
                isDeleted = isDeleted
            )
            messageDao.insertMessage(MessageEntity.fromDomain(domainMsg))
            if (decryptedText != null && decryptedText.isNotBlank()) {
                messageDao.insertMessageFts(
                    MessageFtsEntity(
                        messageId = socketMsg.id,
                        chatPhone = socketMsg.sender,
                        plainText = decryptedText
                    )
                )
            }
            updateChatLastMessage(socketMsg.sender, domainMsg)

            // Trigger notification in background
            repositoryScope.launch {
                showMessageNotification(socketMsg.sender, decryptedText, socketMsg.id)
            }

            // Sync sender's profile in the background
            repositoryScope.launch {
                syncUserProfile(socketMsg.sender)
            }

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
            val bundle = try {
                api.getPreKeyBundle(partnerPhone)
            } catch (e: Exception) {
                Log.w("ChatRepository", "Could not fetch prekey bundle for $partnerPhone, generating fallback prekeys", e)
                val partnerKeys = SignalKeys.generateKeyPair()
                val partnerSignedKeys = SignalKeys.generateKeyPair()
                com.quickchat.core.network.api.PreKeyBundleResponse(
                    phone = partnerPhone,
                    identityKey = SignalKeys.encodePublicKey(partnerKeys.public),
                    signedPreKey = SignalKeys.encodePublicKey(partnerSignedKeys.public),
                    signedPreKeySignature = Base64.encodeToString("fallback_sig".toByteArray(), Base64.NO_WRAP),
                    oneTimePreKey = null
                )
            }
            
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
            val ourIdentityKey = userRepository.getLocalIdentityKey() ?: SignalKeys.generateKeyPair()
            val ourSignedPreKey = userRepository.getLocalSignedPreKey() ?: SignalKeys.generateKeyPair()
            
            val partnerIdentityKey = try {
                api.getPreKeyBundle(partnerPhone).identityKey.let { SignalKeys.decodePublicKey(it) }
            } catch (e: Exception) {
                SignalKeys.generateKeyPair().public
            }
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

    override suspend fun savePhoneContact(phone: String, contactName: String) {
        phoneContactDao.insertOrUpdatePhoneContact(PhoneContactEntity(phone, contactName))
    }

    override suspend fun syncUserProfile(phone: String) {
        try {
            val users = userRepository.syncContacts(listOf(phone))
            if (users.isNotEmpty()) {
                val user = users.first()
                userDao.insertOrUpdateUser(UserEntity.fromDomain(user))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to sync profile for $phone", e)
        }
    }

    override suspend fun syncAllChatProfiles() {
        try {
            val chatsList = chatDao.getChats()
            val phonesToSync = chatsList.filter { !it.isGroup }.map { it.recipientPhone }
            if (phonesToSync.isNotEmpty()) {
                val syncedUsers = userRepository.syncContacts(phonesToSync)
                val entities = syncedUsers.map { UserEntity.fromDomain(it) }
                userDao.insertOrUpdateUsers(entities)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to sync all chat profiles", e)
        }
    }

    override suspend fun ensureChatExists(phone: String, displayName: String) {
        val existing = chatDao.getChat(phone)
        if (existing == null) {
            val chat = ChatEntity(
                recipientPhone = phone,
                displayName = displayName,
                avatarUrl = null,
                isGroup = false,
                lastMessageId = null,
                unreadCount = 0,
                isPinned = false,
                isMuted = false,
                isArchived = false
            )
            chatDao.insertOrUpdateChat(chat)
        }
    }

    override fun getChatFlow(phone: String): Flow<Chat?> {
        return combine(
            chatDao.getChatFlow(phone),
            userDao.getUserFlow(phone),
            phoneContactDao.getPhoneContactFlow(phone)
        ) { chatEntity, userEntity, phoneContactEntity ->
            val (resolvedName, resolvedAvatar) = if (chatEntity?.isGroup == true) {
                chatEntity.displayName to chatEntity.avatarUrl
            } else {
                val localContactName = phoneContactEntity?.contactName
                val name = localContactName ?: userEntity?.displayName ?: phone
                val avatar = userEntity?.avatarUrl
                name to avatar
            }
            
            val isLoaded = chatEntity?.isGroup == true || phoneContactEntity != null || userEntity != null
            Chat(
                recipientPhone = phone,
                displayName = resolvedName,
                avatarUrl = resolvedAvatar,
                isGroup = chatEntity?.isGroup ?: false,
                lastMessage = chatEntity?.lastMessageId?.let { id ->
                    messageDao.getMessage(id)?.toDomain()
                },
                unreadCount = chatEntity?.unreadCount ?: 0,
                isPinned = chatEntity?.isPinned ?: false,
                isMuted = chatEntity?.isMuted ?: false,
                isArchived = chatEntity?.isArchived ?: false,
                isProfileLoaded = isLoaded
            )
        }
    }

    override fun getStarredMessagesFlow(recipientPhone: String): Flow<List<Message>> {
        return messageDao.getStarredMessagesFlow(recipientPhone).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun setMessageStarred(messageId: String, isStarred: Boolean) {
        messageDao.updateMessageStarred(messageId, isStarred)
    }

    override suspend fun setDisappearingDuration(phone: String, durationMs: Long) {
        chatDao.updateDisappearingDuration(phone, durationMs)
    }

    override fun getBlockedContactsFlow(): Flow<List<BlockedContact>> =
        blockedContactDao.getAllBlockedContactsFlow().map { list ->
            list.map { BlockedContact(it.phone, it.displayName) }
        }

    override fun getPhoneContactsFlow(): Flow<List<PhoneContact>> =
        phoneContactDao.getAllPhoneContactsFlow().map { list ->
            list.map { PhoneContact(it.phone, it.contactName) }
        }

    override suspend fun blockUser(phone: String, displayName: String) {
        val myPhone = userRepository.currentUser.value?.phone ?: return
        try {
            api.blockUser(BlockRequest(blockerPhone = myPhone, blockedPhone = phone))
            blockedContactDao.insertBlockedContact(BlockedContactEntity(phone, displayName))
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to block user on server", e)
            blockedContactDao.insertBlockedContact(BlockedContactEntity(phone, displayName))
        }
    }

    override suspend fun unblockUser(phone: String) {
        val myPhone = userRepository.currentUser.value?.phone ?: return
        try {
            api.unblockUser(UnblockRequest(blockerPhone = myPhone, blockedPhone = phone))
            blockedContactDao.deleteBlockedContact(phone)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to unblock user on server", e)
            blockedContactDao.deleteBlockedContact(phone)
        }
    }

    override fun isBlockedFlow(phone: String): Flow<Boolean> = blockedContactDao.isBlockedFlow(phone)

    override suspend fun clearChatHistory(phone: String) {
        messageDao.deleteMessagesForChat(phone)
        messageDao.deleteMessagesFtsForChat(phone)
        val chat = chatDao.getChat(phone)
        if (chat != null) {
            chatDao.insertOrUpdateChat(chat.copy(lastMessageId = null))
        }
    }

    override suspend fun deleteChat(phone: String) {
        messageDao.deleteMessagesForChat(phone)
        messageDao.deleteMessagesFtsForChat(phone)
        chatDao.deleteChat(phone)
    }

    override suspend fun searchLocalMessages(query: String): List<Message> {
        if (query.isBlank()) return emptyList()
        val ftsQuery = query.trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }
        if (ftsQuery.isBlank()) return emptyList()
        return messageDao.searchMessagesFts(ftsQuery).map { it.toDomain() }
    }

    override suspend fun reportUser(
        reportedPhone: String,
        reason: String,
        description: String?,
        attachMessages: Boolean,
        lastNMessages: List<Message>
    ): Boolean {
        val reporterPhone = userRepository.currentUser.value?.phone ?: return false
        val messageDtos = if (attachMessages) {
            lastNMessages.map { msg ->
                ReportedMessageDto(
                    id = msg.id,
                    senderPhone = msg.senderPhone,
                    recipientPhone = msg.recipientPhone,
                    text = msg.plainText ?: "",
                    timestamp = msg.timestamp
                )
            }
        } else {
            emptyList()
        }
        
        return try {
            val response = api.reportUser(
                ReportRequest(
                    reporterPhone = reporterPhone,
                    reportedPhone = reportedPhone,
                    reason = reason,
                    description = description,
                    attachMessages = attachMessages,
                    messages = messageDtos
                )
            )
            response.success
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to submit report", e)
            false
        }
    }

    // --- Delight Features Helpers & Implementations ---

    private data class CloudinaryUploadResult(
        val mediaUrlWithKey: String,
        val publicId: String?,
        val duration: Double?,
        val format: String?,
        val bytes: Long?
    )

    private suspend fun uploadAndEncryptMedia(localPath: String): CloudinaryUploadResult? = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(localPath)
            if (!file.exists()) return@withContext null
            val fileBytes = file.readBytes()
            val encryptResult = MediaEncryptor.encryptFile(fileBytes)
            
            val requestFile = okhttp3.RequestBody.create("application/octet-stream".toMediaTypeOrNull(), fileBytes)
            val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
            val uploadResponse = api.uploadCloudinaryMedia(body)
            if (uploadResponse.success && uploadResponse.secure_url != null) {
                val keyBase64 = Base64.encodeToString(encryptResult.mediaKey, Base64.NO_WRAP)
                val ivBase64 = Base64.encodeToString(encryptResult.iv, Base64.NO_WRAP)
                return@withContext CloudinaryUploadResult(
                    mediaUrlWithKey = "${uploadResponse.secure_url}#$keyBase64,$ivBase64",
                    publicId = uploadResponse.public_id,
                    duration = uploadResponse.duration,
                    format = uploadResponse.format,
                    bytes = uploadResponse.bytes
                )
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to upload media to Cloudinary", e)
        }
        null
    }

    private suspend fun fetchLinkPreviewIfUrl(text: String): String = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val urlRegex = "(https?://[\\w-]+(\\.[\\w-]+)+(/\\S*)?)".toRegex(RegexOption.IGNORE_CASE)
        val match = urlRegex.find(text) ?: return@withContext text
        val url = match.value

        if (text.startsWith("{") && text.endsWith("}")) return@withContext text

        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext text
            val html = response.body?.string() ?: return@withContext text

            val titleRegex = "<meta\\s+[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']*)[\"']".toRegex(RegexOption.IGNORE_CASE)
            val descRegex = "<meta\\s+[^>]*property=[\"']og:description[\"'][^>]*content=[\"']([^\"']*)[\"']".toRegex(RegexOption.IGNORE_CASE)
            val imgRegex = "<meta\\s+[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']*)[\"']".toRegex(RegexOption.IGNORE_CASE)

            val title = titleRegex.find(html)?.groupValues?.get(1) 
                ?: "<title>([^<]*)</title>".toRegex(RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
                ?: url
            val desc = descRegex.find(html)?.groupValues?.get(1) ?: ""
            val img = imgRegex.find(html)?.groupValues?.get(1) ?: ""

            val previewObj = JSONObject().apply {
                put("url", url)
                put("title", title)
                put("description", desc)
                put("imageUrl", img)
            }
            val payloadObj = JSONObject().apply {
                put("text", text)
                put("linkPreview", previewObj)
            }
            payloadObj.toString()
        } catch (e: java.lang.Exception) {
            Log.d("ChatRepository", "Failed to fetch link preview: ${e.message}")
            text
        }
    }

    private suspend fun showMessageNotification(senderPhone: String, text: String, messageId: String) {
        val replyLabel = "Reply"
        val remoteInput = androidx.core.app.RemoteInput.Builder("key_text_reply")
            .setLabel(replyLabel)
            .build()

        val replyIntent = android.content.Intent().apply {
            setClassName(context.packageName, "com.quickchat.app.scheduling.NotificationReplyReceiver")
            putExtra("sender_phone", senderPhone)
            putExtra("notification_id", senderPhone.hashCode())
            putExtra("message_id", messageId)
        }
        val replyPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            senderPhone.hashCode(),
            replyIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )

        val replyAction = androidx.core.app.NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val openIntent = android.content.Intent().apply {
            setClassName(context.packageName, "com.quickchat.app.MainActivity")
            putExtra("navigate_to_chat", senderPhone)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = android.app.PendingIntent.getActivity(
            context,
            senderPhone.hashCode(),
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val senderName = phoneContactDao.getPhoneContact(senderPhone)?.contactName 
            ?: userDao.getUser(senderPhone)?.displayName 
            ?: senderPhone

        val channelId = "chat_messages_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Chat Messages",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle(senderName)
            .setContentText(if (text.startsWith("view_once:")) "View-once media" else if (text.startsWith("{") && text.contains("linkPreview")) {
                try { JSONObject(text).getString("text") } catch(e: Exception) { text }
            } else text)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(openPendingIntent)
            .addAction(replyAction)
            .setAutoCancel(true)

        manager.notify(senderPhone.hashCode(), builder.build())
    }

    override suspend fun setChatPinned(phone: String, isPinned: Boolean) {
        val existing = chatDao.getChat(phone) ?: return
        chatDao.insertOrUpdateChat(existing.copy(isPinned = isPinned))
    }

    override fun getPinnedMessagesFlow(recipientPhone: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(recipientPhone).map { entities ->
            entities.map { it.toDomain() }.filter { it.pinnedAt != null }.sortedByDescending { it.pinnedAt }
        }
    }

    override suspend fun setPinMessage(messageId: String, isPinned: Boolean) {
        val msg = messageDao.getMessage(messageId) ?: return
        val updated = msg.copy(pinnedAt = if (isPinned) System.currentTimeMillis() else null)
        messageDao.insertMessage(updated)

        val action = if (isPinned) "pin" else "unpin"
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return
        val partner = if (msg.senderPhone == currentUserPhone) msg.recipientPhone else msg.senderPhone
        
        val actor = "You"
        val sysText = if (isPinned) "$actor pinned a message" else "$actor unpinned a message"
        
        sendMessage(partner, "pin:${msg.id}:$action:$sysText", MessageType.TEXT)
    }

    override suspend fun setMessageReaction(messageId: String, reaction: String?) {
        val msg = messageDao.getMessage(messageId) ?: return
        val updated = msg.copy(reaction = reaction)
        messageDao.insertMessage(updated)

        val currentUserPhone = userRepository.currentUser.value?.phone ?: return
        val partner = if (msg.senderPhone == currentUserPhone) msg.recipientPhone else msg.senderPhone
        sendMessage(partner, "react:${msg.id}:${reaction ?: "null"}", MessageType.TEXT)
    }

    override suspend fun setVoiceMessagePlaybackSpeed(messageId: String, speed: Float) {
        val msg = messageDao.getMessage(messageId) ?: return
        val updated = msg.copy(playbackSpeed = speed)
        messageDao.insertMessage(updated)
    }

    override suspend fun scheduleMessage(recipientPhone: String, messageText: String, scheduledTime: Long): Long {
        val entity = ScheduledMessageEntity(
            recipientPhone = recipientPhone,
            plainText = messageText,
            messageType = MessageType.TEXT.name,
            scheduledTime = scheduledTime
        )
        val id = scheduledMessageDao.insertScheduled(entity)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent().apply {
            setClassName(context.packageName, "com.quickchat.app.scheduling.ScheduledMessageReceiver")
            putExtra("scheduled_id", id)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                scheduledTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                scheduledTime,
                pendingIntent
            )
        }

        return id
    }

    override suspend fun getPendingScheduledMessages(): List<ScheduledMessageEntity> {
        return scheduledMessageDao.getPendingScheduledMessages(System.currentTimeMillis())
    }

    override suspend fun deleteScheduledMessage(id: Long) {
        scheduledMessageDao.deleteScheduled(id)
        val intent = android.content.Intent().apply {
            setClassName(context.packageName, "com.quickchat.app.scheduling.ScheduledMessageReceiver")
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun getScheduledMessagesFlow(phone: String): Flow<List<ScheduledMessageEntity>> {
        return scheduledMessageDao.getScheduledMessagesFlow(phone)
    }

    override suspend fun editMessage(messageId: String, newText: String) {
        val msg = messageDao.getMessage(messageId) ?: return
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return
        val partner = if (msg.senderPhone == currentUserPhone) msg.recipientPhone else msg.senderPhone

        sendMessage(partner, newText, MessageType.valueOf(msg.messageType), messageId)
    }

    override suspend fun deleteMessage(messageId: String, mode: String) {
        val msg = messageDao.getMessage(messageId) ?: return
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return
        val partner = if (msg.senderPhone == currentUserPhone) msg.recipientPhone else msg.senderPhone

        // Dequeue from outbox to prevent offline sync from re-sending/overwriting deleted message on restart
        outboxDao.dequeueMessage(messageId)

        if (mode == "me") {
            messageDao.deleteMessageLocally(messageId)
            messageDao.deleteMessageFts(messageId)
            socketManager.sendDeleteMessage(messageId, partner, "me")
            repositoryScope.launch {
                try {
                    api.deleteMessageApi(messageId, DeleteMessageRequest(currentUserPhone, partner, "me"))
                } catch (e: Exception) {
                    Log.e("ChatRepository", "REST deleteMessage fallback failed for mode 'me'", e)
                }
            }
        } else if (mode == "everyone") {
            if (msg.senderPhone != currentUserPhone) {
                Log.w("ChatRepository", "Unauthorized deleteMessage for everyone by non-sender")
                return
            }
            Log.d("DEBUG_DELETE_FLOW", "STEP 1 BEFORE DB UPDATE: messageId=$messageId, entity=${messageDao.getMessage(messageId)}")
            messageDao.markMessageAsDeleted(messageId)
            messageDao.deleteMessageFts(messageId)
            val updatedRow = messageDao.getMessage(messageId)
            Log.d("DEBUG_DELETE_FLOW", "STEP 1 AFTER DB UPDATE: messageId=$messageId, updatedEntity=$updatedRow, isDeleted=${updatedRow?.isDeleted}")
            socketManager.sendDeleteMessage(messageId, partner, "everyone", msg.publicId)
            repositoryScope.launch {
                try {
                    api.deleteMessageApi(messageId, DeleteMessageRequest(currentUserPhone, partner, "everyone"))
                } catch (e: Exception) {
                    Log.e("ChatRepository", "REST deleteMessage fallback failed for mode 'everyone'", e)
                }
            }
        }
    }
}
