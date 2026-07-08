package com.quickchat.core.network.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    // Theme Preference: "light", "dark", "system"
    private val _theme = MutableStateFlow(prefs.getString(KEY_THEME, "system") ?: "system")
    val theme: StateFlow<String> = _theme.asStateFlow()

    fun setTheme(themeValue: String) {
        prefs.edit().putString(KEY_THEME, themeValue).apply()
        _theme.value = themeValue
    }

    // Privacy Preferences
    fun getLastSeenVisibility(): String = prefs.getString(KEY_PRIVACY_LAST_SEEN, "everyone") ?: "everyone"
    fun setLastSeenVisibility(value: String) = prefs.edit().putString(KEY_PRIVACY_LAST_SEEN, value).apply()

    fun getReadReceiptsEnabled(): Boolean = prefs.getBoolean(KEY_PRIVACY_READ_RECEIPTS, true)
    fun setReadReceiptsEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_PRIVACY_READ_RECEIPTS, value).apply()

    fun getProfilePhotoVisibility(): String = prefs.getString(KEY_PRIVACY_PROFILE_PHOTO, "everyone") ?: "everyone"
    fun setProfilePhotoVisibility(value: String) = prefs.edit().putString(KEY_PRIVACY_PROFILE_PHOTO, value).apply()

    fun getStatusVisibility(): String = prefs.getString(KEY_PRIVACY_STATUS, "everyone") ?: "everyone"
    fun setStatusVisibility(value: String) = prefs.edit().putString(KEY_PRIVACY_STATUS, value).apply()

    // Notification Preferences
    fun getNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_ENABLED, true)
    fun setNotificationsEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_NOTIF_ENABLED, value).apply()

    fun getSoundEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_SOUND, true)
    fun setSoundEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_NOTIF_SOUND, value).apply()

    fun getVibrationEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_VIBRATION, true)
    fun setVibrationEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_NOTIF_VIBRATION, value).apply()

    fun getNotificationPreviewEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_PREVIEW, true)
    fun setNotificationPreviewEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_NOTIF_PREVIEW, value).apply()

    // Chat Preferences
    private val _chatWallpaper = MutableStateFlow(prefs.getString(KEY_CHAT_WALLPAPER, "default") ?: "default")
    val chatWallpaper: StateFlow<String> = _chatWallpaper.asStateFlow()

    fun setChatWallpaper(value: String) {
        prefs.edit().putString(KEY_CHAT_WALLPAPER, value).apply()
        _chatWallpaper.value = value
    }

    private val _chatFontSize = MutableStateFlow(prefs.getFloat(KEY_CHAT_FONT_SIZE, 16f))
    val chatFontSize: StateFlow<Float> = _chatFontSize.asStateFlow()

    fun setChatFontSize(value: Float) {
        prefs.edit().putFloat(KEY_CHAT_FONT_SIZE, value).apply()
        _chatFontSize.value = value
    }

    fun getEnterToSend(): Boolean = prefs.getBoolean(KEY_CHAT_ENTER_TO_SEND, false)
    fun setEnterToSend(value: Boolean) = prefs.edit().putBoolean(KEY_CHAT_ENTER_TO_SEND, value).apply()

    // Storage Auto Download Preferences
    fun getAutoDownloadWifi(): String = prefs.getString(KEY_STORAGE_WIFI, "photos,audio,docs") ?: "photos,audio,docs"
    fun setAutoDownloadWifi(value: String) = prefs.edit().putString(KEY_STORAGE_WIFI, value).apply()

    fun getAutoDownloadMobile(): String = prefs.getString(KEY_STORAGE_MOBILE, "photos") ?: "photos"
    fun setAutoDownloadMobile(value: String) = prefs.edit().putString(KEY_STORAGE_MOBILE, value).apply()

    // Two step verification (Stretch)
    fun getTwoStepVerificationEnabled(): Boolean = prefs.getBoolean(KEY_ACCOUNT_2FA, false)
    fun setTwoStepVerificationEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_ACCOUNT_2FA, value).apply()

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_PRIVACY_LAST_SEEN = "pref_privacy_last_seen"
        private const val KEY_PRIVACY_READ_RECEIPTS = "pref_privacy_read_receipts"
        private const val KEY_PRIVACY_PROFILE_PHOTO = "pref_privacy_profile_photo"
        private const val KEY_PRIVACY_STATUS = "pref_privacy_status"
        private const val KEY_NOTIF_ENABLED = "pref_notif_enabled"
        private const val KEY_NOTIF_SOUND = "pref_notif_sound"
        private const val KEY_NOTIF_VIBRATION = "pref_notif_vibration"
        private const val KEY_NOTIF_PREVIEW = "pref_notif_preview"
        private const val KEY_CHAT_WALLPAPER = "pref_chat_wallpaper"
        private const val KEY_CHAT_FONT_SIZE = "pref_chat_font_size"
        private const val KEY_CHAT_ENTER_TO_SEND = "pref_chat_enter_to_send"
        private const val KEY_STORAGE_WIFI = "pref_storage_wifi"
        private const val KEY_STORAGE_MOBILE = "pref_storage_mobile"
        private const val KEY_ACCOUNT_2FA = "pref_account_2fa"
    }
}
