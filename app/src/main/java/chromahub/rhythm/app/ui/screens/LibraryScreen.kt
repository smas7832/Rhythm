@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package chromahub.rhythm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import chromahub.rhythm.app.ui.UiConstants
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.AlbumViewType
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.ui.components.CreatePlaylistDialog
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.screens.SongInfoBottomSheet
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.material3.ListItemDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import chromahub.rhythm.app.ui.components.RhythmIcons


enum class LibraryTab { SONGS, PLAYLISTS, ALBUMS }

@Composable
fun LibraryScreen(
    songs: List<Song>,
    albums: List<Album>,
    playlists: List<Playlist>,
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onSongClick: (Song) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddPlaylist: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onAlbumShufflePlay: (Album) -> Unit = { _ -> },
    onPlayQueue: (List<Song>) -> Unit = { _ -> }, // Added for playing a list of songs with queue replacement
    onShuffleQueue: (List<Song>) -> Unit = { _ -> }, // Added for shuffling and playing a list of songs
    onAlbumBottomSheetClick: (Album) -> Unit = { _ -> }, // Added for opening album bottom sheet
    onSort: () -> Unit = {},
    onRefreshClick: () -> Unit, // Changed from onSearchClick to onRefreshClick
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = { _ -> },
    sortOrder: MusicViewModel.SortOrder = MusicViewModel.SortOrder.TITLE_ASC,
    onSkipNext: () -> Unit = {},
    onAddToQueue: (Song) -> Unit,
    initialTab: LibraryTab = LibraryTab.SONGS,
    musicViewModel: MusicViewModel // Add MusicViewModel as a parameter
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val tabs = listOf("Songs", "Playlists", "Albums")
    var selectedTabIndex by remember { mutableStateOf(initialTab.ordinal) }
    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { tabs.size }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Dialog and bottom sheet states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showAlbumBottomSheet by remember { mutableStateOf(false) }
    var showSongInfoSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    val albumBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    // TopAppBar scroll behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    // FAB visibility based on scroll
    val fabVisibility by remember {
        derivedStateOf {
            scrollBehavior.state.collapsedFraction < 0.5f
        }
    }

    // Sync tabs with pager
    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    
    // Update selectedTabIndex when pager settles on a new page
    LaunchedEffect(pagerState.targetPage) {
        if (selectedTabIndex != pagerState.targetPage) {
            selectedTabIndex = pagerState.targetPage
        }
    }

    // Handle dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
    
    if (showSongInfoSheet && selectedSong != null) {
        SongInfoBottomSheet(
            song = selectedSong!!,
            onDismiss = { showSongInfoSheet = false }
        )
    }
    
    // Use bottom sheet instead of dialog
    if (showAddToPlaylistSheet && selectedSong != null) {
        AddToPlaylistBottomSheet(
            song = selectedSong!!,
            playlists = playlists,
            onDismissRequest = { showAddToPlaylistSheet = false },
            onAddToPlaylist = { playlist ->
                onAddSongToPlaylist(selectedSong!!, playlist.id)
                scope.launch {
                    addToPlaylistSheetState.hide()
                }.invokeOnCompletion {
                    if (!addToPlaylistSheetState.isVisible) {
                        showAddToPlaylistSheet = false
                    }
                }
            },
            onCreateNewPlaylist = {
                scope.launch {
                    addToPlaylistSheetState.hide()
                }.invokeOnCompletion {
                    if (!addToPlaylistSheetState.isVisible) {
                        showAddToPlaylistSheet = false
                        showCreatePlaylistDialog = true
                    }
                }
            },
            sheetState = addToPlaylistSheetState
        )
    }
    
    // Album bottom sheet
    if (showAlbumBottomSheet && selectedAlbum != null) {
        AlbumBottomSheet(
            album = selectedAlbum!!,
            onDismiss = { showAlbumBottomSheet = false },
            onSongClick = onSongClick,
            onPlayAll = { songs ->
                // Play the sorted album songs using proper queue replacement
                if (songs.isNotEmpty()) {
                    onPlayQueue(songs) // Use the new queue replacement callback
                } else {
                    selectedAlbum?.let { onAlbumClick(it) }
                }
            },
            onShufflePlay = { songs ->
                // Play shuffled sorted album songs with proper queue replacement
                if (songs.isNotEmpty()) {
                    onShuffleQueue(songs) // Use the new shuffle queue callback
                } else {
                    selectedAlbum?.let { onAlbumShufflePlay(it) }
                }
            },
            onAddToQueue = onAddToQueue,
            onAddSongToPlaylist = { song ->
                selectedSong = song
                scope.launch {
                    albumBottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!albumBottomSheetState.isVisible) {
                        showAlbumBottomSheet = false
                        showAddToPlaylistSheet = true
                    }
                }
            },
            onPlayerClick = onPlayerClick,
            sheetState = albumBottomSheetState,
            haptics = haptics
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    // Refresh button on far left
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onRefreshClick()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                title = {
                    val expandedTextStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    val collapsedTextStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)

                    val fraction = scrollBehavior.state.collapsedFraction
                    val currentFontSize = lerp(expandedTextStyle.fontSize.value, collapsedTextStyle.fontSize.value, fraction).sp
                    val currentFontWeight = if (fraction < 0.5f) FontWeight.Bold else FontWeight.Bold // Changed to FontWeight.Bold

                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = currentFontSize,
                            fontWeight = currentFontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp) // Added padding
                    )
                },
                actions = {
                    // Sort button with indicator of current sort order
                    FilledTonalButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onSort()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sort order text
                        val sortText = when (sortOrder) {
                            MusicViewModel.SortOrder.TITLE_ASC -> "Title A-Z"
                            MusicViewModel.SortOrder.TITLE_DESC -> "Title Z-A"
                            MusicViewModel.SortOrder.ARTIST_ASC -> "Artist A-Z"
                            MusicViewModel.SortOrder.ARTIST_DESC -> "Artist Z-A"
                        }

                        Text(
                            text = sortText,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.padding(horizontal = 8.dp) // Added padding
            )
        },
        bottomBar = {},
        floatingActionButton = {
            if (selectedTabIndex == LibraryTab.PLAYLISTS.ordinal) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(
                        bottom = (LocalMiniPlayerPadding.current.calculateBottomPadding() * 0.5f) + 8.dp
                    ),
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        showCreatePlaylistDialog = true
                    },
                    icon = {
                        Icon(
                            imageVector = RhythmIcons.Add,
                            contentDescription = null
                        )
                    },
                    text = { Text("New Playlist") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    expanded = fabVisibility
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    Spacer(modifier = Modifier.height(4.dp))
                },
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        androidx.compose.material3.TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            width = tabPositions[selectedTabIndex].contentWidth,
                            height = 3.dp,
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            selectedTabIndex = index
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    LibraryTab.SONGS.ordinal -> SongsTab(
                        songs = songs,
                        onSongClick = onSongClick,
                        onAddToPlaylist = { song ->
                            selectedSong = song
                            showAddToPlaylistSheet = true
                        },
                        onAddToQueue = onAddToQueue,
                        onShowSongInfo = { song ->
                            selectedSong = song
                            showSongInfoSheet = true
                        },
                        onAddToBlacklist = { song ->
                            appSettings.addToBlacklist(song.id)
                        },
                        haptics = haptics
                    )
                    LibraryTab.PLAYLISTS.ordinal -> PlaylistsTab(
                        playlists = playlists,
                        onPlaylistClick = onPlaylistClick,
                        haptics = haptics
                    )
                    LibraryTab.ALBUMS.ordinal -> AlbumsTab(
                        albums = albums,
                        onAlbumClick = onAlbumClick,
                        onSongClick = onSongClick,
                        onAlbumBottomSheetClick = { album ->
                            selectedAlbum = album
                            showAlbumBottomSheet = true
                        },
                        haptics = haptics
                    )
                }
            }
        }
    }
}

