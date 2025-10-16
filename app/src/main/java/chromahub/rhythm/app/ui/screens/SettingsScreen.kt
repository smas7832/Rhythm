@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.text.ExperimentalTextApi::class, ExperimentalFoundationApi::class)
package chromahub.rhythm.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.AlbumViewType
import chromahub.rhythm.app.data.ArtistViewType
import chromahub.rhythm.app.ui.screens.AlbumSortOrder
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.CreatePlaylistDialog
import chromahub.rhythm.app.R
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import chromahub.rhythm.app.data.AppSettings
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel
import chromahub.rhythm.app.viewmodel.MusicViewModel
import android.content.Intent
import android.net.Uri
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat // Import SimpleDateFormat
import java.util.Date // Import Date
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import chromahub.rhythm.app.util.HapticUtils

// Bottom Sheet imports
import chromahub.rhythm.app.ui.screens.ApiManagementBottomSheet
import chromahub.rhythm.app.ui.screens.CacheManagementBottomSheet
import chromahub.rhythm.app.ui.screens.CrashLogHistoryBottomSheet
import chromahub.rhythm.app.ui.screens.MediaScanBottomSheet
import chromahub.rhythm.app.ui.screens.MediaScanMode
import chromahub.rhythm.app.ui.screens.BackupRestoreBottomSheet
import chromahub.rhythm.app.ui.screens.EqualizerBottomSheetNew
import chromahub.rhythm.app.ui.screens.LibraryTabOrderBottomSheet
import chromahub.rhythm.app.ui.screens.SleepTimerBottomSheetNew

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    showLyrics: Boolean,
    showOnlineOnlyLyrics: Boolean,
    onShowLyricsChange: (Boolean) -> Unit,
    onShowOnlineOnlyLyricsChange: (Boolean) -> Unit,
    onLyricsSourcePreferenceChange: (chromahub.rhythm.app.data.LyricsSourcePreference) -> Unit,
    onOpenSystemEqualizer: () -> Unit,
    onBack: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val musicViewModel: MusicViewModel = viewModel()

    // Collect playlists for playlist management
    val playlists by musicViewModel.playlists.collectAsState()

    // Use AppSettings for theme settings
    val useSystemTheme by appSettings.useSystemTheme.collectAsState()
    val darkMode by appSettings.darkMode.collectAsState()
    val useDynamicColors by appSettings.useDynamicColors.collectAsState()

    // Lyrics Settings
    val lyricsSourcePreference by appSettings.lyricsSourcePreference.collectAsState()
    
    // Playback Settings
    val highQualityAudio by appSettings.highQualityAudio.collectAsState()
    val gaplessPlayback by appSettings.gaplessPlayback.collectAsState()
    val crossfade by appSettings.crossfade.collectAsState()
    val crossfadeDuration by appSettings.crossfadeDuration.collectAsState()
    val audioNormalization by appSettings.audioNormalization.collectAsState()
    val replayGain by appSettings.replayGain.collectAsState()

    // Cache Settings
    val maxCacheSize by appSettings.maxCacheSize.collectAsState()
    val clearCacheOnExit by appSettings.clearCacheOnExit.collectAsState()

    // Notification Settings
    val useCustomNotification by appSettings.useCustomNotification.collectAsState()

    val scope = rememberCoroutineScope()
    var showApiBottomSheet by remember { mutableStateOf(false) }
    var showCrossfadeDurationDialog by remember { mutableStateOf(false) }
    var showCacheSizeDialog by remember { mutableStateOf(false) }
    var showCrashLogHistoryBottomSheet by remember { mutableStateOf(false) } // New state for crash log history
    var showMediaScanBottomSheet by remember { mutableStateOf(false) } // New state for media scan management
    var showBackupRestoreBottomSheet by remember { mutableStateOf(false) } // New state for backup and restore
    var showCacheManagementBottomSheet by remember { mutableStateOf(false) } // New state for cache management
    var showEqualizerBottomSheet by remember { mutableStateOf(false) } // New state for equalizer
    var showSleepTimerBottomSheet by remember { mutableStateOf(false) } // New state for sleep timer
    var showPlaylistManagementBottomSheet by remember { mutableStateOf(false) } // New state for playlist management
    var showCreatePlaylistDialog by remember { mutableStateOf(false) } // New state for create playlist dialog
    var showLibraryTabOrderBottomSheet by remember { mutableStateOf(false) } // New state for library tab order
    var showThemeCustomizationBottomSheet by remember { mutableStateOf(false) } // New state for theme customization

    val updaterViewModel: AppUpdaterViewModel = viewModel()
    val currentAppVersion by updaterViewModel.currentVersion.collectAsState()

    val haptics = LocalHapticFeedback.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val coroutineScope = rememberCoroutineScope()

    // Search functionality
    var searchQuery by remember { mutableStateOf("") }
    var highlightedSection by remember { mutableStateOf<String?>(null) }
    val lazyListState = rememberLazyListState()

    // Handle back button when bottom sheets are open
    BackHandler(
        enabled = showApiBottomSheet || showCrashLogHistoryBottomSheet ||
                showMediaScanBottomSheet || showBackupRestoreBottomSheet ||
                showCacheManagementBottomSheet || showEqualizerBottomSheet ||
                showPlaylistManagementBottomSheet || showLibraryTabOrderBottomSheet ||
                showSleepTimerBottomSheet || showCrossfadeDurationDialog ||
                showThemeCustomizationBottomSheet
    ) {
        when {
            showApiBottomSheet -> showApiBottomSheet = false
            showCrashLogHistoryBottomSheet -> showCrashLogHistoryBottomSheet = false
            showMediaScanBottomSheet -> showMediaScanBottomSheet = false
            showBackupRestoreBottomSheet -> showBackupRestoreBottomSheet = false
            showCacheManagementBottomSheet -> showCacheManagementBottomSheet = false
            showEqualizerBottomSheet -> showEqualizerBottomSheet = false
            showPlaylistManagementBottomSheet -> showPlaylistManagementBottomSheet = false
            showLibraryTabOrderBottomSheet -> showLibraryTabOrderBottomSheet = false
            showThemeCustomizationBottomSheet -> showThemeCustomizationBottomSheet = false
            showSleepTimerBottomSheet -> showSleepTimerBottomSheet = false
            showCrossfadeDurationDialog -> showCrossfadeDurationDialog = false
        }
    }

    if (showApiBottomSheet) {
        ApiManagementBottomSheet(
            onDismiss = { showApiBottomSheet = false },
            appSettings = appSettings
        )
    }

    if (showCrossfadeDurationDialog) {
        CrossfadeDurationDialog(
            currentDuration = crossfadeDuration,
            onDismiss = { showCrossfadeDurationDialog = false },
            onSave = { duration ->
                appSettings.setCrossfadeDuration(duration)
                showCrossfadeDurationDialog = false
            }
        )
    }

    if (showCacheSizeDialog) {
        CacheSizeDialog(
            currentSize = maxCacheSize,
            onDismiss = { showCacheSizeDialog = false },
            onSave = { size ->
                appSettings.setMaxCacheSize(size)
                showCacheSizeDialog = false
            }
        )
    }

    if (showCrashLogHistoryBottomSheet) {
        CrashLogHistoryBottomSheet(
            onDismiss = { showCrashLogHistoryBottomSheet = false },
            appSettings = appSettings
        )
    }

    if (showMediaScanBottomSheet) {
        MediaScanBottomSheet(
            onDismiss = { showMediaScanBottomSheet = false },
            appSettings = appSettings,
            initialMode = MediaScanMode.BLACKLIST
        )
    }

    if (showBackupRestoreBottomSheet) {
        BackupRestoreBottomSheet(
            onDismiss = { showBackupRestoreBottomSheet = false },
            appSettings = appSettings
        )
    }

    if (showCacheManagementBottomSheet) {
        CacheManagementBottomSheet(
            onDismiss = { showCacheManagementBottomSheet = false },
            appSettings = appSettings,
            musicViewModel = musicViewModel
        )
    }

    if (showEqualizerBottomSheet) {
        EqualizerBottomSheetNew(
            musicViewModel = musicViewModel,
            onDismiss = { showEqualizerBottomSheet = false }
        )
    }

    if (showSleepTimerBottomSheet) {
        SleepTimerBottomSheetNew(
            onDismiss = { showSleepTimerBottomSheet = false },
            currentSong = currentSong,
            isPlaying = isPlaying,
            musicViewModel = musicViewModel
        )
    }

    if (showPlaylistManagementBottomSheet) {
        PlaylistManagementBottomSheet(
            onDismiss = { showPlaylistManagementBottomSheet = false },
            playlists = playlists,
            musicViewModel = musicViewModel,
            onCreatePlaylist = { showCreatePlaylistDialog = true },
            onDeletePlaylist = { playlist ->
                musicViewModel.deletePlaylist(playlist.id)
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                musicViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    if (showLibraryTabOrderBottomSheet) {
        LibraryTabOrderBottomSheet(
            onDismiss = { showLibraryTabOrderBottomSheet = false },
            appSettings = appSettings,
            haptics = haptics
        )
    }

    if (showThemeCustomizationBottomSheet) {
        ThemeCustomizationBottomSheet(
            onDismiss = { showThemeCustomizationBottomSheet = false },
            appSettings = appSettings
        )
    }

    // Searchable settings data structure
    data class SearchableSettingItem(
        val title: String,
        val description: String,
        val section: String,
        val keywords: List<String> = emptyList()
    )

    val searchableSettings = remember {
        listOf(
            // Display & Theme
            SearchableSettingItem(
                "Use System Theme",
                "Follow system dark/light theme setting automatically",
                "Display & Theme",
                listOf("system", "theme", "dark", "light", "automatic")
            ),
            SearchableSettingItem(
                "Dynamic Colors",
                "Use wallpaper-based colors (Android 12+)",
                "Display & Theme",
                listOf("dynamic", "colors", "wallpaper", "material you")
            ),
            SearchableSettingItem(
                "Dark Mode",
                "Enable dark theme",
                "Display & Theme",
                listOf("dark", "mode", "theme", "night")
            ),

            // User Interface & Controls
            SearchableSettingItem(
                "Haptic Feedback",
                "Vibrate when tapping buttons and interacting with the interface",
                "User Interface & Controls",
                listOf("haptic", "vibrate", "feedback", "touch")
            ),

            // Audio & Playback
            SearchableSettingItem(
                "Audio focus",
                "Pause when other apps play audio",
                "Audio & Playback",
                listOf("audio", "focus", "pause", "duck")
            ),
            SearchableSettingItem(
                "Skip silence",
                "Automatically skip silent parts in audio",
                "Audio & Playback",
                listOf("skip", "silence", "trim", "quiet")
            ),
            SearchableSettingItem(
                "Equalizer",
                "Adjust audio frequencies",
                "Audio & Playback",
                listOf("equalizer", "eq", "bass", "treble", "audio")
            ),
            SearchableSettingItem(
                "Audio format",
                "Select audio quality",
                "Audio & Playback",
                listOf("audio", "format", "quality", "bitrate")
            ),

            // Library
            SearchableSettingItem(
                "Scan Folder",
                "Add music folders to library",
                "Library & Content Display",
                listOf("scan", "folder", "music", "library", "add")
            ),
            SearchableSettingItem(
                "Sort Order",
                "Change library sorting",
                "Library & Content Display",
                listOf("sort", "order", "alphabetical", "date")
            ),
            SearchableSettingItem(
                "Tab Order",
                "Customize library tabs",
                "Library & Content Display",
                listOf("tab", "order", "library", "customize", "reorder")
            ),
            SearchableSettingItem(
                "Show Files",
                "Display all audio files",
                "Library & Content Display",
                listOf("files", "show", "display", "explorer")
            ),
            SearchableSettingItem(
                "Playlist Management",
                "Manage playlists",
                "Library & Content Display",
                listOf("playlist", "manage", "organize")
            ),

            // App Updates
            SearchableSettingItem(
                "Check for Updates",
                "Check for app updates",
                "App Updates",
                listOf("update", "check", "version", "new")
            ),
            
            // API
            SearchableSettingItem(
                "API Settings",
                "Configure API endpoints",
                "API",
                listOf("api", "endpoint", "server", "configure")
            ),

            // Storage & Cache
            SearchableSettingItem(
                "Cache Size",
                "Set maximum cache size",
                "Storage & Cache",
                listOf("cache", "size", "storage", "limit")
            ),
            SearchableSettingItem(
                "Clear Cache",
                "Delete cached data",
                "Storage & Cache",
                listOf("clear", "cache", "delete", "clean")
            ),
            SearchableSettingItem(
                "Backup & Restore",
                "Backup app data",
                "Storage & Cache",
                listOf("backup", "restore", "export", "import", "data")
            ),

            // Advanced
            SearchableSettingItem(
                "Crash Logs",
                "View crash history",
                "Advanced",
                listOf("crash", "log", "error", "debug")
            ),
            SearchableSettingItem(
                "Reset Settings",
                "Restore default settings",
                "Advanced",
                listOf("reset", "default", "restore", "clear")
            ),
            SearchableSettingItem(
                "About",
                "App version and info",
                "Advanced",
                listOf("about", "version", "info", "credits")
            )
        )
    }

    // Map section names to their approximate indices in the LazyColumn
    // Index 0 is search bar (when visible), actual sections start at index 1
    val sectionIndexMap = remember {
        mapOf(
            "Display & Theme" to 1,
            "User Interface & Controls" to 2,
            "Audio & Playback" to 3,
            "Library & Content Display" to 4,
            "Storage & Cache" to 5,
            "API" to 6,
            "App Updates" to 7,
            "Advanced" to 8,
            "About" to 9
        )
    }

    val filteredSettings = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            searchableSettings.filter { item ->
                val query = searchQuery.lowercase()
                item.title.lowercase().contains(query) ||
                        item.description.lowercase().contains(query) ||
                        item.keywords.any { it.contains(query) }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val expandedTextStyle =
                        MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    val collapsedTextStyle =
                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)

                    val fraction = scrollBehavior.state.collapsedFraction
                    val currentFontSize = lerp(
                        expandedTextStyle.fontSize.value,
                        collapsedTextStyle.fontSize.value,
                        fraction
                    ).sp
                    val currentFontWeight =
                        if (fraction < 0.5f) FontWeight.Bold else FontWeight.Bold // Changed to FontWeight.Bold

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = currentFontSize,
                            fontWeight = currentFontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp) // Added padding
                    )
                },
                navigationIcon = {
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(
                                context,
                                haptics,
                                HapticFeedbackType.LongPress
                            )
                            onBack()
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
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.padding(horizontal = 8.dp) // Added padding
            )
        },
        bottomBar = {
            if (false /* MiniPlayer handled globally */) {
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
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search TextField (always visible)
            item {
                Column {
                    // Enhanced search bar with improved colors, UI, and font styling
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(42.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Enhanced animated search icon with better styling
                            val iconRotation by animateFloatAsState(
                                targetValue = if (searchQuery.isNotEmpty()) 360f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "searchIconRotation"
                            )

                            val iconScale by animateFloatAsState(
                                targetValue = if (searchQuery.isNotEmpty()) 1.1f else 1.0f,
                                animationSpec = tween(300),
                                label = "searchIconScale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (searchQuery.isNotEmpty())
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier
                                        .size(26.dp)
                                        .graphicsLayer {
                                            rotationZ = iconRotation
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        },
                                    tint = if (searchQuery.isNotEmpty())
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Improved text field with better typography
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search Settings",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 18.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.7f
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            // Enhanced clear button with better visual feedback
                            androidx.compose.animation.AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = tween(200)
                                ) + fadeIn(animationSpec = tween(200)),
                                exit = scaleOut(
                                    targetScale = 0.8f,
                                    animationSpec = tween(150)
                                ) + fadeOut(animationSpec = tween(150))
                            ) {
                                IconButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(
                                            context,
                                            haptics,
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        searchQuery = ""
                                    },
                                    modifier = Modifier
                                        .size(35.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.error
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Search Results
            if (searchQuery.isNotEmpty()) {
                if (filteredSettings.isEmpty()) {
                    // Enhanced empty state with suggestions
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Animated search icon with subtle pulse effect
                            val animationState by rememberInfiniteTransition(
                                label = "emptyStateAnimation"
                            ).animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, delayMillis = 1000, easing = EaseInOutCubic),
                                    repeatMode = RepeatMode.Reverse
                                ), label = "pulseScale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(animationState)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "No settings found for \"$searchQuery\"",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Try searching for different terms like:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Popular search suggestions with enhanced styling
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                maxItemsInEachRow = 3
                            ) {
                                val suggestions = listOf(
                                    "theme", "audio", "playlist", "library", "cache",
                                    "lyrics", "equalizer", "notifications", "backup"
                                )

                                suggestions.forEach { suggestion ->
                                    Surface(
                                        onClick = {
                                            searchQuery = suggestion
                                            HapticUtils.performHapticFeedback(
                                                context,
                                                haptics,
                                                HapticFeedbackType.TextHandleMove
                                            )
                                        },
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(16.dp),
                                        shadowElevation = 1.dp,
                                        tonalElevation = 1.dp
                                    ) {
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Quick action buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        searchQuery = ""
                                        HapticUtils.performHapticFeedback(
                                            context,
                                            haptics,
                                            HapticFeedbackType.TextHandleMove
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Text(
                                        text = "Clear search",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                Button(
                                    onClick = {
                                        // Scroll to top section
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(0)
                                            searchQuery = ""
                                            HapticUtils.performHapticFeedback(
                                                context,
                                                haptics,
                                                HapticFeedbackType.LongPress
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = "Browse all",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tip text
                            Text(
                                text = "💡 Tip: Use keywords like colors, audio, or themes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Search results found
                    items(filteredSettings.size) { index ->
                        val item = filteredSettings[index]

                        // Stagger animation for search results
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 50 // Stagger delay
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 50
                                )
                            )
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        HapticUtils.performHapticFeedback(
                                            context,
                                            haptics,
                                            HapticFeedbackType.LongPress
                                        )
                                        // Highlight the section
                                        highlightedSection = item.section
                                        // Close search
                                        searchQuery = ""
                                        // Scroll to the section
                                        coroutineScope.launch {
                                            val targetIndex = sectionIndexMap[item.section] ?: 0
                                            lazyListState.animateScrollToItem(targetIndex)
                                            // Clear highlight after a delay
                                            delay(2500)
                                            highlightedSection = null
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "in ${item.section}",
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


            // Regular settings content (only show when not searching or search is empty)
            if (searchQuery.isEmpty()) {
                // Display & Theme section
                item {
                    SettingsSectionHeader(
                        title = "Display & Theme",
                        isHighlighted = highlightedSection == "Display & Theme"
                    )

                    // Theme settings moved to Theme Customization Bottom Sheet
                    // All theme customization including system theme, dynamic colors,
                    // dark mode, color schemes, and fonts are now accessible through
                    // the unified Theme Customization interface below

                    SettingsClickableItem(
                        title = "Theme Customization",
                        description = "Customize theme mode, colors, and fonts",
                        icon = Icons.Filled.Palette,
                        onClick = { showThemeCustomizationBottomSheet = true }
                    )

                    SettingsDivider()
                }

                // User Interface & Controls section
                item {
                    SettingsSectionHeader(
                        title = "User Interface & Controls",
                        isHighlighted = highlightedSection == "User Interface & Controls"
                    )

                    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
                    SettingsToggleItem(
                        title = "Haptic Feedback",
                        description = "Vibrate when tapping buttons and interacting with the interface",
                        icon = Icons.Filled.TouchApp,
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = { appSettings.setHapticFeedbackEnabled(it) }
                    )

                    SettingsDivider()
                }

                // Audio & Playback section
                item {
                    SettingsSectionHeader(
                        title = "Audio & Playback",
                        isHighlighted = highlightedSection == "Audio & Playback"
                    )

                    val useSystemVolume by appSettings.useSystemVolume.collectAsState()
                    SettingsToggleItem(
                        title = "System Volume Control",
                        description = "Use device volume controls for music playback",
                        icon = RhythmIcons.Player.VolumeUp,
                        checked = useSystemVolume,
                        onCheckedChange = {
                            appSettings.setUseSystemVolume(it)
                        }
                    )

                    // SettingsToggleItem(
                    //     title = "Custom notifications",
                    //     description = "Use app's custom notification instead of system media notification (experimental)",
                    //     icon = Icons.Filled.Notifications,
                    //     checked = useCustomNotification,
                    //     onCheckedChange = {
                    //         appSettings.setUseCustomNotification(it)
                    //         // Note: Full implementation requires Media3 notification provider integration
                    //     }
                    // )

                    // SettingsToggleItem(
                    //     title = "High quality audio",
                    //     description = "Enable higher bitrate audio streaming/playback",
                    //     icon = RhythmIcons.VolumeUp,
                    //     checked = highQualityAudio,
                    //     onCheckedChange = { appSettings.setHighQualityAudio(it) }
                    // )

                    // SettingsToggleItem(
                    //     title = "Gapless playback",
                    //     description = "Eliminate gaps between tracks for continuous listening",
                    //     icon = RhythmIcons.Queue,
                    //     checked = gaplessPlayback,
                    //     onCheckedChange = { appSettings.setGaplessPlayback(it) }
                    // )

                    // SettingsToggleItem(
                    //     title = "Crossfade",
                    //     description = "Smoothly transition between songs",
                    //     icon = RhythmIcons.Shuffle,
                    //     checked = crossfade,
                    //     onCheckedChange = { appSettings.setCrossfade(it) }
                    // )

                    // AnimatedVisibility(
                    //     visible = crossfade,
                    //     enter = fadeIn() + expandVertically(),
                    //     exit = fadeOut() + shrinkVertically()
                    // ) {
                    //     SettingsClickableItem(
                    //         title = "Crossfade duration",
                    //         description = "Set crossfade duration: ${crossfadeDuration.toInt()} seconds",
                    //         icon = RhythmIcons.Player.Timer,
                    //         onClick = { showCrossfadeDurationDialog = true }
                    //     )
                    // }

                    // SettingsToggleItem(
                    //     title = "Audio normalization",
                    //     description = "Adjust volume levels to a consistent loudness",
                    //     icon = RhythmIcons.VolumeUp,
                    //     checked = audioNormalization,
                    //     onCheckedChange = { appSettings.setAudioNormalization(it) }
                    // )

                    // SettingsToggleItem(
                    //     title = "ReplayGain",
                    //     description = "Apply ReplayGain tags for consistent playback volume",
                    //     icon = RhythmIcons.VolumeUp,
                    //     checked = replayGain,
                    //     onCheckedChange = { appSettings.setReplayGain(it) }
                    // )

                    SettingsToggleItem(
                        title = "Show Lyrics",
                        description = "Display lyrics when available",
                        icon = Icons.Filled.Lyrics,
                        checked = showLyrics,
                        onCheckedChange = {
                            onShowLyricsChange(it)
                        }
                    )

                    AnimatedVisibility(
                        visible = showLyrics,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        SettingsDropdownItem(
                            title = "Lyrics Source Priority",
                            description = "Choose which lyrics source to try first (with automatic fallback to others)",
                            selectedOption = lyricsSourcePreference.displayName,
                            icon = when (lyricsSourcePreference) {
                                chromahub.rhythm.app.data.LyricsSourcePreference.API_FIRST -> Icons.Filled.Cloud
                                chromahub.rhythm.app.data.LyricsSourcePreference.EMBEDDED_FIRST -> Icons.Filled.MusicNote
                                chromahub.rhythm.app.data.LyricsSourcePreference.LOCAL_FIRST -> Icons.Filled.Folder
                            },
                            iconTint = when (lyricsSourcePreference) {
                                chromahub.rhythm.app.data.LyricsSourcePreference.API_FIRST -> MaterialTheme.colorScheme.primary
                                chromahub.rhythm.app.data.LyricsSourcePreference.EMBEDDED_FIRST -> MaterialTheme.colorScheme.secondary
                                chromahub.rhythm.app.data.LyricsSourcePreference.LOCAL_FIRST -> MaterialTheme.colorScheme.tertiary
                            },
                            options = chromahub.rhythm.app.data.LyricsSourcePreference.values().map { it.displayName },
                            onOptionSelected = { displayName ->
                                val preference = chromahub.rhythm.app.data.LyricsSourcePreference.values()
                                    .find { it.displayName == displayName }
                                if (preference != null) {
                                    onLyricsSourcePreferenceChange(preference)
                                }
                            }
                        )
                    }
                    
                    // Queue & Shuffle Behavior Settings
//                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val shuffleUsesExoplayer by appSettings.shuffleUsesExoplayer.collectAsState()
                    SettingsToggleItem(
                        title = "Use ExoPlayer Shuffle",
                        description = "Let the media player handle shuffle (recommended: OFF for manual shuffle)",
                        icon = RhythmIcons.Shuffle,
                        checked = shuffleUsesExoplayer,
                        onCheckedChange = { appSettings.setShuffleUsesExoplayer(it) }
                    )
                    
                    val autoAddToQueue by appSettings.autoAddToQueue.collectAsState()
                    SettingsToggleItem(
                        title = "Auto-add to Queue",
                        description = "Automatically add related songs to queue when playing",
                        icon = RhythmIcons.Queue,
                        checked = autoAddToQueue,
                        onCheckedChange = { appSettings.setAutoAddToQueue(it) }
                    )
                    
                    val clearQueueOnNewSong by appSettings.clearQueueOnNewSong.collectAsState()
                    SettingsToggleItem(
                        title = "Clear Queue on New Song",
                        description = "Clear the current queue when playing a new song directly",
                        icon = RhythmIcons.Delete,
                        checked = clearQueueOnNewSong,
                        onCheckedChange = { appSettings.setClearQueueOnNewSong(it) }
                    )
                    
                    val repeatModePersistence by appSettings.repeatModePersistence.collectAsState()
                    SettingsToggleItem(
                        title = "Remember Repeat Mode",
                        description = "Keep repeat mode setting between app sessions",
                        icon = RhythmIcons.Repeat,
                        checked = repeatModePersistence,
                        onCheckedChange = { appSettings.setRepeatModePersistence(it) }
                    )

//                SettingsClickableItem(
//                    title = "Sleep timer",
//                    description = "Set a timer to automatically stop playback",
//                    icon = Icons.Filled.AccessTime,
//                    onClick = { showSleepTimerBottomSheet = true }
//                )

                    SettingsClickableItem(
                        title = "Equalizer",
                        description = "Adjust audio frequencies and sound effects",
                        icon = Icons.Filled.GraphicEq,
                        onClick = { showEqualizerBottomSheet = true }
                    )

                    SettingsDivider()
                }

                // Library & Content Display section
                item {
                    SettingsSectionHeader(
                        title = "Library & Content Display",
                        isHighlighted = highlightedSection == "Library & Content Display"
                    )

                    // val albumViewType by appSettings.albumViewType.collectAsState()

                    // SettingsDropdownItem(
                    //     title = "Album view type",
                    //     description = "Choose how albums are displayed",
                    //     selectedOption = albumViewType.name.lowercase().replaceFirstChar { it.uppercase() },
                    //     icon = Icons.Filled.Album,
                    //     options = AlbumViewType.values().map {
                    //         it.name.lowercase().replaceFirstChar { char -> char.uppercase() }
                    //     },
                    //     onOptionSelected = { selectedOption ->
                    //         val newViewType = AlbumViewType.values().find {
                    //             it.name.lowercase().replaceFirstChar { char -> char.uppercase() } == selectedOption
                    //         } ?: AlbumViewType.LIST
                    //        appSettings.setAlbumViewType(newViewType)
                    //     }
                    // )
                    // SettingsDivider()

                    // val artistViewType by appSettings.artistViewType.collectAsState()

                    // SettingsDropdownItem(
                    //     title = "Artist view type",
                    //     description = "Choose how artists are displayed",
                    //     selectedOption = artistViewType.name.lowercase().replaceFirstChar { it.uppercase() },
                    //     icon = Icons.Filled.Person,
                    //     options = ArtistViewType.values().map {
                    //         it.name.lowercase().replaceFirstChar { char -> char.uppercase() }
                    //     },
                    //     onOptionSelected = { selectedOption ->
                    //         val newViewType = ArtistViewType.values().find {
                    //             it.name.lowercase().replaceFirstChar { char -> char.uppercase() } == selectedOption
                    //         } ?: ArtistViewType.LIST
                    //         appSettings.setArtistViewType(newViewType)
                    //     }
                    // )
                    // SettingsDivider()

                    // val albumSortOrder by appSettings.albumSortOrder.collectAsState()

                    // SettingsDropdownItem(
                    //     title = "Album sort order",
                    //     description = "Choose how songs are sorted on albums",
                    //     selectedOption = when (AlbumSortOrder.valueOf(albumSortOrder)) {
                    //         AlbumSortOrder.TRACK_NUMBER -> "Track Number"
                    //         AlbumSortOrder.TITLE_ASC -> "Title A-Z"
                    //         AlbumSortOrder.TITLE_DESC -> "Title Z-A"
                    //         AlbumSortOrder.DURATION_ASC -> "Duration ↑"
                    //         AlbumSortOrder.DURATION_DESC -> "Duration ↓"
                    //     },
                    //     icon = RhythmIcons.Actions.Sort,
                    //     options = listOf("Track Number", "Title A-Z", "Title Z-A", "Duration ↑", "Duration ↓"),
                    //     onOptionSelected = { selectedOption ->
                    //         val newSortOrder = when (selectedOption) {
                    //             "Track Number" -> AlbumSortOrder.TRACK_NUMBER
                    //             "Title A-Z" -> AlbumSortOrder.TITLE_ASC
                    //             "Title Z-A" -> AlbumSortOrder.TITLE_DESC
                    //             "Duration ↑" -> AlbumSortOrder.DURATION_ASC
                    //             "Duration ↓" -> AlbumSortOrder.DURATION_DESC
                    //             else -> AlbumSortOrder.TRACK_NUMBER
                    //         }
                    //         appSettings.setAlbumSortOrder(newSortOrder.name)
                    //     }
                    // )
                    // SettingsDivider()

                    // val artistCollaborationMode by appSettings.artistCollaborationMode.collectAsState()

                    // SettingsToggleItem(
                    //     title = "Show artist collaborations",
                    //     description = "Show featured artists and collaborations in artist listings (Default mode filters them)",
                    //     checked = artistCollaborationMode,
                    //     icon = RhythmIcons.Artist,
                    //     onCheckedChange = { enabled ->
                    //         appSettings.setArtistCollaborationMode(enabled)
                    //     }
                    // )
                    
                    // Group by Album Artist Setting
                    val groupByAlbumArtist by appSettings.groupByAlbumArtist.collectAsState()

                    SettingsToggleItem(
                        title = "Group by Album Artist",
                        description = "Group artists by album artist instead of track artist. When enabled, collaboration albums appear under the main artist.",
                        checked = groupByAlbumArtist,
                        icon = Icons.Filled.Person,
                        onCheckedChange = { enabled ->
                            appSettings.setGroupByAlbumArtist(enabled)
                            // Trigger library refresh to reload artists
                            musicViewModel.refreshLibrary()
                        }
                    )

                    // Playlist Management
                    val userPlaylists = playlists.filter { !it.isDefault }
                    val defaultPlaylists = playlists.filter { it.isDefault }

                    SettingsChipItem(
                        title = "Manage Playlists",
                        description = "Create, import, export and organize playlists",
                        primaryChipText = "${userPlaylists.size} custom",
                        secondaryChipText = "${defaultPlaylists.size} default",
                        icon = Icons.Rounded.PlaylistPlay,
                        primaryChipColor = MaterialTheme.colorScheme.primaryContainer,
                        primaryChipTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        secondaryChipColor = MaterialTheme.colorScheme.secondaryContainer,
                        secondaryChipTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = {
                            showPlaylistManagementBottomSheet = true
                        }
                    )

                    // SettingsDivider()

                    // Library Tab Order
                    val tabOrder by appSettings.libraryTabOrder.collectAsState()
                    val tabNames = tabOrder.map { tabId ->
                        when (tabId) {
                            "SONGS" -> "Songs"
                            "PLAYLISTS" -> "Playlists"
                            "ALBUMS" -> "Albums"
                            "ARTISTS" -> "Artists"
                            "EXPLORER" -> "Explorer"
                            else -> tabId
                        }
                    }

                    // Limit the secondary chip text to prevent overflow
                    val secondaryTabText = if (tabNames.size > 1) {
                        val remaining = tabNames.drop(1).take(2) // Show max 2 more tabs
                        val text = remaining.joinToString(" → ")
                        if (tabNames.size > 3) "$text..." else text
                    } else ""

                    SettingsChipItem(
                        title = "Tab Order",
                        description = "Reorder tabs to customize library",
                        primaryChipText = tabNames.getOrNull(0) ?: "Songs",
                        secondaryChipText = secondaryTabText,
                        icon = Icons.Rounded.Reorder,
                        primaryChipColor = MaterialTheme.colorScheme.tertiaryContainer,
                        primaryChipTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        secondaryChipColor = MaterialTheme.colorScheme.surfaceVariant,
                        secondaryChipTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            showLibraryTabOrderBottomSheet = true
                        }
                    )

                    val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
                    val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()

                    // Get the effective blacklist counts using the same method as BlacklistManagementBottomSheet
                    val allSongs by musicViewModel.songs.collectAsState()
                    val filteredSongs by musicViewModel.filteredSongs.collectAsState()

                    // Track if data is still being calculated to show loading state
                    val isCalculatingCounts = remember(allSongs, filteredSongs, blacklistedSongs, blacklistedFolders) {
                        allSongs.isNotEmpty() && filteredSongs.isEmpty() && (blacklistedSongs.isNotEmpty() || blacklistedFolders.isNotEmpty())
                    }

                    // Calculate effective blacklist counts considering both individual songs and folder blacklists
                    val totalSongsCount = allSongs.size
                    val availableSongsCount =
                        filteredSongs.size // This already excludes blacklisted songs
                    val effectivelyBlacklistedSongsCount = totalSongsCount - availableSongsCount
                    val blacklistedFoldersCount = blacklistedFolders.size

                    SettingsChipItem(
                        title = "Manage Media Scan",
                        description = "Control blacklist and whitelist for media scanning",
                        primaryChipText = if (isCalculatingCounts) "Loading..." else "$effectivelyBlacklistedSongsCount blocked",
                        secondaryChipText = "$blacklistedFoldersCount folders",
                        icon = Icons.Filled.FilterList,
                        primaryChipColor = MaterialTheme.colorScheme.errorContainer,
                        primaryChipTextColor = MaterialTheme.colorScheme.onErrorContainer,
                        secondaryChipColor = MaterialTheme.colorScheme.tertiaryContainer,
                        secondaryChipTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            showMediaScanBottomSheet = true
                        }
                    )

                    SettingsDivider()
                }

                // Data & Storage section
                item {
                    SettingsSectionHeader(
                        title = "Storage & Cache",
                        isHighlighted = highlightedSection == "Storage & Cache"
                    )

                    // Cache Management - consolidated into one item
                    SettingsChipItem(
                        title = "Manage Cache",
                        description = "Control cache size, automatic clearing, and manual cleanup",
                        primaryChipText = "${
                            String.format(
                                "%.1f",
                                maxCacheSize / (1024f * 1024f)
                            )
                        } MB",
                        secondaryChipText = if (clearCacheOnExit) "Auto-clear ON" else "Manual only",
                        icon = Icons.Filled.Storage,
                        primaryChipColor = MaterialTheme.colorScheme.primaryContainer,
                        primaryChipTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        secondaryChipColor = if (clearCacheOnExit)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        secondaryChipTextColor = if (clearCacheOnExit)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { showCacheManagementBottomSheet = true }
                    )

                    val autoBackupEnabled by appSettings.autoBackupEnabled.collectAsState()
                    val lastBackupTimestamp by appSettings.lastBackupTimestamp.collectAsState()

                    SettingsChipItem(
                        title = "Backup & Restore",
                        description = "Backup your settings, playlists, and preferences",
                        primaryChipText = if (lastBackupTimestamp > 0) {
                            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                            "Last: ${sdf.format(Date(lastBackupTimestamp))}"
                        } else "Never backed up",
                        secondaryChipText = if (autoBackupEnabled) "Auto-backup ON" else "Manual only",
                        icon = Icons.Filled.Backup,
                        primaryChipColor = if (lastBackupTimestamp > 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        primaryChipTextColor = if (lastBackupTimestamp > 0)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        secondaryChipColor = if (autoBackupEnabled)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        secondaryChipTextColor = if (autoBackupEnabled)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            HapticUtils.performHapticFeedback(
                                context,
                                haptics,
                                HapticFeedbackType.TextHandleMove
                            )
                            showBackupRestoreBottomSheet = true
                        }
                    )

                    SettingsDivider()
                }


                // Integrations & Services section
                item {
                    SettingsSectionHeader(
                        title = "API",
                        isHighlighted = highlightedSection == "Updates & API"
                    )

                    SettingsClickableItem(
                        title = "Manage API Settings",
                        description = "Configure external API keys and services",
                        icon = Icons.Filled.Api,
                        onClick = {
                            HapticUtils.performHapticFeedback(
                                context,
                                haptics,
                                HapticFeedbackType.TextHandleMove
                            )
                            showApiBottomSheet = true
                        }
                    )

                    SettingsDivider()
                }

                // App Updates section
                item {
                    SettingsSectionHeader(
                        title = "App Updates",
                        isHighlighted = highlightedSection == "App Updates"
                    )

                    val autoCheckForUpdates by appSettings.autoCheckForUpdates.collectAsState()
                    val updateChannel by appSettings.updateChannel.collectAsState()
                    val updatesEnabled by appSettings.updatesEnabled.collectAsState()

                    SettingsToggleItem(
                        title = "Enable Updates",
                        description = "Allow the app to check for and download updates",
                        icon = Icons.Filled.SystemUpdate,
                        checked = updatesEnabled,
                        onCheckedChange = { appSettings.setUpdatesEnabled(it) }
                    )

                    AnimatedVisibility(
                        visible = updatesEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            SettingsToggleItem(
                                title = "Periodic Check",
                                description = "Check for updates from Rhythm's GitHub repo automatically",
                                icon = Icons.Filled.Update,
                                checked = autoCheckForUpdates,
                                onCheckedChange = {
                                    appSettings.setAutoCheckForUpdates(it)
                                }
                            )

                            AnimatedVisibility(
                                visible = autoCheckForUpdates,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                EnhancedUpdateChannelOption(
                                    currentChannel = updateChannel,
                                    onChannelChange = { appSettings.setUpdateChannel(it) }
                                )
                            }
                        }
                    }

                    SettingsDivider()
                }

                // Troubleshooting & Diagnostics section
                item {
                    SettingsSectionHeader(
                        title = "Advanced",
                        isHighlighted = highlightedSection == "Advanced"
                    )

                    SettingsClickableItem(
                        title = "Crash Log History",
                        description = "View and manage past crash reports",
                        icon = Icons.Filled.BugReport,
                        onClick = {
                            showCrashLogHistoryBottomSheet = true
                        }
                    )

                    SettingsDivider()
                }

                // About section
                item {
                    SettingsSectionHeader(
                        title = "About",
                        isHighlighted = highlightedSection == "About"
                    )

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToAbout() }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.rhythm_splash_logo),
                                    contentDescription = "Rhythm Logo",
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.width(3.dp))

                                Text(
                                    text = "Rhythm",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Music Player",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentAppVersion.versionName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToAbout,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "About",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }

                                Button(
                                    onClick = onCheckForUpdates,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = RhythmIcons.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Updates",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://t.me/RhythmSupport")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Telegram,
                                        contentDescription = "Telegram Support",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Support Group",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Extra bottom space for mini player
                item {
                    Spacer(modifier = Modifier.height(0.dp))
                }
            } // End of if (!showSearch || searchQuery.isEmpty())
        }
    }
}

    @Composable
    fun SettingsSectionHeader(title: String, isHighlighted: Boolean = false) {
        val context = LocalContext.current

        // Animated background color for highlighting
        val backgroundColor by animateColorAsState(
            targetValue = if (isHighlighted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            },
            animationSpec = tween(durationMillis = 500),
            label = "sectionHighlight"
        )

        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = title,
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

    @Composable
    fun SettingsToggleItem(
        title: String,
        description: String,
        icon: ImageVector,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
        val scaleAnim by animateFloatAsState(
            targetValue = if (checked) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 300f
            ),
            label = "toggleAnimation"
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onCheckedChange(!checked)
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (checked)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Switch
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        onCheckedChange(it)
                        HapticUtils.performHapticFeedback(
                            context,
                            haptic,
                            HapticFeedbackType.LongPress
                        )
                    },
                    modifier = Modifier.scale(scaleAnim),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }

    @Composable
    fun SettingsClickableItem(
        title: String,
        description: String,
        icon: ImageVector,
        iconTint: Color = MaterialTheme.colorScheme.primary, // Added iconTint parameter
        onClick: () -> Unit
    ) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Arrow icon
                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun SettingsDropdownItem(
        title: String,
        description: String,
        selectedOption: String,
        options: List<String>,
        icon: ImageVector,
        iconTint: Color = MaterialTheme.colorScheme.primary,
        onOptionSelected: (String) -> Unit
    ) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
        var showDropdown by remember { mutableStateOf(false) }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    showDropdown = true
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Current selection badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = selectedOption,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dropdown arrow
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Show options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Enhanced Dropdown Menu
            Box {
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    options.forEach { option ->
                        Surface(
                            color = if (selectedOption == option)
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
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedOption == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedOption == option)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when {
                                            selectedOption == option -> Icons.Filled.Check
                                            option == "API" -> Icons.Filled.Cloud
                                            option == "Embedded" -> Icons.Filled.MusicNote
                                            option == "Local" -> Icons.Filled.Folder
                                            option == "Track Number" -> Icons.Filled.FormatListNumbered
                                            option == "Title A-Z" || option == "Title Z-A" -> Icons.Filled.SortByAlpha
                                            option == "Duration ↑" || option == "Duration ↓" -> Icons.Filled.AccessTime
                                            option == "Stable" -> Icons.Filled.Public
                                            option == "Beta" -> Icons.Filled.BugReport
                                            option == "List" -> RhythmIcons.Actions.List
                                            option == "Grid" -> Icons.Filled.GridView
                                            else -> Icons.Filled.Check // Fallback
                                        },
                                        contentDescription = null,
                                        tint = if (selectedOption == option)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    onOptionSelected(option)
                                    showDropdown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = if (selectedOption == option)
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
    }

    @Composable
    fun SettingsChipItem(
        title: String,
        description: String,
        primaryChipText: String,
        secondaryChipText: String,
        icon: ImageVector,
        iconTint: Color = MaterialTheme.colorScheme.primary,
        primaryChipColor: Color = MaterialTheme.colorScheme.primaryContainer,
        primaryChipTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        secondaryChipColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        secondaryChipTextColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick: () -> Unit
    ) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chips
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.widthIn(max = 140.dp) // Limit chip width
                ) {
                    Surface(
                        color = primaryChipColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = primaryChipText,
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryChipTextColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (secondaryChipText.isNotEmpty()) {
                        Surface(
                            color = secondaryChipColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = secondaryChipText,
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryChipTextColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Forward arrow
                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    @Composable
    fun SettingsDivider() {
        Spacer(modifier = Modifier.height(8.dp))
    }

    @Composable
    fun AboutDialog(
        onDismiss: () -> Unit,
        onCheckForUpdates: () -> Unit = {},
        currentAppVersion: chromahub.rhythm.app.viewmodel.AppVersion // Pass currentAppVersion
    ) {
        val context = LocalContext.current // Get context for opening URL
        val haptics = LocalHapticFeedback.current
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = null,
            title = null,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Use the splash logo for a more impressive visual
                    Image(
                        painter = painterResource(id = R.drawable.rhythm_splash_logo),
                        contentDescription = null,
                        modifier = Modifier.size(140.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Rhythm",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Music Player",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Version ${currentAppVersion.versionName}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // Add a check for updates button
//                Button(
//                    onClick = {
//                        onDismiss()  // Close the dialog first
//                        onCheckForUpdates()  // Then navigate to updates screen
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                    ),
//                    modifier = Modifier
//                        .padding(top = 12.dp)
//                        .fillMaxWidth()
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Icon(
//                            imageVector = RhythmIcons.Download,
//                            contentDescription = null,
//                            modifier = Modifier.size(18.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Check for Updates")
//                    }
//                }

                    // Add a Report Bug button inside the dialog
                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(
                                context,
                                haptics,
                                HapticFeedbackType.TextHandleMove
                            )
                            onDismiss() // Close the dialog first
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/cromaguy/Rhythm/issues")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Edit,
                                contentDescription = "Report Bug",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Report Bug")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "By Anjishnu Nandi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A modern music player showcasing Material 3 Expressive design with physics-based animations.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Song,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Playlist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Album,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    @Composable
    fun EnhancedUpdateChannelOption(
        currentChannel: String,
        onChannelChange: (String) -> Unit
    ) {
        SettingsDropdownItem(
            title = "Update Channel",
            description = "Choose how you receive updates",
            selectedOption = currentChannel.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            },
            icon = if (currentChannel == "beta") Icons.Default.BugReport else Icons.Default.Public,
            iconTint = if (currentChannel == "beta") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            options = listOf("Stable", "Beta"),
            onOptionSelected = { selectedOption ->
                onChannelChange(selectedOption.lowercase(Locale.ROOT))
            }
        )
    }

    @Composable
    fun CrossfadeDurationDialog(
        currentDuration: Float,
        onDismiss: () -> Unit,
        onSave: (Float) -> Unit
    ) {
        val context = LocalContext.current
        var sliderPosition by remember { mutableFloatStateOf(currentDuration) }
        val haptics = LocalHapticFeedback.current

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Crossfade Duration") },
            text = {
                Column {
                    Text("Set the duration for crossfading between songs.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..10f, // 0 to 10 seconds
                        steps = 9, // 10 steps for 0-10 seconds (0, 1, 2...10)
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${sliderPosition.toInt()} seconds",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    HapticUtils.performHapticFeedback(
                        context,
                        haptics,
                        HapticFeedbackType.TextHandleMove
                    )
                    onSave(sliderPosition)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    @Composable
    fun CacheSizeDialog(
        currentSize: Long,
        onDismiss: () -> Unit,
        onSave: (Long) -> Unit
    ) {
        val context = LocalContext.current
        val haptics = LocalHapticFeedback.current

        // Convert size ranges to slider values (in MB)
        val minSize = 64L * 1024L * 1024L // 64 MB
        val maxSize = 2048L * 1024L * 1024L // 2 GB
        val stepSize = 64L * 1024L * 1024L // 64 MB steps

        // Current size in MB for slider
        val currentSizeMB = (currentSize / (1024L * 1024L)).coerceIn(64L, 2048L)
        var selectedSizeMB by remember { mutableFloatStateOf(currentSizeMB.toFloat()) }

        // Helper function to format size display
        fun formatSizeDisplay(sizeMB: Float): String {
            return when {
                sizeMB >= 1024f -> "${String.format("%.1f", sizeMB / 1024f)} GB"
                else -> "${sizeMB.toInt()} MB"
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Max Cache Size",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set the maximum size for cached audio and artwork.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Current selection display
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = formatSizeDisplay(selectedSizeMB),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Cache Size Limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Slider
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "64 MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "2 GB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = selectedSizeMB,
                            onValueChange = { newValue ->
                                // Snap to 64MB increments
                                val snappedValue =
                                    ((newValue / 64f).toInt() * 64f).coerceIn(64f, 2048f)
                                selectedSizeMB = snappedValue
                                HapticUtils.performHapticFeedback(
                                    context,
                                    haptics,
                                    HapticFeedbackType.TextHandleMove
                                )
                            },
                            valueRange = 64f..2048f,
                            steps = ((2048 - 64) / 64) - 1, // Number of steps between min and max
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick size options
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickSizes = listOf(128f, 256f, 512f, 1024f)
                        val quickLabels = listOf("128MB", "256MB", "512MB", "1GB")

                        quickSizes.forEachIndexed { index, size ->
                            Surface(
                                onClick = {
                                    selectedSizeMB = size
                                    HapticUtils.performHapticFeedback(
                                        context,
                                        haptics,
                                        HapticFeedbackType.TextHandleMove
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedSizeMB == size)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = quickLabels[index],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedSizeMB == size)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(
                            context,
                            haptics,
                            HapticFeedbackType.LongPress
                        )
                        val sizeInBytes = selectedSizeMB.toLong() * 1024L * 1024L
                        onSave(sizeInBytes)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }


    @Composable
    private fun CompactInfoItem(
        icon: ImageVector,
        text: String
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SpotifyApiConfigDialog(
        currentClientId: String,
        currentClientSecret: String,
        onDismiss: () -> Unit,
        onSave: (clientId: String, clientSecret: String) -> Unit,
        appSettings: chromahub.rhythm.app.data.AppSettings
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var clientId by remember { mutableStateOf(currentClientId) }
        var clientSecret by remember { mutableStateOf(currentClientSecret) }
        var isTestingConnection by remember { mutableStateOf(false) }
        var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Spotify API Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter your Spotify API credentials to enable track search and Canvas videos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Check if using default keys and display a warning
                    val isUsingDefaultKeys =
                        currentClientId.isEmpty() && currentClientSecret.isEmpty()
                    if (isUsingDefaultKeys) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You are currently using the app's default Spotify keys. For full functionality and to avoid rate limits, please input your own Client ID and Secret.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        // Spacer(modifier = Modifier.height(2.dp))
                    }

                    OutlinedTextField(
                        value = clientId,
                        onValueChange = {
                            clientId = it
                            testResult = null
                        },
                        label = { Text("Client ID") },
                        placeholder = { Text("Your Spotify Client ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = clientSecret,
                        onValueChange = {
                            clientSecret = it
                            testResult = null
                        },
                        label = { Text("Client Secret") },
                        placeholder = { Text("Your Spotify Client Secret") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Test connection button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isTestingConnection = true
                                    try {
                                        // Temporarily set the credentials for testing
                                        appSettings.setSpotifyClientId(clientId)
                                        appSettings.setSpotifyClientSecret(clientSecret)

                                        val canvasRepository =
                                            chromahub.rhythm.app.data.CanvasRepository(
                                                context,
                                                appSettings
                                            )
                                        testResult = canvasRepository.testSpotifyApiConfiguration()
                                    } catch (e: Exception) {
                                        testResult = Pair(false, "Error: ${e.message}")
                                    } finally {
                                        isTestingConnection = false
                                    }
                                }
                            },
                            enabled = !isTestingConnection && clientId.isNotBlank() && clientSecret.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isTestingConnection) "Testing..." else "Test Connection")
                        }
                    }

                    // Test result display
                    testResult?.let { (success, message) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (success)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (success)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (success)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Help text
                    Text(
                        text = "To get your Spotify API credentials:\n• Go to developer.spotify.com\n• Create a new app\n• Copy the Client ID and Client Secret",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(clientId, clientSecret)
                    },
                    enabled = clientId.isNotBlank() && clientSecret.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            }
        )
    }
