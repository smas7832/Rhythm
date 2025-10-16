package chromahub.rhythm.app.util

import android.util.Log
import java.util.regex.Pattern

object LyricsParser {

    // Enhanced regex pattern to support various LRC timestamp formats
    // Supports: [mm:ss.xx], [mm:ss:xx], [mm:ss.xxx], [mm:ss], and even [hh:mm:ss.xxx]
    private val timestampPattern = Pattern.compile("\\[(\\d{1,3}):(\\d{2})(?:[.:]?(\\d{0,3}))?\\]")
    private val metadataPattern = Pattern.compile("\\[(ar|ti|al|by|offset|re|ve|length):[^\\]]*\\]", Pattern.CASE_INSENSITIVE)

    fun parseLyrics(lrcContent: String): List<LyricLine> {
        if (lrcContent.isBlank()) return emptyList()
        
        val lyricLines = mutableListOf<LyricLine>()
        val lines = lrcContent.trim().split("\n", "\r\n", "\r")

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // Skip metadata lines (artist, title, album, etc.)
            if (metadataPattern.matcher(trimmedLine).find()) continue
            
            val matcher = timestampPattern.matcher(trimmedLine)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            // Find all timestamps in the line
            while (matcher.find()) {
                try {
                    val timeValue1 = matcher.group(1)?.toLongOrNull() ?: 0
                    val timeValue2 = matcher.group(2)?.toLongOrNull() ?: 0
                    val millisecondsStr = matcher.group(3) ?: ""
                    
                    // Determine if this is HH:MM:SS or MM:SS format
                    // If timeValue1 > 59, it's likely minutes in MM:SS format
                    val (hours, minutes, seconds) = if (timeValue1 > 59) {
                        // Extended format MM:SS where MM can be > 59 (some songs are very long)
                        Triple(0L, timeValue1, timeValue2)
                    } else {
                        // Could be HH:MM:SS or MM:SS - check for another colon
                        val remainingText = trimmedLine.substring(matcher.start())
                        val colonCount = remainingText.substring(0, matcher.end() - matcher.start()).count { it == ':' }
                        if (colonCount >= 2) {
                            // HH:MM:SS format
                            Triple(timeValue1, timeValue2, millisecondsStr.toLongOrNull() ?: 0)
                        } else {
                            // MM:SS format
                            Triple(0L, timeValue1, timeValue2)
                        }
                    }
                    
                    // Handle different millisecond formats more robustly
                    val milliseconds = when {
                        millisecondsStr.isEmpty() -> 0L
                        millisecondsStr.length == 1 -> millisecondsStr.toLong() * 100  // [mm:ss.x] -> x00ms
                        millisecondsStr.length == 2 -> millisecondsStr.toLong() * 10   // [mm:ss.xx] -> xx0ms
                        millisecondsStr.length == 3 -> millisecondsStr.toLong()        // [mm:ss.xxx] -> xxxms
                        else -> millisecondsStr.substring(0, 3).toLong() // Truncate if too long
                    }
                    
                    // Calculate total timestamp in milliseconds
                    val timestamp = (hours * 3600 * 1000) + (minutes * 60 * 1000) + (seconds * 1000) + milliseconds
                    
                    // Only add valid timestamps (prevent negative or unreasonably large values)
                    if (timestamp >= 0 && timestamp < 86400000) { // Less than 24 hours
                        timestamps.add(timestamp)
                    }
                    lastMatchEnd = matcher.end()
                } catch (e: Exception) {
                    Log.w("LyricsParser", "Error parsing timestamp in line: $trimmedLine", e)
                    continue
                }
            }

            // Extract lyrics text after all timestamps
            if (timestamps.isNotEmpty()) {
                val text = if (lastMatchEnd < trimmedLine.length) {
                    trimmedLine.substring(lastMatchEnd).trim()
                } else {
                    ""
                }
                
                // Only add non-empty lyrics (skip empty timestamp lines)
                if (text.isNotEmpty()) {
                    for (timestamp in timestamps) {
                        lyricLines.add(LyricLine(timestamp, text))
                    }
                }
            }
        }

        // Sort by timestamp and remove duplicates
        return lyricLines
            .sortedBy { it.timestamp }
            .distinctBy { "${it.timestamp}_${it.text}" } // Remove exact duplicates
    }
    
    /**
     * Validates if the provided content contains valid LRC format timestamps
     */
    fun isValidLrcFormat(content: String): Boolean {
        if (content.isBlank()) return false
        
        val lines = content.trim().split("\n")
        var timestampCount = 0
        
        for (line in lines) {
            if (timestampPattern.matcher(line.trim()).find()) {
                timestampCount++
                if (timestampCount >= 2) return true // At least 2 timestamped lines
            }
        }
        
        return false
    }
}

data class LyricLine(
    val timestamp: Long,
    val text: String
)
