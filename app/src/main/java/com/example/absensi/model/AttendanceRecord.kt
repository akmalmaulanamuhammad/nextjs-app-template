package com.example.absensi.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttendanceRecord(
    @SerializedName("id")
    val id: Long,

    @SerializedName("user_id")
    val userId: Long,

    @SerializedName("user")
    val user: User?,

    @SerializedName("date")
    val date: String,

    @SerializedName("check_in_time")
    val checkInTime: String?,

    @SerializedName("check_out_time")
    val checkOutTime: String?,

    @SerializedName("check_in_location")
    val checkInLocation: Location?,

    @SerializedName("check_out_location")
    val checkOutLocation: Location?,

    @SerializedName("check_in_photo")
    val checkInPhoto: String?,

    @SerializedName("check_out_photo")
    val checkOutPhoto: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable {

    @Parcelize
    data class Location(
        @SerializedName("latitude")
        val latitude: Double,

        @SerializedName("longitude")
        val longitude: Double,

        @SerializedName("address")
        val address: String?
    ) : Parcelable

    fun isCheckedIn(): Boolean {
        return !checkInTime.isNullOrEmpty()
    }

    fun isCheckedOut(): Boolean {
        return !checkOutTime.isNullOrEmpty()
    }

    fun getAttendanceStatus(): AttendanceStatus {
        return when (status.lowercase()) {
            "present" -> AttendanceStatus.PRESENT
            "late" -> AttendanceStatus.LATE
            "absent" -> AttendanceStatus.ABSENT
            "leave" -> AttendanceStatus.LEAVE
            else -> AttendanceStatus.UNKNOWN
        }
    }

    fun getFormattedCheckInTime(): String {
        return checkInTime ?: "-"
    }

    fun getFormattedCheckOutTime(): String {
        return checkOutTime ?: "-"
    }

    fun getWorkDuration(): String {
        if (checkInTime.isNullOrEmpty() || checkOutTime.isNullOrEmpty()) {
            return "-"
        }

        return try {
            val checkIn = DateTimeUtils.parseTime(checkInTime)
            val checkOut = DateTimeUtils.parseTime(checkOutTime)
            
            if (checkIn != null && checkOut != null) {
                val duration = checkOut.time - checkIn.time
                val hours = duration / (60 * 60 * 1000)
                val minutes = (duration / (60 * 1000)) % 60
                String.format("%02d:%02d", hours, minutes)
            } else {
                "-"
            }
        } catch (e: Exception) {
            "-"
        }
    }

    fun isWithinOfficeRadius(officeLocation: User.OfficeLocation): Boolean {
        val checkInLoc = checkInLocation ?: return false
        
        val distance = LocationUtils.calculateDistance(
            checkInLoc.latitude,
            checkInLoc.longitude,
            officeLocation.latitude,
            officeLocation.longitude
        )
        
        return distance <= officeLocation.radius
    }

    enum class AttendanceStatus {
        PRESENT,
        LATE,
        ABSENT,
        LEAVE,
        UNKNOWN;

        fun getColorRes(): Int {
            return when (this) {
                PRESENT -> R.color.on_time
                LATE -> R.color.late
                ABSENT -> R.color.absent
                LEAVE -> R.color.warning
                UNKNOWN -> R.color.gray_500
            }
        }

        fun getDisplayText(): String {
            return when (this) {
                PRESENT -> "Present"
                LATE -> "Late"
                ABSENT -> "Absent"
                LEAVE -> "Leave"
                UNKNOWN -> "Unknown"
            }
        }
    }

    companion object {
        const val STATUS_PRESENT = "present"
        const val STATUS_LATE = "late"
        const val STATUS_ABSENT = "absent"
        const val STATUS_LEAVE = "leave"
    }
}
