package chromahub.rhythm.app.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MediaPlaybackService : MediaLibraryService() {
    private val TAG = "MediaPlaybackService"
    private var mediaSession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer
    
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
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
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
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus= */ true
            )
            .build()
            .apply {
                // Set repeat mode and shuffle
                repeatMode = Player.REPEAT_MODE_ALL
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
                                }
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "Playback ended")
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
                    }
                    
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e(TAG, "Player error: ${error.message}")
                    }
                })
            }

        // Create the media session
        mediaSession = MediaLibrarySession.Builder(
            this,
            player,
            MediaSessionCallback()
        ).build()
        
        Log.d(TAG, "Media session created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with command")
        
        // Check if this is a settings update intent
        if (intent?.action == ACTION_UPDATE_SETTINGS) {
            updatePlaybackSettings()
        }
        
        return super.onStartCommand(intent, flags, startId)
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
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        Log.d(TAG, "onGetSession called by ${controllerInfo.packageName}")
        return mediaSession
    }

    private inner class MediaSessionCallback : MediaLibrarySession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            Log.d(TAG, "onAddMediaItems called with ${mediaItems.size} items")
            
            val updatedMediaItems = mediaItems.map { mediaItem ->
                // If the item has a mediaUri in the request metadata, use it
                val mediaUri = mediaItem.requestMetadata.mediaUri
                if (mediaUri != null) {
                    Log.d(TAG, "Adding media item with URI: $mediaUri")
                    mediaItem.buildUpon().setUri(mediaUri).build()
                } else {
                    Log.w(TAG, "Media item has no URI: ${mediaItem.mediaId}")
                    mediaItem
                }
            }
            
            return Futures.immediateFuture(updatedMediaItems)
        }
        
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.d(TAG, "Controller connected: ${controller.packageName}")
            return super.onConnect(session, controller)
        }
    }
} 