package chromahub.rhythm.app.ui.screens

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import chromahub.rhythm.app.ui.components.M3FourColorCircularLoader
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.screens.onboarding.OnboardingStep
import chromahub.rhythm.app.ui.screens.onboarding.PermissionScreenState
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.viewmodel.ThemeViewModel
import chromahub.rhythm.app.util.HapticUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    currentStep: OnboardingStep,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onRequestAgain: () -> Unit,
    permissionScreenState: PermissionScreenState,
    isParentLoading: Boolean,
    themeViewModel: ThemeViewModel,
    appSettings: AppSettings,
    musicViewModel: MusicViewModel,
    updaterViewModel: AppUpdaterViewModel = viewModel(),
    onFinish: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    
    // Collect playlists for playlist management bottom sheet
    val playlists by musicViewModel.playlists.collectAsState()
    
    // Bottom sheet states
    var showThemeBottomSheet by remember { mutableStateOf(false) }
    var showMediaScanBottomSheet by remember { mutableStateOf(false) }
    var showLibraryTabOrderBottomSheet by remember { mutableStateOf(false) }
    var showBackupRestoreBottomSheet by remember { mutableStateOf(false) }
    var showPlaylistManagementBottomSheet by remember { mutableStateOf(false) }
    // Note: CacheManagement bottom sheet requires musicViewModel parameter
    // var showCacheManagementBottomSheet by remember { mutableStateOf(false) }
    
    // Responsive sizing
    val isTablet = configuration.screenWidthDp >= 600
    val contentMaxWidth = if (isTablet) 560.dp else androidx.compose.ui.unit.Dp.Infinity
    val horizontalPadding = if (isTablet) 40.dp else 20.dp
    val cardPadding = if (isTablet) 28.dp else 20.dp
    
    // Get current step index
    val stepIndex = when (currentStep) {
        OnboardingStep.WELCOME -> 0
        OnboardingStep.PERMISSIONS -> 1
        OnboardingStep.BACKUP_RESTORE -> 2
        OnboardingStep.AUDIO_PLAYBACK -> 3
        OnboardingStep.THEMING -> 4
        OnboardingStep.LIBRARY_SETUP -> 5
        OnboardingStep.MEDIA_SCAN -> 6
        OnboardingStep.UPDATER -> 7
        OnboardingStep.SETUP_FINISHED -> 8
        OnboardingStep.COMPLETE -> 9
    }

    val totalSteps = 9
    
    // Create pager state
    val pagerState = rememberPagerState(
        initialPage = stepIndex,
        pageCount = { totalSteps }
    )
    
    // Sync pager with step changes
    LaunchedEffect(stepIndex) {
        if (pagerState.currentPage != stepIndex) {
            pagerState.animateScrollToPage(stepIndex)
        }
    }
    
    // Sync step with pager changes
    LaunchedEffect(pagerState.currentPage) {
        val newStep = when (pagerState.currentPage) {
            0 -> OnboardingStep.WELCOME
            1 -> OnboardingStep.PERMISSIONS
            2 -> OnboardingStep.BACKUP_RESTORE
            3 -> OnboardingStep.AUDIO_PLAYBACK
            4 -> OnboardingStep.THEMING
            5 -> OnboardingStep.LIBRARY_SETUP
            6 -> OnboardingStep.MEDIA_SCAN
            7 -> OnboardingStep.UPDATER
            8 -> OnboardingStep.SETUP_FINISHED
            else -> OnboardingStep.COMPLETE
        }
        if (newStep != currentStep && pagerState.currentPage < stepIndex) {
            onPrevStep()
        } else if (newStep != currentStep && pagerState.currentPage > stepIndex) {
            onNextStep()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Single onboarding card container for all pager content
        OnboardingCard(
            isTablet = isTablet,
            modifier = Modifier.weight(1f)
        ) {
            // HorizontalPager for smooth sliding animations
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = when {
                    currentStep != OnboardingStep.PERMISSIONS -> true
                    permissionScreenState == PermissionScreenState.PermissionsGranted -> true
                    permissionScreenState == PermissionScreenState.Loading -> false
                    else -> false
                },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val step = OnboardingStep.values()[page]
                // Container for step-specific content - positioned at top within pager page
                Box(
                    modifier = Modifier.fillMaxSize().padding(top=54.dp, start = horizontalPadding, end = horizontalPadding, bottom = cardPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (step) {
                        OnboardingStep.WELCOME -> {
                            // Welcome screen without card
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
                                onSkip = onNextStep,
                                appSettings = appSettings,
                                onOpenBottomSheet = { showBackupRestoreBottomSheet = true }
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
                                onSkip = onNextStep,
                                themeViewModel = themeViewModel,
                                appSettings = appSettings,
                                onOpenBottomSheet = { showThemeBottomSheet = true }
                            )
                        }
                        OnboardingStep.LIBRARY_SETUP -> {
                            EnhancedLibrarySetupContent(
                                onNextStep = onNextStep,
                                appSettings = appSettings,
                                onOpenTabOrderBottomSheet = { showLibraryTabOrderBottomSheet = true },
                                onOpenPlaylistManagementBottomSheet = { showPlaylistManagementBottomSheet = true }
                            )
                        }
                        OnboardingStep.MEDIA_SCAN -> {
                            EnhancedMediaScanContent(
                                onNextStep = onNextStep,
                                onSkip = onNextStep,
                                appSettings = appSettings,
                                onOpenBottomSheet = { showMediaScanBottomSheet = true }
                            )
                        }
                        OnboardingStep.UPDATER -> {
                            EnhancedUpdaterContent(
                                onNextStep = onNextStep,
                                appSettings = appSettings,
                                updaterViewModel = updaterViewModel
                            )
                        }
                        OnboardingStep.SETUP_FINISHED -> {
                            EnhancedSetupFinishedContent(onFinish = onFinish)
                        }
                        OnboardingStep.COMPLETE -> {
                            // This should not be visible as we transition to the main app
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        // Bottom navigation bar
            AnimatedVisibility(
                visible = currentStep != OnboardingStep.WELCOME,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = if (isTablet) 48.dp else 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            // Back button with spring animation
                        AnimatedVisibility(
                            visible = stepIndex > 0,
                            enter = fadeIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + expandHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ),
                            exit = fadeOut(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + shrinkHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        ) {
                            val buttonScale = remember { Animatable(1f) }
                            
                            OutlinedButton(
                                onClick = { 
                                    scope.launch {
                                        buttonScale.animateTo(0.92f, animationSpec = tween(100))
                                        buttonScale.animateTo(1f, animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessHigh
                                        ))
                                    }
                                    onPrevStep() 
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .graphicsLayer {
                                        scaleX = buttonScale.value
                                        scaleY = buttonScale.value
                                    },
                                shape = RoundedCornerShape(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Back", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        // App logo and step count - centered between back and next buttons
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.rhythm_splash_logo),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            androidx.compose.animation.AnimatedContent(
                                targetState = stepIndex,
                                transitionSpec = {
                                    (slideInVertically { height -> height / 2 } + fadeIn()).togetherWith(
                                        slideOutVertically { height -> -height / 2 } + fadeOut()
                                    )
                                },
                                modifier = Modifier.padding(top = 4.dp),
                                label = "progressText"
                            ) { step ->
                                Text(
                                    text = "${step + 1} of $totalSteps",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Next/Finish button with spring animation
                        val nextButtonScale = remember { Animatable(1f) }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    nextButtonScale.animateTo(0.92f, animationSpec = tween(100))
                                    nextButtonScale.animateTo(1f, animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    ))
                                }
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                when (currentStep) {
                                    OnboardingStep.PERMISSIONS -> {
                                        // For permission step, handle based on state
                                        when (permissionScreenState) {
                                            PermissionScreenState.RedirectToSettings -> {
                                                val intent = android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                intent.data = android.net.Uri.fromParts("package", context.packageName, null)
                                                context.startActivity(intent)
                                                onRequestAgain()
                                            }
                                            PermissionScreenState.PermissionsGranted -> onNextStep()
                                            PermissionScreenState.Loading -> { /* Do nothing while loading */ }
                                            else -> onNextStep() // Trigger permission request
                                        }
                                    }
                                    else -> onNextStep() // All other steps just go next
                                }
                            },
                            enabled = when (currentStep) {
                                OnboardingStep.PERMISSIONS -> !isParentLoading && permissionScreenState != PermissionScreenState.Loading
                                else -> true
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .graphicsLayer {
                                    scaleX = nextButtonScale.value
                                    scaleY = nextButtonScale.value
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (currentStep) {
                                    OnboardingStep.PERMISSIONS -> when (permissionScreenState) {
                                        PermissionScreenState.PermissionsGranted -> MaterialTheme.colorScheme.primary
                                        PermissionScreenState.RedirectToSettings -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                contentColor = when (currentStep) {
                                    OnboardingStep.PERMISSIONS -> when (permissionScreenState) {
                                        PermissionScreenState.PermissionsGranted -> MaterialTheme.colorScheme.onPrimary
                                        PermissionScreenState.RedirectToSettings -> MaterialTheme.colorScheme.onError
                                        else -> MaterialTheme.colorScheme.onPrimary
                                    }
                                    else -> MaterialTheme.colorScheme.onPrimary
                                }
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Crossfade(
                                targetState = currentStep == OnboardingStep.PERMISSIONS && isParentLoading,
                                animationSpec = tween(300),
                                label = "buttonContent"
                            ) { loading ->
                                if (loading) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        M3LinearLoader(
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Checking...",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val buttonText = when {
                                            currentStep == OnboardingStep.SETUP_FINISHED -> "Let's Go!"
                                            currentStep == OnboardingStep.UPDATER -> "Finish Setup"
                                            currentStep == OnboardingStep.PERMISSIONS -> when (permissionScreenState) {
                                                PermissionScreenState.PermissionsGranted -> "Continue"
                                                PermissionScreenState.RedirectToSettings -> "Open Settings"
                                                else -> "Grant Access"
                                            }
                                            else -> "Next"
                                        }
                                        val buttonIcon = when {
                                            currentStep == OnboardingStep.SETUP_FINISHED -> Icons.Filled.Check
                                            currentStep == OnboardingStep.UPDATER -> Icons.AutoMirrored.Filled.ArrowForward
                                            currentStep == OnboardingStep.PERMISSIONS -> when (permissionScreenState) {
                                                PermissionScreenState.PermissionsGranted -> Icons.AutoMirrored.Filled.ArrowForward
                                                PermissionScreenState.RedirectToSettings -> Icons.Filled.Security
                                                else -> Icons.Filled.Security
                                            }
                                            else -> Icons.AutoMirrored.Filled.ArrowForward
                                        }
                                        
                                        Text(
                                            buttonText,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = buttonIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    
    // Bottom sheets for advanced configuration
    if (showThemeBottomSheet) {
        ThemeCustomizationBottomSheet(
            onDismiss = { showThemeBottomSheet = false },
            appSettings = appSettings
        )
    }
    
    if (showMediaScanBottomSheet) {
        MediaScanBottomSheet(
            onDismiss = { showMediaScanBottomSheet = false },
            appSettings = appSettings
        )
    }
    
    if (showLibraryTabOrderBottomSheet) {
        LibraryTabOrderBottomSheet(
            onDismiss = { showLibraryTabOrderBottomSheet = false },
            appSettings = appSettings,
            haptics = haptic
        )
    }
    
    if (showBackupRestoreBottomSheet) {
        BackupRestoreBottomSheet(
            onDismiss = { showBackupRestoreBottomSheet = false },
            appSettings = appSettings
        )
    }
    
    if (showPlaylistManagementBottomSheet) {
        PlaylistManagementBottomSheet(
            onDismiss = { showPlaylistManagementBottomSheet = false },
            playlists = playlists,
            musicViewModel = musicViewModel,
            onCreatePlaylist = { /* Handle in main app after onboarding */ },
            onDeletePlaylist = { /* Handle in main app after onboarding */ }
        )
    }
    
    // Note: CacheManagement bottom sheet requires musicViewModel parameter
    /*
    if (showCacheManagementBottomSheet) {
        CacheManagementBottomSheet(
            onDismiss = { showCacheManagementBottomSheet = false },
            appSettings = appSettings,
            musicViewModel = musicViewModel
        )
    }
    */
}

/**
 * Unified card container for all onboarding steps (except welcome)
 * Provides consistent Material You styling with rounded corners and elevated surface
 */
@Composable
private fun OnboardingCard(
    isTablet: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val contentMaxWidth = 600.dp
    val cardPadding = if (isTablet) 20.dp else 18.dp
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = if (isTablet) 4.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .let { if (isTablet) it.width(contentMaxWidth) else it }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        // Remove vertical scroll since we're not constraining height anymore
        // and let pager handle its own sizing and scrolling behavior
        Column(
            modifier = Modifier.padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun EnhancedWelcomeContent(onNextStep: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // App logo 
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(
                    animationSpec = tween(1000)
                )
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.rhythm_splash_logo),
                        contentDescription = "Rhythm Logo",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App name with staggered animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
                ) {
                    Text(
                        text = "Rhythm",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            letterSpacing = 0.8.sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Subtitle with modern styling and delayed animation
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
                ) {
                    Text(
                        text = "Your all-in-one offline music player",
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 0.4.sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Minimal description with better typography and animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 600)) +
                        slideInVertically(
                            animationSpec = tween(800, delayMillis = 600),
                            initialOffsetY = { it / 3 }
                        )
            ) {
                Text(
                    text = "Personalize every aspect of your listening experience with powerful features and complete privacy. Play your music offline, ad-free, with full control.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(44.dp))
            
            // Enhanced Get Started button with modern design and animation
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(800, delayMillis = 800))
            ) {
                Button(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                        onNextStep()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Feature highlights with animated appearance
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 1000)) +
                        expandVertically(animationSpec = tween(600, delayMillis = 1000))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    WelcomeFeatureChip(
                        icon = Icons.Filled.MusicNote,
                        text = "Offline"
                    )
                    WelcomeFeatureChip(
                        icon = Icons.Filled.Palette,
                        text = "Customizable"
                    )
                    WelcomeFeatureChip(
                        icon = Icons.Filled.Security,
                        text = "Private"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Version info or subtle additional info with animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 1200))
            ) {
                Text(
                    text = "Music player • Designed for Android",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WelcomeFeatureChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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
    val scrollState = rememberScrollState()
    
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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

        // Button removed - now handled by bottom navigation bar
    }
}

@Composable
fun EnhancedPermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean = false
) {
    // Animated state changes
    val containerColor by animateColorAsState(
        targetValue = if (isGranted) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "containerColor"
    )
    
    val iconBackgroundColor by animateColorAsState(
        targetValue = if (isGranted) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "iconBackgroundColor"
    )
    
    val iconTint by animateColorAsState(
        targetValue = if (isGranted) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "iconTint"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = if (isGranted) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                // Crossfade between icon and checkmark
                Crossfade(
                    targetState = isGranted,
                    animationSpec = tween(400),
                    label = "iconCrossfade"
                ) { granted ->
                    Icon(
                        imageVector = if (granted) Icons.Filled.Check else icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
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
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Success badge with animation
                    AnimatedVisibility(
                        visible = isGranted,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✓ Granted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EnhancedBackupRestoreContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings,
    onSkip: () -> Unit = {},
    onOpenBottomSheet: () -> Unit = {}
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State for backup settings
    val autoBackupEnabled by appSettings.autoBackupEnabled.collectAsState()
    val lastBackupTimestamp by appSettings.lastBackupTimestamp.collectAsState()
    
    // Local UI state
    var showBackupTip by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with icon and title
        Column(
            horizontalAlignment = Alignment.Start,
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
                        imageVector = Icons.Filled.Backup,
                        contentDescription = "Backup & Restore",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Protect Your Data",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = "Keep your playlists, settings, and preferences safe with automatic backups. Restore everything when switching devices or after reinstalling - never lose your music collection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
        
        // Vertically centered content area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
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
                    HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
                    appSettings.setAutoBackupEnabled(!autoBackupEnabled)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Autorenew,
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
                        HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
                        appSettings.setAutoBackupEnabled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
        
        // Backup & Restore management card
        Card(
            onClick = onOpenBottomSheet,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Backup & Restore Center",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Create backups or restore from existing backup files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open backup & restore",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
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
        
        // Backup features info card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "What Gets Backed Up?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                BackupFeatureTipItem(
                    icon = Icons.Filled.Save,
                    text = "All settings, playlists, themes, and library customization"
                )
                BackupFeatureTipItem(
                    icon = Icons.Filled.RestoreFromTrash,
                    text = "Restore from files or clipboard with one tap"
                )
                BackupFeatureTipItem(
                    icon = Icons.Filled.Security,
                    text = "Backups stored locally on your device for privacy"
                )
            }
        }
        } // End vertically centered content
        
        Spacer(modifier = Modifier.height(0.dp))
        
        /*Spacer(modifier = Modifier.height(24.dp))
        
        // Skip option with divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "or",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip button
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.labelLarge
            )
        }*/

    }
}

@Composable
private fun BackupFeatureTipItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
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
    val showLyrics by appSettings.showLyrics.collectAsState()
    val onlineOnlyLyrics by appSettings.onlineOnlyLyrics.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
            text = "Audio & Playback Experience",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Customize your audio experience with haptic feedback, volume controls, lyrics display, and equalizer settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Vertically centered content area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
        // Audio options
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
            
            // Show Lyrics toggle
            EnhancedThemeOption(
                icon = Icons.Filled.Lyrics,
                title = "Show Lyrics",
                description = "Display synchronized lyrics when available for your favorite songs.",
                isEnabled = showLyrics,
                onToggle = { appSettings.setShowLyrics(it) }
            )
            
            // Online Lyrics Only toggle (shown when lyrics are enabled)
            AnimatedVisibility(
                visible = showLyrics,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnhancedThemeOption(
                        icon = Icons.Filled.Cloud,
                        title = "Online Lyrics Only",
                        description = "Only fetch and display lyrics when connected to the internet to save data.",
                        isEnabled = onlineOnlyLyrics,
                        onToggle = { appSettings.setOnlineOnlyLyrics(it) }
                    )
                    
                    // Offline lyrics support info
                    AnimatedVisibility(
                        visible = !onlineOnlyLyrics,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Offline lyrics will be loaded from local .lrc files when available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        } // End vertically centered content
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Equalizer info card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Equalizer Available",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fine-tune audio frequencies in Settings > Equalizer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        

    }
}

@Composable
fun EnhancedLibrarySetupContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings,
    onOpenTabOrderBottomSheet: () -> Unit = {},
    onOpenPlaylistManagementBottomSheet: () -> Unit = {}
) {
    val context = LocalContext.current
    val albumViewType by appSettings.albumViewType.collectAsState()
    val artistViewType by appSettings.artistViewType.collectAsState()
    val albumSortOrder by appSettings.albumSortOrder.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
            text = "Organize Your Music Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Customize how your albums, artists, and songs are displayed. Choose your preferred layout, sorting order, and tab arrangement.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Vertically centered content area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
        // Library options
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
        } // End vertically centered content
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Additional features info
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LibraryFeatureCard(
                icon = Icons.Filled.FormatListNumbered,
                title = "Library Tab Order",
                description = "Reorder Songs, Playlists, Albums, Artists, and Explorer tabs",
                onClick = onOpenTabOrderBottomSheet,
                usePrimaryStyle = true
            )

            LibraryFeatureCard(
                icon = Icons.Filled.Queue,
                title = "Playlist Management",
                description = "Create, import, export, and organize your music playlists",
                onClick = onOpenPlaylistManagementBottomSheet,
                usePrimaryStyle = true
            )
        }
        

    }
}

@Composable
private fun LibraryFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    usePrimaryStyle: Boolean = false
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = if (usePrimaryStyle)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick?.invoke() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (usePrimaryStyle)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (usePrimaryStyle)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (usePrimaryStyle)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = if (usePrimaryStyle)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedThemingContent(
    onNextStep: () -> Unit,
    themeViewModel: ThemeViewModel,
    appSettings: AppSettings,
    onSkip: () -> Unit = {},
    onOpenBottomSheet: () -> Unit = {}
) {
    val context = LocalContext.current
    val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
    val darkMode by themeViewModel.darkMode.collectAsState()
    val useDynamicColors by themeViewModel.useDynamicColors.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
            text = "Make Rhythm Yours",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Personalize Rhythm's appearance to match your style. Choose your system theme, font, or Material You dynamic colors (Android 12+).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Live theme preview card
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(600))
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Preview components
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Primary color preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Primary",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Secondary color preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Container",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Tertiary color preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Accent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Theme options - vertically centered
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
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
        } // End vertically centered content
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Advanced theming info card
        Card(
            onClick = onOpenBottomSheet,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Advanced Customization",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Custom color schemes, album art colors, and fonts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open theme customization",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        /*Spacer(modifier = Modifier.height(24.dp))
        
        // Skip option with divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "or",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip button
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.labelLarge
            )
        }*/

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
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
    appSettings: AppSettings,
    updaterViewModel: AppUpdaterViewModel = viewModel()
) {
    val context = LocalContext.current
    val autoCheckForUpdates by appSettings.autoCheckForUpdates.collectAsState()
    val updateChannel by appSettings.updateChannel.collectAsState()
    val updateCheckIntervalHours by appSettings.updateCheckIntervalHours.collectAsState()
    val updatesEnabled by appSettings.updatesEnabled.collectAsState() // NEW
    val scope = rememberCoroutineScope()
    
    // Collect updater states
    val isCheckingForUpdates by updaterViewModel.isCheckingForUpdates.collectAsState()
    val updateAvailable by updaterViewModel.updateAvailable.collectAsState()
    val latestVersion by updaterViewModel.latestVersion.collectAsState()
    val currentVersion by updaterViewModel.currentVersion.collectAsState()
    val isDownloading by updaterViewModel.isDownloading.collectAsState()
    val downloadProgress by updaterViewModel.downloadProgress.collectAsState()
    val downloadedFile by updaterViewModel.downloadedFile.collectAsState()
    val error by updaterViewModel.error.collectAsState()
    
    // Auto-check for updates once when this step is opened and updates are enabled
    var hasCheckedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(updatesEnabled) {
        if (updatesEnabled && !hasCheckedOnce) {
            hasCheckedOnce = true
            updaterViewModel.checkForUpdates(force = true)
        }
    }
    val scrollState = rememberScrollState()
    
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
            text = "Stay Up to Date",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Get the latest features, improvements, and bug fixes automatically from GitHub. Choose between Stable releases (recommended) or Beta builds for early access to new features. No Google Play required.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Update Status UI - Material 3 Expressive Design (No Box/Card)
        val showUpdateStatus = isCheckingForUpdates || isDownloading || 
                               updateAvailable || downloadedFile != null || 
                               (!isCheckingForUpdates && !updateAvailable && hasCheckedOnce && error == null)
        
        AnimatedVisibility(
            visible = showUpdateStatus,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn() + scaleIn(initialScale = 0.9f),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            OnboardingExpressiveUpdateStatus(
                isCheckingForUpdates = isCheckingForUpdates,
                updateAvailable = updateAvailable,
                latestVersion = latestVersion,
                currentVersion = currentVersion,
                isDownloading = isDownloading,
                downloadProgress = downloadProgress,
                downloadedFile = downloadedFile,
                error = error,
                updaterViewModel = updaterViewModel,
                onDownload = { updaterViewModel.downloadUpdate() },
                onInstall = { updaterViewModel.installDownloadedApk() },
                onCancelDownload = { updaterViewModel.cancelDownload() },
                onDismissError = { updaterViewModel.clearError() },
                onRetry = { updaterViewModel.checkForUpdates(force = true) }
            )
        }
        
        // Update options - vertically centered
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
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
                        icon = Icons.Filled.Autorenew,
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
        } // End vertically centered content
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
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
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
        // Step indicator dots with enhanced animations
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep
                
                // Animated dot size and color
                val dotSize by animateDpAsState(
                    targetValue = when {
                        isCurrent -> 14.dp
                        isCompleted -> 10.dp
                        else -> 8.dp
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "dotSize_$index"
                )
                
                val dotColor by animateColorAsState(
                    targetValue = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
                    animationSpec = tween(300),
                    label = "dotColor_$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Show checkmark for completed steps
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isCompleted && !isCurrent,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(6.dp)
                        )
                    }
                    
                    // Pulsing ring for current step
                    if (isCurrent) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_$index")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable<Float>(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale_$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(dotSize * pulseScale)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Animated progress text with smooth transitions
        androidx.compose.animation.AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height / 2 } + fadeOut()
                )
            },
            label = "progressText"
        ) { step ->
            Text(
                text = "Step ${step + 1} of $totalSteps",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EnhancedMediaScanContent(
    onNextStep: () -> Unit,
    appSettings: AppSettings,
    onSkip: () -> Unit = {},
    onOpenBottomSheet: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Get current media scan mode preference
    val mediaScanMode by appSettings.mediaScanMode.collectAsState()
    
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Media Scan Filtering",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Filter Your Music Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Control which audio files appear in your library. Use Blacklist mode to hide unwanted files (ringtones, notifications), or Whitelist mode to only show specific music folders.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Media scan configuration card
        Card(
            onClick = onOpenBottomSheet,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Configure Media Scanning",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Choose Blacklist or Whitelist mode • Current: ${mediaScanMode.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open media scan settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cache management info card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Storage Management",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Manage cache size and storage in Settings > Cache Management",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        
        /*Spacer(modifier = Modifier.height(24.dp))
        
        // Skip option with divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "or",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip button
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.labelLarge
            )
        }*/

    }
}

@Composable
fun MediaScanModeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    example: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Animated scale for press effect
    val cardScale = remember { Animatable(1f) }
    
    // Animated colors
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "containerColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary
        else 
            androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                scope.launch {
                    cardScale.animateTo(0.95f, tween(100))
                    cardScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onSelect() 
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(if (isSelected) 3.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedSetupFinishedContent(onFinish: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        // Success icon with animation
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = tween(1000)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Setup Complete",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Rhythm is ready to play! Your music library is being scanned in the background. Here's what's configured:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Feature highlights - vertically centered
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            SetupCompleteFeature(
                icon = Icons.Filled.LibraryMusic,
                title = "Library Configured",
                description = "Your music library organization preferences are set"
            )

            SetupCompleteFeature(
                icon = Icons.Filled.Palette,
                title = "Theme Applied",
                description = "Your custom theme and appearance settings are ready"
            )

            SetupCompleteFeature(
                icon = Icons.Filled.Backup,
                title = "Backup Options",
                description = "Auto-backup and restoration settings configured"
            )

            // SetupCompleteFeature(
            //     icon = Icons.Filled.TouchApp,
            //     title = "Touch & Audio",
            //     description = "Haptic feedback and audio preferences configured"
            // )

        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Next steps card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "What's Next?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                NextStepItem(
                    icon = Icons.Filled.LibraryMusic,
                    text = "Browse your songs, albums, and artists"
                )
                NextStepItem(
                    icon = Icons.Filled.Queue,
                    text = "Create your first playlist"
                )
                NextStepItem(
                    icon = Icons.Filled.GraphicEq,
                    text = "Fine-tune audio with the Equalizer"
                )
                NextStepItem(
                    icon = Icons.Filled.Settings,
                    text = "Explore more settings anytime"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reminder text
        Text(
            text = "All settings can be changed anytime in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Let's Go button removed - now handled by bottom navigation bar
    }
}

@Composable
private fun NextStepItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

/**
 * Material 3 Expressive Update Status UI - No Box/Card containers
 * Uses flowing gradients, dynamic spacing, and expressive animations
 */
@Composable
private fun OnboardingExpressiveUpdateStatus(
    isCheckingForUpdates: Boolean,
    updateAvailable: Boolean,
    latestVersion: chromahub.rhythm.app.viewmodel.AppVersion?,
    currentVersion: chromahub.rhythm.app.viewmodel.AppVersion,
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadedFile: java.io.File?,
    error: String?,
    updaterViewModel: AppUpdaterViewModel,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onCancelDownload: () -> Unit,
    onDismissError: () -> Unit,
    onRetry: () -> Unit
) {
    // Infinite transition for continuous animations
    val infiniteTransition = rememberInfiniteTransition(label = "update_animations")
    
    // Rotating icon for checking state
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulsing scale for attention
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Breathing glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Success scale animation
    val successScale = remember { Animatable(0.7f) }
    LaunchedEffect(downloadedFile) {
        if (downloadedFile != null) {
            successScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    // Main Column - NO BOX OR CARD WRAPPING
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Status Header - flowing layout, no containers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Floating animated icon - direct render, no background
            Icon(
                imageVector = when {
                    error != null -> Icons.Filled.BugReport
                    downloadedFile != null -> Icons.Filled.CheckCircle
                    updateAvailable -> RhythmIcons.Download
                    isCheckingForUpdates || isDownloading -> Icons.Filled.Autorenew
                    else -> Icons.Filled.Check
                },
                contentDescription = null,
                tint = when {
                    error != null -> MaterialTheme.colorScheme.error
                    downloadedFile != null -> MaterialTheme.colorScheme.tertiary
                    updateAvailable -> MaterialTheme.colorScheme.primary
                    isCheckingForUpdates || isDownloading -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .size(64.dp)
                    .rotate(if (isCheckingForUpdates) rotationAngle else 0f)
                    .scale(
                        when {
                            downloadedFile != null -> successScale.value
                            updateAvailable -> pulseScale
                            else -> 1f
                        }
                    )
                    .alpha(if (isCheckingForUpdates) glowAlpha else 1f)
            )
            
            // Status text column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when {
                        error != null -> "Update Check Failed"
                        downloadedFile != null -> "Ready to Install"
                        isDownloading -> "Downloading"
                        isCheckingForUpdates -> "Checking..."
                        updateAvailable -> "Update Available"
                        else -> "Up to Date"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        error != null -> MaterialTheme.colorScheme.error
                        downloadedFile != null -> MaterialTheme.colorScheme.tertiary
                        updateAvailable -> MaterialTheme.colorScheme.primary
                        isCheckingForUpdates || isDownloading -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    letterSpacing = 0.5.sp
                )
                
                Text(
                    text = when {
                        error != null -> error
                        downloadedFile != null -> "v${latestVersion?.versionName ?: "?"}"
                        isDownloading -> "${downloadProgress.toInt()}% • ${((latestVersion?.apkSize ?: 0) * downloadProgress / 100).toLong().let { updaterViewModel.getReadableFileSize(it) }} / ${latestVersion?.let { updaterViewModel.getReadableFileSize(it.apkSize) } ?: ""}"
                        isCheckingForUpdates -> "Fetching from GitHub..."
                        updateAvailable -> "v${latestVersion?.versionName ?: "?"} • ${latestVersion?.let { updaterViewModel.getReadableFileSize(it.apkSize) } ?: ""}"
                        else -> "v${currentVersion.versionName}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        error != null -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        downloadedFile != null -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        updateAvailable -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        isCheckingForUpdates || isDownloading -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Download progress section - expressive, no containers
        AnimatedVisibility(
            visible = isDownloading,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Progress header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chromahub.rhythm.app.ui.components.M3FourColorCircularLoader(
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "In Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Text(
                        text = "${downloadProgress.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                }
                
                // Gradient progress bar using Canvas - no Box container
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                ) {
                    val cornerRadius = 8.dp.toPx()
                    val progressWidth = size.width * (downloadProgress / 100f)
                    
                    // Background track
                    drawRoundRect(
                        color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                    )
                    
                    // Gradient progress
                    if (progressWidth > 0) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color(0xFF2196F3),
                                    androidx.compose.ui.graphics.Color(0xFF9C27B0),
                                    androidx.compose.ui.graphics.Color(0xFF00BCD4)
                                ),
                                endX = progressWidth
                            ),
                            size = androidx.compose.ui.geometry.Size(progressWidth, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                        )
                    }
                }
            }
        }
        
        // Action buttons - expressive, no containers
        AnimatedVisibility(
            visible = error != null || downloadedFile != null || updateAvailable || isDownloading,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn() + scaleIn(initialScale = 0.9f),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    error != null -> {
                        OutlinedButton(
                            onClick = onDismissError,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Dismiss",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retry",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                    
                    downloadedFile != null -> {
                        Button(
                            onClick = onInstall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(successScale.value),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Install Update Now",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    isDownloading -> {
                        OutlinedButton(
                            onClick = onCancelDownload,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Cancel Download",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                    
                    updateAvailable && latestVersion?.apkAssetName?.isNotEmpty() == true -> {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(pulseScale),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Download,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Download Update",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = latestVersion.let { updaterViewModel.getReadableFileSize(it.apkSize) },
                                    fontWeight = FontWeight.Normal,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Subtle gradient divider - no Spacer container
        AnimatedVisibility(
            visible = isCheckingForUpdates || isDownloading || updateAvailable || downloadedFile != null || error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            ) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun SetupCompleteFeature(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
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
                modifier = Modifier.size(24.dp)
            )

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
