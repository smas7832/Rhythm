package chromahub.rhythm.app.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.util.AudioQualityDetector
import chromahub.rhythm.app.util.AudioFormatDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Quality level for badge styling
 */
private enum class QualityLevel {
    EXCELLENT,  // Studio Master, Dolby Atmos/TrueHD, DTS-HD MA
    GOOD,       // Hi-Res Lossless, CD Quality Lossless
    STANDARD    // Dolby Digital, DTS, High-quality lossy
}

/**
 * Composable that displays audio quality badges (Lossless, Dolby, Hi-Res, etc.)
 * Updated to use the enhanced AudioQualityDetector for more accurate quality categorization
 */
@Composable
fun AudioQualityBadges(
    song: Song,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Get enhanced audio quality information
    var audioQuality by remember(song.id) { mutableStateOf<AudioQualityDetector.AudioQuality?>(null) }

    LaunchedEffect(song.id) {
        withContext(Dispatchers.IO) {
            try {
                // First try to get format info for codec detection
                // Pass song object for better bit depth calculation
                val formatInfo = AudioFormatDetector.detectFormat(context, song.uri, song)

                // Use the enhanced quality detector with format info
                // If format info doesn't have bitrate, use song's bitrate
                val bitrateKbps = if (formatInfo.bitrateKbps > 0) {
                    formatInfo.bitrateKbps
                } else {
                    (song.bitrate ?: 0) / 1000
                }
                
                audioQuality = AudioQualityDetector.detectQuality(
                    codec = formatInfo.codec,
                    sampleRateHz = formatInfo.sampleRateHz,
                    bitrateKbps = bitrateKbps,
                    bitDepth = formatInfo.bitDepth,
                    channelCount = formatInfo.channelCount
                )
            } catch (e: Exception) {
                Log.e("AudioQualityBadges", "Failed to detect audio quality", e)
            }
        }
    }

    audioQuality?.let { quality ->
        // Only show badges for meaningful quality indicators
        val shouldShowBadges = quality.isLossless || quality.isDolby || quality.isDTS || quality.isHiRes ||
                              quality.qualityType != AudioQualityDetector.QualityType.LOSSY_COMPRESSED

        if (shouldShowBadges) {
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary quality badge based on type with enhanced color coding
                when (quality.qualityType) {
                    AudioQualityDetector.QualityType.HI_RES_STUDIO_MASTER -> {
                        QualityBadge(
                            text = "STUDIO MASTER",
                            icon = Icons.Rounded.HighQuality,
                            qualityLevel = QualityLevel.EXCELLENT
                        )
                    }

                    AudioQualityDetector.QualityType.DOLBY_LOSSLESS -> {
                        val badgeText = when {
                            quality.qualityLabel.contains("Atmos") -> "DOLBY ATMOS"
                            quality.qualityLabel.contains("TrueHD") -> "DOLBY TRUEHD"
                            else -> "DOLBY"
                        }
                        QualityBadge(
                            text = badgeText,
                            icon = Icons.Rounded.SurroundSound,
                            qualityLevel = QualityLevel.EXCELLENT
                        )
                    }

                    AudioQualityDetector.QualityType.DOLBY_LOSSY_SURROUND -> {
                        val badgeText = when {
                            quality.qualityLabel.contains("Plus") -> "DOLBY D+"
                            else -> "DOLBY D"
                        }
                        QualityBadge(
                            text = badgeText,
                            icon = Icons.Rounded.SurroundSound,
                            qualityLevel = QualityLevel.STANDARD
                        )
                    }

                    AudioQualityDetector.QualityType.DTS_SURROUND -> {
                        val badgeText = when {
                            quality.isLossless -> "DTS-HD MA"
                            else -> "DTS"
                        }
                        QualityBadge(
                            text = badgeText,
                            icon = Icons.Rounded.SurroundSound,
                            qualityLevel = if (quality.isLossless) QualityLevel.EXCELLENT else QualityLevel.STANDARD
                        )
                    }

                    AudioQualityDetector.QualityType.HI_RES_LOSSLESS -> {
                        QualityBadge(
                            text = "HI-RES LOSSLESS",
                            icon = Icons.Rounded.HighQuality,
                            qualityLevel = QualityLevel.GOOD
                        )
                    }

                    AudioQualityDetector.QualityType.CD_QUALITY_LOSSLESS -> {
                        QualityBadge(
                            text = "LOSSLESS",
                            icon = Icons.Rounded.HighQuality,
                            qualityLevel = QualityLevel.GOOD
                        )
                    }

                    AudioQualityDetector.QualityType.LOSSY_COMPRESSED -> {
                        // Only show high-quality lossy badges (320kbps+)
                        if (quality.qualityDescription.contains("320")) {
                            QualityBadge(
                                text = "320K",
                                icon = Icons.Rounded.HighQuality,
                                qualityLevel = QualityLevel.STANDARD
                            )
                        }
                    }

                    AudioQualityDetector.QualityType.UNKNOWN -> {
                        // Don't show unknown quality badges
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Badge with gradient, animation, and dynamic colors
 */
@Composable
private fun QualityBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    qualityLevel: QualityLevel,
    modifier: Modifier = Modifier
) {
    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Pulsing animation for excellent quality
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (qualityLevel == QualityLevel.EXCELLENT) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Material 3 Expressive colors with gradient
    val (gradientColors, contentColor) = when (qualityLevel) {
        QualityLevel.EXCELLENT -> listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer,
        ) to MaterialTheme.colorScheme.onPrimaryContainer
        
        QualityLevel.GOOD -> listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
        ) to MaterialTheme.colorScheme.onSecondaryContainer
        
        QualityLevel.STANDARD -> listOf(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        ) to MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    AnimatedVisibility(
        visible = visible,
        // enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = spring(
        //     dampingRatio = Spring.DampingRatioMediumBouncy,
        //     stiffness = Spring.StiffnessLow
        // )),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .scale(scale)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            shadowElevation = if (qualityLevel == QualityLevel.EXCELLENT) 0.dp else 0.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(gradientColors)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Legacy badge composable for backwards compatibility
 */
@Composable
private fun LegacyQualityBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    qualityLevel: QualityLevel,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (qualityLevel) {
        QualityLevel.EXCELLENT -> 
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        QualityLevel.GOOD -> 
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        QualityLevel.STANDARD -> 
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            leadingIconContentColor = contentColor
        ),
        border = null,
        modifier = modifier.height(28.dp)
    )
}
