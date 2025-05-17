package chromahub.rhythm.app.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val artworkUri: Uri? = null,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String? = null
) : Parcelable

@Parcelize
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUri: Uri? = null,
    val year: Int = 0,
    val songs: List<Song> = emptyList(),
    val numberOfSongs: Int = 0
) : Parcelable

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val artworkUri: Uri? = null,
    val albums: List<Album> = emptyList(),
    val songs: List<Song> = emptyList()
) : Parcelable

@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList(),
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val artworkUri: Uri? = null
) : Parcelable

// Represents the current playback queue
data class Queue(
    val songs: List<Song>,
    val currentIndex: Int = 0
)

// Represents a location in the app (for the "Living room" feature)
data class PlaybackLocation(
    val id: String,
    val name: String,
    val icon: Int
) 