package com.example.food.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.data.model.OnboardingItem
import com.example.food.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToWelcome: () -> Unit
) {
    ModernOnboardingScreen(
        viewModel = viewModel,
        onNavigateToWelcome = onNavigateToWelcome
    )
}

@Composable
fun ModernOnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToWelcome: () -> Unit
) {
    val onboardingItems by viewModel.onboardingItems.collectAsState()
    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
    ) {
        // Decorative glowing background gradients for premium look
        val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
        val glowOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 200f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_offset"
        )

        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(y = (-50).dp, x = glowOffset.dp - 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF16B24).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(y = 100.dp, x = 100.dp - (glowOffset / 2).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .blur(100.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage < onboardingItems.size - 1) {
                    Text(
                        text = "Skip",
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.completeOnboarding()
                                onNavigateToWelcome()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Onboarding Pager for illustration slides
            Box(modifier = Modifier.weight(1.3f)) {
                OnboardingPager(
                    pagerState = pagerState,
                    onboardingItems = onboardingItems
                )
            }

            // Bottom Section containing Onboarding Card & Navigation Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card showing slide details with micro tags
                if (onboardingItems.isNotEmpty()) {
                    OnboardingCard(item = onboardingItems[pagerState.currentPage])
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Navigation Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page indicator dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(onboardingItems.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "dot_width"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFFF16B24) else Color.White.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }

                    // Next / Start Action Button
                    val isLastPage = pagerState.currentPage == onboardingItems.size - 1
                    Button(
                        onClick = {
                            if (isLastPage) {
                                viewModel.completeOnboarding()
                                onNavigateToWelcome()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF16B24),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isLastPage) "Get Started" else "Next",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isLastPage) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
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
fun OnboardingPager(
    pagerState: PagerState,
    onboardingItems: List<OnboardingItem>
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val item = onboardingItems.getOrNull(page) ?: return@HorizontalPager
        
        // Pager item transition math for high fidelity visual animations
        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val scale = (1f - kotlin.math.abs(pageOffset) * 0.15f).coerceIn(0.85f, 1f)
        val alpha = (1f - kotlin.math.abs(pageOffset) * 0.5f).coerceIn(0.5f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            contentAlignment = Alignment.Center
        ) {
            when (item.type) {
                "discover" -> DiscoverIllustration()
                "order" -> OrderIllustration()
                "personalized" -> PersonalizedIllustration()
                else -> Box(modifier = Modifier.size(200.dp))
            }
        }
    }
}

@Composable
fun OnboardingCard(item: OnboardingItem) {
    // Beautiful glassmorphism container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF161616).copy(alpha = 0.7f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Visual Chips/Tags summarizing the slide content
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFF16B24).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFF16B24).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = item.description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )
        }
    }
}

// ----------------- PREMIUM COMPOSE CANVAS ILLUSTRATIONS -----------------

