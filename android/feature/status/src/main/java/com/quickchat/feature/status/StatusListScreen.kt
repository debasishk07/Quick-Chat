package com.quickchat.feature.status

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.UserStatus
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyDivider
import com.quickchat.core.model.theme.SketchyDoodle
import com.quickchat.core.model.theme.UserAvatar
import com.quickchat.core.model.theme.sketchyBorder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusListScreen(
    viewModel: StatusViewModel,
    onNavigateToCreateTextStatus: () -> Unit,
    onNavigateToCreateMediaStatus: () -> Unit,
    onNavigateToViewStatus: (phone: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val statusFeeds by viewModel.statusFeeds.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val myFeed = statusFeeds.find { it.user.phone == currentUser?.phone }
    val contactFeeds = statusFeeds.filter { it.user.phone != currentUser?.phone }
    val colors = LocalSketchyColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                title = { Text("Status Updates", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.text) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = onNavigateToCreateTextStatus,
                    containerColor = colors.surface,
                    contentColor = colors.text,
                    modifier = Modifier.size(48.dp).sketchyBorder(1.dp, colors.text, 24.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Write text status", modifier = Modifier.size(20.dp))
                }

                FloatingActionButton(
                    onClick = onNavigateToCreateMediaStatus,
                    containerColor = colors.accent,
                    contentColor = Color.White,
                    modifier = Modifier.sketchyBorder(1.dp, colors.text, 28.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Capture status")
                }
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. My Status Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (myFeed != null) onNavigateToViewStatus(currentUser!!.phone)
                            else onNavigateToCreateTextStatus()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (myFeed != null && myFeed.statuses.isNotEmpty() && currentUser != null) {
                            StatusDashedBorderAvatar(currentUser!!, myFeed.statuses.size)
                        } else if (currentUser != null) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                UserAvatar(
                                    avatarUrl = currentUser!!.avatarUrl,
                                    displayName = currentUser!!.displayName,
                                    size = 56.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent)
                                        .sketchyBorder(1.dp, colors.text, 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text("My Status", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                        val subtitleText = if (myFeed != null && myFeed.statuses.isNotEmpty()) {
                            val time = myFeed.statuses.last().timestamp
                            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            "Last updated at ${sdf.format(Date(time))}"
                        } else {
                            "Tap to add status update"
                        }
                        Text(subtitleText, color = colors.text.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }

            item {
                SketchyDivider()
            }

            // 2. Recent Updates Section Header
            if (contactFeeds.isNotEmpty()) {
                item {
                    Text("Recent Updates", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, color = colors.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                items(contactFeeds, key = { it.user.phone }) { feed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToViewStatus(feed.user.phone) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusDashedBorderAvatar(feed.user, feed.statuses.size)

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(feed.user.displayName, color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            val time = feed.statuses.last().timestamp
                            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            Text(sdf.format(Date(time)), color = colors.text.copy(alpha = 0.5f), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SketchyDoodle(name = "empty_status", color = colors.text.copy(alpha = 0.6f), modifier = Modifier.size(120.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No contact updates.",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            fontSize = 18.sp
                        )
                        Text(
                            "Share your status with others!",
                            color = colors.text.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDashedBorderAvatar(user: com.quickchat.core.model.User, segmentsCount: Int) {
    val colors = LocalSketchyColors.current
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            
            if (segmentsCount <= 1) {
                drawCircle(
                    color = colors.accent,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                val gapDegree = 12f
                val arcDegree = (360f - (gapDegree * segmentsCount)) / segmentsCount
                
                for (i in 0 until segmentsCount) {
                    val startAngle = -90f + i * (arcDegree + gapDegree) + (gapDegree / 2f)
                    drawArc(
                        color = colors.accent,
                        startAngle = startAngle,
                        sweepAngle = arcDegree,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        }
        
        UserAvatar(
            avatarUrl = user.avatarUrl,
            displayName = user.displayName,
            size = 46.dp
        )
    }
}
