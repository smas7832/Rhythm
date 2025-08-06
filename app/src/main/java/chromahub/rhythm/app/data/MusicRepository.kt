package chromahub.rhythm.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import chromahub.rhythm.app.network.NetworkClient
import chromahub.rhythm.app.network.MusicBrainzApiService
import chromahub.rhythm.app.network.CoverArtArchiveService
import chromahub.rhythm.app.network.YTMusicApiService
import chromahub.rhythm.app.network.YTMusicSearchRequest
import chromahub.rhythm.app.network.YTMusicContext
import chromahub.rhythm.app.network.YTMusicClient
import chromahub.rhythm.app.network.YTMusicBrowseRequest
import chromahub.rhythm.app.network.extractArtistImageUrl
import chromahub.rhythm.app.network.extractAlbumImageUrl
import chromahub.rhythm.app.network.extractArtistBrowseId
import chromahub.rhythm.app.network.extractAlbumBrowseId
import chromahub.rhythm.app.network.extractArtistThumbnail
import chromahub.rhythm.app.network.extractAlbumCover
import okhttp3.Request
import com.google.gson.JsonParser
import com.google.gson.Gson
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import chromahub.rhythm.app.data.LyricsData

class MusicRepository(private val context: Context) {
    private val TAG = "MusicRepository"

    /**
     * API Fallback Strategy:
     * 
     * ARTIST IMAGES:
     * 1. Check local cache/storage
     * 2. Spotify RapidAPI (primary)
     * 3. YouTube Music (fallback right after Spotify fails)
     * 4. MusicBrainz + CoverArt Archive
     * 5. Last.fm (disabled)
     * 6. Placeholder generation
     * 
     * ALBUM ARTWORK:
     * 1. Check existing local album art
     * 2. Check cache
     * 3. Spotify RapidAPI (primary)
     * 4. YouTube Music (only when local album art is absent)
     * 5. Placeholder generation
     * 
     * TRACK IMAGES:
     * 1. Check if track already has artwork
     * 2. Check if album has artwork (inherit from album)
     * 3. YouTube Music (fallback when no local artwork available)
     */
    
    private val spotifyApiService = NetworkClient.spotifyApiService
    private val lrclibApiService = NetworkClient.lrclibApiService
    private val musicBrainzApiService = NetworkClient.musicBrainzApiService
    private val coverArtArchiveService = NetworkClient.coverArtArchiveService
    private val lastFmApiService =
        NetworkClient.getLastFmApiKey()?.let { NetworkClient.lastFmApiService }
    private val lastFmApiKey = NetworkClient.getLastFmApiKey()
    private val ytmusicApiService = NetworkClient.ytmusicApiService
    private val genericHttpClient = NetworkClient.genericHttpClient

