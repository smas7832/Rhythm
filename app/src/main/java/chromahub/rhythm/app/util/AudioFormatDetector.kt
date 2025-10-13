package chromahub.rhythm.app.util

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import chromahub.rhythm.app.data.Song

/**
 * Utility class for detecting advanced audio formats and codecs
 * Detects ALAC, Dolby (AC-3, E-AC-3, Dolby Atmos), DTS, and lossless formats
 */
object AudioFormatDetector {
    private const val TAG = "AudioFormatDetector"

    data class AudioFormatInfo(
        val codec: String = "Unknown",
        val isLossless: Boolean = false,
        val isDolby: Boolean = false,
        val isDTS: Boolean = false,
        val isHiRes: Boolean = false,
        val bitDepth: Int = 0,
        val sampleRateHz: Int = 0,
        val channelCount: Int = 0,
        val formatName: String = "Unknown"
    )

    /**
     * Detect comprehensive audio format information from a URI
     */
    fun detectFormat(context: Context, uri: Uri): AudioFormatInfo {
        var formatInfo = AudioFormatInfo()

        try {
            // Try MediaExtractor first for codec details
            formatInfo = detectUsingMediaExtractor(context, uri) ?: formatInfo
            
            // Fallback to MediaMetadataRetriever for additional metadata
            val retrieverInfo = detectUsingMetadataRetriever(context, uri)
            formatInfo = mergeFormatInfo(formatInfo, retrieverInfo)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting audio format for $uri", e)
        }

        return formatInfo
    }

