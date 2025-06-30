package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.R
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import chromahub.rhythm.app.data.AppSettings
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.viewmodel.AppUpdaterViewModel
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    showLyrics: Boolean,
    showOnlineOnlyLyrics: Boolean,
    onShowLyricsChange: (Boolean) -> Unit,
    onShowOnlineOnlyLyricsChange: (Boolean) -> Unit,
    useSystemTheme: Boolean,
    darkMode: Boolean,
    onUseSystemThemeChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onOpenSystemEqualizer: () -> Unit,
    onBack: () -> Unit,
    onCheckForUpdates: () -> Unit
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val spotifyKey by appSettings.spotifyApiKey.collectAsState()
    val scope = rememberCoroutineScope()
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(spotifyKey ?: "") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var checkResult: Boolean? by remember { mutableStateOf(null) } // null not checked
    var errorCode: Int? by remember { mutableStateOf(null) } // hold HTTP error for messaging

    val updaterViewModel: AppUpdaterViewModel = viewModel()
    val currentAppVersion by updaterViewModel.currentVersion.collectAsState()

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { if(!isChecking) showApiKeyDialog = false },
            title = {
                Text(
                    text = "Spotify RapidAPI Key",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Obtain a free RapidAPI key for the Spotify23 API (\u2192 Open the \u201cPlayground\u201d and copy your Personal key).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val linkColor = MaterialTheme.colorScheme.primary
                    val annotatedLink = buildAnnotatedString {
                        append("More info")
                        addStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium), 0, length)
                        addStringAnnotation("URL", "https://rapidapi.com/Glavier/api/spotify23/playground", 0, length)
                    }
                    ClickableText(text = annotatedLink, style = MaterialTheme.typography.bodySmall) { offset ->
                        annotatedLink.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { anno ->
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(anno.item))
                            context.startActivity(intent)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            checkResult = null // reset result when editing
                        },
                        singleLine = true,
                        placeholder = { Text("Enter API key") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isChecking -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying key…")
                            }
                        }
                        checkResult == true -> {
                            Text("Key valid!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        checkResult == false -> {
                            val msg = when (errorCode) {
                                429 -> "Monthly limit exceeded for this key. Using default key."
                                500 -> "RapidAPI server error. Please try again later."
                                else -> "Key invalid. We'll keep using the default key."
                            }
                            Text(msg, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isChecking) return@Button
                        isChecking = true
                        checkResult = null
                        scope.launch {
                            val code = verifySpotifyKey(apiKeyInput.trim())
                            val ok = code == HttpURLConnection.HTTP_OK
                            checkResult = ok
                            errorCode = if (ok) null else code
                            if (ok) {
                                appSettings.setSpotifyApiKey(apiKeyInput.trim())
                                showApiKeyDialog = false
                            } else {
                                appSettings.setSpotifyApiKey(null) // fallback
                            }
                            isChecking = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text(if (isChecking) "Verifying…" else "Save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { if(!isChecking) showApiKeyDialog = false }) {
                        Text("Cancel")
                    }
                    if (!spotifyKey.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (isChecking) return@Button
                                scope.launch {
                                    appSettings.setSpotifyApiKey(null)
                                    showApiKeyDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text("Remove")
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false },
            onCheckForUpdates = onCheckForUpdates,
            currentAppVersion = currentAppVersion
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    FilledIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Back,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (false /* MiniPlayer handled globally */) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onPlayPause = onPlayPause,
                    onPlayerClick = onPlayerClick,
                    onSkipNext = onSkipNext
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Appearance section
            item {
                SettingsSectionHeader(title = "Appearance")

                SettingsToggleItem(
                    title = "Use system theme",
                    description = "Use Android's Monet theme colors when on, app's colors when off",
                    icon = RhythmIcons.Settings,
                    checked = useSystemTheme,
                    onCheckedChange = {
                        onUseSystemThemeChange(it)
                    }
                )

                AnimatedVisibility(
                    visible = !useSystemTheme,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingsToggleItem(
                        title = "Dark mode",
                        description = "Enable dark theme",
                        icon = RhythmIcons.LocationFilled,
                        checked = darkMode,
                        onCheckedChange = {
                            onDarkModeChange(it)
                        }
                    )
                }

                SettingsDivider()
            }

            // Playback section (only lyrics and equalizer)
            item {
                SettingsSectionHeader(title = "Playback")

                SettingsToggleItem(
                    title = "Show lyrics",
                    description = "Display lyrics when available",
                    icon = RhythmIcons.Queue,
                    checked = showLyrics,
                    onCheckedChange = {
                        onShowLyricsChange(it)
                    }
                )

                AnimatedVisibility(
                    visible = showLyrics,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingsToggleItem(
                        title = "Online lyrics only",
                        description = "Only fetch and display lyrics when connected to internet",
                        icon = RhythmIcons.LocationFilled,
                        checked = showOnlineOnlyLyrics,
                        onCheckedChange = {
                            onShowOnlineOnlyLyricsChange(it)
                        }
                    )
                }

                SettingsClickableItem(
                    title = "Equalizer",
                    description = "Open system equalizer to adjust audio frequencies",
                    icon = RhythmIcons.VolumeUp,
                    onClick = onOpenSystemEqualizer
                )

                SettingsDivider()
            }

            // Integrations section
            item {
                SettingsSectionHeader(title = "Integrations")

                SettingsClickableItem(
                    title = "Spotify RapidAPI Key",
                    description = if (spotifyKey.isNullOrBlank()) "No key set" else "Key set (tap to change)",
                    icon = RhythmIcons.Settings,
                    onClick = {
                        apiKeyInput = spotifyKey ?: ""
                        showApiKeyDialog = true
                    }
                )

                SettingsDivider()
            }

            // About section
            item {
                SettingsSectionHeader(title = "About")

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
                        .clickable { showAboutDialog = true }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rhythm_logo),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Rhythm Music Player",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Version ${currentAppVersion.versionName} | ChromaHub",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAboutDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                .weight(1f)
                            ) {
                                Text("About")
                            }

                            Button(
                                onClick = onCheckForUpdates,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Updates")
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cromaguy/Rhythm/issues"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Edit,
                                        contentDescription = "Report Bug",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Report Bug")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Extra bottom space for mini player
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .height(2.dp)
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (checked) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "toggleAnimation"
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
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text
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

            // Switch
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(scaleAnim),
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
fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
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
            // Icon
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text
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

            // Arrow icon
            Icon(
                imageVector = RhythmIcons.Forward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsDivider() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    currentAppVersion: chromahub.rhythm.app.viewmodel.AppVersion // Pass currentAppVersion
) {
    val context = LocalContext.current // Get context for opening URL
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = null,
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Use the splash logo for a more impressive visual
                Image(
                    painter = painterResource(id = R.drawable.rhythm_splash_logo),
                    contentDescription = null,
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rhythm",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Music Player",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Version ${currentAppVersion.versionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Add a check for updates button
                Button(
                    onClick = {
                        onDismiss()  // Close the dialog first
                        onCheckForUpdates()  // Then navigate to updates screen
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check for Updates")
                    }
                }

                // Add a Report Bug button inside the dialog
                Button(
                    onClick = {
                        onDismiss() // Close the dialog first
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cromaguy/Rhythm/issues"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Edit,
                            contentDescription = "Report Bug",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report Bug")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "By Anjishnu Nandi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A modern music player showcasing Material 3 Expressive design with physics-based animations.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Song,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Playlist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Simple network check for the RapidAPI key. Uses java.net.HttpURLConnection to avoid
 * Retrofit dependency here. Performs a small search query and returns true if HTTP 200.
 */
suspend fun verifySpotifyKey(key: String): Int? {
    if (key.isBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("https://spotify23.p.rapidapi.com/artist_images/?id=6eUKZXaKkcviH0Ku9w2n3V")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("X-RapidAPI-Key", key)
            conn.setRequestProperty("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.responseCode
        }.getOrNull()
    }
}
