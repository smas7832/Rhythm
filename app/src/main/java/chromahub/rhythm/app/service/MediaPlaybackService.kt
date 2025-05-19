package chromahub.rhythm.app.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media3.common.AudioAttributes as ExoPlayerAudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ConcurrentHashMap

class MediaPlaybackService : MediaLibraryService() {
    private val TAG = "MediaPlaybackService"
    private var mediaSession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer
    
    // Track external files that have been played
    private val externalUriCache = ConcurrentHashMap<String, MediaItem>()
    
    // Audio focus variables
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioManager: AudioManager? = null
    private var playOnAudioFocusGain = false
    
    // SharedPreferences keys
    companion object {
        private const val PREF_NAME = "rhythm_preferences"
        private const val PREF_HIGH_QUALITY_AUDIO = "high_quality_audio"
        private const val PREF_GAPLESS_PLAYBACK = "gapless_playback"
        private const val PREF_CROSSFADE = "crossfade"
        private const val PREF_CROSSFADE_DURATION = "crossfade_duration"
        private const val PREF_AUDIO_NORMALIZATION = "audio_normalization"
        private const val PREF_REPLAY_GAIN = "replay_gain"
        
        // Intent action for updating settings
        const val ACTION_UPDATE_SETTINGS = "chromahub.rhythm.app.action.UPDATE_SETTINGS"
        
        // Intent action for playing external files
        const val ACTION_PLAY_EXTERNAL_FILE = "chromahub.rhythm.app.action.PLAY_EXTERNAL_FILE"
        
        // Intent action for initializing the service
        const val ACTION_INIT_SERVICE = "chromahub.rhythm.app.action.INIT_SERVICE"
    }

