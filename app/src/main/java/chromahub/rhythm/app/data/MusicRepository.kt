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

class MusicRepository(private val context: Context) {
    private val TAG = "MusicRepository"
    private val lastFmApiService = NetworkClient.lastFmApiService
    private val lyricsApiService = NetworkClient.lyricsApiService
    private val apiKey = NetworkClient.getLastFmApiKey()

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

    suspend fun loadArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()
        val collection = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        val sortOrder = "${MediaStore.Audio.Artists.ARTIST} ASC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            // Cache column indices
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(artistColumn)

                val artist = Artist(
                    id = id.toString(),
                    name = name
                )
                artists.add(artist)
            }
        }

        artists
    }

    /**
     * Fetches artist images from LastFm API for artists without images
     */
    suspend fun fetchArtistImages(artists: List<Artist>): List<Artist> = withContext(Dispatchers.IO) {
        val updatedArtists = mutableListOf<Artist>()
        
        // Check if API key is properly set
        if (apiKey == "YOUR_API_KEY_HERE") {
            Log.w(TAG, "Last.fm API key not set. Skipping artist image fetching.")
            return@withContext artists
        }
        
        for (artist in artists) {
            if (artist.artworkUri != null) {
                updatedArtists.add(artist)
                continue
            }
            
            // Check cache first
            val cachedUri = artistImageCache[artist.name]
            if (cachedUri != null) {
                updatedArtists.add(artist.copy(artworkUri = cachedUri))
                continue
            }
            
            try {
                // Skip artists with empty or "Unknown" names
                if (artist.name.isBlank() || artist.name.equals("Unknown", ignoreCase = true)) {
                    updatedArtists.add(artist)
                    continue
                }
                
                // Generate a custom placeholder image based on artist name
                val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                    name = artist.name,
                    size = 500,
                    cacheDir = context.cacheDir
                )
                
                // Add delay to avoid rate limiting
                delay(500)
                
                Log.d(TAG, "Fetching image for artist: ${artist.name}")
                val response = lastFmApiService.getArtistInfo(artist.name, apiKey)
                
                // Check if artist info is not null and has images
                if (response.artist != null && response.artist.image != null) {
                    // Find the largest available image
                    val largeImage = response.artist.image.find { it.size == "extralarge" } 
                        ?: response.artist.image.find { it.size == "large" }
                        ?: response.artist.image.maxByOrNull { 
                            when (it.size) {
                                "small" -> 1
                                "medium" -> 2
                                "large" -> 3
                                "extralarge" -> 4
                                else -> 0
                            }
                        }
                    
                    // Validate the image URL
                    if (largeImage != null && largeImage.url.isNotEmpty() && 
                        !largeImage.url.contains("2a96cbd8b46e442fc41c2b86b821562f") && // Default LastFM placeholder
                        largeImage.url.startsWith("http")) {
                        
                        val imageUri = Uri.parse(largeImage.url)
                        artistImageCache[artist.name] = imageUri
                        updatedArtists.add(artist.copy(artworkUri = imageUri))
                        Log.d(TAG, "Found valid image for artist: ${artist.name}, URL: ${largeImage.url}")
                    } else {
                        // Use our generated placeholder instead
                        Log.d(TAG, "LastFM returned invalid image for artist: ${artist.name}, using generated placeholder")
                        artistImageCache[artist.name] = placeholderUri
                        updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                    }
                } else {
                    Log.d(TAG, "No artist info or images returned for: ${artist.name}, using generated placeholder")
                    artistImageCache[artist.name] = placeholderUri
                    updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching artist image for ${artist.name}", e)
                // Try to use a generated placeholder
                try {
                    val placeholderUri = chromahub.rhythm.app.util.ImageUtils.generatePlaceholderImage(
                        name = artist.name,
                        size = 500,
                        cacheDir = context.cacheDir
                    )
                    updatedArtists.add(artist.copy(artworkUri = placeholderUri))
                } catch (e2: Exception) {
                    // If even placeholder generation fails, keep the original artist without changes
                    updatedArtists.add(artist)
                }
            }
        }
        
        updatedArtists
    }
    
    /**
     * Fetches album art from LastFm API for albums without artwork
     */
    suspend fun fetchAlbumArtwork(albums: List<Album>): List<Album> = withContext(Dispatchers.IO) {
        val updatedAlbums = mutableListOf<Album>()
        
        // Check if API key is properly set
        if (apiKey == "YOUR_API_KEY_HERE") {
            Log.w(TAG, "Last.fm API key not set. Skipping album artwork fetching.")
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
                        // Artwork doesn't exist or can't be accessed, will fetch from Last.fm
                        Log.d(TAG, "Album artwork URI exists but content is null for ${album.title}: ${album.artworkUri}")
                    }
                } else {
                    // Album already has non-content:// artwork (e.g., from Last.fm)
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
                
                Log.d(TAG, "Fetching artwork for album: ${album.title} by ${album.artist}")
                val response = lastFmApiService.getAlbumInfo(album.artist, album.title, apiKey)
                
                // Check if album info is not null and has images
                if (response.album != null && response.album.image != null) {
                    // Find the largest available image
                    val largeImage = response.album.image.find { it.size == "extralarge" } 
                        ?: response.album.image.find { it.size == "large" }
                        ?: response.album.image.maxByOrNull { 
                            when (it.size) {
                                "small" -> 1
                                "medium" -> 2
                                "large" -> 3
                                "extralarge" -> 4
                                else -> 0
                            }
                        }
                    
                    // Validate the image URL
                    if (largeImage != null && largeImage.url.isNotEmpty() && 
                        !largeImage.url.contains("2a96cbd8b46e442fc41c2b86b821562f") && // Default LastFM placeholder
                        largeImage.url.startsWith("http")) {
                        
                        val imageUri = Uri.parse(largeImage.url)
                        albumImageCache[cacheKey] = imageUri
                        updatedAlbums.add(album.copy(artworkUri = imageUri))
                        Log.d(TAG, "Found valid artwork for album: ${album.title}, URL: ${largeImage.url}")
                    } else {
                        // Use our generated placeholder instead
                        Log.d(TAG, "LastFM returned invalid image for album: ${album.title}, using generated placeholder")
                        albumImageCache[cacheKey] = placeholderUri
                        updatedAlbums.add(album.copy(artworkUri = placeholderUri))
                    }
                } else {
                    Log.d(TAG, "No album info or images returned for: ${album.title}, using generated placeholder")
                    albumImageCache[cacheKey] = placeholderUri
                    updatedAlbums.add(album.copy(artworkUri = placeholderUri))
                }
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
     * Fetches lyrics for a song from an online API
     * @return The lyrics as a String, or null if not found or error occurred
     */
    suspend fun fetchLyrics(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        val cacheKey = "$artist:$title".lowercase()
        
        // Check cache first
        lyricsCache[cacheKey]?.let { 
            Log.d(TAG, "Using cached lyrics for $artist - $title")
            return@withContext it 
        }
        
        try {
            Log.d(TAG, "Fetching lyrics for $artist - $title")
            
            // Clean up the artist and title to improve matching
            val cleanArtist = artist.replace(Regex("\\(.*?\\)"), "").trim()
            val cleanTitle = title.replace(Regex("\\(.*?\\)"), "").trim()
            
            val response = lyricsApiService.getLyrics(cleanArtist, cleanTitle)
            
            if (response.isSuccessful && !response.lyrics.isNullOrBlank()) {
                Log.d(TAG, "Successfully fetched lyrics for $artist - $title")
                // Cache the lyrics
                lyricsCache[cacheKey] = response.lyrics
                return@withContext response.lyrics
            } else {
                Log.d(TAG, "No lyrics found for $artist - $title: ${response.error ?: "Unknown error"}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lyrics for $artist - $title", e)
            return@withContext null
        }
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
} 