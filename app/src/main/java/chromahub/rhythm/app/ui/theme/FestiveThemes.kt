package chromahub.rhythm.app.ui.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.Month

/**
 * Represents different festive occasions with their themes
 */
enum class FestiveTheme(
    val displayName: String,
    val description: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val particleColors: List<Color> = listOf()
) {
    NONE(
        displayName = "None",
        description = "No festive theme",
        emoji = "🎵",
        primaryColor = Color(0xFF5C4AD5),
        secondaryColor = Color(0xFF5D5D6B),
        tertiaryColor = Color(0xFFFFDDB6)
    ),
    DIWALI(
        displayName = "Diwali",
        description = "Festival of Lights - Vibrant colors and diyas",
        emoji = "🪔",
        primaryColor = Color(0xFFFF7043), // Brighter Orange
        secondaryColor = Color(0xFFFFB300), // Vibrant Gold
        tertiaryColor = Color(0xFFFFD54F), // Brilliant Gold
        particleColors = listOf(
            Color(0xFFFF7043), // Orange
            Color(0xFFFFB300), // Gold
            Color(0xFFFFD54F), // Bright Gold
            Color(0xFFFF1744), // Red
            Color(0xFFFFC107), // Amber
            Color(0xFFFF6F00)  // Deep Orange
        )
    ),
    CHRISTMAS(
        displayName = "Christmas",
        description = "Festive red and green with snowflakes",
        emoji = "🎄",
        primaryColor = Color(0xFFE53935), // Brighter Christmas Red
        secondaryColor = Color(0xFF43A047), // Vibrant Christmas Green
        tertiaryColor = Color(0xFFFFD740), // Bright Gold
        particleColors = listOf(
            Color(0xFFE53935), // Red
            Color(0xFF43A047), // Green
            Color(0xFFFFFFFF), // White (snow)
            Color(0xFFFFD740), // Gold
            Color(0xFFC62828), // Dark Red
            Color(0xFFEF5350)  // Light Red
        )
    ),
    NEW_YEAR(
        displayName = "New Year",
        description = "Celebrate with fireworks and sparkles",
        emoji = "🎆",
        primaryColor = Color(0xFFFFEB3B), // Brilliant Gold/Yellow
        secondaryColor = Color(0xFFAB47BC), // Vibrant Purple
        tertiaryColor = Color(0xFF26C6DA), // Bright Cyan
        particleColors = listOf(
            Color(0xFFFFEB3B), // Gold
            Color(0xFFAB47BC), // Purple
            Color(0xFF26C6DA), // Cyan
            Color(0xFFFF1744), // Red
            Color(0xFF00E676), // Green
            Color(0xFFFF4081)  // Pink
        )
    ),
    HALLOWEEN(
        displayName = "Halloween",
        description = "Spooky orange and purple vibes",
        emoji = "🎃",
        primaryColor = Color(0xFFFF6F00), // Pumpkin Orange
        secondaryColor = Color(0xFF6A1B9A), // Purple
        tertiaryColor = Color(0xFF212121), // Dark
        particleColors = listOf(
            Color(0xFFFF6F00), // Orange
            Color(0xFF6A1B9A), // Purple
            Color(0xFF212121), // Black
            Color(0xFF4CAF50), // Green
            Color(0xFFFFFFFF)  // White
        )
    ),
    HOLI(
        displayName = "Holi",
        description = "Festival of Colors - Rainbow celebration",
        emoji = "🌈",
        primaryColor = Color(0xFFE91E63), // Pink
        secondaryColor = Color(0xFF00BCD4), // Cyan
        tertiaryColor = Color(0xFFFFEB3B), // Yellow
        particleColors = listOf(
            Color(0xFFE91E63), // Pink
            Color(0xFF9C27B0), // Purple
            Color(0xFF00BCD4), // Cyan
            Color(0xFF4CAF50), // Green
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFFF5722)  // Orange
        )
    ),
    VALENTINES(
        displayName = "Valentine's Day",
        description = "Love is in the air with hearts and roses",
        emoji = "💝",
        primaryColor = Color(0xFFE91E63), // Pink
        secondaryColor = Color(0xFFC2185B), // Rose
        tertiaryColor = Color(0xFFF8BBD0), // Light Pink
        particleColors = listOf(
            Color(0xFFE91E63), // Pink
            Color(0xFFC2185B), // Rose
            Color(0xFFFF4081), // Accent Pink
            Color(0xFFF8BBD0), // Light Pink
            Color(0xFFD32F2F)  // Red
        )
    ),
    EASTER(
        displayName = "Easter",
        description = "Spring celebration with pastel colors",
        emoji = "🐰",
        primaryColor = Color(0xFF9C27B0), // Purple
        secondaryColor = Color(0xFF4CAF50), // Spring Green
        tertiaryColor = Color(0xFFFFEB3B), // Yellow
        particleColors = listOf(
            Color(0xFF9C27B0), // Purple
            Color(0xFF4CAF50), // Green
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFE91E63), // Pink
            Color(0xFF00BCD4)  // Light Blue
        )
    ),
    INDEPENDENCE_DAY(
        displayName = "Independence Day",
        description = "Patriotic celebration (customizable)",
        emoji = "🇮🇳",
        primaryColor = Color(0xFFFF6F00), // Saffron
        secondaryColor = Color(0xFF4CAF50), // Green
        tertiaryColor = Color(0xFFFFFFFF), // White
        particleColors = listOf(
            Color(0xFFFF6F00), // Saffron
            Color(0xFF4CAF50), // Green
            Color(0xFFFFFFFF), // White
            Color(0xFF2196F3)  // Blue
        )
    ),
    THANKSGIVING(
        displayName = "Thanksgiving",
        description = "Warm autumn colors and gratitude",
        emoji = "🦃",
        primaryColor = Color(0xFFFF6F00), // Orange
        secondaryColor = Color(0xFF795548), // Brown
        tertiaryColor = Color(0xFFFFEB3B), // Yellow
        particleColors = listOf(
            Color(0xFFFF6F00), // Orange
            Color(0xFF795548), // Brown
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFD32F2F), // Red
            Color(0xFF8D6E63)  // Light Brown
        )
    );

    companion object {
        /**
         * Auto-detect festive theme based on current date
         */
        fun detectCurrentFestival(): FestiveTheme {
            val today = LocalDate.now()
            val month = today.month
            val day = today.dayOfMonth

            return when {
                // Diwali (October-November, varies by year)
                month == Month.OCTOBER && day >= 20 || month == Month.NOVEMBER && day <= 15 -> DIWALI
                
                // Christmas (December)
                month == Month.DECEMBER && day >= 15 -> CHRISTMAS
                
                // New Year (Late December to early January)
                month == Month.DECEMBER && day >= 25 || month == Month.JANUARY && day <= 7 -> NEW_YEAR
                
                // Halloween (October)
                month == Month.OCTOBER && day >= 20 && day <= 31 -> HALLOWEEN
                
                // Valentine's Day (February)
                month == Month.FEBRUARY && day >= 10 && day <= 20 -> VALENTINES
                
                // Holi (March, varies by year)
                month == Month.MARCH && day >= 1 && day <= 15 -> HOLI
                
                // Easter (March-April, varies by year)
                month == Month.MARCH && day >= 20 || month == Month.APRIL && day <= 20 -> EASTER
                
                // Independence Day India (August)
                month == Month.AUGUST && day >= 10 && day <= 20 -> INDEPENDENCE_DAY
                
                // Thanksgiving (November, 4th Thursday)
                month == Month.NOVEMBER && day >= 20 -> THANKSGIVING
                
                else -> NONE
            }
        }
    }
}

/**
 * Data class to store festive theme preferences
 */
data class FestiveThemeConfig(
    val enabled: Boolean = false,
    val selectedTheme: FestiveTheme = FestiveTheme.NONE,
    val autoDetect: Boolean = true,
    val showParticles: Boolean = true,
    val particleIntensity: Float = 0.7f, // 0.0 to 1.0
    val applyToSplash: Boolean = true,
    val applyToMainUI: Boolean = true
)
