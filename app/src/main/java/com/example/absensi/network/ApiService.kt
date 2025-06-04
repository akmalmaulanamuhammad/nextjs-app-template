package com.example.absensi.network

import com.example.absensi.model.ApiResponse
import com.example.absensi.model.AttendanceRecord
import com.example.absensi.model.AttendanceRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("attendance/submit")
    @Multipart
    fun submitAttendance(
        @Part("user_id") userId: Long,
        @Part photo: MultipartBody.Part,
        @Part("latitude") latitude: Double,
        @Part("longitude") longitude: Double
    ): Call<ApiResponse<AttendanceRecord>>

    @GET("attendance/history")
    fun getAttendanceHistory(
        @Query("user_id") userId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Call<ApiResponse<List<AttendanceRecord>>>

    @GET("attendance/{id}")
    fun getAttendanceDetail(
        @Path("id") attendanceId: Long
    ): Call<ApiResponse<AttendanceRecord>>
}

object ApiClient {
    private const val BASE_URL = "https://your-api-domain.com/api/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Accept", "application/json")
                // Add any other headers like authentication tokens here
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

    val service: ApiService = retrofit.create(ApiService::class.java)
}
