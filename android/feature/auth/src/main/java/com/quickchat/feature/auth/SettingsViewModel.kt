package com.quickchat.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickchat.core.network.repository.UserRepository
import com.quickchat.core.network.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val userRepository: UserRepository
) : ViewModel() {

    val theme = settingsManager.theme
    val chatWallpaper = settingsManager.chatWallpaper
    val chatFontSize = settingsManager.chatFontSize

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    fun setTheme(themeValue: String) {
        settingsManager.setTheme(themeValue)
    }

    fun setChatWallpaper(value: String) {
        settingsManager.setChatWallpaper(value)
    }

    fun setChatFontSize(value: Float) {
        settingsManager.setChatFontSize(value)
    }

    // Privacy
    fun getLastSeenVisibility(): String = settingsManager.getLastSeenVisibility()
    fun setLastSeenVisibility(value: String) = settingsManager.setLastSeenVisibility(value)

    fun getReadReceiptsEnabled(): Boolean = settingsManager.getReadReceiptsEnabled()
    fun setReadReceiptsEnabled(value: Boolean) = settingsManager.setReadReceiptsEnabled(value)

    fun getProfilePhotoVisibility(): String = settingsManager.getProfilePhotoVisibility()
    fun setProfilePhotoVisibility(value: String) = settingsManager.setProfilePhotoVisibility(value)

    fun getStatusVisibility(): String = settingsManager.getStatusVisibility()
    fun setStatusVisibility(value: String) = settingsManager.setStatusVisibility(value)

    fun setUsernameSearchEnabled(userId: String, enabled: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.updatePrivacy(userId, enabled)
            onResult(success)
        }
    }

    // Notifications
    fun getNotificationsEnabled(): Boolean = settingsManager.getNotificationsEnabled()
    fun setNotificationsEnabled(value: Boolean) = settingsManager.setNotificationsEnabled(value)

    fun getSoundEnabled(): Boolean = settingsManager.getSoundEnabled()
    fun setSoundEnabled(value: Boolean) = settingsManager.setSoundEnabled(value)

    fun getVibrationEnabled(): Boolean = settingsManager.getVibrationEnabled()
    fun setVibrationEnabled(value: Boolean) = settingsManager.setVibrationEnabled(value)

    fun getNotificationPreviewEnabled(): Boolean = settingsManager.getNotificationPreviewEnabled()
    fun setNotificationPreviewEnabled(value: Boolean) = settingsManager.setNotificationPreviewEnabled(value)

    // Chats
    fun getEnterToSend(): Boolean = settingsManager.getEnterToSend()
    fun setEnterToSend(value: Boolean) = settingsManager.setEnterToSend(value)

    // Storage
    fun getAutoDownloadWifi(): String = settingsManager.getAutoDownloadWifi()
    fun setAutoDownloadWifi(value: String) = settingsManager.setAutoDownloadWifi(value)

    fun getAutoDownloadMobile(): String = settingsManager.getAutoDownloadMobile()
    fun setAutoDownloadMobile(value: String) = settingsManager.setAutoDownloadMobile(value)

    // Account verification
    fun getTwoStepVerificationEnabled(): Boolean = settingsManager.getTwoStepVerificationEnabled()
    fun setTwoStepVerificationEnabled(value: Boolean) = settingsManager.setTwoStepVerificationEnabled(value)

    fun clearCache(onDone: () -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            onDone()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteAccount()
            _deleteSuccess.value = true
        }
    }

    fun resetDeleteState() {
        _deleteSuccess.value = false
    }
}
