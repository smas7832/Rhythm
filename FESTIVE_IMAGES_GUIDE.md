# Adding Custom Images to Festive Themes

## Overview
The festive theme system is built with image support in mind. Here's how to add custom images for decorations.

## Step 1: Add Images to Resources

### Prepare Your Images
1. Create festive images (PNG with transparency recommended):
   - Diwali: `diya_particle.png`, `rangoli_bg.png`
   - Christmas: `snowflake_particle.png`, `christmas_tree.png`
   - New Year: `firework_particle.png`, `confetti.png`
   - Halloween: `pumpkin_particle.png`, `ghost_particle.png`
   - etc.

2. Optimize images:
   - Keep file size small (< 50KB each)
   - Use appropriate dimensions (32x32 to 128x128 for particles)
   - Use transparent backgrounds

### Add to Project
```
app/src/main/res/drawable/
├── festive_diwali_diya.png
├── festive_christmas_snowflake.png
├── festive_newyear_firework.png
├── festive_halloween_pumpkin.png
├── festive_holi_color_splash.png
├── festive_valentines_heart.png
├── festive_easter_egg.png
└── etc...
```

## Step 2: Load Images in Composable

### Update FestiveDecorations.kt

Add image loading at the top of the `FestiveDecorations` composable:

```kotlin
@Composable
fun FestiveDecorations(
    config: FestiveThemeConfig,
    modifier: Modifier = Modifier
) {
    if (!config.enabled || config.selectedTheme == FestiveTheme.NONE || !config.showParticles) {
        return
    }

    // Load festival-specific images
    val particleImage = remember(config.selectedTheme) {
        when (config.selectedTheme) {
            FestiveTheme.DIWALI -> ImageBitmap.imageResource(R.drawable.festive_diwali_diya)
            FestiveTheme.CHRISTMAS -> ImageBitmap.imageResource(R.drawable.festive_christmas_snowflake)
            FestiveTheme.NEW_YEAR -> ImageBitmap.imageResource(R.drawable.festive_newyear_firework)
            FestiveTheme.HALLOWEEN -> ImageBitmap.imageResource(R.drawable.festive_halloween_pumpkin)
            FestiveTheme.HOLI -> ImageBitmap.imageResource(R.drawable.festive_holi_color_splash)
            FestiveTheme.VALENTINES -> ImageBitmap.imageResource(R.drawable.festive_valentines_heart)
            FestiveTheme.EASTER -> ImageBitmap.imageResource(R.drawable.festive_easter_egg)
            else -> null
        }
    }
    
    // Rest of the code...
}
```

Add the import:
```kotlin
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
```

## Step 3: Draw Images Instead of Shapes

### Update drawParticle function

Replace the shape drawing with image drawing:

```kotlin
/**
 * Draw particle on canvas
 */
private fun DrawScope.drawParticle(
    particle: FestiveParticle,
    particleImage: ImageBitmap? = null
) {
    if (particleImage != null) {
        // Draw image if available
        drawImage(
            image = particleImage,
            topLeft = Offset(
                particle.x - particle.size,
                particle.y - particle.size
            ),
            alpha = particle.alpha,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                particle.color.copy(alpha = particle.alpha)
            )
        )
    } else {
        // Fallback to shape drawing
        when (particle.type) {
            ParticleType.CIRCLE -> {
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = Offset(particle.x, particle.y)
                )
            }
            // ... rest of shape drawing code
        }
    }
}
```

### Update Canvas call

Pass the image to drawParticle:

```kotlin
Canvas(
    modifier = modifier.fillMaxSize()
) {
    screenWidth = size.width
    screenHeight = size.height
    
    particles.forEach { particle ->
        drawParticle(particle, particleImage) // Pass image here
    }
}
```

## Step 4: Add Background Images (Optional)

### For static background decorations:

```kotlin
@Composable
fun FestiveDecorations(
    config: FestiveThemeConfig,
    modifier: Modifier = Modifier
) {
    // ... existing code ...
    
    Box(modifier = modifier.fillMaxSize()) {
        // Background decoration
        val backgroundImage = remember(config.selectedTheme) {
            when (config.selectedTheme) {
                FestiveTheme.CHRISTMAS -> R.drawable.festive_christmas_background
                FestiveTheme.DIWALI -> R.drawable.festive_diwali_background
                else -> null
            }
        }
        
        if (backgroundImage != null) {
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.15f }, // Subtle overlay
                contentScale = ContentScale.Crop
            )
        }
        
        // Animated particles on top
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            particles.forEach { particle ->
                drawParticle(particle, particleImage)
            }
        }
    }
}
```

