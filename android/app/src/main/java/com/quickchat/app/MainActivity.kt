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
import androidx.compose.runtime.remember

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
    val context = androidx.compose.ui.platform.LocalContext.current

    // Check system Accessibility reduced motion setting
    val isReducedMotion = remember {
        try {
            val scale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }
    
    // Shared ViewModels at parent activity level (to preserve states across screens)
    val loginViewModel: LoginViewModel = hiltViewModel()
    val statusViewModel: StatusViewModel = hiltViewModel()
    val chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
    val chatListViewModel: ChatListViewModel = hiltViewModel()

    val chatWallpaper by settingsViewModel.chatWallpaper.collectAsState()
    val chatFontSize by settingsViewModel.chatFontSize.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { NavTransitions.pushEnter(isReducedMotion) },
        exitTransition = { NavTransitions.pushExit(isReducedMotion) },
        popEnterTransition = { NavTransitions.pushPopEnter(isReducedMotion) },
        popExitTransition = { NavTransitions.pushPopExit(isReducedMotion) }
    ) {
        composable(
            route = "login",
            enterTransition = { NavTransitions.modalEnter(isReducedMotion) },
            exitTransition = { NavTransitions.modalExit(isReducedMotion) }
        ) {
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
        
        composable(
            route = "profile_setup",
            enterTransition = { NavTransitions.modalEnter(isReducedMotion) },
            exitTransition = { NavTransitions.modalExit(isReducedMotion) }
        ) {
            ProfileSetupScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = "home",
            enterTransition = { NavTransitions.tabEnter() },
            exitTransition = { NavTransitions.tabExit() }
        ) {
            ChatListScreen(
                viewModel = chatListViewModel,
                onNavigateToChat = { phone, highlightMessageId ->
                    chatRoomViewModel.initRecipient(phone)
                    val route = if (highlightMessageId != null) {
                        "chat/$phone?highlightMessageId=$highlightMessageId"
                    } else {
                        "chat/$phone"
                    }
                    navController.navigate(route)
                },
                onNavigateToStatus = { navController.navigate("status_list") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable(
            route = "chat/{phone}?highlightMessageId={highlightMessageId}",
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("highlightMessageId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val highlightMessageId = backStackEntry.arguments?.getString("highlightMessageId")
            ChatRoomScreen(
                viewModel = chatRoomViewModel,
                chatWallpaper = chatWallpaper,
                chatFontSize = chatFontSize,
                highlightMessageId = highlightMessageId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerify = { recipientPhone ->
                    navController.navigate("verify_security/$recipientPhone")
                },
                onNavigateToContactInfo = { recipientPhone ->
                    navController.navigate("contact_info/$recipientPhone")
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

        composable(
            route = "status_list",
            enterTransition = { NavTransitions.tabEnter() },
            exitTransition = { NavTransitions.tabExit() }
        ) {
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
            route = "status_create",
            enterTransition = { NavTransitions.modalEnter(isReducedMotion) },
            exitTransition = { NavTransitions.modalExit(isReducedMotion) }
        ) {
            StatusCreatorScreen(
                viewModel = statusViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "status_view/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
            enterTransition = { NavTransitions.modalEnter(isReducedMotion) },
            exitTransition = { NavTransitions.modalExit(isReducedMotion) }
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            StatusViewerScreen(
                viewModel = statusViewModel,
                phone = phone,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "profile",
            enterTransition = { NavTransitions.tabEnter() },
            exitTransition = { NavTransitions.tabExit() }
        ) {
            ProfileScreen(
                viewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "settings",
            enterTransition = { NavTransitions.tabEnter() },
            exitTransition = { NavTransitions.tabExit() }
        ) {
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

        composable(
            route = "contact_info/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            ContactInfoScreen(
                viewModel = chatRoomViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerify = { phone -> navController.navigate("verify_security/$phone") },
                onNavigateToMediaGallery = { phone -> navController.navigate("media_gallery/$phone") }
            )
        }

        composable(
            route = "media_gallery/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            MediaGalleryScreen(
                viewModel = chatRoomViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
