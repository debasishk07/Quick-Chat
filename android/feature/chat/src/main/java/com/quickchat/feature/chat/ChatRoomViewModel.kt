package com.quickchat.feature.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickchat.core.crypto.SecurityCodeVerifier
import com.quickchat.core.crypto.SignalKeys
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageType
import com.quickchat.core.network.api.QuickChatApi
import com.quickchat.core.network.repository.ChatRepository
import com.quickchat.core.network.repository.UserRepository
import com.quickchat.core.network.websocket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.PublicKey
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val socketManager: SocketManager,
    private val api: QuickChatApi
) : ViewModel() {

    private val _recipientPhone = MutableStateFlow("")
    val recipientPhone: StateFlow<String> = _recipientPhone.asStateFlow()

    private val _recipientName = MutableStateFlow("")
    val recipientName: StateFlow<String> = _recipientName.asStateFlow()

    private val _recipientAvatar = MutableStateFlow<String?>(null)
    val recipientAvatar: StateFlow<String?> = _recipientAvatar.asStateFlow()

    private val _isProfileLoaded = MutableStateFlow(false)
    val isProfileLoaded: StateFlow<Boolean> = _isProfileLoaded.asStateFlow()

    private val _recipientDisappearingDuration = MutableStateFlow(0L)
    val recipientDisappearingDuration: StateFlow<Long> = _recipientDisappearingDuration.asStateFlow()

    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isPartnerOnline = MutableStateFlow(false)
    val isPartnerOnline: StateFlow<Boolean> = _isPartnerOnline.asStateFlow()

    private val _partnerLastSeen = MutableStateFlow(0L)
    val partnerLastSeen: StateFlow<Long> = _partnerLastSeen.asStateFlow()

    private val _partnerTyping = MutableStateFlow(false)
    val partnerTyping: StateFlow<Boolean> = _partnerTyping.asStateFlow()

    private val _securityFingerprint = MutableStateFlow<String?>(null)
    val securityFingerprint: StateFlow<String?> = _securityFingerprint.asStateFlow()

    private var messageCollectionJob: Job? = null
    private var presenceCollectionJob: Job? = null
    private var profileCollectionJob: Job? = null

    fun initRecipient(phone: String) {
        _recipientPhone.value = phone
        
        // 1. Collect contact details reactively
        profileCollectionJob?.cancel()
        profileCollectionJob = viewModelScope.launch {
            chatRepository.getChatFlow(phone).collect { chat ->
                _recipientName.value = chat?.displayName ?: "Contact $phone"
                _recipientAvatar.value = chat?.avatarUrl
                _isProfileLoaded.value = chat?.isProfileLoaded ?: false
                _recipientDisappearingDuration.value = chat?.disappearingDuration ?: 0L
            }
        }

        viewModelScope.launch {
            chatRepository.isBlockedFlow(phone).collect { blocked ->
                _isBlocked.value = blocked
            }
        }

        viewModelScope.launch {
            chatRepository.syncUserProfile(phone)
            chatRepository.clearUnreadCount(phone)
        }

        // 2. Collect messages flow
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            chatRepository.getMessagesFlow(phone).collect { list ->
                _messages.value = list
            }
        }

        // 3. Listen to live presence and typing of this contact
        presenceCollectionJob?.cancel()
        presenceCollectionJob = viewModelScope.launch {
            // Check typing
            chatRepository.activeTypingState.collect { map ->
                _partnerTyping.value = map[phone] ?: false
            }
        }

        viewModelScope.launch {
            socketManager.presenceChanges.collect { p ->
                if (p.phone == phone) {
                    _isPartnerOnline.value = p.isOnline
                    _partnerLastSeen.value = p.lastSeen
                }
            }
        }

        // 4. Precompute Security fingerprint
        computeSecurityFingerprint(phone)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(_recipientPhone.value, text, MessageType.TEXT)
        }
    }

    fun sendTyping(isTyping: Boolean) {
        viewModelScope.launch {
            chatRepository.sendTyping(_recipientPhone.value, isTyping)
        }
    }

    private fun computeSecurityFingerprint(phone: String) {
        viewModelScope.launch {
            try {
                // Fetch our identity key
                val ourKeyPair = userRepository.getLocalIdentityKey() ?: return@launch
                
                // Fetch partner identity key from server
                val bundle = api.getPreKeyBundle(phone)
                val partnerPubKey = SignalKeys.decodePublicKey(bundle.identityKey)
                
                val fingerprint = SecurityCodeVerifier.generateFingerprint(ourKeyPair.public, partnerPubKey)
                _securityFingerprint.value = fingerprint
            } catch (e: Exception) {
                Log.e("ChatRoomViewModel", "Failed to compute fingerprint", e)
            }
        }
    }

    fun setDisappearingDuration(durationMs: Long) {
        viewModelScope.launch {
            chatRepository.setDisappearingDuration(_recipientPhone.value, durationMs)
        }
    }

    fun blockRecipient() {
        viewModelScope.launch {
            chatRepository.blockUser(_recipientPhone.value, _recipientName.value)
        }
    }

    fun unblockRecipient() {
        viewModelScope.launch {
            chatRepository.unblockUser(_recipientPhone.value)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearChatHistory(_recipientPhone.value)
        }
    }

    fun deleteChat(onCompleted: () -> Unit) {
        viewModelScope.launch {
            chatRepository.deleteChat(_recipientPhone.value)
            onCompleted()
        }
    }

    fun reportRecipient(reason: String, description: String?, attachMessages: Boolean, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val phone = _recipientPhone.value
            val lastN = messages.value.filter { it.plainText != null }.takeLast(5)
            val success = chatRepository.reportUser(phone, reason, description, attachMessages, lastN)
            onCompleted(success)
        }
    }
}
