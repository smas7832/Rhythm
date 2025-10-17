package chromahub.rhythm.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import chromahub.rhythm.app.utils.FontLoader

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Color.Black,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceDim = SurfaceContainerLowestDark,
    surfaceBright = SurfaceContainerHighestDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Color.Black,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceDim = SurfaceContainerLowestLight,
    surfaceBright = SurfaceContainerHighestLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight
)

/**
 * Get custom color scheme based on preset name
 */
fun getCustomColorScheme(schemeName: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme {
    // Check if it's a custom color scheme first
    val customScheme = parseCustomColorScheme(schemeName, darkTheme)
    if (customScheme != null) {
        return customScheme
    }
    
    return when (schemeName) {
        "Warm" -> if (darkTheme) {
            darkColorScheme(
                primary = WarmPrimaryDark,
                onPrimary = WarmOnPrimaryDark,
                primaryContainer = WarmPrimaryContainerDark,
                onPrimaryContainer = WarmOnPrimaryContainerDark,
                secondary = WarmSecondaryDark,
                onSecondary = WarmOnSecondaryDark,
                secondaryContainer = WarmSecondaryContainerDark,
                onSecondaryContainer = WarmOnSecondaryContainerDark,
                tertiary = WarmTertiaryDark,
                onTertiary = WarmOnTertiaryDark,
                tertiaryContainer = WarmTertiaryContainerDark,
                onTertiaryContainer = WarmOnTertiaryContainerDark,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = WarmPrimaryLight,
                onPrimary = WarmOnPrimaryLight,
                primaryContainer = WarmPrimaryContainerLight,
                onPrimaryContainer = WarmOnPrimaryContainerLight,
                secondary = WarmSecondaryLight,
                onSecondary = WarmOnSecondaryLight,
                secondaryContainer = WarmSecondaryContainerLight,
                onSecondaryContainer = WarmOnSecondaryContainerLight,
                tertiary = WarmTertiaryLight,
                onTertiary = WarmOnTertiaryLight,
                tertiaryContainer = WarmTertiaryContainerLight,
                onTertiaryContainer = WarmOnTertiaryContainerLight,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Cool" -> if (darkTheme) {
            darkColorScheme(
                primary = CoolPrimaryDark,
                onPrimary = CoolOnPrimaryDark,
                primaryContainer = CoolPrimaryContainerDark,
                onPrimaryContainer = CoolOnPrimaryContainerDark,
                secondary = CoolSecondaryDark,
                onSecondary = CoolOnSecondaryDark,
                secondaryContainer = CoolSecondaryContainerDark,
                onSecondaryContainer = CoolOnSecondaryContainerDark,
                tertiary = CoolTertiaryDark,
                onTertiary = CoolOnTertiaryDark,
                tertiaryContainer = CoolTertiaryContainerDark,
                onTertiaryContainer = CoolOnTertiaryContainerDark,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = CoolPrimaryLight,
                onPrimary = CoolOnPrimaryLight,
                primaryContainer = CoolPrimaryContainerLight,
                onPrimaryContainer = CoolOnPrimaryContainerLight,
                secondary = CoolSecondaryLight,
                onSecondary = CoolOnSecondaryLight,
                secondaryContainer = CoolSecondaryContainerLight,
                onSecondaryContainer = CoolOnSecondaryContainerLight,
                tertiary = CoolTertiaryLight,
                onTertiary = CoolOnTertiaryLight,
                tertiaryContainer = CoolTertiaryContainerLight,
                onTertiaryContainer = CoolOnTertiaryContainerLight,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Forest" -> if (darkTheme) {
            darkColorScheme(
                primary = ForestPrimaryDark,
                onPrimary = ForestOnPrimaryDark,
                primaryContainer = ForestPrimaryContainerDark,
                onPrimaryContainer = ForestOnPrimaryContainerDark,
                secondary = ForestSecondaryDark,
                onSecondary = ForestOnSecondaryDark,
                secondaryContainer = ForestSecondaryContainerDark,
                onSecondaryContainer = ForestOnSecondaryContainerDark,
                tertiary = ForestTertiaryDark,
                onTertiary = ForestOnTertiaryDark,
                tertiaryContainer = ForestTertiaryContainerDark,
                onTertiaryContainer = ForestOnTertiaryContainerDark,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = ForestPrimaryLight,
                onPrimary = ForestOnPrimaryLight,
                primaryContainer = ForestPrimaryContainerLight,
                onPrimaryContainer = ForestOnPrimaryContainerLight,
                secondary = ForestSecondaryLight,
                onSecondary = ForestOnSecondaryLight,
                secondaryContainer = ForestSecondaryContainerLight,
                onSecondaryContainer = ForestOnSecondaryContainerLight,
                tertiary = ForestTertiaryLight,
                onTertiary = ForestOnTertiaryLight,
                tertiaryContainer = ForestTertiaryContainerLight,
                onTertiaryContainer = ForestOnTertiaryContainerLight,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Rose" -> if (darkTheme) {
            darkColorScheme(
                primary = RosePrimaryDark,
                onPrimary = RoseOnPrimaryDark,
                primaryContainer = RosePrimaryContainerDark,
                onPrimaryContainer = RoseOnPrimaryContainerDark,
                secondary = RoseSecondaryDark,
                onSecondary = RoseOnSecondaryDark,
                secondaryContainer = RoseSecondaryContainerDark,
                onSecondaryContainer = RoseOnSecondaryContainerDark,
                tertiary = RoseTertiaryDark,
                onTertiary = RoseOnTertiaryDark,
                tertiaryContainer = RoseTertiaryContainerDark,
                onTertiaryContainer = RoseOnTertiaryContainerDark,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = RosePrimaryLight,
                onPrimary = RoseOnPrimaryLight,
                primaryContainer = RosePrimaryContainerLight,
                onPrimaryContainer = RoseOnPrimaryContainerLight,
                secondary = RoseSecondaryLight,
                onSecondary = RoseOnSecondaryLight,
                secondaryContainer = RoseSecondaryContainerLight,
                onSecondaryContainer = RoseOnSecondaryContainerLight,
                tertiary = RoseTertiaryLight2,
                onTertiary = RoseOnTertiaryLight,
                tertiaryContainer = RoseTertiaryContainerLight,
                onTertiaryContainer = RoseOnTertiaryContainerLight,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Monochrome" -> if (darkTheme) {
            darkColorScheme(
                primary = MonoPrimaryDark,
                onPrimary = MonoOnPrimaryDark,
                primaryContainer = MonoPrimaryContainerDark,
                onPrimaryContainer = MonoOnPrimaryContainerDark,
                secondary = MonoSecondaryDark,
                onSecondary = MonoOnSecondaryDark,
                secondaryContainer = MonoSecondaryContainerDark,
                onSecondaryContainer = MonoOnSecondaryContainerDark,
                tertiary = MonoTertiaryDark,
                onTertiary = MonoOnTertiaryDark,
                tertiaryContainer = MonoTertiaryContainerDark,
                onTertiaryContainer = MonoOnTertiaryContainerDark,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = MonoPrimaryLight,
                onPrimary = MonoOnPrimaryLight,
                primaryContainer = MonoPrimaryContainerLight,
                onPrimaryContainer = MonoOnPrimaryContainerLight,
                secondary = MonoSecondaryLight,
                onSecondary = MonoOnSecondaryLight,
                secondaryContainer = MonoSecondaryContainerLight,
                onSecondaryContainer = MonoOnSecondaryContainerLight,
                tertiary = MonoTertiaryLight,
                onTertiary = MonoOnTertiaryLight,
                tertiaryContainer = MonoTertiaryContainerLight,
                onTertiaryContainer = MonoOnTertiaryContainerLight,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }

        "Lavender" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF9C7BFF),
                onPrimary = Color(0xFF2C1B69),
                primaryContainer = Color(0xFFC4B5FF),
                onPrimaryContainer = Color(0xFF23005C),
                secondary = Color(0xFFCEB3FF),
                onSecondary = Color(0xFF352B4B),
                secondaryContainer = Color(0xFFE0BBFF),
                onSecondaryContainer = Color(0xFF3F2A6C),
                tertiary = Color(0xFFE5B5FF),
                onTertiary = Color(0xFF44196A),
                tertiaryContainer = Color(0xFFFFD6FF),
                onTertiaryContainer = Color(0xFF52147A),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF7C4DFF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEAEAFF),
                onPrimaryContainer = Color(0xFF23005C),
                secondary = Color(0xFF9575CD),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFEDE7F3),
                onSecondaryContainer = Color(0xFF3F2A6C),
                tertiary = Color(0xFFBA68C8),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFE4FF),
                onTertiaryContainer = Color(0xFF52147A),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Mint" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF4FC3F7),
                onPrimary = Color(0xFF0D3447),
                primaryContainer = Color(0xFF81D4FA),
                onPrimaryContainer = Color(0xFF001B2E),
                secondary = Color(0xFF4DD0E1),
                onSecondary = Color(0xFF00363C),
                secondaryContainer = Color(0xFF26C6DA),
                onSecondaryContainer = Color(0xFF0F3740),
                tertiary = Color(0xFF00BCD4),
                onTertiary = Color(0xFF00363C),
                tertiaryContainer = Color(0xFF4DD0E1),
                onTertiaryContainer = Color(0xFF00363C),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0097A7),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFB2EBF2),
                onPrimaryContainer = Color(0xFF001B2E),
                secondary = Color(0xFF00ACC1),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE0F2F1),
                onSecondaryContainer = Color(0xFF0F3740),
                tertiary = Color(0xFF00BCD4),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFE1F5FE),
                onTertiaryContainer = Color(0xFF00363C),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Ocean" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF4DD0E1),
                onPrimary = Color(0xFF00363C),
                primaryContainer = Color(0xFF006064),
                onPrimaryContainer = Color(0xFFB2EBF2),
                secondary = Color(0xFF4DD0E1),
                onSecondary = Color(0xFF00363C),
                secondaryContainer = Color(0xFF00838F),
                onSecondaryContainer = Color(0xFFB2EBF2),
                tertiary = Color(0xFF00BCD4),
                onTertiary = Color(0xFF00363C),
                tertiaryContainer = Color(0xFF00838F),
                onTertiaryContainer = Color(0xFFB2EBF2),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF006064),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFB2EBF2),
                onPrimaryContainer = Color(0xFF001B2E),
                secondary = Color(0xFF00838F),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFB2DFDB),
                onSecondaryContainer = Color(0xFF00201D),
                tertiary = Color(0xFF00ACC1),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFB2EBF2),
                onTertiaryContainer = Color(0xFF002025),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Aurora" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF00E676),
                onPrimary = Color(0xFF00390A),
                primaryContainer = Color(0xFF00C853),
                onPrimaryContainer = Color(0xFF000D05),
                secondary = Color(0xFF69F0AE),
                onSecondary = Color(0xFF00390A),
                secondaryContainer = Color(0xFF00E676),
                onSecondaryContainer = Color(0xFF000D05),
                tertiary = Color(0xFFDCE775),
                onTertiary = Color(0xFF3F5100),
                tertiaryContainer = Color(0xFF5A7700),
                onTertiaryContainer = Color(0xFFE7F5E1),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF00C853),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF69F0AE),
                onPrimaryContainer = Color(0xFF000D05),
                secondary = Color(0xFF00E676),
                onSecondary = Color(0xFF00390A),
                secondaryContainer = Color(0xFFDCE775),
                onSecondaryContainer = Color(0xFF223608),
                tertiary = Color(0xFF69F0AE),
                onTertiary = Color(0xFF00390A),
                tertiaryContainer = Color(0xFFDCE775),
                onTertiaryContainer = Color(0xFF223608),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Amber" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFF8F00),
                onPrimary = Color(0xFF2E2416),
                primaryContainer = Color(0xFFFF6F00),
                onPrimaryContainer = Color(0xFF4E2600),
                secondary = Color(0xFFFFC107),
                onSecondary = Color(0xFF2E2416),
                secondaryContainer = Color(0xFFFF8F00),
                onSecondaryContainer = Color(0xFF4E2600),
                tertiary = Color(0xFFFFD54F),
                onTertiary = Color(0xFF2E2416),
                tertiaryContainer = Color(0xFFFFC107),
                onTertiaryContainer = Color(0xFF4E2600),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFFF6F00),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFCC02),
                onPrimaryContainer = Color(0xFF2E2416),
                secondary = Color(0xFFFF8F00),
                onSecondary = Color(0xFF2E2416),
                secondaryContainer = Color(0xFFFFE0B2),
                onSecondaryContainer = Color(0xFF442C18),
                tertiary = Color(0xFFFFC107),
                onTertiary = Color(0xFF2E2416),
                tertiaryContainer = Color(0xFFFFECB3),
                onTertiaryContainer = Color(0xFF442C18),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Crimson" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFC62828),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFB71C1C),
                onPrimaryContainer = Color(0xFFFFDAD6),
                secondary = Color(0xFFD32F2F),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFC62828),
                onSecondaryContainer = Color(0xFFFFDAD6),
                tertiary = Color(0xFFE53935),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFD32F2F),
                onTertiaryContainer = Color(0xFFFFDAD6),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFB71C1C),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFF8BBD0),
                onPrimaryContainer = Color(0xFF3E001D),
                secondary = Color(0xFFC62828),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFCDD2),
                onSecondaryContainer = Color(0xFF300016),
                tertiary = Color(0xFFD32F2F),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFABA6),
                onTertiaryContainer = Color(0xFF270001),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        "Emerald" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF388E3C),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF2E7D32),
                onPrimaryContainer = Color(0xFFC8E6C9),
                secondary = Color(0xFF4CAF50),
                onSecondary = Color(0xFF0D5016),
                secondaryContainer = Color(0xFF388E3C),
                onSecondaryContainer = Color(0xFFC8E6C9),
                tertiary = Color(0xFF81C784),
                onTertiary = Color(0xFF0D5016),
                tertiaryContainer = Color(0xFF4CAF50),
                onTertiaryContainer = Color(0xFFC8E6C9),
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF2E7D32),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFC8E6C9),
                onPrimaryContainer = Color(0xFF0D5016),
                secondary = Color(0xFF388E3C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFDCEDC8),
                onSecondaryContainer = Color(0xFF1B5E20),
                tertiary = Color(0xFF4CAF50),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFE7F5E1),
                onTertiaryContainer = Color(0xFF223608),
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
}

