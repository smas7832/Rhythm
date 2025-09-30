package chromahub.rhythm.app.service

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.media3.session.MediaNotification
import androidx.media3.session.DefaultMediaNotificationProvider
import chromahub.rhythm.app.MainActivity
import chromahub.rhythm.app.data.AppSettings
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes as ExoAudioAttributes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import chromahub.rhythm.app.data.Playlist

@OptIn(UnstableApi::class)
class MediaPlaybackService : MediaLibraryService(), Player.Listener {
    private var mediaSession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer
    private lateinit var customCommands: List<CommandButton>

    private var controller: MediaController? = null
    
    // Service-scoped coroutine scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Track current custom layout state to avoid unnecessary updates
    private var lastShuffleState: Boolean? = null
    private var lastRepeatMode: Int? = null
    private var lastFavoriteState: Boolean? = null
    
    // Debounce custom layout updates to prevent flickering
    private var updateLayoutJob: Job? = null
    
    // Sleep Timer functionality
    private var sleepTimerJob: Job? = null
    private var sleepTimerDurationMs: Long = 0L
    private var sleepTimerStartTime: Long = 0L
    private var fadeOutEnabled: Boolean = true
    private var pauseOnlyEnabled: Boolean = false
    
    // Audio effects (for equalizer integration)
    private var equalizer: android.media.audiofx.Equalizer? = null
    private var bassBoost: android.media.audiofx.BassBoost? = null
    private var virtualizer: android.media.audiofx.Virtualizer? = null
    
