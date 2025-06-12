package chromahub.rhythm.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Service interface for Spotify RapidAPI
 */
interface SpotifyApiService {
    @Headers(
        "X-RapidAPI-Key: acd7746756msh38770eb0ec2ea68p15c583jsn5ac26c448f24",
        "X-RapidAPI-Host: spotify23.p.rapidapi.com"
    )
    @GET("search/")
    suspend fun searchArtists(
        @Query("q") query: String,
        @Query("type") type: String = "artist",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1,
        @Query("numberOfTopResults") numberOfTopResults: Int = 1
    ): SearchResponse

    @Headers(
        "X-RapidAPI-Key: acd7746756msh38770eb0ec2ea68p15c583jsn5ac26c448f24",
        "X-RapidAPI-Host: spotify23.p.rapidapi.com"
    )
    @GET("search/")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1,
        @Query("numberOfTopResults") numberOfTopResults: Int = 1
    ): TrackSearchResponse

    @Headers(
        "X-RapidAPI-Key: acd7746756msh38770eb0ec2ea68p15c583jsn5ac26c448f24",
        "X-RapidAPI-Host: spotify23.p.rapidapi.com"
    )
    @GET("track_lyrics/")
    suspend fun getTrackLyrics(@Query("id") trackId: String): LyricsResponse

    @Headers(
        "X-RapidAPI-Key: acd7746756msh38770eb0ec2ea68p15c583jsn5ac26c448f24",
        "X-RapidAPI-Host: spotify23.p.rapidapi.com"
    )
    @GET("artist_overview/")
    suspend fun getArtistOverview(@Query("id") artistId: String): ArtistOverviewResponse
}

data class SearchResponse(
    @SerializedName("artists") val artists: Artists
)

data class Artists(
    @SerializedName("items") val items: List<SpotifyArtist>
)

data class SpotifyArtist(
    @SerializedName("data") val data: ArtistData
)

data class ArtistData(
    @SerializedName("uri") val uri: String,
    @SerializedName("profile") val profile: ArtistProfile,
    @SerializedName("visuals") val visuals: ArtistVisuals
)

data class ArtistProfile(
    @SerializedName("name") val name: String
)

data class ArtistVisuals(
    @SerializedName("avatarImage") val avatarImage: SpotifyImage?
)

data class SpotifyImage(
    @SerializedName("sources") val sources: List<ImageSource>
)

data class ImageSource(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?
)

data class TrackSearchResponse(
    @SerializedName("tracks") val tracks: Tracks
)

data class Tracks(
    @SerializedName("items") val items: List<SpotifyTrack>
)

data class SpotifyTrack(
    @SerializedName("data") val data: TrackData
)

data class TrackData(
    @SerializedName("uri") val uri: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("albumOfTrack") val album: AlbumData,
    @SerializedName("artists") val artists: Artists
)

data class AlbumData(
    @SerializedName("name") val name: String,
    @SerializedName("coverArt") val coverArt: SpotifyImage?
)

data class LyricsResponse(
    @SerializedName("lyrics") val lyrics: Lyrics
)

data class Lyrics(
    @SerializedName("syncType") val syncType: String,
    @SerializedName("lines") val lines: List<LyricLine>
)

data class LyricLine(
    @SerializedName("words") val words: String,
    @SerializedName("startTimeMs") val startTimeMs: String?,
    @SerializedName("endTimeMs") val endTimeMs: String?
)

data class ArtistOverviewResponse(
    val data: ArtistOverviewData
)

data class ArtistOverviewData(
    val artist: ArtistOverviewInfo
)

data class ArtistOverviewInfo(
    val profile: ArtistProfile,
    val visuals: ArtistVisuals?
)

data class ArtistBiography(
    val text: String
)

data class SpotifyTracks(
    @SerializedName("total") val total: Int
) 