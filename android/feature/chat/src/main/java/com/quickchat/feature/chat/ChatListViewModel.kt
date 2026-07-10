package com.quickchat.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickchat.core.model.Chat
import com.quickchat.core.model.Message
import com.quickchat.core.model.PhoneContact
import com.quickchat.core.network.repository.ChatRepository
import com.quickchat.core.network.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import retrofit2.HttpException
import org.json.JSONObject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    // Search query states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    val searchResultsChats: StateFlow<List<Chat>> = combine(_searchQuery, chats) { query, chatList ->
        if (query.isBlank()) {
            emptyList()
        } else {
            chatList.filter {
                it.displayName.contains(query, ignoreCase = true) || it.recipientPhone.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResultsContacts: StateFlow<List<PhoneContact>> = combine(_searchQuery, chatRepository.getPhoneContactsFlow()) { query, contactsList ->
        if (query.isBlank()) {
            emptyList()
        } else {
            contactsList.filter {
                it.contactName.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResultsMessages: StateFlow<List<MessageSearchResult>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                flow {
                    val results = chatRepository.searchLocalMessages(query)
                    val currentChats = chats.value.associateBy { it.recipientPhone }
                    val mapped = results.map { msg ->
                        val chatPhone = if (msg.senderPhone == currentUser.value?.phone) msg.recipientPhone else msg.senderPhone
                        val chat = currentChats[chatPhone]
                        val chatName = chat?.displayName ?: "Contact $chatPhone"
                        val senderName = if (msg.senderPhone == currentUser.value?.phone) "You" else chatName
                        MessageSearchResult(
                            message = msg,
                            senderName = senderName,
                            chatPhone = chatPhone,
                            chatName = chatName
                        )
                    }
                    emit(mapped)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Connect socket for real-time messaging on launch if user is logged in
        currentUser.value?.let {
            chatRepository.initSocketConnection(it.phone)
            viewModelScope.launch {
                chatRepository.syncAllChatProfiles()
            }
        }
    }

    fun startChatWithContact(phone: String, name: String) {
        viewModelScope.launch {
            chatRepository.savePhoneContact(phone, name)
            chatRepository.syncUserProfile(phone)
            chatRepository.ensureChatExists(phone, name)
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

data class MessageSearchResult(
    val message: Message,
    val senderName: String,
    val chatPhone: String,
    val chatName: String
)
