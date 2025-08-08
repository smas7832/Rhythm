package chromahub.rhythm.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service interface for Spotify Canvas API
 * Fetches canvas video data (looping visual videos) for Spotify tracks
 */
interface SpotifyCanvasApiService {
    @GET("canvas")
    suspend fun getCanvas(@Query("id") trackId: String): SpotifyCanvasResponse
}

// ---------- DTOs ----------

data class SpotifyCanvasResponse(
    val data: SpotifyCanvasData?
)

data class SpotifyCanvasData(
    @SerializedName("canvasesList") val canvases: List<SpotifyCanvas> = emptyList()
)

data class SpotifyCanvas(
    val id: String,
    @SerializedName("canvasUrl") val canvasUrl: String,
    @SerializedName("trackUri") val trackUri: String,
    val artist: SpotifyCanvasArtist?,
    @SerializedName("otherId") val otherId: String,
    @SerializedName("canvasUri") val canvasUri: String
)

data class SpotifyCanvasArtist(
    @SerializedName("artistUri") val artistUri: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("artistImgUrl") val artistImgUrl: String
)
