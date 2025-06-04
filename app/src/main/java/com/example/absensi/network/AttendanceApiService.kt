package com.example.absensi.network

import com.example.absensi.model.ApiResponse
import com.example.absensi.model.AttendanceRecord
import com.example.absensi.model.PaginatedResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AttendanceApiService {
    @GET("attendance")
    suspend fun getAttendanceHistory(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<PaginatedResponse<AttendanceRecord>>>

    @GET("attendance/today")
    suspend fun getTodayAttendance(): Response<ApiResponse<AttendanceRecord?>>

    @Multipart
    @POST("attendance/check-in")
    suspend fun checkIn(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("address") address: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("notes") notes: RequestBody? = null
    ): Response<ApiResponse<AttendanceRecord>>

    @Multipart
    @POST("attendance/check-out")
    suspend fun checkOut(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("address") address: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("notes") notes: RequestBody? = null
    ): Response<ApiResponse<AttendanceRecord>>

    @GET("attendance/{id}")
    suspend fun getAttendanceDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<AttendanceRecord>>

    @GET("attendance/summary")
    suspend fun getAttendanceSummary(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<AttendanceSummary>>

    @GET("attendance/office-status")
    suspend fun getOfficeStatus(): Response<ApiResponse<OfficeStatus>>
}

data class AttendanceSummary(
    val total: Int,
    val present: Int,
    val late: Int,
    val absent: Int,
    val leave: Int,
    val workingDays: Int,
    val averageWorkHours: Float,
    val monthlyReport: List<DailySummary>
)

data class DailySummary(
    val date: String,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val workHours: Float?
)

data class OfficeStatus(
    val isWithinWorkingHours: Boolean,
    val isWithinRadius: Boolean,
    val currentTime: String,
    val workingHours: WorkingHours,
    val distance: Double, // Distance from office in meters
    val message: String?
)

data class WorkingHours(
    val start: String,
    val end: String,
    val lateThreshold: Int // minutes
)

// Retrofit client extension for attendance service
object AttendanceApiClient {
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

    val service: AttendanceApiService = retrofit.create(AttendanceApiService::class.java)

    // Create authenticated client with token
    fun createAuthenticatedClient(token: String): AttendanceApiService {
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
            .create(AttendanceApiService::class.java)
    }
}
