package chromahub.rhythm.app.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.FragmentActivity

/**
 * Helper class to show the system media output switcher dialog
 */
object OutputSwitcherHelper {
    private const val TAG = "OutputSwitcherHelper"
    
    // Define the constant for the floating media output switcher dialog
    private const val ACTION_MEDIA_OUTPUT = "android.settings.MEDIA_OUTPUT"
    
    /**
     * Show the system media output switcher dialog
     * This is the recommended way to switch audio output devices on Android 13+
     * 
     * @param activity The activity context to use for showing the dialog
     * @return true if the dialog was shown, false otherwise
     */
    fun showOutputSwitcher(activity: FragmentActivity): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Try the direct media output settings intent first (Android 13+)
                try {
                    val intent = Intent(ACTION_MEDIA_OUTPUT)
                    activity.startActivity(intent)
                    Log.d(TAG, "Showing system media output switcher dialog via ACTION_MEDIA_OUTPUT")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing media output panel: ${e.message}")
                }
                
                // Fall back to MediaRouter if the direct intent fails
                try {
                    val mediaRouter = androidx.mediarouter.media.MediaRouter.getInstance(activity)
                    
                    // Create a selector for all routes
                    val selector = androidx.mediarouter.media.MediaRouteSelector.Builder()
                        .addControlCategory(androidx.mediarouter.media.MediaControlIntent.CATEGORY_LIVE_AUDIO)
                        .addControlCategory(androidx.mediarouter.media.MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                        .build()
                    
                    // Show the dialog
                    val fragment = androidx.mediarouter.app.MediaRouteChooserDialogFragment()
                    fragment.setRouteSelector(selector)
                    fragment.show(activity.supportFragmentManager, "MediaRouteChooserDialog")
                    Log.d(TAG, "Showing MediaRouteChooserDialog")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing MediaRouteChooserDialog: ${e.message}")
                }
            }
            
            // Try alternative intents for older Android versions
            return showFallbackOutputSwitcher(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing output switcher dialog: ${e.message}")
            return showFallbackOutputSwitcher(activity)
        }
    }
    
    /**
     * Show fallback output switcher options
     * 
     * @param context The context to use for showing the dialog
     * @return true if a fallback was shown, false otherwise
     */
    private fun showFallbackOutputSwitcher(context: Context): Boolean {
        // Try various intents that might work on different devices/Android versions
        val intents = listOf(
            Intent("android.settings.MEDIA_OUTPUT"), // Floating dialog (primary)
            Intent("com.android.settings.panel.MediaOutputPanel"), // Alternative path for some devices
            Intent("android.settings.panel.MediaOutputPanel"), // Settings panel (fallback)
            Intent("android.settings.MEDIA_OUTPUT_SETTINGS"), // Settings page (fallback)
            Intent("android.settings.SOUND_SETTINGS"), // Sound settings (last resort)
            Intent("android.settings.BLUETOOTH_SETTINGS"), // Bluetooth settings (last resort)
            Intent(Settings.ACTION_SOUND_SETTINGS) // Standard sound settings (last resort)
        )
        
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Log.d(TAG, "Showing fallback output switcher via intent: ${intent.action}")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error showing fallback intent ${intent.action}: ${e.message}")
            }
        }
        
        Log.e(TAG, "All fallback intents failed")
        return false
    }
} 