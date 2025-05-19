package chromahub.rhythm.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.ui.components.CreatePlaylistDialog
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.screens.LibraryScreen
import chromahub.rhythm.app.ui.screens.NewHomeScreen
import chromahub.rhythm.app.ui.screens.PlayerLocationsScreen
import chromahub.rhythm.app.ui.screens.PlayerScreen
import chromahub.rhythm.app.ui.screens.PlaylistDetailScreen
import chromahub.rhythm.app.ui.screens.SearchScreen
import chromahub.rhythm.app.ui.screens.SettingsScreen
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Player : Screen("player")
    object Settings : Screen("settings")
    object AddToPlaylist : Screen("add_to_playlist")
    object PlayerLocations : Screen("player_locations")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist/$playlistId"
    }
}

@Composable
fun RhythmNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: MusicViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    // Collect state from ViewModel
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val showLyrics by viewModel.showLyrics.collectAsState()
    val showOnlineOnlyLyrics by viewModel.showOnlineOnlyLyrics.collectAsState()
    val lyrics by viewModel.currentLyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()
    
    // Theme state
    val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
    val darkMode by themeViewModel.darkMode.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val onPlayPause = { viewModel.togglePlayPause() }
    val onSkipNext = { viewModel.skipToNext() }
    val onSkipPrevious = { viewModel.skipToPrevious() }
    val onSeek = { value: Float -> viewModel.seekTo(value) }
    val onPlaySong = { song: chromahub.rhythm.app.data.Song -> viewModel.playSong(song) }
    val onPlayAlbum = { album: chromahub.rhythm.app.data.Album -> viewModel.playAlbum(album) }
    val onPlayArtist = { artist: chromahub.rhythm.app.data.Artist -> viewModel.playArtist(artist) }
    val onPlayPlaylist = { playlist: chromahub.rhythm.app.data.Playlist -> viewModel.playPlaylist(playlist) }
    val onToggleShuffle = { viewModel.toggleShuffle() }
    val onToggleRepeat = { viewModel.toggleRepeatMode() }
    val onToggleFavorite = { viewModel.toggleFavorite() }
    
    // Track current destination for hiding navigation bar on player screen
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    
    // Update current route when destination changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: Screen.Home.route
            // Update selectedTab based on current route
            when (currentRoute) {
                Screen.Home.route -> selectedTab = 0
                Screen.Search.route -> selectedTab = 1
                Screen.Library.route -> selectedTab = 2
                Screen.Settings.route -> selectedTab = 3
            }
        }
    }

    Scaffold(
        bottomBar = {
            // Only show the navigation bar if we're not on the player screen or related screens
            if (currentRoute != Screen.Player.route && 
                currentRoute != Screen.PlayerLocations.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val items = listOf(
                        Triple(Screen.Home.route, "Home", 
                            Pair(RhythmIcons.HomeFilled, RhythmIcons.Home)),
                        Triple(Screen.Search.route, "Search", 
                            Pair(RhythmIcons.SearchFilled, RhythmIcons.Search)),
                        Triple(Screen.Library.route, "Library", 
                            Pair(RhythmIcons.Library, RhythmIcons.Library)),
                        Triple(Screen.Settings.route, "Settings", 
                            Pair(RhythmIcons.SettingsFilled, RhythmIcons.Settings))
                    )
                    
                    items.forEachIndexed { index, (route, title, icons) ->
                        val selected = currentRoute == route
                        val (selectedIcon, unselectedIcon) = icons
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                selectedTab = index
                                navController.navigate(route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            },
                            icon = {
                                AnimatedContent(
                                    targetState = selected,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(150)) togetherWith
                                        fadeOut(animationSpec = tween(150))
                                    },
                                    label = "IconAnimation"
                                ) { isSelected ->
                                    Icon(
                                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                        contentDescription = title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                NewHomeScreen(
                    songs = songs,
                    albums = albums,
                    artists = artists,
                    recentlyPlayed = recentlyPlayed,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onSongClick = onPlaySong,
                    onAlbumClick = onPlayAlbum,
                    onArtistClick = onPlayArtist,
                    onPlayPause = onPlayPause,
                    onPlayerClick = {
                        navController.navigate(Screen.Player.route)
                    },
                    onViewAllSongs = {
                        // Navigate to songs screen
                        navController.navigate(Screen.Library.route)
                        selectedTab = 2
                    },
                    onViewAllAlbums = {
                        // Navigate to albums screen
                        navController.navigate(Screen.Library.route)
                        selectedTab = 2
                    },
                    onViewAllArtists = {
                        // Navigate to artists screen
                        navController.navigate(Screen.Library.route)
                        selectedTab = 2
                    },
                    onSkipNext = onSkipNext,
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                        selectedTab = 1
                    }
                )
            }
            
            composable(Screen.Search.route) {
                SearchScreen(
                    songs = songs,
                    albums = albums,
                    artists = artists,
                    playlists = playlists,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onSongClick = onPlaySong,
                    onAlbumClick = onPlayAlbum,
                    onArtistClick = onPlayArtist,
                    onPlaylistClick = { playlist ->
                        // Navigate to playlist detail screen
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                    },
                    onPlayPause = onPlayPause,
                    onPlayerClick = {
                        navController.navigate(Screen.Player.route)
                    },
                    onAddSongToPlaylist = { song ->
                        // Store the selected song in the ViewModel and navigate to add to playlist screen
                        viewModel.setSelectedSongForPlaylist(song)
                        navController.navigate(Screen.AddToPlaylist.route)
                    },
                    onSkipNext = onSkipNext
                )
            }
            
            composable(Screen.Library.route) {
                LibraryScreen(
                    songs = songs,
                    albums = albums,
                    playlists = playlists,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onSongClick = onPlaySong,
                    onPlayPause = onPlayPause,
                    onPlayerClick = {
                        navController.navigate(Screen.Player.route)
                    },
                    onPlaylistClick = { playlist ->
                        // Navigate to playlist detail screen
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                    },
                    onAddPlaylist = {
                        // This is now handled internally with the dialog
                    },
                    onAlbumClick = onPlayAlbum,
                    onSort = {
                        // Implement sort functionality
                        viewModel.sortLibrary()
                    },
                    onAddSongToPlaylist = { song, playlistId ->
                        // Add song to playlist
                        viewModel.addSongToPlaylist(song, playlistId)
                    },
                    onCreatePlaylist = { name ->
                        // Create new playlist
                        viewModel.createPlaylist(name)
                    },
                    sortOrder = sortOrder,
                    onSkipNext = onSkipNext,
                    onAddToQueue = { song ->
                        // Add song to queue
                        viewModel.addSongToQueue(song)
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onPlayPause = onPlayPause,
                    onPlayerClick = {
                        navController.navigate(Screen.Player.route)
                    },
                    onSkipNext = onSkipNext,
                    showLyrics = showLyrics,
                    showOnlineOnlyLyrics = showOnlineOnlyLyrics,
                    onShowLyricsChange = { show ->
                        viewModel.setShowLyrics(show)
                    },
                    onShowOnlineOnlyLyricsChange = { onlineOnly ->
                        viewModel.setShowOnlineOnlyLyrics(onlineOnly)
                    },
                    useSystemTheme = useSystemTheme,
                    darkMode = darkMode,
                    onUseSystemThemeChange = { use ->
                        themeViewModel.setUseSystemTheme(use)
                    },
                    onDarkModeChange = { dark ->
                        themeViewModel.setDarkMode(dark)
                    },
                    onOpenSystemEqualizer = {
                        viewModel.openSystemEqualizer()
                    }
                )
            }
            
            composable(
                route = Screen.Player.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },  // slide in from bottom (full height)
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },  // slide out to bottom (full height)
                        animationSpec = tween(durationMillis = 250)
                    )
                }
            ) {
                val showAddToPlaylistSheet = remember { mutableStateOf(false) }
                val showCreatePlaylistDialog = remember { mutableStateOf(false) }
                
                // If we're returning from AddToPlaylist route with a song to add, show the bottom sheet
                LaunchedEffect(viewModel.selectedSongForPlaylist.collectAsState().value) {
                    if (viewModel.selectedSongForPlaylist.value != null) {
                        showAddToPlaylistSheet.value = true
                    }
                }
                
                // Show create playlist dialog if needed
                if (showCreatePlaylistDialog.value) {
                    // Get the non-delegated value of currentSong
                    val songForDialog = currentSong
                    if (songForDialog != null) {
                        CreatePlaylistDialog(
                            onDismiss = { 
                                showCreatePlaylistDialog.value = false 
                            },
                            onConfirm = { name ->
                                viewModel.createPlaylist(name)
                                showCreatePlaylistDialog.value = false
                            },
                            song = songForDialog,
                            onConfirmWithSong = { name ->
                                viewModel.createPlaylist(name)
                                // The new playlist will be at the end of the list
                                val newPlaylist = viewModel.playlists.value.last()
                                viewModel.addSongToPlaylist(songForDialog, newPlaylist.id)
                                showCreatePlaylistDialog.value = false
                            }
                        )
                    }
                }
                
                PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    location = currentDevice,
                    queuePosition = viewModel.currentQueue.collectAsState().value.currentIndex + 1,
                    queueTotal = viewModel.currentQueue.collectAsState().value.songs.size,
                    onPlayPause = onPlayPause,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onSeek = { position ->
                        // Convert the progress (0-1) to milliseconds
                        val positionMs = (currentSong?.duration ?: 0) * position
                        viewModel.seekTo(positionMs.toLong())
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onLocationClick = {
                        // Show the system output switcher dialog directly
                        viewModel.showOutputSwitcherDialog()
                    },
                    onQueueClick = {
                        // Show queue bottom sheet directly in PlayerScreen
                        // No need to navigate to a separate screen
                    },
                    onToggleShuffle = {
                        viewModel.toggleShuffle()
                    },
                    onToggleRepeat = {
                        viewModel.toggleRepeatMode()
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite()
                    },
                    onAddToPlaylist = {
                        currentSong?.let { song ->
                            viewModel.setSelectedSongForPlaylist(song)
                            showAddToPlaylistSheet.value = true
                        }
                    },
                    isShuffleEnabled = isShuffleEnabled,
                    repeatMode = repeatMode,
                    isFavorite = isFavorite,
                    showLyrics = showLyrics,
                    onlineOnlyLyrics = showOnlineOnlyLyrics,
                    lyrics = lyrics,
                    isLoadingLyrics = isLoadingLyrics,
                    volume = viewModel.volume.collectAsState().value,
                    isMuted = viewModel.isMuted.collectAsState().value,
                    onVolumeChange = { volume ->
                        viewModel.setVolume(volume)
                    },
                    onToggleMute = {
                        viewModel.toggleMute()
                    },
                    onMaxVolume = {
                        viewModel.maxVolume()
                    },
                    playlists = playlists,
                    queue = viewModel.currentQueue.collectAsState().value.songs,
                    onSongClick = { song ->
                        // Play the selected song from the queue
                        viewModel.playSong(song)
                    },
                    onRemoveFromQueue = { song ->
                        viewModel.removeFromQueue(song)
                    },
                    onMoveQueueItem = { fromIndex, toIndex ->
                        viewModel.moveQueueItem(fromIndex, toIndex)
                    },
                    onAddSongsToQueue = {
                        viewModel.addSongsToQueue()
                    },
                    showAddToPlaylistSheet = showAddToPlaylistSheet.value,
                    onAddToPlaylistSheetDismiss = {
                        showAddToPlaylistSheet.value = false
                        viewModel.clearSelectedSongForPlaylist()
                    },
                    onAddSongToPlaylist = { song, playlistId ->
                        viewModel.addSongToPlaylist(song, playlistId)
                    },
                    onCreatePlaylist = { name ->
                        viewModel.createPlaylist(name)
                    },
                    onShowCreatePlaylistDialog = {
                        showCreatePlaylistDialog.value = true
                    }
                )
            }
            
            // Player Locations screen
            composable(Screen.PlayerLocations.route) {
                PlayerLocationsScreen(
                    locations = viewModel.locations.collectAsState().value,
                    currentLocation = currentDevice,
                    onLocationSelect = { location ->
                        // Set the selected location
                        viewModel.setCurrentDevice(location)
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Add playlist detail screen
            @OptIn(ExperimentalMaterial3Api::class)
            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(
                    navArgument("playlistId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                val playlist = playlists.find { it.id == playlistId }
                
                if (playlist != null) {
                    PlaylistDetailScreen(
                        playlist = playlist,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        progress = progress,
                        onPlayPause = onPlayPause,
                        onPlayerClick = {
                            navController.navigate(Screen.Player.route)
                        },
                        onPlayAll = {
                            onPlayPlaylist(playlist)
                        },
                        onSongClick = onPlaySong,
                        onBack = {
                            navController.popBackStack()
                        },
                        onRemoveSong = { song ->
                            viewModel.removeSongFromPlaylist(song, playlistId)
                        },
                        onRenamePlaylist = { newName ->
                            viewModel.renamePlaylist(playlistId, newName)
                        },
                        onDeletePlaylist = {
                            viewModel.deletePlaylist(playlistId)
                            navController.popBackStack()
                        },
                        onAddSongsToPlaylist = {
                            // Set the target playlist ID and navigate to search screen
                            viewModel.setTargetPlaylistForAddingSongs(playlistId)
                            navController.navigate(Screen.AddToPlaylist.route)
                        },
                        onSkipNext = onSkipNext
                    )
                }
            }
            
            // Add to playlist screen
            @OptIn(ExperimentalMaterial3Api::class)
            composable(Screen.AddToPlaylist.route) {
                val songToAdd = viewModel.selectedSongForPlaylist.collectAsState().value
                val targetPlaylistId = viewModel.targetPlaylistId.collectAsState().value
                
                // If we have a target playlist ID, we're adding songs to that playlist
                if (targetPlaylistId != null) {
                    val targetPlaylist = playlists.find { it.id == targetPlaylistId }
                    
                    if (targetPlaylist != null) {
                        // Show song selection screen
                        val availableSongs = songs.filter { song ->
                            // Filter out songs that are already in the playlist
                            !targetPlaylist.songs.any { it.id == song.id }
                        }
                        
                        if (availableSongs.isEmpty()) {
                            // No songs to add
                            AlertDialog(
                                onDismissRequest = {
                                    viewModel.clearTargetPlaylistForAddingSongs()
                                    navController.popBackStack()
                                },
                                title = { Text("No Songs Available") },
                                text = { Text("All songs are already in this playlist.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.clearTargetPlaylistForAddingSongs()
                                        navController.popBackStack()
                                    }) {
                                        Text("OK")
                                    }
                                }
                            )
                        } else {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = { Text("Add Songs to ${targetPlaylist.name}") },
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                viewModel.clearTargetPlaylistForAddingSongs()
                                                navController.popBackStack()
                                            }) {
                                                Icon(
                                                    imageVector = RhythmIcons.Back,
                                                    contentDescription = "Back"
                                                )
                                            }
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                LazyColumn(
                                    modifier = Modifier.padding(innerPadding),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(availableSongs) { song ->
                                        ListItem(
                                            headlineContent = { Text(song.title) },
                                            supportingContent = { Text("${song.artist} • ${song.album}") },
                                            leadingContent = {
                                                AsyncImage(
                                                    model = song.artworkUri,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            },
                                            trailingContent = {
                                                IconButton(onClick = {
                                                    viewModel.addSongToPlaylist(song, targetPlaylistId)
                                                }) {
                                                    Icon(
                                                        imageVector = RhythmIcons.Add,
                                                        contentDescription = "Add to playlist"
                                                    )
                                                }
                                            },
                                            modifier = Modifier.clickable {
                                                viewModel.addSongToPlaylist(song, targetPlaylistId)
                                                // Show a snackbar or some feedback
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Playlist not found, go back
                        LaunchedEffect(Unit) {
                            viewModel.clearTargetPlaylistForAddingSongs()
                            navController.popBackStack()
                        }
                    }
                }
                // If we have a song to add (from Player or Search), we're adding it to a playlist
                else if (songToAdd != null) {
                    // Use a simpler approach without the bottom sheet state
                    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
                    
                    if (showCreatePlaylistDialog) {
                        CreatePlaylistDialog(
                            onDismiss = {
                                showCreatePlaylistDialog = false
                            },
                            onConfirm = { name ->
                                viewModel.createPlaylist(name)
                                showCreatePlaylistDialog = false
                            },
                            song = songToAdd,
                            onConfirmWithSong = { name ->
                                viewModel.createPlaylist(name)
                                // The new playlist will be at the end of the list
                                val newPlaylist = viewModel.playlists.value.last()
                                viewModel.addSongToPlaylist(songToAdd, newPlaylist.id)
                                viewModel.clearSelectedSongForPlaylist()
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // Navigate back to the player screen and show the bottom sheet there
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                } else {
                    // No song selected and no target playlist, go back
                    LaunchedEffect(Unit) {
                        viewModel.clearSelectedSongForPlaylist()
                        viewModel.clearTargetPlaylistForAddingSongs()
                        navController.popBackStack()
                    }
                }
            }
        }
    }
} 