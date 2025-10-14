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
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
    val logoAlpha = remember { Animatable(1f) } // Start visible
    val logoScaleAnim = remember { Animatable(1.0f) } // Start at full splash screen size
    val logoOffsetX = remember { Animatable(0f) } // Logo position - slides left
    
    val appNameOffsetX = remember { Animatable(0f) } // Starts centered behind logo - slides right
    
    val loaderAlpha = remember { Animatable(0f) }
    
    val exitScale = remember { Animatable(1f) }
    val exitAlpha = remember { Animatable(1f) }

    // Monitor media scanning completion
    val isInitialized by musicViewModel.isInitialized.collectAsState()

    LaunchedEffect(Unit) {
        delay(150) // Brief initial delay to match system splash

        // STEP 1: Logo is already visible at full size (matches system splash)
        showLogo = true
        delay(250) // Hold at full size briefly
        
        // Shrink logo to fit alongside text
        logoScaleAnim.animateTo(
            targetValue = 0.55f, // Shrink to smaller size for side-by-side layout
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        delay(100) // Brief pause after shrink

        // STEP 2: Logo slides LEFT and App name appears with expand animation (like TabButton)
        showAppName = true
        // Logo slides left
        launch {
            logoOffsetX.animateTo(
                targetValue = -265f, // Slide left from center
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        // App name offset happens via AnimatedVisibility now
        appNameOffsetX.animateTo(
            targetValue = 105f, // Slide right from center
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        delay(250) // Pause for name to settle

        // STEP 3: Tagline appears with expand animation
        showTagline = true

        delay(150) // Brief delay before loader

        // STEP 4: Loader fades in smoothly
        showLoader = true
        loaderAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = androidx.compose.animation.core.EaseInOut)
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
                verticalArrangement = Arrangement.Center
            ) {
                // Logo and App name on the same line
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // App name revealing with expand animation (like TabButton in ThemeCustomizationBottomSheet)
                    // Rendered first so it appears BEHIND the logo in z-order
                    if (showAppName) {
                        Row(
                            modifier = Modifier.graphicsLayer {
                                translationX = appNameOffsetX.value
                            }
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showAppName,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(400)
                                ),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Text(
                                    text = "Rhythm",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 48.sp,
                                        letterSpacing = 2.sp
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // Logo with entrance animation and breathing effect (centered, then slides left)
                    // Rendered second so it appears ON TOP of the text in z-order
                    if (showLogo) {
                        Image(
                            painter = painterResource(id = R.drawable.rhythm_splash_logo),
                            contentDescription = "Rhythm",
                            modifier = Modifier
                                .size(200.dp) // Larger base size to match system splash
                                .graphicsLayer {
                                    alpha = logoAlpha.value
                                    scaleX = logoScaleAnim.value * logoBreathing
                                    scaleY = logoScaleAnim.value * logoBreathing
                                    translationX = logoOffsetX.value
                                }
                        )
                    }
                }

                // Tagline with expand animation from center
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (showTagline) {
                        Row {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showTagline,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(400)
                                ) + slideInVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    initialOffsetY = { 50 }
                                ),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Text(
                                    text = "Your Music, Your Rhythm",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        letterSpacing = 1.sp,
                                        fontSize = 17.sp
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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
