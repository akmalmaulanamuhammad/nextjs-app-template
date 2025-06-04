package com.example.absensi.repository

import android.content.Context
import com.example.absensi.base.BaseRepository
import com.example.absensi.model.AttendanceRecord
import com.example.absensi.network.AttendanceApiClient
import com.example.absensi.network.AttendanceApiService
import com.example.absensi.network.OfficeStatus
import com.example.absensi.util.NetworkUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AttendanceRepository(context: Context) : BaseRepository(context) {
    private val attendanceService: AttendanceApiService by lazy {
        getAuthToken()?.let { token ->
            AttendanceApiClient.createAuthenticatedClient(token)
        } ?: AttendanceApiClient.service
    }

    suspend fun getAttendanceHistory(
        page: Int,
        perPage: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        status: String? = null
    ): NetworkUtils.NetworkResult<List<AttendanceRecord>> {
        return safeApiCall {
            attendanceService.getAttendanceHistory(page, perPage, startDate, endDate, status)
        }.let { result ->
            when (result) {
                is NetworkUtils.NetworkResult.Success -> {
                    NetworkUtils.NetworkResult.Success(result.data.data.data)
                }
                is NetworkUtils.NetworkResult.Error -> result
                is NetworkUtils.NetworkResult.Loading -> result
            }
        }
    }

    suspend fun getTodayAttendance(): NetworkUtils.NetworkResult<AttendanceRecord?> {
        return safeApiCall {
            attendanceService.getTodayAttendance()
        }
    }

    suspend fun checkIn(
        latitude: Double,
        longitude: Double,
        address: String,
        photoFile: File,
        notes: String? = null
    ): NetworkUtils.NetworkResult<AttendanceRecord> {
        return try {
            // Create request bodies
            val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val addressBody = address.toRequestBody("text/plain".toMediaTypeOrNull())
            val notesBody = notes?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Create photo multipart
            val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                photoFile.name,
                photoRequestBody
            )

            safeApiCall {
                attendanceService.checkIn(
                    latitude = latitudeBody,
                    longitude = longitudeBody,
                    address = addressBody,
                    photo = photoPart,
                    notes = notesBody
                )
            }
        } catch (e: Exception) {
            NetworkUtils.NetworkResult.Error(e.message ?: "Check-in failed")
        }
    }

    suspend fun checkOut(
        latitude: Double,
        longitude: Double,
        address: String,
        photoFile: File,
        notes: String? = null
    ): NetworkUtils.NetworkResult<AttendanceRecord> {
        return try {
            // Create request bodies
            val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val addressBody = address.toRequestBody("text/plain".toMediaTypeOrNull())
            val notesBody = notes?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Create photo multipart
            val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                photoFile.name,
                photoRequestBody
            )

            safeApiCall {
                attendanceService.checkOut(
                    latitude = latitudeBody,
                    longitude = longitudeBody,
                    address = addressBody,
                    photo = photoPart,
                    notes = notesBody
                )
            }
        } catch (e: Exception) {
            NetworkUtils.NetworkResult.Error(e.message ?: "Check-out failed")
        }
    }

    suspend fun getAttendanceDetail(id: Long): NetworkUtils.NetworkResult<AttendanceRecord> {
        return safeApiCall {
            attendanceService.getAttendanceDetail(id)
        }
    }

    suspend fun getAttendanceSummary(
        month: Int? = null,
        year: Int? = null
    ): NetworkUtils.NetworkResult<AttendanceSummary> {
        return safeApiCall {
            attendanceService.getAttendanceSummary(month, year)
        }
    }

    suspend fun getOfficeStatus(): NetworkUtils.NetworkResult<OfficeStatus> {
        return safeApiCall {
            attendanceService.getOfficeStatus()
        }
    }

    // Cache related functions
    private suspend fun cacheAttendanceRecords(records: List<AttendanceRecord>) {
        // Implement caching logic if needed
    }

    private suspend fun getCachedAttendanceRecords(): List<AttendanceRecord>? {
        // Implement cache retrieval logic if needed
        return null
    }

    private fun clearCache() {
        // Implement cache clearing logic if needed
    }

    companion object {
        private const val CACHE_TIMEOUT = 5 * 60 * 1000L // 5 minutes
    }
}
