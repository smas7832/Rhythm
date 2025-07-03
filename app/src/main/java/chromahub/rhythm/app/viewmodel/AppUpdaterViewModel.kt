package chromahub.rhythm.app.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import chromahub.rhythm.app.network.GitHubRelease
import chromahub.rhythm.app.network.NetworkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.content.FileProvider
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import chromahub.rhythm.app.data.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest

/**
 * App version data model
 */
data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val whatsNew: List<String>,
    val knownIssues: List<String>,
    val downloadUrl: String,
    val apkAssetName: String = "",
    val apkSize: Long = 0,
    val releaseNotes: String = "",
    val isPreRelease: Boolean = false,
    val buildNumber: Int = 0
)

data class ReleaseContent(
    val whatsNew: List<String>,
    val knownIssues: List<String>
)

/**
 * Semantic version comparison helper class
 */
private data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val subpatch: Int = 0,
    val buildNumber: Int = 0,
    val isPreRelease: Boolean = false
) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int {
        // Compare major version
        if (major != other.major) return major.compareTo(other.major)
        // Compare minor version
        if (minor != other.minor) return minor.compareTo(other.minor)
        // Compare patch version
        if (patch != other.patch) return patch.compareTo(other.patch)
        // Compare subpatch version
        if (subpatch != other.subpatch) return subpatch.compareTo(other.subpatch)
        // Compare build numbers
        if (buildNumber != other.buildNumber) return buildNumber.compareTo(other.buildNumber)
        // Pre-releases are considered older than regular releases
        if (isPreRelease != other.isPreRelease) {
            return if (isPreRelease) -1 else 1
        }
        return 0
    }
}

/**
 * Download state for tracking download progress and resumption
 */
data class DownloadState(
    val fileName: String,
    val url: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val etag: String?,
    val lastModified: String?,
    val resumePosition: Long
)

/**
 * ViewModel for handling app updates
 */
class AppUpdaterViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AppUpdaterViewModel"
    
    // GitHub repository information
    private val GITHUB_OWNER = "cromaguy"
    private val GITHUB_REPO = "Rhythm"
    
    // Update check interval (6 hours)
    private val UPDATE_CHECK_INTERVAL = TimeUnit.HOURS.toMillis(6)
    
    // API service
    private val gitHubApiService = NetworkManager.createGitHubApiService()
    
    // Last update check timestamp
    private var lastUpdateCheck = 0L
    
    // Active download state
    private var activeDownload: DownloadState? = null
    private var activeCall: Call? = null

    // AppSettings instance
    private val appSettings = AppSettings.getInstance(application.applicationContext)

    // Update channel (stable or beta)
    private lateinit var _updateChannel: MutableStateFlow<String>
    val updateChannel: StateFlow<String> get() = _updateChannel.asStateFlow()
    
    // Current app version info
    private lateinit var _currentVersion: MutableStateFlow<AppVersion>
    val currentVersion: StateFlow<AppVersion> get() = _currentVersion.asStateFlow()
    
    // Latest version info
    private lateinit var _latestVersion: MutableStateFlow<AppVersion?>
    val latestVersion: StateFlow<AppVersion?> get() = _latestVersion.asStateFlow()
    
    // Update check state
    private lateinit var _isCheckingForUpdates: MutableStateFlow<Boolean>
    val isCheckingForUpdates: StateFlow<Boolean> get() = _isCheckingForUpdates.asStateFlow()
    
    // Update available state
    private lateinit var _updateAvailable: MutableStateFlow<Boolean>
    val updateAvailable: StateFlow<Boolean> get() = _updateAvailable.asStateFlow()
    
    // Error state
    private lateinit var _error: MutableStateFlow<String?>
    val error: StateFlow<String?> get() = _error.asStateFlow()
    
    // Download state - true when actively downloading
    private lateinit var _isDownloading: MutableStateFlow<Boolean>
    val isDownloading: StateFlow<Boolean> get() = _isDownloading.asStateFlow()
    
    // Download progress (0-100)
    private lateinit var _downloadProgress: MutableStateFlow<Float>
    val downloadProgress: StateFlow<Float> get() = _downloadProgress.asStateFlow()
    
    // Downloaded file
    private lateinit var _downloadedFile: MutableStateFlow<File?>
    val downloadedFile: StateFlow<File?> get() = _downloadedFile.asStateFlow()
    
    // Download state for tracking download progress and resumption
    private lateinit var _downloadState: MutableStateFlow<DownloadState?>
    val downloadState: StateFlow<DownloadState?> get() = _downloadState.asStateFlow()

    init {
        // Initialize all lateinit properties here
        _updateChannel = MutableStateFlow("stable")
        _currentVersion = MutableStateFlow(
            AppVersion(
                versionName = "2.1.108.274 Beta",
                versionCode = 20208274, // Updated to reflect major*10M + minor*100K + patch*1K + buildNumber
                releaseDate = "2025-07-03",
                whatsNew = emptyList(),
                knownIssues = emptyList(),
                downloadUrl = "",
                isPreRelease = true,
                buildNumber = 274
            )
        )
        _latestVersion = MutableStateFlow<AppVersion?>(null)
        _isCheckingForUpdates = MutableStateFlow(false)
        _updateAvailable = MutableStateFlow(false)
        _error = MutableStateFlow<String?>(null)
        _isDownloading = MutableStateFlow(false)
        _downloadProgress = MutableStateFlow(0f)
        _downloadedFile = MutableStateFlow<File?>(null)
        _downloadState = MutableStateFlow<DownloadState?>(null)

        viewModelScope.launch {
            appSettings.updateChannel.collectLatest { channel ->
                _updateChannel.value = channel
                // Re-check for updates if channel changes
                checkForUpdates(force = true)
            }
        }
    }
    
    /**
     * Check for updates by fetching the latest release from GitHub
     */
    fun checkForUpdates(force: Boolean = false) {
        viewModelScope.launch {
            val autoCheckEnabled = appSettings.autoCheckForUpdates.first()
            val currentChannel = appSettings.updateChannel.first()

            if (!force && !autoCheckEnabled) {
                Log.d(TAG, "Skipping update check - auto-check is disabled and not forced.")
                _isCheckingForUpdates.value = false // Ensure loading state is false
                return@launch
            }

            // Skip check if within update interval unless forced
            if (!force && System.currentTimeMillis() - lastUpdateCheck < UPDATE_CHECK_INTERVAL) {
                Log.d(TAG, "Skipping update check - within interval")
                return@launch
            }
            
            _isCheckingForUpdates.value = true
            _error.value = null
            _latestVersion.value = null  // Clear any previous version data
            
            try {
                val releasesResponse = gitHubApiService.getReleases(GITHUB_OWNER, GITHUB_REPO)
                
                if (releasesResponse.isSuccessful) {
                    val allReleases = releasesResponse.body()
                    
                    if (allReleases.isNullOrEmpty()) {
                        _error.value = "No releases found on GitHub"
                        _isCheckingForUpdates.value = false
                        return@launch
                    }
                    
                    val latestSuitableRelease = findLatestSuitableRelease(allReleases, currentChannel)
                    
                    if (latestSuitableRelease == null) {
                        _error.value = "No suitable release found for channel '$currentChannel'"
                        _isCheckingForUpdates.value = false
                        return@launch
                    }
                    
                    processRelease(latestSuitableRelease)
                    Log.d(TAG, "Latest version processed: ${_latestVersion.value}")
                } else {
                    handleApiError(releasesResponse.code(), releasesResponse.message())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates", e)
                _error.value = "Network error: ${e.message ?: "Unknown error"}"
                _isCheckingForUpdates.value = false
                Log.e(TAG, "Error state set: ${_error.value}")
            }
        }
    }
    
    /**
     * Process a GitHub release into app version information
     */
    private fun processRelease(release: GitHubRelease) {
        // Convert GitHub release to AppVersion
        val appVersion = convertReleaseToAppVersion(release)
        _latestVersion.value = appVersion
        
        // Parse current and new versions for semantic comparison
        val currentSemVer = parseVersionToSemantic(_currentVersion.value.versionName)
        val newSemVer = parseVersionToSemantic(appVersion.versionName)
        
        // Add debug logs
        Log.d(TAG, "Version comparison: current=${_currentVersion.value.versionName} (${currentSemVer}) vs latest=${appVersion.versionName} (${newSemVer})")
        
        // Update is available if:
        // 1. New version is semantically greater than current version
        // 2. If versions are equal, new build number is higher
        // 3. If in pre-release, allow updates to other pre-releases
        _updateAvailable.value = when {
            newSemVer > currentSemVer -> true
            newSemVer == currentSemVer && appVersion.buildNumber > _currentVersion.value.buildNumber -> true
            _currentVersion.value.isPreRelease && appVersion.isPreRelease && appVersion.buildNumber > _currentVersion.value.buildNumber -> true
            else -> false
        }
        
        _isCheckingForUpdates.value = false
        lastUpdateCheck = System.currentTimeMillis()
    }
    
    /**
     * Parse version string to semantic version object
     */
    private fun parseVersionToSemantic(versionString: String): SemanticVersion {
        try {
            // Remove 'v' prefix if present
            val cleaned = versionString.replace(Regex("^v"), "")
            
            // Extract build number if present (format like "b-127")
            val buildNumber = Regex("b-(\\d+)").find(cleaned)?.groupValues?.get(1)?.toInt() ?: 0
            
            // Split version and remove any suffix (like -alpha)
            val versionBase = cleaned.split(" ")[0].split("-")[0]
            val versionParts = versionBase.split(".")
            
            // Check if it's a pre-release
            val isPreRelease = cleaned.contains(Regex("alpha|beta|pre|rc", RegexOption.IGNORE_CASE))
            
            return SemanticVersion(
                major = versionParts.getOrNull(0)?.toInt() ?: 0,
                minor = versionParts.getOrNull(1)?.toInt() ?: 0,
                patch = versionParts.getOrNull(2)?.toInt() ?: 0,
                subpatch = versionParts.getOrNull(3)?.toInt() ?: 0,
                buildNumber = buildNumber,
                isPreRelease = isPreRelease
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing version: $versionString", e)
            return SemanticVersion(0, 0, 0)
        }
    }
    
    /**
     * Handle API errors with specific messages based on status code
     */
    private fun handleApiError(code: Int, message: String) {
        _error.value = when (code) {
            403 -> "GitHub API rate limit exceeded. Please try again later."
            404 -> "No releases found on GitHub."
            500, 502, 503, 504 -> "GitHub server error. Please try again later."
            else -> "GitHub API error: $code - $message"
        }
        _isCheckingForUpdates.value = false
    }
    
    /**
     * Find the latest suitable release based on the update channel.
     * "stable" channel: latest non-prerelease.
     * "beta" channel: latest prerelease or stable release.
     */
    private fun findLatestSuitableRelease(releases: List<GitHubRelease>, channel: String): GitHubRelease? {
        return when (channel) {
            "stable" -> releases
                .filter { !it.draft && !it.prerelease } // Only stable, non-draft releases
                .maxByOrNull { it.published_at }
            "beta" -> releases
                .filter { !it.draft } // Include pre-releases for beta channel, but exclude drafts
                .maxByOrNull { it.published_at }
            else -> null // Should not happen with current implementation
        }
    }
    
    /**
     * Convert a GitHub release to an AppVersion object
     */
    private fun convertReleaseToAppVersion(release: GitHubRelease): AppVersion {
        // Parse version string to semantic version
        val semanticVersion = parseVersionToSemantic(release.tag_name)
        
        // Format the release date
        val releaseDateString = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(release.published_at)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Unknown date"
        }
        
        // Parse changelog from release body
        val releaseContent = parseReleaseBody(release.body)
        Log.d(TAG, "Parsed whatsNew: ${releaseContent.whatsNew}")
        Log.d(TAG, "Parsed knownIssues: ${releaseContent.knownIssues}")
        
        // Find the APK asset if available
        val apkAsset = release.assets.firstOrNull { 
            it.name.endsWith(".apk") && it.state == "uploaded"
        }
        
        // Get download URL, preferring an APK asset if available
        val downloadUrl = apkAsset?.browser_download_url ?: release.html_url
        
        // Format APK size for display if available
        val apkSize = apkAsset?.size ?: 0
        
        return AppVersion(
            versionName = release.name.ifEmpty { release.tag_name },
            versionCode = semanticVersion.let {
                // Using a more robust versionCode calculation: major * 10M + minor * 100K + patch * 1K + buildNumber
                // This allows for 2 digits for major, minor, patch, and 3-4 digits for build number,
                // ensuring uniqueness and monotonicity.
                it.major * 10000000 + it.minor * 100000 + it.patch * 1000 + it.buildNumber
            },
            releaseDate = releaseDateString,
            whatsNew = releaseContent.whatsNew,
            knownIssues = releaseContent.knownIssues,
            downloadUrl = downloadUrl,
            apkAssetName = apkAsset?.name ?: "",
            apkSize = apkSize,
            releaseNotes = release.body,
            isPreRelease = release.prerelease,
            buildNumber = semanticVersion.buildNumber
        )
    }

    /**
     * Parses the release body string to extract "What's New" and "Known Issues" sections.
     * Assumes a Markdown-like format with specific headings.
     */
    private enum class ParsingState {
        NONE, WHATS_NEW, KNOWN_ISSUES
    }

    private fun parseReleaseBody(body: String?): ReleaseContent {
        if (body.isNullOrBlank()) {
            return ReleaseContent(emptyList(), emptyList())
        }

        val whatsNew = mutableListOf<String>()
        val knownIssues = mutableListOf<String>()

        var currentState = ParsingState.NONE

        body.lines().forEach { line ->
            val trimmedLine = line.trim()

            when {
                trimmedLine.startsWith("**What's New:**") -> {
                    currentState = ParsingState.WHATS_NEW
                }
                trimmedLine.startsWith("**Known Issues") -> { // Matches "Known Issues (Will be fixed on a later build):"
                    currentState = ParsingState.KNOWN_ISSUES
                }
                trimmedLine.startsWith("**Build Information:**") -> {
                    currentState = ParsingState.NONE // Stop parsing for these sections
                }
                // If we are in a section and encounter another heading, stop parsing the current section
                (trimmedLine.startsWith("#") || trimmedLine.startsWith("##")) &&
                currentState != ParsingState.NONE -> {
                    currentState = ParsingState.NONE
                }
                else -> {
                    // Add line to current section if we are in one
                    when (currentState) {
                        ParsingState.WHATS_NEW -> {
                            val htmlLine = trimmedLine
                                .replace(Regex("^[*-]\\s*"), "") // Remove list prefixes
                                .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>") // Bold
                                .replace(Regex("_(.*?)_"), "<i>$1</i>") // Italic
                                .replace(Regex("\\[(.*?)\\]\\((.*?)\\)"), "<a href=\"$2\">$1</a>") // Links
                            if (htmlLine.isNotBlank()) {
                                whatsNew.add(htmlLine)
                            }
                        }
                        ParsingState.KNOWN_ISSUES -> {
                            val htmlLine = trimmedLine
                                .replace(Regex("^[*-]\\s*"), "") // Remove list prefixes
                                .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>") // Bold
                                .replace(Regex("_(.*?)_"), "<i>$1</i>") // Italic
                                .replace(Regex("\\[(.*?)\\]\\((.*?)\\)"), "<a href=\"$2\">$1</a>") // Links
                            if (htmlLine.isNotBlank()) {
                                knownIssues.add(htmlLine)
                            }
                        }
                        ParsingState.NONE -> {
                            // Do nothing if not in a specific section
                        }
                    }
                }
            }
        }
        return ReleaseContent(whatsNew, knownIssues)
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Calculate readable file size
     */
    fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        
        return String.format(
            "%.1f %s", 
            size / Math.pow(1024.0, digitGroups.toDouble()), 
            units[digitGroups]
        )
    }
    
    /**
     * Download the update by opening the browser to the download URL
     */
    fun downloadUpdate() {
        val latestVersion = _latestVersion.value
        val downloadUrl = latestVersion?.downloadUrl
        
        if (downloadUrl.isNullOrBlank()) {
            _error.value = "No download URL available"
            return
        }
        
        // If it's not an APK file, open in browser
        if (latestVersion?.apkAssetName.isNullOrEmpty()) {
            openInBrowser(downloadUrl)
            return
        }
        
        // Check if we have an active download
        if (_isDownloading.value) {
            Log.d(TAG, "Download already in progress")
            return
        }
        
        // Start or resume download
        downloadApkInApp(downloadUrl, latestVersion?.apkAssetName ?: "rhythm-update.apk")
    }
    
    /**
     * Open a URL in the browser
     */
    private fun openInBrowser(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(browserIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening download URL", e)
            _error.value = "Could not open download link: ${e.message ?: "Unknown error"}"
        }
    }
    
    /**
     * Download an APK file in-app with progress tracking and resume support
     */
    private fun downloadApkInApp(downloadUrl: String, fileName: String) {
        if (_isDownloading.value) {
            return // Already downloading
        }
        
        _downloadProgress.value = 0f
        activeDownload = null
        _isDownloading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Use app-specific external storage instead of public Downloads
                val context = getApplication<Application>()
                val downloadDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Android 10 and above, use app-specific directory
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                } else {
                    // For older versions, check permission first
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED) {
                        _error.value = "Storage permission required to download updates"
                        _isDownloading.value = false
                        return@launch
                    }
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                }

                if (downloadDir == null) {
                    _error.value = "Could not access storage"
                    _isDownloading.value = false
                    return@launch
                }

                // Ensure download directory exists
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }
                
                // Create or get existing file
                val file = File(downloadDir, fileName)
                val existingLength = if (file.exists()) file.length() else 0L
                
                // Create OkHttp client with longer timeouts
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                // Create request with resume support
                val requestBuilder = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "Rhythm-App")
                
                // Add range header if resuming
                if (existingLength > 0 && activeDownload != null) {
                    requestBuilder.header("Range", "bytes=$existingLength-")
                    requestBuilder.header("If-Match", activeDownload?.etag ?: "*")
                    if (activeDownload?.lastModified != null) {
                        requestBuilder.header("If-Unmodified-Since", activeDownload?.lastModified!!)
                    }
                }
                
                val request = requestBuilder.build()
                
                // Execute request
                activeCall = client.newCall(request)
                activeCall?.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        viewModelScope.launch {
                            if (!call.isCanceled()) {
                                _isDownloading.value = false
                                _error.value = "Download failed: ${e.message ?: "Unknown error"}"
                                Log.e(TAG, "Download failed", e)
                                activeDownload = null
                                activeCall = null
                            }
                        }
                    }
                    
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful && response.code != 206) {
                            viewModelScope.launch {
                                _isDownloading.value = false
                                _error.value = "Download failed: HTTP ${response.code}"
                                activeDownload = null
                                activeCall = null
                            }
                            return
                        }
                        
                        try {
                            // Get content length and resume info
                            val contentLength = response.body?.contentLength() ?: -1L
                            val totalLength = if (response.code == 206) {
                                val range = response.header("Content-Range")
                                range?.substringAfter("/")?.toLongOrNull() ?: contentLength
                            } else {
                                contentLength
                            }
                            
                            // Store download state
                            activeDownload = DownloadState(
                                fileName = fileName,
                                url = downloadUrl,
                                totalBytes = totalLength,
                                downloadedBytes = existingLength,
                                etag = response.header("ETag"),
                                lastModified = response.header("Last-Modified"),
                                resumePosition = existingLength
                            )
                            _downloadState.value = activeDownload
                            
                            // Create output stream
                            val outputStream = FileOutputStream(file, existingLength > 0)
                            
                            // Get input stream
                            val inputStream = response.body?.byteStream()
                            
                            if (inputStream == null) {
                                viewModelScope.launch {
                                    _isDownloading.value = false
                                    _error.value = "Download failed: Empty response"
                                    activeDownload = null
                                    activeCall = null
                                }
                                return
                            }
                            
                            // Create buffer
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = existingLength
                            
                            // Read input stream
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                if (!_isDownloading.value) {
                                    // Download was cancelled
                                    break
                                }
                                
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                
                                // Update progress
                                val totalBytes = if (totalLength > 0) totalLength else contentLength
                                if (totalBytes > 0) {
                                    val progress = (totalBytesRead.toFloat() / totalBytes.toFloat()) * 100f
                                    viewModelScope.launch {
                                        _downloadProgress.value = progress
                                        activeDownload = activeDownload?.copy(downloadedBytes = totalBytesRead)
                                        _downloadState.value = activeDownload
                                    }
                                }
                            }
                            
                            // Close streams
                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()
                            
                            // Download complete
                            viewModelScope.launch {
                                if (_isDownloading.value) {
                                    _isDownloading.value = false
                                    _downloadProgress.value = 100f
                                    _downloadedFile.value = file
                                    activeDownload = null
                                    activeCall = null
                                    _downloadState.value = null
                                    Log.d(TAG, "Download complete: ${file.absolutePath}")
                                }
                            }
                        } catch (e: Exception) {
                            viewModelScope.launch {
                                if (_isDownloading.value) {
                                    _isDownloading.value = false
                                    _error.value = "Download failed: ${e.message ?: "Unknown error"}"
                                    Log.e(TAG, "Download failed", e)
                                    // Keep download state for potential resume
                                    activeCall = null
                                }
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                _isDownloading.value = false
                _error.value = "Download failed: ${e.message ?: "Unknown error"}"
                Log.e(TAG, "Download failed", e)
                activeDownload = null
                activeCall = null
                _downloadState.value = null
            }
        }
    }
    
    /**
     * Install the downloaded APK
     */
    fun installDownloadedApk() {
        val file = _downloadedFile.value ?: return
        
        try {
            val context = getApplication<Application>()
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            _error.value = "Could not install APK: ${e.message ?: "Unknown error"}"
            Log.e(TAG, "Error installing APK", e)
        }
    }
    
    /**
     * Cancel the current download
     */
    fun cancelDownload() {
        activeCall?.cancel()
        activeCall = null
        activeDownload = null
        _isDownloading.value = false
        _downloadProgress.value = 0f
        _downloadState.value = null
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelDownload()
    }
}
