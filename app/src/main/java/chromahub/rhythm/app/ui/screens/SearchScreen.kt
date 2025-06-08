package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.util.M3ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onAddSongToPlaylist: (Song) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val viewModel: MusicViewModel = viewModel()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }
    
    // Filter states
    var filterSongs by remember { mutableStateOf(true) }
    var filterAlbums by remember { mutableStateOf(true) }
    var filterArtists by remember { mutableStateOf(true) }
    var filterPlaylists by remember { mutableStateOf(true) }
    
    // Collect search history from ViewModel
    val searchHistory by viewModel.searchHistory.collectAsState()
    
    // Filter results based on search query and active filters
    val filteredSongs by remember(searchQuery, songs, filterSongs) {
        derivedStateOf {
            if (searchQuery.isBlank() || !filterSongs) emptyList()
            else songs.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val filteredAlbums by remember(searchQuery, albums, filterAlbums) {
        derivedStateOf {
            if (searchQuery.isBlank() || !filterAlbums) emptyList()
            else albums.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val filteredArtists by remember(searchQuery, artists, filterArtists) {
        derivedStateOf {
            if (searchQuery.isBlank() || !filterArtists) emptyList()
            else artists.filter { 
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val filteredPlaylists by remember(searchQuery, playlists, filterPlaylists) {
        derivedStateOf {
            if (searchQuery.isBlank() || !filterPlaylists) emptyList()
            else playlists.filter { 
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val hasSearchResults = filteredSongs.isNotEmpty() || filteredAlbums.isNotEmpty() || 
                          filteredArtists.isNotEmpty() || filteredPlaylists.isNotEmpty()
    
    Scaffold(
        bottomBar = {
            if (currentSong != null) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onPlayPause = onPlayPause,
                    onPlayerClick = onPlayerClick,
                    onSkipNext = onSkipNext
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Material 3 SearchBar with fixed padding
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    isSearchActive = true 
                },
                onSearch = { 
                    focusManager.clearFocus() 
                    if (searchQuery.isNotEmpty()) {
                        viewModel.addSearchQuery(searchQuery)
                    }
                },
                active = false, // We manage our own activeness
                onActiveChange = { },
                placeholder = { 
                    Text(
                        "Search songs, albums, artists...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ) 
                },
                leadingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Back,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            imageVector = RhythmIcons.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    isSearchActive = false
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                shape = SearchBarDefaults.dockedShape,
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    dividerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                ),
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .focusRequester(focusRequester),
            ) { }
            
            Spacer(modifier = Modifier.height(if (showFilterOptions) 8.dp else 16.dp))
            
            // Default content or search results
            if (isSearchActive && searchQuery.isNotEmpty()) {
                // Search results
                if (hasSearchResults) {
                    SearchResults(
                        songs = filteredSongs,
                        albums = filteredAlbums,
                        artists = filteredArtists,
                        playlists = filteredPlaylists,
                        onSongClick = onSongClick,
                        onAlbumClick = onAlbumClick,
                        onArtistClick = onArtistClick,
                        onPlaylistClick = onPlaylistClick,
                        onAddSongToPlaylist = onAddSongToPlaylist
                    )
                } else {
                    // No results
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
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
                                imageVector = RhythmIcons.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(80.dp * animatedSize)
                                    .padding(bottom = 16.dp)
                            )
                            
                            Text(
                                text = if (filterSongs || filterAlbums || filterArtists || filterPlaylists)
                                    "No results found for \"$searchQuery\""
                                else
                                    "All filters are disabled. Enable at least one filter.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                // Default browse content
                SearchBrowseContent(
                    albums = albums,
                    artists = artists,
                    onAlbumClick = onAlbumClick,
                    onArtistClick = onArtistClick,
                    searchHistory = searchHistory,
                    onSearchHistoryClick = { query ->
                        searchQuery = query
                        isSearchActive = true
                    },
                    onClearSearchHistory = {
                        viewModel.clearSearchHistory()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SearchResults(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Songs section
        if (songs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Songs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (songs.size > 3) {
                        TextButton(
                            onClick = { /* Navigate to all songs */ },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "See all (${songs.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            items(songs.take(3)) { song ->
                SearchSongItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onAddToPlaylist = { onAddSongToPlaylist(song) }
                )
            }
        }
        
        // Albums section
        if (albums.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Albums",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (albums.size > 5) {
                        TextButton(
                            onClick = { /* Navigate to all albums */ },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "See all",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(albums) { album ->
                        SearchAlbumItem(
                            album = album,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }
        }
        
        // Artists section
        if (artists.isNotEmpty()) {
            item {
                Text(
                    text = "Artists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(artists) { artist ->
                        SearchArtistItem(
                            artist = artist,
                            onClick = { onArtistClick(artist) }
                        )
                    }
                }
            }
        }
        
        // Playlists section
        if (playlists.isNotEmpty()) {
            item {
                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(playlists) { playlist ->
                        SearchPlaylistItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }
        }
        
        // Add bottom spacing
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
fun SearchBrowseContent(
    albums: List<Album>,
    artists: List<Artist>,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    searchHistory: List<String> = emptyList(),
    onSearchHistoryClick: (String) -> Unit = {},
    onClearSearchHistory: () -> Unit = {}
) {
    // Make entire content scrollable
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp), // Bottom padding for navigation
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Recently searched section with improved styling
            if (searchHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Searched",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    
                    FilledTonalButton(
                        onClick = onClearSearchHistory,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                
                // Enhanced search history items
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    searchHistory.forEach { searchTerm ->
                        RecentSearchItem(
                            searchTerm = searchTerm,
                            onClick = { onSearchHistoryClick(searchTerm) }
                        )
                    }
                }
            } else {
                // Improved empty state for search history
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Searched",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(56.dp)
                                .padding(bottom = 16.dp)
                        )
                        Text(
                            text = "No recent searches",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Mood & Moments section
        item {
            Text(
                text = "Mood & Moments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MoodCard(
                        title = "Energize",
                        description = "Upbeat tracks to boost your energy",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = RhythmIcons.Energy,
                        onClick = { /* Handle mood click */ }
                    )
                }
                
                item {
                    MoodCard(
                        title = "Relax",
                        description = "Calm tracks to help you unwind",
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        icon = RhythmIcons.Relax,
                        onClick = { /* Handle mood click */ }
                    )
                }
                
                item {
                    MoodCard(
                        title = "Focus",
                        description = "Concentration-enhancing music",
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        icon = RhythmIcons.Focus,
                        onClick = { /* Handle mood click */ }
                    )
                }
            }
        }
        
        // Browse categories section
        item {
            Text(
                text = "Browse Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Full categories grid with all items visible
        item {
            // Define our categories
            val categories = listOf(
                Category("Pop", MaterialTheme.colorScheme.primary),
                Category("Rock", MaterialTheme.colorScheme.secondary),
                Category("Hip Hop", MaterialTheme.colorScheme.tertiary),
                Category("Classical", MaterialTheme.colorScheme.primary),
                Category("Jazz", MaterialTheme.colorScheme.secondary),
                Category("Electronic", MaterialTheme.colorScheme.tertiary)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Using a grid with 2 columns
                for (i in categories.indices step 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // First item
                        Box(modifier = Modifier.weight(1f)) {
                            CategoryCard(
                                title = categories[i].name,
                                icon = RhythmIcons.Music.MusicNote,
                                color = categories[i].color,
                                onClick = { /* Handle category click */ }
                            )
                        }
                        
                        // Second item (if exists)
                        if (i + 1 < categories.size) {
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryCard(
                                    title = categories[i + 1].name,
                                    icon = RhythmIcons.Music.MusicNote,
                                    color = categories[i + 1].color,
                                    onClick = { /* Handle category click */ }
                                )
                            }
                        } else {
                            // Empty space to maintain grid layout
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            // Add bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Data class for category information
private data class Category(val name: String, val color: Color)

@Composable
fun RecentSearchItem(
    searchTerm: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search icon in a circular container with enhanced styling
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = RhythmIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Search term with improved typography
            Text(
                text = searchTerm,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            
            // Enhanced forward icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSongItem(
    song: Song,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                M3ImageUtils.TrackImage(
                    imageUrl = song.artworkUri,
                    trackName = song.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Song info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Add to playlist button
            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.AddToPlaylist,
                    contentDescription = "Add to playlist",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SearchAlbumItem(
    album: Album,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Album cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                M3ImageUtils.AlbumArt(
                    imageUrl = album.artworkUri,
                    albumName = album.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Album info
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SearchArtistItem(
    artist: Artist,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artist image with elevation
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(96.dp)
                    .padding(4.dp),
                shadowElevation = 4.dp
            ) {
                M3ImageUtils.ArtistImage(
                    imageUrl = artist.artworkUri,
                    artistName = artist.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreCard(
    genre: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        modifier = Modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = genre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SearchPlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Playlist artwork
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (playlist.artworkUri == null)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surface
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Playlist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "${playlist.songs.size} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Category icon with tonal container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Category title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun RecommendedAlbumItem(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        // Album art with Material 3 card styling
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp,
                pressedElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                M3ImageUtils.AlbumArt(
                    imageUrl = album.artworkUri,
                    albumName = album.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Album info
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MoodCard(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .clickable(onClick = onClick)
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
                onClick = onClick,
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
