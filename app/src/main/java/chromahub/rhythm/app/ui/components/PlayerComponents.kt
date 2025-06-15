package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.theme.PlayerButtonColor
import chromahub.rhythm.app.ui.theme.PlayerProgressColor
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.concurrent.TimeUnit
import chromahub.rhythm.app.ui.components.WaveSlider
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding

/**
 * Main player screen with album art, progress bar, and controls
 */
@Composable
fun Player(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onLocationClick: () -> Unit,
    onQueueClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // For swipe down gesture detection
    var offsetY by remember { mutableStateOf(0f) }
    val swipeThreshold = 150f // Higher threshold for closing player
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY > swipeThreshold) {
                            // Swipe down detected, close player
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        }
                        // Reset offset
                        offsetY = 0f
                    },
                    onDragCancel = {
                        // Reset offset
                        offsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Update offset (only care about downward movement - positive dragAmount)
                        offsetY += dragAmount
                        
                        // If user is dragging upward, don't respond
                        if (dragAmount < 0) {
                            offsetY = 0f
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swipe down indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (offsetY > 0) 0.3f + minOf((offsetY / swipeThreshold) * 0.5f, 0.5f) else 0.3f
                )
            )
        }
        
        // Visual indicator for swipe down action
        if (offsetY > 50f) {
            Text(
                text = "Swipe down to close",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = (offsetY / swipeThreshold).coerceIn(0f, 1f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Album art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (song != null) {
                M3ImageUtils.TrackImage(
                    imageUrl = song.artworkUri,
                    trackName = song.title,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = RhythmIcons.Album,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress bar with wavy design
        WaveSlider(
            value = progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth()
        )

        // Time display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentTime = formatDuration((progress * (song?.duration ?: 0)).toLong())
            val totalTime = formatDuration(song?.duration ?: 0)
            
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = totalTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Player controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip back 30 seconds
            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipPrevious,
                    contentDescription = "Skip to previous",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Play/pause button (larger)
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(PlayerButtonColor)
            ) {
                Icon(
                    imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Skip forward 30 seconds
            IconButton(
                onClick = onSkipNext,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipNext,
                    contentDescription = "Skip to next",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Volume controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Mute */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.VolumeOff,
                    contentDescription = "Mute",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Slider(
                    value = 0.7f,
                    onValueChange = { /* Volume change */ },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            IconButton(
                onClick = { /* Max volume */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.VolumeUp,
                    contentDescription = "Max volume",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom row with location and queue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Living room button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onLocationClick
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.Location,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Living room",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Queue button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onQueueClick
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.Queue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Queue",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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

    // Get the navigation bar insets
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Card(
        onClick = {
            if (!isDismissingPlayer) {
                // Enhanced haptic feedback for click
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlayerClick()
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // Add padding for navigation bar to prevent overlap
            .windowInsetsPadding(WindowInsets.navigationBars)
            .scale(scale)
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
            // Drag handle indicator to show it's swipeable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.3f + dragUpIndicatorAlpha + dragDownIndicatorAlpha
                    )
                )
            }
            
            // Visual indicator for swipe actions
            if (offsetY < -20f) {
                Text(
                    text = "Swipe up for player",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = (-offsetY / swipeUpThreshold).coerceIn(0f, 1f)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else if (offsetY > 20f) {
                Text(
                    text = "Swipe down to dismiss",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = (offsetY / swipeDownThreshold).coerceIn(0f, 1f)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Update the progress indicator to use M3LinearLoader
            M3LinearLoader(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                showTrackGap = true,
                showStopIndicator = true
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(16.dp)
            ) {
                // Album art with elevation
                Surface(
                    modifier = Modifier
                        .size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 4.dp,
                    tonalElevation = 4.dp,
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Show a "live" badge if needed
                        if (song?.title?.contains("LIVE", ignoreCase = true) == true || 
                            song?.genre?.contains("live", ignoreCase = true) == true) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onError,
                                )
                            }
                        }
                    }
                }
                
                // Song info with vertical spacing
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = spacedBy(4.dp)
                ) {
                    Text(
                        text = song?.title ?: "No song playing",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(8.dp)
                    ) {
                        // Artist info
                        Text(
                            text = song?.artist ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Time indicator (current / total)
                        if (song != null) {
                            Text(
                                text = "${formatDuration((progress * (song.duration)).toLong())} / ${formatDuration(song.duration)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                
                // Controls with improved visual hierarchy
                Row(
                    horizontalArrangement = spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/pause button
                    FilledIconButton(
                        onClick = { 
                            // Enhanced haptic feedback for primary action
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlayPause() 
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Next track button
                    FilledTonalIconButton(
                        onClick = { 
                            // Strong haptic feedback for next track
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSkipNext() 
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.SkipNext,
                            contentDescription = "Next track",
                            modifier = Modifier.size(20.dp)
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

@Composable
fun PlayerControls(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onLocationClick: () -> Unit,
    onQueueClick: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // For swipe down gesture detection
    var offsetY by remember { mutableStateOf(0f) }
    val swipeThreshold = 150f // Higher threshold for closing player
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY > swipeThreshold) {
                            // Swipe down detected, close player
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        }
                        // Reset offset
                        offsetY = 0f
                    },
                    onDragCancel = {
                        // Reset offset
                        offsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Update offset (only care about downward movement - positive dragAmount)
                        offsetY += dragAmount
                        
                        // If user is dragging upward, don't respond
                        if (dragAmount < 0) {
                            offsetY = 0f
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swipe down indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (offsetY > 0) 0.3f + minOf((offsetY / swipeThreshold) * 0.5f, 0.5f) else 0.3f
                )
            )
        }
        
        // Visual indicator for swipe down action
        if (offsetY > 50f) {
            Text(
                text = "Swipe down to close",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = (offsetY / swipeThreshold).coerceIn(0f, 1f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Album art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (song != null) {
                M3ImageUtils.TrackImage(
                    imageUrl = song.artworkUri,
                    trackName = song.title,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = RhythmIcons.Album,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress bar with wavy design
        WaveSlider(
            value = progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth()
        )

        // Time display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentTime = formatDuration((progress * (song?.duration ?: 0)).toLong())
            val totalTime = formatDuration(song?.duration ?: 0)
            
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = totalTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Player controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip back 30 seconds
            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipPrevious,
                    contentDescription = "Skip to previous",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Play/pause button (larger)
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(PlayerButtonColor)
            ) {
                Icon(
                    imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Skip forward 30 seconds
            IconButton(
                onClick = onSkipNext,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.SkipNext,
                    contentDescription = "Skip to next",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Volume controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Mute */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.VolumeOff,
                    contentDescription = "Mute",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Slider(
                    value = 0.7f,
                    onValueChange = { /* Volume change */ },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            IconButton(
                onClick = { /* Max volume */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = RhythmIcons.VolumeUp,
                    contentDescription = "Max volume",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom row with location and queue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Living room button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onLocationClick
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.Location,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Living room",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Queue button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onQueueClick
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.Queue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Queue",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 