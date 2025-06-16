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

/**
 * App version data model
 */
data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val changelog: List<String>,
    val downloadUrl: String,
    val apkAssetName: String = "",
    val apkSize: Long = 0,
    val releaseNotes: String = "",
    val isPreRelease: Boolean = false,
    val buildNumber: Int = 0
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
    
    // Current app version info
    private val _currentVersion = MutableStateFlow(
        AppVersion(
            versionName = "2.0.100.6 b-234 Beta",
            versionCode = 2000234,
            releaseDate = "2025-06-16",
            changelog = emptyList(),
            downloadUrl = "",
            isPreRelease = true,
            buildNumber = 234
        )
    )
    val currentVersion: StateFlow<AppVersion> = _currentVersion.asStateFlow()
    
    // Latest version info
    private val _latestVersion = MutableStateFlow<AppVersion?>(null)
    val latestVersion: StateFlow<AppVersion?> = _latestVersion.asStateFlow()
    
    // Update check state
    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()
    
    // Update available state
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Download state - true when actively downloading
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()
    
    // Download progress (0-100)
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    
    // Downloaded file
    private val _downloadedFile = MutableStateFlow<File?>(null)
    val downloadedFile: StateFlow<File?> = _downloadedFile.asStateFlow()
    
    // Download state for tracking download progress and resumption
    private val _downloadState = MutableStateFlow<DownloadState?>(null)
    val downloadState: StateFlow<DownloadState?> = _downloadState.asStateFlow()
    
    /**
     * Check for updates by fetching the latest release from GitHub
     */
    fun checkForUpdates(force: Boolean = false) {
        // Skip check if within update interval unless forced
        if (!force && System.currentTimeMillis() - lastUpdateCheck < UPDATE_CHECK_INTERVAL) {
            Log.d(TAG, "Skipping update check - within interval")
            return
        }
        
        _isCheckingForUpdates.value = true
        _error.value = null
        _latestVersion.value = null  // Clear any previous version data
        
        viewModelScope.launch {
            try {
                // Try to fetch just the latest release first (more efficient)
                val response = gitHubApiService.getLatestRelease(GITHUB_OWNER, GITHUB_REPO)
                
                if (response.isSuccessful) {
                    val latestRelease = response.body()
                    
                    if (latestRelease == null) {
                        _error.value = "No release information available"
                        _isCheckingForUpdates.value = false
                        return@launch
                    }
                    
                    processRelease(latestRelease)
                } else {
                    // If 404 (no releases found) or other error, try getting all releases
                    if (response.code() == 404) {
                        val allReleasesResponse = gitHubApiService.getReleases(GITHUB_OWNER, GITHUB_REPO)
                        
                        if (allReleasesResponse.isSuccessful) {
                            val releases = allReleasesResponse.body()
                            
                            if (releases.isNullOrEmpty()) {
                                _error.value = "No releases found on GitHub"
                                _isCheckingForUpdates.value = false
                                return@launch
                            }
                            
                            // Find latest release
                            val latestRelease = findLatestRelease(releases)
                            
                            if (latestRelease == null) {
                                _error.value = "No suitable release found"
                                _isCheckingForUpdates.value = false
                                return@launch
                            }
                            
                            processRelease(latestRelease)
                        } else {
                            handleApiError(allReleasesResponse.code(), allReleasesResponse.message())
                        }
                    } else {
                        handleApiError(response.code(), response.message())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates", e)
                _error.value = "Network error: ${e.message ?: "Unknown error"}"
                _isCheckingForUpdates.value = false
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
     * Find the latest non-draft release from the list
     * This allows including pre-releases as they may contain important updates
     */
    private fun findLatestRelease(releases: List<GitHubRelease>): GitHubRelease? {
        return releases
            .filter { !it.draft } // Exclude draft releases
            // We're including prereleases as they might have APK builds
            .maxByOrNull { it.published_at }
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
        val changelog = parseChangelog(release.body)
        
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
                it.major * 1000000 + it.minor * 10000 + it.patch * 100 + it.subpatch * 10 + (it.buildNumber / 100)
            },
            releaseDate = releaseDateString,
            changelog = changelog,
            downloadUrl = downloadUrl,
            apkAssetName = apkAsset?.name ?: "",
            apkSize = apkSize,
            releaseNotes = release.body,
            isPreRelease = release.prerelease,
            buildNumber = semanticVersion.buildNumber
        )
    }
    
    /**
     * Parse changelog items from release body
     */
    private fun parseChangelog(body: String): List<String> {
        // Split by new lines
        val lines = body.split("\n")
        val changelog = mutableListOf<String>()
        
        // Look for Markdown-style lists
        var isInList = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Check for standard Markdown list items
            if (trimmedLine.startsWith("- ") || 
                trimmedLine.startsWith("* ") || 
                trimmedLine.matches(Regex("^\\d+\\. .*"))) {
                
                isInList = true
                
                // Extract the content after the list marker
                val content = when {
                    trimmedLine.startsWith("- ") -> trimmedLine.substring(2)
                    trimmedLine.startsWith("* ") -> trimmedLine.substring(2)
                    else -> trimmedLine.replaceFirst(Regex("^\\d+\\. "), "")
                }
                
                changelog.add(content.trim())
            } else if (isInList && trimmedLine.isNotEmpty() && 
                      (trimmedLine.startsWith("  ") || trimmedLine.startsWith("\t"))) {
                // This is a continuation of a list item - append to the last item
                if (changelog.isNotEmpty()) {
                    changelog[changelog.size - 1] += " " + trimmedLine.trim()
                }
            } else if (trimmedLine.startsWith("#") && changelog.isNotEmpty()) {
                // A heading after we've already found some list items - we're done with the changelog
                break
            } else {
                isInList = false
            }
        }
        
        // If no specific changelog items found, look for the first paragraph
        if (changelog.isEmpty() && body.isNotEmpty()) {
            // Find the first non-empty paragraph
            val paragraphs = body.split("\n\n")
            for (paragraph in paragraphs) {
                val trimmed = paragraph.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    changelog.add(trimmed)
                    break
                }
            }
            
            // If still empty, just use the first line of the body
            if (changelog.isEmpty() && lines.isNotEmpty()) {
                changelog.add(lines.first().trim())
            }
        }
        
        return changelog
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
