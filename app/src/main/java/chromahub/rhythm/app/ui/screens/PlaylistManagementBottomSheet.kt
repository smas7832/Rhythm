@file:OptIn(ExperimentalMaterial3Api::class)

package chromahub.rhythm.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.Warning
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.ui.components.BulkPlaylistExportDialog
import chromahub.rhythm.app.ui.components.PlaylistImportDialog
import chromahub.rhythm.app.ui.components.PlaylistOperationProgressDialog
import chromahub.rhythm.app.ui.components.AppRestartDialog // Added import for AppRestartDialog
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.util.AppRestarter // Added import for AppRestarter
import androidx.compose.ui.platform.LocalContext // Added import for LocalContext
import kotlinx.coroutines.delay

@Composable
fun PlaylistManagementBottomSheet(
    onDismiss: () -> Unit,
    playlists: List<Playlist>,
    musicViewModel: MusicViewModel,
    onCreatePlaylist: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current // Capture LocalContext.current here
    
    // Dialog states for import/export operations
    var showBulkExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showOperationProgress by remember { mutableStateOf(false) }
    var operationProgressText by remember { mutableStateOf("") }
    var operationError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showCleanupConfirmDialog by remember { mutableStateOf(false) }

    // Animation states
    var showContent by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentAlpha"
    )

    val contentTranslation by animateFloatAsState(
        targetValue = if (showContent) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentTranslation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                PlaylistManagementHeader(
                    playlistCount = playlists.size
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentTranslation
                }
            ) {
                // Playlist Statistics
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Your Collection",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatisticItem(
                                    label = "Total Playlists",
                                    value = playlists.size.toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                StatisticItem(
                                    label = "User Created",
                                    value = playlists.count { !it.isDefault }.toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                StatisticItem(
                                    label = "Default Lists",
                                    value = playlists.count { it.isDefault }.toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Tips Card
//                item {
//                    Card(
//                        colors = CardDefaults.cardColors(
//                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
//                        ),
//                        shape = RoundedCornerShape(16.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(16.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.Info,
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Spacer(modifier = Modifier.width(12.dp))
//                            Text(
//                                text = "Import/export playlists in M3U format • Bulk operations available",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onTertiaryContainer
//                            )
//                        }
//                    }
//                }

                // Playlist Management Options
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Manage Playlists",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ManagementOptionItem(
                                    icon = Icons.Rounded.AddCircle,
                                    title = "Create New Playlist",
                                    subtitle = "Add a new custom playlist",
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCreatePlaylist()
                                    }
                                )
                                
                                ManagementOptionItem(
                                    icon = Icons.Rounded.FileUpload,
                                    title = "Import Playlists",
                                    subtitle = "Import from JSON, M3U, or PLS files",
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showImportDialog = true
                                    }
                                )
                                
                                ManagementOptionItem(
                                    icon = Icons.Rounded.FileDownload,
                                    title = "Export All Playlists",
                                    subtitle = "Backup all playlists to file",
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showBulkExportDialog = true
                                    }
                                )
                                
                                val emptyPlaylists = playlists.filter { !it.isDefault && it.songs.isEmpty() }
                                if (emptyPlaylists.isNotEmpty()) {
                                    ManagementOptionItem(
                                        icon = Icons.Rounded.CleaningServices,
                                        title = "Cleanup Empty Playlists",
                                        subtitle = "Remove ${emptyPlaylists.size} empty playlist${if (emptyPlaylists.size > 1) "s" else ""}",
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showCleanupConfirmDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Default Playlists Section
                if (playlists.any { it.isDefault }) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Default Playlists",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    playlists.filter { it.isDefault }.forEach { playlist ->
                                        PlaylistItem(
                                            playlist = playlist,
                                            isDefault = true,
                                            onDelete = { /* Cannot delete default playlists */ }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // User Playlists Section
                if (playlists.any { !it.isDefault }) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "My Playlists",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    items(playlists.filter { !it.isDefault }) { playlist ->
                                        PlaylistItem(
                                            playlist = playlist,
                                            isDefault = false,
                                            onDelete = { onDeletePlaylist(playlist) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs for import/export operations
    if (showBulkExportDialog) {
        BulkPlaylistExportDialog(
            playlistCount = playlists.size,
            onDismiss = { showBulkExportDialog = false },
            onExport = { format, includeMetadata ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting playlists..."
                
                // Handle bulk export
                musicViewModel.exportAllPlaylists(format, includeMetadata) { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success handled by snackbar in navigation layer
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Export failed"
                        }
                    )
                }
            },
            onExportToCustomLocation = { format, includeMetadata, directoryUri ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting playlists to selected location..."

                musicViewModel.exportAllPlaylists(format, includeMetadata, directoryUri) { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success handled by snackbar in navigation layer
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Export failed"
                        }
                    )
                }
            }
        )
    }

    if (showImportDialog) {
        PlaylistImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { uri, onResultCallback, onRestartRequiredCallback ->
                showImportDialog = false
                showOperationProgress = true
                operationProgressText = "Importing playlists..."
                
                // Handle import
                musicViewModel.importPlaylist(uri, { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success handled by snackbar in navigation layer
                            // If restart is required, show the restart dialog
                            onRestartRequiredCallback?.invoke()
                            importResult = Pair(1, message) // Assuming 1 playlist imported for simplicity
                            showRestartDialog = true
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Import failed"
                            showErrorDialog = true // Show error dialog on failure
                        }
                    )
                    onResultCallback(result)
                }, onRestartRequiredCallback)
            }
        )
    }

    if (showOperationProgress) {
        PlaylistOperationProgressDialog(
            operation = operationProgressText,
            onDismiss = {
                showOperationProgress = false
                operationProgressText = ""
            }
        )
    }

    if (showErrorDialog && operationError != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                operationError = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(operationError ?: "An unknown error occurred") },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        operationError = null
                    }
                ) {
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

    // App Restart Dialog
    if (showRestartDialog && importResult != null) {
        chromahub.rhythm.app.ui.components.AppRestartDialog(
            onDismiss = { showRestartDialog = false; importResult = null },
            onRestart = {
                showRestartDialog = false
                AppRestarter.restartApp(context) // Use the captured context
                onDismiss() // Dismiss the bottom sheet after restart is initiated
            },
            onContinue = {
                showRestartDialog = false
                importResult = null
                // Continue without restart
            }
        )
    }
    
    // Cleanup Empty Playlists Confirmation Dialog
    if (showCleanupConfirmDialog) {
        val emptyPlaylists = playlists.filter { !it.isDefault && it.songs.isEmpty() }
        
        AlertDialog(
            onDismissRequest = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                showCleanupConfirmDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CleaningServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Cleanup Empty Playlists",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Are you sure you want to delete ${emptyPlaylists.size} empty playlist${if (emptyPlaylists.size > 1) "s" else ""}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (emptyPlaylists.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                emptyPlaylists.take(5).forEach { playlist ->
                                    Text(
                                        text = "• ${playlist.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (emptyPlaylists.size > 5) {
                                    Text(
                                        text = "... and ${emptyPlaylists.size - 5} more",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        // Delete all empty playlists
                        emptyPlaylists.forEach { playlist ->
                            onDeletePlaylist(playlist)
                        }
                        showCleanupConfirmDialog = false
                        android.widget.Toast.makeText(
                            context,
                            "Deleted ${emptyPlaylists.size} empty playlist${if (emptyPlaylists.size > 1) "s" else ""}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        showCleanupConfirmDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun PlaylistManagementHeader(
    playlistCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Playlists",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    text = if (playlistCount == 1) "1 playlist" else "$playlistCount playlists",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ManagementOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: Playlist,
    isDefault: Boolean,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isDefault) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isDefault) Icons.Rounded.Star else Icons.Rounded.PlaylistPlay,
                        contentDescription = null,
                        tint = if (isDefault) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${playlist.songs.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isDefault) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete playlist",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