## Step 5: Advanced - Animated Sprites

For more complex animations (like animated diyas):

```kotlin
@Composable
fun AnimatedDiyaParticle(
    particle: FestiveParticle,
    modifier: Modifier = Modifier
) {
    // Load animation frames
    val frames = remember {
        listOf(
            R.drawable.diya_frame_1,
            R.drawable.diya_frame_2,
            R.drawable.diya_frame_3,
            R.drawable.diya_frame_4
        )
    }
    
    var currentFrame by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(100) // 10 FPS animation
            currentFrame = (currentFrame + 1) % frames.size
        }
    }
    
    Image(
        painter = painterResource(frames[currentFrame]),
        contentDescription = null,
        modifier = modifier
            .size(particle.size.dp * 2)
            .graphicsLayer {
                translationX = particle.x
                translationY = particle.y
                alpha = particle.alpha
                rotationZ = particle.rotation
            }
    )
}
```

## Example: Complete Diwali Theme with Images

```kotlin
// 1. Add resources:
//    - festive_diwali_diya.png (32x32)
//    - festive_diwali_rangoli_bg.png (full screen)
//    - festive_diwali_sparkle.png (16x16)

// 2. Update FestiveDecorations.kt:
@Composable
fun FestiveDecorations(
    config: FestiveThemeConfig,
    modifier: Modifier = Modifier
) {
    if (!config.enabled || config.selectedTheme == FestiveTheme.NONE) {
        return
    }

    val diyaImage = remember {
        if (config.selectedTheme == FestiveTheme.DIWALI) {
            ImageBitmap.imageResource(R.drawable.festive_diwali_diya)
        } else null
    }
    
    val sparkleImage = remember {
        if (config.selectedTheme == FestiveTheme.DIWALI) {
            ImageBitmap.imageResource(R.drawable.festive_diwali_sparkle)
        } else null
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background rangoli pattern
        if (config.selectedTheme == FestiveTheme.DIWALI) {
            Image(
                painter = painterResource(R.drawable.festive_diwali_rangoli_bg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.1f },
                contentScale = ContentScale.Crop
            )
        }
        
        // Animated particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                // Alternate between diya and sparkle images
                val image = if (particle.type == ParticleType.DIYA) {
                    diyaImage
                } else {
                    sparkleImage
                }
                
                if (image != null) {
                    drawImage(
                        image = image,
                        topLeft = Offset(
                            particle.x - particle.size,
                            particle.y - particle.size
                        ),
                        alpha = particle.alpha
                    )
                }
            }
        }
    }
}
```

## Testing

1. Run the app
2. Go to Settings > Themes > Festive tab
3. Enable festive themes
4. Select Diwali (or your test festival)
5. Enable particles
6. Watch your custom images animate!

## Tips

- **Keep images small**: Large images will impact performance
- **Use PNG with transparency**: Best for overlay effects
- **Test on different screen sizes**: Ensure images scale properly
- **Consider dark mode**: Images should work on both light and dark backgrounds
- **Color tinting**: The current system applies color tints to particles - you may want to disable this for colored images
- **Frame optimization**: For animated sprites, keep frame count low (4-8 frames max)

## Troubleshooting

**Images not showing?**
- Check resource names match exactly
- Ensure images are in `drawable` folder
- Verify images are not corrupted
- Check Android Studio's resource compilation

**Performance issues?**
- Reduce particle count (lower intensity)
- Use smaller image files
- Reduce animation frame rate
- Consider using vector drawables for simple shapes

**Images look wrong?**
- Adjust particle size multiplier
- Check alpha/transparency settings
- Verify color tinting is desired
- Test on different screen densities

## Next Steps

With images added, you can:
1. Create festival-specific background overlays
2. Add floating decorative elements (fixed position)
3. Create themed UI elements (buttons, cards)
4. Add sound effects when particles spawn
5. Create particle trails and effects

Enjoy creating beautiful festive experiences! 🎨✨
