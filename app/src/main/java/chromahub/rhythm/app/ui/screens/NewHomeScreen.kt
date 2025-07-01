package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
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
import chromahub.rhythm.app.R
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.ImageUtils
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewHomeScreen(
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
    onNavigateToPlaylist: (String) -> Unit = {},
    updaterViewModel: AppUpdaterViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    
    // State for artist bottom sheet
    var showArtistSheet by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val artistSheetState = rememberModalBottomSheetState()
    
    // Select featured content randomly from all albums
    val featuredContent = remember(albums) {
        if (albums.size <= 5) {
            albums
        } else {
            albums.shuffled().take(5)
        }
    }
    
    // Get all unique artists from songs
    val availableArtists = remember(songs, artists) {
        songs.map { it.artist }
            .distinct()
            .mapNotNull { artistName ->
                artists.find { it.name == artistName }
            }
            .sortedBy { it.name } // Sort alphabetically for better navigation
    }
    
    // Quick picks can still be first few songs but used for other sections
    val quickPicks = songs.take(6)
    val topArtists = artists
    
    // Filter new releases to only show albums from the current year
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentYearReleases = remember(albums, currentYear) {
        albums.filter { it.year == currentYear }
            .ifEmpty { 
                // Fallback to most recent albums if no current year albums are available
                albums.sortedByDescending { it.year }.take(4) 
            }
            .take(4)
    }
    
    // Generate mood-based playlists - in a real app you'd have actual logic for this
    val moodBasedSongs = songs.takeLast(12).shuffled()
    val energeticSongs = songs.take(10).shuffled()
    val relaxingSongs = songs.drop(10).take(10).shuffled()
    
    if (showArtistSheet && selectedArtist != null) {
        ArtistBottomSheet(
            artist = selectedArtist!!,
            songs = songs,
            albums = albums,
            onDismiss = { showArtistSheet = false },
            onSongClick = { song ->
                showArtistSheet = false
                onSongClick(song)
            },
            onAlbumClick = { album ->
                showArtistSheet = false
                onAlbumClick(album)
            },
            sheetState = artistSheetState
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
                    // Settings icon
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = RhythmIcons.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    
                    // Search button
                    FilledIconButton(
                        onClick = onSearchClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent.copy(alpha = 0.0f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp) // Added padding
            )
        },
        bottomBar = {},
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        EnhancedScrollableContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = if (currentSong != null) 0.dp else 0.dp),
            featuredContent = featuredContent,
            albums = albums,
            quickPicks = quickPicks,
            topArtists = topArtists,
            newReleases = currentYearReleases,
            recentlyPlayed = recentlyPlayed,
            moodBasedSongs = moodBasedSongs,
            energeticSongs = energeticSongs,
            relaxingSongs = relaxingSongs,
            onSongClick = onSongClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = { artist ->
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
            updaterViewModel = updaterViewModel
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EnhancedScrollableContent(
    modifier: Modifier = Modifier,
    featuredContent: List<Album>,
    albums: List<Album>,
    quickPicks: List<Song>,
    topArtists: List<Artist>,
    newReleases: List<Album>,
    recentlyPlayed: List<Song>,
    moodBasedSongs: List<Song>,
    energeticSongs: List<Song>,
    relaxingSongs: List<Song>,
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
    updaterViewModel: AppUpdaterViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val allSongs by viewModel.songs.collectAsState()
    
    // Get all unique artists from songs
    val availableArtists = remember(allSongs, topArtists) {
        allSongs.map { it.artist }
            .distinct()
            .mapNotNull { artistName ->
                topArtists.find { it.name == artistName }
            }
            .sortedBy { it.name } // Sort alphabetically for better navigation
    }
    
    // Get current year for New Releases section
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    
    // Create a dynamic featured content that changes periodically
    var currentFeaturedAlbums by remember(featuredContent) { 
        mutableStateOf(
            if (featuredContent.isEmpty()) emptyList()
            else featuredContent
        ) 
    }
    val featuredPagerState = rememberPagerState { currentFeaturedAlbums.size }
    
    // Function to refresh featured content with new random albums
    val refreshFeaturedContent = {
        if (albums.size > 5) {
            currentFeaturedAlbums = albums.shuffled().take(5)
        }
    }
    
    // Ensure featured content is populated on initial launch
    LaunchedEffect(albums) {
        if (currentFeaturedAlbums.isEmpty() && albums.isNotEmpty()) {
            if (albums.size <= 5) {
                currentFeaturedAlbums = albums
            } else {
                currentFeaturedAlbums = albums.shuffled().take(5)
            }
        }
    }
    
    // Generate better mood-based playlists using song characteristics
    val betterMoodBasedSongs = remember(allSongs) {
        // In a real app, this would use audio features like tempo, energy, etc.
        // For this example, we'll use simple heuristics
        
        // Focus playlist: songs with longer duration (assuming they're less distracting)
        val focusSongs = allSongs
            .filter { it.duration > 3 * 60 * 1000 } // Songs longer than 3 minutes
            .shuffled()
            .take(12)
            .ifEmpty { moodBasedSongs }
        
        // Energetic playlist: songs with "rock", "dance", "pop" in the title/artist
        val energeticKeywords = listOf("rock", "dance", "pop", "party", "beat", "energy")
        val betterEnergeticSongs = allSongs
            .filter { song ->
                energeticKeywords.any { keyword ->
                    song.title.contains(keyword, ignoreCase = true) || 
                    song.artist.contains(keyword, ignoreCase = true)
                }
            }
            .shuffled()
            .take(10)
            .ifEmpty { energeticSongs }
        
        // Relaxing playlist: songs with "chill", "relax", "ambient", "piano" in the title/artist
        val relaxingKeywords = listOf("chill", "relax", "ambient", "piano", "sleep", "calm")
        val betterRelaxingSongs = allSongs
            .filter { song ->
                relaxingKeywords.any { keyword ->
                    song.title.contains(keyword, ignoreCase = true) || 
                    song.artist.contains(keyword, ignoreCase = true)
                }
            }
            .shuffled()
            .take(10)
            .ifEmpty { relaxingSongs }
        
        Triple(focusSongs, betterEnergeticSongs, betterRelaxingSongs)
    }
    
    // Get current time to display appropriate greeting
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    
    // Collect update information
    val isCheckingForUpdates by updaterViewModel.isCheckingForUpdates.collectAsState()
    val updateAvailable by updaterViewModel.updateAvailable.collectAsState()
    val latestVersion by updaterViewModel.latestVersion.collectAsState()
    val error by updaterViewModel.error.collectAsState()
    
    // Check for updates when screen is shown
    LaunchedEffect(Unit) {
        updaterViewModel.checkForUpdates()
    }
    
    // Auto-scroll featured content and refresh content periodically
    LaunchedEffect(Unit) {
        while(true) {
            delay(5000)
            if (featuredPagerState.pageCount > 0) {
                val nextPage = (featuredPagerState.currentPage + 1) % featuredPagerState.pageCount
                featuredPagerState.animateScrollToPage(nextPage)
                
                // Every 3 cycles (15 seconds), refresh the featured content
                if (nextPage == 0) {
                    refreshFeaturedContent()
                }
            }
        }
    }
    
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Show update card or welcome section based on update availability
            if (updateAvailable && latestVersion != null && !isCheckingForUpdates && error == null) {
                latestVersion?.let { version ->
                    UpdateAvailableSection(
                        latestVersion = version,
                        onUpdateClick = { autoDownload -> onAppUpdateClick(autoDownload) }
                    )
                }
            } else {
                // Personalized Welcome Section
                WelcomeSection(greeting = greeting, onSearchClick = onSearchClick)
            }
            
            // Fetch recommendations for "Recommended For You" section
            val recommendedSongs = remember(viewModel) {
                viewModel.getRecommendedSongs().take(4)
            }

            // --- Randomized Sections Start ---
            val randomizedSections = remember(
                recentlyPlayed,
                availableArtists,
                newReleases,
                betterMoodBasedSongs
            ) {
                buildList<@Composable () -> Unit> {
                    // Quick Actions
                    add {
                        QuickActionsSection(
                            onNavigateToLibrary = onNavigateToLibrary,
                            onNavigateToPlaylist = onNavigateToPlaylist
                        )
                    }

                    // Featured Albums
                    if (currentFeaturedAlbums.isNotEmpty()) add {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Featured Albums",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Surface(
                                    onClick = onViewAllAlbums,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = "View All",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            FeaturedContentSection(
                                albums = currentFeaturedAlbums,
                                pagerState = featuredPagerState,
                                onAlbumClick = onAlbumClick
                            )
                        }
                    }

                    // Listening Stats
                    add { ListeningStatsSection() }

                    // Recently Played
                    if (recentlyPlayed.isNotEmpty()) add {
                        RecentlyPlayedSection(
                            recentlyPlayed = recentlyPlayed.take(5),
                            onSongClick = onSongClick
                        )
                    }

                    // Artists
                    if (availableArtists.isNotEmpty()) add {
                        SectionTitle(title = "Artists", viewAllAction = null)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = availableArtists,
                                key = { it.name }
                            ) { artist ->
                                NewArtistCard(
                                    artist = artist,
                                    onClick = { onArtistClick(artist) }
                                )
                            }
                        }
                    }

                    // Recent Albums
                    if (newReleases.isNotEmpty()) add {
                        SectionTitle(title = "Recent Albums", viewAllAction = onViewAllAlbums)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(newReleases) { album ->
                                NewAlbumCard(
                                    album = album,
                                    onClick = { onAlbumClick(album) }
                                )
                            }
                        }
                    }

                    // Recommended For You
                    if (recommendedSongs.isNotEmpty()) add {
                        RecommendedForYouSection(
                            songs = recommendedSongs,
                            onSongClick = onSongClick
                        )
                    }

                    // Mood & Moments
                    add {
                        MoodBasedPlaylistsSection(
                            moodBasedSongs = betterMoodBasedSongs.first,
                            energeticSongs = betterMoodBasedSongs.second,
                            relaxingSongs = betterMoodBasedSongs.third,
                            onSongClick = onSongClick
                        )
                    }
                }.shuffled(Random(System.currentTimeMillis()))
            }

            randomizedSections.forEach { it() }
            // --- Randomized Sections End ---
            

            
            // Bottom spacer
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeSection(
    greeting: String,
    onSearchClick: () -> Unit
) {
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    
    // Personalize the welcome message based on recent listening
    val personalizedMessage = remember(recentlyPlayed) {
        if (recentlyPlayed.isNotEmpty()) {
            // Get most recent artist
            val recentArtist = recentlyPlayed.firstOrNull()?.artist
            
            if (!recentArtist.isNullOrBlank() && recentArtist != "Unknown") {
                "How about more music from $recentArtist?"
            } else {
                "What would you like to listen to today?"
            }
        } else {
            "What would you like to listen to today?"
        }
    }
    
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = personalizedMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            
            // Quick search button
            Surface(
                onClick = onSearchClick,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Search for songs, artists..",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToLibrary: () -> Unit,
    onNavigateToPlaylist: (String) -> Unit
) {
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Group button styling with FilledTonalButtons in a Row
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Equalizer button - opens system equalizer
                QuickActionButton(
                    icon = RhythmIcons.Player.Equalizer,
                    text = "Equalizer",
                    onClick = { viewModel.openSystemEqualizer() }
                )
                
                // Playlists button - would navigate to playlists screen in library
                QuickActionButton(
                    icon = RhythmIcons.Playlist,
                    text = "Playlists",
                    onClick = { onNavigateToLibrary() }
                )
                
                // Favorites button - would navigate to favorites playlist
                QuickActionButton(
                    icon = RhythmIcons.FavoriteFilled,
                    text = "Favorites",
                    onClick = { onNavigateToPlaylist("favorites") }
                )
                
                // Shuffle button - plays all songs in shuffle mode
                QuickActionButton(
                    icon = RhythmIcons.Shuffle,
                    text = "Shuffle",
                    onClick = { viewModel.playShuffledSongs() }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(48.dp),
            onClick = onClick
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ListeningStatsSection() {
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val songs by viewModel.songs.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    
    // Calculate total listening time in hours (for a real app, this would come from actual playback history)
    // Here we're estimating based on recently played songs
    val listeningTimeHours = remember(recentlyPlayed) {
        val totalMillis = recentlyPlayed.sumOf { it.duration }
        val hours = totalMillis / (1000 * 60 * 60)
        if (hours < 1) "< 1h" else "${hours}h"
    }
    
    // Calculate number of songs played (using recently played as an approximation)
    val songsPlayed = remember(recentlyPlayed) {
        recentlyPlayed.size.toString()
    }
    
    // Get unique artists count
    val uniqueArtists = remember(recentlyPlayed) {
        recentlyPlayed.map { it.artist }.distinct().size.toString()
    }
    
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                value = listeningTimeHours,
                label = "This Week",
                icon = RhythmIcons.Player.Timer
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )
            
            StatCard(
                value = songsPlayed,
                label = "Songs Played",
                icon = RhythmIcons.Music.MusicNote
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )
            
            StatCard(
                value = uniqueArtists,
                label = "Artists",
                icon = RhythmIcons.Artist
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MoodBasedPlaylistsSection(
    moodBasedSongs: List<Song>,
    energeticSongs: List<Song>,
    relaxingSongs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "Mood & Moments",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MoodPlaylistCard(
                    title = "Energize",
                    description = "Upbeat tracks to boost your energy",
                    songs = energeticSongs,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = RhythmIcons.Energy,
                    onPlayClick = {
                        viewModel.playQueue(energeticSongs)
                        if (energeticSongs.isNotEmpty()) onSongClick(energeticSongs.first())
                    }
                )
            }
            
            item {
                MoodPlaylistCard(
                    title = "Relax",
                    description = "Calm tracks to help you unwind",
                    songs = relaxingSongs,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = RhythmIcons.Relax,
                    onPlayClick = {
                        viewModel.playQueue(relaxingSongs)
                        if (relaxingSongs.isNotEmpty()) onSongClick(relaxingSongs.first())
                    }
                )
            }
            
            item {
                MoodPlaylistCard(
                    title = "Focus",
                    description = "Concentration-enhancing music",
                    songs = moodBasedSongs,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    icon = RhythmIcons.Focus,
                    onPlayClick = {
                        viewModel.playQueue(moodBasedSongs)
                        if (moodBasedSongs.isNotEmpty()) onSongClick(moodBasedSongs.first())
                    }
                )
            }
        }
    }
}

@Composable
private fun MoodPlaylistCard(
    title: String,
    description: String,
    songs: List<Song>,
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector,
    onPlayClick: () -> Unit
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
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
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Play button
            FilledIconButton(
                onClick = onPlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = contentColor,
                    contentColor = backgroundColor
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play $title playlist",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    viewAllAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        if (viewAllAction != null) {
            Surface(
                onClick = viewAllAction,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
@Composable
private fun FeaturedContentSection(
    albums: List<Album>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onAlbumClick: (Album) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced pager with larger cards and better animations
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 16.dp),
            pageSpacing = 16.dp
        ) { page ->
            val album = albums[page]
            FeaturedCard(
                album = album,
                onClick = { onAlbumClick(album) },
                pageOffset = (page - pagerState.currentPage).toFloat().absoluteValue
            )
        }
        
        // Enhanced pager indicators with animations
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val isSelected = index == pagerState.currentPage
                
                // Animate size only
                val size by animateFloatAsState(
                    targetValue = if (isSelected) 10f else 6f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator_size"
                )
                
                // Use direct color values without animation
                val color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(size.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { 
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
private fun RecentlyPlayedSection(
    recentlyPlayed: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Recently Played",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentlyPlayed) { song ->
                    EnhancedRecentChip(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedRecentChip(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    ElevatedCard(
        onClick = { viewModel.playSong(song) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(180.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Song artwork
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.surface
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
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickPickCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album artwork with improved shadow and shape
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp),
                tonalElevation = 4.dp,
                shadowElevation = 2.dp
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
            
            // Song info with improved typography
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Play button with improved design
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedCard(
    album: Album,
    onClick: () -> Unit,
    pageOffset: Float = 0f
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    // Enhanced animations based on offset
    val scale = lerp(
        start = 0.85f,
        stop = 1f,
        fraction = 1f - (pageOffset * 0.35f).coerceIn(0f, 0.35f)
    )
    
    val alpha = lerp(
        start = 0.5f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )
    
    // Rotation effect for 3D-like carousel
    val rotation = lerp(
        start = 0f,
        stop = -12f,
        fraction = pageOffset.coerceIn(0f, 1f)
    )
    
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                rotationY = rotation
                cameraDistance = 16f * density
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp,
            focusedElevation = 10.dp,
            hoveredElevation = 10.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image with improved loading
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .apply(ImageUtils.buildImageRequest(
                        album.artworkUri,
                        album.title,
                        context.cacheDir,
                        ImageUtils.PlaceholderType.ALBUM
                    ))
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Enhanced gradient overlay with multiple layers for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Horizontal gradient for more depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
            
            // Album info with improved layout
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Album info with enhanced text styles
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // Song count with subtle hint
                Text(
                    text = "${album.numberOfSongs} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )
                
                // Button row with play and add to library actions
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play button - play the album directly
                    FilledIconButton(
                        onClick = onClick,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = "Play album",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Add to library button - would add to a user's collection
                    IconButton(
                        onClick = { /* In a real app, this would add the album to the user's library */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Add,
                            contentDescription = "Add to library",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Year badge
                    if (album.year > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = album.year.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Album badge in top right corner
            Surface(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                shape = RoundedCornerShape(topEnd = 28.dp, bottomStart = 16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewAlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    Surface(
        onClick = onClick,
        modifier = modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp
    ) {
        Column {
            // Album artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            album.artworkUri,
                            album.title,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.ALBUM
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Play button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    FilledIconButton(
                        onClick = { viewModel.playAlbum(album) },
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = "Play album",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Album info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NewArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    Column(
        modifier = modifier
            .width(110.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist image with play overlay
        Box {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(110.dp),
                shadowElevation = 0.dp
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            artist.artworkUri,
                            artist.name,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.ARTIST
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Add play button overlay
            FilledIconButton(
                onClick = { viewModel.playArtist(artist) },
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play artist",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecommendedForYouSection(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    if (songs.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recommended For You",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Based on your listening history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                songs.forEachIndexed { index, song ->
                    RecommendedSongItem(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                    
                    if (index != songs.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendedSongItem(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
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
        },
        headlineContent = {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun UpdateAvailableSection(
    latestVersion: AppVersion,
    onUpdateClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }
    
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = { if (!isDownloading) onUpdateClick(false) })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo and app name in a horizontal row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rhythm_splash_logo),
                    contentDescription = "Rhythm Logo",
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Rhythm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Update icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RhythmIcons.Download,
                        contentDescription = "Update available",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Update Available",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Version ${latestVersion.versionName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show "What's New" section if available
            if (latestVersion.whatsNew.isNotEmpty()) {
                Text(
                    text = "What's New:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                latestVersion.whatsNew.take(2).forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Show "Known Issues" section if available
            if (latestVersion.knownIssues.isNotEmpty()) {
                Text(
                    text = "Known Issues:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                latestVersion.knownIssues.take(1).forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (latestVersion.releaseNotes.isNotEmpty() && latestVersion.whatsNew.isEmpty()) {
                // Fallback to general release notes if no structured "What's New" and "Known Issues"
                Text(
                    text = latestVersion.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Update button - use clickable modifier to prevent click propagation
            Surface(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isDownloading) {
                            isDownloading = true
                            onUpdateClick(true)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDownloading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = RhythmIcons.Download,
                            contentDescription = "Download update",
                            tint = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
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
