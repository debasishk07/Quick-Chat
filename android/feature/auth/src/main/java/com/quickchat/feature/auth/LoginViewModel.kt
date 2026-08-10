package com.quickchat.feature.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickchat.core.network.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginFlowState { CHOICE, PHONE_OTP, GOOGLE_LINK_PHONE }

@HiltViewModel
class LoginViewModel @Inject constructor(
    val userRepository: UserRepository
) : ViewModel() {

    private val _loginFlowState = MutableStateFlow(LoginFlowState.CHOICE)
    val loginFlowState: StateFlow<LoginFlowState> = _loginFlowState.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Google Login post-linking phone states
    private val _linkPhone = MutableStateFlow("")
    val linkPhone: StateFlow<String> = _linkPhone.asStateFlow()

    private val _linkPhoneCode = MutableStateFlow("")
    val linkPhoneCode: StateFlow<String> = _linkPhoneCode.asStateFlow()

    private val _linkPhoneCodeSent = MutableStateFlow(false)
    val linkPhoneCodeSent: StateFlow<Boolean> = _linkPhoneCodeSent.asStateFlow()

    private val _googleLinkPhoneRequired = MutableStateFlow(false)
    val googleLinkPhoneRequired: StateFlow<Boolean> = _googleLinkPhoneRequired.asStateFlow()

    private val _showMockGoogleChooser = MutableStateFlow(false)
    val showMockGoogleChooser: StateFlow<Boolean> = _showMockGoogleChooser.asStateFlow()

    private val _tempGoogleUid = MutableStateFlow("")
    private val _tempEmail = MutableStateFlow("")

    // Profile registration states
    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _about = MutableStateFlow("Hey there! I am using Quick Chat.")
    val about: StateFlow<String> = _about.asStateFlow()

    // Username setup onboarding state
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    fun setLoginFlowState(state: LoginFlowState) {
        _loginFlowState.value = state
        _error.value = null
    }

    fun updatePhone(newPhone: String) {
        _phone.value = newPhone
    }

    fun updateOtp(newOtp: String) {
        _otp.value = newOtp
    }

    fun updateLinkPhone(newPhone: String) {
        _linkPhone.value = newPhone
    }

    fun updateLinkPhoneCode(newCode: String) {
        _linkPhoneCode.value = newCode
    }

    fun updateDisplayName(name: String) {
        _displayName.value = name
    }

    fun updateAbout(text: String) {
        _about.value = text
    }

    fun updateUsername(name: String) {
        _username.value = name.lowercase().trim()
        validateUsernameLocal(_username.value)
    }

    private fun validateUsernameLocal(name: String) {
        if (name.isEmpty()) {
            _usernameError.value = null
            return
        }
        val regex = "^[a-z][a-z0-9_]{2,19}$".toRegex()
        if (!regex.matches(name)) {
            _usernameError.value = "3-20 characters, must start with letter, lowercase/numbers/underscores only"
            return
        }
        val reserved = listOf("admin", "administrator", "support", "root", "quickchat", "moderator", "help", "security", "system")
        if (reserved.contains(name)) {
            _usernameError.value = "This username is reserved"
            return
        }
        _usernameError.value = null
    }

    fun sendOtp() {
        if (_phone.value.isBlank() || _phone.value.length < 10) {
            _error.value = "Please enter a valid phone number"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            // Simulate OTP sending locally
            kotlinx.coroutines.delay(1000)
            _otpSent.value = true
            _loading.value = false
        }
    }

    fun verifyOtp(onSuccess: (isNewUser: Boolean) -> Unit) {
        if (_otp.value.length != 6) {
            _error.value = "OTP must be exactly 6 digits"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val cleanDigits = _phone.value.replace(Regex("[^0-9]"), "")
            val mockIdToken = "mock:user_$cleanDigits:null:${_phone.value}"
            val isVerified = userRepository.verifyFirebaseToken(
                idToken = mockIdToken,
                provider = "phone",
                phone = _phone.value
            )
            _loading.value = false
            
            if (isVerified) {
                val existingUser = userRepository.currentUser.value
                val needsUsernameSetup = existingUser == null || existingUser.username.isNullOrEmpty()
                onSuccess(needsUsernameSetup)
            } else {
                _error.value = "Incorrect OTP. Try again."
            }
        }
    }

    private fun initializeFirebaseApp(context: Context) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApiKey("AIzaSyQuickChatMockApiKey2026SecureKey")
                    .setApplicationId("1:123456789012:android:abcdef1234567890")
                    .setProjectId("quick-chat-app")
                    .setGcmSenderId("123456789012")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(context, options)
            }
        } catch (e: Exception) {
            Log.w("LoginViewModel", "FirebaseApp init failed: ${e.message}")
        }
    }

    fun signInWithGoogle(context: Context, onComplete: (isNewUser: Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                initializeFirebaseApp(context)
                val credentialManager = androidx.credentials.CredentialManager.create(context)
                
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setServerClientId("mock-client-id-quickchat")
                    .build()

                val request = androidx.credentials.GetCredentialRequest(listOf(googleIdOption))
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: email.split("@")[0]
                    val avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()
                    val googleUid = "google_" + email.replace(".", "_")

                    val mockToken = "mock:$googleUid:$email:null"
                    val success = userRepository.verifyFirebaseToken(
                        idToken = googleIdTokenCredential.idToken.ifBlank { mockToken },
                        provider = "google",
                        email = email,
                        displayName = displayName,
                        avatarUrl = avatarUrl
                    )

                    _loading.value = false
                    if (success) {
                        val user = userRepository.currentUser.value
                        val needsPhoneLink = user?.phoneNumber.isNullOrEmpty()
                        if (needsPhoneLink) {
                            _tempGoogleUid.value = googleUid
                            _tempEmail.value = email
                            _googleLinkPhoneRequired.value = true
                            _loginFlowState.value = LoginFlowState.GOOGLE_LINK_PHONE
                        } else {
                            onComplete(user?.username.isNullOrEmpty())
                        }
                    } else {
                        _error.value = "Google Login failed to register on server."
                    }
                } else {
                    _loading.value = false
                    _error.value = "Unsupported credential format."
                }
            } catch (e: Exception) {
                Log.w("LoginViewModel", "Credential Manager exception: ${e.message}. Using mock provider.")
                _showMockGoogleChooser.value = true
                _loading.value = false
            }
        }
    }

    fun signInWithGoogleMock(email: String, name: String, avatar: String?, onComplete: (isNewUser: Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _showMockGoogleChooser.value = false
            val googleUid = "google_" + email.replace(".", "_")
            val mockToken = "mock:$googleUid:$email:null"
            val success = userRepository.verifyFirebaseToken(
                idToken = mockToken,
                provider = "google",
                email = email,
                displayName = name,
                avatarUrl = avatar
            )
            _loading.value = false
            if (success) {
                val user = userRepository.currentUser.value
                val needsPhoneLink = user?.phoneNumber.isNullOrEmpty()
                if (needsPhoneLink) {
                    _tempGoogleUid.value = googleUid
                    _tempEmail.value = email
                    _googleLinkPhoneRequired.value = true
                    _loginFlowState.value = LoginFlowState.GOOGLE_LINK_PHONE
                } else {
                    onComplete(user?.username.isNullOrEmpty())
                }
            } else {
                _error.value = "Mock Google login failed"
            }
        }
    }

    fun dismissMockGoogleChooser() {
        _showMockGoogleChooser.value = false
    }

    fun sendLinkPhoneOtp() {
        if (_linkPhone.value.isBlank() || _linkPhone.value.length < 10) {
            _error.value = "Please enter a valid phone number"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            kotlinx.coroutines.delay(1000)
            _linkPhoneCodeSent.value = true
            _loading.value = false
        }
    }

    fun verifyLinkPhoneOtp(onComplete: (isNewUser: Boolean) -> Unit) {
        if (_linkPhoneCode.value.length != 6) {
            _error.value = "Code must be exactly 6 digits"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val success = userRepository.linkPhone(
                userId = _tempGoogleUid.value,
                phone = _linkPhone.value,
                code = _linkPhoneCode.value
            )
            _loading.value = false
            if (success) {
                _googleLinkPhoneRequired.value = false
                val user = userRepository.currentUser.value
                onComplete(user?.username.isNullOrEmpty())
            } else {
                _error.value = "Verification failed or phone already linked"
            }
        }
    }

    fun skipLinkPhone(onComplete: (isNewUser: Boolean) -> Unit) {
        _googleLinkPhoneRequired.value = false
        val user = userRepository.currentUser.value
        onComplete(user?.username.isNullOrEmpty())
    }

    fun completeProfile(onSuccess: () -> Unit) {
        val user = userRepository.currentUser.value
        if (_displayName.value.isBlank()) {
            _error.value = "Display Name cannot be empty"
            return
        }

        val name = _username.value
        if (name.isNotEmpty() && _usernameError.value != null) {
            _error.value = "Please resolve username formatting errors"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val targetPhone = user?.phone ?: _phone.value
            val success = userRepository.registerProfile(
                phone = targetPhone,
                displayName = _displayName.value,
                avatarUrl = user?.avatarUrl,
                about = _about.value
            )
            
            if (success) {
                // Save username if set
                if (name.isNotEmpty()) {
                    val usernameError = userRepository.updateUsername(targetPhone, name)
                    if (usernameError != null) {
                        _error.value = usernameError
                        _loading.value = false
                        return@launch
                    }
                } else {
                    // Auto-assign skippable username
                    val randomSuffix = (1000..9999).random()
                    val autoUsername = "user_${_displayName.value.replace(" ", "").lowercase()}_$randomSuffix".take(20)
                    userRepository.updateUsername(targetPhone, autoUsername)
                }
                
                _loading.value = false
                onSuccess()
            } else {
                _loading.value = false
                _error.value = "Profile setup failed. Try again."
            }
        }
    }

    fun updateProfileAvatar(mediaBytes: ByteArray, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val user = userRepository.currentUser.value
            if (user != null) {
                val fileUrl = userRepository.uploadAvatar(mediaBytes)
                if (fileUrl != null) {
                    val success = userRepository.registerProfile(
                        phone = user.phone,
                        displayName = user.displayName,
                        avatarUrl = fileUrl,
                        about = user.about
                    )
                    onComplete(success)
                } else {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
            _loading.value = false
        }
    }

    fun updateProfileDetails(displayName: String, about: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val user = userRepository.currentUser.value
            if (user != null) {
                val success = userRepository.registerProfile(
                    phone = user.phone,
                    displayName = displayName,
                    avatarUrl = user.avatarUrl,
                    about = about
                )
                onComplete(success)
            } else {
                onComplete(false)
            }
            _loading.value = false
        }
    }

    fun resetStates() {
        _phone.value = ""
        _otp.value = ""
        _otpSent.value = false
        _linkPhone.value = ""
        _linkPhoneCode.value = ""
        _linkPhoneCodeSent.value = false
        _googleLinkPhoneRequired.value = false
        _showMockGoogleChooser.value = false
        _loginFlowState.value = LoginFlowState.CHOICE
        _error.value = null
        _username.value = ""
        _usernameError.value = null
    }

    val currentUser = userRepository.currentUser

    fun logout() {
        userRepository.logout()
    }
}
