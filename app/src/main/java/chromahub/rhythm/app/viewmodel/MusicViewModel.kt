package chromahub.rhythm.app.viewmodel

import android.app.Application

import android.content.ComponentName
import android.content.Context
import android.content.Intent

import android.os.Build
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.MusicRepository
import chromahub.rhythm.app.data.PlaybackLocation
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Queue
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.service.MediaPlaybackService
import chromahub.rhythm.app.util.AudioDeviceManager
import chromahub.rhythm.app.util.EqualizerUtils
import chromahub.rhythm.app.util.GsonUtils
import chromahub.rhythm.app.util.PlaylistImportExportUtils
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.yield
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.io.File
import chromahub.rhythm.app.data.LyricsData // Import LyricsData

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MusicViewModel"
    private val repository = MusicRepository(application)
    
    // Audio device manager
    private val audioDeviceManager = AudioDeviceManager(application)
    
    // Settings manager
    val appSettings = AppSettings.getInstance(application)
    
    // Settings
    val showLyrics = appSettings.showLyrics
    val showOnlineOnlyLyrics = appSettings.onlineOnlyLyrics
    val useSystemTheme = appSettings.useSystemTheme
    val darkMode = appSettings.darkMode
    val autoConnectDevice = appSettings.autoConnectDevice
    val maxCacheSize = appSettings.maxCacheSize
    val clearCacheOnExit = appSettings.clearCacheOnExit
    
    // Playback settings
    val enableHighQualityAudio = appSettings.highQualityAudio
    val enableGaplessPlayback = appSettings.gaplessPlayback
    val enableCrossfade = appSettings.crossfade
    val crossfadeDuration = appSettings.crossfadeDuration
    val enableAudioNormalization = appSettings.audioNormalization
    val enableReplayGain = appSettings.replayGain
    
    // Equalizer settings
    val equalizerEnabled = appSettings.equalizerEnabled
    val equalizerPreset = appSettings.equalizerPreset
    val equalizerBandLevels = appSettings.equalizerBandLevels
    val bassBoostEnabled = appSettings.bassBoostEnabled
    val bassBoostStrength = appSettings.bassBoostStrength
    val virtualizerEnabled = appSettings.virtualizerEnabled
    val virtualizerStrength = appSettings.virtualizerStrength
    
    // Search history
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    // Lyrics
    private val _currentLyrics = MutableStateFlow<LyricsData?>(null)
    val currentLyrics: StateFlow<LyricsData?> = _currentLyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    // New helper methods
    private val _serviceConnected = MutableStateFlow(false)
    val serviceConnected: StateFlow<Boolean> = _serviceConnected.asStateFlow()

    // Lyrics fetch job tracking to prevent race conditions
    private var lyricsFetchJob: Job? = null

    // Main music data
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    // Cache for file paths to avoid repeated ContentResolver queries
    private val pathCache = mutableMapOf<String, String?>()
    
    // Filtered songs excluding blacklisted ones and including only whitelisted ones (both songs and folders)
    val filteredSongs: StateFlow<List<Song>> = kotlinx.coroutines.flow.combine(
        _songs,
        appSettings.mediaScanMode,
        appSettings.blacklistedSongs,
        appSettings.blacklistedFolders,
        appSettings.whitelistedSongs,
        appSettings.whitelistedFolders
    ) { flows: Array<Any> ->
        val songs = flows[0] as List<Song>
        val mode = flows[1] as String
        val blacklistedIds = flows[2] as List<String>
        val blacklistedFolders = flows[3] as List<String>
        val whitelistedIds = flows[4] as List<String>
        val whitelistedFolders = flows[5] as List<String>
        filterSongsAsync(songs, mode, blacklistedIds, blacklistedFolders, whitelistedIds, whitelistedFolders)
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    
    private suspend fun filterSongsAsync(
        songs: List<Song>,
        mediaScanMode: String,
        blacklistedIds: List<String>, 
        blacklistedFolders: List<String>,
        whitelistedIds: List<String>,
        whitelistedFolders: List<String>
    ): List<Song> = withContext(Dispatchers.IO) {
        val hasBlacklist = blacklistedIds.isNotEmpty() || blacklistedFolders.isNotEmpty()
        val hasWhitelist = whitelistedIds.isNotEmpty() || whitelistedFolders.isNotEmpty()
        
        // Determine which filtering mode to use based on mediaScanMode setting
        val useBlacklistMode = mediaScanMode == "blacklist"
        val useWhitelistMode = mediaScanMode == "whitelist"
        
        // If no filters are active for the current mode, return all songs
        if (useBlacklistMode && !hasBlacklist) {
            return@withContext songs
        }
        if (useWhitelistMode && !hasWhitelist) {
            return@withContext songs
        }
        
        val startTime = System.currentTimeMillis()
        val result = mutableListOf<Song>()
        
        // Process in batches to allow yielding
        val batchSize = 100
        var processed = 0
        
        for (song in songs) {
            // In BLACKLIST mode: exclude blacklisted songs/folders, include everything else
            if (useBlacklistMode && hasBlacklist) {
                // Check if song is individually blacklisted
                if (blacklistedIds.contains(song.id)) {
                    processed++
                    continue
                }
                
                // Check if song is in a blacklisted folder
                if (blacklistedFolders.isNotEmpty()) {
                    val songPath = getPathFromUriCached(song.uri)
                    if (songPath != null && isPathBlacklisted(songPath, blacklistedFolders)) {
                        processed++
                        continue
                    }
                }
                
                // Song passed blacklist checks, include it
                result.add(song)
            }
            // In WHITELIST mode: include ONLY whitelisted songs/folders, exclude everything else
            else if (useWhitelistMode && hasWhitelist) {
                var isWhitelisted = false
                
                // Check if song ID is individually whitelisted
                if (whitelistedIds.contains(song.id)) {
                    isWhitelisted = true
                }
                
                // Check if song is in a whitelisted folder
                if (!isWhitelisted && whitelistedFolders.isNotEmpty()) {
                    val songPath = getPathFromUriCached(song.uri)
                    if (songPath != null && isPathWhitelisted(songPath, whitelistedFolders)) {
                        isWhitelisted = true
                    }
                }
                
                // Only add if whitelisted
                if (isWhitelisted) {
                    result.add(song)
                }
            }
            
            processed++
            
            // Yield control periodically to prevent ANR
            if (processed % batchSize == 0) {
                yield()
            }
        }
        
        val endTime = System.currentTimeMillis()
        Log.d(TAG, "Filtered ${songs.size} songs to ${result.size} in ${endTime - startTime}ms (mode: $mediaScanMode, blacklist: $hasBlacklist, whitelist: $hasWhitelist)")
        
        result
    }
    
    private fun isPathBlacklisted(songPath: String, blacklistedFolders: List<String>): Boolean {
        // Normalize song path for consistent comparison
        val normalizedSongPath = songPath.replace("\\", "/")
        
        return blacklistedFolders.any { folderPath ->
            val normalizedFolderPath = folderPath.replace("\\", "/")
            normalizedSongPath.startsWith(normalizedFolderPath, ignoreCase = true)
        }
    }
    
    private fun isPathWhitelisted(songPath: String, whitelistedFolders: List<String>): Boolean {
        // Normalize song path for consistent comparison
        val normalizedSongPath = songPath.replace("\\", "/")
        
        return whitelistedFolders.any { folderPath ->
            val normalizedFolderPath = folderPath.replace("\\", "/")
            normalizedSongPath.startsWith(normalizedFolderPath, ignoreCase = true)
        }
    }

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()
    
    // Filtered albums excluding albums with all songs blacklisted
    val filteredAlbums: StateFlow<List<Album>> = kotlinx.coroutines.flow.combine(
        _albums,
        filteredSongs
    ) { albums, filteredSongs ->
        albums.filter { album ->
            // Include album if it has at least one non-blacklisted song
            filteredSongs.any { song -> song.album == album.title && song.artist == album.artist }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()
    
    // Filtered artists excluding artists with all songs blacklisted
    val filteredArtists: StateFlow<List<Artist>> = kotlinx.coroutines.flow.combine(
        _artists,
        filteredSongs,
        appSettings.groupByAlbumArtist
    ) { artists, filteredSongs, groupByAlbumArtist ->
        artists.filter { artist ->
            // Include artist if they have at least one non-blacklisted song
            // Check against the appropriate field based on grouping mode
            if (groupByAlbumArtist) {
                // When grouping by album artist, match against song's albumArtist (with fallback to artist)
                filteredSongs.any { song -> 
                    val songAlbumArtist = song.albumArtist ?: song.artist
                    songAlbumArtist == artist.name 
                }
            } else {
                // When grouping by track artist, match against song's artist
                filteredSongs.any { song -> song.artist == artist.name }
            }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Use audioDeviceManager for locations instead of the mock data
    val locations = audioDeviceManager.availableDevices
    val currentDevice = audioDeviceManager.currentDevice

    // Recently played songs
    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    // Player state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentQueue = MutableStateFlow(Queue(emptyList(), -1))
    val currentQueue: StateFlow<Queue> = _currentQueue.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // Media loading and seeking states
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _isSeeking = MutableStateFlow(false)
    val isSeeking: StateFlow<Boolean> = _isSeeking.asStateFlow()

    // Volume control
    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    
    private var _previousVolume = 0.7f

    // New player state for additional functionality
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _favoriteSongs = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSongs: StateFlow<Set<String>> = _favoriteSongs.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    // For tracking progress 
    private var progressUpdateJob: Job? = null

    // Selected song for adding to playlist
    private val _selectedSongForPlaylist = MutableStateFlow<Song?>(null)
    val selectedSongForPlaylist: StateFlow<Song?> = _selectedSongForPlaylist.asStateFlow()

    // Target playlist for adding songs
    private val _targetPlaylistId = MutableStateFlow<String?>(null)
    val targetPlaylistId: StateFlow<String?> = _targetPlaylistId.asStateFlow()

    // Sort library functionality - Load saved sort order from AppSettings
    private val _sortOrder = MutableStateFlow(
        try {
            SortOrder.valueOf(appSettings.songsSortOrder.value)
        } catch (e: Exception) {
            SortOrder.TITLE_ASC
        }
    )
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // User preferences and statistics
    private val _listeningTime = MutableStateFlow(appSettings.listeningTime.value)
    val listeningTime: StateFlow<Long> = _listeningTime.asStateFlow()
    
    private val _songsPlayed = MutableStateFlow(appSettings.songsPlayed.value)
    val songsPlayed: StateFlow<Int> = _songsPlayed.asStateFlow()
    
    private val _uniqueArtists = MutableStateFlow(appSettings.uniqueArtists.value)
    val uniqueArtists: StateFlow<Int> = _uniqueArtists.asStateFlow()
    
    private val _genrePreferences = MutableStateFlow<Map<String, Int>>(appSettings.genrePreferences.value)
    val genrePreferences: StateFlow<Map<String, Int>> = _genrePreferences.asStateFlow()
    
    private val _timeBasedPreferences = MutableStateFlow<Map<Int, List<String>>>(appSettings.timeBasedPreferences.value)
    val timeBasedPreferences: StateFlow<Map<Int, List<String>>> = _timeBasedPreferences.asStateFlow()

    // Song play counts
    private val _songPlayCounts = MutableStateFlow<Map<String, Int>>(appSettings.songPlayCounts.value)
    val songPlayCounts: StateFlow<Map<String, Int>> = _songPlayCounts.asStateFlow()

    // Add initialization state tracking
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Media scan loader state for refresh operations
    private val _isMediaScanning = MutableStateFlow(false)
    val isMediaScanning: StateFlow<Boolean> = _isMediaScanning.asStateFlow()

    // Queue operation state
    private val _queueOperationError = MutableStateFlow<String?>(null)
    val queueOperationError: StateFlow<String?> = _queueOperationError.asStateFlow()
    
    // Clear queue operation error
    fun clearQueueOperationError() {
        _queueOperationError.value = null
    }

    enum class SortOrder {
        TITLE_ASC,
        TITLE_DESC,
        ARTIST_ASC,
        ARTIST_DESC,
        DATE_ADDED_ASC,
        DATE_ADDED_DESC,
        DATE_MODIFIED_ASC,
        DATE_MODIFIED_DESC
    }
    
    enum class SleepAction {
        PAUSE, STOP, FADE_OUT
    }

    init {
        Log.d(TAG, "Initializing MusicViewModel")
        
        viewModelScope.launch {
            try {
                initializeViewModelSafely()
            } catch (e: Exception) {
                Log.e(TAG, "Critical error during ViewModel initialization", e)
                handleInitializationFailure(e)
            }
        }
        
        // Initialize non-blocking components asynchronously
        viewModelScope.launch {
            try {
                // Initialize audio device manager but don't start continuous monitoring
                // Device monitoring will be started when needed (player screen, etc.)
                
                // Start tracking session (async)
                startListeningTimeTracking()
                
                // Sleep timer initialization removed - now using direct ViewModel state
                
                Log.d(TAG, "Async initialization completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error in async initialization", e)
            }
        }
        
        // Listen for blacklist/whitelist changes and refresh playlists accordingly
        viewModelScope.launch {
            combine(
                appSettings.blacklistedSongs,
                appSettings.blacklistedFolders,
                appSettings.whitelistedSongs,
                appSettings.whitelistedFolders
            ) { blacklistedSongs, blacklistedFolders, whitelistedSongs, whitelistedFolders ->
                // Trigger when any filter changes
                Unit
            }.collect {
                // Wait for initialization to complete before refreshing
                if (_isInitialized.value) {
                    Log.d(TAG, "Blacklist/Whitelist changed, refreshing playlists to remove filtered songs")
                    refreshPlaylists()
                }
            }
        }
    }
    
    private suspend fun initializeViewModelSafely() {
        Log.d(TAG, "Starting safe data initialization")
        
        // Step 1: Load core music data with error handling
        val initializationResults = initializeCoreData()
        if (!initializationResults.success) {
            throw Exception("Failed to load core music data: ${initializationResults.error}")
        }
        
        Log.d(TAG, "Loaded ${_songs.value.size} songs, ${_albums.value.size} albums, ${_artists.value.size} artists")

        // Step 2: Load settings and persisted data
        val settingsLoaded = loadAllSettings()
        if (!settingsLoaded) {
            Log.w(TAG, "Some settings failed to load, continuing with defaults")
        }

        // Step 3: Initialize components with validation
        val componentsInitialized = initializeComponents()
        if (!componentsInitialized) {
            Log.w(TAG, "Some components failed to initialize")
        }

        // Step 4: Populate playlists safely
        populateDefaultPlaylistsSafely()

        // Step 5: Initialize queue state properly
        initializeQueueState()

        // Step 6: Start background tasks
        startBackgroundTasks()

        // Step 7: Mark as initialized
        _isInitialized.value = true
        Log.d(TAG, "Data initialization complete successfully")
    }
    
    private data class InitializationResult(val success: Boolean, val error: String? = null)
    
    private suspend fun initializeCoreData(): InitializationResult {
        return try {
            val songs = repository.loadSongs()
            val albums = repository.loadAlbums()
            val artists = repository.loadArtists()
            
            if (songs.isEmpty() && albums.isEmpty() && artists.isEmpty()) {
                Log.w(TAG, "No media found, but this might be normal on first run")
            }
            
            _songs.value = songs
            _albums.value = albums
            _artists.value = artists
            
            InitializationResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading core data", e)
            InitializationResult(false, e.message)
        }
    }
    
    private suspend fun loadAllSettings(): Boolean {
        var allSuccess = true
        
        try {
            loadSettings()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading app settings", e)
            allSuccess = false
        }
        
        try {
            loadSearchHistory()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading search history", e)
            allSuccess = false
        }
        
        try {
            loadSavedPlaylists()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved playlists", e)
            allSuccess = false
        }
        
        return allSuccess
    }
    
    private suspend fun initializeComponents(): Boolean {
        var allSuccess = true
        
        try {
            initializeController()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing media controller", e)
            allSuccess = false
        }
        
        try {
            initializeFromPersistence()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing from persistence", e)
            allSuccess = false
        }
        
        try {
            startProgressUpdates()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting progress updates", e)
            allSuccess = false
        }
        
        return allSuccess
    }
    
    private suspend fun populateDefaultPlaylistsSafely() {
        try {
            populateRecentlyAddedPlaylist()
        } catch (e: Exception) {
            Log.e(TAG, "Error populating recently added playlist", e)
        }
        
        try {
            populateMostPlayedPlaylist()
        } catch (e: Exception) {
            Log.e(TAG, "Error populating most played playlist", e)
        }
    }
    
    private fun initializeQueueState() {
        try {
            if (_currentQueue.value.songs.isEmpty()) {
                Log.d(TAG, "Queue is empty, initializing with empty state")
                _currentQueue.value = Queue(emptyList(), -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing queue state", e)
            _currentQueue.value = Queue(emptyList(), -1)
        }
    }
    
    private suspend fun startBackgroundTasks() {
        // Start artwork fetching in background without blocking initialization
        viewModelScope.launch {
            try {
                fetchArtworkFromInternet()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching artwork from internet", e)
            }
        }

        // Start background genre detection after a short delay to allow UI to settle
        viewModelScope.launch {
            try {
                delay(2000) // Wait 2 seconds after app load before starting genre detection
                detectGenresInBackground()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting background genre detection", e)
            }
        }
    }

    /**
     * Detects genres for songs in background and updates the UI dynamically
     */
    private suspend fun detectGenresInBackground() {
        Log.d(TAG, "Starting background genre detection for ${songs.value.size} songs")

        repository.detectGenresInBackground(
            songs = songs.value,
            onProgress = { current, total ->
                // Optional: Could emit progress updates to UI if needed
                Log.d(TAG, "Genre detection progress: $current/$total")
            },
            onComplete = { updatedSongs ->
                // Update the songs state with the new genre information
                _songs.value = updatedSongs
                Log.d(TAG, "Background genre detection completed, updated ${updatedSongs.count { it.genre != null }} songs with genres")
            }
        )
    }
    
    private fun startListeningTimeTracking() {
        viewModelScope.launch {
            try {
                while (isActive) {
                    if (isPlaying.value && _currentSong.value != null) {
                        // Update listening time every minute when actually playing music
                        delay(60000) // 1 minute
                        val newTime = _listeningTime.value + 60000
                        _listeningTime.value = newTime
                        appSettings.setListeningTime(newTime)
                    } else {
                        // Check less frequently when not playing to reduce resource usage
                        // This helps prevent ANR when app is used without playing music
                        delay(5000) // Check every 5 seconds when not playing
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in listening time tracking", e)
            }
        }
    }
    
    private fun handleInitializationFailure(error: Exception) {
        Log.e(TAG, "Handling initialization failure", error)
        
        // Set safe default values
        _songs.value = emptyList()
        _albums.value = emptyList()
        _artists.value = emptyList()
        _currentQueue.value = Queue(emptyList(), -1)
        _isInitialized.value = true // Mark as initialized even with empty data
        
        // Could potentially show error message to user here
    }

    /**
     * Triggers a refresh of all music data by rescanning the device's MediaStore.
     * This will update the songs, albums, and artists in the ViewModel.
     */
    fun refreshLibrary() {
        viewModelScope.launch {
            Log.d(TAG, "Starting library refresh...")
            _isMediaScanning.value = true // Show media scan loader
            _isInitialized.value = false // Indicate that data is being refreshed

            try {
                // Trigger the refresh in the repository
                repository.refreshMusicData()

                // Reload data into StateFlows after refresh
                _songs.value = repository.loadSongs()
                _albums.value = repository.loadAlbums()
                _artists.value = repository.loadArtists()

                // Re-populate dynamic playlists
                populateRecentlyAddedPlaylist()
                populateMostPlayedPlaylist()
                
                // Refresh all playlists to remove songs that no longer exist on the device
                refreshPlaylists()

                // Re-fetch artwork from internet for newly added/updated items (but don't block completion)
                launch { 
                    try {
                        fetchArtworkFromInternet()
                    } catch (e: Exception) {
                        Log.w(TAG, "Artwork fetching failed but continuing with library refresh", e)
                    }
                }

                Log.d(TAG, "Library refresh complete. Loaded ${_songs.value.size} songs, ${_albums.value.size} albums, ${_artists.value.size} artists")
            } catch (e: Exception) {
                Log.e(TAG, "Error during library refresh", e)
                // Ensure we still have some data even if refresh fails
                if (_songs.value.isEmpty()) {
                    try {
                        _songs.value = repository.loadSongs()
                        _albums.value = repository.loadAlbums()
                        _artists.value = repository.loadArtists()
                    } catch (fallbackError: Exception) {
                        Log.e(TAG, "Fallback loading also failed", fallbackError)
                    }
                }
            } finally {
                _isInitialized.value = true // Mark as initialized again
                _isMediaScanning.value = false // Hide media scan loader
                
                // Ensure MediaScanLoader doesn't get stuck by dispatching a completion event
                // This is a safety measure for cases where the StateFlow updates might not trigger UI properly
                delay(1000) // Give UI time to process the state changes
                Log.d(TAG, "Media scanning state cleared - final state: ${_songs.value.size} songs, ${_albums.value.size} albums, ${_artists.value.size} artists")
            }
        }
    }

    /**
     * Refreshes all playlists by re-validating their songs against the currently available songs.
     * This removes songs from playlists if they no longer exist on the device OR are blacklisted.
     */
    private fun refreshPlaylists() {
        Log.d(TAG, "Refreshing playlists...")
        val currentSongsMap = _songs.value.associateBy { it.id }
        val filteredSongsSet = filteredSongs.value.map { it.id }.toSet()
        
        _playlists.value = _playlists.value.map { playlist ->
            val updatedSongs = playlist.songs.filter { song ->
                // Keep song only if it exists on device AND is not filtered out (blacklisted/not whitelisted)
                currentSongsMap.containsKey(song.id) && filteredSongsSet.contains(song.id)
            }
            if (updatedSongs.size < playlist.songs.size) {
                Log.d(TAG, "Removed ${playlist.songs.size - updatedSongs.size} missing/filtered songs from playlist: ${playlist.name}")
                playlist.copy(songs = updatedSongs, dateModified = System.currentTimeMillis())
            } else {
                playlist
            }
        }
        savePlaylists()
        Log.d(TAG, "Playlists refreshed.")
    }
    
    /**
     * Updates the current song metadata in the UI and library
     * @param updatedSong The song with updated metadata
     */
    fun updateCurrentSongMetadata(updatedSong: Song) {
        viewModelScope.launch {
            // Update the song in the main songs list
            val updatedSongs = _songs.value.map { song ->
                if (song.id == updatedSong.id) updatedSong else song
            }
            _songs.value = updatedSongs
            
            // Update current song if it matches
            if (_currentSong.value?.id == updatedSong.id) {
                _currentSong.value = updatedSong
            }
            
            // Update in any playlists
            _playlists.value = _playlists.value.map { playlist ->
                playlist.copy(
                    songs = playlist.songs.map { song ->
                        if (song.id == updatedSong.id) updatedSong else song
                    }
                )
            }
            
            // Update albums and artists to reflect the new metadata
            _albums.value = repository.loadAlbums()
            _artists.value = repository.loadArtists()
            
            Log.d(TAG, "Updated song metadata: ${updatedSong.title} by ${updatedSong.artist}")
        }
    }
    
    /**
     * Saves metadata changes to the audio file and updates the UI
     */
    fun saveMetadataChanges(
        song: Song,
        title: String,
        artist: String,
        album: String,
        genre: String,
        year: Int,
        trackNumber: Int,
        artworkUri: Uri? = null,
        onSuccess: (fileWriteSucceeded: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Try to update the actual file metadata first
                val context = getApplication<Application>().applicationContext
                val success = withContext(Dispatchers.IO) {
                    chromahub.rhythm.app.util.MediaUtils.updateSongMetadata(
                        context = context,
                        song = song,
                        newTitle = title,
                        newArtist = artist,
                        newAlbum = album,
                        newGenre = genre,
                        newYear = year,
                        newTrackNumber = trackNumber
                    )
                }
                
                // Always update in-memory data
                val updatedSong = song.copy(
                    title = title,
                    artist = artist,
                    album = album,
                    genre = genre,
                    year = year,
                    trackNumber = trackNumber
                )
                
                // Update the song using existing function
                updateCurrentSongMetadata(updatedSong)
                
                // Handle artwork saving if provided
                if (artworkUri != null && success) {
                    try {
                        // Save artwork to cache directory for now
                        saveArtworkToCache(context, song, artworkUri)
                        Log.d(TAG, "Artwork saved to cache for: $title")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to save artwork, but metadata was saved successfully", e)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        Log.d(TAG, "Successfully updated file metadata for: $title by $artist")
                        onSuccess(true)
                    } else {
                        // File update failed but MediaStore may have succeeded
                        Log.w(TAG, "File metadata write failed")
                        onSuccess(false)
                    }
                }
                
            } catch (e: chromahub.rhythm.app.util.RecoverableSecurityExceptionWrapper) {
                // Android 11+ scoped storage restriction - file not owned by app
                Log.w(TAG, "RecoverableSecurityException - file modification requires user permission")
                
                // Update in-memory data anyway
                val updatedSong = song.copy(
                    title = title,
                    artist = artist,
                    album = album,
                    genre = genre,
                    year = year,
                    trackNumber = trackNumber
                )
                updateCurrentSongMetadata(updatedSong)
                
                withContext(Dispatchers.Main) {
                    onError("Cannot modify this file: Android security restrictions prevent editing files not created by this app. Metadata updated in library only.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving metadata", e)
                withContext(Dispatchers.Main) {
                    onError("Failed to save metadata: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }
    
    private suspend fun saveArtworkToCache(context: Context, song: Song, artworkUri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(artworkUri)
            if (inputStream != null) {
                val artworkFile = File(context.cacheDir, "artwork_${song.id}.jpg")
                artworkFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                Log.d(TAG, "Artwork saved to cache: ${artworkFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save artwork to cache", e)
            throw e
        }
    }
    
    /**
     * Reloads playlists and favorite songs from settings after a backup restore
     */
    fun reloadPlaylistsFromSettings() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Reloading playlists from settings after restore...")
                loadSavedPlaylists()
                Log.d(TAG, "Playlists successfully reloaded from settings")
            } catch (e: Exception) {
                Log.e(TAG, "Error reloading playlists from settings", e)
            }
        }
    }
    
    /**
     * Ensures current playlists and favorite songs are saved to persistent storage
     * Useful before creating a backup to ensure all data is included
     */
    fun ensurePlaylistsSaved() {
        try {
            savePlaylists()
            saveFavoriteSongs()
            Log.d(TAG, "Playlists and favorites explicitly saved to storage")
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring playlists are saved", e)
        }
    }
    
    private fun loadSavedPlaylists() {
        try {
            // Load playlists
            val playlistsJson = appSettings.playlists.value
            val playlists = if (playlistsJson != null) {
                val type = object : TypeToken<List<Playlist>>() {}.type
                GsonUtils.gson.fromJson<List<Playlist>>(playlistsJson, type)
            } else {
                // Initialize with default playlists if none exist
                listOf(
                    Playlist("1", "Favorites"),
                    Playlist("2", "Recently Added"),
                    Playlist("3", "Most Played")
                )
            }
            _playlists.value = playlists
            
            // Load favorite songs
            val favoriteSongsJson = appSettings.favoriteSongs.value
            if (favoriteSongsJson != null) {
                val type = object : TypeToken<Set<String>>() {}.type
                _favoriteSongs.value = GsonUtils.gson.fromJson(favoriteSongsJson, type)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved playlists", e)
            // Initialize with default playlists on error
            _playlists.value = listOf(
                Playlist("1", "Favorites"),
                Playlist("2", "Recently Added"),
                Playlist("3", "Most Played")
            )
            _favoriteSongs.value = emptySet()
        }
    }

    private fun savePlaylists() {
        try {
            val playlistsJson = GsonUtils.gson.toJson(_playlists.value)
            appSettings.setPlaylists(playlistsJson)
            Log.d(TAG, "Saved ${_playlists.value.size} playlists")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving playlists", e)
        }
    }

    private fun saveFavoriteSongs() {
        try {
            val favoriteSongsJson = GsonUtils.gson.toJson(_favoriteSongs.value)
            appSettings.setFavoriteSongs(favoriteSongsJson)
            Log.d(TAG, "Saved ${_favoriteSongs.value.size} favorite songs")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorite songs", e)
        }
    }

    /**
     * Fetches artist images and album artwork from the internet for items that don't have them
     */
    private fun fetchArtworkFromInternet() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching artist images from internet")
                val missingArtists = _artists.value.filter { it.artworkUri == null }
                Log.d(TAG, "Found ${missingArtists.size} artists without images out of ${_artists.value.size} total artists")
                val chunkSize = 10
                for (batch in missingArtists.chunked(chunkSize)) {
                    val updatedArtists = repository.fetchArtistImages(batch)
                    if (updatedArtists.isNotEmpty()) {
                        val artistMap = updatedArtists.associateBy { it.id }
                        _artists.value = _artists.value.map { artist ->
                            artistMap[artist.id] ?: artist
                        }
                        Log.d(TAG, "Updated ${updatedArtists.size} artists with images from internet (batch)")
                    }
                    // Throttle between batches to avoid hitting API rate limits
                    delay(1000)
                }
                
                Log.d(TAG, "Fetching album artwork from internet")
                // Check for albums with null or content URI artwork (which might not exist)
                val albumsWithContentUri = _albums.value.filter { 
                    it.artworkUri != null && it.artworkUri.toString().startsWith("content://media/external/audio/albumart") 
                }
                Log.d(TAG, "Found ${albumsWithContentUri.size} albums with content:// URIs that might need validation")
                
                // Only fetch for a subset of albums to avoid overwhelming the API
                // Consider albums with content:// URIs as potentially needing artwork
                val albumsToUpdate = _albums.value.filter { 
                    it.artworkUri == null || it.artworkUri.toString().startsWith("content://media/external/audio/albumart") 
                }.take(10)
                
                Log.d(TAG, "Found ${albumsToUpdate.size} albums that might need artwork out of ${_albums.value.size} total albums")
                if (albumsToUpdate.isNotEmpty()) {
                    Log.d(TAG, "Albums to update: ${albumsToUpdate.map { "${it.artist} - ${it.title}" }}")
                    val updatedAlbums = repository.fetchAlbumArtwork(albumsToUpdate)
                    // Update only the albums we fetched, keeping the rest unchanged
                    val albumMap = updatedAlbums.associateBy { it.id }
                    _albums.value = _albums.value.map { 
                        albumMap[it.id] ?: it 
                    }
                    Log.d(TAG, "Updated ${updatedAlbums.size} albums with artwork from internet")
                } else {
                    Log.d(TAG, "No albums found that need artwork")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching artwork from internet", e)
            }
        }
    }
    
    /**
     * Refreshes artwork for a specific artist
     */
    fun refreshArtistImage(artistId: String) {
        viewModelScope.launch {
            val artist = _artists.value.find { it.id == artistId } ?: return@launch
            try {
                val updatedArtists = repository.fetchArtistImages(listOf(artist))
                if (updatedArtists.isNotEmpty()) {
                    val updatedArtist = updatedArtists.first()
                    _artists.value = _artists.value.map { 
                        if (it.id == artistId) updatedArtist else it 
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing artist image", e)
            }
        }
    }
    
    /**
     * Refreshes artwork for a specific album
     */
    fun refreshAlbumArtwork(albumId: String) {
        viewModelScope.launch {
            val album = _albums.value.find { it.id == albumId } ?: return@launch
            try {
                val updatedAlbums = repository.fetchAlbumArtwork(listOf(album))
                if (updatedAlbums.isNotEmpty()) {
                    val updatedAlbum = updatedAlbums.first()
                    _albums.value = _albums.value.map { 
                        if (it.id == albumId) updatedAlbum else it 
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing album artwork", e)
            }
        }
    }

    /**
     * Connect to the media service
     */
    fun connectToMediaService() {
        Log.d(TAG, "Connecting to media service")
        val context = getApplication<Application>()
        
        // Start the service first to ensure it's running
        val serviceIntent = Intent(context, MediaPlaybackService::class.java)
        serviceIntent.action = MediaPlaybackService.ACTION_INIT_SERVICE
        
        // Use startForegroundService for Android 8.0+ to avoid BackgroundServiceStartNotAllowedException
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start media service: ${e.message}", e)
            // Try to connect without starting the service - the MediaController might still work
        }
        
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                Log.d(TAG, "Media controller initialized: $mediaController")
                
                if (mediaController != null) {
                    mediaController?.addListener(playerListener)
                    _serviceConnected.value = true
                    
                    // Update shuffle and repeat mode from controller
                    mediaController?.let { controller ->
                        _isShuffleEnabled.value = controller.shuffleModeEnabled
                        val controllerRepeatMode = controller.repeatMode
                        _repeatMode.value = controllerRepeatMode
                        Log.d(TAG, "Initial repeat mode from controller: $controllerRepeatMode (${
                        when(controllerRepeatMode) {
                            Player.REPEAT_MODE_OFF -> "OFF"
                            Player.REPEAT_MODE_ONE -> "ONE"
                            Player.REPEAT_MODE_ALL -> "ALL"
                            else -> "UNKNOWN"
                        }
                    })")
                    
                    // Sync playback state with controller when app is reopened
                    val isActuallyPlaying = controller.isPlaying
                    _isPlaying.value = isActuallyPlaying
                    Log.d(TAG, "Syncing playback state on controller init: isPlaying=$isActuallyPlaying")
                    
                    // Update duration and start progress updates if playing
                    if (isActuallyPlaying) {
                        _duration.value = controller.duration
                        startProgressUpdates()
                    }
                }
                
                // Check if we have a current song after initializing controller
                updateCurrentSong()
                
                // Debug the queue state after initialization
                debugQueueState()
                
                // Check if we have a pending queue to play
                pendingQueueToPlay?.let { songs ->
                    Log.d(TAG, "Playing pending queue with ${songs.size} songs")
                    playQueue(songs)
                    pendingQueueToPlay = null
                }
            } else {
                _serviceConnected.value = false
                Log.e(TAG, "Failed to get media controller")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing media controller", e)
            _serviceConnected.value = false
        }
        }, MoreExecutors.directExecutor())
    }
    
    // Private initialization method (called from init)
    private fun initializeController() {
        connectToMediaService()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "Playback state changed: $playbackState")
            
            // Update buffering state
            _isBuffering.value = playbackState == Player.STATE_BUFFERING
            
            // Update isPlaying based on both playbackState and controller.isPlaying
            mediaController?.let { controller ->
                // The isPlaying value should be true only when both:
                // 1. The player is in STATE_READY
                // 2. controller.isPlaying is true
                val shouldBePlaying = playbackState == Player.STATE_READY && controller.isPlaying
                
                if (_isPlaying.value != shouldBePlaying) {
                    Log.d(TAG, "Updating isPlaying from ${_isPlaying.value} to $shouldBePlaying")
                    _isPlaying.value = shouldBePlaying
                }
                
                if (playbackState == Player.STATE_READY) {
                    _duration.value = controller.duration
                    Log.d(TAG, "Duration updated: ${controller.duration}")
                    
                    // Update progress immediately for better UI responsiveness
                    if (controller.duration > 0) {
                        val currentProgress = controller.currentPosition.toFloat() / controller.duration.toFloat()
                        _progress.value = currentProgress.coerceIn(0f, 1f)
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    // Handle playback completion - ensure progress is updated to the end
                    _progress.value = 1.0f
                    progressUpdateJob?.cancel()
                    Log.d(TAG, "Playback completed")
                    
                    // If repeat mode is off and we're at the end of the queue, stop playback
                    if (controller.repeatMode == Player.REPEAT_MODE_OFF && 
                        controller.currentMediaItemIndex == controller.mediaItemCount - 1) {
                        controller.pause()
                        _isPlaying.value = false
                    }
                }
            }
            
            // Restart progress updates when playback state changes
            if (_isPlaying.value) {
                startProgressUpdates()
            } else {
                progressUpdateJob?.cancel()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "Media item transition: ${mediaItem?.mediaId}, reason: $reason")
            
            // Reset progress to 0 for immediate UI feedback
            _progress.value = 0f
            
            // Update current song and queue position
            mediaItem?.let { item ->
                val songId = item.mediaId
                val song = _songs.value.find { it.id == songId }
                
                if (song != null) {
                    _currentSong.value = song
                    
                    // Update recently played
                    updateRecentlyPlayed(song)
                    
                    // Update favorite status
                    _isFavorite.value = _favoriteSongs.value.contains(song.id)
                    
                    // Update queue position - comprehensive logic from both listeners
                    val currentQueue = _currentQueue.value
                    val newIndex = currentQueue.songs.indexOfFirst { it.id == songId }
                    
                    if (newIndex != -1 && newIndex != currentQueue.currentIndex) {
                        // Only update if the index actually changed
                        Log.d(TAG, "Updating queue index from ${currentQueue.currentIndex} to $newIndex")
                        _currentQueue.value = currentQueue.copy(currentIndex = newIndex)
                    } else if (newIndex == -1) {
                        // Song not found in current queue - sync with MediaController
                        Log.d(TAG, "Song not in queue, syncing queue from MediaController for: ${song.title}")
                        mediaController?.let { controller ->
                            val mediaItems = (0 until controller.mediaItemCount).map { index ->
                                controller.getMediaItemAt(index)
                            }
                            val mediaItemSongs = mediaItems.mapNotNull { mediaItem ->
                                _songs.value.find { it.id == mediaItem.mediaId }
                            }
                            
                            if (mediaItemSongs.isNotEmpty()) {
                                val currentMediaIndex = controller.currentMediaItemIndex
                                _currentQueue.value = Queue(mediaItemSongs, currentMediaIndex.coerceAtLeast(0))
                                Log.d(TAG, "Synced queue with MediaController: ${mediaItemSongs.size} songs, index: $currentMediaIndex")
                            }
                        }
                    }
                    
                    // Fetch lyrics for the new song
                    fetchLyricsForCurrentSong()
                    
                    // Extract colors from album art if enabled
                    extractColorsFromAlbumArt(song)
                    
                    // Force a duration update
                    mediaController?.let { controller ->
                        _duration.value = controller.duration
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "Is playing changed: $isPlaying")
            
            // Only update if the value is different to avoid unnecessary UI updates
            if (_isPlaying.value != isPlaying) {
                Log.d(TAG, "Updating isPlaying state from ${_isPlaying.value} to $isPlaying")
                _isPlaying.value = isPlaying
            }
            
            // Start or stop progress updates based on playback state
            if (isPlaying) {
                startProgressUpdates()
            } else {
                progressUpdateJob?.cancel()
            }
        }
        
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            Log.d(TAG, "Shuffle mode changed: $shuffleModeEnabled")
            _isShuffleEnabled.value = shuffleModeEnabled
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            Log.d(TAG, "Repeat mode changed: $repeatMode")
            _repeatMode.value = repeatMode
        }
    }
    
    private fun startProgressUpdates() {
        // Cancel existing job if running
        progressUpdateJob?.cancel()
        
        // Reset progress to 0 when starting a new song
        _progress.value = 0f
        
        // Start a new coroutine to update progress
        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                updateProgress()
                delay(100) // Update every 100ms for smooth progress
            }
        }
    }
    
    private fun updateProgress() {
        mediaController?.let { controller ->
            if (controller.duration > 0) {
                val currentProgress = controller.currentPosition.toFloat() / controller.duration.toFloat()
                _progress.value = currentProgress.coerceIn(0f, 1f)
            }
        }
    }

    private fun updateCurrentSong() {
        mediaController?.let { controller ->
            val mediaItem = controller.currentMediaItem
            mediaItem?.let {
                val id = it.mediaId
                val song = _songs.value.find { song -> song.id == id }
                
                if (song != null) {
                    _currentSong.value = song
                    
                    // Update favorite status
                    _isFavorite.value = _favoriteSongs.value.contains(song.id)
                    
                    // Check if the song is in the current queue
                    val currentQueue = _currentQueue.value
                    val songIndexInQueue = currentQueue.songs.indexOfFirst { queueSong -> queueSong.id == song.id }
                    
                    if (songIndexInQueue != -1) {
                        // Song is in queue - update position if different
                        if (songIndexInQueue != currentQueue.currentIndex) {
                            _currentQueue.value = currentQueue.copy(currentIndex = songIndexInQueue)
                            Log.d(TAG, "Updated queue position to $songIndexInQueue on song restore")
                        }
                    } else {
                        // Song is not in queue - this can happen when resuming from a previous session
                        // or when playing external files. Sync the entire queue with MediaController
                        Log.d(TAG, "Song not in queue, syncing entire queue from MediaController for: ${song.title}")
                        syncQueueWithMediaController()
                    }
                    
                    // Update duration and progress for the current song
                    _duration.value = controller.duration
                    if (controller.duration > 0) {
                        val currentProgress = controller.currentPosition.toFloat() / controller.duration.toFloat()
                        _progress.value = currentProgress.coerceIn(0f, 1f)
                        Log.d(TAG, "Updated progress on song restore: ${_progress.value}, position: ${controller.currentPosition}, duration: ${controller.duration}")
                    }
                    
                    // Fetch lyrics for the current song
                    fetchLyricsForCurrentSong()
                } else {
                    Log.w(TAG, "Could not find song with ID: $id")
                }
            } ?: run {
                Log.d(TAG, "No current media item in controller")
                // Clear current song and queue if no media item
                _currentSong.value = null
                if (_currentQueue.value.songs.isNotEmpty()) {
                    _currentQueue.value = Queue(emptyList(), -1)
                    Log.d(TAG, "Cleared queue as no media item is active")
                }
            }
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(this.id)
            .setUri(this.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(this.title)
                    .setArtist(this.artist)
                    .setAlbumTitle(this.album)
                    .setArtworkUri(this.artworkUri)
                    .build()
            )
            .build()
    }

    fun playSong(song: Song) {
        Log.d(TAG, "Playing song: ${song.title}")

        // Clear current lyrics to prevent showing stale lyrics from previous song
        _currentLyrics.value = null

        updateRecentlyPlayed(song)
        trackSongPlay(song)

        val currentQueueSongs = _currentQueue.value.songs.toMutableList()
        val songIndexInQueue = currentQueueSongs.indexOfFirst { it.id == song.id }

        mediaController?.let { controller ->
            if (songIndexInQueue != -1) {
                // Song is already in the queue, just play it
                controller.seekToDefaultPosition(songIndexInQueue)
                _currentQueue.value = _currentQueue.value.copy(currentIndex = songIndexInQueue)
                Log.d(TAG, "Playing existing song in queue at position $songIndexInQueue")
            } else {
                // Song is not in the queue, create a contextual queue
                val contextualQueue = createContextualQueue(song)
                if (contextualQueue.size > 1) {
                    // If we have a contextual queue, play that instead
                    Log.d(TAG, "Creating contextual queue with ${contextualQueue.size} songs")
                    playQueue(contextualQueue)
                    return
                } else {
                    // Fallback: add single song to current queue
                    val currentIndex = _currentQueue.value.currentIndex
                    val insertIndex = if (currentQueueSongs.isEmpty() || currentIndex == -1) 0 else (currentIndex + 1).coerceAtMost(currentQueueSongs.size)
                    currentQueueSongs.add(insertIndex, song)

                    val mediaItem = song.toMediaItem()
                    controller.addMediaItem(insertIndex, mediaItem)

                    _currentQueue.value = Queue(currentQueueSongs, insertIndex)
                    controller.seekToDefaultPosition(insertIndex)
                    Log.d(TAG, "Added single song to queue at position $insertIndex")
                }
            }

            controller.prepare()
            controller.play()

            _currentSong.value = song
            _isPlaying.value = true
            _isFavorite.value = _favoriteSongs.value.contains(song.id)
            startProgressUpdates()
        }
    }

    /**
     * Create a contextual queue based on the song's context (album, artist, recently played, etc.)
     */
    private fun createContextualQueue(song: Song): List<Song> {
        // Try to determine the best context for this song
        
        // Check if song is from recently played - if so, create queue from recently played
        if (_recentlyPlayed.value.any { it.id == song.id }) {
            val recentlyPlayedSongs = _recentlyPlayed.value.take(20) // Limit to recent 20
            val startIndex = recentlyPlayedSongs.indexOfFirst { it.id == song.id }
            if (startIndex != -1) {
                // Reorder so the selected song is first, followed by remaining recently played
                val reorderedList = listOf(song) + recentlyPlayedSongs.filter { it.id != song.id }
                Log.d(TAG, "Created queue from recently played with ${reorderedList.size} songs")
                return reorderedList
            }
        }
        
        // Check if song is part of an album with multiple tracks
        val albumSongs = _songs.value.filter { it.album == song.album && it.artist == song.artist }
        if (albumSongs.size > 1) {
            // Sort by track number if available, otherwise by title
            val sortedAlbumSongs = albumSongs.sortedWith { a, b ->
                if (a.trackNumber != 0 || b.trackNumber != 0) {
                    a.trackNumber.compareTo(b.trackNumber)
                } else {
                    a.title.compareTo(b.title)
                }
            }
            val startIndex = sortedAlbumSongs.indexOfFirst { it.id == song.id }
            if (startIndex != -1) {
                // Reorder so the selected song is first, followed by rest of album
                val reorderedList = listOf(song) + sortedAlbumSongs.filter { it.id != song.id }
                Log.d(TAG, "Created queue from album '${song.album}' with ${reorderedList.size} songs")
                return reorderedList
            }
        }
        
        // Check if song is by an artist with multiple tracks
        val artistSongs = _songs.value.filter { it.artist == song.artist }
        if (artistSongs.size > 1) {
            // Get a reasonable subset of artist songs (popular ones first if available)
            val limitedArtistSongs = if (artistSongs.size > 25) {
                // Sort by play count if available, then take top 25
                artistSongs.sortedByDescending { _songPlayCounts.value[it.id] ?: 0 }.take(25)
            } else {
                artistSongs
            }
            val reorderedList = listOf(song) + limitedArtistSongs.filter { it.id != song.id }.shuffled()
            Log.d(TAG, "Created queue from artist '${song.artist}' with ${reorderedList.size} songs")
            return reorderedList
        }
        
        // Fallback: return just the single song
        Log.d(TAG, "No context found, returning single song queue")
        return listOf(song)
    }

    /**
     * Play a song with options for queue behavior
     */
    fun playSongWithQueueOption(song: Song, replaceQueue: Boolean = false, shuffleQueue: Boolean = false) {
        Log.d(TAG, "Playing song with queue option: ${song.title}, replaceQueue: $replaceQueue")
        
        // Clear current lyrics to prevent showing stale lyrics from previous song
        _currentLyrics.value = null
        
        if (replaceQueue) {
            // Replace the entire queue with this song and context
            val queueSongs = if (shuffleQueue) {
                // Get all songs from the same context (album/playlist) and shuffle
                val contextSongs = _songs.value.filter { it.album == song.album }
                if (contextSongs.size > 1) {
                    val shuffled = contextSongs.shuffled()
                    // Ensure the selected song is first
                    listOf(song) + shuffled.filter { it.id != song.id }
                } else {
                    listOf(song)
                }
            } else {
                // Create contextual queue for the song
                createContextualQueue(song)
            }
            playQueue(queueSongs)
        } else {
            // Add to the current queue and play immediately
            addSongToQueue(song)
            // Seek to the newly added song
            val newIndex = _currentQueue.value.songs.indexOfFirst { it.id == song.id }
            if (newIndex != -1) {
                mediaController?.seekToDefaultPosition(newIndex)
                mediaController?.play()
            }
        }
    }

    /**
     * Play a song from a specific context (playlist, album, etc.)
     * This ensures the queue reflects the context the song was played from
     */
    fun playSongFromContext(song: Song, contextSongs: List<Song>, contextName: String? = null) {
        Log.d(TAG, "Playing song from context: ${song.title}, context: $contextName, contextSize: ${contextSongs.size}")
        
        // Clear current lyrics to prevent showing stale lyrics from previous song
        _currentLyrics.value = null
        
        if (contextSongs.isEmpty()) {
            // Fallback to regular playSong
            playSong(song)
            return
        }
        
        // Find the position of the song in the context
        val songIndex = contextSongs.indexOfFirst { it.id == song.id }
        if (songIndex != -1) {
            // Reorder the context so the selected song plays first, followed by the rest
            val reorderedQueue = listOf(song) + contextSongs.filter { it.id != song.id }
            playQueue(reorderedQueue)
        } else {
            // Song not found in context, add it to the front and play
            val newQueue = listOf(song) + contextSongs
            playQueue(newQueue)
        }
    }

    /**
     * Add songs from a specific context (album, artist, etc.) to queue
     */
    fun addContextToQueue(contextSongs: List<Song>, shuffled: Boolean = false) {
        val songsToAdd = if (shuffled) contextSongs.shuffled() else contextSongs
        addSongsToQueue(songsToAdd)
    }

    private fun updateRecentlyPlayed(song: Song) {
        viewModelScope.launch {
            try {
                val currentList = _recentlyPlayed.value.toMutableList()
                currentList.removeIf { it.id == song.id }
                currentList.add(0, song)
                if (currentList.size > 50) {
                    currentList.removeAt(currentList.size - 1)
                }
                
                // Update both the StateFlow and persistence
                _recentlyPlayed.value = currentList
                appSettings.updateRecentlyPlayed(currentList.map { it.id })
                appSettings.updateLastPlayedTimestamp(System.currentTimeMillis())
                
                // Log the update
                Log.d(TAG, "Updated recently played: ${currentList.size} songs, latest: ${song.title}")
                
                // Update other stats
                updateListeningStats(song)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating recently played", e)
            }
        }
    }
    
    /**
     * Extract colors from album artwork and update theme if color source is set to ALBUM_ART
     */
    private fun extractColorsFromAlbumArt(song: Song) {
        // Only extract if color source is set to ALBUM_ART
        if (appSettings.colorSource.value != "ALBUM_ART") {
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val artworkUri = song.artworkUri
                if (artworkUri == null) {
                    Log.d(TAG, "No artwork URI for song: ${song.title}")
                    return@launch
                }
                
                // Load bitmap from URI
                val context = getApplication<Application>().applicationContext
                val bitmap = try {
                    val inputStream = context.contentResolver.openInputStream(artworkUri)
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load bitmap from URI: $artworkUri", e)
                    null
                }
                
                if (bitmap == null) {
                    Log.d(TAG, "Could not decode bitmap for song: ${song.title}")
                    return@launch
                }
                
                // Extract colors using ColorExtractor utility
                val extractedColors = chromahub.rhythm.app.util.ColorExtractor.extractColorsFromBitmap(bitmap)
                
                if (extractedColors != null) {
                    // Convert to JSON and save to settings
                    val colorsJson = chromahub.rhythm.app.util.ColorExtractor.colorsToJson(extractedColors)
                    appSettings.setExtractedAlbumColors(colorsJson)
                    Log.d(TAG, "Successfully extracted and saved colors from: ${song.title}")
                } else {
                    Log.w(TAG, "Failed to extract colors from: ${song.title}")
                }
                
                // Clean up bitmap
                bitmap.recycle()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting colors from album art", e)
            }
        }
    }

    private fun updateListeningStats(song: Song) {
        viewModelScope.launch {
            try {
                // Update daily listening stats
                val today = java.time.LocalDate.now().toString()
                val dailyStats = appSettings.dailyListeningStats.value.toMutableMap()
                dailyStats[today] = (dailyStats[today] ?: 0L) + 1
                appSettings.updateDailyListeningStats(dailyStats)
                
                // Update weekly top artists
                val currentArtists = appSettings.weeklyTopArtists.value.toMutableMap()
                currentArtists[song.artist] = (currentArtists[song.artist] ?: 0) + 1
                appSettings.updateWeeklyTopArtists(currentArtists)
                
                // Update favorite genres
                song.genre?.let { genre ->
                    val genres = appSettings.favoriteGenres.value.toMutableMap()
                    genres[genre] = (genres[genre] ?: 0) + 1
                    appSettings.updateFavoriteGenres(genres)
                }
                
                // Update mood preferences
                updateMoodPreferences(song)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating listening stats", e)
            }
        }
    }

    private fun updateMoodPreferences(song: Song) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val mood = when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        
        val moodPrefs = appSettings.moodPreferences.value.toMutableMap()
        val songList = moodPrefs.getOrDefault(mood, emptyList()).toMutableList()
        if (songList.size >= 20) songList.removeAt(0)
        songList.add(song.id)
        moodPrefs[mood] = songList
        appSettings.updateMoodPreferences(moodPrefs)
    }

    fun playAlbum(album: Album) {
        viewModelScope.launch {
            Log.d(TAG, "Playing album: ${album.title} (ID: ${album.id})")
            val songs = repository.getSongsForAlbum(album.id)
            Log.d(TAG, "Found ${songs.size} songs for album")
            if (songs.isNotEmpty()) {
                playQueue(songs)
            } else {
                Log.e(TAG, "No songs found for album: ${album.title} (ID: ${album.id})")
                debugQueueState()
            }
        }
    }

    fun playArtist(artist: Artist) {
        viewModelScope.launch {
            Log.d(TAG, "Playing artist: ${artist.name} (ID: ${artist.id})")
            val songs = repository.getSongsForArtist(artist.id)
            Log.d(TAG, "Found ${songs.size} songs for artist")
            if (songs.isNotEmpty()) {
                playQueue(songs)
            } else {
                Log.e(TAG, "No songs found for artist: ${artist.name} (ID: ${artist.id})")
                debugQueueState()
            }
        }
    }

    fun playPlaylist(playlist: Playlist) {
        Log.d(TAG, "Playing playlist: ${playlist.name}")
        if (playlist.songs.isNotEmpty()) {
            playQueue(playlist.songs)
        }
    }

    fun playQueue(songs: List<Song>) {
        Log.d(TAG, "Playing queue with ${songs.size} songs")
        
        // Clear current lyrics to prevent showing stale lyrics from previous song
        _currentLyrics.value = null
        
        if (songs.isEmpty()) {
            Log.e(TAG, "Cannot play empty queue")
            return
        }
        
        mediaController?.let { controller ->
            try {
                // Clear existing queue first
                controller.clearMediaItems()
                
                // Create media items from songs and add them to controller
                songs.forEach { song ->
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(song.id)
                        .setUri(song.uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumTitle(song.album)
                                .setArtworkUri(song.artworkUri)
                                .build()
                        )
                        .build()
                    
                    controller.addMediaItem(mediaItem)
                }
                
                // Set the queue in the view model immediately for UI responsiveness
                _currentQueue.value = Queue(songs, 0)
                
                // Start playback from the first song
                controller.seekToDefaultPosition(0)
                controller.prepare()
                controller.play()
                
                // Update current song and state
                val firstSong = songs.firstOrNull()
                _currentSong.value = firstSong
                _isPlaying.value = true
                
                // Add first song to recently played
                firstSong?.let { updateRecentlyPlayed(it) }
                
                // Update favorite status
                _isFavorite.value = firstSong?.let { song -> 
                    _favoriteSongs.value.contains(song.id) 
                } ?: false
                
                startProgressUpdates()
                
                Log.d(TAG, "Successfully started playback of queue with ${songs.size} songs")
                
                // Debug queue state
                debugQueueState()
                
                // Double-check queue sync after a short delay to ensure consistency
                viewModelScope.launch {
                    delay(500) // Wait for MediaController to be ready
                    if (controller.mediaItemCount != songs.size) {
                        Log.w(TAG, "Queue size mismatch after playback start - syncing")
                        syncQueueWithMediaController()
                        debugQueueState()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error playing queue", e)
                // Reset queue state on error
                _currentQueue.value = Queue(emptyList(), -1)
                _queueOperationError.value = "Error playing queue: ${e.message}"
            }
        } ?: run {
            Log.e(TAG, "Cannot play queue - media controller is null")
            // Try to reconnect to the media service if controller is null
            connectToMediaService()
            
            // Store the songs to play once we have a controller
            pendingQueueToPlay = songs
        }
    }

    
    // Store pending queue to play when controller becomes available
    private var pendingQueueToPlay: List<Song>? = null

    fun togglePlayPause() {
        Log.d(TAG, "Toggle play/pause, current state: ${_isPlaying.value}")
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
                _isPlaying.value = false
                progressUpdateJob?.cancel()
            } else {
                controller.play()
                _isPlaying.value = true
                startProgressUpdates()
            }
        }
    }
    
    // Sleep Timer Functions
    fun pauseMusic() {
        Log.d(TAG, "Pausing music via sleep timer")
        mediaController?.let { controller ->
            controller.pause()
            _isPlaying.value = false
            progressUpdateJob?.cancel()
        }
    }
    
    fun stopMusic() {
        Log.d(TAG, "Stopping music via sleep timer")
        mediaController?.let { controller ->
            controller.stop()
            _isPlaying.value = false
            progressUpdateJob?.cancel()
        }
    }
    
    fun fadeOutAndPause() {
        Log.d(TAG, "Fading out and pausing music via sleep timer")
        mediaController?.let { controller ->
            // Implement fade out effect by gradually reducing volume
            viewModelScope.launch {
                val originalVolume = controller.volume
                var currentVolume = originalVolume
                
                // Fade out over 3 seconds
                repeat(30) {
                    currentVolume -= originalVolume / 30f
                    controller.volume = maxOf(0f, currentVolume)
                    delay(100) // 100ms intervals for smooth fade
                }
                
                // Pause and restore volume for next play
                controller.pause()
                controller.volume = originalVolume
                _isPlaying.value = false
                progressUpdateJob?.cancel()
            }
        }
    }

    fun skipToNext() {
        Log.d(TAG, "Skip to next")
        mediaController?.let { controller ->
            // Check if there are more songs in the queue
            if (controller.hasNextMediaItem()) {
                // Get the next song before seeking to update UI immediately
                val nextIndex = (controller.currentMediaItemIndex + 1) % controller.mediaItemCount
                val nextMediaItem = controller.getMediaItemAt(nextIndex)
                val nextSongId = nextMediaItem.mediaId
                val nextSong = _songs.value.find { it.id == nextSongId }
                
                // Update the current queue position first for immediate UI feedback
                val currentQueue = _currentQueue.value
                if (currentQueue.songs.isNotEmpty()) {
                    val currentIndex = currentQueue.currentIndex
                    val newIndex = (currentIndex + 1) % currentQueue.songs.size
                    _currentQueue.value = currentQueue.copy(currentIndex = newIndex)
                    
                    // Reset progress to 0 for immediate UI feedback
                    _progress.value = 0f
                    
                    Log.d(TAG, "Updated queue position from $currentIndex to $newIndex")
                }
                
                // Update the current song immediately for better UX
                if (nextSong != null) {
                    _currentSong.value = nextSong
                    // Update recently played
                    updateRecentlyPlayed(nextSong)
                    // Update favorite status
                    _isFavorite.value = _favoriteSongs.value.contains(nextSong.id)
                    // Fetch lyrics for the new song
                    fetchLyricsForCurrentSong()
                }
                
                // Now perform the actual seek operation
                controller.seekToNext()
            } else {
                Log.d(TAG, "No next song available")
            }
        }
    }

    fun skipToPrevious() {
        Log.d(TAG, "Skip to previous")
        mediaController?.let { controller ->
            // If current position is past the threshold, restart current song
            if (controller.currentPosition > REWIND_THRESHOLD_MS) {
                Log.d(TAG, "Current position (${controller.currentPosition}ms) > threshold (${REWIND_THRESHOLD_MS}ms), restarting current song.")
                controller.seekTo(0)
                _progress.value = 0f // Immediately reset progress for UI
            } else {
                // Otherwise, skip to the actual previous song
                if (controller.hasPreviousMediaItem()) {
                    // Get the previous song before seeking to update UI immediately
                    val prevIndex = if (controller.currentMediaItemIndex > 0)
                        controller.currentMediaItemIndex - 1
                    else
                        controller.mediaItemCount - 1

                    val prevMediaItem = controller.getMediaItemAt(prevIndex)
                    val prevSongId = prevMediaItem.mediaId
                    val prevSong = _songs.value.find { it.id == prevSongId }

                    // Update the current queue position first for immediate UI feedback
                    val currentQueue = _currentQueue.value
                    if (currentQueue.songs.isNotEmpty()) {
                        val currentIndex = currentQueue.currentIndex
                        val newIndex = if (currentIndex > 0)
                            currentIndex - 1
                        else
                            currentQueue.songs.size - 1

                        _currentQueue.value = currentQueue.copy(currentIndex = newIndex)

                        // Reset progress to 0 for immediate UI feedback
                        _progress.value = 0f

                        Log.d(TAG, "Updated queue position from $currentIndex to $newIndex")
                    }

                    // Update the current song immediately for better UX
                    if (prevSong != null) {
                        _currentSong.value = prevSong
                        // Update recently played
                        updateRecentlyPlayed(prevSong)
                        // Update favorite status
                        _isFavorite.value = _favoriteSongs.value.contains(prevSong.id)
                        // Fetch lyrics for the new song
                        fetchLyricsForCurrentSong()
                    }

                    // Now perform the actual seek operation
                    controller.seekToPrevious()
                } else {
                    Log.d(TAG, "No previous song available, restarting current song.")
                    // If no previous song, but still within threshold, restart current song
                    controller.seekTo(0)
                    _progress.value = 0f // Immediately reset progress for UI
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        Log.d(TAG, "Seek to position: $positionMs ms")
        _isSeeking.value = true
        mediaController?.seekTo(positionMs)
        updateProgress() // Immediately update progress after seeking
        
        // Reset seeking state after a delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _isSeeking.value = false
        }
    }

    fun seekTo(progress: Float) {
        mediaController?.let { controller ->
            val positionMs = (progress * controller.duration).toLong()
            Log.d(TAG, "Seek to progress: $progress (${positionMs}ms)")
            _isSeeking.value = true
            controller.seekTo(positionMs)
            updateProgress() // Immediately update progress after seeking
            
            // Reset seeking state after a delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                _isSeeking.value = false
            }
        }
    }

    fun skipBackward() {
        mediaController?.let { controller ->
            val newPosition = (controller.currentPosition - 30_000).coerceAtLeast(0)
            Log.d(TAG, "Skip backward 30s to ${newPosition}ms")
            _isSeeking.value = true
            controller.seekTo(newPosition)
            updateProgress() // Immediately update progress after seeking
            
            // Reset seeking state after a delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                _isSeeking.value = false
            }
        }
    }

    fun skipForward() {
        mediaController?.let { controller ->
            val newPosition = (controller.currentPosition + 30_000).coerceAtMost(controller.duration)
            Log.d(TAG, "Skip forward 30s to ${newPosition}ms")
            _isSeeking.value = true
            controller.seekTo(newPosition)
            updateProgress() // Immediately update progress after seeking
            
            // Reset seeking state after a delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                _isSeeking.value = false
            }
        }
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            val newShuffleMode = !controller.shuffleModeEnabled
            Log.d(TAG, "Toggle shuffle mode to: $newShuffleMode")
            controller.shuffleModeEnabled = newShuffleMode
            _isShuffleEnabled.value = newShuffleMode
        }
    }
    
    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            val currentMode = controller.repeatMode
            val newMode = when (currentMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
            Log.d(TAG, "Toggle repeat mode from $currentMode to $newMode")
            
            // Update the player's repeat mode
            controller.repeatMode = newMode
            
            // Update our state to match
            _repeatMode.value = newMode
            
            // Log the new state for debugging
            Log.d(TAG, "Repeat mode is now: ${when(newMode) {
                Player.REPEAT_MODE_OFF -> "OFF"
                Player.REPEAT_MODE_ONE -> "ONE"
                Player.REPEAT_MODE_ALL -> "ALL"
                else -> "UNKNOWN"
            }}")
        }
    }
    
    fun toggleFavorite() {
        _currentSong.value?.let { song ->
            toggleFavorite(song)
        }
    }

    /**
     * Toggle favorite status for a specific song
     */
    fun toggleFavorite(song: Song) {
        val songId = song.id
        val currentFavorites = _favoriteSongs.value.toMutableSet()
        
        if (currentFavorites.contains(songId)) {
            Log.d(TAG, "Removing song from favorites: ${song.title}")
            currentFavorites.remove(songId)
            
            // Update _isFavorite only if this is the current song
            if (_currentSong.value?.id == songId) {
                _isFavorite.value = false
            }
            
            // Remove from Favorites playlist
            _playlists.value = _playlists.value.map { playlist ->
                if (playlist.id == "1" && playlist.name == "Favorites") {
                    playlist.copy(songs = playlist.songs.filter { it.id != song.id })
                } else {
                    playlist
                }
            }
            savePlaylists()
        } else {
            Log.d(TAG, "Adding song to favorites: ${song.title}")
            currentFavorites.add(songId)
            
            // Update _isFavorite only if this is the current song
            if (_currentSong.value?.id == songId) {
                _isFavorite.value = true
            }
            
            // Add to Favorites playlist
            _playlists.value = _playlists.value.map { playlist ->
                if (playlist.id == "1" && playlist.name == "Favorites") {
                    playlist.copy(songs = playlist.songs + song)
                } else {
                    playlist
                }
            }
            savePlaylists()
        }
        
        _favoriteSongs.value = currentFavorites
        saveFavoriteSongs()
        
        // Notify MediaPlaybackService about favorite state change
        notifyMediaServiceFavoriteChange()
    }
    
    private fun notifyMediaServiceFavoriteChange() {
        // Send broadcast to notify MediaPlaybackService about favorite state change
        val intent = Intent("chromahub.rhythm.app.action.FAVORITE_CHANGED")
        getApplication<Application>().sendBroadcast(intent)
    }

    fun clearCurrentSong() {
        Log.d(TAG, "Clearing current song (mini player dismissed)")
        mediaController?.let { controller ->
            controller.stop()
            controller.clearMediaItems()
        }
        _currentSong.value = null
        _isPlaying.value = false
        _progress.value = 0f
        _duration.value = 0L
        _currentQueue.value = Queue(emptyList(), -1)
        progressUpdateJob?.cancel()
    }

    /**
     * Start monitoring for audio device changes
     */
    private fun startDeviceMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                // Refresh devices every 1 second to ensure UI is up-to-date
                // This is especially important when switching between speaker and Bluetooth
                audioDeviceManager.refreshDevices()
                delay(1000) // Reduced from 5000ms to 1000ms for more responsive UI
            }
        }
    }

    // Add controlled device monitoring that can be started/stopped
    private var deviceMonitoringJob: Job? = null

    /**
     * Start device monitoring when needed (e.g., when player screen is open)
     */
    fun startDeviceMonitoringOnDemand() {
        if (deviceMonitoringJob?.isActive == true) {
            Log.d(TAG, "Device monitoring already running")
            return
        }
        
        Log.d(TAG, "Starting on-demand device monitoring")
        deviceMonitoringJob = viewModelScope.launch {
            while (isActive) {
                // Refresh devices every 2 seconds when actively monitoring
                audioDeviceManager.refreshDevices()
                delay(2000)
            }
        }
    }

    /**
     * Stop device monitoring when not needed
     */
    fun stopDeviceMonitoringOnDemand() {
        Log.d(TAG, "Stopping on-demand device monitoring")
        deviceMonitoringJob?.cancel()
        deviceMonitoringJob = null
    }

    /**
     * Refresh audio devices manually
     */
    fun refreshAudioDevices() {
        Log.d(TAG, "Manually refreshing audio devices")
        audioDeviceManager.refreshDevices()
    }

    /**
     * Set the current audio output device
     */
    fun setCurrentDevice(device: PlaybackLocation) {
        Log.d(TAG, "Setting current device: ${device.name}")
        audioDeviceManager.setCurrentDevice(device)
    }

    /**
     * Show the system output switcher dialog
     */
    fun showOutputSwitcherDialog() {
        audioDeviceManager.showOutputSwitcherDialog()
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel being cleared - cleaning up resources")
        
        // Cancel all coroutine jobs
        progressUpdateJob?.cancel()
        deviceMonitoringJob?.cancel()
        lyricsFetchJob?.cancel()
        
        // Release media controller
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        
        // Clean up audio device manager
        audioDeviceManager.cleanup()
        
        // Clean up sleep timer
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        
        // Clean up repository to prevent memory leaks
        try {
            getMusicRepository().cleanup()
            Log.d(TAG, "Repository cleaned up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up repository", e)
        }
        
        super.onCleared()
    }

    /**
     * Populates the "Recently Added" playlist with songs from current year's albums.
     * Only includes songs that pass the blacklist/whitelist filters.
     */
    private fun populateRecentlyAddedPlaylist() {
        viewModelScope.launch {
            val recentlyAddedPlaylist = _playlists.value.find { it.id == "2" && it.name == "Recently Added" }
            if (recentlyAddedPlaylist == null) {
                Log.e(TAG, "Recently Added playlist not found, cannot populate.")
                return@launch
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentYearAlbums = _albums.value.filter { it.year == currentYear }
                .ifEmpty {
                    // Fallback to most recent albums if no current year albums are available
                    _albums.value.sortedByDescending { it.year }.take(4)
                }

            val songsToAdd = mutableSetOf<Song>()
            currentYearAlbums.forEach { album ->
                val albumSongs = repository.getSongsForAlbum(album.id)
                songsToAdd.addAll(albumSongs)
            }

            // Filter songs using the same blacklist/whitelist logic as filteredSongs
            val currentFilteredSongs = filteredSongs.value.toSet()
            val filteredSongsToAdd = songsToAdd.filter { it in currentFilteredSongs }

            // Add songs to the playlist, avoiding duplicates and respecting filters
            val updatedSongs = (recentlyAddedPlaylist.songs.toSet() + filteredSongsToAdd).toList()
                .filter { it in currentFilteredSongs } // Remove any previously added songs that are now filtered
            
            _playlists.value = _playlists.value.map { playlist ->
                if (playlist.id == "2") {
                    playlist.copy(songs = updatedSongs, dateModified = System.currentTimeMillis())
                } else {
                    playlist
                }
            }
            savePlaylists()
            Log.d(TAG, "Populated Recently Added playlist with ${filteredSongsToAdd.size} new filtered songs (${songsToAdd.size - filteredSongsToAdd.size} filtered out).")
        }
    }

    /**
     * Populates the "Most Played" playlist based on song play counts.
     */
    private fun populateMostPlayedPlaylist() {
        viewModelScope.launch {
            val mostPlayedPlaylist = _playlists.value.find { it.id == "3" && it.name == "Most Played" }
            if (mostPlayedPlaylist == null) {
                Log.e(TAG, "Most Played playlist not found, cannot populate.")
                return@launch
            }

            val sortedSongsByPlayCount = _songs.value.sortedByDescending { song ->
                _songPlayCounts.value[song.id] ?: 0
            }

            // Take top 50 most played songs
            val topSongs = sortedSongsByPlayCount.take(50)

            // Add these songs to the playlist, replacing existing ones to keep it fresh
            _playlists.value = _playlists.value.map { playlist ->
                if (playlist.id == "3") {
                    playlist.copy(songs = topSongs, dateModified = System.currentTimeMillis())
                } else {
                    playlist
                }
            }
            savePlaylists()
            Log.d(TAG, "Populated Most Played playlist with ${topSongs.size} songs.")
        }
    }

    // New functions for playlist management
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val newPlaylist = repository.createPlaylist(name)
            _playlists.value = _playlists.value + newPlaylist
            savePlaylists()
            Log.d(TAG, "Created new playlist: ${newPlaylist.name}")
        }
    }

    fun addSongToPlaylist(song: Song, playlistId: String, showSnackbar: (String) -> Unit) {
        // Check if song is filtered out (blacklisted or not whitelisted)
        val filteredSongsSet = filteredSongs.value.map { it.id }.toSet()
        if (!filteredSongsSet.contains(song.id)) {
            showSnackbar("Cannot add ${song.title} - song is filtered out")
            return
        }
        
        var success = false
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                // Check if song is already in the playlist
                if (playlist.songs.any { it.id == song.id }) {
                    showSnackbar("${song.title} is already in playlist '${playlist.name}'")
                    playlist
                } else {
                    val updatedSongs = playlist.songs + song
                    success = true
                    showSnackbar("Added ${song.title} to ${playlist.name}")
                    playlist.copy(
                        songs = updatedSongs,
                        dateModified = System.currentTimeMillis()
                    )
                }
            } else {
                playlist
            }
        }
        savePlaylists()
        if (success) {
            Log.d(TAG, "Added song to playlist: ${song.title}")
        }
    }

    fun removeSongFromPlaylist(song: Song, playlistId: String, showSnackbar: (String) -> Unit) {
        var success = false
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                val updatedSongs = playlist.songs.filter { it.id != song.id }
                if (updatedSongs.size < playlist.songs.size) {
                    success = true
                    showSnackbar("Removed ${song.title} from ${playlist.name}")
                    playlist.copy(
                        songs = updatedSongs,
                        dateModified = System.currentTimeMillis()
                    )
                } else {
                    playlist // Song not found in playlist, no change
                }
            } else {
                playlist
            }
        }
        savePlaylists()
        if (success) {
            Log.d(TAG, "Removed song from playlist: ${song.title}")
        } else {
            Log.d(TAG, "Song '${song.title}' not found in playlist '$playlistId' for removal.")
        }
    }

    fun deletePlaylist(playlistId: String) {
        // Prevent deleting default playlists
        if (playlistId == "1" || playlistId == "2" || playlistId == "3") {
            Log.d(TAG, "Cannot delete default playlist: $playlistId")
            return
        }
        
        _playlists.value = _playlists.value.filter { it.id != playlistId }
        savePlaylists()
        Log.d(TAG, "Deleted playlist: $playlistId")
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(
                    name = newName,
                    dateModified = System.currentTimeMillis()
                )
            } else {
                playlist
            }
        }
        Log.d(TAG, "Renamed playlist to: $newName")
        savePlaylists()
    }

    fun setSelectedSongForPlaylist(song: Song) {
        _selectedSongForPlaylist.value = song
    }

    fun clearSelectedSongForPlaylist() {
        _selectedSongForPlaylist.value = null
    }
    
    fun setTargetPlaylistForAddingSongs(playlistId: String) {
        _targetPlaylistId.value = playlistId
    }
    
    fun clearTargetPlaylistForAddingSongs() {
        _targetPlaylistId.value = null
    }
    
    // Playlist Import/Export Functions
    
    /**
     * Exports a single playlist to the specified format
     */
    fun exportPlaylist(
        playlistId: String,
        format: PlaylistImportExportUtils.PlaylistExportFormat,
        userSelectedDirectoryUri: Uri? = null,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val playlist = _playlists.value.find { it.id == playlistId }
                if (playlist == null) {
                    onResult(Result.failure(IllegalArgumentException("Playlist not found")))
                    return@launch
                }
                
                val result = withContext(Dispatchers.IO) {
                    PlaylistImportExportUtils.exportPlaylist(
                        context = getApplication<Application>(),
                        playlist = playlist,
                        format = format,
                        userSelectedDirectoryUri = userSelectedDirectoryUri
                    )
                }
                
                result.fold(
                    onSuccess = { file ->
                        onResult(Result.success("Playlist '${playlist.name}' exported to ${file.absolutePath}"))
                        Log.d(TAG, "Successfully exported playlist: ${playlist.name}")
                    },
                    onFailure = { exception ->
                        onResult(Result.failure(exception))
                        Log.e(TAG, "Failed to export playlist: ${playlist.name}", exception)
                    }
                )
            } catch (e: Exception) {
                onResult(Result.failure(e))
                Log.e(TAG, "Error in exportPlaylist", e)
            }
        }
    }
    
    /**
     * Exports all user-created playlists (excludes default playlists)
     */
    fun exportAllPlaylists(
        format: PlaylistImportExportUtils.PlaylistExportFormat,
        includeDefaultPlaylists: Boolean = false,
        userSelectedDirectoryUri: Uri? = null,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting export all playlists operation")
                
                val playlistsToExport = if (includeDefaultPlaylists) {
                    _playlists.value
                } else {
                    // Exclude default playlists (Favorites, Recently Added, Most Played)
                    _playlists.value.filter { it.id !in listOf("1", "2", "3") }
                }
                
                if (playlistsToExport.isEmpty()) {
                    Log.w(TAG, "No playlists to export")
                    onResult(Result.failure(IllegalStateException("No playlists available to export")))
                    return@launch
                }
                
                Log.d(TAG, "Exporting ${playlistsToExport.size} playlists")
                
                // Add timeout to prevent infinite loading
                val result = withTimeoutOrNull(30000) { // 30 second timeout
                    withContext(Dispatchers.IO) {
                        PlaylistImportExportUtils.exportAllPlaylists(
                            context = getApplication<Application>(),
                            playlists = playlistsToExport,
                            format = format,
                            userSelectedDirectoryUri = userSelectedDirectoryUri
                        )
                    }
                }
                
                if (result == null) {
                    Log.e(TAG, "Export operation timed out")
                    onResult(Result.failure(Exception("Export operation timed out after 30 seconds")))
                    return@launch
                }
                
                result.fold(
                    onSuccess = { file ->
                        Log.d(TAG, "Successfully exported ${playlistsToExport.size} playlists to ${file.absolutePath}")
                        onResult(Result.success("Successfully exported ${playlistsToExport.size} playlists to ${file.absolutePath}"))
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to export all playlists", exception)
                        onResult(Result.failure(exception))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in exportAllPlaylists", e)
                onResult(Result.failure(e))
            }
        }
    }
    
    /**
     * Imports a playlist from a file URI
     */
    fun importPlaylist(
        uri: Uri,
        onResult: (Result<String>) -> Unit,
        onRestartRequired: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting import playlist operation from URI: $uri")
                
                // Add timeout to prevent infinite loading
                val result = withTimeoutOrNull(30000) { // 30 second timeout
                    withContext(Dispatchers.IO) {
                        PlaylistImportExportUtils.importPlaylist(
                            context = getApplication<Application>(),
                            uri = uri,
                            availableSongs = _songs.value
                        )
                    }
                }
                
                if (result == null) {
                    Log.e(TAG, "Import operation timed out")
                    onResult(Result.failure(Exception("Import operation timed out after 30 seconds")))
                    return@launch
                }
                
                result.fold(
                    onSuccess = { importedPlaylist ->
                        Log.d(TAG, "Successfully imported playlist from utility: ${importedPlaylist.name}")
                        
                        // Check if playlist with same name already exists
                        val existingPlaylist = _playlists.value.find { it.name == importedPlaylist.name }
                        val finalPlaylist = if (existingPlaylist != null) {
                            // Add suffix to avoid name conflict
                            importedPlaylist.copy(name = "${importedPlaylist.name} (Imported)")
                        } else {
                            importedPlaylist
                        }
                        
                        // Add the imported playlist to our list
                        _playlists.value = _playlists.value + finalPlaylist
                        savePlaylists()
                        
                        val matchedCount = finalPlaylist.songs.size
                        Log.d(TAG, "Successfully imported playlist: ${finalPlaylist.name} with $matchedCount songs")
                        onResult(Result.success("Successfully imported playlist '${finalPlaylist.name}' with $matchedCount songs. App restart recommended for best experience."))
                        
                        // Recommend app restart for imported playlists to ensure proper UI update
                        onRestartRequired?.invoke()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to import playlist from $uri", exception)
                        onResult(Result.failure(exception))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in importPlaylist", e)
                onResult(Result.failure(e))
            }
        }
    }
    
    /**
     * Restarts the application to ensure all changes take effect
     */
    fun restartApp() {
        val context = getApplication<Application>().applicationContext
        
        // Create restart intent
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            
            // Start the activity and exit current process
            context.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
    
    /**
     * Gets all available export formats
     */
    fun getAvailableExportFormats(): List<PlaylistImportExportUtils.PlaylistExportFormat> {
        return PlaylistImportExportUtils.PlaylistExportFormat.values().toList()
    }
    
    // Sort library functionality
    fun sortLibrary() {
        viewModelScope.launch {
            // Cycle through sort orders
            _sortOrder.value = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> SortOrder.TITLE_DESC
                SortOrder.TITLE_DESC -> SortOrder.ARTIST_ASC
                SortOrder.ARTIST_ASC -> SortOrder.ARTIST_DESC
                SortOrder.ARTIST_DESC -> SortOrder.DATE_ADDED_ASC
                SortOrder.DATE_ADDED_ASC -> SortOrder.DATE_ADDED_DESC
                SortOrder.DATE_ADDED_DESC -> SortOrder.DATE_MODIFIED_ASC
                SortOrder.DATE_MODIFIED_ASC -> SortOrder.DATE_MODIFIED_DESC
                SortOrder.DATE_MODIFIED_DESC -> SortOrder.TITLE_ASC
            }
            
            // Sort songs based on current sort order
            _songs.value = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> _songs.value.sortedBy { it.title }
                SortOrder.TITLE_DESC -> _songs.value.sortedByDescending { it.title }
                SortOrder.ARTIST_ASC -> _songs.value.sortedBy { it.artist }
                SortOrder.ARTIST_DESC -> _songs.value.sortedByDescending { it.artist }
                SortOrder.DATE_ADDED_ASC -> _songs.value.sortedBy { it.dateAdded }
                SortOrder.DATE_ADDED_DESC -> _songs.value.sortedByDescending { it.dateAdded }
                SortOrder.DATE_MODIFIED_ASC -> _songs.value.sortedBy { it.dateAdded } // Using dateAdded as a proxy for dateModified for songs
                SortOrder.DATE_MODIFIED_DESC -> _songs.value.sortedByDescending { it.dateAdded } // Using dateAdded as a proxy for dateModified for songs
            }
            
            // Sort albums based on current sort order
            _albums.value = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> _albums.value.sortedBy { it.title }
                SortOrder.TITLE_DESC -> _albums.value.sortedByDescending { it.title }
                SortOrder.ARTIST_ASC -> _albums.value.sortedBy { it.artist }
                SortOrder.ARTIST_DESC -> _albums.value.sortedByDescending { it.artist }
                SortOrder.DATE_ADDED_ASC -> _albums.value.sortedBy { it.id.toLongOrNull() ?: 0L } // Placeholder for date added
                SortOrder.DATE_ADDED_DESC -> _albums.value.sortedByDescending { it.id.toLongOrNull() ?: 0L } // Placeholder for date added
                SortOrder.DATE_MODIFIED_ASC -> _albums.value.sortedBy { it.dateModified }
                SortOrder.DATE_MODIFIED_DESC -> _albums.value.sortedByDescending { it.dateModified }
            }
            
            // Sort playlists by name, keeping the default playlists at the top
            _playlists.value = _playlists.value.sortedWith(
                compareBy<Playlist> { 
                    // Put default playlists first
                    when (it.id) {
                        "1" -> 0 // Favorites
                        "2" -> 1 // Recently Added
                        "3" -> 2 // Most Played
                        else -> 3 // User-created playlists
                    }
                }.thenBy { 
                    // Then sort by name according to current sort order
                    when (_sortOrder.value) {
                        SortOrder.TITLE_ASC, SortOrder.ARTIST_ASC, SortOrder.DATE_ADDED_ASC, SortOrder.DATE_MODIFIED_ASC -> it.name
                        SortOrder.TITLE_DESC, SortOrder.ARTIST_DESC, SortOrder.DATE_ADDED_DESC, SortOrder.DATE_MODIFIED_DESC -> it.name.reversed()
                    }
                }
            )
            
            Log.d(TAG, "Library sorted: ${_sortOrder.value}")
        }
    }
    
    // Set specific sort order
    fun setSortOrder(newSortOrder: SortOrder) {
        viewModelScope.launch {
            if (_sortOrder.value != newSortOrder) {
                _sortOrder.value = newSortOrder
                // Save sort order to AppSettings for persistence
                appSettings.setSongsSortOrder(newSortOrder.name)
                
                // Sort songs based on new sort order
                _songs.value = when (newSortOrder) {
                    SortOrder.TITLE_ASC -> _songs.value.sortedBy { it.title }
                    SortOrder.TITLE_DESC -> _songs.value.sortedByDescending { it.title }
                    SortOrder.ARTIST_ASC -> _songs.value.sortedBy { it.artist }
                    SortOrder.ARTIST_DESC -> _songs.value.sortedByDescending { it.artist }
                    SortOrder.DATE_ADDED_ASC -> _songs.value.sortedBy { it.dateAdded }
                    SortOrder.DATE_ADDED_DESC -> _songs.value.sortedByDescending { it.dateAdded }
                    SortOrder.DATE_MODIFIED_ASC -> _songs.value.sortedBy { it.dateAdded } // Using dateAdded as a proxy for dateModified for songs
                    SortOrder.DATE_MODIFIED_DESC -> _songs.value.sortedByDescending { it.dateAdded } // Using dateAdded as a proxy for dateModified for songs
                }
                
                // Sort albums based on new sort order
                _albums.value = when (newSortOrder) {
                    SortOrder.TITLE_ASC -> _albums.value.sortedBy { it.title }
                    SortOrder.TITLE_DESC -> _albums.value.sortedByDescending { it.title }
                    SortOrder.ARTIST_ASC -> _albums.value.sortedBy { it.artist }
                    SortOrder.ARTIST_DESC -> _albums.value.sortedByDescending { it.artist }
                    SortOrder.DATE_ADDED_ASC -> _albums.value.sortedBy { it.id.toLongOrNull() ?: 0L } // Placeholder for date added
                    SortOrder.DATE_ADDED_DESC -> _albums.value.sortedByDescending { it.id.toLongOrNull() ?: 0L } // Placeholder for date added
                    SortOrder.DATE_MODIFIED_ASC -> _albums.value.sortedBy { it.dateModified }
                    SortOrder.DATE_MODIFIED_DESC -> _albums.value.sortedByDescending { it.dateModified }
                }
                
                // Sort playlists by name, keeping the default playlists at the top
                _playlists.value = _playlists.value.sortedWith(
                    compareBy<Playlist> { 
                        // Put default playlists first
                        when (it.id) {
                            "1" -> 0 // Favorites
                            "2" -> 1 // Recently Added
                            "3" -> 2 // Most Played
                            else -> 3 // User-created playlists
                        }
                    }.thenBy { 
                        // Then sort by name according to current sort order
                        when (newSortOrder) {
                            SortOrder.TITLE_ASC, SortOrder.ARTIST_ASC -> it.name
                            SortOrder.TITLE_DESC, SortOrder.ARTIST_DESC -> it.name.reversed()
                            SortOrder.DATE_ADDED_ASC -> it.dateCreated
                            SortOrder.DATE_ADDED_DESC -> -it.dateCreated // Descending
                            SortOrder.DATE_MODIFIED_ASC -> it.dateModified
                            SortOrder.DATE_MODIFIED_DESC -> -it.dateModified // Descending
                            else -> it.name // Default for other sort orders
                        }
                    }
                )
                
                Log.d(TAG, "Sort order set to: $newSortOrder")
            }
        }
    }

    /**
     * Loads all settings from SharedPreferences
     */
    private fun loadSettings() {
        Log.d(TAG, "Loading settings from SharedPreferences")
        // Settings are now handled by AppSettings, verify they are loaded
        Log.d(TAG, "Loaded settings: " +
                "HQ Audio=${enableHighQualityAudio.value}, " +
                "Gapless=${enableGaplessPlayback.value}, " +
                "Crossfade=${enableCrossfade.value} (${crossfadeDuration.value}s), " +
                "Normalization=${enableAudioNormalization.value}, " +
                "ReplayGain=${enableReplayGain.value}, " +
                "ShowLyrics=${showLyrics.value}, " +
                "OnlineOnlyLyrics=${showOnlineOnlyLyrics.value}, " +
                "UseSystemTheme=${useSystemTheme.value}, " +
                "DarkMode=${darkMode.value}, " +
                "AutoConnectDevice=${autoConnectDevice.value}, " +
                "MaxCacheSize=${maxCacheSize.value}, " +
                "ClearCacheOnExit=${clearCacheOnExit.value}")

        // Load song play counts
        _songPlayCounts.value = appSettings.songPlayCounts.value
    }

    /**
     * Updates the show lyrics setting
     */
    fun setShowLyrics(show: Boolean) {
        appSettings.setShowLyrics(show)
        if (show && currentSong.value != null) {
            fetchLyricsForCurrentSong()
        } else {
            _currentLyrics.value = null
        }
    }
    
    /**
     * Updates the online-only lyrics setting
     */
    fun setShowOnlineOnlyLyrics(onlineOnly: Boolean) {
        appSettings.setOnlineOnlyLyrics(onlineOnly)
        if (showLyrics.value && currentSong.value != null) {
            fetchLyricsForCurrentSong()
        }
    }
    
    /**
     * Fetches lyrics for the current song if settings allow, with automatic retry logic
     * Now properly handles race conditions and song changes
     */
    private fun fetchLyricsForCurrentSong(retryCount: Int = 0) {
        val song = currentSong.value ?: return
        
        // Cancel any previous lyrics fetch to prevent race conditions
        lyricsFetchJob?.cancel()
        
        // Clear current lyrics first only on initial attempt
        // This prevents showing stale lyrics from previous song
        if (retryCount == 0) {
            _currentLyrics.value = null
        }
        
        // Check if lyrics are enabled
        if (!showLyrics.value) {
            return
        }
        
        // Check if we should only fetch lyrics when online
        if (showOnlineOnlyLyrics.value && !repository.isNetworkAvailable()) {
            Log.d(TAG, "Online-only lyrics enabled but device is offline")
            _currentLyrics.value = LyricsData("Online-only lyrics enabled.\nConnect to the internet to view lyrics.", null)
            _isLoadingLyrics.value = false
            return
        }
        
        // Create a new job for this lyrics fetch
        lyricsFetchJob = viewModelScope.launch {
            _isLoadingLyrics.value = true
            try {
                // Store the song ID to validate it hasn't changed
                val fetchingSongId = song.id
                Log.d(TAG, "Fetching lyrics for: ${song.artist} - ${song.title} (ID: $fetchingSongId)")
                
                val lyricsData = repository.fetchLyrics(song.artist, song.title, song.id)
                
                // Verify the song hasn't changed before updating lyrics
                if (currentSong.value?.id == fetchingSongId && isActive) {
                    _currentLyrics.value = lyricsData
                    Log.d(TAG, "Successfully fetched lyrics for: ${song.artist} - ${song.title}")
                } else {
                    Log.d(TAG, "Song changed during lyrics fetch, discarding results for: ${song.title}")
                }
            } catch (e: Exception) {
                // If the job was cancelled, don't log as error
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Lyrics fetch cancelled for: ${song.title}")
                    throw e // Re-throw to properly handle cancellation
                }
                
                Log.e(TAG, "Error fetching lyrics (attempt ${retryCount + 1})", e)
                
                // Only retry if the song is still the same
                if (currentSong.value?.id != song.id) {
                    Log.d(TAG, "Song changed, not retrying lyrics fetch")
                    return@launch
                }
                
                // Retry logic - try up to 2 more times with exponential backoff
                if (retryCount < 2) {
                    val delayMs = (1000 * (retryCount + 1)).toLong() // 1s, 2s delays
                    Log.d(TAG, "Retrying lyrics fetch in ${delayMs}ms...")
                    delay(delayMs)
                    
                    // Check if the song is still the same before retrying
                    if (currentSong.value?.id == song.id && isActive) {
                        fetchLyricsForCurrentSong(retryCount + 1)
                        return@launch
                    } else {
                        Log.d(TAG, "Song changed during retry, cancelling lyrics fetch")
                    }
                } else {
                    // All retries failed - only show error if song hasn't changed
                    if (currentSong.value?.id == song.id && isActive) {
                        Log.w(TAG, "Failed to fetch lyrics after ${retryCount + 1} attempts")
                        _currentLyrics.value = LyricsData("Unable to load lyrics. Tap to retry.", null)
                    }
                }
            } finally {
                if (isActive) {
                    _isLoadingLyrics.value = false
                }
            }
        }
    }
    
    /**
     * Manually retry fetching lyrics for the current song
     */
    fun retryFetchLyrics() {
        Log.d(TAG, "Manual retry of lyrics fetch requested")
        fetchLyricsForCurrentSong(0)
    }
    
    /**
     * Save edited lyrics for the current song to cache
     */
    fun saveEditedLyrics(editedLyrics: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val song = _currentSong.value
                if (song != null) {
                    val artist = song.artist
                    val title = song.title
                    
                    // Determine if lyrics are synced (contains timestamps)
                    val isSynced = editedLyrics.contains(Regex("\\[\\d{2}:\\d{2}\\.\\d{2}]"))
                    
                    val lyricsData = if (isSynced) {
                        LyricsData(plainLyrics = null, syncedLyrics = editedLyrics)
                    } else {
                        LyricsData(plainLyrics = editedLyrics, syncedLyrics = null)
                    }
                    
                    // Save to cache (internal storage)
                    val fileName = "${artist}_${title}.json".replace(Regex("[^a-zA-Z0-9._-]"), "_")
                    val lyricsDir = File(getApplication<Application>().filesDir, "lyrics")
                    if (!lyricsDir.exists()) {
                        lyricsDir.mkdirs()
                    }
                    val file = File(lyricsDir, fileName)
                    val json = Gson().toJson(lyricsData)
                    file.writeText(json)
                    
                    // Update in-memory state
                    _currentLyrics.value = lyricsData
                    
                    Log.d(TAG, "Saved edited lyrics for: $title by $artist")
                } else {
                    Log.w(TAG, "Cannot save lyrics - no current song")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving edited lyrics", e)
            }
        }
    }

    fun setVolume(newVolume: Float) {
        val clampedVolume = newVolume.coerceIn(0f, 1f)
        Log.d(TAG, "Setting volume to: $clampedVolume")
        mediaController?.let { controller ->
            controller.volume = clampedVolume
            _volume.value = clampedVolume
            if (clampedVolume > 0f) {
                _isMuted.value = false
            }
        }
    }
    
    fun toggleMute() {
        Log.d(TAG, "Toggling mute")
        if (_isMuted.value) {
            // Unmute - restore previous volume
            setVolume(_previousVolume)
            _isMuted.value = false
        } else {
            // Mute - save current volume and set to 0
            _previousVolume = _volume.value
            setVolume(0f)
            _isMuted.value = true
        }
    }
    
    fun maxVolume() {
        Log.d(TAG, "Setting max volume")
        setVolume(1.0f)
    }

    /**
     * Opens the system equalizer for the current audio session
     */
    fun openSystemEqualizer() {
        val context = getApplication<Application>()
        EqualizerUtils.openSystemEqualizer(context)
    }

    // Playback settings functions
    fun setHighQualityAudio(enable: Boolean) {
        appSettings.setHighQualityAudio(enable)
        applyPlaybackSettings()
    }
    
    fun setGaplessPlayback(enable: Boolean) {
        appSettings.setGaplessPlayback(enable)
        applyPlaybackSettings()
    }
    
    fun setCrossfade(enable: Boolean) {
        appSettings.setCrossfade(enable)
        applyPlaybackSettings()
    }
    
    fun setCrossfadeDuration(duration: Float) {
        appSettings.setCrossfadeDuration(duration)
        applyPlaybackSettings()
    }
    
    fun setAudioNormalization(enable: Boolean) {
        appSettings.setAudioNormalization(enable)
        applyPlaybackSettings()
    }
    
    fun setReplayGain(enable: Boolean) {
        appSettings.setReplayGain(enable)
        applyPlaybackSettings()
    }
    
    private fun applyPlaybackSettings() {
        // Apply settings to the media player
        mediaController?.let { controller ->
            Log.d(TAG, "Applied playback settings: " +
                    "HQ Audio=${enableHighQualityAudio.value}, " +
                    "Gapless=${enableGaplessPlayback.value}, " +
                    "Crossfade=${enableCrossfade.value} (${crossfadeDuration.value}s), " +
                    "Normalization=${enableAudioNormalization.value}, " +
                    "ReplayGain=${enableReplayGain.value}")
            
            // Send intent to update service settings
            val context = getApplication<Application>()
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.ACTION_UPDATE_SETTINGS
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start media service for settings update: ${e.message}", e)
            }
        }
    }

    /**
     * Add a song to the queue
     */
    fun addSongToQueue(song: Song) {
        Log.d(TAG, "Adding song to queue: ${song.title}")
        
        // Clear any previous error
        _queueOperationError.value = null
        
        mediaController?.let { controller ->
            try {
                val mediaItem = MediaItem.Builder()
                    .setMediaId(song.id)
                    .setUri(song.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(song.artworkUri)
                            .build()
                    )
                    .build()
                
                // Add to media controller queue
                controller.addMediaItem(mediaItem)
                
                // If nothing is currently playing, start playback
                if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_ENDED) {
                    controller.prepare()
                    controller.play()
                }
                
                // Update the queue in our state - make a defensive copy
                val currentQueueSongs = _currentQueue.value.songs.toMutableList()
                currentQueueSongs.add(song)
                
                // Make sure current index is valid
                val currentIndex = if (_currentQueue.value.currentIndex == -1 && currentQueueSongs.size == 1) {
                    // First song added to empty queue
                    0
                } else {
                    _currentQueue.value.currentIndex
                }
                
                _currentQueue.value = Queue(currentQueueSongs, currentIndex)
                
                Log.d(TAG, "Successfully added '${song.title}'. Queue now has ${currentQueueSongs.size} songs, current index: $currentIndex")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding song to queue", e)
                _queueOperationError.value = "Failed to add '${song.title}' to queue: ${e.message}"
            }
        } ?: run {
            val errorMsg = "Cannot add song to queue - media controller is null"
            Log.e(TAG, errorMsg)
            _queueOperationError.value = errorMsg
        }
    }

    /**
     * Add a song to play next (right after the current song in the queue)
     */
    fun playNext(song: Song) {
        Log.d(TAG, "Adding song to play next: ${song.title}")
        
        // Clear any previous error
        _queueOperationError.value = null
        
        mediaController?.let { controller ->
            try {
                val mediaItem = MediaItem.Builder()
                    .setMediaId(song.id)
                    .setUri(song.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(song.artworkUri)
                            .build()
                    )
                    .build()
                
                // Calculate the position to insert (right after current song)
                val currentIndex = controller.currentMediaItemIndex
                val insertIndex = if (currentIndex >= 0) currentIndex + 1 else 0
                
                // Add to media controller queue at specific position
                controller.addMediaItem(insertIndex, mediaItem)
                
                // If nothing is currently playing, start playback
                if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_ENDED) {
                    controller.prepare()
                    controller.play()
                }
                
                // Update the queue in our state - make a defensive copy
                val currentQueueSongs = _currentQueue.value.songs.toMutableList()
                val currentQueueIndex = _currentQueue.value.currentIndex
                val queueInsertIndex = if (currentQueueIndex >= 0 && currentQueueIndex < currentQueueSongs.size) {
                    currentQueueIndex + 1
                } else {
                    0
                }
                currentQueueSongs.add(queueInsertIndex, song)
                
                _currentQueue.value = Queue(currentQueueSongs, currentQueueIndex)
                
                Log.d(TAG, "Successfully added '${song.title}' to play next at position $queueInsertIndex. Queue now has ${currentQueueSongs.size} songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding song to play next", e)
                _queueOperationError.value = "Failed to add '${song.title}' to play next: ${e.message}"
            }
        } ?: run {
            val errorMsg = "Cannot add song to play next - media controller is null"
            Log.e(TAG, errorMsg)
            _queueOperationError.value = errorMsg
        }
    }

    /**
     * Remove a song from the queue
     */
    fun removeFromQueue(song: Song) {
        Log.d(TAG, "Removing song from queue: ${song.title}")
        
        // Clear any previous error
        _queueOperationError.value = null
        
        val currentQueue = _currentQueue.value
        val currentSong = _currentSong.value
        val songIndex = currentQueue.songs.indexOfFirst { it.id == song.id }
        
        // Check if the song is in the queue
        if (songIndex == -1) {
            Log.d(TAG, "Song '${song.title}' not found in queue")
            _queueOperationError.value = "Song '${song.title}' not found in queue"
            return
        }
        
        // Don't remove the currently playing song
        if (currentSong != null && song.id == currentSong.id) {
            Log.d(TAG, "Cannot remove currently playing song: ${song.title}")
            _queueOperationError.value = "Cannot remove the currently playing song"
            return
        }
        
        mediaController?.let { controller ->
            try {
                // Check if the controller has this media item
                if (songIndex < controller.mediaItemCount) {
                    // Remove from the media controller
                    Log.d(TAG, "Removing media item at position $songIndex")
                    controller.removeMediaItem(songIndex)
                } else {
                    throw IndexOutOfBoundsException("Media item index out of bounds: $songIndex, controller has ${controller.mediaItemCount} items")
                }
                
                // Update the local queue state - this ensures UI is still updated even if controller fails
                val updatedSongs = currentQueue.songs.toMutableList().apply {
                    removeAt(songIndex)
                }
                
                // Adjust current index if needed
                val newIndex = if (songIndex < currentQueue.currentIndex) {
                    currentQueue.currentIndex - 1
                } else {
                    currentQueue.currentIndex
                }
                
                Log.d(TAG, "Successfully removed '${song.title}'. Queue now has ${updatedSongs.size} songs, current index: $newIndex")
                _currentQueue.value = Queue(updatedSongs, newIndex)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing song from queue", e)
                _queueOperationError.value = "Failed to remove '${song.title}' from queue: ${e.message}"
            }
        } ?: run {
            val errorMsg = "Cannot remove song - media controller is null"
            Log.e(TAG, errorMsg)
            _queueOperationError.value = errorMsg
        }
    }
    
    /**
     * Move a song in the queue from one position to another
     */
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        Log.d(TAG, "Moving queue item from $fromIndex to $toIndex")
        
        // Clear any previous error
        _queueOperationError.value = null
        
        val currentQueue = _currentQueue.value
        val songs = currentQueue.songs.toMutableList()
        
        // Input validation to prevent index out of bounds
        if (fromIndex < 0 || fromIndex >= songs.size || toIndex < 0 || toIndex >= songs.size) {
            val errorMsg = "Cannot move queue item - invalid indices: from=$fromIndex, to=$toIndex, size=${songs.size}"
            Log.e(TAG, errorMsg)
            _queueOperationError.value = errorMsg
            return
        }
        
        // Additional validation for edge cases
        if (fromIndex == toIndex) {
            Log.d(TAG, "Moving queue item to same position, ignoring")
            return
        }
        
        // Safely update local state first
        try {
            // Store original state for rollback
            val originalSongs = songs.toList()
            val originalCurrentIndex = currentQueue.currentIndex
            
            // Apply the move in the local queue
            val song = songs.removeAt(fromIndex)
            songs.add(toIndex, song)
            
            // Calculate new current index with better logic
            val newCurrentIndex = when {
                // If we moved the current song
                fromIndex == currentQueue.currentIndex -> toIndex
                // If we moved a song from before the current song to after it
                fromIndex < currentQueue.currentIndex && toIndex >= currentQueue.currentIndex -> 
                    currentQueue.currentIndex - 1
                // If we moved a song from after the current song to before it
                fromIndex > currentQueue.currentIndex && toIndex <= currentQueue.currentIndex -> 
                    currentQueue.currentIndex + 1
                // Otherwise, current index doesn't change
                else -> currentQueue.currentIndex
            }.coerceIn(0, songs.size - 1) // Ensure index is always valid
            
            // Update local state optimistically
            _currentQueue.value = Queue(songs, newCurrentIndex)
            
            // Also move it in the MediaController with proper error handling
            mediaController?.let { controller ->
                try {
                    // Validate controller state
                    if (controller.mediaItemCount != originalSongs.size) {
                        Log.w(TAG, "MediaController item count mismatch: expected ${originalSongs.size}, got ${controller.mediaItemCount}")
                    }
                    
                    if (fromIndex < controller.mediaItemCount && toIndex < controller.mediaItemCount) {
                        Log.d(TAG, "Moving media item in controller from $fromIndex to $toIndex")
                        controller.moveMediaItem(fromIndex, toIndex)
                        Log.d(TAG, "Successfully moved queue item from $fromIndex to $toIndex, new current index: $newCurrentIndex")
                    } else {
                        throw IndexOutOfBoundsException("Cannot move media item - index out of bounds in controller: from=$fromIndex, to=$toIndex, count=${controller.mediaItemCount}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error moving media item in controller, rolling back UI state", e)
                    // Rollback to original state
                    _currentQueue.value = Queue(originalSongs, originalCurrentIndex)
                    _queueOperationError.value = "Failed to move queue item: ${e.message}"
                    return
                }
            } ?: run {
                Log.e(TAG, "Cannot move queue item - media controller is null, rolling back UI state")
                // Rollback to original state
                _currentQueue.value = Queue(originalSongs, originalCurrentIndex)
                _queueOperationError.value = "Cannot move queue item - media controller is null"
                return
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error moving queue item", e)
            _queueOperationError.value = "Failed to move queue item: ${e.message}"
        }
    }
    
    /**
     * Add multiple songs to the queue
     */
    fun addSongsToQueue(songs: List<Song>) {
        Log.d(TAG, "Adding ${songs.size} songs to queue")
        
        if (songs.isEmpty()) {
            Log.d(TAG, "No songs to add to queue")
            return
        }
        
        // Clear any previous error
        _queueOperationError.value = null
        
        mediaController?.let { controller ->
            try {
                // Add each song to the media controller
                songs.forEach { song ->
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(song.id)
                        .setUri(song.uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumTitle(song.album)
                                .setArtworkUri(song.artworkUri)
                                .build()
                        )
                        .build()
                    
                    controller.addMediaItem(mediaItem)
                }
                
                // If nothing is currently playing, start playback
                if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_ENDED) {
                    controller.prepare()
                   
                    controller.play()
                }
                
                // Update the queue in our state - make a defensive copy
                val currentQueueSongs = _currentQueue.value.songs.toMutableList()
                currentQueueSongs.addAll(songs)
                _currentQueue.value = Queue(currentQueueSongs, _currentQueue.value.currentIndex)
                
                Log.d(TAG, "Successfully added ${songs.size} songs. Queue now has ${currentQueueSongs.size} songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding songs to queue", e)
                _queueOperationError.value = "Failed to add ${songs.size} songs to queue: ${e.message}"
            }
        } ?: run {
            val errorMsg = "Cannot add songs to queue - media controller is null"
            Log.e(TAG, errorMsg)
            _queueOperationError.value = errorMsg
        }
    }

    /**
     * Add songs to the queue (legacy method for single song - keeping for backward compatibility)
     */
    fun addSongsToQueue() {
        // This method is now a placeholder for UI navigation
        // The actual song addition should use addSongsToQueue(songs: List<Song>)
    }

    // Search history methods
    private fun loadSearchHistory() {
        val searchHistoryJson = appSettings.searchHistory.value
        if (searchHistoryJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val history = GsonUtils.gson.fromJson<List<String>>(searchHistoryJson, type)
                _searchHistory.value = history
            } catch (e: Exception) {
                Log.e(TAG, "Error loading search history", e)
                _searchHistory.value = emptyList()
            }
        }
    }
    
    private fun saveSearchHistory() {
        val searchHistoryJson = GsonUtils.gson.toJson(_searchHistory.value)
        appSettings.setSearchHistory(searchHistoryJson)
    }
    
    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        appSettings.setSearchHistory(null)
    }

    /**
     * Checks if the service is connected and ready
     */
    fun isServiceConnected(): Boolean {
        return _serviceConnected.value && mediaController != null
    }
    
    /**
     * Checks if music is currently playing
     */
    fun isPlaying(): Boolean {
        return _isPlaying.value
    }
    
    /**
     * Plays an external audio file that was opened from outside the app
     */
    fun playExternalAudioFile(song: Song) {
        Log.d(TAG, "Playing external audio file: ${song.title}, URI: ${song.uri}")
        
        // Add to recently played list
        updateRecentlyPlayed(song)
        
        // Create a media item from the song
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.artworkUri)
                    .build()
            )
            .build()
        
        // First check if we have a valid controller
        if (mediaController == null) {
            Log.d(TAG, "Media controller is null, reconnecting to service")
            // Try to reconnect to the service
            connectToMediaService()
            
            // Create a delayed job to retry playback once we have a controller
            viewModelScope.launch {
                var attempts = 0
                val maxAttempts = 15  // Increased from 10 to 15 attempts for cold starts
                val delayMs = 800L    // Increased from 500ms to 800ms between attempts
                
                // Add initial delay to give service more time to fully initialize
                delay(1000)
                
                while (mediaController == null && attempts < maxAttempts) {
                    delay(delayMs)
                    attempts++
                    Log.d(TAG, "Waiting for media controller (attempt $attempts)")
                    
                    // Try reconnecting if we're still not connected after half the attempts
                    if (attempts == maxAttempts / 2) {
                        Log.d(TAG, "Still no controller, trying to reconnect...")
                        connectToMediaService()
                    }
                }
                
                if (mediaController != null) {
                    actuallyPlayExternalFile(mediaController!!, mediaItem, song)
                } else {
                    Log.e(TAG, "Failed to obtain media controller after $maxAttempts attempts")
                    // Update UI state to reflect that we have a song but it's not playing
                    _currentSong.value = song
                    _isPlaying.value = false
                    _currentQueue.value = Queue(listOf(song), 0)
                }
            }
        } else {
            // We have a controller, use it directly
            actuallyPlayExternalFile(mediaController!!, mediaItem, song)
        }
    }
    
    /**
     * Helper method to actually play the external file once we have a valid controller
     */
    private fun actuallyPlayExternalFile(controller: MediaController, mediaItem: MediaItem, song: Song) {
        Log.d(TAG, "Using controller to play: ${song.title}")
        
        try {
            // Clear existing queue to avoid conflicts
            controller.clearMediaItems()
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
            
            // Update UI state
            _currentSong.value = song
            _isPlaying.value = true
            
            // Create a new queue with just this song
            _currentQueue.value = Queue(listOf(song), 0)
            
            // Update favorite status
            _isFavorite.value = _favoriteSongs.value.contains(song.id)
            
            // Start progress tracking
            startProgressUpdates()
            
            // Mark the service as connected
            _serviceConnected.value = true
            
            // Double-check if playback actually started with multiple retries
            viewModelScope.launch {
                var retryCount = 0
                val maxRetries = 5
                
                while (retryCount < maxRetries) {
                    delay(500)
                    if (!controller.isPlaying) {
                        Log.d(TAG, "Playback didn't start, retry #${retryCount + 1}")
                        controller.play()
                        retryCount++
                    } else {
                        // Playback started successfully
                        Log.d(TAG, "Playback confirmed as started")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing external file", e)
            // Mark UI as not playing
            _isPlaying.value = false
            // Still update other UI elements so the user sees the song
            _currentSong.value = song
            _currentQueue.value = Queue(listOf(song), 0)
        }
    }

    /**
     * Shuffles all available songs and plays them as a queue
     */
    /**
     * Plays a list of songs as a queue.
     */
    fun playSongs(songs: List<Song>) {
        Log.d(TAG, "Playing list of songs: ${songs.size} songs")
        playQueue(songs)
    }

    /**
     * Shuffles a list of songs and plays them as a queue.
     */
    fun playShuffled(songs: List<Song>) {
        Log.d(TAG, "Playing shuffled list of songs: ${songs.size} songs")
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            playQueue(shuffledSongs)

            // Enable shuffle mode after starting playback
            mediaController?.shuffleModeEnabled = true
            _isShuffleEnabled.value = true

            Log.d(TAG, "Started shuffled playback of ${songs.size} songs")
        } else {
            Log.e(TAG, "No songs provided to play shuffled")
        }
    }

    fun playShuffledSongs() {
        val allSongs = _songs.value
        if (allSongs.isEmpty()) return
        
        // Shuffle the songs
        val shuffledSongs = allSongs.shuffled()
        
        // Start with the first song
        val firstSong = shuffledSongs.first()
        Log.d(TAG, "Playing shuffled songs starting with: ${firstSong.title}")
        
        // Set shuffle mode to enabled
        mediaController?.shuffleModeEnabled = true
        _isShuffleEnabled.value = true
        
        // Create a new queue with the shuffled songs
        _currentQueue.value = Queue(shuffledSongs, 0)
        
        // Play the first song
        playSong(firstSong)
        
        // Add to recently played
        updateRecentlyPlayed(firstSong)
    }

    // Search history methods
    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            val currentHistory = _searchHistory.value.toMutableList()
            
            // Remove the query if it already exists to avoid duplicates
            currentHistory.remove(query)
            
            // Add the new query at the beginning
            currentHistory.add(0, query)
            
            // Limit history to 10 items
            val limitedHistory = currentHistory.take(10)
            
            // Update the state
            _searchHistory.value = limitedHistory
            
            // Save to SharedPreferences
            saveSearchHistory()
        }
    }

    // Theme Settings Methods
    fun setUseSystemTheme(use: Boolean) {
        appSettings.setUseSystemTheme(use)
    }
    
    fun setDarkMode(dark: Boolean) {
        appSettings.setDarkMode(dark)
    }
    
    // Cache Settings Methods
    fun setMaxCacheSize(size: Long) {
        appSettings.setMaxCacheSize(size)
    }
    
    fun setClearCacheOnExit(clear: Boolean) {
        appSettings.setClearCacheOnExit(clear)
    }
    
    // Audio Device Settings Methods
    fun setAutoConnectDevice(enable: Boolean) {
        appSettings.setAutoConnectDevice(enable)
    }

    // Enhanced play tracking with user preferences
    private fun trackSongPlay(song: Song) {
        viewModelScope.launch {
            // Update songs played count
            val newCount = _songsPlayed.value + 1
            _songsPlayed.value = newCount
            appSettings.setSongsPlayed(newCount)
            
            // Update unique artists
            val currentArtists = _uniqueArtists.value
            if (!_recentlyPlayed.value.any { it.artist == song.artist }) {
                _uniqueArtists.value = currentArtists + 1
                appSettings.setUniqueArtists(currentArtists + 1)
            }
            
            // Update genre preferences
            val currentGenrePrefs = _genrePreferences.value.toMutableMap()
            song.genre?.let { genre ->
                val count = currentGenrePrefs.getOrDefault(genre, 0) + 1
                currentGenrePrefs[genre] = count
                appSettings.setGenrePreferences(currentGenrePrefs)
            }

            // Update song play counts
            val currentSongPlayCounts = _songPlayCounts.value.toMutableMap()
            val playCount = currentSongPlayCounts.getOrDefault(song.id, 0) + 1
            currentSongPlayCounts[song.id] = playCount
            _songPlayCounts.value = currentSongPlayCounts
            appSettings.setSongPlayCounts(currentSongPlayCounts)
            
            // Update time-based preferences
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val currentTimePrefs = _timeBasedPreferences.value.toMutableMap()
            val timeSlot = (hour / 3) * 3 // Group into 3-hour slots
            val songs = currentTimePrefs.getOrDefault(timeSlot, emptyList()).toMutableList()
            if (songs.size >= 20) songs.removeAt(0) // Keep last 20 songs
            songs.add(song.id)
            currentTimePrefs[timeSlot] = songs
            _timeBasedPreferences.value = currentTimePrefs
            appSettings.setTimeBasedPreferences(currentTimePrefs)
        }
    }

    // Enhanced recommendation algorithms
    fun getRecommendedSongs(): List<Song> {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeSlot = (currentHour / 3) * 3
        
        // Get songs frequently played in this time slot
        val timeBasedSongs = _timeBasedPreferences.value[timeSlot]?.mapNotNull { id ->
            _songs.value.find { it.id == id }
        } ?: emptyList()
        
        // Get songs from preferred genres
        val preferredGenres = _genrePreferences.value.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        
        val genreBasedSongs = _songs.value.filter { song ->
            song.genre in preferredGenres
        }
        
        // Filter out blacklisted songs
        val blacklistedIds = appSettings.blacklistedSongs.value
        
        // Combine and shuffle recommendations
        return (timeBasedSongs + genreBasedSongs)
            .distinct()
            .filter { !blacklistedIds.contains(it.id) } // Exclude blacklisted songs
            .shuffled()
            .take(10)
    }

    // Enhanced mood-based playlists
    fun getMoodBasedPlaylists(): List<Song> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val mood = when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        
        val moodPrefs = appSettings.moodPreferences.value
        val songIds = moodPrefs[mood] ?: emptyList()
        
        return songIds.mapNotNull { id ->
            _songs.value.find { it.id == id }
        }
    }

    // Enhanced recommendation methods
    fun getPersonalizedRecommendations(): List<Song> {
        val recentArtists = _recentlyPlayed.value
            .map { it.artist }
            .distinct()
            .take(3)
        
        return _songs.value
            .filter { song -> 
                recentArtists.contains(song.artist) && 
                !_recentlyPlayed.value.contains(song)
            }
            .shuffled()
            .take(10)
    }

    fun getListeningStats(): String {
        val listeningTime = appSettings.listeningTime.value
        val hours = listeningTime / (1000 * 60 * 60)
        return if (hours < 1) "< 1h" else "${hours}h"
    }

    // Initialize from persistence
    private suspend fun initializeFromPersistence() {
        Log.d(TAG, "Initializing from persistence. Songs loaded: ${_songs.value.size}")
        try {
            // Restore recently played
            val recentIds = appSettings.recentlyPlayed.value
            val recentSongs = recentIds.mapNotNull { id ->
                _songs.value.find { it.id == id }
            }
            _recentlyPlayed.value = recentSongs
            
            // Clean up old daily stats (keep only last 30 days)
            val thirtyDaysAgo = java.time.LocalDate.now().minusDays(30)
            val cleanedDailyStats = appSettings.dailyListeningStats.value.filterKeys { date ->
                try {
                    java.time.LocalDate.parse(date).isAfter(thirtyDaysAgo)
                } catch (e: Exception) {
                    // Handle potential parsing errors for old data
                    false
                }
            }
            appSettings.updateDailyListeningStats(cleanedDailyStats)
            
            // Clean up weekly top artists (reset every week)
            val lastPlayed = appSettings.lastPlayedTimestamp.value
            if (System.currentTimeMillis() - lastPlayed > 7 * 24 * 60 * 60 * 1000) {
                appSettings.updateWeeklyTopArtists(emptyMap())
            }

            // Initialize mood-based preferences if empty
            if (appSettings.moodPreferences.value.isEmpty()) {
                val initialMoodPrefs = mapOf(
                    "morning" to emptyList<String>(),
                    "afternoon" to emptyList(),
                    "evening" to emptyList(),
                    "night" to emptyList()
                )
                appSettings.updateMoodPreferences(initialMoodPrefs)
            }

            // Log initialization status
            Log.d(TAG, "Initialized from persistence: ${recentSongs.size} recent songs loaded")
            Log.d(TAG, "Daily stats entries: ${cleanedDailyStats.size}")
            Log.d(TAG, "Weekly top artists: ${appSettings.weeklyTopArtists.value.size}")

            // Restore song play counts
            _songPlayCounts.value = appSettings.songPlayCounts.value
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing from persistence", e)
        }
    }

    /**
     * Get current queue info for debugging
     */
    fun getQueueInfo(): String {
        val queue = _currentQueue.value
        return "Queue: ${queue.songs.size} songs, current index: ${queue.currentIndex}, current song: ${_currentSong.value?.title}"
    }

    /**
     * Force sync the queue state with MediaController
     * This is useful for debugging queue inconsistencies
     */
    fun syncQueueWithMediaController() {
        mediaController?.let { controller ->
            Log.d(TAG, "Syncing queue with MediaController")
            Log.d(TAG, "MediaController: ${controller.mediaItemCount} items, current index: ${controller.currentMediaItemIndex}")
            Log.d(TAG, "ViewModel queue: ${_currentQueue.value.songs.size} songs, current index: ${_currentQueue.value.currentIndex}")
            
            if (controller.mediaItemCount > 0) {
                val mediaItems = (0 until controller.mediaItemCount).map { index ->
                    controller.getMediaItemAt(index)
                }
                val mediaItemSongs = mediaItems.mapNotNull { mediaItem ->
                    _songs.value.find { it.id == mediaItem.mediaId }
                }
                
                val currentMediaIndex = controller.currentMediaItemIndex.coerceAtLeast(0)
                _currentQueue.value = Queue(mediaItemSongs, currentMediaIndex)
                
                // Update current song if needed
                if (mediaItemSongs.isNotEmpty() && currentMediaIndex < mediaItemSongs.size) {
                    val currentSong = mediaItemSongs[currentMediaIndex]
                    _currentSong.value = currentSong
                    _isFavorite.value = _favoriteSongs.value.contains(currentSong.id)
                }
                
                Log.d(TAG, "Synced queue: ${mediaItemSongs.size} songs, index: $currentMediaIndex")
            } else {
                // No items in MediaController, clear queue
                _currentQueue.value = Queue(emptyList(), -1)
                _currentSong.value = null
                Log.d(TAG, "Cleared queue - no items in MediaController")
            }
        } ?: run {
            Log.w(TAG, "Cannot sync queue - MediaController is null")
        }
    }

    /**
     * Debug function to print current queue state
     */
    fun debugQueueState() {
        Log.d(TAG, "=== QUEUE DEBUG INFO ===")
        Log.d(TAG, "ViewModel queue: ${_currentQueue.value.songs.size} songs, current index: ${_currentQueue.value.currentIndex}")
        if (_currentQueue.value.songs.isNotEmpty()) {
            Log.d(TAG, "Queue songs:")
            _currentQueue.value.songs.forEachIndexed { index, song ->
                val marker = if (index == _currentQueue.value.currentIndex) " -> " else "    "
                Log.d(TAG, "$marker$index: ${song.title} by ${song.artist}")
            }
        }
        
        mediaController?.let { controller ->
            Log.d(TAG, "MediaController: ${controller.mediaItemCount} items, current index: ${controller.currentMediaItemIndex}")
            Log.d(TAG, "MediaController state: ${controller.playbackState}, isPlaying: ${controller.isPlaying}")
            if (controller.mediaItemCount > 0) {
                Log.d(TAG, "MediaController songs:")
                for (i in 0 until controller.mediaItemCount) {
                    val mediaItem = controller.getMediaItemAt(i)
                    val marker = if (i == controller.currentMediaItemIndex) " -> " else "    "
                    Log.d(TAG, "$marker$i: ${mediaItem.mediaId} (${mediaItem.mediaMetadata.title})")
                }
            }
        } ?: run {
            Log.d(TAG, "MediaController: null")
        }
        
        Log.d(TAG, "Current song: ${_currentSong.value?.title ?: "none"}")
        Log.d(TAG, "Is playing: ${_isPlaying.value}")
        Log.d(TAG, "========================")
    }

    companion object {
        // SharedPreferences keys
        private const val PREF_HIGH_QUALITY_AUDIO = "high_quality_audio"
        private const val PREF_GAPLESS_PLAYBACK = "gapless_playback"
        private const val PREF_CROSSFADE = "crossfade"
        private const val PREF_CROSSFADE_DURATION = "crossfade_duration"
        private const val PREF_AUDIO_NORMALIZATION = "audio_normalization"
        private const val PREF_REPLAY_GAIN = "replay_gain"
        private const val PREF_SHOW_LYRICS = "show_lyrics"
        private const val PREF_ONLINE_ONLY_LYRICS = "online_only_lyrics"
        private const val PREF_SONG_PLAY_COUNTS = "song_play_counts"
        
        // Player control constants
        private const val REWIND_THRESHOLD_MS = 3000L // 3 seconds
    }

    fun playAlbumShuffled(album: Album) {
        viewModelScope.launch {
            Log.d(TAG, "Playing shuffled album: ${album.title} (ID: ${album.id})")
            val songs = repository.getSongsForAlbum(album.id)
            Log.d(TAG, "Found ${songs.size} songs for album")
            if (songs.isNotEmpty()) {
                // Shuffle the songs and play them as a queue
                val shuffledSongs = songs.shuffled()
                playQueue(shuffledSongs)
                
                // Enable shuffle mode after starting playback
                mediaController?.shuffleModeEnabled = true
                _isShuffleEnabled.value = true
                
                Log.d(TAG, "Started shuffled playback of album: ${album.title}")
            } else {
                Log.e(TAG, "No songs found for album: ${album.title} (ID: ${album.id})")
                debugQueueState()
            }
        }
    }

    fun playPlaylistShuffled(playlist: Playlist) {
        Log.d(TAG, "Playing shuffled playlist: ${playlist.name}")
        if (playlist.songs.isNotEmpty()) {
            // Shuffle the songs and play them as a queue
            val shuffledSongs = playlist.songs.shuffled()
            playQueue(shuffledSongs)
            
            // Enable shuffle mode after starting playback
            mediaController?.shuffleModeEnabled = true
            _isShuffleEnabled.value = true
            
            Log.d(TAG, "Started shuffled playback of playlist: ${playlist.name}")
        } else {
            Log.e(TAG, "No songs found in playlist: ${playlist.name}")
        }
    }
    
    /**
     * Helper function to get file path from URI
     */
    private fun getPathFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    // For content URIs, query the MediaStore to get the file path
                    val projection = arrayOf(MediaStore.Audio.Media.DATA)
                    getApplication<Application>().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                            cursor.getString(dataIndex)
                        } else null
                    }
                }
                "file" -> {
                    // For file URIs, get the path directly
                    uri.path
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting path from URI: $uri", e)
            null
        }
    }
    
    /**
     * Cached version of getPathFromUri to avoid repeated ContentResolver queries
     */
    private fun getPathFromUriCached(uri: Uri): String? {
        val uriString = uri.toString()
        return pathCache.getOrPut(uriString) {
            getPathFromUri(uri)
        }
    }
    
    /**
     * Exposes the MusicRepository instance for cache cleanup operations
     * @return The MusicRepository instance
     */
    fun getMusicRepository(): MusicRepository {
        return repository
    }
    
    // Clean sleep timer functionality
    fun startSleepTimer(minutes: Int, action: SleepAction) {
        // Stop any existing timer first
        stopSleepTimer()
        
        val totalSeconds = minutes * 60L
        _sleepTimerActive.value = true
        _sleepTimerRemainingSeconds.value = totalSeconds
        _sleepTimerAction.value = action.name
        
        Log.d(TAG, "Starting sleep timer: ${minutes} minutes, action: ${action.name}")
        
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            
            while (remaining > 0 && _sleepTimerActive.value) {
                delay(1000) // Wait 1 second
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
            }
            
            // Timer finished, execute action
            if (_sleepTimerActive.value && remaining <= 0) {
                Log.d(TAG, "Sleep timer finished, executing action: ${action.name}")
                
                when (action) {
                    SleepAction.FADE_OUT -> fadeOutAndPause()
                    SleepAction.PAUSE -> pauseMusic()
                    SleepAction.STOP -> stopMusic()
                }
                
                // Reset timer state
                _sleepTimerActive.value = false
                _sleepTimerRemainingSeconds.value = 0L
            }
        }
    }
    
    fun stopSleepTimer() {
        Log.d(TAG, "Stopping sleep timer")
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerActive.value = false
        _sleepTimerRemainingSeconds.value = 0L
    }
    
    // Equalizer functionality
    fun setEqualizerEnabled(enabled: Boolean) {
        val context = getApplication<Application>()
        
        // Save to settings
        appSettings.setEqualizerEnabled(enabled)
        
        // Send to service
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_SET_EQUALIZER_ENABLED
            putExtra("enabled", enabled)
        }
        context.startService(intent)
        Log.d(TAG, "Set equalizer enabled: $enabled")
    }
    
    fun setEqualizerBandLevel(band: Short, level: Short) {
        val context = getApplication<Application>()
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_SET_EQUALIZER_BAND
            putExtra("band", band)
            putExtra("level", level)
        }
        context.startService(intent)
        Log.d(TAG, "Set equalizer band $band to level $level")
    }
    
    fun setBassBoost(enabled: Boolean, strength: Short = 500) {
        val context = getApplication<Application>()
        
        // Save to settings
        appSettings.setBassBoostEnabled(enabled)
        appSettings.setBassBoostStrength(strength.toInt())
        
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_SET_BASS_BOOST
            putExtra("enabled", enabled)
            putExtra("strength", strength)
        }
        context.startService(intent)
        Log.d(TAG, "Set bass boost enabled: $enabled, strength: $strength")
    }
    
    fun setVirtualizer(enabled: Boolean, strength: Short = 500) {
        val context = getApplication<Application>()
        
        // Save to settings
        appSettings.setVirtualizerEnabled(enabled)
        appSettings.setVirtualizerStrength(strength.toInt())
        
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_SET_VIRTUALIZER
            putExtra("enabled", enabled)
            putExtra("strength", strength)
        }
        context.startService(intent)
        Log.d(TAG, "Set virtualizer enabled: $enabled, strength: $strength")
    }
    
    fun applyEqualizerPreset(preset: String, levels: List<Float>) {
        val context = getApplication<Application>()
        
        // Save to settings
        appSettings.setEqualizerPreset(preset)
        appSettings.setEqualizerBandLevels(levels.joinToString(","))
        
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_APPLY_EQUALIZER_PRESET
            putExtra("preset", preset)
            putExtra("levels", levels.toFloatArray())
        }
        context.startService(intent)
        Log.d(TAG, "Applied equalizer preset: $preset")
    }
    
    // Clean sleep timer implementation
    private val _sleepTimerActive = MutableStateFlow(false)
    val sleepTimerActive: StateFlow<Boolean> = _sleepTimerActive.asStateFlow()
    
    private val _sleepTimerRemainingSeconds = MutableStateFlow(0L)
    val sleepTimerRemainingSeconds: StateFlow<Long> = _sleepTimerRemainingSeconds.asStateFlow()
    
    private val _sleepTimerAction = MutableStateFlow("FADE_OUT")
    val sleepTimerAction: StateFlow<String> = _sleepTimerAction.asStateFlow()
    
    private var sleepTimerJob: kotlinx.coroutines.Job? = null
    
    // Sleep timer now uses direct ViewModel state management instead of broadcast receivers
    
}
