package chromahub.rhythm.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApiService {
    @GET("?method=artist.getinfo&format=json")
    suspend fun getArtistInfo(
        @Query("artist") artistName: String,
        @Query("api_key") apiKey: String
    ): ArtistResponse

    @GET("?method=album.getinfo&format=json")
    suspend fun getAlbumInfo(
        @Query("artist") artistName: String,
        @Query("album") albumName: String,
        @Query("api_key") apiKey: String
    ): AlbumResponse
}

data class ArtistResponse(
    val artist: ArtistInfo
)

data class ArtistInfo(
    val name: String,
    val mbid: String?,
    val url: String,
    val image: List<Image>
)

data class AlbumResponse(
    val album: AlbumInfo
)

data class AlbumInfo(
    val name: String,
    val artist: String,
    val mbid: String?,
    val url: String,
    val image: List<Image>
)

data class Image(
    @SerializedName("#text")
    val text: String,
    val size: String
) {
    val url: String
        get() = text
} 