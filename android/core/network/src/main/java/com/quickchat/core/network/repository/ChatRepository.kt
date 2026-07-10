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
import java.security.KeyPair
import java.security.PublicKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    fun getChatsFlow(): Flow<List<Chat>>
    fun getMessagesFlow(recipientPhone: String): Flow<List<Message>>
    suspend fun sendMessage(recipientPhone: String, messageText: String, type: MessageType, existingMessageId: String? = null): String
    suspend fun sendTyping(recipientPhone: String, isTyping: Boolean)
    suspend fun searchMessages(query: String): List<Message>
    fun initSocketConnection(phone: String)
    fun closeSocketConnection()
    val activeTypingState: StateFlow<Map<String, Boolean>> // phone -> isTyping
    suspend fun getChat(phone: String): Chat?
    suspend fun clearUnreadCount(phone: String)
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
    private val userRepository: UserRepository
) : ChatRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    
    private val _activeTypingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val activeTypingState: StateFlow<Map<String, Boolean>> = _activeTypingState

    private val _incomingCallSignals = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    override val incomingCallSignals: SharedFlow<Pair<String, String>> = _incomingCallSignals.asSharedFlow()

    init {
        observeSocketEvents()
        
        // Start background cleaner for E2EE disappearing messages
        repositoryScope.launch {
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
            entities.map { it.toDomain() }
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

    override suspend fun sendMessage(recipientPhone: String, messageText: String, type: MessageType, existingMessageId: String?): String {
        val messageId = existingMessageId ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserPhone = userRepository.currentUser.value?.phone ?: return ""

        val chat = chatDao.getChat(recipientPhone)
        val disappearingDuration = chat?.disappearingDuration ?: 0L
        val expireAt = if (disappearingDuration > 0) timestamp + disappearingDuration else null

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
            expireAt = expireAt
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
            sendMessage(
                recipientPhone = q.recipientPhone,
                messageText = q.plainText ?: "",
                type = MessageType.valueOf(q.messageType),
                existingMessageId = q.id
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

        repositoryScope.launch {
            socketManager.presenceChanges.collect { presence ->
                syncUserProfile(presence.phone)
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

            val chat = chatDao.getChat(socketMsg.sender)
            val disappearingDuration = chat?.disappearingDuration ?: 0L
            val expireAt = if (disappearingDuration > 0) socketMsg.timestamp + disappearingDuration else null

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
                plainText = decryptedText,
                expireAt = expireAt
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
}
