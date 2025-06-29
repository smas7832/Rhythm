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
import chromahub.rhythm.app.ui.navigation.RhythmNavigation
import chromahub.rhythm.app.ui.theme.RhythmTheme
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale // Corrected import location
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
import android.provider.Settings // Corrected import location

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val musicViewModel: MusicViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
                        delay(2000) // Reduced delay for faster startup with external files
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
                                        themeViewModel = themeViewModel
                                    )
                                }
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
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.EaseInOutQuad),
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
        
        Text(
            text = "Rhythm",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(alpha)
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) } // Overall loading state for the PermissionHandler
    var shouldShowSettingsRedirect by remember { mutableStateOf(false) }

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

    LaunchedEffect(permissionCheckTrigger) {
        isLoading = true // Start loading whenever a check is triggered

        // Launch permission request. This is a suspend function and will wait for user interaction.
        permissionsState.launchMultiplePermissionRequest()

        // After the request returns (user has interacted with dialog or it didn't show):
        val allStoragePermissionsGranted = permissionsState.permissions
            .filter { it.permission in storagePermissions }
            .all { it.status.isGranted }

        if (allStoragePermissionsGranted) {
            shouldShowSettingsRedirect = false
            // Initialize media service
            val intent = Intent(context, chromahub.rhythm.app.service.MediaPlaybackService::class.java)
            intent.action = chromahub.rhythm.app.service.MediaPlaybackService.ACTION_INIT_SERVICE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            delay(1000) // Give service time to initialize
        } else {
            // Permissions are not granted. Determine if redirect to settings is needed.
            shouldShowSettingsRedirect = permissionsState.permissions
                .filter { it.permission in storagePermissions }
                .any { !it.status.isGranted && !it.status.shouldShowRationale }
        }
        isLoading = false // Stop loading after all checks and actions are done
    }

    // Trigger initial permission check on first composition
    LaunchedEffect(Unit) {
        permissionCheckTrigger++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isLoading && permissionsState.allPermissionsGranted,
            enter = fadeIn(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)) +
                   slideInVertically(
                       initialOffsetY = { it / 3 },
                       animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)
                   )
        ) {
            onPermissionsGranted()
        }

        AnimatedVisibility(
            visible = isLoading,
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
            visible = !isLoading && !permissionsState.allPermissionsGranted,
            enter = fadeIn(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic)) +
                   scaleIn(initialScale = 0.95f, animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic))
        ) {
            PermissionDeniedScreen(
                onRequestAgain = {
                    permissionCheckTrigger++ // Trigger a new permission check
                },
                shouldShowSettingsRedirect = shouldShowSettingsRedirect,
                isParentLoading = isLoading
            )
        }
    }
}

@Composable
fun PermissionDeniedScreen(
    onRequestAgain: () -> Unit,
    shouldShowSettingsRedirect: Boolean,
    isParentLoading: Boolean // New parameter
) {
    val context = LocalContext.current
    var isButtonLoading by remember { mutableStateOf(false) } // Local state for button feedback

    LaunchedEffect(isParentLoading) {
        if (!isParentLoading) {
            isButtonLoading = false // Reset button loading when parent loading finishes
        }
    }
    
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
                    Icon(
                        imageVector = Icons.Filled.Warning, // A more generic "info" or "alert" icon
                        contentDescription = "Permissions Required",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "To provide you with the best music experience, Rhythm needs access to certain permissions.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Permission explanations with icons
                    PermissionExplanationRow(
                        icon = RhythmIcons.Music.Audiotrack,
                        description = "Access your music files on this device to play your favorite songs."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionExplanationRow(
                        icon = RhythmIcons.Devices.Bluetooth,
                        description = "Detect Bluetooth audio devices for playback and control."
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
                                isButtonLoading = true // Start button loading
                                onRequestAgain()
                            }
                        },
                        enabled = !isButtonLoading, // Disable button while local loading
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
                                    text = if (shouldShowSettingsRedirect) "Open App Settings" else "Grant Permissions",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                    
                    if (isButtonLoading) { // Show linear progress only when button is loading
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }

                    if (shouldShowSettingsRedirect) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "It looks like you've permanently denied some permissions. Please enable them manually in app settings.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
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
