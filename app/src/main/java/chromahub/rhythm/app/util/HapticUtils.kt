package chromahub.rhythm.app.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import chromahub.rhythm.app.data.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Utility object for haptic feedback that respects user settings
 */
object HapticUtils {
    
    /**
     * Performs haptic feedback only if enabled in settings
     */
    fun performHapticFeedback(
        context: Context,
        hapticFeedback: HapticFeedback,
        type: HapticFeedbackType
    ) {
        val appSettings = AppSettings.getInstance(context)
        // Use runBlocking for synchronous check - this is acceptable for a simple boolean check
        val isEnabled = runBlocking { appSettings.hapticFeedbackEnabled.first() }
        
        if (isEnabled) {
            hapticFeedback.performHapticFeedback(type)
        }
    }
}

/**
 * Composable extension function for easier haptic feedback in Compose
 */
@Composable
fun HapticFeedback.performIfEnabled(type: HapticFeedbackType) {
    val context = LocalContext.current
    HapticUtils.performHapticFeedback(context, this, type)
}