@Composable
fun SongsTab(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onAddToBlacklist: (Song) -> Unit, // Add blacklist callback
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Define categories based on song properties - only show working filters
    val categories = remember(songs) {
        val allCategories = mutableListOf("All")
        
        // Extract actual music genres from songs
        val genres = songs.mapNotNull { song ->
            song.genre?.takeIf { it.isNotBlank() && it.lowercase() != "unknown" }
        }.distinct()
        
        // Add genres first (these are the primary categories)
        allCategories.addAll(genres)
        
        // Add song type based categories only if they have songs
        val losslessSongs = songs.filter { song ->
            song.uri.toString().let { uri ->
                uri.contains(".flac", ignoreCase = true) || 
                uri.contains(".alac", ignoreCase = true) ||
                uri.contains(".wav", ignoreCase = true)
            }
        }
        if (losslessSongs.isNotEmpty()) allCategories.add("Lossless")
        
        val highQualitySongs = songs.filter { song ->
            song.uri.toString().let { uri ->
                uri.contains(".m4a", ignoreCase = true) ||
                (uri.contains(".mp3", ignoreCase = true) && song.duration > 0)
            }
        }
        if (highQualitySongs.isNotEmpty()) allCategories.add("High Quality")
        
        val standardSongs = songs.filter { song ->
            song.uri.toString().let { uri ->
                uri.contains(".mp3", ignoreCase = true) ||
                uri.contains(".aac", ignoreCase = true) ||
                uri.contains(".ogg", ignoreCase = true)
            }
        }
        if (standardSongs.isNotEmpty()) allCategories.add("Standard")
        
        // Favorites - songs in sweet spot duration range
        val favoritesSongs = songs.filter { song ->
            song.duration > 2 * 60 * 1000 && song.duration < 6 * 60 * 1000
        }
        if (favoritesSongs.isNotEmpty()) allCategories.add("Favorites")
        
        // Add duration-based categories only if they have songs and not too many genres
        if (genres.size < 3) {
            val shortSongs = songs.filter { it.duration < 3 * 60 * 1000 }
            if (shortSongs.isNotEmpty()) allCategories.add("Short (< 3 min)")
            
            val mediumSongs = songs.filter { it.duration in (3 * 60 * 1000)..(5 * 60 * 1000) }
            if (mediumSongs.isNotEmpty()) allCategories.add("Medium (3-5 min)")
            
            val longSongs = songs.filter { it.duration > 5 * 60 * 1000 }
            if (longSongs.isNotEmpty()) allCategories.add("Long (> 5 min)")
        }
        
        allCategories
    }
    
    // Filter songs based on selected category
    val filteredSongs = remember(songs, selectedCategory) {
        when (selectedCategory) {
            "All" -> songs
            "Short (< 3 min)" -> songs.filter { it.duration < 3 * 60 * 1000 }
            "Medium (3-5 min)" -> songs.filter { it.duration in (3 * 60 * 1000)..(5 * 60 * 1000) }
            "Long (> 5 min)" -> songs.filter { it.duration > 5 * 60 * 1000 }
            "Lossless" -> songs.filter { song ->
                // Filter for high-quality audio files (FLAC, ALAC, etc.)
                song.uri.toString().let { uri ->
                    uri.contains(".flac", ignoreCase = true) || 
                    uri.contains(".alac", ignoreCase = true) ||
                    uri.contains(".wav", ignoreCase = true)
                }
            }
            "High Quality" -> songs.filter { song ->
                // Filter for high bitrate files (320kbps+ MP3, high quality M4A, etc.)
                song.uri.toString().let { uri ->
                    uri.contains(".m4a", ignoreCase = true) ||
                    (uri.contains(".mp3", ignoreCase = true) && song.duration > 0) // Proxy for quality
                }
            }
            "Standard" -> songs.filter { song ->
                // Filter for standard quality files
                song.uri.toString().let { uri ->
                    uri.contains(".mp3", ignoreCase = true) ||
                    uri.contains(".aac", ignoreCase = true) ||
                    uri.contains(".ogg", ignoreCase = true)
                }
            }
            "Recently Added" -> songs.filter { song ->
                // Filter songs added in the last 30 days (using a simple heuristic)
                // Note: This is a simplified approach, in real apps you'd store dateAdded
                System.currentTimeMillis() - song.duration < 30L * 24 * 60 * 60 * 1000 // Placeholder logic
            }
            "Favorites" -> songs.filter { song ->
                // Filter for songs that might be favorites (longer duration as proxy for preference)
                song.duration > 2 * 60 * 1000 && song.duration < 6 * 60 * 1000 // Sweet spot for popular songs
            }
            else -> songs.filter { song ->
                song.genre?.equals(selectedCategory, ignoreCase = true) == true
            }
        }
    }
    
    if (songs.isEmpty()) {
        EmptyState(
            message = "No songs yet",
            icon = RhythmIcons.Music.Song
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Enhanced Songs Section Header (Sticky)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = RhythmIcons.Relax,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Your Music",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${filteredSongs.size} of ${songs.size} tracks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier
                            .height(2.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(1.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
                    ) {}
                }
            }

            // Category chips (Sticky)
            if (categories.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), // Added horizontal padding
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category

                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "chipContainerColor"
                        )
                        val labelColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "chipLabelColor"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "chipBorderColor"
                        )
                        val borderWidth by animateDpAsState(
                            targetValue = if (isSelected) 2.dp else 1.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "chipBorderWidth"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "chipScale"
                        )

                        FilterChip(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                selectedCategory = category
                            },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = containerColor,
                                selectedLabelColor = labelColor,
                                containerColor = containerColor,
                                labelColor = labelColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = borderColor,
                                selectedBorderColor = borderColor,
                                borderWidth = borderWidth
                            ),
                            shape = RoundedCornerShape(50.dp), // More rounded corners
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                        )
                    }
                }
            }
            // Scrollable Songs List
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 8.dp, // Start below the sticky elements
                        bottom = LocalMiniPlayerPadding.current.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(
                        items = filteredSongs,
                        key = { it.id }
                    ) { song ->
                        AnimateIn {
                            LibrarySongItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onMoreClick = { onAddToPlaylist(song) },
                                onAddToQueue = { onAddToQueue(song) },
                                onShowSongInfo = { onShowSongInfo(song) },
                                onAddToBlacklist = { onAddToBlacklist(song) },
                                haptics = haptics // Pass haptics
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    if (playlists.isEmpty()) {
        EmptyState(
            message = "No playlists yet\nCreate your first playlist using the + button",
            icon = RhythmIcons.Music.Playlist
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Enhanced Playlists Section Header (Sticky)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = RhythmIcons.PlaylistFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Your Playlists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${playlists.size} ${if (playlists.size == 1) "playlist" else "playlists"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier
                            .height(2.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    ) {}
                }
            }

            // Scrollable Playlists List
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 8.dp, // Start below the sticky elements
                        bottom = LocalMiniPlayerPadding.current.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(
                        items = playlists,
                        key = { it.id }
                    ) { playlist ->
                        AnimateIn {
                            PlaylistItem(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) },
                                haptics = haptics // Pass haptics
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumsTab(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumBottomSheetClick: (Album) -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val albumViewType by appSettings.albumViewType.collectAsState()

    if (albums.isEmpty()) {
        EmptyState(
            message = "No albums yet",
            icon = RhythmIcons.Music.Album
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Enhanced Albums Section Header (Sticky)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = RhythmIcons.Music.Album,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Your Albums",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${albums.size} ${if (albums.size == 1) "album" else "albums"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // View type toggle button
                    FilledIconButton(
                        onClick = {
                            val newViewType = if (albumViewType == AlbumViewType.LIST) AlbumViewType.GRID else AlbumViewType.LIST
                            appSettings.setAlbumViewType(newViewType)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (albumViewType == AlbumViewType.LIST) RhythmIcons.AppsGrid else RhythmIcons.List,
                            contentDescription = "Toggle view type",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier
                            .height(2.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    ) {}
                }
            }

            // Scrollable Albums Content
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
            ) {
                if (albumViewType == AlbumViewType.GRID) {
                    AlbumsGrid(
                        albums = albums,
                        onAlbumClick = { album ->
                            onAlbumBottomSheetClick(album)
                        },
                        onAlbumPlay = onAlbumClick, // This plays the album
                        onSongClick = onSongClick,
                        haptics = haptics // Pass haptics
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp, // Start below the sticky elements
                            bottom = LocalMiniPlayerPadding.current.calculateBottomPadding() + 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(
                            items = albums,
                            key = { it.id }
                        ) { album ->
                            AnimateIn {
                                LibraryAlbumItem(
                                    album = album,
                                    onClick = { onAlbumBottomSheetClick(album) }, // Changed to open bottom sheet
                                    onPlayClick = {
                                        // Play the entire album
                                        onAlbumClick(album)
                                    },
                                    haptics = haptics // Pass haptics
                                )
                            }
                        }
                    }
                }
            }
        }
    }
                    }


@Composable
fun LibrarySongItem(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onShowSongInfo: () -> Unit,
    onAddToBlacklist: () -> Unit, // Add blacklist callback
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = song.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.size(56.dp)
            ) {
                M3ImageUtils.TrackImage(
                    imageUrl = song.artworkUri,
                    trackName = song.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        trailingContent = {
            Row {
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        onAddToQueue()
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = RhythmIcons.Queue,
                        contentDescription = "Add to queue",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        showDropdown = true
                    },
                    modifier = Modifier.size(36.dp),
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
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        showDropdown = false
                    },
                    modifier = Modifier
                        .widthIn(min = 200.dp)
                        .padding(4.dp), // Adjusted padding for the menu itself
                    shape = RoundedCornerShape(16.dp) // Consistent shape
                ) {
                    // Add to playlist
                    Surface(
                        color = Color.Transparent, // Make item background transparent to show menu background
                        shape = RoundedCornerShape(12.dp), // Rounded corners for the item background
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp) // Adjusted padding to match album sort menu
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Add to playlist",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface // Default text color
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
                                onMoreClick()
                            }
                        )
                    }

                    // Song info
                    Surface(
                        color = Color.Transparent, // Make item background transparent to show menu background
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp) // Adjusted padding to match album sort menu
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
                    Surface(
                        color = Color.Transparent, // Make item background transparent to show menu background
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp) // Adjusted padding to match album sort menu
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Add to blacklist",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error // Keep error color for blacklist
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showDropdown = false
                                onAddToBlacklist()
                            }
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                onClick()
            })
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    Surface(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced playlist artwork with proper playlist icon
            Surface(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(25.dp),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (playlist.artworkUri != null) Color.Transparent 
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (playlist.artworkUri != null) {
                        M3ImageUtils.PlaylistImage(
                            imageUrl = playlist.artworkUri,
                            playlistName = playlist.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Use proper playlist icon from RhythmIcons
                        Icon(
                            imageVector = RhythmIcons.PlaylistFilled, // Using the proper playlist icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(18.dp))
            
            // Enhanced playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = "${playlist.songs.size} ${if (playlist.songs.size == 1) "song" else "songs"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (playlist.songs.isNotEmpty()) {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        val totalDurationMs = playlist.songs.sumOf { it.duration }
                        val totalMinutes = (totalDurationMs / (1000 * 60)).toInt()
                        val durationText = if (totalMinutes >= 60) {
                            val hours = totalMinutes / 60
                            val minutes = totalMinutes % 60
                            "${hours}h ${minutes}m"
                        } else {
                            "${totalMinutes}m"
                        }
                        
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Forward arrow with enhanced styling
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open playlist",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryAlbumItem(
    album: Album,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    
    Surface(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced album artwork
            Surface(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (album.artworkUri != null) Color.Transparent 
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (album.artworkUri != null) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = album.artworkUri,
                            albumName = album.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = RhythmIcons.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(18.dp))
            
            // Enhanced album info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp) // Add padding to prevent text from being cut off
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp)) // Increase spacing
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp)) // Increase spacing
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${album.numberOfSongs} ${if (album.numberOfSongs == 1) "song" else "songs"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    if (album.year > 0) {
                        Text(
                            text = " • ${album.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Enhanced play button
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    onPlayClick()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play album",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(48.dp)
            ) {
                val animatedSize by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 100f
                    ),
                    label = "iconAnimation"
                )
                
                val animatedAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 800,
                        delayMillis = 200
                    ),
                    label = "alphaAnimation"
                )
                
                // Enhanced icon container with gradient background
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer { alpha = animatedAlpha }
                        )
                    }
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Start building your music collection",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha * 0.8f }
                )
            }
        }
    }
}

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
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
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
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${album.songs.size} songs",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            
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
                                    // Clear queue first, then play sorted songs in order
                                    if (sortedSongs.isNotEmpty()) {
                                        // Pass the sorted songs to the callback for proper queue management
                                        onPlayAll(sortedSongs)
                                        onSongClick(sortedSongs.first()) // Start with first sorted song
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
                                    // Shuffle the sorted songs and play
                                    if (sortedSongs.isNotEmpty()) {
                                        // Pass the sorted and shuffled songs to the callback
                                        val shuffledSongs = sortedSongs.shuffled()
                                        onShufflePlay(shuffledSongs)
                                        onSongClick(shuffledSongs.first()) // Start with first shuffled song
                                    } else {
                                        val shuffledSongs = album.songs.shuffled()
                                        onShufflePlay(shuffledSongs)
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
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
                ) {
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onAddToQueue()
                        },
                        modifier = Modifier.size(36.dp), // Reduced size
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Player.Queue,
                            contentDescription = "Add to queue",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onAddToPlaylist()
                        },
                        modifier = Modifier.size(36.dp), // Reduced size
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = "Add to playlist",
                            modifier = Modifier.size(20.dp)
                        )
                    }
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

@Composable
private fun AnimateIn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 50),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.graphicsLayer(
            alpha = alpha,
            scaleX = scale,
            scaleY = scale
        )
    ) {
        content()
    }
}

@Composable
fun AlbumsGrid(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAlbumPlay: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = LocalMiniPlayerPadding.current.calculateBottomPadding() + 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = albums,
            key = { it.id }
        ) { album ->
            AnimateIn {
                AlbumGridItem(
                    album = album,
                    onClick = { onAlbumClick(album) }, // Card click opens bottom sheet
                    onPlayClick = { onAlbumPlay(album) }, // Play button plays album
                    haptics = haptics // Pass haptics
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .aspectRatio(0.72f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Album artwork - maintain square ratio
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 0.dp,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (album.artworkUri != null) Color.Transparent 
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (album.artworkUri != null) {
                            M3ImageUtils.AlbumArt(
                                imageUrl = album.artworkUri,
                                albumName = album.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = RhythmIcons.Album,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Album title
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Artist name
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Song count
                Text(
                    text = "${album.numberOfSongs} ${if (album.numberOfSongs == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        onPlayClick()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play album",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
