package com.example.absensi.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm:ss"
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    private const val DISPLAY_TIME_FORMAT = "HH:mm"
    private const val DISPLAY_DATE_TIME_FORMAT = "dd MMM yyyy, HH:mm"
    private const val DAY_NAME_FORMAT = "EEEE"

    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val timeFormatter = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
    private val displayDateFormatter = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
    private val displayTimeFormatter = SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault())
    private val displayDateTimeFormatter = SimpleDateFormat(DISPLAY_DATE_TIME_FORMAT, Locale.getDefault())
    private val dayNameFormatter = SimpleDateFormat(DAY_NAME_FORMAT, Locale.getDefault())

    fun getCurrentDate(): String = dateFormatter.format(Date())

    fun getCurrentTime(): String = timeFormatter.format(Date())

    fun getCurrentDateTime(): String = dateTimeFormatter.format(Date())

    fun formatDate(date: String?): String {
        if (date.isNullOrEmpty()) return "-"
        return try {
            val parsedDate = dateFormatter.parse(date)
            displayDateFormatter.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }

    fun formatTime(time: String?): String {
        if (time.isNullOrEmpty()) return "-"
        return try {
            val parsedTime = timeFormatter.parse(time)
            displayTimeFormatter.format(parsedTime!!)
        } catch (e: Exception) {
            time
        }
    }

    fun formatDateTime(dateTime: String?): String {
        if (dateTime.isNullOrEmpty()) return "-"
        return try {
            val parsedDateTime = dateTimeFormatter.parse(dateTime)
            displayDateTimeFormatter.format(parsedDateTime!!)
        } catch (e: Exception) {
            dateTime
        }
    }

    fun getDayName(date: String?): String {
        if (date.isNullOrEmpty()) return "-"
        return try {
            val parsedDate = dateFormatter.parse(date)
            dayNameFormatter.format(parsedDate!!)
        } catch (e: Exception) {
            "-"
        }
    }

    fun parseDate(date: String?): Date? {
        if (date.isNullOrEmpty()) return null
        return try {
            dateFormatter.parse(date)
        } catch (e: Exception) {
            null
        }
    }

    fun parseTime(time: String?): Date? {
        if (time.isNullOrEmpty()) return null
        return try {
            timeFormatter.parse(time)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDateTime(dateTime: String?): Date? {
        if (dateTime.isNullOrEmpty()) return null
        return try {
            dateTimeFormatter.parse(dateTime)
        } catch (e: Exception) {
            null
        }
    }

    fun getMonthRange(date: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val firstDay = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val lastDay = calendar.time

        return Pair(firstDay, lastDay)
    }

    fun calculateDuration(startTime: String?, endTime: String?): String {
        if (startTime.isNullOrEmpty() || endTime.isNullOrEmpty()) return "-"

        return try {
            val start = timeFormatter.parse(startTime)!!
            val end = timeFormatter.parse(endTime)!!
            val durationMillis = end.time - start.time

            val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60

            String.format("%02d:%02d", hours, minutes)
        } catch (e: Exception) {
            "-"
        }
    }

    fun isToday(date: String?): Boolean {
        if (date.isNullOrEmpty()) return false
        return try {
            val parsedDate = dateFormatter.parse(date)
            val today = Calendar.getInstance()
            val compareDate = Calendar.getInstance()
            compareDate.time = parsedDate!!

            today.get(Calendar.YEAR) == compareDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == compareDate.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    fun isWithinWorkingHours(
        currentTime: String,
        startTime: String,
        endTime: String,
        lateThresholdMinutes: Int
    ): Boolean {
        return try {
            val current = timeFormatter.parse(currentTime)!!
            val start = timeFormatter.parse(startTime)!!
            val end = timeFormatter.parse(endTime)!!

            // Add late threshold to start time
            val calendar = Calendar.getInstance()
            calendar.time = start
            calendar.add(Calendar.MINUTE, lateThresholdMinutes)
            val lateTime = calendar.time

            current.after(start) && current.before(lateTime)
        } catch (e: Exception) {
            false
        }
    }
}
