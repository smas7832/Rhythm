@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package chromahub.rhythm.app.ui.screens

import android.widget.Toast
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import java.io.File
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import kotlin.collections.sortedBy
import kotlin.collections.mutableListOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.*
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.net.Uri
import android.util.Log
import chromahub.rhythm.app.util.PlaylistImportExportUtils
import chromahub.rhythm.app.util.AppRestarter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.AlbumViewType
import chromahub.rhythm.app.data.ArtistViewType
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.screens.AddToPlaylistBottomSheet
import chromahub.rhythm.app.ui.components.CreatePlaylistDialog
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.components.BulkPlaylistExportDialog
import chromahub.rhythm.app.ui.components.PlaylistImportDialog
import chromahub.rhythm.app.ui.components.PlaylistOperationProgressDialog
import chromahub.rhythm.app.ui.components.PlaylistOperationResultDialog
import chromahub.rhythm.app.ui.screens.SongInfoBottomSheet
import chromahub.rhythm.app.ui.screens.PlaylistManagementBottomSheet
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import androidx.compose.material3.ListItemDefaults
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material.icons.rounded.ArrowCircleUp
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader


enum class LibraryTab { SONGS, PLAYLISTS, ALBUMS, ARTISTS, EXPLORER }

@Composable
fun LibraryScreen(
    songs: List<Song>,
    albums: List<Album>,
    playlists: List<Playlist>,
    artists: List<Artist>,
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onSongClick: (Song) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddPlaylist: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
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
    musicViewModel: MusicViewModel, // Add MusicViewModel as a parameter
    onExportAllPlaylists: ((PlaylistImportExportUtils.PlaylistExportFormat, Boolean, Uri?, (Result<String>) -> Unit) -> Unit)? = null,
    onImportPlaylist: ((Uri, (Result<String>) -> Unit, (() -> Unit)?) -> Unit)? = null,
    onRestartApp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val tabOrder by appSettings.libraryTabOrder.collectAsState()
    
    // Map tab IDs to display names
    val tabs = remember(tabOrder) {
        tabOrder.map { tabId ->
            when (tabId) {
                "SONGS" -> "Songs"
                "PLAYLISTS" -> "Playlists"
                "ALBUMS" -> "Albums"
                "ARTISTS" -> "Artists"
                "EXPLORER" -> "Explorer"
                else -> tabId
            }
        }
    }
    
    // Find initial tab index based on the reordered tabs
    val initialTabIndex = remember(tabOrder, initialTab) {
        val tabId = initialTab.name
        tabOrder.indexOf(tabId).takeIf { it >= 0 } ?: 0
    }
    
    var selectedTabIndex by rememberSaveable { mutableStateOf(initialTabIndex) }
    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { tabs.size }
    val tabRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Auto-scroll tab row to show selected tab when returning to this screen
    LaunchedEffect(selectedTabIndex) {
        tabRowState.animateScrollToItem(selectedTabIndex)
    }
    
    // Dialog and bottom sheet states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showAlbumBottomSheet by remember { mutableStateOf(false) }
    var showArtistBottomSheet by remember { mutableStateOf(false) }
    var showSongInfoSheet by remember { mutableStateOf(false) }
    var showBulkExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showOperationProgress by remember { mutableStateOf(false) }
    var operationInProgress by remember { mutableStateOf("") }
    var operationResult by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    
    // Import/Export related state
    var operationProgressText by remember { mutableStateOf("") }
    var operationError by remember { mutableStateOf<String?>(null) }
    var showExportResultDialog by remember { mutableStateOf(false) }
    var exportResultsData by remember { mutableStateOf<List<Pair<String, Boolean>>?>(null) }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    
    // Explorer reload trigger
    var explorerReloadTrigger by remember { mutableStateOf(0) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    val albumBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val artistBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    // TopAppBar scroll behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    // FAB visibility based on scroll
    val fabVisibility by remember {
        derivedStateOf {
            scrollBehavior.state.collapsedFraction < 0.5f
        }
    }

    // FAB menu state
    var showPlaylistFabMenu by remember { mutableStateOf(false) }
    var showPlaylistManagementSheet by remember { mutableStateOf(false) }

    // Function to close FAB menu from other places
    val closeFabMenu = {
        showPlaylistFabMenu = false
    }

    // Handle FAB menu item clicks - close menu after action
    val onCreatePlaylistFromFab: () -> Unit = {
        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
        showCreatePlaylistDialog = true
        showPlaylistFabMenu = false
    }

    val onImportPlaylistFromFab: (() -> Unit)? = if (onImportPlaylist != null) {
        {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            showImportDialog = true
            showPlaylistFabMenu = false
        }
    } else null

    val onExportPlaylistsFromFab: (() -> Unit)? = if (onExportAllPlaylists != null) {
        {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            showBulkExportDialog = true
            showPlaylistFabMenu = false
        }
    } else null

    // Lambda to pass to PlaylistFabMenu for import
    val onImportPlaylistForFab: (() -> Unit)? = if (onImportPlaylist != null) {
        {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            showImportDialog = true
        }
    } else null

    // Lambda to pass to PlaylistFabMenu for manage
    val onManagePlaylists: (() -> Unit) = {
        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
        showPlaylistManagementSheet = true
    }

    // Sync tabs with pager - only animate when tab button is clicked
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }
    


    // Update selectedTabIndex when pager settles on a new page (handles swiping)
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress && selectedTabIndex != pagerState.currentPage) {
            selectedTabIndex = pagerState.currentPage
            // Auto-scroll tab buttons to show selected tab
            tabRowState.animateScrollToItem(pagerState.currentPage)
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
        // Get the latest version of the song from the songs list
        val displaySong = songs.find { it.id == selectedSong!!.id } ?: selectedSong
        
        SongInfoBottomSheet(
            song = displaySong!!,
            onDismiss = { showSongInfoSheet = false },
            appSettings = appSettings,
            onEditSong = { title, artist, album, genre, year, trackNumber ->
                // Use the ViewModel's new metadata saving function with callbacks
                musicViewModel.saveMetadataChanges(
                    song = displaySong!!,
                    title = title,
                    artist = artist,
                    album = album,
                    genre = genre,
                    year = year,
                    trackNumber = trackNumber,
                    onSuccess = { fileWriteSucceeded ->
                        if (fileWriteSucceeded) {
                            Toast.makeText(context, "Metadata saved successfully to file!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Metadata updated in library only. File write failed - check permissions.", Toast.LENGTH_LONG).show()
                        }
                        // Don't close the sheet - let the user see the updated info
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            }
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
            haptics = haptics,
            onPlayNext = { song -> musicViewModel.playNext(song) },
            onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
            favoriteSongs = musicViewModel.favoriteSongs.collectAsState().value,
            onShowSongInfo = { song ->
                selectedSong = song
                showSongInfoSheet = true
            },
            onAddToBlacklist = { song ->
                appSettings.addToBlacklist(song.id)
            }
        )
    }
    
    // Artist bottom sheet
    if (showArtistBottomSheet && selectedArtist != null) {
        ArtistBottomSheet(
            artist = selectedArtist!!,
            onDismiss = { showArtistBottomSheet = false },
            onSongClick = onSongClick,
            onAlbumClick = onAlbumClick,
            onPlayAll = { songs ->
                if (songs.isNotEmpty()) {
                    onPlayQueue(songs)
                }
            },
            onShufflePlay = { songs ->
                if (songs.isNotEmpty()) {
                    onShuffleQueue(songs)
                }
            },
            onAddToQueue = onAddToQueue,
            onAddSongToPlaylist = { song ->
                selectedSong = song
                scope.launch {
                    artistBottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!artistBottomSheetState.isVisible) {
                        showArtistBottomSheet = false
                        showAddToPlaylistSheet = true
                    }
                }
            },
            onPlayerClick = onPlayerClick,
            sheetState = artistBottomSheetState,
            haptics = haptics,
            onPlayNext = { song -> musicViewModel.playNext(song) },
            onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
            favoriteSongs = musicViewModel.favoriteSongs.collectAsState().value,
            onShowSongInfo = { song ->
                selectedSong = song
                showSongInfoSheet = true
            },
            onAddToBlacklist = { song ->
                appSettings.addToBlacklist(song.id)
            }
        )
    }

    // Playlist Management bottom sheet
    if (showPlaylistManagementSheet) {
        PlaylistManagementBottomSheet(
            onDismiss = { showPlaylistManagementSheet = false },
            playlists = playlists,
            musicViewModel = musicViewModel,
            onCreatePlaylist = { showCreatePlaylistDialog = true },
            onDeletePlaylist = { playlist ->
//                musicViewModel.deletePlaylist(playlist)
            }
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
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            // If on Explorer tab, reload explorer; otherwise, trigger media scan
                            if (tabOrder.getOrNull(selectedTabIndex) == "EXPLORER") {
                                explorerReloadTrigger++
                            } else {
                                onRefreshClick()
                            }
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
                    // Tab-specific actions moved from section headers
                    when (tabOrder.getOrNull(selectedTabIndex)) {
                        "ALBUMS" -> {
                            // Enhanced Album view toggle
                            val albumViewType by appSettings.albumViewType.collectAsState()
                            
                            // Animation for button press
                            val buttonScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "albumToggleScale"
                            )
                            
                            FilledTonalIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    val newViewType = if (albumViewType == AlbumViewType.LIST) AlbumViewType.GRID else AlbumViewType.LIST
                                    appSettings.setAlbumViewType(newViewType)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = buttonScale
                                        scaleY = buttonScale
                                    }
                            ) {
                                Icon(
                                    imageVector = if (albumViewType == AlbumViewType.LIST) Icons.Default.GridView else Icons.AutoMirrored.Rounded.ViewList,
                                    contentDescription = if (albumViewType == AlbumViewType.LIST) "Switch to Grid View" else "Switch to List View",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        "ARTISTS" -> {
                            // Enhanced Artist view toggle  
                            val artistViewType by appSettings.artistViewType.collectAsState()
                            
                            // Animation for button press
                            val buttonScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "artistToggleScale"
                            )
                            
                            FilledTonalIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    val newViewType = if (artistViewType == ArtistViewType.LIST) ArtistViewType.GRID else ArtistViewType.LIST
                                    appSettings.setArtistViewType(newViewType)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = buttonScale
                                        scaleY = buttonScale
                                    }
                            ) {
                                Icon(
                                    imageVector = if (artistViewType == ArtistViewType.LIST) Icons.Default.GridView else Icons.AutoMirrored.Rounded.ViewList,
                                    contentDescription = if (artistViewType == ArtistViewType.LIST) "Switch to Grid View" else "Switch to List View",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        

                    }
                    
                    // Sort dropdown like AlbumBottomSheet (only show for Songs and Albums)
                    val currentTabId = tabOrder.getOrNull(selectedTabIndex)
                    if (currentTabId == "SONGS" || currentTabId == "ALBUMS") {
                        var showSortMenu by remember { mutableStateOf(false) }
                        var pendingSortOrder by remember { mutableStateOf<MusicViewModel.SortOrder?>(null) }
                        
                        // Clear pending sort order when actual sort order changes
                        LaunchedEffect(sortOrder) {
                            pendingSortOrder = null
                        }
                        
                        Box {
                        // Enhanced sort button
                        val sortButtonScale by animateFloatAsState(
                            targetValue = if (showSortMenu) 0.95f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "sortButtonScale"
                        )
                        
                        FilledTonalButton(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showSortMenu = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.graphicsLayer {
                                scaleX = sortButtonScale
                                scaleY = sortButtonScale
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sort order text
                            val sortText = when (sortOrder) {
                                MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.TITLE_DESC -> "Title"
                                MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.ARTIST_DESC -> "Artist"
                                MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_ADDED_DESC -> "Date Added"
                                MusicViewModel.SortOrder.DATE_MODIFIED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> "Date Modified"
                            }

                            Text(
                                text = sortText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            val sortArrowIcon = when (sortOrder) {
                                MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_ASC -> Icons.Default.ArrowUpward
                                MusicViewModel.SortOrder.TITLE_DESC, MusicViewModel.SortOrder.ARTIST_DESC, MusicViewModel.SortOrder.DATE_ADDED_DESC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> Icons.Default.ArrowDownward
                            }
                            
                            Icon(
                                imageVector = sortArrowIcon,
                                contentDescription = if (sortOrder.name.endsWith("_ASC")) "Ascending" else "Descending",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            MusicViewModel.SortOrder.values().forEach { order ->
                                val isSelected = (pendingSortOrder ?: sortOrder) == order
                                Surface(
                                    color = if (isSelected) 
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
                                                    MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.TITLE_DESC -> "Title"
                                                    MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.ARTIST_DESC -> "Artist"
                                                    MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_ADDED_DESC -> "Date Added"
                                                    MusicViewModel.SortOrder.DATE_MODIFIED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> "Date Modified"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (order) {
                                                    MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.TITLE_DESC -> Icons.Filled.SortByAlpha
                                                    MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.ARTIST_DESC -> Icons.Filled.Person
                                                    MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_ADDED_DESC -> Icons.Filled.DateRange
                                                    MusicViewModel.SortOrder.DATE_MODIFIED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> Icons.Filled.EditCalendar
                                                },
                                                contentDescription = null,
                                                tint = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        trailingIcon = {
                                            when (order) {
                                                MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_ASC -> {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowUpward,
                                                        contentDescription = "Ascending",
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                MusicViewModel.SortOrder.TITLE_DESC, MusicViewModel.SortOrder.ARTIST_DESC, MusicViewModel.SortOrder.DATE_ADDED_DESC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDownward,
                                                        contentDescription = "Descending",
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                else -> {}
                                            }
                                        },
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                            pendingSortOrder = order
                                            showSortMenu = false
                                            // Set the specific sort order instead of cycling
                                            if (sortOrder != order) {
                                                musicViewModel.setSortOrder(order)
                                            }
                                        },
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = if (isSelected) 
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
            // Only show FAB on playlists tab
            if (tabOrder.getOrNull(selectedTabIndex) == "PLAYLISTS") {
                PlaylistFabMenu(
                    expanded = showPlaylistFabMenu,
                    onCreatePlaylist = onCreatePlaylistFromFab,
                    onImportPlaylist = onImportPlaylistFromFab,
                onExportPlaylists = onExportPlaylistsFromFab,
//                    onManagePlaylists = onManagePlaylists
                    haptics = haptics // Pass haptics to PlaylistFabMenu
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Scrollable Navigation Buttons
            LazyRow(
                state = tabRowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(tabs.size) { index ->
                    val isSelected = selectedTabIndex == index
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "buttonScale"
                    )
                    
                    val animatedContainerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "buttonContainerColor"
                    )
                    
                    val animatedContentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "buttonContentColor"
                    )

                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            selectedTabIndex = index
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                                tabRowState.animateScrollToItem(index)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedContainerColor,
                            contentColor = animatedContentColor
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                            },
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (tabOrder.getOrNull(index)) {
                                    "SONGS" -> RhythmIcons.Relax
                                    "PLAYLISTS" -> RhythmIcons.PlaylistFilled
                                    "ALBUMS" -> RhythmIcons.Music.Album
                                    "ARTISTS" -> RhythmIcons.Artist
                                    "EXPLORER" -> Icons.Default.Folder
                                    else -> RhythmIcons.Music.Song
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = tabs[index],
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Edit button at the end to open LibraryTabReorderBottomSheet
                item {
                    var showLibraryTabOrderSheet by remember { mutableStateOf(false) }
                    
                    FilledTonalIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showLibraryTabOrderSheet = true
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Reorder tabs",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (showLibraryTabOrderSheet) {
                        LibraryTabOrderBottomSheet(
                            onDismiss = { showLibraryTabOrderSheet = false },
                            appSettings = appSettings,
                            haptics = haptics
                        )
                    }
                }
            }
            
            // Single Big Card Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 0.dp
            ) {
                // Content with animation
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) { page ->
                    // Dynamically show tab content based on tab order
                    when (tabOrder.getOrNull(page)) {
                        "SONGS" -> {
                            // Sort songs according to current sort order
                            val sortedSongs = remember(songs, sortOrder) {
                                when (sortOrder) {
                                    MusicViewModel.SortOrder.TITLE_ASC -> songs.sortedBy { it.title.lowercase() }
                                    MusicViewModel.SortOrder.TITLE_DESC -> songs.sortedByDescending { it.title.lowercase() }
                                    MusicViewModel.SortOrder.ARTIST_ASC -> songs.sortedBy { it.artist.lowercase() }
                                    MusicViewModel.SortOrder.ARTIST_DESC -> songs.sortedByDescending { it.artist.lowercase() }
                                    MusicViewModel.SortOrder.DATE_ADDED_ASC -> songs.sortedBy { it.dateAdded }
                                    MusicViewModel.SortOrder.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAdded }
                                    MusicViewModel.SortOrder.DATE_MODIFIED_ASC -> songs.sortedBy { it.dateAdded } // Song doesn't have dateModified, use dateAdded
                                    MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> songs.sortedByDescending { it.dateAdded } // Song doesn't have dateModified, use dateAdded
                                }
                            }
                            SingleCardSongsContent(
                            songs = sortedSongs,
                            albums = albums,
                            artists = artists,
                            onSongClick = onSongClick,
                            onAddToPlaylist = { song ->
                                selectedSong = song
                                showAddToPlaylistSheet = true
                            },
                            onAddToQueue = onAddToQueue,
                            onPlayNext = { song -> musicViewModel.playNext(song) },
                            onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
                            favoriteSongs = musicViewModel.favoriteSongs.collectAsState().value,
                            onGoToArtist = onArtistClick,
                            onGoToAlbum = onAlbumClick,
                            onShowSongInfo = { song ->
                                selectedSong = song
                                showSongInfoSheet = true
                            },
                            onAddToBlacklist = { song ->
                                appSettings.addToBlacklist(song.id)
                            },
                            onPlayQueue = onPlayQueue,
                            onShuffleQueue = onShuffleQueue,
                            haptics = haptics
                        )
                        }
                        "PLAYLISTS" -> SingleCardPlaylistsContent(
                            playlists = playlists,
                            onPlaylistClick = onPlaylistClick,
                            haptics = haptics,
                            onCreatePlaylist = { showCreatePlaylistDialog = true },
                            onImportPlaylist = { showImportDialog = true },
                            onExportPlaylists = { showBulkExportDialog = true }
                        )
                        "ALBUMS" -> SingleCardAlbumsContent(
                            albums = albums,
                            onAlbumClick = onAlbumClick,
                            onSongClick = onSongClick,
                            onAlbumBottomSheetClick = { album ->
                                selectedAlbum = album
                                showAlbumBottomSheet = true
                            },
                            haptics = haptics,
                            appSettings = appSettings
                        )
                        "ARTISTS" -> SingleCardArtistsContent(
                            artists = artists,
                            onArtistClick = { artist ->
                                selectedArtist = artist
                                showArtistBottomSheet = true
                            },
                            haptics = haptics
                        )
                        "EXPLORER" -> SingleCardExplorerContent(
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
                            onPlayQueue = onPlayQueue,
                            onShuffleQueue = onShuffleQueue,
                            haptics = haptics,
                            appSettings = appSettings,
                            reloadTrigger = explorerReloadTrigger,
                            onCreatePlaylist = onCreatePlaylist,
                            musicViewModel = musicViewModel
                        )
                    }
                }
            }
        }
    }
    
    // Playlist import/export dialogs
    if (showBulkExportDialog && onExportAllPlaylists != null) {
        BulkPlaylistExportDialog(
            playlistCount = playlists.size,
            onDismiss = { 
                showBulkExportDialog = false
                operationError = null
            },
            onExport = { format, includeDefault ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting playlists..."
                
                onExportAllPlaylists(format, includeDefault, null) { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success will be shown via snackbar from navigation layer
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Export failed"
                        }
                    )
                }
            },
            onExportToCustomLocation = { format, includeDefault, directoryUri ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting playlists to selected location..."
                
                onExportAllPlaylists(format, includeDefault, directoryUri) { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success will be shown via snackbar from navigation layer
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Export failed"
                        }
                    )
                }
            }
        )
    }
    
    if (showImportDialog && onImportPlaylist != null) {
        PlaylistImportDialog(
            onDismiss = { 
                showImportDialog = false
                operationError = null
            },
            onImport = { uri, onResult, onRestartRequired ->
                showImportDialog = false
                showOperationProgress = true
                operationProgressText = "Importing playlist..."
                onImportPlaylist(uri, { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            operationResult = Pair(message, true)
                            showRestartDialog = true
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Import failed"
                        }
                    )
                    onResult(result)
                }, onRestartRequired)
            }
        )
    }

    // App Restart Dialog
    if (showRestartDialog && onRestartApp != null) {
        chromahub.rhythm.app.ui.components.AppRestartDialog(
            onDismiss = { showRestartDialog = false },
            onRestart = {
                showRestartDialog = false
                onRestartApp()
            },
            onContinue = {
                showRestartDialog = false
                // Continue without restart
            }
        )
    }

    // Progress dialog for long operations
    if (showOperationProgress) {
        PlaylistOperationProgressDialog(
            operation = operationProgressText,
            onDismiss = {
                showOperationProgress = false
                operationProgressText = ""
            }
        )
    }
    
    // Simple success/error dialogs for now
    if (operationError != null) {
        AlertDialog(
            onDismissRequest = { operationError = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(operationError!!) },
            confirmButton = {
                Button(onClick = { operationError = null }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog to show import result and offer restart
    if (showImportResultDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = { showImportResultDialog = false; importResult = null },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Import Complete") },
            text = {
                val (count, message) = importResult!!
                Text("Successfully imported $count playlists.\n$message\n\nRestart the app to apply changes.")
            },
            confirmButton = {
                Button(onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showImportResultDialog = false
                    importResult = null
                    AppRestarter.restartApp(context)
                }) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart App")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showImportResultDialog = false
                    importResult = null
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Later")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SingleCardSongsContent(
    songs: List<Song>,
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit = {},
    onToggleFavorite: (Song) -> Unit = {},
    favoriteSongs: Set<String> = emptySet(),
    onGoToArtist: (Artist) -> Unit = {},
    onGoToAlbum: (Album) -> Unit = {},
    onShowSongInfo: (Song) -> Unit,
    onAddToBlacklist: (Song) -> Unit,
    onPlayQueue: (List<Song>) -> Unit = { _ -> },
    onShuffleQueue: (List<Song>) -> Unit = { _ -> },
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    // Define categories based on song properties - only show working filters
    val categories = remember(songs) {
        val allCategories = mutableListOf("All")

        // Extract actual music genres from songs (only non-empty, non-null genres)
        val genres = songs.mapNotNull { song ->
            song.genre?.takeIf { it.isNotBlank() }
        }.distinct().sorted()

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

        // Add duration-based categories only if they have songs
        val shortSongs = songs.filter { it.duration < 3 * 60 * 1000 }
        if (shortSongs.isNotEmpty()) allCategories.add("Short (< 3 min)")

        val mediumSongs = songs.filter { it.duration in (3 * 60 * 1000)..(5 * 60 * 1000) }
        if (mediumSongs.isNotEmpty()) allCategories.add("Medium (3-5 min)")

        val longSongs = songs.filter { it.duration > 5 * 60 * 1000 }
        if (longSongs.isNotEmpty()) allCategories.add("Long (> 5 min)")

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
                song.uri.toString().let { uri ->
                    uri.contains(".flac", ignoreCase = true) ||
                            uri.contains(".alac", ignoreCase = true) ||
                            uri.contains(".wav", ignoreCase = true)
                }
            }

            "High Quality" -> songs.filter { song ->
                song.uri.toString().let { uri ->
                    uri.contains(".m4a", ignoreCase = true) ||
                            (uri.contains(".mp3", ignoreCase = true) && song.duration > 0)
                }
            }

            "Standard" -> songs.filter { song ->
                song.uri.toString().let { uri ->
                    uri.contains(".mp3", ignoreCase = true) ||
                            uri.contains(".aac", ignoreCase = true) ||
                            uri.contains(".ogg", ignoreCase = true)
                }
            }

            "Favorites" -> songs.filter { song ->
                song.duration > 2 * 60 * 1000 && song.duration < 6 * 60 * 1000
            }

            else -> songs.filter { song ->
                // This handles genre filtering
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 96.dp // Space for floating button group (avoid overlap)
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Sticky Section Header
                stickyHeader {
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            ) {}
                        }
                    }
                }

                // Sticky Filter Chips
                if (categories.size > 1) {
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { category ->
                                    val isSelected = selectedCategory == category

                                    val containerColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipContainerColor"
                                    )
                                    val labelColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipLabelColor"
                                    )
                                    val borderColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.6f
                                        ),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipBorderColor"
                                    )
                                    val borderWidth by animateDpAsState(
                                        targetValue = if (isSelected) 2.dp else 1.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipBorderWidth"
                                    )
                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.05f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipScale"
                                    )

                                    FilterChip(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(
                                                context,
                                                haptics,
                                                HapticFeedbackType.LongPress
                                            )
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
                                        shape = RoundedCornerShape(50.dp),
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Songs Items
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
                            onPlayNext = { onPlayNext(song) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            isFavorite = favoriteSongs.contains(song.id),
                            onGoToArtist = { 
                                // Find the artist from the list
                                val artist = artists.find { it.name.equals(song.artist, ignoreCase = true) }
                                artist?.let { onGoToArtist(it) }
                            },
                            onGoToAlbum = { 
                                // Find the album from the list
                                val album = albums.find { 
                                    it.title.equals(song.album, ignoreCase = true) && 
                                    it.artist.equals(song.artist, ignoreCase = true)
                                }
                                album?.let { onGoToAlbum(it) }
                            },
                            onShowSongInfo = { onShowSongInfo(song) },
                            onAddToBlacklist = { onAddToBlacklist(song) },
                            haptics = haptics
                        )
                    }
                }
            }

            // Bottom Floating Button Group with animations
            AnimatedVisibility(
                visible = filteredSongs.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                BottomFloatingButtonGroup(
                    modifier = Modifier
                        .padding(bottom = 16.dp), // Simple fixed spacing from bottom
                    onPlayAll = {
                        HapticUtils.performHapticFeedback(
                            context,
                            haptics,
                            HapticFeedbackType.LongPress
                        )
                        onPlayQueue(filteredSongs)
                    },
                    onShuffle = {
                        HapticUtils.performHapticFeedback(
                            context,
                            haptics,
                            HapticFeedbackType.LongPress
                        )
                        onShuffleQueue(filteredSongs)
                    },
                    haptics = haptics
                )
            }
        }
    }
}

