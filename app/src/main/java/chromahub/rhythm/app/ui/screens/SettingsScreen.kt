@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.text.ExperimentalTextApi::class)
package chromahub.rhythm.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
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
    onOpenSystemEqualizer: () -> Unit,
    onBack: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val musicViewModel: MusicViewModel = viewModel()
    
    // Use AppSettings for theme settings
    val useSystemTheme by appSettings.useSystemTheme.collectAsState()
    val darkMode by appSettings.darkMode.collectAsState()
    val useDynamicColors by appSettings.useDynamicColors.collectAsState()

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
    
    val spotifyKey by appSettings.spotifyApiKey.collectAsState()
    val scope = rememberCoroutineScope()
    var showApiBottomSheet by remember { mutableStateOf(false) }
    var showCrossfadeDurationDialog by remember { mutableStateOf(false) }
    var showCacheSizeDialog by remember { mutableStateOf(false) }
    var showCrashLogHistoryBottomSheet by remember { mutableStateOf(false) } // New state for crash log history
    var showBlacklistManagementBottomSheet by remember { mutableStateOf(false) } // New state for blacklist management
    var showBackupRestoreBottomSheet by remember { mutableStateOf(false) } // New state for backup and restore
    var showCacheManagementBottomSheet by remember { mutableStateOf(false) } // New state for cache management

    val updaterViewModel: AppUpdaterViewModel = viewModel()
    val currentAppVersion by updaterViewModel.currentVersion.collectAsState()

    val haptics = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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

    if (showBlacklistManagementBottomSheet) {
        BlacklistManagementBottomSheet(
            onDismiss = { showBlacklistManagementBottomSheet = false },
            appSettings = appSettings
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val expandedTextStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    val collapsedTextStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)

                    val fraction = scrollBehavior.state.collapsedFraction
                    val currentFontSize = lerp(expandedTextStyle.fontSize.value, collapsedTextStyle.fontSize.value, fraction).sp
                    val currentFontWeight = if (fraction < 0.5f) FontWeight.Bold else FontWeight.Bold // Changed to FontWeight.Bold

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
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Display & Theme section
            item {
                SettingsSectionHeader(title = "Display & Theme")

                SettingsToggleItem(
                    title = "Use system theme",
                    description = "Follow system dark/light theme setting automatically",
                    icon = Icons.Filled.Settings,
                    checked = useSystemTheme,
                    onCheckedChange = {
                        appSettings.setUseSystemTheme(it)
                    }
                )

                SettingsToggleItem(
                    title = "Dynamic colors",
                    description = "Use wallpaper-based colors (Android 12+)",
                    icon = Icons.Filled.Palette,
                    checked = useDynamicColors,
                    onCheckedChange = {
                        appSettings.setUseDynamicColors(it)
                    }
                )

                AnimatedVisibility(
                    visible = !useSystemTheme,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingsToggleItem(
                        title = "Dark mode",
                        description = "Enable dark theme",
                        icon = Icons.Filled.DarkMode,
                        checked = darkMode,
                        onCheckedChange = {
                            appSettings.setDarkMode(it)
                        }
                    )
                }

                SettingsDivider()
            }
            
            // User Interface & Controls section
            item {
                SettingsSectionHeader(title = "User Interface & Controls")

                val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
                SettingsToggleItem(
                    title = "Haptic feedback",
                    description = "Vibrate when tapping buttons and interacting with the interface",
                    icon = Icons.Filled.TouchApp,
                    checked = hapticFeedbackEnabled,
                    onCheckedChange = { appSettings.setHapticFeedbackEnabled(it) }
                )

                SettingsDivider()
            }
            
            // Audio & Playback section
            item {
                SettingsSectionHeader(title = "Audio & Playback")

                val useSystemVolume by appSettings.useSystemVolume.collectAsState()
                SettingsToggleItem(
                    title = "System volume control",
                    description = "Use device volume controls for music playback",
                    icon = RhythmIcons.Player.VolumeUp,
                    checked = useSystemVolume,
                    onCheckedChange = {
                        appSettings.setUseSystemVolume(it)
                    }
                )

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
                    title = "Show lyrics",
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
                ) {                        SettingsToggleItem(
                            title = "Online lyrics only",
                            description = "Only show lyrics when connected to the internet",
                            icon = Icons.Filled.Cloud,
                            checked = showOnlineOnlyLyrics,
                            onCheckedChange = {
                                onShowOnlineOnlyLyricsChange(it)
                            }
                        )
                }

                SettingsClickableItem(
                    title = "Equalizer",
                    description = "Open system equalizer to adjust audio frequencies",
                    icon = Icons.Filled.GraphicEq,
                    onClick = onOpenSystemEqualizer
                )

                SettingsDivider()
            }

            // Library & Content Display section
            item {
                SettingsSectionHeader(title = "Library & Content Display")

                val albumViewType by appSettings.albumViewType.collectAsState()
                
                SettingsDropdownItem(
                    title = "Album view type",
                    description = "Choose how albums are displayed",
                    selectedOption = albumViewType.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Filled.Album,
                    options = AlbumViewType.values().map { 
                        it.name.lowercase().replaceFirstChar { char -> char.uppercase() } 
                    },
                    onOptionSelected = { selectedOption ->
                        val newViewType = AlbumViewType.values().find { 
                            it.name.lowercase().replaceFirstChar { char -> char.uppercase() } == selectedOption 
                        } ?: AlbumViewType.LIST
                       appSettings.setAlbumViewType(newViewType)
                    }
                )
                SettingsDivider()

                val artistViewType by appSettings.artistViewType.collectAsState()
                
                SettingsDropdownItem(
                    title = "Artist view type",
                    description = "Choose how artists are displayed",
                    selectedOption = artistViewType.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Filled.Person,
                    options = ArtistViewType.values().map { 
                        it.name.lowercase().replaceFirstChar { char -> char.uppercase() } 
                    },
                    onOptionSelected = { selectedOption ->
                        val newViewType = ArtistViewType.values().find { 
                            it.name.lowercase().replaceFirstChar { char -> char.uppercase() } == selectedOption 
                        } ?: ArtistViewType.LIST
                        appSettings.setArtistViewType(newViewType)
                    }
                )
                SettingsDivider()

                val albumSortOrder by appSettings.albumSortOrder.collectAsState()
                
                SettingsDropdownItem(
                    title = "Album sort order",
                    description = "Choose how songs are sorted on albums",
                    selectedOption = when (AlbumSortOrder.valueOf(albumSortOrder)) {
                        AlbumSortOrder.TRACK_NUMBER -> "Track Number"
                        AlbumSortOrder.TITLE_ASC -> "Title A-Z"
                        AlbumSortOrder.TITLE_DESC -> "Title Z-A"
                        AlbumSortOrder.DURATION_ASC -> "Duration ↑"
                        AlbumSortOrder.DURATION_DESC -> "Duration ↓"
                    },
                    icon = RhythmIcons.Actions.Sort,
                    options = listOf("Track Number", "Title A-Z", "Title Z-A", "Duration ↑", "Duration ↓"),
                    onOptionSelected = { selectedOption ->
                        val newSortOrder = when (selectedOption) {
                            "Track Number" -> AlbumSortOrder.TRACK_NUMBER
                            "Title A-Z" -> AlbumSortOrder.TITLE_ASC
                            "Title Z-A" -> AlbumSortOrder.TITLE_DESC
                            "Duration ↑" -> AlbumSortOrder.DURATION_ASC
                            "Duration ↓" -> AlbumSortOrder.DURATION_DESC
                            else -> AlbumSortOrder.TRACK_NUMBER
                        }
                        appSettings.setAlbumSortOrder(newSortOrder.name)
                    }
                )

                val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
                val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()
                
                // Get the effective blacklist counts using the same method as BlacklistManagementBottomSheet
                val allSongs by musicViewModel.songs.collectAsState()
                val filteredSongs by musicViewModel.filteredSongs.collectAsState()
                
                // Calculate effective blacklist counts considering both individual songs and folder blacklists
                val totalSongsCount = allSongs.size
                val availableSongsCount = filteredSongs.size // This already excludes blacklisted songs
                val effectivelyBlacklistedSongsCount = totalSongsCount - availableSongsCount
                val blacklistedFoldersCount = blacklistedFolders.size
                
                SettingsChipItem(
                    title = "Manage blacklist",
                    description = "Control which songs and folders are hidden",
                    primaryChipText = "$effectivelyBlacklistedSongsCount songs",
                    secondaryChipText = "$blacklistedFoldersCount folders",
                    icon = Icons.Filled.Block,
                    primaryChipColor = MaterialTheme.colorScheme.errorContainer,
                    primaryChipTextColor = MaterialTheme.colorScheme.onErrorContainer,
                    secondaryChipColor = MaterialTheme.colorScheme.tertiaryContainer,
                    secondaryChipTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { 
                        showBlacklistManagementBottomSheet = true
                    }
                )

                SettingsDivider()
            }

            // Data & Storage section
            item {
                SettingsSectionHeader(title = "Data & Storage")

                // Cache Management - consolidated into one item
                SettingsChipItem(
                    title = "Manage cache",
                    description = "Control cache size, automatic clearing, and manual cleanup",
                    primaryChipText = "${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB",
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
                    onClick = { showBackupRestoreBottomSheet = true }
                )

                SettingsDivider()
            }

            

            // Integrations & Services section
            item {
                SettingsSectionHeader(title = "Integrations & Services")

                SettingsClickableItem(
                    title = "Manage API Settings",
                    description = "Configure external API keys and services",
                    icon = Icons.Filled.Api,
                    onClick = {
                        showApiBottomSheet = true
                    }
                )

                SettingsDivider()
            }

            // App Updates section
            item {
                SettingsSectionHeader(title = "App Updates")

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
                SettingsSectionHeader(title = "Troubleshooting & Diagnostics")

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
                SettingsSectionHeader(title = "About")

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
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/RhythmSupport"))
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
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
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
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
                                    imageVector = when (option) {
                                        "Track Number" -> Icons.Filled.FormatListNumbered
                                        "Title A-Z", "Title Z-A" -> Icons.Filled.SortByAlpha
                                        "Duration ↑", "Duration ↓" -> Icons.Filled.AccessTime
                                        "Stable" -> Icons.Filled.Public
                                        "Beta" -> Icons.Filled.BugReport
                                        "List" -> RhythmIcons.Actions.List
                                        "Grid" -> Icons.Filled.GridView
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                
                Surface(
                    color = secondaryChipColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = secondaryChipText,
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryChipTextColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
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
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        onDismiss() // Close the dialog first
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cromaguy/Rhythm/issues"))
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
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Simple network check for the RapidAPI key. Uses java.net.HttpURLConnection to avoid
 * Retrofit dependency here. Performs a small search query and returns true if HTTP 200.
 */
suspend fun verifySpotifyKey(key: String): Int? {
    if (key.isBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("https://spotify23.p.rapidapi.com/artist_images/?id=6eUKZXaKkcviH0Ku9w2n3V")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("X-RapidAPI-Key", key)
            conn.setRequestProperty("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.responseCode
        }.getOrNull()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiManagementBottomSheet(
    onDismiss: () -> Unit,
    appSettings: AppSettings
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // API states
    val spotifyKey by appSettings.spotifyApiKey.collectAsState()
    var showSpotifyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(spotifyKey ?: "") }
    var isChecking by remember { mutableStateOf(false) }
    var checkResult: Boolean? by remember { mutableStateOf(null) }
    var errorCode: Int? by remember { mutableStateOf(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Api,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "API Management",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Configure external API services for enhanced features",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // API Services List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Spotify RapidAPI
                item {
                    ApiServiceCard(
                        title = "Spotify RapidAPI",
                        description = "Enhanced artist images, album artwork, and lyrics",
                        status = if (spotifyKey.isNullOrBlank()) "Not configured" else "Active",
                        isConfigured = !spotifyKey.isNullOrBlank(),
                        icon = Icons.Default.Public,
                        onClick = {
                            apiKeyInput = spotifyKey ?: ""
                            showSpotifyDialog = true
                        }
                    )
                }

                // LRCLib
                item {
                    ApiServiceCard(
                        title = "LRCLib",
                        description = "Free lyrics service - no configuration needed",
                        status = "Always active",
                        isConfigured = true,
                        icon = RhythmIcons.Queue,
                        onClick = { /* No action needed */ }
                    )
                }

                // MusicBrainz
//                item {
//                    ApiServiceCard(
//                        title = "MusicBrainz",
//                        description = "Music metadata and artist information",
//                        status = "Always active",
//                        isConfigured = true,
//                        icon = RhythmIcons.Album,
//                        onClick = { /* No action needed */ }
//                    )
//                }

                // CoverArt Archive
                item {
                    ApiServiceCard(
                        title = "CoverArt Archive",
                        description = "Album artwork from the community",
                        status = "Always active",
                        isConfigured = true,
                        icon = RhythmIcons.Song,
                        onClick = { /* No action needed */ }
                    )
                }

                // Last.fm
//                item {
//                    ApiServiceCard(
//                        title = "Last.fm",
//                        description = "Artist images and music information",
//                        status = "Always active",
//                        isConfigured = true,
//                        icon = RhythmIcons.Artist,
//                        onClick = { /* No action needed */ }
//                    )
//                }

                // YouTube Music
                item {
                    ApiServiceCard(
                        title = "YouTube Music",
                        description = "Fallback for artist images (after Spotify), album art (when local art absent), and track images",
                        status = "Always active",
                        isConfigured = true,
                        icon = RhythmIcons.Album,
                        onClick = { /* No action needed */ }
                    )
                }

                // GitHub (for updates)
                item {
                    ApiServiceCard(
                        title = "GitHub API",
                        description = "App updates and release information",
                        status = "Always active",
                        isConfigured = true,
                        icon = RhythmIcons.Download,
                        onClick = { /* No action needed */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Close button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Spotify API Key Dialog
    if (showSpotifyDialog) {
        AlertDialog(
            onDismissRequest = { if(!isChecking) showSpotifyDialog = false },
            title = {
                Text(
                    text = "Spotify RapidAPI Key",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Obtain a free RapidAPI key for the Spotify23 API (\u2192 Open the \u201cPlayground\u201d and copy your Personal key).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val linkColor = MaterialTheme.colorScheme.primary
                    val annotatedLink = buildAnnotatedString {
                        append("More info")
                        addStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium), 0, length)
                        addLink(LinkAnnotation.Url("https://rapidapi.com/Glavier/api/spotify23/playground"), 0, length)
                    }
                    Text(
                        text = annotatedLink, 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://rapidapi.com/Glavier/api/spotify23/playground"))
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            checkResult = null // reset result when editing
                        },
                        singleLine = true,
                        placeholder = { Text("Enter API key") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isChecking -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying key…")
                            }
                        }
                        checkResult == true -> {
                            Text("Key valid!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        checkResult == false -> {
                            val msg = when (errorCode) {
                                429 -> "Monthly limit exceeded for this key. Using default key."
                                500 -> "RapidAPI server error. Please try again later."
                                else -> "Key invalid. We'll keep using the default key."
                            }
                            Text(msg, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isChecking) return@Button
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        isChecking = true
                        checkResult = null
                        scope.launch {
                            val code = verifySpotifyKey(apiKeyInput.trim())
                            val ok = code == HttpURLConnection.HTTP_OK
                            checkResult = ok
                            errorCode = if (ok) null else code
                            if (ok) {
                                appSettings.setSpotifyApiKey(apiKeyInput.trim())
                                showSpotifyDialog = false
                            } else {
                                appSettings.setSpotifyApiKey(null) // fallback
                            }
                            isChecking = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text(if (isChecking) "Verifying…" else "Save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { 
                        if(!isChecking) {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            showSpotifyDialog = false 
                        }
                    }) {
                        Text("Cancel")
                    }
                    if (!spotifyKey.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (isChecking) return@Button
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    appSettings.setSpotifyApiKey(null)
                                    showSpotifyDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text("Remove")
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ApiServiceCard(
    title: String,
    description: String,
    status: String,
    isConfigured: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConfigured)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isConfigured)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (isConfigured)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConfigured)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow icon (only for configurable services)
            if (title == "Spotify RapidAPI") {
                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Configure",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedUpdateChannelOption(
    currentChannel: String,
    onChannelChange: (String) -> Unit
) {
    SettingsDropdownItem(
        title = "Update Channel",
        description = "Choose how you receive updates",
        selectedOption = currentChannel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
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
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                onSave(sliderPosition) 
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
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
                            val snappedValue = ((newValue / 64f).toInt() * 64f).coerceIn(64f, 2048f)
                            selectedSizeMB = snappedValue
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
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
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
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
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    val sizeInBytes = selectedSizeMB.toLong() * 1024L * 1024L
                    onSave(sizeInBytes) 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashLogHistoryBottomSheet(
    onDismiss: () -> Unit,
    appSettings: AppSettings
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val crashLogHistory by appSettings.crashLogHistory.collectAsState()

    var showLogDetailDialog by remember { mutableStateOf(false) }
    var selectedLog: String? by remember { mutableStateOf(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Crash Log History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Review past application crash reports.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (crashLogHistory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "No crashes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No crash logs found. Good job!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    // modifier = Modifier.weight(1f), // Make LazyColumn fill available space
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(crashLogHistory.size) { index ->
                        val entry = crashLogHistory[index]
                        CrashLogEntryCard(entry = entry) {
                            selectedLog = entry.log
                            showLogDetailDialog = true
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        appSettings.clearCrashLogHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    enabled = crashLogHistory.isNotEmpty() // Enable only if history is not empty
                ) {
                    Text("Clear All Logs")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        chromahub.rhythm.app.util.CrashReporter.testCrash() // Call the test crash function
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Crash")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogDetailDialog) {
        AlertDialog(
            onDismissRequest = { showLogDetailDialog = false },
            title = { Text("Crash Log Details") },
            text = {
                OutlinedTextField(
                    value = selectedLog ?: "No log details available.",
                    onValueChange = { /* Read-only */ },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Rhythm Crash Log", selectedLog)
                        clipboard.setPrimaryClip(clip)
                        showLogDetailDialog = false
                        Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copy Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDetailDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CrashLogEntryCard(entry: chromahub.rhythm.app.data.CrashLogEntry, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Crashed on: ${dateFormat.format(Date(entry.timestamp))}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.log.lines().firstOrNull() ?: "No details available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    onDismiss: () -> Unit,
    appSettings: AppSettings
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Collect states
    val autoBackupEnabled by appSettings.autoBackupEnabled.collectAsState()
    val lastBackupTimestamp by appSettings.lastBackupTimestamp.collectAsState()
    val backupLocation by appSettings.backupLocation.collectAsState()
    
    // Local states
    var isCreatingBackup by remember { mutableStateOf(false) }
    var isRestoringBackup by remember { mutableStateOf(false) }
    var isRestoringFromFile by remember { mutableStateOf(false) }
    var showBackupSuccess by remember { mutableStateOf(false) }
    var showRestoreSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // File picker launcher for restore functionality
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        isRestoringFromFile = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        
                        // Read the backup file content
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val backupJson = inputStream?.bufferedReader()?.use { it.readText() }
                        
                        if (!backupJson.isNullOrEmpty()) {
                            if (appSettings.restoreFromBackup(backupJson)) {
                                showRestoreSuccess = true
                            } else {
                                errorMessage = "Invalid backup format or corrupted data"
                                showError = true
                            }
                        } else {
                            errorMessage = "Unable to read the backup file"
                            showError = true
                        }
                    } catch (e: Exception) {
                        errorMessage = "Failed to restore from file: ${e.message}"
                        showError = true
                    } finally {
                        isRestoringFromFile = false
                        isRestoringBackup = false
                    }
                }
            }
        } else {
            isRestoringFromFile = false
            isRestoringBackup = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Keep your settings, playlists, and preferences safe with automatic or manual backups",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Backup Status Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Backup Status Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.errorContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (lastBackupTimestamp > 0) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (lastBackupTimestamp > 0) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (lastBackupTimestamp > 0) {
                                        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                                        sdf.format(Date(lastBackupTimestamp))
                                    } else "Never",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastBackupTimestamp > 0) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Last Backup",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (lastBackupTimestamp > 0) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        
                        // Auto Backup Status Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.tertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (autoBackupEnabled) Icons.Filled.AutoAwesome else Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = if (autoBackupEnabled) 
                                        MaterialTheme.colorScheme.onTertiaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (autoBackupEnabled) "Enabled" else "Manual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoBackupEnabled) 
                                        MaterialTheme.colorScheme.onTertiaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Auto Backup",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (autoBackupEnabled) 
                                        MaterialTheme.colorScheme.onTertiaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Backup location info if available
                    backupLocation?.let { location ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = location.substringAfterLast("/"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Auto-backup toggle
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                appSettings.setAutoBackupEnabled(!autoBackupEnabled)
                                HapticUtils.performHapticFeedback(context, haptics, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-backup",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Automatically backup settings weekly",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Switch(
                                checked = autoBackupEnabled,
                                onCheckedChange = {
                                    appSettings.setAutoBackupEnabled(it)
                                    HapticUtils.performHapticFeedback(context, haptics, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }

                // Create Backup Button
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCreatingBackup && !isRestoringBackup) {
                                scope.launch {
                                    try {
                                        isCreatingBackup = true
                                        HapticUtils.performHapticFeedback(context, haptics, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        
                                        val backupJson = appSettings.createBackup()
                                        
                                        // Save backup to Downloads folder
                                        val fileName = "rhythm_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                                        
                                        // Use Storage Access Framework for Android 10+
                                        val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                                        if (downloadsDir != null) {
                                            val backupFile = java.io.File(downloadsDir, fileName)
                                            backupFile.writeText(backupJson)
                                            
                                            appSettings.setLastBackupTimestamp(System.currentTimeMillis())
                                            appSettings.setBackupLocation(backupFile.absolutePath)
                                            
                                            showBackupSuccess = true
                                            
                                            // Also copy to clipboard for easy sharing
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Rhythm Backup", backupJson)
                                            clipboard.setPrimaryClip(clip)
                                        } else {
                                            errorMessage = "Unable to access storage for backup"
                                            showError = true
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to create backup: ${e.message}"
                                        showError = true
                                    } finally {
                                        isCreatingBackup = false
                                    }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (isCreatingBackup) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isCreatingBackup) "Creating Backup..." else "Create Backup",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Save your data to file and clipboard",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (!isCreatingBackup && !isRestoringBackup) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = "Create",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Restore Backup Buttons Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Restore from Clipboard Button
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isCreatingBackup && !isRestoringBackup && !isRestoringFromFile) {
                                    scope.launch {
                                        try {
                                            isRestoringBackup = true
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                            
                                            // Get backup from clipboard
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = clipboard.primaryClip
                                            if (clip != null && clip.itemCount > 0) {
                                                val backupJson = clip.getItemAt(0).text.toString()
                                                
                                                if (appSettings.restoreFromBackup(backupJson)) {
                                                    showRestoreSuccess = true
                                                } else {
                                                    errorMessage = "Invalid backup format or corrupted data"
                                                    showError = true
                                                }
                                            } else {
                                                errorMessage = "No backup data found in clipboard. Please copy a backup first."
                                                showError = true
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to restore backup: ${e.message}"
                                            showError = true
                                        } finally {
                                            isRestoringBackup = false
                                        }
                                    }
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                if (isRestoringBackup && !isRestoringFromFile) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = if (isRestoringBackup && !isRestoringFromFile) "Restoring..." else "From Clipboard",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Paste backup data",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Restore from File Button
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isCreatingBackup && !isRestoringBackup && !isRestoringFromFile) {
                                    isRestoringFromFile = true
                                    isRestoringBackup = true
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                    
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "application/json"
                                        // Also accept all files in case JSON MIME type isn't recognized
                                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain", "*/*"))
                                    }
                                    filePickerLauncher.launch(intent)
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                if (isRestoringFromFile) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = if (isRestoringFromFile) "Opening..." else "From File",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Select backup file",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Compact Help Information Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Quick Info",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CompactInfoItem(
                                    icon = Icons.Filled.Save,
                                    text = "Includes settings, playlists, and preferences"
                                )
                                
                                CompactInfoItem(
                                    icon = Icons.Filled.Folder,
                                    text = "Saved to Downloads and copied to clipboard"
                                )
                                
                                CompactInfoItem(
                                    icon = Icons.Filled.CloudUpload,
                                    text = "Restore from clipboard or file picker"
                                )
                                
                                CompactInfoItem(
                                    icon = Icons.Filled.RestartAlt,
                                    text = "Restore requires app restart"
                                )
                            }
                        }
                    }
                }
                
                // Close button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FilledTonalButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Close",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Success/Error Dialogs remain the same
    if (showBackupSuccess) {
        AlertDialog(
            onDismissRequest = { showBackupSuccess = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Backup Created Successfully") },
            text = { Text("Your backup has been saved to Downloads folder and copied to clipboard. You can now share or save this backup safely.") },
            confirmButton = {
                TextButton(onClick = { showBackupSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showRestoreSuccess) {
        AlertDialog(
            onDismissRequest = { showRestoreSuccess = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Restore Completed") },
            text = { Text("Your settings have been restored successfully. Please restart the app for all changes to take effect.") },
            confirmButton = {
                TextButton(onClick = { showRestoreSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
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
fun CacheManagementBottomSheet(
    onDismiss: () -> Unit,
    appSettings: AppSettings,
    musicViewModel: MusicViewModel
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Collect states
    val maxCacheSize by appSettings.maxCacheSize.collectAsState()
    val clearCacheOnExit by appSettings.clearCacheOnExit.collectAsState()
    
    // Local states
    var currentCacheSize by remember { mutableStateOf(0L) }
    var isCalculatingSize by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var showClearCacheSuccess by remember { mutableStateOf(false) }
    var showCacheSizeDialog by remember { mutableStateOf(false) }
    var cacheDetails by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    // Calculate cache size when the sheet opens
    LaunchedEffect(Unit) {
        isCalculatingSize = true
        try {
            currentCacheSize = chromahub.rhythm.app.util.CacheManager.getCacheSize(context)
            
            // Get detailed cache breakdown
            val internalCacheSize = chromahub.rhythm.app.util.CacheManager.getDirectorySize(context.cacheDir)
            val externalCacheSize = context.externalCacheDir?.let { 
                chromahub.rhythm.app.util.CacheManager.getDirectorySize(it) 
            } ?: 0L
            
            cacheDetails = mapOf(
                "Internal Cache" to internalCacheSize,
                "External Cache" to externalCacheSize
            )
        } catch (e: Exception) {
            Log.e("CacheManagement", "Error calculating cache size", e)
        } finally {
            isCalculatingSize = false
        }
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cache Management",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Manage cached data including images, temporary files, and other app data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Current Cache Status
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Current Cache Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isCalculatingSize) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Calculating cache size...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Total cache size
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Total Cache Size:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = chromahub.rhythm.app.util.CacheManager.formatBytes(currentCacheSize),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Cache breakdown
                            cacheDetails.forEach { (label, size) ->
                                if (size > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "  • $label:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = chromahub.rhythm.app.util.CacheManager.formatBytes(size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Cache limit
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Cache Limit:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Cache Settings
            // item {
            //     Text(
            //         text = "Cache Settings",
            //         style = MaterialTheme.typography.titleMedium,
            //         fontWeight = FontWeight.SemiBold,
            //         modifier = Modifier.padding(bottom = 12.dp)
            //     )
            // }

            // Max cache size setting
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showCacheSizeDialog = true
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DataUsage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Maximum Cache Size",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Set the storage limit for cached data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Clear cache on exit toggle
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            appSettings.setClearCacheOnExit(!clearCacheOnExit)
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = null,
                            tint = if (clearCacheOnExit) MaterialTheme.colorScheme.primary 
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Cache on Exit",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Automatically clear cached data when app closes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = clearCacheOnExit,
                            onCheckedChange = { 
                                appSettings.setClearCacheOnExit(it)
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Actions
            // item {
            //     Text(
            //         text = "Actions",
            //         style = MaterialTheme.typography.titleMedium,
            //         fontWeight = FontWeight.SemiBold,
            //         modifier = Modifier.padding(bottom = 12.dp)
            //     )
            // }

            // Clear cache now button
            item {
                Button(
                    onClick = {
                        if (!isClearingCache) {
                            isClearingCache = true
                            scope.launch {
                                try {
                                    // Clear cache using CacheManager
                                    chromahub.rhythm.app.util.CacheManager.clearAllCache(context)
                                    
                                    // Clear in-memory caches from MusicRepository
                                    musicViewModel.getMusicRepository().clearInMemoryCaches()
                                    
                                    // Recalculate cache size
                                    currentCacheSize = chromahub.rhythm.app.util.CacheManager.getCacheSize(context)
                                    
                                    val internalCacheSize = context.cacheDir.let { cacheDir ->
                                        if (cacheDir.exists()) {
                                            cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                                        } else 0L
                                    }
                                    val externalCacheSize = context.externalCacheDir?.let { externalCache ->
                                        if (externalCache.exists()) {
                                            externalCache.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                                        } else 0L
                                    } ?: 0L
                                    
                                    cacheDetails = mapOf(
                                        "Internal Cache" to internalCacheSize,
                                        "External Cache" to externalCacheSize
                                    )
                                    
                                    showClearCacheSuccess = true
                                    delay(3000)
                                    showClearCacheSuccess = false
                                } catch (e: Exception) {
                                    Log.e("CacheManagement", "Error clearing cache", e)
                                } finally {
                                    isClearingCache = false
                                }
                            }
                        }
                    },
                    enabled = !isClearingCache && !isCalculatingSize,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showClearCacheSuccess) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (showClearCacheSuccess) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clearing cache...")
                    } else if (showClearCacheSuccess) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cache cleared!")
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Cache")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Information section
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About Cache",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        listOf(
                            "Cache includes temporary files, images, and app data",
                            "Clearing cache may temporarily slow down the app",
                            "Cached data will rebuild automatically as needed",
                            "Auto-clearing helps maintain optimal performance"
                        ).forEach { info ->
                            CompactInfoItem(
                                icon = Icons.Filled.FiberManualRecord,
                                text = info
                            )
                            if (info != "Auto-clearing helps maintain optimal performance") {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Close button
            item {
                FilledTonalButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Close",
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
