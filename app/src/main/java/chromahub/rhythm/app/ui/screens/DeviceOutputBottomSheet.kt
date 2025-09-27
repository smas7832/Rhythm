package chromahub.rhythm.app.ui.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import chromahub.rhythm.app.data.PlaybackLocation
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.util.HapticUtils
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.SheetValue // Add this import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceOutputBottomSheet(
    locations: List<PlaybackLocation>,
    currentLocation: PlaybackLocation?,
    volume: Float,
    isMuted: Boolean,
    onLocationSelect: (PlaybackLocation) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onMaxVolume: () -> Unit,
    onRefreshDevices: () -> Unit,
    onDismiss: () -> Unit,
    appSettings: AppSettings,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    
    // Get app settings to check if system volume is enabled
    // Note: You'll need to pass AppSettings as a parameter or inject it
    // For now, we'll assume it's passed as a parameter
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    
    // System volume state
    var systemVolume by remember { mutableFloatStateOf(0.5f) }
    var systemMaxVolume by remember { mutableStateOf(15) }
    
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

    // Initialize system volume and monitor for changes
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
        
        // Get system volume
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        systemVolume = currentVolume.toFloat() / maxVolume.toFloat()
        systemMaxVolume = maxVolume
    }
    
    // Monitor system volume changes
    LaunchedEffect(Unit) {
        while (true) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newSystemVolume = currentVolume.toFloat() / maxVolume.toFloat()
            
            if (newSystemVolume != systemVolume) {
                systemVolume = newSystemVolume
                systemMaxVolume = maxVolume
            }
            
            delay(500) // Check every 500ms
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val animatedDragHandlePadding by animateDpAsState(
        targetValue = if (sheetState.currentValue == SheetValue.Expanded) {
            statusBarPadding
        } else {
            0.dp
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "animatedDragHandlePadding"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { 
            Column(
                modifier = Modifier.padding(top = animatedDragHandlePadding)
            ) {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                DeviceOutputHeader(
                    onRefreshDevices = onRefreshDevices,
                    devicesCount = locations.size,
                    haptics = haptics
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Volume Control Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                VolumeControlCard(
                    volume = volume,
                    isMuted = isMuted,
                    systemVolume = systemVolume,
                    systemMaxVolume = systemMaxVolume,
                    appSettings = appSettings,
                    context = context,
                    onVolumeChange = onVolumeChange,
                    onToggleMute = onToggleMute,
                    onMaxVolume = onMaxVolume,
                    onSystemVolumeChange = { newVolume ->
                        systemVolume = newVolume
                    },
                    haptics = haptics
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Devices section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentTranslation
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED DEVICES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                Text(
                    text = "${locations.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Device list
            if (locations.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentTranslation
                        }
                ) {
                    items(locations) { location ->
                        DeviceCard(
                            location = location,
                            isSelected = currentLocation?.id == location.id,
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                onLocationSelect(location)
                            }
                        )
                    }
                }
            } else {
                // Empty state
                EmptyDevicesState(
                    onRefreshClick = onRefreshDevices,
                    modifier = Modifier.graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentTranslation
                    }
                )
            }
        }
    }
}

@Composable
private fun DeviceOutputHeader(
    onRefreshDevices: () -> Unit,
    devicesCount: Int,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Audio",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (devicesCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        text = if (devicesCount == 1) "1 device" else "$devicesCount devices",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Refresh devices button
        FilledTonalIconButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                onRefreshDevices()
            },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = RhythmIcons.Refresh,
                contentDescription = "Refresh devices",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun VolumeControlCard(
    volume: Float,
    isMuted: Boolean,
    systemVolume: Float,
    systemMaxVolume: Int,
    appSettings: AppSettings,
    context: Context,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onMaxVolume: () -> Unit,
    onSystemVolumeChange: (Float) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val useSystemVolume by appSettings.useSystemVolume.collectAsState()
    
    // Current volume values based on setting
    val currentVolume = if (useSystemVolume) systemVolume else volume
    val currentIsMuted = if (useSystemVolume) (systemVolume == 0f) else isMuted
    
    // System volume control functions
    val setSystemVolume = { newVolume: Float ->
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val targetVolume = (newVolume * systemMaxVolume).toInt().coerceIn(0, systemMaxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        onSystemVolumeChange(newVolume)
    }
    
    val toggleSystemMute = {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (systemVolume > 0f) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            onSystemVolumeChange(0f)
        } else {
            val halfVolume = systemMaxVolume / 2
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, halfVolume, 0)
            onSystemVolumeChange(0.5f)
        }
    }
    
    val setSystemMaxVolume = {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemMaxVolume, 0)
        onSystemVolumeChange(1f)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Volume header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (currentIsMuted) RhythmIcons.VolumeOff else 
                                if (currentVolume < 0.3f) RhythmIcons.VolumeMute else 
                                if (currentVolume < 0.7f) RhythmIcons.VolumeDown else 
                                RhythmIcons.VolumeUp,
                    contentDescription = "Volume",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = if (useSystemVolume) "System Volume" else "App Volume",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Volume percentage
                Text(
                    text = "${(if (currentIsMuted) 0f else currentVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Volume controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Volume down button
                IconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        if (useSystemVolume) {
                            val newVolume = (systemVolume - 0.1f).coerceAtLeast(0f)
                            setSystemVolume(newVolume)
                        } else {
                            val newVolume = (volume - 0.1f).coerceAtLeast(0f)
                            onVolumeChange(newVolume)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = RhythmIcons.Remove,
                        contentDescription = "Decrease volume",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Volume slider
                Slider(
                    value = if (currentIsMuted) 0f else currentVolume,
                    onValueChange = { newVolume ->
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        if (useSystemVolume) {
                            setSystemVolume(newVolume)
                        } else {
                            onVolumeChange(newVolume)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                )
                
                // Volume up button
                IconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        if (useSystemVolume) {
                            val newVolume = (systemVolume + 0.1f).coerceAtMost(1f)
                            setSystemVolume(newVolume)
                        } else {
                            val newVolume = (volume + 0.1f).coerceAtMost(1f)
                            onVolumeChange(newVolume)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = RhythmIcons.Add,
                        contentDescription = "Increase volume",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    location: PlaybackLocation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isSelected) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon background
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getDeviceIcon(location),
                        contentDescription = null,
                        tint = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Device info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Device type description
                val typeDescription = when {
                    location.id.startsWith("bt_") -> "Bluetooth device"
                    location.id == "wired_headset" -> "Wired headphones"
                    location.id == "speaker" -> "Phone speaker"
                    else -> "Audio device"
                }
                
                Text(
                    text = typeDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Selected indicator
            if (isSelected) {
                Icon(
                    imageVector = RhythmIcons.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDevicesState(
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = RhythmIcons.Speaker,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No devices found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Try refreshing to find available devices",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FilledTonalIconButton(
                onClick = onRefreshClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.Refresh,
                    contentDescription = "Refresh devices"
                )
            }
        }
    }
}

@Composable
private fun getDeviceIcon(location: PlaybackLocation) = when {
    location.id.startsWith("bt_") -> RhythmIcons.BluetoothFilled
    location.id == "wired_headset" -> RhythmIcons.HeadphonesFilled
    location.id == "speaker" -> RhythmIcons.SpeakerFilled
    else -> RhythmIcons.Speaker
}
