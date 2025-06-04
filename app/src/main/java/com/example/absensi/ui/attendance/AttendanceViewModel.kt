package com.example.absensi.ui.attendance

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.absensi.base.BaseViewModel
import com.example.absensi.model.AttendanceRecord
import com.example.absensi.network.OfficeStatus
import com.example.absensi.repository.AttendanceRepository
import com.example.absensi.util.NetworkUtils
import java.io.File

class AttendanceViewModel(application: Application) : BaseViewModel(application) {
    private val attendanceRepository = AttendanceRepository(application)

    private val _todayAttendance = MutableLiveData<NetworkUtils.NetworkResult<AttendanceRecord?>>()
    val todayAttendance: LiveData<NetworkUtils.NetworkResult<AttendanceRecord?>> = _todayAttendance

    private val _checkInResult = MutableLiveData<NetworkUtils.NetworkResult<AttendanceRecord>>()
    val checkInResult: LiveData<NetworkUtils.NetworkResult<AttendanceRecord>> = _checkInResult

    private val _checkOutResult = MutableLiveData<NetworkUtils.NetworkResult<AttendanceRecord>>()
    val checkOutResult: LiveData<NetworkUtils.NetworkResult<AttendanceRecord>> = _checkOutResult

    private val _officeStatus = MutableLiveData<NetworkUtils.NetworkResult<OfficeStatus>>()
    val officeStatus: LiveData<NetworkUtils.NetworkResult<OfficeStatus>> = _officeStatus

    private val _attendanceSummary = MutableLiveData<NetworkUtils.NetworkResult<AttendanceSummary>>()
    val attendanceSummary: LiveData<NetworkUtils.NetworkResult<AttendanceSummary>> = _attendanceSummary

    fun getTodayAttendance() {
        launchDataLoad(
            execution = {
                attendanceRepository.getTodayAttendance()
            },
            onSuccess = { attendance ->
                _todayAttendance.value = NetworkUtils.NetworkResult.Success(attendance)
            },
            onError = { error ->
                _todayAttendance.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to get today's attendance")
            }
        )
    }

    fun checkIn(
        latitude: Double,
        longitude: Double,
        address: String,
        photoFile: File,
        notes: String? = null
    ) {
        if (!validateLocation(latitude, longitude)) return
        if (!validatePhoto(photoFile)) return

        launchDataLoad(
            execution = {
                attendanceRepository.checkIn(latitude, longitude, address, photoFile, notes)
            },
            onSuccess = { attendance ->
                _checkInResult.value = NetworkUtils.NetworkResult.Success(attendance)
                getTodayAttendance() // Refresh today's attendance
            },
            onError = { error ->
                _checkInResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Check-in failed")
            }
        )
    }

    fun checkOut(
        latitude: Double,
        longitude: Double,
        address: String,
        photoFile: File,
        notes: String? = null
    ) {
        if (!validateLocation(latitude, longitude)) return
        if (!validatePhoto(photoFile)) return

        launchDataLoad(
            execution = {
                attendanceRepository.checkOut(latitude, longitude, address, photoFile, notes)
            },
            onSuccess = { attendance ->
                _checkOutResult.value = NetworkUtils.NetworkResult.Success(attendance)
                getTodayAttendance() // Refresh today's attendance
            },
            onError = { error ->
                _checkOutResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Check-out failed")
            }
        )
    }

    fun getOfficeStatus() {
        launchDataLoad(
            execution = {
                attendanceRepository.getOfficeStatus()
            },
            onSuccess = { status ->
                _officeStatus.value = NetworkUtils.NetworkResult.Success(status)
            },
            onError = { error ->
                _officeStatus.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to get office status")
            }
        )
    }

    fun getAttendanceSummary(month: Int? = null, year: Int? = null) {
        launchDataLoad(
            execution = {
                attendanceRepository.getAttendanceSummary(month, year)
            },
            onSuccess = { summary ->
                _attendanceSummary.value = NetworkUtils.NetworkResult.Success(summary)
            },
            onError = { error ->
                _attendanceSummary.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to get attendance summary")
            }
        )
    }

    private fun validateLocation(latitude: Double, longitude: Double): Boolean {
        if (latitude == 0.0 || longitude == 0.0) {
            showError("Invalid location coordinates")
            return false
        }
        return true
    }

    private fun validatePhoto(photoFile: File): Boolean {
        if (!photoFile.exists()) {
            showError("Photo is required")
            return false
        }

        // Check file size (max 5MB)
        val maxSize = 5 * 1024 * 1024 // 5MB in bytes
        if (photoFile.length() > maxSize) {
            showError("Photo size should be less than 5MB")
            return false
        }

        return true
    }

    fun canCheckIn(): Boolean {
        val status = _officeStatus.value
        if (status is NetworkUtils.NetworkResult.Success) {
            return status.data.isWithinWorkingHours && status.data.isWithinRadius
        }
        return false
    }

    fun canCheckOut(): Boolean {
        val attendance = _todayAttendance.value
        if (attendance is NetworkUtils.NetworkResult.Success) {
            return attendance.data?.isCheckedIn() == true && attendance.data.checkOutTime == null
        }
        return false
    }

    fun refreshData() {
        getTodayAttendance()
        getOfficeStatus()
        getAttendanceSummary()
    }
}
