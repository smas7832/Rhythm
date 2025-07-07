package chromahub.rhythm.app.ui.screens

import android.util.Log
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.data.PlaybackLocation
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.WaveSlider
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.theme.PlayerButtonColor
import chromahub.rhythm.app.ui.components.M3PlaceholderType
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
import android.view.animation.OvershootInterpolator
import chromahub.rhythm.app.ui.components.SyncedLyricsView // Import SyncedLyricsView

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
    locations: List<PlaybackLocation> = emptyList(),
    onLocationSelect: (PlaybackLocation) -> Unit = {},
    volume: Float = 0.7f,
    isMuted: Boolean = false,
    onVolumeChange: (Float) -> Unit = {},
    onToggleMute: () -> Unit = {},
    onMaxVolume: () -> Unit = {},
    onRefreshDevices: () -> Unit = {},
    onStopDeviceMonitoring: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    isShuffleEnabled: Boolean = false,
    repeatMode: Int = 0,
    isFavorite: Boolean = false,
    showLyrics: Boolean = true,
    onlineOnlyLyrics: Boolean = false,
    lyrics: chromahub.rhythm.app.data.LyricsData? = null,
    isLoadingLyrics: Boolean = false,
    playlists: List<Playlist> = emptyList(),
    queue: List<Song> = emptyList(),
    onSongClick: (Song) -> Unit = {},
    onRemoveFromQueue: (Song) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onAddSongsToQueue: () -> Unit = {},
    onNavigateToLibrary: (LibraryTab) -> Unit = {},
    showAddToPlaylistSheet: Boolean = false,
    onAddToPlaylistSheetDismiss: () -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = { _ -> },
    onShowCreatePlaylistDialog: () -> Unit = {} ,
    onClearQueue: () -> Unit = {}
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
    val deviceOutputSheetState = rememberModalBottomSheetState()
    var showQueueSheet by remember { mutableStateOf(false) }
    var showDeviceOutputSheet by remember { mutableStateOf(false) }
    
    // For swipe down gesture detection - improved parameters
    var targetOffsetY by remember { mutableStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = tween(
            durationMillis = 300,
            easing = { fraction ->
                OvershootInterpolator(2.0f).getInterpolation(fraction)
            }
        ),
        label = "animatedOffsetY"
    )
    val swipeThreshold = 120f
    
    // Animation for dismiss swipe gesture
    val dismissProgress = (animatedOffsetY / swipeThreshold).coerceIn(0f, 1f)
    val contentAlpha by animateFloatAsState(
        targetValue = 1f - (dismissProgress * 0.4f),
        label = "contentAlpha"
    )
    
    // Scale effect for swipe
    val contentScale by animateFloatAsState(
        targetValue = 1f - (dismissProgress * 0.05f),
        label = "contentScale"
    )
    
    // For dismissing animation
    var isDismissing by remember { mutableStateOf(false) }
    
    // Handle dismissing animation
    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            delay(50)
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
    
    // Start device monitoring when player screen is shown and stop when closed
    LaunchedEffect(Unit) {
        onRefreshDevices()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            onStopDeviceMonitoring()
        }
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
            onAddSongsClick = {
                // Dismiss the queue sheet first
                scope.launch {
                    queueSheetState.hide()
                }.invokeOnCompletion {
                    if (!queueSheetState.isVisible) {
                        // Navigate to the LibraryScreen (Songs tab) with a hint about adding songs
                        onNavigateToLibrary(LibraryTab.SONGS)
                    }
                }
            },
            onClearQueue = {
                // Clear the queue and dismiss the sheet
                onClearQueue()
                scope.launch {
                    queueSheetState.hide()
                }.invokeOnCompletion {
                    if (!queueSheetState.isVisible) {
                        showQueueSheet = false
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
            onDismissRequest = onAddToPlaylistSheetDismiss,
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

    // Device Output Bottom Sheet
    if (showDeviceOutputSheet) {
        LaunchedEffect(showDeviceOutputSheet) {
            if (showDeviceOutputSheet) {
                onRefreshDevices()
            }
        }
        
        DeviceOutputBottomSheet(
            locations = locations,
            currentLocation = location,
            volume = volume,
            isMuted = isMuted,
            onLocationSelect = { selectedLocation ->
                onLocationSelect(selectedLocation)
                // Hide the sheet after selection
                scope.launch {
                    deviceOutputSheetState.hide()
                }.invokeOnCompletion {
                    if (!deviceOutputSheetState.isVisible) {
                        showDeviceOutputSheet = false
                        onStopDeviceMonitoring()
                    }
                }
            },
            onVolumeChange = onVolumeChange,
            onToggleMute = onToggleMute,
            onMaxVolume = onMaxVolume,
            onRefreshDevices = onRefreshDevices,
            onDismiss = { 
                showDeviceOutputSheet = false
                onStopDeviceMonitoring()
            },
            sheetState = deviceOutputSheetState
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                        )
                        
                        // Small spacing after the pill
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        AnimatedVisibility(
                            visible = song != null,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        }
                    }
                },
                navigationIcon = {
                    // Increased horizontal padding for better edge spacing
                    Box(modifier = Modifier.padding(start = 16.dp)) {
                        FilledTonalIconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp),
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
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            FilledTonalIconButton(
                                onClick = { showLyricsView = !showLyricsView },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = if (showLyricsView) RhythmIcons.Album else RhythmIcons.MusicNote,
                                    contentDescription = if (showLyricsView) "Show Album Art" else "Show Lyrics",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = Modifier
            .graphicsLayer {
                // Apply translation and alpha effects based on swipe gesture
                translationY = animatedOffsetY
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
                    var dragAmountY = 0f
                    
                    detectVerticalDragGestures(
                        onDragStart = {
                            dragAmountY = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            
                            // Only track downward swipes for dismissal
                            if (dragAmount > 0) {
                                dragAmountY += dragAmount
                                // Apply resistance effect - slower movement as user drags further
                                targetOffsetY = (dragAmountY * 0.6f).coerceIn(0f, screenHeight / 2)
                            }
                        },
                        onDragEnd = {
                            // If we've dragged enough, close the player with haptic feedback
                            if (dragAmountY > swipeThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDismissing = true
                            } else {
                                targetOffsetY = 0f
                            }
                        },
                        onDragCancel = {
                            // Reset position immediately if not dismissing
                            targetOffsetY = 0f
                        }
                    )
                }
        ) {
            // Add swipe down indicator text when dragging down
            if (animatedOffsetY > swipeThreshold * 0.3f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    val textAlpha = ((animatedOffsetY - (swipeThreshold * 0.3f)) / (swipeThreshold * 0.7f)).coerceIn(0f, 1f)
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main content column - now takes up all available space
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (isCompactHeight) Arrangement.Center else Arrangement.Top
                ) {
                    // Calculate dynamic top padding based on screen height
                    val dynamicTopPadding = with(density) {
                        // Use screen height to determine padding
                        // The taller the screen, the more padding we add at the top
                        (configuration.screenHeightDp * 0.03f).dp
                    }
                    
                    // Add dynamic spacing at the top that expands/contracts based on screen size
                    if (!isCompactHeight) {
                        Spacer(modifier = Modifier.height(dynamicTopPadding))
                    }
                    
                    // Album artwork or lyrics view with smooth transitions
                    AnimatedVisibility(
                        visible = !showLyricsView,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300))
                    ) {
                        // Album artwork with increased top padding
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth(albumArtSize)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                            M3PlaceholderType.TRACK
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
                            }
                        }
                    }
                    
                    // Lyrics view with animation
                    AnimatedVisibility(
                        visible = showLyricsView,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300))
                    ) {
                        // Lyrics view - full width and height for better readability with themed background
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompactHeight) 300.dp else 400.dp)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            // Background with album art blur effect
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Background image with blur effect
                                if (song?.artworkUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(song.artworkUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.6f,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(28.dp))
                                            .graphicsLayer {
                                                // Scale up slightly to create a softer look
                                                scaleX = 1.1f
                                                scaleY = 1.1f
                                            }
                                    )
                                } else {
                                    // Fallback gradient background when no album art
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
                                                    )
                                                ),
                                                RoundedCornerShape(28.dp)
                                            )
                                    )
                                }
                                
                                // Overlay with semi-transparent background for text readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(28.dp)
                                        )
                                )
                                
                                // Additional subtle overlay for better text contrast
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                                                ),
                                                radius = 500f
                                            ),
                                            shape = RoundedCornerShape(28.dp)
                                        )
                                )
                                
                                // Content area with lyrics
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    when {
                                        isLoadingLyrics -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                M3CircularLoader(
                                                    modifier = Modifier.size(48.dp),
                                                    fourColor = true
                                                )
                                            }
                                        }
                                        lyrics == null || 
                                        !lyrics.hasLyrics() ||
                                        lyrics.isErrorMessage() -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Icon(
                                                        imageVector = RhythmIcons.MusicNote,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = if (onlineOnlyLyrics)
                                                            "Currently no lyrics are available for this song.\n"
                                                        else
                                                            "No lyrics available for this song.",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            // Extract appropriate lyrics text from LyricsData object
                                            val lyricsText = remember(lyrics) {
                                                lyrics?.getBestLyrics() ?: ""
                                            }
                                            
                                            val parsedLyrics = remember(lyricsText) {
                                                chromahub.rhythm.app.util.LyricsParser.parseLyrics(lyricsText)
                                            }
                                            
                                            if (parsedLyrics.isNotEmpty()) {
                                                // Use SyncedLyricsView for synchronized lyrics
                                                SyncedLyricsView(
                                                    lyrics = lyricsText,
                                                    currentPlaybackTime = currentTimeMs,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                // Fallback to plain text lyrics if not synchronized
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .verticalScroll(rememberScrollState()),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = lyricsText,
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.6f,
                                                            fontWeight = FontWeight.Medium,
                                                            letterSpacing = 0.5.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp)
                                                    )
                                                }
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
                            // Song info with increased vertical padding
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
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
                                    modifier = Modifier.padding(vertical = 4.dp)
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
                                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                    1 -> RhythmIcons.RepeatOne
                                    2 -> RhythmIcons.Repeat
                                    else -> RhythmIcons.Repeat
                                }
                                
                                val tint = when (repeatMode) {
                                    0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Toggle repeat mode",
                                    tint = tint
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 16.dp))
                    
                    // Only add favorite/playlist buttons if we have space
                    if (!isCompactHeight) {
                        // Action buttons row - only show on taller screens with slightly reduced spacing
                        Spacer(modifier = Modifier.height(12.dp))
                        
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
                        // Device Output button with rounded pill shape
                        Card(
                            onClick = {
                                // Add haptic feedback 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Show device output sheet
                                showDeviceOutputSheet = true
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
                                    text = location?.name ?: "Device Output",
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
