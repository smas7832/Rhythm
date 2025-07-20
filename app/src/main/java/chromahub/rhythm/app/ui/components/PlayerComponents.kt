package chromahub.rhythm.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.theme.PlayerButtonColor
import chromahub.rhythm.app.ui.theme.PlayerProgressColor
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.concurrent.TimeUnit
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import chromahub.rhythm.app.ui.components.M3LinearLoader
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding


/**
 * Mini player that appears at the bottom of the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )
    
    // Animation for tap feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )

    // Animation for song change bounce effect
    var songChangeBounceTrigger by remember { mutableStateOf(false) }
    val songBounceScale by animateFloatAsState(
        targetValue = if (songChangeBounceTrigger) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "songBounceScale"
    )

    // Trigger bounce animation when song changes
    LaunchedEffect(song) {
        if (song != null) {
            songChangeBounceTrigger = true
            delay(100) // Short delay to initiate the bounce
            songChangeBounceTrigger = false // Let the spring animation bring it back to 1f
        }
    }
    
    // For swipe gesture detection
    var offsetY by remember { mutableStateOf(0f) }
    val swipeUpThreshold = 100f // Minimum distance to trigger player open
    val swipeDownThreshold = 100f // Minimum distance to trigger dismissal
    
    // Track last offset for haptic feedback at intervals
    var lastHapticOffset by remember { mutableStateOf(0f) }
    
    // Animation for translation during swipe
    val translationOffset by animateFloatAsState(
        targetValue = if (offsetY > 0) offsetY.coerceAtMost(200f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "translationOffset"
    )
    
    // Calculate alpha based on offset
    val alphaValue by animateFloatAsState(
        targetValue = if (offsetY > 0) {
            // Fade out as user swipes down
            (1f - (offsetY / 300f)).coerceIn(0.2f, 1f)
        } else {
            1f
        },
        label = "alphaValue"
    )
    
    // For tracking if the mini player is being dismissed
    var isDismissingPlayer by remember { mutableStateOf(false) }
    
    // If dismissing, animate out and stop playback
    LaunchedEffect(isDismissingPlayer) {
        if (isDismissingPlayer) {
            // Stop playback when dismissing
            if (isPlaying) {
                onPlayPause()
            }
            delay(300) // Wait for animation to complete
            // We should actually hide the player when dismissed
            // In a real app, this would communicate back to parent that player should be hidden
            // For now, just reset the state
            isDismissingPlayer = false
            offsetY = 0f
        }
    }

    // Enhanced navigation bar handling for proper positioning across all navigation types
    val navigationBarInsets = WindowInsets.navigationBars
    val systemBarInsets = WindowInsets.systemBars
    val navigationBarHeight = with(density) {
        navigationBarInsets.getBottom(density).toDp()
    }
    val systemBarsBottom = with(density) {
        systemBarInsets.getBottom(density).toDp()
    }
    
    // Comprehensive bottom padding calculation that handles all scenarios
    val bottomPadding = when {
        // Check if we're in fullscreen mode or gesture navigation without visible navbar
        navigationBarHeight <= 0.dp -> 16.dp // No navigation bar, use generous padding from bottom edge
        
        // 3-button navigation (high navbar)
        navigationBarHeight > 48.dp -> {
            // Add more generous padding above 3-button navigation to prevent overlap and provide breathing room
            navigationBarHeight + 12.dp
        }
        
        // Gesture navigation with visible hint bar (low navbar)
        navigationBarHeight > 0.dp && navigationBarHeight <= 48.dp -> {
            // Position just above the gesture hint bar with some breathing room
            navigationBarHeight + 8.dp
        }
        
        // Fallback for edge cases
        else -> 16.dp
    }

    Card(
        onClick = {
            if (!isDismissingPlayer) {
                // Enhanced haptic feedback for click
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlayerClick()
            }
        },
        shape = RoundedCornerShape(24.dp), // Slightly reduced corner radius for better visual balance
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp, // Remove elevation as requested
            pressedElevation = 0.dp  // Remove press elevation too
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomPadding) // Use our calculated smart bottom padding
            .scale(scale * songBounceScale) // Combined scale for press and song change bounce
            .graphicsLayer { 
                // Apply translation based on swipe gesture
                translationY = if (isDismissingPlayer) 300f else translationOffset
                alpha = alphaValue
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { 
                        // Reset the last haptic offset on new drag
                        lastHapticOffset = 0f
                        
                        // Initial feedback when starting to drag
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDragEnd = {
                        if (offsetY < -swipeUpThreshold) {
                            // Swipe up detected, open player with stronger feedback
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlayerClick()
                        } else if (offsetY > swipeDownThreshold) {
                            // Swipe down detected, dismiss mini player with stronger feedback
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isDismissingPlayer = true
                        } else {
                            // Snap-back haptic when releasing before threshold
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        // Reset offset if not dismissing
                        if (!isDismissingPlayer) {
                            offsetY = 0f
                        }
                    },
                    onDragCancel = {
                        // Feedback when drag canceled
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // Reset offset if not dismissing
                        if (!isDismissingPlayer) {
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Update offset for both up and down gestures
                        offsetY += dragAmount
                        
                        // Provide interval haptic feedback during drag
                        // For swipe up (negative offsetY)
                        if (offsetY < 0 && abs(offsetY) - abs(lastHapticOffset) > swipeUpThreshold / 3) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticOffset = offsetY
                        }
                        // For swipe down (positive offsetY)
                        else if (offsetY > 0 && abs(offsetY) - abs(lastHapticOffset) > swipeDownThreshold / 3) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticOffset = offsetY
                        }
                    }
                )
            },
        interactionSource = interactionSource
    ) {
        // Display a visual hint when user starts dragging
        val dragUpIndicatorAlpha = if (offsetY < 0) minOf((-offsetY / swipeUpThreshold) * 0.3f, 0.3f) else 0f
        val dragDownIndicatorAlpha = if (offsetY > 0) minOf((offsetY / swipeDownThreshold) * 0.3f, 0.3f) else 0f
        
        Column {
            // Enhanced drag handle indicator with better visual feedback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(40.dp) // Slightly wider for better touch target
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.4f + dragUpIndicatorAlpha + dragDownIndicatorAlpha
                    )
                )
            }

            // Visual indicator for swipe actions with improved positioning
            AnimatedVisibility(
                visible = offsetY < -20f,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = (-offsetY / swipeUpThreshold).coerceIn(0f, 0.8f)),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "⬆ Swipe up for full player",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = offsetY > 20f,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = (offsetY / swipeDownThreshold).coerceIn(0f, 0.8f)),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "⬇ Swipe down to dismiss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress bar for the mini player
            if (song != null) {
                LinearProgressIndicator(
                    progress = { animatedProgress }, // Use lambda for progress
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp) // Added horizontal padding
                        .height(4.dp), // Thinner progress bar
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp), // Increased padding for better spacing
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(16.dp)
            ) {
                // Enhanced album art with no shadows as requested
                Surface(
                    modifier = Modifier
                        .size(56.dp), // Slightly smaller for better proportion
                    shape = RoundedCornerShape(14.dp), // Adjusted corner radius
                    shadowElevation = 0.dp, // Remove shadow as requested
                    tonalElevation = 2.dp, // Keep subtle tonal elevation for depth
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box {
                        if (song != null) {
                            M3ImageUtils.TrackImage(
                                imageUrl = song.artworkUri,
                                trackName = song.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Enhanced "live" badge with better styling
                        if (song?.title?.contains("LIVE", ignoreCase = true) == true || 
                            song?.genre?.contains("live", ignoreCase = true) == true) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
                
                // Enhanced song info with better typography and spacing
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = spacedBy(2.dp) // Tighter spacing
                ) {
                    Text(
                        text = song?.title ?: "No song playing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(6.dp)
                    ) {
                        // Artist info with enhanced styling
                        Text(
                            text = song?.artist ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Compact time indicator with better styling
                        if (song != null && progress > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${formatDuration((progress * song.duration).toLong())}/${formatDuration(song.duration)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                // Enhanced controls with better visual hierarchy and spacing
                Row(
                    horizontalArrangement = spacedBy(10.dp), // Increased spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamic shape play/pause button - rounded square for pause, circle for play
                    FilledIconButton(
                        onClick = {
                            // Enhanced haptic feedback for primary action
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlayPause()
                        },
                        modifier = Modifier.size(44.dp), // Slightly smaller for better proportion
                        shape = if (isPlaying) RoundedCornerShape(18.dp) else CircleShape, // Dynamic shape based on state
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Enhanced next track button with better styling
                    FilledTonalIconButton(
                        onClick = { 
                            // Strong haptic feedback for next track
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSkipNext() 
                        },
                        modifier = Modifier.size(36.dp), // Smaller secondary button
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.SkipNext,
                            contentDescription = "Next track",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format duration from milliseconds to mm:ss format
 */
fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}
