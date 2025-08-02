package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.R
import chromahub.rhythm.app.ui.annotations.RhythmAnimation
import kotlinx.coroutines.delay

@RhythmAnimation(
    type = chromahub.rhythm.app.ui.annotations.AnimationType.SCALE,
    duration = 3000,
    description = "Enhanced splash screen with sophisticated animations, floating particles, and dynamic text reveal"
)
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    
    // Enhanced logo scaling with bounce effect
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    // Floating logo animation
    val logoOffsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = androidx.compose.animation.core.EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloat"
    )

    // Dynamic glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Rotating gradient background
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "backgroundRotation"
    )

    // Particle system simulation
    val particleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle1"
    )

    val particleOffset2 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle2"
    )

    var showTagline by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200) // Delay before showing tagline
        showTagline = true
        delay(800) // Additional delay for subtitle
        showSubtitle = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background elements
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationAngle * 0.1f }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Floating particles
            for (i in 0..5) {
                val angle = (particleOffset1 + i * 60f) * (kotlin.math.PI / 180f)
                val radius = 150f + i * 30f
                val x = center.x + kotlin.math.cos(angle).toFloat() * radius
                val y = center.y + kotlin.math.sin(angle).toFloat() * radius
                
                drawCircle(
                    color = Color(0xFF6750A4).copy(alpha = 0.2f * glowAlpha),
                    radius = 4f + i * 2f,
                    center = Offset(x, y)
                )
            }
            
            for (i in 0..3) {
                val angle = (particleOffset2 + i * 90f) * (kotlin.math.PI / 180f)
                val radius = 200f + i * 40f
                val x = center.x + kotlin.math.cos(angle).toFloat() * radius
                val y = center.y + kotlin.math.sin(angle).toFloat() * radius
                
                drawCircle(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f * glowAlpha),
                    radius = 6f + i * 3f,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced logo container with glow effect
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        // translationY = logoOffsetY
                        // scaleX = logoScale
                        // scaleY = logoScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Glow background
                // Surface(
                //     modifier = Modifier
                //         .size(240.dp)
                //         .scale(logoScale * 1.2f),
                //     shape = CircleShape,
                //     color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.3f),
                //     shadowElevation = 0.dp
                // ) {}
                
                // // Secondary glow
                // Surface(
                //     modifier = Modifier
                //         .size(200.dp)
                //         .scale(logoScale),
                //     shape = CircleShape,
                //     color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = glowAlpha * 0.5f),
                //     shadowElevation = 0.dp
                // ) {}

                // Main logo
                Image(
                    painter = painterResource(id = R.drawable.rhythm_splash_logo),
                    contentDescription = "Rhythm",
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer {
                            alpha = 0.9f + (glowAlpha * 0.1f)
                        }
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

            // Animated title with letter-by-letter reveal
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 0.8f
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Text(
                    text = "Rhythm",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        letterSpacing = 2.sp
                    ),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .graphicsLayer {
                            shadowElevation = 0f
                            alpha = 0.8f + (glowAlpha * 0.2f)
                        }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced tagline with slide-in animation
            AnimatedVisibility(
                visible = showTagline,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(1200, delayMillis = 200))
            ) {
                Text(
                    text = "Your Music, Your Way",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        letterSpacing = 1.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            alpha = 0.7f + (glowAlpha * 0.3f)
                        }
                )
            }

            Spacer(modifier = Modifier.height(0.dp))
        }

        // Loading indicator at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val animationDelay = index * 200
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = animationDelay, easing = androidx.compose.animation.core.EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    ) {}
                }
            }
        }
    }
}
