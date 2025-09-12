package chromahub.rhythm.app.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.input.pointer.pointerInput
//import kotlinx.coroutines.awaitRelease
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.res.painterResource
import chromahub.rhythm.app.data.PlaybackLocation
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.WaveSlider
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.theme.PlayerButtonColor
// import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.HapticUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import chromahub.rhythm.app.ui.components.M3CircularLoader
import android.view.animation.OvershootInterpolator
import chromahub.rhythm.app.ui.screens.EqualizerBottomSheetNew
import chromahub.rhythm.app.ui.screens.SleepTimerBottomSheetNew
import chromahub.rhythm.app.ui.components.SyncedLyricsView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import chromahub.rhythm.app.ui.components.formatDuration
import java.util.concurrent.TimeUnit // Import TimeUnit for duration formatting
import chromahub.rhythm.app.ui.components.M3CircularLoader // Added for play/pause button loader
import chromahub.rhythm.app.ui.screens.QueueBottomSheet
import chromahub.rhythm.app.ui.screens.LibraryTab
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.ui.screens.DeviceOutputBottomSheet
import chromahub.rhythm.app.ui.screens.SongInfoBottomSheet
import chromahub.rhythm.app.ui.screens.ArtistBottomSheet
import chromahub.rhythm.app.ui.components.CanvasPlayer
import chromahub.rhythm.app.data.CanvasRepository
import chromahub.rhythm.app.data.CanvasData
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist

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
    onClearQueue: () -> Unit = {},
    // New parameters for loader control and bottom sheets
    isMediaLoading: Boolean = false,
    isSeeking: Boolean = false,
    onShowAlbumBottomSheet: () -> Unit = {},
    onShowArtistBottomSheet: () -> Unit = {},
    // Album and artist data for bottom sheets
    songs: List<Song> = emptyList(),
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    onPlayAlbumSongs: (List<Song>) -> Unit = {},
    onShuffleAlbumSongs: (List<Song>) -> Unit = {},
    onPlayArtistSongs: (List<Song>) -> Unit = {},
    onShuffleArtistSongs: (List<Song>) -> Unit = {},
    appSettings: chromahub.rhythm.app.data.AppSettings,
    musicViewModel: chromahub.rhythm.app.viewmodel.MusicViewModel
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()

    // Get AppSettings for volume control setting
    val appSettingsInstance =
        appSettings ?: chromahub.rhythm.app.data.AppSettings.getInstance(context)
    val useSystemVolume by appSettingsInstance.useSystemVolume.collectAsState()

    // System volume state
    var systemVolume by remember { mutableFloatStateOf(0.5f) }

    // Monitor system volume changes
    LaunchedEffect(useSystemVolume) {
        if (useSystemVolume) {
            while (true) {
                val audioManager =
                    context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                val currentVolume =
                    audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
                val maxVolume =
                    audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                val newSystemVolume =
                    if (maxVolume > 0) currentVolume.toFloat() / maxVolume.toFloat() else 0f

                if (newSystemVolume != systemVolume) {
                    systemVolume = newSystemVolume
                }

                kotlinx.coroutines.delay(500) // Check every 500ms
            }
        }
    }

    // Calculate screen dimensions
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }

    // Enhanced screen size detection for better responsiveness
    val isCompactHeight = configuration.screenHeightDp < 600
    val isLargeHeight = configuration.screenHeightDp > 800
    
    // Bottom sheet states
    var showEqualizerBottomSheet by remember { mutableStateOf(false) }
    var showSleepTimerBottomSheet by remember { mutableStateOf(false) }
    val isCompactWidth = configuration.screenWidthDp < 400

    // Dynamic sizing based on screen dimensions
    val albumArtSize = when {
        isCompactHeight -> 0.55f
        isLargeHeight -> 0.65f
        else -> 0.6f
    }

    // Animation for album art entry
    var showAlbumArt by remember { mutableStateOf(false) }

    // Toggle between album art and lyrics with improved state management
    var showLyricsView by remember { mutableStateOf(false) }
    var isLyricsContentVisible by remember { mutableStateOf(false) }
    var isSongInfoVisible by remember { mutableStateOf(true) }

    // Canvas video state with improved caching and retry capability
    var canvasData by remember { mutableStateOf<CanvasData?>(null) }
    var canvasRetryCount by remember { mutableStateOf(0) }
    val canvasRepository = remember { 
        CanvasRepository(context, appSettings).apply {
            // Clear expired cache entries on initialization and optimize memory
            clearExpiredCache()
            optimizeCache() // New method for better cache management
        }
    }

    // Canvas retry function
    val retryCanvasLoading = {
        if (song != null && canvasRetryCount < 2) { // Allow up to 2 retries
            canvasRetryCount++
            
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    Log.d("PlayerScreen", "Retrying canvas load for: ${song.artist} - ${song.title} (attempt $canvasRetryCount)")
                    val canvas = canvasRepository.retryCanvasForSong(song.artist, song.title)
                    canvasData = canvas
                    
                    if (canvas != null) {
                        Log.d("PlayerScreen", "Canvas retry successful: ${canvas.videoUrl}")
                    } else {
                        Log.d("PlayerScreen", "Canvas retry failed - no canvas found")
                    }
                } catch (e: Exception) {
                    Log.e("PlayerScreen", "Canvas retry failed: ${e.message}")
                    canvasData = null
                }
            }
        }
    }

    // Reset lyrics view when lyrics are disabled
    LaunchedEffect(showLyrics) {
        if (!showLyrics && showLyricsView) {
            showLyricsView = false
            isLyricsContentVisible = false
            isSongInfoVisible = true  // Ensure song info is shown when lyrics are disabled
        }
    }

    // Reset to song info when song changes and load canvas
    LaunchedEffect(song?.id) {
        if (song != null) {
            showLyricsView = false
            isLyricsContentVisible = false
            isSongInfoVisible = true
            
            // Reset canvas state
            canvasData = null
            canvasRetryCount = 0 // Reset retry count for new song
            
            // Try to load canvas for the new song
            try {
                Log.d("PlayerScreen", "Loading canvas for: ${song.artist} - ${song.title}")
                
                // Enhanced cache check with better logging
                val cachedCanvas = canvasRepository.getCachedCanvas(song.artist, song.title)
                if (cachedCanvas != null) {
                    Log.d("PlayerScreen", "Using cached canvas: ${cachedCanvas.videoUrl}")
                    canvasData = cachedCanvas
                } else {
                    Log.d("PlayerScreen", "No cached canvas found, fetching from API...")
                    // Fetch from API if not cached
                    val canvas = canvasRepository.fetchCanvasForSong(song.artist, song.title)
                    canvasData = canvas
                    
                    if (canvas != null) {
                        Log.d("PlayerScreen", "Canvas loaded successfully: ${canvas.videoUrl}")
                    } else {
                        Log.d("PlayerScreen", "No canvas available for: ${song.artist} - ${song.title}")
                    }
                }
                
                // Preload canvas for upcoming queue items for better performance
                if (queue.isNotEmpty() && queuePosition < queue.size) {
                    val upcomingSongs = queue.drop(queuePosition + 1).take(3).map { it.artist to it.title }
                    if (upcomingSongs.isNotEmpty()) {
                        canvasRepository.preloadCanvasForQueue(upcomingSongs)
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerScreen", "Failed to load canvas: ${e.message}")
                canvasData = null
            }
        }
    }

    // Improved state management for smooth, non-overlapping transitions
    LaunchedEffect(showLyricsView, showLyrics) {
        if (showLyrics && showLyricsView) {
            // Transitioning to lyrics view
            isSongInfoVisible = false // Hide song info immediately
            delay(200) // Wait for song info to fade out completely
            isLyricsContentVisible = true // Then show lyrics content
        } else {
            // Transitioning back to song info view
            isLyricsContentVisible = false // Hide lyrics immediately
            delay(300) // Wait for lyrics to fade out completely
            isSongInfoVisible = true // Then show song info
        }
    }

    // Bottom sheet states
    val queueSheetState = rememberModalBottomSheetState()
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    val deviceOutputSheetState = rememberModalBottomSheetState()
    val albumBottomSheetState = rememberModalBottomSheetState()
    val artistBottomSheetState = rememberModalBottomSheetState()
    var showQueueSheet by remember { mutableStateOf(false) }
    var showDeviceOutputSheet by remember { mutableStateOf(false) }
    var showSongInfoSheet by remember { mutableStateOf(false) }
    var showAlbumSheet by remember { mutableStateOf(false) }
    var showArtistSheet by remember { mutableStateOf(false) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }

    // State for showing loader in play/pause button
    var showLoaderInPlayPauseButton by remember { mutableStateOf(false) }
    
    // Show loader only when media is loading or seeking
    LaunchedEffect(isMediaLoading, isSeeking) {
        showLoaderInPlayPauseButton = isMediaLoading || isSeeking
    }

    // For swipe down gesture detection - enhanced for continuous sliding
    var targetOffsetY by remember { mutableStateOf(0f) }
    var isActivelySwiping by remember { mutableStateOf(false) }
    
    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = if (isActivelySwiping) {
            // During active swiping, use immediate response for real-time feedback
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        } else {
            // When releasing, use smooth bouncy animation
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "animatedOffsetY"
    )
    val swipeThreshold = 80f // Further reduced for easier dismissal
    val miniPlayerThreshold = screenHeight * 0.4f // Threshold for mini player transition

    // Enhanced animation for dismiss swipe gesture with continuous feedback
    val dismissProgress = (animatedOffsetY / swipeThreshold).coerceIn(0f, 1f)
    val miniPlayerProgress = (animatedOffsetY / miniPlayerThreshold).coerceIn(0f, 1f)
    
    val contentAlpha by animateFloatAsState(
        targetValue = 1f - (dismissProgress * 0.2f), // Even more subtle fade
        animationSpec = if (isActivelySwiping) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "contentAlpha"
    )

    // Enhanced scale effect with continuous interpolation
    val contentScale by animateFloatAsState(
        targetValue = 1f - (miniPlayerProgress * 0.05f), // Scale based on mini player progress
        animationSpec = if (isActivelySwiping) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "contentScale"
    )

    // For dismissing animation
    var isDismissing by remember { mutableStateOf(false) }

    // Handle dismissing animation
    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            delay(0)
            onBack()
        }
    }

    // Calculate current and total time
    val currentTimeMs = ((song?.duration ?: 0) * progress).toLong()
    val totalTimeMs = song?.duration ?: 0

    // Format current and total time
    val currentTimeFormatted = formatDuration(currentTimeMs)
    val totalTimeFormatted = formatDuration(totalTimeMs)

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
        targetValue = if (showAlbumArt) {
            if (showLyricsView) 0.98f else 1f  // Slightly smaller when showing lyrics
        } else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "albumScale"
    )

    val albumAlpha by animateFloatAsState(
        targetValue = if (showAlbumArt) {
            when {
                showLyricsView && isLyricsContentVisible -> 0.95f  // Slightly dimmed when lyrics are showing
                !isSongInfoVisible && !isLyricsContentVisible -> 0.8f  // Dimmed during transition
                else -> 1f
            }
        } else 0f,
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
                onSongClick(selectedSong)
                showQueueSheet = false
            },
            onDismiss = { showQueueSheet = false },
            onRemoveSong = { songToRemove ->
                // Remove the song from the queue
                onRemoveFromQueue(songToRemove)
            },
            onMoveQueueItem = { fromIndex, toIndex ->
                onMoveQueueItem(fromIndex, toIndex)
            },
            onAddSongsClick = {
                showQueueSheet = false
                onNavigateToLibrary(LibraryTab.SONGS)
            },
            onClearQueue = {
                onClearQueue()
                showQueueSheet = false
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
                playlist.id?.let { playlistId ->
                    onAddSongToPlaylist(song, playlistId)
                    onAddToPlaylistSheetDismiss()
                }
            },
            onCreateNewPlaylist = {
                onAddToPlaylistSheetDismiss()
                onShowCreatePlaylistDialog()
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
            onLocationSelect = {
                onLocationSelect(it)
                showDeviceOutputSheet = false
            },
            onVolumeChange = onVolumeChange,
            onToggleMute = onToggleMute,
            onMaxVolume = onMaxVolume,
            onRefreshDevices = onRefreshDevices,
            onDismiss = {
                showDeviceOutputSheet = false
                onStopDeviceMonitoring()
            },
            appSettings = appSettings ?: chromahub.rhythm.app.data.AppSettings.getInstance(context),
            sheetState = deviceOutputSheetState
        )
    }

    // Song Info Bottom Sheet
    if (showSongInfoSheet && song != null) {
        SongInfoBottomSheet(
            song = song,
            onDismiss = { showSongInfoSheet = false }
        )
    }

    // Album Bottom Sheet
    if (showAlbumSheet && selectedAlbum != null) {
        AlbumBottomSheet(
            album = selectedAlbum!!,
            onDismiss = { 
                showAlbumSheet = false
                selectedAlbum = null
            },
            onSongClick = onSongClick,
            onPlayAll = { songs -> onPlayAlbumSongs(songs) },
            onShufflePlay = { songs -> onShuffleAlbumSongs(songs) },
            onAddToQueue = { song -> /* TODO: Add queue functionality */ },
            onAddSongToPlaylist = { song -> onAddSongToPlaylist(song, "") },
            onPlayerClick = onBack,
            sheetState = albumBottomSheetState,
            haptics = haptic
        )
    }

    // Artist Bottom Sheet
    if (showArtistSheet && selectedArtist != null) {
        ArtistBottomSheet(
            artist = selectedArtist!!,
            songs = songs,
            albums = albums,
            onDismiss = { 
                showArtistSheet = false
                selectedArtist = null
            },
            onSongClick = onSongClick,
            onAlbumClick = { album -> 
                selectedAlbum = album
                showAlbumSheet = true
            },
            onPlayAll = { onPlayArtistSongs(songs.filter { it.artist == selectedArtist!!.name }) },
            onShufflePlay = { onShuffleArtistSongs(songs.filter { it.artist == selectedArtist!!.name }) },
            onAddToQueue = { song -> /* TODO: Add queue functionality */ },
            onAddSongToPlaylist = { song -> onAddSongToPlaylist(song, "") },
            sheetState = artistBottomSheetState
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
                            // Enhanced NOW PLAYING with better styling
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "NOW PLAYING",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.4.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Song and playlist info, always visible if song is not null
                        AnimatedVisibility(
                            visible = song != null,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            if (song != null && songPlaylist != null) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Enhanced playlist name with better styling
                                Text(
                                    text = songPlaylist.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.25.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    // Increased horizontal padding for better edge spacing
                    Box(modifier = Modifier.padding(start = 16.dp)) {
                        FilledTonalIconButton(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onBack()
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Minimize",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    // Song info button
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        FilledTonalIconButton(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                showSongInfoSheet = true
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Song info",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BottomSheetDefaults.ContainerColor,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = BottomSheetDefaults.ContainerColor, // Set the background color here
        modifier = Modifier
            .graphicsLayer {
                // Apply translation and enhanced effects based on swipe gesture
                translationY = animatedOffsetY
                alpha = contentAlpha
                scaleX = contentScale
                scaleY = contentScale
                
                // Add subtle rotation and blur effect during transition
                rotationX = miniPlayerProgress * 2f // Subtle 3D effect
                
                // Create depth effect by adjusting corner radius during swipe
                clip = true
                shape = RoundedCornerShape(
                    topStart = (miniPlayerProgress * 16).dp,
                    topEnd = (miniPlayerProgress * 16).dp
                )
            }
    ) { paddingValues ->
        // Use Box as the root container to allow absolute positioning
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    var dragAmountY = 0f
                    var velocityY = 0f
                    var lastDragTime = 0L

                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            dragAmountY = 0f
                            velocityY = 0f
                            lastDragTime = System.currentTimeMillis()
                            isActivelySwiping = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val currentTime = System.currentTimeMillis()
                            val deltaTime = (currentTime - lastDragTime).coerceAtLeast(1L)
                            
                            // Track both upward and downward swipes for more natural feel
                            if (dragAmount > 0 || (dragAmount < 0 && dragAmountY > 0)) {
                                dragAmountY = (dragAmountY + dragAmount).coerceAtLeast(0f)
                                velocityY = dragAmount / deltaTime * 1000f // pixels per second
                                
                                // Smooth resistance curve that doesn't stop the movement
                                val maxDrag = screenHeight * 0.8f
                                val resistanceFactor = when {
                                    dragAmountY < swipeThreshold -> 1.0f
                                    dragAmountY < miniPlayerThreshold -> 0.8f
                                    else -> {
                                        val overDrag = dragAmountY - miniPlayerThreshold
                                        val remainingDistance = maxDrag - miniPlayerThreshold
                                        0.3f + (0.5f * (1f - (overDrag / remainingDistance).coerceIn(0f, 1f)))
                                    }
                                }
                                
                                targetOffsetY = (dragAmountY * resistanceFactor).coerceIn(0f, maxDrag)
                            }
                            lastDragTime = currentTime
                        },
                        onDragEnd = {
                            isActivelySwiping = false
                            
                            // Enhanced decision logic for smooth transitions
                            when {
                                // Quick flick down - immediate dismiss
                                velocityY > 800f && dragAmountY > swipeThreshold * 0.3f -> {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    isDismissing = true
                                }
                                // Dragged past mini player threshold - dismiss
                                dragAmountY > miniPlayerThreshold -> {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    isDismissing = true
                                }
                                // Moderate drag with some velocity - dismiss
                                dragAmountY > swipeThreshold && velocityY > 300f -> {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    isDismissing = true
                                }
                                // Otherwise spring back smoothly
                                else -> {
                                    targetOffsetY = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            isActivelySwiping = false
                            // Smooth spring back animation
                            targetOffsetY = 0f
                        }
                    )
                }
        ) {
            // Enhanced swipe down indicator with progressive feedback
            if (animatedOffsetY > swipeThreshold * 0.1f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    val textAlpha =
                        ((animatedOffsetY - (swipeThreshold * 0.1f)) / (swipeThreshold * 0.9f)).coerceIn(
                            0f,
                            1f
                        )
                    
                    // Progressive feedback based on drag distance
                    val (feedbackText, feedbackColor) = when {
                        animatedOffsetY > miniPlayerThreshold * 0.8f -> 
                            "Release to close" to MaterialTheme.colorScheme.error
                        animatedOffsetY > swipeThreshold * 2f -> 
                            "Continue to minimize" to MaterialTheme.colorScheme.tertiary
                        animatedOffsetY > swipeThreshold * 0.8f -> 
                            "Keep swiping to close" to MaterialTheme.colorScheme.primary
                        else -> 
                            "Swipe down to minimize" to MaterialTheme.colorScheme.secondary
                    }
                    
                    // Enhanced visual feedback with animated background and progress indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = feedbackColor.copy(alpha = textAlpha * 0.15f),
                        modifier = Modifier.graphicsLayer {
                            this.alpha = textAlpha
                            scaleX = 0.7f + (textAlpha * 0.3f)
                            scaleY = 0.7f + (textAlpha * 0.3f)
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = feedbackText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = feedbackColor,
                                textAlign = TextAlign.Center
                            )
                            
                            // Progress indicator showing swipe progress
                            if (animatedOffsetY > swipeThreshold * 0.3f) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(feedbackColor.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(
                                                (animatedOffsetY / miniPlayerThreshold).coerceIn(0f, 1f)
                                            )
                                            .background(
                                                feedbackColor,
                                                RoundedCornerShape(1.5.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MAIN CHANGE: Use a Column that fills the available space but anchors content to the bottom
            // This makes all content stick to the bottom of the screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main content column - optimized spacing to reduce vertical padding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            horizontal = if (isCompactWidth) 8.dp else 12.dp, // Reduced horizontal padding
                            vertical = if (isCompactHeight) 2.dp else 4.dp    // Further reduced vertical padding
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (isCompactHeight) Arrangement.Center else Arrangement.SpaceEvenly
                ) {
                    // Optimized dynamic spacing - reduced overall spacing
                    val contentSpacing = when {
                        isCompactHeight -> 1.dp   // Further reduced from 2.dp
                        isLargeHeight -> 4.dp     // Further reduced from 8.dp  
                        else -> 2.dp              // Further reduced from 4.dp
                    }

                    // Album artwork or lyrics view with smooth transitions
                    // Album artwork with optimized padding and improved click handling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1.0f) // Enlarged album art to touch screen edges
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(if (isCompactHeight) 20.dp else 28.dp))
                            .graphicsLayer {
                                scaleX = albumScale * (1f - miniPlayerProgress * 0.1f) // Subtle shrink during swipe
                                scaleY = albumScale * (1f - miniPlayerProgress * 0.1f)
                                alpha = albumAlpha * (1f - miniPlayerProgress * 0.3f) // Fade during transition
                                translationY = miniPlayerProgress * 20f // Slight upward movement
                            }
                            .clickable(
                                enabled = showLyrics // Only enable click if lyrics are available
                            ) {
                                // Add haptic feedback and prevent rapid toggling
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                if (showLyrics && !isLyricsContentVisible && isSongInfoVisible) {
                                    // Only toggle if not currently transitioning
                                    showLyricsView = !showLyricsView
                                } else if (showLyrics && isLyricsContentVisible && !isSongInfoVisible) {
                                    // Allow toggling back from lyrics view
                                    showLyricsView = !showLyricsView
                                }
                            },
                        contentAlignment = Alignment.TopCenter // Align content to the center
                    ) {
                        // Always render CanvasPlayer if song is not null, let CanvasPlayer manage its states
                        if (song != null) {
                            CanvasPlayer(
                                videoUrl = canvasData?.videoUrl,
                                isPlaying = true, // Always keep canvas playing
                                cornerRadius = if (isCompactHeight) 20.dp else 28.dp,
                                modifier = Modifier.fillMaxSize(),
                                albumArtUrl = song.artworkUri,
                                albumName = song.title,
                                onCanvasLoaded = {
                                    Log.d("PlayerScreen", "Canvas loaded and album art transition completed")
                                },
                                onCanvasFailed = {
                                    Log.d("PlayerScreen", "Canvas failed, falling back to album art")
                                },
                                onRetryRequested = retryCanvasLoading,
                                fallbackContent = {
                                    // Custom fallback content with enhanced styling
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        // Album art content with enhanced loading state
                                        if (song.artworkUri != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(song.artworkUri)
                                                    .placeholder(chromahub.rhythm.app.R.drawable.rhythm_logo)
                                                    .error(chromahub.rhythm.app.R.drawable.rhythm_logo)
                                                    .build(),
                                                contentDescription = "Album artwork for ${song.title}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(if (isCompactHeight) 20.dp else 28.dp))
                                            )
                                        } else {
                                            // Fallback to Rhythm logo if artwork is null
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(if (isCompactHeight) 20.dp else 28.dp))
                                                    .background(
                                                        Brush.radialGradient(
                                                            colors = listOf(
                                                                BottomSheetDefaults.ContainerColor.copy(alpha = 0.3f),
                                                                BottomSheetDefaults.ContainerColor
                                                            ),
                                                            radius = 400f
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = chromahub.rhythm.app.R.drawable.rhythm_logo),
                                                    contentDescription = "Album artwork for ${song.title}",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(120.dp)
                                                )
                                                
                                                // Add gradient overlays for consistency
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    BottomSheetDefaults.ContainerColor.copy(alpha = 0.6f),
                                                                    BottomSheetDefaults.ContainerColor.copy(alpha = 0.9f),
                                                                    BottomSheetDefaults.ContainerColor.copy(alpha = 1.0f)
                                                                )
                                                            )
                                                        )
                                                )
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(
                                                                    BottomSheetDefaults.ContainerColor.copy(alpha = 0.2f),
                                                                    Color.Transparent,
                                                                    Color.Transparent,
                                                                    BottomSheetDefaults.ContainerColor.copy(alpha = 0.2f)
                                                                )
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            // Default placeholder if no song with Rhythm logo
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(if (isCompactHeight) 20.dp else 28.dp))
                                    .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.3f),
                                                        BottomSheetDefaults.ContainerColor
                                                    ),
                                                    radius = 400f
                                                )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = chromahub.rhythm.app.R.drawable.rhythm_logo),
                                    contentDescription = "No song playing",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(120.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(), // This Column will fill the Box
                            horizontalAlignment = Alignment.CenterHorizontally, // Center its children horizontally
                            verticalArrangement = Arrangement.Bottom // Align content to the bottom
                        ) {
                            // Song info overlay on album art with improved animations
                            AnimatedVisibility(
                                visible = song != null && isSongInfoVisible && !showLyricsView,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOut)) + 
                                       slideInVertically(
                                           animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                                       ) { it / 3 },
                                exit = fadeOut(animationSpec = tween(durationMillis = 200, easing = EaseInOut)) + 
                                      slideOutVertically(
                                          animationSpec = tween(durationMillis = 200, easing = EaseInOut)
                                      ) { it / 3 }
                            ) {
                                if (song != null) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = if (isCompactWidth) 12.dp else 16.dp,
                                                vertical = 16.dp // Add padding from the bottom
                                            )
                                    ) {
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.15.sp,
                                                fontSize = if (isCompactHeight) 22.sp else 28.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp)
                                                .graphicsLayer { alpha = 0.99f }
                                                .background(Color.Transparent) // Transparent background
                                        )

                                        Spacer(modifier = Modifier.height(if (isCompactHeight) 2.dp else 4.dp))

                                        Text(
                                            text = buildString {
                                                append(song.artist)
                                                if (!song.album.isNullOrBlank() && song.album != song.artist) {
                                                    append(" • ")
                                                    append(song.album)
                                                }
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.4.sp,
                                                fontSize = if (isCompactHeight) 14.sp else 16.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp)
                                                .graphicsLayer { alpha = 0.99f }
                                                .background(Color.Transparent) // Transparent background
                                        )
                                    }
                                }
                            }

                            // Lyrics overlay view with improved animations
                            AnimatedVisibility(
                                visible = isLyricsContentVisible && showLyrics && showLyricsView,
                                enter = fadeIn(animationSpec = tween(durationMillis = 350, easing = EaseInOut)) + 
                                       slideInVertically(
                                           animationSpec = tween(durationMillis = 350, easing = EaseInOut)
                                       ) { -it / 2 },
                                exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = EaseInOut)) + 
                                      slideOutVertically(
                                          animationSpec = tween(durationMillis = 250, easing = EaseInOut)
                                      ) { -it / 2 },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Deeper gradient overlay for lyrics
                                    // Box(
                                    //     modifier = Modifier
                                    //         .fillMaxSize()
                                    //         .background(
                                    //             Brush.verticalGradient(
                                    //                 colors = listOf(
                                    //                     MaterialTheme.colorScheme.surface.copy(alpha = 0.0f), // Start more transparent
                                    //                     MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), // Reduced from 0.5f
                                    //                     MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), // Reduced from 0.9f
                                    //                     MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)  // Reduced from 1.0f
                                    //                 )
                                    //             )
                                    //         )
                                    // )

                                    // Horizontal gradient for more depth
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.4f), // Reduced from 0.6f
                                                        Color.Transparent,
                                                        Color.Transparent,
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.4f) // Reduced from 0.6f
                                                    )
                                                )
                                            )
                                    )

                                    // Overlay with semi-transparent background for text readability (from original lyrics view)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.50f), // Reduced from 0.70f
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.60f), // Reduced from 0.80f
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.75f)  // Reduced from 0.85f
                                                    )
                                                ),
                                                shape = RoundedCornerShape(if (isCompactHeight) 0.dp else 0.dp) // Keep rounded corners
                                            )
                                    )

                                    // Additional subtle overlay for better text contrast (from original lyrics view)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0.10f) // Reduced from 0.15f
                                                    ),
                                                    radius = 500f
                                                ),
                                                shape = RoundedCornerShape(if (isCompactHeight) 0.dp else 0.dp) // Keep rounded corners
                                            )
                                    )

                                    // Content area with lyrics - optimized padding (from original lyrics view)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize() // Use full artwork length
                                            .padding(
                                                when {
                                                    isCompactHeight -> 12.dp
                                                    isLargeHeight -> 20.dp
                                                    else -> 16.dp
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            isLoadingLyrics -> {
                                                // Show loader in the lyrics view area instead of center
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 32.dp)
                                                ) {
                                                    M3CircularLoader(
                                                        modifier = Modifier.size(56.dp),
                                                        fourColor = true,
                                                        isExpressive = true
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = "Loading lyrics...",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        textAlign = TextAlign.Center
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
                                                            tint = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.8f
                                                            ),
                                                            modifier = Modifier.size(48.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                            text = if (onlineOnlyLyrics)
                                                                "Currently no lyrics are available for this song.\n"
                                                            else
                                                                "No lyrics available for this song.",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.8f
                                                            ),
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
                                                    chromahub.rhythm.app.util.LyricsParser.parseLyrics(
                                                        lyricsText
                                                    )
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
                    }


                    // Spacer to push content up and allow bottom controls to be anchored
                    Spacer(modifier = Modifier.height(8.dp))

                } // End of main content area that gets pushed up

                // Bottom controls container - anchored to the bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Subtle fade during swipe
                            alpha = 1f - (miniPlayerProgress * 0.4f)
                            translationY = miniPlayerProgress * 30f
                        }
                ) {
                    // Progress bar and time indicators - combined into a single row with pill-shaped time indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Current time pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = currentTimeFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Wave progress slider
                        WaveSlider(
                            value = progress,
                            onValueChange = onSeek,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp), // Give slider more space
                            waveColor = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            isPlaying = isPlaying
                        )
                        
                        // Total time pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = totalTimeFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))




                    // Main player controls matching the reference image exactly
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = if (isPlaying) Arrangement.SpaceEvenly else Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), // Adjust spacing based on play state
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous button - Circular like in reference image
                        Surface(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                onSkipPrevious()
                            },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.SkipPrevious,
                                    contentDescription = "Previous track",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }

                        // Seek 10 seconds back button
                        AnimatedVisibility(
                            visible = isPlaying, // Show when paused
                            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) +
                                    scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
                            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)) +
                                   scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh))
                        ) {
                            Surface(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    onSeek((currentTimeMs - 10000).coerceAtLeast(0L).toFloat() / totalTimeMs)
                                },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(24.dp), // Squircle shape
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Replay10,
                                        contentDescription = "Seek 10 seconds back",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Play/Pause button - Large Oval/Pill shaped exactly like in reference image
                        val playPauseButtonWidth by animateDpAsState(
                            targetValue = if (isPlaying) 60.dp else 140.dp, // Expand when inactive
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                                visibilityThreshold = null
                            ),
                            label = "playPauseButtonWidth"
                        )
                        val playPauseButtonHeight by animateDpAsState(
                            targetValue = if (isPlaying) 60.dp else 60.dp, // Keep height consistent
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                                visibilityThreshold = null
                            ),
                            label = "playPauseButtonHeight"
                        )

                        Surface(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onPlayPause()
                            },
                            modifier = Modifier
                                .width(playPauseButtonWidth)
                                .height(playPauseButtonHeight),
                            shape = RoundedCornerShape(30.dp), // Pill/oval shape
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showLoaderInPlayPauseButton) {
                                    M3CircularLoader(
                                        modifier = Modifier.size(24.dp), // Adjust size to fit
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        isExpressive = true
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    AnimatedVisibility(
                                        visible = !isPlaying && !showLoaderInPlayPauseButton, // Show text when inactive and no loader
                                        enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) +
                                                scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
                                        exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)) +
                                               scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh))
                                    ) {
                                        Row {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isPlaying) "PAUSE" else "PLAY",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.2.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Seek 10 seconds forward button
                        AnimatedVisibility(
                            visible = isPlaying, // Show when paused
                            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) +
                                    scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
                            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)) +
                                   scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh))
                        ) {
                            Surface(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    onSeek((currentTimeMs + 10000).coerceAtMost(totalTimeMs.toLong()).toFloat() / totalTimeMs)
                                },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(24.dp), // Squircle shape
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Forward10,
                                        contentDescription = "Seek 10 seconds forward",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Next button - Circular like in reference image
                        Surface(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onSkipNext()
                            },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.SkipNext,
                                    contentDescription = "Next track",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Secondary action buttons row - compact design
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle button
                        val shuffleIsActive = isShuffleEnabled
                        val shuffleButtonColor by animateColorAsState(
                            targetValue = if (shuffleIsActive) {
                                if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceContainerLowest
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            label = "shuffleButtonColor"
                        )
                        val shuffleContentColor by animateColorAsState(
                            targetValue = if (shuffleIsActive) {
                                if (isDarkTheme) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            label = "shuffleContentColor"
                        )
                        val shuffleButtonWidth by animateDpAsState(
                            targetValue = if (shuffleIsActive) 48.dp else 100.dp, // Expand when inactive
                            label = "shuffleButtonWidth"
                        )
                        val shuffleButtonShape by animateDpAsState(
                            targetValue = if (shuffleIsActive) 24.dp else 24.dp, // 24.dp for RoundedCornerShape(24.dp) and CircleShape for 48.dp size
                            label = "shuffleButtonShape"
                        )

                        Surface(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onToggleShuffle()
                            },
                            modifier = Modifier
                                .width(shuffleButtonWidth)
                                .height(48.dp),
                            shape = RoundedCornerShape(shuffleButtonShape),
                            color = shuffleButtonColor,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Shuffle,
                                    contentDescription = "Toggle shuffle",
                                    modifier = Modifier.size(20.dp),
                                    tint = shuffleContentColor
                                )
                                AnimatedVisibility(visible = !shuffleIsActive) { // Show text when inactive
                                    Row {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SHUFFLE",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            ),
                                            color = shuffleContentColor
                                        )
                                    }
                                }
                            }
                        }

                        // Lyrics toggle button (only show if lyrics are enabled)
                        if (showLyrics) {
                            val lyricsIsActive = showLyricsView
                            val lyricsButtonColor by animateColorAsState(
                                targetValue = if (lyricsIsActive) {
                                    if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceContainerLowest
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                label = "lyricsButtonColor"
                            )
                            val lyricsContentColor by animateColorAsState(
                                targetValue = if (lyricsIsActive) {
                                    if (isDarkTheme) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                },
                                label = "lyricsContentColor"
                            )
                            val lyricsButtonWidth by animateDpAsState(
                                targetValue = if (lyricsIsActive) 48.dp else 100.dp, // Expand when inactive
                                label = "lyricsButtonWidth"
                            )
                            val lyricsButtonShape by animateDpAsState(
                                targetValue = if (lyricsIsActive) 24.dp else 24.dp,
                                label = "lyricsButtonShape"
                            )

                            Surface(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    if (!isLyricsContentVisible && isSongInfoVisible) {
                                        showLyricsView = !showLyricsView
                                    } else if (isLyricsContentVisible && !isSongInfoVisible) {
                                        showLyricsView = !showLyricsView
                                    }
                                },
                                modifier = Modifier
                                    .width(lyricsButtonWidth)
                                    .height(48.dp),
                                shape = RoundedCornerShape(lyricsButtonShape),
                                color = lyricsButtonColor,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Player.Lyrics,
                                        contentDescription = "Toggle lyrics",
                                        modifier = Modifier.size(20.dp),
                                        tint = lyricsContentColor
                                    )
                                    AnimatedVisibility(visible = !lyricsIsActive) { // Show text when inactive
                                        Row {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "LYRICS",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.8.sp
                                                ),
                                                color = lyricsContentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Repeat button
                        val repeatIsActive = repeatMode != 0
                        val repeatButtonColor by animateColorAsState(
                            targetValue = if (repeatIsActive) {
                                if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceContainerLowest
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            label = "repeatButtonColor"
                        )
                        val repeatContentColor by animateColorAsState(
                            targetValue = if (repeatIsActive) {
                                if (isDarkTheme) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            label = "repeatContentColor"
                        )
                        val repeatButtonWidth by animateDpAsState(
                            targetValue = if (repeatIsActive) 48.dp else 100.dp, // Expand when inactive
                            label = "repeatButtonWidth"
                        )
                        val repeatButtonShape by animateDpAsState(
                            targetValue = if (repeatIsActive) 24.dp else 24.dp,
                            label = "repeatButtonShape"
                        )

                        Surface(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onToggleRepeat()
                            },
                            modifier = Modifier
                                .width(repeatButtonWidth)
                                .height(48.dp),
                            shape = RoundedCornerShape(repeatButtonShape),
                            color = repeatButtonColor,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val icon = when (repeatMode) {
                                    1 -> RhythmIcons.RepeatOne
                                    2 -> RhythmIcons.Repeat
                                    else -> RhythmIcons.Repeat
                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Toggle repeat mode",
                                    modifier = Modifier.size(20.dp),
                                    tint = repeatContentColor
                                )
                                AnimatedVisibility(visible = !repeatIsActive) { // Show text when inactive
                                    Row {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "REPEAT",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            ),
                                            color = repeatContentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action chips for playlist, favorite, album, and artist
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        // Add to Playlist chip
                        item {
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "addToPlaylistScale"
                            )
                            AssistChip(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    onAddToPlaylist()
                                },
                                label = {
                                    Text(
                                        "Add to",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RhythmIcons.AddToPlaylist,
                                        contentDescription = "Add to playlist",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .height(32.dp) // Reduced height
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    isPressed = false
                                                }
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp), // Adjusted shape for smaller size
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null // Removed border
                            )
                        }
                        
                        // Favorite chip
                        item {
                            val containerColor by animateColorAsState(
                                targetValue = if (isFavorite) Color.Red.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "favoriteChipContainerColor"
                            )
                            val labelColor by animateColorAsState(
                                targetValue = if (isFavorite) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "favoriteChipLabelColor"
                            )
                            val iconColor by animateColorAsState(
                                targetValue = if (isFavorite) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "favoriteChipIconColor"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (isFavorite) 1.05f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "favoriteChipScale"
                            )

                            FilterChip(
                                selected = isFavorite,
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    onToggleFavorite()
                                },
                                label = {
                                    Text(
                                        "Favorite",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isFavorite) RhythmIcons.FavoriteFilled else RhythmIcons.Favorite,
                                        contentDescription = "Toggle favorite",
                                        modifier = Modifier.size(16.dp) // Reduced icon size
                                    )
                                },
                                modifier = Modifier
                                    .height(32.dp) // Reduced height
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                shape = RoundedCornerShape(16.dp), // Adjusted shape for smaller size
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = containerColor,
                                    labelColor = labelColor,
                                    iconColor = iconColor,
                                    selectedContainerColor = containerColor,
                                    selectedLabelColor = labelColor,
                                    selectedLeadingIconColor = iconColor
                                ),
                                border = null // Removed border
                            )
                        }
                        
                        // Album chip  
                        item {
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "albumChipScale"
                            )
                            AssistChip(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    // Find the album for the current song and show bottom sheet
                                    song?.let { currentSong ->
                                        val albumForSong = albums.find { it.title == currentSong.album }
                                        albumForSong?.let {
                                            selectedAlbum = it
                                            showAlbumSheet = true
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        "Album",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RhythmIcons.Music.Album,
                                        contentDescription = "Show album",
                                        modifier = Modifier.size(16.dp) // Reduced icon size
                                    )
                                },
                                modifier = Modifier
                                    .height(32.dp) // Reduced height
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    isPressed = false
                                                }
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp), // Adjusted shape for smaller size
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null // Removed border
                            )
                        }
                        
                        // Artist chip
                        item {
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "artistChipScale"
                            )
                            AssistChip(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    // Find the artist for the current song and show bottom sheet
                                    song?.let { currentSong ->
                                        val artistForSong = artists.find { it.name == currentSong.artist }
                                        artistForSong?.let {
                                            selectedArtist = it
                                            showArtistSheet = true
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        "Artist",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RhythmIcons.Music.Artist,
                                        contentDescription = "Show artist",
                                        modifier = Modifier.size(16.dp) // Reduced icon size
                                    )
                                },
                                modifier = Modifier
                                    .height(32.dp) // Reduced height
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    isPressed = false
                                                }
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp), // Adjusted shape for smaller size
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null // Removed border
                            )
                        }
                        
                        // Equalizer chip
                        item {
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "equalizerChipScale"
                            )
                            AssistChip(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    showEqualizerBottomSheet = true
                                },
                                label = {
                                    Text(
                                        "EQ",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Show equalizer",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    isPressed = false
                                                }
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null
                            )
                        }
                        
                        // Sleep Timer chip
                        item {
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "sleepTimerChipScale"
                            )
                            AssistChip(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                    showSleepTimerBottomSheet = true
                                },
                                label = {
                                    Text(
                                        "Timer",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Show sleep timer",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    isPressed = false
                                                }
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Bottom buttons - optimized responsive design with reduced padding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (isCompactWidth) 8.dp else 12.dp,   // Reduced padding
                            vertical = if (isCompactHeight) 8.dp else 12.dp    // Reduced padding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        if (isCompactWidth) 6.dp else 8.dp // Reduced spacing
                    )
                ) {
                    // Device Output button with rounded pill shape - optimized padding
                    Surface(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            showDeviceOutputSheet = true
                        },
                        shape = RoundedCornerShape(if (isCompactHeight) 20.dp else 24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = if (isCompactHeight) 8.dp else 10.dp,    // Reduced padding
                                    horizontal = if (isCompactWidth) 8.dp else 12.dp    // Reduced padding
                                ),
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
                                modifier = Modifier.size(if (isCompactHeight) 20.dp else 24.dp)
                            )

                            if (!isCompactWidth) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = location?.name ?: "Device Output",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = if (isCompactHeight) 12.sp else 14.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    val displayVolume =
                                        if (useSystemVolume) systemVolume else volume
                                    val volumeText = if (useSystemVolume) "System" else "App"
                                    Text(
                                        text = "${(displayVolume * 100).toInt()}% $volumeText",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = if (isCompactHeight) 10.sp else 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.7f
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Queue button - optimized design with reduced padding
                    Surface(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            if (song != null) {
                                showQueueSheet = true
                            } else {
                                onQueueClick()
                            }
                        },
                        shape = RoundedCornerShape(if (isCompactHeight) 20.dp else 24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = if (isCompactHeight) 8.dp else 10.dp,    // Reduced padding
                                    horizontal = if (isCompactWidth) 8.dp else 12.dp    // Reduced padding
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Queue,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(if (isCompactHeight) 20.dp else 24.dp)
                            )

                            if (!isCompactWidth) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Queue",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = if (isCompactHeight) 12.sp else 14.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "$queuePosition of $queueTotal",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = if (isCompactHeight) 10.sp else 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.7f
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    
    // Bottom sheets
    if (showEqualizerBottomSheet) {
        EqualizerBottomSheetNew(
            musicViewModel = musicViewModel,
            onDismiss = { showEqualizerBottomSheet = false }
        )
    }
    
    if (showSleepTimerBottomSheet) {
        SleepTimerBottomSheetNew(
            onDismiss = { showSleepTimerBottomSheet = false },
            currentSong = song,
            isPlaying = isPlaying,
            musicViewModel = musicViewModel
        )
    }
}
