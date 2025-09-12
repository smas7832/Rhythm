@file:OptIn(ExperimentalMaterial3Api::class)

package chromahub.rhythm.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import chromahub.rhythm.app.viewmodel.MusicViewModel

data class EqualizerPreset(
    val name: String,
    val icon: ImageVector,
    val bands: List<Float>
)

@Composable
fun EqualizerBottomSheetNew(
    musicViewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    
    // Collect states from settings
    val equalizerEnabledState by musicViewModel.equalizerEnabled.collectAsState()
    val equalizerPresetState by musicViewModel.equalizerPreset.collectAsState()
    val equalizerBandLevelsState by musicViewModel.equalizerBandLevels.collectAsState()
    val bassBoostEnabledState by musicViewModel.bassBoostEnabled.collectAsState()
    val bassBoostStrengthState by musicViewModel.bassBoostStrength.collectAsState()
    val virtualizerEnabledState by musicViewModel.virtualizerEnabled.collectAsState()
    val virtualizerStrengthState by musicViewModel.virtualizerStrength.collectAsState()
    
    // Local mutable states for UI
    var isEqualizerEnabled by remember(equalizerEnabledState) { mutableStateOf(equalizerEnabledState) }
    var selectedPreset by remember(equalizerPresetState) { mutableStateOf(equalizerPresetState) }
    var bandLevels by remember(equalizerBandLevelsState) { 
        mutableStateOf(
            equalizerBandLevelsState.split(",").mapNotNull { it.toFloatOrNull() }.let { levels ->
                if (levels.size == 5) levels else List(5) { 0f }
            }
        )
    }
    var isBassBoostEnabled by remember(bassBoostEnabledState) { mutableStateOf(bassBoostEnabledState) }
    var bassBoostStrength by remember(bassBoostStrengthState) { mutableFloatStateOf(bassBoostStrengthState.toFloat()) }
    var isVirtualizerEnabled by remember(virtualizerEnabledState) { mutableStateOf(virtualizerEnabledState) }
    var virtualizerStrength by remember(virtualizerStrengthState) { mutableFloatStateOf(virtualizerStrengthState.toFloat()) }
    
    // Preset definitions
    val presets = listOf(
        EqualizerPreset("Flat", Icons.Rounded.LinearScale, listOf(0f, 0f, 0f, 0f, 0f)),
        EqualizerPreset("Rock", Icons.Rounded.MusicNote, listOf(5f, 3f, -2f, 2f, 8f)),
        EqualizerPreset("Pop", Icons.Rounded.Star, listOf(2f, 5f, 3f, -1f, 2f)),
        EqualizerPreset("Jazz", Icons.Rounded.Piano, listOf(4f, 2f, -2f, 2f, 6f)),
        EqualizerPreset("Classical", Icons.Rounded.LibraryMusic, listOf(3f, -2f, -3f, -1f, 4f)),
        EqualizerPreset("Electronic", Icons.Rounded.ElectricBolt, listOf(6f, 4f, 1f, 3f, 7f)),
        EqualizerPreset("Hip Hop", Icons.Rounded.GraphicEq, listOf(7f, 4f, 0f, 2f, 6f)),
        EqualizerPreset("Vocal", Icons.Rounded.RecordVoiceOver, listOf(0f, 3f, 5f, 4f, 2f))
    )
    
    val frequencyLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
    
    // Functions
    fun applyPreset(preset: EqualizerPreset) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        selectedPreset = preset.name
        bandLevels = preset.bands
        
        // Save to settings
        musicViewModel.appSettings.setEqualizerPreset(preset.name)
        musicViewModel.appSettings.setEqualizerBandLevels(preset.bands.joinToString(","))
        
        // Apply to service
        musicViewModel.applyEqualizerPreset(preset.name, preset.bands)
    }
    
    fun updateBandLevel(band: Int, level: Float) {
        val newLevels = bandLevels.toMutableList()
        newLevels[band] = level
        bandLevels = newLevels
        selectedPreset = "Custom"
        
        // Save to settings
        musicViewModel.appSettings.setEqualizerBandLevels(newLevels.joinToString(","))
        musicViewModel.appSettings.setEqualizerPreset("Custom")
        
        // Apply to service
        val levelShort = (level * 100).toInt().toShort()
        musicViewModel.setEqualizerBandLevel(band.toShort(), levelShort)
    }
    
    fun openSystemEqualizer() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        musicViewModel.openSystemEqualizer()
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
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEqualizerEnabled) "Enhanced audio experience" else "Disabled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEqualizerEnabled,
                        onCheckedChange = { enabled ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            isEqualizerEnabled = enabled
                            musicViewModel.setEqualizerEnabled(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Customize your audio experience with 5-band equalizer and audio effects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isEqualizerEnabled) {
                // Presets Section
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
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Presets",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = selectedPreset,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(presets) { preset ->
                                    val isSelected = selectedPreset == preset.name
                                    
                                    Card(
                                        onClick = { applyPreset(preset) },
                                        modifier = Modifier.size(width = 85.dp, height = 100.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surfaceVariant
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
                                                imageVector = preset.icon,
                                                contentDescription = null,
                                                tint = if (isSelected) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Frequency Bands Section
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
                                    imageVector = Icons.Rounded.Equalizer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Frequency Bands",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Frequency Response Visualization
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    val bandWidth = width / bandLevels.size
                                    
                                    // Draw grid lines
                                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                                    for (i in 0..4) {
                                        val y = height * i / 4f
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                    
                                    // Draw frequency response curve
                                    val primaryColor = Color(0xFF6750A4)
                                    val points = bandLevels.mapIndexed { index, level ->
                                        val x = (index + 0.5f) * bandWidth
                                        val normalizedLevel = (level + 15f) / 30f
                                        val y = height * (1f - normalizedLevel)
                                        Offset(x, y)
                                    }
                                    
                                    // Draw connecting lines
                                    for (i in 0 until points.size - 1) {
                                        drawLine(
                                            color = primaryColor,
                                            start = points[i],
                                            end = points[i + 1],
                                            strokeWidth = 3.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                    
                                    // Draw points
                                    points.forEach { point ->
                                        drawCircle(
                                            color = primaryColor,
                                            radius = 6.dp.toPx(),
                                            center = point
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 3.dp.toPx(),
                                            center = point
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Band Sliders
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                bandLevels.forEachIndexed { index, level ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${level.toInt()}dB",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (level != 0f) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Box(
                                            modifier = Modifier.height(120.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Slider(
                                                value = level,
                                                onValueChange = { newLevel ->
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    updateBandLevel(index, newLevel)
                                                },
                                                valueRange = -15f..15f,
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .rotate(-90f),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = MaterialTheme.colorScheme.outline
                                                )
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = frequencyLabels[index],
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Audio Effects Section
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
                                    imageVector = Icons.Rounded.AudioFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Audio Effects",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Bass Boost
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Speaker,
                                    contentDescription = null,
                                    tint = if (isBassBoostEnabled) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bass Boost",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isBassBoostEnabled) "${(bassBoostStrength/10).toInt()}% intensity" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Switch(
                                    checked = isBassBoostEnabled,
                                    onCheckedChange = { enabled ->
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isBassBoostEnabled = enabled
                                        musicViewModel.setBassBoost(enabled, bassBoostStrength.toInt().toShort())
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                            
                            if (isBassBoostEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Slider(
                                    value = bassBoostStrength,
                                    onValueChange = { strength ->
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        bassBoostStrength = strength
                                        musicViewModel.setBassBoost(true, strength.toInt().toShort())
                                    },
                                    valueRange = 0f..1000f,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Virtualizer
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    tint = if (isVirtualizerEnabled) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Virtualizer",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isVirtualizerEnabled) "${(virtualizerStrength/10).toInt()}% intensity" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Switch(
                                    checked = isVirtualizerEnabled,
                                    onCheckedChange = { enabled ->
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isVirtualizerEnabled = enabled
                                        musicViewModel.setVirtualizer(enabled, virtualizerStrength.toInt().toShort())
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                            
                            if (isVirtualizerEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Slider(
                                    value = virtualizerStrength,
                                    onValueChange = { strength ->
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        virtualizerStrength = strength
                                        musicViewModel.setVirtualizer(true, strength.toInt().toShort())
                                    },
                                    valueRange = 0f..1000f,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
                
                // System Equalizer Section
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "System Equalizer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Access your device's built-in equalizer",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            FilledTonalButton(
                                onClick = { openSystemEqualizer() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open System Equalizer")
                            }
                        }
                    }
                }
            }
        }
    }
}
