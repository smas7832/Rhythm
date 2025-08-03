package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.ElevatedCard
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
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import androidx.compose.runtime.Composable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import androidx.compose.material3.rememberModalBottomSheetState
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.ui.components.CreatePlaylistDialog
import chromahub.rhythm.app.ui.screens.ArtistBottomSheet
import chromahub.rhythm.app.util.ArtistCollaborationUtils
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.ui.screens.SettingsSectionHeader
import chromahub.rhythm.app.ui.components.M3PlaceholderType

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
    onSkipNext: () -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: MusicViewModel = viewModel()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAllSongsPage by remember { mutableStateOf(false) }
    
    // Filter states
    var filterSongs by remember { mutableStateOf(true) }
    var filterAlbums by remember { mutableStateOf(true) }
    var filterArtists by remember { mutableStateOf(true) }
    var filterPlaylists by remember { mutableStateOf(true) }
    
    // Collect search history from ViewModel
    val searchHistory by viewModel.searchHistory.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    
    // Enhanced search logic with better ranking and fuzzy matching
    val searchedSongs by remember(searchQuery, songs) {
        derivedStateOf {
            if (searchQuery.isBlank()) emptyList()
            else {
                val query = searchQuery.lowercase()
                songs.filter { song ->
                    listOf(song.title, song.artist, song.album)
                        .any { it.contains(query, ignoreCase = true) }
                }.sortedWith(compareBy<Song> { song ->
                    // Prioritize exact matches in title
                    when {
                        song.title.lowercase() == query -> 0
                        song.title.lowercase().startsWith(query) -> 1
                        song.artist.lowercase() == query -> 2
                        song.artist.lowercase().startsWith(query) -> 3
                        song.album.lowercase() == query -> 4
                        song.album.lowercase().startsWith(query) -> 5
                        else -> 6
                    }
                }.thenBy { it.title })
            }
        }
    }

    val filteredSongs by remember(searchedSongs, filterSongs) {
        derivedStateOf {
            if (filterSongs) searchedSongs else emptyList()
        }
    }

    val searchedAlbums by remember(searchQuery, albums) {
        derivedStateOf {
            if (searchQuery.isBlank()) emptyList()
            else {
                val query = searchQuery.lowercase()
                albums.filter { album ->
                    listOf(album.title, album.artist)
                        .any { it.contains(query, ignoreCase = true) }
                }.sortedWith(compareBy<Album> { album ->
                    // Prioritize exact matches in title
                    when {
                        album.title.lowercase() == query -> 0
                        album.title.lowercase().startsWith(query) -> 1
                        album.artist.lowercase() == query -> 2
                        album.artist.lowercase().startsWith(query) -> 3
                        else -> 4
                    }
                }.thenBy { it.title })
            }
        }
    }

    val filteredAlbums by remember(searchedAlbums, filterAlbums) {
        derivedStateOf {
            if (filterAlbums) searchedAlbums else emptyList()
        }
    }
    
    val uniqueArtists = remember(artists, songs) {
        ArtistCollaborationUtils.extractIndividualArtists(artists, songs)
    }

    val searchedArtists by remember(searchQuery, uniqueArtists) {
        derivedStateOf {
            if (searchQuery.isBlank()) emptyList()
            else {
                val query = searchQuery.lowercase()
                uniqueArtists.filter { artist ->
                    artist.name.contains(query, ignoreCase = true)
                }.sortedWith(compareBy<Artist> { artist ->
                    // Prioritize exact matches and starts with
                    when {
                        artist.name.lowercase() == query -> 0
                        artist.name.lowercase().startsWith(query) -> 1
                        else -> 2
                    }
                }.thenBy { it.name })
            }
        }
    }

    val filteredArtists by remember(searchedArtists, filterArtists) {
        derivedStateOf {
            if (filterArtists) searchedArtists else emptyList()
        }
    }

    val searchedPlaylists by remember(searchQuery, playlists) {
        derivedStateOf {
            if (searchQuery.isBlank()) emptyList()
            else {
                val query = searchQuery.lowercase()
                playlists.filter { playlist ->
                    playlist.name.contains(query, ignoreCase = true)
                }.sortedWith(compareBy<Playlist> { playlist ->
                    // Prioritize exact matches and starts with
                    when {
                        playlist.name.lowercase() == query -> 0
                        playlist.name.lowercase().startsWith(query) -> 1
                        else -> 2
                    }
                }.thenBy { it.name })
            }
        }
    }

    val filteredPlaylists by remember(searchedPlaylists, filterPlaylists) {
        derivedStateOf {
            if (filterPlaylists) searchedPlaylists else emptyList()
        }
    }
    
    val totalResults = filteredSongs.size + filteredAlbums.size + filteredArtists.size + filteredPlaylists.size
    val hasSearchResults = totalResults > 0
    val hasNoResults = searchQuery.isNotEmpty() && totalResults == 0
    
    val scope = rememberCoroutineScope()
    
    // Bottom sheet + dialog state for add-to-playlist flow
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    
    // Album bottom sheet state
    var showAlbumBottomSheet by remember { mutableStateOf(false) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    val albumBottomSheetState = rememberModalBottomSheetState()
    
    // Artist bottom sheet state  
    var showArtistBottomSheet by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val artistBottomSheetState = rememberModalBottomSheetState()
    val haptics = LocalHapticFeedback.current
    
    Scaffold(
        bottomBar = {}
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Enhanced Material 3 SearchBar with modern design and animations
            // Enhanced Material 3 SearchBar with modern design and animations
            DockedSearchBar(
                expanded = isSearchActive,
                onExpandedChange = { isSearchActive = it },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    dividerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .focusRequester(focusRequester)
                    .then(if (isSearchActive) Modifier.fillMaxSize() else Modifier),
                inputField = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { newQuery ->
                            searchQuery = newQuery
                            isSearchActive = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "  Search your 🎵 Rhythm...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 5.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                        onBack()
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Back,
                                        contentDescription = "Back",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Icon(
                                    imageVector = RhythmIcons.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    // Filter toggle button with accent background
                                    IconButton(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                            showFilterOptions = !showFilterOptions
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = if (showFilterOptions)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent,
                                            contentColor = if (showFilterOptions)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FilterList,
                                            contentDescription = "Filters",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    val animatedAlpha by animateFloatAsState(
                                        targetValue = 1f,
                                        animationSpec = spring(dampingRatio = 0.8f),
                                        label = "clearButtonAlpha"
                                    )

                                    IconButton(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                            searchQuery = ""
                                            isSearchActive = false
                                            focusManager.clearFocus()
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        modifier = Modifier.graphicsLayer(alpha = animatedAlpha)
                                    ) {
                                        Icon(
                                            imageVector = RhythmIcons.Close,
                                            contentDescription = "Clear search",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotEmpty()) {
                                viewModel.addSearchQuery(searchQuery)
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                },
                content = {
                    // Content for the expanded search bar
                    if (showAllSongsPage) {
                        AllSongsPage(
                            songs = filteredSongs,
                            onSongClick = onSongClick,
                            onAddSongToPlaylist = { song ->
                                selectedSong = song
                                showAddToPlaylistSheet = true
                            },
                            onBack = { showAllSongsPage = false },
                            haptics = haptics
                        )
                    } else if (searchQuery.isEmpty()) {
                        // The content is now shown outside the search bar when not active
                    } else if (hasSearchResults) {
                        // Search results
                        SearchResults(
                            songs = filteredSongs,
                            albums = filteredAlbums,
                            artists = filteredArtists,
                            playlists = filteredPlaylists,
                            searchQuery = searchQuery,
                            totalResults = totalResults,
                            onSongClick = onSongClick,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onPlaylistClick = onPlaylistClick,
                            onAddSongToPlaylist = { song ->
                                selectedSong = song
                                showAddToPlaylistSheet = true
                            },
                            onAlbumBottomSheetClick = { album ->
                                selectedAlbum = album
                                showAlbumBottomSheet = true
                            },
                            onArtistBottomSheetClick = { artist ->
                                selectedArtist = artist
                                showArtistBottomSheet = true
                            },
                            onViewAllSongsClick = { showAllSongsPage = true },
                            filterSection = {
                                AnimatedVisibility(
                                    visible = showFilterOptions,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Filter Results",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            )

                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp)
                                            ) {
                                                // Select All / Deselect All chip
                                                item {
                                                    val allSelected = filterSongs && filterAlbums && filterArtists && filterPlaylists
                                                    val noneSelected = !filterSongs && !filterAlbums && !filterArtists && !filterPlaylists

                                                    FilterChip(
                                                        onClick = {
                                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                            if (allSelected || (!allSelected && !noneSelected)) {
                                                                // Deselect all
                                                                filterSongs = false
                                                                filterAlbums = false
                                                                filterArtists = false
                                                                filterPlaylists = false
                                                            } else {
                                                                // Select all
                                                                filterSongs = true
                                                                filterAlbums = true
                                                                filterArtists = true
                                                                filterPlaylists = true
                                                            }
                                                        },
                                                        label = {
                                                            Text(if (allSelected) "Deselect All" else "Select All")
                                                        },
                                                        selected = allSelected,
                                                        leadingIcon = if (allSelected) {
                                                            { Icon(RhythmIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                    )
                                                }

                                                item {
                                                    FilterChip(
                                                        onClick = {
                                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                            filterSongs = !filterSongs
                                                        },
                                                        label = {
                                                            Text("Songs (${searchedSongs.size})")
                                                        },
                                                        selected = filterSongs,
                                                        leadingIcon = if (filterSongs) {
                                                            { Icon(RhythmIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    )
                                                }

                                                item {
                                                    FilterChip(
                                                        onClick = {
                                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                            filterAlbums = !filterAlbums
                                                        },
                                                        label = {
                                                            Text("Albums (${searchedAlbums.size})")
                                                        },
                                                        selected = filterAlbums,
                                                        leadingIcon = if (filterAlbums) {
                                                            { Icon(RhythmIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    )
                                                }

                                                item {
                                                    FilterChip(
                                                        onClick = {
                                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                            filterArtists = !filterArtists
                                                        },
                                                        label = {
                                                            Text("Artists (${searchedArtists.size})")
                                                        },
                                                        selected = filterArtists,
                                                        leadingIcon = if (filterArtists) {
                                                            { Icon(RhythmIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    )
                                                }

                                                item {
                                                    FilterChip(
                                                        onClick = {
                                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                            filterPlaylists = !filterPlaylists
                                                        },
                                                        label = {
                                                            Text("Playlists (${searchedPlaylists.size})")
                                                        },
                                                        selected = filterPlaylists,
                                                        leadingIcon = if (filterPlaylists) {
                                                            { Icon(RhythmIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    )
                                                }
                                            }

                                            if (searchQuery.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (totalResults == 0) "No results found" else "$totalResults results found",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (totalResults == 0)
                                                            MaterialTheme.colorScheme.error
                                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = if (totalResults == 0) FontWeight.Medium else FontWeight.Normal
                                                    )

                                                    if (totalResults > 0) {
                                                        val activeFilters = listOf(
                                                            "Songs" to filterSongs,
                                                            "Albums" to filterAlbums,
                                                            "Artists" to filterArtists,
                                                            "Playlists" to filterPlaylists
                                                        ).count { it.second }

                                                        Text(
                                                            text = "$activeFilters filters active",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    } else {
                        // No results found
                        NoSearchResults(
                            searchQuery = searchQuery,
                            hasActiveFilters = filterSongs || filterAlbums || filterArtists || filterPlaylists,
                            onEnableFilters = {
                                showFilterOptions = true
                                filterSongs = true
                                filterAlbums = true
                                filterArtists = true
                                filterPlaylists = true
                            }
                        )
                    }
                }
            )

            if (!isSearchActive) {
                val recommendedSongs = remember(viewModel) {
                    viewModel.getRecommendedSongs().take(4)
                }
                DefaultSearchContent(
                    searchHistory = searchHistory,
                    recentlyPlayed = recentlyPlayed,
                    recommendedSongs = recommendedSongs,
                    onSongClick = onSongClick,
                    onSearchQuerySelect = { query ->
                        searchQuery = query
                        isSearchActive = true
                    },
                    onAddSongToPlaylist = { song ->
                        selectedSong = song
                        showAddToPlaylistSheet = true
                    },
                    onClearSearchHistory = {
                        viewModel.clearSearchHistory()
                    }
                )
            }
        }
    }
    
    // Add-to-playlist bottom sheet
    if (showAddToPlaylistSheet && selectedSong != null) {
        AddToPlaylistBottomSheet(
            song = selectedSong!!,
            playlists = playlists,
            onDismissRequest = { showAddToPlaylistSheet = false },
            onAddToPlaylist = { playlist ->
                onAddSongToPlaylist(selectedSong!!, playlist.id ?: "")
                scope.launch {
                    addToPlaylistSheetState.hide()
                }.invokeOnCompletion {
                    if (!addToPlaylistSheetState.isVisible) {
                        showAddToPlaylistSheet = false
                    }
                }
            },
            onCreateNewPlaylist = {
                scope.launch { addToPlaylistSheetState.hide() }.invokeOnCompletion {
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
            onDismiss = { 
                scope.launch {
                    albumBottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!albumBottomSheetState.isVisible) {
                        showAlbumBottomSheet = false
                    }
                }
            },
            onSongClick = onSongClick,
            onPlayAll = { songs -> 
                // Play all songs from the album with proper sorting
                if (songs.isNotEmpty()) {
                    onSongClick(songs.first())
                } else {
                    onAlbumClick(selectedAlbum!!)
                }
            },
            onShufflePlay = { songs -> 
                // Play shuffled songs from the album
                if (songs.isNotEmpty()) {
                    val shuffled = songs.shuffled()
                    onSongClick(shuffled.first())
                } else {
                    onAlbumClick(selectedAlbum!!)
                }
            },  
            onAddToQueue = { song -> onSongClick(song) }, // For now, just play the song
            onAddSongToPlaylist = { song ->
                selectedSong = song
                showAddToPlaylistSheet = true
            },
            onPlayerClick = onPlayerClick,
            haptics = LocalHapticFeedback.current,
            sheetState = albumBottomSheetState
        )
    }
    
    // Artist bottom sheet
    if (showArtistBottomSheet && selectedArtist != null) {
        ArtistBottomSheet(
            artist = selectedArtist!!,
            songs = songs, // Pass all songs to filter within ArtistBottomSheet
            albums = albums, // Pass all albums to filter within ArtistBottomSheet
            onDismiss = { 
                scope.launch {
                    artistBottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!artistBottomSheetState.isVisible) {
                        showArtistBottomSheet = false
                    }
                }
            },
            onSongClick = onSongClick,
            onAlbumClick = onAlbumClick,
            onPlayAll = { onArtistClick(selectedArtist!!) }, // Use existing artist click handler
            onShufflePlay = { onArtistClick(selectedArtist!!) }, // Use existing artist click handler
            onAddToQueue = { song -> onSongClick(song) }, // For now, just play the song
            onAddSongToPlaylist = { song ->
                selectedSong = song
                showAddToPlaylistSheet = true
            },
            sheetState = artistBottomSheetState
        )
    }
    
    // Create playlist dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SearchResults(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    searchQuery: String,
    totalResults: Int,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onAlbumBottomSheetClick: (Album) -> Unit = {}, // New parameter
    onArtistBottomSheetClick: (Artist) -> Unit = {},
    onViewAllSongsClick: () -> Unit,
    filterSection: @Composable () -> Unit // Add this parameter
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Filter section
        item {
            filterSection()
        }

        // Results header with improved design
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(12.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$totalResults items found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Songs section
        if (songs.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RhythmIcons.Song,
                                contentDescription = "Songs",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Songs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${songs.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        songs.take(5).forEach { song ->
                            SearchSongItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onAddToPlaylist = { onAddSongToPlaylist(song) },
                                haptics = haptics
                            )
                        }
                    }

                    if (songs.size > 5) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .clickable {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    onViewAllSongsClick()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "View All Songs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "See all ${songs.size} songs",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = RhythmIcons.Forward,
                                    contentDescription = "View all",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Albums section
        if (albums.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RhythmIcons.Album,
                                contentDescription = "Albums",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Albums",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${albums.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(albums.take(10)) { album ->
                            SearchAlbumItem(
                                album = album,
                                onClick = { onAlbumBottomSheetClick(album) } // Use the new lambda
                            )
                        }
                    }
                }
            }
        }
        
        // Artists section
        if (artists.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RhythmIcons.Artist,
                                contentDescription = "Artists",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Artists",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${artists.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(artists.take(10)) { artist ->
                            SearchArtistItem(
                                artist = artist,
                                onClick = {
                                    onArtistBottomSheetClick(artist)
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Playlists section
        if (playlists.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RhythmIcons.PlaylistFilled,
                                contentDescription = "Playlists",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Playlists",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${playlists.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(playlists.take(10)) { playlist ->
                            SearchPlaylistItem(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) }
                            )
                        }
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
    onClearSearchHistory: () -> Unit = {},
    recentlyPlayed: List<Song> = emptyList(),
    focusSongs: List<Song> = emptyList(),
    energeticSongs: List<Song> = emptyList(),
    relaxingSongs: List<Song> = emptyList(),
    onSongClick: (Song) -> Unit = {},
    onArtistBottomSheetClick: (Artist) -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
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
        
        // Mood & Moments and Recently Played (shared design)
        if (recentlyPlayed.isNotEmpty()) {
            item {
                RecentlyPlayedSection(recentlyPlayed = recentlyPlayed, onSongClick = onSongClick)
            }
        } else {
            // Empty state for recently played in SearchBrowseContent
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Queue,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No Recently Played",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Songs you play will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Add bottom spacing
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun RecentSearchItem(
    searchTerm: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
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
    onAddToPlaylist: (Song) -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art with enhanced styling
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.size(56.dp)
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
            
            // Song info with improved spacing
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
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Add to playlist button with enhanced styling
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onAddToPlaylist(song)
                },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.AddToPlaylist,
                    contentDescription = "Add to playlist",
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
    val haptics = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                onClick()
            }),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    .clip(RoundedCornerShape(16.dp))
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
    val haptics = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                onClick()
            }),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artist image
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(96.dp)
                    .padding(4.dp)
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

@Composable
fun GenreCard(
    genre: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        modifier = Modifier.clickable(onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        })
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
    val haptics = LocalHapticFeedback.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .width(150.dp)
            .padding(vertical = 6.dp), // Added vertical padding for consistency
        shape = RoundedCornerShape(20.dp), // Increased radius
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Playlist artwork
            Surface( // Use Surface for better elevation and shape control
                modifier = Modifier.size(68.dp), // Increased size
                shape = RoundedCornerShape(25.dp), // Increased radius
                color = MaterialTheme.colorScheme.primaryContainer // Use primary container color
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
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .apply(ImageUtils.buildImageRequest(
                                    playlist.artworkUri,
                                    playlist.name,
                                    context.cacheDir,
                                    M3PlaceholderType.PLAYLIST
                                ))
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Use proper playlist icon from RhythmIcons
                        Icon(
                            imageVector = RhythmIcons.PlaylistFilled, // Using the proper playlist icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer, // Use onPrimaryContainer color
                            modifier = Modifier.size(44.dp) // Increased icon size
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium, // Changed to titleMedium
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "${playlist.songs.size} songs",
                style = MaterialTheme.typography.bodyMedium, // Changed to bodyMedium
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Use onSurfaceVariant color
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
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clickable(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                onClick()
            })
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
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                onClick()
            })
    ) {
        // Album art with Material 3 card styling
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .apply(ImageUtils.buildImageRequest(
                            album.artworkUri,
                            album.title,
                            LocalContext.current.cacheDir,
                            M3PlaceholderType.ALBUM
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
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
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .clickable {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
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
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ---------- Shared composables copied from NewHomeScreen ----------
@Composable
private fun RecentlyPlayedSection(
    recentlyPlayed: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = RhythmIcons.Queue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                
                // Show count badge
                if (recentlyPlayed.size > 8) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${recentlyPlayed.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(recentlyPlayed.take(8)) { song ->
                    EnhancedRecentChip(song = song, onClick = { onSongClick(song) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedRecentChip(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: (Song) -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.surface
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
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DefaultSearchContent(
    searchHistory: List<String>,
    recentlyPlayed: List<Song>,
    recommendedSongs: List<Song>,
    onSongClick: (Song) -> Unit,
    onSearchQuerySelect: (String) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onClearSearchHistory: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current // Get the HapticFeedback instance
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search History with improved design
        item {
            if (searchHistory.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = RhythmIcons.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                            
                            FilledTonalButton(
                                onClick = onClearSearchHistory,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(searchHistory.take(6)) { query ->
                                Card(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                        onSearchQuerySelect(query)
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = RhythmIcons.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = query,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty state card for no recent searches
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No Recent Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Your search history will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Recently Played using the NewHomeScreen widget style
        if (recentlyPlayed.isNotEmpty()) {
            item {
                RecentlyPlayedSection(
                    recentlyPlayed = recentlyPlayed,
                    onSongClick = onSongClick
                )
            }
        } else {
            // Empty state for recently played
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Queue,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No Recently Played",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Songs you play will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        if (recommendedSongs.isNotEmpty()) {
            item {
                RecommendedForYouSection(
                    songs = recommendedSongs,
                    onSongClick = onSongClick
                )
            }
        }
    }
}

@Composable
private fun NoSearchResults(
    searchQuery: String,
    hasActiveFilters: Boolean,
    onEnableFilters: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val animatedSize by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 100f
            ),
            label = "iconAnimation"
        )
        
        // Enhanced empty state with gradient background
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = RhythmIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(60.dp * animatedSize)
                        .graphicsLayer(
                            scaleX = animatedSize,
                            scaleY = animatedSize
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (hasActiveFilters)
                "No Results Found"
            else
                "All Filters Disabled",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = if (hasActiveFilters)
                "We couldn't find anything matching \"$searchQuery\". Try adjusting your search terms or filters."
            else
                "Enable at least one filter to search your music library.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
        )
        
        if (!hasActiveFilters) {
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    onEnableFilters()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Filters", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💡 Search Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val tips = listOf(
                        "Try different keywords",
                        "Check your spelling",
                        "Use fewer words",
                        "Try searching by artist or album"
                    )
                    
                    tips.forEach { tip ->
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
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
    val haptics = LocalHapticFeedback.current
    
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                onClick()
            })
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
private fun MoodPlaylistCard(
    title: String,
    subtitle: String,
    emoji: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        if (songs.isNotEmpty()) onSongClick(songs.first())
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (songs.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(songs.take(5)) { song ->
                        Card(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                onSongClick(song)
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.width(120.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .apply(ImageUtils.buildImageRequest(
                                            song.artworkUri,
                                            song.title,
                                            LocalContext.current.cacheDir,
                                            M3PlaceholderType.TRACK
                                        ))
                                        .build(),
                                    contentDescription = song.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
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
            }
        }
    }
}

@Composable
fun AllSongsPage(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onBack: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                onBack()
            }) {
                Icon(RhythmIcons.Back, contentDescription = "Back")
            }
            Text(
                text = "All Songs (${songs.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                AnimateIn {
                    SearchSongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onAddToPlaylist = { onAddSongToPlaylist(song) },
                        haptics = haptics // Pass haptics to SearchSongItem
                    )
                }
            }
        }
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
