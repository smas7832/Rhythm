package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.util.LyricsParser
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SyncedLyricsView(
    lyrics: String,
    currentPlaybackTime: Long,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val parsedLyrics = remember(lyrics) {
        LyricsParser.parseLyrics(lyrics)
    }

    val coroutineScope = rememberCoroutineScope()
    
    // Find current line index more efficiently
    val currentLineIndex by remember(currentPlaybackTime, parsedLyrics) {
        derivedStateOf {
            parsedLyrics.indexOfLast { it.timestamp <= currentPlaybackTime }
        }
    }

    // Enhanced auto-scroll with smoother animation
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && parsedLyrics.isNotEmpty()) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val firstVisibleItem = visibleItems.firstOrNull()?.index ?: 0
            val lastVisibleItem = visibleItems.lastOrNull()?.index ?: 0
            // Calculate the offset to place the current line at the middle
            val offset = listState.layoutInfo.viewportSize.height / 3
            coroutineScope.launch {
                // Scroll to the current line with an offset
                listState.animateScrollToItem(currentLineIndex, scrollOffset = -offset)
            }
        }
    }

    if (parsedLyrics.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Synchronized lyrics not available for this format.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 30.dp) // Increased padding for better centering
        ) {
            itemsIndexed(parsedLyrics) { index, line ->
                val isCurrentLine = currentLineIndex == index
                
                // Calculate progress towards the next line for smoother transition
                val progressToNextLine = if (isCurrentLine && index + 1 < parsedLyrics.size) {
                    val nextLineTimestamp = parsedLyrics[index + 1].timestamp
                    val timeDiff = nextLineTimestamp - line.timestamp
                    if (timeDiff > 0) {
                        ((currentPlaybackTime - line.timestamp).toFloat() / timeDiff).coerceIn(0f, 1f)
                    } else 0f
                } else 0f

                // Animated alpha for smoother transitions and fading out
                val alpha by animateFloatAsState(
                    targetValue = when {
                        isCurrentLine -> 1f
                        index == currentLineIndex + 1 -> 0.7f + (0.3f * (1 - progressToNextLine)) // Fade in next line
                        index < currentLineIndex -> 0.4f // Past lines
                        else -> 0.6f // Future lines
                    },
                    animationSpec = tween(300),
                    label = "lyricAlpha"
                )
                
                // Animated scale for current line emphasis, with slight scaling for next line
                val scale by animateFloatAsState(
                    targetValue = when {
                        isCurrentLine -> 1.05f
                        index == currentLineIndex + 1 -> 1f + (0.05f * progressToNextLine) // Scale up next line
                        else -> 1f
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "lyricScale"
                )

                Text(
                    text = line.text,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Medium,
                        lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.4f
                    ),
                    color = if (isCurrentLine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}
