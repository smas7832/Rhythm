package chromahub.rhythm.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import chromahub.rhythm.app.R
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * Utility class for handling image-related operations
 */
object ImageUtils {
    
    private const val TAG = "ImageUtils"
    
    /**
     * Generates a placeholder image with the first letter of the name
     * @param name The name to use for the placeholder
     * @param size The size of the bitmap to generate
     * @param cacheDir The directory to cache the generated image
     * @return Uri to the generated image or null if generation failed
     */
    fun generatePlaceholderImage(name: String, size: Int = 300, cacheDir: File): Uri? {
        return try {
            val letter = name.firstOrNull()?.uppercase() ?: "?"
            val color = getColorForName(name)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw background
            val paint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            
            // Draw text
            paint.apply {
                this.color = Color.WHITE
                textSize = size * 0.5f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            
            val textBounds = Rect()
            paint.getTextBounds(letter, 0, letter.length, textBounds)
            
            val x = size / 2f
            val y = size / 2f + textBounds.height() / 2f - textBounds.bottom
            
            canvas.drawText(letter, x, y, paint)
            
            // Save to cache
            val file = File(cacheDir, "placeholder_${name.hashCode()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            file.toUri()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating placeholder image", e)
            null
        }
    }
    
    /**
     * Generates a consistent color based on the name
     */
    private fun getColorForName(name: String): Int {
        val seed = name.hashCode()
        val random = Random(seed)
        
        // Generate a vibrant color
        val hue = random.nextFloat() * 360f
        val saturation = 0.7f + random.nextFloat() * 0.3f
        val value = 0.5f + random.nextFloat() * 0.3f
        
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    
    /**
     * Builds an image request with a placeholder
     */
    fun buildImageRequest(
        data: Any?,
        name: String?,
        cacheDir: File,
        type: PlaceholderType = PlaceholderType.GENERAL
    ): ImageRequest.Builder.() -> Unit = {
        // Set the main data source
        data(data)
        
        // Enable crossfade animation
        crossfade(true)
        
        // Set Material 3 placeholder based on content type
        val placeholderResId = when (type) {
            PlaceholderType.ALBUM -> R.drawable.ic_album_placeholder
            PlaceholderType.ARTIST -> R.drawable.ic_artist_placeholder
            PlaceholderType.TRACK -> R.drawable.ic_track_placeholder
            PlaceholderType.PLAYLIST -> R.drawable.ic_playlist_placeholder
            PlaceholderType.GENERAL -> R.drawable.ic_audio_placeholder
        }
        placeholder(placeholderResId)
        error(placeholderResId)
        
        // Add a listener to handle the result
        listener(
            onSuccess = { _, result ->
                // Image loaded successfully
                if (result is SuccessResult) {
                    Log.d(TAG, "Image loaded successfully")
                }
            },
            onError = { _, result ->
                // Image failed to load
                if (result is ErrorResult) {
                    Log.e(TAG, "Error loading image", result.throwable)
                }
            }
        )
    }
    
    /**
     * Enum defining the type of placeholder to use
     */
    enum class PlaceholderType {
        ALBUM,
        ARTIST,
        TRACK,
        PLAYLIST,
        GENERAL
    }
} 