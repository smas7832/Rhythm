package chromahub.rhythm.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Singleton class to manage all app settings using SharedPreferences
 */
class AppSettings private constructor(context: Context) {
    companion object {
        private const val PREFS_NAME = "rhythm_preferences"
        
        // Playback Settings
        private const val KEY_HIGH_QUALITY_AUDIO = "high_quality_audio"
        private const val KEY_GAPLESS_PLAYBACK = "gapless_playback"
        private const val KEY_CROSSFADE = "crossfade"
        private const val KEY_CROSSFADE_DURATION = "crossfade_duration"
        private const val KEY_AUDIO_NORMALIZATION = "audio_normalization"
        private const val KEY_REPLAY_GAIN = "replay_gain"
        
        // Lyrics Settings
        private const val KEY_SHOW_LYRICS = "show_lyrics"
        private const val KEY_ONLINE_ONLY_LYRICS = "online_only_lyrics"
        
        // Theme Settings
        private const val KEY_USE_SYSTEM_THEME = "use_system_theme"
        private const val KEY_DARK_MODE = "dark_mode"
        
        // Audio Device Settings
        private const val KEY_LAST_AUDIO_DEVICE = "last_audio_device"
        private const val KEY_AUTO_CONNECT_DEVICE = "auto_connect_device"
        
        // Cache Settings
        private const val KEY_MAX_CACHE_SIZE = "max_cache_size"
        private const val KEY_CLEAR_CACHE_ON_EXIT = "clear_cache_on_exit"
        
        // Search History
        private const val KEY_SEARCH_HISTORY = "search_history"
        
        // Playlists
        private const val KEY_PLAYLISTS = "playlists"
        private const val KEY_FAVORITE_SONGS = "favorite_songs"
        
        // User Statistics
        private const val KEY_LISTENING_TIME = "listening_time"
        private const val KEY_SONGS_PLAYED = "songs_played"
        private const val KEY_UNIQUE_ARTISTS = "unique_artists"
        private const val KEY_GENRE_PREFERENCES = "genre_preferences"
        private const val KEY_TIME_BASED_PREFERENCES = "time_based_preferences"
        
        // Recently Played
        private const val KEY_RECENTLY_PLAYED = "recently_played"
        private const val KEY_LAST_PLAYED_TIMESTAMP = "last_played_timestamp"
        
        // Spotify Integration
        private const val KEY_SPOTIFY_API_KEY = "spotify_api_key"
        
        // Enhanced User Preferences
        private const val KEY_FAVORITE_GENRES = "favorite_genres"
        private const val KEY_DAILY_LISTENING_STATS = "daily_listening_stats"
        private const val KEY_WEEKLY_TOP_ARTISTS = "weekly_top_artists"
        private const val KEY_MOOD_PREFERENCES = "mood_preferences"
        
        // Song Play Counts
        private const val KEY_SONG_PLAY_COUNTS = "song_play_counts"

        // Onboarding
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // App Updater Settings
        private const val KEY_AUTO_CHECK_FOR_UPDATES = "auto_check_for_updates"
        private const val KEY_UPDATE_CHANNEL = "update_channel" // New key for update channel

        // Beta Program
        private const val KEY_HAS_SHOWN_BETA_POPUP = "has_shown_beta_popup"
        
        @Volatile
        private var INSTANCE: AppSettings? = null
        
        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettings(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Playback Settings
    private val _highQualityAudio = MutableStateFlow(prefs.getBoolean(KEY_HIGH_QUALITY_AUDIO, true))
    val highQualityAudio: StateFlow<Boolean> = _highQualityAudio.asStateFlow()
    
    private val _gaplessPlayback = MutableStateFlow(prefs.getBoolean(KEY_GAPLESS_PLAYBACK, true))
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()
    
    private val _crossfade = MutableStateFlow(prefs.getBoolean(KEY_CROSSFADE, false))
    val crossfade: StateFlow<Boolean> = _crossfade.asStateFlow()
    
    private val _crossfadeDuration = MutableStateFlow(prefs.getFloat(KEY_CROSSFADE_DURATION, 2f))
    val crossfadeDuration: StateFlow<Float> = _crossfadeDuration.asStateFlow()
    
    private val _audioNormalization = MutableStateFlow(prefs.getBoolean(KEY_AUDIO_NORMALIZATION, true))
    val audioNormalization: StateFlow<Boolean> = _audioNormalization.asStateFlow()
    
    private val _replayGain = MutableStateFlow(prefs.getBoolean(KEY_REPLAY_GAIN, false))
    val replayGain: StateFlow<Boolean> = _replayGain.asStateFlow()
    
    // Lyrics Settings
    private val _showLyrics = MutableStateFlow(prefs.getBoolean(KEY_SHOW_LYRICS, true))
    val showLyrics: StateFlow<Boolean> = _showLyrics.asStateFlow()
    
    private val _onlineOnlyLyrics = MutableStateFlow(prefs.getBoolean(KEY_ONLINE_ONLY_LYRICS, true))
    val onlineOnlyLyrics: StateFlow<Boolean> = _onlineOnlyLyrics.asStateFlow()
    
    // Theme Settings
    private val _useSystemTheme = MutableStateFlow(prefs.getBoolean(KEY_USE_SYSTEM_THEME, false))
    val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()
    
    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, true))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()
    
