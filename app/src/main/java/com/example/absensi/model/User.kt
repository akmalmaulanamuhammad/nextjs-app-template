package com.example.absensi.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("employee_id")
    val employeeId: String,

    @SerializedName("photo_url")
    val photoUrl: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("office_location")
    val officeLocation: OfficeLocation?,

    @SerializedName("department")
    val department: String?,

    @SerializedName("position")
    val position: String?,

    @SerializedName("join_date")
    val joinDate: String?,

    @SerializedName("last_login")
    val lastLogin: String?,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable {

    @Parcelize
    data class OfficeLocation(
        @SerializedName("id")
        val id: Long,

        @SerializedName("name")
        val name: String,

        @SerializedName("address")
        val address: String,

        @SerializedName("latitude")
        val latitude: Double,

        @SerializedName("longitude")
        val longitude: Double,

        @SerializedName("radius")
        val radius: Int,

        @SerializedName("working_hours")
        val workingHours: WorkingHours
    ) : Parcelable

    @Parcelize
    data class WorkingHours(
        @SerializedName("start")
        val start: String,

        @SerializedName("end")
        val end: String,

        @SerializedName("late_threshold")
        val lateThreshold: Int // minutes
    ) : Parcelable

    fun isEmailVerified(): Boolean {
        return !emailVerifiedAt.isNullOrEmpty()
    }

    fun isActive(): Boolean {
        return status.equals("active", ignoreCase = true)
    }

    fun isAdmin(): Boolean {
        return role.equals("admin", ignoreCase = true)
    }

    fun getFullName(): String {
        return name
    }

    fun getInitials(): String {
        return name.split(" ")
            .take(2)
            .joinToString("") { it.firstOrNull()?.toString() ?: "" }
            .uppercase()
    }

    fun getFormattedPhone(): String {
        return phone ?: "-"
    }

    fun hasOfficeLocation(): Boolean {
        return officeLocation != null
    }

    fun getWorkingHoursRange(): String {
        return officeLocation?.workingHours?.let {
            "${it.start} - ${it.end}"
        } ?: "-"
    }

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_USER = "user"
        const val STATUS_ACTIVE = "active"
        const val STATUS_INACTIVE = "inactive"
    }
}
