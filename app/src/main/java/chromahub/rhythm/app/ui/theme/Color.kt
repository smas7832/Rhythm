package chromahub.rhythm.app.ui.theme

import androidx.compose.ui.graphics.Color

// Material Design 3 Color System - Light Theme
// Primary color palette based on purple/violet for music app
val PrimaryLight = Color(0xFF5C4AD5) // Vibrant purple - main brand color
val OnPrimaryLight = Color(0xFFFFFFFF) // White text on primary
val PrimaryContainerLight = Color(0xFFE6DEFF) // Lighter purple container
val OnPrimaryContainerLight = Color(0xFF170C3E) // Dark text on primary container

// Secondary color palette - complementary teal/green
val SecondaryLight = Color(0xFF5D5D6B) // Neutral gray-purple for balance
val OnSecondaryLight = Color(0xFFFFFFFF) // White text on secondary
val SecondaryContainerLight = Color(0xFFE3E1F0) // Light gray-purple container
val OnSecondaryContainerLight = Color(0xFF1A1A25) // Dark text on secondary container

// Tertiary color palette - accent orange/amber for music controls
val TertiaryLight = Color(0xFF7E5636) // Warm brown for accent
val OnTertiaryLight = Color(0xFFFFFFFF) // White text on tertiary
val TertiaryContainerLight = Color(0xFFFFDDB6) // Light orange container
val OnTertiaryContainerLight = Color(0xFF2C1600) // Dark text on tertiary container

// Error color palette
val ErrorLight = Color(0xFFBA1A1A) // Standard Material error red
val OnErrorLight = Color(0xFFFFFFFF) // White text on error
val ErrorContainerLight = Color(0xFFFFDAD6) // Light red container
val OnErrorContainerLight = Color(0xFF410002) // Dark text on error container

// Background and surface colors
val BackgroundLight = Color(0xFFFEFBFF) // Pure white background
val OnBackgroundLight = Color(0xFF1B1B1F) // Dark text on background
val SurfaceLight = Color(0xFFFEFBFF) // Surface same as background
val OnSurfaceLight = Color(0xFF1B1B1F) // Dark text on surface
val SurfaceVariantLight = Color(0xFFE6E1EC) // Light gray-purple surface variant
val OnSurfaceVariantLight = Color(0xFF48454E) // Medium gray text

// Outline colors for borders and dividers
val OutlineLight = Color(0xFF79767F) // Medium gray outline
val OutlineVariantLight = Color(0xFFCAC6D0) // Light gray outline variant

// Surface containers for different elevation levels
val SurfaceContainerLowestLight = Color(0xFFFFFFFF) // Lowest elevation (pure white)
val SurfaceContainerLowLight = Color(0xFFF8F6FA) // Low elevation
val SurfaceContainerLight = Color(0xFFF2F0F4) // Medium elevation
val SurfaceContainerHighLight = Color(0xFFECEAEE) // High elevation
val SurfaceContainerHighestLight = Color(0xFFE6E4E8) // Highest elevation

// Inverse colors for special cases
val InverseSurfaceLight = Color(0xFF303033) // Dark surface for light theme
val InverseOnSurfaceLight = Color(0xFFF3F0F4) // Light text on inverse surface
val InversePrimaryLight = Color(0xFFCBC2FF) // Light primary on dark surface

// Material Design 3 Color System - Dark Theme
// Primary color palette based on purple/violet for music app
val PrimaryDark = Color(0xFFCBC2FF) // Light purple for dark theme
val OnPrimaryDark = Color(0xFF170C3E) // Dark text on primary
val PrimaryContainerDark = Color(0xFF433499) // Medium purple container
val OnPrimaryContainerDark = Color(0xFFE6DEFF) // Light text on primary container

// Secondary color palette - complementary neutral
val SecondaryDark = Color(0xFFC7C5D4) // Light gray-purple for balance
val OnSecondaryDark = Color(0xFF30303C) // Dark text on secondary
val SecondaryContainerDark = Color(0xFF464653) // Medium gray-purple container
val OnSecondaryContainerDark = Color(0xFFE3E1F0) // Light text on secondary container

// Tertiary color palette - accent orange/amber for music controls
val TertiaryDark = Color(0xFFE8BD88) // Light orange for accent
val OnTertiaryDark = Color(0xFF432A0D) // Dark text on tertiary
val TertiaryContainerDark = Color(0xFF624020) // Medium orange container
val OnTertiaryContainerDark = Color(0xFFFFDDB6) // Light text on tertiary container

// Error color palette
val ErrorDark = Color(0xFFFFB4AB) // Light red for dark theme
val OnErrorDark = Color(0xFF690005) // Dark text on error
val ErrorContainerDark = Color(0xFF93000A) // Medium red container
val OnErrorContainerDark = Color(0xFFFFDAD6) // Light text on error container

// Background and surface colors
val BackgroundDark = Color(0xFF131316) // Dark background
val OnBackgroundDark = Color(0xFFE5E1E6) // Light text on background
val SurfaceDark = Color(0xFF131316) // Surface same as background
val OnSurfaceDark = Color(0xFFE5E1E6) // Light text on surface
val SurfaceVariantDark = Color(0xFF48454E) // Medium gray surface variant
val OnSurfaceVariantDark = Color(0xFFCAC6D0) // Light gray text

// Outline colors for borders and dividers
val OutlineDark = Color(0xFF938F99) // Light gray outline
val OutlineVariantDark = Color(0xFF48454E) // Medium gray outline variant

// Surface containers for different elevation levels
val SurfaceContainerLowestDark = Color(0xFF0E0E11) // Lowest elevation
val SurfaceContainerLowDark = Color(0xFF1B1B1F) // Low elevation
val SurfaceContainerDark = Color(0xFF1F1F23) // Medium elevation
val SurfaceContainerHighDark = Color(0xFF2A2A2E) // High elevation
val SurfaceContainerHighestDark = Color(0xFF353539) // Highest elevation

// Inverse colors for special cases
val InverseSurfaceDark = Color(0xFFE5E1E6) // Light surface for dark theme
val InverseOnSurfaceDark = Color(0xFF303033) // Dark text on inverse surface
val InversePrimaryDark = Color(0xFF5C4AD5) // Dark primary on light surface

// Legacy music-specific colors (for backward compatibility)
val MusicPrimaryLight = PrimaryLight
val MusicPrimaryVariantLight = PrimaryContainerLight
val MusicSecondaryLight = TertiaryLight

val MusicPrimaryDark = PrimaryDark
val MusicPrimaryVariantDark = PrimaryContainerDark
val MusicSecondaryDark = TertiaryDark

// UI Specific Colors for player components
val PlayerButtonColor = PrimaryLight
val PlayerButtonColorDark = PrimaryDark
val PlayerProgressColor = TertiaryLight
val PlayerProgressBackgroundLight = SurfaceContainerLight
val PlayerProgressBackgroundDark = SurfaceContainerDark
val PlayerBackgroundLight = BackgroundLight
val PlayerBackgroundDark = BackgroundDark

// Status colors for notifications, errors, etc.
val SuccessLight = Color(0xFF2E7D32) // Material green
val SuccessDark = Color(0xFF66BB6A) // Light green for dark theme
val WarningLight = Color(0xFFEF6C00) // Material orange
val WarningDark = Color(0xFFFFB74D) // Light orange for dark theme