package chromahub.rhythm.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import chromahub.rhythm.app.data.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker that performs automatic weekly backups of app settings and data
 */
class BackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "BackupWorker"
        const val WORK_NAME = "auto_backup_work"
        private const val BACKUP_FOLDER = "RhythmBackups"
        private const val MAX_AUTO_BACKUPS = 4 // Keep last 4 weekly backups
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting automatic backup...")
            
            val appSettings = AppSettings.getInstance(applicationContext)
            
            // Check if auto-backup is still enabled
            if (!appSettings.autoBackupEnabled.value) {
                Log.d(TAG, "Auto-backup is disabled, skipping...")
                return@withContext Result.success()
            }

            // Create backup JSON
            val backupJson = appSettings.createBackup()
            
            // Get or create backup directory
            val backupDir = File(applicationContext.getExternalFilesDir(null), BACKUP_FOLDER)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // Create backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "auto_backup_$timestamp.json")
            
            // Write backup to file
            backupFile.writeText(backupJson)
            Log.d(TAG, "Auto-backup saved to: ${backupFile.absolutePath}")
            
            // Update last backup timestamp
            appSettings.setLastBackupTimestamp(System.currentTimeMillis())
            appSettings.setBackupLocation(backupFile.absolutePath)
            
            // Clean up old backups (keep only the most recent ones)
            cleanupOldBackups(backupDir)
            
            Log.d(TAG, "Automatic backup completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto-backup failed: ${e.message}", e)
            Result.retry()
        }
    }
    
    /**
     * Remove old auto-backups, keeping only the most recent ones
     */
    private fun cleanupOldBackups(backupDir: File) {
        try {
            val backupFiles = backupDir.listFiles { file ->
                file.name.startsWith("auto_backup_") && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: return
            
            // Delete older backups beyond the limit
            if (backupFiles.size > MAX_AUTO_BACKUPS) {
                backupFiles.drop(MAX_AUTO_BACKUPS).forEach { file ->
                    if (file.delete()) {
                        Log.d(TAG, "Deleted old backup: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old backups: ${e.message}", e)
        }
    }
}