    // Audio focus change listener
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "Audio focus gained")
                if (playOnAudioFocusGain) {
                    player.volume = 1.0f
                    player.play()
                    playOnAudioFocusGain = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "Audio focus lost")
                // Save state and pause
                playOnAudioFocusGain = player.isPlaying
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "Audio focus lost temporarily")
                // Save state and pause
                playOnAudioFocusGain = player.isPlaying
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "Audio focus loss - can duck")
                // Lower volume but continue playing
                player.volume = 0.3f
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        // Initialize AudioManager for audio focus
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        try {
            // Get settings from SharedPreferences
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val enableHighQualityAudio = prefs.getBoolean(PREF_HIGH_QUALITY_AUDIO, true)
            val enableGaplessPlayback = prefs.getBoolean(PREF_GAPLESS_PLAYBACK, true)
            val enableCrossfade = prefs.getBoolean(PREF_CROSSFADE, false)
            val crossfadeDuration = prefs.getFloat(PREF_CROSSFADE_DURATION, 2f)
            val enableAudioNormalization = prefs.getBoolean(PREF_AUDIO_NORMALIZATION, true)
            val enableReplayGain = prefs.getBoolean(PREF_REPLAY_GAIN, false)
            
            Log.d(TAG, "Applying settings: " +
                    "HQ Audio=$enableHighQualityAudio, " +
                    "Gapless=$enableGaplessPlayback, " +
                    "Crossfade=$enableCrossfade (${crossfadeDuration}s), " +
                    "Normalization=$enableAudioNormalization, " +
                    "ReplayGain=$enableReplayGain")
            
            // Initialize the player with settings
            try {
                player = ExoPlayer.Builder(this)
                    .setAudioAttributes(
                        ExoPlayerAudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .build(),
                        /* handleAudioFocus= */ false // We'll handle audio focus manually
                    )
                    .setHandleAudioBecomingNoisy(true) // Pause when headphones disconnected
                    .build()
                    .apply {
                        // Set repeat mode and shuffle
                        repeatMode = Player.REPEAT_MODE_OFF
                        shuffleModeEnabled = false
                        
                        // Apply audio normalization if enabled
                        if (enableAudioNormalization) {
                            // In a real implementation, we would configure audio processors here
                            // This is a simplified example
                            volume = 1.0f
                        }
                        
                        // Apply crossfade settings if enabled
                        if (enableCrossfade) {
                            // ExoPlayer doesn't have built-in crossfade, but in a real app
                            // we would implement a custom crossfade solution
                            // For example, using multiple players or audio mixers
                            Log.d(TAG, "Crossfade enabled with duration: $crossfadeDuration seconds")
                        }
                        
                        // Add a listener for playback state changes
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                when (playbackState) {
                                    Player.STATE_READY -> {
                                        Log.d(TAG, "Playback ready")
                                        if (player.playWhenReady) {
                                            Log.d(TAG, "Starting playback")
                                            requestAudioFocus()
                                        }
                                    }
                                    Player.STATE_ENDED -> {
                                        Log.d(TAG, "Playback ended")
                                        abandonAudioFocus()
                                    }
                                    Player.STATE_BUFFERING -> {
                                        Log.d(TAG, "Playback buffering")
                                    }
                                    Player.STATE_IDLE -> {
                                        Log.d(TAG, "Playback idle")
                                    }
                                }
                            }
                            
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                Log.d(TAG, "isPlaying changed to: $isPlaying")
                                if (isPlaying) {
                                    requestAudioFocus()
                                }
                            }
                            
                            override fun onRepeatModeChanged(repeatMode: Int) {
                                Log.d(TAG, "Repeat mode changed in service to: $repeatMode (${
                                    when(repeatMode) {
                                        Player.REPEAT_MODE_OFF -> "OFF"
                                        Player.REPEAT_MODE_ONE -> "ONE"
                                        Player.REPEAT_MODE_ALL -> "ALL"
                                        else -> "UNKNOWN"
                                    }
                                })")
                            }
                            
                            override fun onPlayerError(error: PlaybackException) {
                                Log.e(TAG, "Player error: ${error.message} (code: ${error.errorCode})", error)
                                
                                // Try to recover from the error
                                when (error.errorCode) {
                                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                                        Log.d(TAG, "Network error, attempting to retry")
                                        player.prepare()
                                    }
                                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                                        Log.e(TAG, "Media file not found")
                                        // Skip to next if current item causes error
                                        if (player.hasNextMediaItem()) {
                                            player.seekToNext()
                                        }
                                    }
                                    else -> {
                                        // For other errors, try to reset the player
                                        Log.d(TAG, "Attempting to recover from error")
                                        try {
                                            player.stop()
                                            player.clearMediaItems()
                                            player.prepare()
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Failed to recover from player error", e)
                                        }
                                    }
                                }
                            }
                            
                            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                                // Cache the media item if it's from an external URI
                                player.currentMediaItem?.let { mediaItem ->
                                    if (mediaItem.localConfiguration?.uri?.scheme == "content" || 
                                        mediaItem.localConfiguration?.uri?.scheme == "file") {
                                        val uri = mediaItem.localConfiguration?.uri?.toString() ?: return
                                        if (!uri.contains("media/external/audio/media")) {
                                            // This is likely an external file, cache it
                                            externalUriCache[uri] = mediaItem
                                            Log.d(TAG, "Cached external media item: $uri")
                                        }
                                    }
                                }
                            }
                        })
                    }
                
                Log.d(TAG, "ExoPlayer initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing ExoPlayer", e)
                // Create a minimal player as fallback
                player = ExoPlayer.Builder(this).build()
            }

            // Create the media session
            try {
                mediaSession = MediaLibrarySession.Builder(
                    this,
                    player,
                    MediaSessionCallback()
                ).build()
                
                Log.d(TAG, "Media session created")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating media session", e)
                // Try with a simpler configuration
                try {
                    mediaSession = MediaLibrarySession.Builder(
                        this,
                        player,
                        MediaSessionCallback()
                    ).build()
                } catch (e2: Exception) {
                    Log.e(TAG, "Fatal error creating media session", e2)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating media playback service", e)
            // Try to recover by creating a minimal player
            try {
                player = ExoPlayer.Builder(this).build()
                mediaSession = MediaLibrarySession.Builder(
                    this,
                    player,
                    MediaSessionCallback()
                ).build()
                Log.d(TAG, "Created fallback player after error")
            } catch (e2: Exception) {
                Log.e(TAG, "Fatal error creating media playback service", e2)
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with command: ${intent?.action}")
        
        // Check if this is a settings update intent
        when (intent?.action) {
            ACTION_UPDATE_SETTINGS -> {
                updatePlaybackSettings()
            }
            ACTION_PLAY_EXTERNAL_FILE -> {
                intent.data?.let { uri ->
                    playExternalFile(uri)
                }
            }
            ACTION_INIT_SERVICE -> {
                Log.d(TAG, "Service initialization requested")
                // Nothing specific to do, just keeping the service alive
            }
        }
        
        // Always return START_STICKY to ensure the service restarts if killed
        return START_STICKY
    }
    
    /**
     * Play an external audio file
     */
    private fun playExternalFile(uri: Uri) {
        Log.d(TAG, "Playing external file: $uri")
        
        // Release audio focus first to ensure we can request it again
        abandonAudioFocus()
        
        // Wait longer before requesting audio focus again
        try {
            Thread.sleep(500)  // Increased from 100ms to 500ms
        } catch (e: InterruptedException) {
            Log.w(TAG, "Sleep interrupted while waiting for audio focus", e)
        }
        
        // Request audio focus before playing
        if (!requestAudioFocus()) {
            Log.e(TAG, "Failed to obtain audio focus, will retry once more")
            try {
                Thread.sleep(1000)  // Wait even longer for second attempt
                if (!requestAudioFocus()) {
                    Log.e(TAG, "Failed to obtain audio focus after retry")
                    return
                }
            } catch (e: InterruptedException) {
                Log.w(TAG, "Sleep interrupted during audio focus retry", e)
                return
            }
        }
        
        // Check if we've seen this URI before
        val cachedItem = externalUriCache[uri.toString()]
        if (cachedItem != null) {
            Log.d(TAG, "Using cached media item for URI: $uri")
            
            // Clear the player first to avoid conflicts with existing items
            player.clearMediaItems()
            
            // Play the media item
            player.setMediaItem(cachedItem)
            player.prepare()
            player.play()
            
            return
        }
        
        // Extract metadata from the audio file
        try {
            val song = chromahub.rhythm.app.util.MediaUtils.extractMetadataFromUri(this, uri)
            Log.d(TAG, "Extracted metadata for external file: ${song.title} by ${song.artist}")
            
            // Create a media item with the extracted metadata
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaId(uri.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.artworkUri)
                        .build()
                )
                .build()
            
            // Clear the player first to avoid conflicts with existing items
            player.clearMediaItems()
            
            // Play the media item
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            // Cache the media item
            externalUriCache[uri.toString()] = mediaItem
            
            // Force a recheck of playback state in case it doesn't start
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        Log.d(TAG, "Playback ready, ensuring play is called")
                        player.play()
                        player.removeListener(this)
                    }
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting metadata from external file", e)
            
            // Fall back to basic playback if metadata extraction fails
            val mimeType = contentResolver.getType(uri)
            Log.d(TAG, "Falling back to basic playback with mime type: $mimeType")
            
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(uri.lastPathSegment ?: "Unknown")
                        .build()
                )
                .build()
            
            // Clear the player first to avoid conflicts with existing items
            player.clearMediaItems()
            
            // Play the media item
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            // Cache the media item
            externalUriCache[uri.toString()] = mediaItem
        }
    }
    
    /**
     * Updates playback settings from SharedPreferences
     */
    private fun updatePlaybackSettings() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val enableHighQualityAudio = prefs.getBoolean(PREF_HIGH_QUALITY_AUDIO, true)
        val enableGaplessPlayback = prefs.getBoolean(PREF_GAPLESS_PLAYBACK, true)
        val enableCrossfade = prefs.getBoolean(PREF_CROSSFADE, false)
        val crossfadeDuration = prefs.getFloat(PREF_CROSSFADE_DURATION, 2f)
        val enableAudioNormalization = prefs.getBoolean(PREF_AUDIO_NORMALIZATION, true)
        val enableReplayGain = prefs.getBoolean(PREF_REPLAY_GAIN, false)
        
        Log.d(TAG, "Updating playback settings: " +
                "HQ Audio=$enableHighQualityAudio, " +
                "Gapless=$enableGaplessPlayback, " +
                "Crossfade=$enableCrossfade (${crossfadeDuration}s), " +
                "Normalization=$enableAudioNormalization, " +
                "ReplayGain=$enableReplayGain")
        
        // Apply audio normalization
        if (enableAudioNormalization) {
            // In a real implementation, we would configure audio processors here
            player.volume = 1.0f
        }
        
        // Note: Some settings like gapless playback require player recreation
        // In a real app, we would handle this more gracefully
    }

    override fun onDestroy() {
        Log.d(TAG, "Service being destroyed")
        mediaSession?.run {
            abandonAudioFocus()
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = mediaSession

    private inner class MediaSessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.d(TAG, "onConnect: ${controller.packageName}")
            return super.onConnect(session, controller)
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            Log.d(TAG, "onDisconnected: ${controller.packageName}")
            super.onDisconnected(session, controller)
        }
        
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            Log.d(TAG, "onAddMediaItems: ${mediaItems.size} items")
            
            val updatedMediaItems = mediaItems.map { mediaItem ->
                if (mediaItem.requestMetadata.searchQuery != null) {
                    // This is a search request
                    Log.d(TAG, "Search request: ${mediaItem.requestMetadata.searchQuery}")
                    mediaItem
                } else if (mediaItem.mediaId.isNotEmpty()) {
                    // Check if this is an external URI that we've cached
                    val cachedItem = externalUriCache[mediaItem.mediaId]
                    cachedItem ?: mediaItem
                } else {
                    mediaItem
                }
            }
            
            return Futures.immediateFuture(updatedMediaItems)
        }
        
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: androidx.media3.session.MediaLibraryService.LibraryParams?
        ): ListenableFuture<androidx.media3.session.LibraryResult<MediaItem>> {
            Log.d(TAG, "onGetLibraryRoot from ${browser.packageName}")
            
            // Create a root media item
            val rootItem = MediaItem.Builder()
                .setMediaId("root")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Rhythm Music Library")
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .build()
                )
                .build()
                
            return Futures.immediateFuture(androidx.media3.session.LibraryResult.ofItem(rootItem, params))
        }
    }
    
    /**
     * Requests audio focus using the appropriate API based on Android version
     */
    private fun requestAudioFocus(): Boolean {
        audioManager?.let { am ->
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                
                audioFocusRequest?.let { request ->
                    am.requestAudioFocus(request)
                } ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
            
            val success = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (!success) {
                Log.w(TAG, "Failed to get audio focus, result: $result")
            } else {
                Log.d(TAG, "Audio focus request granted")
            }
            return success
        }
        return false
    }
    
    /**
     * Abandons audio focus using the appropriate API based on Android version
     */
    private fun abandonAudioFocus() {
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    am.abandonAudioFocusRequest(request)
                    Log.d(TAG, "Abandoned audio focus using API 26+ method")
                }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(audioFocusChangeListener)
                Log.d(TAG, "Abandoned audio focus using legacy method")
            }
        }
    }
} 