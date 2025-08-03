package chromahub.rhythm.app.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader
import chromahub.rhythm.app.ui.screens.onboarding.OnboardingStep
import chromahub.rhythm.app.ui.screens.onboarding.PermissionScreenState
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: @Composable () -> Unit,
    themeViewModel: ThemeViewModel,
    appSettings: AppSettings,
    isLoading: Boolean, // Pass as parameter
    isInitializingApp: Boolean, // Pass as parameter
    onSetIsLoading: (Boolean) -> Unit, // Callback to update state
    onSetIsInitializingApp: (Boolean) -> Unit, // Callback to update state
    musicViewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onboardingCompleted by appSettings.onboardingCompleted.collectAsState()
    var permissionScreenState by remember { mutableStateOf<PermissionScreenState>(PermissionScreenState.Loading) }
    var permissionRequestLaunched by remember { mutableStateOf(false) } // New state to track if permission request has been launched
    var showMediaScanLoader by remember { mutableStateOf(false) } // New state for media scan loader

    var currentOnboardingStep by remember {
        mutableStateOf(
            if (onboardingCompleted) OnboardingStep.COMPLETE else OnboardingStep.WELCOME
        )
    }

    // For Android 14+, we support partial photo access, Android 13+ needs READ_MEDIA_AUDIO, older versions need READ_EXTERNAL_STORAGE
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14+ supports partial photo/media access
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13 requires granular media permissions
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        // Android 12 and below use legacy storage permissions
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    
    // Notification permissions for Android 13+
    val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
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
    val essentialPermissions = storagePermissions + bluetoothPermissions + notificationPermissions
    
    val permissionsState = rememberMultiplePermissionsState(essentialPermissions)
    
    val lifecycleOwner = LocalLifecycleOwner.current

    // Centralized function to evaluate permission status and update onboarding step
    suspend fun evaluatePermissionsAndSetStep() {
        // Check if we have the essential storage permissions
        val hasStoragePermissions = storagePermissions.all { permission ->
            permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
        }

        if (hasStoragePermissions) {
            permissionScreenState = PermissionScreenState.PermissionsGranted
            if (!onboardingCompleted) {
                currentOnboardingStep = OnboardingStep.AUDIO_PLAYBACK // Move to audio/playback step
            } else {
                currentOnboardingStep = OnboardingStep.COMPLETE
                // Show media scan loader if onboarding was already completed (first startup after onboarding)
                if (!showMediaScanLoader) {
                    showMediaScanLoader = true
                }
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
                scope.launch {
                    evaluatePermissionsAndSetStep()
                }
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
                    scope.launch {
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
        // Show the main app only after media scanning is complete
        AnimatedVisibility(
            visible = currentOnboardingStep == OnboardingStep.COMPLETE && !isInitializingApp && !showMediaScanLoader, // Show app when complete AND not initializing AND media scan is done
            enter = fadeIn(animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)) +
                   slideInVertically(
                       initialOffsetY = { it / 3 },
                       animationSpec = tween(1000, easing = androidx.compose.animation.core.EaseOutCubic)
                   )
        ) {
            onPermissionsGranted()
        }

        // Show media scan loader after onboarding completion
        AnimatedVisibility(
            visible = showMediaScanLoader,
            enter = fadeIn(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOutCubic)),
            exit = fadeOut(animationSpec = tween(800, easing = androidx.compose.animation.core.EaseInCubic))
        ) {
            MediaScanLoader(
                musicViewModel = musicViewModel,
                onScanComplete = {
                    showMediaScanLoader = false
                }
            )
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
                        OnboardingStep.AUDIO_PLAYBACK -> currentOnboardingStep = OnboardingStep.THEMING // Move to theming
                        OnboardingStep.THEMING -> currentOnboardingStep = OnboardingStep.LIBRARY_SETUP // Move to library setup
                        OnboardingStep.LIBRARY_SETUP -> currentOnboardingStep = OnboardingStep.UPDATER // Move to updater
                        OnboardingStep.UPDATER -> {
                            appSettings.setOnboardingCompleted(true) // Mark onboarding as complete
                            currentOnboardingStep = OnboardingStep.COMPLETE // Move to complete
                            showMediaScanLoader = true // Show media scan loader after onboarding
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
