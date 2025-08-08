package chromahub.rhythm.app.data

import android.util.Log
import chromahub.rhythm.app.network.NetworkClient
import chromahub.rhythm.app.network.SpotifyCanvas
import chromahub.rhythm.app.network.SpotifyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
class CanvasRepository(private val context: android.content.Context, private val appSettings: AppSettings) {
    companion object {
        private const val TAG = "CanvasRepository"
    }
    
    private val canvasApiService = NetworkClient.canvasApiService
    private val spotifyService = SpotifyService(appSettings)
    
    // Cache for canvas URLs to avoid redundant API calls
    private val canvasCache = mutableMapOf<String, CanvasData?>()
    // Cache for Spotify track ID lookups to avoid redundant searches
    private val trackIdCache = mutableMapOf<String, String?>()
    
    /**
     * Fetches canvas video for a given track using Spotify track ID
     */
    suspend fun fetchCanvasForTrack(spotifyTrackId: String?): CanvasData? = withContext(Dispatchers.IO) {
        if (spotifyTrackId.isNullOrBlank()) {
            Log.d(TAG, "No Spotify track ID provided")
            return@withContext null
        }
        
        // Check cache first
        val cached = canvasCache[spotifyTrackId]
        if (cached != null) {
            Log.d(TAG, "Using cached canvas for track: $spotifyTrackId")
            return@withContext cached
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
                
                // Cache the result
                canvasCache[spotifyTrackId] = canvasData
                Log.d(TAG, "Found canvas for track: $spotifyTrackId - ${canvas.canvasUrl}")
                return@withContext canvasData
            } else {
                Log.d(TAG, "No canvas found for track: $spotifyTrackId")
                canvasCache[spotifyTrackId] = null
                return@withContext null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch canvas for track $spotifyTrackId: ${e.message}")
            Log.w(TAG, "Exception details: ${e.javaClass.simpleName}")
            e.printStackTrace()
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
        
        // Check cache first
        val cached = trackIdCache[cacheKey]
        if (cached != null) {
            Log.d(TAG, "Using cached Spotify ID for: $cacheKey")
            return@withContext cached
        }
        
        // Search Spotify for the track
        val spotifyTrack = spotifyService.searchTrack(artist, title)
        val trackId = spotifyTrack?.id
        
        // Cache the result (including null results to avoid repeat searches)
        trackIdCache[cacheKey] = trackId
        
        return@withContext trackId
    }
    
    /**
     * Clears the canvas cache
     */
    fun clearCache() {
        canvasCache.clear()
        trackIdCache.clear()
        Log.d(TAG, "Canvas and track ID caches cleared")
    }
    
    /**
     * Tests the Spotify API configuration
     */
    suspend fun testSpotifyApiConfiguration(): Pair<Boolean, String> {
        return spotifyService.testApiConfiguration()
    }
}