@Composable
fun SingleCardPlaylistsContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onCreatePlaylist: (() -> Unit)? = null,
    onImportPlaylist: (() -> Unit)? = null,
    onExportPlaylists: (() -> Unit)? = null
) {
    if (playlists.isEmpty()) {
        EmptyState(
            message = "No playlists yet\nCreate your first playlist using the + button",
            icon = RhythmIcons.Music.Playlist
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 16.dp // Simple spacing - Scaffold handles rest
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Sticky Section Header
            stickyHeader {
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
            }

            // Playlist Items
            items(
                items = playlists,
                key = { it.id }
            ) { playlist ->
                AnimateIn {
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        haptics = haptics
                    )
                }
            }
        }
    }
}

@Composable
fun SingleCardAlbumsContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumBottomSheetClick: (Album) -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    appSettings: AppSettings
) {
    val albumViewType by appSettings.albumViewType.collectAsState()

    if (albums.isEmpty()) {
        EmptyState(
            message = "No albums yet",
            icon = RhythmIcons.Music.Album
        )
    } else {
                        if (albumViewType == AlbumViewType.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp // Simple spacing - Scaffold handles rest
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sticky Section Header
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Your Albums",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${albums.size} ${if (albums.size == 1) "album" else "albums"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(60.dp),
                                shape = RoundedCornerShape(1.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            ) {}
                        }
                    }
                }                // Album Grid Items
                items(
                    items = albums,
                    key = { it.id }
                ) { album ->
                    AnimateIn {
                        AlbumGridItem(
                            album = album,
                            onClick = { onAlbumBottomSheetClick(album) },
                            onPlayClick = { onAlbumClick(album) },
                            haptics = haptics
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 16.dp // Simple spacing - Scaffold handles rest
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Sticky Section Header
                stickyHeader {
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
                                        imageVector = RhythmIcons.Music.Album,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Your Albums",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${albums.size} ${if (albums.size == 1) "album" else "albums"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(60.dp),
                                shape = RoundedCornerShape(1.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            ) {}
                        }
                    }
                }

                // Album List Items
                items(
                    items = albums,
                    key = { it.id }
                ) { album ->
                    AnimateIn {
                        LibraryAlbumItem(
                            album = album,
                            onClick = { onAlbumBottomSheetClick(album) },
                            onPlayClick = { onAlbumClick(album) },
                            haptics = haptics
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Deprecated("Use SingleCardSongsContent instead")
fun SongsTab(
    songs: List<Song>,
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit = {},
    onToggleFavorite: (Song) -> Unit = {},
    favoriteSongs: Set<String> = emptySet(),
    onGoToArtist: (Artist) -> Unit = {},
    onGoToAlbum: (Album) -> Unit = {},
    onShowSongInfo: (Song) -> Unit,
    onAddToBlacklist: (Song) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Define categories based on song properties - only show working filters
    val categories = remember(songs) {
        val allCategories = mutableListOf("All")
        
        // Extract actual music genres from songs
        val genres = songs.mapNotNull { song ->
            song.genre?.takeIf { it.isNotBlank() && it.lowercase() != "unknown" }
        }.distinct().sorted()
        
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
        
        // Add duration-based categories only if they have songs
        val shortSongs = songs.filter { it.duration < 3 * 60 * 1000 }
        if (shortSongs.isNotEmpty()) allCategories.add("Short (< 3 min)")
        
        val mediumSongs = songs.filter { it.duration in (3 * 60 * 1000)..(5 * 60 * 1000) }
        if (mediumSongs.isNotEmpty()) allCategories.add("Medium (3-5 min)")
        
        val longSongs = songs.filter { it.duration > 5 * 60 * 1000 }
        if (longSongs.isNotEmpty()) allCategories.add("Long (> 5 min)")
        
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
                            (uri.contains(
                                ".mp3",
                                ignoreCase = true
                            ) && song.duration > 0) // Proxy for quality
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

            "Unknown Genre" -> songs.filter { song ->
                song.genre.isNullOrBlank() || song.genre.lowercase() == "unknown"
            }

            else -> songs.filter { song ->
                // This handles genre filtering - check both song.genre and extended info
                val songGenre = song.genre?.trim()?.takeIf { it.isNotBlank() }
                songGenre?.equals(selectedCategory, ignoreCase = true) == true
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
                        bottom = 16.dp // Simple spacing - Scaffold handles rest
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
                                onPlayNext = { onPlayNext(song) },
                                onToggleFavorite = { onToggleFavorite(song) },
                                isFavorite = favoriteSongs.contains(song.id),
                                onGoToArtist = { 
                                    // Find the artist from the list
                                    val artist = artists.find { it.name.equals(song.artist, ignoreCase = true) }
                                    artist?.let { onGoToArtist(it) }
                                },
                                onGoToAlbum = { 
                                    // Find the album from the list
                                    val album = albums.find { 
                                        it.title.equals(song.album, ignoreCase = true) && 
                                        it.artist.equals(song.artist, ignoreCase = true)
                                    }
                                    album?.let { onGoToAlbum(it) }
                                },
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
@Deprecated("Use SingleCardPlaylistsContent instead")
fun PlaylistsTab(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
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
                        bottom = 16.dp // Simple spacing - Scaffold handles rest
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
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
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
                            bottom = 16.dp // Simple spacing - Scaffold handles rest
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
    onPlayNext: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    onGoToArtist: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
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
            Text(
                text = buildString {
                    append(song.artist)
                    append(" • ")
                    append(song.album)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showDropdown = true
                },
                modifier = Modifier
                    .size(width = 40.dp, height = 36.dp),
                shape = RoundedCornerShape(18.dp), // Pill shape like Android 16
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
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showDropdown = false
                },
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(5.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                // Play next
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Play next",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
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
                            onPlayNext()
                        }
                    )
                }

                // Add to queue
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add to queue",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Queue,
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
                            onAddToQueue()
                        }
                    )
                }

                // Toggle favorite
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isFavorite) "Remove from favorites" else "Add to favorites",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onToggleFavorite()
                        }
                    )
                }

                // Add to playlist
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add to playlist",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
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

                // Go to artist
                // Surface(
                //     color = MaterialTheme.colorScheme.surfaceContainerHigh,
                //     shape = RoundedCornerShape(12.dp),
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .padding(horizontal = 8.dp, vertical = 2.dp)
                // ) {
                //     DropdownMenuItem(
                //         text = {
                //             Text(
                //                 "Go to artist",
                //                 style = MaterialTheme.typography.bodyMedium,
                //                 fontWeight = FontWeight.Medium,
                //                 color = MaterialTheme.colorScheme.onSurface
                //             )
                //         },
                //         leadingIcon = {
                //             Surface(
                //                 color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                //                 shape = CircleShape,
                //                 modifier = Modifier.size(32.dp)
                //             ) {
                //                 Icon(
                //                     imageVector = Icons.Rounded.Person,
                //                     contentDescription = null,
                //                     tint = MaterialTheme.colorScheme.onSecondaryContainer,
                //                     modifier = Modifier
                //                         .fillMaxSize()
                //                         .padding(6.dp)
                //                 )
                //             }
                //         },
                //         onClick = {
                //             HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                //             showDropdown = false
                //             onGoToArtist()
                //         }
                //     )
                // }

                // Go to album
                // Surface(
                //     color = MaterialTheme.colorScheme.surfaceContainerHigh,
                //     shape = RoundedCornerShape(12.dp),
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .padding(horizontal = 8.dp, vertical = 2.dp)
                // ) {
                //     DropdownMenuItem(
                //         text = {
                //             Text(
                //                 "Go to album",
                //                 style = MaterialTheme.typography.bodyMedium,
                //                 fontWeight = FontWeight.Medium,
                //                 color = MaterialTheme.colorScheme.onSurface
                //             )
                //         },
                //         leadingIcon = {
                //             Surface(
                //                 color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                //                 shape = CircleShape,
                //                 modifier = Modifier.size(32.dp)
                //             ) {
                //                 Icon(
                //                     imageVector = Icons.Rounded.Album,
                //                     contentDescription = null,
                //                     tint = MaterialTheme.colorScheme.onSecondaryContainer,
                //                     modifier = Modifier
                //                         .fillMaxSize()
                //                         .padding(6.dp)
                //                 )
                //             }
                //         },
                //         onClick = {
                //             HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                //             showDropdown = false
                //             onGoToAlbum()
                //         }
                //     )
                // }

                // Song info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
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
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add to blacklist",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
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
    
    // Get unique album arts from playlist songs (up to 4)
    val albumArts = remember(playlist.songs) {
        playlist.songs
            .distinctBy { it.albumId }
            .take(4)
    }
    
    Surface(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stylish playlist artwork with collage
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (playlist.artworkUri != null) {
                        // Use custom playlist artwork if available
                        M3ImageUtils.PlaylistImage(
                            imageUrl = playlist.artworkUri,
                            playlistName = playlist.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (albumArts.isNotEmpty()) {
                        // Create collage from album arts
                        PlaylistArtCollage(
                            songs = albumArts,
                            playlistName = playlist.name
                        )
                    } else {
                        // Fallback to playlist icon with solid background (matching artwork corner radius)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = RhythmIcons.PlaylistFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Playlist info with better typography
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Enhanced metadata display with pill shape
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${playlist.songs.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (playlist.songs.isNotEmpty()) {
                        val totalDurationMs = playlist.songs.sumOf { it.duration }
                        val totalMinutes = (totalDurationMs / (1000 * 60)).toInt()
                        val durationText = if (totalMinutes >= 60) {
                            val hours = totalMinutes / 60
                            val minutes = totalMinutes % 60
                            "${hours}h ${minutes}m"
                        } else {
                            "${totalMinutes}m"
                        }

                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Stylish forward arrow with animation hint
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open playlist",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistArtCollage(
    songs: List<Song>,
    playlistName: String
) {
    when (songs.size) {
        1 -> {
            // Single album art
            M3ImageUtils.AlbumArt(
                imageUrl = songs[0].artworkUri,
                albumName = songs[0].album,
                modifier = Modifier.fillMaxSize()
            )
        }
        2 -> {
            // Two album arts side by side
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    M3ImageUtils.AlbumArt(
                        imageUrl = songs[0].artworkUri,
                        albumName = songs[0].album,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    M3ImageUtils.AlbumArt(
                        imageUrl = songs[1].artworkUri,
                        albumName = songs[1].album,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        3 -> {
            // Three album arts: one large on left, two stacked on right
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    M3ImageUtils.AlbumArt(
                        imageUrl = songs[0].artworkUri,
                        albumName = songs[0].album,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[1].artworkUri,
                            albumName = songs[1].album,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[2].artworkUri,
                            albumName = songs[2].album,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        else -> {
            // Four album arts in a 2x2 grid
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[0].artworkUri,
                            albumName = songs[0].album,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[1].artworkUri,
                            albumName = songs[1].album,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[2].artworkUri,
                            albumName = songs[2].album,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[3].artworkUri,
                            albumName = songs[3].album,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Song count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${album.numberOfSongs} Songs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Year pill
                    if (album.year > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${album.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Enhanced play button
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
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
            bottom = 16.dp // Simple spacing - Scaffold handles rest
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
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                
                // Pills row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    // Song count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${album.numberOfSongs}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Year pill
                    if (album.year > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "${album.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
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

@Composable
fun SingleCardArtistsContent(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val appSettings = remember { AppSettings.getInstance(context) }
    
    // Get artist view type from settings
    val artistViewType by appSettings.artistViewType.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var currentSortOption by remember { mutableStateOf(ArtistSortOption.NAME_ASC) }
    var showSortOptions by remember { mutableStateOf(false) }
    
    // Define categories for artists
    val categories = remember(artists) {
        listOf("All")
    }
    
    // Sort artists based on selected option
    val sortedArtists = remember(artists, currentSortOption) {
        when (currentSortOption) {
            ArtistSortOption.NAME_ASC -> artists.sortedBy { it.name.lowercase() }
            ArtistSortOption.NAME_DESC -> artists.sortedByDescending { it.name.lowercase() }
            ArtistSortOption.TRACK_COUNT_DESC -> artists.sortedByDescending { it.numberOfTracks }
            ArtistSortOption.ALBUM_COUNT_DESC -> artists.sortedByDescending { it.numberOfAlbums }
        }
    }
    
    // Determine if we should use grid or list view
    val isGridView = artistViewType == ArtistViewType.GRID
    
    if (isGridView) {
        // Grid view using LazyVerticalGrid as main container
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 16.dp // Simple spacing - Scaffold handles rest
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sticky header for grid view
            item(span = { GridItemSpan(maxLineSpan) }) {
                ArtistSectionHeader(
                    artistCount = sortedArtists.size
                )
            }
            
            if (sortedArtists.isNotEmpty()) {
                items(sortedArtists, key = { it.id }) { artist ->
                    AnimateIn {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ArtistGridCard(
                                artist = artist,
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    onArtistClick(artist)
                                },
                                onPlayClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    viewModel.playArtist(artist)
                                }
                            )
                        }
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyArtistsState()
                }
            }
        }
    } else {
        // List view using LazyColumn as main container
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 16.dp // Simple spacing - Scaffold handles rest
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sticky header for list view
            stickyHeader {
                ArtistSectionHeader(
                    artistCount = sortedArtists.size
                )
            }
            
            if (sortedArtists.isNotEmpty()) {
                items(sortedArtists, key = { it.id }) { artist ->
                    AnimateIn {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ArtistListCard(
                                artist = artist,
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    onArtistClick(artist)
                                },
                                onPlayClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    viewModel.playArtist(artist)
                                }
                            )
                        }
                    }
                }
            } else {
                item {
                    EmptyArtistsState()
                }
            }
        }
    }

    // Sort options bottom sheet
    if (showSortOptions) {
        ModalBottomSheet(
            onDismissRequest = { showSortOptions = false },
            sheetState = rememberModalBottomSheetState(),
            dragHandle = { 
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ArtistSectionHeader(
    artistCount: Int
) {
    
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Artists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$artistCount ${if (artistCount == 1) "artist" else "artists"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }



            Surface(
                modifier = Modifier
                    .height(2.dp)
                    .width(60.dp),
                shape = RoundedCornerShape(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            ) {}
        }
    }
}

@Composable
private fun EmptyArtistsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
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

enum class ArtistSortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    TRACK_COUNT_DESC("Songs (High to Low)"),
    ALBUM_COUNT_DESC("Albums (High to Low)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistGridCard(
    artist: Artist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
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
                    color = MaterialTheme.colorScheme.surfaceContainer,
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
                    onClick = onPlayClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(48.dp)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                // Artist name
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Artist info row with pills (centered)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Track count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${artist.numberOfTracks}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    // Album count pill
                    if (artist.numberOfAlbums > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "${artist.numberOfAlbums}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistListCard(
    artist: Artist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist circular image
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
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

                // Artist info with pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Track count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${artist.numberOfTracks} Songs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Album count pill
                    if (artist.numberOfAlbums > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${artist.numberOfAlbums} Albums",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Play button
            FilledIconButton(
                onClick = onPlayClick,
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

@Composable
fun SingleCardExplorerContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onPlayQueue: (List<Song>) -> Unit,
    onShuffleQueue: (List<Song>) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    appSettings: AppSettings,
    reloadTrigger: Int = 0,
    onCreatePlaylist: (String) -> Unit = { _ -> },
    musicViewModel: MusicViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity
    
    // State for creating playlist from folder
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var folderSongsForPlaylist by remember { mutableStateOf<List<Song>>(emptyList()) }
    var playlistNamePrefix by remember { mutableStateOf("") }
    
    val playlists by musicViewModel.playlists.collectAsState()

    // Handle back gesture to go level up - currentPath is defined inside SingleCardExplorerContent, this code may be in wrong scope
    // Removing duplicated back handler that references undefined currentPath
    // Code should be inside the composable where currentPath is defined

    // Check storage permission based on Android version
    val hasStoragePermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - check for media permissions
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 - scoped storage, check media permissions
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 9 and below - full storage access
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var currentPath by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoadingDirectory by remember { mutableStateOf(false) }

    // Handle permission result in a LaunchedEffect
    LaunchedEffect(hasStoragePermission) {
        if (!hasStoragePermission) {
            showPermissionDialog = true
        }
    }

    // Permission not granted - show request UI
    if (!hasStoragePermission || showPermissionDialog) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Permission request card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Permission icon
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = "Storage Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Description
                    Text(
                        text = "To browse your music files and explore your device's storage, Rhythm needs permission to access your files and media.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grant permission button
                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Android 13+
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                                    1001
                                )
                            } else {
                                // Android 12 and below
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    1001
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (hasStoragePermission) Icons.Default.Check else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasStoragePermission) "Permission Granted" else "Grant Permission",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Alternative info text
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "This allows access only to audio files and folders containing music.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Permission granted - show file explorer

    // Get audio file extensions to filter
    val audioExtensions = remember {
        setOf("mp3", "flac", "m4a", "aac", "ogg", "wav", "wma", "aiff", "opus")
    }

    // Directory items state - loaded asynchronously to prevent ANR
    var currentItems by remember { mutableStateOf<List<ExplorerItem>>(emptyList()) }
    
    // Initial loading state for first tab open
    var isInitialLoading by remember { mutableStateOf(true) }

    // Pinned folders state
    val pinnedFolders by appSettings.pinnedFolders.collectAsState()

    // Breadcrumb scroll state
    val breadcrumbScrollState = rememberLazyListState()

    // Handle back gesture to go level up
    if (currentPath != null) {
        BackHandler {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            currentPath = getParentPath(currentPath!!)
        }
    }

    // Cache for directory contents to improve performance
    val directoryCache = remember { mutableMapOf<String?, List<ExplorerItem>>() }
    
    // Debounce key to prevent rapid recompositions causing ANR
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Handle reload trigger - clear cache and reload current directory
    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0) {
            // Clear the entire cache
            directoryCache.clear()
            
            // Force reload of current directory by removing it from cache and triggering reload
            val cacheKey = currentPath
            directoryCache.remove(cacheKey)
            
            // Cancel any pending load operation
            debounceJob?.cancel()
            
            // Reload current directory
            if (currentPath == null) {
                val storageItems = getStorageRoots(context)
                currentItems = storageItems
                directoryCache[cacheKey] = storageItems
                isLoadingDirectory = false
            } else {
                isLoadingDirectory = true
                try {
                    val items = withContext(Dispatchers.IO) {
                        getDirectoryContentsOptimized(currentPath!!, audioExtensions, songs, context)
                    }
                    // Filter: keep files and folders that have audio content (checks recursively)
                    val filteredItems = items.filter { 
                        it.type != ExplorerItemType.FOLDER || it.itemCount > 0 || hasAudioContentRecursive(it.path, songs, context)
                    }
                    val sortedItems = filteredItems.sortedWith(
                        compareBy<ExplorerItem> { it.type != ExplorerItemType.FOLDER }
                            .thenBy { it.name.lowercase() }
                    )
                    currentItems = sortedItems
                    directoryCache[cacheKey] = sortedItems
                } catch (e: Exception) {
                    currentItems = emptyList()
                } finally {
                    isLoadingDirectory = false
                }
            }
        }
    }

    // Load directory contents asynchronously with caching and debouncing
    LaunchedEffect(currentPath) {
        // Cancel any pending load operation
        debounceJob?.cancel()
        
        val cacheKey = currentPath
        
        if (currentPath == null) {
            // Show device storage roots - run in IO to prevent blocking
            isLoadingDirectory = true
            val storageItems = withContext(Dispatchers.IO) {
                getStorageRoots(context)
            }
            currentItems = storageItems
            directoryCache[cacheKey] = storageItems
            isLoadingDirectory = false
            isInitialLoading = false
        } else {
            // Check cache first to avoid unnecessary reloads
            // Use cache if it exists (even if empty, as it might be a legitimately empty folder)
            val cached = directoryCache[cacheKey]
            if (cached != null) {
                // Use cached results immediately
                isLoadingDirectory = false
                currentItems = cached
            } else {
                // Set loading state FIRST before clearing items to ensure loading indicator shows
                isLoadingDirectory = true
                
                // DON'T clear current items immediately - keep showing previous content during load
                // This prevents the empty state flash when navigation is interrupted
                
                // Load without debounce to make navigation feel more responsive
                debounceJob = launch {
                    try {
                        val items = withContext(Dispatchers.IO) {
                            getDirectoryContentsOptimized(currentPath!!, audioExtensions, songs, context)
                        }
                        
                        // OPTIMIZED: Check for nested audio in parallel using coroutines
                        val filteredItems = withContext(Dispatchers.IO) {
                            items.filter { item ->
                                when {
                                    // Always keep files
                                    item.type != ExplorerItemType.FOLDER -> true
                                    // Keep folders with direct audio count > 0
                                    item.itemCount > 0 -> true
                                    // For folders with 0 direct count, check if they have nested audio
                                    // Use the optimized check that queries songs list instead of filesystem
                                    else -> hasAudioContentRecursive(item.path, songs, context, maxDepth = 3)
                                }
                            }
                        }
                        
                        val sortedItems = filteredItems.sortedWith(
                            compareBy<ExplorerItem> { it.type != ExplorerItemType.FOLDER }
                                .thenBy { it.name.lowercase() }
                        )
                        
                        // Only update if this job wasn't cancelled (i.e., user didn't navigate away)
                        if (isActive) {
                            currentItems = sortedItems
                            
                            // Cache the results (including empty results for legitimately empty folders)
                            if (directoryCache.size >= 20) {
                                // Remove oldest entry if cache is full
                                directoryCache.remove(directoryCache.keys.first())
                            }
                            directoryCache[cacheKey] = sortedItems
                        }
                    } catch (e: Exception) {
                        // Only update if this job wasn't cancelled
                        if (isActive) {
                            android.util.Log.e("LibraryScreen", "Error loading directory $currentPath", e)
                            isInitialLoading = false
                            // Check if we have cached data from a previous successful load
                            val previousCache = directoryCache[cacheKey]
                            if (previousCache != null) {
                                // Use previous cache on error
                                currentItems = previousCache
                            } else {
                                // Show empty state only if no cache exists
                                currentItems = emptyList()
                            }
                        }
                    } finally {
                        if (isActive) {
                            isLoadingDirectory = false
                        }
                    }
                }
            }
        }
    }

    // Get current folder songs for play all/shuffle buttons
    val currentFolderSongs = remember(currentItems) {
        currentItems.filter { it.type == ExplorerItemType.FILE && it.song != null }
            .mapNotNull { it.song }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 96.dp // Space for floating buttons
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Sticky Section Header for Explorer with breadcrumb navigation
            stickyHeader {
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Explore",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentPath == null) "${currentItems.size} locations" else "${currentItems.size} items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Back button if not at root
                        if (currentPath != null) {
                            Surface(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    currentPath = getParentPath(currentPath!!)
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Go back",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Breadcrumb navigation
                    if (currentPath != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ExplorerBreadcrumb(
                            path = currentPath!!,
                            onNavigateTo = { newPath ->
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                currentPath = newPath
                            },
                            onGoHome = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                currentPath = null
                            },
                            scrollState = breadcrumbScrollState
                        )
                    }
                }
            }
        }

                // Single unified loading indicator
                // Show "Initializing" only at root on first load, otherwise show "Loading directory"
                if (isLoadingDirectory || isInitialLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                M3FourColorCircularLoader(
                                    modifier = Modifier.size(48.dp)
                                )

                                Text(
                                    text = if (isInitialLoading && currentPath == null) {
                                        "Initializing Explorer..."
                                    } else {
                                        "Loading directory..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Storage Locations - Show at root level
                if (!isInitialLoading && currentPath == null && currentItems.any { it.type == ExplorerItemType.STORAGE }) {
                    // Filter only storage items
                    val storageItems = currentItems.filter { it.type == ExplorerItemType.STORAGE }

                    // Header for storage locations
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Storage",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Storage Locations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Storage items
                    items(
                        items = storageItems,
                        key = { "storage_" + it.path }
                    ) { item ->
                        AnimateIn {
                            ExplorerItemCard(
                                item = item,
                                onItemClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    currentPath = item.path
                                },
                                onSongClick = onSongClick,
                                onAddToPlaylist = onAddToPlaylist,
                                onAddToQueue = onAddToQueue,
                                onShowSongInfo = onShowSongInfo,
                                haptics = haptics,
                                isPinned = false, // Storages can't be pinned
                                onPinToggle = null, // No pin toggle for storages
                                onPlayFolder = null, // Storages don't have play option
                                onAddFolderToQueue = null // Storages don't have queue option
                            )
                        }
                    }
                }

                // Pinned Folders - only show when at root and have pinned folders
                if (!isInitialLoading && currentPath == null && pinnedFolders.isNotEmpty()) {
                    // Filter pinned folders that exist
                    val existingPinnedFolders = pinnedFolders.filter { pinnedPath ->
                        try {
                            val file = File(pinnedPath)
                            file.exists() && file.isDirectory && file.canRead()
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (existingPinnedFolders.isNotEmpty()) {
                        // Create ExplorerItem for each pinned folder
                        val pinnedFolderItems = existingPinnedFolders.map { folderPath ->
                            val folderName = File(folderPath).name
                            val itemCount = countAudioFilesInDirectoryShallow(File(folderPath), audioExtensions)
                            ExplorerItem(
                                name = folderName,
                                path = folderPath,
                                isDirectory = true,
                                itemCount = itemCount,
                                type = ExplorerItemType.FOLDER,
                                song = null
                            )
                        }

                        // Header for pinned folders
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Pushpin,
                                    contentDescription = "Pinned",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Pinned Folders",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Pinned folder items
                        items(
                            items = pinnedFolderItems,
                            key = { "pinned_" + it.path }
                        ) { item ->
                            AnimateIn {
                                ExplorerItemCard(
                                    item = item,
                                    onItemClick = {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                        currentPath = item.path
                                    },
                                    onSongClick = onSongClick,
                                    onAddToPlaylist = onAddToPlaylist,
                                    onAddToQueue = onAddToQueue,
                                    onShowSongInfo = onShowSongInfo,
                                    haptics = haptics,
                                    isPinned = true,
                                    onPinToggle = {
                                        appSettings.removeFolderFromPinned(item.path)
                                    },
                                    onPlayFolder = { folderItem ->
                                        // Get songs in this folder and create playlist
                                        val folderSongs = songs.filter { song ->
                                            try {
                                                val songPath = getFilePathFromUri(song.uri, context) ?: ""
                                                val normalizedSongPath = songPath.replace("//", "/")
                                                val normalizedFolderPath = folderItem.path.replace("//", "/").trimEnd('/')
                                                normalizedSongPath.startsWith("$normalizedFolderPath/")
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }
                                        if (folderSongs.isNotEmpty()) {
                                            folderSongsForPlaylist = folderSongs
                                            playlistNamePrefix = folderItem.name
                                            showCreatePlaylistDialog = true
                                        }
                                    },
                                    onAddFolderToQueue = { folderItem ->
                                        // Get songs in this folder and add to queue
                                        val folderSongs = songs.filter { song ->
                                            try {
                                                val songPath = getFilePathFromUri(song.uri, context) ?: ""
                                                val normalizedSongPath = songPath.replace("//", "/")
                                                val normalizedFolderPath = folderItem.path.replace("//", "/").trimEnd('/')
                                                normalizedSongPath.startsWith("$normalizedFolderPath/")
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }
                                        if (folderSongs.isNotEmpty()) {
                                            folderSongs.forEach { song -> onAddToQueue(song) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Explorer Items - only show when not loading and not at root level
                if (!isLoadingDirectory && currentPath != null) {
                    items(
                        items = currentItems,
                        key = { it.path + it.name + it.type }
                    ) { item ->
                        AnimateIn {
                            ExplorerItemCard(
                                item = item,
                                onItemClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)

                                    when (item.type) {
                                        ExplorerItemType.STORAGE, ExplorerItemType.FOLDER -> {
                                            // Navigate into directory
                                            currentPath = item.path
                                        }
                                        ExplorerItemType.FILE -> {
                                            // Play the song and add ALL folder songs to queue
                                            item.song?.let { song -> 
                                                // Add all songs in folder to queue first
                                                currentFolderSongs.forEach { folderSong -> 
                                                    onAddToQueue(folderSong) 
                                                }
                                                // Then play the selected song (which is already in queue)
                                                onSongClick(song)
                                            }
                                        }
                                    }
                                },
                                onSongClick = onSongClick,
                                onAddToPlaylist = onAddToPlaylist,
                                onAddToQueue = onAddToQueue,
                                onShowSongInfo = onShowSongInfo,
                                haptics = haptics,
                                isPinned = pinnedFolders.contains(item.path),
                                onPinToggle = if (item.type == ExplorerItemType.FOLDER) {
                                    {
                                        if (pinnedFolders.contains(item.path)) {
                                            appSettings.removeFolderFromPinned(item.path)
                                        } else {
                                            appSettings.addFolderToPinned(item.path)
                                        }
                                    }
                                } else null,
                                onPlayFolder = if (item.type == ExplorerItemType.FOLDER) {
                                    { folderItem ->
                                        // Get all songs in this folder and create playlist
                                        val folderSongs = songs.filter { song ->
                                            try {
                                                val songPath = getFilePathFromUri(song.uri, context) ?: ""
                                                val normalizedSongPath = songPath.replace("//", "/")
                                                val normalizedFolderPath = folderItem.path.replace("//", "/").trimEnd('/')
                                                normalizedSongPath.startsWith("$normalizedFolderPath/")
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }
                                        if (folderSongs.isNotEmpty()) {
                                            folderSongsForPlaylist = folderSongs
                                            playlistNamePrefix = folderItem.name
                                            showCreatePlaylistDialog = true
                                        }
                                    }
                                } else null,
                                onAddFolderToQueue = if (item.type == ExplorerItemType.FOLDER) {
                                    { folderItem ->
                                        // Get all songs in this folder and add to queue
                                        val folderSongs = songs.filter { song ->
                                            try {
                                                val songPath = getFilePathFromUri(song.uri, context) ?: ""
                                                val normalizedSongPath = songPath.replace("//", "/")
                                                val normalizedFolderPath = folderItem.path.replace("//", "/").trimEnd('/')
                                                normalizedSongPath.startsWith("$normalizedFolderPath/")
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }
                                        if (folderSongs.isNotEmpty()) {
                                            folderSongs.forEach { song -> onAddToQueue(song) }
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }

                // Empty state - show when no items and not loading, with single retry option
                if (!isInitialLoading && currentItems.isEmpty() && !isLoadingDirectory) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Empty state illustration with multiple elements
                                Box(
                                    modifier = Modifier.size(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Background circle
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shadowElevation = 0.dp
                                    ) {}

                                    // Main empty folder icon
                                    Icon(
                                        imageVector = Icons.Default.FolderOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )

                                    // Subtle secondary icons
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 16.dp, y = (-8).dp)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.BottomStart)
                                            .offset(x = (-12).dp, y = 12.dp)
                                    )
                                }

                                // Main message
                                Text(
                                    text = if (currentPath == null) "No storage found" else "Empty folder",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                // Secondary description
                                Text(
                                    text = if (currentPath == null)
                                        "Connect storage devices or check permissions to explore your music files"
                                    else
                                        "This folder doesn't contain any audio files",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
                                )

                                // Action buttons
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // Refresh button
                                    // FilledTonalButton(
                                    //     onClick = {
                                    //         HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    //         // Clear cache and reload directory
                                    //         directoryCache.clear()
                                    //         // Trigger reload by setting path to itself
                                    //         val temp = currentPath
                                    //         currentPath = null
                                    //         currentPath = temp
                                    //     },
                                    //     shape = RoundedCornerShape(12.dp)
                                    // ) {
                                    //     Icon(
                                    //         imageVector = Icons.Default.Refresh,
                                    //         contentDescription = null,
                                    //         modifier = Modifier.size(18.dp)
                                    //     )
                                    //     Spacer(modifier = Modifier.width(8.dp))
                                    //     Text(
                                    //         text = "Refresh",
                                    //         style = MaterialTheme.typography.labelLarge,
                                    //         fontWeight = FontWeight.Medium
                                    //     )
                                    // }

                                    // Permission info
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(12.dp),
                                        tonalElevation = 0.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Check storage permissions if files don't appear",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Third row: Go back button if we're not at root
                                    if (currentPath != null) {
                                        OutlinedButton(
                                            onClick = {
                                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                                currentPath = getParentPath(currentPath!!)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Go Back",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        
        // Bottom Floating Button Group - only show when there are songs in current folder with animations
        AnimatedVisibility(
            visible = currentFolderSongs.isNotEmpty() && currentPath != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            BottomFloatingButtonGroup(
                modifier = Modifier
                    .padding(bottom = 16.dp), // Simple fixed spacing
                onPlayAll = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onPlayQueue(currentFolderSongs)
                },
                onShuffle = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onShuffleQueue(currentFolderSongs)
                },
                haptics = haptics
            )
        }
    }
    
    // Create playlist dialog for folder
    if (showCreatePlaylistDialog) {
        val scope = rememberCoroutineScope()
        var playlistName by remember { mutableStateOf(playlistNamePrefix) }
        var isCreating by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = {
                if (!isCreating) {
                    showCreatePlaylistDialog = false
                    folderSongsForPlaylist = emptyList()
                    playlistNamePrefix = ""
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    "Create Playlist from Folder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (isCreating) {
                        // Show loading state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            M3FourColorCircularLoader(
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Creating playlist and adding ${folderSongsForPlaylist.size} songs...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            "Add ${folderSongsForPlaylist.size} ${if (folderSongsForPlaylist.size == 1) "song" else "songs"} to a new playlist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = { 
                                playlistName = it
                                isError = it.isBlank()
                            },
                            label = { Text("Playlist name") },
                            isError = isError,
                            supportingText = {
                                if (isError) {
                                    Text(
                                        text = "Playlist name cannot be empty",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!isCreating) {
                    Button(
                        onClick = {
                            if (playlistName.isBlank()) {
                                isError = true
                            } else {
                                isCreating = true
                                // Create the playlist using coroutine scope
                                scope.launch {
                                    try {
                                        // Create the playlist
                                        onCreatePlaylist(playlistName)
                                        
                                        // Wait for playlist to be created and available in the list
                                        var attempts = 0
                                        var newPlaylist: chromahub.rhythm.app.data.Playlist? = null
                                        while (attempts < 20 && newPlaylist == null) {
                                            kotlinx.coroutines.delay(100)
                                            newPlaylist = playlists.firstOrNull { it.name == playlistName }
                                            attempts++
                                        }
                                        
                                        if (newPlaylist != null) {
                                            // Add all songs to the playlist with proper error handling
                                            folderSongsForPlaylist.forEach { song ->
                                                musicViewModel.addSongToPlaylist(song, newPlaylist.id) { _ -> }
                                                kotlinx.coroutines.delay(10) // Small delay between adds to avoid race conditions
                                            }
                                            Log.d("LibraryScreen", "Successfully added ${folderSongsForPlaylist.size} songs to playlist: $playlistName")
                                        } else {
                                            Log.e("LibraryScreen", "Failed to find newly created playlist: $playlistName")
                                        }
                                        
                                        // Close dialog
                                        showCreatePlaylistDialog = false
                                        folderSongsForPlaylist = emptyList()
                                        playlistNamePrefix = ""
                                        isCreating = false
                                    } catch (e: Exception) {
                                        Log.e("LibraryScreen", "Error creating playlist", e)
                                        isCreating = false
                                    }
                                }
                            }
                        },
                        enabled = playlistName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create")
                    }
                }
            },
            dismissButton = {
                if (!isCreating) {
                    OutlinedButton(
                        onClick = {
                            showCreatePlaylistDialog = false
                            folderSongsForPlaylist = emptyList()
                            playlistNamePrefix = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// Helper function to get device storage roots
fun getStorageRoots(context: android.content.Context): List<ExplorerItem> {
    val items = mutableListOf<ExplorerItem>()

    try {
        // Get internal storage (primary external storage)
        val internalStorage = Environment.getExternalStorageDirectory()
        if (internalStorage.exists()) {
            items.add(ExplorerItem(
                name = "Internal Storage",
                path = internalStorage.absolutePath,
                isDirectory = true,
                itemCount = 0, // Will be calculated dynamically
                type = ExplorerItemType.STORAGE,
                song = null
            ))
        }

        // Get external storage directories (SD cards, etc.)
        // This gives us all removable storage paths
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
        
        externalDirs.forEachIndexed { index, dir ->
            if (dir != null && index > 0) { // Skip index 0 as it's internal storage
                // Navigate up to get the actual SD card root
                // From /storage/XXXX-XXXX/Android/data/package/files to /storage/XXXX-XXXX
                var sdCardRoot = dir
                var depth = 0
                while (sdCardRoot.parent != null && depth < 10) {
                    sdCardRoot = sdCardRoot.parentFile ?: break
                    depth++
                    // Stop when we reach /storage/XXXX-XXXX level
                    if (sdCardRoot.parent == "/storage" || sdCardRoot.parentFile?.name == "storage") {
                        break
                    }
                }
                
                if (sdCardRoot.exists() && sdCardRoot.canRead()) {
                    val storageName = "SD Card ${if (index > 1) index else ""}"
                    items.add(ExplorerItem(
                        name = storageName.trim(),
                        path = sdCardRoot.absolutePath,
                        isDirectory = true,
                        itemCount = 0,
                        type = ExplorerItemType.STORAGE,
                        song = null
                    ))
                }
            }
        }
        
        // Alternative method: Check /storage directory directly
        val storageDir = File("/storage")
        if (storageDir.exists() && storageDir.isDirectory) {
            storageDir.listFiles()?.forEach { file ->
                if (file.isDirectory && 
                    file.name != "emulated" && 
                    file.name != "self" && 
                    !file.name.startsWith(".") &&
                    file.canRead() &&
                    !items.any { it.path == file.absolutePath }) {
                    
                    items.add(ExplorerItem(
                        name = "Removable Storage (${file.name})",
                        path = file.absolutePath,
                        isDirectory = true,
                        itemCount = 0,
                        type = ExplorerItemType.STORAGE,
                        song = null
                    ))
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("LibraryScreen", "Error getting storage roots", e)
    }

    return items
}

// Data classes for explorer functionality
data class ExplorerItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val itemCount: Int,
    val type: ExplorerItemType,
    val song: Song? = null
)

enum class ExplorerItemType {
    STORAGE, FOLDER, FILE
}

// Helper functions
fun getParentDirectory(uriString: String): String {
    return try {
        val uri = android.net.Uri.parse(uriString)
        val path = uri.path ?: ""
        val lastSlashIndex = path.lastIndexOf('/')
        if (lastSlashIndex > 0) {
            path.substring(0, lastSlashIndex)
        } else {
            path
        }
    } catch (e: Exception) {
        ""
    }
}

fun getParentPath(path: String): String? {
    val lastSlashIndex = path.lastIndexOf('/')
    return if (lastSlashIndex > 0) {
        path.substring(0, lastSlashIndex)
    } else {
        null // At root
    }
}

// Helper function to get root directories from songs
fun getRootDirectories(songs: List<Song>): List<ExplorerItem> {
    val directories = mutableListOf<String>()

    songs.forEach { song ->
        try {
            val uri = android.net.Uri.parse(song.uri.toString())
            val path = uri.path ?: ""
            val dirPath = path.substringBeforeLast('/', "")

            if (dirPath.isNotEmpty()) {
                val normalizedDir = dirPath.replace("//", "/")
                if (!directories.contains(normalizedDir)) {
                    directories.add(normalizedDir)
                }
            }
        } catch (e: Exception) {
            // Skip this song if parsing fails
        }
    }

    return directories.map { dirPath ->
        val itemCount = songs.count { song ->
            try {
                val songPath = android.net.Uri.parse(song.uri.toString()).path ?: ""
                val songDir = songPath.substringBeforeLast('/', "")
                songDir == dirPath
            } catch (e: Exception) {
                false
            }
        }

        val dirName = dirPath.substringAfterLast('/').takeIf { it.isNotEmpty() } ?: dirPath

        ExplorerItem(
            name = dirName,
            path = dirPath,
            isDirectory = true,
            itemCount = itemCount,
            type = ExplorerItemType.FOLDER,
            song = null
        )
    }
}

// Helper function to count audio files in a directory based on songs
fun getAudioFileCountSongsInDirectory(
    songs: List<Song>,
    directoryPath: String,
    audioExtensions: Set<String>
): Int {
    return songs.count { song ->
        try {
            val songPath = android.net.Uri.parse(song.uri.toString()).path ?: ""
            val normalizedSongPath = songPath.replace("//", "/")
            val normalizedDirPath = directoryPath.replace("//", "/")

            // Check if the song path starts with the directory path
            normalizedSongPath.startsWith(normalizedDirPath) && normalizedSongPath != normalizedDirPath
        } catch (e: Exception) {
            false
        }
    }
}

// Optimized version for better performance - avoids excessive filesystem operations
// Now filters by MediaStore to show only indexed files with proper metadata
fun getDirectoryContentsOptimized(directoryPath: String, audioExtensions: Set<String>, songs: List<Song>, context: android.content.Context): List<ExplorerItem> {
    val items = mutableListOf<ExplorerItem>()

    try {
        val directory = File(directoryPath)
        if (!directory.exists()) {
            return items
        }

        // Build a map of file paths to songs for quick lookup
        val songsByPath = buildSongPathMap(songs, context)

        // Try to list files - may fail on SD card root due to permissions
        val files = try {
            directory.listFiles()
        } catch (e: SecurityException) {
            android.util.Log.d("LibraryScreen", "Cannot list files directly for $directoryPath, using MediaStore fallback")
            // If we can't list files directly (e.g., SD card root), fall back to MediaStore approach
            // Filter songs that belong to this directory or its subdirectories
            val dirPath = directoryPath.replace("//", "/").trimEnd('/')
            val songsInDir = songs.filter { song ->
                try {
                    val songPath = getFilePathFromUri(song.uri, context)
                    if (songPath != null) {
                        val normalizedSongPath = songPath.replace("//", "/")
                        // Check if song is in this directory or subdirectories
                        normalizedSongPath.startsWith("$dirPath/") || normalizedSongPath == dirPath
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
            
            android.util.Log.d("LibraryScreen", "Found ${songsInDir.size} songs in $dirPath using MediaStore")
            
            // Build directory structure from MediaStore songs
            val subdirs = mutableSetOf<String>()
            songsInDir.forEach { song ->
                try {
                    val songPath = getFilePathFromUri(song.uri, context) ?: return@forEach
                    val normalizedSongPath = songPath.replace("//", "/")
                    val normalizedDirPath = dirPath.trimEnd('/')
                    
                    // Get the path relative to current directory
                    val relativePath = if (normalizedSongPath.startsWith("$normalizedDirPath/")) {
                        normalizedSongPath.removePrefix("$normalizedDirPath/")
                    } else {
                        normalizedSongPath.removePrefix(normalizedDirPath).removePrefix("/")
                    }
                    
                    val firstSlash = relativePath.indexOf('/')
                    if (firstSlash > 0) {
                        // This song is in a subdirectory
                        subdirs.add(relativePath.substring(0, firstSlash))
                    } else if (firstSlash < 0 && relativePath.isNotEmpty()) {
                        // This song is directly in this directory
                        val extension = File(songPath).extension.lowercase()
                        if (extension in audioExtensions) {
                            items.add(ExplorerItem(
                                name = song.title,
                                path = songPath,
                                isDirectory = false,
                                itemCount = 1,
                                type = ExplorerItemType.FILE,
                                song = song
                            ))
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LibraryScreen", "Error processing song ${song.title}", e)
                }
            }
            
            // Add subdirectories
            subdirs.forEach { subdir ->
                val subdirPath = "$dirPath/$subdir"
                val audioCount = songsInDir.count { song ->
                    try {
                        val songPath = getFilePathFromUri(song.uri, context)
                        songPath != null && songPath.replace("//", "/").startsWith("$subdirPath/")
                    } catch (e: Exception) {
                        false
                    }
                }
                if (audioCount > 0) {
                    items.add(ExplorerItem(
                        name = subdir,
                        path = subdirPath,
                        isDirectory = true,
                        itemCount = audioCount,
                        type = ExplorerItemType.FOLDER,
                        song = null
                    ))
                }
            }
            
            android.util.Log.d("LibraryScreen", "MediaStore fallback returned ${items.size} items (${items.count { !it.isDirectory }} files, ${items.count { it.isDirectory }} folders)")
            return items
        }
        
        // Normal file listing succeeded
        files?.forEach { file ->
            if (file.isDirectory) {
                // Use shallow count only (much faster) for better performance
                // Count only files that are in MediaStore
                val audioCount = countMediaStoreAudioFilesInDirectoryShallow(file, songsByPath)
                // Always add folders - let the filter decide if they should be shown
                // This prevents hiding folders that have nested audio
                items.add(ExplorerItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = true,
                    itemCount = audioCount,
                    type = ExplorerItemType.FOLDER,
                    song = null
                ))
            } else if (file.isFile) {
                val extension = file.extension.lowercase()
                if (extension in audioExtensions) {
                    // Try to find song by absolute path first
                    var song = songsByPath[file.absolutePath]
                    
                    // If not found, try with normalized paths (handle // and path variations)
                    if (song == null) {
                        val normalizedPath = file.absolutePath.replace("//", "/")
                        song = songsByPath[normalizedPath]
                    }
                    
                    // If still not found, try fuzzy matching by checking all songs
                    if (song == null) {
                        song = songs.find { candidateSong ->
                            try {
                                val candidatePath = getFilePathFromUri(candidateSong.uri, context)
                                candidatePath != null && 
                                (candidatePath == file.absolutePath || 
                                 candidatePath.replace("//", "/") == file.absolutePath.replace("//", "/"))
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }
                    
                    if (song != null) {
                        items.add(ExplorerItem(
                            name = song.title, // Use actual metadata title instead of filename
                            path = file.absolutePath,
                            isDirectory = false,
                            itemCount = 1,
                            type = ExplorerItemType.FILE,
                            song = song
                        ))
                    }
                }
            }
        }
    } catch (e: Exception) {
        // Handle permission or access errors gracefully
        android.util.Log.e("LibraryScreen", "Error reading directory: $directoryPath", e)
    }

    return items
}

// Helper function to build a map of file paths to songs from MediaStore
fun buildSongPathMap(songs: List<Song>, context: android.content.Context): Map<String, Song> {
    val pathMap = mutableMapOf<String, Song>()
    
    songs.forEach { song ->
        try {
            // Try to get the actual file path from the content URI
            val path = getFilePathFromUri(song.uri, context)
            if (path != null && path.isNotEmpty()) {
                pathMap[path] = song
            }
        } catch (e: Exception) {
            // Skip songs that can't be mapped to file paths
        }
    }
    
    return pathMap
}

// Helper function to get file path from MediaStore content URI
fun getFilePathFromUri(uri: android.net.Uri, context: android.content.Context): String? {
    return try {
        // MediaStore content URIs are in format: content://media/external/audio/media/{id}
        // We need to query MediaStore to get the actual file path
        val projection = arrayOf(android.provider.MediaStore.Audio.Media.DATA)
        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA)
                it.getString(columnIndex)
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

// Helper function to count audio files in a directory recursively
fun countAudioFilesInDirectory(directory: File, audioExtensions: Set<String>): Int {
    var count = 0

    try {
        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isDirectory) {
                count += countAudioFilesInDirectory(file, audioExtensions)
            } else if (file.isFile) {
                val extension = file.extension.lowercase()
                if (extension in audioExtensions) {
                    count++
                }
            }
        }
    } catch (e: Exception) {
        // Handle permission errors
    }

    return count
}

// Helper function to count audio files in a directory (shallow - first level only)
fun countAudioFilesInDirectoryShallow(directory: File, audioExtensions: Set<String>): Int {
    var count = 0

    try {
        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isFile) {
                val extension = file.extension.lowercase()
                if (extension in audioExtensions) {
                    count++
                }
            }
            // Don't recurse into subdirectories for performance
        }
    } catch (e: Exception) {
        // Handle permission errors
    }

    return count
}

// Helper function to count MediaStore audio files in a directory (shallow - first level only)
fun countMediaStoreAudioFilesInDirectoryShallow(directory: File, songsByPath: Map<String, Song>): Int {
    var count = 0

    try {
        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isFile) {
                // Only count files that exist in MediaStore
                if (songsByPath.containsKey(file.absolutePath)) {
                    count++
                }
            }
            // Don't recurse into subdirectories for performance
        }
    } catch (e: Exception) {
        // Handle permission errors
    }

    return count
}

// Helper function to check if a folder has audio content (recursively checks nested folders)
fun hasAudioContentRecursive(path: String, songs: List<Song>, context: android.content.Context, maxDepth: Int = 3): Boolean {
    if (maxDepth <= 0) return false
    
    return try {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            return false
        }
        
        // Check if any songs in the songs list are inside this directory or its subdirectories
        val normalizedDirPath = path.replace("//", "/").trimEnd('/')
        songs.any { song ->
            try {
                val songPath = getFilePathFromUri(song.uri, context) ?: return@any false
                val normalizedSongPath = songPath.replace("//", "/")
                normalizedSongPath.startsWith("$normalizedDirPath/")
            } catch (e: Exception) {
                false
            }
        }
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderItem(
    folderName: String,
    songCount: Int,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder icon with enhanced styling
            Surface(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Folder info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = folderName,
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
                        text = "$songCount ${if (songCount == 1) "track" else "tracks"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Forward arrow
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
                        contentDescription = "Open folder",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistFabMenuContent(
    onCreatePlaylist: () -> Unit,
    onImportPlaylist: (() -> Unit)?,
    onExportPlaylists: (() -> Unit)?,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Added haptics parameter
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .widthIn(max = 200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Export playlists item
        if (onExportPlaylists != null) {
            FloatingActionButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onExportPlaylists()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Export playlists",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Import playlist item
        if (onImportPlaylist != null) {
            FloatingActionButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onImportPlaylist()
                    }
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Actions.Download,
                    contentDescription = "Import playlist",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Create playlist item (always shown)
        FloatingActionButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                scope.launch {
                    onCreatePlaylist()
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = RhythmIcons.Add,
                contentDescription = "Create playlist",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PlaylistFabMenu(
    expanded: Boolean,
    onCreatePlaylist: () -> Unit,
    onImportPlaylist: (() -> Unit)?,
    onExportPlaylists: (() -> Unit)?,
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Added haptics parameter
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val miniPlayerPadding = LocalMiniPlayerPadding.current

    // Animate FAB expansion
    val fabScale by animateFloatAsState(
        targetValue = if (isExpanded) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabScale"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        val miniPlayerPadding = LocalMiniPlayerPadding.current

        // Calculate consistent spacing from main FAB (bottom = 50.dp, FAB size = 56.dp)
        // Base position above main FAB: 50 + 56 + 16 = 122.dp
        val fabBaseBottom = 50.dp + 56.dp + 16.dp
        val menuItemHeight = 56.dp // Approximate FabMenuItem height
        val spacing = 16.dp

        // FAB Menu Items with corrected positioning
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(300, delayMillis = 300)) +
                   slideInHorizontally(
                       animationSpec = tween(300, delayMillis = 300),
                       initialOffsetX = { it / 2 }
                   ),
            exit = fadeOut(animationSpec = tween(200, delayMillis = 200)) +
                  slideOutHorizontally(
                      animationSpec = tween(200, delayMillis = 200),
                      targetOffsetX = { it / 2 }
                  )
        ) {
            FabMenuItem(
                label = "New playlist",
                icon = RhythmIcons.Add,
                contentDescription = "Create new playlist",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onCreatePlaylist()
                        isExpanded = false
                    }
                },
                animationDelay = 100,
                haptics = haptics, // Pass haptics
                modifier = Modifier.padding(
                    bottom = fabBaseBottom, // Directly above main FAB
                    end = 16.dp
                )
            )
        }

        AnimatedVisibility(
            visible = isExpanded && onImportPlaylist != null,
            enter = fadeIn(animationSpec = tween(300, delayMillis = 150)) +
                   slideInHorizontally(
                       animationSpec = tween(300, delayMillis = 150),
                       initialOffsetX = { it / 2 }
                   ),
            exit = fadeOut(animationSpec = tween(200, delayMillis = 100)) +
                  slideOutHorizontally(
                      animationSpec = tween(200, delayMillis = 100),
                      targetOffsetX = { it / 2 }
                  )
        ) {
            FabMenuItem(
                label = "Import playlist",
                icon = RhythmIcons.Actions.Download,
                contentDescription = "Import playlist",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onImportPlaylist?.invoke()
                        isExpanded = false
                    }
                },
                animationDelay = 50,
                haptics = haptics, // Pass haptics
                modifier = Modifier.padding(
                    bottom = fabBaseBottom + menuItemHeight + spacing, // Above "New playlist"
                    end = 16.dp
                )
            )
        }

        AnimatedVisibility(
            visible = isExpanded && onExportPlaylists != null,
            enter = fadeIn(animationSpec = tween(300, delayMillis = 0)) +
                   slideInHorizontally(
                       animationSpec = tween(300, delayMillis = 0),
                       initialOffsetX = { it / 2 }
                   ),
            exit = fadeOut(animationSpec = tween(200, delayMillis = 0)) +
                  slideOutHorizontally(
                      animationSpec = tween(200, delayMillis = 0),
                      targetOffsetX = { it / 2 }
                  )
        ) {
            FabMenuItem(
                label = "Export playlists",
                icon = Icons.Default.FileUpload,
                contentDescription = "Export playlists",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onExportPlaylists?.invoke()
                        isExpanded = false
                    }
                },
                animationDelay = 0,
                haptics = haptics, // Pass haptics
                modifier = Modifier.padding(
                    bottom = fabBaseBottom + (menuItemHeight + spacing) * 2, // Above "Import playlist"
                    end = 16.dp
                )
            )
        }

        // Main FAB with simple rotation animation
        val fabRotation by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "fabRotation"
        )

        FloatingActionButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                isExpanded = !isExpanded
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 12.dp
            ),
            modifier = Modifier
                .padding(bottom = 10.dp) // Simple fixed spacing
                .size(56.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else RhythmIcons.Add,
                contentDescription = if (isExpanded) "Close FAB menu" else "Open FAB menu",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = fabRotation
                    }
            )
        }
    }
}

@Composable
fun ExplorerBreadcrumb(
    path: String,
    onNavigateTo: (String) -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val rawSegments = path.split("/").filter { it.isNotEmpty() }

    // Collapse Android internal storage path (/storage/emulated/0) into a single 'Internal Storage' breadcrumb
    val displaySegments: List<Pair<String, String>> = run {
        if (rawSegments.size >= 3 && rawSegments[0].equals("storage", true)
            && rawSegments[1].equals("emulated", true) && rawSegments[2] == "0"
        ) {
            val basePath = "/storage/emulated/0"
            val rest = if (rawSegments.size > 3) rawSegments.subList(3, rawSegments.size) else emptyList()
            val segments = mutableListOf<Pair<String, String>>()
            // Add the collapsed internal storage segment
            segments.add("Internal Storage" to basePath)
            // Add remaining segments with full paths
            var current = basePath
            for (s in rest) {
                current = "$current/$s"
                segments.add(s to current)
            }
            segments
        } else {
            // Normal path -> each segment maps to its accumulated path
            val segments = mutableListOf<Pair<String, String>>()
            var current = ""
            for (s in rawSegments) {
                current = "$current/$s"
                segments.add(s to current)
            }
            segments
        }
    }

    // Auto-scroll to the active breadcrumb (last segment)
    LaunchedEffect(displaySegments) {
        if (displaySegments.isNotEmpty()) {
            val lastIndex = (displaySegments.size * 2) + 1 // Account for chevrons and segments
            scrollState.animateScrollToItem(lastIndex.coerceAtLeast(0))
        }
    }

    // Add animations to path chips
    LazyRow(
        state = scrollState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        // Home button with enhanced styling
        item {
            val homeScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "homeScale"
            )

            Surface(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onGoHome()
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .height(36.dp)
                    .graphicsLayer {
                        scaleX = homeScale
                        scaleY = homeScale
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Path segments with enhanced styling
        displaySegments.forEachIndexed { index, pair ->
            val (segmentDisplay, segmentPath) = pair

            item {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            val currentPath = segmentPath
            val isLastSegment = index == displaySegments.lastIndex

            item {
                val chipScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "chipScale_$index"
                )

                val chipBackgroundColor by animateColorAsState(
                    targetValue = if (isLastSegment)
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                    else
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "chipBackground_$index"
                )

                Surface(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onNavigateTo(currentPath)
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = chipBackgroundColor,
                    border = if (isLastSegment) BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    ) else null,
                    modifier = Modifier
                        .height(36.dp)
                        .graphicsLayer {
                            scaleX = chipScale
                            scaleY = chipScale
                        }
                ) {
                    // Handle long segment names with truncation
                    val displayText = if (segmentDisplay.length > 15) {
                        segmentDisplay.take(12) + "..."
                    } else {
                        segmentDisplay
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Optional icon for folders/directories
                        if (isLastSegment) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isLastSegment) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isLastSegment)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Subtle indicator for current location
                        if (isLastSegment) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Current location",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerItemCard(
    item: ExplorerItem,
    onItemClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onPinToggle: (() -> Unit)? = null,
    onPlayFolder: ((ExplorerItem) -> Unit)? = null,
    onAddFolderToQueue: ((ExplorerItem) -> Unit)? = null
) {
    val context = LocalContext.current

    when (item.type) {
        ExplorerItemType.STORAGE -> {
            Card(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onItemClick()
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Storage icon
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item.name) {
                                    "Internal Storage" -> Icons.Default.Storage
                                    else -> Icons.Default.SdStorage
                                },
                                contentDescription = "${item.name} icon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Storage info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap to browse files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Forward arrow
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Open storage",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        ExplorerItemType.FOLDER -> {
            var showDropdown by remember { mutableStateOf(false) }
            
            Card(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onItemClick()
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Folder icon - reduced size from 68.dp to 56.dp
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Folder",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp) // Reduced proportionally
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Folder info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall, // Smaller text to fit reduced height
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

            // Pin indicator for pinned folders - smaller and more prominent
                            if (isPinned) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp) // Smaller size
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = RhythmIcons.Pushpin,
                                            contentDescription = "Pinned",
                                            tint = MaterialTheme.colorScheme.onTertiary,
                                            modifier = Modifier.size(8.dp) // Smaller icon
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp) // Slightly smaller
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${item.itemCount} ${if (item.itemCount == 1) "track" else "tracks"}",
                                style = MaterialTheme.typography.bodySmall, // Smaller text
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Pin button (only show for folders in root directory)
                    if (onPinToggle != null) {
                        FilledTonalIconButton(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                onPinToggle()
                            },
                            modifier = Modifier.size(32.dp), // Smaller button
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isPinned) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (isPinned) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = if (isPinned) RhythmIcons.Pushpin else RhythmIcons.PinOutline,
                                contentDescription = if (isPinned) "Unpin folder" else "Pin folder",
                                modifier = Modifier.size(16.dp) // Smaller icon
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Options menu button for folders with songs
                    if (item.itemCount > 0 && (onPlayFolder != null || onAddFolderToQueue != null)) {
                        Box {
                            FilledIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    showDropdown = true
                                },
                                modifier = Modifier.size(width = 40.dp, height = 36.dp),
                                shape = RoundedCornerShape(18.dp), // Pill shape like songs tab
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.More,
                                    contentDescription = "Folder options",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    showDropdown = false
                                },
                                modifier = Modifier
                                    .widthIn(min = 220.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(5.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                // Create playlist from folder
                                onPlayFolder?.let {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Create playlist",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
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
                                                it(item)
                                            }
                                        )
                                    }
                                }
                                
                                // Add all to queue
                                onAddFolderToQueue?.let {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Add all to queue",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = RhythmIcons.Queue,
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
                                                it(item)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Forward arrow - commented out for now
                    // Surface(
                    //     modifier = Modifier.size(38.dp),
                    //     shape = CircleShape,
                    //     color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    // ) {
                    //     Box(contentAlignment = Alignment.Center) {
                    //         Icon(
                    //             imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    //             contentDescription = "Open folder",
                    //             tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    //             modifier = Modifier.size(18.dp)
                    //         )
                    //     }
                    // }
                }
            }
        }

        ExplorerItemType.FILE -> {
            val song = item.song
            if (song != null) {
                // Use the existing LibrarySongItem for files
                LibrarySongItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onAddToPlaylist(song) },
                    onAddToQueue = { onAddToQueue(song) },
                    onShowSongInfo = { onShowSongInfo(song) },
                    onAddToBlacklist = { /* Files in explorer don't have blacklist functionality */ },
                    haptics = haptics
                )
            }
        }
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    containerColor: Color, // Added containerColor
    contentColor: Color,   // Added contentColor
    onClick: () -> Unit,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Added haptics parameter
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Define scope here

    // Tap animation state
    val pressedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh),
        label = "pressedScale_$label"
    )

    // Staggered entrance animation
    val entranceScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "entranceScale_$label"
    )

    val entranceAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = animationDelay
        ),
        label = "entranceAlpha_$label"
    )

    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            isPressed = true
            onClick()
            // Reset pressed state after animation
            scope.launch { // Use the local scope
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        },
        shape = RoundedCornerShape(50.dp), // Pill shape
        colors = CardDefaults.cardColors( // Use CardDefaults.cardColors
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 8.dp
        ),
        modifier = modifier
            .graphicsLayer {
                scaleX = entranceScale * pressedScale
                scaleY = entranceScale * pressedScale
                alpha = entranceAlpha
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        awaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Bottom Floating Button Group Component
@Composable
fun BottomFloatingButtonGroup(
    modifier: Modifier = Modifier,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Loading states
    var isPlayAllLoading by remember { mutableStateOf(false) }
    var isShuffleLoading by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play All Button
            Button(
                onClick = {
                    if (!isPlayAllLoading && !isShuffleLoading) {
                        isPlayAllLoading = true
                        onPlayAll()
                        // Reset loading state after a delay
                        scope.launch {
                            kotlinx.coroutines.delay(1500)
                            isPlayAllLoading = false
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                enabled = !isPlayAllLoading && !isShuffleLoading
            ) {
                if (isPlayAllLoading) {
                    chromahub.rhythm.app.ui.components.SimpleCircularLoader(
                        size = 20.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play all",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play All",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Shuffle Button
            FilledIconButton(
                onClick = {
                    if (!isPlayAllLoading && !isShuffleLoading) {
                        isShuffleLoading = true
                        onShuffle()
                        // Reset loading state after a delay
                        scope.launch {
                            kotlinx.coroutines.delay(1500)
                            isShuffleLoading = false
                        }
                    }
                },
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                enabled = !isPlayAllLoading && !isShuffleLoading
            ) {
                if (isShuffleLoading) {
                    chromahub.rhythm.app.ui.components.SimpleCircularLoader(
                        size = 24.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    Icon(
                        imageVector = RhythmIcons.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
