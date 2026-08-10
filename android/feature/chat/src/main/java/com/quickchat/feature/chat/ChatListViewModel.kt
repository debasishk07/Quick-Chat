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
    private val userRepository: UserRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {

    val currentUser = userRepository.currentUser

    // Observe chats flow from DB
    val chats: StateFlow<List<Chat>> = chatRepository.getChatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _favoriteOrder = MutableStateFlow<List<String>>(emptyList())
    val favoriteOrder: StateFlow<List<String>> = _favoriteOrder.asStateFlow()

    private fun loadFavoriteOrder() {
        val json = sharedPreferences.getString("favorite_contacts_order", null)
        if (json != null) {
            try {
                val array = org.json.JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                _favoriteOrder.value = list
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun saveFavoriteOrder(order: List<String>) {
        _favoriteOrder.value = order
        val array = org.json.JSONArray(order)
        sharedPreferences.edit().putString("favorite_contacts_order", array.toString()).apply()
    }

    fun toggleChatPinned(phone: String, isPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.setChatPinned(phone, isPinned)
            val currentOrder = _favoriteOrder.value.toMutableList()
            if (isPinned) {
                if (!currentOrder.contains(phone)) {
                    currentOrder.add(phone)
                }
            } else {
                currentOrder.remove(phone)
            }
            saveFavoriteOrder(currentOrder)
        }
    }

    fun deleteChat(phone: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(phone)
        }
    }

    val favorites: StateFlow<List<Chat>> = combine(chats, _favoriteOrder) { chatList, order ->
        val pinned = chatList.filter { it.isPinned }
        val orderMap = order.withIndex().associate { it.value to it.index }
        pinned.sortedWith(compareBy({ orderMap[it.recipientPhone] ?: Integer.MAX_VALUE }, { it.recipientPhone }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        loadFavoriteOrder()
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
