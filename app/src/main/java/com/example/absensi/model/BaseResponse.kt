package com.example.absensi.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("errors")
    val errors: List<String>? = null,
    
    @SerializedName("meta")
    val meta: MetaData? = null
)

data class MetaData(
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("last_page")
    val lastPage: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total")
    val total: Int
)

data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("errors")
    val errors: List<String>? = null
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("device_token")
    val deviceToken: String? = null
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: User,
    
    @SerializedName("token_type")
    val tokenType: String = "Bearer"
)

data class RegisterRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("employee_id")
    val employeeId: String
)

data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("photo")
    val photo: String?
)

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,
    
    @SerializedName("new_password")
    val newPassword: String,
    
    @SerializedName("confirm_password")
    val confirmPassword: String
)

data class PaginatedResponse<T>(
    @SerializedName("data")
    val data: List<T>,
    
    @SerializedName("meta")
    val meta: MetaData
)

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val error: ErrorResponse? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(
        message: String,
        data: T? = null,
        error: ErrorResponse? = null
    ) : Resource<T>(data, message, error)
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

data class ValidationResult(
    val success: Boolean,
    val errorMessage: String? = null
)

fun validateEmail(email: String): ValidationResult {
    return when {
        email.isEmpty() -> ValidationResult(false, "Email is required")
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            ValidationResult(false, "Invalid email format")
        else -> ValidationResult(true)
    }
}

fun validatePassword(password: String): ValidationResult {
    return when {
        password.isEmpty() -> ValidationResult(false, "Password is required")
        password.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
        else -> ValidationResult(true)
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
    return when {
        confirmPassword.isEmpty() -> ValidationResult(false, "Confirm password is required")
        password != confirmPassword -> ValidationResult(false, "Passwords do not match")
        else -> ValidationResult(true)
    }
}
