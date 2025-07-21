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
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current

    // Find current line index more efficiently
    val currentLineIndex by remember(currentPlaybackTime, parsedLyrics) {
        derivedStateOf {
            parsedLyrics.indexOfLast { it.timestamp <= currentPlaybackTime }
        }
    }

    // Enhanced auto-scroll with smoother animation
    LaunchedEffect(currentLineIndex, listState.layoutInfo.viewportSize) {
        if (currentLineIndex >= 0 && parsedLyrics.isNotEmpty()) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo

            // Calculate the offset to center the current line
            // Assuming all items have similar height for consistent centering
            val itemHeightPx = visibleItems.firstOrNull()?.size ?: 0
            val viewportHeightPx = listState.layoutInfo.viewportSize.height
            val offsetPx = (viewportHeightPx / 2 - itemHeightPx / 2).coerceAtLeast(0)

            coroutineScope.launch {
                // Scroll to the current line with an offset to center it
                listState.animateScrollToItem(currentLineIndex, scrollOffset = offsetPx)
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
            // Adjust content padding to allow for vertical centering of items
            contentPadding = with(density) {
                PaddingValues(vertical = (listState.layoutInfo.viewportSize.height / 2).toDp())
            }
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
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
