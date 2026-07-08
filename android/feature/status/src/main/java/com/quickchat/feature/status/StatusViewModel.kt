package com.quickchat.feature.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickchat.core.model.StatusMediaType
import com.quickchat.core.model.UserStatus
import com.quickchat.core.network.repository.StatusRepository
import com.quickchat.core.network.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser = userRepository.currentUser

    // Observe status updates from Room DB
    val statusFeeds: StateFlow<List<UserStatus>> = statusRepository.getStatusesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    init {
        purgeExpired()
    }

    fun purgeExpired() {
        viewModelScope.launch {
            statusRepository.purgeExpiredStatuses()
        }
    }

    fun postTextStatus(text: String, backgroundColor: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            _uploadSuccess.value = false
            
            // Text status simulated by converting text to bytes for E2EE pipeline consistency
            val mockMediaBytes = text.toByteArray(Charsets.UTF_8)
            val success = statusRepository.uploadStatus(
                mediaBytes = mockMediaBytes,
                caption = backgroundColor, // Store styling metadata in caption slot
                mediaType = StatusMediaType.TEXT
            )
            
            _loading.value = false
            _uploadSuccess.value = success
        }
    }

    fun postMediaStatus(mediaBytes: ByteArray, caption: String?, type: StatusMediaType) {
        viewModelScope.launch {
            _loading.value = true
            _uploadSuccess.value = false
            
            val success = statusRepository.uploadStatus(mediaBytes, caption, type)
            
            _loading.value = false
            _uploadSuccess.value = success
        }
    }

    fun resetUploadState() {
        _uploadSuccess.value = false
    }
}
