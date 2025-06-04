package com.example.absensi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.example.absensi.util.ImageUtils
import com.example.absensi.util.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AbsensiApplication : Application() {
    lateinit var preferenceManager: PreferenceManager
        private set

    companion object {
        const val CHANNEL_ATTENDANCE_REMINDER = "attendance_reminder"
        const val CHANNEL_GENERAL = "general_notifications"
        const val CHANNEL_UPDATES = "updates"

        private lateinit var instance: AbsensiApplication

        fun getInstance(): AbsensiApplication = instance

        fun getPreferenceManager(): PreferenceManager = instance.preferenceManager
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize PreferenceManager
        preferenceManager = PreferenceManager(this)

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create notification channels
        createNotificationChannels()

        // Initialize other components
        initializeComponents()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Attendance reminder channel
            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE_REMINDER,
                getString(R.string.channel_attendance_reminder),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_attendance_reminder_description)
                enableVibration(true)
            }

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                getString(R.string.channel_general),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_general_description)
            }

            // Updates channel
            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                getString(R.string.channel_updates),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_updates_description)
            }

            // Register the channels
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(
                listOf(attendanceChannel, generalChannel, updatesChannel)
            )
        }
    }

    private fun initializeComponents() {
        // Initialize WorkManager
        WorkManager.initialize(
            this,
            WorkManagerConfiguration.Builder()
                .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
                .build()
        )

        // Schedule periodic work
        schedulePeriodicTasks()

        // Initialize crash reporting in release builds
        if (!BuildConfig.DEBUG) {
            initializeCrashReporting()
        }
    }

    private fun schedulePeriodicTasks() {
        // Schedule attendance reminder
        val attendanceReminderRequest = PeriodicWorkRequestBuilder<AttendanceReminderWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "attendance_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            attendanceReminderRequest
        )
    }

    private fun initializeCrashReporting() {
        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            setCustomKey("version_name", BuildConfig.VERSION_NAME)
            setCustomKey("version_code", BuildConfig.VERSION_CODE)
        }
    }

    fun logout() {
        // Cancel all background work
        WorkManager.getInstance(this).cancelAllWork()

        // Clear user session
        preferenceManager.clearSession()

        // Clear app data
        clearAppData()

        // Log the event
        Timber.i("User logged out and app data cleared")
    }

    private fun clearAppData() {
        try {
            // Clear app cache
            cacheDir.deleteRecursively()
            
            // Clear databases
            getDatabasePath("attendance.db")?.delete()
            
            // Clear shared preferences except essential settings
            preferenceManager.clearAll()
            
            // Clear files directory
            filesDir.listFiles()?.forEach { file ->
                if (!file.name.startsWith("essential_")) {
                    file.delete()
                }
            }

            // Clear external cache if available
            externalCacheDir?.deleteRecursively()
        } catch (e: Exception) {
            Timber.e(e, "Error clearing app data")
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_COMPLETE,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Clear non-critical caches
                clearNonEssentialData()
            }
        }
    }

    private fun clearNonEssentialData() {
        try {
            // Clear image caches
            ImageUtils.clearImageCache(this)
            
            // Clear temporary files
            getExternalFilesDir(null)?.listFiles { file ->
                file.name.startsWith("temp_")
            }?.forEach { it.delete() }
        } catch (e: Exception) {
            Timber.e(e, "Error clearing non-essential data")
        }
    }
}
