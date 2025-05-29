package chromahub.rhythm.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import chromahub.rhythm.app.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class MusicRepository(private val context: Context) {
    private val TAG = "MusicRepository"
    private val spotifyApiService = NetworkClient.spotifyApiService
    private val apiKey = NetworkClient.getSpotifyApiKey()

    // Cache for artist images to avoid redundant API calls
    private val artistImageCache = mutableMapOf<String, Uri?>()
    private val albumImageCache = mutableMapOf<String, Uri?>()
    private val lyricsCache = mutableMapOf<String, String>()

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
            MediaStore.Audio.Media.YEAR
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

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val track = cursor.getInt(trackColumn)
                val year = cursor.getInt(yearColumn)

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
            val songsCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
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
                
                Log.d(TAG, "Loaded album: $title by $artist with artwork URI: $albumArtUri")

                val album = Album(
                    id = id.toString(),
                    title = title,
                    artist = artist,
                    artworkUri = albumArtUri,
                    year = year,
                    numberOfSongs = songsCount
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
            val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val tracksColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

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
    private suspend fun findBetterArtistName(artistId: String): String? = withContext(Dispatchers.IO) {
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
     * Fetches artist images from Spotify API for artists without images
     */
    suspend fun fetchArtistImages(artists: List<Artist>): List<Artist> = withContext(Dispatchers.IO) {
        val updatedArtists = mutableListOf<Artist>()
    
        // Check if API key is properly set
        if (apiKey.isBlank()) {
            Log.w(TAG, "Spotify API key not set. Skipping artist image fetching.")
            return@withContext artists
        }
        
        for (artist in artists) {
            try {
                Log.d(TAG, "Processing artist: ${artist.name}")
                
                if (artist.artworkUri != null) {
                    Log.d(TAG, "Artist ${artist.name} already has artwork: ${artist.artworkUri}")
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
                if (artist.name.isBlank() || artist.name.equals("Unknown Artist", ignoreCase = true)) {
                    Log.d(TAG, "Skipping unknown/blank artist name")
                    val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                        name = "Unknown Artist",
                        size = 500,
                        cacheDir = context.cacheDir
                    )
                    artistImageCache[artist.name] = placeholderUri
                    updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                    continue
                }
                
                // Only try online fetch if network is available
                if (isNetworkAvailable()) {
                    Log.d(TAG, "Searching for artist on Spotify: ${artist.name}")
                    
                    val searchResponse = spotifyApiService.searchArtists(artist.name)
                    val spotifyArtist = searchResponse.artists.items.firstOrNull()
                    
                    if (spotifyArtist != null) {
                        Log.d(TAG, "Found Spotify artist: ${spotifyArtist.data.profile.name}")
                        
                        val avatarImage = spotifyArtist.data.visuals.avatarImage
                        if (avatarImage != null && avatarImage.sources.isNotEmpty()) {
                            // Find the largest available image
                            val largeImage = avatarImage.sources.maxByOrNull { it.width ?: 0 }
                            
                            if (largeImage != null && largeImage.url.isNotEmpty()) {
                                val imageUri = Uri.parse(largeImage.url)
                                Log.d(TAG, "Found image URL for ${artist.name}: ${largeImage.url}")
                                
                                artistImageCache[artist.name] = imageUri
                                saveLocalArtistImage(artist.name, imageUri.toString())
                                
                                updatedArtists.add(artist.copy(artworkUri = imageUri))
                                continue
                            }
                        }
                    } else {
                        Log.d(TAG, "No Spotify artist found for: ${artist.name}")
                    }
                } else {
                    Log.d(TAG, "No network connection available")
                }
                
                // If we get here, generate a placeholder image
                Log.d(TAG, "Generating placeholder for artist: ${artist.name}")
                val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                    name = artist.name,
                    size = 500,
                    cacheDir = context.cacheDir
                )
                artistImageCache[artist.name] = placeholderUri
                updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching artist image for ${artist.name}", e)
                try {
                    val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
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
     * Fetches lyrics for a song using Spotify RapidAPI
     */
    suspend fun fetchLyrics(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        if (artist.isBlank() || title.isBlank()) {
            Log.d(TAG, "Cannot fetch lyrics: artist or title is blank")
            return@withContext "No lyrics available for this song"
        }
        
        val cacheKey = "$artist:$title".lowercase()
        Log.d(TAG, "Fetching lyrics for: $artist - $title")
        
        // Check cache first
        lyricsCache[cacheKey]?.let { 
            Log.d(TAG, "Using cached lyrics for $artist - $title")
            return@withContext it 
        }
        
        // Try to find local lyrics file
        val localLyrics = findLocalLyrics(artist, title)
        if (localLyrics != null) {
            Log.d(TAG, "Using local lyrics for $artist - $title")
            lyricsCache[cacheKey] = localLyrics
            return@withContext localLyrics
        }
        
        // Check network connectivity
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable for lyrics fetch")
            return@withContext "Lyrics not available offline.\nConnect to the internet to view lyrics."
        }
        
        try {
            Log.d(TAG, "Fetching lyrics online for $artist - $title")
            
            // Clean up artist and title for better matching
            val cleanArtist = artist.trim().replace(Regex("\\(.*?\\)"), "").trim()
            val cleanTitle = title.trim().replace(Regex("\\(.*?\\)"), "").trim()
            
            // Search for the track on Spotify
            val searchQuery = "$cleanTitle $cleanArtist"
            Log.d(TAG, "Searching for track: $searchQuery")
            
            val searchResponse = spotifyApiService.searchTracks(searchQuery)
            val track = searchResponse.tracks.items.firstOrNull()
            
            if (track != null) {
                Log.d(TAG, "Found Spotify track: ${track.data.name} (${track.data.id})")
                
                // Get lyrics using track ID
                val lyricsResponse = spotifyApiService.getTrackLyrics(track.data.id)
                
                if (lyricsResponse.lyrics.lines.isNotEmpty()) {
                    // Convert lyrics lines to text
                    val lyricsText = lyricsResponse.lyrics.lines
                        .joinToString("\n") { it.words }
                    
                    Log.d(TAG, "Found lyrics with ${lyricsResponse.lyrics.lines.size} lines")
                    
                    // Cache the result
                    lyricsCache[cacheKey] = lyricsText
                    
                    // Save lyrics locally
                    saveLocalLyrics(artist, title, lyricsText)
                    
                    return@withContext lyricsText
                } else {
                    Log.d(TAG, "No lyrics lines found in response")
                }
            } else {
                Log.d(TAG, "No track found for: $searchQuery")
            }
            
            Log.d(TAG, "No lyrics found for $artist - $title")
            return@withContext "No lyrics found for this song"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lyrics for $artist - $title: ${e.message}", e)
            return@withContext "Error loading lyrics"
        }
    }

    /**
     * Finds local lyrics file in app's files directory
     */
    private fun findLocalLyrics(artist: String, title: String): String? {
        val fileName = "${artist}_${title}.txt".replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(context.filesDir, "lyrics/$fileName")
        return try {
            if (file.exists()) {
                file.readText()
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
    private fun saveLocalLyrics(artist: String, title: String, lyrics: String) {
        try {
            val fileName = "${artist}_${title}.txt".replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val lyricsDir = File(context.filesDir, "lyrics")
            lyricsDir.mkdirs()
            
            val file = File(lyricsDir, fileName)
            file.writeText(lyrics)
            Log.d(TAG, "Saved lyrics to local file: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving lyrics to local file: ${e.message}", e)
        }
    }

    /**
     * Fetches album art from Spotify API for albums without artwork
     */
    suspend fun fetchAlbumArtwork(albums: List<Album>): List<Album> = withContext(Dispatchers.IO) {
        val updatedAlbums = mutableListOf<Album>()
        
        // Check if API key is properly set
        if (apiKey.isBlank()) {
            Log.w(TAG, "Spotify API key not set. Skipping album artwork fetching.")
            return@withContext albums
        }
        
        for (album in albums) {
            // Check if the album has a content:// URI and if it actually exists
            if (album.artworkUri != null) {
                if (album.artworkUri.toString().startsWith("content://media/external/audio/albumart")) {
                    // Try to open the input stream to check if the artwork exists
                    var artworkExists = false
                    try {
                        context.contentResolver.openInputStream(album.artworkUri)?.use { inputStream ->
                            // Artwork exists
                            artworkExists = true
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Album artwork URI exists but can't be accessed for ${album.title}: ${album.artworkUri}", e)
                        artworkExists = false
                    }
                    
                    if (artworkExists) {
                        // Artwork exists, keep the original URI
                        Log.d(TAG, "Album artwork exists for ${album.title}: ${album.artworkUri}")
                        updatedAlbums.add(album)
                        continue
                    } else {
                        // Artwork doesn't exist or can't be accessed, will fetch from Spotify
                        Log.d(TAG, "Album artwork URI exists but content is null for ${album.title}: ${album.artworkUri}")
                    }
                } else {
                    // Album already has non-content:// artwork (e.g., from Spotify)
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
                    album.title.equals("Unknown", ignoreCase = true)) {
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
                
                // Search for the album on Spotify
                val searchResponse = spotifyApiService.searchTracks("${album.title} ${album.artist}")
                val track = searchResponse.tracks.items.firstOrNull()
                
                if (track != null && track.data.album.coverArt != null) {
                    val coverArt = track.data.album.coverArt
                    if (coverArt.sources.isNotEmpty()) {
                        // Find the largest available image
                        val largeImage = coverArt.sources.maxByOrNull { it.width ?: 0 }
                        
                        // Validate the image URL
                        if (largeImage != null && largeImage.url.isNotEmpty() && largeImage.url.startsWith("http")) {
                            val imageUri = Uri.parse(largeImage.url)
                            albumImageCache[cacheKey] = imageUri
                            updatedAlbums.add(album.copy(artworkUri = imageUri))
                            Log.d(TAG, "Found valid artwork for album: ${album.title}, URL: ${largeImage.url}")
                            continue
                        }
                    }
                }
                
                // If we get here, either no album was found or no valid image was found
                Log.d(TAG, "No valid Spotify image found for album: ${album.title}, using generated placeholder")
                albumImageCache[cacheKey] = placeholderUri
                updatedAlbums.add(album.copy(artworkUri = placeholderUri))
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching album artwork for ${album.title}", e)
                // Try to use a generated placeholder
                try {
                    val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
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

    suspend fun getSongsForAlbum(albumId: String): List<Song> {
        val songs = loadSongs()
        Log.d("MusicRepository", "Getting songs for album ID: $albumId")
        val albumSongs = songs.filter { song ->
            val albumMatch = song.album == albumId
            if (albumMatch) {
                Log.d("MusicRepository", "Found song ${song.title} matching album name: ${song.album}")
            }
            albumMatch
        }
        
        if (albumSongs.isEmpty()) {
            Log.d("MusicRepository", "No songs found for album ID: $albumId")
            return loadSongsForAlbumDirect(albumId)
        }
        
        return albumSongs
    }
    
    private suspend fun loadSongsForAlbumDirect(albumId: String): List<Song> = withContext(Dispatchers.IO) {
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
                
                Log.d("MusicRepository", "Found song: $title for album: $album (ID: $retrievedAlbumId)")
                
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

    suspend fun getSongsForArtist(artistId: String): List<Song> {
        val songs = loadSongs()
        return songs.filter { it.artist == artistId }
    }

    suspend fun getAlbumsForArtist(artistId: String): List<Album> {
        val albums = loadAlbums()
        return albums.filter { it.artist == artistId }
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
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        
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
} 