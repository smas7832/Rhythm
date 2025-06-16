package chromahub.rhythm.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import chromahub.rhythm.app.ui.UiConstants
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
import chromahub.rhythm.app.ui.screens.AppUpdaterScreen
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
import androidx.compose.animation.togetherWith
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import chromahub.rhythm.app.ui.screens.LibraryTab

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library?tab={tab}") {
        fun createRoute(tab: LibraryTab = LibraryTab.SONGS): String = "library?tab=${tab.name.lowercase()}"
    }
    object Player : Screen("player")
    object Settings : Screen("settings")
    object AddToPlaylist : Screen("add_to_playlist")
    object PlayerLocations : Screen("player_locations")
    object AppUpdater : Screen("app_updater?autoDownload={autoDownload}") {
        fun createRoute(autoDownload: Boolean = false) = "app_updater?autoDownload=$autoDownload"
    }
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
    val onPlayPlaylist =
        { playlist: chromahub.rhythm.app.data.Playlist -> viewModel.playPlaylist(playlist) }
    val onToggleShuffle = { viewModel.toggleShuffle() }
    val onToggleRepeat = { viewModel.toggleRepeatMode() }
    val onToggleFavorite = { viewModel.toggleFavorite() }

    // Player click navigates to full player screen
    val onPlayerClick = {
        navController.navigate(Screen.Player.route)
    }

    // Track current destination for hiding navigation bar on player screen
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }

    // Update current route when destination changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: Screen.Home.route
            // Update selectedTab based on current route
            when {
                currentRoute == Screen.Home.route -> selectedTab = 0
                currentRoute.startsWith("library") -> selectedTab = 1
            }
        }
    }

    // Provide dynamic mini-player padding to all destinations
    val showMiniPlayer = currentSong != null && currentRoute != Screen.Player.route
    val showNavBar = currentRoute == Screen.Home.route || currentRoute.startsWith("library")
    val miniPlayerPaddingValues = remember(showMiniPlayer, showNavBar) {
        PaddingValues(
            bottom = when {
                showMiniPlayer -> UiConstants.MiniPlayerHeight + if (showNavBar) UiConstants.NavBarHeight else 0.dp
                showNavBar -> UiConstants.NavBarHeight
                else -> 0.dp
            }
        )
    }

    CompositionLocalProvider(LocalMiniPlayerPadding provides miniPlayerPaddingValues) {
        Scaffold(
            bottomBar = {
                Column {
                    // Global MiniPlayer (hidden on full player screen)
                    if (currentSong != null && currentRoute != Screen.Player.route) {
                        MiniPlayer(
                            song = currentSong,
                            isPlaying = isPlaying,
                            progress = progress,
                            onPlayPause = onPlayPause,
                            onPlayerClick = onPlayerClick,
                            onSkipNext = onSkipNext
                        )
                    }

                    // Navigation bar shown only on specific routes
                    if (showNavBar) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(28.dp),
                                tonalElevation = 3.dp,
                                modifier = Modifier
                                    .height(64.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val items = listOf(
                                        Triple(
                                            Screen.Home.route, "Home",
                                            Pair(RhythmIcons.HomeFilled, RhythmIcons.Home)
                                        ),
                                        Triple(
                                            Screen.Library.createRoute(), "Library",
                                            Pair(RhythmIcons.Library, RhythmIcons.Library)
                                        )
                                    )

                                    items.forEachIndexed { index, (route, title, icons) ->
                                        val isSelected = index == selectedTab

                                        val (selectedIcon, unselectedIcon) = icons

                                        // Animation values
                                        val animatedScale by animateFloatAsState(
                                            targetValue = if (isSelected) 1.1f else 1.0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "scale"
                                        )

                                        val animatedAlpha by animateFloatAsState(
                                            targetValue = if (isSelected) 1f else 0.7f,
                                            animationSpec = tween(300),
                                            label = "alpha"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clickable {
                                                    selectedTab = index
                                                    navController.navigate(route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Background indicator
                                            if (isSelected) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.8f)
                                                        .height(48.dp)
                                                ) {}
                                            }

                                            // Horizontal layout for icon and text
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier
                                                    .graphicsLayer {
                                                        scaleX = animatedScale
                                                        scaleY = animatedScale
                                                        alpha = animatedAlpha
                                                    }
                                                    .padding(horizontal = 16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                                    contentDescription = title,
                                                    tint = if (isSelected)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (isSelected)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Add spacer that takes up the navigation bar height
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .padding(LocalMiniPlayerPadding.current)
            ) {
                composable(
                    route = Screen.Home.route,
                    enterTransition = {
                        when {
                            initialState.destination.route?.startsWith("library") == true -> {
                                // Horizontal slide animation when coming from Library
                                fadeIn(animationSpec = tween(300)) +
                                        slideInHorizontally(
                                            initialOffsetX = { -it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Default animation for other sources
                                fadeIn(animationSpec = tween(300))
                            }
                        }
                    },
                    exitTransition = {
                        when {
                            targetState.destination.route?.startsWith("library") == true -> {
                                // Horizontal slide animation when going to Library
                                fadeOut(animationSpec = tween(300)) +
                                        slideOutHorizontally(
                                            targetOffsetX = { -it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Default animation for other destinations
                                fadeOut(animationSpec = tween(300))
                            }
                        }
                    },
                    popEnterTransition = {
                        when {
                            initialState.destination.route?.startsWith("library") == true -> {
                                // Restore horizontal slide animation when popping back from Library
                                fadeIn(animationSpec = tween(300)) +
                                        slideInHorizontally(
                                            initialOffsetX = { -it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Simple faster fade animation when popping back from other screens
                                fadeIn(animationSpec = tween(200))
                            }
                        }
                    },
                    popExitTransition = {
                        // Simple faster fade animation when being popped from
                        fadeOut(animationSpec = tween(200))
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
                            navController.navigate(Screen.Library.createRoute(LibraryTab.SONGS))
                            selectedTab = 1
                        },
                        onViewAllAlbums = {
                            navController.navigate(Screen.Library.createRoute(LibraryTab.PLAYLISTS))
                            selectedTab = 1
                        },
                        onViewAllArtists = {
                            navController.navigate(Screen.Library.createRoute(LibraryTab.PLAYLISTS))
                            selectedTab = 1
                        },
                        onSkipNext = onSkipNext,
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onSettingsClick = {
                            // Navigate to the settings screen
                            navController.navigate(Screen.Settings.route)
                        },
                        onAppUpdateClick = { autoDownload ->
                            // Navigate to app updater with autoDownload parameter
                            navController.navigate(Screen.AppUpdater.createRoute(autoDownload))
                        },
                        onNavigateToLibrary = {
                            // Navigate to library with playlists tab selected
                            navController.navigate(Screen.Library.createRoute(LibraryTab.PLAYLISTS))
                            selectedTab = 0 // Assuming tab 0 is playlists
                        },
                        onNavigateToPlaylist = { playlistId ->
                            // Navigate to the specified playlist
                            // For "favorites", we'll use the ID "1" which is the favorites playlist
                            val id = if (playlistId == "favorites") "1" else playlistId
                            navController.navigate(Screen.PlaylistDetail.createRoute(id))
                        }
                    )
                }

                composable(
                    Screen.Search.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(350)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(400, easing = EaseOutQuint)
                                )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(350)) +
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300, easing = EaseInOutQuart)
                                )
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(350)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(400, easing = EaseOutQuint)
                                )
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(350)) +
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300, easing = EaseInOutQuart)
                                )
                    }
                ) {
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
                        onSkipNext = onSkipNext,
                        onAddSongToPlaylist = { song, playlistId ->
                            viewModel.addSongToPlaylist(song, playlistId)
                        },
                        onCreatePlaylist = { name ->
                            viewModel.createPlaylist(name)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    Screen.Settings.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(350)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(400, easing = EaseOutQuint)
                                )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(350)) +
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300, easing = EaseInOutQuart)
                                )
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(350)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(400, easing = EaseOutQuint)
                                )
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(350)) +
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300, easing = EaseInOutQuart)
                                )
                    }
                ) {
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
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                        onCheckForUpdates = {
                            // Navigate to the app updater screen
                            navController.navigate(Screen.AppUpdater.createRoute(true))
                        }
                    )
                }

                composable(
                    route = Screen.Library.route,
                    arguments = listOf(
                        navArgument("tab") {
                            type = NavType.StringType
                            defaultValue = "songs"
                        }
                    ),
                    enterTransition = {
                        when (initialState.destination.route) {
                            Screen.Home.route -> {
                                // Horizontal slide animation when coming from Home
                                fadeIn(animationSpec = tween(300)) +
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Default animation for other sources
                                fadeIn(animationSpec = tween(300))
                            }
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            Screen.Home.route -> {
                                // Horizontal slide animation when going to Home
                                fadeOut(animationSpec = tween(300)) +
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Default animation for other destinations
                                fadeOut(animationSpec = tween(300))
                            }
                        }
                    },
                    popEnterTransition = {
                        when (initialState.destination.route) {
                            Screen.Home.route -> {
                                // Restore horizontal slide animation when popping back from Home
                                fadeIn(animationSpec = tween(300)) +
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Simple faster fade animation when popping back from other screens
                                fadeIn(animationSpec = tween(200))
                            }
                        }
                    },
                    popExitTransition = {
                        when (targetState.destination.route) {
                            Screen.Home.route -> {
                                // Restore horizontal slide animation when popping back to Home
                                fadeOut(animationSpec = tween(300)) +
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(350, easing = EaseInOutQuart)
                                        )
                            }

                            else -> {
                                // Simple faster fade animation when being popped from for other destinations
                                fadeOut(animationSpec = tween(200))
                            }
                        }
                    }
                ) {
                    val tabArg = it.arguments?.getString("tab") ?: "songs"
                    val initialTab =
                        if (tabArg == "playlists") LibraryTab.PLAYLISTS else LibraryTab.SONGS

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
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        sortOrder = sortOrder,
                        onSkipNext = onSkipNext,
                        onAddToQueue = { song ->
                            // Add song to queue
                            viewModel.addSongToQueue(song)
                        }
                    )
                }

                composable(
                    route = Screen.Player.route,
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it / 3 },  // slide in from 1/3 of the way down for smoother effect
                            animationSpec = tween(
                                durationMillis = 350, // slightly longer for more floaty effect
                                easing = EaseOutQuint // smooth acceleration for floaty feel
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = EaseOutQuint
                            )
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = EaseInOutQuart // smoother exit animation
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 200
                            )
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
                                        LargeTopAppBar(
                                            title = {
                                                Text(
                                                    text = "Add Songs to ${targetPlaylist.name}",
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    maxLines = 1
                                                )
                                            },
                                            navigationIcon = {
                                                FilledIconButton(
                                                    onClick = {
                                                        viewModel.clearTargetPlaylistForAddingSongs()
                                                        navController.popBackStack()
                                                    },
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = RhythmIcons.Back,
                                                        contentDescription = "Back"
                                                    )
                                                }
                                            },
                                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                                containerColor = Color.Transparent,
                                                scrolledContainerColor = Color.Transparent
                                            )
                                        )
                                    }
                                ) { innerPadding ->
                                    LazyColumn(
                                        modifier = Modifier.padding(innerPadding),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        items(availableSongs) { song ->
                                            Surface(
                                                onClick = {
                                                    viewModel.addSongToPlaylist(
                                                        song,
                                                        targetPlaylistId
                                                    )
                                                    // Show a snackbar or some feedback
                                                },
                                                color = MaterialTheme.colorScheme.surfaceContainer,
                                                shape = RoundedCornerShape(12.dp),
                                                tonalElevation = 1.dp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Album art
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    ) {
                                                        AsyncImage(
                                                            model = song.artworkUri,
                                                            contentDescription = null,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }

                                                    // Song info
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(horizontal = 12.dp)
                                                    ) {
                                                        Text(
                                                            text = song.title,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Text(
                                                            text = "${song.artist} • ${song.album}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.7f
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    // Add button
                                                    FilledIconButton(
                                                        onClick = {
                                                            viewModel.addSongToPlaylist(
                                                                song,
                                                                targetPlaylistId
                                                            )
                                                            // Show a snackbar or some feedback
                                                        },
                                                        colors = IconButtonDefaults.filledIconButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = RhythmIcons.Add,
                                                            contentDescription = "Add to playlist"
                                                        )
                                                    }
                                                }
                                            }
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

                // Add App Updater screen
                composable(
                    Screen.AppUpdater.route,
                    arguments = listOf(
                        navArgument("autoDownload") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    ),
                    enterTransition = {
                        fadeIn(animationSpec = tween(350)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(400, easing = EaseOutQuint)
                                )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(350)) +
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300, easing = EaseInOutQuart)
                                )
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(350)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(400, easing = EaseOutQuint)
                                )
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(350)) +
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300, easing = EaseInOutQuart)
                                )
                    }
                ) {
                    val autoDownload = it.arguments?.getBoolean("autoDownload") ?: false

                    AppUpdaterScreen(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        progress = progress,
                        onPlayPause = onPlayPause,
                        onPlayerClick = {
                            navController.navigate(Screen.Player.route)
                        },
                        onSkipNext = onSkipNext,
                        onBack = {
                            navController.popBackStack()
                        },
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                        autoDownload = autoDownload
                    )
                }
            }
        }
    }
}
