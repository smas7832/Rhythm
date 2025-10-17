package chromahub.rhythm.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.theme.FestiveTheme
import chromahub.rhythm.app.ui.theme.FestiveThemeConfig
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Particle data class for festive animations
 */
data class FestiveParticle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var size: Float,
    var color: Color,
    var alpha: Float,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    val type: ParticleType = ParticleType.CIRCLE
)

enum class ParticleType {
    CIRCLE,
    STAR,
    HEART,
    SPARKLE,
    SNOWFLAKE,
    LEAF,
    DIYA,
    FIREWORK
}

/**
 * Festive decorations overlay with animated particles
 */
@Composable
fun FestiveDecorations(
    config: FestiveThemeConfig,
    modifier: Modifier = Modifier
) {
    if (!config.enabled || config.selectedTheme == FestiveTheme.NONE || !config.showParticles) {
        return
    }

    val density = LocalDensity.current
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }
    
    val particleCount = (30 * config.particleIntensity).toInt().coerceAtLeast(10)
    val particles = remember { mutableStateListOf<FestiveParticle>() }
    
    // Initialize particles
    LaunchedEffect(config.selectedTheme, particleCount) {
        particles.clear()
        repeat(particleCount) {
            particles.add(createRandomParticle(config.selectedTheme, screenWidth, screenHeight))
        }
    }
    
    // Animate particles
    LaunchedEffect(config.selectedTheme) {
        while (true) {
            delay(8L) // ~120 FPS for smoother animation
            particles.forEachIndexed { index, particle ->
                updateParticle(particle, screenWidth, screenHeight, config.selectedTheme)
                
                // Respawn particle if it goes off screen
                if (particle.y > screenHeight + 50 || particle.x < -50 || particle.x > screenWidth + 50) {
                    particles[index] = createRandomParticle(config.selectedTheme, screenWidth, screenHeight, respawn = true)
                }
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        screenWidth = size.width
        screenHeight = size.height
        
        particles.forEach { particle ->
            drawParticle(particle)
        }
    }
}

/**
 * Create a random particle based on the festive theme
 */
private fun createRandomParticle(
    theme: FestiveTheme,
    screenWidth: Float,
    screenHeight: Float,
    respawn: Boolean = false
): FestiveParticle {
    val particleType = getParticleTypeForTheme(theme)
    val colors = theme.particleColors.ifEmpty { 
        listOf(theme.primaryColor, theme.secondaryColor, theme.tertiaryColor) 
    }
    
    return FestiveParticle(
        x = Random.nextFloat() * screenWidth,
        y = if (respawn) -50f else Random.nextFloat() * screenHeight,
        velocityX = (Random.nextFloat() - 0.5f) * 2f,
        velocityY = when (theme) {
            FestiveTheme.CHRISTMAS -> Random.nextFloat() * 1.5f + 0.5f // Snow falls down
            FestiveTheme.HOLI -> (Random.nextFloat() - 0.5f) * 3f // Colors fly everywhere
            FestiveTheme.NEW_YEAR -> -Random.nextFloat() * 3f - 1f // Fireworks go up
            else -> Random.nextFloat() * 2f + 0.5f
        },
        size = Random.nextFloat() * 8f + 4f,
        color = colors.random(),
        alpha = Random.nextFloat() * 0.5f + 0.3f,
        rotation = Random.nextFloat() * 360f,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 5f,
        type = particleType
    )
}

/**
 * Get appropriate particle type for the theme
 */
private fun getParticleTypeForTheme(theme: FestiveTheme): ParticleType {
    return when (theme) {
        FestiveTheme.CHRISTMAS -> if (Random.nextFloat() > 0.5f) ParticleType.SNOWFLAKE else ParticleType.STAR
        FestiveTheme.DIWALI -> if (Random.nextFloat() > 0.5f) ParticleType.DIYA else ParticleType.SPARKLE
        FestiveTheme.VALENTINES -> if (Random.nextFloat() > 0.5f) ParticleType.HEART else ParticleType.SPARKLE
        FestiveTheme.NEW_YEAR -> ParticleType.FIREWORK
        FestiveTheme.HOLI -> ParticleType.CIRCLE
        FestiveTheme.HALLOWEEN -> if (Random.nextFloat() > 0.5f) ParticleType.STAR else ParticleType.CIRCLE
        FestiveTheme.EASTER -> ParticleType.CIRCLE
        FestiveTheme.INDEPENDENCE_DAY -> ParticleType.STAR
        FestiveTheme.THANKSGIVING -> ParticleType.LEAF
        else -> ParticleType.CIRCLE
    }
}

/**
 * Update particle position and properties
 */
private fun updateParticle(
    particle: FestiveParticle,
    screenWidth: Float,
    screenHeight: Float,
    theme: FestiveTheme
) {
    particle.x += particle.velocityX
    particle.y += particle.velocityY
    particle.rotation += particle.rotationSpeed
    
    // Add some wave motion for certain themes
    when (theme) {
        FestiveTheme.CHRISTMAS -> {
            particle.x += sin(particle.y / 50f) * 0.5f
        }
        FestiveTheme.HOLI -> {
            particle.alpha = (sin(particle.y / 30f) * 0.3f + 0.5f).coerceIn(0.2f, 0.8f)
        }
        FestiveTheme.NEW_YEAR -> {
            // Fireworks fade out as they rise
            particle.alpha -= 0.01f
            if (particle.alpha < 0.1f) particle.alpha = 0.8f
        }
        else -> {}
    }
}

/**
 * Draw particle on canvas
 */
private fun DrawScope.drawParticle(particle: FestiveParticle) {
    when (particle.type) {
        ParticleType.CIRCLE -> {
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
        ParticleType.STAR -> {
            drawStar(particle)
        }
        ParticleType.HEART -> {
            drawHeart(particle)
        }
        ParticleType.SPARKLE -> {
            drawSparkle(particle)
        }
        ParticleType.SNOWFLAKE -> {
            drawSnowflake(particle)
        }
        ParticleType.LEAF -> {
            drawLeaf(particle)
        }
        ParticleType.DIYA -> {
            drawDiya(particle)
        }
        ParticleType.FIREWORK -> {
            drawFirework(particle)
        }
    }
}

/**
 * Draw a star shape
 */
private fun DrawScope.drawStar(particle: FestiveParticle) {
    val points = 5
    val outerRadius = particle.size
    val innerRadius = particle.size * 0.4f
    val angle = Math.PI / points
    val center = Offset(particle.x, particle.y)
    
    // Simple star approximation with circles at points
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val currentAngle = angle * i + Math.toRadians(particle.rotation.toDouble())
        val x = center.x + (radius * cos(currentAngle)).toFloat()
        val y = center.y + (radius * sin(currentAngle)).toFloat()
        
        drawCircle(
            color = particle.color.copy(alpha = particle.alpha),
            radius = particle.size * 0.3f,
            center = Offset(x, y)
        )
    }
}

/**
 * Draw a heart shape (simplified)
 */
private fun DrawScope.drawHeart(particle: FestiveParticle) {
    val center = Offset(particle.x, particle.y)
    val size = particle.size
    
    // Left circle
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.6f,
        center = Offset(center.x - size * 0.3f, center.y - size * 0.2f)
    )
    
    // Right circle
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.6f,
        center = Offset(center.x + size * 0.3f, center.y - size * 0.2f)
    )
    
    // Bottom triangle approximation
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.8f,
        center = Offset(center.x, center.y + size * 0.3f)
    )
}

