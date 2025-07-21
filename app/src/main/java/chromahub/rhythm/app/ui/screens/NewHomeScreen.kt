package chromahub.rhythm.app.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import chromahub.rhythm.app.R
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.screens.ArtistBottomSheet
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

// Data class for time-based theming
private data class TimeTheme(
    val emoji: String,
    val gradientColors: List<String>,
    val accentEmoji: String
)

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
    onAddToQueue: (Song) -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onNavigateToPlaylist: (String) -> Unit = {},
    updaterViewModel: AppUpdaterViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    
    // State for artist bottom sheet
    var showArtistSheet by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val artistSheetState = rememberModalBottomSheetState()
    
    // Select featured content from all albums (5 randomly shuffled albums)
    val featuredContent = remember(albums) {
        albums.shuffled().take(5)
    }
    
    // Get all unique artists from songs using collaboration-aware filtering
    val availableArtists = remember(songs, artists) {
        ArtistCollaborationUtils.extractIndividualArtists(artists, songs)
            .sortedBy { it.name } // Sort alphabetically for better navigation
    }
    
    // Quick picks can still be first few songs but used for other sections
    val quickPicks = songs.take(6)
    val topArtists = availableArtists
    
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

    // Filter recently added albums (last month, sorted by date modified)
    // Generate mood-based playlists - ordered by recency and characteristics
    val moodBasedSongs = songs.takeLast(12)
    val energeticSongs = songs.take(10)
    val relaxingSongs = songs.drop(10).take(10)

    // Filter recently added songs (last month, sorted by date added)
    val recentlyAddedSongs = remember(songs) {
        val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
        songs.filter { it.dateAdded >= oneMonthAgo }
            .sortedByDescending { it.dateAdded }
            .take(5) // Take top 5 recently added songs
    }

    // Show artist bottom sheet when an artist is selected
    if (showArtistSheet && selectedArtist != null) {
        ArtistBottomSheet(
            artist = selectedArtist!!,
            songs = songs,
            albums = albums,
            onDismiss = { showArtistSheet = false },
            onSongClick = { song: Song ->
                showArtistSheet = false
                onSongClick(song)
            },
            onAlbumClick = { album: Album ->
                showArtistSheet = false
                onAlbumClick(album)
            },
            onPlayAll = {
                // Queue will be managed in ArtistBottomSheet itself using proper queue setup
                showArtistSheet = false
            },
            onShufflePlay = {
                // Queue will be managed in ArtistBottomSheet itself using proper queue setup
                showArtistSheet = false
            },
            onAddToQueue = { song ->
                onAddToQueue(song)
            },
            onAddSongToPlaylist = { song ->
                onAddSongToPlaylist(song, "")
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
                    FilledIconButton(
                        onClick = onSettingsClick,
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
        EnhancedScrollableContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = if (currentSong != null) 0.dp else 0.dp),
            featuredContent = featuredContent,
            albums = albums,
            topArtists = topArtists,
            newReleases = currentYearReleases,
            recentlyAddedSongs = recentlyAddedSongs, // Pass recently added songs
            recentlyPlayed = recentlyPlayed,
            moodBasedSongs = moodBasedSongs,
            energeticSongs = energeticSongs,
            relaxingSongs = relaxingSongs,
            onSongClick = onSongClick,
            onAlbumClick = onAlbumClick,
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
    topArtists: List<Artist>,
    newReleases: List<Album>,
    recentlyAddedSongs: List<Song>, // New parameter
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
    val scrollState = rememberScrollState()
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val allSongs by viewModel.songs.collectAsState()
    
    // Optimize artist computation with improved filtering for collaborations
    val availableArtists = remember(allSongs, topArtists) {
        // Define all possible collaboration separators
        val collaborationSeparators = listOf(
            ", ", ",", " & ", " and ", "&", " feat. ", " featuring ", " ft. ", 
            " with ", " x ", " X ", " + ", " vs ", " VS ", " / ", ";", " · "
        )

        // Create a regex pattern to detect collaboration strings within artist names
        val collaborationRegex = collaborationSeparators
            .map { Regex.escape(it) }
            .joinToString("|")
            .toRegex(RegexOption.IGNORE_CASE)

        // Filter out any Artist objects from the initial 'topArtists' list that appear to be collaboration strings
        // This prevents combined artist names from appearing as single artists in the list
        val filteredTopArtists = topArtists.filter { artist ->
            !artist.name.contains(collaborationRegex)
        }

        // Extract individual artist names from all songs, splitting collaboration entries properly
        val extractedArtistNames = allSongs.asSequence()
            .flatMap { song -> 
                var artistString = song.artist
                // Replace all separators with a standard one for consistent splitting
                collaborationSeparators.forEach { separator ->
                    artistString = artistString.replace(separator, "||")
                }
                
                // Split, clean up, and handle parentheses for featuring mentions
                artistString.split("||")
                    .map { it.trim() }
                    .map { name ->
                        // Remove featuring mentions in parentheses
                        if (name.contains("(") && (name.contains("feat") || name.contains("ft") || name.contains("featuring"))) {
                            name.substringBefore("(").trim()
                        } else {
                            name
                        }
                    }
            }
            .filter { it.length > 1 } // Filter out very short names
            .distinct()
        
        // Match extracted individual artist names to the filtered artist objects and sort alphabetically
        extractedArtistNames
            .mapNotNull { artistName ->
                // Find exact matches first, then partial matches as fallback from the filtered list
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
    
    // Featured albums state with auto-refresh
    var currentFeaturedAlbums by remember(featuredContent) { 
        mutableStateOf(featuredContent) 
    }
    val featuredPagerState = rememberPagerState(pageCount = { currentFeaturedAlbums.size })
    
    // Auto-refresh featured content every 30 seconds
    LaunchedEffect(albums) {
        while (true) {
            delay(30000) // 30 seconds
            if (albums.size > 5) {
                currentFeaturedAlbums = albums.shuffled().take(5)
            }
        }
    }
    
    // Enhanced mood-based content with better filtering
    val enhancedMoodContent = remember(allSongs) {
        val focusSongs = allSongs
            .filter { it.duration > 3 * 60 * 1000 }
            .take(12)
            .ifEmpty { moodBasedSongs }
        
        val energeticKeywords = listOf("rock", "dance", "pop", "party", "beat", "energy", "fast", "upbeat")
        val betterEnergeticSongs = allSongs
            .filter { song ->
                energeticKeywords.any { keyword ->
                    song.title.contains(keyword, ignoreCase = true) || 
                    song.artist.contains(keyword, ignoreCase = true)
                }
            }
            .take(10)
            .ifEmpty { energeticSongs }
        
        val relaxingKeywords = listOf("chill", "relax", "ambient", "piano", "sleep", "calm", "soft", "peaceful")
        val betterRelaxingSongs = allSongs
            .filter { song ->
                relaxingKeywords.any { keyword ->
                    song.title.contains(keyword, ignoreCase = true) || 
                    song.artist.contains(keyword, ignoreCase = true)
                }
            }
            .take(10)
            .ifEmpty { relaxingSongs }
        
        Triple(focusSongs, betterEnergeticSongs, betterRelaxingSongs)
    }
    
    // Greeting based on time
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
    
    // Auto-scroll featured pager
    LaunchedEffect(featuredPagerState.pageCount) {
        if (featuredPagerState.pageCount > 1) {
            while (true) {
                delay(4000) // 4 seconds per page
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
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Update card or welcome section
            AnimatedVisibility(
                visible = updateAvailable && latestVersion != null && error == null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                latestVersion?.let { version ->
                    UpdateAvailableSection(
                        latestVersion = version,
                        onUpdateClick = onAppUpdateClick
                    )
                }
            }
            
            if (!updateAvailable || latestVersion == null || error != null) {
                WelcomeSection(greeting = greeting, onSearchClick = onSearchClick)
            }
            
            // Recommendations
            val recommendedSongs = remember(viewModel) {
                viewModel.getRecommendedSongs().take(4)
            }

            // --- Enhanced Home Screen Sections ---

            // Recently Played - Show user's activity
            AnimatedVisibility(
                visible = recentlyPlayed.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                RecentlyPlayedSection(
                    recentlyPlayed = recentlyPlayed.take(5),
                    onSongClick = onSongClick
                )
            }

            // Featured Albums Carousel
            AnimatedVisibility(
                visible = currentFeaturedAlbums.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(title = "Discover Albums", viewAllAction = onViewAllAlbums, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                    FeaturedContentSection(
                        albums = currentFeaturedAlbums,
                        pagerState = featuredPagerState,
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            // Recommended For You
            AnimatedVisibility(
                visible = recommendedSongs.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                RecommendedForYouSection(
                    songs = recommendedSongs,
                    onSongClick = onSongClick
                )
            }

            // Artists Section - Horizontal Carousel
            AnimatedVisibility(
                visible = availableArtists.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                ArtistsCarouselSection(
                    artists = availableArtists,
                    songs = allSongs,
                    onArtistClick = onArtistClick,
                    onViewAllArtists = onViewAllArtists
                )
            }

            // New Releases
            AnimatedVisibility(
                visible = newReleases.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column {
                    SectionTitle(title = "New Releases", viewAllAction = onViewAllAlbums, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(newReleases) { album ->
                            NewAlbumCard(
                                album = album,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }
                }
            }

            // Recently Added Songs
            AnimatedVisibility(
                visible = recentlyAddedSongs.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column {
                    SectionTitle(title = "Recently Added", viewAllAction = onViewAllSongs, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentlyAddedSongs) { song ->
                            EnhancedRecentChip(
                                song = song,
                                onClick = { onSongClick(song) }
                            )
                        }
                    }
                }
            }

            // Listening Stats
            ListeningStatsSection()

            // Mood & Moments - Placed last for exploration
            MoodBasedPlaylistsSection(
                moodBasedSongs = enhancedMoodContent.first,
                energeticSongs = enhancedMoodContent.second,
                relaxingSongs = enhancedMoodContent.third,
                onSongClick = onSongClick
            )
        }
    }
}

@Composable
private fun ArtistsCarouselSection(
    artists: List<Artist>,
    songs: List<Song>,
    onArtistClick: (Artist) -> Unit,
    onViewAllArtists: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section header
        SectionTitle(
            title = "Artists",
            viewAllAction = onViewAllArtists,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
        
        // Horizontal carousel of artists
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(artists) { artist ->
                ArtistCarouselCard(
                    artist = artist,
                    songs = songs,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Composable
private fun ArtistCarouselCard(
    artist: Artist,
    songs: List<Song>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    // Filter songs by the current artist
    val artistSongs = remember(songs, artist) {
        chromahub.rhythm.app.util.ArtistCollaborationUtils.filterSongsByArtist(songs, artist.name)
    }

    Column(
        modifier = modifier
            .width(128.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist image container with play overlay
        Box(
            modifier = Modifier.size(128.dp)
        ) {
            // Artist circular image
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                shadowElevation = 0.dp
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            artist.artworkUri,
                            artist.name,
                            context.cacheDir,
                            M3PlaceholderType.ARTIST
                        ))
                        .build(),
                    contentDescription = "Artist ${artist.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Play button overlay positioned at bottom right
            Surface(
                onClick = { 
                    if (artistSongs.isNotEmpty()) {
                        viewModel.playQueue(artistSongs)
                    }
                    onClick()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play ${artist.name}",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Artist name below the image
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
        
        // Track count as subtle additional info
    }
}

@Composable
private fun WelcomeSection(
    greeting: String,
    onSearchClick: () -> Unit
) {
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    
    // Time-based inspirational quotes with music theme
    val timeBasedQuote = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        when {
            hour in 5..8 -> listOf(
                "Start your day with the perfect soundtrack ✨",
                "Let music energize your morning routine 🎵",
                "Every morning is a new symphony waiting to begin 🌅",
                "Rise and shine with melodies that move you 🎶",
                "Coffee and music - the perfect morning blend ☕"
            )
            hour in 9..11 -> listOf(
                "Keep the momentum going with your favorite beats 🚀",
                "Music makes everything better, especially work 💪",
                "Let the rhythm guide your productive morning 🎯",
                "Turn up the focus with some great tunes 🔥",
                "Morning motivation through music 📈"
            )
            hour in 12..14 -> listOf(
                "Take a musical break and recharge 🔄",
                "Lunch time calls for your favorite playlist 🍽️",
                "Midday melodies to keep you going 🌞",
                "Fuel your afternoon with some great music ⚡",
                "The perfect soundtrack for your lunch break 🎼"
            )
            hour in 15..17 -> listOf(
                "Power through the afternoon with epic tunes 💪",
                "Let music be your afternoon energy boost 🌟",
                "Beat the afternoon slump with your favorites 🎵",
                "Turn up the volume and turn up the productivity 📊",
                "Music makes the workday so much better 🎧"
            )
            hour in 18..20 -> listOf(
                "Time to unwind with some soothing melodies 🌅",
                "Let the evening soundtrack begin 🎶",
                "Perfect time for your chill playlist 😌",
                "Wind down with music that speaks to your soul 💫",
                "Evening vibes call for the perfect playlist 🌆"
            )
            hour in 21..23 -> listOf(
                "Night time is music time 🌙",
                "Let the stars dance to your favorite songs ⭐",
                "Perfect night for discovering new artists 🎭",
                "Late night listening sessions hit different 🌃",
                "End your day with music that moves you 💫"
            )
            else -> listOf(
                "Music never sleeps, and neither do we 🌙",
                "Late night sessions with your favorite tunes 🎵",
                "The night is young, and so is your playlist 🌌",
                "Midnight melodies for the soul 🎼",
                "When the world sleeps, music keeps us company 💫"
            )
        }.let { quotes ->
            // Add special weekend quotes
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                quotes + listOf(
                    "Weekend vibes deserve the perfect playlist 🎉",
                    "Saturday/Sunday soundtrack loading... 🎶",
                    "Weekends are for music and good vibes only ✨"
                )
            } else quotes
        }.random()
    }
    
    // Enhanced personalized message with more context and variety
    val personalizedMessage = remember(recentlyPlayed) {
        if (recentlyPlayed.isNotEmpty()) {
            val recentArtist = recentlyPlayed.firstOrNull()?.artist
            val recentSong = recentlyPlayed.firstOrNull()?.title
            val playCount = recentlyPlayed.size
            
            when {
                !recentArtist.isNullOrBlank() && recentArtist != "Unknown" && !recentSong.isNullOrBlank() -> {
                    val messages = listOf(
                        "Continue where you left off with \"$recentSong\"",
                        "Ready to dive back into \"$recentSong\"?",
                        "\"$recentSong\" is waiting for you",
                        "Pick up where you left off, or explore something new"
                    )
                    messages.random()
                }
                !recentArtist.isNullOrBlank() && recentArtist != "Unknown" -> {
                    val messages = listOf(
                        "More $recentArtist coming right up!",
                        "Dive deeper into $recentArtist's discography",
                        "Time for more amazing tracks from $recentArtist",
                        "Continue your $recentArtist journey"
                    )
                    messages.random()
                }
                playCount > 5 -> {
                    "You've been quite active! Ready for more musical adventures?"
                }
                else -> {
                    val messages = listOf(
                        "What musical journey shall we embark on today?",
                        "Your next favorite song is just a search away",
                        "Ready to discover your new obsession?"
                    )
                    messages.random()
                }
            }
        } else {
            val messages = listOf(
                "Your musical adventure starts here",
                "Every great playlist begins with a single song",
                "Ready to create some musical memories?",
                "Let's find your next favorite song",
                "The perfect soundtrack to your day awaits"
            )
            messages.random()
        }
    }
    
    // Enhanced dynamic theme based on time of day with more design elements
    val timeBasedTheme = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..8 -> TimeTheme(
                emoji = "🌅",
                gradientColors = listOf("sunrise", "morning"),
                accentEmoji = "☀️"
            )
            hour in 9..11 -> TimeTheme(
                emoji = "☀️",
                gradientColors = listOf("morning", "midday"), 
                accentEmoji = "💪"
            )
            hour in 12..14 -> TimeTheme(
                emoji = "☀️",
                gradientColors = listOf("midday", "afternoon"),
                accentEmoji = "⚡"
            )
            hour in 15..17 -> TimeTheme(
                emoji = "🌤️",
                gradientColors = listOf("afternoon", "evening"),
                accentEmoji = "🎵"
            )
            hour in 18..20 -> TimeTheme(
                emoji = "🌆",
                gradientColors = listOf("evening", "sunset"),
                accentEmoji = "🎶"
            )
            hour in 21..23 -> TimeTheme(
                emoji = "🌃",
                gradientColors = listOf("sunset", "night"),
                accentEmoji = "🌙"
            )
            else -> TimeTheme(
                emoji = "🌙",
                gradientColors = listOf("night", "midnight"),
                accentEmoji = "⭐"
            )
        }
    }

    // Get themed decorative elements based on time
    val decorativeElements = remember(timeBasedTheme) {
        when (timeBasedTheme.gradientColors[0]) {
            "sunrise" -> listOf("🌄", "🐦", "🌱")
            "morning" -> listOf("☕", "📰", "🌻")
            "midday" -> listOf("🌞", "🌴", "🏖️")
            "afternoon" -> listOf("☁️", "🍃", "🌸")
            "evening" -> listOf("🌅", "🕊️", "🌺")
            "sunset" -> listOf("🌇", "🎨", "🦋")
            "night" -> listOf("🌙", "⭐", "🦉")
            else -> listOf("🌌", "✨", "💫")
        }
    }

    // Simple card with enhanced design including quotes and animated emojis
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 8.dp)
            .clickable { onSearchClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background decorative elements
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                decorativeElements.forEach { element ->
                    Text(
                        text = element,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.alpha(0.2f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Main greeting row with animated emoji
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    // Animated main emoji
                    val infiniteTransition = rememberInfiniteTransition(label = "emoji_pulse")
                    val emojiScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "emoji_scale"
                    )
                    
                    Text(
                        text = timeBasedTheme.emoji,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .graphicsLayer {
                                scaleX = emojiScale
                                scaleY = emojiScale
                            }
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // Greeting with accent emoji
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = timeBasedTheme.accentEmoji,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        // Simple message
                        Text(
                            text = personalizedMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Search icon
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Inspirational quote section with enhanced design
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quote icon with animation
                        val quoteTransition = rememberInfiniteTransition(label = "quote_pulse")
                        val quoteScale by quoteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "quote_scale"
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer {
                                    scaleX = quoteScale
                                    scaleY = quoteScale
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "💭",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = timeBasedQuote,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Listening Stats",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "This week's activity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Icon(
                imageVector = RhythmIcons.Relax,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Enhanced stats cards with better spacing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stat Card 1: Listening Time
            ElevatedCard(
                onClick = { /* Optional: navigate to detailed stats */ },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                EnhancedStatCardContent(
                    value = listeningTimeHours,
                    label = "Rhythm-ed",
                    icon = RhythmIcons.Player.Timer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            // Stat Card 2: Songs Played
            ElevatedCard(
                onClick = { /* Optional: navigate to detailed stats */ },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                EnhancedStatCardContent(
                    value = songsPlayed,
                    label = "Songs",
                    icon = RhythmIcons.Music.MusicNote,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            // Stat Card 3: Artists
            ElevatedCard(
                onClick = { /* Optional: navigate to detailed stats */ },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                EnhancedStatCardContent(
                    value = uniqueArtists,
                    label = "Artists",
                    icon = RhythmIcons.Artist,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatCardContent(
    value: String,
    label: String,
    icon: ImageVector,
    contentColor: Color,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    ),
                    radius = 120f
                )
            )
            .padding(vertical = 24.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with enhanced styling
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

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
private fun StatCardContent(
    value: String,
    label: String,
    icon: ImageVector,
    contentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.7f)
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        if (energeticSongs.isNotEmpty()) {
                            // Play with queue replacement and shuffling for better variety
                            viewModel.playSongWithQueueOption(
                                song = energeticSongs.first(), 
                                replaceQueue = true, 
                                shuffleQueue = true
                            )
                            onSongClick(energeticSongs.first())
                        }
                    },
                    onAddToQueueClick = {
                        if (energeticSongs.isNotEmpty()) {
                            viewModel.addContextToQueue(energeticSongs, shuffled = true)
                        }
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
                        if (relaxingSongs.isNotEmpty()) {
                            viewModel.playSongWithQueueOption(
                                song = relaxingSongs.first(), 
                                replaceQueue = true
                            )
                            onSongClick(relaxingSongs.first())
                        }
                    },
                    onAddToQueueClick = {
                        if (relaxingSongs.isNotEmpty()) {
                            viewModel.addContextToQueue(relaxingSongs)
                        }
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
                        if (moodBasedSongs.isNotEmpty()) {
                            viewModel.playSongWithQueueOption(
                                song = moodBasedSongs.first(), 
                                replaceQueue = true
                            )
                            onSongClick(moodBasedSongs.first())
                        }
                    },
                    onAddToQueueClick = {
                        if (moodBasedSongs.isNotEmpty()) {
                            viewModel.addContextToQueue(moodBasedSongs)
                        }
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
    onPlayClick: () -> Unit,
    onAddToQueueClick: () -> Unit = {}
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .width(240.dp)
            .height(260.dp),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column {
                Surface(
                    shape = CircleShape,
                    color = contentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(14.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Action buttons with enhanced Android 16 styling
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add to queue button with enhanced styling
                Surface(
                    onClick = onAddToQueueClick,
                    shape = CircleShape,
                    color = contentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = RhythmIcons.AddToQueue,
                        contentDescription = "Add $title to queue",
                        tint = contentColor,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(0.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Play button with enhanced styling
                Surface(
                    onClick = onPlayClick,
                    shape = CircleShape,
                    color = contentColor,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play $title",
                        tint = backgroundColor,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(0.dp)
                    )
                }
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
            .padding(horizontal = 0.dp, vertical = 12.dp),
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
            FilledIconButton(
                onClick = viewAllAction,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "View All",
                    modifier = Modifier.size(20.dp)
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
                .padding(horizontal = 8.dp),
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced section header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pick up where you left off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Icon(
                    imageVector = RhythmIcons.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Enhanced song cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedRecentChip(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(200.dp)
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced song artwork with elevation
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
                tonalElevation = 4.dp,
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Play indicator icon
            Icon(
                imageVector = RhythmIcons.Player.Play,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(0.7f)
            )
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
                            M3PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
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
                        M3PlaceholderType.ALBUM
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
        modifier = modifier.width(180.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            // Album artwork with enhanced corner radius
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
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
                
                // Enhanced play button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Surface(
                        onClick = onClick,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 0.dp
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = "Play album",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(12.dp)
                        )
                    }
                }
            }
            
            // Album info with enhanced padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = album.artist,
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
private fun EnhancedArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Artist image with gradient overlay and play button
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(90.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .apply(ImageUtils.buildImageRequest(
                                artist.artworkUri,
                                artist.name,
                                context.cacheDir,
                                M3PlaceholderType.ARTIST
                            ))
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Play button with enhanced design
                Surface(
                    onClick = { viewModel.playArtist(artist) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = "Play ${artist.name}",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Artist name with better typography
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Additional info (songs count, etc.)
        }
    }
}

@Composable
private fun CompactArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist image
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(56.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            artist.artworkUri,
                            artist.name,
                            context.cacheDir,
                            M3PlaceholderType.ARTIST
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Artist info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
            }
            
            // Play button
            IconButton(
                onClick = { viewModel.playArtist(artist) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play ${artist.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
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
                            M3PlaceholderType.ARTIST
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
                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                .padding(horizontal = 8.dp)
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
                            M3PlaceholderType.TRACK
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
            .padding(horizontal = 8.dp, vertical = 8.dp)
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
                
                Spacer(modifier = Modifier.width(3.dp))
                
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
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)),
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
                        text = HtmlCompat.fromHtml(item, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
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
                        text = HtmlCompat.fromHtml(item, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
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
                    text = HtmlCompat.fromHtml(latestVersion.releaseNotes, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
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
                            // Navigate to AppUpdaterScreen instead of auto-downloading
                            onUpdateClick(false) // Pass false to navigate to updater screen
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
                        chromahub.rhythm.app.ui.components.M3FourColorCircularLoader(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp.value
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
