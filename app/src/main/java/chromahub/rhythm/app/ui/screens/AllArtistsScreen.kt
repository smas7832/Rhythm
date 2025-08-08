package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import chromahub.rhythm.app.ui.components.RhythmIcons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.ArtistViewType
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.components.M3ExpressiveCircularIndicator // Added import
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.ArtistCollaborationUtils
import chromahub.rhythm.app.util.HapticUtils
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllArtistsScreen(
    artists: List<Artist>,
    songs: List<Song>,
    albums: List<Album>,
    onBackClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSearchClick: () -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appSettings = remember { chromahub.rhythm.app.data.AppSettings.getInstance(context) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // State for artist bottom sheet
    var showArtistSheet by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val artistSheetState = rememberModalBottomSheetState()

    // State for sorting
    var sortedArtists by remember(artists) { mutableStateOf(artists.sortedBy { it.name }) }
    var showSortOptions by remember { mutableStateOf(false) }
    var currentSortOption by remember { mutableStateOf(ArtistSortOption.NAME_ASC) }
    var isLoading by remember { mutableStateOf(false) }

    // State for view mode (grid or list) - get from settings
    val artistViewType by appSettings.artistViewType.collectAsState()
    val isGridView = artistViewType == chromahub.rhythm.app.data.ArtistViewType.GRID

    // Apply sorting whenever the sort option changes or artists list changes
    LaunchedEffect(artists, songs, currentSortOption) {
        isLoading = true
        try {
            // Add a small delay to show loading state for quick operations
            kotlinx.coroutines.delay(300)
            
            // Use the centralized utility to extract individual artists from collaborations
            val processedArtists = ArtistCollaborationUtils.extractIndividualArtists(artists, songs)

            // Apply sorting to the processed artists
            sortedArtists = when (currentSortOption) {
                ArtistSortOption.NAME_ASC -> processedArtists.sortedBy { it.name.lowercase() }
                ArtistSortOption.NAME_DESC -> processedArtists.sortedByDescending { it.name.lowercase() }
                ArtistSortOption.TRACK_COUNT_DESC -> processedArtists.sortedByDescending { it.numberOfTracks }
                ArtistSortOption.ALBUM_COUNT_DESC -> processedArtists.sortedByDescending { it.numberOfAlbums }
            }
        } finally {
            isLoading = false
        }
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
                showArtistSheet = false
            },
            onShufflePlay = {
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
                        text = "Artists",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = currentFontSize,
                            fontWeight = currentFontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                navigationIcon = {
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onBackClick()
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
                actions = {
                    // Search icon
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onSearchClick()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search"
                        )
                    }
                    // View toggle icon
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            val newViewType = if (isGridView) {
                                chromahub.rhythm.app.data.ArtistViewType.LIST
                            } else {
                                chromahub.rhythm.app.data.ArtistViewType.GRID
                            }
                            appSettings.setArtistViewType(newViewType)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (isGridView) RhythmIcons.List else RhythmIcons.AppsGrid,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View"
                        )
                    }
                    // Sort/Filter icon
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            showSortOptions = true
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort Artists"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Show loading indicator while filtering
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            M3ExpressiveCircularIndicator(
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing Artists...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Artists grid/list
                    if (sortedArtists.isNotEmpty()) {
                        // Artists Section Header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
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
                                            imageVector = RhythmIcons.Artist,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Your Artists",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    
                                    Text(
                                        text = "${sortedArtists.size} ${if (sortedArtists.size == 1) "artist" else "artists"}",
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
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            if (isGridView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(sortedArtists, key = { it.id }) { artist ->
                                        AnimateIn {
                                            AllArtistsCard(
                                                artist = artist,
                                                onClick = {
                                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                    selectedArtist = artist
                                                    showArtistSheet = true
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(sortedArtists, key = { it.id }) { artist ->
                                        AnimateIn {
                                            ArtistListItem(
                                                artist = artist,
                                                onClick = {
                                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                    selectedArtist = artist
                                                    showArtistSheet = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Artist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No artists found",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Add some music to see your artists here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sort options bottom sheet
    if (showSortOptions) {
        ModalBottomSheet(
            onDismissRequest = { showSortOptions = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sort Artists By",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ArtistSortOption.entries.forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                currentSortOption = sortOption
                                showSortOptions = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortOption.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (currentSortOption == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (currentSortOption == sortOption) {
                            Icon(
                                imageVector = RhythmIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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

    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 50),
        label = "alpha"
    )

    val scale by androidx.compose.animation.core.animateFloatAsState(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllArtistsCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptics = LocalHapticFeedback.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Use a slightly higher surface color
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp, // Add a subtle elevation
            pressedElevation = 4.dp // Increase elevation on press
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artist image with play button overlay
            Box(
                modifier = Modifier.size(120.dp)
            ) {
                // Artist circular image
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer, // Use a slightly lower surface color for the image background
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    shadowElevation = 0.dp
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
                        // Fallback to a placeholder if artwork is null
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
                
                // Play button overlay positioned at bottom right
                Surface(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        viewModel.playArtist(artist)
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp, // Increase shadow for play button
                    modifier = Modifier
                        .size(48.dp) // Slightly larger play button
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = "Play ${artist.name}",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp) // Larger icon
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // Fixed height to prevent irregular card sizes
            ) {
                // Artist name
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium, // Larger title
                    fontWeight = FontWeight.Bold, // Bolder font
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Artist info row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Track count
                    Icon(
                        imageVector = RhythmIcons.Music.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, // Use onSurfaceVariant for icons
                        modifier = Modifier.size(18.dp) // Slightly larger icon
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // Increased spacing
                    Text(
                        text = "${artist.numberOfTracks}",
                        style = MaterialTheme.typography.bodySmall, // Larger text
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Use onSurfaceVariant for text
                    )
                    
                    if (artist.numberOfAlbums > 0) {
                        Spacer(modifier = Modifier.width(16.dp)) // Increased spacing
                        
                        // Album count
                        Icon(
                            imageVector = RhythmIcons.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, // Use onSurfaceVariant for icons
                            modifier = Modifier.size(18.dp) // Slightly larger icon
                        )
                        Spacer(modifier = Modifier.width(6.dp)) // Increased spacing
                        Text(
                            text = "${artist.numberOfAlbums}",
                            style = MaterialTheme.typography.bodySmall, // Larger text
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Use onSurfaceVariant for text
                        )
                    }
                }
            }
        }
    }
}

enum class ArtistSortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    TRACK_COUNT_DESC("Songs (High to Low)"),
    ALBUM_COUNT_DESC("Albums (High to Low)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistListItem(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val haptics = LocalHapticFeedback.current

    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist circular image
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                shadowElevation = 0.dp
            ) {
                if (artist.artworkUri != null) {
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
                } else {
                    // Fallback to a placeholder if artwork is null
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Artist name
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Artist info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RhythmIcons.Music.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${artist.numberOfTracks} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (artist.numberOfAlbums > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = RhythmIcons.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${artist.numberOfAlbums} albums",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Play button
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    viewModel.playArtist(artist)
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play ${artist.name}",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
