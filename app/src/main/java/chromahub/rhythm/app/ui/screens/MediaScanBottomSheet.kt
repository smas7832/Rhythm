package chromahub.rhythm.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.DocumentsContract
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.SimpleCircularLoader
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import java.io.File

enum class MediaScanMode {
    BLACKLIST, WHITELIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScanBottomSheet(
    onDismiss: () -> Unit,
    appSettings: AppSettings,
    initialMode: MediaScanMode = MediaScanMode.BLACKLIST
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val musicViewModel: MusicViewModel = viewModel()
    
    // Get all songs and filtered items
    val allSongs by musicViewModel.songs.collectAsState() // We need all songs here for management
    val filteredSongs by musicViewModel.filteredSongs.collectAsState() // Get filtered songs for accurate counting
    val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
    val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()
    val whitelistedSongs by appSettings.whitelistedSongs.collectAsState()
    val whitelistedFolders by appSettings.whitelistedFolders.collectAsState()
    
    // Mode state
    var currentMode by remember { mutableStateOf(initialMode) }
    
    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Songs", "Folders")
    
    // Filter songs based on current mode
    val filteredSongDetails = remember(allSongs, blacklistedSongs, whitelistedSongs, currentMode) {
        when (currentMode) {
            MediaScanMode.BLACKLIST -> allSongs.filter { song -> blacklistedSongs.contains(song.id) }
            MediaScanMode.WHITELIST -> allSongs.filter { song -> whitelistedSongs.contains(song.id) }
        }
    }
    
    // Get filtered folders based on current mode
    val filteredFoldersList = remember(blacklistedFolders, whitelistedFolders, currentMode) {
        when (currentMode) {
            MediaScanMode.BLACKLIST -> blacklistedFolders
            MediaScanMode.WHITELIST -> whitelistedFolders
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header with mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (currentMode == MediaScanMode.BLACKLIST) Icons.Filled.Block else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (currentMode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Media Scan Management",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentMode == MediaScanMode.BLACKLIST) "Exclude from scanning" else "Include in scanning",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mode toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Blacklist mode button
                    FilterChip(
                        onClick = { 
                            HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            currentMode = MediaScanMode.BLACKLIST 
                        },
                        label = { 
                            Text(
                                "Blacklist",
                                fontWeight = if (currentMode == MediaScanMode.BLACKLIST) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        selected = currentMode == MediaScanMode.BLACKLIST,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Whitelist mode button
                    FilterChip(
                        onClick = { 
                            HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            currentMode = MediaScanMode.WHITELIST 
                        },
                        label = { 
                            Text(
                                "Whitelist",
                                fontWeight = if (currentMode == MediaScanMode.WHITELIST) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        selected = currentMode == MediaScanMode.WHITELIST,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = if (currentMode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            selectedTabIndex = index
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tips Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "How It Works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    MediaScanTipItem(
                        icon = Icons.Filled.Block,
                        text = "Blacklist hides unwanted audio (ringtones, notifications)"
                    )
                    MediaScanTipItem(
                        icon = Icons.Filled.CheckCircle,
                        text = "Whitelist shows only specific folders for a curated library"
                    )
                    MediaScanTipItem(
                        icon = Icons.Filled.Folder,
                        text = "Folder filters apply to all songs within that folder"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab content
            when (selectedTabIndex) {
                0 -> SongsFilterTab(
                    mode = currentMode,
                    filteredSongs = if (currentMode == MediaScanMode.BLACKLIST) blacklistedSongs else whitelistedSongs,
                    filteredSongDetails = filteredSongDetails,
                    allSongs = allSongs,
                    filteredAvailableSongs = filteredSongs,
                    appSettings = appSettings,
                    haptic = haptic,
                    context = context
                )
                1 -> FoldersFilterTab(
                    mode = currentMode,
                    filteredFolders = filteredFoldersList,
                    appSettings = appSettings,
                    haptic = haptic,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun FilteredSongItem(
    song: Song,
    mode: MediaScanMode,
    onRemoveFromFilter: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Song info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Remove button
            IconButton(
                onClick = {
                    isLoading = true
                    HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onRemoveFromFilter()
                    isLoading = false
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    SimpleCircularLoader(
                        color = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        size = 24.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircle,
                        contentDescription = if (mode == MediaScanMode.BLACKLIST) "Remove from blacklist" else "Remove from whitelist",
                        tint = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SongsFilterTab(
    mode: MediaScanMode,
    filteredSongs: List<String>,
    filteredSongDetails: List<Song>,
    allSongs: List<Song>,
    filteredAvailableSongs: List<Song>,
    appSettings: AppSettings,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    context: android.content.Context
) {
    // Calculate effective filter counts considering both individual songs and folder filters
    val totalSongsCount = allSongs.size
    val availableSongsCount = filteredAvailableSongs.size // This already excludes blacklisted songs
    val effectivelyFilteredCount = if (mode == MediaScanMode.BLACKLIST) {
        totalSongsCount - availableSongsCount
    } else {
        filteredSongDetails.size // For whitelist, show count of whitelisted songs
    }
    
    Column {
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "$effectivelyFilteredCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (mode == MediaScanMode.BLACKLIST) "Blocked" else "Whitelisted",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "$totalSongsCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    if (mode == MediaScanMode.BLACKLIST) {
                        appSettings.clearBlacklist()
                    } else {
                        appSettings.clearWhitelist()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear All")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(filteredSongDetails, key = { it.id }) { song ->
                FilteredSongItem(
                    song = song,
                    mode = mode,
                    onRemoveFromFilter = {
                        HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        if (mode == MediaScanMode.BLACKLIST) {
                            appSettings.removeFromBlacklist(song.id)
                        } else {
                            appSettings.removeFromWhitelist(song.id)
                        }
                    }
                )
            }
        }
    }

    // Empty state
    AnimatedVisibility(
        visible = filteredSongDetails.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (mode == MediaScanMode.BLACKLIST) Icons.Filled.Check else Icons.Filled.PlaylistAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (mode == MediaScanMode.BLACKLIST) "No blacklisted songs" else "No whitelisted songs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (mode == MediaScanMode.BLACKLIST) "All your songs are available for playback" else "Add songs to whitelist for exclusive playback",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FoldersFilterTab(
    mode: MediaScanMode,
    filteredFolders: List<String>,
    appSettings: AppSettings,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    context: android.content.Context
) {
    // Folder picker launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val docId = DocumentsContract.getTreeDocumentId(uri)
                    val folderPath = docId.substringAfter(":")
                    
                    if (mode == MediaScanMode.BLACKLIST) {
                        appSettings.addFolderToBlacklist("/$folderPath")
                    } else {
                        appSettings.addFolderToWhitelist("/$folderPath")
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    Column {
        // Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${filteredFolders.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (mode == MediaScanMode.BLACKLIST) "Blocked folders" else "Whitelisted folders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    folderPickerLauncher.launch(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Folder")
            }
            
            if (filteredFolders.isNotEmpty()) {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        if (mode == MediaScanMode.BLACKLIST) {
                            appSettings.clearFolderBlacklist()
                        } else {
                            appSettings.clearFolderWhitelist()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Folder list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(filteredFolders) { folderPath ->
                FilteredFolderItem(
                    folderPath = folderPath,
                    mode = mode,
                    onRemoveFromFilter = {
                        HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        if (mode == MediaScanMode.BLACKLIST) {
                            appSettings.removeFolderFromBlacklist(folderPath)
                        } else {
                            appSettings.removeFolderFromWhitelist(folderPath)
                        }
                    }
                )
            }
        }
    }
    
    // Empty state
    AnimatedVisibility(
        visible = filteredFolders.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (mode == MediaScanMode.BLACKLIST) "No blocked folders" else "No whitelisted folders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (mode == MediaScanMode.BLACKLIST) "All folders are available for scanning" else "Add folders to whitelist for exclusive scanning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilteredFolderItem(
    folderPath: String,
    mode: MediaScanMode,
    onRemoveFromFilter: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder icon
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Folder path
            Text(
                text = File(folderPath).name.ifEmpty { folderPath },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Remove button
            IconButton(
                onClick = {
                    isLoading = true
                    HapticUtils.performHapticFeedback(context, haptic, androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onRemoveFromFilter()
                    isLoading = false
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    SimpleCircularLoader(
                        color = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        size = 24.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircle,
                        contentDescription = if (mode == MediaScanMode.BLACKLIST) "Remove from blacklist" else "Remove from whitelist",
                        tint = if (mode == MediaScanMode.BLACKLIST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaScanTipItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}