    // BroadcastReceiver to listen for favorite changes from ViewModel
    private val favoriteChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "chromahub.rhythm.app.action.FAVORITE_CHANGED" -> {
                    Log.d(TAG, "Received favorite change notification from ViewModel")
                    // Use debounced update to prevent conflicts with other updates
                    scheduleCustomLayoutUpdate(250) // Longer delay for external changes
                }
            }
        }
    }

    private val repeatCommand: CommandButton
        get() = when (val mode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF) {
            Player.REPEAT_MODE_OFF -> customCommands[2]
            Player.REPEAT_MODE_ALL -> customCommands[3]
            Player.REPEAT_MODE_ONE -> customCommands[4]
            else -> customCommands[2] // Fallback to REPEAT_MODE_OFF command
        }

    private val shuffleCommand: CommandButton
        get() = if (controller?.shuffleModeEnabled == true) {
            customCommands[1]
        } else {
            customCommands[0]
        }

    private fun getCurrentFavoriteCommand(): CommandButton {
        return if (isCurrentSongFavorite()) {
            customCommands[6] // Remove from favorites (filled heart)
        } else {
            customCommands[5] // Add to favorites (heart outline)
        }
    }

    // Track external files that have been played
    private val externalUriCache = ConcurrentHashMap<String, MediaItem>()

    // Settings manager
    private lateinit var appSettings: AppSettings
    
    // Custom notification provider for app-specific notifications
    private var customNotificationProvider: DefaultMediaNotificationProvider? = null
    
    // SharedPreferences keys
    companion object {
        private const val TAG = "MediaPlaybackService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "RhythmMediaPlayback"

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
        
        // Intent actions for sleep timer
        const val ACTION_START_SLEEP_TIMER = "chromahub.rhythm.app.action.START_SLEEP_TIMER"
        const val ACTION_STOP_SLEEP_TIMER = "chromahub.rhythm.app.action.STOP_SLEEP_TIMER"
        
        // Intent actions for equalizer
        const val ACTION_SET_EQUALIZER_ENABLED = "chromahub.rhythm.app.action.SET_EQUALIZER_ENABLED"
        const val ACTION_SET_EQUALIZER_BAND = "chromahub.rhythm.app.action.SET_EQUALIZER_BAND"
        const val ACTION_SET_BASS_BOOST = "chromahub.rhythm.app.action.SET_BASS_BOOST"
        const val ACTION_SET_VIRTUALIZER = "chromahub.rhythm.app.action.SET_VIRTUALIZER"
        const val ACTION_APPLY_EQUALIZER_PRESET = "chromahub.rhythm.app.action.APPLY_EQUALIZER_PRESET"
        
        // Broadcast actions for status updates
        const val BROADCAST_SLEEP_TIMER_STATUS = "chromahub.rhythm.app.broadcast.SLEEP_TIMER_STATUS"
        const val EXTRA_TIMER_ACTIVE = "timer_active"
        const val EXTRA_REMAINING_TIME = "remaining_time"

        // Playback custom commands
        const val REPEAT_MODE_ALL = "repeat_all"
        const val REPEAT_MODE_ONE = "repeat_one"
        const val REPEAT_MODE_OFF = "repeat_off"
        const val SHUFFLE_MODE_ON = "shuffle_on"
        const val SHUFFLE_MODE_OFF = "shuffle_off"
        const val FAVORITE_ON = "favorite_on"
        const val FAVORITE_OFF = "favorite_off"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Create notification channel first (required for Android 8.0+)
        createNotificationChannel()

        // Start foreground immediately to avoid ANR
        startForeground("Rhythm Music", "Starting service...")

        // Initialize settings manager (fast operation)
        updateForegroundNotification("Rhythm Music", "Loading settings...")
        appSettings = AppSettings.getInstance(applicationContext)

        // Register BroadcastReceiver for favorite changes
        updateForegroundNotification("Rhythm Music", "Setting up components...")
        val filter = IntentFilter("chromahub.rhythm.app.action.FAVORITE_CHANGED")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(favoriteChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(favoriteChangeReceiver, filter)
        }

        try {
            // Initialize core components on main thread (required for media service)
            updateForegroundNotification("Rhythm Music", "Initializing player...")
            initializePlayer()

            updateForegroundNotification("Rhythm Music", "Creating playback controls...")
            createCustomCommands()

            // Create the media session (required synchronously)
            updateForegroundNotification("Rhythm Music", "Setting up media session...")
            mediaSession = createMediaSession()

            // Initialize controller asynchronously to avoid blocking
            updateForegroundNotification("Rhythm Music", "Initializing media controller...")
            createController()

            // Observe notification preference changes
            updateForegroundNotification("Rhythm Music", "Service ready")
            observeNotificationSettings()

            Log.d(TAG, "Service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing service", e)
            updateForegroundNotification("Rhythm Music", "Initialization failed")
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rhythm Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForeground(title: String = "Rhythm Music", content: String = "Initializing music service...") {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(chromahub.rhythm.app.R.drawable.ic_notification)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Started foreground service: $title - $content")
    }

    private fun updateForegroundNotification(title: String, content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(chromahub.rhythm.app.R.drawable.ic_notification)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Updated foreground notification: $title - $content")
    }
    
    private fun observeNotificationSettings() {
        serviceScope.launch {
            appSettings.useCustomNotification.collect { useCustomNotification ->
                Log.d(TAG, "Notification preference changed: $useCustomNotification")
                updateNotificationProvider(useCustomNotification)
            }
        }
    }
    
    private fun updateNotificationProvider(useCustomNotification: Boolean) {
        try {
            if (useCustomNotification && customNotificationProvider == null) {
                // Switch to custom notification provider
                customNotificationProvider = DefaultMediaNotificationProvider.Builder(this)
                    .setChannelId(CHANNEL_ID)
                    .setNotificationId(NOTIFICATION_ID)
                    .build()
                Log.d(TAG, "Switched to custom notification provider")
            } else if (!useCustomNotification && customNotificationProvider != null) {
                // Switch back to system media notifications
                customNotificationProvider = null
                Log.d(TAG, "Switched to system media notifications")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification provider", e)
        }
    }
    
    private fun initializePlayer() {
        // Build the player with current settings
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                ExoAudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            
        // Add listener to initialize audio effects when session ID is ready
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && player.audioSessionId != 0) {
                    // Reinitialize audio effects with valid session ID
                    Log.d(TAG, "Player ready with session ID ${player.audioSessionId}, reinitializing effects")
                    initializeAudioEffects()
                }
            }
        })
        
        // Apply current settings
        applyPlayerSettings()
        
        // Try to initialize audio effects (might fail if session ID not ready)
        initializeAudioEffects()
    }

    private fun createController() {
        // Build the controller asynchronously to avoid blocking the main thread
        val controllerFuture = MediaController.Builder(this, mediaSession!!.token)
            .buildAsync()
        
        controllerFuture.addListener({
            try {
                controller = controllerFuture.get()
                controller?.addListener(this)
                // Only set custom layout if controller is properly initialized
                controller?.let {
                    forceCustomLayoutUpdate() // Use force update for initial setup
                }
                Log.d(TAG, "MediaController initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MediaController", e)
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(this))
    }

    private fun createCustomCommands() {
        customCommands = listOf(
            CommandButton.Builder(CommandButton.ICON_SHUFFLE_OFF)
                .setDisplayName("Shuffle mode")
                .setSessionCommand(
                    SessionCommand(SHUFFLE_MODE_ON, Bundle.EMPTY)
                )
                .build(),
            CommandButton.Builder(CommandButton.ICON_SHUFFLE_ON)
                .setDisplayName("Shuffle mode")
                .setSessionCommand(
                    SessionCommand(SHUFFLE_MODE_OFF, Bundle.EMPTY)
                )
                .build(),
            CommandButton.Builder(CommandButton.ICON_REPEAT_OFF)
                .setDisplayName("Repeat mode")
                .setSessionCommand(
                    SessionCommand(REPEAT_MODE_ALL, Bundle.EMPTY)
                )
                .build(),
            CommandButton.Builder(CommandButton.ICON_REPEAT_ALL)
                .setDisplayName("Repeat mode")
                .setSessionCommand(
                    SessionCommand(REPEAT_MODE_ONE, Bundle.EMPTY)
                )
                .build(),
            CommandButton.Builder(CommandButton.ICON_REPEAT_ONE)
                .setDisplayName("Repeat mode")
                .setSessionCommand(
                    SessionCommand(REPEAT_MODE_OFF, Bundle.EMPTY)
                )
                .build(),
            // Favorite commands
            CommandButton.Builder()
                .setDisplayName("Add to favorites")
                .setIconResId(chromahub.rhythm.app.R.drawable.ic_favorite_border)
                .setSessionCommand(
                    SessionCommand(FAVORITE_ON, Bundle.EMPTY)
                )
                .build(),
            CommandButton.Builder()
                .setDisplayName("Remove from favorites")
                .setIconResId(chromahub.rhythm.app.R.drawable.ic_favorite_filled)
                .setSessionCommand(
                    SessionCommand(FAVORITE_OFF, Bundle.EMPTY)
                )
                .build()
        )
    }

    private fun createMediaSession(): MediaLibrarySession {
        // PendingIntent that launches MainActivity when user taps media controls
        val sessionIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sessionBuilder = MediaLibrarySession.Builder(
            this,
            player,
            MediaSessionCallback()
        ).setSessionActivity(pendingIntent)
        
        // Configure notification provider based on user setting
        if (appSettings.useCustomNotification.value) {
            // Use custom notification provider for app-specific styling
            customNotificationProvider = DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setNotificationId(NOTIFICATION_ID)
                .build()
            // Note: MediaLibrarySession doesn't directly support setMediaNotificationProvider
            // The custom notification provider will be handled through MediaNotificationManager
        }
        // If custom notifications are disabled, Media3 uses system media notifications
        
        return sessionBuilder.build()
    }
    
    private fun isCurrentSongFavorite(): Boolean {
        val currentMediaItem = player.currentMediaItem
        return if (currentMediaItem != null) {
            // Get favorite songs from settings
            val favoriteSongsJson = appSettings.favoriteSongs.value
            if (favoriteSongsJson != null && favoriteSongsJson.isNotEmpty()) {
                try {
                    val type = object : TypeToken<Set<String>>() {}.type
                    val favoriteSongs: Set<String> = Gson().fromJson(favoriteSongsJson, type)
                    favoriteSongs.contains(currentMediaItem.mediaId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing favorite songs", e)
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }
    
    private fun toggleCurrentSongFavorite() {
        val currentMediaItem = player.currentMediaItem
        if (currentMediaItem != null) {
            serviceScope.launch {
                try {
                    // Get current favorites
                    val favoriteSongsJson = appSettings.favoriteSongs.value
                    val currentFavorites = if (favoriteSongsJson != null && favoriteSongsJson.isNotEmpty()) {
                        try {
                            val type = object : TypeToken<Set<String>>() {}.type
                            Gson().fromJson<Set<String>>(favoriteSongsJson, type).toMutableSet()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing favorite songs", e)
                            mutableSetOf<String>()
                        }
                    } else {
                        mutableSetOf<String>()
                    }
                    
                    val songId = currentMediaItem.mediaId
                    val wasRemoving = currentFavorites.contains(songId)
                    
                    // Toggle favorite status
                    if (currentFavorites.contains(songId)) {
                        currentFavorites.remove(songId)
                        Log.d(TAG, "Removed song from favorites via notification: $songId")
                    } else {
                        currentFavorites.add(songId)
                        Log.d(TAG, "Added song to favorites via notification: $songId")
                    }
                    
                    // Save updated favorites
                    val updatedJson = Gson().toJson(currentFavorites)
                    appSettings.setFavoriteSongs(updatedJson)
                    
                    // Also need to update the favorites playlist to stay in sync
                    updateFavoritesPlaylist(songId, !wasRemoving)
                    
                    // Schedule custom layout update with debouncing
                    scheduleCustomLayoutUpdate(200) // Longer delay for favorite changes
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error toggling favorite", e)
                }
            }
        }
    }
    
    private fun updateFavoritesPlaylist(songId: String, isAdding: Boolean) {
        try {
            // Get current playlists
            val playlistsJson = appSettings.playlists.value
            if (playlistsJson.isNullOrEmpty()) return
            
            val type = object : TypeToken<List<Playlist>>() {}.type
            val playlists: MutableList<Playlist> = Gson().fromJson(playlistsJson, type)
            
            // Find and update the Favorites playlist (ID: "1")
            val favoritesPlaylist = playlists.find { it.id == "1" && it.name == "Favorites" }
            if (favoritesPlaylist != null) {
                val updatedPlaylist = if (isAdding) {
                    // Add song to favorites playlist if not already there
                    if (!favoritesPlaylist.songs.any { it.id == songId }) {
                        // Find the song to add (would need access to all songs, this is a limitation)
                        Log.d(TAG, "Would add song $songId to favorites playlist, but song details not available in service")
                        favoritesPlaylist
                    } else {
                        favoritesPlaylist
                    }
                } else {
                    // Remove song from favorites playlist
                    favoritesPlaylist.copy(
                        songs = favoritesPlaylist.songs.filter { it.id != songId },
                        dateModified = System.currentTimeMillis()
                    )
                }
                
                // Update the playlist in the list
                val updatedPlaylists = playlists.map { if (it.id == "1") updatedPlaylist else it }
                val updatedPlaylistsJson = Gson().toJson(updatedPlaylists)
                appSettings.setPlaylists(updatedPlaylistsJson)
                
                Log.d(TAG, "Updated favorites playlist: ${if (isAdding) "added" else "removed"} song $songId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorites playlist", e)
        }
    }
    
    private fun updateCustomLayout() {
        try {
            // Create a new instance of the favorite command to avoid reference issues
            val currentFavoriteCommand = getCurrentFavoriteCommand()
            val currentShuffleCommand = shuffleCommand
            val currentRepeatCommand = repeatCommand
            
            mediaSession?.setCustomLayout(ImmutableList.of(currentShuffleCommand, currentRepeatCommand))
            
            // Update state tracking after successful update
            lastShuffleState = controller?.shuffleModeEnabled ?: false
            lastRepeatMode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
            lastFavoriteState = isCurrentSongFavorite()
            
            val currentMediaItem = player.currentMediaItem
            Log.d(TAG, "Updated custom layout - Song: ${currentMediaItem?.mediaMetadata?.title}, " +
                      "Favorite state: ${lastFavoriteState}, " +
                      "Shuffle: ${lastShuffleState}, " +
                      "Repeat: ${lastRepeatMode}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating custom layout", e)
        }
    }
    
    private fun updateCustomLayoutSmart() {
        // Only update if layout actually needs to change
        // This helps prevent unnecessary recreations and flickering
        mediaSession?.let { session ->
            try {
                val currentShuffleState = controller?.shuffleModeEnabled ?: false
                val currentRepeatMode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
                val currentFavoriteState = isCurrentSongFavorite()
                
                // Check if anything actually changed
                if (currentShuffleState == lastShuffleState &&
                    currentRepeatMode == lastRepeatMode &&
                    currentFavoriteState == lastFavoriteState) {
                    Log.d(TAG, "Custom layout state unchanged, skipping update")
                    return
                }
                
                // Update state tracking
                lastShuffleState = currentShuffleState
                lastRepeatMode = currentRepeatMode
                lastFavoriteState = currentFavoriteState
                
                val currentFavoriteCommand = getCurrentFavoriteCommand()
                val currentShuffleCommand = shuffleCommand
                val currentRepeatCommand = repeatCommand
                
                // Create the layout
                session.setCustomLayout(ImmutableList.of(currentShuffleCommand, currentRepeatCommand))
                
                Log.d(TAG, "Smart updated custom layout - Favorite: $currentFavoriteState, " +
                          "Shuffle: $currentShuffleState, Repeat: $currentRepeatMode")
            } catch (e: Exception) {
                Log.e(TAG, "Error in smart custom layout update", e)
            }
        }
    }
    
    private fun scheduleCustomLayoutUpdate(delayMs: Long = 150) {
        // Cancel any pending update
        updateLayoutJob?.cancel()
        
        // Schedule a new update with debouncing
        updateLayoutJob = serviceScope.launch {
            kotlinx.coroutines.delay(delayMs)
            updateCustomLayoutSmart()
        }
    }
    
    private fun forceCustomLayoutUpdate() {
        // Force an immediate update without debouncing (for initial setup)
        serviceScope.launch {
            updateCustomLayout()
        }
    }
    
    private fun applyPlayerSettings() {
        player.apply {
            // Apply crossfade if enabled
            if (appSettings.crossfade.value) {
                // Note: This is a placeholder. In a real implementation,
                // you would configure the actual crossfade duration
                // using the appSettings.crossfadeDuration.value
            }

            // Apply audio normalization
            if (appSettings.audioNormalization.value) {
                volume = 1.0f
            }

            // Apply replay gain if enabled
            if (appSettings.replayGain.value) {
                // Note: This is a placeholder. In a real implementation,
                // you would configure replay gain processing
            }
        }

        Log.d(TAG, "Applied player settings: " +
                "HQ Audio=${appSettings.highQualityAudio.value}, " +
                "Gapless=${appSettings.gaplessPlayback.value}, " +
                "Crossfade=${appSettings.crossfade.value} (${appSettings.crossfadeDuration.value}s), " +
                "Normalization=${appSettings.audioNormalization.value}, " +
                "ReplayGain=${appSettings.replayGain.value}")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with command: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_UPDATE_SETTINGS -> {
                Log.d(TAG, "Updating service settings")
                applyPlayerSettings()
            }
            ACTION_PLAY_EXTERNAL_FILE -> {
                intent.data?.let { uri ->
                    playExternalFile(uri)
                }
            }
            ACTION_INIT_SERVICE -> {
                Log.d(TAG, "Service initialization requested")
                // Load and apply settings when service starts
                applyPlayerSettings()
            }
            ACTION_START_SLEEP_TIMER -> {
                val durationMs = intent.getLongExtra("duration", 0L)
                val fadeOut = intent.getBooleanExtra("fadeOut", true)
                val pauseOnly = intent.getBooleanExtra("pauseOnly", false)
                if (durationMs > 0) {
                    startSleepTimer(durationMs, fadeOut, pauseOnly)
                }
            }
            ACTION_STOP_SLEEP_TIMER -> {
                stopSleepTimer()
            }
            ACTION_SET_EQUALIZER_ENABLED -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                setEqualizerEnabled(enabled)
            }
            ACTION_SET_EQUALIZER_BAND -> {
                val band = intent.getShortExtra("band", 0)
                val level = intent.getShortExtra("level", 0)
                setEqualizerBandLevel(band, level)
            }
            ACTION_SET_BASS_BOOST -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                val strength = intent.getShortExtra("strength", 0)
                setBassBoostEnabled(enabled)
                if (enabled) setBassBoostStrength(strength)
            }
            ACTION_SET_VIRTUALIZER -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                val strength = intent.getShortExtra("strength", 0)
                setVirtualizerEnabled(enabled)
                if (enabled) setVirtualizerStrength(strength)
            }
            ACTION_APPLY_EQUALIZER_PRESET -> {
                val preset = intent.getStringExtra("preset") ?: ""
                val levels = intent.getFloatArrayExtra("levels")
                if (levels != null && levels.size == 5) {
                    applyEqualizerPreset(levels)
                }
            }
        }
        
        // We make sure to call the super implementation
        return super.onStartCommand(intent, flags, startId)
    }
    
    /**
     * Play an external audio file
     */
    private fun playExternalFile(uri: Uri) {
        Log.d(TAG, "Playing external file: $uri")

        // Use service-scoped coroutine to handle operations without blocking the main thread
        serviceScope.launch {
            try {
                // Check if we've seen this URI before (on main thread - quick cache lookup)
                val cachedItem = externalUriCache[uri.toString()]
                if (cachedItem != null) {
                    Log.d(TAG, "Using cached media item for URI: $uri")
                    
                    // Clear the player first to avoid conflicts with existing items
                    player.clearMediaItems()
                    
                    // Play the media item
                    player.setMediaItem(cachedItem)
                    player.prepare()
                    player.play()
                    
                    return@launch
                }
                
                // Add a small delay before processing to allow previous operations to complete
                delay(500)
                
                // Extract metadata from the audio file in a background thread
                val mediaItem = withContext(Dispatchers.IO) {
                    try {
                        val song = chromahub.rhythm.app.util.MediaUtils.extractMetadataFromUri(this@MediaPlaybackService, uri)
                        Log.d(TAG, "Extracted metadata for external file: ${song.title} by ${song.artist}")
                        
                        // Create a media item with the extracted metadata
                        MediaItem.Builder()
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
                            
                    } catch (e: Exception) {
                        Log.e(TAG, "Error extracting metadata from external file", e)
                        
                        // Fall back to basic playback if metadata extraction fails
                        val mimeType = contentResolver.getType(uri)
                        Log.d(TAG, "Falling back to basic playback with mime type: $mimeType")
                        
                        MediaItem.Builder()
                            .setUri(uri)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(uri.lastPathSegment ?: "Unknown")
                                    .build()
                            )
                            .build()
                    }
                }
                
                // Back on main thread - set up playback
                player.clearMediaItems()
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
                Log.e(TAG, "Error in playExternalFile coroutine", e)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service being destroyed")
        
        // Unregister BroadcastReceiver
        try {
            unregisterReceiver(favoriteChangeReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering favorite change receiver", e)
        }
        
        // Cancel all coroutines and pending jobs
        updateLayoutJob?.cancel()
        sleepTimerJob?.cancel()
        serviceScope.cancel()
        
        // Release audio effects
        releaseAudioEffects()
        
        mediaSession?.run {
            player.release()
            controller?.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = mediaSession
    
    @OptIn(UnstableApi::class)
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        // Let Media3 handle notification updates but ensure our icon is used
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    private inner class MediaSessionCallback : MediaLibrarySession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.d(TAG, "onConnect: ${controller.packageName}")
            val availableCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
                .buildUpon()
            if (session.isMediaNotificationController(controller) ||
                session.isAutoCompanionController(controller) ||
                session.isAutomotiveController(controller)
            ) {
                for (commandButton in customCommands) {
                    commandButton.sessionCommand?.let { availableCommands.add(it) }
                }
            }
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableCommands.build())
                .build()
        }

        @OptIn(UnstableApi::class)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            val serviceController = this@MediaPlaybackService.controller
            if (serviceController == null) {
                Log.w(TAG, "Controller not ready for custom command: ${customCommand.customAction}")
                return Futures.immediateFuture(SessionResult(SessionError.ERROR_SESSION_DISCONNECTED))
            }
            
            return Futures.immediateFuture(
                when (customCommand.customAction) {
                    SHUFFLE_MODE_ON -> {
                        serviceController.shuffleModeEnabled = true
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    SHUFFLE_MODE_OFF -> {
                        serviceController.shuffleModeEnabled = false
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    REPEAT_MODE_OFF -> {
                        serviceController.repeatMode = Player.REPEAT_MODE_OFF
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    REPEAT_MODE_ONE -> {
                        serviceController.repeatMode = Player.REPEAT_MODE_ONE
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    REPEAT_MODE_ALL -> {
                        serviceController.repeatMode = Player.REPEAT_MODE_ALL
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    FAVORITE_ON -> {
                        // Add current song to favorites
                        Log.d(TAG, "Favorite ON command received")
                        toggleCurrentSongFavorite()
                        // Immediate UI feedback for responsive feel
                        serviceScope.launch {
                            kotlinx.coroutines.delay(50) // Very short delay for immediate response
                            updateCustomLayoutSmart()
                        }
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    FAVORITE_OFF -> {
                        // Remove current song from favorites  
                        Log.d(TAG, "Favorite OFF command received")
                        toggleCurrentSongFavorite()
                        // Immediate UI feedback for responsive feel
                        serviceScope.launch {
                            kotlinx.coroutines.delay(50) // Very short delay for immediate response
                            updateCustomLayoutSmart()
                        }
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    else -> {
                        SessionResult(SessionError.ERROR_NOT_SUPPORTED)
                    }
                })
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
            params: LibraryParams?
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

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        super.onShuffleModeEnabledChanged(shuffleModeEnabled)
        Log.d(TAG, "Shuffle mode changed to: $shuffleModeEnabled")
        // Use debounced update to prevent rapid UI changes
        scheduleCustomLayoutUpdate(100)
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super<Player.Listener>.onRepeatModeChanged(repeatMode)
        Log.d(TAG, "Repeat mode changed to: $repeatMode")
        // Use debounced update to prevent rapid UI changes
        scheduleCustomLayoutUpdate(100)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        Log.d(TAG, "Media item transitioned: ${mediaItem?.mediaMetadata?.title}")
        // Update custom layout when song changes to reflect correct favorite state
        scheduleCustomLayoutUpdate(50) // Shorter delay for song transitions
    }

    // Sleep Timer functionality
    fun startSleepTimer(durationMs: Long, fadeOut: Boolean = true, pauseOnly: Boolean = false) {
        Log.d(TAG, "Starting sleep timer: ${durationMs}ms, fadeOut: $fadeOut, pauseOnly: $pauseOnly")
        stopSleepTimer() // Stop any existing timer
        
        if (durationMs <= 0) {
            Log.e(TAG, "Invalid sleep timer duration: $durationMs")
            return
        }
        
        sleepTimerDurationMs = durationMs
        sleepTimerStartTime = System.currentTimeMillis()
        fadeOutEnabled = fadeOut
        pauseOnlyEnabled = pauseOnly
        
        // Broadcast initial status immediately
        broadcastSleepTimerStatus()
        
        sleepTimerJob = serviceScope.launch {
            try {
                if (fadeOut && durationMs > 10000) { // Only fade if duration > 10 seconds
                    // Regular updates until fade start time (last 10 seconds)
                    val fadeStartTime = durationMs - 10000
                    var remainingTime = durationMs
                    
                    while (remainingTime > 10000) {
                        delay(1000) // Update every second
                        remainingTime = durationMs - (System.currentTimeMillis() - sleepTimerStartTime)
                        if (remainingTime <= 0) break
                        broadcastSleepTimerStatus()
                    }
                    
                    // Fade out over 10 seconds
                    val originalVolume = player.volume
                    val fadeSteps = 100
                    val fadeInterval = 10000L / fadeSteps
                    
                    for (i in fadeSteps downTo 0) {
                        val volume = originalVolume * (i.toFloat() / fadeSteps)
                        player.volume = volume
                        delay(fadeInterval)
                        // Broadcast status every few steps during fade
                        if (i % 10 == 0) {
                            broadcastSleepTimerStatus()
                        }
                    }
                } else {
                    // No fade out, broadcast updates every second until completion
                    var remainingTime = durationMs
                    while (remainingTime > 0) {
                        delay(1000) // Update every second
                        remainingTime = durationMs - (System.currentTimeMillis() - sleepTimerStartTime)
                        if (remainingTime <= 0) break
                        broadcastSleepTimerStatus()
                    }
                }
                
                // Timer finished - pause or stop playback
                if (pauseOnly) {
                    player.pause()
                    Log.d(TAG, "Sleep timer paused playback")
                } else {
                    player.stop()
                    Log.d(TAG, "Sleep timer stopped playback")
                }
                
                // Reset volume if it was changed during fade
                if (fadeOut) {
                    player.volume = 1.0f
                }
                
                resetSleepTimer()
                
            } catch (e: CancellationException) {
                Log.d(TAG, "Sleep timer was cancelled")
                resetSleepTimer()
            } catch (e: Exception) {
                Log.e(TAG, "Error in sleep timer", e)
                resetSleepTimer()
            } finally {
                broadcastSleepTimerStatus()
            }
        }
        
        Log.d(TAG, "Sleep timer job started for ${durationMs}ms")
    }
    
    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        
        // Reset volume if it was changed during fade
        if (fadeOutEnabled) {
            player.volume = 1.0f
        }
        
        resetSleepTimer()
        broadcastSleepTimerStatus()
        Log.d(TAG, "Sleep timer stopped")
    }
    
    fun getRemainingTimeMs(): Long {
        return if (sleepTimerJob?.isActive == true && sleepTimerDurationMs > 0 && sleepTimerStartTime > 0) {
            val elapsed = System.currentTimeMillis() - sleepTimerStartTime
            maxOf(0, sleepTimerDurationMs - elapsed)
        } else {
            0L
        }
    }
    
    private fun resetSleepTimer() {
        sleepTimerDurationMs = 0L
        sleepTimerStartTime = 0L
        fadeOutEnabled = true
        pauseOnlyEnabled = false
    }
    
    private fun broadcastSleepTimerStatus() {
        val intent = Intent(BROADCAST_SLEEP_TIMER_STATUS).apply {
            putExtra(EXTRA_TIMER_ACTIVE, isSleepTimerActive())
            putExtra(EXTRA_REMAINING_TIME, getRemainingTimeMs())
        }
        sendBroadcast(intent)
    }
    
    // Audio Effects (Equalizer) functionality
    fun initializeAudioEffects() {
        try {
            val audioSessionId = player.audioSessionId
            Log.d(TAG, "Initializing audio effects with session ID: $audioSessionId")
            
            // Skip initialization if session ID is invalid
            if (audioSessionId == 0) {
                Log.w(TAG, "Invalid audio session ID (0), skipping effects initialization")
                return
            }
            
            // Initialize equalizer
            try {
                equalizer?.release()
                equalizer = android.media.audiofx.Equalizer(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Equalizer initialized with ${equalizer?.numberOfBands} bands for session $audioSessionId")
            } catch (e: Exception) {
                Log.w(TAG, "Equalizer not available: ${e.message}")
            }
            
            // Initialize bass boost
            try {
                bassBoost?.release()
                bassBoost = android.media.audiofx.BassBoost(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Bass boost initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Bass boost not available: ${e.message}")
            }
            
            // Initialize virtualizer
            try {
                virtualizer?.release()
                virtualizer = android.media.audiofx.Virtualizer(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Virtualizer initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Virtualizer not available: ${e.message}")
            }
            
            // Load saved settings and apply them
            loadSavedAudioEffects()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing audio effects", e)
        }
    }
    
    private fun loadSavedAudioEffects() {
        try {
            // Load equalizer settings
            equalizer?.enabled = appSettings.equalizerEnabled.value
            
            // Load band levels
            val bandLevelsString = appSettings.equalizerBandLevels.value
            val bandLevels = bandLevelsString.split(",").mapNotNull { it.toFloatOrNull() }
            if (bandLevels.size == 5) {
                equalizer?.let { eq ->
                    for (i in 0 until minOf(eq.numberOfBands.toInt(), bandLevels.size)) {
                        val level = (bandLevels[i] * 100).toInt().toShort()
                        eq.setBandLevel(i.toShort(), level)
                    }
                }
            }
            
            // Load bass boost settings
            bassBoost?.enabled = appSettings.bassBoostEnabled.value
            if (appSettings.bassBoostEnabled.value) {
                bassBoost?.setStrength(appSettings.bassBoostStrength.value.toShort())
            }
            
            // Load virtualizer settings
            virtualizer?.enabled = appSettings.virtualizerEnabled.value
            if (appSettings.virtualizerEnabled.value) {
                virtualizer?.setStrength(appSettings.virtualizerStrength.value.toShort())
            }
            
            Log.d(TAG, "Loaded saved audio effects - EQ: ${appSettings.equalizerEnabled.value}, Bass: ${appSettings.bassBoostEnabled.value}, Virtualizer: ${appSettings.virtualizerEnabled.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved audio effects", e)
        }
    }
    
    fun setEqualizerEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        Log.d(TAG, "Equalizer enabled: $enabled")
    }
    
    fun setEqualizerBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
            Log.d(TAG, "Set equalizer band $band to level $level")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting equalizer band level", e)
        }
    }
    
    fun getEqualizerBandLevel(band: Short): Short {
        return equalizer?.getBandLevel(band) ?: 0
    }
    
    fun getNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 0
    }
    
    fun getBandFreqRange(band: Short): IntArray? {
        return equalizer?.getBandFreqRange(band)
    }
    
    fun applyEqualizerPreset(levels: FloatArray) {
        try {
            equalizer?.let { eq ->
                val numberOfBands = eq.numberOfBands
                for (i in 0 until minOf(numberOfBands.toInt(), levels.size)) {
                    val level = (levels[i] * 100).toInt().toShort() // Convert -1.0 to 1.0 range to -100 to 100
                    eq.setBandLevel(i.toShort(), level)
                }
                Log.d(TAG, "Applied equalizer preset with ${levels.size} bands")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying equalizer preset", e)
        }
    }
    
    fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled
        Log.d(TAG, "Bass boost enabled: $enabled")
    }
    
    fun setBassBoostStrength(strength: Short) {
        try {
            bassBoost?.setStrength(strength)
            Log.d(TAG, "Bass boost strength set to $strength")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting bass boost strength", e)
        }
    }
    
    fun getBassBoostStrength(): Short {
        return bassBoost?.roundedStrength ?: 0
    }
    
    fun setVirtualizerEnabled(enabled: Boolean) {
        virtualizer?.enabled = enabled
        Log.d(TAG, "Virtualizer enabled: $enabled")
    }
    
    fun setVirtualizerStrength(strength: Short) {
        try {
            virtualizer?.setStrength(strength)
            Log.d(TAG, "Virtualizer strength set to $strength")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting virtualizer strength", e)
        }
    }
    
    fun getVirtualizerStrength(): Short {
        return virtualizer?.roundedStrength ?: 0
    }
    
    // Public methods for external access
    fun getMediaSession(): MediaLibrarySession? = mediaSession
    
    fun getSleepTimerRemainingTime(): Long = sleepTimerDurationMs - (System.currentTimeMillis() - sleepTimerStartTime)
    
    fun isSleepTimerActive(): Boolean = sleepTimerJob?.isActive == true
    
    private fun releaseAudioEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            equalizer = null
            bassBoost = null
            virtualizer = null
            Log.d(TAG, "Audio effects released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio effects", e)
        }
    }
}
