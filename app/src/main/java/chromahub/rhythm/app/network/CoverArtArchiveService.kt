package chromahub.rhythm.app.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Very small Retrofit interface for the Cover Art Archive.
 * For artists, endpoint is https://coverartarchive.org/artist/{mbid}
 */
interface CoverArtArchiveService {
    @GET("artist/{mbid}")
    suspend fun getArtistImages(@Path("mbid") mbid: String): CoverArtArchiveArtistResponse
}

// ---------------- DTOs -----------------

data class CoverArtArchiveArtistResponse(
    val images: List<CAAImage> = emptyList()
)

data class CAAImage(
    val image: String,
    val front: Boolean? = null,
    val back: Boolean? = null
)
