package chromahub.rhythm.app.util

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Utility class for managing app cache operations
 */
object CacheManager {
    private const val TAG = "CacheManager"
    
    /**
     * Clears all cached data including:
     * - Internal cache directory
     * - External cache directory (if available)
     * - Coil image cache (memory and disk)
     * 
     * @param context Application context
     * @param imageLoader Coil ImageLoader instance for clearing image cache
     */
    @OptIn(ExperimentalCoilApi::class)
    suspend fun clearAllCache(context: Context, imageLoader: ImageLoader? = null) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting cache cleanup...")
            
            // Clear internal cache directory
            clearCacheDirectory(context.cacheDir)
            
            // Clear external cache directory if available
            context.externalCacheDir?.let { externalCache ->
                clearCacheDirectory(externalCache)
            }
            
            // Clear Coil image cache
            imageLoader?.let { loader ->
                try {
                    // Clear memory cache
                    loader.memoryCache?.clear()
                    Log.d(TAG, "Cleared Coil memory cache")
                    
                    // Clear disk cache
                    loader.diskCache?.clear()
                    Log.d(TAG, "Cleared Coil disk cache")
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing Coil cache", e)
                }
            }
            
            Log.d(TAG, "Cache cleanup completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during cache cleanup", e)
        }
    }
    
    /**
     * Clears a specific cache directory recursively
     * 
     * @param cacheDir The cache directory to clear
     */
    private fun clearCacheDirectory(cacheDir: File) {
        try {
            if (!cacheDir.exists()) {
                Log.d(TAG, "Cache directory doesn't exist: ${cacheDir.absolutePath}")
                return
            }
            
            val filesDeleted = deleteRecursively(cacheDir, false) // Don't delete the directory itself
            Log.d(TAG, "Cleared cache directory: ${cacheDir.absolutePath}, files deleted: $filesDeleted")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache directory: ${cacheDir.absolutePath}", e)
        }
    }
    
    /**
     * Recursively deletes files and directories
     * 
     * @param file The file or directory to delete
     * @param deleteRoot Whether to delete the root directory itself
     * @return Number of files/directories deleted
     */
    private fun deleteRecursively(file: File, deleteRoot: Boolean = true): Int {
        var deletedCount = 0
        
        try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    deletedCount += deleteRecursively(child, true)
                }
                
                if (deleteRoot && file.delete()) {
                    deletedCount++
                }
            } else {
                if (file.delete()) {
                    deletedCount++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${file.absolutePath}", e)
        }
        
        return deletedCount
    }
    
    /**
     * Gets the total size of cache directories in bytes
     * 
     * @param context Application context
     * @return Total cache size in bytes
     */
    suspend fun getCacheSize(context: Context): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        
        try {
            // Internal cache size
            totalSize += getDirectorySize(context.cacheDir)
            
            // External cache size
            context.externalCacheDir?.let { externalCache ->
                totalSize += getDirectorySize(externalCache)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
        }
        
        return@withContext totalSize
    }
    
    /**
     * Calculates the size of a directory recursively
     * 
     * @param directory The directory to calculate size for
     * @return Size in bytes
     */
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        
        try {
            if (!directory.exists()) return 0L
            
            if (directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    size += if (file.isDirectory) {
                        getDirectorySize(file)
                    } else {
                        file.length()
                    }
                }
            } else {
                size = directory.length()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating directory size: ${directory.absolutePath}", e)
        }
        
        return size
    }
    
    /**
     * Formats bytes into a human-readable string
     * 
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return if (unitIndex == 0) {
            "${size.toInt()} ${units[unitIndex]}"
        } else {
            "%.1f %s".format(size, units[unitIndex])
        }
    }
    
    /**
     * Clears only image-related cache
     * 
     * @param context Application context
     * @param imageLoader Coil ImageLoader instance
     */
    @OptIn(ExperimentalCoilApi::class)
    suspend fun clearImageCache(context: Context, imageLoader: ImageLoader? = null) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Clearing image cache...")
            
            // Clear Coil image cache
            imageLoader?.let { loader ->
                try {
                    loader.memoryCache?.clear()
                    loader.diskCache?.clear()
                    Log.d(TAG, "Cleared Coil image cache")
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing Coil image cache", e)
                }
            }
            
            // Clear generated placeholder images from internal cache
            val cacheDir = context.cacheDir
            cacheDir.listFiles { _, name ->
                name.startsWith("placeholder_") && name.endsWith(".png")
            }?.forEach { file ->
                if (file.delete()) {
                    Log.d(TAG, "Deleted placeholder image: ${file.name}")
                }
            }
            
            Log.d(TAG, "Image cache cleanup completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing image cache", e)
        }
    }
}
