package chromahub.rhythm.app.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
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
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MusicViewModel"
    private val repository = MusicRepository(application)
    
    // Audio device manager
    private val audioDeviceManager = AudioDeviceManager(application)
    
    // Settings manager
    private val appSettings = AppSettings.getInstance(application)
    
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
    
    // Search history
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    // Lyrics
    private val _currentLyrics = MutableStateFlow<String?>(null)
    val currentLyrics: StateFlow<String?> = _currentLyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    // New helper methods
    private val _serviceConnected = MutableStateFlow(false)
    val serviceConnected: StateFlow<Boolean> = _serviceConnected.asStateFlow()

    // Main music data
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

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

    private val _currentQueue = MutableStateFlow(Queue(emptyList()))
    val currentQueue: StateFlow<Queue> = _currentQueue.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

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

    // Sort library functionality
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
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

    // Add initialization state tracking
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    enum class SortOrder {
        TITLE_ASC,
        TITLE_DESC,
        ARTIST_ASC,
        ARTIST_DESC
    }

    init {
        Log.d(TAG, "Initializing MusicViewModel")
        loadMusic()
        initializeController()
        // Start progress updates
        startProgressUpdates()
        // Load favorite songs
        loadFavorites()
        // Load search history
        loadSearchHistory()
        // Load all settings
        loadSettings()
        
        // Refresh devices periodically
        startDeviceMonitoring()
        
        // Start tracking session
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    // Update listening time every minute
                    delay(60000) // 1 minute
                    val newTime = _listeningTime.value + 60000
                    _listeningTime.value = newTime
                    appSettings.setListeningTime(newTime)
                } else {
                    delay(1000) // Check every second when not playing
                }
            }
        }
        initializeFromPersistence()
    }

    private fun loadMusic() {
        viewModelScope.launch {
            Log.d(TAG, "Loading music data")
            _songs.value = repository.loadSongs()
            _albums.value = repository.loadAlbums()
            _artists.value = repository.loadArtists()
            
            // Load saved playlists
            loadSavedPlaylists()
            
            Log.d(TAG, "Loaded ${_songs.value.size} songs")
            
            // Fetch artist images and album artwork from internet
            fetchArtworkFromInternet()
        }
    }
    
    private fun loadSavedPlaylists() {
        try {
            // Load playlists
            val playlistsJson = appSettings.playlists.value
            val playlists = if (playlistsJson != null) {
                val type = object : TypeToken<List<Playlist>>() {}.type
                Gson().fromJson<List<Playlist>>(playlistsJson, type)
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
                _favoriteSongs.value = Gson().fromJson(favoriteSongsJson, type)
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
            val playlistsJson = Gson().toJson(_playlists.value)
            appSettings.setPlaylists(playlistsJson)
            Log.d(TAG, "Saved ${_playlists.value.size} playlists")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving playlists", e)
        }
    }

    private fun saveFavoriteSongs() {
        try {
            val favoriteSongsJson = Gson().toJson(_favoriteSongs.value)
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
                // Only fetch for a subset of artists to avoid overwhelming the API
                val artistsToUpdate = _artists.value.filter { it.artworkUri == null }.take(10)
                Log.d(TAG, "Found ${artistsToUpdate.size} artists without images out of ${_artists.value.size} total artists")
                if (artistsToUpdate.isNotEmpty()) {
                    val updatedArtists = repository.fetchArtistImages(artistsToUpdate)
                    // Update only the artists we fetched, keeping the rest unchanged
                    val artistMap = updatedArtists.associateBy { it.id }
                    _artists.value = _artists.value.map { 
                        artistMap[it.id] ?: it 
                    }
                    Log.d(TAG, "Updated ${updatedArtists.size} artists with images from internet")
                }
                
                // Add a delay between API calls to avoid rate limiting
                delay(1000)
                
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

    private fun loadFavorites() {
        viewModelScope.launch {
            // In a real app, this would load from a database or preferences
            // For now, we'll use an empty set
            _favoriteSongs.value = emptySet()
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
        context.startService(serviceIntent)
        
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
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
        }, MoreExecutors.directExecutor())
    }
    
    // Private initialization method (called from init)
    private fun initializeController() {
        connectToMediaService()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "Playback state changed: $playbackState")
            
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
                    
                    // Update queue position
                    val currentQueueSongs = _currentQueue.value.songs
                    val songIndexInQueue = currentQueueSongs.indexOfFirst { it.id == songId }
                    
                    if (songIndexInQueue != -1) {
                        _currentQueue.value = _currentQueue.value.copy(currentIndex = songIndexInQueue)
                        Log.d(TAG, "Updated queue position to $songIndexInQueue")
                    }
                    
                    // Fetch lyrics for the new song
                    fetchLyricsForCurrentSong()
                    
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
                    val songIndexInQueue = _currentQueue.value.songs.indexOfFirst { queueSong -> queueSong.id == song.id }
                    if (songIndexInQueue != -1) {
                        // Update the queue position if the song is in the queue
                        _currentQueue.value = _currentQueue.value.copy(currentIndex = songIndexInQueue)
                    } else {
                        // If the song is not in the queue, create a new queue with just this song
                        // This can happen if the song was played directly without using playQueue
                        _currentQueue.value = Queue(listOf(song), 0)
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
                }
            }
        }
    }

    fun playSong(song: Song) {
        Log.d(TAG, "Playing song: ${song.title}")
        
        // Add to recently played list
        updateRecentlyPlayed(song)
        
        // Track song play statistics
        trackSongPlay(song)
        
        // Check if the song is in the current queue
        val currentQueueSongs = _currentQueue.value.songs
        val songIndexInQueue = currentQueueSongs.indexOfFirst { it.id == song.id }
        
        if (songIndexInQueue != -1) {
            // If the song is in the queue, play it from that position
            Log.d(TAG, "Song found in queue at position $songIndexInQueue, playing from queue")
            mediaController?.let { controller ->
                controller.seekToDefaultPosition(songIndexInQueue)
                controller.prepare()
                controller.play()
                _currentSong.value = song
                _isPlaying.value = true
                // Update the current queue with the new position
                _currentQueue.value = _currentQueue.value.copy(currentIndex = songIndexInQueue)
                // Update favorite status
                _isFavorite.value = _favoriteSongs.value.contains(song.id)
                startProgressUpdates()
            }
        } else {
            // If the song is not in the queue, create a new queue with just this song
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
            
            mediaController?.let { controller ->
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
                _currentSong.value = song
                _isPlaying.value = true
                // Create a new queue with just this song
                _currentQueue.value = Queue(listOf(song), 0)
                // Update favorite status
                _isFavorite.value = _favoriteSongs.value.contains(song.id)
                startProgressUpdates()
            }
        }
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
            }
        }
    }

    fun playArtist(artist: Artist) {
        viewModelScope.launch {
            Log.d(TAG, "Playing artist: ${artist.name}")
            val songs = repository.getSongsForArtist(artist.id)
            if (songs.isNotEmpty()) {
                playQueue(songs)
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
        
        if (songs.isEmpty()) {
            Log.e(TAG, "Cannot play empty queue")
            return
        }
        
        mediaController?.let { controller ->
            try {
                // Clear existing queue
                controller.clearMediaItems()
                
                // Create media items from songs
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
                
                // Set the queue in the view model
                _currentQueue.value = Queue(songs, 0)
                
                // Start playback from the first song
                controller.seekToDefaultPosition(0)
                controller.prepare()
                controller.play()
                
                // Update current song
                _currentSong.value = songs.firstOrNull()
                _isPlaying.value = true
                
                // Add first song to recently played
                _currentSong.value?.let { updateRecentlyPlayed(it) }
                
                // Update favorite status
                _isFavorite.value = _currentSong.value?.let { song -> 
                    _favoriteSongs.value.contains(song.id) 
                } ?: false
                
                startProgressUpdates()
                
                // Add a listener to update the queue position when the media item changes
                controller.removeListener(mediaItemTransitionListener)
                controller.addListener(mediaItemTransitionListener)
                
                Log.d(TAG, "Successfully started playback of queue with ${songs.size} songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing queue", e)
            }
        } ?: run {
            Log.e(TAG, "Cannot play queue - media controller is null")
            // Try to reconnect to the media service if controller is null
            connectToMediaService()
            
            // Store the songs to play once we have a controller
            pendingQueueToPlay = songs
        }
    }

    // Store a reference to the listener to avoid adding multiple listeners
    private val mediaItemTransitionListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            
            // Update the queue position based on the current media item
            mediaItem?.let { item ->
                val songId = item.mediaId
                val newIndex = _currentQueue.value.songs.indexOfFirst { it.id == songId }
                if (newIndex != -1) {
                    _currentQueue.value = _currentQueue.value.copy(currentIndex = newIndex)
                    
                    // Also update the current song
                    val song = _currentQueue.value.songs[newIndex]
                    _currentSong.value = song
                    
                    // Add to recently played
                    updateRecentlyPlayed(song)
                }
            }
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
                
                // Update the current queue position
                val newIndex = (_currentQueue.value.currentIndex + 1) % _currentQueue.value.songs.size
                _currentQueue.value = _currentQueue.value.copy(currentIndex = newIndex)
                
                // Reset progress to 0 for immediate UI feedback
                _progress.value = 0f
                
                // Now perform the actual seek operation
                controller.seekToNext()
            }
        }
    }

    fun skipToPrevious() {
        Log.d(TAG, "Skip to previous")
        mediaController?.let { controller ->
            // Check if there are previous songs in the queue
            if (controller.hasPreviousMediaItem()) {
                // Get the previous song before seeking to update UI immediately
                val prevIndex = if (controller.currentMediaItemIndex > 0) 
                    controller.currentMediaItemIndex - 1 
                else 
                    controller.mediaItemCount - 1
                
                val prevMediaItem = controller.getMediaItemAt(prevIndex)
                val prevSongId = prevMediaItem.mediaId
                val prevSong = _songs.value.find { it.id == prevSongId }
                
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
                
                // Update the current queue position
                val newIndex = if (_currentQueue.value.currentIndex > 0) 
                    _currentQueue.value.currentIndex - 1 
                else 
                    _currentQueue.value.songs.size - 1
                
                _currentQueue.value = _currentQueue.value.copy(currentIndex = newIndex)
                
                // Reset progress to 0 for immediate UI feedback
                _progress.value = 0f
                
                // Now perform the actual seek operation
                controller.seekToPrevious()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        Log.d(TAG, "Seek to position: $positionMs ms")
        mediaController?.seekTo(positionMs)
        updateProgress() // Immediately update progress after seeking
    }

    fun seekTo(progress: Float) {
        mediaController?.let { controller ->
            val positionMs = (progress * controller.duration).toLong()
            Log.d(TAG, "Seek to progress: $progress (${positionMs}ms)")
            controller.seekTo(positionMs)
            updateProgress() // Immediately update progress after seeking
        }
    }

    fun skipBackward() {
        mediaController?.let { controller ->
            val newPosition = (controller.currentPosition - 30_000).coerceAtLeast(0)
            Log.d(TAG, "Skip backward 30s to ${newPosition}ms")
            controller.seekTo(newPosition)
            updateProgress() // Immediately update progress after seeking
        }
    }

    fun skipForward() {
        mediaController?.let { controller ->
            val newPosition = (controller.currentPosition + 30_000).coerceAtMost(controller.duration)
            Log.d(TAG, "Skip forward 30s to ${newPosition}ms")
            controller.seekTo(newPosition)
            updateProgress() // Immediately update progress after seeking
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
            val songId = song.id
            val currentFavorites = _favoriteSongs.value.toMutableSet()
            
            if (currentFavorites.contains(songId)) {
                Log.d(TAG, "Removing song from favorites: ${song.title}")
                currentFavorites.remove(songId)
                _isFavorite.value = false
                
                // Remove from Favorites playlist
                _playlists.value = _playlists.value.map { playlist ->
                    if (playlist.id == "1" && playlist.name == "Favorites") {
                        playlist.copy(songs = playlist.songs.filter { it.id != song.id })
                    } else {
                        playlist
                    }
                }
            } else {
                Log.d(TAG, "Adding song to favorites: ${song.title}")
                currentFavorites.add(songId)
                _isFavorite.value = true
                
                // Add to Favorites playlist
                _playlists.value = _playlists.value.map { playlist ->
                    if (playlist.id == "1" && playlist.name == "Favorites") {
                        playlist.copy(songs = playlist.songs + song)
                    } else {
                        playlist
                    }
                }
            }
            
            _favoriteSongs.value = currentFavorites
            saveFavoriteSongs()
            savePlaylists()
        }
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
        Log.d(TAG, "ViewModel being cleared")
        progressUpdateJob?.cancel()
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        audioDeviceManager.cleanup()
        super.onCleared()
    }

    // New functions for playlist management
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val newPlaylist = repository.createPlaylist(name)
            _playlists.value = _playlists.value + newPlaylist
            Log.d(TAG, "Created new playlist: ${newPlaylist.name}")
            savePlaylists()
        }
    }

    fun addSongToPlaylist(song: Song, playlistId: String) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                // Check if song is already in the playlist
                if (playlist.songs.any { it.id == song.id }) {
                    playlist
                } else {
                    val updatedSongs = playlist.songs + song
                    playlist.copy(
                        songs = updatedSongs,
                        dateModified = System.currentTimeMillis()
                    )
                }
            } else {
                playlist
            }
        }
        Log.d(TAG, "Added song to playlist: ${song.title}")
        savePlaylists()
    }

    fun removeSongFromPlaylist(song: Song, playlistId: String) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(
                    songs = playlist.songs.filter { it.id != song.id },
                    dateModified = System.currentTimeMillis()
                )
            } else {
                playlist
            }
        }
        Log.d(TAG, "Removed song from playlist: ${song.title}")
        savePlaylists()
    }

    fun deletePlaylist(playlistId: String) {
        // Prevent deleting default playlists
        if (playlistId == "1" || playlistId == "2" || playlistId == "3") {
            Log.d(TAG, "Cannot delete default playlist: $playlistId")
            return
        }
        
        _playlists.value = _playlists.value.filter { it.id != playlistId }
        Log.d(TAG, "Deleted playlist: $playlistId")
        savePlaylists()
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
    
    // Sort library functionality
    fun sortLibrary() {
        viewModelScope.launch {
            // Cycle through sort orders
            _sortOrder.value = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> SortOrder.TITLE_DESC
                SortOrder.TITLE_DESC -> SortOrder.ARTIST_ASC
                SortOrder.ARTIST_ASC -> SortOrder.ARTIST_DESC
                SortOrder.ARTIST_DESC -> SortOrder.TITLE_ASC
            }
            
            // Sort songs based on current sort order
            _songs.value = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> _songs.value.sortedBy { it.title }
                SortOrder.TITLE_DESC -> _songs.value.sortedByDescending { it.title }
                SortOrder.ARTIST_ASC -> _songs.value.sortedBy { it.artist }
                SortOrder.ARTIST_DESC -> _songs.value.sortedByDescending { it.artist }
            }
            
            // Sort albums based on current sort order
            _albums.value = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> _albums.value.sortedBy { it.title }
                SortOrder.TITLE_DESC -> _albums.value.sortedByDescending { it.title }
                SortOrder.ARTIST_ASC -> _albums.value.sortedBy { it.artist }
                SortOrder.ARTIST_DESC -> _albums.value.sortedByDescending { it.artist }
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
                    if (_sortOrder.value == SortOrder.TITLE_ASC || _sortOrder.value == SortOrder.ARTIST_ASC) {
                        it.name
                    } else {
                        it.name.reversed()
                    }
                }
            )
            
            Log.d(TAG, "Library sorted: ${_sortOrder.value}")
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
     * Fetches lyrics for the current song if settings allow
     */
    private fun fetchLyricsForCurrentSong() {
        val song = currentSong.value ?: return
        
        // Clear current lyrics first
        _currentLyrics.value = null
        
        // Check if lyrics are enabled
        if (!showLyrics.value) {
            return
        }
        
        // Check if we should only fetch lyrics when online
        if (showOnlineOnlyLyrics.value && !repository.isNetworkAvailable()) {
            Log.d(TAG, "Online-only lyrics enabled but device is offline")
            return
        }
        
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            try {
                val lyrics = repository.fetchLyrics(song.artist, song.title)
                _currentLyrics.value = lyrics
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching lyrics", e)
                _currentLyrics.value = null
            } finally {
                _isLoadingLyrics.value = false
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
            context.startService(intent)
        }
    }

    /**
     * Add a song to the queue
     */
    fun addSongToQueue(song: Song) {
        Log.d(TAG, "Adding song to queue: ${song.title}")
        mediaController?.let { controller ->
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
            _currentQueue.value = Queue(currentQueueSongs, _currentQueue.value.currentIndex)
            
            Log.d(TAG, "Updated queue: now has ${currentQueueSongs.size} songs")
        } ?: run {
            Log.e(TAG, "Cannot add song to queue - media controller is null")
        }
    }

    /**
     * Remove a song from the queue
     */
    fun removeFromQueue(song: Song) {
        Log.d(TAG, "Removing song from queue: ${song.title}")
        val currentQueue = _currentQueue.value
        val currentSong = _currentSong.value
        val songIndex = currentQueue.songs.indexOfFirst { it.id == song.id }
        
        // Check if the song is in the queue
        if (songIndex == -1) {
            Log.d(TAG, "Song not found in queue")
            return
        }
        
        // Don't remove the currently playing song
        if (currentSong != null && song.id == currentSong.id) {
            Log.d(TAG, "Cannot remove currently playing song")
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
                    Log.e(TAG, "Media item index out of bounds: $songIndex, controller has ${controller.mediaItemCount} items")
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
                
                Log.d(TAG, "Updated queue: ${updatedSongs.size} songs, current index: $newIndex")
                _currentQueue.value = Queue(updatedSongs, newIndex)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing song from queue", e)
            }
        } ?: run {
            Log.e(TAG, "Cannot remove song - media controller is null")
        }
    }
    
    /**
     * Move a song in the queue from one position to another
     */
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        Log.d(TAG, "Moving queue item from $fromIndex to $toIndex")
        val currentQueue = _currentQueue.value
        val songs = currentQueue.songs.toMutableList()
        
        // Input validation to prevent index out of bounds
        if (fromIndex < 0 || fromIndex >= songs.size || toIndex < 0 || toIndex >= songs.size) {
            Log.e(TAG, "Cannot move queue item - invalid indices: from=$fromIndex, to=$toIndex, size=${songs.size}")
            return
        }
        
        // Safely update local state first
        try {
            // Apply the move in the local queue
            val song = songs.removeAt(fromIndex)
            songs.add(toIndex, song)
            
            // Also move it in the MediaController
            mediaController?.let { controller ->
                try {
                    if (fromIndex < controller.mediaItemCount && toIndex < controller.mediaItemCount) {
                        Log.d(TAG, "Moving media item in controller from $fromIndex to $toIndex")
                        controller.moveMediaItem(fromIndex, toIndex)
                    } else {
                        Log.e(TAG, "Cannot move media item - index out of bounds in controller: from=$fromIndex, to=$toIndex, count=${controller.mediaItemCount}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error moving media item in controller", e)
                }
            } ?: run {
                Log.e(TAG, "Cannot move queue item - media controller is null")
            }
            
            // Adjust current index if needed
            val newIndex = when {
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
            }
            
            Log.d(TAG, "Updated queue position: $newIndex")
            _currentQueue.value = Queue(songs, newIndex)
        } catch (e: Exception) {
            Log.e(TAG, "Error moving queue item", e)
        }
    }
    
    /**
     * Add songs to the queue
     */
    fun addSongsToQueue() {
        // This would typically show a dialog or navigate to a screen to select songs
        // For now, it's a placeholder for the UI to connect to
    }

    // Search history methods
    private fun loadSearchHistory() {
        val searchHistoryJson = appSettings.searchHistory.value
        if (searchHistoryJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val history = Gson().fromJson<List<String>>(searchHistoryJson, type)
                _searchHistory.value = history
            } catch (e: Exception) {
                Log.e(TAG, "Error loading search history", e)
                _searchHistory.value = emptyList()
            }
        }
    }
    
    private fun saveSearchHistory() {
        val searchHistoryJson = Gson().toJson(_searchHistory.value)
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
            
            // Create a delayed job to retry playback once connected
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
                _genrePreferences.value = currentGenrePrefs
                appSettings.setGenrePreferences(currentGenrePrefs)
            }
            
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
        
        // Combine and shuffle recommendations
        return (timeBasedSongs + genreBasedSongs)
            .distinct()
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
    private fun initializeFromPersistence() {
        viewModelScope.launch {
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
                    java.time.LocalDate.parse(date).isAfter(thirtyDaysAgo)
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
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing from persistence", e)
            }
        }
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
    }
} 