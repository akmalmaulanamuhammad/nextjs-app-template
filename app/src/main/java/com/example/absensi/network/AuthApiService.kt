package com.example.absensi.network

import com.example.absensi.model.*
import retrofit2.Call
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<LoginResponse>>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<LoginResponse>>

    @POST("auth/refresh-token")
    fun refreshToken(@Header("Authorization") refreshToken: String): Call<ApiResponse<LoginResponse>>

    @POST("auth/logout")
    fun logout(@Header("Authorization") token: String): Call<ApiResponse<Unit>>

    @POST("auth/forgot-password")
    fun forgotPassword(@Body email: String): Call<ApiResponse<Unit>>

    @POST("auth/reset-password")
    fun resetPassword(
        @Query("token") token: String,
        @Body request: ResetPasswordRequest
    ): Call<ApiResponse<Unit>>

    @POST("auth/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Call<ApiResponse<Unit>>

    @GET("auth/verify-email")
    fun verifyEmail(@Query("token") token: String): Call<ApiResponse<Unit>>

    @POST("auth/resend-verification")
    fun resendVerification(@Body email: String): Call<ApiResponse<Unit>>

    @GET("auth/profile")
    fun getProfile(@Header("Authorization") token: String): Call<ApiResponse<User>>

    @PUT("auth/profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<ApiResponse<User>>
}

data class ResetPasswordRequest(
    val password: String,
    val confirmPassword: String
)

// Retrofit client extension for auth service
object AuthApiClient {
    private const val BASE_URL = "https://your-api-domain.com/api/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: AuthApiService = retrofit.create(AuthApiService::class.java)

    // Token interceptor for authenticated requests
    fun createAuthenticatedClient(token: String): AuthApiService {
        val authenticatedClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
