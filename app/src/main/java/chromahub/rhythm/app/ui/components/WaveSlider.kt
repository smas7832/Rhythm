package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    enabled: Boolean = true
) {
    var sliderPosition by remember { mutableStateOf(value) }
    
    // Update sliderPosition when value changes
    LaunchedEffect(value) {
        sliderPosition = value
    }
    
    // Animation for wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Draw the wave
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(30.dp)
        ) {
            val width = size.width
            val height = size.height / 2
            val centerY = size.height / 2
            
            // Draw background track
            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            if (sliderPosition > 0f) {
                // Draw wavy progress line
                val progressWidth = width * sliderPosition
                val path = Path()
                val amplitude = 5.dp.toPx() // Height of the wave
                val period = 30.dp.toPx() // Length of one wave cycle
                
                // Move to the start point
                path.moveTo(0f, centerY)
                
                // Create the wavy path
                var x = 0f
                while (x <= progressWidth) {
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
            }
        }
        
        // Invisible slider to handle user interaction
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                onValueChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = waveColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
} 