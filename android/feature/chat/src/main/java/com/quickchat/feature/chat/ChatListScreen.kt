package com.quickchat.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.Chat
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyDoodle
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder
import java.text.SimpleDateFormat
import java.util.*

// Simple Helper import to override mutableStateOf
import androidx.compose.runtime.mutableStateOf as mutableStateFlowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onNavigateToChat: (phone: String) -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val chats by viewModel.chats.collectAsState()
    val typingStates by viewModel.typingStates.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showStartChatDialog by remember { mutableStateFlowOf(false) }
    var showMenu by remember { mutableStateFlowOf(false) }
    
    val colors = LocalSketchyColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Quick Chat",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        currentUser?.let {
                            Text("My Phone: ${it.phone}", fontSize = 11.sp, color = colors.text.copy(alpha = 0.6f))
                        }
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 4.dp)) {
                        UserAvatar(
                            avatarUrl = currentUser?.avatarUrl,
                            displayName = currentUser?.displayName ?: "Me",
                            size = 36.dp,
                            onClick = onNavigateToProfile
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToStatus() }) {
                        Text("STATUS", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = colors.text)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(colors.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile", color = colors.text) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings", color = colors.text) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout", color = colors.text) },
                                onClick = {
                                    showMenu = false
                                    viewModel.logout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.text,
                    navigationIconContentColor = colors.text
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showStartChatDialog = true },
                containerColor = colors.accent,
                contentColor = Color.White,
                modifier = Modifier.sketchyBorder(1.dp, colors.text, 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Start Chat")
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            if (chats.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SketchyDoodle(name = "empty_chat", color = colors.text.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No chats yet.",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.text
                    )
                    Text(
                        "Tap + to start messaging!",
                        color = colors.text.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chats, key = { it.recipientPhone }) { chat ->
                        val isTyping = typingStates[chat.recipientPhone] ?: false
                        val lastMessageText = when {
                            isTyping -> "typing..."
                            chat.lastMessage != null -> chat.lastMessage!!.plainText ?: "[Encrypted Media]"
                            else -> "No messages yet"
                        }
                        
                        ChatRow(
                            chat = chat,
                            lastMessageText = lastMessageText,
                            isTyping = isTyping,
                            onClick = { onNavigateToChat(chat.recipientPhone) }
                        )
                        SketchyDivider()
                    }
                }
            }

            if (showStartChatDialog) {
                StartChatDialog(
                    onDismiss = { showStartChatDialog = false },
                    onConfirm = { phone, name ->
                        showStartChatDialog = false
                        viewModel.startChatWithContact(phone, name)
                        onNavigateToChat(phone)
                    },
                    onSearchUsername = { query, callback ->
                        viewModel.searchUserByUsername(query, callback)
                    }
                )
            }
        }
    }
}

@Composable
fun ChatRow(
    chat: Chat,
    lastMessageText: String,
    isTyping: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalSketchyColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            avatarUrl = chat.avatarUrl,
            displayName = chat.displayName,
            size = 52.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.displayName,
                    color = colors.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val timeStr = chat.lastMessage?.let {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    sdf.format(Date(it.timestamp))
                } ?: ""
                Text(
                    text = timeStr,
                    color = if (chat.unreadCount > 0) colors.accent else colors.text.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastMessageText,
                    color = when {
                        isTyping -> colors.accent
                        chat.unreadCount > 0 -> colors.text
                        else -> colors.text.copy(alpha = 0.6f)
                    },
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isMuted) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Muted",
                            tint = colors.text.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                    }
                    if (chat.isPinned) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = "Pinned",
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                    }
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                                .sketchyBorder(0.5.dp, colors.text, 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StartChatDialog(
    onDismiss: () -> Unit,
    onConfirm: (phone: String, name: String) -> Unit,
    onSearchUsername: (String, (com.quickchat.core.model.User?, String?) -> Unit) -> Unit
) {
    var activeTab by remember { mutableStateFlowOf("phone") } // "phone" or "username"
    var phone by remember { mutableStateFlowOf("") }
    var name by remember { mutableStateFlowOf("") }
    
    var usernameQuery by remember { mutableStateFlowOf("") }
    var searchLoading by remember { mutableStateFlowOf(false) }
    var searchError by remember { mutableStateFlowOf<String?>(null) }
    var foundUser by remember { mutableStateFlowOf<com.quickchat.core.model.User?>(null) }

    val colors = LocalSketchyColors.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Start New Chat",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    fontSize = 20.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "By Phone",
                        fontSize = 14.sp,
                        fontWeight = if (activeTab == "phone") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == "phone") colors.accent else colors.text.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { activeTab = "phone" }
                            .padding(vertical = 4.dp)
                    )
                    Text(
                        text = "By Username",
                        fontSize = 14.sp,
                        fontWeight = if (activeTab == "username") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == "username") colors.accent else colors.text.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { activeTab = "username" }
                            .padding(vertical = 4.dp)
                    )
                }
                SketchyDivider()
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (activeTab == "phone") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedLabelColor = colors.accent,
                            unfocusedLabelColor = colors.text.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedLabelColor = colors.accent,
                            unfocusedLabelColor = colors.text.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = usernameQuery,
                            onValueChange = { usernameQuery = it.lowercase().trim() },
                            placeholder = { Text("Enter username...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text,
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.text.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Button(
                            onClick = {
                                if (usernameQuery.isNotBlank()) {
                                    searchLoading = true
                                    searchError = null
                                    foundUser = null
                                    onSearchUsername(usernameQuery) { user, err ->
                                        searchLoading = false
                                        if (err != null) {
                                            searchError = err
                                        } else {
                                            foundUser = user
                                        }
                                    }
                                }
                            },
                            enabled = !searchLoading && usernameQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            if (searchLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Search", color = Color.White)
                            }
                        }
                    }

                    if (searchError != null) {
                        Text(
                            text = searchError!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    foundUser?.let { user ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sketchyBorder(1.dp, colors.text, 12.dp)
                                .background(colors.background)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    avatarUrl = user.avatarUrl,
                                    displayName = user.displayName,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.text,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "@${user.username}",
                                        fontSize = 12.sp,
                                        color = colors.accent
                                    )
                                    if (!user.about.isNullOrEmpty()) {
                                        Text(
                                            text = user.about ?: "",
                                            fontSize = 12.sp,
                                            color = colors.text.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (activeTab == "phone") {
                TextButton(
                    onClick = {
                        if (phone.isNotBlank() && name.isNotBlank()) {
                            onConfirm(phone, name)
                        }
                    }
                ) {
                    Text("Start Chat", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            } else {
                foundUser?.let { user ->
                    TextButton(
                        onClick = {
                            onConfirm(user.phone, user.displayName)
                        }
                    ) {
                        Text("Start Chat", color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", color = colors.text)
            }
        },
        containerColor = colors.surface
    )
}
