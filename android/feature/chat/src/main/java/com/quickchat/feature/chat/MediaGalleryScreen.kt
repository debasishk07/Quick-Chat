package com.quickchat.feature.chat

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.quickchat.core.model.MessageType
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.sketchyBorder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryScreen(
    viewModel: ChatRoomViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalSketchyColors.current

    val partnerName by viewModel.recipientName.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Media", "Links", "Docs")

    // 1. Extract Media (images & videos)
    val mediaItems = remember(messages) {
        messages.filter { it.messageType == MessageType.IMAGE || it.messageType == MessageType.VIDEO }
    }

    // 2. Extract Links (HTTP/HTTPS URLs in text messages)
    val urlPattern = Pattern.compile(
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
    )
    val linkItems = remember(messages) {
        messages.filter { it.messageType == MessageType.TEXT && it.plainText != null }
            .flatMap { msg ->
                val text = msg.plainText ?: ""
                val matcher = urlPattern.matcher(text)
                val urls = mutableListOf<Pair<String, Long>>()
                while (matcher.find()) {
                    urls.add(matcher.group() to msg.timestamp)
                }
                urls
            }
    }

    // 3. Extract Documents
    val docItems = remember(messages) {
        messages.filter { it.messageType == MessageType.DOCUMENT }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$partnerName - Media", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.text
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Header Selection Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.background,
                contentColor = colors.accent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = colors.accent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) },
                        selectedContentColor = colors.accent,
                        unselectedContentColor = colors.text.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Media tab Grid
                        if (mediaItems.isEmpty()) {
                            EmptyPlaceholder(text = "No shared media files")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(mediaItems) { item ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.border.copy(alpha = 0.1f))
                                            .sketchyBorder(1.dp, colors.text, 8.dp)
                                            .clickable {
                                                Toast.makeText(context, "Opening shared media", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        AsyncImage(
                                            model = item.plainText?.substringBefore("#"),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Links tab List
                        if (linkItems.isEmpty()) {
                            EmptyPlaceholder(text = "No shared links")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(linkItems) { (url, timestamp) ->
                                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Outlined.Link, contentDescription = "Link", tint = colors.accent)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = url,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.accent,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "Sent on ${sdf.format(Date(timestamp))}",
                                                    fontSize = 11.sp,
                                                    color = colors.text.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Docs tab List
                        if (docItems.isEmpty()) {
                            EmptyPlaceholder(text = "No shared documents")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(docItems) { item ->
                                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                Toast.makeText(context, "Opening document", Toast.LENGTH_SHORT).show()
                                            },
                                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Outlined.Description, contentDescription = "Doc", tint = colors.text)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.plainText ?: "document.pdf",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.text,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "Sent on ${sdf.format(Date(item.timestamp))}",
                                                    fontSize = 11.sp,
                                                    color = colors.text.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPlaceholder(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = LocalSketchyColors.current.text.copy(alpha = 0.4f),
            fontFamily = FontFamily.Serif
        )
    }
}