    /**
     * Detect format using MediaExtractor (best for codec detection)
     */
    private fun detectUsingMediaExtractor(context: Context, uri: Uri): AudioFormatInfo? {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            
            // Find audio track
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                
                if (mime.startsWith("audio/")) {
                    return parseMediaFormat(format, mime)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaExtractor failed: ${e.message}")
        } finally {
            try {
                extractor.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        return null
    }

    /**
     * Parse MediaFormat to extract codec and quality information
     */
    private fun parseMediaFormat(format: MediaFormat, mime: String): AudioFormatInfo {
        val sampleRate = format.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: 0
        val channelCount = format.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: 0
        val bitrate = format.getIntegerOrNull(MediaFormat.KEY_BIT_RATE) ?: 0
        
        // Detect codec from MIME type
        val codec = when {
            mime.contains("alac", ignoreCase = true) -> "ALAC"
            mime.contains("flac", ignoreCase = true) -> "FLAC"
            mime.contains("opus", ignoreCase = true) -> "Opus"
            mime.contains("vorbis", ignoreCase = true) -> "Vorbis"
            mime.contains("ac3", ignoreCase = true) -> "AC-3"
            mime.contains("eac3", ignoreCase = true) -> "E-AC-3"
            mime.contains("dts", ignoreCase = true) -> "DTS"
            mime.contains("mp4a", ignoreCase = true) -> "AAC"
            mime.contains("mpeg", ignoreCase = true) -> "MP3"
            mime.contains("raw", ignoreCase = true) -> "PCM"
            else -> mime.substringAfter("/").uppercase()
        }
        
        // Determine if lossless
        val isLossless = codec in listOf("ALAC", "FLAC", "PCM", "WAV", "APE", "DSD")
        
        // Determine if Dolby
        val isDolby = codec in listOf("AC-3", "E-AC-3") || codec.contains("ATMOS", ignoreCase = true)
        
        // Determine if DTS
        val isDTS = codec.contains("DTS", ignoreCase = true)
        
        // Determine if Hi-Res (>= 48kHz sample rate or lossless)
        val isHiRes = sampleRate >= 48000 || isLossless
        
        // Try to get bit depth for lossless formats
        val bitDepth = format.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING) ?: 0
        
        val formatName = when {
            codec == "ALAC" -> "Apple Lossless"
            codec == "FLAC" -> "FLAC Lossless"
            codec == "E-AC-3" -> "Dolby Digital Plus"
            codec == "AC-3" -> "Dolby Digital"
            codec.contains("ATMOS") -> "Dolby Atmos"
            codec == "DTS" -> "DTS Audio"
            codec == "AAC" && bitrate > 256000 -> "AAC High Quality"
            else -> codec
        }
        
        return AudioFormatInfo(
            codec = codec,
            isLossless = isLossless,
            isDolby = isDolby,
            isDTS = isDTS,
            isHiRes = isHiRes,
            bitDepth = bitDepth,
            sampleRateHz = sampleRate,
            channelCount = channelCount,
            formatName = formatName
        )
    }

    /**
     * Detect format using MediaMetadataRetriever as fallback
     */
    private fun detectUsingMetadataRetriever(context: Context, uri: Uri): AudioFormatInfo? {
        val retriever = MediaMetadataRetriever()
        try {
            // Use the correct overload: setDataSource(Context, Uri)
            retriever.setDataSource(context, uri)
            
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 0
            
            // Try to detect codec from file path or mime type
            val codec = when {
                mime?.contains("alac", ignoreCase = true) == true -> "ALAC"
                mime?.contains("flac", ignoreCase = true) == true -> "FLAC"
                mime?.contains("mp4", ignoreCase = true) == true -> "AAC" // Could be ALAC in MP4 container
                mime?.contains("mpeg", ignoreCase = true) == true -> "MP3"
                mime?.contains("ogg", ignoreCase = true) == true -> "OGG Vorbis"
                else -> "Unknown"
            }
            
            val isLossless = codec in listOf("ALAC", "FLAC", "PCM", "WAV")
            val isHiRes = sampleRate >= 48000 || isLossless
            
            return AudioFormatInfo(
                codec = codec,
                isLossless = isLossless,
                isDolby = false,
                isDTS = false,
                isHiRes = isHiRes,
                sampleRateHz = sampleRate,
                formatName = codec
            )
        } catch (e: Exception) {
            Log.w(TAG, "MediaMetadataRetriever failed: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        return null
    }

    /**
     * Merge two AudioFormatInfo objects, preferring non-default values
     */
    private fun mergeFormatInfo(primary: AudioFormatInfo, fallback: AudioFormatInfo?): AudioFormatInfo {
        if (fallback == null) return primary
        
        return AudioFormatInfo(
            codec = if (primary.codec != "Unknown") primary.codec else fallback.codec,
            isLossless = primary.isLossless || fallback.isLossless,
            isDolby = primary.isDolby || fallback.isDolby,
            isDTS = primary.isDTS || fallback.isDTS,
            isHiRes = primary.isHiRes || fallback.isHiRes,
            bitDepth = if (primary.bitDepth > 0) primary.bitDepth else fallback.bitDepth,
            sampleRateHz = if (primary.sampleRateHz > 0) primary.sampleRateHz else fallback.sampleRateHz,
            channelCount = if (primary.channelCount > 0) primary.channelCount else fallback.channelCount,
            formatName = if (primary.formatName != "Unknown") primary.formatName else fallback.formatName
        )
    }

    /**
     * Detect ALAC specifically (which often uses .m4a container)
     */
    fun isALAC(context: Context, uri: Uri): Boolean {
        // Check file extension first
        val path = uri.toString()
        if (path.endsWith(".alac", ignoreCase = true)) return true
        
        // For .m4a files, need to check the codec
        if (path.endsWith(".m4a", ignoreCase = true)) {
            val formatInfo = detectFormat(context, uri)
            return formatInfo.codec == "ALAC"
        }
        
        return false
    }

    /**
     * Helper extension to safely get integer from MediaFormat
     */
    private fun MediaFormat.getIntegerOrNull(key: String): Int? {
        return try {
            if (containsKey(key)) getInteger(key) else null
        } catch (e: Exception) {
            null
        }
    }
}
