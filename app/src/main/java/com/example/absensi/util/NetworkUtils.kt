package com.example.absensi.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkUtils {
    private const val TOKEN_EXPIRED_CODE = 401
    private const val MAX_RETRIES = 3
    private const val INITIAL_DELAY = 1000L // 1 second
    private const val MAX_DELAY = 5000L // 5 seconds
    private const val BACKOFF_MULTIPLIER = 2.0

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun isTokenExpired(error: Exception): Boolean {
        return (error as? HttpException)?.code() == TOKEN_EXPIRED_CODE
    }

    fun isTokenExpired(result: NetworkResult<*>): Boolean {
        return result is NetworkResult.Error && result.code == TOKEN_EXPIRED_CODE
    }

    suspend fun <T> retryIO(
        times: Int = MAX_RETRIES,
        initialDelay: Long = INITIAL_DELAY,
        maxDelay: Long = MAX_DELAY,
        factor: Double = BACKOFF_MULTIPLIER,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                when (e) {
                    is IOException,
                    is SocketTimeoutException,
                    is UnknownHostException -> {
                        // Retry on network errors
                    }
                    else -> throw e
                }
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }

    sealed class NetworkResult<out T> {
        data class Success<out T>(val data: T) : NetworkResult<T>()
        data class Error(
            val message: String,
            val code: Int? = null,
            val exception: Exception? = null
        ) : NetworkResult<Nothing>()
        object Loading : NetworkResult<Nothing>()

        fun isSuccessful(): Boolean = this is Success

        fun getOrNull(): T? = when (this) {
            is Success -> data
            else -> null
        }

        fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
            is Success -> data
            else -> default
        }

        fun getOrThrow(): T = when (this) {
            is Success -> data
            is Error -> throw exception ?: IllegalStateException(message)
            is Loading -> throw IllegalStateException("Result is still loading")
        }
    }

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    401 -> "Unauthorized access. Please login again."
                    403 -> "Access forbidden. You don't have permission."
                    404 -> "Resource not found."
                    500 -> "Internal server error. Please try again later."
                    else -> "Network error occurred. Please try again."
                }
            }
            is UnknownHostException -> "Unable to reach server. Please check your internet connection."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            is IOException -> "Network error occurred. Please check your internet connection."
            else -> throwable.message ?: "An unexpected error occurred."
        }
    }

    fun getNetworkType(context: Context): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE

            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
                ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            }
        }
    }

    enum class NetworkType {
        WIFI,
        CELLULAR,
        ETHERNET,
        NONE
    }
}