/**
 * Parse custom color scheme from format: custom_primaryHex_secondaryHex_tertiaryHex
 */
fun parseCustomColorScheme(schemeName: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme? {
    if (!schemeName.startsWith("custom_")) return null
    
    val parts = schemeName.split("_")
    if (parts.size != 4) return null
    
    try {
        val primaryHex = parts[1].padStart(6, '0')
        val secondaryHex = parts[2].padStart(6, '0') 
        val tertiaryHex = parts[3].padStart(6, '0')
        
        val primary = Color(("FF$primaryHex").toLong(16))
        val secondary = Color(("FF$secondaryHex").toLong(16))
        val tertiary = Color(("FF$tertiaryHex").toLong(16))
        
        // Create a basic color scheme using the custom colors
        // For simplicity, we'll use a similar structure to the default schemes
        return if (darkTheme) {
            darkColorScheme(
                primary = primary,
                onPrimary = if (primary.luminance() > 0.5f) Color.Black else Color.White,
                primaryContainer = primary.copy(alpha = 0.3f),
                onPrimaryContainer = if (primary.copy(alpha = 0.3f).luminance() > 0.5f) Color.Black else Color.White,
                secondary = secondary,
                onSecondary = if (secondary.luminance() > 0.5f) Color.Black else Color.White,
                secondaryContainer = secondary.copy(alpha = 0.3f),
                onSecondaryContainer = if (secondary.copy(alpha = 0.3f).luminance() > 0.5f) Color.Black else Color.White,
                tertiary = tertiary,
                onTertiary = if (tertiary.luminance() > 0.5f) Color.Black else Color.White,
                tertiaryContainer = tertiary.copy(alpha = 0.3f),
                onTertiaryContainer = if (tertiary.copy(alpha = 0.3f).luminance() > 0.5f) Color.Black else Color.White,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = if (primary.luminance() > 0.5f) Color.Black else Color.White,
                primaryContainer = primary.copy(alpha = 0.2f),
                onPrimaryContainer = if (primary.copy(alpha = 0.2f).luminance() > 0.5f) Color.Black else Color.White,
                secondary = secondary,
                onSecondary = if (secondary.luminance() > 0.5f) Color.Black else Color.White,
                secondaryContainer = secondary.copy(alpha = 0.2f),
                onSecondaryContainer = if (secondary.copy(alpha = 0.2f).luminance() > 0.5f) Color.Black else Color.White,
                tertiary = tertiary,
                onTertiary = if (tertiary.luminance() > 0.5f) Color.Black else Color.White,
                tertiaryContainer = tertiary.copy(alpha = 0.2f),
                onTertiaryContainer = if (tertiary.copy(alpha = 0.2f).luminance() > 0.5f) Color.Black else Color.White,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
    } catch (e: Exception) {
        // Invalid custom scheme format, return null
        return null
    }
}

/**
 * Create a color scheme from extracted album art colors
 */
fun getAlbumArtColorScheme(colorsJson: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme {
    val extractedColors = chromahub.rhythm.app.util.ColorExtractor.jsonToColors(colorsJson)
    
    // Fallback to default if parsing fails
    if (extractedColors == null) {
        return if (darkTheme) DarkColorScheme else LightColorScheme
    }
    
    // The extracted colors already contain proper Material 3 color roles
    // We just need to convert them to Compose Color and adjust for dark/light theme
    
    return if (darkTheme) {
        // For dark theme, we need to invert/adjust the extracted light theme colors
        val primary = Color(extractedColors.primary)
        val secondary = Color(extractedColors.secondary)
        val tertiary = Color(extractedColors.tertiary)
        
        darkColorScheme(
            primary = primary, // Use full color for dark theme
            onPrimary = Color(extractedColors.onPrimary),
            // Create darker container colors for dark theme with better brightness and saturation
            primaryContainer = primary.copy(alpha = 1f).let { 
                Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
                    android.graphics.Color.colorToHSV(it.toArgb(), this)
                    this[1] = (this[1] * 0.85f).coerceAtLeast(0.4f) // Maintain more saturation (85% of original, min 40%)
                    this[2] *= 0.55f // Increase brightness from 40% to 55% for better visibility
                }))
            },
            onPrimaryContainer = Color(extractedColors.onPrimary),
            
            secondary = secondary,
            onSecondary = Color(extractedColors.onSecondary),
            secondaryContainer = secondary.copy(alpha = 1f).let {
                Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
                    android.graphics.Color.colorToHSV(it.toArgb(), this)
                    this[1] = (this[1] * 0.85f).coerceAtLeast(0.4f)
                    this[2] *= 0.55f
                }))
            },
            onSecondaryContainer = Color(extractedColors.onSecondary),
            
            tertiary = tertiary,
            onTertiary = Color(extractedColors.onTertiary),
            tertiaryContainer = tertiary.copy(alpha = 1f).let {
                Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
                    android.graphics.Color.colorToHSV(it.toArgb(), this)
                    this[1] = (this[1] * 0.85f).coerceAtLeast(0.4f)
                    this[2] *= 0.55f
                }))
            },
            onTertiaryContainer = Color(extractedColors.onTertiary),
            
            // Use app's standard dark background and surfaces for consistency
            background = BackgroundDark,
            onBackground = OnBackgroundDark,
            surface = SurfaceDark,
            onSurface = OnSurfaceDark,
            surfaceVariant = SurfaceVariantDark,
            onSurfaceVariant = OnSurfaceVariantDark,
            surfaceContainerLowest = SurfaceContainerLowestDark,
            surfaceContainerLow = SurfaceContainerLowDark,
            surfaceContainer = SurfaceContainerDark,
            surfaceContainerHigh = SurfaceContainerHighDark,
            surfaceContainerHighest = SurfaceContainerHighestDark,
            
            error = ErrorDark,
            onError = OnErrorDark,
            errorContainer = ErrorContainerDark,
            onErrorContainer = OnErrorContainerDark,
            
            outline = OutlineDark,
            outlineVariant = OutlineVariantDark,
            scrim = Color.Black,
            inverseSurface = InverseSurfaceDark,
            inverseOnSurface = InverseOnSurfaceDark,
            inversePrimary = InversePrimaryDark
        )
    } else {
        // For light theme, use extracted colors directly
        lightColorScheme(
            primary = Color(extractedColors.primary),
            onPrimary = Color(extractedColors.onPrimary),
            primaryContainer = Color(extractedColors.primaryContainer),
            onPrimaryContainer = Color(extractedColors.onPrimaryContainer),
            
            secondary = Color(extractedColors.secondary),
            onSecondary = Color(extractedColors.onSecondary),
            secondaryContainer = Color(extractedColors.secondaryContainer),
            onSecondaryContainer = Color(extractedColors.onSecondaryContainer),
            
            tertiary = Color(extractedColors.tertiary),
            onTertiary = Color(extractedColors.onTertiary),
            tertiaryContainer = Color(extractedColors.tertiaryContainer),
            onTertiaryContainer = Color(extractedColors.onTertiaryContainer),
            
            // Use extracted surface colors for light theme
            surface = Color(extractedColors.surface),
            onSurface = Color(extractedColors.onSurface),
            surfaceVariant = Color(extractedColors.surfaceVariant),
            onSurfaceVariant = Color(extractedColors.onSurfaceVariant),
            background = Color(extractedColors.surface).copy(alpha = 0.98f),
            onBackground = Color(extractedColors.onSurface),
            
            // Use app's standard surface containers for consistency
            surfaceContainerLowest = SurfaceContainerLowestLight,
            surfaceContainerLow = SurfaceContainerLowLight,
            surfaceContainer = SurfaceContainerLight,
            surfaceContainerHigh = SurfaceContainerHighLight,
            surfaceContainerHighest = SurfaceContainerHighestLight,
            
            error = ErrorLight,
            onError = OnErrorLight,
            errorContainer = ErrorContainerLight,
            onErrorContainer = OnErrorContainerLight,
            
            outline = OutlineLight,
            outlineVariant = OutlineVariantLight,
            scrim = Color.Black,
            inverseSurface = InverseSurfaceLight,
            inverseOnSurface = InverseOnSurfaceLight,
            inversePrimary = InversePrimaryLight
        )
    }
}

