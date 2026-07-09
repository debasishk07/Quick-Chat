package com.quickchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quickchat.core.network.repository.UserRepository
import com.quickchat.feature.auth.LoginScreen
import com.quickchat.feature.auth.LoginViewModel
import com.quickchat.feature.auth.ProfileSetupScreen
import com.quickchat.feature.auth.ProfileScreen
import com.quickchat.feature.auth.SettingsScreen
import com.quickchat.feature.auth.SettingsViewModel
import com.quickchat.core.model.theme.SketchyTheme
import com.quickchat.feature.chat.*
import com.quickchat.feature.status.StatusCreatorScreen
import com.quickchat.feature.status.StatusListScreen
import com.quickchat.feature.status.StatusViewModel
import com.quickchat.feature.status.StatusViewerScreen
import com.quickchat.app.call.WebRtcCallManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var callManager: WebRtcCallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themePreference by settingsViewModel.theme.collectAsState("system")
            val isDark = when(themePreference) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            SketchyTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLoggedIn = userRepository.currentUser.value != null
                    val startDestination = if (isLoggedIn) "home" else "login"
                    
                    QuickChatNavHost(
                        startDestination = startDestination,
                        settingsViewModel = settingsViewModel,
                        callManager = callManager
                    )
                }
            }
        }
    }
}

@Composable
fun QuickChatNavHost(startDestination: String, settingsViewModel: SettingsViewModel, callManager: WebRtcCallManager) {
    val navController = rememberNavController()
    
    // Shared ViewModels at parent activity level (to preserve states across screens)
    val loginViewModel: LoginViewModel = hiltViewModel()
    val statusViewModel: StatusViewModel = hiltViewModel()
    val chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
    val chatListViewModel: ChatListViewModel = hiltViewModel()

    val chatWallpaper by settingsViewModel.chatWallpaper.collectAsState()
    val chatFontSize by settingsViewModel.chatFontSize.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToProfileSetup = { navController.navigate("profile_setup") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("profile_setup") {
            ProfileSetupScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            ChatListScreen(
                viewModel = chatListViewModel,
                onNavigateToChat = { phone ->
                    chatRoomViewModel.initRecipient(phone)
                    navController.navigate("chat/$phone")
                },
                onNavigateToStatus = { navController.navigate("status_list") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable(
            route = "chat/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            ChatRoomScreen(
                viewModel = chatRoomViewModel,
                chatWallpaper = chatWallpaper,
                chatFontSize = chatFontSize,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerify = { recipientPhone ->
                    navController.navigate("verify_security/$recipientPhone")
                },
                onStartCall = { recipientPhone, isVideo ->
                    callManager.initiateCall(recipientPhone, isVideo)
                }
            )
        }

        composable(
            route = "verify_security/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            SecurityVerificationScreen(
                viewModel = chatRoomViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("status_list") {
            StatusListScreen(
                viewModel = statusViewModel,
                onNavigateToCreateTextStatus = { navController.navigate("status_create") },
                onNavigateToCreateMediaStatus = { navController.navigate("status_create") }, // Text/Media mock together
                onNavigateToViewStatus = { phone ->
                    navController.navigate("status_view/$phone")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "status_create"
        ) {
            StatusCreatorScreen(
                viewModel = statusViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "status_view/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            StatusViewerScreen(
                viewModel = statusViewModel,
                phone = phone,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                loginViewModel = loginViewModel,
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
