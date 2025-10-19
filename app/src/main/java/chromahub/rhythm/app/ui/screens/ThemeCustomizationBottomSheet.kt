@file:OptIn(ExperimentalMaterial3Api::class)
package chromahub.rhythm.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import chromahub.rhythm.app.utils.FontLoader
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.ui.hapticfeedback.HapticFeedback
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.theme.parseCustomColorScheme
import chromahub.rhythm.app.ui.theme.getCustomFontPreviewStyle
import chromahub.rhythm.app.ui.theme.getFontPreviewStyle
import chromahub.rhythm.app.util.HapticUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ColorSchemeOption(
    val name: String,
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color
)

data class FontOption(
    val name: String,
    val displayName: String,
    val description: String
)

enum class ColorSource(val displayName: String, val description: String, val icon: ImageVector) {
    ALBUM_ART("Album Art", "Extract colors from currently playing album artwork", Icons.Filled.Album),
    MONET("System Colors", "Use Material You colors from your wallpaper", Icons.Filled.Wallpaper),
    CUSTOM("Custom Scheme", "Choose from predefined color schemes", Icons.Filled.Palette)
}

enum class FontSource(val displayName: String, val description: String, val icon: ImageVector) {
    SYSTEM("System Font", "Use the device's default font", Icons.Filled.PhoneAndroid),
    CUSTOM("Custom Font", "Import and use a custom font file", Icons.Filled.FontDownload)
}

// HSL Color conversion utilities
data class HSLColor(val hue: Float, val saturation: Float, val lightness: Float)

fun Color.toHSL(): HSLColor {
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val diff = max - min

    val lightness = (max + min) / 2f

    val saturation = if (diff == 0f) 0f else diff / (1f - kotlin.math.abs(2f * lightness - 1f))

    val hue = when (max) {
        min -> 0f
        r -> ((g - b) / diff) % 6
        g -> (b - r) / diff + 2
        b -> (r - g) / diff + 4
        else -> 0f
    } * 60f

    return HSLColor(
        hue = if (hue < 0) hue + 360f else hue,
        saturation = saturation,
        lightness = lightness
    )
}

fun HSLColor.toColor(): Color {
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}

