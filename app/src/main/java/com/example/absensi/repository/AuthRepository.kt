package com.example.absensi.repository

import android.content.Context
import com.example.absensi.base.BaseRepository
import com.example.absensi.model.*
import com.example.absensi.network.AuthApiClient
import com.example.absensi.network.AuthApiService
import com.example.absensi.util.NetworkUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AuthRepository(context: Context) : BaseRepository(context) {
    private val authService: AuthApiService by lazy {
        getAuthToken()?.let { token ->
            AuthApiClient.createAuthenticatedClient(token)
        } ?: AuthApiClient.service
    }

    suspend fun login(email: String, password: String, deviceToken: String? = null): NetworkUtils.NetworkResult<LoginResponse> {
        return safeApiCall {
            val request = LoginRequest(email, password, deviceToken)
            authService.login(request)
        }.also { result ->
            if (result is NetworkUtils.NetworkResult.Success) {
                saveAuthData(result.data)
            }
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String?,
        employeeId: String
    ): NetworkUtils.NetworkResult<LoginResponse> {
        return safeApiCall {
            val request = RegisterRequest(name, email, password, phone, employeeId)
            authService.register(request)
        }.also { result ->
            if (result is NetworkUtils.NetworkResult.Success) {
                saveAuthData(result.data)
            }
        }
    }

    suspend fun logout(): NetworkUtils.NetworkResult<Unit> {
        return safeApiCall {
            val token = getAuthToken() ?: return@safeApiCall throw Exception("No auth token found")
            authService.logout("Bearer $token")
        }.also {
            if (it is NetworkUtils.NetworkResult.Success) {
                clearSession()
            }
        }
    }

    suspend fun refreshToken(): NetworkUtils.NetworkResult<LoginResponse> {
        return safeApiCall {
            val refreshToken = getRefreshToken() ?: throw Exception("No refresh token found")
            authService.refreshToken("Bearer $refreshToken")
        }.also { result ->
            if (result is NetworkUtils.NetworkResult.Success) {
                saveAuthData(result.data)
            }
        }
    }

    suspend fun forgotPassword(email: String): NetworkUtils.NetworkResult<Unit> {
        return safeApiCall {
            authService.forgotPassword(email)
        }
    }

    suspend fun resetPassword(
        token: String,
        password: String,
        confirmPassword: String
    ): NetworkUtils.NetworkResult<Unit> {
        return safeApiCall {
            val request = ResetPasswordRequest(password, confirmPassword)
            authService.resetPassword(token, request)
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): NetworkUtils.NetworkResult<Unit> {
        return safeApiCall {
            val token = getAuthToken() ?: return@safeApiCall throw Exception("No auth token found")
            val request = ChangePasswordRequest(currentPassword, newPassword, confirmPassword)
            authService.changePassword("Bearer $token", request)
        }
    }

    suspend fun getProfile(): NetworkUtils.NetworkResult<User> {
        return safeApiCall {
            val token = getAuthToken() ?: return@safeApiCall throw Exception("No auth token found")
            authService.getProfile("Bearer $token")
        }.also { result ->
            if (result is NetworkUtils.NetworkResult.Success) {
                saveUser(result.data)
            }
        }
    }

    suspend fun updateProfile(
        name: String?,
        phone: String?,
        photoFile: File?
    ): NetworkUtils.NetworkResult<User> {
        return safeApiCall {
            val token = getAuthToken() ?: return@safeApiCall throw Exception("No auth token found")
            val request = UpdateProfileRequest(name, phone, null)

            // Add photo if provided
            val photo = photoFile?.let {
                val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photo", it.name, requestBody)
            }

            authService.updateProfile("Bearer $token", request)
        }.also { result ->
            if (result is NetworkUtils.NetworkResult.Success) {
                saveUser(result.data)
            }
        }
    }

    private fun saveAuthData(loginResponse: LoginResponse) {
        preferenceManager.saveAuthToken(loginResponse.token)
        preferenceManager.saveUser(loginResponse.user)
    }

    private fun getRefreshToken(): String? {
        return preferenceManager.getRefreshToken()
    }

    private fun saveUser(user: User) {
        preferenceManager.saveUser(user)
    }

    fun isLoggedIn(): Boolean {
        return preferenceManager.isLoggedIn()
    }

    fun getUser(): User? {
        return preferenceManager.getUser()
    }
}
