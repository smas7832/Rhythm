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

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MusicViewModel"
    private val repository = MusicRepository(application)
    
    // Audio device manager
    private val audioDeviceManager = AudioDeviceManager(application)
    
    // SharedPreferences for storing settings
    private val sharedPreferences = application.getSharedPreferences("rhythm_preferences", Context.MODE_PRIVATE)

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
    val currentLocation = audioDeviceManager.currentDevice

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

    // Settings
    private val _showLyrics = MutableStateFlow(true)
    val showLyrics: StateFlow<Boolean> = _showLyrics.asStateFlow()
    
    private val _showOnlineOnlyLyrics = MutableStateFlow(false)
    val showOnlineOnlyLyrics: StateFlow<Boolean> = _showOnlineOnlyLyrics.asStateFlow()
    
    // Playback settings
    private val _enableHighQualityAudio = MutableStateFlow(true)
    val enableHighQualityAudio: StateFlow<Boolean> = _enableHighQualityAudio.asStateFlow()
    
    private val _enableGaplessPlayback = MutableStateFlow(true)
    val enableGaplessPlayback: StateFlow<Boolean> = _enableGaplessPlayback.asStateFlow()
    
    private val _enableCrossfade = MutableStateFlow(false)
    val enableCrossfade: StateFlow<Boolean> = _enableCrossfade.asStateFlow()
    
    private val _crossfadeDuration = MutableStateFlow(2f)
    val crossfadeDuration: StateFlow<Float> = _crossfadeDuration.asStateFlow()
    
    private val _enableAudioNormalization = MutableStateFlow(true)
    val enableAudioNormalization: StateFlow<Boolean> = _enableAudioNormalization.asStateFlow()
    
    private val _enableReplayGain = MutableStateFlow(false)
    val enableReplayGain: StateFlow<Boolean> = _enableReplayGain.asStateFlow()
    
    // Lyrics
    private val _currentLyrics = MutableStateFlow<String?>(null)
    val currentLyrics: StateFlow<String?> = _currentLyrics.asStateFlow()
    
    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    enum class SortOrder {
        TITLE_ASC,
        TITLE_DESC,
        ARTIST_ASC,
        ARTIST_DESC
    }

    init {
        Log.d(TAG, "Initializing MusicViewModel")
        loadSettings() // Load settings first
        loadMusic()
        initializeController()
        // Start progress updates
        startProgressUpdates()
        // Load favorite songs
        loadFavorites()
        
        // Refresh devices periodically
        startDeviceMonitoring()
    }

    private fun loadMusic() {
        viewModelScope.launch {
            Log.d(TAG, "Loading music data")
            _songs.value = repository.loadSongs()
            _albums.value = repository.loadAlbums()
            _artists.value = repository.loadArtists()
            
            // Initialize with a few playlists for demo
            _playlists.value = listOf(
                Playlist("1", "Favorites"),
                Playlist("2", "Recently Added"),
                Playlist("3", "Most Played")
            )
            Log.d(TAG, "Loaded ${_songs.value.size} songs")
            
            // Fetch artist images and album artwork from internet
            fetchArtworkFromInternet()
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

    private fun initializeController() {
        Log.d(TAG, "Initializing media controller")
        val context = getApplication<Application>()
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            Log.d(TAG, "Media controller initialized: $mediaController")
            mediaController?.addListener(playerListener)
            // Check if we have a current song after initializing controller
            updateCurrentSong()
            
            // Update shuffle and repeat mode from controller
            mediaController?.let { controller ->
                _isShuffleEnabled.value = controller.shuffleModeEnabled
                _repeatMode.value = controller.repeatMode
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "Playback state changed: $playbackState")
            _isPlaying.value = playbackState == Player.STATE_READY && 
                               mediaController?.isPlaying == true
            
            mediaController?.let { controller ->
                if (playbackState == Player.STATE_READY) {
                    _duration.value = controller.duration
                    Log.d(TAG, "Duration updated: ${controller.duration}")
                } else if (playbackState == Player.STATE_ENDED) {
                    // Handle playback completion - ensure progress is updated to the end
                    _progress.value = 1.0f
                    progressUpdateJob?.cancel()
                    Log.d(TAG, "Playback completed")
                }
            }
            
            // Restart progress updates when playback state changes
            if (_isPlaying.value) {
                startProgressUpdates()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "Media item transition: ${mediaItem?.mediaId}, reason: $reason")
            updateCurrentSong()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "Is playing changed: $isPlaying")
            _isPlaying.value = isPlaying
            
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
                _currentSong.value = song
                
                // Update favorite status
                _isFavorite.value = song?.let { s -> _favoriteSongs.value.contains(s.id) } ?: false
                
                // Check if the song is in the Favorites playlist
                if (song != null) {
                    val favoritesPlaylist = _playlists.value.find { it.id == "1" && it.name == "Favorites" }
                    if (favoritesPlaylist != null) {
                        val isInFavorites = favoritesPlaylist.songs.any { it.id == song.id }
                        if (isInFavorites != _isFavorite.value) {
                            // Sync the favorite status with the playlist
                            if (isInFavorites) {
                                _favoriteSongs.value = _favoriteSongs.value + song.id
                                _isFavorite.value = true
                            } else {
                                _favoriteSongs.value = _favoriteSongs.value - song.id
                                _isFavorite.value = false
                            }
                        }
                    }
                }
                
                // Fetch lyrics for the new song
                fetchLyricsForCurrentSong()
            }
        }
    }

    fun playSong(song: Song) {
        Log.d(TAG, "Playing song: ${song.title}")
        
        // Add to recently played list
        updateRecentlyPlayed(song)
        
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
            // Update favorite status
            _isFavorite.value = _favoriteSongs.value.contains(song.id)
            startProgressUpdates()
        }
    }

    private fun updateRecentlyPlayed(song: Song) {
        // Remove the song if it already exists in the list to avoid duplicates
        val currentList = _recentlyPlayed.value.toMutableList()
        currentList.removeIf { it.id == song.id }
        
        // Add the song at the beginning of the list
        currentList.add(0, song)
        
        // Keep only the last 10 songs
        _recentlyPlayed.value = currentList.take(10)
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
        mediaController?.let { controller ->
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
            
            controller.prepare()
            controller.play()
            _currentQueue.value = Queue(songs)
            _currentSong.value = songs.firstOrNull()
            _isPlaying.value = true
            
            // Add first song to recently played
            _currentSong.value?.let { updateRecentlyPlayed(it) }
            
            // Update favorite status
            _isFavorite.value = _currentSong.value?.let { song -> 
                _favoriteSongs.value.contains(song.id) 
            } ?: false
            startProgressUpdates()
        }
    }

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
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        Log.d(TAG, "Skip to previous")
        mediaController?.seekToPrevious()
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
            controller.repeatMode = newMode
            _repeatMode.value = newMode
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
            
            // In a real app, save this to a database or preferences
            // For this example, we'll just keep it in memory
        }
    }

    /**
     * Start monitoring for audio device changes
     */
    private fun startDeviceMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                // Refresh devices every 5 seconds
                audioDeviceManager.refreshDevices()
                delay(5000)
            }
        }
    }

    /**
     * Set the current audio output device
     */
    fun setCurrentLocation(location: PlaybackLocation) {
        Log.d(TAG, "Setting current location: ${location.name}")
        audioDeviceManager.setCurrentDevice(location)
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel being cleared")
        progressUpdateJob?.cancel()
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }

    // New functions for playlist management
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val newPlaylist = repository.createPlaylist(name)
            _playlists.value = _playlists.value + newPlaylist
            Log.d(TAG, "Created new playlist: ${newPlaylist.name}")
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
    }

    fun deletePlaylist(playlistId: String) {
        // Prevent deleting default playlists
        if (playlistId == "1" || playlistId == "2" || playlistId == "3") {
            Log.d(TAG, "Cannot delete default playlist: $playlistId")
            return
        }
        
        _playlists.value = _playlists.value.filter { it.id != playlistId }
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
        // Load playback settings
        _enableHighQualityAudio.value = sharedPreferences.getBoolean(PREF_HIGH_QUALITY_AUDIO, true)
        _enableGaplessPlayback.value = sharedPreferences.getBoolean(PREF_GAPLESS_PLAYBACK, true)
        _enableCrossfade.value = sharedPreferences.getBoolean(PREF_CROSSFADE, false)
        _crossfadeDuration.value = sharedPreferences.getFloat(PREF_CROSSFADE_DURATION, 2f)
        _enableAudioNormalization.value = sharedPreferences.getBoolean(PREF_AUDIO_NORMALIZATION, true)
        _enableReplayGain.value = sharedPreferences.getBoolean(PREF_REPLAY_GAIN, false)
        
        // Load lyrics settings
        _showLyrics.value = sharedPreferences.getBoolean(PREF_SHOW_LYRICS, true)
        _showOnlineOnlyLyrics.value = sharedPreferences.getBoolean(PREF_ONLINE_ONLY_LYRICS, false)
        
        Log.d(TAG, "Loaded settings: " +
                "HQ Audio=${_enableHighQualityAudio.value}, " +
                "Gapless=${_enableGaplessPlayback.value}, " +
                "Crossfade=${_enableCrossfade.value} (${_crossfadeDuration.value}s), " +
                "Normalization=${_enableAudioNormalization.value}, " +
                "ReplayGain=${_enableReplayGain.value}, " +
                "ShowLyrics=${_showLyrics.value}, " +
                "OnlineOnlyLyrics=${_showOnlineOnlyLyrics.value}")
    }

    /**
     * Updates the show lyrics setting
     */
    fun setShowLyrics(show: Boolean) {
        _showLyrics.value = show
        sharedPreferences.edit().putBoolean(PREF_SHOW_LYRICS, show).apply()
        if (show && _currentSong.value != null) {
            fetchLyricsForCurrentSong()
        } else {
            _currentLyrics.value = null
        }
    }
    
    /**
     * Updates the online-only lyrics setting
     */
    fun setShowOnlineOnlyLyrics(onlineOnly: Boolean) {
        _showOnlineOnlyLyrics.value = onlineOnly
        sharedPreferences.edit().putBoolean(PREF_ONLINE_ONLY_LYRICS, onlineOnly).apply()
        if (_showLyrics.value && _currentSong.value != null) {
            fetchLyricsForCurrentSong()
        }
    }
    
    /**
     * Fetches lyrics for the current song if settings allow
     */
    private fun fetchLyricsForCurrentSong() {
        val song = _currentSong.value ?: return
        
        // Clear current lyrics first
        _currentLyrics.value = null
        
        // Check if lyrics are enabled
        if (!_showLyrics.value) {
            return
        }
        
        // Check if we should only fetch lyrics when online
        if (_showOnlineOnlyLyrics.value && !repository.isNetworkAvailable()) {
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
        _enableHighQualityAudio.value = enable
        sharedPreferences.edit().putBoolean(PREF_HIGH_QUALITY_AUDIO, enable).apply()
        applyPlaybackSettings()
    }
    
    fun setGaplessPlayback(enable: Boolean) {
        _enableGaplessPlayback.value = enable
        sharedPreferences.edit().putBoolean(PREF_GAPLESS_PLAYBACK, enable).apply()
        applyPlaybackSettings()
    }
    
    fun setCrossfade(enable: Boolean) {
        _enableCrossfade.value = enable
        sharedPreferences.edit().putBoolean(PREF_CROSSFADE, enable).apply()
        applyPlaybackSettings()
    }
    
    fun setCrossfadeDuration(duration: Float) {
        _crossfadeDuration.value = duration
        sharedPreferences.edit().putFloat(PREF_CROSSFADE_DURATION, duration).apply()
        applyPlaybackSettings()
    }
    
    fun setAudioNormalization(enable: Boolean) {
        _enableAudioNormalization.value = enable
        sharedPreferences.edit().putBoolean(PREF_AUDIO_NORMALIZATION, enable).apply()
        applyPlaybackSettings()
    }
    
    fun setReplayGain(enable: Boolean) {
        _enableReplayGain.value = enable
        sharedPreferences.edit().putBoolean(PREF_REPLAY_GAIN, enable).apply()
        applyPlaybackSettings()
    }
    
    private fun applyPlaybackSettings() {
        // Apply settings to the media player
        mediaController?.let { controller ->
            // In a real app, these settings would be applied to the ExoPlayer instance
            // For example:
            // if (controller is ExoPlayer) {
            //     controller.setGaplessPlayback(_enableGaplessPlayback.value)
            //     if (_enableCrossfade.value) {
            //         controller.setCrossfadeDuration(_crossfadeDuration.value.toInt() * 1000)
            //     }
            //     controller.setAudioAttributes(
            //         AudioAttributes.Builder()
            //             .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            //             .setUsage(C.USAGE_MEDIA)
            //             .build(),
            //         !_enableHighQualityAudio.value
            //     )
            // }
            
            Log.d(TAG, "Applied playback settings: " +
                    "HQ Audio=${_enableHighQualityAudio.value}, " +
                    "Gapless=${_enableGaplessPlayback.value}, " +
                    "Crossfade=${_enableCrossfade.value} (${_crossfadeDuration.value}s), " +
                    "Normalization=${_enableAudioNormalization.value}, " +
                    "ReplayGain=${_enableReplayGain.value}")
            
            // Send intent to update service settings
            val context = getApplication<Application>()
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.ACTION_UPDATE_SETTINGS
            }
            context.startService(intent)
        }
    }

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
            
            controller.addMediaItem(mediaItem)
            
            // If nothing is currently playing, start playback
            if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_ENDED) {
                controller.prepare()
                controller.play()
            }
            
            // Update the queue in our state
            val currentQueueSongs = _currentQueue.value.songs.toMutableList()
            currentQueueSongs.add(song)
            _currentQueue.value = Queue(currentQueueSongs)
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