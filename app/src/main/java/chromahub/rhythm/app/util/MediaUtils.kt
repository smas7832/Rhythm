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
        val contentResolver = context.contentResolver
        val retriever = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration: Long = 0
        var artworkUri: Uri? = null
        var year: Int = 0
        
        try {
            // First try to get basic info from content resolver
            contentResolver.query(
                uri,
                null,
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
                        // Remove extension from display name to use as fallback title
                        title = displayName.substringBeforeLast(".")
                    }
                    
                    if (titleIndex != -1) {
                        title = cursor.getString(titleIndex)
                    }
                }
            }
            
            // Now use MediaMetadataRetriever for more detailed metadata
            retriever.setDataSource(context, uri)
            
            // Extract metadata
            val extractedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val extractedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val extractedAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val extractedDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val extractedYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            
            // Use extracted metadata if available, otherwise use fallbacks
            title = extractedTitle ?: title ?: uri.lastPathSegment ?: "Unknown"
            artist = extractedArtist ?: "Unknown Artist"
            album = extractedAlbum ?: "Unknown Album"
            duration = extractedDuration?.toLongOrNull() ?: 0L
            year = extractedYear?.toIntOrNull() ?: 0
            
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
        
        try {
            // Query ContentResolver for file information
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.TRACK
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
                    
                    filePath = cursor.getString(dataIndex) ?: ""
                    fileSize = cursor.getLong(sizeIndex)
                    dateAdded = cursor.getLong(dateAddedIndex) * 1000 // Convert to milliseconds
                    dateModified = cursor.getLong(dateModifiedIndex) * 1000 // Convert to milliseconds
                    composer = cursor.getString(composerIndex) ?: ""
                    
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
                
                // Get sample rate (only available on API 23+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val sampleRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                    if (sampleRateStr != null) {
                        val sampleRateValue = sampleRateStr.toIntOrNull()
                        if (sampleRateValue != null) {
                            sampleRate = "${sampleRateValue} Hz"
                        }
                    }
                }
                
                // Determine format from file extension
                val file = File(filePath)
                val extension = file.extension.lowercase()
                format = when (extension) {
                    "mp3" -> "MP3"
                    "flac" -> "FLAC"
                    "ogg" -> "OGG"
                    "m4a", "aac" -> "AAC"
                    "wav" -> "WAV"
                    "wma" -> "WMA"
                    else -> extension.uppercase()
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
            totalTracks = totalTracks
        )
    }
} 