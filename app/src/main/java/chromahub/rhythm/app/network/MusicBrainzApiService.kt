package chromahub.rhythm.app.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service definitions for the MusicBrainz public web service.
 * Only the minimal endpoints and fields that we need for fetching
 * artist images (via the "image" relation) are defined here.
 *
 * API documentation: https://musicbrainz.org/doc/Development/XML_Web_Service/Version_2
 */
interface MusicBrainzApiService {

    /**
     * Searches for an artist by name and returns basic information.
     * We request JSON format for easier parsing.
     *
     * Example: /ws/2/artist?query=artist:%22Coldplay%22&fmt=json
     */
    @GET("ws/2/artist")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("fmt") fmt: String = "json",
        @Query("limit") limit: Int = 1
    ): MusicBrainzSearchResponse

    /**
     * Fetch detailed artist information including URL relations.
     * The "url-rels" include external links such as images, Wikipedia,
     * Wikimedia Commons, etc.
     *
     * Example: /ws/2/artist/{mbid}?inc=url-rels&fmt=json
     */
    @GET("ws/2/artist/{mbid}")
    suspend fun getArtistDetails(
        @Path("mbid") mbid: String,
        @Query("inc") inc: String = "url-rels",
        @Query("fmt") fmt: String = "json"
    ): MusicBrainzArtistDetailsResponse

    /**
     * Searches for a recording by title and artist name.
     * We request JSON format for easier parsing.
     *
     * Example: /ws/2/recording?query=recording:%22Viva%20La%20Vida%22%20AND%20artist:%22Coldplay%22&fmt=json
     */
    @GET("ws/2/recording")
    suspend fun searchRecordings(
        @Query("query") query: String,
        @Query("fmt") fmt: String = "json",
        @Query("limit") limit: Int = 1
    ): MusicBrainzSearchResponse

    /**
     * Fetch detailed recording information including URL relations.
     * The "url-rels" include external links such as lyrics sites (relation type "lyrics").
     *
     * Example: /ws/2/recording/{mbid}?inc=url-rels&fmt=json
     */
    @GET("ws/2/recording/{mbid}")
    suspend fun getRecordingDetails(
        @Path("mbid") mbid: String,
        @Query("inc") inc: String = "url-rels",
        @Query("fmt") fmt: String = "json"
    ): MusicBrainzRecordingDetailsResponse
}

// ------------------------------- //
//              DTOs              //
// ------------------------------- //

data class MusicBrainzSearchResponse(
    val artists: List<MusicBrainzArtist> = emptyList(),
    val recordings: List<MusicBrainzRecording> = emptyList()
)

data class MusicBrainzArtist(
    val id: String,
    val name: String,
    val score: Int? = null
)

data class MusicBrainzArtistDetailsResponse(
    val relations: List<MusicBrainzRelation> = emptyList()
)

data class MusicBrainzRelation(
    val type: String? = null,
    val url: MusicBrainzUrl? = null
)

data class MusicBrainzUrl(
    val resource: String? = null
)

// ---------- Recording DTOs for lyrics fallback ----------

data class MusicBrainzRecording(
    val id: String,
    val title: String,
    val score: Int? = null
)

data class MusicBrainzRecordingDetailsResponse(
    val relations: List<MusicBrainzRelation> = emptyList()
)

