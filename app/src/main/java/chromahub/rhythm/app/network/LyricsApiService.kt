package chromahub.rhythm.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service to fetch lyrics from an external API
 */
interface LyricsApiService {
    @GET("lyrics")
    suspend fun getLyrics(
        @Query("artist") artistName: String,
        @Query("title") songTitle: String
    ): LyricsResponse
}

data class LyricsResponse(
    val lyrics: String?,
    val error: String?
) {
    val isSuccessful: Boolean
        get() = lyrics != null && error == null
} 