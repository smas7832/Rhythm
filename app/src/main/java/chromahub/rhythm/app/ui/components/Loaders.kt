package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asAndroidPath
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

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
    showTrackGap: Boolean = true, // This parameter will be ignored for wavy loader
    showStopIndicator: Boolean = true, // This parameter will be ignored for wavy loader
    fourColor: Boolean = false // This parameter will be ignored for wavy loader
) {
    M3WaveProgressIndicator(
        progress = progress ?: 1f, // Use 1f for indeterminate
        modifier = modifier,
        waveColor = color,
        trackColor = trackColor,
        showLabel = false // Linear loader typically doesn't show label
    )
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
    strokeWidth: Float = 4f, // This parameter will be ignored for wavy loader
    showTrackGap: Boolean = true, // This parameter will be ignored for wavy loader
    fourColor: Boolean = false // This parameter will be ignored for wavy loader
) {
    M3CircularWaveProgressIndicator(
        progress = progress ?: 1f, // Use 1f for indeterminate
        modifier = modifier,
        colors = if (fourColor) {
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            listOf(color, color) // Ensure at least two colors for sweepGradient
        },
        trackColor = trackColor,
        strokeWidth = strokeWidth
    )
}

/**
 * Material Design 3 four-color circular progress indicator
 * Uses four colors from the Material 3 color scheme for indeterminate progress
 */
@Composable
fun M3FourColorCircularLoader(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f // This parameter will be ignored for wavy loader
) {
    // The four-color animation will be replaced by a single wave color
    M3CircularWaveProgressIndicator(
        progress = 1f, // Indeterminate, so full wave
        modifier = modifier,
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeWidth = strokeWidth
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
    // The four-color animation will be replaced by a single wave color
    M3WaveProgressIndicator(
        progress = 1f, // Indeterminate, so full wave
        modifier = modifier,
        waveColor = MaterialTheme.colorScheme.primary, // Use primary color
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        showLabel = false
    )
}

/**
 * Material Design 3 linear progress indicator with buffer
 * Shows both determinate progress and buffer progress
 */
@Composable
fun M3BufferedLinearLoader(
    progress: Float,
    buffer: Float, // This parameter will be ignored for wavy loader
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    bufferColor: Color = color.copy(alpha = 0.4f) // This parameter will be ignored for wavy loader
) {
    M3WaveProgressIndicator(
        progress = progress,
        modifier = modifier,
        waveColor = color,
        trackColor = trackColor,
        showLabel = false
    )
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
            animation = tween(1500, easing = LinearEasing), // Slightly slower for smoother effect
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val animatedWaveAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAmplitude"
    )

    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
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
                    .drawBehind {
                        if (animatedProgress > 0f) {
                            val shadowColor = Color.Black.copy(alpha = 0.2f)
                            val shadowRadius = 4.dp.toPx()
                            val shadowOffset = Offset(0f, 2.dp.toPx())

                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    color = shadowColor.toArgb()
                                    maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
                                }
                                val path = Path()
                                val width = size.width
                                val height = size.height
                                val centerY = height / 2
                                val progressWidth = width * animatedProgress
                                val amplitude = 4.dp.toPx() * animatedWaveAmplitude
                                val period = 25.dp.toPx()

                                path.moveTo(0f, centerY + shadowOffset.y)
                                var x = 0f
                                while (x <= progressWidth) {
                                    val waveY = centerY + amplitude * sin((x / period) * 2 * PI.toFloat() + animatedOffset) + shadowOffset.y
                                    path.lineTo(x, waveY)
                                    x += 1.5f
                                }
                                canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                            }
                        }
                    }
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
                    val amplitude = 4.dp.toPx() * animatedWaveAmplitude // Height of the wave, animated
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
 * Material Design 3 compliant circular wavy progress indicator
 */
