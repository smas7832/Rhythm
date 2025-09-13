package chromahub.rhythm.app.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.util.ImageUtils
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for handling media-related operations
 */
object MediaUtils {
    private const val TAG = "MediaUtils"
    
    /**
     * Extracts metadata from an external audio file
     * @param context The application context
     * @param uri The URI of the audio file
     * @return A Song object with metadata extracted from the file
     */
    fun extractMetadataFromUri(context: Context, uri: Uri): Song {
        Log.d(TAG, "Extracting metadata from URI: $uri")
        val contentResolver = context.contentResolver
        val retriever = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration: Long = 0
        var artworkUri: Uri? = null
        var year: Int = 0
        
        try {
            // First verify we can access the URI
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    // Read a small amount to ensure the file is accessible
                    val buffer = ByteArray(1024)
                    stream.read(buffer)
                    Log.d(TAG, "URI is accessible and readable")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cannot access URI: $uri", e)
                throw IllegalArgumentException("Cannot access file at $uri", e)
            }
            
            // Try to get basic info from content resolver (with error handling)
            try {
                contentResolver.query(
                    uri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        // Try to get file metadata from content resolver
                        val displayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        val titleIndex = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
                        
                        if (displayNameIndex != -1) {
                            val displayName = cursor.getString(displayNameIndex)
                            if (!displayName.isNullOrBlank()) {
                                // Remove extension from display name to use as fallback title
                                title = displayName.substringBeforeLast(".")
                                Log.d(TAG, "Got display name: $displayName")
                            }
                        }
                        
                        if (titleIndex != -1) {
                            val cursorTitle = cursor.getString(titleIndex)
                            if (!cursorTitle.isNullOrBlank()) {
                                title = cursorTitle
                                Log.d(TAG, "Got cursor title: $cursorTitle")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not query content resolver for basic metadata", e)
                // Continue with MediaMetadataRetriever
            }
            
            // Now use MediaMetadataRetriever for more detailed metadata
            try {
                retriever.setDataSource(context, uri)
                
                // Extract metadata
                val extractedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val extractedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                val extractedAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                val extractedDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val extractedYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                
                // Use extracted metadata if available, otherwise use fallbacks
                title = extractedTitle ?: title ?: uri.lastPathSegment?.substringBeforeLast(".") ?: "Unknown"
                artist = extractedArtist ?: "Unknown Artist"
                album = extractedAlbum ?: "Unknown Album"
                duration = extractedDuration?.toLongOrNull() ?: 0L
                year = extractedYear?.toIntOrNull() ?: 0
                
                Log.d(TAG, "Metadata extraction successful: Title=$title, Artist=$artist, Duration=$duration")
            } catch (e: Exception) {
                Log.w(TAG, "MediaMetadataRetriever failed, using fallback values", e)
                // Use basic fallbacks
                title = title ?: uri.lastPathSegment?.substringBeforeLast(".") ?: "Unknown File"
                artist = "Unknown Artist"
                album = "Unknown Album"
                duration = 0L
                year = 0
            }
            
            // Extract album art
            val embeddedArt = retriever.embeddedPicture
            if (embeddedArt != null) {
                // Save embedded artwork to cache
                try {
                    val artworkFile = File(context.cacheDir, "artwork_${uri.hashCode()}.jpg")
                    FileOutputStream(artworkFile).use { out ->
                        val bitmap = BitmapFactory.decodeByteArray(embeddedArt, 0, embeddedArt.size)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        out.flush()
                    }
                    artworkUri = artworkFile.toUri()
                    Log.d(TAG, "Saved embedded artwork to: $artworkUri")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save embedded artwork", e)
                }
            }
            
            // If we couldn't extract artwork, try to generate a placeholder
            if (artworkUri == null) {
                artworkUri = ImageUtils.generatePlaceholderImage(
                    name = title,
                    size = 500,
                    cacheDir = context.cacheDir
                )
            }
            
            Log.d(TAG, "Extracted metadata - Title: $title, Artist: $artist, Album: $album, Duration: $duration")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting metadata", e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
        
        // Create and return a Song object with the extracted metadata
        return Song(
            id = uri.toString(),
            title = title ?: "Unknown",
            artist = artist ?: "Unknown Artist",
            album = album ?: "Unknown Album",
            duration = duration,
            uri = uri,
            artworkUri = artworkUri,
            trackNumber = 0,
            year = year
        )
    }
    
    /**
     * Gets the mime type of a file from its URI
     * @param context The application context
     * @param uri The URI of the file
     * @return The mime type of the file, or null if it couldn't be determined
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> context.contentResolver.getType(uri)
            ContentResolver.SCHEME_FILE -> {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
            }
            else -> null
        }
    }

    /**
     * Gets extended information about a song including file size, bitrate, sample rate, etc.
     * @param context The application context
     * @param song The song to get extended info for
     * @return ExtendedSongInfo with additional metadata
     */
    fun getExtendedSongInfo(context: Context, song: Song): chromahub.rhythm.app.ui.screens.ExtendedSongInfo {
        val contentResolver = context.contentResolver
        val retriever = MediaMetadataRetriever()
        
        var fileSize = 0L
        var bitrate = "Unknown"
        var sampleRate = "Unknown"
        var format = "Unknown"
        var dateAdded = 0L
        var dateModified = 0L
        var filePath = ""
        var composer = ""
        var discNumber = 0
        var totalTracks = 0
        var albumArtist = ""
        var year = 0
        var mimeType = ""
        var channels = "Unknown"
        var hasLyrics = false
        var isBookmark = -1L
        
        try {
            // Query ContentResolver for file information
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.MIME_TYPE
            )
            
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Audio.Media._ID} = ?",
                arrayOf(song.id.toString()),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                    val composerIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)
                    val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                    val albumArtistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
                    val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                    val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                    
                    filePath = cursor.getString(dataIndex) ?: ""
                    fileSize = cursor.getLong(sizeIndex)
                    dateAdded = cursor.getLong(dateAddedIndex) * 1000 // Convert to milliseconds
                    dateModified = cursor.getLong(dateModifiedIndex) * 1000 // Convert to milliseconds
                    composer = cursor.getString(composerIndex) ?: ""
                    albumArtist = cursor.getString(albumArtistIndex) ?: ""
                    year = cursor.getInt(yearIndex)
                    mimeType = cursor.getString(mimeTypeIndex) ?: ""
                    // isBookmark is not available in all Android versions, so we'll skip it
                    
                    // Extract track and disc numbers from TRACK field
                    val trackInfo = cursor.getInt(trackIndex)
                    if (trackInfo > 0) {
                        discNumber = trackInfo / 1000
                        totalTracks = trackInfo % 1000
                    }
                }
            }
            