@Composable
fun ThemeCustomizationBottomSheet(
    onDismiss: () -> Unit,
    appSettings: AppSettings
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Font file picker launcher
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy font to internal storage
            val fontPath = FontLoader.copyFontToInternalStorage(context, it)
            if (fontPath != null) {
                // Validate that the font can be loaded
                val testFont = FontLoader.loadCustomFont(context, fontPath)
                if (testFont != null) {
                    // Save to settings
                    appSettings.setCustomFontPath(fontPath)
                    appSettings.setFontSource("CUSTOM")
                    
                    // Extract and save font name
                    val fontName = FontLoader.getFontFileName(fontPath) ?: "Custom Font"
                    appSettings.setCustomFontFamily(fontName)
                    
                    // Show success feedback
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Font imported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Font file copied but can't be loaded
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.Reject)
                    Toast.makeText(context, "Invalid font file format", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Failed to copy font file
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.Reject)
                Toast.makeText(context, "Failed to import font file", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Theme states
    val currentColorScheme by appSettings.customColorScheme.collectAsState()
    val currentFont by appSettings.customFont.collectAsState()
    val useDynamicColors by appSettings.useDynamicColors.collectAsState()
    val useSystemTheme by appSettings.useSystemTheme.collectAsState()
    val darkMode by appSettings.darkMode.collectAsState()
    val colorSource by appSettings.colorSource.collectAsState()
    val extractedAlbumColors by appSettings.extractedAlbumColors.collectAsState()
    
    // Tab state - now 4 tabs: Overview, Colors, Fonts, Festive
    var selectedTab by remember { mutableStateOf(0) }
    
    // Festive theme states
    val festiveThemeEnabled by appSettings.festiveThemeEnabled.collectAsState()
    val festiveThemeSelected by appSettings.festiveThemeSelected.collectAsState()
    val festiveThemeAutoDetect by appSettings.festiveThemeAutoDetect.collectAsState()
    val festiveThemeShowParticles by appSettings.festiveThemeShowParticles.collectAsState()
    val festiveThemeShowDecorations by appSettings.festiveThemeShowDecorations.collectAsState()
    val festiveThemeParticleIntensity by appSettings.festiveThemeParticleIntensity.collectAsState()
    val festiveThemeApplyToSplash by appSettings.festiveThemeApplyToSplash.collectAsState()
    val festiveThemeApplyToMainUI by appSettings.festiveThemeApplyToMainUI.collectAsState()
    
    // Color source state - initialize based on saved setting
    var selectedColorSource by remember(colorSource) { 
        mutableStateOf(
            when (colorSource) {
                "ALBUM_ART" -> ColorSource.ALBUM_ART
                "MONET" -> ColorSource.MONET
                "CUSTOM" -> ColorSource.CUSTOM
                else -> ColorSource.CUSTOM
            }
        )
    }
    
    // Font states
    val fontSource by appSettings.fontSource.collectAsState()
    val customFontPath by appSettings.customFontPath.collectAsState()
    val customFontFamily by appSettings.customFontFamily.collectAsState()
    
    // Font source state - initialize based on saved setting
    var selectedFontSource by remember(fontSource) {
        mutableStateOf(
            when (fontSource) {
                "CUSTOM" -> FontSource.CUSTOM
                "SYSTEM" -> FontSource.SYSTEM
                else -> FontSource.SYSTEM
            }
        )
    }

    // Animation states
    var showContent by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentAlpha"
    )

    val contentTranslation by animateFloatAsState(
        targetValue = if (showContent) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentTranslation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    // Handle color source changes
    LaunchedEffect(selectedColorSource) {
        when (selectedColorSource) {
            ColorSource.MONET -> {
                appSettings.setUseDynamicColors(true)
                appSettings.setColorSource("MONET")
            }
            ColorSource.ALBUM_ART -> {
                appSettings.setUseDynamicColors(false)
                appSettings.setColorSource("ALBUM_ART")
                // Colors will be extracted automatically when songs play in MusicViewModel
                // Force immediate extraction if a song is currently playing
                // Note: The actual extraction happens in MusicViewModel when media transitions
            }
            ColorSource.CUSTOM -> {
                appSettings.setUseDynamicColors(false)
                appSettings.setColorSource("CUSTOM")
            }
        }
    }
    
    // Handle font source changes
    LaunchedEffect(selectedFontSource) {
        when (selectedFontSource) {
            FontSource.SYSTEM -> {
                appSettings.setFontSource("SYSTEM")
                // Only set to System if no custom font is configured
                // This prevents resetting to System font when reopening the sheet
                val fontPath = customFontPath // Store in local variable to avoid smart cast issue
                if (fontPath == null || fontPath.isEmpty()) {
                    appSettings.setCustomFont("System")
                }
            }
            FontSource.CUSTOM -> {
                appSettings.setFontSource("CUSTOM")
                // Custom font path should already be set via import
                // The font will be loaded from customFontPath when applied
            }
        }
    }
    
    // Color scheme options
    val colorSchemeOptions = remember {
        listOf(
            ColorSchemeOption(
                name = "Default",
                displayName = "Default Purple",
                description = "The classic Rhythm experience with vibrant purple tones",
                primaryColor = Color(0xFF5C4AD5),
                secondaryColor = Color(0xFF5D5D6B),
                tertiaryColor = Color(0xFFFFDDB6)
            ),
            ColorSchemeOption(
                name = "Warm",
                displayName = "Warm Sunset",
                description = "Cozy orange and red tones for a warm atmosphere",
                primaryColor = Color(0xFFFF6B35),
                secondaryColor = Color(0xFFF7931E),
                tertiaryColor = Color(0xFFFFC857)
            ),
            ColorSchemeOption(
                name = "Cool",
                displayName = "Cool Ocean",
                description = "Refreshing blue and teal tones for a calming vibe",
                primaryColor = Color(0xFF1E88E5),
                secondaryColor = Color(0xFF00897B),
                tertiaryColor = Color(0xFF80DEEA)
            ),
            ColorSchemeOption(
                name = "Forest",
                displayName = "Forest Green",
                description = "Natural green tones inspired by nature",
                primaryColor = Color(0xFF2E7D32),
                secondaryColor = Color(0xFF558B2F),
                tertiaryColor = Color(0xFF9CCC65)
            ),
            ColorSchemeOption(
                name = "Rose",
                displayName = "Rose Pink",
                description = "Elegant pink and magenta tones",
                primaryColor = Color(0xFFE91E63),
                secondaryColor = Color(0xFFC2185B),
                tertiaryColor = Color(0xFFF8BBD0)
            ),
            ColorSchemeOption(
                name = "Monochrome",
                displayName = "Monochrome",
                description = "Minimalist grayscale for a clean, modern look",
                primaryColor = Color(0xFF424242),
                secondaryColor = Color(0xFF616161),
                tertiaryColor = Color(0xFF9E9E9E)
            ),
            ColorSchemeOption(
                name = "Lavender",
                displayName = "Lavender",
                description = "Calming purple and lavender tones for relaxation",
                primaryColor = Color(0xFF7C4DFF),
                secondaryColor = Color(0xFF9575CD),
                tertiaryColor = Color(0xFFBA68C8)
            ),
            ColorSchemeOption(
                name = "Ocean",
                displayName = "Deep Ocean",
                description = "Deep blues and aquamarines for oceanic serenity",
                primaryColor = Color(0xFF006064),
                secondaryColor = Color(0xFF00838F),
                tertiaryColor = Color(0xFF00ACC1)
            ),
            ColorSchemeOption(
                name = "Aurora",
                displayName = "Northern Lights",
                description = "Vibrant greens and blues inspired by the aurora borealis",
                primaryColor = Color(0xFF00C853),
                secondaryColor = Color(0xFF00E676),
                tertiaryColor = Color(0xFF69F0AE)
            ),
            ColorSchemeOption(
                name = "Amber",
                displayName = "Golden Amber",
                description = "Rich amber and gold tones for a luxurious feel",
                primaryColor = Color(0xFFFF6F00),
                secondaryColor = Color(0xFFFF8F00),
                tertiaryColor = Color(0xFFFFC107)
            ),
            ColorSchemeOption(
                name = "Crimson",
                displayName = "Deep Crimson",
                description = "Bold burgundy and crimson shades for drama",
                primaryColor = Color(0xFFB71C1C),
                secondaryColor = Color(0xFFC62828),
                tertiaryColor = Color(0xFFD32F2F)
            ),
            ColorSchemeOption(
                name = "Emerald",
                displayName = "Emerald Dream",
                description = "Fresh emerald greens with natural forest hues",
                primaryColor = Color(0xFF2E7D32),
                secondaryColor = Color(0xFF388E3C),
                tertiaryColor = Color(0xFF4CAF50)
            ),
            ColorSchemeOption(
                name = "Mint",
                displayName = "Mint",
                description = "Fresh and clean cyan and mint green tones",
                primaryColor = Color(0xFF0097A7),
                secondaryColor = Color(0xFF00ACC1),
                tertiaryColor = Color(0xFF00BCD4)
            )
        )
    }
    
    // Font options
    val fontOptions = remember {
        listOf(
            FontOption(
                name = "System",
                displayName = "System Default",
                description = "Use your device's default font"
            ),
            FontOption(
                name = "Slate",
                displayName = "Slate",
                description = "Elegant serif font with a classic, traditional appearance"
            ),
            FontOption(
                name = "Inter",
                displayName = "Inter",
                description = "Clean and modern sans-serif font, highly readable"
            ),
            FontOption(
                name = "JetBrains",
                displayName = "JetBrains Mono",
                description = "Monospace font perfect for technical content"
            ),
            FontOption(
                name = "Quicksand",
                displayName = "Quicksand",
                description = "Rounded font with a softer, friendlier appearance"
            ),
            // FontOption(
            //     name = "Classic",
            //     displayName = "Classic Serif",
            //     description = "Traditional serif typeface, perfect for formal content"
            // ),
            // FontOption(
            //     name = "Casual",
            //     displayName = "Casual Sans",
            //     description = "Relaxed sans-serif font for a friendly, approachable feel"
            // ),
            // FontOption(
            //     name = "Modern",
            //     displayName = "Modern",
            //     description = "Contemporary typeface with clean, geometric shapes"
            // ),
            // FontOption(
            //     name = "Typewriter",
            //     displayName = "Typewriter",
            //     description = "Classic typewriter-inspired monospace font"
            // ),
            // FontOption(
            //     name = "Playful",
            //     displayName = "Playful",
            //     description = "Fun and creative typeface with rounded characters"
            // )
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
            onDismiss()
        },
        sheetState = bottomSheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header and Tabs
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
//                        Surface(
//                            shape = CircleShape,
//                            color = MaterialTheme.colorScheme.primaryContainer,
//                            modifier = Modifier.size(48.dp)
//                        ) {
//                            Box(
//                                contentAlignment = Alignment.Center,
//                                modifier = Modifier.fillMaxSize()
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Filled.Palette,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }

//                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Themes",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Personalize your experience",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tabs - Modern pill-style tab row
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TabButton(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                },
                                icon = Icons.Filled.Settings,
                                text = "Overview",
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                },
                                icon = Icons.Filled.Palette,
                                text = "Colors",
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                selected = selectedTab == 2,
                                onClick = {
                                    selectedTab = 2
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                },
                                icon = Icons.Filled.TextFields,
                                text = "Fonts",
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                selected = selectedTab == 3,
                                onClick = {
                                    selectedTab = 3
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                },
                                icon = Icons.Filled.Celebration,
                                text = "Festive",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content with animated transitions
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentTranslation
                }
            ) {
                when (selectedTab) {
                    0 -> OverviewContent(
                        useDynamicColors = useDynamicColors,
                        useSystemTheme = useSystemTheme,
                        darkMode = darkMode,
                        selectedColorSource = selectedColorSource,
                        selectedFontSource = selectedFontSource,
                        onDynamicColorsChange = { appSettings.setUseDynamicColors(it) },
                        onSystemThemeChange = { appSettings.setUseSystemTheme(it) },
                        onDarkModeChange = { appSettings.setDarkMode(it) },
                        onColorSourceChange = { source -> selectedColorSource = source },
                        onFontSourceChange = { source -> 
                            selectedFontSource = source
                            appSettings.setFontSource(source.name)
                            // Clear system font selection when switching to CUSTOM
                            if (source == FontSource.CUSTOM && customFontPath != null) {
                                // Custom font is active, don't change system font setting
                            } else if (source == FontSource.SYSTEM) {
                                // Reset to system font if no custom font was previously selected
                                if (customFontPath == null) {
                                    appSettings.setCustomFont("System")
                                }
                            }
                        },
                        context = context,
                        haptics = haptics
                    )
                    1 -> ColorSchemeContent(
                        colorSchemeOptions = colorSchemeOptions,
                        currentScheme = currentColorScheme,
                        selectedColorSource = selectedColorSource,
                        onSchemeSelected = { scheme ->
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            appSettings.setCustomColorScheme(scheme)
                        },
                        appSettings = appSettings
                    )
                    2 -> FontContent(
                        fontOptions = fontOptions,
                        currentFont = currentFont,
                        selectedFontSource = selectedFontSource,
                        customFontPath = customFontPath,
                        customFontFamily = customFontFamily,
                        onFontSelected = { font ->
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            appSettings.setCustomFont(font)
                            // When selecting a system font, switch to SYSTEM source
                            if (selectedFontSource != FontSource.SYSTEM) {
                                appSettings.setFontSource("SYSTEM")
                            }
                        },
                        onImportFont = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            // Launch font file picker for TTF/OTF files
                            fontPickerLauncher.launch("font/*")
                        },
                        context = context,
                        haptics = haptics
                    )
                    3 -> FestiveContent(
                        festiveThemeEnabled = festiveThemeEnabled,
                        festiveThemeSelected = festiveThemeSelected,
                        festiveThemeAutoDetect = festiveThemeAutoDetect,
                        festiveThemeShowParticles = festiveThemeShowParticles,
                        festiveThemeShowDecorations = festiveThemeShowDecorations,
                        festiveThemeParticleIntensity = festiveThemeParticleIntensity,
                        festiveThemeApplyToSplash = festiveThemeApplyToSplash,
                        festiveThemeApplyToMainUI = festiveThemeApplyToMainUI,
                        onFestiveThemeEnabledChange = { appSettings.setFestiveThemeEnabled(it) },
                        onFestiveThemeSelected = { appSettings.setFestiveThemeSelected(it) },
                        onFestiveThemeAutoDetectChange = { appSettings.setFestiveThemeAutoDetect(it) },
                        onFestiveThemeShowParticlesChange = { appSettings.setFestiveThemeShowParticles(it) },
                        onFestiveThemeShowDecorationsChange = { appSettings.setFestiveThemeShowDecorations(it) },
                        onFestiveThemeParticleIntensityChange = { appSettings.setFestiveThemeParticleIntensity(it) },
                        onFestiveThemeApplyToSplashChange = { appSettings.setFestiveThemeApplyToSplash(it) },
                        onFestiveThemeApplyToMainUIChange = { appSettings.setFestiveThemeApplyToMainUI(it) },
                        context = context,
                        haptics = haptics
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSchemeContent(
    colorSchemeOptions: List<ColorSchemeOption>,
    currentScheme: String,
    selectedColorSource: ColorSource,
    onSchemeSelected: (String) -> Unit,
    appSettings: AppSettings
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var isColorPickerExpanded by remember { mutableStateOf(false) }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Show message if not using custom color source
        if (selectedColorSource != ColorSource.CUSTOM) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (selectedColorSource == ColorSource.ALBUM_ART) {
                                "Colors are extracted from album artwork. Custom schemes are disabled."
                            } else {
                                "Using Material You system colors. Custom schemes are disabled."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        
        // Expandable color picker option
        if (selectedColorSource == ColorSource.CUSTOM) {
            item {
                ExpandableColorPickerCard(
                    isExpanded = isColorPickerExpanded,
                    onExpandToggle = { 
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        isColorPickerExpanded = !isColorPickerExpanded
                    },
                    currentScheme = currentScheme,
                    onApply = { primary, secondary, tertiary ->
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        val primaryHex = String.format("%06X", (primary.toArgb() and 0xFFFFFF))
                        val secondaryHex = String.format("%06X", (secondary.toArgb() and 0xFFFFFF))
                        val tertiaryHex = String.format("%06X", (tertiary.toArgb() and 0xFFFFFF))
                        val customScheme = "custom_${primaryHex}_${secondaryHex}_${tertiaryHex}"
                        appSettings.setCustomColorScheme(customScheme)
                        onSchemeSelected(customScheme)
                        Toast.makeText(context, "Custom colors applied!", Toast.LENGTH_SHORT).show()
                        isColorPickerExpanded = false
                    },
                    context = context,
                    haptics = haptics
                )
            }
            
            // Section Header: Featured Schemes
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FEATURED SCHEMES",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        
        // Featured schemes (Default Purple to Monochrome)
        val featuredSchemes = colorSchemeOptions.filter { 
            it.name in listOf("Default", "Warm", "Cool", "Forest", "Rose", "Monochrome")
        }
        
        items(featuredSchemes) { option ->
            ColorSchemeCard(
                option = option,
                isSelected = currentScheme == option.name,
                isEnabled = selectedColorSource == ColorSource.CUSTOM,
                onSelect = { onSchemeSelected(option.name) }
            )
        }
        
        // Section Header: More Schemes
        if (selectedColorSource == ColorSource.CUSTOM) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MORE SCHEMES",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        
        // Other schemes
        val otherSchemes = colorSchemeOptions.filter { 
            it.name !in listOf("Default", "Warm", "Cool", "Forest", "Rose", "Monochrome")
        }
        
        items(otherSchemes) { option ->
            ColorSchemeCard(
                option = option,
                isSelected = currentScheme == option.name,
                isEnabled = selectedColorSource == ColorSource.CUSTOM,
                onSelect = { onSchemeSelected(option.name) }
            )
        }
        
        // Bottom padding for better scrolling experience
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorSchemeCard(
    option: ColorSchemeOption,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onSelect: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected && isEnabled) 
            MaterialTheme.colorScheme.primaryContainer 
        else if (isEnabled)
            MaterialTheme.colorScheme.surfaceContainerHigh
        else
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "container_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected && isEnabled) 
            MaterialTheme.colorScheme.primary 
        else 
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "border_color"
    )

    Card(
        onClick = { if (isEnabled) onSelect() },
        enabled = isEnabled,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview circles
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = option.primaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = option.secondaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = option.tertiaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {}
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected && isEnabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else if (isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected && isEnabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else if (isEnabled)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            
            if (isSelected && isEnabled) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun FontContent(
    fontOptions: List<FontOption>,
    currentFont: String,
    selectedFontSource: FontSource,
    customFontPath: String?,
    customFontFamily: String?,
    onFontSelected: (String) -> Unit,
    onImportFont: () -> Unit,
    context: Context,
    haptics: HapticFeedback
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Custom Font Import Section (only show when CUSTOM is selected)
        if (selectedFontSource == FontSource.CUSTOM) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FontDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (customFontPath != null) "Custom Font Active" else "Import Custom Font",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    if (customFontPath != null) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                if (customFontPath != null && customFontFamily != null) {
                                    Text(
                                        text = customFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        FilledTonalButton(
                            onClick = onImportFont,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (customFontPath != null) "Change Font File" else "Import TTF/OTF Font",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show preview if custom font is loaded
                        if (customFontPath != null) {
                            val customFont = remember(customFontPath) {
                                FontLoader.loadCustomFont(context, customFontPath)
                            }
                            
                            if (customFont != null) {
                                Text(
                                    text = "The quick brown fox jumps over the lazy dog",
                                    style = getCustomFontPreviewStyle(customFont),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        Text(
                            text = "Import a .ttf or .otf font file to use throughout the app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Info text when custom font is active
        if (selectedFontSource == FontSource.CUSTOM && customFontPath != null) {
            item {
                Text(
                    text = "System fonts are disabled while custom font is active. Switch to System font source in the Overview tab to use these fonts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
        
        items(fontOptions) { option ->
            FontCard(
                option = option,
                isSelected = currentFont == option.name && selectedFontSource == FontSource.SYSTEM,
                isEnabled = selectedFontSource == FontSource.SYSTEM,
                onSelect = { 
                    if (selectedFontSource == FontSource.SYSTEM) {
                        onFontSelected(option.name)
                    }
                }
            )
        }
        
        // Bottom padding for better scrolling experience
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FontSourceCard(
    source: FontSource,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "font_source_container"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "font_source_border"
    )

    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = source.icon,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = source.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            // Always reserve space for checkmark to maintain consistent card height
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FontCard(
    option: FontOption,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onSelect: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "container_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "border_color"
    )

    Card(
        onClick = { if (isEnabled) onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (isEnabled) 1f else 0.5f
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Font preview text
            Surface(
                color = if (isSelected)
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "The quick brown fox jumps over the lazy dog",
                    style = getFontPreviewStyle(option.name),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.primaryContainer
        else 
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabBackground"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabContent"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier.height(48.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (selected) 12.dp else 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            // Only show text for selected tab to save space
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(
                    animationSpec = tween(200)
                )
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewContent(
    useDynamicColors: Boolean,
    useSystemTheme: Boolean,
    darkMode: Boolean,
    selectedColorSource: ColorSource,
    selectedFontSource: FontSource,
    onDynamicColorsChange: (Boolean) -> Unit,
    onSystemThemeChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onColorSourceChange: (ColorSource) -> Unit,
    onFontSourceChange: (FontSource) -> Unit,
    context: Context,
    haptics: HapticFeedback
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Color Source Selection
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COLOR SOURCE",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSource.entries.forEach { source ->
                    ColorSourceCard(
                        colorSource = source,
                        isSelected = selectedColorSource == source,
                        onSelect = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onColorSourceChange(source)
                        }
                    )
                }
            }
        }
        
        // Spacer between sections
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Font Source Selection
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FONT SOURCE",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FontSource.entries.forEach { source ->
                    FontSourceCard(
                        source = source,
                        isSelected = selectedFontSource == source,
                        onSelect = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onFontSourceChange(source)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Spacer between sections
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Theme Mode Settings
        item {
            // Section header with UP NEXT style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THEME MODE",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // System Theme Toggle
                ThemeSettingCard(
                    icon = Icons.Outlined.Brightness4,
                    title = "Follow System Theme",
                    description = "Automatically switch between light and dark mode based on system settings",
                    checked = useSystemTheme,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                        onSystemThemeChange(it)
                    }
                )
                
                // Dark Mode Toggle (only shown if not using system theme)
                AnimatedVisibility(
                    visible = !useSystemTheme,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ThemeSettingCard(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        description = "Use dark theme for better viewing in low light",
                        checked = darkMode,
                        onCheckedChange = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onDarkModeChange(it)
                        }
                    )
                }
            }
        }
        
        // Tips Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Good to Know",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    ThemeTipItem(
                        icon = Icons.Filled.Palette,
                        text = "Album Art colors adapt dynamically to your music"
                    )
                    ThemeTipItem(
                        icon = Icons.Filled.Wallpaper,
                        text = "Material You uses system wallpaper colors (Android 12+)"
                    )
                    ThemeTipItem(
                        icon = Icons.Filled.FontDownload,
                        text = "Import custom fonts to personalize typography"
                    )
                }
            }
        }
        
        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ColorSourceCard(
    colorSource: ColorSource,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = colorSource.icon,
                        contentDescription = null,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = colorSource.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = colorSource.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ThemeTipItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun ExpandableColorPickerCard(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    currentScheme: String,
    onApply: (Color, Color, Color) -> Unit,
    context: Context,
    haptics: HapticFeedback
) {
    // Parse current custom colors from the scheme name, or use defaults
    val customScheme = parseCustomColorScheme(currentScheme, false)

    var primaryColor by remember(currentScheme) {
        if (customScheme != null) {
            mutableStateOf(customScheme.primary)
        } else {
            mutableStateOf(Color(0xFF5C4AD5)) // Default purple
        }
    }
    var secondaryColor by remember(currentScheme) {
        if (customScheme != null) {
            mutableStateOf(customScheme.secondary)
        } else {
            mutableStateOf(Color(0xFF5D5D6B))
        }
    }
    var tertiaryColor by remember(currentScheme) {
        if (customScheme != null) {
            mutableStateOf(customScheme.tertiary)
        } else {
            mutableStateOf(Color(0xFFFFDDB6))
        }
    }

    var selectedColorType by remember { mutableStateOf(ColorType.PRIMARY) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle)
            ) {
                Icon(
                    imageVector = Icons.Filled.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Color Picker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (isExpanded) "Customize your theme colors" else "Create your own custom color palette",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = rotationAngle }
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    // Color preview row with selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ColorPreviewItem(
                            label = "Primary",
                            color = primaryColor,
                            isSelected = selectedColorType == ColorType.PRIMARY,
                            onClick = { 
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                selectedColorType = ColorType.PRIMARY 
                            }
                        )
                        ColorPreviewItem(
                            label = "Secondary",
                            color = secondaryColor,
                            isSelected = selectedColorType == ColorType.SECONDARY,
                            onClick = { 
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                selectedColorType = ColorType.SECONDARY 
                            }
                        )
                        ColorPreviewItem(
                            label = "Tertiary",
                            color = tertiaryColor,
                            isSelected = selectedColorType == ColorType.TERTIARY,
                            onClick = { 
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                selectedColorType = ColorType.TERTIARY 
                            }
                        )
                    }

                    // Color picker controls
                    when (selectedColorType) {
                        ColorType.PRIMARY -> ColorPickerControls(
                            color = primaryColor,
                            onColorChange = { primaryColor = it }
                        )
                        ColorType.SECONDARY -> ColorPickerControls(
                            color = secondaryColor,
                            onColorChange = { secondaryColor = it }
                        )
                        ColorType.TERTIARY -> ColorPickerControls(
                            color = tertiaryColor,
                            onColorChange = { tertiaryColor = it }
                        )
                    }

                    // Preset colors
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val presetColors = listOf(
                        Color(0xFF5C4AD5), Color(0xFFFF6B35), Color(0xFF1E88E5),
                        Color(0xFF2E7D32), Color(0xFFE91E63), Color(0xFF424242),
                        Color(0xFF7C4DFF), Color(0xFF006064), Color(0xFF00C853),
                        Color(0xFFFF6F00), Color(0xFFB71C1C), Color(0xFF0097A7)
                    )

                    // Preset color grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetColors.chunked(6).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowColors.forEach { presetColor ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = presetColor,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .weight(1f)
                                            .clickable {
                                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                when (selectedColorType) {
                                                    ColorType.PRIMARY -> primaryColor = presetColor
                                                    ColorType.SECONDARY -> secondaryColor = presetColor
                                                    ColorType.TERTIARY -> tertiaryColor = presetColor
                                                }
                                            }
                                    ) {}
                                }
                                // Fill remaining space if row is not full
                                repeat(6 - rowColors.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Apply button
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            onApply(primaryColor, secondaryColor, tertiaryColor)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apply Colors",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomColorPickerDialog(
    onDismiss: () -> Unit,
    onApply: (Color, Color, Color) -> Unit,
    currentScheme: String,
    context: Context,
    haptics: HapticFeedback
) {
    // Parse current custom colors from the scheme name, or use defaults
    val customScheme = parseCustomColorScheme(currentScheme, false)

    var primaryColor by remember(currentScheme) {
        if (customScheme != null) {
            mutableStateOf(customScheme.primary)
        } else {
            mutableStateOf(Color(0xFF5C4AD5)) // Default purple
        }
    }
    var secondaryColor by remember(currentScheme) {
        if (customScheme != null) {
            mutableStateOf(customScheme.secondary)
        } else {
            mutableStateOf(Color(0xFF5D5D6B))
        }
    }
    var tertiaryColor by remember(currentScheme) {
        if (customScheme != null) {
            mutableStateOf(customScheme.tertiary)
        } else {
            mutableStateOf(Color(0xFFFFDDB6))
        }
    }

    var selectedColorType by remember { mutableStateOf(ColorType.PRIMARY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.ColorLens,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Custom Color Picker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Color preview row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorPreviewItem(
                        label = "Primary",
                        color = primaryColor,
                        isSelected = selectedColorType == ColorType.PRIMARY,
                        onClick = { selectedColorType = ColorType.PRIMARY }
                    )
                    ColorPreviewItem(
                        label = "Secondary",
                        color = secondaryColor,
                        isSelected = selectedColorType == ColorType.SECONDARY,
                        onClick = { selectedColorType = ColorType.SECONDARY }
                    )
                    ColorPreviewItem(
                        label = "Tertiary",
                        color = tertiaryColor,
                        isSelected = selectedColorType == ColorType.TERTIARY,
                        onClick = { selectedColorType = ColorType.TERTIARY }
                    )
                }

                // Color picker controls
                when (selectedColorType) {
                    ColorType.PRIMARY -> ColorPickerControls(
                        color = primaryColor,
                        onColorChange = { primaryColor = it }
                    )
                    ColorType.SECONDARY -> ColorPickerControls(
                        color = secondaryColor,
                        onColorChange = { secondaryColor = it }
                    )
                    ColorType.TERTIARY -> ColorPickerControls(
                        color = tertiaryColor,
                        onColorChange = { tertiaryColor = it }
                    )
                }

                // Preset colors
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                val presetColors = listOf(
                    Color(0xFF5C4AD5), Color(0xFFFF6B35), Color(0xFF1E88E5),
                    Color(0xFF2E7D32), Color(0xFFE91E63), Color(0xFF424242),
                    Color(0xFF7C4DFF), Color(0xFF006064), Color(0xFF00C853),
                    Color(0xFFFF6F00), Color(0xFFB71C1C), Color(0xFF0097A7)
                )

                // Use regular rows instead of LazyVerticalGrid to avoid nesting scrollable components
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Split presets into rows of 6
                    presetColors.chunked(6).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowColors.forEach { presetColor ->
                                Surface(
                                    shape = CircleShape,
                                    color = presetColor,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .weight(1f)
                                        .clickable {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                            when (selectedColorType) {
                                                ColorType.PRIMARY -> primaryColor = presetColor
                                                ColorType.SECONDARY -> secondaryColor = presetColor
                                                ColorType.TERTIARY -> tertiaryColor = presetColor
                                            }
                                        }
                                ) {}
                            }
                            // Fill remaining space if row is not full
                            repeat(6 - rowColors.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    onApply(primaryColor, secondaryColor, tertiaryColor)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Colors")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    onDismiss()
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp
    )
}

@Composable
private fun ColorPreviewItem(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color,
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else {
                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            },
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPickerControls(
    color: Color,
    onColorChange: (Color) -> Unit
) {
    val hsl = remember(color) { color.toHSL() }

    var hue by remember(color) { mutableStateOf(hsl.hue) }
    var saturation by remember(color) { mutableStateOf(hsl.saturation) }
    var lightness by remember(color) { mutableStateOf(hsl.lightness) }

    var showAdvanced by remember { mutableStateOf(false) }

    // Update color when HSL values change
    LaunchedEffect(hue, saturation, lightness) {
        val newColor = HSLColor(hue, saturation, lightness).toColor()
        onColorChange(newColor)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Current color display with hex code - enhanced design
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("#%06X", (color.toArgb() and 0xFFFFFF)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.9f) else Color.White,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Hue Wheel/Palette
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hue",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "${hue.toInt()}°",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hue slider with color gradient - enhanced with rounded corners
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = (0..360 step 20).map { h ->
                                HSLColor(h.toFloat(), 1f, 0.5f).toColor()
                            }
                        )
                    )
            ) {
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saturation Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saturation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "${(saturation * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.LightGray,
                                HSLColor(hue, 1f, lightness).toColor()
                            )
                        )
                    )
            ) {
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lightness/Value Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lightness",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "${(lightness * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                HSLColor(hue, saturation, 0.5f).toColor(),
                                Color.White
                            )
                        )
                    )
            ) {
                Slider(
                    value = lightness,
                    onValueChange = { lightness = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Advanced RGB controls toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Advanced RGB Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = showAdvanced,
                onCheckedChange = { showAdvanced = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        // Advanced RGB controls
        AnimatedVisibility(
            visible = showAdvanced,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                val red = (color.red * 255).toInt()
                val green = (color.green * 255).toInt()
                val blue = (color.blue * 255).toInt()

                var redValue by remember(color) { mutableStateOf(red.toFloat()) }
                var greenValue by remember(color) { mutableStateOf(green.toFloat()) }
                var blueValue by remember(color) { mutableStateOf(blue.toFloat()) }

                // Update HSL when RGB changes
                LaunchedEffect(redValue, greenValue, blueValue) {
                    val rgbColor = Color(
                        red = redValue / 255f,
                        green = greenValue / 255f,
                        blue = blueValue / 255f
                    )
                    val newHsl = rgbColor.toHSL()
                    hue = newHsl.hue
                    saturation = newHsl.saturation
                    lightness = newHsl.lightness
                }

                ColorSlider(
                    label = "Red",
                    value = redValue,
                    onValueChange = { redValue = it },
                    color = Color.Red,
                    valueRange = 0f..255f
                )

                Spacer(modifier = Modifier.height(16.dp))

                ColorSlider(
                    label = "Green",
                    value = greenValue,
                    onValueChange = { greenValue = it },
                    color = Color.Green,
                    valueRange = 0f..255f
                )

                Spacer(modifier = Modifier.height(16.dp))

                ColorSlider(
                    label = "Blue",
                    value = blueValue,
                    onValueChange = { blueValue = it },
                    color = Color.Blue,
                    valueRange = 0f..255f
                )
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = value.toInt().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.3f),
                activeTickColor = color,
                inactiveTickColor = color.copy(alpha = 0.3f)
            )
        )
    }
}

private enum class ColorType {
    PRIMARY, SECONDARY, TERTIARY
}

@Composable
private fun FestiveContent(
    festiveThemeEnabled: Boolean,
    festiveThemeSelected: String,
    festiveThemeAutoDetect: Boolean,
    festiveThemeShowParticles: Boolean,
    festiveThemeShowDecorations: Boolean,
    festiveThemeParticleIntensity: Float,
    festiveThemeApplyToSplash: Boolean,
    festiveThemeApplyToMainUI: Boolean,
    onFestiveThemeEnabledChange: (Boolean) -> Unit,
    onFestiveThemeSelected: (String) -> Unit,
    onFestiveThemeAutoDetectChange: (Boolean) -> Unit,
    onFestiveThemeShowParticlesChange: (Boolean) -> Unit,
    onFestiveThemeShowDecorationsChange: (Boolean) -> Unit,
    onFestiveThemeParticleIntensityChange: (Float) -> Unit,
    onFestiveThemeApplyToSplashChange: (Boolean) -> Unit,
    onFestiveThemeApplyToMainUIChange: (Boolean) -> Unit,
    context: Context,
    haptics: HapticFeedback
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Active festival indicator (if enabled)
        if (festiveThemeEnabled) {
            item {
                val activeFestive = remember(festiveThemeEnabled, festiveThemeAutoDetect, festiveThemeSelected) {
                    if (festiveThemeAutoDetect) {
                        chromahub.rhythm.app.ui.theme.FestiveTheme.detectCurrentFestival()
                    } else {
                        try {
                            chromahub.rhythm.app.ui.theme.FestiveTheme.valueOf(festiveThemeSelected)
                        } catch (e: Exception) {
                            chromahub.rhythm.app.ui.theme.FestiveTheme.NONE
                        }
                    }
                }
                
                if (activeFestive != chromahub.rhythm.app.ui.theme.FestiveTheme.NONE) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeFestive.emoji,
                                style = MaterialTheme.typography.displaySmall,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Active Festival",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(8.dp)
                                    ) {
                                        // Pulsing indicator
                                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                        val scale by infiniteTransition.animateFloat(
                                            initialValue = 0.8f,
                                            targetValue = 1.2f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1000),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "pulse"
                                        )
                                        Box(modifier = Modifier.fillMaxSize().scale(scale))
                                    }
                                }
                                Text(
                                    text = activeFestive.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        // Enable Festive Themes Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (festiveThemeEnabled) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (festiveThemeEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Celebration,
                                contentDescription = null,
                                tint = if (festiveThemeEnabled)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Festive Themes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (festiveThemeEnabled)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Celebrate special occasions with themed decorations",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (festiveThemeEnabled)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = festiveThemeEnabled,
                        onCheckedChange = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            onFestiveThemeEnabledChange(it)
                        }
                    )
                }
            }
        }
        
        // Show content only if enabled
        if (festiveThemeEnabled) {
            // Auto-detect toggle
            item {
                ThemeSettingCard(
                    icon = Icons.Filled.AutoAwesome,
                    title = "Auto-detect Festival",
                    description = "Automatically apply themes based on current date",
                    checked = festiveThemeAutoDetect,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onFestiveThemeAutoDetectChange(it)
                    }
                )
            }
            
            // Festive Theme Selection
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT FESTIVAL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            
            // Festival theme cards
            items(chromahub.rhythm.app.ui.theme.FestiveTheme.entries.filter { it != chromahub.rhythm.app.ui.theme.FestiveTheme.NONE }) { theme ->
                FestiveThemeCard(
                    theme = theme,
                    isSelected = festiveThemeSelected == theme.name,
                    isEnabled = !festiveThemeAutoDetect,
                    onSelect = {
                        if (!festiveThemeAutoDetect) {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onFestiveThemeSelected(theme.name)
                        }
                    }
                )
            }
            
            // Decoration Settings Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECORATION SETTINGS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            
            // Show Particles Toggle
            item {
                ThemeSettingCard(
                    icon = Icons.Filled.Stars,
                    title = "Show Particles",
                    description = "Display animated decorative particles",
                    checked = festiveThemeShowParticles,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onFestiveThemeShowParticlesChange(it)
                    }
                )
            }
            
            // Show Decorations Toggle
            item {
                ThemeSettingCard(
                    icon = Icons.Filled.Celebration,
                    title = "Show Decorations",
                    description = "Display festival-specific decorative elements and greetings",
                    checked = festiveThemeShowDecorations,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onFestiveThemeShowDecorationsChange(it)
                    }
                )
            }
            
            // Particle Intensity Slider
            if (festiveThemeShowParticles) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Particle Intensity",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(festiveThemeParticleIntensity * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Slider(
                                value = festiveThemeParticleIntensity,
                                onValueChange = {
                                    onFestiveThemeParticleIntensityChange(it)
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            )
                        }
                    }
                }
            }
            
            // Apply to Splash Screen
            item {
                ThemeSettingCard(
                    icon = Icons.Filled.FlashOn,
                    title = "Apply to Splash Screen",
                    description = "Show festive decorations on app launch",
                    checked = festiveThemeApplyToSplash,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onFestiveThemeApplyToSplashChange(it)
                    }
                )
            }
            
            // Apply to Main UI
            item {
                ThemeSettingCard(
                    icon = Icons.Filled.Dashboard,
                    title = "Apply to Main UI",
                    description = "Show festive decorations throughout the app",
                    checked = festiveThemeApplyToMainUI,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onFestiveThemeApplyToMainUIChange(it)
                    }
                )
            }
        }
        
        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FestiveThemeCard(
    theme: chromahub.rhythm.app.ui.theme.FestiveTheme,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "container_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "border_color"
    )

    Card(
        onClick = { if (isEnabled) onSelect() },
        enabled = isEnabled,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (isEnabled) 1f else 0.5f
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji and color preview
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                // Large emoji
                Text(
                    text = theme.emoji,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                // Color circles
                Surface(
                    shape = CircleShape,
                    color = theme.primaryColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = theme.secondaryColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = theme.tertiaryColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                ) {}
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected && isEnabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else if (isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected && isEnabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else if (isEnabled)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            
            if (isSelected && isEnabled) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
