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
    val releaseNotes: String = ""
)

/**
 * ViewModel for handling app updates
 */
class AppUpdaterViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AppUpdaterViewModel"
    
    // GitHub repository information
    private val GITHUB_OWNER = "cromaguy"
    private val GITHUB_REPO = "RhythmM3Ex"
    
    // API service
    private val gitHubApiService = NetworkManager.createGitHubApiService()
    
    // Current app version info
    private val _currentVersion = MutableStateFlow(
        AppVersion(
            versionName = "1.0.0 Alpha", // Matches the About dialog version display
            versionCode = 100,           // From build.gradle.kts
            releaseDate = "",            // We don't store release date in the app
            changelog = emptyList(),
            downloadUrl = ""
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
    
    /**
     * Check for updates by fetching the latest release from GitHub
     */
    fun checkForUpdates() {
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
        
        // Check if an update is available by comparing version codes
        val currentVersionCode = _currentVersion.value.versionCode
        val newVersionCode = appVersion.versionCode
        
        _updateAvailable.value = newVersionCode > currentVersionCode
        _isCheckingForUpdates.value = false
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
        // Parse version code from tag name
        // Assumes tag format is like "v1.2.3" or "1.2.3"
        val versionCodeString = release.tag_name.replace(Regex("^v"), "")
        val versionParts = versionCodeString.split(".")
        val versionCode = if (versionParts.size >= 3) {
            try {
                val major = versionParts[0].toInt()
                val minor = versionParts[1].toInt()
                val patch = versionParts[2].toInt()
                
                // Create a numeric version code similar to how Android typically does it
                major * 10000 + minor * 100 + patch
            } catch (e: NumberFormatException) {
                // Fallback if parsing fails
                100
            }
        } else {
            // Fallback if tag format is unexpected
            100
        }
        
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
            versionCode = versionCode,
            releaseDate = releaseDateString,
            changelog = changelog,
            downloadUrl = downloadUrl,
            apkAssetName = apkAsset?.name ?: "",
            apkSize = apkSize,
            releaseNotes = release.body
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
        
        // Download in-app if it's an APK
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
     * Download an APK file in-app with progress tracking
     */
    private fun downloadApkInApp(downloadUrl: String, fileName: String) {
        if (_isDownloading.value) {
            return // Already downloading
        }
        
        _isDownloading.value = true
        _downloadProgress.value = 0f
        _error.value = null
        _downloadedFile.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create downloads directory if it doesn't exist
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                
                // Create file
                val file = File(downloadsDir, fileName)
                
                // Create OkHttp client
                val client = OkHttpClient()
                
                // Create request
                val request = Request.Builder()
                    .url(downloadUrl)
                    .build()
                
                // Execute request
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        viewModelScope.launch {
                            _isDownloading.value = false
                            _error.value = "Download failed: ${e.message ?: "Unknown error"}"
                            Log.e(TAG, "Download failed", e)
                        }
                    }
                    
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            viewModelScope.launch {
                                _isDownloading.value = false
                                _error.value = "Download failed: HTTP ${response.code}"
                            }
                            return
                        }
                        
                        // Get content length
                        val contentLength = response.body?.contentLength() ?: -1L
                        
                        try {
                            // Create output stream
                            val outputStream = FileOutputStream(file)
                            
                            // Get input stream
                            val inputStream = response.body?.byteStream()
                            
                            if (inputStream == null) {
                                viewModelScope.launch {
                                    _isDownloading.value = false
                                    _error.value = "Download failed: Empty response"
                                }
                                return
                            }
                            
                            // Create buffer
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            
                            // Read input stream
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                
                                // Update progress
                                if (contentLength > 0) {
                                    val progress = (totalBytesRead.toFloat() / contentLength.toFloat()) * 100f
                                    viewModelScope.launch {
                                        _downloadProgress.value = progress
                                    }
                                }
                            }
                            
                            // Close streams
                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()
                            
                            // Download complete
                            viewModelScope.launch {
                                _isDownloading.value = false
                                _downloadProgress.value = 100f
                                _downloadedFile.value = file
                                Log.d(TAG, "Download complete: ${file.absolutePath}")
                            }
                        } catch (e: Exception) {
                            viewModelScope.launch {
                                _isDownloading.value = false
                                _error.value = "Download failed: ${e.message ?: "Unknown error"}"
                                Log.e(TAG, "Download failed", e)
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                _isDownloading.value = false
                _error.value = "Download failed: ${e.message ?: "Unknown error"}"
                Log.e(TAG, "Download failed", e)
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
     * Cancel the download in progress
     */
    fun cancelDownload() {
        if (_isDownloading.value) {
            _isDownloading.value = false
            _downloadProgress.value = 0f
            _error.value = null
        }
    }
} 