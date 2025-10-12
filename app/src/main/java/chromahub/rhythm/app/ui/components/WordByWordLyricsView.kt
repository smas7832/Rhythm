package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.util.AppleMusicLyricsParser
import chromahub.rhythm.app.util.WordByWordLyricLine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Composable for displaying word-by-word synchronized lyrics from Apple Music
 */
@Composable
fun WordByWordLyricsView(
    wordByWordLyrics: String,
    currentPlaybackTime: Long,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onSeek: ((Long) -> Unit)? = null
) {
    val parsedLyrics = remember(wordByWordLyrics) {
        AppleMusicLyricsParser.parseWordByWordLyrics(wordByWordLyrics)
    }

    val coroutineScope = rememberCoroutineScope()
    
    // Find current line index
    val currentLineIndex by remember(currentPlaybackTime, parsedLyrics) {
        derivedStateOf {
            parsedLyrics.indexOfLast { line ->
                currentPlaybackTime >= line.lineTimestamp && currentPlaybackTime <= line.lineEndtime
            }
        }
    }

    // Auto-scroll to current line
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && parsedLyrics.isNotEmpty()) {
            val offset = listState.layoutInfo.viewportSize.height / 3
            coroutineScope.launch {
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
                text = "Word-by-word lyrics not available.",
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
            contentPadding = PaddingValues(vertical = 30.dp)
        ) {
            itemsIndexed(parsedLyrics) { index, line ->
                val isCurrentLine = currentLineIndex == index
                
                // Animated scale for current line
                val scale by animateFloatAsState(
                    targetValue = if (isCurrentLine) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "lineScale"
                )

                // Build annotated string with word-level highlighting
                val annotatedText = buildAnnotatedString {
                    line.words.forEachIndexed { wordIndex, word ->
                        val isWordActive = isCurrentLine && 
                            currentPlaybackTime >= word.timestamp && 
                            currentPlaybackTime <= word.endtime
                        
                        val wordAlpha = when {
                            isWordActive -> 1f
                            isCurrentLine -> 0.8f
                            index == currentLineIndex + 1 -> 0.6f
                            else -> 0.4f
                        }
                        
                        val wordColor = if (isWordActive) {
                            MaterialTheme.colorScheme.primary
                        } else if (isCurrentLine) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = wordAlpha)
                        }
                        
                        withStyle(
                            SpanStyle(
                                color = wordColor,
                                fontWeight = if (isWordActive) FontWeight.Bold else 
                                    if (isCurrentLine) FontWeight.SemiBold else FontWeight.Normal
                            )
                        ) {
                            // Add space before word if it's not a syllable part
                            if (wordIndex > 0 && !word.isPart) {
                                append(" ")
                            }
                            append(word.text)
                        }
                    }
                }
                
                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.4f
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSeek?.invoke(line.lineTimestamp)
                        }
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
