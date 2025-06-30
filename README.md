# Rhythm - Offline Music App

<div align="center">
  <img src="assets/Rhythm.png" alt="Rhythm Logo" width="200"/>
</div>

## Overview

Rhythm is a music player for Android that delivers a seamless audio experience with a beautiful Material You design. Enjoy your music collection with advanced playback features, intuitive controls, and comprehensive audio format support.

## Screenshots

<table>
  <tr>
    <td align="center">
      <img src="assets/ScreenShots/Home.png" alt="Home Screen" width="200"><br>
      <b>🎵 Home Screen 1</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Home2.png" alt="Home Screen" width="200"><br>
      <b>🎵 Home Screen 2</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Home3.png" alt="Home Screen" width="200"><br>
      <b>🎵 Home Screen 3</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="assets/ScreenShots/Player.png" alt="Now Playing" width="200"><br>
      <b>▶️ Now Playing</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Player_Lyrics.png" alt="Lyrics View" width="200"><br>
      <b>📝 Lyrics View</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Playlist.png" alt="Playlist View" width="200"><br>
      <b>🎼 Playlist View</b>
    </td>
  </tr>
  <tr>
      <td align="center">
      <img src="assets/ScreenShots/Library_Songs.png" alt="Songs Library" width="200"><br>
      <b>🎧 Songs Library</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Library_Albums.png" alt="Albums View" width="200"><br>
      <b>💿 Albums View</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Updater.png" alt="Updater" width="200"><br>
      <b>💿 Updater</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="assets/ScreenShots/Search.png" alt="Search" width="200"><br>
      <b>🔍 Search</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Queue.png" alt="Queue Management" width="200"><br>
      <b>⏭️ Queue</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Settings.png" alt="Settings" width="200"><br>
      <b>⚙️ Settings</b>
    </td>
  </tr>
</table>

## Features

- 🎨 **Modern UI**: Built with Jetpack Compose and Material 3 design
- 🎵 **Comprehensive Audio Support**: Plays various formats including MP3, AAC, FLAC, OGG, WAV
- 📱 **Android Support**: Targets Android API 34 with compatibility down to Android 8.0
- 🎯 **Media3 Integration**: Robust media playback using ExoPlayer
- 📂 **Advanced Playlist Management**: Create, edit, and organize playlists
- 🔄 **Background Playback**: Continuous playback with foreground service
- 🎨 **Dynamic Theming**: Material You/Monet theming and fluid animations
- 🔊 **Smart Audio**: Gapless playback, crossfade, audio normalization
- 🎵 **Advanced Features**: Equalizer, high-quality mode, replay gain
- 🔊 **Output Control**: Seamless audio device and Bluetooth switching
- 📊 **Personalization**: Listening stats, mood-based playlists
- 🌐 **Online Integration**: Automatic updates, online lyrics
- 🔄 **Auto-Resume**: Remembers playback state and queue
- 🎨 **Adaptive UI**: Beautiful transitions and responsive design

## Technical Stack

- **Language**: Kotlin with Coroutines
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3 (Material You)
- **Media Playback**: Media3 ExoPlayer
- **Audio Processing**: Media3 Session, Audio Effects
- **Navigation**: Compose Navigation
- **Image Loading**: Coil for async image loading
- **Networking**: Retrofit2 + OkHttp3
- **JSON Parsing**: Gson
- **Animations**: Compose Animation with Physics
- **Drag & Drop**: Reorderable Compose
- **Device Integration**: MediaRouter, Bluetooth APIs
- **Storage**: Android Storage Access Framework
- **Background Services**: Foreground Service for playback

### Requirements

- Android 8.0+ (API level 26)
- 50MB+ free storage space
- Internet connection for online features

### Installation

Download the latest release APK from the [Releases](https://github.com/cromaguy/Rhythm/releases) page.

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/           # Kotlin source files
│   │   ├── res/           # Resources
│   │   └── AndroidManifest.xml
│   ├── test/              # Unit tests
│   └── androidTest/       # Instrumentation tests
├── build.gradle.kts       # App-level build configuration
└── proguard-rules.pro    # ProGuard rules
```

## Permissions

The app requires the following permissions:

- Storage access (for media files)
- Media audio access
- Foreground service (for playback)
- Internet access
- Bluetooth connectivity
- Network state

## 📄 License

Rhythm is available under the MIT license. See the [LICENSE](LICENSE) file for more info.

## Version

Current version: 2.0.100.6 (Build 253 Pre-Release Beta)

## Support

If you encounter any issues or have feature requests, please file them in the [Issues](https://github.com/cromaguy/Rhythm/issues) section.

<div align="center">
  <sub>Made with ❤️ by Anjishnu</sub>
</div>
