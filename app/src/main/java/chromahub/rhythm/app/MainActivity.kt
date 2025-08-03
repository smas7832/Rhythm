package chromahub.rhythm.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import chromahub.rhythm.app.R
import chromahub.rhythm.app.ui.components.RhythmIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WavingHand // New import for Welcome screen icon
import androidx.compose.material.icons.filled.DarkMode // New import for Dark Mode icon
import chromahub.rhythm.app.ui.navigation.RhythmNavigation
import chromahub.rhythm.app.ui.theme.RhythmTheme
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel // Import AppUpdaterViewModel
import chromahub.rhythm.app.util.CrashReporter // Import CrashReporter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.util.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner // Corrected import for LocalLifecycleOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.animation.Crossfade
import androidx.compose.ui.text.font.FontWeight
import chromahub.rhythm.app.viewmodel.MusicViewModel
//import chromahub.rhythm.app.ui.annotations.RhythmAnimation
import android.provider.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import chromahub.rhythm.app.data.AppSettings // Import AppSettings
import java.util.Locale // Import Locale
import androidx.compose.material.icons.filled.Public // Import Public icon
import androidx.compose.material.icons.filled.BugReport // Import BugReport icon
import androidx.compose.material.icons.filled.Check // Import Check icon
import androidx.compose.material.icons.filled.MusicNote // Import MusicNote icon
import androidx.compose.material.icons.filled.Palette // Import Palette icon
import androidx.compose.material.icons.filled.Security // New import for Security icon
import androidx.compose.material.icons.filled.SystemUpdate // Import SystemUpdate icon
import androidx.compose.material.icons.filled.KeyboardArrowDown // Import KeyboardArrowDown icon
import androidx.compose.material.icons.filled.FormatListNumbered // Import FormatListNumbered icon
import androidx.compose.material.icons.filled.SortByAlpha // Import SortByAlpha icon
import androidx.compose.material.icons.filled.AccessTime // Import AccessTime icon
import androidx.compose.material.icons.filled.GridView // Import GridView icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.rememberCoroutineScope
import chromahub.rhythm.app.ui.components.M3LinearLoader // Import M3LinearLoader
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader // Import M3FourColorCircularLoader
import androidx.compose.ui.platform.LocalHapticFeedback // Import LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType // Import HapticFeedbackType
import androidx.compose.material3.ButtonDefaults // Import ButtonDefaults
import chromahub.rhythm.app.ui.screens.SplashScreen
import chromahub.rhythm.app.ui.screens.PermissionHandler
import chromahub.rhythm.app.ui.screens.BetaProgramPopup
import chromahub.rhythm.app.ui.screens.OnboardingScreen
import chromahub.rhythm.app.ui.screens.onboarding.OnboardingStep
import chromahub.rhythm.app.ui.screens.onboarding.PermissionScreenState

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val musicViewModel: MusicViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val appUpdaterViewModel: AppUpdaterViewModel by viewModels() // Inject AppUpdaterViewModel
    private lateinit var appSettings: AppSettings // Declare AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appSettings = AppSettings.getInstance(applicationContext) // Initialize AppSettings
        
        // Initialize NetworkClient with AppSettings for dynamic API keys
        chromahub.rhythm.app.network.NetworkClient.initialize(appSettings)

        // Initialize CrashReporter
        CrashReporter.init(application)
        
        // We'll delay intent handling until after initialization
        val startupIntent = intent
        
        setContent {
            val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
            val darkMode by themeViewModel.darkMode.collectAsState()
            val useDynamicColors by themeViewModel.useDynamicColors.collectAsState()
            
            // Determine the theme based on settings
            val isDarkTheme = if (useSystemTheme) {
                // Use system default
                androidx.compose.foundation.isSystemInDarkTheme()
            } else {
                // Use app setting
                darkMode
            }
            
            RhythmTheme(
                darkTheme = isDarkTheme,
                // Use dynamic colors (Monet) when system theme is enabled
                dynamicColor = useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Show splash screen first, then transition to the app
                    var showSplash by remember { mutableStateOf(true) }
                    val hasShownBetaPopup by appSettings.hasShownBetaPopup.collectAsState()
                    var showBetaPopup by remember { mutableStateOf(false) }
                    val currentAppVersion by appUpdaterViewModel.currentVersion.collectAsState() // Observe current version
                    val updateChannel by appUpdaterViewModel.updateChannel.collectAsState() // Observe update channel

                    // State for permission handling and app initialization
                    var shouldShowSettingsRedirect by remember { mutableStateOf(false) }
                    var isLoading by remember { mutableStateOf(true) } // Start as true to show loading during splash/initial checks
                    var isInitializingApp by remember { mutableStateOf(false) }
                    val lastCrashLog by appSettings.lastCrashLog.collectAsState() // Observe last crash log

                    // Handle splash screen completion and post-init tasks
                    LaunchedEffect(Unit) {
                        // Wait for splash screen to complete (it will call onMediaScanComplete when ready)
                        // This LaunchedEffect will handle post-splash tasks like beta popup and intent handling
                    }
                    
                    // Function to handle splash completion
                    fun onSplashComplete() {
                        showSplash = false
                        isLoading = false // Stop initial loading after splash

                        // Show beta popup if it hasn't been shown before AND the current version is a pre-release
                        if (!hasShownBetaPopup && currentAppVersion.isPreRelease) {
                            showBetaPopup = true
                        }

                        // Check for previous crash logs
                        lastCrashLog?.let {
                            // CrashActivity is now responsible for showing the dialog
                        }
                        
                        // Handle intent after splash screen disappears and app is initialized
                        if (startupIntent?.action == Intent.ACTION_VIEW && startupIntent.data != null) {
                            // Small delay to ensure view models are ready, then handle intent
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(500)
                                handleIntent(startupIntent)
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = !showSplash,
                            enter = fadeIn(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)) + 
                                   scaleIn(initialScale = 0.92f, animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)),
                        ) {
                            PermissionHandler(
                                onPermissionsGranted = {
                                    RhythmNavigation(
                                        viewModel = musicViewModel,
                                        themeViewModel = themeViewModel,
                                        appSettings = appSettings
                                    )
                                },
                                themeViewModel = themeViewModel,
                                appSettings = appSettings,
                                isLoading = isLoading,
                                isInitializingApp = isInitializingApp,
                                onSetIsLoading = { isLoading = it },
                                onSetIsInitializingApp = { isInitializingApp = it },
                                musicViewModel = musicViewModel
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = showSplash,
                            exit = fadeOut(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseInCubic))
                        ) {
                            SplashScreen(
                                musicViewModel = musicViewModel,
                                onMediaScanComplete = { onSplashComplete() }
                            )
                        }

                        // Beta Program Popup
                        AnimatedVisibility(
                            visible = showBetaPopup,
                            enter = fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = fadeOut() + scaleOut(targetScale = 0.8f)
                        ) {
                            BetaProgramPopup(onDismiss = {
                                showBetaPopup = false
                                appSettings.setHasShownBetaPopup(true)
                            })
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle the new intent
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        
        Log.d(TAG, "Handling intent: ${intent.action}, data: ${intent.data}")
        
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                // Handle external audio file
                intent.data?.let { uri ->
                    Log.d(TAG, "Received ACTION_VIEW intent with URI: $uri")
                    
                    // Check if the URI is a content URI
                    if (uri.scheme == "content" || uri.scheme == "file") {
                        handleExternalAudioFile(uri)
                    }
                }
            }
        }
    }
    
    private fun handleExternalAudioFile(uri: Uri) {
        Log.d(TAG, "Handling external audio file: $uri")
        
        // Check if the URI is an audio file
        val mimeType = MediaUtils.getMimeType(this, uri)
        
        if (mimeType?.startsWith("audio/") == true || uri.toString().endsWith(".mp3", ignoreCase = true) ||
            uri.toString().endsWith(".m4a", ignoreCase = true) || uri.toString().endsWith(".wav", ignoreCase = true) ||
            uri.toString().endsWith(".ogg", ignoreCase = true) || uri.toString().endsWith(".flac", ignoreCase = true)) {
            
            Log.d(TAG, "File is recognized as audio with mime type: $mimeType")
            
            // Start the service to ensure it's running
            val serviceIntent = Intent(this, chromahub.rhythm.app.service.MediaPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
            // Extract metadata from the audio file
            lifecycleScope.launch {
                try {
                    // Extract metadata on a background thread
                    val song = withContext(Dispatchers.IO) {
                        MediaUtils.extractMetadataFromUri(this@MainActivity, uri)
                    }
                    
                    Log.d(TAG, "Extracted song metadata: ${song.title} by ${song.artist} from ${song.album}")
                    
                    // Make sure the MediaPlaybackService is connected before trying to play the file
                    if (!musicViewModel.isServiceConnected()) {
                        // Wait for the service to connect
                        musicViewModel.connectToMediaService()
                        delay(1500)  // Give service time to connect
                    }
                    
                    // Play the external file
                    musicViewModel.playExternalAudioFile(song)
                    
                    // If playback doesn't start, try sending the intent directly to the service as a fallback
                    if (!musicViewModel.isPlaying()) {
                        delay(2000)
                        if (!musicViewModel.isPlaying()) {
                            Log.d(TAG, "Using direct service intent as fallback")
                            val playIntent = Intent(this@MainActivity, chromahub.rhythm.app.service.MediaPlaybackService::class.java)
                            playIntent.action = chromahub.rhythm.app.service.MediaPlaybackService.ACTION_PLAY_EXTERNAL_FILE
                            playIntent.data = uri
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(playIntent)
                            } else {
                                startService(playIntent)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing external audio file", e)
                    Toast.makeText(this@MainActivity, "Error playing audio file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e(TAG, "File is not recognized as audio. Mime type: $mimeType")
            Toast.makeText(this, "Unsupported file format", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
    
    // Helper function to get step name for accessibility
    private fun getStepName(step: OnboardingStep): String {
        return when (step) {
            OnboardingStep.WELCOME -> "Welcome"
            OnboardingStep.PERMISSIONS -> "Permissions"
            OnboardingStep.AUDIO_PLAYBACK -> "Audio & Playback"
            OnboardingStep.THEMING -> "Theming"
            OnboardingStep.LIBRARY_SETUP -> "Library Setup"
            OnboardingStep.UPDATER -> "Updates"
            OnboardingStep.COMPLETE -> "Complete"
        }
    }
}
