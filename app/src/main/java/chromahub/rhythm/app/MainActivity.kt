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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import chromahub.rhythm.app.ui.components.M3CircularLoader
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader
import chromahub.rhythm.app.ui.components.M3PulseLoader
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.util.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner // Corrected import for LocalLifecycleOwner
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

enum class OnboardingStep {
    WELCOME,
    PERMISSIONS,
    THEMING,
    UPDATER, // New state for app updater settings
    COMPLETE
}

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val musicViewModel: MusicViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
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
                    
                    // Animate the splash screen out after a delay
                    LaunchedEffect(Unit) {
                        delay(3000) // Increased delay for a longer gap between splash and onboarding
                        showSplash = false
                        
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
                                        appSettings = appSettings // Pass appSettings here
                                    )
                                },
                                themeViewModel = themeViewModel, // Pass themeViewModel here
                                appSettings = appSettings // Pass appSettings here
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = showSplash,
                            exit = fadeOut(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseInCubic))
                        ) {
                            SplashScreen()
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
    themeViewModel: ThemeViewModel, // Add this parameter
    appSettings: AppSettings // Add this parameter
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) } // Overall loading state for the PermissionHandler (primarily for permission dialogs)
    var isInitializingApp by remember { mutableStateOf(false) } // New loading state for post-onboarding initialization
    var shouldShowSettingsRedirect by remember { mutableStateOf(false) }
    val onboardingCompleted by appSettings.onboardingCompleted.collectAsState() // Read onboarding status

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
    
    // This state will trigger a re-evaluation of permissions and potentially a new request
    var permissionCheckTrigger by remember { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

        // Function to check and update permission status
        val checkPermissionsAndProceed: suspend () -> Unit = {
            val allStoragePermissionsGranted = permissionsState.permissions
                .filter { it.permission in storagePermissions }
                .all { it.status.isGranted }

            if (allStoragePermissionsGranted) {
                shouldShowSettingsRedirect = false
                if (!onboardingCompleted) {
                    currentOnboardingStep = OnboardingStep.THEMING
                } else {
                    // Onboarding is complete, transition to app with a loading screen
                    currentOnboardingStep = OnboardingStep.COMPLETE
                    isInitializingApp = true // Start app initialization loading
                }
                // Initialize media service
                val intent = Intent(context, chromahub.rhythm.app.service.MediaPlaybackService::class.java)
                intent.action = chromahub.rhythm.app.service.MediaPlaybackService.ACTION_INIT_SERVICE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                delay(1000) // Give service time to initialize
                isLoading = false // Stop general loading
                isInitializingApp = false // Stop app initialization loading
            } else {
                // Permissions are not granted. Stay on permissions screen.
                val ungrantedStoragePermissions = permissionsState.permissions
                    .filter { it.permission in storagePermissions && !it.status.isGranted }

                shouldShowSettingsRedirect = ungrantedStoragePermissions.isNotEmpty() &&
                                             ungrantedStoragePermissions.all { !it.status.shouldShowRationale }
                currentOnboardingStep = OnboardingStep.PERMISSIONS // Explicitly stay on permissions
                isLoading = false // Stop loading, show permission screen again
                isInitializingApp = false // Ensure this is false if we're on permissions
            }
        }

        // LaunchedEffect to trigger initial permission request or re-request
        LaunchedEffect(permissionCheckTrigger, onboardingCompleted) {
            // This effect is triggered when the user explicitly requests permissions or on initial load
            if (currentOnboardingStep == OnboardingStep.PERMISSIONS) {
                val allStoragePermissionsGranted = permissionsState.permissions
                    .filter { it.permission in storagePermissions }
                    .all { it.status.isGranted }

                if (!allStoragePermissionsGranted) {
                    // Only launch request if permissions are not granted
                    isLoading = true // Show loader while permission dialog is active
                    permissionsState.launchMultiplePermissionRequest()
                    isLoading = false // Immediately set to false after launching request, as the dialog is now external
                } else {
                    // If permissions are already granted (e.g., on initial load or after a previous request)
                    checkPermissionsAndProceed()
                }
            } else if (onboardingCompleted && currentOnboardingStep != OnboardingStep.COMPLETE) {
                // If onboarding is completed, and we are not already in COMPLETE state,
                // check permissions and proceed to app if granted.
                isLoading = true // Start loading when checking permissions on resume for completed onboarding
                checkPermissionsAndProceed()
            }
        }

        // LaunchedEffect to observe lifecycle and re-check permissions on resume
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
                override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                    super.onResume(owner)
                    // When activity resumes, if we are on the permissions step, re-check
                    if (currentOnboardingStep == OnboardingStep.PERMISSIONS || (onboardingCompleted && currentOnboardingStep != OnboardingStep.COMPLETE)) {
                        // Small delay to ensure system permission dialogs are fully dismissed
                        // and permission state is updated by the system.
                        launch {
                            delay(500)
                            isLoading = true // Start loading when re-checking permissions on resume
                            checkPermissionsAndProceed()
                        }
                    }
                }
            })
        }

        // New LaunchedEffect to specifically observe permission state changes and move to theming/updater/complete
        LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
            // This effect runs when permission state changes (e.g., after user interacts with dialog)
            if (currentOnboardingStep == OnboardingStep.PERMISSIONS) {
                isLoading = false // Explicitly stop loading as soon as permission dialog is dismissed
                // Re-evaluate permissions and update UI state
                checkPermissionsAndProceed()
            }
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
                visible = (isLoading && currentOnboardingStep != OnboardingStep.COMPLETE) || isInitializingApp, // Show loading if general loading OR app initializing
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
                                // When "Grant Permissions" is clicked, set loading to true and trigger permission request
                                isLoading = true
                                permissionCheckTrigger++
                            }
                            OnboardingStep.THEMING -> currentOnboardingStep = OnboardingStep.UPDATER // Move to updater
                            OnboardingStep.UPDATER -> {
                                appSettings.setOnboardingCompleted(true) // Mark onboarding as complete
                                currentOnboardingStep = OnboardingStep.COMPLETE // Move to complete
                                // The checkPermissionsAndProceed will handle setting isInitializingApp = true
                                // and then false after service init.
                            }
                            OnboardingStep.COMPLETE -> { /* Should not happen */ }
                        }
                    },
                    onRequestAgain = {
                        // This is for "Open App Settings" or re-requesting permissions
                        isLoading = true // Set loading to true when requesting again
                        permissionCheckTrigger++ // Trigger a new permission check
                    },
                    shouldShowSettingsRedirect = shouldShowSettingsRedirect,
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
        shouldShowSettingsRedirect: Boolean,
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
                                    onRequestAgain = onRequestAgain,
                                    shouldShowSettingsRedirect = shouldShowSettingsRedirect,
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
        onRequestAgain: () -> Unit,
        shouldShowSettingsRedirect: Boolean,
        isButtonLoading: Boolean // Receive this from OnboardingScreen
    ) {
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Permissions Required",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
            )
    
            Text(
                text = "Grant Permissions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
    
            Text(
                text = "Rhythm needs a few permissions to access your music and connect to devices.",
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
                    if (shouldShowSettingsRedirect) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        onRequestAgain()
                    }
                },
                enabled = !isButtonLoading, // Use the passed-down state to enable/disable
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
                            text = if (shouldShowSettingsRedirect) "Open Settings" else "Grant Access",
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
    
            if (shouldShowSettingsRedirect) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Permissions permanently denied. Please enable them in app settings.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
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
