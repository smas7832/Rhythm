package chromahub.rhythm.app.ui.screens

import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.components.SimpleCircularLoader
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Data class to hold additional song metadata
data class ExtendedSongInfo(
    val fileSize: Long = 0,
    val bitrate: String = "Unknown",
    val sampleRate: String = "Unknown",
    val format: String = "Unknown",
    val composer: String = "",
    val discNumber: Int = 0,
    val dateAdded: Long = 0,
    val dateModified: Long = 0,
    val filePath: String = "",
    val albumArtist: String = "",
    val year: Int = 0,
    val mimeType: String = "",
    val channels: String = "Unknown",
    val hasLyrics: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongInfoBottomSheet(
    song: Song?,
    onDismiss: () -> Unit,
    onEditSong: ((title: String, artist: String, album: String, genre: String, year: Int, trackNumber: Int) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appSettings = remember { AppSettings.getInstance(context) }
    var extendedInfo by remember { mutableStateOf<ExtendedSongInfo?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    
    // Blacklist states
    val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
    val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()
    var isLoadingBlacklist by remember { mutableStateOf(false) }
    
    // Check if song is blacklisted
    val isBlacklisted = song?.let { blacklistedSongs.contains(it.id) } ?: false
    
    // Check if song is in a blacklisted folder
    val folderPath = remember(song?.uri) {
        song?.let { 
            try {
                when (it.uri.scheme) {
                    "content" -> {
                        val projection = arrayOf(MediaStore.Audio.Media.DATA)
                        context.contentResolver.query(it.uri, projection, null, null, null)
                            ?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                                    val filePath = cursor.getString(dataIndex)
                                    File(filePath).parent
                                } else null
                            }
                    }
                    "file" -> File(it.uri.path ?: "").parent
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    val isInBlacklistedFolder = folderPath != null && blacklistedFolders.any { blacklistedPath ->
        folderPath.startsWith(blacklistedPath, ignoreCase = true)
    }

    if (song == null) {
        onDismiss()
        return
    }

    // Load extended metadata
    LaunchedEffect(song.id) {
        extendedInfo = withContext(Dispatchers.IO) {
            MediaUtils.getExtendedSongInfo(context, song)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header with album art and track info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art with modern styling
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .apply(
                                    ImageUtils.buildImageRequest(
                                        song.artworkUri,
                                        song.title,
                                        context.cacheDir,
                                        M3PlaceholderType.TRACK
                                    )
                                )
                                .build(),
                            contentDescription = "Album artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Song info with improved layout
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Edit button
                    onEditSong?.let { editCallback ->
                        FilledTonalIconButton(
                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showEditSheet = true
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit metadata"
                            )
                        }
                    }
                }
            }

            item {
                // Action buttons (Top Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Block/Unblock Song
                    FilledTonalButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            isLoadingBlacklist = true
                            
                            song?.let { songToBlock ->
                                if (isBlacklisted) {
                                    appSettings.removeFromBlacklist(songToBlock.id)
                                } else {
                                    appSettings.addToBlacklist(songToBlock.id)
                                }
                                
                                isLoadingBlacklist = false
                                val message = if (isBlacklisted) "Song removed from blocklist" else "Song added to blocklist" 
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoadingBlacklist,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isBlacklisted) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        if (isLoadingBlacklist) {
                            SimpleCircularLoader(
                                size = 16.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = if (isBlacklisted) Icons.Rounded.Block else Icons.Rounded.DoNotDisturb,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBlacklisted) "Unblock" else "Block")
                    }
                    
                    // Block/Unblock Folder
                    if (folderPath != null) {
                        FilledTonalButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                isLoadingBlacklist = true
                                
                                if (isInBlacklistedFolder) {
                                    appSettings.removeFolderFromBlacklist(folderPath)
                                } else {
                                    appSettings.addFolderToBlacklist(folderPath)
                                }
                                
                                isLoadingBlacklist = false
                                val message = if (isInBlacklistedFolder) "Folder removed from blocklist" else "Folder added to blocklist"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            },
                            enabled = !isLoadingBlacklist,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isInBlacklistedFolder) 
                                    MaterialTheme.colorScheme.errorContainer 
                                else 
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            if (isLoadingBlacklist) {
                                SimpleCircularLoader(
                                    size = 16.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = if (isInBlacklistedFolder) Icons.Rounded.FolderOff else Icons.Rounded.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isInBlacklistedFolder) "Unblock Folder" else "Block Folder")
                        }
                    }
                }
            }
            
            // item {
            //     // Action buttons (Bottom Row)
            //     Row(
            //         modifier = Modifier.fillMaxWidth(),
            //         horizontalArrangement = Arrangement.spacedBy(8.dp)
            //     ) {
            //         // Share Song Info
            //         FilledTonalButton(
            //             onClick = {
            //                 haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            //                 val shareIntent = Intent().apply {
            //                     action = Intent.ACTION_SEND
            //                     putExtra(Intent.EXTRA_TEXT, "Now playing: ${song.title} by ${song.artist}")
            //                     type = "text/plain"
            //                 }
            //                 context.startActivity(Intent.createChooser(shareIntent, "Share song"))
            //             },
            //             modifier = Modifier.weight(1f),
            //             colors = ButtonDefaults.filledTonalButtonColors(
            //                 containerColor = MaterialTheme.colorScheme.primaryContainer
            //             )
            //         ) {
            //             Icon(
            //                 imageVector = Icons.Rounded.Share,
            //                 contentDescription = null,
            //                 modifier = Modifier.size(16.dp)
            //             )
            //             Spacer(modifier = Modifier.width(8.dp))
            //             Text("Share Info")
            //         }
                    
            //         // Share Original File
            //         FilledTonalButton(
            //             onClick = {
            //                 haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            //                 try {
            //                     val shareIntent = Intent().apply {
            //                         action = Intent.ACTION_SEND
            //                         type = "audio/*"
            //                         putExtra(Intent.EXTRA_STREAM, song.uri)
            //                         addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //                     }
            //                     context.startActivity(Intent.createChooser(shareIntent, "Share original file"))
            //                 } catch (e: Exception) {
            //                     Toast.makeText(context, "Unable to share file", Toast.LENGTH_SHORT).show()
            //                 }
            //             },
            //             modifier = Modifier.weight(1f),
            //             colors = ButtonDefaults.filledTonalButtonColors(
            //                 containerColor = MaterialTheme.colorScheme.secondaryContainer
            //             )
            //         ) {
            //             Icon(
            //                 imageVector = Icons.Rounded.AudioFile,
            //                 contentDescription = null,
            //                 modifier = Modifier.size(16.dp)
            //             )
            //             Spacer(modifier = Modifier.width(8.dp))
            //             Text("Share File")
            //         }
                    
            //         // Open in external player
            //         FilledTonalButton(
            //             onClick = {
            //                 haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            //                 val intent = Intent().apply {
            //                     action = Intent.ACTION_VIEW
            //                     setDataAndType(song.uri, "audio/*")
            //                     addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //                 }
            //                 try {
            //                     context.startActivity(intent)
            //                 } catch (_: Exception) {
            //                     Toast.makeText(context, "No app found to open file", Toast.LENGTH_SHORT).show()
            //                 }
            //             },
            //             modifier = Modifier.weight(1f),
            //             colors = ButtonDefaults.filledTonalButtonColors(
            //                 containerColor = MaterialTheme.colorScheme.tertiaryContainer
            //             )
            //         ) {
            //             Icon(
            //                 imageVector = Icons.Rounded.PlayArrow,
            //                 contentDescription = null,
            //                 modifier = Modifier.size(16.dp)
            //             )
            //             Spacer(modifier = Modifier.width(8.dp))
            //             Text("Open")
            //         }
            //     }
            // }

            item {
                // Metadata grid section
                MetadataGridSection(
                    song = song,
                    extendedInfo = extendedInfo
                )
            }
        }
        
        // Show Edit Sheet
        if (showEditSheet) {
            EditSongSheet(
                song = song,
                onDismiss = { showEditSheet = false },
                onSave = { title, artist, album, genre, year, trackNumber ->
                    onEditSong?.invoke(title, artist, album, genre, year, trackNumber)
                    showEditSheet = false
                }
            )
        }
    }
}