@Composable
fun DiscoverIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "discover_transition")
    
    // Steam animation variables
    val steamOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -40f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "steam_1"
    )
    val steamOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -40f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "steam_2"
    )
    val steamAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "steam_alpha"
    )

    // mesob tray spinning slightly
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Restart),
        label = "mesob_rotation"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val baseRadius = size.minDimension / 2.3f

            // 1. Draw outer Mesob Woven Basket Platter (Traditional Ethiopian Art Style)
            drawCircle(
                color = Color(0xFF2E1A0C),
                radius = baseRadius,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            // Inner design rings of Mesob Basket
            val colorList = listOf(Color(0xFFF16B24), Color(0xFF4CAF50), Color(0xFFFFB74D))
            for (i in 0 until 3) {
                drawCircle(
                    color = colorList[i],
                    radius = baseRadius - (12.dp * (i + 1)).toPx(),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )
            }

            // Injera plate backing
            drawCircle(
                color = Color(0xFFC0A080).copy(alpha = 0.3f),
                radius = baseRadius - 38.dp.toPx()
            )

            // 2. Draw traditional food dollops (Wot / Tibs)
            val wotPositions = listOf(
                Pair(center.x - 30.dp.toPx(), center.y - 30.dp.toPx()) to Color(0xFF9E2A2B), // Key Wot (Red)
                Pair(center.x + 35.dp.toPx(), center.y - 20.dp.toPx()) to Color(0xFFE9C46A), // Alicha (Yellow)
                Pair(center.x - 10.dp.toPx(), center.y + 40.dp.toPx()) to Color(0xFF4F772D), // Gomen (Green)
                Pair(center.x + 25.dp.toPx(), center.y + 30.dp.toPx()) to Color(0xFF9B2226)  // Tibs (Brown/Red)
            )

            wotPositions.forEach { (pos, color) ->
                drawCircle(
                    color = color,
                    radius = 14.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(pos.first, pos.second)
                )
                // subtle shading details
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = 6.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(pos.first - 4.dp.toPx(), pos.second - 4.dp.toPx())
                )
            }

            // Draw center roll of Injera
            drawCircle(
                color = Color(0xFFD4C5B9),
                radius = 16.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFFB09F91),
                radius = 16.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
            )
        }

        // Overlay Coffee Jebena (Clay Pot) on the mesob side
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .offset(x = (-30).dp, y = (-20).dp)
        ) {
            val w = size.width
            val h = size.height

            // Spout
            val spoutPath = Path().apply {
                moveTo(w * 0.15f, h * 0.35f)
                lineTo(w * 0.35f, h * 0.45f)
                lineTo(w * 0.32f, h * 0.50f)
                close()
            }
            drawPath(spoutPath, Color(0xFF3E2723))

            // Body
            drawCircle(
                color = Color(0xFF271510),
                radius = w * 0.3f,
                center = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.65f)
            )

            // Neck of Jebena
            val neckPath = Path().apply {
                moveTo(w * 0.45f, h * 0.45f)
                lineTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.6f, h * 0.15f)
                lineTo(w * 0.65f, h * 0.45f)
                close()
            }
            drawPath(neckPath, Color(0xFF271510))

            // Traditional Handle
            val handlePath = Path().apply {
                moveTo(w * 0.62f, h * 0.35f)
                quadraticTo(w * 0.85f, h * 0.45f, w * 0.75f, h * 0.7f)
            }
            drawPath(
                handlePath,
                Color(0xFFFFB74D),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Base Ring
            drawOval(
                color = Color(0xFFFFB74D).copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.88f),
                size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.08f)
            )

            // Dynamic Steam lines rising
            val steamPath1 = Path().apply {
                moveTo(w * 0.18f, h * 0.3f + steamOffset1)
                quadraticTo(w * 0.15f, h * 0.2f + steamOffset1, w * 0.22f, h * 0.1f + steamOffset1)
            }
            val steamPath2 = Path().apply {
                moveTo(w * 0.26f, h * 0.28f + steamOffset2)
                quadraticTo(w * 0.29f, h * 0.18f + steamOffset2, w * 0.24f, h * 0.08f + steamOffset2)
            }

            drawPath(
                steamPath1,
                Color.White.copy(alpha = steamAlpha),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                steamPath2,
                Color.White.copy(alpha = steamAlpha * 0.8f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun OrderIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "order_transition")

    // Pulsing delivery wave
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val w = size.width
            val h = size.height

            // Draw connecting paths (Dashed Glowing Route lines)
            val connectionPath = Path().apply {
                moveTo(w * 0.2f, h * 0.4f)
                cubicTo(w * 0.4f, h * 0.2f, w * 0.6f, h * 0.8f, w * 0.8f, h * 0.5f)
            }
            drawPath(
                connectionPath,
                Color(0xFFF16B24).copy(alpha = 0.4f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Delivery Station Circle (Left)
            val centerLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.4f)
            drawCircle(
                color = Color(0xFFF16B24).copy(alpha = 0.15f),
                radius = 45.dp.toPx(),
                center = centerLeft
            )
            drawCircle(
                color = Color(0xFFF16B24),
                radius = 32.dp.toPx(),
                center = centerLeft,
                style = Stroke(width = 2.dp.toPx())
            )

            // Takeaway Station Circle (Middle Bottom)
            val centerMiddle = androidx.compose.ui.geometry.Offset(center.x, h * 0.72f)
            drawCircle(
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                radius = 45.dp.toPx(),
                center = centerMiddle
            )
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 32.dp.toPx(),
                center = centerMiddle,
                style = Stroke(width = 2.dp.toPx())
            )

            // Dine-in Station Circle (Right)
            val centerRight = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.4f)
            drawCircle(
                color = Color(0xFFFFB74D).copy(alpha = 0.15f),
                radius = 45.dp.toPx(),
                center = centerRight
            )
            drawCircle(
                color = Color(0xFFFFB74D),
                radius = 32.dp.toPx(),
                center = centerRight,
                style = Stroke(width = 2.dp.toPx())
            )

            // Custom Vector symbols inside the stations:
            // 1. Delivery (Bike/box path outline)
            val bikePath = Path().apply {
                moveTo(centerLeft.x - 12.dp.toPx(), centerLeft.y + 4.dp.toPx())
                lineTo(centerLeft.x + 12.dp.toPx(), centerLeft.y + 4.dp.toPx())
                moveTo(centerLeft.x - 6.dp.toPx(), centerLeft.y - 8.dp.toPx())
                lineTo(centerLeft.x + 8.dp.toPx(), centerLeft.y - 8.dp.toPx())
                lineTo(centerLeft.x + 14.dp.toPx(), centerLeft.y + 4.dp.toPx())
            }
            drawPath(bikePath, Color(0xFFF16B24), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(Color(0xFFF16B24), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(centerLeft.x - 6.dp.toPx(), centerLeft.y + 8.dp.toPx()))
            drawCircle(Color(0xFFF16B24), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(centerLeft.x + 6.dp.toPx(), centerLeft.y + 8.dp.toPx()))

            // 2. Takeaway (Paper Bag Outline)
            val bagPath = Path().apply {
                moveTo(centerMiddle.x - 10.dp.toPx(), centerMiddle.y + 10.dp.toPx())
                lineTo(centerMiddle.x - 10.dp.toPx(), centerMiddle.y - 8.dp.toPx())
                lineTo(centerMiddle.x + 10.dp.toPx(), centerMiddle.y - 8.dp.toPx())
                lineTo(centerMiddle.x + 10.dp.toPx(), centerMiddle.y + 10.dp.toPx())
                close()
                // Handle
                moveTo(centerMiddle.x - 5.dp.toPx(), centerMiddle.y - 8.dp.toPx())
                quadraticTo(centerMiddle.x, centerMiddle.y - 15.dp.toPx(), centerMiddle.x + 5.dp.toPx(), centerMiddle.y - 8.dp.toPx())
            }
            drawPath(bagPath, Color(0xFF4CAF50), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

            // 3. Dine-In (Covered Dish / Plate Outline)
            val platePath = Path().apply {
                // Dome lid
                moveTo(centerRight.x - 14.dp.toPx(), centerRight.y)
                quadraticTo(centerRight.x, centerRight.y - 16.dp.toPx(), centerRight.x + 14.dp.toPx(), centerRight.y)
                close()
                // Platter line
                moveTo(centerRight.x - 18.dp.toPx(), centerRight.y + 4.dp.toPx())
                lineTo(centerRight.x + 18.dp.toPx(), centerRight.y + 4.dp.toPx())
            }
            drawPath(platePath, Color(0xFFFFB74D), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun PersonalizedIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "personalized_transition")
    
    // Wave animation to show dynamic AI
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val w = size.width
            val h = size.height

            // Draw glowing background radar ring
            drawCircle(
                color = Color(0xFFF16B24).copy(alpha = 0.08f),
                radius = 110.dp.toPx()
            )
            drawCircle(
                color = Color(0xFFF16B24).copy(alpha = 0.15f),
                radius = 85.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw phone recommendation frame
            val phoneWidth = 72.dp.toPx()
            val phoneHeight = 130.dp.toPx()
            val phoneOffset = androidx.compose.ui.geometry.Offset(center.x - phoneWidth / 2, center.y - phoneHeight / 2)
            
            drawRoundRect(
                color = Color(0xFF1E1E1E),
                topLeft = phoneOffset,
                size = androidx.compose.ui.geometry.Size(phoneWidth, phoneHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )

            // Recommended meal cards popping up out of the phone
            // Card 1
            val card1Offset = androidx.compose.ui.geometry.Offset(center.x - 85.dp.toPx(), center.y - 35.dp.toPx())
            val card1Size = androidx.compose.ui.geometry.Size(65.dp.toPx(), 36.dp.toPx())
            drawRoundRect(
                color = Color(0xFF1A1A1A),
                topLeft = card1Offset,
                size = card1Size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFF16B24).copy(alpha = 0.5f),
                topLeft = card1Offset,
                size = card1Size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Tiny star inside Card 1
            drawCircle(
                color = Color(0xFFFFB74D),
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(card1Offset.x + 12.dp.toPx(), card1Offset.y + 12.dp.toPx())
            )
            // Lines in card 1
            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = androidx.compose.ui.geometry.Offset(card1Offset.x + 22.dp.toPx(), card1Offset.y + 12.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(card1Offset.x + 55.dp.toPx(), card1Offset.y + 12.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(card1Offset.x + 12.dp.toPx(), card1Offset.y + 24.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(card1Offset.x + 48.dp.toPx(), card1Offset.y + 24.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            // Card 2
            val card2Offset = androidx.compose.ui.geometry.Offset(center.x + 25.dp.toPx(), center.y + 5.dp.toPx())
            val card2Size = androidx.compose.ui.geometry.Size(65.dp.toPx(), 36.dp.toPx())
            drawRoundRect(
                color = Color(0xFF1A1A1A),
                topLeft = card2Offset,
                size = card2Size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                topLeft = card2Offset,
                size = card2Size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Tiny star inside Card 2
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(card2Offset.x + 12.dp.toPx(), card2Offset.y + 12.dp.toPx())
            )
            // Lines in card 2
            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = androidx.compose.ui.geometry.Offset(card2Offset.x + 22.dp.toPx(), card2Offset.y + 12.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(card2Offset.x + 55.dp.toPx(), card2Offset.y + 12.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(card2Offset.x + 12.dp.toPx(), card2Offset.y + 24.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(card2Offset.x + 48.dp.toPx(), card2Offset.y + 24.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            // Dynamic AI spark / recommendation glow lines
            val waveRads = Math.toRadians(waveOffset.toDouble())
            val sparkOffset = (Math.sin(waveRads) * 6.dp.toPx()).toFloat()
            
            drawCircle(
                color = Color(0xFFFFB74D),
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(center.x - 45.dp.toPx(), center.y + 55.dp.toPx() + sparkOffset)
            )
            drawCircle(
                color = Color(0xFFF16B24),
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(center.x + 55.dp.toPx(), center.y - 60.dp.toPx() - sparkOffset)
            )
        }
    }
}
