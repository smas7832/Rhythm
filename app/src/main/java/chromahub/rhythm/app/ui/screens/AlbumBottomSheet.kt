package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalConfiguration
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

enum class AlbumSortOrder {
    TRACK_NUMBER,
    TITLE_ASC,
    TITLE_DESC,
    DURATION_ASC,
    DURATION_DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumBottomSheet(
    album: Album,
    onDismiss: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit, // Updated to pass sorted songs
    onShufflePlay: (List<Song>) -> Unit, // Updated to pass sorted songs
    onAddToQueue: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onPlayerClick: () -> Unit,
    sheetState: SheetState,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback, // Add haptics parameter
    onPlayNext: (Song) -> Unit = {},
    onToggleFavorite: (Song) -> Unit = {},
    favoriteSongs: Set<String> = emptySet(),
    onShowSongInfo: (Song) -> Unit = {},
    onAddToBlacklist: (Song) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Get AppSettings for sort order persistence
    val appSettings = remember { AppSettings.getInstance(context) }
    val savedSortOrder by appSettings.albumSortOrder.collectAsState()
    
    // Sort order state with persistence
    var sortOrder by remember { 
        mutableStateOf(
            try {
                AlbumSortOrder.valueOf(savedSortOrder)
            } catch (e: Exception) {
                AlbumSortOrder.TRACK_NUMBER
            }
        )
    }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Save sort order when it changes
    LaunchedEffect(sortOrder) {
        appSettings.setAlbumSortOrder(sortOrder.name)
    }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(500),
        label = "headerAlpha"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(700),
        label = "contentAlpha"
    )
    
    val contentTranslation by animateFloatAsState(
        targetValue = if (showContent) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentTranslation"
    )
    
