package com.quickchat.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickchat.core.model.Chat
import com.quickchat.core.network.repository.ChatRepository
import com.quickchat.core.network.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import retrofit2.HttpException
import org.json.JSONObject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser = userRepository.currentUser

    // Observe chats flow from DB
    val chats: StateFlow<List<Chat>> = chatRepository.getChatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe live typing states
    val typingStates = chatRepository.activeTypingState

    init {
        // Connect socket for real-time messaging on launch if user is logged in
        currentUser.value?.let {
            chatRepository.initSocketConnection(it.phone)
        }
    }

    fun startChatWithContact(phone: String, name: String) {
        viewModelScope.launch {
            // Check if contact sync matches, then initialize E2E session / insert mock chat
            // To start a chat, we just send a mock first message or write it to Room DB
            val existing = chats.value.find { it.recipientPhone == phone }
            if (existing == null) {
                // Insert a dummy chat item in DB to open the view
                // In production, sync prekey and let user send first message.
                // We will handle it by just navigating to the room which automatically spins up E2E.
            }
        }
    }

    fun searchUserByUsername(username: String, onResult: (com.quickchat.core.model.User?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepository.searchUserByUsername(username)
                onResult(user, null)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = if (!errorBody.isNullOrEmpty()) {
                    try {
                        JSONObject(errorBody as String).getString("error")
                    } catch (jsonEx: Exception) {
                        e.message()
                    }
                } else {
                    e.message()
                }
                onResult(null, errorMsg)
            } catch (e: Exception) {
                onResult(null, e.localizedMessage ?: "User not found")
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
        // Close socket
        chatRepository.closeSocketConnection()
    }
}
