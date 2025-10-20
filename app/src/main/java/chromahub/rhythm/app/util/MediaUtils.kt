package chromahub.rhythm.app.util

import android.app.RemoteAction
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
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import androidx.core.net.toUri
import chromahub.rhythm.app.data.Song
import java.io.File
import java.io.FileOutputStream

/**
 * Exception wrapper for RecoverableSecurityException that includes the pending intent
 * and temp file path for retry after permission is granted
 */
class RecoverableSecurityExceptionWrapper(
    message: String,
    val userAction: RemoteAction,
    val fileUri: Uri,
    val tempFilePath: String
) : Exception(message)

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
        var genre: String? = null

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
                        val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)

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
                val extractedGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)

                // Use extracted metadata if available, otherwise use fallbacks
                title = extractedTitle ?: title ?: uri.lastPathSegment?.substringBeforeLast(".") ?: "Unknown"
                artist = extractedArtist ?: "Unknown Artist"
                album = extractedAlbum ?: "Unknown Album"
                duration = extractedDuration?.toLongOrNull() ?: 0L
                year = extractedYear?.toIntOrNull() ?: 0
                genre = extractedGenre

                Log.d(TAG, "Metadata extraction successful: Title=$title, Artist=$artist, Duration=$duration, Genre=$genre")
            } catch (e: Exception) {
                Log.w(TAG, "MediaMetadataRetriever failed, using fallback values", e)
                // Use basic fallbacks
                title = title ?: uri.lastPathSegment?.substringBeforeLast(".") ?: "Unknown File"
                artist = "Unknown Artist"
                album = "Unknown Album"
                duration = 0L
                year = 0
                genre = null
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
            year = year,
            genre = genre
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
        var genre = song.genre // Initialize with existing song genre

        // Use song's built-in audio metadata if available
        if (song.bitrate != null) {
            bitrate = "${song.bitrate / 1000} kbps"
        }
        if (song.sampleRate != null) {
            sampleRate = "${song.sampleRate / 1000} kHz"
        }
        if (song.channels != null) {
            channels = when (song.channels) {
                1 -> "Mono"
                2 -> "Stereo"
                6 -> "5.1 Surround"
                8 -> "7.1 Surround"
                else -> "${song.channels} channels"
            }
        }
        if (song.codec != null) {
            format = song.codec
        }

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
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media._ID // Need song ID for genre lookup
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
                    val songIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

                    filePath = cursor.getString(dataIndex) ?: ""
                    fileSize = cursor.getLong(sizeIndex)
                    dateAdded = cursor.getLong(dateAddedIndex) * 1000 // Convert to milliseconds
                    dateModified = cursor.getLong(dateModifiedIndex) * 1000 // Convert to milliseconds
                    composer = cursor.getString(composerIndex) ?: ""
                    albumArtist = cursor.getString(albumArtistIndex) ?: ""
                    year = cursor.getInt(yearIndex)
                    mimeType = cursor.getString(mimeTypeIndex) ?: ""
                    val songId = cursor.getLong(songIdIndex)

                    // Use enhanced genre detection with multiple fallbacks
                    if (genre.isNullOrEmpty()) {
                        genre = getGenreForSong(context, song.uri, songId.toInt())
                    }
                    
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
                
                // Get number of audio channels using MediaExtractor (correct method)
                // Only extract if song doesn't already have this info
                if (channels == "Unknown") {
                    var extractor: android.media.MediaExtractor? = null
                    try {
                        extractor = android.media.MediaExtractor()
                        extractor.setDataSource(filePath)
                        
                        // Find the audio track
                        for (i in 0 until extractor.trackCount) {
                            val format = extractor.getTrackFormat(i)
                            val mime = format.getString(android.media.MediaFormat.KEY_MIME)
                            
                            if (mime?.startsWith("audio/") == true) {
                                // Get channel count from MediaFormat
                                if (format.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) {
                                    val channelCount = format.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT)
                                    channels = when (channelCount) {
                                        1 -> "Mono"
                                        2 -> "Stereo"
                                        6 -> "5.1 Surround"
                                        8 -> "7.1 Surround"
                                        else -> "$channelCount channels"
                                    }
                                }
                                break
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error extracting channel count with MediaExtractor: ${e.message}")
                        channels = "Stereo" // Default fallback
                    } finally {
                        try {
                            extractor?.release()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error releasing MediaExtractor: ${e.message}")
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
                
                // Fill in missing genre if available from MediaMetadataRetriever
                if (genre.isNullOrEmpty()) {
                    val genreStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                    if (!genreStr.isNullOrEmpty()) {
                        genre = genreStr
                    }
                }
                
                // Determine format from file extension and MIME type
                val file = File(filePath)
                val extension = file.extension.lowercase()
                
                // Try to detect advanced audio format using AudioFormatDetector
                val audioFormatInfo = try {
                    AudioFormatDetector.detectFormat(context, song.uri)
                } catch (e: Exception) {
                    Log.w(TAG, "AudioFormatDetector failed, using fallback detection", e)
                    null
                }
                
                // Use detected codec or fall back to extension-based detection
                format = when {
                    audioFormatInfo != null && audioFormatInfo.codec != "Unknown" -> audioFormatInfo.codec
                    mimeType.contains("mp3", ignoreCase = true) || extension == "mp3" -> "MP3"
                    mimeType.contains("flac", ignoreCase = true) || extension == "flac" -> "FLAC"
                    mimeType.contains("ogg", ignoreCase = true) || extension == "ogg" -> "OGG"
                    mimeType.contains("alac", ignoreCase = true) || extension == "alac" -> "ALAC"
                    extension == "m4a" -> {
                        // M4A can be AAC or ALAC - check with AudioFormatDetector
                        if (AudioFormatDetector.isALAC(context, song.uri)) "ALAC" else "AAC"
                    }
                    mimeType.contains("aac", ignoreCase = true) || extension == "aac" -> "AAC"
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
        
        // Detect audio format information for lossless, Dolby, etc.
        // Pass the song object to help with bit depth calculation
        val audioFormatInfo = try {
            AudioFormatDetector.detectFormat(context, song.uri, song)
        } catch (e: Exception) {
            Log.w(TAG, "AudioFormatDetector failed in getExtendedSongInfo", e)
            null
        }
        
        // Calculate detailed audio quality using AudioQualityDetector
        val bitrateValue = song.bitrate ?: 0
        val sampleRateValue = audioFormatInfo?.sampleRateHz ?: song.sampleRate ?: 0
        val bitrateKbps = bitrateValue / 1000
        val codecValue = audioFormatInfo?.codec ?: format
        val bitDepthValue = audioFormatInfo?.bitDepth ?: 0
        val channelCountValue = audioFormatInfo?.channelCount ?: song.channels ?: 2
        
        val audioQuality = AudioQualityDetector.detectQuality(
            codec = codecValue,
            sampleRateHz = sampleRateValue,
            bitrateKbps = bitrateKbps,
            bitDepth = bitDepthValue,
            channelCount = channelCountValue
        )
        
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
            hasLyrics = hasLyrics,
            genre = genre ?: "", // Pass the extracted genre
            // Audio quality indicators (legacy)
            isLossless = audioQuality.isLossless,
            isDolby = audioQuality.isDolby,
            isDTS = audioQuality.isDTS,
            isHiRes = audioQuality.isHiRes,
            audioCodec = audioFormatInfo?.codec ?: format,
            formatName = audioFormatInfo?.formatName ?: format,
            // Enhanced quality information
            qualityType = audioQuality.qualityType.name,
            qualityLabel = audioQuality.qualityLabel,
            qualityDescription = audioQuality.qualityDescription,
            bitDepth = audioQuality.bitDepthEstimate,
            qualityCategory = audioQuality.category
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
        newYear: Int = 0,
        newTrackNumber: Int
    ): Boolean {
        return try {
            val contentResolver = context.contentResolver
            
            // Log initial state for debugging
            Log.d(TAG, "Attempting to update metadata for song: ${song.title}")
            Log.d(TAG, "Song URI: ${song.uri}")
            Log.d(TAG, "Song ID: ${song.id}")
            Log.d(TAG, "New values - Title: $newTitle, Artist: $newArtist, Album: $newAlbum, Genre: $newGenre, Year: $newYear, Track: $newTrackNumber")
            
            // Check if URI is valid and accessible
            if (!song.uri.toString().startsWith("content://media/")) {
                Log.e(TAG, "Invalid URI scheme for metadata update: ${song.uri}")
                return false
            }
            
            // Get file path from URI for metadata writing
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
            
            // Write metadata to the actual file
            var fileWriteSucceeded = false
            
            // For Android 10+ (API 29+), we need to use a temporary file approach
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    Log.d(TAG, "Using temporary file approach for Android 10+ file write")
                    Log.d(TAG, "Song URI: ${song.uri}")
                    Log.d(TAG, "Song path: $filePath")
                    
                    // Get the file extension to preserve format
                    val fileName = song.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    val extension = filePath?.substringAfterLast('.', "mp3") ?: "mp3"
                    val tempFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.$extension")
                    
                    try {
                        // Step 1: Copy original file to temp location
                        Log.d(TAG, "Step 1: Copying original file to temp...")
                        var bytesRead = 0L
                        contentResolver.openInputStream(song.uri)?.use { inputStream ->
                            tempFile.outputStream().use { outputStream ->
                                bytesRead = inputStream.copyTo(outputStream)
                            }
                        }
                        Log.d(TAG, "Copied $bytesRead bytes to temp location: ${tempFile.absolutePath}")
                        
                        if (!tempFile.exists() || tempFile.length() == 0L) {
                            throw Exception("Failed to copy file to temp location")
                        }
                        
                        // Step 2: Modify metadata using jaudiotagger on temp file
                        Log.d(TAG, "Step 2: Modifying metadata in temp file...")
                        val audioFileObj = AudioFileIO.read(tempFile)
                        val tag: Tag = audioFileObj.tag ?: audioFileObj.createDefaultTag()
                        
                        tag.setField(FieldKey.TITLE, newTitle)
                        tag.setField(FieldKey.ARTIST, newArtist)
                        tag.setField(FieldKey.ALBUM, newAlbum)
                        if (newGenre.isNotBlank()) {
                            tag.setField(FieldKey.GENRE, newGenre)
                        }
                        if (newYear > 0) {
                            tag.setField(FieldKey.YEAR, newYear.toString())
                        }
                        if (newTrackNumber > 0) {
                            tag.setField(FieldKey.TRACK, newTrackNumber.toString())
                        }
                        
                        audioFileObj.tag = tag
                        AudioFileIO.write(audioFileObj)
                        Log.d(TAG, "Metadata written to temp file successfully")
                        
                        // Step 3: Copy modified temp file back to original location
                        Log.d(TAG, "Step 3: Copying modified file back to original location...")
                        
                        // Try to open output stream - this is where it might fail on Android 10+
                        val outputStream = try {
                            contentResolver.openOutputStream(song.uri, "w")
                        } catch (e: android.app.RecoverableSecurityException) {
                            // Android 11+ requires user permission via createWriteRequest
                            Log.e(TAG, "RecoverableSecurityException - Need to request user permission for this file")
                            Log.e(TAG, "This file was not created by this app. User permission is required to modify it.")
                            Log.e(TAG, "Solution: App needs to use MediaStore.createWriteRequest() and show permission dialog")
                            
                            // Store the temp file path so it can be used after permission is granted
                            // For now, we'll throw with the pending intent info
                            throw RecoverableSecurityExceptionWrapper(
                                "User permission required to modify this file",
                                e.userAction,
                                song.uri,
                                tempFile.absolutePath
                            )
                        } catch (e: SecurityException) {
                            Log.e(TAG, "SecurityException opening output stream - app may not have write permission for this file", e)
                            null
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception opening output stream: ${e.javaClass.simpleName} - ${e.message}", e)
                            null
                        }
                        
                        if (outputStream == null) {
                            Log.e(TAG, "Failed to open output stream for writing. This typically means:")
                            Log.e(TAG, "1. File is on external SD card (requires special permissions)")
                            Log.e(TAG, "2. File is in a protected directory")
                            Log.e(TAG, "3. App doesn't own this file (Android 11+ scoped storage restriction)")
                            throw Exception("Cannot open output stream for URI: ${song.uri}")
                        }
                        
                        outputStream.use { outStream ->
                            tempFile.inputStream().use { inputStream ->
                                val bytesCopied = inputStream.copyTo(outStream)
                                Log.d(TAG, "Copied $bytesCopied bytes back to original location")
                            }
                        }
                        
                        fileWriteSucceeded = true
                        Log.d(TAG, "Successfully wrote metadata using temp file approach for Android 10+")
                        
                    } catch (inner: RecoverableSecurityExceptionWrapper) {
                        // This is a special case where we need user permission
                        Log.e(TAG, "RecoverableSecurityException caught - user permission required")
                        // Re-throw to be handled by the ViewModel/UI layer
                        throw inner
                    } catch (inner: Exception) {
                        Log.e(TAG, "Error during temp file operations", inner)
                        throw inner
                    } finally {
                        // Clean up temp file
                        if (tempFile.exists()) {
                            val deleted = tempFile.delete()
                            Log.d(TAG, "Temp file cleanup: ${if (deleted) "success" else "failed"}")
                        }
                    }
                } catch (e: RecoverableSecurityExceptionWrapper) {
                    // User permission required - propagate up to UI layer
                    Log.w(TAG, "User permission required for file modification: ${e.message}")
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to write metadata using temp file approach on Android 10+", e)
                    Log.e(TAG, "Error details: ${e.message}")
                    e.printStackTrace()
                    // Continue with MediaStore update even if file writing fails
                }
            } else {
                // For Android 9 and below, use traditional file access
                if (filePath == null) {
                    Log.e(TAG, "Could not get file path for metadata update")
                } else {
                    val audioFile = File(filePath)
                    if (!audioFile.exists()) {
                        Log.e(TAG, "File does not exist: $filePath")
                    } else if (!audioFile.canWrite()) {
                        Log.e(TAG, "File is not writable: $filePath")
                    } else {
                        try {
                            val audioFileObj = AudioFileIO.read(audioFile)
                            val tag: Tag = audioFileObj.tag ?: audioFileObj.createDefaultTag()
                            
                            tag.setField(FieldKey.TITLE, newTitle)
                            tag.setField(FieldKey.ARTIST, newArtist)
                            tag.setField(FieldKey.ALBUM, newAlbum)
                            if (newGenre.isNotBlank()) {
                                tag.setField(FieldKey.GENRE, newGenre)
                            }
                            if (newYear > 0) {
                                tag.setField(FieldKey.YEAR, newYear.toString())
                            }
                            if (newTrackNumber > 0) {
                                tag.setField(FieldKey.TRACK, newTrackNumber.toString())
                            }
                            
                            audioFileObj.tag = tag
                            AudioFileIO.write(audioFileObj)
                            
                            fileWriteSucceeded = true
                            Log.d(TAG, "Successfully wrote metadata to file (Android 9-): $filePath")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to write metadata to file using jaudiotagger", e)
                        }
                    }
                }
            }
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
                if (newYear > 0) {
                    put(MediaStore.Audio.Media.YEAR, newYear)
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
            val mediaStoreSuccess = rowsUpdated > 0
            
            // Log the results of both operations
            Log.d(TAG, "Metadata update results - MediaStore: $mediaStoreSuccess, File write: $fileWriteSucceeded")
            
            // Only trigger media scanner if the file write actually succeeded
            // If file write failed but MediaStore succeeded, scanner would overwrite with old metadata
            if (mediaStoreSuccess && fileWriteSucceeded) {
                // Trigger media scanner to refresh the metadata
                try {
                    // Get file path from URI
                    val scanFilePath = when (song.uri.scheme) {
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
                    
                    if (scanFilePath != null) {
                        android.media.MediaScannerConnection.scanFile(
                            context,
                            arrayOf(scanFilePath),
                            null,
                            null
                        )
                        Log.d(TAG, "Media scanner triggered for updated file: $scanFilePath")
                    } else {
                        Log.w(TAG, "Could not get file path for media scanner")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to trigger media scanner", e)
                }
            } else if (mediaStoreSuccess && !fileWriteSucceeded) {
                Log.i(TAG, "MediaStore updated but file write failed - skipping media scanner to preserve MediaStore changes")
            }
            
            // Return true ONLY if file write succeeded (the actual persistent change)
            // MediaStore updates are temporary and will be overwritten by media scanner
            fileWriteSucceeded
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update song metadata: ${song.title}", e)
            false
        }
    }
    
    /**
     * Gets the genre name from MediaStore.Audio.Genres table using the song ID
     * @param contentResolver The ContentResolver to use for queries
     * @param songId The song ID to look up genre for
     * @return The genre name, or null if not found
     */
    private fun getGenreNameFromMediaStore(contentResolver: ContentResolver, songId: Int): String? {
        return try {
            // First get the genre ID from the audio_genres_map table
            val genreIdProjection = arrayOf(MediaStore.Audio.Genres.Members.GENRE_ID)
            val genreIdCursor = contentResolver.query(
                MediaStore.Audio.Genres.getContentUriForAudioId("external", songId),
                genreIdProjection,
                null,
                null,
                null
            )

            genreIdCursor?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val genreIdIndex = cursor.getColumnIndex(MediaStore.Audio.Genres.Members.GENRE_ID)
                    if (genreIdIndex != -1) {
                        val genreId = cursor.getLong(genreIdIndex)

                        // Now get the genre name from the genres table
                        val genreNameProjection = arrayOf(MediaStore.Audio.Genres.NAME)
                        val genreNameCursor = contentResolver.query(
                            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                            genreNameProjection,
                            "${MediaStore.Audio.Genres._ID} = ?",
                            arrayOf(genreId.toString()),
                            null
                        )

                        genreNameCursor?.use { nameCursor ->
                            if (nameCursor.moveToFirst()) {
                                val nameIndex = nameCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)
                                if (nameIndex != -1) {
                                    val genreName = nameCursor.getString(nameIndex)
                                    Log.d(TAG, "Found genre name: $genreName for song ID: $songId")
                                    return genreName
                                }
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "No genre found for song ID: $songId")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting genre name from MediaStore", e)
            null
        }
    }

    /**
     * Enhanced genre detection with multiple fallback methods
     * @param context The application context
     * @param songUri The URI of the song file
     * @param songId The song ID for MediaStore queries
     * @return The detected genre name, or null if not found
     */
    fun getGenreForSong(context: Context, songUri: Uri, songId: Int): String? {
        // Method 1: Try MediaStore.Audio.Media.GENRE column (may contain genre ID or name)
        try {
            val genreFromMediaStoreColumn = getGenreFromMediaStoreColumn(context, songId)
            if (!genreFromMediaStoreColumn.isNullOrBlank()) {
                Log.d(TAG, "Found genre from MediaStore column: $genreFromMediaStoreColumn for song ID: $songId")
                return genreFromMediaStoreColumn
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get genre from MediaStore column", e)
        }

        // Method 2: Try MediaStore.Audio.Genres table lookup
        try {
            val genreFromGenresTable = getGenreNameFromMediaStore(context.contentResolver, songId)
            if (!genreFromGenresTable.isNullOrBlank()) {
                Log.d(TAG, "Found genre from Genres table: $genreFromGenresTable for song ID: $songId")
                return genreFromGenresTable
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get genre from Genres table", e)
        }

        // Method 3: Try MediaMetadataRetriever
        try {
            val genreFromRetriever = getGenreFromMediaMetadataRetriever(context, songUri)
            if (!genreFromRetriever.isNullOrBlank()) {
                Log.d(TAG, "Found genre from MediaMetadataRetriever: $genreFromRetriever for song URI: $songUri")
                return genreFromRetriever
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get genre from MediaMetadataRetriever", e)
        }

        // Method 4: Try to infer genre from file path or filename patterns
        try {
            val genreFromPath = inferGenreFromPath(songUri)
            if (!genreFromPath.isNullOrBlank()) {
                Log.d(TAG, "Inferred genre from path: $genreFromPath for song URI: $songUri")
                return genreFromPath
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to infer genre from path", e)
        }

        Log.d(TAG, "No genre found for song ID: $songId, URI: $songUri")
        return null
    }

    /**
     * Gets genre directly from MediaStore.Audio.Media.GENRE column
     * This column may contain either a genre ID or genre name depending on Android version
     */
    private fun getGenreFromMediaStoreColumn(context: Context, songId: Int): String? {
        return try {
            val projection = arrayOf(MediaStore.Audio.Media.GENRE)
            val selection = "${MediaStore.Audio.Media._ID} = ?"
            val selectionArgs = arrayOf(songId.toString())

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val genreIndex = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                    if (genreIndex != -1) {
                        val genreValue = cursor.getString(genreIndex)?.trim()
                        if (!genreValue.isNullOrBlank()) {
                            // Check if it's a numeric genre ID or a genre name
                            val genreId = genreValue.toLongOrNull()
                            if (genreId != null && genreId > 0) {
                                // It's a genre ID, try to convert it to name
                                return getGenreNameFromId(context.contentResolver, genreId)
                            } else {
                                // It's already a genre name
                                return genreValue
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting genre from MediaStore column", e)
            null
        }
    }

    /**
     * Converts a genre ID to genre name using the Genres table
     */
    private fun getGenreNameFromId(contentResolver: ContentResolver, genreId: Long): String? {
        return try {
            val projection = arrayOf(MediaStore.Audio.Genres.NAME)
            val selection = "${MediaStore.Audio.Genres._ID} = ?"
            val selectionArgs = arrayOf(genreId.toString())

            contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME)
                    if (nameIndex != -1) {
                        return cursor.getString(nameIndex)?.trim()
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error converting genre ID to name", e)
            null
        }
    }

    /**
     * Gets genre from MediaMetadataRetriever
     */
    private fun getGenreFromMediaMetadataRetriever(context: Context, songUri: Uri): String? {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, songUri)
            val genre = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_GENRE)
            genre?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting genre from MediaMetadataRetriever", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
    }

    /**
     * Attempts to infer genre from file path or filename patterns
     */
    private fun inferGenreFromPath(songUri: Uri): String? {
        return try {
            val path = songUri.toString().lowercase()

            // Common genre patterns in file paths
            when {
                path.contains("rock") -> "Rock"
                path.contains("pop") -> "Pop"
                path.contains("hip.hop") || path.contains("hiphop") || path.contains("rap") -> "Hip Hop"
                path.contains("jazz") -> "Jazz"
                path.contains("classical") || path.contains("classic") -> "Classical"
                path.contains("electronic") || path.contains("electro") || path.contains("edm") -> "Electronic"
                path.contains("country") -> "Country"
                path.contains("blues") -> "Blues"
                path.contains("reggae") -> "Reggae"
                path.contains("folk") -> "Folk"
                path.contains("metal") -> "Metal"
                path.contains("punk") -> "Punk"
                path.contains("indie") -> "Indie"
                path.contains("alternative") -> "Alternative"
                path.contains("r&b") || path.contains("rnb") -> "R&B"
                path.contains("soul") -> "Soul"
                path.contains("funk") -> "Funk"
                path.contains("disco") -> "Disco"
                path.contains("dance") -> "Dance"
                path.contains("house") -> "House"
                path.contains("techno") -> "Techno"
                path.contains("trance") -> "Trance"
                path.contains("ambient") -> "Ambient"
                path.contains("soundtrack") || path.contains("ost") -> "Soundtrack"
                path.contains("instrumental") -> "Instrumental"
                path.contains("vocal") -> "Vocal"
                path.contains("christmas") || path.contains("holiday") -> "Holiday"
                path.contains("world") -> "World"
                path.contains("latin") -> "Latin"
                path.contains("african") -> "African"
                path.contains("asian") -> "Asian"
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inferring genre from path", e)
            null
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
