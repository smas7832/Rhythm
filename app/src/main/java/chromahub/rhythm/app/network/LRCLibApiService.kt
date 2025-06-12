package chromahub.rhythm.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service interface for LRCLib lyrics API
 */
interface LRCLibApiService {
    /**
     * Search lyrics records by query or track/artist details.
     * This endpoint returns a JSON array of matching records.
     * We default to using the `q` parameter for simplicity.
     */
    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String? = null,
        @Query("track_name") trackName: String? = null,
        @Query("artist_name") artistName: String? = null,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Int? = null
    ): List<LrcLibLyrics>
}

/**
 * Data class mapping of LRCLib search result
 */
data class LrcLibLyrics(
    @SerializedName("id") val id: Int,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("albumName") val albumName: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("instrumental") val instrumental: Boolean?,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
)
