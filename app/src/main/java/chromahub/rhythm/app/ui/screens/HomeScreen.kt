package chromahub.rhythm.app.ui.screens

import kotlinx.coroutines.CoroutineScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material.icons.rounded.PlayArrow // Or your specific icon
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import chromahub.rhythm.app.util.performIfEnabled
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.R
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.screens.ArtistBottomSheet
import chromahub.rhythm.app.ui.screens.AlbumBottomSheet
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.ArtistCollaborationUtils
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel
import chromahub.rhythm.app.viewmodel.AppVersion
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import java.util.Calendar
import kotlin.random.Random
import androidx.core.text.HtmlCompat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    recentlyPlayed: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onViewAllSongs: () -> Unit,
    onViewAllAlbums: () -> Unit,
    onViewAllArtists: () -> Unit,
    onSkipNext: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAppUpdateClick: (autoDownload: Boolean) -> Unit = { onSettingsClick() },
    onNavigateToLibrary: () -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onNavigateToPlaylist: (String) -> Unit = {},
    onCreatePlaylist: (String) -> Unit = { _ -> },
    updaterViewModel: AppUpdaterViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val musicViewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    
    // State for artist bottom sheet
    var showArtistSheet by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val artistSheetState = rememberModalBottomSheetState()

    // State for album bottom sheet
    var showAlbumBottomSheet by remember { mutableStateOf(false) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    val albumSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // State for AddToPlaylist bottom sheet
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var selectedSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    
    // Select featured content from all albums (enhanced selection)
    val featuredContent = remember(albums) {
        albums.shuffled().take(6) // Increased to 6 for better variety
    }
    
    // Get all unique artists with better collaboration handling
    val availableArtists = remember(songs, artists) {
        ArtistCollaborationUtils.extractIndividualArtists(artists, songs)
            .sortedBy { it.name }
    }
    
    val quickPicks = songs.take(8) // Increased for better variety
    val topArtists = availableArtists
    
    // Enhanced filtering for new releases
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentYearReleases = remember(albums, currentYear) {
        albums.filter { it.year == currentYear }
            .ifEmpty {
                albums.sortedByDescending { it.year }.take(6) // Increased count
            }
    }

    // Enhanced mood-based content
    val moodBasedSongs = songs.takeLast(15) // Increased for variety
    val energeticSongs = songs.take(12)
    val relaxingSongs = songs.drop(12).take(12)

    // Enhanced recently added songs
    val recentlyAddedSongs = remember(songs) {
        val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
        songs.filter { it.dateAdded >= oneMonthAgo }
            .sortedByDescending { it.dateAdded }
    }

    // Enhanced recently added albums (for unified styling with new releases)
    val recentlyAddedAlbums = remember(albums, songs) {
        val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
        val recentSongIds = songs.filter { it.dateAdded >= oneMonthAgo }.map { it.id }.toSet()
        albums.filter { album ->
            album.songs.any { song -> song.id in recentSongIds }
        }.sortedByDescending { album ->
            album.songs.mapNotNull { song ->
                if (song.id in recentSongIds) song.dateAdded else null
            }.maxOfOrNull { it } ?: 0L
        }
    }

    // Bottom sheet handlers (keeping same logic as original)
    if (showArtistSheet && selectedArtist != null) {
        ArtistBottomSheet(
            artist = selectedArtist!!,
            onDismiss = { showArtistSheet = false },
            onSongClick = { song: Song ->
                showArtistSheet = false
                onSongClick(song)
            },
            onAlbumClick = { album: Album ->
                showArtistSheet = false
                onAlbumClick(album)
            },
            onPlayAll = { songs ->
                if (songs.isNotEmpty()) {
                    onSongClick(songs.first())
                }
            },
            onShufflePlay = { songs ->
                if (songs.isNotEmpty()) {
                    onSongClick(songs.shuffled().first())
                }
            },
            onAddToQueue = { song ->
                onAddToQueue(song)
            },
            onAddSongToPlaylist = { song ->
                selectedSongForPlaylist = song
                scope.launch {
                    artistSheetState.hide()
                }.invokeOnCompletion {
                    if (!artistSheetState.isVisible) {
                        showArtistSheet = false
                        showAddToPlaylistSheet = true
                    }
                }
            },
            onPlayerClick = onPlayerClick,
            sheetState = artistSheetState,
            haptics = haptics
        )
    }

    if (showAlbumBottomSheet && selectedAlbum != null) {
        AlbumBottomSheet(
            album = selectedAlbum!!,
            onDismiss = { showAlbumBottomSheet = false },
            onSongClick = onSongClick,
            onPlayAll = { songsToPlay ->
                if (songsToPlay.isNotEmpty()) {
                    onSongClick(songsToPlay.first())
                }
                scope.launch {
                    albumSheetState.hide()
                }.invokeOnCompletion {
                    if (!albumSheetState.isVisible) {
                        showAlbumBottomSheet = false
                    }
                }
            },
            onShufflePlay = { songsToPlay ->
                if (songsToPlay.isNotEmpty()) {
                    onSongClick(songsToPlay.first())
                }
                scope.launch {
                    albumSheetState.hide()
                }.invokeOnCompletion {
                    if (!albumSheetState.isVisible) {
                        showAlbumBottomSheet = false
                    }
                }
            },
            onAddToQueue = onAddToQueue,
            onAddSongToPlaylist = { song ->
                selectedSongForPlaylist = song
                scope.launch {
                    albumSheetState.hide()
                }.invokeOnCompletion {
                    if (!albumSheetState.isVisible) {
                        showAlbumBottomSheet = false
                        showAddToPlaylistSheet = true
                    }
                }
            },
            onPlayerClick = onPlayerClick,
            haptics = LocalHapticFeedback.current,
            sheetState = albumSheetState
        )
    }

    if (showAddToPlaylistSheet && selectedSongForPlaylist != null) {
        val musicViewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
        val playlists by musicViewModel.playlists.collectAsState()

        chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet(
            song = selectedSongForPlaylist!!,
            playlists = playlists,
            onDismissRequest = { showAddToPlaylistSheet = false },
            onAddToPlaylist = { playlist ->
                onAddSongToPlaylist(selectedSongForPlaylist!!, playlist.id)
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

    if (showCreatePlaylistDialog) {
        chromahub.rhythm.app.ui.components.CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                musicViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Preserved topbar as requested
            LargeTopAppBar(
                title = {
                    val expandedTextStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    val collapsedTextStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)

                    val fraction = scrollBehavior.state.collapsedFraction
                    val currentFontSize = lerp(expandedTextStyle.fontSize.value, collapsedTextStyle.fontSize.value, fraction).sp
                    val currentFontWeight = if (fraction < 0.5f) FontWeight.Bold else FontWeight.Bold

                    Text(
                        "Rhythm",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = currentFontSize,
                            fontWeight = currentFontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            onSettingsClick()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent.copy(alpha = 0.0f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        bottomBar = {},
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ModernScrollableContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = if (currentSong != null) 0.dp else 0.dp),
            featuredContent = featuredContent,
            albums = albums,
            topArtists = topArtists,
            newReleases = currentYearReleases,
            recentlyAddedSongs = recentlyAddedSongs,
            recentlyAddedAlbums = recentlyAddedAlbums,
            recentlyPlayed = recentlyPlayed,
            moodBasedSongs = moodBasedSongs,
            energeticSongs = energeticSongs,
            relaxingSongs = relaxingSongs,
            songs = songs,
            onSongClick = onSongClick,
            onAlbumClick = { album: Album ->
                selectedAlbum = album
                showAlbumBottomSheet = true
            },
            onArtistClick = { artist: Artist ->
                selectedArtist = artist
                showArtistSheet = true
            },
            onViewAllSongs = onViewAllSongs,
            onViewAllAlbums = onViewAllAlbums,
            onViewAllArtists = onViewAllArtists,
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick,
            onAppUpdateClick = onAppUpdateClick,
            onNavigateToLibrary = onNavigateToLibrary,
            onNavigateToPlaylist = onNavigateToPlaylist,
            updaterViewModel = updaterViewModel,
            musicViewModel = musicViewModel,
            coroutineScope = scope
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernScrollableContent(
    modifier: Modifier = Modifier,
    featuredContent: List<Album>,
    albums: List<Album>,
    topArtists: List<Artist>,
    newReleases: List<Album>,
    recentlyAddedSongs: List<Song>,
    recentlyAddedAlbums: List<Album>,
    recentlyPlayed: List<Song>,
    moodBasedSongs: List<Song>,
    energeticSongs: List<Song>,
    relaxingSongs: List<Song>,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onViewAllSongs: () -> Unit,
    onViewAllAlbums: () -> Unit,
    onViewAllArtists: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onAppUpdateClick: (Boolean) -> Unit = { onSettingsClick() },
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToPlaylist: (String) -> Unit = {},
    updaterViewModel: AppUpdaterViewModel = viewModel(),
    musicViewModel: chromahub.rhythm.app.viewmodel.MusicViewModel,
    coroutineScope: CoroutineScope
) {
    val scrollState = rememberScrollState()
    val allSongs by musicViewModel.filteredSongs.collectAsState()
    
    // Enhanced artist computation
    val availableArtists = remember(allSongs, topArtists) {
        val collaborationSeparators = listOf(
            ", ", ",", " & ", " and ", "&", " feat. ", " featuring ", " ft. ", 
            " with ", " x ", " X ", " + ", " vs ", " VS ", " / ", ";", " · "
        )

        val collaborationRegex = collaborationSeparators
            .map { Regex.escape(it) }
            .joinToString("|")
            .toRegex(RegexOption.IGNORE_CASE)

        val filteredTopArtists = topArtists.filter { artist ->
            !artist.name.contains(collaborationRegex)
        }

        val extractedArtistNames = allSongs.asSequence()
            .flatMap { song -> 
                var artistString = song.artist
                collaborationSeparators.forEach { separator ->
                    artistString = artistString.replace(separator, "||")
                }
                
                artistString.split("||")
                    .map { it.trim() }
                    .map { name ->
                        if (name.contains("(") && (name.contains("feat") || name.contains("ft") || name.contains("featuring"))) {
                            name.substringBefore("(").trim()
                        } else {
                            name
                        }
                    }
            }
            .filter { it.length > 1 }
            .distinct()
        
        extractedArtistNames
            .mapNotNull { artistName ->
                filteredTopArtists.find { it.name.equals(artistName, ignoreCase = true) }
                    ?: filteredTopArtists.find { 
                        it.name.equals(artistName, ignoreCase = true) || 
                        (artistName.length > 3 && it.name.contains(artistName, ignoreCase = true))
                    }
            }
            .distinct()
            .sortedBy { it.name }
            .toList()
    }
    
    // Featured albums with auto-refresh
    var currentFeaturedAlbums by remember(featuredContent) { 
        mutableStateOf(featuredContent) 
    }
    val featuredPagerState = rememberPagerState(pageCount = { currentFeaturedAlbums.size })
    
    LaunchedEffect(albums) {
        while (true) {
            delay(35000) // 35 seconds for smoother transitions
            if (albums.size > 6) {
                currentFeaturedAlbums = albums.shuffled().take(6)
            }
        }
    }
    
    // Enhanced mood content
    val enhancedMoodContent = remember(allSongs) {
        val focusSongs = allSongs
            .filter { it.duration > 3 * 60 * 1000 }
            .take(15) // Increased count
            .ifEmpty { moodBasedSongs }
        
        val energeticKeywords = listOf("rock", "dance", "pop", "party", "beat", "energy", "fast", "upbeat", "electronic")
        val betterEnergeticSongs = allSongs
            .filter { song ->
                energeticKeywords.any { keyword ->
                    song.title.contains(keyword, ignoreCase = true) || 
                    song.artist.contains(keyword, ignoreCase = true)
                }
            }
            .take(12)
            .ifEmpty { energeticSongs }
        
        val relaxingKeywords = listOf("chill", "relax", "ambient", "piano", "sleep", "calm", "soft", "peaceful", "acoustic")
        val betterRelaxingSongs = allSongs
            .filter { song ->
                relaxingKeywords.any { keyword ->
                    song.title.contains(keyword, ignoreCase = true) || 
                    song.artist.contains(keyword, ignoreCase = true)
                }
            }
            .take(12)
            .ifEmpty { relaxingSongs }
        
        Triple(focusSongs, betterEnergeticSongs, betterRelaxingSongs)
    }
    
    // Time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    
    // Update information
    val updateAvailable by updaterViewModel.updateAvailable.collectAsState()
    val latestVersion by updaterViewModel.latestVersion.collectAsState()
    val error by updaterViewModel.error.collectAsState()
    val updatesEnabled by updaterViewModel.appSettings.updatesEnabled.collectAsState(initial = true)
    
    // Auto-scroll featured pager with improved timing
    LaunchedEffect(featuredPagerState.pageCount) {
        if (featuredPagerState.pageCount > 1) {
            while (true) {
                delay(5000) // 5 seconds per page
                val nextPage = (featuredPagerState.currentPage + 1) % featuredPagerState.pageCount
                featuredPagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }
    
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            contentPadding = PaddingValues(bottom = 24.dp) // No top padding to connect with topbar
        ) {
            // Update section (preserved as requested)
            item {
                AnimatedVisibility(
                    visible = updateAvailable && latestVersion != null && error == null && updatesEnabled,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    latestVersion?.let { version ->
                        ModernUpdateSection(
                            latestVersion = version,
                            onUpdateClick = onAppUpdateClick
                        )
                    }
                }
            }

            // Welcome/Greeting section (preserved as requested)
            item {
                if (!updateAvailable || latestVersion == null || error != null || !updatesEnabled) {
                    ModernWelcomeSection(greeting = greeting, onSearchClick = onSearchClick)
                }
            }

            // Spacing between greeting and recently played
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Recently Played with modern design
            item {
                AnimatedVisibility(
                    visible = recentlyPlayed.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ModernRecentlyPlayedSection(
                        recentlyPlayed = recentlyPlayed.take(6), // Increased count
                        onSongClick = onSongClick,
                        musicViewModel = musicViewModel,
                        coroutineScope = coroutineScope
                    )
                }
            }

            // Spacing between recently played and featured albums
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Featured Albums with enhanced design
            item {
                AnimatedVisibility(
                    visible = currentFeaturedAlbums.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column {
                        ModernSectionTitle(
                            title = "Featured Albums", 
                            subtitle = "Discover amazing music",
                            viewAllAction = onViewAllAlbums
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ModernFeaturedSection(
                            albums = currentFeaturedAlbums,
                            pagerState = featuredPagerState,
                            onAlbumClick = onAlbumClick
                        )
                    }
                }
            }

            // Spacing between featured and artists
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Artists carousel with modern layout
            item {
                AnimatedVisibility(
                    visible = availableArtists.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ModernArtistsSection(
                        artists = availableArtists,
                        songs = allSongs,
                        onArtistClick = onArtistClick,
                        onViewAllArtists = onViewAllArtists
                    )
                }
            }

            // Spacing between artists and new releases
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // New Releases with enhanced cards
            item {
                AnimatedVisibility(
                    visible = newReleases.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column {
                        ModernSectionTitle(
                            title = "New Releases",
                            subtitle = "Fresh music just for you",
                            onPlayAll = {
                                coroutineScope.launch {
                                    val allNewReleaseSongs = newReleases.flatMap { album ->
                                        musicViewModel.getMusicRepository().getSongsForAlbum(album.id)
                                    }
                                    if (allNewReleaseSongs.isNotEmpty()) {
                                        musicViewModel.playQueue(allNewReleaseSongs)
                                    }
                                }
                            },
                            onShufflePlay = {
                                coroutineScope.launch {
                                    val allNewReleaseSongs = newReleases.flatMap { album ->
                                        musicViewModel.getMusicRepository().getSongsForAlbum(album.id)
                                    }
                                    if (allNewReleaseSongs.isNotEmpty()) {
                                        musicViewModel.playShuffled(allNewReleaseSongs)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(newReleases, key = { it.id }) { album ->
                                ModernAlbumCard(
                                    album = album,
                                    onClick = { onAlbumClick(album) }
                                )
                            }
                        }
                    }
                }
            }

            // Spacing between new releases and recently added
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Recently Added Albums (matching new releases style)
            item {
                AnimatedVisibility(
                    visible = recentlyAddedAlbums.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column {
                        ModernSectionTitle(
                            title = "Recently Added",
                            subtitle = "Your latest additions",
                            onPlayAll = {
                                if (recentlyAddedAlbums.isNotEmpty()) {
                                    val allSongs = recentlyAddedAlbums.flatMap { album ->
                                        album.songs
                                    }
                                    musicViewModel.playQueue(allSongs)
                                }
                            },
                            onShufflePlay = {
                                if (recentlyAddedAlbums.isNotEmpty()) {
                                    val allSongs = recentlyAddedAlbums.flatMap { album ->
                                        album.songs
                                    }
                                    musicViewModel.playShuffled(allSongs)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(recentlyAddedAlbums, key = { it.id }) { album ->
                                ModernAlbumCard(
                                    album = album,
                                    onClick = { onAlbumClick(album) }
                                )
                            }
                        }
                    }
                }
            }

            // Spacing before stats section
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Listening Stats with modern design
            item {
                ModernListeningStatsSection()
            }

            // Spacing before mood section
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Mood & Moments - Enhanced final section
            item {
                ModernMoodSection(
                    moodBasedSongs = enhancedMoodContent.first,
                    energeticSongs = enhancedMoodContent.second,
                    relaxingSongs = enhancedMoodContent.third,
                    onSongClick = onSongClick
                )
            }

            // Add some bottom padding for mini player
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// Modern Component Functions - Part 2

@Composable
private fun ModernWelcomeSection(
    greeting: String,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    // Enhanced time-based quotes
    val timeBasedQuote = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..11 -> listOf(
                "Start your day with the perfect soundtrack ✨",
                "Let music energize your morning routine 🎵",
                "Every morning is a new symphony waiting to begin 🌅",
                "Coffee and music - the perfect morning blend ☕"
            )
            hour in 12..17 -> listOf(
                "Power through with your favorite beats 🚀",
                "Music makes everything better 💪",
                "Let the rhythm guide your day 🎯",
                "Turn up the focus with great tunes 🔥"
            )
            else -> listOf(
                "Time to unwind with soothing melodies 🌅",
                "Let the evening soundtrack begin 🎶",
                "Perfect time for your chill playlist 😌",
                "Night time is music time 🌙"
            )
        }.random()
    }
    
    val personalizedMessage = remember(recentlyPlayed) {
        if (recentlyPlayed.isNotEmpty()) {
            val recentSong = recentlyPlayed.firstOrNull()?.title
            if (!recentSong.isNullOrBlank()) {
                "Continue where you left off with \"$recentSong\""
            } else {
                "Your musical adventure continues"
            }
        } else {
            "Ready to discover your next favorite song?"
        }
    }
    
    val timeBasedTheme = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..11 -> Triple("☀️", "morning", "🌻")
            hour in 12..17 -> Triple("🌤️", "afternoon", "⚡")
            else -> Triple("🌙", "evening", "✨")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                onSearchClick() 
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Decorative elements in background
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp), // Reduced padding
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
            ) {
                repeat(2) { // Reduced from 3 to 2 decorative elements
                    Text(
                        text = timeBasedTheme.third,
                        style = MaterialTheme.typography.titleLarge, // Reduced from headlineSmall
                        modifier = Modifier.alpha(0.12f) // Slightly more transparent
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp) // Reduced padding for more compact design
            ) {
                // Main greeting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp) // Reduced spacing
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "emoji_pulse")
                    val emojiScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "emoji_scale"
                    )
                    
                    Text(
                        text = timeBasedTheme.first,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .graphicsLayer {
                                scaleX = emojiScale
                                scaleY = emojiScale
                            }
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = personalizedMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp) // Reduced spacing
                        )
                    }

                    // Modern search button with expressive design
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            onSearchClick()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(46.dp) // Larger, more prominent
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Quote section with Material 3 Expressive design
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp), // More rounded
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), // Reduced padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp) // Reduced size
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "💭",
                                    style = MaterialTheme.typography.bodyLarge // Reduced size
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp)) // Reduced spacing
                        
                        Text(
                            text = timeBasedQuote,
                            style = MaterialTheme.typography.bodyMedium, // Larger text for readability
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            lineHeight = 22.sp, // Better line height
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernRecentlyPlayedSection(
    recentlyPlayed: List<Song>,
    onSongClick: (Song) -> Unit,
    musicViewModel: chromahub.rhythm.app.viewmodel.MusicViewModel,
    coroutineScope: CoroutineScope
) {
    Column {
        ModernSectionTitle(
            title = "Recently Played",
            subtitle = "Continue your musical journey",
            onPlayAll = {
                coroutineScope.launch {
                    if (recentlyPlayed.isNotEmpty()) {
                        musicViewModel.playQueue(recentlyPlayed)
                    }
                }
            },
            onShufflePlay = {
                coroutineScope.launch {
                    if (recentlyPlayed.isNotEmpty()) {
                        musicViewModel.playShuffled(recentlyPlayed)
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // More spacing between items
        ) {
            items(recentlyPlayed, key = { it.id }) { song ->
                ModernRecentSongCard(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
private fun ModernRecentSongCard(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier
            .width(180.dp) // Slightly wider for better proportion
            .height(80.dp), // Slightly taller
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // More padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced artwork with more rounded corners
            Surface(
                shape = RoundedCornerShape(16.dp), // More rounded
                modifier = Modifier.size(52.dp), // Slightly larger
                color = MaterialTheme.colorScheme.surfaceVariant
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
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Play indicator
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ModernSectionTitle(
    title: String,
    subtitle: String? = null,
    viewAllAction: (() -> Unit)? = null,
    onPlayAll: (() -> Unit)? = null,
    onShufflePlay: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onPlayAll?.let {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        it()
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            onShufflePlay?.let {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        it()
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Shuffle",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            viewAllAction?.let {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        it()
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernFeaturedSection(
    albums: List<Album>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onAlbumClick: (Album) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Enhanced pager with better design
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp) // Taller for more dramatic presence
                .padding(horizontal = 8.dp), // More padding
            pageSpacing = 24.dp // More spacing between pages
        ) { page ->
            val album = albums[page]
            ModernFeaturedCard(
                album = album,
                onClick = { onAlbumClick(album) },
                pageOffset = (page - pagerState.currentPage).toFloat().absoluteValue
            )
        }
        
        // Modern page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val isSelected = index == pagerState.currentPage
                
                val width by animateFloatAsState(
                    targetValue = if (isSelected) 24f else 8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator_width"
                )
                
                val color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(width.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)) // More rounded indicators
                        .background(color)
                        .clickable { 
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = index,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun ModernFeaturedCard(
    album: Album,
    onClick: (Album) -> Unit,
    pageOffset: Float = 0f
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptic = LocalHapticFeedback.current
    
    // Enhanced 3D animations
    val scale = lerp(
        start = 0.88f,
        stop = 1f,
        fraction = 1f - (pageOffset * 0.3f).coerceIn(0f, 0.3f)
    )
    
    val alpha = lerp(
        start = 0.6f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )
    
    val rotation = lerp(
        start = 0f,
        stop = -8f,
        fraction = pageOffset.coerceIn(0f, 1f)
    )
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
            onClick(album)
        },
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                rotationY = rotation
                cameraDistance = 20f * density
            },
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Album artwork background
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .apply(ImageUtils.buildImageRequest(
                        album.artworkUri,
                        album.title,
                        context.cacheDir,
                        M3PlaceholderType.ALBUM
                    ))
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Enhanced gradient overlays for better text readability
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
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (album.year > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = album.year.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Enhanced play button
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            viewModel.playAlbum(album)
                            onClick(album)
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play album",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Featured badge with more expressive design
            Surface(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.95f),
                shape = RoundedCornerShape(topEnd = 40.dp, bottomStart = 28.dp), // More rounded
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelLarge, // Slightly larger
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp) // More padding
                )
            }
        }
    }
}

// Additional Modern Components

@Composable
private fun ModernArtistsSection(
    artists: List<Artist>,
    songs: List<Song>,
    onArtistClick: (Artist) -> Unit,
    onViewAllArtists: () -> Unit
) {
    Column {
        ModernSectionTitle(
            title = "Top Artists",
            subtitle = "Your favorite musicians",
            viewAllAction = onViewAllArtists
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artists, key = { it.id }) { artist ->
                ModernArtistCard(
                    artist = artist,
                    songs = songs,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Composable
private fun ModernArtistCard(
    artist: Artist,
    songs: List<Song>,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { 
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                onClick() 
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(120.dp)) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                if (artist.artworkUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .apply(
                                ImageUtils.buildImageRequest(
                                    artist.artworkUri,
                                    artist.name,
                                    context.cacheDir,
                                    M3PlaceholderType.ARTIST
                                )
                            )
                            .build(),
                        contentDescription = "Artist ${artist.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Artist,
                            contentDescription = "Artist ${artist.name}",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
            
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                    val artistSongs = chromahub.rhythm.app.util.ArtistCollaborationUtils.filterSongsByArtist(songs, artist.name)
                    if (artistSongs.isNotEmpty()) {
                        viewModel.playQueue(artistSongs)
                    }
                    onClick()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play ${artist.name}",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ModernAlbumCard(
    album: Album,
    onClick: (Album) -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptic = LocalHapticFeedback.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
            onClick(album)
        },
        modifier = Modifier
            .width(160.dp)
            .height(240.dp), // Fixed height to prevent layout issues
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp)) // More rounded artwork
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            album.artworkUri,
                            album.title,
                            context.cacheDir,
                            M3PlaceholderType.ALBUM
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            viewModel.playAlbum(album)
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play album",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleSmall, // Smaller text to prevent overflow
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp // Better line height
                )
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ModernSongCard(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier
            .width(180.dp)
            .height(260.dp), // Fixed height to prevent layout issues
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Take most of the space
                color = MaterialTheme.colorScheme.surfaceVariant
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
            
            // Text section with fixed height
            Column(
                modifier = Modifier.height(60.dp), // Fixed height for text
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall, // Smaller text
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp // Compact line height
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall, // Smaller text
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ModernListeningStatsSection() {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()

    val listeningTimeHours = remember(recentlyPlayed) {
        val totalMillis = recentlyPlayed.sumOf { it.duration }
        val hours = totalMillis / (1000 * 60 * 60)
        if (hours < 1) "< 1h" else "${hours}h"
    }

    val songsPlayed = remember(recentlyPlayed) {
        recentlyPlayed.size.toString()
    }

    val uniqueArtists = remember(recentlyPlayed) {
        recentlyPlayed.map { it.artist }.distinct().size.toString()
    }

    // Enhanced stats card with better design
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your Listening Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "This week's musical journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats grid with better layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedStatItem(
                    modifier = Modifier.weight(1f),
                    value = listeningTimeHours,
                    label = "Listening Time",
                    icon = RhythmIcons.Player.Timer,
                    accentColor = MaterialTheme.colorScheme.primary
                )
                
                EnhancedStatItem(
                    modifier = Modifier.weight(1f),
                    value = songsPlayed,
                    label = "Songs Played",
                    icon = RhythmIcons.Music.MusicNote,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
                
                EnhancedStatItem(
                    modifier = Modifier.weight(1f),
                    value = uniqueArtists,
                    label = "Artists",
                    icon = RhythmIcons.Artist,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatItem(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    accentColor: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon with accent color background
        Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.12f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Value with emphasis
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ModernStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernMoodSection(
    moodBasedSongs: List<Song>,
    energeticSongs: List<Song>,
    relaxingSongs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptic = LocalHapticFeedback.current
    
    Column {
        ModernSectionTitle(
            title = "Mood & Moments",
            subtitle = "Music for every feeling"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ModernMoodCard(
                    title = "Energize",
                    description = "Boost your energy",
                    songs = energeticSongs,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = RhythmIcons.Energy,
                    songCount = energeticSongs.size,
                    onPlayClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        if (energeticSongs.isNotEmpty()) {
                            viewModel.playSongWithQueueOption(
                                song = energeticSongs.first(), 
                                replaceQueue = true, 
                                shuffleQueue = true
                            )
                            onSongClick(energeticSongs.first())
                        }
                    }
                )
            }
            
            item {
                ModernMoodCard(
                    title = "Relax",
                    description = "Unwind and breathe",
                    songs = relaxingSongs,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = RhythmIcons.Relax,
                    songCount = relaxingSongs.size,
                    onPlayClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        if (relaxingSongs.isNotEmpty()) {
                            viewModel.playSongWithQueueOption(
                                song = relaxingSongs.first(), 
                                replaceQueue = true
                            )
                            onSongClick(relaxingSongs.first())
                        }
                    }
                )
            }
            
            item {
                ModernMoodCard(
                    title = "Focus",
                    description = "Concentration mode",
                    songs = moodBasedSongs,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    icon = RhythmIcons.Focus,
                    songCount = moodBasedSongs.size,
                    onPlayClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        if (moodBasedSongs.isNotEmpty()) {
                            viewModel.playSongWithQueueOption(
                                song = moodBasedSongs.first(), 
                                replaceQueue = true
                            )
                            onSongClick(moodBasedSongs.first())
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernMoodCard(
    title: String,
    description: String,
    songs: List<Song>,
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector,
    songCount: Int,
    onPlayClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(220.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
                
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        onPlayClick()
                    },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = contentColor,
                        contentColor = backgroundColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play $title",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Surface(
                color = contentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(bottomStart = 20.dp, topEnd = 36.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "$songCount songs",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernUpdateSection(
    latestVersion: AppVersion,
    onUpdateClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                if (!isDownloading) {
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                    onUpdateClick(false)
                }
            }),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rhythm_splash_logo),
                    contentDescription = "Rhythm Logo",
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Rhythm",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = RhythmIcons.Download,
                        contentDescription = "Update available",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(10.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Text(
                    text = "Update Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Version ${latestVersion.versionName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (latestVersion.whatsNew.isNotEmpty()) {
                Text(
                    text = "What's New:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                latestVersion.whatsNew.take(2).forEach { item ->
                    Text(
                        text = HtmlCompat.fromHtml(item, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            ElevatedCard(
                onClick = {
                    if (!isDownloading) {
                        isDownloading = true
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        onUpdateClick(false)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDownloading) {
                        chromahub.rhythm.app.ui.components.M3FourColorCircularLoader(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp.value
                        )
                    } else {
                        Icon(
                            imageVector = RhythmIcons.Download,
                            contentDescription = "Download update",
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = if (isDownloading) "Downloading..." else "Update Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    }
}
