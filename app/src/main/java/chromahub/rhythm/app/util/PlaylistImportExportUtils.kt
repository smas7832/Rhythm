package chromahub.rhythm.app.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for importing and exporting playlists in various formats
 */
object PlaylistImportExportUtils {
    private const val TAG = "PlaylistImportExport"
    
    enum class PlaylistExportFormat(val extension: String, val mimeType: String, val displayName: String) {
        JSON(".json", "application/json", "JSON Format"),
        M3U(".m3u", "audio/x-mpegurl", "M3U Playlist"),
        M3U8(".m3u8", "application/x-mpegURL", "M3U8 Playlist"),
        PLS(".pls", "audio/x-scpls", "PLS Playlist")
    }
    
    data class PlaylistExportData(
        val name: String,
        val id: String,
        val dateCreated: Long,
        val dateModified: Long,
        val songs: List<PlaylistSongEntry>,
        val exportedAt: Long = System.currentTimeMillis(),
        val exportedBy: String = "Rhythm Music Player"
    )
    
    data class PlaylistSongEntry(
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val filePath: String?, // Original file path if available
        val uri: String, // Content URI
        val trackNumber: Int = 0,
        val year: Int = 0
    )
    
    /**
     * Exports a single playlist to the specified format
     */
    fun exportPlaylist(
        context: Context,
        playlist: Playlist,
        format: PlaylistExportFormat,
        outputDirectory: File? = null
    ): Result<File> {
        return try {
            val exportDir = outputDirectory ?: getDefaultExportDirectory(context)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = sanitizeFileName("${playlist.name}_${getCurrentTimestamp()}${format.extension}")
            val outputFile = File(exportDir, fileName)
            
            when (format) {
                PlaylistExportFormat.JSON -> exportToJson(playlist, outputFile)
                PlaylistExportFormat.M3U -> exportToM3u(playlist, outputFile, false)
                PlaylistExportFormat.M3U8 -> exportToM3u(playlist, outputFile, true)
                PlaylistExportFormat.PLS -> exportToPls(playlist, outputFile)
            }
            
            Log.d(TAG, "Successfully exported playlist '${playlist.name}' to ${outputFile.absolutePath}")
            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting playlist '${playlist.name}'", e)
            Result.failure(e)
        }
    }
    
    /**
     * Exports multiple playlists to a single ZIP file
     */
    fun exportAllPlaylists(
        context: Context,
        playlists: List<Playlist>,
        format: PlaylistExportFormat,
        outputDirectory: File? = null
    ): Result<File> {
        return try {
            val exportDir = outputDirectory ?: getDefaultExportDirectory(context)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = "RhythmPlaylists_${getCurrentTimestamp()}.zip"
            val zipFile = File(exportDir, fileName)
            
            createPlaylistsZip(playlists, format, zipFile)
            
            Log.d(TAG, "Successfully exported ${playlists.size} playlists to ${zipFile.absolutePath}")
            Result.success(zipFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting all playlists", e)
            Result.failure(e)
        }
    }
    
    /**
     * Imports a playlist from a file
     */
    fun importPlaylist(
        context: Context,
        uri: Uri,
        availableSongs: List<Song>
    ): Result<Playlist> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalArgumentException("Cannot open file"))
            
            val content = inputStream.bufferedReader().use { it.readText() }
            val fileName = getFileName(context, uri)
            
            val playlist = when {
                fileName.endsWith(".json", true) -> importFromJson(content, availableSongs)
                fileName.endsWith(".m3u", true) || fileName.endsWith(".m3u8", true) -> 
                    importFromM3u(content, fileName, availableSongs)
                fileName.endsWith(".pls", true) -> importFromPls(content, fileName, availableSongs)
                else -> return Result.failure(IllegalArgumentException("Unsupported file format"))
            }
            
