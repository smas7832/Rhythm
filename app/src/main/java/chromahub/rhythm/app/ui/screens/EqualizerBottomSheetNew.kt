@file:OptIn(ExperimentalMaterial3Api::class)

package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

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
        EqualizerPreset("Electronic", Icons.Rounded.GraphicEq, listOf(6f, 4f, 1f, 3f, 7f)),
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
                .padding(bottom = 24.dp)
//                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
        ) {
            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                EqualizerHeader(
                    isEqualizerEnabled = isEqualizerEnabled,
                    onToggleEqualizer = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isEqualizerEnabled = enabled
                        musicViewModel.setEqualizerEnabled(enabled)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isEqualizerEnabled) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentTranslation
                    }
                ) {
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
                                        
                                        // Animated scale for selected preset
                                        val scale by animateFloatAsState(
                                            targetValue = if (isSelected) 1.05f else 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            label = "presetScale"
                                        )
                                        
                                        // Animated elevation for selected preset
                                        val elevation by animateFloatAsState(
                                            targetValue = if (isSelected) 4f else 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            label = "presetElevation"
                                        )
                                        
                                        Card(
                                            onClick = { applyPreset(preset) },
                                            modifier = Modifier
                                                .size(width = 85.dp, height = 100.dp)
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp)
                                                    .animateContentSize(
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                                            stiffness = Spring.StiffnessMedium
                                                        )
                                                    ),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                // Animated icon size for selected preset
                                                val iconSize by animateFloatAsState(
                                                    targetValue = if (isSelected) 28f else 24f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    ),
                                                    label = "iconSize"
                                                )
                                                
                                                Icon(
                                                    imageVector = preset.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) 
                                                        MaterialTheme.colorScheme.primary 
                                                    else 
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                        .size(iconSize.dp)
                                                        .graphicsLayer {
                                                            if (isSelected) {
                                                                rotationZ = 5f
                                                            }
                                                        }
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

                                Spacer(modifier = Modifier.height(20.dp))

                                // Frequency Response Mini Chart - Moved above bands
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Frequency Response",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp)
                                        ) {
                                            val primaryColor = MaterialTheme.colorScheme.primary
                                            val outlineColor = MaterialTheme.colorScheme.outline
                                            
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val width = size.width
                                                val height = size.height
                                                val bandWidth = width / bandLevels.size

                                                // Draw center line (0dB)
                                                drawLine(
                                                    color = outlineColor.copy(alpha = 0.5f),
                                                    start = Offset(0f, height / 2),
                                                    end = Offset(width, height / 2),
                                                    strokeWidth = 1.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                                                )

                                                // Draw frequency response curve
                                                val points = bandLevels.mapIndexed { index, level ->
                                                    val x = (index + 0.5f) * bandWidth
                                                    val normalizedLevel = (level + 15f) / 30f
                                                    val y = height * (1f - normalizedLevel)
                                                    Offset(x, y)
                                                }

                                                // Draw smooth curve
                                                if (points.size > 1) {
                                                    val path = Path().apply {
                                                        moveTo(points[0].x, points[0].y)
                                                        for (i in 1 until points.size) {
                                                            val p0 = points[i - 1]
                                                            val p1 = points[i]
                                                            val controlX = (p0.x + p1.x) / 2
                                                            quadraticTo(controlX, p0.y, p1.x, p1.y)
                                                        }
                                                    }

                                                    drawPath(
                                                        path = path,
                                                        color = primaryColor,
                                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                                    )
                                                }

                                                // Draw points
                                                points.forEach { point ->
                                                    drawCircle(
                                                        color = primaryColor,
                                                        radius = 4.dp.toPx(),
                                                        center = point
                                                    )
                                                    drawCircle(
                                                        color = Color.White,
                                                        radius = 2.dp.toPx(),
                                                        center = point
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Modern Frequency Bands Grid
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    bandLevels.forEachIndexed { index, level ->
                                        // Individual Band Card
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                // Frequency Label
                                                Column(
                                                    horizontalAlignment = Alignment.Start,
                                                    modifier = Modifier.width(60.dp)
                                                ) {
                                                    Text(
                                                        text = frequencyLabels[index],
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${level.toInt()}dB",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (level != 0f)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                // Level Indicator Bar
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(8.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceVariant,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                ) {
                                                    val progress = (level + 15f) / 30f
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(progress)
                                                            .background(
                                                                if (level > 0f) MaterialTheme.colorScheme.primary
                                                                else if (level < 0f) MaterialTheme.colorScheme.error
                                                                else MaterialTheme.colorScheme.outline,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .animateContentSize()
                                                    )
                                                }

                                                // Slider Control
                                                Box(
                                                    modifier = Modifier.width(120.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Slider(
                                                        value = level,
                                                        onValueChange = { newLevel ->
                                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            updateBandLevel(index, newLevel)
                                                        },
                                                        valueRange = -15f..15f,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = SliderDefaults.colors(
                                                            thumbColor = MaterialTheme.colorScheme.primary,
                                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
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
                                
                                // Bass Boost - No scale, smooth animations
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isBassBoostEnabled) 
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        
                                        AnimatedVisibility(
                                            visible = isBassBoostEnabled,
                                            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                                                   androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(300)),
                                            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + 
                                                  androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(200))
                                        ) {
                                            Column {
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
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Virtualizer - No scale, smooth animations
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isVirtualizerEnabled) 
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        
                                        AnimatedVisibility(
                                            visible = isVirtualizerEnabled,
                                            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                                                   androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(300)),
                                            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + 
                                                  androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(200))
                                        ) {
                                            Column {
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
            } else {
                // Empty state when equalizer is disabled
                EmptyEqualizerContent(
                    musicViewModel = musicViewModel,
                    onEnableEqualizer = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isEqualizerEnabled = enabled
                        musicViewModel.setEqualizerEnabled(enabled)
                    },
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
private fun EqualizerHeader(
    isEqualizerEnabled: Boolean,
    onToggleEqualizer: (Boolean) -> Unit,
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
                text = "Equalizer",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                    text = if (isEqualizerEnabled) "Enabled" else "Disabled",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Switch(
            checked = isEqualizerEnabled,
            onCheckedChange = onToggleEqualizer,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun EmptyEqualizerContent(
    musicViewModel: MusicViewModel,
    onEnableEqualizer: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated empty state with better design
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Equalizer is disabled",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enable the equalizer to customize your audio experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Better styled button
            FilledTonalButton(
                onClick = { musicViewModel.openSystemEqualizer() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open System Equalizer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
