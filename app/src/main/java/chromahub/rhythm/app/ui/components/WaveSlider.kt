package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.theme.PlayerProgressColor
import kotlin.math.PI
import kotlin.math.sin

/**
 * A custom slider with a wavy line effect for the progress indicator,
 * matching the one shown in the screenshots.
 */
@Composable
fun WaveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    waveColor: Color = PlayerProgressColor,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    enabled: Boolean = true,
    isPlaying: Boolean = true
) {
    var sliderPosition by remember { mutableStateOf(value) }
    var isDragging by remember { mutableStateOf(false) }
    val layoutDirection = LocalLayoutDirection.current
    
    // Update sliderPosition when value changes
    LaunchedEffect(value) {
        if (!isDragging) {
            sliderPosition = value
        }
    }

    // Animation for wave movement - only animate when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 2 * PI.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Increased height for handle
            .padding(horizontal = 16.dp)
    ) {
        // Draw the wave and handle
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures { offset ->
                            val newPosition = (offset.x / size.width).coerceIn(0f, 1f)
                            val adjustedPosition = if (layoutDirection == LayoutDirection.Rtl) {
                                1f - newPosition
                            } else {
                                newPosition
                            }
                            sliderPosition = adjustedPosition
                            onValueChange(adjustedPosition)
                        }
                    }
                }
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false }
                        ) { _, dragAmount ->
                            val dragDelta = dragAmount.x / size.width
                            val adjustedDelta = if (layoutDirection == LayoutDirection.Rtl) {
                                -dragDelta
                            } else {
                                dragDelta
                            }
                            val newPosition = (sliderPosition + adjustedDelta).coerceIn(0f, 1f)
                            sliderPosition = newPosition
                            onValueChange(newPosition)
                        }
                    }
                }
        ) {
            val width = size.width
            val centerY = size.height / 2
            val handleX = if (layoutDirection == LayoutDirection.Rtl) {
                width * (1f - sliderPosition)
            } else {
                width * sliderPosition
            }
            
            // Draw wave background and progress
            drawWaveProgress(
                width = width,
                centerY = centerY,
                progressPosition = handleX,
                waveColor = waveColor,
                trackColor = trackColor,
                isPlaying = isPlaying,
                animatedOffset = animatedOffset
            )
            
            // Draw the custom expressive handle
            drawExpressiveHandle(
                centerX = handleX,
                centerY = centerY,
                handleColor = waveColor,
                isPlaying = isPlaying,
                layoutDirection = layoutDirection
            )
        }
    }
}

/**
 * Draws the wave progress line
 */
