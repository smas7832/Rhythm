package chromahub.rhythm.app.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import java.net.HttpURLConnection
import java.net.URL
import chromahub.rhythm.app.data.AppSettings
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel
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

    val updaterViewModel: AppUpdaterViewModel = viewModel()
    val currentAppVersion by updaterViewModel.currentVersion.collectAsState()

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
                        onClick = onBack,
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
            // Appearance section
            item {
                SettingsSectionHeader(title = "Appearance")

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
            
            // Library section
            item {
                SettingsSectionHeader(title = "Personalization")

                val albumViewType by appSettings.albumViewType.collectAsState()
                var showAlbumViewDropdown by remember { mutableStateOf(false) }
                
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
                    icon = Icons.Filled.Sort,
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

                SettingsDivider()
            }

            // Playback section
            item {
                SettingsSectionHeader(title = "Playback")

//                SettingsToggleItem(
//                    title = "High quality audio",
//                    description = "Enable higher bitrate audio streaming/playback",
//                    icon = RhythmIcons.VolumeUp,
//                    checked = highQualityAudio,
//                    onCheckedChange = { appSettings.setHighQualityAudio(it) }
//                )
//
//                SettingsToggleItem(
//                    title = "Gapless playback",
//                    description = "Eliminate gaps between tracks for continuous listening",
//                    icon = RhythmIcons.Queue,
//                    checked = gaplessPlayback,
//                    onCheckedChange = { appSettings.setGaplessPlayback(it) }
//                )
//
//                SettingsToggleItem(
//                    title = "Crossfade",
//                    description = "Smoothly transition between songs",
//                    icon = RhythmIcons.Shuffle,
//                    checked = crossfade,
//                    onCheckedChange = { appSettings.setCrossfade(it) }
//                )
//
//                AnimatedVisibility(
//                    visible = crossfade,
//                    enter = fadeIn() + expandVertically(),
//                    exit = fadeOut() + shrinkVertically()
//                ) {
//                    SettingsClickableItem(
//                        title = "Crossfade duration",
//                        description = "Set crossfade duration: ${crossfadeDuration.toInt()} seconds",
//                        icon = RhythmIcons.Player.Timer,
//                        onClick = { showCrossfadeDurationDialog = true }
//                    )
//                }
//
//                SettingsToggleItem(
//                    title = "Audio normalization",
//                    description = "Adjust volume levels to a consistent loudness",
//                    icon = RhythmIcons.VolumeUp,
//                    checked = audioNormalization,
//                    onCheckedChange = { appSettings.setAudioNormalization(it) }
//                )
//
//                SettingsToggleItem(
//                    title = "ReplayGain",
//                    description = "Apply ReplayGain tags for consistent playback volume",
//                    icon = RhythmIcons.VolumeUp,
//                    checked = replayGain,
//                    onCheckedChange = { appSettings.setReplayGain(it) }
//                )

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

                val useSystemVolume by appSettings.useSystemVolume.collectAsState()
                SettingsToggleItem(
                    title = "System volume control",
                    description = "Use device volume controls for music playback",
                    icon = Icons.Filled.VolumeUp,
                    checked = useSystemVolume,
                    onCheckedChange = {
                        appSettings.setUseSystemVolume(it)
                    }
                )

                SettingsClickableItem(
                    title = "Equalizer",
                    description = "Open system equalizer to adjust audio frequencies",
                    icon = Icons.Filled.GraphicEq,
                    onClick = onOpenSystemEqualizer
                )

                SettingsDivider()
            }

            // Cache section
            item {
                SettingsSectionHeader(title = "Cache")

                SettingsClickableItem(
                    title = "Max cache size",
                    description = "Current: ${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB",
                    icon = Icons.Filled.Storage,
                    onClick = { showCacheSizeDialog = true }
                )

                SettingsToggleItem(
                    title = "Clear cache on exit",
                    description = "Automatically clear cached data when the app closes",
                    icon = Icons.Filled.ClearAll,
                    checked = clearCacheOnExit,
                    onCheckedChange = { appSettings.setClearCacheOnExit(it) }
                )

                SettingsDivider()
            }

            // API Management section
            item {
                SettingsSectionHeader(title = "API Management")

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

            // Updates section
            item {
                SettingsSectionHeader(title = "Updates")

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
                        // App icon with background
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.rhythm_logo),
                                contentDescription = null,
                                modifier = Modifier.size(70.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Rhythm Music Player",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Version ${currentAppVersion.versionName}",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "by Team ChromaHub",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

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
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cromaguy/Rhythm/issues"))
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
                                    imageVector = RhythmIcons.Edit,
                                    contentDescription = "Report Bug",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Report Bug",
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
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
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
                onCheckedChange = onCheckedChange,
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
    var showDropdown by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { showDropdown = true }
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
                                        "List" -> Icons.Filled.List
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                    imageVector = RhythmIcons.Settings,
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
                item {
                    ApiServiceCard(
                        title = "MusicBrainz",
                        description = "Music metadata and artist information",
                        status = "Always active",
                        isConfigured = true,
                        icon = RhythmIcons.Album,
                        onClick = { /* No action needed */ }
                    )
                }

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
                item {
                    ApiServiceCard(
                        title = "Last.fm",
                        description = "Artist images and music information",
                        status = "Always active",
                        isConfigured = true,
                        icon = RhythmIcons.Artist,
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
                        addStringAnnotation("URL", "https://rapidapi.com/Glavier/api/spotify23/playground", 0, length)
                    }
                    ClickableText(text = annotatedLink, style = MaterialTheme.typography.bodySmall) { offset ->
                        annotatedLink.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { anno ->
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(anno.item))
                            context.startActivity(intent)
                        }
                    }
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
                    TextButton(onClick = { if(!isChecking) showSpotifyDialog = false }) {
                        Text("Cancel")
                    }
                    if (!spotifyKey.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (isChecking) return@Button
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
    var sliderPosition by remember { mutableFloatStateOf(currentDuration) }

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
            Button(onClick = { onSave(sliderPosition) }) {
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
    var selectedSizeIndex by remember {
        mutableIntStateOf(
            when (currentSize) {
                1024L * 1024L * 128L -> 0 // 128 MB
                1024L * 1024L * 256L -> 1 // 256 MB
                1024L * 1024L * 512L -> 2 // 512 MB
                1024L * 1024L * 1024L -> 3 // 1 GB
                else -> 2 // Default to 512MB if current size doesn't match predefined
            }
        )
    }

    val cacheSizes = listOf(
        128L * 1024L * 1024L, // 128 MB
        256L * 1024L * 1024L, // 256 MB
        512L * 1024L * 1024L, // 512 MB
        1024L * 1024L * 1024L // 1 GB
    )

    val cacheSizeLabels = listOf("128 MB", "256 MB", "512 MB", "1 GB")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Max Cache Size") },
        text = {
            Column {
                Text("Set the maximum size for cached audio and artwork.")
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    cacheSizeLabels.forEachIndexed { index, label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSizeIndex = index }
                                .padding(vertical = 8.dp)
                        ) {
                            Switch(
                                checked = selectedSizeIndex == index,
                                onCheckedChange = { selectedSizeIndex = index }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(cacheSizes[selectedSizeIndex]) }) {
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