/**
 * Draw a sparkle/diamond shape
 */
private fun DrawScope.drawSparkle(particle: FestiveParticle) {
    val center = Offset(particle.x, particle.y)
    val size = particle.size
    
    // Draw 4 circles in diamond pattern
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.4f,
        center = Offset(center.x, center.y - size)
    )
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.4f,
        center = Offset(center.x + size, center.y)
    )
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.4f,
        center = Offset(center.x, center.y + size)
    )
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.4f,
        center = Offset(center.x - size, center.y)
    )
}

/**
 * Draw a snowflake
 */
private fun DrawScope.drawSnowflake(particle: FestiveParticle) {
    val center = Offset(particle.x, particle.y)
    val size = particle.size
    
    // Draw 6 arms of snowflake
    for (i in 0 until 6) {
        val angle = Math.toRadians((60.0 * i) + particle.rotation.toDouble())
        val endX = center.x + (size * cos(angle)).toFloat()
        val endY = center.y + (size * sin(angle)).toFloat()
        
        drawCircle(
            color = particle.color.copy(alpha = particle.alpha),
            radius = size * 0.3f,
            center = Offset(endX, endY)
        )
    }
    
    // Center
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size * 0.4f,
        center = center
    )
}

/**
 * Draw a leaf
 */
private fun DrawScope.drawLeaf(particle: FestiveParticle) {
    // Simplified as an oval
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = particle.size,
        center = Offset(particle.x, particle.y)
    )
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha * 0.7f),
        radius = particle.size * 0.6f,
        center = Offset(particle.x + particle.size * 0.3f, particle.y)
    )
}

/**
 * Draw a diya (lamp)
 */
private fun DrawScope.drawDiya(particle: FestiveParticle) {
    val center = Offset(particle.x, particle.y)
    val size = particle.size
    
    // Flame (orange/yellow)
    drawCircle(
        color = Color(0xFFFFD700).copy(alpha = particle.alpha),
        radius = size * 0.5f,
        center = Offset(center.x, center.y - size * 0.5f)
    )
    
    // Lamp base
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size,
        center = center
    )
}

/**
 * Draw a firework burst
 */
private fun DrawScope.drawFirework(particle: FestiveParticle) {
    val center = Offset(particle.x, particle.y)
    val size = particle.size
    
    // Draw multiple circles radiating outward
    for (i in 0 until 8) {
        val angle = Math.toRadians(45.0 * i)
        val distance = size * 1.5f
        val x = center.x + (distance * cos(angle)).toFloat()
        val y = center.y + (distance * sin(angle)).toFloat()
        
        drawCircle(
            color = particle.color.copy(alpha = particle.alpha),
            radius = size * 0.4f,
            center = Offset(x, y)
        )
    }
    
    // Center burst
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha * 1.2f),
        radius = size * 0.8f,
        center = center
    )
}
