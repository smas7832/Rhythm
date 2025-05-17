package chromahub.rhythm.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.AudioEffect
import android.util.Log
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController

/**
 * Utility class for handling equalizer-related functionality
 */
object EqualizerUtils {
    private const val TAG = "EqualizerUtils"
    
    /**
     * Opens the system equalizer
     * 
     * @param context The context to use for starting the activity
     * @return true if the equalizer was opened successfully, false otherwise
     */
    fun openSystemEqualizer(context: Context): Boolean {
        return try {
            // First try with the standard audio effect action
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                // Add flags to open in a new task
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Check if there's an app that can handle this intent
            if (isIntentResolvable(context, intent)) {
                context.startActivity(intent)
                Log.d(TAG, "Opened system equalizer with standard intent")
                true
            } else {
                // Try to find a specific equalizer app
                val found = tryOpenSpecificEqualizerApps(context)
                if (!found) {
                    Log.w(TAG, "No system equalizer app found")
                    Toast.makeText(
                        context, 
                        "No equalizer app found on your device", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                found
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening system equalizer", e)
            Toast.makeText(
                context, 
                "Could not open equalizer: ${e.localizedMessage}", 
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    
    /**
     * Try to open specific known equalizer apps
     */
    private fun tryOpenSpecificEqualizerApps(context: Context): Boolean {
        // List of known equalizer packages
        val equalizerPackages = listOf(
            "com.android.musicfx",                // Android's built-in equalizer
            "com.sec.android.app.soundalive",     // Samsung's equalizer
            "com.motorola.dtv.soundenhancer",     // Motorola's equalizer
            "com.xiaomi.equalizer",               // Xiaomi's equalizer
            "com.oneplus.sound.tuner",            // OnePlus equalizer
            "com.sony.soundenhancement.spapp",    // Sony's equalizer
            "com.google.android.soundpicker",     // Google's sound picker
            "com.huawei.audioeffectcenter"        // Huawei's equalizer
        )
        
        val packageManager = context.packageManager
        
        // Try each known equalizer package
        for (packageName in equalizerPackages) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Log.d(TAG, "Opened equalizer app: $packageName")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching equalizer package: $packageName", e)
                // Continue trying other packages
            }
        }
        
        // Try a more generic approach - open sound settings
        try {
            val settingsIntent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (isIntentResolvable(context, settingsIntent)) {
                context.startActivity(settingsIntent)
                Log.d(TAG, "Opened sound settings")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening sound settings", e)
        }
        
        return false
    }
    
    /**
     * Check if an intent can be resolved to an activity
     */
    private fun isIntentResolvable(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo != null
    }
} 