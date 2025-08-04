package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.R
import chromahub.rhythm.app.ui.components.M3ExpressiveLinearIndicator
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Media Scan Loader Screen
 * Shows a beautiful loading animation while scanning for media files
 * Will not exit until media scanning is complete
 */
@Composable
fun MediaScanLoader(
    musicViewModel: MusicViewModel = viewModel(),
    onScanComplete: () -> Unit
) {
    val songs by musicViewModel.songs.collectAsState()
    val albums by musicViewModel.albums.collectAsState()
    val artists by musicViewModel.artists.collectAsState()
    
    // Track scanning progress
    var scanProgress by remember { mutableStateOf(0f) }
    var currentStep by remember { mutableStateOf("Initializing...") }
    var songsFound by remember { mutableIntStateOf(0) }
    var albumsFound by remember { mutableIntStateOf(0) }
    var artistsFound by remember { mutableIntStateOf(0) }
    var isComplete by remember { mutableStateOf(false) }
    
    // Breathing animation for the main loader
    val infiniteTransition = rememberInfiniteTransition(label = "mediaScanAnimation")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )
    
    // Rotation animation for decorative elements
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = scanProgress,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "progressAnimation"
    )
    
    // Monitor media scanning progress
    LaunchedEffect(songs.size, albums.size, artists.size) {
        songsFound = songs.size
        albumsFound = albums.size
        artistsFound = artists.size
        
        // Update progress based on content found
        when {
            songs.isEmpty() && albums.isEmpty() && artists.isEmpty() -> {
                scanProgress = 0.1f
                currentStep = "Searching for music files..."
            }
            songs.isNotEmpty() && albums.isEmpty() -> {
                scanProgress = 0.4f
                currentStep = "Organizing songs..."
            }
            albums.isNotEmpty() && artists.isEmpty() -> {
                scanProgress = 0.7f
                currentStep = "Building album library..."
            }
            artists.isNotEmpty() -> {
                scanProgress = 0.9f
                currentStep = "Finalizing artist collection..."
                
                // Wait a bit more to ensure everything is properly loaded
                delay(1500)
                scanProgress = 1.0f
                currentStep = "Media scan complete!"
                
                // Mark as complete and trigger completion after animation
                delay(1000)
                isComplete = true
                delay(500)
                onScanComplete()
            }
        }
    }
    
    // Trigger initial media scan
    LaunchedEffect(Unit) {
        currentStep = "Starting media scan..."
        delay(500)
        musicViewModel.refreshLibrary()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background decorative elements
            DecorativeBackground(rotation = rotation)
            
            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // App logo and name with breathing animation - matching onboarding format
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(bottom = 48.dp)
                        // .scale(breathingScale)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rhythm_splash_logo),
                        contentDescription = null,
                        modifier = Modifier.size(66.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rhythm",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Progress card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main loader
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            M3FourColorCircularLoader(
                                modifier = Modifier.size(80.dp),
                                strokeWidth = 6f
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Progress bar
                        M3ExpressiveLinearIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxWidth(),
                            primaryColor = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Current step text
                        Text(
                            text = currentStep,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Statistics
                        AnimatedVisibility(
                            visible = songsFound > 0 || albumsFound > 0 || artistsFound > 0,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            MediaScanStats(
                                songsFound = songsFound,
                                albumsFound = albumsFound,
                                artistsFound = artistsFound
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Completion message
                AnimatedVisibility(
                    visible = isComplete,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Ready to groove!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaScanStats(
    songsFound: Int,
    albumsFound: Int,
    artistsFound: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            icon = RhythmIcons.MusicNote,
            count = songsFound,
            label = "Songs",
            color = MaterialTheme.colorScheme.primary
        )
        
        StatItem(
            icon = RhythmIcons.Album,
            count = albumsFound,
            label = "Albums",
            color = MaterialTheme.colorScheme.secondary
        )
        
        StatItem(
            icon = RhythmIcons.Artist,
            count = artistsFound,
            label = "Artists",
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DecorativeBackground(rotation: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Floating music notes
        repeat(8) { index ->
            val angle = (index * 45f) + rotation
            val radius = 200.dp
            val offsetX = radius * cos(Math.toRadians(angle.toDouble())).toFloat()
            val offsetY = radius * sin(Math.toRadians(angle.toDouble())).toFloat()
            
            Icon(
                imageVector = RhythmIcons.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        translationX = offsetX.value
                        translationY = offsetY.value
                        rotationZ = rotation
                        alpha = 0.3f + (sin(Math.toRadians((angle * 2).toDouble())).toFloat() * 0.2f)
                    }
                    .align(Alignment.Center)
            )
        }
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                        ),
                        radius = 800f
                    )
                )
        )
    }
}