    // Cache for artist images to avoid redundant API calls
    private val artistImageCache = mutableMapOf<String, Uri?>()
    private val albumImageCache = mutableMapOf<String, Uri?>()
    private val lyricsCache = mutableMapOf<String, LyricsData>()

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED // Add DATE_ADDED to projection
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            // Cache column indices
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED) // Get index for DATE_ADDED

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val track = cursor.getInt(trackColumn)
                val year = cursor.getInt(yearColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L // Convert seconds to milliseconds

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                val song = Song(
                    id = id.toString(),
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId.toString(),
                    duration = duration,
                    uri = contentUri,
                    artworkUri = albumArtUri,
                    trackNumber = track,
                    year = year,
                    dateAdded = dateAdded // Populate the new field
                )
                songs.add(song)
            }
        }

        songs
    }

    suspend fun loadAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        val collection = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )

        val sortOrder = "${MediaStore.Audio.Albums.ALBUM} ASC"

        // Load all songs once
        val allSongs = loadSongs()
        val songsByAlbumTitle = allSongs.groupBy { it.album }

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            // Cache column indices
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val songsCountColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(albumColumn)
                val artist = cursor.getString(artistColumn)
                val songsCount = cursor.getInt(songsCountColumn)
                val year = cursor.getInt(yearColumn)

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    id
                )

                // Get songs for this album from the pre-loaded map
                val albumSongs = songsByAlbumTitle[title] ?: emptyList()

                val album = Album(
                    id = id.toString(),
                    title = title,
                    artist = artist,
                    artworkUri = albumArtUri,
                    year = year,
                    songs = albumSongs,
                    numberOfSongs = albumSongs.size
                )
                albums.add(album)
            }
        }

        Log.d(TAG, "Loaded ${albums.size} albums")
        albums
    }

    /**
     * Loads artists from device storage with enhanced metadata extraction
     */
    suspend fun loadArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()
        val collection = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        val selection = "${MediaStore.Audio.Artists.ARTIST} != ''"
        val sortOrder = "${MediaStore.Audio.Artists.ARTIST} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            // Cache column indices
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumsColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val tracksColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                var name = cursor.getString(artistColumn)
                val numAlbums = cursor.getInt(albumsColumn)
                val numTracks = cursor.getInt(tracksColumn)

                // Clean up artist name
                if (name.isNullOrBlank() || name.equals("<unknown>", ignoreCase = true)) {
                    // Try to find a better name from the artist's tracks
                    name = findBetterArtistName(id.toString()) ?: "Unknown Artist"
                }

                val artist = Artist(
                    id = id.toString(),
                    name = name,
                    numberOfAlbums = numAlbums,
                    numberOfTracks = numTracks
                )
                artists.add(artist)
            }
        }

        artists
    }

    /**
     * Tries to find a better artist name from their tracks
     */
    private suspend fun findBetterArtistName(artistId: String): String? =
        withContext(Dispatchers.IO) {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.Audio.Media.ARTIST_ID} = ?"
            val selectionArgs = arrayOf(artistId)

            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Audio.Media.ARTIST),
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val artists = mutableSetOf<String>()

                while (cursor.moveToNext()) {
                    val artist = cursor.getString(artistColumn)
                    if (!artist.isNullOrBlank() && !artist.equals("<unknown>", ignoreCase = true)) {
                        artists.add(artist)
                    }
                }

                // Return the most common non-blank artist name
                artists.maxByOrNull { name ->
                    cursor.moveToFirst()
                    var count = 0
                    while (cursor.moveToNext()) {
                        if (cursor.getString(artistColumn) == name) count++
                    }
                    count
                }
            }
        }

    /**
     * Triggers a full rescan of music data from the device's MediaStore.
     * This method will reload songs, albums, and artists.
     */
    suspend fun refreshMusicData() {
        Log.d(TAG, "Refreshing music data...")
        // Simply calling loadSongs, loadAlbums, loadArtists will re-query MediaStore
        // and provide fresh data. The ViewModel will then update its StateFlows.
        // No need to return anything here, as the ViewModel will call these methods
        // and collect the results.
        loadSongs()
        loadAlbums()
        loadArtists()
        Log.d(TAG, "Music data refresh complete.")
    }

    /**
     * Fetches artist images from Spotify API for artists without images
     */
    suspend fun fetchArtistImages(artists: List<Artist>): List<Artist> =
        withContext(Dispatchers.IO) {
            val updatedArtists = mutableListOf<Artist>()

            // NetworkClient will handle API key dynamically (user-provided or fallback to default)

            for (artist in artists) {
                try {
                    Log.d(TAG, "Processing artist: ${artist.name}")

                    if (artist.artworkUri != null) {
                        Log.d(
                            TAG,
                            "Artist ${artist.name} already has artwork: ${artist.artworkUri}"
                        )
                        updatedArtists.add(artist)
                        continue
                    }

                    // Check cache first
                    val cachedUri = artistImageCache[artist.name]
                    if (cachedUri != null) {
                        Log.d(TAG, "Using cached image for artist: ${artist.name}")
                        updatedArtists.add(artist.copy(artworkUri = cachedUri))
                        continue
                    }

                    // Check local storage for artist image
                    val localImage = findLocalArtistImage(artist.name)
                    if (localImage != null) {
                        Log.d(TAG, "Using local image for artist: ${artist.name}")
                        artistImageCache[artist.name] = localImage
                        updatedArtists.add(artist.copy(artworkUri = localImage))
                        continue
                    }

                    // Skip artists with empty or "Unknown" names
                    if (artist.name.isBlank() || artist.name.equals(
                            "Unknown Artist",
                            ignoreCase = true
                        )
                    ) {
                        Log.d(TAG, "Skipping unknown/blank artist name")
                        val placeholderUri =
                            chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                                name = "Unknown Artist",
                                size = 500,
                                cacheDir = context.cacheDir
                            )
                        artistImageCache[artist.name] = placeholderUri
                        updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                        continue
                    }

                    // Only try online fetch if network is available and Spotify API is enabled
                    if (isNetworkAvailable() && NetworkClient.isSpotifyApiEnabled()) {
                        Log.d(TAG, "Searching for artist on Spotify: ${artist.name}")
                        try {
                            val searchResponse = spotifyApiService.searchArtists(artist.name)
                            val spotifyArtist = searchResponse.artists.items.firstOrNull()

                            if (spotifyArtist != null) {
                                Log.d(
                                    TAG,
                                    "Found Spotify artist: ${spotifyArtist.data.profile.name}"
                                )
                                val avatarImage = spotifyArtist.data.visuals.avatarImage
                                if (avatarImage != null && avatarImage.sources.isNotEmpty()) {
                                    val largeImage =
                                        avatarImage.sources.maxByOrNull { it.width ?: 0 }
                                    if (largeImage != null && largeImage.url.isNotEmpty()) {
                                        val imageUri = Uri.parse(largeImage.url)
                                        Log.d(
                                            TAG,
                                            "Found image URL for ${artist.name}: ${largeImage.url}"
                                        )
                                        artistImageCache[artist.name] = imageUri
                                        saveLocalArtistImage(artist.name, imageUri.toString())
                                        updatedArtists.add(artist.copy(artworkUri = imageUri))
                                        continue
                                    }
                                }
                            } else {
                                Log.d(TAG, "No Spotify artist found for: ${artist.name}")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Spotify lookup failed for ${artist.name}: ${e.message}")
                        }
                    } else {
                        Log.d(
                            TAG,
                            "No network connection available while fetching ${artist.name} image"
                        )
                    }

                    // -------- YouTube Music fallback (right after Spotify fails) --------
                    if (NetworkClient.isYTMusicApiEnabled()) {
                        try {
                            Log.d(TAG, "Trying YTMusic for artist: ${artist.name}")
                            
                        // Create search request for artist
                        val searchRequest = YTMusicSearchRequest(
                            context = YTMusicContext(YTMusicClient()),
                            query = artist.name,
                            params = "EgWKAQIIAWoKEAoQAxAEEAkQBQ%3D%3D" // Artist search filter
                        )
                        
                        val searchResponse = ytmusicApiService.search(request = searchRequest)
                        if (searchResponse.isSuccessful) {
                            val imageUrl = searchResponse.body()?.extractArtistImageUrl()
                            if (!imageUrl.isNullOrEmpty()) {
                                val imageUri = Uri.parse(imageUrl)
                                artistImageCache[artist.name] = imageUri
                                saveLocalArtistImage(artist.name, imageUrl)
                                updatedArtists.add(artist.copy(artworkUri = imageUri))
                                Log.d(TAG, "Found YTMusic image for ${artist.name}: $imageUrl")
                                continue
                            }
                            
                            // If search result has browseId, try to get detailed artist info for better quality image
                            val browseId = searchResponse.body()?.extractArtistBrowseId()
                            if (!browseId.isNullOrEmpty()) {
                                val browseRequest = YTMusicBrowseRequest(
                                    context = YTMusicContext(YTMusicClient()),
                                    browseId = browseId
                                )
                                val artistResponse = ytmusicApiService.getArtist(request = browseRequest)
                                if (artistResponse.isSuccessful) {
                                    val detailedImageUrl = artistResponse.body()?.extractArtistThumbnail()
                                    if (!detailedImageUrl.isNullOrEmpty()) {
                                        val imageUri = Uri.parse(detailedImageUrl)
                                        artistImageCache[artist.name] = imageUri
                                        saveLocalArtistImage(artist.name, detailedImageUrl)
                                        updatedArtists.add(artist.copy(artworkUri = imageUri))
                                        Log.d(TAG, "Found detailed YTMusic image for ${artist.name}: $detailedImageUrl")
                                        continue
                                    }
                                }
                            }
                        }
                        } catch (e: Exception) {
                            Log.w(TAG, "YTMusic fallback failed for ${artist.name}: ${e.message}")
                        }
                    }

                    // ----- Fallback using MusicBrainz + CoverArtArchive -----
                    if (NetworkClient.isMusicBrainzApiEnabled() && NetworkClient.isCoverArtApiEnabled()) {
                        try {
                            Log.d(TAG, "Searching MusicBrainz for artist: ${artist.name}")
                            val mbResponse =
                                musicBrainzApiService.searchArtists("artist:\"${artist.name}\"")
                            val mbid = mbResponse.artists.firstOrNull()?.id
                            if (mbid != null) {
                                Log.d(
                                    TAG,
                                "Found MBID $mbid for ${artist.name}, querying Cover Art Archive"
                            )
                            val caaResponse = coverArtArchiveService.getArtistImages(mbid)
                            val imageUrl = caaResponse.images.firstOrNull()?.image
                            if (!imageUrl.isNullOrEmpty()) {
                                val imageUri = Uri.parse(imageUrl)
                                artistImageCache[artist.name] = imageUri
                                saveLocalArtistImage(artist.name, imageUrl)
                                updatedArtists.add(artist.copy(artworkUri = imageUri))
                                continue
                            }
                        }
                        } catch (e: Exception) {
                            Log.w(
                                TAG,
                                "MusicBrainz/CoverArt fallback failed for ${artist.name}: ${e.message}"
                            )
                        }
                    }

                    // -------- Final fallback using Last.fm (moved to end) --------
                    if (NetworkClient.isLastFmApiEnabled()) {
                        try {
                            if (false && lastFmApiKey.isNotBlank()) { // Last.fm temporarily disabled
                                Log.d(TAG, "Trying Last.fm (final fallback) for artist: ${artist.name}")
                                val lfResponse = lastFmApiService?.getArtistInfo(
                                    artist = artist.name,
                                    apiKey = lastFmApiKey
                                )
                                val sizeOrder = listOf("small", "medium", "large", "extralarge", "mega")
                            var imageUrl = lfResponse?.artist?.image
                                ?.filter {
                                    it.url.isNotBlank() && !it.url.contains("2a96cbd8b46e442fc41c2b86b821562f") && !it.url.contains(
                                        "/noimage/"
                                    )
                                }
                                ?.maxByOrNull { sizeOrder.indexOf(it.size.lowercase()) }?.url
                            if (imageUrl.isNullOrEmpty()) {
                                val primaryName = artist.name.split("[,&()].+".toRegex())[0].trim()
                                if (primaryName.isNotEmpty() && primaryName != artist.name) {
                                    Log.d(
                                        TAG,
                                        "Retrying Last.fm with primary artist name: $primaryName"
                                    )
                                    val retry = lastFmApiService?.getArtistInfo(
                                        artist = primaryName,
                                        apiKey = lastFmApiKey
                                    )
                                    imageUrl = retry?.artist?.image
                                        ?.filter {
                                            it.url.isNotBlank() && !it.url.contains("2a96cbd8b46e442fc41c2b86b821562f") && !it.url.contains(
                                                "/noimage/"
                                            )
                                        }
                                        ?.maxByOrNull { sizeOrder.indexOf(it.size.lowercase()) }?.url
                                }
                            }
                            if (!imageUrl.isNullOrEmpty()) {
                                val imageUri = Uri.parse(imageUrl)
                                artistImageCache[artist.name] = imageUri
                                saveLocalArtistImage(artist.name, imageUrl)
                                updatedArtists.add(artist.copy(artworkUri = imageUri))
                                continue
                            }
                        }
                        } catch (e: Exception) {
                            Log.w(TAG, "Last.fm final fallback failed for ${artist.name}: ${e.message}")
                        }
                    }

                    // If we get here, generate a placeholder image
                    Log.d(TAG, "Generating placeholder for artist: ${artist.name}")
                    val placeholderUri =
                        chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                            name = artist.name,
                            size = 500,
                            cacheDir = context.cacheDir
                        )
                    artistImageCache[artist.name] = placeholderUri
                    updatedArtists.add(artist.copy(artworkUri = placeholderUri))

                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching artist image for ${artist.name}", e)
                    try {
                        val placeholderUri =
                            chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                                name = artist.name,
                                size = 500,
                                cacheDir = context.cacheDir
                            )
                        updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error generating placeholder for ${artist.name}", e2)
                        updatedArtists.add(artist)
                    }
                }
            }

            updatedArtists
        }

    /**
     * Fetches lyrics for a song using various APIs, prioritizing synced lyrics.
     */
    suspend fun fetchLyrics(artist: String, title: String): LyricsData? =
        withContext(Dispatchers.IO) {
            if (artist.isBlank() || title.isBlank())
                return@withContext LyricsData("No lyrics available for this song", null)

            val cacheKey = "$artist:$title".lowercase()
            lyricsCache[cacheKey]?.let { return@withContext it }

            findLocalLyrics(artist, title)?.let {
                lyricsCache[cacheKey] = it
                return@withContext it
            }

            if (!isNetworkAvailable()) {
                return@withContext LyricsData(
                    "Lyrics not available offline.\nConnect to the internet to view lyrics.",
                    null
                )
            }

            val cleanArtist = artist.trim().replace(Regex("\\(.*?\\)"), "").trim()
            val cleanTitle = title.trim().replace(Regex("\\(.*?\\)"), "").trim()

            var plainLyrics: String? = null
            var syncedLyrics: String? = null

            // ---- LRCLib (Enhanced search with multiple strategies) ----
            if (NetworkClient.isLrcLibApiEnabled()) {
                try {
                    // Strategy 1: Search by track name and artist name
                    var results =
                    lrclibApiService.searchLyrics(trackName = cleanTitle, artistName = cleanArtist)

                // If no results, try fallback strategies
                if (results.isEmpty()) {
                    // Strategy 2: Search with generic query combining artist and title
                    val query = "$cleanArtist $cleanTitle"
                    results = lrclibApiService.searchLyrics(query = query)
                }

                if (results.isEmpty()) {
                    // Strategy 3: Try without parenthetical content and with simplified names
                    val simplifiedArtist =
                        cleanArtist.split(" feat.", " ft.", " featuring").first().trim()
                    val simplifiedTitle =
                        cleanTitle.split(" feat.", " ft.", " featuring").first().trim()
                    results = lrclibApiService.searchLyrics(
                        trackName = simplifiedTitle,
                        artistName = simplifiedArtist
                    )
                }

                // Find the best match - prioritize exact matches, then synced lyrics, then any lyrics
                val bestMatch = results.firstOrNull { result ->
                    val artistMatch =
                        result.artistName?.lowercase()?.contains(cleanArtist.lowercase()) == true ||
                                cleanArtist.lowercase()
                                    .contains(result.artistName?.lowercase() ?: "")
                    val titleMatch =
                        result.trackName?.lowercase()?.contains(cleanTitle.lowercase()) == true ||
                                cleanTitle.lowercase().contains(result.trackName?.lowercase() ?: "")

                    (artistMatch && titleMatch) && result.hasLyrics()
                } ?: results.firstOrNull { it.hasSyncedLyrics() } // Prefer synced lyrics
                ?: results.firstOrNull { it.hasLyrics() } // Then any lyrics

                bestMatch?.let { bm ->
                    syncedLyrics = bm.getSyncedLyricsOrNull()
                    plainLyrics = bm.getPlainLyricsOrNull()

                    if (syncedLyrics != null || plainLyrics != null) {
                        Log.d(
                            TAG,
                            "LRCLib lyrics found - Synced: ${syncedLyrics != null}, Plain: ${plainLyrics != null}"
                        )
                        val lyricsData = LyricsData(plainLyrics, syncedLyrics)
                        lyricsCache[cacheKey] = lyricsData
                        saveLocalLyrics(artist, title, lyricsData)
                        return@withContext lyricsData
                    }
                }
                } catch (e: Exception) {
                    Log.e(TAG, "LRCLib lyrics fetch failed: ${e.message}", e)
                }
            }

            // ---- Spotify RapidAPI (Plain lyrics) ----
            if (NetworkClient.isSpotifyApiEnabled()) {
                try {
                    val searchQuery = "$cleanTitle $cleanArtist"
                    val searchResp = spotifyApiService.searchTracks(searchQuery)
                    val track = searchResp.tracks.items.firstOrNull()
                    track?.let {
                        val lyricsResp = spotifyApiService.getTrackLyrics(it.data.id)
                        if (lyricsResp.lyrics.lines.isNotEmpty()) {
                            plainLyrics = lyricsResp.lyrics.lines.joinToString("\n") { ln -> ln.words }
                        val lyricsData = LyricsData(plainLyrics, null)
                        lyricsCache[cacheKey] = lyricsData
                        saveLocalLyrics(artist, title, lyricsData)
                        return@withContext lyricsData
                    }
                }
                } catch (e: Exception) {
                    Log.w(TAG, "Spotify RapidAPI lyrics fetch failed: ${e.message}")
                }
            }

            // ---- MusicBrainz lyrics link (Plain lyrics) ----
            if (NetworkClient.isMusicBrainzApiEnabled()) {
                try {
                    fetchPlainLyricsFromMusicBrainz(cleanArtist, cleanTitle)?.let {
                        plainLyrics = it
                    val lyricsData = LyricsData(plainLyrics, null)
                    lyricsCache[cacheKey] = lyricsData
                    saveLocalLyrics(artist, title, lyricsData)
                    return@withContext lyricsData
                }
                } catch (e: Exception) {
                    Log.w(TAG, "MusicBrainz lyrics fallback failed: ${e.message}")
                }
            }

            return@withContext LyricsData("No lyrics found for this song", null)
        }

    /**
     * Finds local lyrics file in app's files directory
     */
    private fun findLocalLyrics(artist: String, title: String): LyricsData? {
        val fileName = "${artist}_${title}.json".replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(context.filesDir, "lyrics/$fileName")
        return try {
            if (file.exists()) {
                val json = file.readText()
                Gson().fromJson(json, LyricsData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading local lyrics file: ${e.message}", e)
            null
        }
    }

    /**
     * Saves lyrics to a local file
     */
    private fun saveLocalLyrics(artist: String, title: String, lyricsData: LyricsData) {
        try {
            val fileName = "${artist}_${title}.json".replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val lyricsDir = File(context.filesDir, "lyrics")
            lyricsDir.mkdirs()

            val file = File(lyricsDir, fileName)
            val json = Gson().toJson(lyricsData)
            file.writeText(json)
            Log.d(TAG, "Saved lyrics to local file: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving lyrics to local file: ${e.message}", e)
        }
    }

    /**
     * Attempts to retrieve plain lyrics via MusicBrainz recording relations that point
     * to external lyrics sites (type = "lyrics"). If a URL is found we fetch it
     * and strip HTML to get plain text.
     */
    private suspend fun fetchPlainLyricsFromMusicBrainz(artist: String, title: String): String? =
        withContext(Dispatchers.IO) {
            try {
                if (!NetworkClient.isMusicBrainzApiEnabled()) {
                    return@withContext null
                }
                
                val query = "recording:\"$title\" AND artist:\"$artist\""
                val searchResp = musicBrainzApiService.searchRecordings(query)
                val recording = searchResp.recordings.firstOrNull() ?: return@withContext null
                val details = musicBrainzApiService.getRecordingDetails(recording.id)
                val lyricsUrl =
                    details.relations.firstOrNull { it.type.equals("lyrics", true) }?.url?.resource
                        ?: return@withContext null

                val req =
                    Request.Builder().url(lyricsUrl).header("User-Agent", "RhythmApp/1.0").build()
                genericHttpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val html = resp.body?.string() ?: return@withContext null
                    // Very naive HTML tag removal
                    val plain = html.replace(Regex("<[^>]*>"), "\n").replace("&nbsp;", " ").lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .joinToString("\n")
                    return@use if (plain.split('\n').size >= 10) plain else null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching lyrics via MusicBrainz: ${e.message}")
                null
            }
        }

    /**
     * Fetches album art from Spotify API for albums without artwork
     */
    suspend fun fetchAlbumArtwork(albums: List<Album>): List<Album> = withContext(Dispatchers.IO) {
        val updatedAlbums = mutableListOf<Album>()

        for (album in albums) {
            // Check if the album has a content:// URI and if it actually exists
            if (album.artworkUri != null) {
                if (album.artworkUri.toString()
                        .startsWith("content://media/external/audio/albumart")
                ) {
                    // Try to open the input stream to check if the artwork exists
                    var artworkExists = false
                    try {
                        context.contentResolver.openInputStream(album.artworkUri)?.use {
                            artworkExists = true
                        }
                    } catch (e: Exception) {
                        Log.d(
                            TAG,
                            "Album artwork URI exists but can't be accessed for ${album.title}: ${album.artworkUri}",
                            e
                        )
                        artworkExists = false
                    }

                    if (artworkExists) {
                        updatedAlbums.add(album)
                        continue
                    }
                } else {
                    updatedAlbums.add(album)
                    continue
                }
            }

            // Check cache first
            val cacheKey = "${album.artist}:${album.title}"
            val cachedUri = albumImageCache[cacheKey]
            if (cachedUri != null) {
                updatedAlbums.add(album.copy(artworkUri = cachedUri))
                continue
            }

            try {
                // Skip albums with empty or "Unknown" artist/title
                if (album.artist.isBlank() || album.title.isBlank() ||
                    album.artist.equals("Unknown", ignoreCase = true) ||
                    album.title.equals("Unknown", ignoreCase = true)
                ) {
                    updatedAlbums.add(album)
                    continue
                }

                // Generate a custom placeholder image based on album name
                val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                    name = album.title,
                    size = 500,
                    cacheDir = context.cacheDir
                )

                // Add delay to avoid rate limiting
                delay(500)

                Log.d(TAG, "Searching for album: ${album.title} by ${album.artist}")

                // Search for the album on Spotify (only if enabled)
                if (NetworkClient.isSpotifyApiEnabled()) {
                    val searchResponse =
                        spotifyApiService.searchTracks("${album.title} ${album.artist}")
                    val track = searchResponse.tracks.items.firstOrNull()

                    if (track != null && track.data.album.coverArt != null) {
                        val coverArt = track.data.album.coverArt
                        if (coverArt.sources.isNotEmpty()) {
                        // Find the largest available image
                        val largeImage = coverArt.sources.maxByOrNull { it.width ?: 0 }

                        // Validate the image URL
                        if (largeImage != null && largeImage.url.isNotEmpty() && largeImage.url.startsWith(
                                "http"
                            )
                        ) {
                            val imageUri = Uri.parse(largeImage.url)
                            albumImageCache[cacheKey] = imageUri
                            updatedAlbums.add(album.copy(artworkUri = imageUri))
                            Log.d(
                                TAG,
                                "Found valid artwork for album: ${album.title}, URL: ${largeImage.url}"
                            )
                            continue
                        }
                    }
                }
                }

                // -------- YTMusic fallback (only when local album art is absent) --------
                // Check if we should use YTMusic fallback for albums
                if (NetworkClient.isYTMusicApiEnabled()) {
                    var foundAlbumArt = false
                    try {
                        Log.d(TAG, "Trying YTMusic for album: ${album.title} by ${album.artist}")
                        
                        // Create search request for album
                        val searchQuery = "${album.title} ${album.artist}"
                    val searchRequest = YTMusicSearchRequest(
                        context = YTMusicContext(YTMusicClient()),
                        query = searchQuery,
                        params = "EgWKAQIYAWoKEAoQAxAEEAkQBQ%3D%3D" // Album search filter
                    )
                    
                    val searchResponse = ytmusicApiService.search(request = searchRequest)
                    if (searchResponse.isSuccessful) {
                        val imageUrl = searchResponse.body()?.extractAlbumImageUrl()
                        if (!imageUrl.isNullOrEmpty()) {
                            val imageUri = Uri.parse(imageUrl)
                            albumImageCache[cacheKey] = imageUri
                            updatedAlbums.add(album.copy(artworkUri = imageUri))
                            Log.d(TAG, "Found YTMusic album art for ${album.title}: $imageUrl")
                            foundAlbumArt = true
                        } else {
                            // Try to get detailed album info for better quality cover art
                            val browseId = searchResponse.body()?.extractAlbumBrowseId()
                            if (!browseId.isNullOrEmpty()) {
                                val browseRequest = YTMusicBrowseRequest(
                                    context = YTMusicContext(YTMusicClient()),
                                    browseId = browseId
                                )
                                val albumResponse = ytmusicApiService.getAlbum(request = browseRequest)
                                if (albumResponse.isSuccessful) {
                                    val detailedImageUrl = albumResponse.body()?.extractAlbumCover()
                                    if (!detailedImageUrl.isNullOrEmpty()) {
                                        val imageUri = Uri.parse(detailedImageUrl)
                                        albumImageCache[cacheKey] = imageUri
                                        updatedAlbums.add(album.copy(artworkUri = imageUri))
                                        Log.d(TAG, "Found detailed YTMusic album art for ${album.title}: $detailedImageUrl")
                                        foundAlbumArt = true
                                    }
                                }
                            }
                        }
                    }
                    } catch (e: Exception) {
                        Log.w(TAG, "YTMusic fallback failed for album ${album.title}: ${e.message}")
                    }

                    // If YTMusic didn't find anything, use placeholder
                    if (!foundAlbumArt) {
                        Log.d(
                            TAG,
                            "No valid image found for album: ${album.title}, using generated placeholder"
                        )
                        albumImageCache[cacheKey] = placeholderUri
                        updatedAlbums.add(album.copy(artworkUri = placeholderUri))
                    }
                } else {
                    // YTMusic is disabled, use placeholder
                    Log.d(
                        TAG,
                        "No valid image found for album: ${album.title}, using generated placeholder"
                    )
                    albumImageCache[cacheKey] = placeholderUri
                    updatedAlbums.add(album.copy(artworkUri = placeholderUri))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching album artwork for ${album.title}", e)
                // Try to use a generated placeholder
                try {
                    val placeholderUri =
                        chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                            name = album.title,
                            size = 500,
                            cacheDir = context.cacheDir
                        )
                    updatedAlbums.add(album.copy(artworkUri = placeholderUri))
                } catch (e2: Exception) {
                    // If even placeholder generation fails, keep the original album without changes
                    updatedAlbums.add(album)
                }
            }
        }

        updatedAlbums
    }

    /**
     * Fetches track/song images from YTMusic API for songs without artwork.
     * This is used as a fallback when local album art is absent and other APIs fail.
     */
    suspend fun fetchTrackArtwork(songs: List<Song>): List<Song> = withContext(Dispatchers.IO) {
        val updatedSongs = mutableListOf<Song>()

        for (song in songs) {
            // Only fetch if song doesn't have artwork already
            if (song.artworkUri != null) {
                updatedSongs.add(song)
                continue
            }

            // Check if album has artwork - if yes, we don't need track-specific artwork
            if (song.albumId.isNotBlank()) {
                val albums = loadAlbums()
                val album = albums.find { it.id == song.albumId }
                if (album?.artworkUri != null) {
                    // Album has artwork, no need for track-specific image
                    updatedSongs.add(song)
                    continue
                }
            }

            val cacheKey = "${song.artist}:${song.title}"
            
            // Check cache first
            val cachedUri = albumImageCache[cacheKey] // Reuse album cache for tracks
            if (cachedUri != null) {
                updatedSongs.add(song.copy(artworkUri = cachedUri))
                continue
            }

            try {
                // Skip songs with empty or "Unknown" artist/title
                if (song.artist.isBlank() || song.title.isBlank() ||
                    song.artist.equals("Unknown", ignoreCase = true) ||
                    song.title.equals("Unknown", ignoreCase = true)
                ) {
                    updatedSongs.add(song)
                    continue
                }

                if (!NetworkClient.isYTMusicApiEnabled()) {
                    Log.d(TAG, "YTMusic API is disabled, skipping track artwork for: ${song.title}")
                    updatedSongs.add(song)
                    continue
                }

                Log.d(TAG, "Searching YTMusic for track: ${song.title} by ${song.artist}")

                // Add delay to avoid rate limiting
                delay(200)

                // Create search request for song/track
                val searchQuery = "${song.title} ${song.artist}"
                val searchRequest = YTMusicSearchRequest(
                    context = YTMusicContext(YTMusicClient()),
                    query = searchQuery,
                    params = "EgWKAQIIAWoKEAoQAxAEEAkQBQ%3D%3D" // Song search filter
                )

                val searchResponse = ytmusicApiService.search(request = searchRequest)
                if (searchResponse.isSuccessful) {
                    // For tracks, we can extract image from the first result
                    val imageUrl = searchResponse.body()?.extractAlbumImageUrl() // Tracks use same thumbnail structure
                    if (!imageUrl.isNullOrEmpty()) {
                        val imageUri = Uri.parse(imageUrl)
                        albumImageCache[cacheKey] = imageUri // Cache for future use
                        updatedSongs.add(song.copy(artworkUri = imageUri))
                        Log.d(TAG, "Found YTMusic track image for ${song.title}: $imageUrl")
                        continue
                    }
                }

                Log.d(TAG, "No YTMusic image found for track: ${song.title}")
                updatedSongs.add(song)

            } catch (e: Exception) {
                Log.w(TAG, "YTMusic track image fetch failed for ${song.title}: ${e.message}")
                updatedSongs.add(song)
            }
        }

        updatedSongs
    }

    suspend fun getSongsForAlbum(albumId: String): List<Song> = withContext(Dispatchers.IO) {
        val allSongs = loadSongs() // Ensure songs are loaded once
        val allAlbums = loadAlbums() // Ensure albums are loaded once

        Log.d("MusicRepository", "Getting songs for album ID: $albumId")

        // Find the album by ID
        val album = allAlbums.find { it.id == albumId }
        if (album == null) {
            Log.e("MusicRepository", "Album not found with ID: $albumId")
            return@withContext emptyList()
        }

        Log.d("MusicRepository", "Found album: ${album.title} (ID: $albumId)")

        // Filter songs that match the album's title and ID
        val albumSongs = allSongs.filter { song ->
            val albumTitleMatch = song.album == album.title
            val albumIdMatch = song.albumId == albumId
            albumTitleMatch && albumIdMatch
        }

        Log.d("MusicRepository", "Found ${albumSongs.size} songs for album: ${album.title}")

        if (albumSongs.isEmpty()) {
            Log.d(
                "MusicRepository",
                "No songs found for album title: ${album.title} and ID: $albumId, trying direct lookup"
            )
            return@withContext loadSongsForAlbumDirect(albumId)
        }

        return@withContext albumSongs
    }

    private suspend fun loadSongsForAlbumDirect(albumId: String): List<Song> =
        withContext(Dispatchers.IO) {
            val songs = mutableListOf<Song>()
            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
            val selectionArgs = arrayOf(albumId)
            val sortOrder = "${MediaStore.Audio.Media.TRACK} ASC"

            Log.d("MusicRepository", "Querying MediaStore directly for album ID: $albumId")

            context.contentResolver.query(
                collection,
                null, // Get all columns
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                Log.d("MusicRepository", "Found ${cursor.count} songs for album ID: $albumId")

                // Cache column indices
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val retrievedAlbumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val track = cursor.getInt(trackColumn)
                    val year = cursor.getInt(yearColumn)

                    Log.d(
                        "MusicRepository",
                        "Found song: $title for album: $album (ID: $retrievedAlbumId)"
                    )

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        retrievedAlbumId
                    )

                    val song = Song(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = retrievedAlbumId.toString(),
                        duration = duration,
                        uri = contentUri,
                        artworkUri = albumArtUri,
                        trackNumber = track,
                        year = year
                    )
                    songs.add(song)
                }
            }

            songs
        }

    suspend fun getSongsForArtist(artistId: String): List<Song> = withContext(Dispatchers.IO) {
        val allSongs = loadSongs() // Ensure songs are loaded once
        val allArtists = loadArtists() // Ensure artists are loaded once

        Log.d("MusicRepository", "Getting songs for artist ID: $artistId")

        // Find the artist by ID
        val artist = allArtists.find { it.id == artistId }
        if (artist == null) {
            Log.e("MusicRepository", "Artist not found with ID: $artistId")
            return@withContext emptyList()
        }

        Log.d("MusicRepository", "Found artist: ${artist.name} (ID: $artistId)")

        // Filter songs that match the artist's name
        val artistSongs = allSongs.filter { song ->
            song.artist == artist.name
        }

        Log.d("MusicRepository", "Found ${artistSongs.size} songs for artist: ${artist.name}")
        return@withContext artistSongs
    }

    suspend fun getAlbumsForArtist(artistId: String): List<Album> = withContext(Dispatchers.IO) {
        val allAlbums = loadAlbums() // Ensure albums are loaded once
        val allArtists = loadArtists() // Ensure artists are loaded once

        Log.d("MusicRepository", "Getting albums for artist ID: $artistId")

        // Find the artist by ID
        val artist = allArtists.find { it.id == artistId }
        if (artist == null) {
            Log.e("MusicRepository", "Artist not found with ID: $artistId")
            return@withContext emptyList()
        }

        Log.d("MusicRepository", "Found artist: ${artist.name} (ID: $artistId)")

        // Filter albums that match the artist's name
        val artistAlbums = allAlbums.filter { album ->
            album.artist == artist.name
        }

        Log.d("MusicRepository", "Found ${artistAlbums.size} albums for artist: ${artist.name}")
        return@withContext artistAlbums
    }

    suspend fun createPlaylist(name: String): Playlist {
        return Playlist(
            id = System.currentTimeMillis().toString(),
            name = name
        )
    }

    // Mock data for locations
    fun getLocations(): List<PlaybackLocation> {
        return listOf(
            PlaybackLocation(
                id = "living_room",
                name = "Living room",
                icon = 0 // Replace with actual icon resource
            ),
            PlaybackLocation(
                id = "bedroom",
                name = "Bedroom",
                icon = 0 // Replace with actual icon resource
            ),
            PlaybackLocation(
                id = "kitchen",
                name = "Kitchen",
                icon = 0 // Replace with actual icon resource
            )
        )
    }

    /**
     * Checks if the device is currently connected to the internet
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    /**
     * Finds locally stored artist image
     */
    private fun findLocalArtistImage(artistName: String): Uri? {
        val fileName = "${artistName}.jpg".replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(context.filesDir, "artist_images/$fileName")
        return if (file.exists()) Uri.fromFile(file) else null
    }

    /**
     * Saves artist image to local storage
     */
    private fun saveLocalArtistImage(artistName: String, imageUrl: String) {
        try {
            val fileName = "${artistName}.jpg".replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val imageDir = File(context.filesDir, "artist_images")
            imageDir.mkdirs()

            val file = File(imageDir, fileName)
            URL(imageUrl).openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Saved artist image to local file: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving artist image to local file: ${e.message}", e)
        }
    }
    
    /**
     * Clears all in-memory caches
     */
    fun clearInMemoryCaches() {
        try {
            artistImageCache.clear()
            albumImageCache.clear()
            lyricsCache.clear()
            Log.d(TAG, "Cleared all in-memory caches (artist images, album images, lyrics)")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing in-memory caches", e)
        }
    }
    
    /**
     * Gets the current size of in-memory caches
     * @return A map containing cache names and their sizes
     */
    fun getInMemoryCacheInfo(): Map<String, Int> {
        return mapOf(
            "artistImageCache" to artistImageCache.size,
            "albumImageCache" to albumImageCache.size,
            "lyricsCache" to lyricsCache.size
        )
    }
}
