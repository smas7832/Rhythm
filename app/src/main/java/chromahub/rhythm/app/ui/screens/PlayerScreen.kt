package chromahub.rhythm.app.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.PlaybackLocation
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.WaveSlider
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.theme.PlayerButtonColor
import chromahub.rhythm.app.util.ImageUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import kotlin.math.abs
import chromahub.rhythm.app.ui.components.M3CircularLoader

// Add the calculateOvershootInterpolation function
/**
 * Calculates overshooting interpolation similar to Android's OvershootInterpolator
 * Used to create bouncy animation effects in a Compose-friendly way
 */
private fun calculateOvershootInterpolation(fraction: Float, tension: Float = 1.5f): Float {
    // This simulates the overshoot effect with a simple formula
    if (fraction < 0.6f) {
        // Accelerate phase
        return fraction / 0.6f
    } else {
        // Overshoot and settle phase
        val x = (fraction - 0.6f) / 0.4f // normalized from 0 to 1 in the overshoot range
        return 1f + tension * x * (1 - x) * Math.sin(Math.PI * x).toFloat()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    location: PlaybackLocation?,
    queuePosition: Int = 1,
    queueTotal: Int = 1,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onBack: () -> Unit,
    onLocationClick: () -> Unit,
    onQueueClick: () -> Unit,
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    isShuffleEnabled: Boolean = false,
    repeatMode: Int = 0,
    isFavorite: Boolean = false,
    showLyrics: Boolean = true,
    onlineOnlyLyrics: Boolean = false,
    lyrics: String? = null,
    isLoadingLyrics: Boolean = false,
    volume: Float = 0.7f,
    isMuted: Boolean = false,
    onVolumeChange: (Float) -> Unit = {},
    onToggleMute: () -> Unit = {},
    onMaxVolume: () -> Unit = {},
    playlists: List<Playlist> = emptyList(),
    queue: List<Song> = emptyList(),
    onSongClick: (Song) -> Unit = {},
    onRemoveFromQueue: (Song) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onAddSongsToQueue: () -> Unit = {}, // This will be the navigation action
    onNavigateToLibrary: (LibraryTab) -> Unit = {}, // New parameter for navigation
    showAddToPlaylistSheet: Boolean = false,
    onAddToPlaylistSheetDismiss: () -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = { _ -> },
    onShowCreatePlaylistDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Calculate screen dimensions
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    
    // Use screen size to determine layout constraints
    val isCompactHeight = configuration.screenHeightDp < 600
    val albumArtSize = if (isCompactHeight) 0.55f else 0.7f
    
    // Animation for album art entry
    var showAlbumArt by remember { mutableStateOf(false) }
    
    // Toggle between album art and lyrics
    var showLyricsView by remember { mutableStateOf(false) }
    
    // Bottom sheet states
    val queueSheetState = rememberModalBottomSheetState()
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    var showQueueSheet by remember { mutableStateOf(false) }
    
    // For swipe down gesture detection - improved parameters
    var offsetY by remember { mutableStateOf(0f) }
    val swipeThreshold = 120f // Lower threshold for easier closing
    
    // Animation for dismiss swipe gesture
    val dismissProgress = (offsetY / swipeThreshold).coerceIn(0f, 1f)
    val contentAlpha by animateFloatAsState(
        targetValue = 1f - (dismissProgress * 0.4f), // Slightly more aggressive fade
        label = "contentAlpha"
    )
    
    // Scale effect for swipe
    val contentScale by animateFloatAsState(
        targetValue = 1f - (dismissProgress * 0.05f), // Subtle scale effect
        label = "contentScale"
    )
    
    // For dismissing animation
    var isDismissing by remember { mutableStateOf(false) }
    
    // Handle dismissing animation
    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            delay(50) // Shorter delay for more responsive feel
            onBack()
        }
    }
    
    // Format time as mm:ss
    val formatTime = { timeMs: Long ->
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        String.format("%d:%02d", minutes, seconds)
    }
    
    // Calculate current and total time
    val currentTimeMs = ((song?.duration ?: 0) * progress).toLong()
    val totalTimeMs = song?.duration ?: 0
    
    // Format current and total time
    val currentTimeFormatted = formatTime(currentTimeMs)
    val totalTimeFormatted = formatTime(totalTimeMs)
    
    LaunchedEffect(song?.id) {
        // Reset animation when song changes
        showAlbumArt = false
        delay(100)
        showAlbumArt = true
        
        // Reset to album art view when song changes
        showLyricsView = false
    }
    
    val albumScale by animateFloatAsState(
        targetValue = if (showAlbumArt) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "albumScale"
    )
    
    val albumAlpha by animateFloatAsState(
        targetValue = if (showAlbumArt) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "albumAlpha"
    )
    
    // Find which playlist the current song belongs to
    val songPlaylist = remember(song, playlists) {
        if (song == null) null
        else playlists.find { playlist ->
            playlist.songs.any { it.id == song.id }
        }
    }

    // Show bottom sheets if needed
    if (showQueueSheet && song != null) {
        QueueBottomSheet(
            currentSong = song,
            queue = queue,
            currentQueueIndex = queuePosition - 1,
            onSongClick = { selectedSong -> 
                // Play the selected song
                onSongClick(selectedSong)
                // Hide the sheet after selection
                scope.launch {
                    queueSheetState.hide()
                }.invokeOnCompletion {
                    if (!queueSheetState.isVisible) {
                        showQueueSheet = false
                    }
                }
            },
            onDismiss = { showQueueSheet = false },
            onRemoveSong = { songToRemove ->
                // Remove the song from the queue
                onRemoveFromQueue(songToRemove)
            },
            onMoveQueueItem = { fromIndex, toIndex ->
                // Move queue item in the actual queue
                onMoveQueueItem(fromIndex, toIndex)
            },
            onAddSongsClick = { // Changed parameter name
                // Dismiss the queue sheet first
                scope.launch {
                    queueSheetState.hide()
                }.invokeOnCompletion {
                    if (!queueSheetState.isVisible) {
                        // Navigate to the LibraryScreen (Songs tab)
                        onNavigateToLibrary(LibraryTab.SONGS)
                    }
                }
            },
            sheetState = queueSheetState
        )
    }
    
    if (showAddToPlaylistSheet && song != null) {
        AddToPlaylistBottomSheet(
            song = song,
            playlists = playlists,
            onDismiss = onAddToPlaylistSheetDismiss,
            onAddToPlaylist = { playlist ->
                // Add the current song to the selected playlist
                playlist.id?.let { playlistId ->
                    // Actually add the song to the playlist
                    onAddSongToPlaylist(song, playlistId)
                    // Hide the bottom sheet after adding
                    scope.launch {
                        addToPlaylistSheetState.hide()
                    }.invokeOnCompletion {
                        if (!addToPlaylistSheetState.isVisible) {
                            onAddToPlaylistSheetDismiss()
                        }
                    }
                }
            },
            onCreateNewPlaylist = {
                // Hide the sheet first
                scope.launch {
                    addToPlaylistSheetState.hide()
                }.invokeOnCompletion {
                    if (!addToPlaylistSheetState.isVisible) {
                        onAddToPlaylistSheetDismiss()
                        // Now show the create playlist dialog
                        // We'll handle this in the parent component
                        onShowCreatePlaylistDialog()
                    }
                }
            },
            sheetState = addToPlaylistSheetState
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Add swipe indicator pill - similar to mini player
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                .padding(bottom = 8.dp)
                        )
                        
                        // Small spacing after the pill
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        
                        if (song != null && songPlaylist != null) {
                            Text(
                                text = songPlaylist.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 48.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    // Increased horizontal padding for better edge spacing
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        FilledTonalIconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Back,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    if (showLyrics && song != null) {
                        // Increased horizontal padding for better edge spacing
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            FilledTonalIconButton(
                                onClick = { showLyricsView = !showLyricsView },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = if (showLyricsView) RhythmIcons.Album else RhythmIcons.Queue,
                                    contentDescription = if (showLyricsView) "Show Album Art" else "Show Lyrics",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp)) // Match the size of the button + padding
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = Modifier
            .graphicsLayer {
                // Apply translation and alpha effects based on swipe gesture
                translationY = offsetY
                alpha = contentAlpha
                scaleX = contentScale
                scaleY = contentScale
            }
    ) { paddingValues ->
        // Use Box as the root container to allow absolute positioning
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    var totalDragDistance = 0f
                    var initialDragY = 0f
                    
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            totalDragDistance = 0f
                            initialDragY = offset.y
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume() // Important: consume the gesture to prevent conflicts
                            
                            // Only track downward swipes for dismissal
                            if (dragAmount > 0) {
                                totalDragDistance += dragAmount
                                // Apply resistance effect - slower movement as user drags further
                                offsetY = (totalDragDistance * 0.6f).coerceIn(0f, screenHeight / 2)
                            }
                        },
                        onDragEnd = {
                            // If we've dragged enough, close the player with haptic feedback
                            if (totalDragDistance > swipeThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDismissing = true
                            } else {
                                // Reset position with animation using Compose-friendly approach
                                scope.launch {
                                    // Smoothly animate back to position
                                    val startValue = offsetY
                                    val animationDuration = 300 // longer duration for bouncy feel
                                    val startTime = System.currentTimeMillis()
                                    
                                    while (true) {
                                        val elapsedTime = System.currentTimeMillis() - startTime
                                        if (elapsedTime >= animationDuration) {
                                            offsetY = 0f
                                            break
                                        }
                                        
                                        val fraction = (elapsedTime / animationDuration.toFloat()).coerceIn(0f, 1f)
                                        // Apply overshooting effect by adjusting the fraction
                                        val adjustedFraction = calculateOvershootInterpolation(fraction, 2.0f) // More pronounced bounce
                                        offsetY = startValue * (1 - adjustedFraction)
                                        
                                        // Wait for next frame
                                        delay(16) // roughly 60fps
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            // Reset position immediately if not dismissing
                            scope.launch {
                                offsetY = 0f
                            }
                        }
                    )
                }
        ) {
            // Add swipe down indicator text when dragging down
            if (offsetY > swipeThreshold * 0.3f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    val textAlpha = ((offsetY - (swipeThreshold * 0.3f)) / (swipeThreshold * 0.7f)).coerceIn(0f, 1f)
                    Text(
                        text = "Release to close",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = textAlpha),
                        modifier = Modifier.graphicsLayer {
                            this.alpha = textAlpha
                        }
                    )
                }
            }
            
            // MAIN CHANGE: Use a Column that fills the available space but anchors content to the bottom
            // This makes all content stick to the bottom of the screen
            Column( // This outer column will now manage the vertical distribution
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally // Keep this for horizontal centering
            ) {
                // Main content column - now takes up all available space
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // This makes it take all available vertical space
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Calculate dynamic top padding based on screen height
                    val dynamicTopPadding = with(density) {
                        // Use screen height to determine padding
                        // The taller the screen, the more padding we add at the top
                        (configuration.screenHeightDp * 0.03f).dp
                    }
                    
                    // Add dynamic spacing at the top that expands/contracts based on screen size
                    Spacer(modifier = Modifier.height(dynamicTopPadding))
                    
                    // Album artwork or lyrics view with reduced top padding
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth(albumArtSize)
                            .padding(horizontal = 16.dp, vertical = 8.dp), // Reduced vertical padding
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 8.dp
                        ),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            if (!showLyricsView) {
                                // Album artwork with animations
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = albumScale,
                                            scaleY = albumScale,
                                            alpha = albumAlpha
                                        )
                                ) {
                                    if (song?.artworkUri != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .apply(ImageUtils.buildImageRequest(
                                                    song.artworkUri,
                                                    song.title,
                                                    context.cacheDir,
                                                    ImageUtils.PlaceholderType.TRACK
                                                ))
                                                .build(),
                                            contentDescription = "Album artwork for ${song.title}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(28.dp))
                                        )
                                    } else {
                                        // Fallback album art
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = RhythmIcons.Album,
                                                contentDescription = "Album art",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.fillMaxSize(0.5f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Lyrics view
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isLoadingLyrics -> {
                                            M3CircularLoader(
                                                modifier = Modifier.size(48.dp),
                                                fourColor = true
                                            )
                                        }
                                        lyrics != null -> {
                                            // Keep scroll only for lyrics view
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                Text(
                                                    text = lyrics,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        else -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = RhythmIcons.MusicNote,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = if (onlineOnlyLyrics) 
                                                        "No lyrics available.\nConnect to the internet to view lyrics." 
                                                    else 
                                                        "No lyrics available for this song.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 12.dp))
                    
                    AnimatedVisibility(
                        visible = song != null,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it }
                    ) {
                        if (song != null) {
                            // Song info with reduced padding
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp) // Reduced vertical padding
                            ) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Text(
                                    text = "${song.artist} • ${song.album}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = 2.dp) // Reduced vertical padding
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))
                    
                    // Progress slider and time indicators
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        // Progress slider
                        WaveSlider(
                            value = progress,
                            onValueChange = onSeek,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Time indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp), // Reduced vertical padding
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentTimeFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Text(
                                text = totalTimeFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))
                    
                    // Player controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle button with badge if enabled
                        BadgedBox(
                            badge = {
                                if (isShuffleEnabled) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { 
                                    // Add haptic feedback
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleShuffle() 
                                }
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Shuffle,
                                    contentDescription = "Toggle shuffle",
                                    tint = if (isShuffleEnabled) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Skip previous button
                        FilledTonalIconButton(
                            onClick = { 
                                // Add haptic feedback
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSkipPrevious() 
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = RhythmIcons.SkipPrevious,
                                contentDescription = "Previous track"
                            )
                        }
                        
                        // Play/pause button (larger)
                        FilledIconButton(
                            onClick = {
                                // Add haptic feedback with stronger intensity for primary control
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPlayPause()
                            },
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Skip next button
                        FilledTonalIconButton(
                            onClick = { 
                                // Add haptic feedback
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSkipNext() 
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = RhythmIcons.SkipNext,
                                contentDescription = "Next track"
                            )
                        }
                        
                        // Repeat button with badge for mode
                        BadgedBox(
                            badge = {
                                if (repeatMode > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        if (repeatMode == 1) {
                                            Text("1", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { 
                                    // Add haptic feedback
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleRepeat() 
                                }
                            ) {
                                // Show different icons based on repeat mode
                                val icon = when (repeatMode) {
                                    1 -> RhythmIcons.RepeatOne  // REPEAT_MODE_ONE
                                    2 -> RhythmIcons.Repeat     // REPEAT_MODE_ALL
                                    else -> RhythmIcons.Repeat  // REPEAT_MODE_OFF
                                }
                                
                                val tint = when (repeatMode) {
                                    0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // OFF - dimmed
                                    else -> MaterialTheme.colorScheme.primary                   // ON - highlighted
                                }
                                
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Toggle repeat mode",
                                    tint = tint
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))
                    
                    // Volume controls with more compact design
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(horizontal = 16.dp, vertical = 2.dp), // Reduced vertical padding
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute button
                            IconButton(
                                onClick = { 
                                    // Add haptic feedback
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleMute() 
                                }
                            ) {
                                Icon(
                                    imageVector = if (isMuted) RhythmIcons.VolumeOff else 
                                                if (volume < 0.3f) RhythmIcons.VolumeMute else 
                                                if (volume < 0.7f) RhythmIcons.VolumeDown else 
                                                RhythmIcons.VolumeUp,
                                    contentDescription = "Toggle mute",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Volume slider with haptic feedback
                            Slider(
                                value = if (isMuted) 0f else volume,
                                onValueChange = { newValue ->
                                    // Add subtle haptic feedback when dragging the slider
                                    if (abs(newValue - volume) > 0.05f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onVolumeChange(newValue)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            
                            // Max volume button
                            IconButton(
                                onClick = { 
                                    // Add haptic feedback
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onMaxVolume() 
                                }
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.VolumeUp,
                                    contentDescription = "Max volume",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Only add favorite/playlist buttons if we have space
                    if (!isCompactHeight) {
                        // Action buttons row - only show on taller screens with slightly reduced spacing
                        Spacer(modifier = Modifier.height(12.dp)) // Reduced from 16.dp
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Favorite button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FilledTonalIconButton(
                                    onClick = { 
                                        // Add haptic feedback with heart effect for favorite
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onToggleFavorite() 
                                    },
                                    modifier = Modifier.size(48.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) RhythmIcons.FavoriteFilled else RhythmIcons.Favorite,
                                        contentDescription = "Toggle favorite",
                                        tint = if (isFavorite) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Text(
                                    text = "Favorite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            // Add to playlist button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FilledTonalIconButton(
                                    onClick = { 
                                        // Add haptic feedback
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // Call onAddToPlaylist directly instead of setting showAddToPlaylistSheet
                                        onAddToPlaylist()
                                    },
                                    modifier = Modifier.size(48.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.AddToPlaylist,
                                        contentDescription = "Add to playlist"
                                    )
                                }
                                Text(
                                    text = "Add to Playlist",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Bottom buttons - fixed position at the bottom of the screen
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Location button with rounded pill shape like in the screenshot
                        Card(
                            onClick = {
                                // Add haptic feedback 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLocationClick()
                            },
                            shape = RoundedCornerShape(24.dp), // Rounded pill shape
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Choose icon based on device type
                                val icon = when {
                                    location?.id?.startsWith("bt_") == true -> RhythmIcons.BluetoothFilled
                                    location?.id == "wired_headset" -> RhythmIcons.HeadphonesFilled
                                    location?.id == "speaker" -> RhythmIcons.SpeakerFilled
                                    else -> RhythmIcons.Location
                                }
                                
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = location?.name ?: "Choose device",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        // Queue button - simplified for compact screens - same style
                        Card(
                            onClick = { 
                                // Add haptic feedback
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Show the queue bottom sheet instead of navigating
                                if (song != null) {
                                    showQueueSheet = true
                                } else {
                                    // Fall back to the original behavior if no song
                                    onQueueClick()
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Queue,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Queue ($queuePosition/$queueTotal)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    currentSong: Song?,
    queue: List<Song>,
    currentQueueIndex: Int = 0,
    onSongClick: (Song) -> Unit,
    onDismiss: () -> Unit,
    onRemoveSong: (Song) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onAddSongsClick: () -> Unit = {}, // Changed parameter name
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create a mutable queue for reordering that updates when the queue changes
    val mutableQueue = remember(queue) { queue.toMutableStateList() }
    
    // Set up reorderable state for drag and drop functionality
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            try {
                if (from.index < mutableQueue.size && to.index < mutableQueue.size) {
                    // Move the item in the UI first for immediate feedback
                    mutableQueue.apply {
                        add(to.index, removeAt(from.index))
                    }
                    // Call the callback to update the actual queue in ViewModel
                    onMoveQueueItem(from.index, to.index)
                }
            } catch (e: Exception) {
                Log.e("QueueBottomSheet", "Error during drag reorder", e)
            }
        }
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header with title and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Queue",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${mutableQueue.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Add songs button
                FilledTonalIconButton(
                    onClick = onAddSongsClick, // Changed onClick to use new parameter
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add songs"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (mutableQueue.isEmpty()) {
                EmptyQueueContent(onAddSongsClick)
            } else {
                // Now Playing section - show current song separately
                currentSong?.let { song ->
                    NowPlayingCard(song)
                }
                
                // Only show "UP NEXT" and the rest of the queue if there's more than one song
                if (mutableQueue.size > 1) {
                    // Queue header for upcoming songs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UP NEXT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    
                    // Queue list with reordering
                    LazyColumn(
                        state = reorderableState.listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .reorderable(reorderableState),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(
                            items = mutableQueue,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            // Skip the current song as it's already shown in the NowPlayingCard
                            if (currentSong == null || song.id != currentSong.id) {
                                ReorderableItem(reorderableState, key = song.id) { isDragging ->
                                    QueueItem(
                                        song = song,
                                        index = index,
                                        isDragging = isDragging,
                                        onSongClick = { onSongClick(song) },
                                        onRemove = { 
                                            try {
                                                // First update local UI state for immediate feedback
                                                val indexToRemove = mutableQueue.indexOf(song)
                                                if (indexToRemove >= 0 && indexToRemove < mutableQueue.size) {
                                                    mutableQueue.removeAt(indexToRemove)
                                                }
                                                // Then update the actual queue via ViewModel
                                                onRemoveSong(song)
                                            } catch (e: Exception) {
                                                Log.e("QueueBottomSheet", "Error removing item", e)
                                            }
                                        },
                                        reorderableState = reorderableState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyQueueContent(onAddSongsClick: () -> Unit) { // Changed parameter name
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = RhythmIcons.Queue,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your queue is empty",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(onClick = onAddSongsClick) { // Changed onClick to use new parameter
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add songs")
            }
        }
    }
}

@Composable
private fun NowPlayingCard(song: Song) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art with subtle elevation
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .apply(ImageUtils.buildImageRequest(
                                song.artworkUri,
                                song.title,
                                context.cacheDir,
                                ImageUtils.PlaceholderType.TRACK
                            ))
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Song info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistBottomSheet(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Song card at the top
            SongToAddCard(song)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create new playlist button
            CreatePlaylistButton(onCreateNewPlaylist)
            
            // Your playlists section
            if (playlists.isNotEmpty()) {
                PlaylistsHeader(playlists.size)
                
                // Playlist grid
                PlaylistGrid(
                    playlists = playlists,
                    onPlaylistClick = onAddToPlaylist
                )
            } else {
                EmptyPlaylistsState()
            }
        }
    }
}

@Composable
private fun SongToAddCard(song: Song) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 0.dp
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .apply(ImageUtils.buildImageRequest(
                            song.artworkUri,
                            song.title,
                            LocalContext.current.cacheDir,
                            ImageUtils.PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Song info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Add to playlist",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CreatePlaylistButton(onClick: () -> Unit) {
    // Accent-filled card without elevation
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading accent icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Create New Playlist",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = RhythmIcons.Forward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PlaylistsHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "YOUR PLAYLISTS",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PlaylistGrid(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(playlists.chunked(2)) { rowPlaylists ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPlaylists.forEach { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // If there's an odd number of playlists, add an empty space
                if (rowPlaylists.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaylistsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = RhythmIcons.Playlist,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No playlists yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create a playlist to add songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(170.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Artwork or placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.artworkUri != null) {
                    AsyncImage(
                        model = playlist.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = RhythmIcons.Playlist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Playlist info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${playlist.songs.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerLocationsScreen(
    locations: List<PlaybackLocation>,
    currentLocation: PlaybackLocation?,
    onLocationSelect: (PlaybackLocation) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Choose Output Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = RhythmIcons.Back,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (locations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No devices available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(locations) { location ->
                    val isSelected = currentLocation?.id == location.id
                    
                    Surface(
                        onClick = { onLocationSelect(location) },
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else 
                            Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Device icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Choose icon based on device type
                                val icon = when {
                                    location.id.startsWith("bt_") -> RhythmIcons.Bluetooth
                                    location.id == "wired_headset" -> RhythmIcons.Headphones
                                    location.id == "speaker" -> RhythmIcons.Speaker
                                    else -> RhythmIcons.Location
                                }
                                
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) 
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Device info
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Device name
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary
                                    else 
                                        MaterialTheme.colorScheme.onBackground
                                )
                                
                                // Device type
                                val deviceType = when {
                                    location.id.startsWith("bt_") -> "Bluetooth"
                                    location.id == "wired_headset" -> "Wired Headphones"
                                    location.id == "speaker" -> "Built-in Speaker"
                                    else -> "Audio Device"
                                }
                                
                                Text(
                                    text = deviceType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                            
                            // Selected indicator
                            if (isSelected) {
                                Icon(
                                    imageVector = RhythmIcons.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueItem(
    song: Song,
    index: Int,
    isDragging: Boolean,
    onSongClick: () -> Unit,
    onRemove: () -> Unit,
    reorderableState: ReorderableLazyListState
) {
    val context = LocalContext.current
    
    OutlinedCard(
        onClick = onSongClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isDragging) 
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDragging)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Queue position with drag handle - make it obvious it can be dragged
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .detectReorderAfterLongPress(reorderableState),
                contentAlignment = Alignment.Center
            ) {
                if (isDragging) {
                    // Show drag handle when dragging
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    // Show position number when not dragging
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Album art with smaller size
            Surface(
                modifier = Modifier
                    .size(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            song.artworkUri,
                            song.title,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            
            // Song info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Remove button with improved style and feedback
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove from queue",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
