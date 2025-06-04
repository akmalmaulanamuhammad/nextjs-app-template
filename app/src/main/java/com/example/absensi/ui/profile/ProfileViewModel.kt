package com.example.absensi.ui.profile

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.absensi.base.BaseViewModel
import com.example.absensi.model.User
import com.example.absensi.repository.AuthRepository
import com.example.absensi.util.NetworkUtils
import java.io.File

class ProfileViewModel(application: Application) : BaseViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _profileData = MutableLiveData<NetworkUtils.NetworkResult<User>>()
    val profileData: LiveData<NetworkUtils.NetworkResult<User>> = _profileData

    private val _updateProfileResult = MutableLiveData<NetworkUtils.NetworkResult<User>>()
    val updateProfileResult: LiveData<NetworkUtils.NetworkResult<User>> = _updateProfileResult

    private val _changePasswordResult = MutableLiveData<NetworkUtils.NetworkResult<Unit>>()
    val changePasswordResult: LiveData<NetworkUtils.NetworkResult<Unit>> = _changePasswordResult

    private val _logoutResult = MutableLiveData<NetworkUtils.NetworkResult<Unit>>()
    val logoutResult: LiveData<NetworkUtils.NetworkResult<Unit>> = _logoutResult

    init {
        loadProfile()
    }

    fun loadProfile() {
        launchDataLoad(
            execution = {
                authRepository.getProfile()
            },
            onSuccess = { user ->
                _profileData.value = NetworkUtils.NetworkResult.Success(user)
            },
            onError = { error ->
                _profileData.value = NetworkUtils.NetworkResult.Error(
                    error.message ?: "Failed to load profile"
                )
            }
        )
    }

    fun updateProfile(
        name: String?,
        phone: String?,
        photoFile: File? = null
    ) {
        if (!validateProfileUpdate(name, phone)) return

        launchDataLoad(
            execution = {
                authRepository.updateProfile(name, phone, photoFile)
            },
            onSuccess = { user ->
                _updateProfileResult.value = NetworkUtils.NetworkResult.Success(user)
                _profileData.value = NetworkUtils.NetworkResult.Success(user)
            },
            onError = { error ->
                _updateProfileResult.value = NetworkUtils.NetworkResult.Error(
                    error.message ?: "Failed to update profile"
                )
            }
        )
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        if (!validatePasswordChange(currentPassword, newPassword, confirmPassword)) return

        launchDataLoad(
            execution = {
                authRepository.changePassword(currentPassword, newPassword, confirmPassword)
            },
            onSuccess = {
                _changePasswordResult.value = NetworkUtils.NetworkResult.Success(Unit)
            },
            onError = { error ->
                _changePasswordResult.value = NetworkUtils.NetworkResult.Error(
                    error.message ?: "Failed to change password"
                )
            }
        )
    }

    fun logout() {
        launchDataLoad(
            execution = {
                authRepository.logout()
            },
            onSuccess = {
                _logoutResult.value = NetworkUtils.NetworkResult.Success(Unit)
            },
            onError = { error ->
                _logoutResult.value = NetworkUtils.NetworkResult.Error(
                    error.message ?: "Failed to logout"
                )
            }
        )
    }

    private fun validateProfileUpdate(name: String?, phone: String?): Boolean {
        if (name.isNullOrBlank()) {
            showError("Name cannot be empty")
            return false
        }

        if (!phone.isNullOrBlank()) {
            // Basic phone number validation
            if (!phone.matches(Regex("^[+]?[0-9]{10,13}$"))) {
                showError("Invalid phone number format")
                return false
            }
        }

        return true
    }

    private fun validatePasswordChange(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        if (currentPassword.isBlank()) {
            showError("Current password is required")
            return false
        }

        val passwordValidation = validatePassword(newPassword)
        if (!passwordValidation.success) {
            showError(passwordValidation.errorMessage!!)
            return false
        }

        if (newPassword != confirmPassword) {
            showError("New passwords do not match")
            return false
        }

        if (currentPassword == newPassword) {
            showError("New password must be different from current password")
            return false
        }

        return true
    }

    fun getCurrentUser(): User? {
        return (_profileData.value as? NetworkUtils.NetworkResult.Success)?.data
    }

    fun isProfileComplete(): Boolean {
        val user = getCurrentUser() ?: return false
        return !user.name.isBlank() &&
                !user.email.isBlank() &&
                !user.phone.isNullOrBlank() &&
                !user.employeeId.isBlank()
    }

    fun hasVerifiedEmail(): Boolean {
        return getCurrentUser()?.isEmailVerified() == true
    }

    fun isAdmin(): Boolean {
        return getCurrentUser()?.isAdmin() == true
    }

    fun getProfileCompletion(): Int {
        val user = getCurrentUser() ?: return 0
        var completedFields = 0
        var totalFields = 0

        if (!user.name.isBlank()) completedFields++
        totalFields++

        if (!user.email.isBlank()) completedFields++
        totalFields++

        if (!user.phone.isNullOrBlank()) completedFields++
        totalFields++

        if (!user.employeeId.isBlank()) completedFields++
        totalFields++

        if (user.photoUrl != null) completedFields++
        totalFields++

        return ((completedFields.toFloat() / totalFields) * 100).toInt()
    }
}
