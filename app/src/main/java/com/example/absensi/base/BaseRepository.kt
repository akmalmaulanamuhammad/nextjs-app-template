package com.example.absensi.base

import android.content.Context
import com.example.absensi.util.NetworkUtils
import com.example.absensi.util.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.io.IOException

abstract class BaseRepository(protected val context: Context) {
    protected val preferenceManager = PreferenceManager(context)

    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): NetworkUtils.NetworkResult<T> {
        try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return NetworkUtils.NetworkResult.Error(message = "No internet connection")
            }

            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return NetworkUtils.NetworkResult.Success(body)
                }
            }
            return NetworkUtils.NetworkResult.Error(
                message = "Error: ${response.code()} ${response.message()}",
                code = response.code()
            )
        } catch (e: Exception) {
            return NetworkUtils.NetworkResult.Error(
                message = when (e) {
                    is IOException -> "Network error occurred"
                    else -> e.message ?: "An error occurred"
                }
            )
        }
    }

    protected fun <T> safeApiCallFlow(
        apiCall: suspend () -> Response<T>
    ): Flow<NetworkUtils.NetworkResult<T>> = flow {
        emit(NetworkUtils.NetworkResult.Loading)
        emit(safeApiCall { apiCall() })
    }.flowOn(Dispatchers.IO)

    protected suspend fun <T> retryIO(
        times: Int = 3,
        initialDelay: Long = 100,
        maxDelay: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T = NetworkUtils.retryIO(times, initialDelay, maxDelay, factor, block)

    protected fun getAuthToken(): String? {
        return preferenceManager.getAuthToken()
    }

    protected fun getUserId(): Long? {
        return preferenceManager.getUser()?.id
    }

    protected fun clearSession() {
        preferenceManager.clearSession()
    }

    protected fun isAuthenticated(): Boolean {
        return preferenceManager.isLoggedIn()
    }

    protected fun requireAuthentication(action: () -> Unit) {
        if (isAuthenticated()) {
            action()
        } else {
            throw SecurityException("Authentication required")
        }
    }

    // Cache handling
    protected suspend fun <T> getCachedData(
        cacheKey: String,
        fetchFromNetwork: suspend () -> T,
        saveToCache: suspend (T) -> Unit,
        getFromCache: suspend () -> T?,
        forceRefresh: Boolean = false
    ): NetworkUtils.NetworkResult<T> {
        return try {
            // Try to get from cache first if not forcing refresh
            if (!forceRefresh) {
                getFromCache()?.let {
                    return NetworkUtils.NetworkResult.Success(it)
                }
            }

            // Fetch from network
            val networkData = fetchFromNetwork()
            
            // Save to cache
            saveToCache(networkData)
            
            NetworkUtils.NetworkResult.Success(networkData)
        } catch (e: Exception) {
            NetworkUtils.NetworkResult.Error(e.message ?: "An error occurred")
        }
    }

    // Database operations
    protected suspend fun <T> safeDbOperation(
        operation: suspend () -> T
    ): NetworkUtils.NetworkResult<T> {
        return try {
            NetworkUtils.NetworkResult.Success(operation())
        } catch (e: Exception) {
            NetworkUtils.NetworkResult.Error(e.message ?: "Database error occurred")
        }
    }

    // Shared Preferences operations
    protected fun <T> saveToPrefs(key: String, value: T) {
        when (value) {
            is String -> preferenceManager.preferences.edit().putString(key, value).apply()
            is Int -> preferenceManager.preferences.edit().putInt(key, value).apply()
            is Long -> preferenceManager.preferences.edit().putLong(key, value).apply()
            is Float -> preferenceManager.preferences.edit().putFloat(key, value).apply()
            is Boolean -> preferenceManager.preferences.edit().putBoolean(key, value).apply()
            else -> throw IllegalArgumentException("Type not supported")
        }
    }

    protected inline fun <reified T> getFromPrefs(key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> preferenceManager.preferences.getString(key, defaultValue as String) as T
            Int::class -> preferenceManager.preferences.getInt(key, defaultValue as Int) as T
            Long::class -> preferenceManager.preferences.getLong(key, defaultValue as Long) as T
            Float::class -> preferenceManager.preferences.getFloat(key, defaultValue as Float) as T
            Boolean::class -> preferenceManager.preferences.getBoolean(key, defaultValue as Boolean) as T
            else -> throw IllegalArgumentException("Type not supported")
        }
    }

    protected fun removeFromPrefs(key: String) {
        preferenceManager.preferences.edit().remove(key).apply()
    }
}
