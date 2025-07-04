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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.animation.Crossfade
import androidx.compose.ui.text.font.FontWeight
import chromahub.rhythm.app.viewmodel.MusicViewModel
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
import chromahub.rhythm.app.data.AppSettings // Import AppSettings
import java.util.Locale // Import Locale
import androidx.compose.material.icons.filled.Public // Import Public icon
import androidx.compose.material.icons.filled.BugReport // Import BugReport icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader // Import M3FourColorCircularLoader

enum class OnboardingStep {
    WELCOME,
    PERMISSIONS,
    THEMING,
    UPDATER, // New state for app updater settings
    COMPLETE
}

sealed class PermissionScreenState {
    object Loading : PermissionScreenState()
    object PermissionsRequired : PermissionScreenState()
    object ShowRationale : PermissionScreenState()
    object RedirectToSettings : PermissionScreenState()
    object PermissionsGranted : PermissionScreenState()
}

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

                    // Animate the splash screen out after a delay
                    LaunchedEffect(Unit) {
                        delay(3000) // Increased delay for a longer gap between splash and onboarding
                        showSplash = false
                        isLoading = false // Stop initial loading after splash

                        // Show beta popup if it hasn't been shown before AND the current version is a pre-release
                        if (!hasShownBetaPopup && currentAppVersion.isPreRelease) {
                            showBetaPopup = true
                        }
                        
                        // Handle intent after splash screen disappears and app is initialized
                        if (startupIntent?.action == Intent.ACTION_VIEW && startupIntent.data != null) {
                            delay(500) // Small additional delay to ensure view models are ready
                            handleIntent(startupIntent)
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
                                onSetIsInitializingApp = { isInitializingApp = it }
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = showSplash,
                            exit = fadeOut(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseInCubic))
                        ) {
                            SplashScreen()
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
}

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f, // More subtle pulse
        targetValue = 1.02f, // More subtle pulse
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Slower, linear animation
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f, // More subtle fade
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Slower, linear animation
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .graphicsLayer {
                        this.alpha = alpha
                    },
                contentAlignment = Alignment.Center
            ) {
                // Use our custom logo drawable
                Image(
                    painter = painterResource(id = R.drawable.rhythm_splash_logo),
                    contentDescription = "Rhythm",
                    modifier = Modifier.size(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp)) // Add some space between logo and text

            Text(
                text = "Rhythm",
                style = MaterialTheme.typography.displaySmall, // Changed to displaySmall
                fontWeight = FontWeight.Bold, // Added bold font weight
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space between text and progress indicator

            LinearProgressIndicator(
                modifier = Modifier
                    .width(120.dp) // Fixed width for the progress indicator
                    .alpha(alpha), // Apply alpha animation
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: @Composable () -> Unit,
    themeViewModel: ThemeViewModel,
    appSettings: AppSettings,
    isLoading: Boolean, // Pass as parameter
    isInitializingApp: Boolean, // Pass as parameter
    onSetIsLoading: (Boolean) -> Unit, // Callback to update state
    onSetIsInitializingApp: (Boolean) -> Unit // Callback to update state
) {
    val context = LocalContext.current
    val onboardingCompleted by appSettings.onboardingCompleted.collectAsState()
    var permissionScreenState by remember { mutableStateOf<PermissionScreenState>(PermissionScreenState.Loading) }
    var permissionRequestLaunched by remember { mutableStateOf(false) } // New state to track if permission request has been launched

    var currentOnboardingStep by remember {
        mutableStateOf(
            if (onboardingCompleted) OnboardingStep.COMPLETE else OnboardingStep.WELCOME
        )
    }

    // For Android 13+, we need READ_MEDIA_AUDIO, for older versions we need READ_EXTERNAL_STORAGE
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
        )
    } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // Android 10 (Q) and below need WRITE_EXTERNAL_STORAGE
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }
    
    // Bluetooth permissions based on Android version
    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ requires BLUETOOTH_CONNECT and BLUETOOTH_SCAN
        listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        // Older versions use BLUETOOTH and BLUETOOTH_ADMIN
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
    
    // Combine all required permissions
    val allPermissions = storagePermissions + bluetoothPermissions
    
    val permissionsState = rememberMultiplePermissionsState(allPermissions)
    
    val lifecycleOwner = LocalLifecycleOwner.current

    // Centralized function to evaluate permission status and update onboarding step
    val evaluatePermissionsAndSetStep: suspend () -> Unit = {
        val allStoragePermissionsGranted = permissionsState.permissions
            .filter { it.permission in storagePermissions }
            .all { it.status.isGranted }

        if (allStoragePermissionsGranted) {
            permissionScreenState = PermissionScreenState.PermissionsGranted
            if (!onboardingCompleted) {
                currentOnboardingStep = OnboardingStep.THEMING
            } else {
                currentOnboardingStep = OnboardingStep.COMPLETE
                onSetIsInitializingApp(true) // Start app initialization
                val intent = Intent(context, chromahub.rhythm.app.service.MediaPlaybackService::class.java)
                intent.action = chromahub.rhythm.app.service.MediaPlaybackService.ACTION_INIT_SERVICE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                delay(1000) // Give service time to initialize
                onSetIsInitializingApp(false) // End app initialization
            }
            onSetIsLoading(false) // Always set loading to false after evaluation
        } else {
            // Permissions are NOT granted.
            // Permissions are NOT granted.
            val ungrantedStoragePermissions = permissionsState.permissions
                .filter { it.permission in storagePermissions && !it.status.isGranted }

            val shouldShowRationaleForAny = ungrantedStoragePermissions.any { it.status.shouldShowRationale }
            val allDeniedWithoutRationale = ungrantedStoragePermissions.isNotEmpty() && ungrantedStoragePermissions.all { !it.status.shouldShowRationale }

            // Determine the correct permission screen state
            if (!permissionRequestLaunched && !onboardingCompleted) {
                // This is the very first time the app is asking for permissions on a fresh install/first run.
                // The system permission dialog has not been shown yet.
                permissionScreenState = PermissionScreenState.PermissionsRequired
            } else if (shouldShowRationaleForAny) {
                // Permissions were denied, but not permanently. We should show rationale.
                permissionScreenState = PermissionScreenState.ShowRationale
            } else if (allDeniedWithoutRationale) {
                // Permissions were denied permanently (either by user checking "don't ask again" or revoking from settings).
                permissionScreenState = PermissionScreenState.RedirectToSettings
            } else {
                // Fallback for any other unhandled state, assume permissions are required
                // This might catch cases where permissions were revoked after onboarding, but not permanently.
                permissionScreenState = PermissionScreenState.PermissionsRequired
            }
            currentOnboardingStep = OnboardingStep.PERMISSIONS
            onSetIsLoading(false) // Always set loading to false after evaluation
        }
    }

    // Effect to trigger permission request when entering PERMISSIONS step
    LaunchedEffect(currentOnboardingStep, permissionScreenState) { // Add permissionScreenState as a key
        if (currentOnboardingStep == OnboardingStep.PERMISSIONS) {
            // Only set loading and launch request if we are in PermissionsRequired state
            if (permissionScreenState == PermissionScreenState.PermissionsRequired && !permissionRequestLaunched) {
                onSetIsLoading(true) // Show loader on the button
                permissionsState.launchMultiplePermissionRequest()
                permissionRequestLaunched = true
            } else if (permissionScreenState == PermissionScreenState.Loading) {
                // If still in loading, re-evaluate to determine the correct state
                evaluatePermissionsAndSetStep()
            }
        }
    }

    // Effect to re-evaluate permissions after a request or on resume
    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
        // This effect runs when permission state changes (e.g., after user interacts with dialog)
        // or when the app resumes.
        // Only re-evaluate if we are currently on the permissions step or if onboarding is complete
        // but permissions somehow became ungranted (e.g., user revoked from settings).
        if (currentOnboardingStep == OnboardingStep.PERMISSIONS || (onboardingCompleted && permissionScreenState != PermissionScreenState.PermissionsGranted)) {
            onSetIsLoading(true) // Start loading when re-checking permissions
            evaluatePermissionsAndSetStep()
        }
    }

    // Effect to observe lifecycle and re-check permissions on resume
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                super.onResume(owner)
                // When activity resumes, if we are on the permissions step, re-check
                if (currentOnboardingStep == OnboardingStep.PERMISSIONS || (onboardingCompleted && permissionScreenState != PermissionScreenState.PermissionsGranted)) {
                    launch {
                        delay(500) // Small delay to ensure system permission dialogs are fully dismissed
                        onSetIsLoading(true) // Start loading when re-checking permissions on resume
                        evaluatePermissionsAndSetStep()
                        permissionRequestLaunched = false // Reset for next time
                    }
                }
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = currentOnboardingStep == OnboardingStep.COMPLETE && !isInitializingApp, // Show app when complete AND not initializing
            enter = fadeIn(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)) +
                   slideInVertically(
                       initialOffsetY = { it / 3 },
                       animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)
                   )
        ) {
            onPermissionsGranted()
        }

        AnimatedVisibility(
            visible = isInitializingApp || (isLoading && currentOnboardingStep != OnboardingStep.COMPLETE && currentOnboardingStep != OnboardingStep.PERMISSIONS), // Show loading if app initializing, or general loading not on permission screen
            exit = fadeOut(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseInCubic))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                M3FourColorCircularLoader(
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = !isLoading && !isInitializingApp && currentOnboardingStep != OnboardingStep.COMPLETE, // Show onboarding if not loading AND not initializing AND not complete
            enter = fadeIn(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic)) +
                   scaleIn(initialScale = 0.95f, animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic))
        ) {
            OnboardingScreen(
                currentStep = currentOnboardingStep,
                onNextStep = {
                    when (currentOnboardingStep) {
                        OnboardingStep.WELCOME -> currentOnboardingStep = OnboardingStep.PERMISSIONS
                        OnboardingStep.PERMISSIONS -> {
                            // This branch is now only for the "Grant Access" button click
                            // which should trigger the permission request.
                            onSetIsLoading(true) // Set loading to true when requesting
                            permissionsState.launchMultiplePermissionRequest() // Directly launch request
                            permissionRequestLaunched = true // Mark as launched
                        }
                        OnboardingStep.THEMING -> currentOnboardingStep = OnboardingStep.UPDATER // Move to updater
                        OnboardingStep.UPDATER -> {
                            appSettings.setOnboardingCompleted(true) // Mark onboarding as complete
                            currentOnboardingStep = OnboardingStep.COMPLETE // Move to complete
                            // The evaluatePermissionsAndSetStep will handle setting isInitializingApp = true
                            // and then false after service init.
                        }
                        OnboardingStep.COMPLETE -> { /* Should not happen */ }
                    }
                },
                onRequestAgain = {
                    // This is for "Open App Settings" or re-requesting permissions
                    // This will be called when the user clicks "Grant Access" or "Open Settings"
                    // in the PermissionContent.
                    onSetIsLoading(true) // Set loading to true when requesting again
                    // The action (launching settings or re-requesting) will be handled inside PermissionContent
                    // based on permissionScreenState.
                    // No need to launchMultiplePermissionRequest here, as it's handled by the button click in PermissionContent
                },
                permissionScreenState = permissionScreenState, // Pass the state
                isParentLoading = isLoading,
                themeViewModel = themeViewModel,
                appSettings = appSettings // Pass appSettings to OnboardingScreen
            )
        }
    }
}
    
    @Composable
    fun OnboardingScreen(
        currentStep: OnboardingStep, // New parameter
        onNextStep: () -> Unit, // New parameter
        onRequestAgain: () -> Unit, // Keep this for permission re-request
        permissionScreenState: PermissionScreenState, // New parameter
        isParentLoading: Boolean,
        themeViewModel: ThemeViewModel, // New parameter
        appSettings: AppSettings // New parameter
    ) {
        val context = LocalContext.current
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // App name and logo above the card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rhythm_logo),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Rhythm",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh, // Use a higher surface variant for more contrast
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth(0.9f) // Make the card slightly wider
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Crossfade(targetState = currentStep, label = "onboardingStepTransition") { step ->
                            when (step) {
                                OnboardingStep.WELCOME -> WelcomeContent(onNextStep)
                                OnboardingStep.PERMISSIONS -> PermissionContent(
                                    permissionScreenState = permissionScreenState, // Pass the state
                                    onGrantAccess = onNextStep, // This will trigger the permission request in PermissionHandler
                                    onOpenSettings = onRequestAgain, // This will trigger opening settings
                                    isButtonLoading = isParentLoading // Pass isParentLoading directly
                                )
                                OnboardingStep.THEMING -> ThemingContent(
                                    themeViewModel = themeViewModel,
                                    onNextStep = onNextStep
                            )
                            OnboardingStep.UPDATER -> UpdaterContent(
                                appSettings = appSettings,
                                onNextStep = onNextStep
                            )
                            OnboardingStep.COMPLETE -> { /* Should not be visible here */ }
                        }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun WelcomeContent(onNext: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.WavingHand, // Reverted to WavingHand icon
                contentDescription = "Welcome",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
            )
            Text(
                text = "Welcome to Rhythm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Your personalized music journey begins here. Let's get started.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            FilledTonalButton(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Continue", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    
    @Composable
    fun PermissionContent(
        permissionScreenState: PermissionScreenState, // New parameter
        onGrantAccess: () -> Unit, // Callback for "Grant Access"
        onOpenSettings: () -> Unit, // Callback for "Open Settings"
        isButtonLoading: Boolean // Receive this from OnboardingScreen
    ) {
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = RhythmIcons.Actions.List,
                contentDescription = "Permissions Required",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
            )
    
            Text(
                text = when (permissionScreenState) {
                    PermissionScreenState.PermissionsRequired -> "Grant Permissions"
                    PermissionScreenState.ShowRationale -> "Permissions Needed"
                    PermissionScreenState.RedirectToSettings -> "Permissions Denied"
                    else -> "Permissions" // Should not happen
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
    
            Text(
                text = when (permissionScreenState) {
                    PermissionScreenState.PermissionsRequired -> "Rhythm needs a few permissions to access your music and connect to devices."
                    PermissionScreenState.ShowRationale -> "Rhythm needs access to your music files and Bluetooth to function properly. Please grant the necessary permissions."
                    PermissionScreenState.RedirectToSettings -> "Permissions permanently denied. Please enable them in app settings to use Rhythm."
                    else -> "" // Should not happen
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
    
            PermissionExplanationRow(
                icon = RhythmIcons.Music.Audiotrack,
                description = "Access music files on your device."
            )
            Spacer(modifier = Modifier.height(12.dp))
            PermissionExplanationRow(
                icon = RhythmIcons.Devices.Bluetooth,
                description = "Detect Bluetooth audio devices."
            )
            Spacer(modifier = Modifier.height(24.dp))
    
            FilledTonalButton(
                onClick = {
                    when (permissionScreenState) {
                        PermissionScreenState.PermissionsRequired, PermissionScreenState.ShowRationale -> onGrantAccess()
                        PermissionScreenState.RedirectToSettings -> {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            onOpenSettings() // Call the callback to update loading state
                        }
                        else -> { /* Do nothing */ }
                    }
                },
                enabled = !isButtonLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Crossfade(targetState = isButtonLoading, label = "buttonLoading") { requesting ->
                    if (requesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = when (permissionScreenState) {
                                PermissionScreenState.PermissionsRequired, PermissionScreenState.ShowRationale -> "Grant Access"
                                PermissionScreenState.RedirectToSettings -> "Open Settings"
                                else -> "Continue"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
    
            if (isButtonLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
    
    @Composable
    fun ThemingContent(
        themeViewModel: ThemeViewModel,
        onNextStep: () -> Unit // To signal completion
    ) {
        val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
        val darkMode by themeViewModel.darkMode.collectAsState()
    
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = RhythmIcons.Settings, // A relevant icon for theming
                contentDescription = "Theming Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
            )
            Text(
                text = "Customize Your Theme",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Set up your preferred look and feel for Rhythm.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
    
            // Use system theme toggle
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { themeViewModel.setUseSystemTheme(!useSystemTheme) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (useSystemTheme)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Settings, // Reverted to Settings icon
                            contentDescription = null,
                            tint = if (useSystemTheme)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "System theme",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Match system theme or use app's default colors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
    
                    Switch(
                        checked = useSystemTheme,
                        onCheckedChange = { themeViewModel.setUseSystemTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
    
            // Dark mode toggle (only visible if not using system theme)
            AnimatedVisibility(
                visible = !useSystemTheme,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { themeViewModel.setDarkMode(!darkMode) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (darkMode)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DarkMode, // Changed to DarkMode icon
                                contentDescription = null,
                                tint = if (darkMode)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
    
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Dark mode",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Enable dark mode for a darker interface.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
    
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { themeViewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
    
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(
                onClick = onNextStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Next", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    
    @Composable
    fun UpdaterContent(
        appSettings: AppSettings,
        onNextStep: () -> Unit
    ) {
        val autoCheckForUpdates by appSettings.autoCheckForUpdates.collectAsState()
        val updateChannel by appSettings.updateChannel.collectAsState()
        var showChannelDropdown by remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = RhythmIcons.Actions.Update, // Using the new Update icon from Actions
                contentDescription = "App Updates",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
            )
            Text(
                text = "App Updates Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Manage how Rhythm checks for app updates from GitHub.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { appSettings.setAutoCheckForUpdates(!autoCheckForUpdates) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (autoCheckForUpdates)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Actions.Update, // Using the new Update icon from Actions
                            contentDescription = null,
                            tint = if (autoCheckForUpdates)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Auto check",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically check for new app versions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = autoCheckForUpdates,
                        onCheckedChange = { appSettings.setAutoCheckForUpdates(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = autoCheckForUpdates,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SettingsClickableItem(
                        title = "Update Channel",
                        description = "Current: ${updateChannel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}",
                        icon = if (updateChannel == "beta") Icons.Default.BugReport else Icons.Default.Public,
                        iconTint = if (updateChannel == "beta") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        onClick = { showChannelDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showChannelDropdown,
                        onDismissRequest = { showChannelDropdown = false },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd) // Align to the end of the clickable item
                            .width(150.dp) // Adjust width as needed
                    ) {
                        DropdownMenuItem(
                            text = { Text("Stable") },
                            onClick = {
                                appSettings.setUpdateChannel("stable")
                                showChannelDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Beta") },
                            onClick = {
                                appSettings.setUpdateChannel("beta")
                                showChannelDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(
                onClick = onNextStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Finish", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    
    @Composable
    fun PermissionExplanationRow(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    fun SettingsClickableItem(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
        onClick: () -> Unit
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun BetaProgramPopup(onDismiss: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss), // Dismiss on outside click
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
                    .clickable(enabled = false) { /* Prevent dismissal when clicking inside card */ },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Beta Program Warning",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Welcome to the Beta Program!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "You are currently using the beta release of Rhythm. This version is still in development and may contain bugs, incomplete features, or unexpected behavior. Your feedback is highly appreciated ;)",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Got It!", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
