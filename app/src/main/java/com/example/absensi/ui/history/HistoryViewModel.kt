package com.example.absensi.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.absensi.base.BaseViewModel
import com.example.absensi.model.AttendanceRecord
import com.example.absensi.network.AttendanceSummary
import com.example.absensi.repository.AttendanceRepository
import com.example.absensi.util.DateTimeUtils
import com.example.absensi.util.NetworkUtils
import java.util.*

class HistoryViewModel(application: Application) : BaseViewModel(application) {
    private val attendanceRepository = AttendanceRepository(application)

    private val _attendanceHistory = MutableLiveData<NetworkUtils.NetworkResult<List<AttendanceRecord>>>()
    val attendanceHistory: LiveData<NetworkUtils.NetworkResult<List<AttendanceRecord>>> = _attendanceHistory

    private val _attendanceSummary = MutableLiveData<NetworkUtils.NetworkResult<AttendanceSummary>>()
    val attendanceSummary: LiveData<NetworkUtils.NetworkResult<AttendanceSummary>> = _attendanceSummary

    private val _selectedDate = MutableLiveData<Calendar>()
    val selectedDate: LiveData<Calendar> = _selectedDate

    private var currentPage = 1
    private var hasMorePages = true
    private var isLoading = false

    init {
        _selectedDate.value = Calendar.getInstance()
    }

    fun loadAttendanceHistory(
        refresh: Boolean = false,
        startDate: String? = null,
        endDate: String? = null,
        status: String? = null
    ) {
        if (isLoading) return
        if (refresh) {
            currentPage = 1
            hasMorePages = true
        }
        if (!hasMorePages && !refresh) return

        isLoading = true
        launchDataLoad(
            execution = {
                attendanceRepository.getAttendanceHistory(
                    page = currentPage,
                    startDate = startDate,
                    endDate = endDate,
                    status = status
                )
            },
            onSuccess = { records ->
                val currentList = if (currentPage == 1) emptyList() else
                    (_attendanceHistory.value as? NetworkUtils.NetworkResult.Success)?.data ?: emptyList()
                
                val updatedList = currentList + records
                _attendanceHistory.value = NetworkUtils.NetworkResult.Success(updatedList)
                
                hasMorePages = records.isNotEmpty()
                currentPage++
            },
            onError = { error ->
                _attendanceHistory.value = NetworkUtils.NetworkResult.Error(
                    error.message ?: "Failed to load attendance history"
                )
            },
            showLoading = currentPage == 1
        ).also {
            isLoading = false
        }
    }

    fun loadAttendanceSummary(month: Int? = null, year: Int? = null) {
        launchDataLoad(
            execution = {
                attendanceRepository.getAttendanceSummary(month, year)
            },
            onSuccess = { summary ->
                _attendanceSummary.value = NetworkUtils.NetworkResult.Success(summary)
            },
            onError = { error ->
                _attendanceSummary.value = NetworkUtils.NetworkResult.Error(
                    error.message ?: "Failed to load attendance summary"
                )
            }
        )
    }

    fun setSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar
        loadMonthData(calendar)
    }

    fun loadMonthData(calendar: Calendar = Calendar.getInstance()) {
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based
        val year = calendar.get(Calendar.YEAR)
        
        val monthRange = DateTimeUtils.getMonthRange(calendar.time)
        
        loadAttendanceHistory(
            refresh = true,
            startDate = DateTimeUtils.formatDate(monthRange.first),
            endDate = DateTimeUtils.formatDate(monthRange.second)
        )
        
        loadAttendanceSummary(month, year)
    }

    fun refreshData() {
        loadMonthData(_selectedDate.value ?: Calendar.getInstance())
    }

    fun loadNextPage() {
        loadAttendanceHistory(refresh = false)
    }

    fun filterByStatus(status: String?) {
        val calendar = _selectedDate.value ?: Calendar.getInstance()
        val monthRange = DateTimeUtils.getMonthRange(calendar.time)
        
        loadAttendanceHistory(
            refresh = true,
            startDate = DateTimeUtils.formatDate(monthRange.first),
            endDate = DateTimeUtils.formatDate(monthRange.second),
            status = status
        )
    }

    fun getFilteredAttendance(status: AttendanceRecord.AttendanceStatus): List<AttendanceRecord> {
        return (_attendanceHistory.value as? NetworkUtils.NetworkResult.Success)?.data?.filter {
            it.getAttendanceStatus() == status
        } ?: emptyList()
    }

    fun calculateAttendanceStats(): AttendanceStats {
        val records = (_attendanceHistory.value as? NetworkUtils.NetworkResult.Success)?.data
            ?: return AttendanceStats()

        return AttendanceStats(
            total = records.size,
            present = records.count { it.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.PRESENT },
            late = records.count { it.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.LATE },
            absent = records.count { it.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.ABSENT },
            leave = records.count { it.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.LEAVE }
        )
    }

    data class AttendanceStats(
        val total: Int = 0,
        val present: Int = 0,
        val late: Int = 0,
        val absent: Int = 0,
        val leave: Int = 0
    ) {
        fun getPresentPercentage(): Float = if (total > 0) (present.toFloat() / total) * 100 else 0f
        fun getLatePercentage(): Float = if (total > 0) (late.toFloat() / total) * 100 else 0f
        fun getAbsentPercentage(): Float = if (total > 0) (absent.toFloat() / total) * 100 else 0f
        fun getLeavePercentage(): Float = if (total > 0) (leave.toFloat() / total) * 100 else 0f
    }
}