    // Audio Device Settings
    private val _lastAudioDevice = MutableStateFlow(prefs.getString(KEY_LAST_AUDIO_DEVICE, null))
    val lastAudioDevice: StateFlow<String?> = _lastAudioDevice.asStateFlow()
    
    private val _autoConnectDevice = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CONNECT_DEVICE, true))
    val autoConnectDevice: StateFlow<Boolean> = _autoConnectDevice.asStateFlow()
    
    // Cache Settings
    private val _maxCacheSize = MutableStateFlow(prefs.getLong(KEY_MAX_CACHE_SIZE, 1024L * 1024L * 512L)) // 512MB default
    val maxCacheSize: StateFlow<Long> = _maxCacheSize.asStateFlow()
    
    private val _clearCacheOnExit = MutableStateFlow(prefs.getBoolean(KEY_CLEAR_CACHE_ON_EXIT, false))
    val clearCacheOnExit: StateFlow<Boolean> = _clearCacheOnExit.asStateFlow()
    
    // Search History
    private val _searchHistory = MutableStateFlow<String?>(prefs.getString(KEY_SEARCH_HISTORY, null))
    val searchHistory: StateFlow<String?> = _searchHistory.asStateFlow()
    
    // Playlists
    private val _playlists = MutableStateFlow<String?>(prefs.getString(KEY_PLAYLISTS, null))
    val playlists: StateFlow<String?> = _playlists.asStateFlow()

    private val _favoriteSongs = MutableStateFlow<String?>(prefs.getString(KEY_FAVORITE_SONGS, null))
    val favoriteSongs: StateFlow<String?> = _favoriteSongs.asStateFlow()
    
    // User Statistics
    private val _listeningTime = MutableStateFlow(prefs.getLong(KEY_LISTENING_TIME, 0L))
    val listeningTime: StateFlow<Long> = _listeningTime.asStateFlow()
    
    private val _songsPlayed = MutableStateFlow(prefs.getInt(KEY_SONGS_PLAYED, 0))
    val songsPlayed: StateFlow<Int> = _songsPlayed.asStateFlow()
    
    private val _uniqueArtists = MutableStateFlow(prefs.getInt(KEY_UNIQUE_ARTISTS, 0))
    val uniqueArtists: StateFlow<Int> = _uniqueArtists.asStateFlow()
    
    private val _genrePreferences = MutableStateFlow<Map<String, Int>>(
        try {
            val json = prefs.getString(KEY_GENRE_PREFERENCES, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val genrePreferences: StateFlow<Map<String, Int>> = _genrePreferences.asStateFlow()
    
    private val _timeBasedPreferences = MutableStateFlow<Map<Int, List<String>>>(
        try {
            val json = prefs.getString(KEY_TIME_BASED_PREFERENCES, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<Int, List<String>>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val timeBasedPreferences: StateFlow<Map<Int, List<String>>> = _timeBasedPreferences.asStateFlow()
    
    // Recently Played
    private val _recentlyPlayed = MutableStateFlow<List<String>>(
        try {
            val json = prefs.getString(KEY_RECENTLY_PLAYED, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val recentlyPlayed: StateFlow<List<String>> = _recentlyPlayed.asStateFlow()
    
    private val _lastPlayedTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_PLAYED_TIMESTAMP, 0L))
    val lastPlayedTimestamp: StateFlow<Long> = _lastPlayedTimestamp.asStateFlow()
    
    // Spotify RapidAPI Key
    private val _spotifyApiKey = MutableStateFlow<String?>(prefs.getString(KEY_SPOTIFY_API_KEY, null))
    val spotifyApiKey: StateFlow<String?> = _spotifyApiKey.asStateFlow()

    // Enhanced User Preferences
    private val _favoriteGenres = MutableStateFlow<Map<String, Int>>(
        try {
            val json = prefs.getString(KEY_FAVORITE_GENRES, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val favoriteGenres: StateFlow<Map<String, Int>> = _favoriteGenres.asStateFlow()
    
    private val _dailyListeningStats = MutableStateFlow<Map<String, Long>>(
        try {
            val json = prefs.getString(KEY_DAILY_LISTENING_STATS, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<String, Long>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val dailyListeningStats: StateFlow<Map<String, Long>> = _dailyListeningStats.asStateFlow()
    
    private val _weeklyTopArtists = MutableStateFlow<Map<String, Int>>(
        try {
            val json = prefs.getString(KEY_WEEKLY_TOP_ARTISTS, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val weeklyTopArtists: StateFlow<Map<String, Int>> = _weeklyTopArtists.asStateFlow()
    
    private val _moodPreferences = MutableStateFlow<Map<String, List<String>>>(
        try {
            val json = prefs.getString(KEY_MOOD_PREFERENCES, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<String, List<String>>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val moodPreferences: StateFlow<Map<String, List<String>>> = _moodPreferences.asStateFlow()

    private val _songPlayCounts = MutableStateFlow<Map<String, Int>>(
        try {
            val json = prefs.getString(KEY_SONG_PLAY_COUNTS, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    )
    val songPlayCounts: StateFlow<Map<String, Int>> = _songPlayCounts.asStateFlow()

    // Onboarding
    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // App Updater Settings
    private val _autoCheckForUpdates = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CHECK_FOR_UPDATES, true))
    val autoCheckForUpdates: StateFlow<Boolean> = _autoCheckForUpdates.asStateFlow()

    private val _updateChannel = MutableStateFlow(prefs.getString(KEY_UPDATE_CHANNEL, "stable") ?: "stable")
    val updateChannel: StateFlow<String> = _updateChannel.asStateFlow()

    // Beta Program
    private val _hasShownBetaPopup = MutableStateFlow(prefs.getBoolean(KEY_HAS_SHOWN_BETA_POPUP, false))
    val hasShownBetaPopup: StateFlow<Boolean> = _hasShownBetaPopup.asStateFlow()
    
    // Playback Settings Methods
    fun setHighQualityAudio(enable: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_QUALITY_AUDIO, enable).apply()
        _highQualityAudio.value = enable
    }
    
    fun setGaplessPlayback(enable: Boolean) {
        prefs.edit().putBoolean(KEY_GAPLESS_PLAYBACK, enable).apply()
        _gaplessPlayback.value = enable
    }
    
    fun setCrossfade(enable: Boolean) {
        prefs.edit().putBoolean(KEY_CROSSFADE, enable).apply()
        _crossfade.value = enable
    }
    
    fun setCrossfadeDuration(duration: Float) {
        prefs.edit().putFloat(KEY_CROSSFADE_DURATION, duration).apply()
        _crossfadeDuration.value = duration
    }
    
    fun setAudioNormalization(enable: Boolean) {
        prefs.edit().putBoolean(KEY_AUDIO_NORMALIZATION, enable).apply()
        _audioNormalization.value = enable
    }
    
    fun setReplayGain(enable: Boolean) {
        prefs.edit().putBoolean(KEY_REPLAY_GAIN, enable).apply()
        _replayGain.value = enable
    }
    
    // Lyrics Settings Methods
    fun setShowLyrics(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_LYRICS, show).apply()
        _showLyrics.value = show
    }
    
    fun setOnlineOnlyLyrics(onlineOnly: Boolean) {
        prefs.edit().putBoolean(KEY_ONLINE_ONLY_LYRICS, onlineOnly).apply()
        _onlineOnlyLyrics.value = onlineOnly
    }
    
    // Theme Settings Methods
    fun setUseSystemTheme(use: Boolean) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_THEME, use).apply()
        _useSystemTheme.value = use
    }
    
    fun setDarkMode(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
        _darkMode.value = dark
    }
    
    // Audio Device Settings Methods
    fun setLastAudioDevice(deviceId: String?) {
        if (deviceId == null) {
            prefs.edit().remove(KEY_LAST_AUDIO_DEVICE).apply()
        } else {
            prefs.edit().putString(KEY_LAST_AUDIO_DEVICE, deviceId).apply()
        }
        _lastAudioDevice.value = deviceId
    }
    
    fun setAutoConnectDevice(enable: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT_DEVICE, enable).apply()
        _autoConnectDevice.value = enable
    }
    
    // Cache Settings Methods
    fun setMaxCacheSize(size: Long) {
        prefs.edit().putLong(KEY_MAX_CACHE_SIZE, size).apply()
        _maxCacheSize.value = size
    }
    
    fun setClearCacheOnExit(clear: Boolean) {
        prefs.edit().putBoolean(KEY_CLEAR_CACHE_ON_EXIT, clear).apply()
        _clearCacheOnExit.value = clear
    }
    
    // Search History Methods
    fun setSearchHistory(history: String?) {
        if (history == null) {
            prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
        } else {
            prefs.edit().putString(KEY_SEARCH_HISTORY, history).apply()
        }
        _searchHistory.value = history
    }

    // Playlists
    fun setPlaylists(playlistsJson: String?) {
        if (playlistsJson == null) {
            prefs.edit().remove(KEY_PLAYLISTS).commit()
        } else {
            prefs.edit().putString(KEY_PLAYLISTS, playlistsJson).commit()
        }
        _playlists.value = playlistsJson
    }

    fun setFavoriteSongs(favoriteSongsJson: String?) {
        if (favoriteSongsJson == null) {
            prefs.edit().remove(KEY_FAVORITE_SONGS).commit()
        } else {
            prefs.edit().putString(KEY_FAVORITE_SONGS, favoriteSongsJson).commit()
        }
        _favoriteSongs.value = favoriteSongsJson
    }

    // User Statistics Methods
    fun setListeningTime(time: Long) {
        prefs.edit().putLong(KEY_LISTENING_TIME, time).apply()
        _listeningTime.value = time
    }
    
    fun setSongsPlayed(count: Int) {
        prefs.edit().putInt(KEY_SONGS_PLAYED, count).apply()
        _songsPlayed.value = count
    }
    
    fun setUniqueArtists(count: Int) {
        prefs.edit().putInt(KEY_UNIQUE_ARTISTS, count).apply()
        _uniqueArtists.value = count
    }
    
    fun setGenrePreferences(preferences: Map<String, Int>) {
        val json = Gson().toJson(preferences)
        prefs.edit().putString(KEY_GENRE_PREFERENCES, json).apply()
        _genrePreferences.value = preferences
    }
    
    fun setTimeBasedPreferences(preferences: Map<Int, List<String>>) {
        val json = Gson().toJson(preferences)
        prefs.edit().putString(KEY_TIME_BASED_PREFERENCES, json).apply()
        _timeBasedPreferences.value = preferences
    }

    // Recently Played Methods
    fun updateRecentlyPlayed(songIds: List<String>) {
        val json = Gson().toJson(songIds)
        prefs.edit().putString(KEY_RECENTLY_PLAYED, json).apply()
        _recentlyPlayed.value = songIds
    }
    
    fun updateLastPlayedTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_PLAYED_TIMESTAMP, timestamp).apply()
        _lastPlayedTimestamp.value = timestamp
    }
    
    // Spotify API Key Methods
    fun setSpotifyApiKey(key: String?) {
        if (key.isNullOrBlank()) {
            prefs.edit().remove(KEY_SPOTIFY_API_KEY).apply()
            _spotifyApiKey.value = null
        } else {
            prefs.edit().putString(KEY_SPOTIFY_API_KEY, key).apply()
            _spotifyApiKey.value = key
        }
    }

    // Enhanced User Preferences Methods
    fun updateFavoriteGenres(genres: Map<String, Int>) {
        val json = Gson().toJson(genres)
        prefs.edit().putString(KEY_FAVORITE_GENRES, json).apply()
        _favoriteGenres.value = genres
    }
    
    fun updateDailyListeningStats(stats: Map<String, Long>) {
        val json = Gson().toJson(stats)
        prefs.edit().putString(KEY_DAILY_LISTENING_STATS, json).apply()
        _dailyListeningStats.value = stats
    }
    
    fun updateWeeklyTopArtists(artists: Map<String, Int>) {
        val json = Gson().toJson(artists)
        prefs.edit().putString(KEY_WEEKLY_TOP_ARTISTS, json).apply()
        _weeklyTopArtists.value = artists
    }
    
    fun updateMoodPreferences(preferences: Map<String, List<String>>) {
        val json = Gson().toJson(preferences)
        prefs.edit().putString(KEY_MOOD_PREFERENCES, json).apply()
        _moodPreferences.value = preferences
    }

    // Song Play Counts Methods
    fun setSongPlayCounts(counts: Map<String, Int>) {
        val json = Gson().toJson(counts)
        prefs.edit().putString(KEY_SONG_PLAY_COUNTS, json).apply()
        _songPlayCounts.value = counts
    }

    // Onboarding Methods
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    // App Updater Settings Methods
    fun setAutoCheckForUpdates(enable: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CHECK_FOR_UPDATES, enable).apply()
        _autoCheckForUpdates.value = enable
    }

    fun setUpdateChannel(channel: String) {
        prefs.edit().putString(KEY_UPDATE_CHANNEL, channel).apply()
        _updateChannel.value = channel
    }

    // Beta Program Methods
    fun setHasShownBetaPopup(shown: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SHOWN_BETA_POPUP, shown).apply()
        _hasShownBetaPopup.value = shown
    }
}
