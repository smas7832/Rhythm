package chromahub.rhythm.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service interface for Deezer API
 * Deezer provides a free public API for searching artists and getting their images
 * API Documentation: https://developers.deezer.com/api
 */
interface DeezerApiService {
    @GET("search/artist")
    suspend fun searchArtists(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1
    ): DeezerSearchResponse
}

// ---------- DTOs ----------

data class DeezerSearchResponse(
    val data: List<DeezerArtist> = emptyList(),
    val total: Int = 0,
    val next: String? = null
)

data class DeezerArtist(
    val id: Long,
    val name: String,
    val link: String,
    val share: String? = null,
    val picture: String? = null,
    @SerializedName("picture_small") val pictureSmall: String? = null,
    @SerializedName("picture_medium") val pictureMedium: String? = null,
    @SerializedName("picture_big") val pictureBig: String? = null,
    @SerializedName("picture_xl") val pictureXl: String? = null,
    @SerializedName("nb_album") val nbAlbum: Int = 0,
    @SerializedName("nb_fan") val nbFan: Int = 0,
    val radio: Boolean = false,
    val tracklist: String? = null,
    val type: String = "artist"
)