            // Use MediaMetadataRetriever for additional audio metadata
            if (filePath.isNotEmpty()) {
                retriever.setDataSource(filePath)
                
                // Get bitrate
                val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                if (bitrateStr != null) {
                    val bitrateValue = bitrateStr.toIntOrNull()
                    if (bitrateValue != null) {
                        bitrate = "${bitrateValue / 1000} kbps"
                    }
                }
                
                // Get sample rate (try multiple methods for better compatibility)
                val sampleRateStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ has better metadata support
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                } else {
                    // Fallback for older versions
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                }
                
                if (sampleRateStr != null) {
                    val sampleRateValue = sampleRateStr.toIntOrNull()
                    if (sampleRateValue != null) {
                        sampleRate = "${sampleRateValue} Hz"
                    }
                }
                
                // Get number of audio channels
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val channelCountStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
                    if (channelCountStr != null) {
                        val channelCount = channelCountStr.toIntOrNull()
                        if (channelCount != null) {
                            channels = when (channelCount) {
                                1 -> "Mono"
                                2 -> "Stereo"
                                else -> "$channelCount channels"
                            }
                        }
                    }
                }
                
                // Check for lyrics availability (METADATA_KEY_LYRICS not available in all versions)
                // We'll skip lyrics detection for now to maintain compatibility
                hasLyrics = false
                
                // Fill in missing composer if available from MediaMetadataRetriever
                if (composer.isEmpty()) {
                    val composerStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
                    if (!composerStr.isNullOrEmpty()) {
                        composer = composerStr
                    }
                }
                
                // Fill in missing album artist if available
                if (albumArtist.isEmpty()) {
                    val albumArtistStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                    if (!albumArtistStr.isNullOrEmpty()) {
                        albumArtist = albumArtistStr
                    }
                }
                
                // Fill in missing year if available
                if (year == 0) {
                    val yearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    if (!yearStr.isNullOrEmpty()) {
                        year = yearStr.toIntOrNull() ?: 0
                    }
                }
                
                // Determine format from file extension and MIME type
                val file = File(filePath)
                val extension = file.extension.lowercase()
                format = when {
                    mimeType.contains("mp3", ignoreCase = true) || extension == "mp3" -> "MP3"
                    mimeType.contains("flac", ignoreCase = true) || extension == "flac" -> "FLAC"
                    mimeType.contains("ogg", ignoreCase = true) || extension == "ogg" -> "OGG"
                    mimeType.contains("aac", ignoreCase = true) || extension in listOf("m4a", "aac") -> "AAC"
                    mimeType.contains("wav", ignoreCase = true) || extension == "wav" -> "WAV"
                    mimeType.contains("wma", ignoreCase = true) || extension == "wma" -> "WMA"
                    extension.isNotEmpty() -> extension.uppercase()
                    mimeType.isNotEmpty() -> mimeType.substringAfter("/").uppercase()
                    else -> "Unknown"
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting extended song info", e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
        
        return chromahub.rhythm.app.ui.screens.ExtendedSongInfo(
            fileSize = fileSize,
            bitrate = bitrate,
            sampleRate = sampleRate,
            format = format,
            dateAdded = dateAdded,
            dateModified = dateModified,
            filePath = filePath,
            composer = composer,
            discNumber = discNumber,
            albumArtist = albumArtist,
            year = year,
            mimeType = mimeType,
            channels = channels,
            hasLyrics = hasLyrics
        )
    }
    
    /**
     * Updates song metadata using ContentResolver
     * @param context The application context
     * @param song The song to update
     * @param newTitle The new title
     * @param newArtist The new artist
     * @param newAlbum The new album
     * @param newGenre The new genre
     * @param newTrackNumber The new track number
     * @return true if the update was successful, false otherwise
     */
    fun updateSongMetadata(
        context: Context,
        song: Song,
        newTitle: String,
        newArtist: String,
        newAlbum: String,
        newGenre: String,
        newTrackNumber: Int
    ): Boolean {
        return try {
            val contentResolver = context.contentResolver
            
            // Log initial state for debugging
            Log.d(TAG, "Attempting to update metadata for song: ${song.title}")
            Log.d(TAG, "Song URI: ${song.uri}")
            Log.d(TAG, "Song ID: ${song.id}")
            Log.d(TAG, "New values - Title: $newTitle, Artist: $newArtist, Album: $newAlbum, Genre: $newGenre, Year: N/A, Track: $newTrackNumber")
            
            // Check if URI is valid and accessible
            if (!song.uri.toString().startsWith("content://media/")) {
                Log.e(TAG, "Invalid URI scheme for metadata update: ${song.uri}")
                return false
            }
            
            // Validate required fields
            if (newTitle.isBlank()) {
                Log.e(TAG, "Title cannot be blank")
                return false
            }
            if (newArtist.isBlank()) {
                Log.e(TAG, "Artist cannot be blank")
                return false
            }
            
            // Check write permissions for older versions
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                val hasWritePermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (!hasWritePermission) {
                    Log.e(TAG, "Missing WRITE_EXTERNAL_STORAGE permission")
                    return false
                }
            }
            
            // For Android 11+, check if we can access the specific file
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    // Test if we can query the file
                    val testCursor = contentResolver.query(
                        song.uri,
                        arrayOf(MediaStore.Audio.Media._ID),
                        null,
                        null,
                        null
                    )
                    testCursor?.close()
                } catch (e: SecurityException) {
                    Log.e(TAG, "Cannot access file - permission denied: ${song.uri}")
                    return false
                }
            }
            
            val values = android.content.ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, newTitle)
                put(MediaStore.Audio.Media.ARTIST, newArtist)
                put(MediaStore.Audio.Media.ALBUM, newAlbum)
                if (newGenre.isNotBlank()) {
                    put(MediaStore.Audio.Media.GENRE, newGenre)
                }
                if (newTrackNumber > 0) {
                    put(MediaStore.Audio.Media.TRACK, newTrackNumber)
                }
            }
            
            // For Android 11+ (API 30+), we need to handle MediaStore differently
            val rowsUpdated = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    // Try direct update first
                    val directUpdate = contentResolver.update(
                        song.uri,
                        values,
                        null,
                        null
                    )
                    
                    if (directUpdate > 0) {
                        Log.d(TAG, "Direct update successful for song: ${song.title}")
                        directUpdate
                    } else {
                        Log.w(TAG, "Direct update failed, may require user permission for song: ${song.title}")
                        // On Android 11+, we might need to request user permission for modifying this file
                        // For now, we'll try alternative approach using MediaStore.Audio.Media table
                        updateViaMediaStore(contentResolver, song, values)
                    }
                } catch (e: SecurityException) {
                    Log.w(TAG, "Security exception during direct update, trying alternative approach", e)
                    updateViaMediaStore(contentResolver, song, values)
                }
            } else {
                // For older Android versions, direct update should work
                contentResolver.update(
                    song.uri,
                    values,
                    null,
                    null
                )
            }
            
            Log.d(TAG, "Updated $rowsUpdated rows for song: ${song.title}")
            val success = rowsUpdated > 0
            
            if (success) {
                // Trigger media scanner to refresh the metadata
                try {
                    // Get file path from URI
                    val filePath = when (song.uri.scheme) {
                        "content" -> {
                            val projection = arrayOf(MediaStore.Audio.Media.DATA)
                            contentResolver.query(song.uri, projection, null, null, null)
                                ?.use { cursor ->
                                    if (cursor.moveToFirst()) {
                                        val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                                        cursor.getString(dataIndex)
                                    } else null
                                }
                        }
                        "file" -> song.uri.path
                        else -> null
                    }
                    
                    if (filePath != null) {
                        android.media.MediaScannerConnection.scanFile(
                            context,
                            arrayOf(filePath),
                            null,
                            null
                        )
                        Log.d(TAG, "Media scanner triggered for updated file: $filePath")
                    } else {
                        Log.w(TAG, "Could not get file path for media scanner")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to trigger media scanner", e)
                }
            }
            
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update song metadata: ${song.title}", e)
            false
        }
    }
    
    private fun updateViaMediaStore(
        contentResolver: android.content.ContentResolver,
        song: Song,
        values: android.content.ContentValues
    ): Int {
        return try {
            Log.d(TAG, "Attempting update via MediaStore table for song ID: ${song.id}")
            
            // Try to update via MediaStore query using the song ID
            val selection = "${MediaStore.Audio.Media._ID} = ?"
            val selectionArgs = arrayOf(song.id.toString())
            
            val rowsUpdated = contentResolver.update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                values,
                selection,
                selectionArgs
            )
            
            Log.d(TAG, "MediaStore update result: $rowsUpdated rows updated")
            
            if (rowsUpdated == 0) {
                // Try alternative approach with different content URI
                Log.d(TAG, "Trying alternative content URI")
                val altRowsUpdated = contentResolver.update(
                    MediaStore.Audio.Media.getContentUri("external"),
                    values,
                    selection,
                    selectionArgs
                )
                Log.d(TAG, "Alternative URI update result: $altRowsUpdated rows updated")
                return altRowsUpdated
            }
            
            rowsUpdated
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during MediaStore update - check app permissions", e)
            0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update via MediaStore table", e)
            0
        }
    }
} 