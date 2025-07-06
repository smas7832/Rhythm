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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.rememberCoroutineScope
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
    
    // Helper function to get step name for accessibility
    private fun getStepName(step: OnboardingStep): String {
        return when (step) {
            OnboardingStep.WELCOME -> "Welcome"
            OnboardingStep.PERMISSIONS -> "Permissions"
            OnboardingStep.THEMING -> "Theming"
            OnboardingStep.UPDATER -> "Updates"
            OnboardingStep.COMPLETE -> "Complete"
        }
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
        // Android 13+ requires granular media permissions
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        // Android 12 and below use legacy storage permissions
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
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
    
    // Only request essential permissions that are actually needed
    val essentialPermissions = storagePermissions + bluetoothPermissions
    
    val permissionsState = rememberMultiplePermissionsState(essentialPermissions)
    
    val lifecycleOwner = LocalLifecycleOwner.current

    // Centralized function to evaluate permission status and update onboarding step
    val evaluatePermissionsAndSetStep: suspend () -> Unit = {
        // Check if we have the essential storage permissions
        val hasStoragePermissions = storagePermissions.all { permission ->
            permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
        }

        if (hasStoragePermissions) {
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
            // Check denied permissions state
            val deniedStoragePermissions = storagePermissions.filter { permission ->
                permissionsState.permissions.find { it.permission == permission }?.status?.isGranted != true
            }

            val shouldShowRationaleForAny = deniedStoragePermissions.any { permission ->
                permissionsState.permissions.find { it.permission == permission }?.status?.shouldShowRationale == true
            }
            
            val allDeniedPermanently = deniedStoragePermissions.isNotEmpty() && deniedStoragePermissions.all { permission ->
                val permissionState = permissionsState.permissions.find { it.permission == permission }
                permissionState?.status?.shouldShowRationale == false && permissionRequestLaunched
            }

            // Determine the correct permission screen state
            if (!permissionRequestLaunched && !onboardingCompleted) {
                // First time asking for permissions
                permissionScreenState = PermissionScreenState.PermissionsRequired
            } else if (shouldShowRationaleForAny) {
                // User denied but we can show rationale
                permissionScreenState = PermissionScreenState.ShowRationale
            } else if (allDeniedPermanently) {
                // User denied permanently
                permissionScreenState = PermissionScreenState.RedirectToSettings
            } else {
                // Default state
                permissionScreenState = PermissionScreenState.PermissionsRequired
            }
            currentOnboardingStep = OnboardingStep.PERMISSIONS
            onSetIsLoading(false) // Always set loading to false after evaluation
        }
    }

    // Effect to trigger permission request when entering PERMISSIONS step
    LaunchedEffect(currentOnboardingStep, permissionScreenState) {
        if (currentOnboardingStep == OnboardingStep.PERMISSIONS) {
            // Only set loading and launch request if we are in PermissionsRequired state
            if (permissionScreenState == PermissionScreenState.PermissionsRequired && !permissionRequestLaunched) {
                onSetIsLoading(true) // Show loader on the button
                try {
                    permissionsState.launchMultiplePermissionRequest()
                    permissionRequestLaunched = true
                } catch (e: Exception) {
                    // Handle permission request failure
                    onSetIsLoading(false)
                    permissionScreenState = PermissionScreenState.ShowRationale
                }
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
        currentStep: OnboardingStep,
        onNextStep: () -> Unit,
        onRequestAgain: () -> Unit,
        permissionScreenState: PermissionScreenState,
        isParentLoading: Boolean,
        themeViewModel: ThemeViewModel,
        appSettings: AppSettings
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        
        // Get current step index for progress
        val stepIndex = when (currentStep) {
            OnboardingStep.WELCOME -> 0
            OnboardingStep.PERMISSIONS -> 1
            OnboardingStep.THEMING -> 2
            OnboardingStep.UPDATER -> 3
            OnboardingStep.COMPLETE -> 4
        }
        
        val totalSteps = 4 // Welcome, Permissions, Theming, Updater
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Enhanced header with progress
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        // App name and logo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.rhythm_logo),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Rhythm",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Progress indicator
                        OnboardingProgressIndicator(
                            currentStep = stepIndex,
                            totalSteps = totalSteps
                        )
                    }
                }
                
                // Enhanced main card with better animations
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Enhanced step transition with slide animations
                        Crossfade(
                            targetState = currentStep,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = androidx.compose.animation.core.EaseInOutCubic
                            ),
                            label = "onboardingStepTransition"
                        ) { step ->
                            when (step) {
                                OnboardingStep.WELCOME -> EnhancedWelcomeContent(onNextStep)
                                OnboardingStep.PERMISSIONS -> EnhancedPermissionContent(
                                    permissionScreenState = permissionScreenState,
                                    onGrantAccess = onNextStep,
                                    onOpenSettings = onRequestAgain,
                                    isButtonLoading = isParentLoading
                                )
                                OnboardingStep.THEMING -> EnhancedThemingContent(
                                    themeViewModel = themeViewModel,
                                    onNextStep = onNextStep
                                )
                                OnboardingStep.UPDATER -> EnhancedUpdaterContent(
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
    fun EnhancedWelcomeContent(onNextStep: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced icon with animation
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WavingHand,
                        contentDescription = "Welcome",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Welcome to Rhythm",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = "Your personalized music journey begins here.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Let's set up your perfect music experience in just a few simple steps.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Feature highlights
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureHighlight(
                    icon = Icons.Filled.MusicNote,
                    text = "Access your entire music library"
                )
                FeatureHighlight(
                    icon = RhythmIcons.Devices.Bluetooth,
                    text = "Connect to Bluetooth devices seamlessly"
                )
                FeatureHighlight(
                    icon = Icons.Filled.Palette,
                    text = "Customize your theme and appearance"
                )
            }
            
            FilledTonalButton(
                onClick = onNextStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Get Started", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = RhythmIcons.Forward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun FeatureHighlight(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        text: String
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun EnhancedPermissionContent(
        permissionScreenState: PermissionScreenState,
        onGrantAccess: () -> Unit,
        onOpenSettings: () -> Unit,
        isButtonLoading: Boolean
    ) {
        val context = LocalContext.current
        
        // Define permissions based on Android version within the composable
        val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        
        val essentialPermissions = storagePermissions + bluetoothPermissions
        val permissionsState = rememberMultiplePermissionsState(essentialPermissions)
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced icon with dynamic state
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            when (permissionScreenState) {
                                PermissionScreenState.RedirectToSettings -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RhythmIcons.Actions.List,
                        contentDescription = "Permissions Required",
                        tint = when (permissionScreenState) {
                            PermissionScreenState.RedirectToSettings -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
    
            Spacer(modifier = Modifier.height(24.dp))
    
            Text(
                text = when (permissionScreenState) {
                    PermissionScreenState.PermissionsRequired -> "Grant Permissions"
                    PermissionScreenState.ShowRationale -> "Permissions Needed"
                    PermissionScreenState.RedirectToSettings -> "Permissions Denied"
                    else -> "Permissions"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
    
            Text(
                text = when (permissionScreenState) {
                    PermissionScreenState.PermissionsRequired -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            "Rhythm needs specific permissions to access your music files and connect to devices."
                        } else {
                            "Rhythm needs a few permissions to provide you with the best music experience."
                        }
                    }
                    PermissionScreenState.ShowRationale -> "These permissions are essential for Rhythm to function properly. Please grant access to continue."
                    PermissionScreenState.RedirectToSettings -> "It looks like permissions were denied. Please enable them in app settings to enjoy Rhythm."
                    else -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 16.dp else 32.dp)
            )
            
            // Android 13+ permission notice
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissionScreenState == PermissionScreenState.PermissionsRequired) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Actions.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android 13+ uses granular media permissions for better privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
    
            // Enhanced permission explanation cards
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                EnhancedPermissionCard(
                    icon = RhythmIcons.Music.Audiotrack,
                    title = "Music Access",
                    description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        "Access your audio files to play your music collection"
                    } else {
                        "Read and play your music files from device storage"
                    },
                    isGranted = storagePermissions.all { permission ->
                        permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
                    }
                )
                EnhancedPermissionCard(
                    icon = RhythmIcons.Devices.Bluetooth,
                    title = "Bluetooth Access",
                    description = "Connect and control Bluetooth audio devices",
                    isGranted = bluetoothPermissions.all { permission ->
                        permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
                    }
                )
            }
    
            FilledTonalButton(
                onClick = {
                    when (permissionScreenState) {
                        PermissionScreenState.PermissionsRequired, PermissionScreenState.ShowRationale -> onGrantAccess()
                        PermissionScreenState.RedirectToSettings -> {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            onOpenSettings()
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
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (permissionScreenState) {
                                    PermissionScreenState.PermissionsRequired, PermissionScreenState.ShowRationale -> "Grant Access"
                                    PermissionScreenState.RedirectToSettings -> "Open Settings"
                                    else -> "Continue"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = RhythmIcons.Forward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun EnhancedPermissionCard(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        description: String,
        isGranted: Boolean = false
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isGranted) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Permission granted",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isGranted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Granted",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    @Composable
    fun EnhancedThemingContent(
        themeViewModel: ThemeViewModel,
        onNextStep: () -> Unit
    ) {
        val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
        val darkMode by themeViewModel.darkMode.collectAsState()
        val useDynamicColors by themeViewModel.useDynamicColors.collectAsState()
    
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced theming icon
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = "Theming Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Customize Your Experience",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = "Choose how Rhythm looks and feels to match your style.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
    
            // Enhanced theme options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // System theme toggle
                EnhancedThemeOption(
                    icon = RhythmIcons.Settings,
                    title = "System theme",
                    description = "Follow your device's theme settings",
                    isChecked = useSystemTheme,
                    onCheckedChange = { themeViewModel.setUseSystemTheme(it) }
                )
    
                // Dark mode toggle (only visible if not using system theme)
                AnimatedVisibility(
                    visible = !useSystemTheme,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    EnhancedThemeOption(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark mode",
                        description = "Use dark colors for better night viewing",
                        isChecked = darkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                }
                
                // Dynamic colors (Material You) - only show on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    EnhancedThemeOption(
                        icon = Icons.Filled.Palette,
                        title = "Dynamic colors",
                        description = "Use colors from your wallpaper",
                        isChecked = useDynamicColors,
                        onCheckedChange = { themeViewModel.setUseDynamicColors(it) }
                    )
                }
            }
            
            FilledTonalButton(
                onClick = onNextStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Continue", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = RhythmIcons.Forward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun EnhancedThemeOption(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        description: String,
        isChecked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onCheckedChange(!isChecked) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isChecked)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
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
    
    @Composable
    fun EnhancedUpdaterContent(
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
            // Enhanced updater icon
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RhythmIcons.Actions.Update,
                        contentDescription = "App Updates",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Stay Up to Date",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = "Configure how Rhythm checks for updates and new features.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Enhanced update options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // Auto check toggle
                EnhancedUpdateOption(
                    icon = RhythmIcons.Actions.Update,
                    title = "Auto check for updates",
                    description = "Automatically check for new versions",
                    isChecked = autoCheckForUpdates,
                    onCheckedChange = { appSettings.setAutoCheckForUpdates(it) }
                )

                // Update channel selection (only visible if auto check is enabled)
                AnimatedVisibility(
                    visible = autoCheckForUpdates,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    EnhancedUpdateChannelOption(
                        currentChannel = updateChannel,
                        onChannelChange = { appSettings.setUpdateChannel(it) }
                    )
                }
            }

            FilledTonalButton(
                onClick = onNextStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Complete Setup", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun EnhancedUpdateOption(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        description: String,
        isChecked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onCheckedChange(!isChecked) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isChecked)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
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

    @Composable
    fun EnhancedUpdateChannelOption(
        currentChannel: String,
        onChannelChange: (String) -> Unit
    ) {
        var showChannelDropdown by remember { mutableStateOf(false) }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { showChannelDropdown = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentChannel == "beta") Icons.Default.BugReport else Icons.Default.Public,
                        contentDescription = null,
                        tint = if (currentChannel == "beta") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Update Channel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current: ${currentChannel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Change channel",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            DropdownMenu(
                expanded = showChannelDropdown,
                onDismissRequest = { showChannelDropdown = false },
                shape = MaterialTheme.shapes.medium
            ) {
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Stable")
                                Text(
                                    "Stable releases only",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onChannelChange("stable")
                        showChannelDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Beta")
                                Text(
                                    "Get early access to new features",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onChannelChange("beta")
                        showChannelDropdown = false
                    }
                )
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

    @Composable
    fun OnboardingProgressIndicator(
        currentStep: Int,
        totalSteps: Int,
        modifier: Modifier = Modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            // Step indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSteps) { index ->
                    val isCompleted = index < currentStep
                    val isCurrent = index == currentStep
                    
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> MaterialTheme.colorScheme.primary
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    }
                                )
                                .animateContentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(6.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress text
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