/**
 * Apply festive theme colors to the base color scheme
 */
private fun applyFestiveColors(
    baseScheme: ColorScheme,
    festiveTheme: chromahub.rhythm.app.ui.theme.FestiveTheme,
    darkTheme: Boolean
): ColorScheme {
    // More vibrant festive colors - higher blend factor for stronger impact
    val primaryBlend = 0.85f // Strong festive primary color
    val secondaryBlend = 0.75f // Good festive secondary
    val tertiaryBlend = 0.70f // Balanced tertiary
    val containerBlend = 0.60f // Visible in containers
    
    // Adjust colors for dark/light theme
    val adjustedPrimary = if (darkTheme) {
        // Lighten festive colors for dark theme
        festiveTheme.primaryColor.copy(
            red = (festiveTheme.primaryColor.red * 0.9f + 0.1f).coerceAtMost(1f),
            green = (festiveTheme.primaryColor.green * 0.9f + 0.1f).coerceAtMost(1f),
            blue = (festiveTheme.primaryColor.blue * 0.9f + 0.1f).coerceAtMost(1f)
        )
    } else {
        // Keep vibrant for light theme
        festiveTheme.primaryColor
    }
    
    val adjustedSecondary = if (darkTheme) {
        festiveTheme.secondaryColor.copy(
            red = (festiveTheme.secondaryColor.red * 0.85f + 0.15f).coerceAtMost(1f),
            green = (festiveTheme.secondaryColor.green * 0.85f + 0.15f).coerceAtMost(1f),
            blue = (festiveTheme.secondaryColor.blue * 0.85f + 0.15f).coerceAtMost(1f)
        )
    } else {
        festiveTheme.secondaryColor
    }
    
    val adjustedTertiary = if (darkTheme) {
        festiveTheme.tertiaryColor.copy(
            red = (festiveTheme.tertiaryColor.red * 0.80f + 0.20f).coerceAtMost(1f),
            green = (festiveTheme.tertiaryColor.green * 0.80f + 0.20f).coerceAtMost(1f),
            blue = (festiveTheme.tertiaryColor.blue * 0.80f + 0.20f).coerceAtMost(1f)
        )
    } else {
        festiveTheme.tertiaryColor
    }
    
    return baseScheme.copy(
        primary = blendColors(baseScheme.primary, adjustedPrimary, primaryBlend),
        onPrimary = if (adjustedPrimary.luminance() > 0.5f) Color.Black else Color.White,
        primaryContainer = blendColors(
            baseScheme.primaryContainer, 
            adjustedPrimary.copy(alpha = if (darkTheme) 0.25f else 0.15f), 
            containerBlend
        ),
        onPrimaryContainer = if (darkTheme) adjustedPrimary.copy(
            red = (adjustedPrimary.red + 0.3f).coerceAtMost(1f),
            green = (adjustedPrimary.green + 0.3f).coerceAtMost(1f),
            blue = (adjustedPrimary.blue + 0.3f).coerceAtMost(1f)
        ) else adjustedPrimary.copy(
            red = (adjustedPrimary.red * 0.7f).coerceAtLeast(0f),
            green = (adjustedPrimary.green * 0.7f).coerceAtLeast(0f),
            blue = (adjustedPrimary.blue * 0.7f).coerceAtLeast(0f)
        ),
        secondary = blendColors(baseScheme.secondary, adjustedSecondary, secondaryBlend),
        onSecondary = if (adjustedSecondary.luminance() > 0.5f) Color.Black else Color.White,
        secondaryContainer = blendColors(
            baseScheme.secondaryContainer, 
            adjustedSecondary.copy(alpha = if (darkTheme) 0.25f else 0.15f), 
            containerBlend * 0.8f
        ),
        tertiary = blendColors(baseScheme.tertiary, adjustedTertiary, tertiaryBlend),
        onTertiary = if (adjustedTertiary.luminance() > 0.5f) Color.Black else Color.White,
        tertiaryContainer = blendColors(
            baseScheme.tertiaryContainer, 
            adjustedTertiary.copy(alpha = if (darkTheme) 0.25f else 0.15f), 
            containerBlend * 0.8f
        )
    )
}

