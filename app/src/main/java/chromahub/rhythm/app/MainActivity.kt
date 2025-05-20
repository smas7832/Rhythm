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
import chromahub.rhythm.app.ui.navigation.RhythmNavigation
import chromahub.rhythm.app.ui.theme.RhythmTheme
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
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
    var isLoading by remember { mutableStateOf(true) }
    var permissionsChecked by remember { mutableStateOf(false) }
    
    // For Android 13+, we need READ_MEDIA_AUDIO, for older versions we need READ_EXTERNAL_STORAGE
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
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
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
        // Add a small delay to show loading indicator
        delay(2500)
        isLoading = false
        permissionsChecked = true
        
        // If permissions are granted, make sure we initialize media playback service
        if (permissionsState.permissions
            .filter { it.permission in storagePermissions }
            .all { it.status.isGranted }) {
            
            // Initialize the media service to ensure it's ready when needed
            val intent = Intent(context, chromahub.rhythm.app.service.MediaPlaybackService::class.java)
            intent.action = chromahub.rhythm.app.service.MediaPlaybackService.ACTION_INIT_SERVICE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            // Give the service additional time to fully initialize
            delay(1000)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content that appears when permissions are granted
        AnimatedVisibility(
            visible = permissionsChecked && permissionsState.permissions
                .filter { it.permission in storagePermissions }
                .all { it.status.isGranted },
            enter = fadeIn(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)) + 
                   slideInVertically(
                       initialOffsetY = { it / 3 },
                       animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)
                   )
        ) {
            onPermissionsGranted()
        }
        
        // Loading indicator
        AnimatedVisibility(
            visible = isLoading,
            exit = fadeOut(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseInCubic))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Updated to use the new Material 3 circular loader with four colors
                M3FourColorCircularLoader(
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        
        // Permission denied screen
        AnimatedVisibility(
            visible = !isLoading && permissionsChecked && !permissionsState.permissions
                .filter { it.permission in storagePermissions }
                .all { it.status.isGranted },
            enter = fadeIn(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic)) +
                   scaleIn(initialScale = 0.95f, animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic))
        ) {
            PermissionDeniedScreen(
                onRequestAgain = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }
    }
}

@Composable
fun PermissionDeniedScreen(
    onRequestAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rhythm_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Storage Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Storage permission is required to access music files on your device. " +
                           "Bluetooth permissions are needed to detect and use Bluetooth audio devices.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Surface(
                    onClick = onRequestAgain,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Grant Permissions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}