@Composable
private fun MetadataGridSection(
    song: Song,
    extendedInfo: ExtendedSongInfo?
) {
    // Prepare metadata items (avoiding duplicates and showing more information)
    val metadataItems = buildList {
        // Basic song info
        add(MetadataItem("Duration", formatDuration(song.duration), Icons.Rounded.Schedule))
        
        // Track info (prefer extended info if available)
        val trackNum = if (song.trackNumber > 0) song.trackNumber else 0
        val discNum = extendedInfo?.discNumber ?: 0
        when {
            trackNum > 0 && discNum > 0 -> add(MetadataItem("Track", "$discNum.$trackNum", Icons.Rounded.FormatListNumbered))
            trackNum > 0 -> add(MetadataItem("Track", trackNum.toString(), Icons.Rounded.FormatListNumbered))
        }
        
        // Year (prefer song data, fallback to extended info)
        val yearValue = if (song.year > 0) song.year else extendedInfo?.year ?: 0
        if (yearValue > 0) {
            add(MetadataItem("Year", yearValue.toString(), Icons.Rounded.DateRange))
        }
        
        // Genre
        if (!song.genre.isNullOrEmpty()) {
            add(MetadataItem("Genre", song.genre, Icons.Rounded.Category))
        }
        
        extendedInfo?.let { info ->
            // Audio quality info
            if (info.bitrate != "Unknown") {
                add(MetadataItem("Bitrate", info.bitrate, Icons.Rounded.GraphicEq))
            }
            if (info.sampleRate != "Unknown") {
                add(MetadataItem("Sample Rate", info.sampleRate, Icons.Rounded.Tune))
            }
            if (info.channels != "Unknown") {
                add(MetadataItem("Channels", info.channels, Icons.Rounded.SettingsInputComponent))
            }
            if (info.format != "Unknown") {
                add(MetadataItem("Format", info.format, Icons.Rounded.MusicNote))
            }
            
            // File info
            if (info.fileSize > 0) {
                add(MetadataItem("File Size", formatFileSize(info.fileSize), Icons.Rounded.FolderOpen))
            }
            
            // Additional metadata (non-duplicating)
            if (info.composer.isNotEmpty() && info.composer != song.artist) {
                add(MetadataItem("Composer", info.composer, Icons.Rounded.EditNote))
            }
            if (info.albumArtist.isNotEmpty() && info.albumArtist != song.artist) {
                add(MetadataItem("Album Artist", info.albumArtist, Icons.Rounded.Person))
            }
            if (info.hasLyrics) {
                add(MetadataItem("Lyrics", "Available", Icons.Rounded.Lyrics))
            }
            if (info.mimeType.isNotEmpty()) {
                add(MetadataItem("MIME Type", info.mimeType.substringAfter("/").uppercase(), Icons.Rounded.Code))
            }
            
            // Date info
            if (info.dateAdded > 0) {
                add(MetadataItem("Date Added", formatDate(info.dateAdded), Icons.Rounded.Add))
            }
            if (info.dateModified > 0 && info.dateModified != info.dateAdded) {
                add(MetadataItem("Modified", formatDate(info.dateModified), Icons.Rounded.Update))
            }
        }
    }
    
    if (metadataItems.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Metadata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(((metadataItems.size / 2 + metadataItems.size % 2) * 80).dp)
                ) {
                    items(metadataItems) { item ->
                        MetadataGridItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataGridItem(
    item: MetadataItem
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSongSheet(
    song: Song,
    onDismiss: () -> Unit,
    onSave: (title: String, artist: String, album: String, genre: String, year: Int, trackNumber: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var genre by remember { mutableStateOf(song.genre ?: "") }
    var year by remember { mutableStateOf(if (song.year > 0) song.year.toString() else "") }
    var trackNumber by remember { mutableStateOf(if (song.trackNumber > 0) song.trackNumber.toString() else "") }
    val haptics = LocalHapticFeedback.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Metadata",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            // Artist field
            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text("Artist") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            // Album field
            OutlinedTextField(
                value = album,
                onValueChange = { album = it },
                label = { Text("Album") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            // Genre field
            OutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = { Text("Genre") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Category,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            // Year and Track Number in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Year field
                OutlinedTextField(
                    value = year,
                    onValueChange = { input ->
                        // Only allow digits and limit to 4 characters
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            year = input
                        }
                    },
                    label = { Text("Year") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Track Number field
                OutlinedTextField(
                    value = trackNumber,
                    onValueChange = { input ->
                        // Only allow digits and limit to 3 characters
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            trackNumber = input
                        }
                    },
                    label = { Text("Track") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FormatListNumbered,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val yearInt = year.toIntOrNull() ?: 0
                        val trackInt = trackNumber.toIntOrNull() ?: 0
                        onSave(title.trim(), artist.trim(), album.trim(), genre.trim(), yearInt, trackInt)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = title.isNotBlank() && artist.isNotBlank()
                ) {
                    Text("Save")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Data classes
data class MetadataItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)

// Helper functions
private fun formatDuration(durationMs: Long): String {
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(durationMs) - java.util.concurrent.TimeUnit.MINUTES.toSeconds(minutes)
    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(durationMs)

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}

private fun formatDate(timestamp: Long): String {
    return if (timestamp > 0) {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        formatter.format(date)
    } else {
        "Unknown"
    }
}