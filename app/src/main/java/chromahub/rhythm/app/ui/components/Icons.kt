package chromahub.rhythm.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Queue
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune

/**
 * Material Design 3 icons for the Rhythm app
 */
object RhythmIcons {
    /**
     * Player controls
     */
    object Player {
        // Standard player controls
        val Play = Icons.Rounded.PlayArrow
        val Pause = Icons.Rounded.Pause
        val SkipNext = Icons.Rounded.SkipNext
        val SkipPrevious = Icons.Rounded.SkipPrevious
        val Shuffle = Icons.Rounded.Shuffle
        val Repeat = Icons.Rounded.Repeat
        val RepeatOne = Icons.Rounded.RepeatOne
        
        // Volume controls
        val VolumeUp = Icons.AutoMirrored.Rounded.VolumeUp
        val VolumeDown = Icons.AutoMirrored.Rounded.VolumeDown
        val VolumeMute = Icons.AutoMirrored.Rounded.VolumeMute
        val VolumeOff = Icons.AutoMirrored.Rounded.VolumeOff
        
        // Additional player controls
        val Queue = Icons.Rounded.QueueMusic
        val Equalizer = Icons.Rounded.Equalizer
        val Speed = Icons.Rounded.Speed
        val Timer = Icons.Rounded.Timer
    }
    
    /**
     * Navigation icons
     */
    object Navigation {
        // Bottom navigation
        val Home = Icons.Rounded.Home
        val HomeOutlined = Icons.Outlined.Home
        val Search = Icons.Rounded.Search
        val SearchOutlined = Icons.Outlined.Search
        val Library = Icons.Rounded.LibraryMusic
        val LibraryOutlined = Icons.Outlined.LibraryMusic
        val Settings = Icons.Rounded.Settings
        val SettingsOutlined = Icons.Outlined.Settings
        
        // Navigation actions
        val Back = Icons.AutoMirrored.Rounded.ArrowBack
        val Forward = Icons.AutoMirrored.Rounded.ArrowForward
        val Close = Icons.Rounded.Close
    }
    
    /**
     * Music item icons
     */
    object Music {
        // Music entities
        val Song = Icons.Rounded.MusicNote
        val SongOutlined = Icons.Outlined.Audiotrack
        val Album = Icons.Rounded.Album
        val AlbumOutlined = Icons.Outlined.Album
        val Artist = Icons.Rounded.Person
        val ArtistOutlined = Icons.Outlined.Person
        val Playlist = Icons.AutoMirrored.Rounded.PlaylistAdd
        val PlaylistOutlined = Icons.AutoMirrored.Outlined.PlaylistAdd
        val MusicNote = Icons.Rounded.MusicNote
        val Audiotrack = Icons.Rounded.Audiotrack
    }
    
    /**
     * Action icons
     */
    object Actions {
        // Common actions
        val Favorite = Icons.Rounded.Favorite
        val FavoriteOutlined = Icons.Rounded.FavoriteBorder
        val Add = Icons.Rounded.Add
        val Remove = Icons.Rounded.Remove
        val Edit = Icons.Rounded.Edit
        val Delete = Icons.Rounded.Delete
        val Check = Icons.Rounded.Check
        val More = Icons.Rounded.MoreVert
        val List = Icons.AutoMirrored.Rounded.List
        val Refresh = Icons.Rounded.Refresh
        val Download = Icons.Rounded.Download
        val Tune = Icons.Rounded.Tune
    }
    
    /**
     * Device icons
     */
    object Devices {
        // Audio output devices
        val Bluetooth = Icons.Rounded.Bluetooth
        val BluetoothOutlined = Icons.Outlined.Bluetooth
        val Headphones = Icons.Rounded.Headphones
        val HeadphonesOutlined = Icons.Outlined.Headphones
        val Speaker = Icons.Rounded.Speaker
        val SpeakerOutlined = Icons.Outlined.Speaker
        
        // Location
        val Location = Icons.Rounded.Place
        val LocationOutlined = Icons.Outlined.Place
    }
    
    /**
     * Legacy icons (for backward compatibility)
     * These will be gradually phased out in favor of the categorized icons above
     */
    // Player controls
    val Play = Player.Play
    val Pause = Player.Pause
    val SkipNext = Player.SkipNext
    val SkipPrevious = Player.SkipPrevious
    val Shuffle = Player.Shuffle
    val Repeat = Player.Repeat
    val RepeatOne = Player.RepeatOne
    val VolumeUp = Player.VolumeUp
    val VolumeDown = Player.VolumeDown
    val VolumeMute = Player.VolumeMute
    val VolumeOff = Player.VolumeOff
    
    // Navigation
    val Home = Navigation.HomeOutlined
    val HomeFilled = Navigation.Home
    val Search = Navigation.SearchOutlined
    val SearchFilled = Navigation.Search
    val Library = Navigation.LibraryOutlined
    val Settings = Navigation.SettingsOutlined
    val SettingsFilled = Navigation.Settings
    
    // Music items
    val Song = Music.SongOutlined
    val SongFilled = Music.Song
    val Album = Music.AlbumOutlined
    val Artist = Music.ArtistOutlined
    val ArtistFilled = Music.Artist
    val Playlist = Music.PlaylistOutlined
    
    // Actions
    val Favorite = Actions.FavoriteOutlined
    val FavoriteFilled = Actions.Favorite
    val Add = Actions.Add
    val Remove = Actions.Remove
    val Edit = Actions.Edit
    val Delete = Actions.Delete
    val AddToPlaylist = Music.Playlist
    val More = Actions.More
    val Queue = Player.Queue
    val Check = Actions.Check
    val Close = Navigation.Close
    val Back = Navigation.Back
    val Forward = Navigation.Forward
    val Download = Actions.Download
    val List = Actions.List
    val Refresh = Actions.Refresh
    
    // Location
    val Location = Devices.LocationOutlined
    val LocationFilled = Devices.Location
    
    // Audio devices
    val Bluetooth = Devices.BluetoothOutlined
    val BluetoothFilled = Devices.Bluetooth
    val Headphones = Devices.HeadphonesOutlined
    val HeadphonesFilled = Devices.Headphones
    val Speaker = Devices.SpeakerOutlined
    val SpeakerFilled = Devices.Speaker
} 