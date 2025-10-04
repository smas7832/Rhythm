package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.R
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    musicViewModel: MusicViewModel,
    onMediaScanComplete: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")
    
    // Subtle breathing animation for logo
    val logoBreathing by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = androidx.compose.animation.core.EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoBreathing"
    )

    // Animation state flags
    var showLogo by remember { mutableStateOf(false) }
    var showAppName by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }
    var exitSplash by remember { mutableStateOf(false) }

    // Animatable values for smooth animations
    val logoAlpha = remember { Animatable(0f) }
    val logoScaleAnim = remember { Animatable(0.5f) }
    
    val appNameOffsetX = remember { Animatable(-150f) } // Slides from left (behind logo)
    val appNameAlpha = remember { Animatable(0f) }
    
    val taglineOffsetY = remember { Animatable(-80f) } // Slides down from behind
    val taglineAlpha = remember { Animatable(0f) }
    
    val loaderAlpha = remember { Animatable(0f) }
    
    val exitScale = remember { Animatable(1f) }
    val exitAlpha = remember { Animatable(1f) }

    // Monitor media scanning completion
    val isInitialized by musicViewModel.isInitialized.collectAsState()

    LaunchedEffect(Unit) {
        delay(150)
        
        // STEP 1: Logo appears with scale and fade
        showLogo = true
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(0, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
        logoScaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
//        delay(100)
        
        // STEP 2: App name slides horizontally from behind the logo (left to center)
        showAppName = true
        launch {
            appNameAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(delayMillis = 0, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
        appNameOffsetX.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
//        delay(100)
        
        // STEP 3: Tagline slides down from behind logo and name
        showTagline = true
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(0)
            )
        }
        taglineOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
//        delay(100)
        
        // STEP 4: Loader fades in at bottom
        showLoader = true
        loaderAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(100)
        )
    }



    // Handle media scanning completion - exit animation
    LaunchedEffect(isInitialized) {
        if (isInitialized && !exitSplash) {
            delay(2000)
            exitSplash = true
            
            launch {
                exitScale.animateTo(0.90f, animationSpec = tween(350))
            }
            exitAlpha.animateTo(0f, animationSpec = tween(350))
            
            delay(100)
            onMediaScanComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer {
                scaleX = exitScale.value
                scaleY = exitScale.value
                alpha = exitAlpha.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Background particles using the drawable
//        AnimatedVisibility(
//            visible = showContent,
//            enter = fadeIn(animationSpec = tween(1000))
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.splash_particles),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(300.dp)
//                    .graphicsLayer {
//                        alpha = 0.3f
//                        scaleX = logoScale * 1.2f
//                        scaleY = logoScale * 1.2f
//                    }
//            )
//        }

        // Main content with dramatic text reveal
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Logo with entrance animation and breathing effect
                if (showLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.rhythm_splash_logo),
                        contentDescription = "Rhythm",
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer {
                                alpha = logoAlpha.value
                                scaleX = logoScaleAnim.value * logoBreathing
                                scaleY = logoScaleAnim.value * logoBreathing
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // App name sliding from left (behind logo)
                Box(
                    modifier = Modifier.height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showAppName) {
                        Text(
                            text = "Rhythm",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 48.sp,
                                letterSpacing = 2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = appNameAlpha.value
                                    translationX = appNameOffsetX.value
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tagline sliding down from behind logo and name
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showTagline) {
                        Text(
                            text = "Your Music, Your Rhythm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 1.sp,
                                fontSize = 17.sp
                            ),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = taglineAlpha.value
                                    translationY = taglineOffsetY.value
                                }
                        )
                    }
                }
            }
        }

        // Loading indicator at bottom with fade in
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .graphicsLayer {
                    alpha = loaderAlpha.value
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            if (showLoader) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Loading your music library...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val animationDelay = index * 150
                            val dotScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        600,
                                        delayMillis = animationDelay,
                                        easing = androidx.compose.animation.core.EaseInOut
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "dot$index"
                            )

                            Surface(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(dotScale),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
