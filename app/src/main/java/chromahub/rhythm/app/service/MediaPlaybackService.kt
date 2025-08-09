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
        startForeground()
        
        // Initialize settings manager (fast operation)
        appSettings = AppSettings.getInstance(applicationContext)
        
        // Register BroadcastReceiver for favorite changes
        val filter = IntentFilter("chromahub.rhythm.app.action.FAVORITE_CHANGED")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(favoriteChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(favoriteChangeReceiver, filter)
        }

        try {
            // Initialize core components on main thread (required for media service)
            initializePlayer()
            createCustomCommands()
            
            // Create the media session (required synchronously)
            mediaSession = createMediaSession()
            
            // Initialize controller asynchronously to avoid blocking
            createController()
            
            Log.d(TAG, "Service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing service", e)
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
    
    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rhythm Music")
            .setContentText("Initializing music service...")
            .setSmallIcon(chromahub.rhythm.app.R.drawable.ic_notification)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Started foreground service")
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
            
        // Apply current settings
        applyPlayerSettings()
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

        return MediaLibrarySession.Builder(
            this,
            player,
            MediaSessionCallback()
        )
            .setSessionActivity(pendingIntent)
            .build()
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
            
            mediaSession?.setCustomLayout(ImmutableList.of(currentShuffleCommand, currentRepeatCommand, currentFavoriteCommand))
            
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
                session.setCustomLayout(ImmutableList.of(currentShuffleCommand, currentRepeatCommand, currentFavoriteCommand))
                
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
        serviceScope.cancel()
        
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
}