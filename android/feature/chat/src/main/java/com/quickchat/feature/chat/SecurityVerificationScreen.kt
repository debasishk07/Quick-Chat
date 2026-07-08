package com.quickchat.feature.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickchat.core.model.theme.LocalSketchyColors
import com.quickchat.core.model.theme.SketchyCard
import com.quickchat.core.model.theme.sketchyBorder
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityVerificationScreen(
    viewModel: ChatRoomViewModel,
    onNavigateBack: () -> Unit
) {
    val partnerPhone by viewModel.recipientPhone.collectAsState()
    val partnerName by viewModel.recipientName.collectAsState()
    val fingerprint by viewModel.securityFingerprint.collectAsState()
    val colors = LocalSketchyColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                title = { Text("Verify Security Code", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.text) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Verify with $partnerName",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To verify that messages with $partnerName are end-to-end encrypted, scan this QR code on their device or compare these 60 numbers.",
                fontSize = 14.sp,
                color = colors.text.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Simulated QR Code Frame
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White)
                    .sketchyBorder(2.dp, colors.accent, 16.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SimulatedQRCodeCanvas(partnerPhone)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 60-digit fingerprint code Card
            if (fingerprint != null) {
                SketchyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val blocks = fingerprint!!.split(" ")
                        if (blocks.size == 5) {
                            Text(
                                text = "${blocks[0]}   ${blocks[1]}   ${blocks[2]}",
                                color = colors.accent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${blocks[3]}   ${blocks[4]}",
                                color = colors.accent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = fingerprint!!,
                                color = colors.accent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                CircularProgressIndicator(color = colors.accent)
            }
        }
    }
}

@Composable
fun SimulatedQRCodeCanvas(seed: String) {
    val random = Random(seed.hashCode())
    val gridSize = 21

    Canvas(modifier = Modifier.fillMaxSize()) {
        val pixelWidth = size.width / gridSize
        val pixelHeight = size.height / gridSize

        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val isFinderPattern = (x < 7 && y < 7) || (x >= gridSize - 7 && y < 7) || (x < 7 && y >= gridSize - 7)
                
                if (isFinderPattern) {
                    val isBorder = x == 0 || x == 6 || y == 0 || y == 6 ||
                                   (x == gridSize - 7 || x == gridSize - 1 || (y == 0 || y == 6)) ||
                                   ((x == 0 || x == 6) || y == gridSize - 7 || y == gridSize - 1)
                    val isCenter = (x in 2..4 && y in 2..4) ||
                                   (x in (gridSize - 5)..(gridSize - 3) && y in 2..4) ||
                                   (x in 2..4 && y in (gridSize - 5)..(gridSize - 3))
                    
                    if (isBorder || isCenter) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(x * pixelWidth, y * pixelHeight),
                            size = Size(pixelWidth, pixelHeight)
                        )
                    }
                } else {
                    if (random.nextBoolean()) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(x * pixelWidth, y * pixelHeight),
                            size = Size(pixelWidth, pixelHeight)
                        )
                    }
                }
            }
        }
    }
}
