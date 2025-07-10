@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package chromahub.rhythm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.AlbumViewType
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.ui.components.CreatePlaylistDialog
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.screens.SongInfoBottomSheet
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.material3.ListItemDefaults


enum class LibraryTab { SONGS, PLAYLISTS }

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
    onSort: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = { _ -> },
    sortOrder: MusicViewModel.SortOrder = MusicViewModel.SortOrder.TITLE_ASC,
    onSkipNext: () -> Unit = {},
    onAddToQueue: (Song) -> Unit,
    initialTab: LibraryTab = LibraryTab.SONGS
) {
    val tabs = listOf("Songs", "Playlists & Albums")
    var selectedTabIndex by remember { mutableStateOf(initialTab.ordinal) }
    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { tabs.size }
    val scope = rememberCoroutineScope()
    
    // Dialog and bottom sheet states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showAlbumBottomSheet by remember { mutableStateOf(false) }
    var showSongInfoSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    val albumBottomSheetState = rememberModalBottomSheetState()
    
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
    
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
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
            onPlayAll = { 
                // Play the entire album using the proper album playback
                selectedAlbum?.let { onAlbumClick(it) }
            },
            onShufflePlay = {
                // Play shuffled album songs using the proper shuffled album playback
                selectedAlbum?.let { onAlbumShufflePlay(it) }
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
            sheetState = albumBottomSheetState
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    // Search button on far left
                    FilledIconButton(
                        onClick = onSearchClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Search,
                            contentDescription = "Search"
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
                        onClick = onSort,
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
            if (selectedTabIndex == 1) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(
                        bottom = (LocalMiniPlayerPadding.current.calculateBottomPadding() * 0.5f) + 8.dp
                    ),
                    onClick = { showCreatePlaylistDialog = true },
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
                    0 -> SongsTab(
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
                        }
                    )
                    1 -> PlaylistsTab(
                        playlists = playlists,
                        albums = albums,
                        onPlaylistClick = onPlaylistClick,
                        onAlbumClick = onAlbumClick,
                        onSongClick = onSongClick,
                        onAlbumBottomSheetClick = { album ->
                            selectedAlbum = album
                            showAlbumBottomSheet = true
                        }
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
    onShowSongInfo: (Song) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Define categories based on song properties
    val categories = remember(songs) {
        val allCategories = mutableListOf("All")
        
        // Extract actual music genres from songs
        val genres = songs.mapNotNull { song ->
            song.genre?.takeIf { it.isNotBlank() && it.lowercase() != "unknown" }
        }.distinct() // Removed .sorted() to keep original order
        
        // Add genres first (these are the primary categories)
        allCategories.addAll(genres)
        
        // Only add other categories if we have fewer than 5 genres
        if (genres.size < 5) {
            // Add duration-based categories
            allCategories.add("Short (< 3 min)")
            allCategories.add("Medium (3-5 min)")
            allCategories.add("Long (> 5 min)")
            
            // Add alphabetical categories only if really needed
            if (genres.size < 3) {
                allCategories.add("A-M")
                allCategories.add("N-Z")
            }
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
            "A-M" -> songs.filter { it.title.firstOrNull()?.uppercaseChar() in 'A'..'M' }
            "N-Z" -> songs.filter { it.title.firstOrNull()?.uppercaseChar() in 'N'..'Z' }
            else -> songs.filter { song ->
                song.genre?.equals(selectedCategory, ignoreCase = true) == true
            }
        }
    }
    
    if (songs.isEmpty()) {
        EmptyState(
            message = "No songs yet",
            icon = RhythmIcons.Song
        )
    } else {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = LocalMiniPlayerPadding.current.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = RhythmIcons.Song,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "On-Device Tracks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "${filteredSongs.size} of ${songs.size} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Category chips
                if (categories.size > 1) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    onClick = { selectedCategory = category },
                                    label = { 
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    selected = selectedCategory == category,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
                
                items(
                    items = filteredSongs,
                    key = { it.id }
                ) { song ->
                    LibrarySongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { onAddToPlaylist(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onShowSongInfo = { onShowSongInfo(song) }
                    )
                }
            }
        }
    }
    }


@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    albums: List<Album>,
    onPlaylistClick: (Playlist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumBottomSheetClick: (Album) -> Unit = {}
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val albumViewType by appSettings.albumViewType.collectAsState()
    
    if (playlists.isEmpty() && albums.isEmpty()) {
        EmptyState(
            message = "No playlists yet\nCreate your first playlist using the + button",
            icon = RhythmIcons.AddToPlaylist
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = LocalMiniPlayerPadding.current.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (playlists.isNotEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Playlists",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }
                
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) }
                    )
                }
            }
            
            if (albums.isNotEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Albums",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

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
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (albumViewType == AlbumViewType.LIST) RhythmIcons.AppsGrid else RhythmIcons.List,
                                    contentDescription = "Toggle view type",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }
                
                // Albums content based on view type
                if (albumViewType == AlbumViewType.GRID) {
                    item {
                        AlbumsGrid(
                            albums = albums,
                            onAlbumClick = { album ->
                                onAlbumBottomSheetClick(album)
                            },
                            onAlbumPlay = onAlbumClick, // This plays the album
                            onSongClick = onSongClick
                        )
                    }
                } else {
                    items(albums) { album ->
                        LibraryAlbumItem(
                            album = album,
                            onClick = { onAlbumBottomSheetClick(album) }, // Changed to open bottom sheet
                            onPlayClick = { 
                                // Play the entire album
                                onAlbumClick(album)
                            }
                        )
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
    onShowSongInfo: () -> Unit
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
                    onClick = { onAddToQueue() },
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
                    onClick = { showDropdown = true },
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
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier,
                    shape = MaterialTheme.shapes.medium
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to playlist") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showDropdown = false
                            onMoreClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Song info") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showDropdown = false
                            onShowSongInfo()
                        }
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced playlist artwork with proper playlist icon
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
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
                            imageVector = RhythmIcons.AddToPlaylist, // Using the proper playlist icon consistently
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
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
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open playlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
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
    onPlayClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced album artwork
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (album.artworkUri != null) Color.Transparent 
                            else MaterialTheme.colorScheme.primaryContainer
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    if (album.year != null && album.year > 0) {
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
                onClick = onPlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val animatedSize by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 100f
                ),
                label = "iconAnimation"
            )
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(80.dp * animatedSize)
                    .padding(bottom = 24.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumBottomSheet(
    album: Album,
    onDismiss: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    sheetState: SheetState
) {
    val context = LocalContext.current
    
    // Animation states inspired by ArtistBottomSheet
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
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced Album Header with gradient overlay and improved layout
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
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
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.size(140.dp)
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
                    
                    // Horizontal gradient for enhanced depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                    
                    // Enhanced close button with blur effect simulation
                    FilledIconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(20.dp)
                            .align(Alignment.TopEnd)
                            .size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Close,
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
                            
                            if (album.year != null && album.year > 0) {
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
                            // Enhanced Play All button
                            Button(
                                onClick = onPlayAll,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Play,
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

                            // Enhanced Shuffle button
                            FilledIconButton(
                                onClick = onShufflePlay,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Shuffle,
                                    contentDescription = "Shuffle play",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Enhanced Songs header with better spacing and design
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .graphicsLayer { 
                            alpha = contentAlpha
                            translationY = contentTranslation
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Songs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${album.songs.size}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Enhanced Songs list with improved animations
            if (album.songs.isNotEmpty()) {
                items(
                    items = album.songs,
                    key = { it.id }
                ) { song ->
                    EnhancedAlbumSongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onAddToPlaylist = { onAddSongToPlaylist(song) },
                        modifier = Modifier
                            .animateItemPlacement()
                            .graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentTranslation
                            }
                    )
                }
            } else {
                item {
                    // Empty state for no songs
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .graphicsLayer { 
                                alpha = contentAlpha
                                translationY = contentTranslation
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
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
            
            // Enhanced bottom spacing
            item {
                Spacer(
                    modifier = Modifier
                        .height(40.dp)
                        .graphicsLayer { alpha = contentAlpha }
                )
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
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
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${song.trackNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(52.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = onAddToQueue,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Queue,
                            contentDescription = "Add to queue",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.More,
                            contentDescription = "More options",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Add to playlist",
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = RhythmIcons.AddToPlaylist,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showDropdown = false
                                onAddToPlaylist()
                            }
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
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
fun AlbumsGrid(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAlbumPlay: (Album) -> Unit,
    onSongClick: (Song) -> Unit
) {
    val albumsPerRow = 2
    val albumRows = albums.chunked(albumsPerRow)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        albumRows.forEach { rowAlbums ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAlbums.forEach { album ->
                    AlbumGridItem(
                        album = album,
                        onClick = { onAlbumClick(album) }, // Card click opens bottom sheet
                        onPlayClick = { onAlbumPlay(album) }, // Play button plays album
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if the row is not complete
                repeat(albumsPerRow - rowAlbums.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Add bottom padding
        Spacer(modifier = Modifier.height(LocalMiniPlayerPadding.current.calculateBottomPadding()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(0.72f), // Further reduced from 0.8f to 0.75f for more vertical space
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    .padding(10.dp) // Reduced from 12.dp to 10.dp
            ) {
                // Album artwork - maintain square ratio
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f), // Keep artwork square
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (album.artworkUri != null) Color.Transparent 
                                else MaterialTheme.colorScheme.primaryContainer
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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp)) // Reduced from 16.dp to 12.dp
                
                // Album title - made more prominent with more padding
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, // Reduced from 2 to 1 to save space
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp, // Reduced from 18.sp
                    modifier = Modifier.padding(horizontal = 2.dp) // Reduced from 4.dp
                )
                
                Spacer(modifier = Modifier.height(4.dp)) // Reduced from 6.dp
                
                // Artist name with more padding
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp) // Reduced from 4.dp
                )
                
                Spacer(modifier = Modifier.height(2.dp)) // Reduced from 4.dp
                
                // Song count
                Text(
                    text = "${album.numberOfSongs} ${if (album.numberOfSongs == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Play button overlay - made smaller to save space
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp) // Reduced from 10.dp
            ) {
                FilledIconButton(
                    onClick = onPlayClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(32.dp) // Reduced from 36.dp
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play album",
                        modifier = Modifier.size(16.dp) // Reduced from 18.dp
                    )
                }
            }
        }
    }
}
