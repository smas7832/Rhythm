package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material Design 3 compliant linear progress indicator with rounded corners
 * Updated to match the latest Material 3 specifications
 */
@Composable
fun M3LinearLoader(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showTrackGap: Boolean = true,
    showStopIndicator: Boolean = true,
    fourColor: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(4.dp) // M3 standard height is 4dp
    ) {
        if (progress != null) {
            // Determinate progress indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp)), // Rounded corners per M3
                color = color,
                trackColor = if (showTrackGap) trackColor else trackColor.copy(alpha = 0.4f),
                strokeCap = if (showStopIndicator) StrokeCap.Round else StrokeCap.Butt
            )
        } else {
            // Indeterminate progress indicator
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp)), // Rounded corners per M3
                color = color,
                trackColor = if (showTrackGap) trackColor else trackColor.copy(alpha = 0.4f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/**
 * Material Design 3 compliant circular progress indicator
 * Updated to match the latest Material 3 specifications
 */
@Composable
fun M3CircularLoader(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    strokeWidth: Float = 4f,
    showTrackGap: Boolean = true,
    fourColor: Boolean = false
) {
    if (progress != null) {
        // Determinate circular progress
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier.size(48.dp),
            color = color,
            trackColor = if (showTrackGap) trackColor else Color.Transparent,
            strokeWidth = strokeWidth.dp,
            strokeCap = StrokeCap.Round // M3 spec uses rounded stroke cap
        )
    } else {
        // Indeterminate circular progress
        CircularProgressIndicator(
            modifier = modifier.size(48.dp),
            color = color,
            trackColor = if (showTrackGap) trackColor else Color.Transparent,
            strokeWidth = strokeWidth.dp,
            strokeCap = StrokeCap.Round // M3 spec uses rounded stroke cap
        )
    }
}

/**
 * Material Design 3 four-color circular progress indicator
 * Uses four colors from the Material 3 color scheme for indeterminate progress
 */
@Composable
fun M3FourColorCircularLoader(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f
) {
    // Use the four colors from Material 3 color scheme
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.primaryContainer
    val color3 = MaterialTheme.colorScheme.tertiary
    val color4 = MaterialTheme.colorScheme.tertiaryContainer
    
    val infiniteTransition = rememberInfiniteTransition(label = "fourColorAnimation")
    val colorIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorIndex"
    )
    
    val currentColor = when {
        colorIndex < 1f -> color1
        colorIndex < 2f -> color2
        colorIndex < 3f -> color3
        else -> color4
    }
    
    CircularProgressIndicator(
        modifier = modifier.size(48.dp),
        color = currentColor,
        trackColor = Color.Transparent,
        strokeWidth = strokeWidth.dp,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Material Design 3 four-color linear progress indicator
 * Uses four colors from the Material 3 color scheme for indeterminate progress
 */
@Composable
fun M3FourColorLinearLoader(
    modifier: Modifier = Modifier
) {
    // Use the four colors from Material 3 color scheme
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.primaryContainer
    val color3 = MaterialTheme.colorScheme.tertiary
    val color4 = MaterialTheme.colorScheme.tertiaryContainer
    
    val infiniteTransition = rememberInfiniteTransition(label = "fourColorAnimation")
    val colorIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorIndex"
    )
    
    val currentColor = when {
        colorIndex < 1f -> color1
        colorIndex < 2f -> color2
        colorIndex < 3f -> color3
        else -> color4
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(4.dp)
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp)),
            color = currentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Material Design 3 linear progress indicator with buffer
 * Shows both determinate progress and buffer progress
 */
@Composable
fun M3BufferedLinearLoader(
    progress: Float,
    buffer: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    bufferColor: Color = color.copy(alpha = 0.4f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(4.dp)
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor)
        )
        
        // Buffer
        Box(
            modifier = Modifier
                .fillMaxWidth(buffer)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(bufferColor)
        )
        
        // Progress
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

/**
 * Enhanced wave slider with Material Design 3 styling
 * Improved from the original WaveSlider with better animations and styling
 */
@Composable
fun M3WaveProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showLabel: Boolean = false
) {
    // Animation for wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )
    
    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progressAnimation"
    )
    
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Draw the wave
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2
                
                // Draw background track
                drawLine(
                    color = trackColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                if (animatedProgress > 0f) {
                    // Create gradient for wave
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(
                            waveColor.copy(alpha = 0.7f),
                            waveColor
                        )
                    )
                    
                    // Draw wavy progress line
                    val progressWidth = width * animatedProgress
                    val path = Path()
                    val amplitude = 4.dp.toPx() // Height of the wave
                    val period = 25.dp.toPx() // Length of one wave cycle
                    
                    // Move to the start point
                    path.moveTo(0f, centerY)
                    
                    // Create the wavy path
                    var x = 0f
                    while (x <= progressWidth) {
                        val waveY = centerY + amplitude * sin((x / period) * 2 * PI.toFloat() + animatedOffset)
                        path.lineTo(x, waveY)
                        x += 1.5f // Smaller increment for smoother curve
                    }
                    
                    // Draw the wavy path
                    drawPath(
                        path = path,
                        brush = gradient,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.cornerPathEffect(8f)
                        )
                    )
                }
            }
        }
        
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Pulsating circular loader that follows Material Design 3 guidelines
 */
@Composable
fun M3PulseLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
    ) {
        // Background pulse circle
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .size(48.dp * scale)
                .align(Alignment.Center),
            color = color.copy(alpha = alpha),
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
        
        // Foreground circle
        CircularProgressIndicator(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center),
            color = color,
            trackColor = Color.Transparent,
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Material Design 3 segmented progress indicator
 * Shows progress as a series of segments that fill up
 */
@Composable
fun M3SegmentedLoader(
    progress: Float,
    modifier: Modifier = Modifier,
    segmentCount: Int = 5,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showLabel: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "segmentProgress"
    )
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val activeSegments = (animatedProgress * segmentCount).toInt()
            val partialSegment = (animatedProgress * segmentCount) - activeSegments
            
            repeat(segmentCount) { index ->
                val segmentColor = when {
                    index < activeSegments -> activeColor
                    index == activeSegments -> activeColor.copy(alpha = partialSegment)
                    else -> inactiveColor
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(segmentColor)
                )
            }
        }
        
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Material Design 3 dot progress indicator
 * Shows progress as a series of animated dots
 */
@Composable
fun M3DotLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotSize: Float = 8f,
    dotCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotAnimation")
    
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val delay = index * 200 // Stagger the animations
            
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotScale$index"
            )
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size((dotSize * scale).dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * Material Design 3 branded loader with surface elevation
 * Shows a circular progress indicator on an elevated surface
 */
@Composable
fun M3BrandedLoader(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    showLabel: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = surfaceColor,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            if (progress != null) {
                // Determinate progress
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(64.dp),
                    color = color,
                    trackColor = trackColor,
                    strokeWidth = 5.dp,
                    strokeCap = StrokeCap.Round
                )
                
                if (showLabel) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Indeterminate progress
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = color,
                    trackColor = trackColor,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Material Design 3 step progress indicator
 * Shows progress as a series of steps with the current step highlighted
 */
@Composable
fun M3StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    completedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 0 until totalSteps) {
            val color = when {
                step < currentStep -> completedColor
                step == currentStep -> activeColor
                else -> inactiveColor
            }
            
            // Step indicator
            Box(
                modifier = Modifier
                    .size(if (step == currentStep) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            // Connector line between steps (except after the last step)
            if (step < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (step < currentStep) completedColor else inactiveColor
                        )
                )
            }
        }
    }
} 