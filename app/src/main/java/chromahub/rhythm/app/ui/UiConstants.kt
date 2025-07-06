package chromahub.rhythm.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Central place for shared UI constants and composition locals.
 */
object UiConstants {
    /** Height of the global Mini-player card (including its padding). */
    val MiniPlayerHeight = 88.dp // Reduced from 96.dp for better proportions
    /** Height of the bottom navigation bar */
    val NavBarHeight = 64.dp
}

/**
 * CompositionLocal that provides bottom padding equal to the Mini-player height
 * on screens where the Mini-player is shown. Defaults to zero when not provided.
 */
val LocalMiniPlayerPadding = compositionLocalOf { PaddingValues(bottom = UiConstants.MiniPlayerHeight) }
