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
import androidx.compose.foundation.layout.Box
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
 * Represents either a lyrics line or a gap indicator
 */
sealed class LyricsItem {
    data class LyricLine(val line: WordByWordLyricLine, val index: Int) : LyricsItem()
    data class Gap(val duration: Long, val startTime: Long) : LyricsItem()
}

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

    // Create items list with gaps for instrumental sections
    val lyricsItems = remember(parsedLyrics) {
        val items = mutableListOf<LyricsItem>()
        parsedLyrics.forEachIndexed { index, line ->
            items.add(LyricsItem.LyricLine(line, index))
            
            // Check for gap to next line
            if (index < parsedLyrics.size - 1) {
                val nextLine = parsedLyrics[index + 1]
                val gapDuration = nextLine.lineTimestamp - line.lineEndtime
                if (gapDuration > 3000) { // 3 seconds threshold
                    items.add(LyricsItem.Gap(gapDuration, line.lineEndtime))
                }
            }
        }
        items
    }

    val coroutineScope = rememberCoroutineScope()
    
    // Find current line index (among lyric lines only)
    val currentLineIndex by remember(currentPlaybackTime, parsedLyrics) {
        derivedStateOf {
            parsedLyrics.indexOfLast { line ->
                currentPlaybackTime >= line.lineTimestamp && currentPlaybackTime <= line.lineEndtime
            }
        }
    }

    // Find current item index (including gaps)
    val currentItemIndex by remember(currentPlaybackTime, lyricsItems) {
        derivedStateOf {
            lyricsItems.indexOfFirst { item ->
                when (item) {
                    is LyricsItem.LyricLine -> 
                        currentPlaybackTime >= item.line.lineTimestamp && currentPlaybackTime <= item.line.lineEndtime
                    is LyricsItem.Gap -> 
                        currentPlaybackTime >= item.startTime && currentPlaybackTime < item.startTime + item.duration
                }
            }.takeIf { it >= 0 } ?: 0
        }
    }

    // Auto-scroll to current lyric line with elastic spring animation
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && parsedLyrics.isNotEmpty()) {
            // Find the corresponding item index in lyricsItems
            val targetItemIndex = lyricsItems.indexOfFirst { item ->
                item is LyricsItem.LyricLine && item.index == currentLineIndex
            }

            if (targetItemIndex >= 0) {
                val offset = listState.layoutInfo.viewportSize.height / 3

                coroutineScope.launch {
                    // Add staggering delay based on line position for elastic effect
                    val delayMs = when {
                        currentLineIndex == 0 -> 0L
                        currentLineIndex < 3 -> 50L
                        else -> 100L + (currentLineIndex * 20L).coerceAtMost(300L)
                    }

                    if (delayMs > 0) {
                        delay(delayMs)
                    }

                    // Use elastic spring animation for smooth, bouncy scrolling
                    listState.animateScrollToItem(
                        index = targetItemIndex,
                        scrollOffset = -offset
                    )
                }
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
            itemsIndexed(lyricsItems) { itemIndex, item ->
                when (item) {
                    is LyricsItem.LyricLine -> {
                        val line = item.line
                        val index = item.index
                        val isCurrentLine = currentLineIndex == index
                        val isUpcomingLine = index > currentLineIndex
                        val linesAhead = index - currentLineIndex
                        
                        // Animated scale for current line with elastic spring
                        val scale by animateFloatAsState(
                            targetValue = when {
                                isCurrentLine -> 1.08f
                                isUpcomingLine && linesAhead == 1 -> 1.02f
                                else -> 1f
                            },
                            animationSpec = spring<Float>(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            ),
                            label = "lineScale"
                        )

                        // Staggered opacity animation for upcoming lines with elastic effect
                        val opacity by animateFloatAsState(
                            targetValue = when {
                                isCurrentLine -> 1f
                                isUpcomingLine && linesAhead <= 4 -> 0.9f - (linesAhead * 0.1f)
                                else -> 0.3f
                            },
                            animationSpec = if (isUpcomingLine) {
                                spring<Float>(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessVeryLow,
                                    visibilityThreshold = 0.01f
                                )
                            } else {
                                spring<Float>(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            },
                            label = "lineOpacity"
                        )

                        // Staggered translation animation for upcoming lines with elastic bounce
                        val animatedTranslationY by animateFloatAsState(
                            targetValue = when {
                                isUpcomingLine && linesAhead <= 3 -> (linesAhead * 6f)
                                else -> 0f
                            },
                            animationSpec = spring<Float>(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            ),
                            label = "lineTranslation"
                        )

                        // Subtle rotation animation for elastic effect
                        val rotationZ by animateFloatAsState(
                            targetValue = if (isCurrentLine) 0.5f else 0f,
                            animationSpec = spring<Float>(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            ),
                            label = "lineRotation"
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
                                    alpha = opacity
                                    translationY = animatedTranslationY
//                                    rotationZ = rotationZ
                                }
                        )
                    }
                    is LyricsItem.Gap -> {
                        // Visual indicator for instrumental gap
                        val isCurrentGap = currentPlaybackTime >= item.startTime && 
                            currentPlaybackTime < item.startTime + item.duration
                        
                        val gapHeight = (item.duration / 1000f).coerceIn(20f, 80f) // 20-80dp based on duration
                        
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gapHeight.dp)
                                .padding(horizontal = 32.dp)
                        )
                        
                        // Musical note icon or wave indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "♪",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = if (isCurrentGap) 0.8f else 0.3f
                                )
                            )
                        }
                        
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gapHeight.dp)
                                .padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }
    }
}