    // Sort songs based on selected order
    val sortedSongs = remember(album.songs, sortOrder) {
        when (sortOrder) {
            AlbumSortOrder.TRACK_NUMBER -> album.songs.sortedWith { a, b ->
                when {
                    a.trackNumber > 0 && b.trackNumber > 0 -> a.trackNumber.compareTo(b.trackNumber)
                    a.trackNumber > 0 -> -1
                    b.trackNumber > 0 -> 1
                    else -> a.title.compareTo(b.title, ignoreCase = true)
                }
            }
            AlbumSortOrder.TITLE_ASC -> album.songs.sortedBy { it.title.lowercase() }
            AlbumSortOrder.TITLE_DESC -> album.songs.sortedByDescending { it.title.lowercase() }
            AlbumSortOrder.DURATION_ASC -> album.songs.sortedBy { it.duration }
            AlbumSortOrder.DURATION_DESC -> album.songs.sortedByDescending { it.duration }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier
            .fillMaxHeight() // Make it full height to prevent floating
            .imePadding() // Handle keyboard properly
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding() // Handle navigation bar properly
            ) {
                // Sticky Enhanced Album Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .graphicsLayer { alpha = headerAlpha }
                ) {
                    // Album artwork with enhanced background
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (album.artworkUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .apply(ImageUtils.buildImageRequest(
                                        album.artworkUri,
                                        album.title,
                                        context.cacheDir,
                                        M3PlaceholderType.ALBUM
                                    ))
                                    .build(),
                                contentDescription = "Album artwork for ${album.title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Enhanced fallback with animated gradient background
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                                MaterialTheme.colorScheme.surface
                                            ),
                                            radius = 600f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.size(120.dp)
                                )
                            }
                        }
                    }
                    
                    // Enhanced gradient overlay with multiple layers for better depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )
                    
                    // Enhanced close button with blur effect simulation
                    FilledIconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(WindowInsets.statusBars.asPaddingValues()) // Adjust for status bar
                        .padding(20.dp)
                        .size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Enhanced album info at bottom with better typography
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = album.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Surface(
//                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
//                                shape = RoundedCornerShape(12.dp)
//                            ) {
//                                Text(
//                                    text = "${album.songs.size} songs",
//                                    style = MaterialTheme.typography.bodyLarge,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    fontWeight = FontWeight.Medium,
//                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
//                                )
//                            }
                            
                            if (album.year > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${album.year}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Enhanced action buttons row with better spacing and styling
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Enhanced Play All button with sorted songs
                            Button(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    // Play All - don't call onSongClick as onPlayAll handles queue properly
                                    if (sortedSongs.isNotEmpty()) {
                                        // Pass the sorted songs to the callback for proper queue management
                                        onPlayAll(sortedSongs)
                                    } else {
                                        onPlayAll(album.songs)
                                    }
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            onDismiss()
                                            onPlayerClick()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Play All",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Enhanced Shuffle button with sorted songs
                            FilledIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    // Pass songs to be shuffled by viewModel (respects shuffle settings)
                                    if (sortedSongs.isNotEmpty()) {
                                        onShufflePlay(sortedSongs)
                                    } else {
                                        onShufflePlay(album.songs)
                                    }
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            onDismiss()
                                            onPlayerClick()
                                        }
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shuffle,
                                    contentDescription = "Shuffle play",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        }
                    }
                
                // Sticky Songs Header with sorting controls
                if (sortedSongs.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 0.dp, // Add subtle elevation when scrolled
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Songs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${sortedSongs.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                
                                // Sort button with dropdown
                                Box {
                                    FilledIconButton(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                            showSortMenu = true
                                        },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = RhythmIcons.Actions.Sort,
                                            contentDescription = "Sort songs",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false },
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        AlbumSortOrder.values().forEach { order ->
                                            Surface(
                                                color = if (sortOrder == order) 
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                                else 
                                                    Color.Transparent,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { 
                                                        Text(
                                                            text = when (order) {
                                                                AlbumSortOrder.TRACK_NUMBER -> "Track Number"
                                                                AlbumSortOrder.TITLE_ASC -> "Title A-Z"
                                                                AlbumSortOrder.TITLE_DESC -> "Title Z-A"
                                                                AlbumSortOrder.DURATION_ASC -> "Duration ↑"
                                                                AlbumSortOrder.DURATION_DESC -> "Duration ↓"
                                                            },
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (sortOrder == order) 
                                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                                            else 
                                                                MaterialTheme.colorScheme.onSurface
                                                        ) 
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = when (order) {
                                                                AlbumSortOrder.TRACK_NUMBER -> Icons.Filled.FormatListNumbered
                                                                AlbumSortOrder.TITLE_ASC, AlbumSortOrder.TITLE_DESC -> Icons.Filled.SortByAlpha
                                                                AlbumSortOrder.DURATION_ASC, AlbumSortOrder.DURATION_DESC -> Icons.Filled.AccessTime
                                                            },
                                                            contentDescription = null,
                                                            tint = if (sortOrder == order) 
                                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                                            else 
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    },
                                                    onClick = {
                                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                        sortOrder = order
                                                        showSortMenu = false
                                                        // Save the new sort order to persistent storage
                                                        appSettings.setAlbumSortOrder(order.name)
                                                        // The sorted songs list will be automatically updated
                                                        // due to the derivedStateOf in sortedSongs
                                                    },
                                                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                                                        textColor = if (sortOrder == order) 
                                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                                        else 
                                                            MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Big Card for Songs
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // This card takes remaining space and allows its content to scroll
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding for the card itself
                    shape = RoundedCornerShape(24.dp), // Rounded corners for the card
                    color = MaterialTheme.colorScheme.surfaceContainer, // Distinct background for the card
                    shadowElevation = 0.dp // No shadow for a flat look
                ) {
                    // Scrollable Songs List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(), // Fill the card
                        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp) // Add bottom padding for last item
                    ) {
                        // Songs content with sorting
                        if (sortedSongs.isNotEmpty()) {
                            items(
                                items = sortedSongs,
                                key = { it.id }
                            ) { song ->
                                EnhancedAlbumSongItem(
                                    song = song,
                                    onClick = {
                                        onSongClick(song)
                                        scope.launch {
                                            sheetState.hide()
                                        }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                onDismiss()
                                                onPlayerClick()
                                            }
                                        }
                                    },
                                    onAddToQueue = { onAddToQueue(song) },
                                    onAddToPlaylist = { onAddSongToPlaylist(song) },
                                    onPlayNext = { onPlayNext(song) },
                                    onToggleFavorite = { onToggleFavorite(song) },
                                    isFavorite = favoriteSongs.contains(song.id),
                                    onShowSongInfo = { onShowSongInfo(song) },
                                    onAddToBlacklist = { onAddToBlacklist(song) },
                                    modifier = Modifier
                                        .animateItem(), // Keep item placement animation
                                    haptics = haptics
                                )
                            }
                        } else {
                            item {
                                // Empty state for no songs
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "No songs in this album",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Enhanced bottom spacing (inside the card, so it scrolls with songs)
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedAlbumSongItem(
    song: Song,
    onClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onPlayNext: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    onShowSongInfo: () -> Unit = {},
    onAddToBlacklist: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }
    
    Surface(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
        shape = RoundedCornerShape(12.dp), // Reduced corner radius
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        tonalElevation = 1.dp
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (song.trackNumber > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                                ) {
                                Text(
                                    text = "${song.trackNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                        
                        Spacer(modifier = Modifier.width(12.dp)) // Reduced spacing
                    }
                    
                    if (song.duration > 0) {
                        val durationText = formatDuration(song.duration)
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(8.dp), // Reduced corner radius
                    modifier = Modifier.size(48.dp), // Reduced size
                    tonalElevation = 2.dp
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .apply(ImageUtils.buildImageRequest(
                                song.artworkUri,
                                song.title,
                                context.cacheDir,
                                M3PlaceholderType.TRACK
                            ))
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            trailingContent = {
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        showDropdown = true
                    },
                    modifier = Modifier
                        .size(width = 40.dp, height = 36.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = RhythmIcons.More,
                        contentDescription = "More options",
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        showDropdown = false
                    },
                    modifier = Modifier
                        .widthIn(min = 220.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(5.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    // Play next
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Play next",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showDropdown = false
                                onPlayNext()
                            }
                        )
                    }

                    // Add to queue
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Add to queue",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Queue,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showDropdown = false
                                onAddToQueue()
                            }
                        )
                    }

                    // Toggle favorite
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isFavorite) "Remove from favorites" else "Add to favorites",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showDropdown = false
                                onToggleFavorite()
                            }
                        )
                    }

                    // Add to playlist
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Add to playlist",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showDropdown = false
                                onAddToPlaylist()
                            }
                        )
                    }

                    // Song info
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Song info",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showDropdown = false
                                onShowSongInfo()
                            }
                        )
                    }

                    // Add to blacklist
                    // Surface(
                    //     color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    //     shape = RoundedCornerShape(16.dp),
                    //     modifier = Modifier
                    //         .fillMaxWidth()
                    //         .padding(horizontal = 8.dp, vertical = 2.dp)
                    // ) {
                    //     DropdownMenuItem(
                    //         text = {
                    //             Text(
                    //                 "Hide song",
                    //                 style = MaterialTheme.typography.bodyMedium,
                    //                 fontWeight = FontWeight.Medium,
                    //                 color = MaterialTheme.colorScheme.onErrorContainer
                    //             )
                    //         },
                    //         leadingIcon = {
                    //             Surface(
                    //                 color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    //                 shape = CircleShape,
                    //                 modifier = Modifier.size(32.dp)
                    //             ) {
                    //                 Icon(
                    //                     imageVector = Icons.Rounded.Block,
                    //                     contentDescription = null,
                    //                     tint = MaterialTheme.colorScheme.error,
                    //                     modifier = Modifier
                    //                         .fillMaxSize()
                    //                         .padding(6.dp)
                    //                 )
                    //             }
                    //         },
                    //         onClick = {
                    //             HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    //             showDropdown = false
                    //             onAddToBlacklist()
                    //         }
                    //     )
                    // }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp) // Removed internal padding
        )
    }
}

// Helper function to format duration (reuse from PlaylistDetailScreen)
private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}