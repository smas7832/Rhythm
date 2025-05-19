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
} 