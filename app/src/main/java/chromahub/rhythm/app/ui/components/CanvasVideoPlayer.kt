package chromahub.rhythm.app.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * A composable that displays a looping video player for Spotify Canvas videos
 */
@Composable
fun CanvasVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    cornerRadius: androidx.compose.ui.unit.Dp = 28.dp,
    onVideoReady: (() -> Unit)? = null,
    onVideoError: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }
    var isVideoLoaded by remember(videoUrl) { mutableStateOf(false) }

    // Initialize ExoPlayer
    LaunchedEffect(videoUrl) {
        Log.d("CanvasVideoPlayer", "Initializing video player for: $videoUrl")
        isVideoLoaded = false
        
        try {
            val player = ExoPlayer.Builder(context)
                .build()
                .apply {
                    val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
                    setMediaItem(mediaItem)
                    prepare()
                    repeatMode = Player.REPEAT_MODE_ONE // Loop the video
                    volume = 0f // Canvas videos are silent
                    
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_READY -> {
                                    Log.d("CanvasVideoPlayer", "Video ready")
                                    isVideoLoaded = true
                                    onVideoReady?.invoke()
                                }
                                Player.STATE_ENDED -> {
                                    Log.d("CanvasVideoPlayer", "Video ended")
                                }
                            }
                        }
                        
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e("CanvasVideoPlayer", "Video playback error: ${error.message}")
                            onVideoError?.invoke()
                        }
                    })
                }
            
            exoPlayer = player
        } catch (e: Exception) {
            Log.e("CanvasVideoPlayer", "Failed to initialize video player: ${e.message}")
            onVideoError?.invoke()
        }
    }

    // Control playback based on isPlaying state
    LaunchedEffect(isPlaying, exoPlayer) {
        exoPlayer?.let { player ->
            if (isPlaying && isVideoLoaded) {
                player.play()
            } else {
                player.pause()
            }
        }
    }

    // Cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            Log.d("CanvasVideoPlayer", "Disposing video player")
            exoPlayer?.release()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Loading state with animated indicator
        if (!isVideoLoaded && exoPlayer != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                    .alpha(
                        animateFloatAsState(
                            targetValue = if (isVideoLoaded) 0f else 1f,
                            animationSpec = tween(600),
                            label = "loading_alpha"
                        ).value
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .scale(
                            animateFloatAsState(
                                targetValue = if (isVideoLoaded) 0f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "loading_scale"
                            ).value
                        ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    strokeWidth = 2.dp
                )
            }
        }
        
        // Video player with smooth entrance animation
        if (isVideoLoaded && exoPlayer != null) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.Transparent)
                    .alpha(
                        animateFloatAsState(
                            targetValue = if (isVideoLoaded) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 800,
                                delayMillis = 200,
                                easing = FastOutSlowInEasing
                            ),
                            label = "video_alpha"
                        ).value
                    )
                    .scale(
                        animateFloatAsState(
                            targetValue = if (isVideoLoaded) 1f else 0.92f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "video_scale"
                        ).value
                    )
            )
        }
    }
}

/**
 * Simplified canvas player that handles common use cases with enhanced animations
 */
@Composable
fun CanvasPlayer(
    videoUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 28.dp
) {
    var isVisible by remember(videoUrl) { mutableStateOf(false) }
    
    LaunchedEffect(videoUrl) {
        isVisible = !videoUrl.isNullOrBlank()
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.9f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutLinearInEasing
            )
        ) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(400)
        )
    ) {
        if (!videoUrl.isNullOrBlank()) {
            CanvasVideoPlayer(
                videoUrl = videoUrl,
                isPlaying = isPlaying,
                cornerRadius = cornerRadius,
                modifier = modifier,
                onVideoReady = {
                    Log.d("CanvasPlayer", "Canvas video is ready and animated in")
                },
                onVideoError = {
                    Log.w("CanvasPlayer", "Canvas video failed to load, will fade out")
                    isVisible = false
                }
            )
        }
    }
}
