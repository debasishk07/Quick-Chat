package com.quickchat.feature.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import com.quickchat.core.model.PhoneContact
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
    onNavigateToChat: (phone: String, highlightMessageId: String?) -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val chats by viewModel.chats.collectAsState()
    val typingStates by viewModel.typingStates.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showStartChatDialog by remember { mutableStateFlowOf(false) }
    var showMenu by remember { mutableStateFlowOf(false) }

    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResultsChats by viewModel.searchResultsChats.collectAsState()
    val searchResultsContacts by viewModel.searchResultsContacts.collectAsState()
    val searchResultsMessages by viewModel.searchResultsMessages.collectAsState()
    
    val colors = LocalSketchyColors.current

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                },
                label = "SearchAppBarTransition"
            ) { active ->
                if (active) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(64.dp),
                        color = colors.background
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            IconButton(onClick = { viewModel.setSearchActive(false) }) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search chats, contacts, messages...", color = colors.text.copy(alpha = 0.5f)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accent,
                                    unfocusedBorderColor = colors.text.copy(alpha = 0.3f),
                                    cursorColor = colors.accent,
                                    focusedLabelColor = colors.accent,
                                    unfocusedLabelColor = colors.text.copy(alpha = 0.5f)
                                ),
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.text)
                                        }
                                    }
                                }
                            )
                        }
                    }
                } else {
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
                            IconButton(onClick = { viewModel.setSearchActive(true) }) {
                                Icon(Icons.Outlined.Search, contentDescription = "Search", tint = colors.text)
                            }
                            IconButton(onClick = { onNavigateToStatus() }) {
                                Image(
                                    painter = painterResource(id = R.drawable.status),
                                    contentDescription = "Status",
                                    modifier = Modifier.size(24.dp)
                                )
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
                }
            }
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
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "SearchBodyTransition"
            ) { active ->
                if (active) {
                    SearchResultsView(
                        query = searchQuery,
                        chats = searchResultsChats,
                        contacts = searchResultsContacts,
                        messages = searchResultsMessages,
                        onChatSelected = { phone ->
                            onNavigateToChat(phone, null)
                        },
                        onMessageSelected = { phone, msgId ->
                            onNavigateToChat(phone, msgId)
                        },
                        onStartNewChat = { phone, name ->
                            viewModel.startChatWithContact(phone, name)
                            onNavigateToChat(phone, null)
                        }
                    )
                } else {
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
                                    onClick = { onNavigateToChat(chat.recipientPhone, null) }
                                )
                                SketchyDivider()
                            }
                        }
                    }
                }
            }

            if (showStartChatDialog) {
                StartChatDialog(
                    onDismiss = { showStartChatDialog = false },
                    onConfirm = { phone, name ->
                        showStartChatDialog = false
                        viewModel.startChatWithContact(phone, name)
                        onNavigateToChat(phone, null)
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
        if (!chat.isProfileLoaded) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.text.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp)
                        .background(colors.text.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .background(colors.text.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                )
            }
        } else {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = chat.displayName,
                            color = colors.text,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (chat.disappearingDuration > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Disappearing messages active",
                                tint = colors.accent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    
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

@Composable
fun SearchResultsView(
    query: String,
    chats: List<Chat>,
    contacts: List<PhoneContact>,
    messages: List<MessageSearchResult>,
    onChatSelected: (phone: String) -> Unit,
    onMessageSelected: (phone: String, messageId: String) -> Unit,
    onStartNewChat: (phone: String, name: String) -> Unit
) {
    val colors = LocalSketchyColors.current

    if (query.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = colors.text.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search chats, contacts, or messages",
                    color = colors.text.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp
                )
            }
        }
    } else if (chats.isEmpty() && contacts.isEmpty() && messages.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SketchyDoodle(name = "empty_chat", color = colors.text.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No results for '$query'",
                    color = colors.text.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (chats.isNotEmpty()) {
                item {
                    SearchSectionHeader(title = "Chats")
                }
                items(chats) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatSelected(chat.recipientPhone) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(avatarUrl = chat.avatarUrl, displayName = chat.displayName, size = 40.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = chat.displayName,
                            color = colors.text,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    SketchyDivider()
                }
            }

            if (contacts.isNotEmpty()) {
                item {
                    SearchSectionHeader(title = "Contacts")
                }
                items(contacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartNewChat(contact.phone, contact.contactName) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(avatarUrl = null, displayName = contact.contactName, size = 40.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = contact.contactName,
                                color = colors.text,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = contact.phone,
                                color = colors.text.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }
                    SketchyDivider()
                }
            }

            if (messages.isNotEmpty()) {
                item {
                    SearchSectionHeader(title = "Messages")
                }
                items(messages) { result ->
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val timeStr = sdf.format(Date(result.message.timestamp))
                    val highlightedText = buildHighlightSnippet(
                        text = result.message.plainText ?: "",
                        query = query,
                        highlightColor = colors.accent
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMessageSelected(result.chatPhone, result.message.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Message,
                            contentDescription = null,
                            tint = colors.text.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${result.chatName} (${result.senderName})",
                                    color = colors.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = timeStr,
                                    color = colors.text.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = highlightedText,
                                color = colors.text.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    SketchyDivider()
                }
            }
        }
    }
}

@Composable
fun SearchSectionHeader(title: String) {
    val colors = LocalSketchyColors.current
    Surface(
        color = colors.border.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.accent,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

fun buildHighlightSnippet(
    text: String,
    query: String,
    highlightColor: Color
): androidx.compose.ui.text.AnnotatedString {
    val cleanQuery = query.trim()
    if (cleanQuery.isEmpty()) return androidx.compose.ui.text.AnnotatedString(text)

    val index = text.indexOf(cleanQuery, ignoreCase = true)
    if (index < 0) return androidx.compose.ui.text.AnnotatedString(text)

    val start = (index - 25).coerceAtLeast(0)
    val end = (index + cleanQuery.length + 25).coerceAtMost(text.length)

    val snippet = text.substring(start, end)
    val prefix = if (start > 0) "..." else ""
    val suffix = if (end < text.length) "..." else ""

    val finalSnippet = prefix + snippet + suffix
    val matchInSnippetIndex = finalSnippet.indexOf(cleanQuery, ignoreCase = true)

    return androidx.compose.ui.text.buildAnnotatedString {
        append(finalSnippet)
        if (matchInSnippetIndex >= 0) {
            addStyle(
                style = androidx.compose.ui.text.SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold
                ),
                start = matchInSnippetIndex,
                end = matchInSnippetIndex + cleanQuery.length
            )
        }
    }
}
