package chromahub.rhythm.app.util

import android.util.Log
import java.util.regex.Pattern

object LyricsParser {

    // Enhanced regex pattern to support various LRC timestamp formats
    private val timestampPattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})[.:]?(\\d{0,3})\\]")
    private val metadataPattern = Pattern.compile("\\[(ar|ti|al|by|offset|re|ve):[^\\]]*\\]")

    fun parseLyrics(lrcContent: String): List<LyricLine> {
        if (lrcContent.isBlank()) return emptyList()
        
        val lyricLines = mutableListOf<LyricLine>()
        val lines = lrcContent.trim().split("\n")

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
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0
                val millisecondsStr = matcher.group(3) ?: ""
                
                // Handle different millisecond formats
                val milliseconds = when {
                    millisecondsStr.isEmpty() -> 0
                    millisecondsStr.length == 1 -> millisecondsStr.toLong() * 100  // [mm:ss.x]
                    millisecondsStr.length == 2 -> millisecondsStr.toLong() * 10   // [mm:ss.xx]
                    millisecondsStr.length == 3 -> millisecondsStr.toLong()        // [mm:ss.xxx]
                    else -> 0
                }
                
                val timestamp = (minutes * 60 * 1000) + (seconds * 1000) + milliseconds
                timestamps.add(timestamp)
                lastMatchEnd = matcher.end()
            }

            // Extract lyrics text after all timestamps
            if (timestamps.isNotEmpty()) {
                val text = trimmedLine.substring(lastMatchEnd).trim()
                
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
