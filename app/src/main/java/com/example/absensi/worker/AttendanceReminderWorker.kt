package com.example.absensi.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.absensi.AbsensiApplication
import com.example.absensi.MainActivity
import com.example.absensi.R
import com.example.absensi.util.DateTimeUtils
import timber.log.Timber
import java.util.*

class AttendanceReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            checkAndSendReminder()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in AttendanceReminderWorker")
            Result.failure()
        }
    }

    private suspend fun checkAndSendReminder() {
        val preferenceManager = AbsensiApplication.getPreferenceManager()
        
        // Only show reminder if notifications are enabled
        if (!preferenceManager.isNotificationEnabled()) {
            return
        }

        val currentTime = Calendar.getInstance()
        val workStartTime = preferenceManager.getWorkingHoursStart()
        
        if (workStartTime != null && shouldShowReminder(currentTime, workStartTime)) {
            showNotification()
        }
    }

    private fun shouldShowReminder(currentTime: Calendar, workStartTime: String): Boolean {
        // Parse work start time
        val (hours, minutes) = workStartTime.split(":").map { it.toInt() }
        val workStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            add(Calendar.MINUTE, -30) // Remind 30 minutes before work starts
        }

        // Check if current time is within reminder window
        return currentTime.after(workStart) && 
               currentTime.before(workStart.apply { add(Calendar.MINUTE, 15) }) && // 15-minute reminder window
               currentTime.get(Calendar.DAY_OF_WEEK) !in arrayOf(Calendar.SATURDAY, Calendar.SUNDAY) &&
               !DateTimeUtils.isHoliday(currentTime.time) // Don't remind on holidays
    }

    private fun showNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "attendance") // Tell MainActivity to navigate to attendance screen
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, AbsensiApplication.CHANNEL_ATTENDANCE_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.attendance_reminder_title))
            .setContentText(context.getString(R.string.attendance_reminder_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check_in,
                context.getString(R.string.action_check_in),
                createCheckInPendingIntent()
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createCheckInPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "attendance")
            putExtra("action", "check_in")
        }

        return PendingIntent.getActivity(
            context,
            CHECK_IN_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_IN_REQUEST_CODE = 1002
    }
}
