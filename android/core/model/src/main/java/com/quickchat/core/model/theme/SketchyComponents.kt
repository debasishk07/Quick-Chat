package com.quickchat.core.model.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// 1. sketchyBorder Modifier - Draws double, hand-drawn look lines
fun Modifier.sketchyBorder(
    width: Dp = 1.5.dp,
    color: Color,
    cornerRadius: Dp = 8.dp
): Modifier = drawBehind {
    val strokeWidth = width.toPx()
    val radius = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    // Outer hand-sketched border
    val path1 = Path().apply {
        moveTo(0f, radius)
        lineTo(0f, h - radius)
        quadraticBezierTo(0f, h, radius, h)
        lineTo(w - radius, h)
        quadraticBezierTo(w, h, w, h - radius)
        lineTo(w, radius)
        quadraticBezierTo(w, 0f, w - radius, 0f)
        lineTo(radius, 0f)
        quadraticBezierTo(0f, 0f, 0f, radius)
    }

    // Inner offset sketched line (slightly lighter/thinner for sketchy style)
    val path2 = Path().apply {
        val offset = strokeWidth * 0.8f
        moveTo(offset, radius + offset)
        lineTo(offset, h - radius - offset)
        quadraticBezierTo(offset, h - offset, radius + offset, h - offset)
        lineTo(w - radius - offset, h - offset)
        quadraticBezierTo(w - offset, h - offset, w - offset, h - radius - offset)
        lineTo(w - offset, radius + offset)
        quadraticBezierTo(w - offset, offset, w - radius - offset, offset)
        lineTo(radius + offset, offset)
        quadraticBezierTo(offset, offset, offset, radius + offset)
    }

    drawPath(path1, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    drawPath(path2, color.copy(alpha = 0.4f), style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round))
}

