package chromahub.rhythm.app.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName


/**
 * Minimal Retrofit interface for Last.fm API to fetch artist info including images.
 * Docs: https://www.last.fm/api/show/artist.getInfo
 */
interface LastFmApiService {
    @GET("2.0/")
    suspend fun getArtistInfo(
        @Query("method") method: String = "artist.getinfo",
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("autocorrect") autocorrect: Int = 1
    ): LastFmArtistInfoResponse
}

// ---------- DTOs ----------

data class LastFmArtistInfoResponse(
    val artist: LastFmArtist? = null
)

data class LastFmArtist(
    val name: String? = null,
    val image: List<LastFmImage> = emptyList()
)



data class LastFmImage(
    @SerializedName("#text") val url: String = "",
    val size: String = ""
)
