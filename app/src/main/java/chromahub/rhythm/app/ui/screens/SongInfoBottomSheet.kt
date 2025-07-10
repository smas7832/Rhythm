package chromahub.rhythm.app.ui.screens

import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class to hold additional song metadata
data class ExtendedSongInfo(
    val fileSize: Long = 0,
    val bitrate: String = "Unknown",
    val sampleRate: String = "Unknown",
    val format: String = "Unknown",
    val dateAdded: Long = 0,
    val dateModified: Long = 0,
    val filePath: String = "",
    val composer: String = "",
    val discNumber: Int = 0,
    val totalTracks: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongInfoBottomSheet(
    song: Song?,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current
    var extendedInfo by remember { mutableStateOf<ExtendedSongInfo?>(null) }

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
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with horizontal layout - Album art and track info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Smaller Album Art
                ElevatedCard(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song.artworkUri != null) {
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
                                contentDescription = "Album artwork for ${song.title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(
                                imageVector = RhythmIcons.Album,
                                contentDescription = "Album art",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Track Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Track Metadata Section
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Track Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Basic track info
                    DetailRow(label = "Duration", value = formatDuration(song.duration))
                    if (song.trackNumber > 0) {
                        DetailRow(label = "Track", value = song.trackNumber.toString())
                    }
                    if (song.year > 0) {
                        DetailRow(label = "Year", value = song.year.toString())
                    }
                    if (!song.genre.isNullOrEmpty()) {
                        DetailRow(label = "Genre", value = song.genre)
                    }

                    // Extended metadata if available
                    extendedInfo?.let { info ->
                        if (info.bitrate != "Unknown") {
                            DetailRow(label = "Bitrate", value = info.bitrate)
                        }
                        if (info.sampleRate != "Unknown") {
                            DetailRow(label = "Sample Rate", value = info.sampleRate)
                        }
                        if (info.format != "Unknown") {
                            DetailRow(label = "Format", value = info.format)
                        }
                        if (info.fileSize > 0) {
                            DetailRow(label = "File Size", value = formatFileSize(info.fileSize))
                        }
                        if (info.composer.isNotEmpty()) {
                            DetailRow(label = "Composer", value = info.composer)
                        }
                        if (info.discNumber > 0) {
                            DetailRow(label = "Disc", value = info.discNumber.toString())
                        }
                        if (info.dateAdded > 0) {
                            DetailRow(label = "Date Added", value = formatDate(info.dateAdded))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // File Information Section
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "File Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    extendedInfo?.let { info ->
                        if (info.filePath.isNotEmpty()) {
                            DetailRow(label = "Location", value = File(info.filePath).name)
                            if (info.dateModified > 0) {
                                DetailRow(label = "Modified", value = formatDate(info.dateModified))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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

private fun formatDate(timestampMs: Long): String {
    val date = Date(timestampMs)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