/**
 * Blend two colors together
 */
private fun blendColors(base: Color, overlay: Color, factor: Float): Color {
    val clampedFactor = factor.coerceIn(0f, 1f)
    return Color(
        red = base.red * (1 - clampedFactor) + overlay.red * clampedFactor,
        green = base.green * (1 - clampedFactor) + overlay.green * clampedFactor,
        blue = base.blue * (1 - clampedFactor) + overlay.blue * clampedFactor,
        alpha = base.alpha
    )
}

@Composable
fun RhythmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our expressive theme
    customColorScheme: String = "Default",
    customFont: String = "System",
    fontSource: String = "SYSTEM",
    customFontPath: String? = null,
    colorSource: String = "CUSTOM",
    extractedAlbumColorsJson: String? = null,
    festiveThemeEnabled: Boolean = false,
    festiveThemeSelected: String = "NONE",
    festiveThemeAutoDetect: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Determine active festive theme
    val activeFestiveTheme = remember(festiveThemeEnabled, festiveThemeAutoDetect, festiveThemeSelected) {
        if (!festiveThemeEnabled) {
            chromahub.rhythm.app.ui.theme.FestiveTheme.NONE
        } else if (festiveThemeAutoDetect) {
            chromahub.rhythm.app.ui.theme.FestiveTheme.detectCurrentFestival()
        } else {
            try {
                chromahub.rhythm.app.ui.theme.FestiveTheme.valueOf(festiveThemeSelected)
            } catch (e: Exception) {
                chromahub.rhythm.app.ui.theme.FestiveTheme.NONE
            }
        }
    }
    
    val baseColorScheme = when {
        // Album art colors take highest priority when available
        colorSource == "ALBUM_ART" && extractedAlbumColorsJson != null -> {
            getAlbumArtColorScheme(extractedAlbumColorsJson, darkTheme)
        }
        // Dynamic Material You colors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Custom preset color schemes
        customColorScheme != "Default" -> getCustomColorScheme(customColorScheme, darkTheme)
        // Default Rhythm color scheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Apply festive theme colors if enabled
    val colorScheme = if (festiveThemeEnabled && activeFestiveTheme != chromahub.rhythm.app.ui.theme.FestiveTheme.NONE) {
        applyFestiveColors(baseColorScheme, activeFestiveTheme, darkTheme)
    } else {
        baseColorScheme
    }
    
    // Load typography based on font source
    val typography = when (fontSource) {
        "CUSTOM" -> {
            // Try to load custom font
            val customFontFamily = FontLoader.loadCustomFont(context, customFontPath)
            if (customFontFamily != null) {
                getTypographyWithCustomFont(customFontFamily)
            } else {
                // Fall back to system font if custom font fails to load
                getTypographyForFont(customFont)
            }
        }
        else -> {
            // Use system fonts
            getTypographyForFont(customFont)
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Set system bar colors to transparent for true edge-to-edge
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT)
            
            // Handle system bar appearance based on theme
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Status bar icons/text color
            insetsController.isAppearanceLightStatusBars = !darkTheme
            
            // Navigation bar icons/buttons color
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
