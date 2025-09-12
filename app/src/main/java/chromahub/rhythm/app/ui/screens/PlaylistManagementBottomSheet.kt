@file:OptIn(ExperimentalMaterial3Api::class)

package chromahub.rhythm.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.ui.components.PlaylistImportDialog
import chromahub.rhythm.app.ui.components.BulkPlaylistExportDialog
import chromahub.rhythm.app.ui.components.PlaylistOperationProgressDialog
import chromahub.rhythm.app.ui.components.PlaylistOperationResultDialog
import chromahub.rhythm.app.util.PlaylistImportExportUtils

@Composable
fun PlaylistManagementBottomSheet(
    onDismiss: () -> Unit,
    playlists: List<Playlist>,
    musicViewModel: MusicViewModel,
    onCreatePlaylist: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    
    // Dialog states for import/export operations
    var showBulkExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showOperationProgress by remember { mutableStateOf(false) }
    var operationProgressText by remember { mutableStateOf("") }
    var operationError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Create document launcher for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { exportUri ->
            // Handle bulk export result
            showOperationProgress = true
            operationProgressText = "Exporting playlists..."
            
            // The actual export will be handled by the dialog callback
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlaylistPlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Playlist Management",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Text(
                        //     text = "Manage your playlists and import/export",
                        //     style = MaterialTheme.typography.bodyMedium,
                        //     color = MaterialTheme.colorScheme.onSurfaceVariant
                        // )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Create, organize, and backup your music playlists.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // // Quick Actions
            // item {
            //     Card(
            //         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            //         shape = RoundedCornerShape(16.dp),
            //         modifier = Modifier.fillMaxWidth()
            //     ) {
            //         Column(
            //             modifier = Modifier.padding(20.dp)
            //         ) {
            //             Row(
            //                 verticalAlignment = Alignment.CenterVertically,
            //                 modifier = Modifier.fillMaxWidth()
            //             ) {
            //                 Icon(
            //                     imageVector = Icons.Rounded.FlashOn,
            //                     contentDescription = null,
            //                     tint = MaterialTheme.colorScheme.primary
            //                 )
            //                 Spacer(modifier = Modifier.width(12.dp))
            //                 Text(
            //                     text = "Quick Actions",
            //                     style = MaterialTheme.typography.titleMedium,
            //                     fontWeight = FontWeight.SemiBold
            //                 )
            //             }
                        
            //             Spacer(modifier = Modifier.height(16.dp))
                        
            //             LazyRow(
            //                 horizontalArrangement = Arrangement.spacedBy(12.dp),
            //                 contentPadding = PaddingValues(horizontal = 4.dp)
            //             ) {
            //                 item {
            //                     QuickActionCard(
            //                         icon = Icons.Rounded.Add,
            //                         label = "Create\nPlaylist",
            //                         onClick = {
            //                             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            //                             onCreatePlaylist()
            //                         }
            //                     )
            //                 }
            //                 item {
            //                     QuickActionCard(
            //                         icon = Icons.Rounded.FileUpload,
            //                         label = "Import\nPlaylists",
            //                         onClick = {
            //                             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            //                             showImportDialog = true
            //                         }
            //                     )
            //                 }
            //                 item {
            //                     QuickActionCard(
            //                         icon = Icons.Rounded.FileDownload,
            //                         label = "Export All",
            //                         onClick = {
            //                             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            //                             showBulkExportDialog = true
            //                         }
            //                     )
            //                 }
            //             }
            //         }
            //     }
            // }

            // Playlist Statistics
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
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

            // Playlist Management Options
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
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Management Options",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            
                            if (playlists.any { !it.isDefault }) {
                                ManagementOptionItem(
                                    icon = Icons.Rounded.CleaningServices,
                                    title = "Cleanup Empty Playlists",
                                    subtitle = "Remove playlists with no songs",
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // TODO: Implement cleanup functionality
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Default Playlists",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(playlists.filter { it.isDefault }) { playlist ->
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
                                    tint = MaterialTheme.colorScheme.primary
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
                                verticalArrangement = Arrangement.spacedBy(8.dp),
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

    // Dialogs for import/export operations
    if (showBulkExportDialog) {
        BulkPlaylistExportDialog(
            playlistCount = playlists.size,
            onDismiss = { showBulkExportDialog = false },
            onExport = { format, includeMetadata ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting ${playlists.size} playlists..."
                
                // Handle bulk export
                musicViewModel.exportAllPlaylists(format, includeMetadata)
            }
        )
    }

    if (showImportDialog) {
        PlaylistImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { uri ->
                showImportDialog = false
                showOperationProgress = true
                operationProgressText = "Importing playlists..."
                
                // Handle import
                musicViewModel.importPlaylist(uri)
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
            title = { Text("Error") },
            text = { Text(operationError ?: "An unknown error occurred") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        operationError = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 90.dp, height = 100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
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
            containerColor = if (isDefault) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDefault) Icons.Rounded.Star else Icons.Rounded.PlaylistPlay,
                contentDescription = null,
                tint = if (isDefault) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${playlist.songs.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isDefault) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete playlist",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
