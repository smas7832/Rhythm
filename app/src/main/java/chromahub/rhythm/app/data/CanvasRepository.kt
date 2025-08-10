package chromahub.rhythm.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import chromahub.rhythm.app.network.NetworkClient
import chromahub.rhythm.app.network.SpotifyCanvas
import chromahub.rhythm.app.network.SpotifyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Data class to represent canvas video information
 */
data class CanvasData(
    val videoUrl: String,
    val trackId: String,
    val artistName: String? = null,
    val artistImageUrl: String? = null
)

/**
 * Extension functions for MusicRepository to handle Spotify Canvas
 */
class CanvasRepository(private val context: Context, private val appSettings: AppSettings) {
    companion object {
        private const val TAG = "CanvasRepository"
        private const val PREFS_NAME = "canvas_cache"
        private const val CACHE_EXPIRY_HOURS = 24
    }
    
    private val canvasApiService = NetworkClient.canvasApiService
    private val spotifyService = SpotifyService(appSettings)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // In-memory cache for canvas URLs to avoid redundant API calls
    private val canvasCache = mutableMapOf<String, CanvasData?>()
    // In-memory cache for Spotify track ID lookups to avoid redundant searches
    private val trackIdCache = mutableMapOf<String, String?>()
    
    init {
        // Load cached data from SharedPreferences on initialization
        loadPersistentCache()
    }
    
    
    /**
     * Load persistent cache from SharedPreferences
     */
    private fun loadPersistentCache() {
        try {
            // Load canvas cache
            prefs.getString("canvas_cache", null)?.let { cacheJson ->
                val type = object : TypeToken<Map<String, CanvasData?>>() {}.type
                val cacheMap: Map<String, CanvasData?> = gson.fromJson(cacheJson, type)
                canvasCache.putAll(cacheMap)
                Log.d(TAG, "Loaded ${cacheMap.size} canvas entries from persistent cache")
            }
            
            // Load track ID cache
            prefs.getString("track_id_cache", null)?.let { cacheJson ->
                val type = object : TypeToken<Map<String, String?>>() {}.type
                val cacheMap: Map<String, String?> = gson.fromJson(cacheJson, type)
                trackIdCache.putAll(cacheMap)
                Log.d(TAG, "Loaded ${cacheMap.size} track ID entries from persistent cache")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load persistent cache: ${e.message}")
        }
    }
    
    /**
     * Save cache to SharedPreferences
     */
    private fun savePersistentCache() {
        try {
            prefs.edit().apply {
                putString("canvas_cache", gson.toJson(canvasCache.toMap()))
                putString("track_id_cache", gson.toJson(trackIdCache.toMap()))
                putLong("last_cache_update", System.currentTimeMillis())
                apply()
            }
            Log.d(TAG, "Saved cache to persistent storage")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save persistent cache: ${e.message}")
        }
    }
    
    /**
     * Check if cache is expired
     */
    private fun isCacheExpired(): Boolean {
        val lastUpdate = prefs.getLong("last_cache_update", 0)
        val expiryTime = CACHE_EXPIRY_HOURS * 60 * 60 * 1000L // Convert hours to milliseconds
        return System.currentTimeMillis() - lastUpdate > expiryTime
    }
    
    /**
     * Fetches canvas video for a given track using Spotify track ID
     */
    /**
     * Fetches canvas video for a given track using Spotify track ID
     */
    suspend fun fetchCanvasForTrack(spotifyTrackId: String?): CanvasData? = withContext(Dispatchers.IO) {
        if (spotifyTrackId.isNullOrBlank()) {
            Log.d(TAG, "No Spotify track ID provided")
            return@withContext null
        }
        
        // Check in-memory cache first
        val cached = canvasCache[spotifyTrackId]
        if (cached != null) {
            Log.d(TAG, "Using cached canvas for track: $spotifyTrackId")
            return@withContext cached
        }
        
        // Check if we have a cached null result (to avoid repeat API calls for tracks without canvas)
        if (canvasCache.containsKey(spotifyTrackId)) {
            Log.d(TAG, "Track $spotifyTrackId previously had no canvas (cached)")
            return@withContext null
        }
        
        // Check if Canvas API is enabled
        if (!NetworkClient.isCanvasApiEnabled()) {
            Log.d(TAG, "Canvas API is disabled")
            return@withContext null
        }

        try {
            Log.d(TAG, "Making API call to fetch canvas for track: $spotifyTrackId")
            val response = canvasApiService.getCanvas(spotifyTrackId)
            Log.d(TAG, "Canvas API response received: data=${response.data}")
            
            val canvas = response.data?.canvases?.firstOrNull()
            Log.d(TAG, "First canvas from response: $canvas")
            
            if (canvas != null && canvas.canvasUrl.isNotBlank()) {
                val canvasData = CanvasData(
                    videoUrl = canvas.canvasUrl,
                    trackId = spotifyTrackId,
                    artistName = canvas.artist?.artistName,
                    artistImageUrl = canvas.artist?.artistImgUrl
                )
                
                // Cache the result both in-memory and persistently
                canvasCache[spotifyTrackId] = canvasData
                savePersistentCache()
                Log.d(TAG, "Found and cached canvas for track: $spotifyTrackId - ${canvas.canvasUrl}")
                return@withContext canvasData
            } else {
                Log.d(TAG, "No canvas found for track: $spotifyTrackId")
                // Cache the null result to avoid repeat API calls
                canvasCache[spotifyTrackId] = null
                savePersistentCache()
                return@withContext null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch canvas for track $spotifyTrackId: ${e.message}")
            Log.w(TAG, "Exception details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            // Cache null result to avoid immediate retry
            canvasCache[spotifyTrackId] = null
            return@withContext null
        }
    }
    
    /**
     * Fetches canvas for a track by searching for its Spotify ID first
     */
    suspend fun fetchCanvasForSong(artist: String, title: String): CanvasData? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Fetching canvas for: $artist - $title")
        
        // First get the Spotify track ID
        val spotifyTrackId = getSpotifyTrackId(artist, title)
        if (spotifyTrackId == null) {
            Log.d(TAG, "No Spotify ID found for: $artist - $title")
            return@withContext null
        }
        
        return@withContext fetchCanvasForTrack(spotifyTrackId)
    }
    
    /**
     * Gets Spotify track ID for a song, using cache if available
     */
    private suspend fun getSpotifyTrackId(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        val cacheKey = "$artist - $title"
        
        // Check in-memory cache first
        val cached = trackIdCache[cacheKey]
        if (cached != null) {
            Log.d(TAG, "Using cached Spotify ID for: $cacheKey")
            return@withContext cached
        }
        
        // Check if we have a cached null result
        if (trackIdCache.containsKey(cacheKey)) {
            Log.d(TAG, "No Spotify ID available for: $cacheKey (cached)")
            return@withContext null
        }
        
        // Search Spotify for the track
        val spotifyTrack = spotifyService.searchTrack(artist, title)
        val trackId = spotifyTrack?.id
        
        // Cache the result (including null results to avoid repeat searches)
        trackIdCache[cacheKey] = trackId
        savePersistentCache()
        
        return@withContext trackId
    }
    
    /**
     * Clears the canvas cache (both in-memory and persistent)
     */
    fun clearCache() {
        canvasCache.clear()
        trackIdCache.clear()
        prefs.edit().clear().apply()
        Log.d(TAG, "Canvas and track ID caches cleared (both in-memory and persistent)")
    }
    
    /**
     * Clear expired cache entries
     */
    fun clearExpiredCache() {
        if (isCacheExpired()) {
            Log.d(TAG, "Cache expired, clearing all entries")
            clearCache()
        }
    }
    
    /**
     * Optimize cache by implementing LRU strategy and memory management
     */
    fun optimizeCache() {
        try {
            val maxCacheSize = 100 // Maximum number of cached entries
            val currentSize = canvasCache.size + trackIdCache.size
            
            if (currentSize > maxCacheSize) {
                Log.d(TAG, "Cache size ($currentSize) exceeds maximum ($maxCacheSize), optimizing...")
                
                // Remove oldest entries (simple LRU implementation)
                val entriesToRemove = currentSize - maxCacheSize + 10 // Remove extra for buffer
                
                // Clear some canvas cache entries (keep successful lookups, remove nulls first)
                val nullEntries = canvasCache.filter { it.value == null }.keys.take(entriesToRemove / 2)
                nullEntries.forEach { canvasCache.remove(it) }
                
                // Clear some track ID cache entries
                val trackIdNullEntries = trackIdCache.filter { it.value == null }.keys.take(entriesToRemove / 2)
                trackIdNullEntries.forEach { trackIdCache.remove(it) }
                
                // Save optimized cache
                savePersistentCache()
                
                Log.d(TAG, "Cache optimized: removed $entriesToRemove entries, new size: ${canvasCache.size + trackIdCache.size}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to optimize cache: ${e.message}")
        }
    }
    
    /**
     * Get cache statistics for debugging
     */
    fun getCacheStats(): String {
        val canvasCount = canvasCache.size
        val trackIdCount = trackIdCache.size
        val lastUpdate = prefs.getLong("last_cache_update", 0)
        val isExpired = isCacheExpired()
        return "Canvas entries: $canvasCount, Track ID entries: $trackIdCount, Last update: $lastUpdate, Expired: $isExpired"
    }
    
    /**
     * Preload canvas for upcoming songs in queue for smoother experience
     */
    suspend fun preloadCanvasForQueue(songs: List<Pair<String, String>>) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Preloading canvas for ${songs.size} songs")
        songs.take(3).forEach { (artist, title) -> // Limit to 3 upcoming songs
            try {
                fetchCanvasForSong(artist, title)
                Log.d(TAG, "Preloaded canvas for: $artist - $title")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to preload canvas for $artist - $title: ${e.message}")
            }
        }
    }
    
    /**
     * Get cached canvas without making API calls - improved logic
     */
    fun getCachedCanvas(artist: String, title: String): CanvasData? {
        val trackIdKey = "$artist - $title"
        
        // First check if we have a cached track ID
        val trackId = trackIdCache[trackIdKey]
        if (trackId != null) {
            // We have a track ID, check for canvas data
            val canvasData = canvasCache[trackId]
            if (canvasData != null) {
                Log.d(TAG, "Found cached canvas for $trackIdKey: ${canvasData.videoUrl}")
                return canvasData
            }
        }
        
        // Alternative approach: check all canvas entries for matching artist/title
        canvasCache.entries.forEach { (cachedTrackId, canvasData) ->
            canvasData?.let { canvas ->
                if (canvas.artistName?.equals(artist, ignoreCase = true) == true) {
                    // Found a potential match, cache the trackId mapping for future use
                    trackIdCache[trackIdKey] = cachedTrackId
                    savePersistentCache()
                    Log.d(TAG, "Found canvas via artist match for $trackIdKey: ${canvas.videoUrl}")
                    return canvas
                }
            }
        }
        
        Log.d(TAG, "No cached canvas found for $trackIdKey")
        return null
    }
    
    /**
     * Check if canvas is available in cache
     */
    fun hasCanvasInCache(artist: String, title: String): Boolean {
        return getCachedCanvas(artist, title) != null
    }
    
    /**
     * Invalidate cache for a specific song to force refresh
     */
    fun invalidateCanvasCache(artist: String, title: String) {
        val trackIdKey = "$artist - $title"
        val trackId = trackIdCache[trackIdKey]
        if (trackId != null) {
            canvasCache.remove(trackId)
            Log.d(TAG, "Invalidated canvas cache for $trackIdKey")
            savePersistentCache()
        }
    }
    
    /**
     * Retry fetching canvas for a song (clears any cached null results first)
     */
    suspend fun retryCanvasForSong(artist: String, title: String): CanvasData? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Retrying canvas fetch for: $artist - $title")
        
        // Clear any cached null results to force a fresh API call
        invalidateCanvasCache(artist, title)
        
        // Also clear any null track ID cache
        val trackIdKey = "$artist - $title"
        if (trackIdCache[trackIdKey] == null) {
            trackIdCache.remove(trackIdKey)
        }
        
        // Now fetch fresh from API
        return@withContext fetchCanvasForSong(artist, title)
    }
    
    /**
     * Tests the Spotify API configuration
     */
    suspend fun testSpotifyApiConfiguration(): Pair<Boolean, String> {
        return spotifyService.testApiConfiguration()
    }
}
