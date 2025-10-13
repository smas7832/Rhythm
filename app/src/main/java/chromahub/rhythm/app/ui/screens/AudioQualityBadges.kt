package chromahub.rhythm.app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.util.AudioFormatDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable that displays audio quality badges (Lossless, Dolby, Hi-Res, etc.)
 */
@Composable
fun AudioQualityBadges(
    song: Song,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Get audio format information
    var audioFormatInfo by remember(song.id) { mutableStateOf<AudioFormatDetector.AudioFormatInfo?>(null) }
    
    LaunchedEffect(song.id) {
        withContext(Dispatchers.IO) {
            try {
                audioFormatInfo = AudioFormatDetector.detectFormat(context, song.uri)
            } catch (e: Exception) {
                Log.e("AudioQualityBadges", "Failed to detect audio format", e)
            }
        }
    }
    
    audioFormatInfo?.let { formatInfo ->
        // Only show if there's something to display
        if (formatInfo.isLossless || formatInfo.isDolby || formatInfo.isDTS || formatInfo.isHiRes) {
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lossless badge
                if (formatInfo.isLossless) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "LOSSLESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.HighQuality,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null,
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                
                // Dolby badge
                if (formatInfo.isDolby) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = if (formatInfo.codec.contains("E-AC-3")) "DOLBY D+" else "DOLBY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.SurroundSound,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        border = null,
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                
                // DTS badge
                if (formatInfo.isDTS) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "DTS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.SurroundSound,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        border = null,
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                
                // Hi-Res badge (only if not already showing Lossless)
                if (formatInfo.isHiRes && !formatInfo.isLossless) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "HI-RES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.HighQuality,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null,
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }
    }
}
