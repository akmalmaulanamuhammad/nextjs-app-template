package com.example.absensi.util

import android.content.Context
import android.content.SharedPreferences
import com.example.absensi.model.User
import com.google.gson.Gson

class PreferenceManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor = preferences.edit()
    private val gson = Gson()

    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return preferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        editor.putString(KEY_REFRESH_TOKEN, token)
        editor.apply()
    }

    fun getRefreshToken(): String? {
        return preferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        editor.putString(KEY_USER, userJson)
        editor.apply()
    }

    fun getUser(): User? {
        val userJson = preferences.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun saveDeviceToken(token: String) {
        editor.putString(KEY_DEVICE_TOKEN, token)
        editor.apply()
    }

    fun getDeviceToken(): String? {
        return preferences.getString(KEY_DEVICE_TOKEN, null)
    }

    fun saveLastSyncTime(timestamp: Long) {
        editor.putLong(KEY_LAST_SYNC, timestamp)
        editor.apply()
    }

    fun getLastSyncTime(): Long {
        return preferences.getLong(KEY_LAST_SYNC, 0)
    }

    fun saveWorkingHours(start: String, end: String, lateThreshold: Int) {
        editor.putString(KEY_WORKING_HOURS_START, start)
        editor.putString(KEY_WORKING_HOURS_END, end)
        editor.putInt(KEY_LATE_THRESHOLD, lateThreshold)
        editor.apply()
    }

    fun getWorkingHoursStart(): String? {
        return preferences.getString(KEY_WORKING_HOURS_START, null)
    }

    fun getWorkingHoursEnd(): String? {
        return preferences.getString(KEY_WORKING_HOURS_END, null)
    }

    fun getLateThreshold(): Int {
        return preferences.getInt(KEY_LATE_THRESHOLD, DEFAULT_LATE_THRESHOLD)
    }

    fun saveOfficeLocation(latitude: Double, longitude: Double, radius: Int) {
        editor.putString(KEY_OFFICE_LATITUDE, latitude.toString())
        editor.putString(KEY_OFFICE_LONGITUDE, longitude.toString())
        editor.putInt(KEY_OFFICE_RADIUS, radius)
        editor.apply()
    }

    fun getOfficeLatitude(): Double {
        return preferences.getString(KEY_OFFICE_LATITUDE, "0.0")?.toDoubleOrNull() ?: 0.0
    }

    fun getOfficeLongitude(): Double {
        return preferences.getString(KEY_OFFICE_LONGITUDE, "0.0")?.toDoubleOrNull() ?: 0.0
    }

    fun getOfficeRadius(): Int {
        return preferences.getInt(KEY_OFFICE_RADIUS, DEFAULT_OFFICE_RADIUS)
    }

    fun saveThemeMode(isDarkMode: Boolean) {
        editor.putBoolean(KEY_DARK_MODE, isDarkMode)
        editor.apply()
    }

    fun isDarkMode(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun saveNotificationEnabled(enabled: Boolean) {
        editor.putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
        editor.apply()
    }

    fun isNotificationEnabled(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    fun isFirstLaunch(): Boolean {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(isFirst: Boolean) {
        editor.putBoolean(KEY_FIRST_LAUNCH, isFirst)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    fun clearSession() {
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_REFRESH_TOKEN)
        editor.remove(KEY_USER)
        editor.apply()
    }

    fun clearAll() {
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val PREF_NAME = "AbsensiPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user"
        private const val KEY_DEVICE_TOKEN = "device_token"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_WORKING_HOURS_START = "working_hours_start"
        private const val KEY_WORKING_HOURS_END = "working_hours_end"
        private const val KEY_LATE_THRESHOLD = "late_threshold"
        private const val KEY_OFFICE_LATITUDE = "office_latitude"
        private const val KEY_OFFICE_LONGITUDE = "office_longitude"
        private const val KEY_OFFICE_RADIUS = "office_radius"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"

        private const val DEFAULT_LATE_THRESHOLD = 15 // 15 minutes
        private const val DEFAULT_OFFICE_RADIUS = 100 // 100 meters
    }
}
