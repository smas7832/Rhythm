package chromahub.rhythm.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.HapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesBottomSheet(
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = RhythmIcons.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Open Source Libraries",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Rhythm is built with amazing open source libraries. We're grateful to the following projects and their contributors:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // List of libraries
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LicenseSheetItem(
                    name = "Jetpack Compose",
                    description = "Android's modern toolkit for building native UI",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/jetpack/compose",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Material 3 Components",
                    description = "Material Design 3 components for Android",
                    license = "Apache License 2.0",
                    url = "https://m3.material.io/",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Media3 ExoPlayer",
                    description = "Modern media playback library for Android",
                    license = "Apache License 2.0",
                    url = "https://github.com/androidx/media",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Kotlin Coroutines",
                    description = "Asynchronous programming framework for Kotlin",
                    license = "Apache License 2.0",
                    url = "https://github.com/Kotlin/kotlinx.coroutines",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Coil",
                    description = "Image loading library for Android backed by Kotlin Coroutines",
                    license = "Apache License 2.0",
                    url = "https://coil-kt.github.io/coil/",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Retrofit",
                    description = "Type-safe HTTP client for Android and Java",
                    license = "Apache License 2.0",
                    url = "https://square.github.io/retrofit/",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "OkHttp",
                    description = "HTTP client for Android, Kotlin, and Java",
                    license = "Apache License 2.0",
                    url = "https://square.github.io/okhttp/",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Gson",
                    description = "Java serialization/deserialization library for JSON",
                    license = "Apache License 2.0",
                    url = "https://github.com/google/gson",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "AndroidX Navigation",
                    description = "Navigation components for Android apps",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/guide/navigation",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "Accompanist Permissions",
                    description = "Compose utilities for permissions handling",
                    license = "Apache License 2.0",
                    url = "https://google.github.io/accompanist/permissions/",
                    context = context,
                    haptic = haptic
                )
                
                LicenseSheetItem(
                    name = "AndroidX Palette",
                    description = "Library to extract prominent colors from images",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/jetpack/androidx/releases/palette",
                    context = context,
                    haptic = haptic
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Apache License 2.0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "All libraries used in Rhythm are licensed under the Apache License 2.0, which permits use, reproduction, and distribution with proper attribution.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LicenseSheetItem(
    name: String,
    description: String,
    license: String,
    url: String,
    context: android.content.Context,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = license,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = RhythmIcons.ArtistFilled,
                contentDescription = "View License",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
