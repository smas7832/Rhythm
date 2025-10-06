@file:OptIn(ExperimentalMaterial3Api::class)
package chromahub.rhythm.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.theme.getFontPreviewStyle
import chromahub.rhythm.app.ui.theme.getCustomFontPreviewStyle
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
    
    // Tab state - now 3 tabs: Overview, Colors, Fonts
    var selectedTab by remember { mutableStateOf(0) }
    
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
                        }
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
    onSchemeSelected: (String) -> Unit
) {
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
        
        items(colorSchemeOptions) { option ->
            ColorSchemeCard(
                option = option,
                isSelected = currentScheme == option.name,
                isEnabled = selectedColorSource == ColorSource.CUSTOM,
                onSelect = { onSchemeSelected(option.name) }
            )
        }
        
        // Bottom padding for better scrolling experience
        item {
            Spacer(modifier = Modifier.height(8.dp))
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
        )
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
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
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
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
            Text(
                text = "Color Source",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
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
        
        // Font Source Selection
        item {
            Text(
                text = "Font Source",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
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
        
        // Theme Mode Settings
        item {
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
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