private fun DrawScope.drawWaveProgress(
    width: Float,
    centerY: Float,
    progressPosition: Float,
    waveColor: Color,
    trackColor: Color,
    isPlaying: Boolean,
    animatedOffset: Float
) {
    // Draw background track for the inactive part
    if (progressPosition < width) {
        drawLine(
            color = trackColor,
            start = Offset(progressPosition, centerY),
            end = Offset(width, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    
    if (progressPosition > 0f) {
        if (isPlaying) {
            // Draw wavy progress line when playing
            val path = Path()
            val amplitude = 5.dp.toPx() // Height of the wave
            val period = 30.dp.toPx() // Length of one wave cycle
            
            // Move to the start point
            path.moveTo(0f, centerY)
            
            // Create the wavy path
            var x = 0f
            while (x <= progressPosition) {
                val waveY = centerY + amplitude * sin((x / period) * 2 * PI.toFloat() + animatedOffset)
                path.lineTo(x, waveY)
                x += 2f // Increment for smoother curve
            }
            
            // Draw the wavy path
            drawPath(
                path = path,
                color = waveColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.cornerPathEffect(16f)
                )
            )
        } else {
            // Draw straight line when paused
            drawLine(
                color = waveColor,
                start = Offset(0f, centerY),
                end = Offset(progressPosition, centerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draws the Material 3 expressive handle shape
 * Based on Material 3 design guidelines for expressive components
 */
private fun DrawScope.drawExpressiveHandle(
    centerX: Float,
    centerY: Float,
    handleColor: Color,
    isPlaying: Boolean,
    layoutDirection: LayoutDirection
) {
    val handleSize = 24.dp.toPx()
    val innerRadius = 8.dp.toPx()
    
    // Create expressive shape using organic curves
    val handlePath = Path().apply {
        val radius = handleSize / 2
        val controlOffset = radius * 0.3f // Control point offset for organic curves
        
        // Start from top
        moveTo(centerX, centerY - radius)
        
        // Top-right curve (asymmetric for expressiveness)
        cubicTo(
            centerX + controlOffset, centerY - radius,
            centerX + radius, centerY - controlOffset * 0.7f,
            centerX + radius, centerY
        )
        
        // Right-bottom curve
        cubicTo(
            centerX + radius, centerY + controlOffset * 1.2f,
            centerX + controlOffset * 0.8f, centerY + radius,
            centerX, centerY + radius
        )
        
        // Bottom-left curve
        cubicTo(
            centerX - controlOffset * 0.8f, centerY + radius,
            centerX - radius, centerY + controlOffset * 1.2f,
            centerX - radius, centerY
        )
        
        // Left-top curve (completing the organic shape)
        cubicTo(
            centerX - radius, centerY - controlOffset * 0.7f,
            centerX - controlOffset, centerY - radius,
            centerX, centerY - radius
        )
        
        close()
    }
    
    // Draw the main handle shape with elevation effect
    drawPath(
        path = handlePath,
        color = handleColor.copy(alpha = 0.95f)
    )
    
    // Add inner accent for dynamic feel when playing
    if (isPlaying) {
        val innerPath = Path().apply {
            val innerControlOffset = innerRadius * 0.4f
            
            // Create smaller inner shape with slightly different curves
            moveTo(centerX, centerY - innerRadius)
            
            cubicTo(
                centerX + innerControlOffset, centerY - innerRadius,
                centerX + innerRadius, centerY - innerControlOffset * 0.6f,
                centerX + innerRadius, centerY
            )
            
            cubicTo(
                centerX + innerRadius, centerY + innerControlOffset * 1.3f,
                centerX + innerControlOffset * 0.7f, centerY + innerRadius,
                centerX, centerY + innerRadius
            )
            
            cubicTo(
                centerX - innerControlOffset * 0.7f, centerY + innerRadius,
                centerX - innerRadius, centerY + innerControlOffset * 1.3f,
                centerX - innerRadius, centerY
            )
            
            cubicTo(
                centerX - innerRadius, centerY - innerControlOffset * 0.6f,
                centerX - innerControlOffset, centerY - innerRadius,
                centerX, centerY - innerRadius
            )
            
            close()
        }
        
        // Draw the inner accent with a lighter color
        drawPath(
            path = innerPath,
            color = handleColor.copy(alpha = 0.4f)
        )
    }
    
    // Add subtle shadow/elevation effect
    val shadowOffset = 2.dp.toPx()
    val shadowPath = Path().apply {
        val radius = handleSize / 2
        val controlOffset = radius * 0.3f
        
        // Offset the entire path slightly for shadow effect
        moveTo(centerX + shadowOffset * 0.5f, centerY - radius + shadowOffset)
        
        cubicTo(
            centerX + controlOffset + shadowOffset * 0.5f, centerY - radius + shadowOffset,
            centerX + radius + shadowOffset * 0.5f, centerY - controlOffset * 0.7f + shadowOffset,
            centerX + radius + shadowOffset * 0.5f, centerY + shadowOffset
        )
        
        cubicTo(
            centerX + radius + shadowOffset * 0.5f, centerY + controlOffset * 1.2f + shadowOffset,
            centerX + controlOffset * 0.8f + shadowOffset * 0.5f, centerY + radius + shadowOffset,
            centerX + shadowOffset * 0.5f, centerY + radius + shadowOffset
        )
        
        cubicTo(
            centerX - controlOffset * 0.8f + shadowOffset * 0.5f, centerY + radius + shadowOffset,
            centerX - radius + shadowOffset * 0.5f, centerY + controlOffset * 1.2f + shadowOffset,
            centerX - radius + shadowOffset * 0.5f, centerY + shadowOffset
        )
        
        cubicTo(
            centerX - radius + shadowOffset * 0.5f, centerY - controlOffset * 0.7f + shadowOffset,
            centerX - controlOffset + shadowOffset * 0.5f, centerY - radius + shadowOffset,
            centerX + shadowOffset * 0.5f, centerY - radius + shadowOffset
        )
        
        close()
    }
    
    // Draw shadow behind the main handle
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.15f)
    )
}