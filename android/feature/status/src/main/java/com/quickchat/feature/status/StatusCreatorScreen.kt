package com.quickchat.feature.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.sketchyBorder

// Simple Helper import to override mutableStateOf
import androidx.compose.runtime.mutableStateOf as mutableStateFlowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCreatorScreen(
    viewModel: StatusViewModel,
    onNavigateBack: () -> Unit
) {
    var textInput by remember { mutableStateFlowOf("") }
    
    // Background and text color pairings for premium minimal look
    val colorPairs = listOf(
        "#F5F0E8" to "#1F1B16", // Cream / Charcoal
        "#E5B09E" to "#1F1B16", // Terracotta / Charcoal
        "#161513" to "#F5F0E8", // Charcoal / Cream
        "#E0E5D7" to "#1F1B16", // Soft Sage / Charcoal
        "#E3DEEC" to "#1F1B16"  // Soft Lavender / Charcoal
    )
    var activePairIndex by remember { mutableStateFlowOf(0) }
    
    val bgHex = colorPairs[activePairIndex].first
    val textHex = colorPairs[activePairIndex].second
    
    val currentBgColor = Color(android.graphics.Color.parseColor(bgHex))
    val currentTextColor = Color(android.graphics.Color.parseColor(textHex))

    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val themeColors = LocalSketchyColors.current

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            viewModel.resetUploadState()
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor)
            .padding(24.dp)
    ) {
        // Close button top left
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp)
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = currentTextColor)
        }

        // Color palette cycle button top right
        IconButton(
            onClick = {
                activePairIndex = (activePairIndex + 1) % colorPairs.size
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp)
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = "Change Background", tint = currentTextColor)
        }

        // Text input center
        TextField(
            value = textInput,
            onValueChange = { if (it.length <= 150) textInput = it },
            placeholder = {
                Text(
                    "Type a status update...",
                    color = currentTextColor.copy(alpha = 0.5f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = TextStyle(
                color = currentTextColor,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = currentTextColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .wrapContentHeight()
        )

        // Character counter
        Text(
            text = "${textInput.length}/150",
            color = currentTextColor.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 24.dp, start = 16.dp)
        )

        // Send Fab bottom right
        FloatingActionButton(
            onClick = {
                if (textInput.isNotBlank() && !loading) {
                    viewModel.postTextStatus(textInput, bgHex)
                }
            },
            containerColor = themeColors.accent,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .sketchyBorder(1.dp, currentTextColor, 28.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Outlined.Send, contentDescription = "Post Status")
            }
        }
    }
}