@Composable
fun M3CircularWaveProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary), // Ensure default has at least two colors
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Float = 4f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circularWaveAnimation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Slightly slower for smoother effect
            repeatMode = RepeatMode.Restart
        ),
        label = "circularWaveOffset"
    )

    val animatedWaveAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circularWaveAmplitude"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "circularProgressAnimation"
    )

    // Ensure colors list has at least two elements for sweepGradient
    val safeColors = if (colors.size < 2) {
        // Fallback to a default gradient if not enough colors are provided
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
    } else {
        colors
    }

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(48.dp)
                .drawBehind {
                    if (animatedProgress > 0f) {
                        val shadowColor = Color.Black.copy(alpha = 0.2f)
                        val shadowRadius = 4.dp.toPx()
                        val shadowOffset = Offset(2.dp.toPx(), 2.dp.toPx()) // Slight offset for circular shadow

                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = shadowColor.toArgb()
                                maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
                            }
                            val path = Path()
                            val radius = size.minDimension / 2 - strokeWidth.dp.toPx() / 2
                            val center = Offset(size.width / 2, size.height / 2)

                            val startAngle = -90f
                            val sweepAngle = animatedProgress * 360f
                            val amplitude = 2.dp.toPx() * animatedWaveAmplitude

                            for (angle in 0..sweepAngle.toInt()) {
                                val currentAngleRad = Math.toRadians(startAngle + angle.toDouble()).toFloat()
                                val waveOffset = amplitude * sin(currentAngleRad * 5 + animatedOffset)

                                val x = center.x + (radius + waveOffset) * cos(currentAngleRad) + shadowOffset.x
                                val y = center.y + (radius + waveOffset) * sin(currentAngleRad) + shadowOffset.y

                                if (angle == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                        }
                    }
                }
        ) {
            val radius = size.minDimension / 2 - strokeWidth.dp.toPx() / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Draw background track
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            )

            if (animatedProgress > 0f) {
                val gradient = Brush.sweepGradient(
                    colors = safeColors, // Use safeColors here
                    center = center
                )

                val path = Path()
                val startAngle = -90f // Start from top
                val sweepAngle = animatedProgress * 360f
                val amplitude = 2.dp.toPx() * animatedWaveAmplitude // Height of the wave, animated

                for (angle in 0..sweepAngle.toInt()) {
                    val currentAngleRad = Math.toRadians(startAngle + angle.toDouble()).toFloat()
                    val waveOffset = amplitude * sin(currentAngleRad * 6 + animatedOffset) // 6 waves around the circle

                    val x = center.x + (radius + waveOffset) * cos(currentAngleRad)
                    val y = center.y + (radius + waveOffset) * sin(currentAngleRad)

                    if (angle == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    brush = gradient,
                    style = Stroke(
                        width = strokeWidth.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.cornerPathEffect(8f)
                    )
                )
            }
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
    // Pulse animation will be replaced by circular wave animation
    M3CircularWaveProgressIndicator(
        progress = 1f, // Indeterminate, so full wave
        modifier = modifier,
        colors = listOf(color, color), // Ensure at least two colors for sweepGradient
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeWidth = 4f
    )
}

/**
 * Material Design 3 segmented progress indicator
 * Shows progress as a series of segments that fill up
 */
@Composable
fun M3SegmentedLoader(
    progress: Float,
    modifier: Modifier = Modifier,
    segmentCount: Int = 5, // This parameter will be ignored for wavy loader
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showLabel: Boolean = false
) {
    M3WaveProgressIndicator(
        progress = progress,
        modifier = modifier,
        waveColor = activeColor, // Use activeColor as waveColor
        trackColor = inactiveColor, // Use inactiveColor as trackColor
        showLabel = showLabel
    )
}

/**
 * Material Design 3 dot progress indicator
 * Shows progress as a series of animated dots
 */
@Composable
fun M3DotLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotSize: Float = 8f, // This parameter will be ignored for wavy loader
    dotCount: Int = 3 // This parameter will be ignored for wavy loader
) {
    // Dot animation will be replaced by circular wave animation
    M3CircularWaveProgressIndicator(
        progress = 1f, // Indeterminate, so full wave
        modifier = modifier,
        colors = listOf(color, color), // Ensure at least two colors for sweepGradient
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeWidth = 4f
    )
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
    surfaceColor: Color = MaterialTheme.colorScheme.surface, // This parameter will be ignored for wavy loader
    showLabel: Boolean = false
) {
    // Branded loader will be replaced by circular wave animation
    M3CircularWaveProgressIndicator(
        progress = progress ?: 1f, // Use 1f for indeterminate
        modifier = modifier,
        colors = listOf(color, color), // Ensure at least two colors for sweepGradient
        trackColor = trackColor,
        strokeWidth = 5f // Use a slightly thicker stroke for branded loader
    )
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
    // Step progress will be replaced by linear wave animation
    val progress = if (totalSteps > 0) currentStep.toFloat() / totalSteps.toFloat() else 0f
    M3WaveProgressIndicator(
        progress = progress,
        modifier = modifier,
        waveColor = activeColor,
        trackColor = inactiveColor,
        showLabel = true // Show label for step progress
    )
}
