package com.example.absensi.ui.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.absensi.base.BaseViewModel
import com.example.absensi.model.LoginResponse
import com.example.absensi.model.User
import com.example.absensi.repository.AuthRepository
import com.example.absensi.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(application: Application) : BaseViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _loginResult = MutableLiveData<NetworkUtils.NetworkResult<LoginResponse>>()
    val loginResult: LiveData<NetworkUtils.NetworkResult<LoginResponse>> = _loginResult

    private val _registerResult = MutableLiveData<NetworkUtils.NetworkResult<LoginResponse>>()
    val registerResult: LiveData<NetworkUtils.NetworkResult<LoginResponse>> = _registerResult

    private val _logoutResult = MutableLiveData<NetworkUtils.NetworkResult<Unit>>()
    val logoutResult: LiveData<NetworkUtils.NetworkResult<Unit>> = _logoutResult

    private val _profileResult = MutableLiveData<NetworkUtils.NetworkResult<User>>()
    val profileResult: LiveData<NetworkUtils.NetworkResult<User>> = _profileResult

    private val _forgotPasswordResult = MutableLiveData<NetworkUtils.NetworkResult<Unit>>()
    val forgotPasswordResult: LiveData<NetworkUtils.NetworkResult<Unit>> = _forgotPasswordResult

    private val _resetPasswordResult = MutableLiveData<NetworkUtils.NetworkResult<Unit>>()
    val resetPasswordResult: LiveData<NetworkUtils.NetworkResult<Unit>> = _resetPasswordResult

    private val _changePasswordResult = MutableLiveData<NetworkUtils.NetworkResult<Unit>>()
    val changePasswordResult: LiveData<NetworkUtils.NetworkResult<Unit>> = _changePasswordResult

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String, deviceToken: String? = null) {
        if (!validateLoginInput(email, password)) return

        launchDataLoad(
            execution = {
                authRepository.login(email, password, deviceToken)
            },
            onSuccess = { response ->
                _loginResult.value = NetworkUtils.NetworkResult.Success(response)
                _authState.value = AuthState.Authenticated(response.user)
            },
            onError = { error ->
                _loginResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Login failed")
                _authState.value = AuthState.Unauthenticated
            }
        )
    }

    fun register(
        name: String,
        email: String,
        password: String,
        phone: String?,
        employeeId: String
    ) {
        if (!validateRegisterInput(name, email, password, employeeId)) return

        launchDataLoad(
            execution = {
                authRepository.register(name, email, password, phone, employeeId)
            },
            onSuccess = { response ->
                _registerResult.value = NetworkUtils.NetworkResult.Success(response)
                _authState.value = AuthState.Authenticated(response.user)
            },
            onError = { error ->
                _registerResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Registration failed")
                _authState.value = AuthState.Unauthenticated
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
                _authState.value = AuthState.Unauthenticated
            },
            onError = { error ->
                _logoutResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Logout failed")
            }
        )
    }

    fun getProfile() {
        launchDataLoad(
            execution = {
                authRepository.getProfile()
            },
            onSuccess = { user ->
                _profileResult.value = NetworkUtils.NetworkResult.Success(user)
                _authState.value = AuthState.Authenticated(user)
            },
            onError = { error ->
                _profileResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to get profile")
            }
        )
    }

    fun forgotPassword(email: String) {
        if (!validateEmail(email)) return

        launchDataLoad(
            execution = {
                authRepository.forgotPassword(email)
            },
            onSuccess = {
                _forgotPasswordResult.value = NetworkUtils.NetworkResult.Success(Unit)
            },
            onError = { error ->
                _forgotPasswordResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to process request")
            }
        )
    }

    fun resetPassword(token: String, password: String, confirmPassword: String) {
        if (!validatePasswordReset(password, confirmPassword)) return

        launchDataLoad(
            execution = {
                authRepository.resetPassword(token, password, confirmPassword)
            },
            onSuccess = {
                _resetPasswordResult.value = NetworkUtils.NetworkResult.Success(Unit)
            },
            onError = { error ->
                _resetPasswordResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to reset password")
            }
        )
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (!validatePasswordChange(currentPassword, newPassword, confirmPassword)) return

        launchDataLoad(
            execution = {
                authRepository.changePassword(currentPassword, newPassword, confirmPassword)
            },
            onSuccess = {
                _changePasswordResult.value = NetworkUtils.NetworkResult.Success(Unit)
            },
            onError = { error ->
                _changePasswordResult.value = NetworkUtils.NetworkResult.Error(error.message ?: "Failed to change password")
            }
        )
    }

    fun checkAuthState() {
        _authState.value = if (authRepository.isLoggedIn()) {
            authRepository.getUser()?.let {
                AuthState.Authenticated(it)
            } ?: AuthState.Unauthenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)

        return emailValidation.success && passwordValidation.success
    }

    private fun validateRegisterInput(
        name: String,
        email: String,
        password: String,
        employeeId: String
    ): Boolean {
        if (name.isBlank()) {
            showError("Name is required")
            return false
        }

        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)

        if (employeeId.isBlank()) {
            showError("Employee ID is required")
            return false
        }

        return emailValidation.success && passwordValidation.success
    }

    private fun validatePasswordReset(password: String, confirmPassword: String): Boolean {
        val passwordValidation = validatePassword(password)
        val confirmValidation = validateConfirmPassword(password, confirmPassword)

        return passwordValidation.success && confirmValidation.success
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

        return validatePasswordReset(newPassword, confirmPassword)
    }

    sealed class AuthState {
        object Initial : AuthState()
        object Unauthenticated : AuthState()
        data class Authenticated(val user: User) : AuthState()
    }
}