// 2. Squiggly Divider
@Composable
fun SketchyDivider(
    modifier: Modifier = Modifier,
    color: Color = LocalSketchyColors.current.border.copy(alpha = 0.5f),
    thickness: Dp = 1.5.dp
) {
    Canvas(modifier = modifier.fillMaxWidth().height(8.dp)) {
        val w = size.width
        val h = size.height / 2
        val strokePx = thickness.toPx()
        val path = Path().apply {
            moveTo(0f, h)
            var x = 0f
            val step = w / 25f
            var isUp = true
            while (x < w) {
                x += step
                val yOffset = if (isUp) 2.5.dp.toPx() else (-2.5).dp.toPx()
                quadraticBezierTo(x - step / 2, h + yOffset, x, h)
                isUp = !isUp
            }
        }
        drawPath(path, color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
    }
}

// 3. Sketchy Card
@Composable
fun SketchyCard(
    modifier: Modifier = Modifier,
    containerColor: Color = LocalSketchyColors.current.surface,
    borderColor: Color = LocalSketchyColors.current.border,
    cornerRadius: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(cornerRadius))
            .sketchyBorder(1.5.dp, borderColor, cornerRadius)
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

// 4. Sketchy native doodles for empty states and decorations
@Composable
fun SketchyDoodle(
    name: String,
    modifier: Modifier = Modifier,
    color: Color = LocalSketchyColors.current.border
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        
        when (name) {
            "empty_chat" -> {
                // Draw a hand-drawn speech bubble + squiggly star
                val bubble = Path().apply {
                    moveTo(w * 0.2f, h * 0.4f)
                    quadraticBezierTo(w * 0.5f, h * 0.1f, w * 0.8f, h * 0.4f)
                    quadraticBezierTo(w * 0.9f, h * 0.6f, w * 0.7f, h * 0.8f)
                    lineTo(w * 0.8f, h * 0.9f) // point
                    lineTo(w * 0.5f, h * 0.8f)
                    quadraticBezierTo(w * 0.2f, h * 0.8f, w * 0.2f, h * 0.4f)
                }
                drawPath(bubble, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                
                // Squiggle lines inside speech bubble
                val line1 = Path().apply {
                    moveTo(w * 0.35f, h * 0.45f)
                    lineTo(w * 0.65f, h * 0.45f)
                }
                val line2 = Path().apply {
                    moveTo(w * 0.4f, h * 0.6f)
                    lineTo(w * 0.6f, h * 0.6f)
                }
                drawPath(line1, color.copy(alpha = 0.7f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(line2, color.copy(alpha = 0.7f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            }
            "empty_status" -> {
                // Draw a camera shape with dynamic hand-drawn line styling
                val camBody = Path().apply {
                    moveTo(w * 0.2f, h * 0.4f)
                    lineTo(w * 0.35f, h * 0.4f)
                    lineTo(w * 0.4f, h * 0.3f)
                    lineTo(w * 0.6f, h * 0.3f)
                    lineTo(w * 0.65f, h * 0.4f)
                    lineTo(w * 0.8f, h * 0.4f)
                    lineTo(w * 0.8f, h * 0.8f)
                    lineTo(w * 0.2f, h * 0.8f)
                    close()
                }
                drawPath(camBody, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                
                // Lens
                drawCircle(
                    color = color,
                    radius = w * 0.15f,
                    center = center.copy(y = h * 0.6f),
                    style = Stroke(width = 2.dp.toPx())
                )
                // Small flash dot
                drawCircle(
                    color = color,
                    radius = w * 0.03f,
                    center = center.copy(x = w * 0.7f, y = h * 0.48f)
                )
            }
            "no_results" -> {
                // Magnifying glass with organic imperfect handle
                val glass = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(w * 0.25f, h * 0.2f, w * 0.65f, h * 0.6f))
                }
                val handle = Path().apply {
                    moveTo(w * 0.58f, h * 0.53f)
                    lineTo(w * 0.85f, h * 0.85f)
                    // Double line handle
                    moveTo(w * 0.62f, h * 0.49f)
                    lineTo(w * 0.89f, h * 0.81f)
                }
                drawPath(glass, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                drawPath(handle, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }
            else -> {
                // A simple sketchy star
                val star = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f)
                    lineTo(w * 0.65f, h * 0.4f)
                    lineTo(w * 0.95f, h * 0.45f)
                    lineTo(w * 0.7f, h * 0.68f)
                    lineTo(w * 0.78f, h * 0.98f)
                    lineTo(w * 0.5f, h * 0.8f)
                    lineTo(w * 0.22f, h * 0.98f)
                    lineTo(w * 0.3f, h * 0.68f)
                    lineTo(w * 0.05f, h * 0.45f)
                    lineTo(w * 0.35f, h * 0.4f)
                    close()
                }
                drawPath(star, color, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
fun UserAvatar(
    avatarUrl: String?,
    displayName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val initial = displayName.trim().firstOrNull()?.uppercaseChar() ?: '?'
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD),
        Color(0xFF7986CB), Color(0xFF64B5F6), Color(0xFF4FC3F7), Color(0xFF4DB6AC),
        Color(0xFF81C784), Color(0xFFD4E157), Color(0xFFFFD54F), Color(0xFFFFB74D),
        Color(0xFFFF8A65), Color(0xFFA1887F)
    )
    val colorIndex = Math.abs(displayName.hashCode()) % colors.size
    val bgColor = colors[colorIndex]

    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .let {
            if (onClick != null) it.clickable(onClick = onClick) else it
        }

    if (!avatarUrl.isNullOrEmpty()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar of $displayName",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
            error = ColorPainter(bgColor)
        )
    } else {
        Box(
            modifier = avatarModifier.background(bgColor),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                color = Color.White,
                fontSize = (size.value * 0.45f).sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
fun SketchyBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalSketchyColors.current
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun dismissWithAnimation() {
        isVisible = false
        scope.launch {
            delay(200)
            onDismissRequest()
        }
    }

    val EaseOutQuart = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    Dialog(
        onDismissRequest = { dismissWithAnimation() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { dismissWithAnimation() }
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 250, easing = EaseOutQuart),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(durationMillis = 250)),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 200, easing = EaseOutQuart),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Consume click
                        )
                        .background(
                            color = colors.background, // Match sketchy calm background
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .sketchyBorder(
                            width = 2.dp,
                            color = colors.text,
                            cornerRadius = 20.dp
                        )
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 4.dp)
                                .background(colors.text.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        content()
                    }
                }
            }
        }
    }
}
