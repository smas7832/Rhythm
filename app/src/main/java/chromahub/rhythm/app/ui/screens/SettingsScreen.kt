package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    showLyrics: Boolean = true,
    showOnlineOnlyLyrics: Boolean = false,
    onShowLyricsChange: (Boolean) -> Unit = {},
    onShowOnlineOnlyLyricsChange: (Boolean) -> Unit = {},
    useSystemTheme: Boolean = false,
    darkMode: Boolean = true,
    onUseSystemThemeChange: (Boolean) -> Unit = {},
    onDarkModeChange: (Boolean) -> Unit = {},
    onOpenSystemEqualizer: () -> Unit = {},
    enableHighQualityAudio: Boolean = true,
    enableGaplessPlayback: Boolean = true,
    enableCrossfade: Boolean = false,
    crossfadeDuration: Float = 2f,
    enableAudioNormalization: Boolean = true,
    enableReplayGain: Boolean = false,
    onHighQualityAudioChange: (Boolean) -> Unit = {},
    onGaplessPlaybackChange: (Boolean) -> Unit = {},
    onCrossfadeChange: (Boolean) -> Unit = {},
    onCrossfadeDurationChange: (Float) -> Unit = {},
    onAudioNormalizationChange: (Boolean) -> Unit = {},
    onReplayGainChange: (Boolean) -> Unit = {}
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    
    // New settings state variables
    var enableEqualizer by remember { mutableStateOf(false) }
    var enableDataSaver by remember { mutableStateOf(false) }
    var autoDownloadOnWifi by remember { mutableStateOf(true) }
    var showNotifications by remember { mutableStateOf(true) }
    var enableSleepTimer by remember { mutableStateOf(false) }
    var sleepTimerDuration by remember { mutableIntStateOf(30) }
    var showSleepTimerOptions by remember { mutableStateOf(false) }
    var showEqualizerPresetOptions by remember { mutableStateOf(false) }
    var selectedEqualizerPreset by remember { mutableStateOf("Normal") }
    
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
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
                    description = "Switch between app default and system color theme",
                    icon = RhythmIcons.Settings,
                    checked = useSystemTheme,
                    onCheckedChange = onUseSystemThemeChange
                )
                
                AnimatedVisibility(
                    visible = !useSystemTheme,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingsToggleItem(
                        title = "Dark mode",
                        description = "Enable dark theme",
                        icon = RhythmIcons.LocationFilled,
                        checked = darkMode,
                        onCheckedChange = onDarkModeChange
                    )
                }
                
                SettingsDivider()
            }
            
            // Playback section
            item {
                SettingsSectionHeader(title = "Playback")
                
                SettingsToggleItem(
                    title = "High quality audio",
                    description = "Stream and download music in higher quality (uses more data)",
                    icon = RhythmIcons.VolumeUp,
                    checked = enableHighQualityAudio,
                    onCheckedChange = onHighQualityAudioChange
                )
                
                SettingsToggleItem(
                    title = "Gapless playback",
                    description = "Eliminate gaps between tracks for seamless listening",
                    icon = RhythmIcons.SkipNext,
                    checked = enableGaplessPlayback,
                    onCheckedChange = onGaplessPlaybackChange
                )
                
                SettingsToggleItem(
                    title = "Crossfade",
                    description = "Smoothly transition between songs",
                    icon = RhythmIcons.Shuffle,
                    checked = enableCrossfade,
                    onCheckedChange = onCrossfadeChange
                )
                
                AnimatedVisibility(
                    visible = enableCrossfade,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Crossfade duration: ${crossfadeDuration.toInt()} seconds",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Slider(
                            value = crossfadeDuration,
                            onValueChange = onCrossfadeDurationChange,
                            valueRange = 1f..12f,
                            steps = 11,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                SettingsToggleItem(
                    title = "Audio normalization",
                    description = "Balance volume levels between tracks",
                    icon = RhythmIcons.VolumeDown,
                    checked = enableAudioNormalization,
                    onCheckedChange = onAudioNormalizationChange
                )
                
                SettingsToggleItem(
                    title = "ReplayGain",
                    description = "Use track metadata for volume normalization",
                    icon = RhythmIcons.Refresh,
                    checked = enableReplayGain,
                    onCheckedChange = onReplayGainChange
                )
                
                SettingsToggleItem(
                    title = "Show lyrics",
                    description = "Display lyrics when available",
                    icon = RhythmIcons.Queue,
                    checked = showLyrics,
                    onCheckedChange = onShowLyricsChange
                )
                
                AnimatedVisibility(
                    visible = showLyrics,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingsToggleItem(
                        title = "Online lyrics only",
                        description = "Only fetch and display lyrics when connected to internet",
                        icon = RhythmIcons.LocationFilled,
                        checked = showOnlineOnlyLyrics,
                        onCheckedChange = onShowOnlineOnlyLyricsChange
                    )
                }
                
                SettingsClickableItem(
                    title = "Equalizer",
                    description = "Open system equalizer to adjust audio frequencies",
                    icon = RhythmIcons.VolumeUp,
                    onClick = onOpenSystemEqualizer
                )
                
                SettingsDivider()
            }
            
            // Data & Storage section
            item {
                SettingsSectionHeader(title = "Data & Storage")
                
                SettingsToggleItem(
                    title = "Data saver",
                    description = "Reduce data usage when streaming music",
                    icon = RhythmIcons.Download,
                    checked = enableDataSaver,
                    onCheckedChange = { enableDataSaver = it }
                )
                
                SettingsToggleItem(
                    title = "Auto-download on Wi-Fi",
                    description = "Download played songs when connected to Wi-Fi",
                    icon = RhythmIcons.Download,
                    checked = autoDownloadOnWifi,
                    onCheckedChange = { autoDownloadOnWifi = it }
                )
                
                SettingsClickableItem(
                    title = "Clear cache",
                    description = "Delete temporary files to free up space",
                    icon = RhythmIcons.Delete,
                    onClick = { /* Implement cache clearing */ }
                )
                
                SettingsDivider()
            }
            
            // Notifications & Controls section
            item {
                SettingsSectionHeader(title = "Notifications & Controls")
                
                SettingsToggleItem(
                    title = "Show notifications",
                    description = "Display playback controls in notification area",
                    icon = RhythmIcons.Queue,
                    checked = showNotifications,
                    onCheckedChange = { showNotifications = it }
                )
                
                SettingsToggleItem(
                    title = "Sleep timer",
                    description = "Stop playback after a set time",
                    icon = RhythmIcons.Pause,
                    checked = enableSleepTimer,
                    onCheckedChange = { enableSleepTimer = it }
                )
                
                AnimatedVisibility(
                    visible = enableSleepTimer,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        SettingsClickableItem(
                            title = "Timer duration",
                            description = "$sleepTimerDuration minutes",
                            icon = RhythmIcons.List,
                            onClick = { showSleepTimerOptions = true }
                        )
                        
                        if (showSleepTimerOptions) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                DropdownMenu(
                                    expanded = showSleepTimerOptions,
                                    onDismissRequest = { showSleepTimerOptions = false },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    listOf(5, 10, 15, 30, 45, 60, 90).forEach { minutes ->
                                        DropdownMenuItem(
                                            text = { Text("$minutes minutes") },
                                            onClick = {
                                                sleepTimerDuration = minutes
                                                showSleepTimerOptions = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                SettingsDivider()
            }
            
            // About section
            item {
                SettingsSectionHeader(title = "About")
                
                SettingsClickableItem(
                    title = "About Rhythm",
                    description = "Version 1.0.0 Alpha | ChromaHub",
                    icon = RhythmIcons.Settings,
                    onClick = { showAboutDialog = true }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Extra bottom space for mini player
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 16.dp)
    )
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
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Arrow icon
        Icon(
            imageVector = RhythmIcons.Forward,
            contentDescription = "Open",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "About Rhythm",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Rhythm Music Player",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Version 1.0.0 Alpha | ChromaHub",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "By Anjishnu Nandi ;)",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A modern music player showcasing Material 3 Expressive design with physics-based animations.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}