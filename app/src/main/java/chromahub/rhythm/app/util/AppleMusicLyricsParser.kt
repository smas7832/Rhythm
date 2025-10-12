package chromahub.rhythm.app.util

import android.util.Log
import chromahub.rhythm.app.network.AppleMusicLyricsLine
import chromahub.rhythm.app.network.AppleMusicWord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppleMusicLyricsParser {
    private const val TAG = "AppleMusicLyricsParser"

    /**
     * Parses Apple Music word-by-word lyrics JSON into structured format
     * @param jsonContent JSON string containing Apple Music lyrics data
     * @return List of parsed word-level lyrics, or empty if parsing fails
     */
    fun parseWordByWordLyrics(jsonContent: String): List<WordByWordLyricLine> {
        if (jsonContent.isBlank()) return emptyList()
        
        return try {
            val gson = Gson()
            val listType = object : TypeToken<List<AppleMusicLyricsLine>>() {}.type
            val appleMusicLines: List<AppleMusicLyricsLine> = gson.fromJson(jsonContent, listType)
            
            appleMusicLines.mapNotNull { line ->
                val words = line.text?.map { word ->
                    WordByWordWord(
                        text = word.text,
                        isPart = word.part ?: false,
                        timestamp = word.timestamp,
                        endtime = word.endtime
                    )
                } ?: emptyList()
                
                if (words.isNotEmpty()) {
                    WordByWordLyricLine(
                        words = words,
                        lineTimestamp = line.timestamp ?: 0L,
                        lineEndtime = line.endtime ?: 0L,
                        background = line.background ?: false
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Apple Music word-by-word lyrics", e)
            emptyList()
        }
    }
    
    /**
     * Convert word-by-word lyrics to plain text (for display when word highlighting is not needed)
     */
    fun toPlainText(wordByWordLines: List<WordByWordLyricLine>): String {
        return wordByWordLines.joinToString("\n") { line ->
            line.words.joinToString("") { word ->
                if (word.isPart && word.text.isNotEmpty()) {
                    word.text // syllable, no space before
                } else {
                    " ${word.text}"
                }
            }.trim()
        }
    }
    
    /**
     * Convert word-by-word lyrics to LRC format (for compatibility)
     */
    fun toLRCFormat(wordByWordLines: List<WordByWordLyricLine>): String {
        return wordByWordLines.joinToString("\n") { line ->
            val timestamp = formatLRCTimestamp(line.lineTimestamp)
            val text = line.words.joinToString("") { word ->
                if (word.isPart && word.text.isNotEmpty()) {
                    word.text
                } else {
                    " ${word.text}"
                }
            }.trim()
            "[$timestamp]$text"
        }
    }
    
    private fun formatLRCTimestamp(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = (milliseconds % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, millis)
    }
}

/**
 * Represents a line of lyrics with word-level timing
 */
data class WordByWordLyricLine(
    val words: List<WordByWordWord>,
    val lineTimestamp: Long,
    val lineEndtime: Long,
    val background: Boolean = false
)

/**
 * Represents a single word with precise timing
 */
data class WordByWordWord(
    val text: String,
    val isPart: Boolean, // true if this is a syllable/part of a split word
    val timestamp: Long, // start time in milliseconds
    val endtime: Long // end time in milliseconds
)