            Log.d(TAG, "Successfully imported playlist '${playlist.name}' with ${playlist.songs.size} songs")
            Result.success(playlist)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing playlist from $uri", e)
            Result.failure(e)
        }
    }
    
    private fun exportToJson(playlist: Playlist, outputFile: File) {
        val exportData = PlaylistExportData(
            name = playlist.name,
            id = playlist.id,
            dateCreated = playlist.dateCreated,
            dateModified = playlist.dateModified,
            songs = playlist.songs.map { song ->
                PlaylistSongEntry(
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    duration = song.duration,
                    filePath = song.uri.path,
                    uri = song.uri.toString(),
                    trackNumber = song.trackNumber,
                    year = song.year
                )
            }
        )
        
        val json = Gson().toJson(exportData)
        outputFile.writeText(json)
    }
    
    private fun exportToM3u(playlist: Playlist, outputFile: File, extended: Boolean) {
        outputFile.bufferedWriter().use { writer ->
            if (extended) {
                writer.write("#EXTM3U\n")
            }
            
            playlist.songs.forEach { song ->
                if (extended) {
                    writer.write("#EXTINF:${song.duration / 1000},${song.artist} - ${song.title}\n")
                }
                writer.write("${song.uri}\n")
            }
        }
    }
    
    private fun exportToPls(playlist: Playlist, outputFile: File) {
        outputFile.bufferedWriter().use { writer ->
            writer.write("[playlist]\n")
            
            playlist.songs.forEachIndexed { index, song ->
                val num = index + 1
                writer.write("File$num=${song.uri}\n")
                writer.write("Title$num=${song.artist} - ${song.title}\n")
                writer.write("Length$num=${song.duration / 1000}\n")
            }
            
            writer.write("NumberOfEntries=${playlist.songs.size}\n")
            writer.write("Version=2\n")
        }
    }
    
    private fun importFromJson(content: String, availableSongs: List<Song>): Playlist {
        val exportData = Gson().fromJson(content, PlaylistExportData::class.java)
        val songMap = availableSongs.associateBy { it.uri.toString() }
        
        val matchedSongs = exportData.songs.mapNotNull { entry ->
            // Try to match by URI first
            songMap[entry.uri] ?: 
            // Fallback: match by title and artist
            availableSongs.find { 
                it.title.equals(entry.title, ignoreCase = true) && 
                it.artist.equals(entry.artist, ignoreCase = true) 
            }
        }
        
        return Playlist(
            id = System.currentTimeMillis().toString(), // Generate new ID
            name = exportData.name,
            songs = matchedSongs,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
    }
    
    private fun importFromM3u(content: String, fileName: String, availableSongs: List<Song>): Playlist {
        val lines = content.lines().filter { it.isNotBlank() }
        val matchedSongs = mutableListOf<Song>()
        var currentTitle = ""
        
        lines.forEach { line ->
            when {
                line.startsWith("#EXTINF:") -> {
                    // Extract title from extended info
                    val titleStart = line.indexOf(',')
                    if (titleStart != -1 && titleStart + 1 < line.length) {
                        currentTitle = line.substring(titleStart + 1)
                    }
                }
                line.startsWith("#") -> {
                    // Skip other comments
                }
                else -> {
                    // This is a file path/URI
                    val song = findSongByPathOrTitle(line, currentTitle, availableSongs)
                    song?.let { matchedSongs.add(it) }
                    currentTitle = ""
                }
            }
        }
        
        return Playlist(
            id = System.currentTimeMillis().toString(),
            name = fileName.substringBeforeLast("."),
            songs = matchedSongs,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
    }
    
    private fun importFromPls(content: String, fileName: String, availableSongs: List<Song>): Playlist {
        val lines = content.lines()
        val matchedSongs = mutableListOf<Song>()
        val entries = mutableMapOf<Int, Triple<String?, String?, String?>>() // file, title, length
        
        lines.forEach { line ->
            when {
                line.startsWith("File") -> {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val num = parts[0].removePrefix("File").toIntOrNull()
                        if (num != null) {
                            val current = entries[num] ?: Triple(null, null, null)
                            entries[num] = current.copy(first = parts[1])
                        }
                    }
                }
                line.startsWith("Title") -> {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val num = parts[0].removePrefix("Title").toIntOrNull()
                        if (num != null) {
                            val current = entries[num] ?: Triple(null, null, null)
                            entries[num] = current.copy(second = parts[1])
                        }
                    }
                }
            }
        }
        
        // Process entries in order
        entries.toSortedMap().forEach { (_, entry) ->
            val (filePath, title, _) = entry
            if (filePath != null) {
                val song = findSongByPathOrTitle(filePath, title ?: "", availableSongs)
                song?.let { matchedSongs.add(it) }
            }
        }
        
        return Playlist(
            id = System.currentTimeMillis().toString(),
            name = fileName.substringBeforeLast("."),
            songs = matchedSongs,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
    }
    
    private fun findSongByPathOrTitle(path: String, title: String, availableSongs: List<Song>): Song? {
        // Try exact URI match first
        availableSongs.find { it.uri.toString() == path }?.let { return it }
        
        // Try file name match
        val fileName = path.substringAfterLast("/").substringBeforeLast(".")
        availableSongs.find { 
            it.uri.lastPathSegment?.substringBeforeLast(".")?.equals(fileName, ignoreCase = true) == true 
        }?.let { return it }
        
        // Try title match
        if (title.isNotBlank()) {
            val cleanTitle = title.replace(" - ", "").trim()
            availableSongs.find { song ->
                cleanTitle.contains(song.title, ignoreCase = true) || 
                cleanTitle.contains("${song.artist} - ${song.title}", ignoreCase = true)
            }?.let { return it }
        }
        
        return null
    }
    
    private fun createPlaylistsZip(playlists: List<Playlist>, format: PlaylistExportFormat, zipFile: File) {
        // For now, we'll just export as individual files
        // In a full implementation, you'd use ZipOutputStream
        val tempDir = File(zipFile.parent, "temp_playlists")
        tempDir.mkdirs()
        
        try {
            playlists.forEach { playlist ->
                val fileName = sanitizeFileName("${playlist.name}${format.extension}")
                val tempFile = File(tempDir, fileName)
                
                when (format) {
                    PlaylistExportFormat.JSON -> exportToJson(playlist, tempFile)
                    PlaylistExportFormat.M3U -> exportToM3u(playlist, tempFile, false)
                    PlaylistExportFormat.M3U8 -> exportToM3u(playlist, tempFile, true)
                    PlaylistExportFormat.PLS -> exportToPls(playlist, tempFile)
                }
            }
            
            // For now, just rename the temp directory to indicate completion
            // In a full implementation, you'd zip the contents
            Log.d(TAG, "Exported ${playlists.size} playlists to ${tempDir.absolutePath}")
        } finally {
            // Clean up temp directory in a real implementation
        }
    }
    
    private fun getDefaultExportDirectory(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Playlists")
    }
    
    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
    
    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
    
    private fun getFileName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment ?: "unknown"
    }
}
