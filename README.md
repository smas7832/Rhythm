# Rhythm - Modern Android Music Player

<div align="center">
  <img src="app/rhythm_logo.png" alt="Rhythm Logo" width="200"/>
</div>

## Overview

Rhythm is a modern, feature-rich Android music player built with Jetpack Compose and Material 3 design principles. It offers a beautiful user interface combined with powerful audio playback capabilities using Media3.

## Features

- 🎨 **Modern UI**: Built with Jetpack Compose and Material 3 design
- 🎵 **Comprehensive Audio Support**: Plays various formats including MP3, AAC, FLAC, OGG, WAV
- 📱 **Android 13+ Support**: Targets Android API 34 with minimum SDK 26
- 🎯 **Media3 Integration**: Robust media playback using ExoPlayer
- 📂 **Playlist Management**: Support for various playlist formats
- 🔄 **Background Playback**: Continuous playback with foreground service
- 🎨 **Customizable Interface**: Material 3 theming and animations
- 🔊 **Output Switcher**: Seamless audio output device switching
- 📱 **Bluetooth Support**: Enhanced Bluetooth device connectivity
- 🌐 **Network Features**: Internet connectivity for extended functionality

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Media Playback**: Media3 ExoPlayer
- **Navigation**: Compose Navigation
- **Image Loading**: Coil
- **Networking**: Retrofit2 + OkHttp3
- **JSON Parsing**: Gson
- **Animations**: Compose Animation
- **Drag & Drop**: Reorderable Compose

### Requirements
- Android 12+
- 50MB+ free storage space

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

Current version: 1.8.100.4 (Build 1080153)

<div align="center">
  <sub>Made with ❤️ by Anjishnu</sub>
</div>
