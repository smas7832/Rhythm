package chromahub.rhythm.app.ui.screens

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.R
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.M3LinearLoader
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.screens.onboarding.OnboardingStep
import chromahub.rhythm.app.ui.screens.onboarding.PermissionScreenState
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import chromahub.rhythm.app.util.HapticUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.util.Locale

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
        OnboardingStep.BACKUP_RESTORE -> 2
        OnboardingStep.AUDIO_PLAYBACK -> 3
        OnboardingStep.THEMING -> 4
        OnboardingStep.LIBRARY_SETUP -> 5
        OnboardingStep.UPDATER -> 6
        OnboardingStep.COMPLETE -> 7
    }
    
    val totalSteps = 7 // Welcome, Permissions, Backup/Restore, Audio/Playback, Theming, Library Setup, Updater
    
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
                            painter = painterResource(id = R.drawable.rhythm_splash_logo),
                            contentDescription = null,
                            modifier = Modifier.size(66.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
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
                    .fillMaxWidth() // Changed to fillMaxWidth
                    .padding(horizontal = 0.dp) // Added horizontal padding
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .verticalScroll(rememberScrollState()), // Make content scrollable
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Enhanced step transition with slide animations
                    Crossfade(
                        targetState = currentStep,
                        animationSpec = tween(
                            easing = androidx.compose.animation.core.EaseInOutCubic
                        ),
                        label = "onboardingStepTransition"
                    ) { step ->
                        when (step) {
                            OnboardingStep.WELCOME -> {
                                EnhancedWelcomeContent(onNextStep = onNextStep)
                            }
                            OnboardingStep.PERMISSIONS -> {
                                EnhancedPermissionContent(
                                    permissionScreenState = permissionScreenState,
                                    onGrantAccess = {
                                        onNextStep() // Trigger permission request
                                    },
                                    onOpenSettings = {
                                        val intent = android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = android.net.Uri.fromParts("package", context.packageName, null)
                                        context.startActivity(intent)
                                        onRequestAgain() // Set loading state
                                    },
                                    isButtonLoading = isParentLoading
                                )
                            }
                            OnboardingStep.BACKUP_RESTORE -> {
                                EnhancedBackupRestoreContent(
                                    onNextStep = onNextStep,
                                    appSettings = appSettings
                                )
                            }
                            OnboardingStep.AUDIO_PLAYBACK -> {
                                EnhancedAudioPlaybackContent(
                                    onNextStep = onNextStep,
                                    appSettings = appSettings
                                )
                            }
                            OnboardingStep.THEMING -> {
                                EnhancedThemingContent(
                                    onNextStep = onNextStep,
                                    themeViewModel = themeViewModel
                                )
                            }
                            OnboardingStep.LIBRARY_SETUP -> {
                                EnhancedLibrarySetupContent(
                                    onNextStep = onNextStep,
                                    appSettings = appSettings
                                )
                            }
                            OnboardingStep.UPDATER -> {
                                EnhancedUpdaterContent(
                                    onNextStep = onNextStep,
                                    appSettings = appSettings
                                )
                            }
                            OnboardingStep.COMPLETE -> {
                                // This should not be visible as we transition to the main app
                                Box {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedWelcomeContent(onNextStep: () -> Unit) {
    val context = LocalContext.current
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
            text = "Welcome to Rhythm!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Rhythm is a powerful music player designed to enhance your listening experience. Let's get you set up with a few simple steps.",
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
                text = "Manage and play your entire local music collection."
            )
            FeatureHighlight(
                icon = RhythmIcons.Devices.Bluetooth,
                text = "Connect seamlessly to Bluetooth speakers and other audio devices."
            )
            FeatureHighlight(
                icon = Icons.Filled.Palette,
                text = "Personalize your app's appearance with various themes and dynamic colors."
            )
        }
        
        val haptic = LocalHapticFeedback.current
        FilledTonalButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onNextStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
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
    val context = LocalContext.current
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
    
    val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
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
    
    val essentialPermissions = storagePermissions + bluetoothPermissions + notificationPermissions
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (permissionScreenState) {
                        PermissionScreenState.PermissionsGranted -> Icons.Filled.Check
                        PermissionScreenState.RedirectToSettings -> Icons.Filled.Security
                        else -> Icons.Filled.Security
                    },
                    contentDescription = "Permissions",
                    tint = when (permissionScreenState) {
                        PermissionScreenState.PermissionsGranted -> MaterialTheme.colorScheme.primary
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
                PermissionScreenState.PermissionsGranted -> "Permissions Granted!"
                PermissionScreenState.RedirectToSettings -> "Action Required: Open Settings"
                PermissionScreenState.ShowRationale -> "Permissions Needed"
                else -> "Grant Permissions"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = when (permissionScreenState) {
                PermissionScreenState.PermissionsGranted -> "All necessary permissions have been granted. You are ready to proceed!"
                PermissionScreenState.RedirectToSettings -> "To grant the required permissions, please navigate to the app settings on your device."
                PermissionScreenState.ShowRationale -> "Rhythm requires certain permissions to function correctly. Please grant them to continue."
                else -> "To fully utilize Rhythm's features, we need your permission to access certain device capabilities."
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android 13+ Notice",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "On newer Android versions, you'll see multiple permission requests. Please grant access to audio files and notifications for the best experience.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Enhanced permission explanation cards
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnhancedPermissionCard(
                icon = Icons.Filled.MusicNote,
                title = "Music Library Access",
                description = "Read your local music files and display them in the app",
                isGranted = storagePermissions.all { permission ->
                    permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
                }
            )
            
            EnhancedPermissionCard(
                icon = RhythmIcons.Devices.Bluetooth,
                title = "Bluetooth Connectivity",
                description = "Connect to Bluetooth speakers, headphones, and audio devices",
                isGranted = bluetoothPermissions.all { permission ->
                    permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
                }
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                EnhancedPermissionCard(
                    icon = RhythmIcons.Notifications,
                    title = "Notifications",
                    description = "Show playback controls and music information in notifications",
                    isGranted = notificationPermissions.all { permission ->
                        permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
                    }
                )
            }
        }

        val haptic = LocalHapticFeedback.current
        FilledTonalButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                when (permissionScreenState) {
                    PermissionScreenState.RedirectToSettings -> onOpenSettings()
                    else -> onGrantAccess()
                }
            },
            enabled = !isButtonLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = when (permissionScreenState) {
                    PermissionScreenState.PermissionsGranted -> MaterialTheme.colorScheme.primary
                    PermissionScreenState.RedirectToSettings -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = when (permissionScreenState) {
                    PermissionScreenState.PermissionsGranted -> MaterialTheme.colorScheme.onPrimary
                    PermissionScreenState.RedirectToSettings -> MaterialTheme.colorScheme.onError
                    else -> MaterialTheme.colorScheme.onPrimary
                }
            )
        ) {
            if (isButtonLoading) {
                M3LinearLoader(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonText = when (permissionScreenState) {
                        PermissionScreenState.PermissionsGranted -> "Continue"
                        PermissionScreenState.RedirectToSettings -> "Open App Settings"
                        else -> "Grant Access"
                    }
                    
                    Text(buttonText, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = when (permissionScreenState) {
                            PermissionScreenState.PermissionsGranted -> RhythmIcons.Forward
                            PermissionScreenState.RedirectToSettings -> Icons.Filled.Security
                            else -> Icons.Filled.Security
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceContainer
        )
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
                        if (isGranted) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Filled.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun EnhancedBackupRestoreContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // State for backup settings
    val autoBackupEnabled by appSettings.autoBackupEnabled.collectAsState()
    val lastBackupTimestamp by appSettings.lastBackupTimestamp.collectAsState()
    
    // Local UI state
    var showBackupTip by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with icon and title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Keep your settings, playlists, and preferences safe",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Auto-backup toggle card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                    appSettings.setAutoBackupEnabled(!autoBackupEnabled)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = if (autoBackupEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Automatically backup your settings weekly",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = autoBackupEnabled,
                    onCheckedChange = {
                        HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                        appSettings.setAutoBackupEnabled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
        
        // Info cards about backup features
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackupFeatureCard(
                icon = Icons.Filled.Save,
                title = "Complete Backup",
                description = "All your settings, playlists, and preferences are included"
            )
            
            BackupFeatureCard(
                icon = Icons.Filled.RestoreFromTrash,
                title = "Easy Restore",
                description = "Restore from files or clipboard with one tap"
            )
            
            BackupFeatureCard(
                icon = Icons.Filled.Security,
                title = "Local Storage",
                description = "Backups are stored locally on your device for privacy"
            )
        }
        
        // Tip card
        AnimatedVisibility(
            visible = autoBackupEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "You can manually create backups anytime in Settings > Backup & Restore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Continue button
        Button(
            onClick = {
                HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                onNextStep()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BackupFeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun EnhancedAudioPlaybackContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings
) {
    val context = LocalContext.current
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
    val useSystemVolume by appSettings.useSystemVolume.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced audio icon
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
                    imageVector = RhythmIcons.Player.VolumeUp,
                    contentDescription = "Audio & Playback Settings",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Audio & Playback",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Adjust audio and playback settings to optimize your listening experience.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Audio options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Haptic feedback toggle
            EnhancedThemeOption(
                icon = Icons.Filled.TouchApp,
                title = "Haptic Feedback",
                description = "Enable subtle vibrations on interactions for a more tactile experience.",
                isEnabled = hapticFeedbackEnabled,
                onToggle = { appSettings.setHapticFeedbackEnabled(it) }
            )

            // System volume control toggle
            EnhancedThemeOption(
                icon = RhythmIcons.Player.VolumeUp,
                title = "System Volume Control",
                description = "Allow your device's physical volume buttons to control Rhythm's playback volume.",
                isEnabled = useSystemVolume,
                onToggle = { appSettings.setUseSystemVolume(it) }
            )
        }
        
        val haptic = LocalHapticFeedback.current
        FilledTonalButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onNextStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Next", style = MaterialTheme.typography.labelLarge)
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
fun EnhancedLibrarySetupContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings
) {
    val context = LocalContext.current
    val albumViewType by appSettings.albumViewType.collectAsState()
    val artistViewType by appSettings.artistViewType.collectAsState()
    val albumSortOrder by appSettings.albumSortOrder.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced library icon
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
                    imageVector = Icons.Filled.LibraryMusic,
                    contentDescription = "Library Setup",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Library Organization",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Customize how your music library is displayed and organized within Rhythm.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Library options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Album view type dropdown
            SettingsDropdownItem(
                title = "Album Display Style",
                description = "List or grid layout for albums.",
                selectedOption = albumViewType.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Filled.Album,
                options = listOf("List", "Grid"),
                onOptionSelected = { selectedOption ->
                    val newViewType = chromahub.rhythm.app.data.AlbumViewType.valueOf(selectedOption.uppercase())
                    appSettings.setAlbumViewType(newViewType)
                }
            )

            // Artist view type dropdown
            SettingsDropdownItem(
                title = "Artist Display Style",
                description = "List or grid layout for artists.",
                selectedOption = artistViewType.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Filled.Person,
                options = listOf("List", "Grid"),
                onOptionSelected = { selectedOption ->
                    val newViewType = chromahub.rhythm.app.data.ArtistViewType.valueOf(selectedOption.uppercase())
                    appSettings.setArtistViewType(newViewType)
                }
            )

            // Album sort order dropdown
            SettingsDropdownItem(
                title = "Album Song Order",
                description = "Default sorting order for songs within albums.",
                selectedOption = when (chromahub.rhythm.app.ui.screens.AlbumSortOrder.valueOf(albumSortOrder)) {
                    chromahub.rhythm.app.ui.screens.AlbumSortOrder.TRACK_NUMBER -> "Track"
                    chromahub.rhythm.app.ui.screens.AlbumSortOrder.TITLE_ASC -> "Title A-Z"
                    chromahub.rhythm.app.ui.screens.AlbumSortOrder.TITLE_DESC -> "Title Z-A"
                    chromahub.rhythm.app.ui.screens.AlbumSortOrder.DURATION_ASC -> "Duration ↑"
                    chromahub.rhythm.app.ui.screens.AlbumSortOrder.DURATION_DESC -> "Duration ↓"
                },
                icon = RhythmIcons.Actions.Sort,
                options = listOf("Track Number", "Title A-Z", "Title Z-A", "Duration ↑", "Duration ↓"),
                onOptionSelected = { selectedOption ->
                    val newSortOrder = when (selectedOption) {
                        "Track Number" -> chromahub.rhythm.app.ui.screens.AlbumSortOrder.TRACK_NUMBER
                        "Title A-Z" -> chromahub.rhythm.app.ui.screens.AlbumSortOrder.TITLE_ASC
                        "Title Z-A" -> chromahub.rhythm.app.ui.screens.AlbumSortOrder.TITLE_DESC
                        "Duration ↑" -> chromahub.rhythm.app.ui.screens.AlbumSortOrder.DURATION_ASC
                        "Duration ↓" -> chromahub.rhythm.app.ui.screens.AlbumSortOrder.DURATION_DESC
                        else -> chromahub.rhythm.app.ui.screens.AlbumSortOrder.TRACK_NUMBER
                    }
                    appSettings.setAlbumSortOrder(newSortOrder.name)
                }
            )
        }
        
        val haptic = LocalHapticFeedback.current
        FilledTonalButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onNextStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Next", style = MaterialTheme.typography.labelLarge)
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
fun EnhancedThemingContent(
    onNextStep: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
    val darkMode by themeViewModel.darkMode.collectAsState()
    val useDynamicColors by themeViewModel.useDynamicColors.collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced icon with animation
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
                    contentDescription = "Theming",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Customize Your Theme",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Personalize Rhythm's appearance to match your style. Choose between system theme, dark mode, and dynamic colors.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Theme options
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System theme toggle
            EnhancedThemeOption(
                icon = Icons.Filled.DarkMode,
                title = "Follow System Theme",
                description = "Automatically switch between light and dark themes based on your device's system settings.",
                isEnabled = useSystemTheme,
                onToggle = { enabled ->
                    scope.launch {
                        themeViewModel.setUseSystemTheme(enabled)
                    }
                }
            )
            
            // Manual dark mode toggle (only shown when system theme is off)
            AnimatedVisibility(
                visible = !useSystemTheme,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                EnhancedThemeOption(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark Mode",
                    description = "Manually enable or disable dark mode for the application.",
                    isEnabled = darkMode,
                    onToggle = { enabled ->
                        scope.launch {
                            themeViewModel.setDarkMode(enabled)
                        }
                    }
                )
            }
            
            // Dynamic colors (Material You) - only on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                EnhancedThemeOption(
                    icon = Icons.Filled.Palette,
                    title = "Dynamic Colors (Material You)",
                    description = "Apply colors extracted from your device's wallpaper for a cohesive look.",
                    isEnabled = useDynamicColors,
                    onToggle = { enabled ->
                        scope.launch {
                            themeViewModel.setUseDynamicColors(enabled)
                        }
                    }
                )
            }
        }
        
        val haptic = LocalHapticFeedback.current
        FilledTonalButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onNextStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Next", style = MaterialTheme.typography.labelLarge)
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
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { 
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                onToggle(!isEnabled) 
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.surfaceContainerLow
            else 
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                    onToggle(enabled)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    }
}

@Composable
fun EnhancedUpdaterContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings
) {
    val context = LocalContext.current
    val autoCheckForUpdates by appSettings.autoCheckForUpdates.collectAsState()
    val updateChannel by appSettings.updateChannel.collectAsState()
    val updateCheckIntervalHours by appSettings.updateCheckIntervalHours.collectAsState()
    val updatesEnabled by appSettings.updatesEnabled.collectAsState() // NEW
    val scope = rememberCoroutineScope()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced icon with animation
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
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = "App Updates",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "App Updates",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Configure how Rhythm checks for and receives updates to ensure you always have the latest features and improvements.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Update options
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable Updates toggle (NEW)
            EnhancedUpdateOption(
                icon = Icons.Filled.SystemUpdate,
                title = "Enable Updates",
                description = "Allow the app to check for and download updates",
                isEnabled = updatesEnabled,
                onToggle = { enabled ->
                    scope.launch {
                        appSettings.setUpdatesEnabled(enabled)
                    }
                }
            )

            // Animated visibility for other update options based on updatesEnabled
            AnimatedVisibility(
                visible = updatesEnabled, // CHANGED from autoCheckForUpdates
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { // Changed spacing to 16.dp for consistency
                    // Auto check for updates toggle (now inside AnimatedVisibility)
                    EnhancedUpdateOption(
                        icon = Icons.Filled.SystemUpdate, // Changed icon to Update from SystemUpdate
                        title = "Periodic Check", // Changed title
                        description = "Check for updates from Rhythm's GitHub repo automatically", // Changed description
                        isEnabled = autoCheckForUpdates,
                        onToggle = { enabled ->
                            scope.launch {
                                appSettings.setAutoCheckForUpdates(enabled)
                            }
                        }
                    )
                    /*                    // Show update interval dropdown when auto-check is enabled
                    AnimatedVisibility(
                        visible = autoCheckForUpdates,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Update check interval dropdown
                    SettingsDropdownItem(
                        title = "Check Frequency",
                        description = "How often to check for updates.",
                        selectedOption = when (updateCheckIntervalHours) {
                            1 -> "Every Hour"
                            3 -> "Every 3 Hours"
                            6 -> "Every 6 Hours"
                            12 -> "Every 12 Hours"
                            24 -> "Daily"
                            else -> "Every 6 Hours"
                        },
                        icon = Icons.Filled.AccessTime,
                        options = listOf(
                            "Every Hour",
                            "Every 3 Hours", 
                            "Every 6 Hours",
                            "Every 12 Hours",
                            "Daily"
                        ),
                        onOptionSelected = { selectedOption ->
                            val hours = when (selectedOption) {
                                "Every Hour" -> 1
                                "Every 3 Hours" -> 3
                                "Every 6 Hours" -> 6
                                "Every 12 Hours" -> 12
                                "Daily" -> 24
                                else -> 6
                            }
                            scope.launch {
                                appSettings.setUpdateCheckIntervalHours(hours)
                            }
                        }
                    )*/

                            // Update channel selection dropdown
                    SettingsDropdownItem(
                        title = "Update Channel",
                        description = "Stable or beta versions.",
                        selectedOption = when (updateChannel) {
                            "stable" -> "Stable"
                            "beta" -> "Beta"
                            else -> "Stable"
                        },
                        icon = when (updateChannel) {
                            "stable" -> Icons.Filled.Public
                            "beta" -> Icons.Filled.BugReport
                            else -> Icons.Filled.Public
                        },
                        options = listOf("Stable", "Beta"),
                        onOptionSelected = { selectedOption ->
                            val channel = when (selectedOption) {
                                "Stable" -> "stable"
                                "Beta" -> "beta"
                                else -> "stable"
                            }
                            scope.launch {
                                appSettings.setUpdateChannel(channel)
                            }
                        }
                    )
                }
            }
        }
        
        // Spacer(modifier = Modifier.height(32.dp)) // Added spacer for consistent button spacing
        
        val haptic = LocalHapticFeedback.current
        FilledTonalButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onNextStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Finish Setup", style = MaterialTheme.typography.labelLarge)
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
fun EnhancedUpdateOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { 
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                onToggle(!isEnabled) 
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.surfaceContainerLow
            else 
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                    onToggle(enabled)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    }
}

@Composable
fun EnhancedUpdateChannelOption(
    channel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        onClick = onSelect
    )
}

@Composable
fun SettingsDropdownItem(
    title: String,
    description: String,
    selectedOption: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { 
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                showDropdown = true 
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Selected option badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Show options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Enhanced Dropdown Menu
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            options.forEach { option ->
                Surface(
                    color = if (selectedOption == option) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    else 
                        androidx.compose.ui.graphics.Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedOption == option) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedOption == option) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when {
                                    option.contains("Track Number") -> Icons.Filled.FormatListNumbered
                                    option.contains("Title A-Z") || option.contains("Title Z-A") -> Icons.Filled.SortByAlpha
                                    option.contains("Duration") -> Icons.Filled.AccessTime
                                    option.contains("List") -> RhythmIcons.Actions.List
                                    option.contains("Grid") -> Icons.Filled.GridView
                                    option.contains("Hour") -> Icons.Filled.AccessTime
                                    option.contains("Stable") -> Icons.Filled.Public
                                    option.contains("Beta") -> Icons.Filled.BugReport
                                    else -> Icons.Filled.Check // Fallback
                                },
                                contentDescription = null,
                                tint = if (selectedOption == option) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            onOptionSelected(option)
                            showDropdown = false
                        },
                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                            textColor = if (selectedOption == option) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    )
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
