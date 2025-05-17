package chromahub.rhythm.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
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

class MainActivity : ComponentActivity() {
    private val musicViewModel: MusicViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
            val darkMode by themeViewModel.darkMode.collectAsState()
            
            // Determine the theme based on settings
            val isDarkTheme = if (useSystemTheme) {
                // Use system default
                androidx.compose.foundation.isSystemInDarkTheme()
            } else {
                // Use app setting
                darkMode
            }
            
            RhythmTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: @Composable () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    
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
    
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
        // Add a small delay to show loading indicator
        delay(500)
        isLoading = false
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Check if all storage permissions are granted (essential)
        val storagePermissionsGranted = permissionsState.permissions
            .filter { it.permission in storagePermissions }
            .all { it.status.isGranted }
        
        // Check if any Bluetooth permissions are granted (optional but preferred)
        val bluetoothPermissionsGranted = permissionsState.permissions
            .filter { it.permission in bluetoothPermissions }
            .any { it.status.isGranted }
        
        if (storagePermissionsGranted) {
            // We can proceed even without Bluetooth permissions
            onPermissionsGranted()
        } else {
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
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Storage permission is required to access music files on your device. " +
                           "Bluetooth permissions are needed to detect and use Bluetooth audio devices. " +
                           "Please grant the permissions to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                TextButton(
                    onClick = onRequestAgain,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 16.dp)
                ) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}