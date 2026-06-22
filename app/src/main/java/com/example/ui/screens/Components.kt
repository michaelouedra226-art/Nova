package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MediaItem

// Premium Dark & Gold Theme Colors
val ObsidianSpace = Color(0xFF070A13)
val DeepNavySlate = Color(0xFF0F172A)
val GoldenSun = Color(0xFFEAB308)
val DarkGoldHighlight = Color(0xFFCA8A04)
val IceCyan = Color(0xFF38BDF8)
val CardSlateGrey = Color(0xFF1E293B)
val LightGlassOverlay = Color(0x33FFFFFF)
val HeavyGlassOverlay = Color(0x1F000000)

@Composable
fun AmbientGlassyBackground() {
    val transition = rememberInfiniteTransition(label = "ambient_orbs")
    
    val orbX1 by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_x1"
    )
    val orbY1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_y1"
    )
    val orbX2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_x2"
    )
    val orbY2 by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(19000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_y2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712)) // Deep premium obsidian space
            .drawBehind {
                val w = size.width
                val h = size.height
                
                // Neon Cyan Glow Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0EA5E9).copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(w * orbX1, h * orbY1),
                        radius = w * 0.75f
                    )
                )

                // Velvet Violet Glow Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD946EF).copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * orbX2, h * orbY2),
                        radius = w * 0.85f
                    )
                )

                // Classic Golden Accent Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEAB308).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.3f),
                        radius = w * 0.45f
                    )
                )
            }
    )
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    borderColor: Color = Color.White.copy(alpha = 0.18f),
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val duration = 400
    val customEase = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glass_card_scale"
    )

    val bgTopAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.60f else 0.40f,
        animationSpec = tween(duration, easing = customEase),
        label = "glass_card_bg_top"
    )

    val bgBottomAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.35f else 0.20f,
        animationSpec = tween(duration, easing = customEase),
        label = "glass_card_bg_bot"
    )

    val borderTopAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.45f else 0.22f,
        animationSpec = tween(duration - 100, easing = customEase),
        label = "glass_card_border_top"
    )

    val borderBottomAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.15f else 0.03f,
        animationSpec = tween(duration - 100, easing = customEase),
        label = "glass_card_border_bot"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = bgTopAlpha),
                        Color(0xFF0F172A).copy(alpha = bgBottomAlpha)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderTopAlpha),
                        Color.White.copy(alpha = borderBottomAlpha)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun GlassmorphicRowItem(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val duration = 350
    val customEase = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glass_row_scale"
    )

    val bgTopAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.50f else 0.30f,
        animationSpec = tween(duration, easing = customEase),
        label = "glass_row_bg_top"
    )

    val bgBottomAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.25f else 0.15f,
        animationSpec = tween(duration, easing = customEase),
        label = "glass_row_bg_bot"
    )

    val borderTopAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.35f else 0.14f,
        animationSpec = tween(duration - 100, easing = customEase),
        label = "glass_row_border_top"
    )

    val borderBottomAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.10f else 0.02f,
        animationSpec = tween(duration - 100, easing = customEase),
        label = "glass_row_border_bot"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = bgTopAlpha),
                        Color(0xFF0F172A).copy(alpha = bgBottomAlpha)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderTopAlpha),
                        Color.White.copy(alpha = borderBottomAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GoldenSun
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.8f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 15.sp,
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.sp
            )
        )
    }
}

// Extract beautiful dynamic multi-color linear gradients based on selection preset
fun getPresetColors(presetId: Int): List<Color> {
    return when (presetId) {
        0 -> listOf(Color(0xFFA855F7), Color(0xFF3B82F6)) // Neon Purple to Blue
        1 -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444)) // Neon Gold to Orange Red
        2 -> listOf(Color(0xFF10B981), Color(0xFF06B6D4)) // Emerald to Cyan
        3 -> listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)) // Pink to Violet
        4 -> listOf(Color(0xFF94A3B8), Color(0xFF475569)) // Silver Slate
        else -> listOf(GoldenSun, DeepNavySlate)
    }
}

@Composable
fun DynamicOverlayAtmosphere(presetId: Int, isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "aura")
    val animOffset by transition.animateFloat(
        initialValue = -100f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val colors = getPresetColors(presetId)
    val playFactor = if (isPlaying) 1.2f else 0.5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .drawBehind {
                // Large radial gradient glowing in backdrop
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors[0].copy(alpha = 0.25f * playFactor), Color.Transparent),
                        center = Offset(size.width / 2f + animOffset/2, size.height / 3f),
                        radius = size.width * 1.1f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors[1].copy(alpha = 0.2f * playFactor), Color.Transparent),
                        center = Offset(size.width / 3f, size.height * 0.7f - animOffset / 3),
                        radius = size.width * 0.9f
                    )
                )
            }
    )
}

// Custom-drawn responsive live EQ Visualizer Bars
@Composable
fun RealtimeEqualizerVisualizer(
    color: Color = GoldenSun,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val count = 18
    val items = remember { List(count) { (20..90).random() } }
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    
    val animationStates = items.map { targetValue ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = targetValue.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween((300..900).random(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar"
            )
        } else {
            remember { mutableStateOf(12f) }
        }
    }

    Row(
        modifier = modifier.height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        animationStates.forEach { anim ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(anim.value.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(color)
            )
        }
    }
}

// Stunning Custom Material 3 styled Bar Graph
@Composable
fun CustomAestheticBarChart(
    data: List<Int>,
    labels: List<String>,
    accentColor: Color = GoldenSun
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, value ->
            val maxVal = (data.maxOrNull() ?: 1).toFloat()
            val ratio = value / maxVal

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${value}m",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.75f * ratio)
                        .width(22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = labels.getOrNull(index) ?: "",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
