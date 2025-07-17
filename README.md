<div align="center">
  <h1>🎵 Rhythm - Material 3 Music Player</h1>
  
  [![GitHub release (latest by date)](https://img.shields.io/github/v/release/cromaguy/Rhythm?style=for-the-badge&logo=github&color=9c27b0)](https://github.com/cromaguy/Rhythm/releases/latest)
  [![IzzyOnDroid](https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/chromahub.rhythm.app&style=for-the-badge&logo=android&label=IzzySoft&color=3f51b5)](https://apt.izzysoft.de/fdroid/index/apk/chromahub.rhythm.app)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge&logo=open-source-initiative&logoColor=white)](LICENSE)
  [![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=for-the-badge&logo=android)](https://android-arsenal.com/api?level=26)
  [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
  
  <img src="assets/Banner.png" alt="Rhythm Logo" width="600"/>
  
  ### A next-generation Android music player showcasing Material 3 design, modern architecture, and cutting-edge features

  ---

## 📊 Project Stats

![GitHub stars](https://img.shields.io/github/stars/cromaguy/Rhythm?style=social)
![GitHub forks](https://img.shields.io/github/forks/cromaguy/Rhythm?style=social)
![GitHub issues](https://img.shields.io/github/issues/cromaguy/Rhythm)
![GitHub pull requests](https://img.shields.io/github/issues-pr/cromaguy/Rhythm)
![GitHub code size](https://img.shields.io/github/languages/code-size/cromaguy/Rhythm)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/cromaguy/Rhythm)

---

## 🔗 Quick Links

- 🏠 **Repository**: [GitHub](https://github.com/cromaguy/Rhythm)
- 📱 **Latest Release**: [Download](https://github.com/cromaguy/Rhythm/releases/latest)
- 🐛 **Bug Reports**: [Issues](https://github.com/cromaguy/Rhythm/issues)
- 💬 **Discussions**: [TG Community](https://t.me/RhythmSupport)
- 🛡️ **Security**: [Security Policy](https://github.com/cromaguy/Rhythm/security/policy)
- 📋 **Project Board**: [Development Progress](https://github.com/cromaguy/Rhythm/projects)

---
  
  <p align="center">
    <a href="#-features">Features</a> •
    <a href="#-screenshots">Screenshots</a> •
    <a href="#-download">Download</a> •
    <a href="#-technology-stack">Tech Stack</a> •
    <a href="#-building">Building</a> •
    <a href="#-contributing">Contributing</a> •
    <a href="#-credits">Credits</a>
  </p>
</div>

---

## ✨ Features

### 🎨 **Modern Design & User Experience**
- **Material 3 Design**: Latest Material You design with dynamic theming and wallpaper-based colors
- **Adaptive UI**: Responsive layouts that adapt to different screen sizes and orientations
- **Physics-based Animations**: Smooth, natural animations with spring physics throughout the interface
- **Dynamic Colors**: Wallpaper-based color extraction (Android 12+) with fallback themes
- **Dark/Light Themes**: Automatic system theme following or manual theme selection

### 🎵 **Advanced Audio Playback**
- **High-Quality Audio**: Support for lossless formats (FLAC, ALAC) and high-bitrate files
- **Gapless Playback**: Seamless transitions between tracks using ExoPlayer's advanced buffering
- **Audio Focus Management**: Proper handling of phone calls, notifications, and other audio interruptions
- **Background Playback**: Continuous music playback with memory-efficient foreground service
- **System/App Volume Control**: Choose between controlling system media volume or app-specific volume
- **Equalizer Integration**: System equalizer support for audio customization

### 📚 **Comprehensive Library Management**
- **Smart Scanning**: Fast, efficient music library indexing with metadata extraction from ID3 tags
- **Multiple Views**: Grid and list views for albums, songs, and playlists with customizable layouts
- **Advanced Sorting**: Sort by title, artist, album, date, duration, genre, and more
- **Smart Search**: Real-time search across songs, albums, artists, and playlists with fuzzy matching
- **Playlist Management**: Create, edit, reorder, and delete custom playlists with drag-and-drop support

### 🎤 **Lyrics & Metadata**
- **Synchronized Lyrics**: Time-synced lyrics display with real-time highlighting and smooth scrolling
- **Online Lyrics Integration**: Automatic lyrics fetching from LRCLib and other sources
- **Offline/Online Modes**: Choose between cached lyrics or always-online fetching
- **Rich Metadata**: Display detailed song information including bitrate, format, file size, and encoding
- **Album Artwork**: High-quality artwork display with multiple fallback sources (Spotify, Last.fm, MusicBrainz, YouTube Music)

### 🔄 **Smart Features**
- **Advanced Queue Management**: Queue with history, upcoming tracks, and manual reordering
- **Multiple Shuffle Modes**: True shuffle, album shuffle, and artist shuffle algorithms
- **Repeat Modes**: Track repeat, playlist repeat, and no repeat with visual indicators
- **Recently Played**: Track listening history with timestamps and play counts
- **Auto-Resume**: Restore playback state, position, and queue after app restart

### 🌐 **Online Integration**
- **Automatic Updates**: GitHub-based update checking with stable/beta channels and in-app installation
- **API Integration**: Support for Spotify RapidAPI, Last.fm, MusicBrainz, YouTube Music, and other services
- **Enhanced Metadata**: Online artwork and artist information fetching with intelligent caching
- **Community Features**: Integrated bug reporting and feature requests via GitHub Issues

---


## 📱 Screenshots

<table>
  <tr>
    <td align="center">
      <img src="assets/ScreenShots/Home.png" alt="Home Screen" width="200"><br>
      <b>🏠 Home Screen</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Home2.png" alt="Home Alternative" width="200"><br>
      <b>🎵 Alternative Layout</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Artist.png" alt="Artist View" width="200"><br>
      <b>🎤 Artist View</b>
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
      <b>🔄 Auto Updater</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="assets/ScreenShots/Search.png" alt="Search" width="200"><br>
      <b>🔍 Smart Search</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Queue.png" alt="Queue Management" width="200"><br>
      <b>⏭️ Queue Management</b>
    </td>
    <td align="center">
      <img src="assets/ScreenShots/Settings.png" alt="Settings" width="200"><br>
      <b>⚙️ Settings</b>
    </td>
  </tr>
</table>

---


## 🛠 Technology Stack

### 🎨 **UI Framework**
- **Jetpack Compose**: Modern declarative UI toolkit for native Android development
- **Material 3**: Latest Material Design components with dynamic theming and Material You
- **Coil**: Efficient image loading and caching with Compose integration

### 🏗️ **Architecture**
- **MVVM Pattern**: Model-View-ViewModel architecture for clean separation of concerns
- **Clean Architecture**: Layered architecture with clear dependency rules
- **Coroutines & Flow**: Asynchronous programming with structured concurrency
- **StateFlow**: Reactive state management for UI updates

### 🎵 **Media & Data**
- **ExoPlayer (Media3)**: Advanced media playback engine with gapless support and audio focus
- **MediaMetadataRetriever**: Audio metadata extraction and ID3 tag processing
- **Gson**: JSON serialization for data persistence and API communication
- **DataStore**: Modern preference storage replacing SharedPreferences

### 🌐 **Integrations**
- **GitHub API**: Automatic app updates and release management
- **LRCLib**: Free lyrics service integration with synchronized display
- **Spotify RapidAPI**: Enhanced artist images and metadata (optional)
- **MusicBrainz**: Open music database for accurate metadata
- **Last.fm**: Artist information and high-quality images
- **YouTube Music**: Fallback for artist images, album art, and track images

### 📊 **Data Management**
- **File-based Storage**: Music library indexing and playlist management
- **Memory Caching**: Efficient artwork and metadata caching strategies
- **Storage Access Framework**: Modern file access for Android 11+ compatibility

---

## 📥 Download

### 🚀 **Latest Release**
Get the latest version from our [GitHub Releases](https://github.com/cromaguy/Rhythm/releases/latest)


### 🎯 **Installation Methods**

#### **📱 Direct APK Download**
1. Visit our [Releases page](https://github.com/cromaguy/Rhythm/releases)
2. Download the latest `app-release.apk`
3. Enable "Unknown sources" in Android settings
4. Install the APK

#### **🔒 IzzyOnDroid F-Droid Repository**
1. Add IzzyOnDroid repo: `https://apt.izzysoft.de/fdroid/repo`
2. Search for "Rhythm Music Player"
3. Install directly through F-Droid client


### 📋 **System Requirements**
- **Android Version**: 7.1 (API 26) or higher
- **Storage**: 50MB+ for app installation
- **RAM**: 2GB+ recommended for smooth performance

---

## 🔧 Setup & Configuration

### 🎵 **First Launch**
1. **Grant Permissions**: Allow storage access for music scanning
2. **Library Scan**: Automatic music discovery from device storage
3. **Theme Selection**: Choose your preferred theme and color scheme

### ⚙️ **Settings Overview**

#### **🎨 Appearance**
- **System Theme**: Follow Android's Material You colors
- **Dynamic Colors**: Wallpaper-based color extraction (Android 12+)
- **Dark Mode**: Manual dark/light theme selection
- **Album View Type**: Choose between grid and list layouts

#### **🎵 Playback**
- **Show Lyrics**: Enable/disable synchronized lyrics display
- **Online Lyrics**: Choose between offline and online lyrics fetching
- **System Volume**: Control device media volume or app-specific volume
- **Equalizer**: Access system audio equalizer

#### **🔄 Updates**
- **Auto Check**: Automatic update checking from GitHub releases
- **Update Channel**: Choose between stable and beta release channels

### 🔑 **API Configuration (Optional)**
- **Spotify RapidAPI**: Enhanced artwork and metadata with personal API key
- **Custom Keys**: Configure your own API keys for extended features

---

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/chromahub/rhythm/app/
│   │   │   ├── data/              # Data models and repositories
│   │   │   ├── network/           # Network services and API clients
│   │   │   ├── service/           # Background media playback service
│   │   │   ├── ui/                # All UI components
│   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   ├── screens/       # App screens
│   │   │   │   └── theme/         # Theming and styling
│   │   │   ├── util/              # Utility classes
│   │   │   ├── viewmodel/         # ViewModels
│   │   │   └── MainActivity.kt    # App entry point
│   │   ├── res/                   # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml    # App manifest
│   ├── test/                      # Unit tests
│   └── androidTest/               # Instrumentation tests
├── build.gradle.kts               # App-level build configuration
└── proguard-rules.pro            # ProGuard rules for release builds
```

---

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help make Rhythm even better:

### 🐛 **Bug Reports**
- Use the [Issue Tracker](https://github.com/cromaguy/Rhythm/issues) to report bugs
- Include detailed steps to reproduce the issue
- Provide device information and Android version
- Attach logs or screenshots if applicable

### 💡 **Feature Requests**
- Check existing issues to avoid duplicates
- Clearly describe the proposed feature and its benefits
- Consider implementation complexity and user impact

### 🔧 **Code Contributions**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes following our coding standards
4. Test thoroughly on different devices
5. Commit with clear, descriptive messages
6. Push to your branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request with detailed description

### 📋 **Development Guidelines**
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Jetpack Compose best practices
- Ensure Material 3 design consistency
- Write unit tests for new features
- Update documentation as needed
- Test on multiple screen sizes and Android versions

---

## 🏆 Credits & Acknowledgments

### 👨‍💻 **Development Team**
- **[Anjishnu Nandi](https://github.com/cromaguy)** - Lead Developer & Project Architect
  - Core development and UI/UX design
  - Architecture planning and implementation
  - Material 3 design system integration

### 🌟 **Special Thanks**
- **Google Material Design Team** - For Material 3 design principles and components
- **Android Open Source Project** - For providing the foundation of Android development
- **Jetpack Compose Team** - For the modern declarative UI toolkit
- **Open Source Community** - For inspiration, libraries, and continuous support

### 📚 **Core Libraries & Dependencies**
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern UI toolkit for native Android
- **[Material 3](https://m3.material.io/)** - Design system and components
- **[ExoPlayer](https://exoplayer.dev/)** - Professional-grade media playback
- **[Coil](https://coil-kt.github.io/coil/)** - Image loading optimized for Android and Compose
- **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** - Asynchronous programming

### 🎵 **Audio & Metadata Services**
- **[LRCLib](https://lrclib.net/)** - Free, community-driven lyrics service
- **[MusicBrainz](https://musicbrainz.org/)** - Open music encyclopedia
- **[Last.fm](https://www.last.fm/)** - Music discovery and artist information
- **[Cover Art Archive](https://coverartarchive.org/)** - Community-driven album artwork database
- **[YouTube Music](https://music.youtube.com/)** - Fallback source for artist images, album art, and track thumbnails
- **[Spotify Web API](https://developer.spotify.com/documentation/web-api)** - Enhanced metadata and artwork

---

## 🔐 Permissions

The app requires the following permissions for optimal functionality:

### **📁 Storage Access**
- `READ_MEDIA_AUDIO` (Android 13+) - Access music files
- `READ_MEDIA_IMAGES` (Android 13+) - Access album artwork
- `READ_EXTERNAL_STORAGE` (Android 12 and below) - Legacy storage access
- `WRITE_EXTERNAL_STORAGE` (Android 10 and below) - Legacy storage write access

### **🎵 Media Playback**
- `FOREGROUND_SERVICE` - Background music playback
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Media-specific foreground service
- `WAKE_LOCK` - Prevent device sleep during playback

### **🌐 Network Features**
- `INTERNET` - Online features (lyrics, updates, artwork)
- `ACCESS_NETWORK_STATE` - Network connectivity checks

### **🔄 Updates & Installation**
- `REQUEST_INSTALL_PACKAGES` - In-app update installation

### **📡 Bluetooth Integration**
- `BLUETOOTH` - Bluetooth device detection
- `BLUETOOTH_ADMIN` - Bluetooth connection management
- `BLUETOOTH_CONNECT` (Android 12+) - Connect to Bluetooth devices
- `BLUETOOTH_SCAN` (Android 12+) - Scan for Bluetooth devices

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Team ChromaHub

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


<div align="center">
  <h3>🎵 Experience music like never before with Rhythm! 🎵</h3>
  
  **[⬇️ Download Now](https://github.com/cromaguy/Rhythm/releases/latest)** | **[⭐ Star on GitHub](https://github.com/cromaguy/Rhythm)** | **[🐛 Report Issues](https://github.com/cromaguy/Rhythm/issues)**
  
  <br>
  
  <sub>Made with ❤️ by <a href="https://github.com/cromaguy">Anjishnu Nandi</a> and the <strong>Team ChromaHub</strong></sub>
  
  <br><br>
  
  <img src="https://img.shields.io/badge/Built%20with-❤️%20%26%20Kotlin-purple?style=for-the-badge" alt="Built with Love and Kotlin">
</div>
