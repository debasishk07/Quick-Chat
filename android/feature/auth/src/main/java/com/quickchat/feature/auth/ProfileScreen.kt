package com.quickchat.feature.auth

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyCard
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder
import com.quickchat.core.model.theme.SketchyBottomSheet
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    val currentUser by viewModel.currentUser.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var nameInput by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var aboutInput by remember(currentUser) { mutableStateOf(currentUser?.about ?: "") }
    var usernameInput by remember(currentUser) { mutableStateOf(currentUser?.username ?: "") }
    
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingAbout by remember { mutableStateOf(false) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var usernameErrorMsg by remember { mutableStateOf<String?>(null) }
    
    var showFullAvatarDialog by remember { mutableStateOf(false) }
    var showImageSourceOptions by remember { mutableStateOf(false) }
    
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val colors = LocalSketchyColors.current

    // Helper function to perform local programmatic circular cropping
    fun cropToCircle(uri: Uri): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val original = android.graphics.BitmapFactory.decodeStream(inputStream) ?: return null
            val size = Math.min(original.width, original.height)
            val output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(output)
            val paint = android.graphics.Paint().apply { isAntiAlias = true }
            
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
            
            val srcRect = android.graphics.Rect(
                (original.width - size) / 2,
                (original.height - size) / 2,
                (original.width + size) / 2,
                (original.height + size) / 2
            )
            val destRect = android.graphics.Rect(0, 0, size, size)
            canvas.drawBitmap(original, srcRect, destRect, paint)
            
            val stream = java.io.ByteArrayOutputStream()
            output.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val croppedBytes = cropToCircle(it)
            if (croppedBytes != null) {
                viewModel.updateProfileAvatar(croppedBytes) { success ->
                    if (success) {
                        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { uri ->
                val croppedBytes = cropToCircle(uri)
                if (croppedBytes != null) {
                    viewModel.updateProfileAvatar(croppedBytes) { ok ->
                        if (ok) {
                            Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun launchCamera() {
        try {
            val cacheDir = context.cacheDir
            val tempFile = File.createTempFile("avatar_capture", ".jpg", cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "com.quickchat.fileprovider",
                tempFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Camera launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun validateUsername(input: String) {
        val trimmed = input.lowercase().trim()
        if (trimmed.isEmpty()) {
            usernameErrorMsg = "Username cannot be empty"
            return
        }
        val regex = "^[a-z][a-z0-9_]{2,19}$".toRegex()
        if (!regex.matches(trimmed)) {
            usernameErrorMsg = "3-20 chars, must start with letter, lowercase/numbers/underscores only"
            return
        }
        val reserved = listOf("admin", "administrator", "support", "root", "quickchat", "moderator", "help", "security", "system")
        if (reserved.contains(trimmed)) {
            usernameErrorMsg = "This username is reserved"
            return
        }
        usernameErrorMsg = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.text,
                    navigationIconContentColor = colors.text
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Interactive Profile Photo
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .sketchyBorder(width = 2.dp, color = colors.accent, cornerRadius = 75.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatar(
                        avatarUrl = currentUser?.avatarUrl,
                        displayName = currentUser?.displayName ?: "Me",
                        size = 142.dp,
                        onClick = { showFullAvatarDialog = true }
                    )

                    // Edit camera badge overlay
                    IconButton(
                        onClick = { showImageSourceOptions = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .sketchyBorder(1.dp, colors.text, 18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                SketchyDivider()
                Spacer(modifier = Modifier.height(24.dp))

                // 2. Display Name Card
                SketchyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Display Name",
                                fontSize = 12.sp,
                                color = colors.text.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isEditingName) {
                                TextField(
                                    value = nameInput,
                                    onValueChange = { if (it.length <= 20) nameInput = it },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = colors.text,
                                        unfocusedTextColor = colors.text
                                    )
                                )
                            } else {
                                Text(
                                    currentUser?.displayName ?: "",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isEditingName) {
                                    if (nameInput.isNotBlank()) {
                                        viewModel.updateProfileDetails(nameInput, aboutInput) {
                                            isEditingName = false
                                        }
                                    } else {
                                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isEditingName = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Name",
                                tint = colors.accent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Unique Username Card
                SketchyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Unique Username",
                                fontSize = 12.sp,
                                color = colors.text.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isEditingUsername) {
                                TextField(
                                    value = usernameInput,
                                    onValueChange = {
                                        usernameInput = it.lowercase().trim()
                                        validateUsername(usernameInput)
                                    },
                                    singleLine = true,
                                    supportingText = {
                                        if (usernameErrorMsg != null) {
                                            Text(usernameErrorMsg!!, color = Color(0xFFD32F2F))
                                        }
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = colors.text,
                                        unfocusedTextColor = colors.text
                                    )
                                )
                            } else {
                                Text(
                                    text = if (currentUser?.username.isNullOrEmpty()) "Not set" else "@${currentUser?.username}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isEditingUsername) {
                                    validateUsername(usernameInput)
                                    if (usernameErrorMsg == null) {
                                        coroutineScope.launch {
                                            val error = viewModel.userRepository.updateUsername(currentUser?.phone ?: "", usernameInput)
                                            if (error != null) {
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            } else {
                                                isEditingUsername = false
                                                Toast.makeText(context, "Username updated", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, usernameErrorMsg!!, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isEditingUsername = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Username",
                                tint = colors.accent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. About / Bio Card
                SketchyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "About",
                                fontSize = 12.sp,
                                color = colors.text.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isEditingAbout) {
                                TextField(
                                    value = aboutInput,
                                    onValueChange = { if (it.length <= 100) aboutInput = it },
                                    singleLine = false,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = colors.text,
                                        unfocusedTextColor = colors.text
                                    )
                                )
                            } else {
                                Text(
                                    currentUser?.about ?: "",
                                    fontSize = 16.sp,
                                    color = colors.text
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isEditingAbout) {
                                    viewModel.updateProfileDetails(nameInput, aboutInput) {
                                        isEditingAbout = false
                                    }
                                } else {
                                    isEditingAbout = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit About",
                                tint = colors.accent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Phone Card (Read-only)
                SketchyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            "Phone Number",
                            fontSize = 12.sp,
                            color = colors.text.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            currentUser?.phoneNumber ?: "Not linked",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text.copy(alpha = 0.7f)
                        )
                    }
                }

                if (!currentUser?.email.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SketchyCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Email Address",
                                fontSize = 12.sp,
                                color = colors.text.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                currentUser?.email ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.accent)
                }
            }

            // Image Picker Bottom Sheet/Dialog
            if (showImageSourceOptions) {
                SketchyBottomSheet(
                    onDismissRequest = { showImageSourceOptions = false }
                ) {
                    Text(
                        text = "Select Profile Photo",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.text,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Column {
                        ListItem(
                            headlineContent = { Text("Take Photo (Camera)", color = colors.text) },
                            colors = ListItemDefaults.colors(containerColor = colors.background),
                            modifier = Modifier.clickable {
                                showImageSourceOptions = false
                                launchCamera()
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Choose Existing (Gallery)", color = colors.text) },
                            colors = ListItemDefaults.colors(containerColor = colors.background),
                            modifier = Modifier.clickable {
                                showImageSourceOptions = false
                                galleryLauncher.launch("image/*")
                            }
                        )
                    }
                }
            }

            // Fullscreen Avatar View Dialog
            if (showFullAvatarDialog) {
                AlertDialog(
                    onDismissRequest = { showFullAvatarDialog = false },
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .sketchyBorder(2.dp, colors.accent, 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatar(
                                avatarUrl = currentUser?.avatarUrl,
                                displayName = currentUser?.displayName ?: "Me",
                                size = 260.dp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFullAvatarDialog = false }) {
                            Text("Close", color = colors.text)
                        }
                    },
                    containerColor = colors.background
                )
            }
        }
    }
}
