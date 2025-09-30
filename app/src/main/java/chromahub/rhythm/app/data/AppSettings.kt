package chromahub.rhythm.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date // Import Date for timestamp

/**
 * Data class to represent a single crash log entry
 */
data class CrashLogEntry(
    val timestamp: Long,
    val log: String
)

/**
 * Enum for album view types in the library
 */
enum class AlbumViewType {
    LIST, GRID
}

/**
 * Enum for artist view types in the library
 */
enum class ArtistViewType {
    LIST, GRID
}

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
        private const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"
        
        // Library Settings
        private const val KEY_ALBUM_VIEW_TYPE = "album_view_type"
        private const val KEY_ARTIST_VIEW_TYPE = "artist_view_type"
        private const val KEY_ALBUM_SORT_ORDER = "album_sort_order"
        private const val KEY_ARTIST_COLLABORATION_MODE = "artist_collaboration_mode"
        
        // Audio Device Settings
        private const val KEY_LAST_AUDIO_DEVICE = "last_audio_device"
        private const val KEY_AUTO_CONNECT_DEVICE = "auto_connect_device"
        private const val KEY_USE_SYSTEM_VOLUME = "use_system_volume"
        
        // Equalizer Settings
        private const val KEY_EQUALIZER_ENABLED = "equalizer_enabled"
        private const val KEY_EQUALIZER_PRESET = "equalizer_preset"
        private const val KEY_EQUALIZER_BAND_LEVELS = "equalizer_band_levels"
        private const val KEY_BASS_BOOST_ENABLED = "bass_boost_enabled"
        private const val KEY_BASS_BOOST_STRENGTH = "bass_boost_strength"
        private const val KEY_VIRTUALIZER_ENABLED = "virtualizer_enabled"
        private const val KEY_VIRTUALIZER_STRENGTH = "virtualizer_strength"
        
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
        
        // API Integration
        private const val KEY_DEEZER_API_ENABLED = "deezer_api_enabled"
        private const val KEY_CANVAS_API_ENABLED = "canvas_api_enabled"
        private const val KEY_LRCLIB_API_ENABLED = "lrclib_api_enabled"
        private const val KEY_YTMUSIC_API_ENABLED = "ytmusic_api_enabled"
        private const val KEY_SPOTIFY_API_ENABLED = "spotify_api_enabled"
        private const val KEY_SPOTIFY_CLIENT_ID = "spotify_client_id"
        private const val KEY_SPOTIFY_CLIENT_SECRET = "spotify_client_secret"
        
        // Enhanced User Preferences
        private const val KEY_FAVORITE_GENRES = "favorite_genres"
        private const val KEY_DAILY_LISTENING_STATS = "daily_listening_stats"
        private const val KEY_WEEKLY_TOP_ARTISTS = "weekly_top_artists"
        private const val KEY_MOOD_PREFERENCES = "mood_preferences"
        
        // Song Play Counts
        private const val KEY_SONG_PLAY_COUNTS = "song_play_counts"

        // Onboarding
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_INITIAL_MEDIA_SCAN_COMPLETED = "initial_media_scan_completed"

        // App Updater Settings
        private const val KEY_AUTO_CHECK_FOR_UPDATES = "auto_check_for_updates"
        private const val KEY_UPDATE_CHANNEL = "update_channel" // New key for update channel
        private const val KEY_UPDATES_ENABLED = "updates_enabled" // Master switch for updates
        private const val KEY_MEDIA_SCAN_MODE = "media_scan_mode" // Mode for media scanning: "blacklist" or "whitelist"
        private const val KEY_UPDATE_CHECK_INTERVAL_HOURS = "update_check_interval_hours" // Configurable interval

        // Beta Program
        private const val KEY_HAS_SHOWN_BETA_POPUP = "has_shown_beta_popup"

        // Crash Reporting
        private const val KEY_LAST_CRASH_LOG = "last_crash_log"
        private const val KEY_CRASH_LOG_HISTORY = "crash_log_history" // New key for crash log history
        
        // Haptic Feedback
        private const val KEY_HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled"
        
        // Notification Settings
        private const val KEY_USE_CUSTOM_NOTIFICATION = "use_custom_notification"
        
        // Blacklisted Songs
        private const val KEY_BLACKLISTED_SONGS = "blacklisted_songs"
        
        // Blacklisted Folders
        private const val KEY_BLACKLISTED_FOLDERS = "blacklisted_folders"
        
        // Whitelisted Songs
        private const val KEY_WHITELISTED_SONGS = "whitelisted_songs"
        
        // Whitelisted Folders
        private const val KEY_WHITELISTED_FOLDERS = "whitelisted_folders"

        // Pinned Folders (Explorer)
        private const val KEY_PINNED_FOLDERS = "pinned_folders"
        
        // Backup and Restore
        private const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_BACKUP_LOCATION = "backup_location"
        
        // Sleep Timer
        private const val KEY_SLEEP_TIMER_ACTIVE = "sleep_timer_active"
        private const val KEY_SLEEP_TIMER_REMAINING_SECONDS = "sleep_timer_remaining_seconds"
        private const val KEY_SLEEP_TIMER_ACTION = "sleep_timer_action"
        
        
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
    private val _useSystemTheme = MutableStateFlow(prefs.getBoolean(KEY_USE_SYSTEM_THEME, true))
    val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()
    
    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, true))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()
    
    private val _useDynamicColors = MutableStateFlow(prefs.getBoolean(KEY_USE_DYNAMIC_COLORS, false))
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()
    
    // Library Settings
    private val _albumViewType = MutableStateFlow(
        AlbumViewType.valueOf(prefs.getString(KEY_ALBUM_VIEW_TYPE, AlbumViewType.GRID.name) ?: AlbumViewType.GRID.name)
    )
    val albumViewType: StateFlow<AlbumViewType> = _albumViewType.asStateFlow()
    
    private val _artistViewType = MutableStateFlow(
        ArtistViewType.valueOf(prefs.getString(KEY_ARTIST_VIEW_TYPE, ArtistViewType.GRID.name) ?: ArtistViewType.GRID.name)
    )
    val artistViewType: StateFlow<ArtistViewType> = _artistViewType.asStateFlow()
    
    // Album Sort Order
    private val _albumSortOrder = MutableStateFlow(prefs.getString(KEY_ALBUM_SORT_ORDER, "TRACK_NUMBER") ?: "TRACK_NUMBER")
    val albumSortOrder: StateFlow<String> = _albumSortOrder.asStateFlow()
    
    // Artist Collaboration Mode
    private val _artistCollaborationMode = MutableStateFlow(prefs.getBoolean(KEY_ARTIST_COLLABORATION_MODE, false))
    val artistCollaborationMode: StateFlow<Boolean> = _artistCollaborationMode.asStateFlow()
    
    // Audio Device Settings
    private val _lastAudioDevice = MutableStateFlow(prefs.getString(KEY_LAST_AUDIO_DEVICE, null))
    val lastAudioDevice: StateFlow<String?> = _lastAudioDevice.asStateFlow()
    
    private val _autoConnectDevice = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CONNECT_DEVICE, true))
    val autoConnectDevice: StateFlow<Boolean> = _autoConnectDevice.asStateFlow()
    
    private val _useSystemVolume = MutableStateFlow(prefs.getBoolean(KEY_USE_SYSTEM_VOLUME, false))
    val useSystemVolume: StateFlow<Boolean> = _useSystemVolume.asStateFlow()
    
    // Equalizer Settings
    private val _equalizerEnabled = MutableStateFlow(prefs.getBoolean(KEY_EQUALIZER_ENABLED, false))
    val equalizerEnabled: StateFlow<Boolean> = _equalizerEnabled.asStateFlow()
    
    private val _equalizerPreset = MutableStateFlow(prefs.getString(KEY_EQUALIZER_PRESET, "Custom") ?: "Custom")
    val equalizerPreset: StateFlow<String> = _equalizerPreset.asStateFlow()
    
    private val _equalizerBandLevels = MutableStateFlow(prefs.getString(KEY_EQUALIZER_BAND_LEVELS, "0.0,0.0,0.0,0.0,0.0") ?: "0.0,0.0,0.0,0.0,0.0")
    val equalizerBandLevels: StateFlow<String> = _equalizerBandLevels.asStateFlow()
    
    private val _bassBoostEnabled = MutableStateFlow(prefs.getBoolean(KEY_BASS_BOOST_ENABLED, false))
    val bassBoostEnabled: StateFlow<Boolean> = _bassBoostEnabled.asStateFlow()
    
    private val _bassBoostStrength = MutableStateFlow(prefs.getInt(KEY_BASS_BOOST_STRENGTH, 500))
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()
    
    private val _virtualizerEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIRTUALIZER_ENABLED, false))
    val virtualizerEnabled: StateFlow<Boolean> = _virtualizerEnabled.asStateFlow()
    
    private val _virtualizerStrength = MutableStateFlow(prefs.getInt(KEY_VIRTUALIZER_STRENGTH, 500))
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()
    
    // Sleep Timer
    private val _sleepTimerActive = MutableStateFlow(prefs.getBoolean(KEY_SLEEP_TIMER_ACTIVE, false))
    val sleepTimerActive: StateFlow<Boolean> = _sleepTimerActive.asStateFlow()
    
    private val _sleepTimerRemainingSeconds = MutableStateFlow(prefs.getLong(KEY_SLEEP_TIMER_REMAINING_SECONDS, 0L))
    val sleepTimerRemainingSeconds: StateFlow<Long> = _sleepTimerRemainingSeconds.asStateFlow()
    
    private val _sleepTimerAction = MutableStateFlow(prefs.getString(KEY_SLEEP_TIMER_ACTION, "FADE_OUT") ?: "FADE_OUT")
    val sleepTimerAction: StateFlow<String> = _sleepTimerAction.asStateFlow()
    
    // Cache Settings
    private val _maxCacheSize = MutableStateFlow(safeLong(KEY_MAX_CACHE_SIZE, 1024L * 1024L * 512L)) // 512MB default
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
    private val _listeningTime = MutableStateFlow(safeLong(KEY_LISTENING_TIME, 0L))
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
    
    private val _lastPlayedTimestamp = MutableStateFlow(safeLong(KEY_LAST_PLAYED_TIMESTAMP, 0L))
    val lastPlayedTimestamp: StateFlow<Long> = _lastPlayedTimestamp.asStateFlow()
    
    // API Enable/Disable States
    private val _deezerApiEnabled = MutableStateFlow(prefs.getBoolean(KEY_DEEZER_API_ENABLED, true))
    val deezerApiEnabled: StateFlow<Boolean> = _deezerApiEnabled.asStateFlow()
    
    private val _canvasApiEnabled = MutableStateFlow(prefs.getBoolean(KEY_CANVAS_API_ENABLED, true))
    val canvasApiEnabled: StateFlow<Boolean> = _canvasApiEnabled.asStateFlow()
    
    private val _lrclibApiEnabled = MutableStateFlow(prefs.getBoolean(KEY_LRCLIB_API_ENABLED, true))
    val lrclibApiEnabled: StateFlow<Boolean> = _lrclibApiEnabled.asStateFlow()
    
    private val _ytMusicApiEnabled = MutableStateFlow(prefs.getBoolean(KEY_YTMUSIC_API_ENABLED, true))
    val ytMusicApiEnabled: StateFlow<Boolean> = _ytMusicApiEnabled.asStateFlow()
    
    private val _spotifyApiEnabled = MutableStateFlow(prefs.getBoolean(KEY_SPOTIFY_API_ENABLED, false))
    val spotifyApiEnabled: StateFlow<Boolean> = _spotifyApiEnabled.asStateFlow()
    
    private val _spotifyClientId = MutableStateFlow(prefs.getString(KEY_SPOTIFY_CLIENT_ID, "") ?: "")
    val spotifyClientId: StateFlow<String> = _spotifyClientId.asStateFlow()
    
    private val _spotifyClientSecret = MutableStateFlow(prefs.getString(KEY_SPOTIFY_CLIENT_SECRET, "") ?: "")
    val spotifyClientSecret: StateFlow<String> = _spotifyClientSecret.asStateFlow()

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

    private val _initialMediaScanCompleted = MutableStateFlow(prefs.getBoolean(KEY_INITIAL_MEDIA_SCAN_COMPLETED, false))
    val initialMediaScanCompleted: StateFlow<Boolean> = _initialMediaScanCompleted.asStateFlow()

    // App Updater Settings
    private val _autoCheckForUpdates = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CHECK_FOR_UPDATES, true))
    val autoCheckForUpdates: StateFlow<Boolean> = _autoCheckForUpdates.asStateFlow()

    private val _updateChannel = MutableStateFlow(prefs.getString(KEY_UPDATE_CHANNEL, "stable") ?: "stable")
    val updateChannel: StateFlow<String> = _updateChannel.asStateFlow()

    private val _updatesEnabled = MutableStateFlow(prefs.getBoolean(KEY_UPDATES_ENABLED, true))
    val updatesEnabled: StateFlow<Boolean> = _updatesEnabled.asStateFlow()

    // Media Scan Mode
    private val _mediaScanMode = MutableStateFlow(prefs.getString(KEY_MEDIA_SCAN_MODE, "blacklist") ?: "blacklist")
    val mediaScanMode: StateFlow<String> = _mediaScanMode.asStateFlow()

    private val _updateCheckIntervalHours = MutableStateFlow(prefs.getInt(KEY_UPDATE_CHECK_INTERVAL_HOURS, 6))
    val updateCheckIntervalHours: StateFlow<Int> = _updateCheckIntervalHours.asStateFlow()

    // Beta Program
    private val _hasShownBetaPopup = MutableStateFlow(prefs.getBoolean(KEY_HAS_SHOWN_BETA_POPUP, false))
    val hasShownBetaPopup: StateFlow<Boolean> = _hasShownBetaPopup.asStateFlow()

    // Crash Reporting
    private val _lastCrashLog = MutableStateFlow<String?>(prefs.getString(KEY_LAST_CRASH_LOG, null))
    val lastCrashLog: StateFlow<String?> = _lastCrashLog.asStateFlow()

    private val _crashLogHistory = MutableStateFlow<List<CrashLogEntry>>(
        try {
            val json = prefs.getString(KEY_CRASH_LOG_HISTORY, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<List<CrashLogEntry>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val crashLogHistory: StateFlow<List<CrashLogEntry>> = _crashLogHistory.asStateFlow()
    
    // Haptic Feedback Settings
    private val _hapticFeedbackEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, true))
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()
    
    // Notification Settings
    private val _useCustomNotification = MutableStateFlow(prefs.getBoolean(KEY_USE_CUSTOM_NOTIFICATION, false))
    val useCustomNotification: StateFlow<Boolean> = _useCustomNotification.asStateFlow()
    
    // Blacklisted Songs
    private val _blacklistedSongs = MutableStateFlow<List<String>>(
        try {
            val json = prefs.getString(KEY_BLACKLISTED_SONGS, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val blacklistedSongs: StateFlow<List<String>> = _blacklistedSongs.asStateFlow()
    
    // Blacklisted Folders
    private val _blacklistedFolders = MutableStateFlow<List<String>>(
        try {
            val json = prefs.getString(KEY_BLACKLISTED_FOLDERS, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val blacklistedFolders: StateFlow<List<String>> = _blacklistedFolders.asStateFlow()
    
    // Whitelisted Songs
    private val _whitelistedSongs = MutableStateFlow<List<String>>(
        try {
            val json = prefs.getString(KEY_WHITELISTED_SONGS, null)
            if (json != null) {
                val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                Gson().fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val whitelistedSongs: StateFlow<List<String>> = _whitelistedSongs.asStateFlow()
    
    // Whitelisted Folders
    private val _whitelistedFolders = MutableStateFlow<List<String>>(
        try {
            val json = prefs.getString(KEY_WHITELISTED_FOLDERS, null)
            if (json != null) {
                val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                Gson().fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val whitelistedFolders: StateFlow<List<String>> = _whitelistedFolders.asStateFlow()

    // Pinned Folders (Explorer)
    private val _pinnedFolders = MutableStateFlow<List<String>>(
        try {
            val json = prefs.getString(KEY_PINNED_FOLDERS, null)
            if (json != null) {
                Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    )
    val pinnedFolders: StateFlow<List<String>> = _pinnedFolders.asStateFlow()

    // Backup and Restore Settings
    private val _lastBackupTimestamp = MutableStateFlow(safeLong(KEY_LAST_BACKUP_TIMESTAMP, 0L))
    val lastBackupTimestamp: StateFlow<Long> = _lastBackupTimestamp.asStateFlow()
    
    private val _autoBackupEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false))
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()
    
    private val _backupLocation = MutableStateFlow(prefs.getString(KEY_BACKUP_LOCATION, null))
    val backupLocation: StateFlow<String?> = _backupLocation.asStateFlow()
    
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
        if (isValidCrossfadeDuration(duration)) {
            prefs.edit().putFloat(KEY_CROSSFADE_DURATION, duration).apply()
            _crossfadeDuration.value = duration
        } else {
            Log.w("AppSettings", "Invalid crossfade duration: $duration, keeping current value")
        }
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
    
    fun setUseDynamicColors(use: Boolean) {
        prefs.edit().putBoolean(KEY_USE_DYNAMIC_COLORS, use).apply()
        _useDynamicColors.value = use
    }
    
    // Library Settings Methods
    fun setAlbumViewType(viewType: AlbumViewType) {
        prefs.edit().putString(KEY_ALBUM_VIEW_TYPE, viewType.name).apply()
        _albumViewType.value = viewType
    }
    
    fun setArtistViewType(viewType: ArtistViewType) {
        prefs.edit().putString(KEY_ARTIST_VIEW_TYPE, viewType.name).apply()
        _artistViewType.value = viewType
    }
    
    fun setAlbumSortOrder(sortOrder: String) {
        prefs.edit().putString(KEY_ALBUM_SORT_ORDER, sortOrder).apply()
        _albumSortOrder.value = sortOrder
    }
    
    fun setArtistCollaborationMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ARTIST_COLLABORATION_MODE, enabled).apply()
        _artistCollaborationMode.value = enabled
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
    
    fun setUseSystemVolume(enable: Boolean) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_VOLUME, enable).apply()
        _useSystemVolume.value = enable
    }
    
    // Equalizer Settings Methods
    fun setEqualizerEnabled(enable: Boolean) {
        prefs.edit().putBoolean(KEY_EQUALIZER_ENABLED, enable).apply()
        _equalizerEnabled.value = enable
    }
    
    fun setEqualizerPreset(preset: String) {
        prefs.edit().putString(KEY_EQUALIZER_PRESET, preset).apply()
        _equalizerPreset.value = preset
    }
    
    fun setEqualizerBandLevels(levels: String) {
        prefs.edit().putString(KEY_EQUALIZER_BAND_LEVELS, levels).apply()
        _equalizerBandLevels.value = levels
    }
    
    fun setBassBoostEnabled(enable: Boolean) {
        prefs.edit().putBoolean(KEY_BASS_BOOST_ENABLED, enable).apply()
        _bassBoostEnabled.value = enable
    }
    
    fun setBassBoostStrength(strength: Int) {
        prefs.edit().putInt(KEY_BASS_BOOST_STRENGTH, strength).apply()
        _bassBoostStrength.value = strength
    }
    
    fun setVirtualizerEnabled(enable: Boolean) {
        prefs.edit().putBoolean(KEY_VIRTUALIZER_ENABLED, enable).apply()
        _virtualizerEnabled.value = enable
    }
    
    fun setVirtualizerStrength(strength: Int) {
        prefs.edit().putInt(KEY_VIRTUALIZER_STRENGTH, strength).apply()
        _virtualizerStrength.value = strength
    }
    
    // Sleep Timer Methods
    fun setSleepTimerActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_SLEEP_TIMER_ACTIVE, active).apply()
        _sleepTimerActive.value = active
    }
    
    fun setSleepTimerRemainingSeconds(seconds: Long) {
        prefs.edit().putLong(KEY_SLEEP_TIMER_REMAINING_SECONDS, seconds).apply()
        _sleepTimerRemainingSeconds.value = seconds
    }
    
    fun setSleepTimerAction(action: String) {
        prefs.edit().putString(KEY_SLEEP_TIMER_ACTION, action).apply()
        _sleepTimerAction.value = action
    }
    
    // Cache Settings Methods
    fun setMaxCacheSize(size: Long) {
        if (isValidCacheSize(size)) {
            prefs.edit().putLong(KEY_MAX_CACHE_SIZE, size).apply()
            _maxCacheSize.value = size
        } else {
            Log.w("AppSettings", "Invalid cache size: $size, keeping current value")
        }
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
    
    
    // API Enable/Disable Methods
    fun setDeezerApiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEEZER_API_ENABLED, enabled).apply()
        _deezerApiEnabled.value = enabled
    }
    
    fun setCanvasApiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CANVAS_API_ENABLED, enabled).apply()
        _canvasApiEnabled.value = enabled
    }
    
    fun setLrcLibApiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LRCLIB_API_ENABLED, enabled).apply()
        _lrclibApiEnabled.value = enabled
    }
    
    fun setYTMusicApiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_YTMUSIC_API_ENABLED, enabled).apply()
        _ytMusicApiEnabled.value = enabled
    }
    
    fun setSpotifyApiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPOTIFY_API_ENABLED, enabled).apply()
        _spotifyApiEnabled.value = enabled
    }
    
    fun setSpotifyClientId(clientId: String) {
        prefs.edit().putString(KEY_SPOTIFY_CLIENT_ID, clientId).apply()
        _spotifyClientId.value = clientId
    }
    
    fun setSpotifyClientSecret(clientSecret: String) {
        prefs.edit().putString(KEY_SPOTIFY_CLIENT_SECRET, clientSecret).apply()
        _spotifyClientSecret.value = clientSecret
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

    fun setInitialMediaScanCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_INITIAL_MEDIA_SCAN_COMPLETED, completed).apply()
        _initialMediaScanCompleted.value = completed
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

    fun setUpdatesEnabled(enable: Boolean) {
        prefs.edit().putBoolean(KEY_UPDATES_ENABLED, enable).apply()
        _updatesEnabled.value = enable
    }

    fun setMediaScanMode(mode: String) {
        prefs.edit().putString(KEY_MEDIA_SCAN_MODE, mode).apply()
        _mediaScanMode.value = mode
    }

    fun setUpdateCheckIntervalHours(hours: Int) {
        prefs.edit().putInt(KEY_UPDATE_CHECK_INTERVAL_HOURS, hours).apply()
        _updateCheckIntervalHours.value = hours
    }

    // Beta Program Methods
    fun setHasShownBetaPopup(shown: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SHOWN_BETA_POPUP, shown).apply()
        _hasShownBetaPopup.value = shown
    }

    // Crash Reporting Methods
    fun setLastCrashLog(log: String?) {
        if (log == null) {
            prefs.edit().remove(KEY_LAST_CRASH_LOG).apply()
        } else {
            prefs.edit().putString(KEY_LAST_CRASH_LOG, log).apply()
        }
        _lastCrashLog.value = log
    }

    fun addCrashLogEntry(log: String) {
        val currentHistory = _crashLogHistory.value.toMutableList()
        val newEntry = CrashLogEntry(System.currentTimeMillis(), log)
        currentHistory.add(0, newEntry) // Add to the beginning
        // Keep only the last 8 crash logs to prevent excessive storage
        val limitedHistory = currentHistory.take(6)
        val json = Gson().toJson(limitedHistory)
        prefs.edit().putString(KEY_CRASH_LOG_HISTORY, json).commit() // Changed to commit() for synchronous write
        _crashLogHistory.value = limitedHistory
    }

    fun clearCrashLogHistory() {
        prefs.edit().remove(KEY_CRASH_LOG_HISTORY).apply()
        _crashLogHistory.value = emptyList()
    }
    
    // Haptic Feedback Methods
    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, enabled).apply()
        _hapticFeedbackEnabled.value = enabled
    }
    
    // Notification Settings Methods
    fun setUseCustomNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CUSTOM_NOTIFICATION, enabled).apply()
        _useCustomNotification.value = enabled
    }
    
    // Blacklisted Songs Methods
    fun addToBlacklist(songId: String) {
        val currentList = _blacklistedSongs.value.toMutableList()
        if (!currentList.contains(songId)) {
            currentList.add(songId)
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_BLACKLISTED_SONGS, json).apply()
            _blacklistedSongs.value = currentList
        }
    }
    
    fun removeFromBlacklist(songId: String) {
        val currentList = _blacklistedSongs.value.toMutableList()
        if (currentList.remove(songId)) {
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_BLACKLISTED_SONGS, json).apply()
            _blacklistedSongs.value = currentList
        }
    }
    
    fun isBlacklisted(songId: String): Boolean {
        return _blacklistedSongs.value.contains(songId)
    }
    
    fun clearBlacklist() {
        prefs.edit().remove(KEY_BLACKLISTED_SONGS).apply()
        _blacklistedSongs.value = emptyList()
    }
    
    // Blacklisted Folders Methods
    fun addFolderToBlacklist(folderPath: String) {
        val currentList = _blacklistedFolders.value.toMutableList()
        if (!currentList.contains(folderPath)) {
            currentList.add(folderPath)
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_BLACKLISTED_FOLDERS, json).apply()
            _blacklistedFolders.value = currentList
        }
    }
    
    fun removeFolderFromBlacklist(folderPath: String) {
        val currentList = _blacklistedFolders.value.toMutableList()
        if (currentList.remove(folderPath)) {
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_BLACKLISTED_FOLDERS, json).apply()
            _blacklistedFolders.value = currentList
        }
    }
    
    fun isFolderBlacklisted(folderPath: String): Boolean {
        return _blacklistedFolders.value.any { blacklistedPath ->
            folderPath.startsWith(blacklistedPath, ignoreCase = true)
        }
    }
    
    fun clearFolderBlacklist() {
        prefs.edit().remove(KEY_BLACKLISTED_FOLDERS).apply()
        _blacklistedFolders.value = emptyList()
    }
    
    // Bulk operations for better synchronization
    fun removeFolderAndRelatedSongs(folderPath: String, songsInFolder: List<String>) {
        // Remove folder from blacklist
        val currentFolders = _blacklistedFolders.value.toMutableList()
        if (currentFolders.remove(folderPath)) {
            val foldersJson = Gson().toJson(currentFolders)
            prefs.edit().putString(KEY_BLACKLISTED_FOLDERS, foldersJson).apply()
            _blacklistedFolders.value = currentFolders
        }
        
        // Remove related songs from individual blacklist
        val currentSongs = _blacklistedSongs.value.toMutableList()
        var songsRemoved = false
        songsInFolder.forEach { songId ->
            if (currentSongs.remove(songId)) {
                songsRemoved = true
            }
        }
        
        if (songsRemoved) {
            val songsJson = Gson().toJson(currentSongs)
            prefs.edit().putString(KEY_BLACKLISTED_SONGS, songsJson).apply()
            _blacklistedSongs.value = currentSongs
        }
    }
    
    fun addFolderAndOptionalSong(folderPath: String, songId: String?) {
        // Add folder to blacklist
        val currentFolders = _blacklistedFolders.value.toMutableList()
        if (!currentFolders.contains(folderPath)) {
            currentFolders.add(folderPath)
            val foldersJson = Gson().toJson(currentFolders)
            prefs.edit().putString(KEY_BLACKLISTED_FOLDERS, foldersJson).apply()
            _blacklistedFolders.value = currentFolders
        }
        
        // Optionally add song to individual blacklist for immediate synchronization
        songId?.let { id ->
            val currentSongs = _blacklistedSongs.value.toMutableList()
            if (!currentSongs.contains(id)) {
                currentSongs.add(id)
                val songsJson = Gson().toJson(currentSongs)
                prefs.edit().putString(KEY_BLACKLISTED_SONGS, songsJson).apply()
                _blacklistedSongs.value = currentSongs
            }
        }
    }
    
    // Helper method to check if a song would be filtered by current blacklist rules
    fun isEffectivelyBlacklisted(songId: String, songPath: String?): Boolean {
        // Check individual song blacklist
        if (_blacklistedSongs.value.contains(songId)) return true
        
        // Check folder blacklist
        if (songPath != null) {
            return _blacklistedFolders.value.any { folderPath ->
                songPath.startsWith(folderPath, ignoreCase = true)
            }
        }
        
        return false
    }
    
    // Whitelisted Songs Methods
    fun addToWhitelist(songId: String) {
        val currentList = _whitelistedSongs.value.toMutableList()
        if (!currentList.contains(songId)) {
            currentList.add(songId)
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_WHITELISTED_SONGS, json).apply()
            _whitelistedSongs.value = currentList
        }
    }
    
    fun removeFromWhitelist(songId: String) {
        val currentList = _whitelistedSongs.value.toMutableList()
        if (currentList.remove(songId)) {
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_WHITELISTED_SONGS, json).apply()
            _whitelistedSongs.value = currentList
        }
    }
    
    fun isSongWhitelisted(songId: String): Boolean {
        return _whitelistedSongs.value.contains(songId)
    }
    
    fun clearWhitelist() {
        prefs.edit().remove(KEY_WHITELISTED_SONGS).apply()
        _whitelistedSongs.value = emptyList()
    }
    
    // Whitelisted Folders Methods
    fun addFolderToWhitelist(folderPath: String) {
        val currentList = _whitelistedFolders.value.toMutableList()
        if (!currentList.contains(folderPath)) {
            currentList.add(folderPath)
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_WHITELISTED_FOLDERS, json).apply()
            _whitelistedFolders.value = currentList
        }
    }
    
    fun removeFolderFromWhitelist(folderPath: String) {
        val currentList = _whitelistedFolders.value.toMutableList()
        if (currentList.remove(folderPath)) {
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_WHITELISTED_FOLDERS, json).apply()
            _whitelistedFolders.value = currentList
        }
    }
    
    fun isFolderWhitelisted(folderPath: String): Boolean {
        return _whitelistedFolders.value.any { whitelistedPath ->
            folderPath.startsWith(whitelistedPath, ignoreCase = true)
        }
    }
    
    fun clearFolderWhitelist() {
        prefs.edit().remove(KEY_WHITELISTED_FOLDERS).apply()
        _whitelistedFolders.value = emptyList()
    }

    // Pinned Folders Methods (Explorer)
    fun addFolderToPinned(folderPath: String) {
        val currentList = _pinnedFolders.value.toMutableList()
        if (!currentList.contains(folderPath)) {
            currentList.add(folderPath)
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_PINNED_FOLDERS, json).apply()
            _pinnedFolders.value = currentList
        }
    }

    fun removeFolderFromPinned(folderPath: String) {
        val currentList = _pinnedFolders.value.toMutableList()
        if (currentList.remove(folderPath)) {
            val json = Gson().toJson(currentList)
            prefs.edit().putString(KEY_PINNED_FOLDERS, json).apply()
            _pinnedFolders.value = currentList
        }
    }

    fun isFolderPinned(folderPath: String): Boolean {
        return _pinnedFolders.value.contains(folderPath)
    }

    fun clearPinnedFolders() {
        prefs.edit().remove(KEY_PINNED_FOLDERS).apply()
        _pinnedFolders.value = emptyList()
    }
    
    // Helper method to check if a song would be filtered by current whitelist rules
    fun isEffectivelyWhitelisted(songId: String, songPath: String?): Boolean {
        // If no whitelist exists, all songs are effectively whitelisted
        if (_whitelistedSongs.value.isEmpty() && _whitelistedFolders.value.isEmpty()) {
            return true
        }
        
        // Check individual song whitelist
        if (_whitelistedSongs.value.contains(songId)) return true
        
        // Check folder whitelist
        if (songPath != null) {
            return _whitelistedFolders.value.any { folderPath ->
                songPath.startsWith(folderPath, ignoreCase = true)
            }
        }
        
        return false
    }
    
    // Backup and Restore Methods
    fun setLastBackupTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIMESTAMP, timestamp).apply()
        _lastBackupTimestamp.value = timestamp
    }
    
    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
        _autoBackupEnabled.value = enabled
    }
    
    fun setBackupLocation(location: String?) {
        if (location == null) {
            prefs.edit().remove(KEY_BACKUP_LOCATION).apply()
        } else {
            prefs.edit().putString(KEY_BACKUP_LOCATION, location).apply()
        }
        _backupLocation.value = location
    }
    
    /**
     * Creates a complete backup of all app data as JSON
     */
    fun createBackup(): String {
        val backupData = mutableMapOf<String, Any?>()
        val preferencesTypes = mutableMapOf<String, String>()
        
        // Get all preferences
        val allPrefs = prefs.all
        
        // Filter out sensitive or temporary data if needed
        val filteredPrefs = allPrefs.filterKeys { key ->
            // Include all keys for now, but you could exclude sensitive data here
            true
        }
        
        // Store type information for each preference
        filteredPrefs.forEach { (key, value) ->
            preferencesTypes[key] = when (value) {
                is Boolean -> "Boolean"
                is Float -> "Float"
                is Int -> "Int"
                is Long -> "Long"
                is String -> "String"
                is Set<*> -> "StringSet"
                else -> "Unknown"
            }
        }
        
        backupData["preferences"] = filteredPrefs
        backupData["preferences_types"] = preferencesTypes
        backupData["timestamp"] = System.currentTimeMillis()
        backupData["app_version"] = "1.0.0" // You might want to get this dynamically
        backupData["backup_version"] = 2 // Increment version to handle playlist data properly
        
        // Explicitly include playlist and favorite songs data even if already in preferences
        // This ensures they are properly backed up and restored
        try {
            val playlistsJson = prefs.getString(KEY_PLAYLISTS, null)
            val favoriteSongsJson = prefs.getString(KEY_FAVORITE_SONGS, null)
            
            if (playlistsJson != null) {
                backupData["playlists_data"] = playlistsJson
                Log.d("AppSettings", "Including playlists data in backup: ${playlistsJson.length} characters")
            }
            
            if (favoriteSongsJson != null) {
                backupData["favorite_songs_data"] = favoriteSongsJson
                Log.d("AppSettings", "Including favorite songs data in backup: ${favoriteSongsJson.length} characters")
            }
            
            // Also include blacklisted songs and folders
            val blacklistedSongsJson = prefs.getString(KEY_BLACKLISTED_SONGS, null)
            val blacklistedFoldersJson = prefs.getString(KEY_BLACKLISTED_FOLDERS, null)
            
            if (blacklistedSongsJson != null) {
                backupData["blacklisted_songs_data"] = blacklistedSongsJson
            }
            
            if (blacklistedFoldersJson != null) {
                backupData["blacklisted_folders_data"] = blacklistedFoldersJson
            }
            
        } catch (e: Exception) {
            Log.e("AppSettings", "Error including playlist data in backup", e)
        }
        
        return Gson().toJson(backupData)
    }
    
    /**
     * Restores app data from a backup JSON string
     */
    fun restoreFromBackup(backupJson: String): Boolean {
        return try {
            Log.d("AppSettings", "Attempting to restore from backup...")
            val backupData = Gson().fromJson(backupJson, Map::class.java) as Map<String, Any?>
            val preferences = backupData["preferences"] as? Map<String, Any?> ?: return false
            val preferencesTypes = backupData["preferences_types"] as? Map<String, String> ?: emptyMap()
            val backupVersion = (backupData["backup_version"] as? Double)?.toInt() ?: 1
            
            Log.d("AppSettings", "Backup version: $backupVersion")
            
            // Clear existing preferences (optional - you might want to merge instead)
            val editor = prefs.edit()
            
            // Restore all preferences with proper type handling
            preferences.forEach { (key, value) ->
                val originalType = preferencesTypes[key]
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> {
                        // Check if this should be a Long or Int based on original type
                        when (originalType) {
                            "Long" -> editor.putLong(key, value.toLong())
                            "Int" -> editor.putInt(key, value.toInt())
                            else -> editor.putFloat(key, value)
                        }
                    }
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is String -> editor.putString(key, value)
                    is Double -> {
                        // JSON deserializes numbers as Double, convert based on original type
                        when (originalType) {
                            "Float" -> editor.putFloat(key, value.toFloat())
                            "Long" -> editor.putLong(key, value.toLong())
                            "Int" -> editor.putInt(key, value.toInt())
                            else -> editor.putFloat(key, value.toFloat()) // Default fallback
                        }
                    }
                    // Handle any other types as needed
                }
            }
            
            // Handle backup version 2 and above - explicit playlist data restoration
            if (backupVersion >= 2) {
                Log.d("AppSettings", "Restoring playlist data from backup version $backupVersion")
                
                // Restore playlists data explicitly
                val playlistsData = backupData["playlists_data"] as? String
                if (playlistsData != null) {
                    editor.putString(KEY_PLAYLISTS, playlistsData)
                    Log.d("AppSettings", "Restored playlists data: ${playlistsData.length} characters")
                }
                
                // Restore favorite songs data explicitly
                val favoriteSongsData = backupData["favorite_songs_data"] as? String
                if (favoriteSongsData != null) {
                    editor.putString(KEY_FAVORITE_SONGS, favoriteSongsData)
                    Log.d("AppSettings", "Restored favorite songs data: ${favoriteSongsData.length} characters")
                }
                
                // Restore blacklisted songs and folders
                val blacklistedSongsData = backupData["blacklisted_songs_data"] as? String
                if (blacklistedSongsData != null) {
                    editor.putString(KEY_BLACKLISTED_SONGS, blacklistedSongsData)
                    Log.d("AppSettings", "Restored blacklisted songs data")
                }
                
                val blacklistedFoldersData = backupData["blacklisted_folders_data"] as? String
                if (blacklistedFoldersData != null) {
                    editor.putString(KEY_BLACKLISTED_FOLDERS, blacklistedFoldersData)
                    Log.d("AppSettings", "Restored blacklisted folders data")
                }
            } else {
                Log.d("AppSettings", "Backup version $backupVersion - using preferences-based restoration")
            }
            
            editor.apply()
            
            // Refresh all StateFlows to reflect the restored data
            refreshAllStateFlows()
            
            Log.d("AppSettings", "Backup restoration completed successfully")
            true
        } catch (e: Exception) {
            Log.e("AppSettings", "Failed to restore backup", e)
            false
        }
    }
    
    /**
     * Safely get a Long value from SharedPreferences, handling JSON restore type casting issues
     */
    private fun safeLong(key: String, defaultValue: Long): Long {
        return try {
            prefs.getLong(key, defaultValue)
        } catch (e: ClassCastException) {
            // Handle case where value was stored as Float/Double from JSON restore
            try {
                prefs.getFloat(key, defaultValue.toFloat()).toLong()
            } catch (e2: Exception) {
                // If all else fails, use default and fix the stored value
                prefs.edit().putLong(key, defaultValue).apply()
                defaultValue
            }
        }
    }
    
    /**
     * Helper function to safely set values with validation
     */
    private fun safeSetValue(key: String, value: Any, validator: ((Any) -> Boolean)? = null): Boolean {
        return try {
            if (validator != null && !validator(value)) {
                Log.w("AppSettings", "Invalid value for key $key: $value")
                return false
            }
            
            val editor = prefs.edit()
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                else -> {
                    Log.w("AppSettings", "Unsupported value type for key $key")
                    return false
                }
            }
            
            editor.apply()
            true
        } catch (e: Exception) {
            Log.e("AppSettings", "Error setting preference $key", e)
            false
        }
    }
    
    /**
     * Validates cache size value
     */
    private fun isValidCacheSize(size: Long): Boolean {
        val minSize = 50L * 1024 * 1024 // 50MB minimum
        val maxSize = 10L * 1024 * 1024 * 1024 // 10GB maximum
        return size in minSize..maxSize
    }
    
    /**
     * Validates crossfade duration
     */
    private fun isValidCrossfadeDuration(duration: Float): Boolean {
        return duration in 0.1f..10.0f
    }
    
    /**
     * Validates update check interval
     */
    private fun isValidUpdateInterval(hours: Int): Boolean {
        return hours in 1..168 // 1 hour to 1 week
    }
    
    /**
     * Clears app cache if the clear cache on exit setting is enabled
     * This should be called when the app is being destroyed
     * 
     * @param context Application context
     * @param musicRepository Optional MusicRepository instance to clear in-memory caches
     */
    suspend fun performCacheCleanupOnExit(
        context: Context, 
        musicRepository: chromahub.rhythm.app.data.MusicRepository? = null
    ) {
        if (_clearCacheOnExit.value) {
            try {
                Log.d("AppSettings", "Performing cache cleanup on app exit...")
                
                // Clear file system caches
                chromahub.rhythm.app.util.CacheManager.clearAllCache(context)
                
                // Clear in-memory caches from MusicRepository
                musicRepository?.clearInMemoryCaches()
                
                Log.d("AppSettings", "Cache cleanup completed successfully")
            } catch (e: Exception) {
                Log.e("AppSettings", "Error during cache cleanup on exit", e)
            }
        }
    }
    
    /**
     * Refreshes all StateFlows to reflect current SharedPreferences values
     */
    private fun refreshAllStateFlows() {
        // Playback Settings
        _highQualityAudio.value = prefs.getBoolean(KEY_HIGH_QUALITY_AUDIO, true)
        _gaplessPlayback.value = prefs.getBoolean(KEY_GAPLESS_PLAYBACK, true)
        _crossfade.value = prefs.getBoolean(KEY_CROSSFADE, false)
        _crossfadeDuration.value = prefs.getFloat(KEY_CROSSFADE_DURATION, 2f)
        _audioNormalization.value = prefs.getBoolean(KEY_AUDIO_NORMALIZATION, true)
        _replayGain.value = prefs.getBoolean(KEY_REPLAY_GAIN, false)
        
        // Theme Settings
        _useSystemTheme.value = prefs.getBoolean(KEY_USE_SYSTEM_THEME, true)
        _darkMode.value = prefs.getBoolean(KEY_DARK_MODE, true)
        _useDynamicColors.value = prefs.getBoolean(KEY_USE_DYNAMIC_COLORS, false)
        
        // Library Settings
        _albumViewType.value = AlbumViewType.valueOf(prefs.getString(KEY_ALBUM_VIEW_TYPE, AlbumViewType.GRID.name) ?: AlbumViewType.GRID.name)
        _artistViewType.value = ArtistViewType.valueOf(prefs.getString(KEY_ARTIST_VIEW_TYPE, ArtistViewType.GRID.name) ?: ArtistViewType.GRID.name)
        _albumSortOrder.value = prefs.getString(KEY_ALBUM_SORT_ORDER, "TRACK_NUMBER") ?: "TRACK_NUMBER"
        _artistCollaborationMode.value = prefs.getBoolean(KEY_ARTIST_COLLABORATION_MODE, false)
        
        // Audio Device Settings
        _lastAudioDevice.value = prefs.getString(KEY_LAST_AUDIO_DEVICE, null)
        _autoConnectDevice.value = prefs.getBoolean(KEY_AUTO_CONNECT_DEVICE, true)
        _useSystemVolume.value = prefs.getBoolean(KEY_USE_SYSTEM_VOLUME, false)
        
        // Cache Settings
        _maxCacheSize.value = safeLong(KEY_MAX_CACHE_SIZE, 500L * 1024L * 1024L)
        _clearCacheOnExit.value = prefs.getBoolean(KEY_CLEAR_CACHE_ON_EXIT, false)
        
        // Other settings
        _showLyrics.value = prefs.getBoolean(KEY_SHOW_LYRICS, true)
        _onlineOnlyLyrics.value = prefs.getBoolean(KEY_ONLINE_ONLY_LYRICS, true)
        _searchHistory.value = prefs.getString(KEY_SEARCH_HISTORY, null)
        _playlists.value = prefs.getString(KEY_PLAYLISTS, null)
        _favoriteSongs.value = prefs.getString(KEY_FAVORITE_SONGS, null)
        
        // User Statistics
        _listeningTime.value = safeLong(KEY_LISTENING_TIME, 0L)
        _songsPlayed.value = prefs.getInt(KEY_SONGS_PLAYED, 0)
        _uniqueArtists.value = prefs.getInt(KEY_UNIQUE_ARTISTS, 0)
        
        // Enhanced User Preferences
        _favoriteGenres.value = try {
            val json = prefs.getString(KEY_FAVORITE_GENRES, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type) else emptyMap()
        } catch (e: Exception) { emptyMap() }
        
        _dailyListeningStats.value = try {
            val json = prefs.getString(KEY_DAILY_LISTENING_STATS, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<Map<String, Long>>() {}.type) else emptyMap()
        } catch (e: Exception) { emptyMap() }
        
        _weeklyTopArtists.value = try {
            val json = prefs.getString(KEY_WEEKLY_TOP_ARTISTS, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type) else emptyMap()
        } catch (e: Exception) { emptyMap() }
        
        _moodPreferences.value = try {
            val json = prefs.getString(KEY_MOOD_PREFERENCES, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<Map<String, List<String>>>() {}.type) else emptyMap()
        } catch (e: Exception) { emptyMap() }
        
        _songPlayCounts.value = try {
            val json = prefs.getString(KEY_SONG_PLAY_COUNTS, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<Map<String, Int>>() {}.type) else emptyMap()
        } catch (e: Exception) { emptyMap() }
        
        // Recently Played
        _recentlyPlayed.value = try {
            val json = prefs.getString(KEY_RECENTLY_PLAYED, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) else emptyList()
        } catch (e: Exception) { emptyList() }
        
        _lastPlayedTimestamp.value = safeLong(KEY_LAST_PLAYED_TIMESTAMP, 0L)
        
        // API Enable/Disable States
        _deezerApiEnabled.value = prefs.getBoolean(KEY_DEEZER_API_ENABLED, true)
        _canvasApiEnabled.value = prefs.getBoolean(KEY_CANVAS_API_ENABLED, true)
        _lrclibApiEnabled.value = prefs.getBoolean(KEY_LRCLIB_API_ENABLED, true)
        _ytMusicApiEnabled.value = prefs.getBoolean(KEY_YTMUSIC_API_ENABLED, true)
        _spotifyApiEnabled.value = prefs.getBoolean(KEY_SPOTIFY_API_ENABLED, false)
        _spotifyClientId.value = prefs.getString(KEY_SPOTIFY_CLIENT_ID, "") ?: ""
        _spotifyClientSecret.value = prefs.getString(KEY_SPOTIFY_CLIENT_SECRET, "") ?: ""
        
        // App Updates
        _autoCheckForUpdates.value = prefs.getBoolean(KEY_AUTO_CHECK_FOR_UPDATES, true)
        _updateChannel.value = prefs.getString(KEY_UPDATE_CHANNEL, "stable") ?: "stable"
        _updatesEnabled.value = prefs.getBoolean(KEY_UPDATES_ENABLED, true)
        _mediaScanMode.value = prefs.getString(KEY_MEDIA_SCAN_MODE, "blacklist") ?: "blacklist"
        _updateCheckIntervalHours.value = prefs.getInt(KEY_UPDATE_CHECK_INTERVAL_HOURS, 24)
        
        // Beta Program
        _hasShownBetaPopup.value = prefs.getBoolean(KEY_HAS_SHOWN_BETA_POPUP, false)
        
        // Crash Reporting
        _lastCrashLog.value = prefs.getString(KEY_LAST_CRASH_LOG, null)
        _crashLogHistory.value = try {
            val json = prefs.getString(KEY_CRASH_LOG_HISTORY, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<List<CrashLogEntry>>() {}.type) else emptyList()
        } catch (e: Exception) { emptyList() }
        
        // Other settings
        _hapticFeedbackEnabled.value = prefs.getBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, true)
        _onboardingCompleted.value = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        _initialMediaScanCompleted.value = prefs.getBoolean(KEY_INITIAL_MEDIA_SCAN_COMPLETED, false)
        
        // Blacklisted items
        _blacklistedSongs.value = try {
            val json = prefs.getString(KEY_BLACKLISTED_SONGS, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) else emptyList()
        } catch (e: Exception) { emptyList() }
        
        _blacklistedFolders.value = try {
            val json = prefs.getString(KEY_BLACKLISTED_FOLDERS, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) else emptyList()
        } catch (e: Exception) { emptyList() }
        
        // Backup settings
        _lastBackupTimestamp.value = safeLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
        _autoBackupEnabled.value = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false)
        _backupLocation.value = prefs.getString(KEY_BACKUP_LOCATION, null)
        
        // Sleep Timer settings
        _sleepTimerActive.value = prefs.getBoolean(KEY_SLEEP_TIMER_ACTIVE, false)
        _sleepTimerRemainingSeconds.value = prefs.getLong(KEY_SLEEP_TIMER_REMAINING_SECONDS, 0L)
        _sleepTimerAction.value = prefs.getString(KEY_SLEEP_TIMER_ACTION, "FADE_OUT") ?: "FADE_OUT"

        // Pinned Folders
        _pinnedFolders.value = try {
            val json = prefs.getString(KEY_PINNED_FOLDERS, null)
            if (json != null) Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) else emptyList()
        } catch (e: Exception) { emptyList() }
    }
}
