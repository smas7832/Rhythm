@file:OptIn(ExperimentalMaterial3Api::class)
package chromahub.rhythm.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.HapticUtils
import kotlinx.coroutines.launch

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
    val deezerApiEnabled by appSettings.deezerApiEnabled.collectAsState()
    val canvasApiEnabled by appSettings.canvasApiEnabled.collectAsState()
    val lrclibApiEnabled by appSettings.lrclibApiEnabled.collectAsState()
    val ytMusicApiEnabled by appSettings.ytMusicApiEnabled.collectAsState()
    val spotifyApiEnabled by appSettings.spotifyApiEnabled.collectAsState()
    val spotifyClientId by appSettings.spotifyClientId.collectAsState()
    val spotifyClientSecret by appSettings.spotifyClientSecret.collectAsState()
    
    // Spotify API dialog state
    var showSpotifyConfigDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onDismiss()
        },
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
                text = "Control which external API services are active to enhance your music experience. Toggle services on/off to manage data usage and functionality.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // API Services List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Deezer API
                item {
                    ApiServiceCard(
                        title = "Deezer",
                        description = "Free artist images and album artwork - no setup needed",
                        status = "Ready",
                        isConfigured = true,
                        isEnabled = deezerApiEnabled,
                        icon = Icons.Default.Public,
                        showToggle = true,
                        onToggle = { enabled -> appSettings.setDeezerApiEnabled(enabled) },
                        onClick = { /* No configuration needed */ }
                    )
                }

                // Combined Spotify Canvas API
                item {
                    ApiServiceCard(
                        title = "Spotify Canvas",
                        description = if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
                            "Spotify integration for Canvas videos (High Data Usage)"
                        } else {
                            "Canvas videos from Spotify (Please use your own key!)"
                        },
                        status = if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
                            "Active"
                        } else {
                            "Need Setup"
                        },
                        isConfigured = true, // Always configured since free API is available
                        isEnabled = canvasApiEnabled && (spotifyApiEnabled || true), // Enable if either API is available
                        icon = RhythmIcons.Song,
                        showToggle = true,
                        onToggle = { enabled -> 
                            appSettings.setCanvasApiEnabled(enabled)
                            // Auto-clear canvas cache when disabled
                            if (!enabled) {
                                scope.launch {
                                    try {
                                        val canvasRepository = chromahub.rhythm.app.data.CanvasRepository(context, appSettings)
                                        canvasRepository.clearCache()
                                        Log.d("ApiManagement", "Canvas cache cleared due to API being disabled")
                                    } catch (e: Exception) {
                                        Log.e("ApiManagement", "Error clearing canvas cache", e)
                                    }
                                }
                            }
                        },
                        onClick = { 
                            showSpotifyConfigDialog = true 
                        }
                    )
                }

                // Apple Music (Word-by-word lyrics)
                item {
                    val appleMusicApiEnabled by appSettings.appleMusicApiEnabled.collectAsState()
                    ApiServiceCard(
                        title = "Apple Music",
                        description = "Word-by-word synchronized lyrics (Highest Quality)",
                        status = "Ready",
                        isConfigured = true,
                        isEnabled = appleMusicApiEnabled,
                        icon = RhythmIcons.Queue,
                        showToggle = true,
                        onToggle = { enabled -> appSettings.setAppleMusicApiEnabled(enabled) },
                        onClick = { /* No configuration needed */ }
                    )
                }

                // LRCLib (Line-by-line lyrics)
                item {
                    ApiServiceCard(
                        title = "LRCLib",
                        description = "Free line-by-line synced lyrics (Fallback)",
                        status = "Ready",
                        isConfigured = true,
                        isEnabled = lrclibApiEnabled,
                        icon = RhythmIcons.Queue,
                        showToggle = true,
                        onToggle = { enabled -> appSettings.setLrcLibApiEnabled(enabled) },
                        onClick = { /* No configuration needed */ }
                    )
                }

                // YouTube Music
                item {
                    ApiServiceCard(
                        title = "YouTube Music",
                        description = "Fallback for artist images and album artwork",
                        status = "Ready",
                        isConfigured = true,
                        isEnabled = ytMusicApiEnabled,
                        icon = RhythmIcons.Album,
                        showToggle = true,
                        onToggle = { enabled -> appSettings.setYTMusicApiEnabled(enabled) },
                        onClick = { /* No configuration needed */ }
                    )
                }

                // GitHub (for updates)
                item {
                    ApiServiceCard(
                        title = "GitHub",
                        description = "App updates and release information",
                        status = "Ready",
                        isConfigured = true,
                        isEnabled = true, // Always enabled for updates
                        icon = RhythmIcons.Download,
                        showToggle = false, // Can't disable update checks
                        onClick = { /* No configuration needed */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // // Close button
            // Button(
            //     onClick = {
            //         HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            //         onDismiss()
            //     },
            //     colors = ButtonDefaults.buttonColors(
            //         containerColor = MaterialTheme.colorScheme.primary
            //     ),
            //     modifier = Modifier.fillMaxWidth()
            // ) {
            //     Text("Close")
            // }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Spotify API Configuration Dialog
    if (showSpotifyConfigDialog) {
        SpotifyApiConfigDialog(
            currentClientId = spotifyClientId,
            currentClientSecret = spotifyClientSecret,
            onDismiss = { showSpotifyConfigDialog = false },
            onSave = { clientId, clientSecret ->
                appSettings.setSpotifyClientId(clientId)
                appSettings.setSpotifyClientSecret(clientSecret)
                // Auto-enable API if credentials are provided
                if (clientId.isNotEmpty() && clientSecret.isNotEmpty()) {
                    appSettings.setSpotifyApiEnabled(true)
                }
                showSpotifyConfigDialog = false
            },
            appSettings = appSettings
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
    isEnabled: Boolean = true,
    showToggle: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
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
                        when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                            isConfigured -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                        isConfigured -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    },
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
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                            isConfigured -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (!isEnabled) "Disabled" else status,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                                isConfigured -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Toggle or Arrow icon
            if (showToggle && onToggle != null) {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        onToggle(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
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
}
