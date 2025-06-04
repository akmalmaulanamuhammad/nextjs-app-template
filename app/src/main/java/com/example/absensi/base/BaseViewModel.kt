package com.example.absensi.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.absensi.AbsensiApplication
import com.example.absensi.util.NetworkUtils
import com.example.absensi.util.PreferenceManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val preferenceManager: PreferenceManager by lazy {
        AbsensiApplication.getPreferenceManager()
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    protected fun <T> launchDataLoad(
        execution: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: ((Exception) -> Unit)? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                if (showLoading) showLoading()
                val result = execution()
                onSuccess(result)
            } catch (e: Exception) {
                Timber.e(e, "Error in data load")
                onError?.invoke(e) ?: handleError(e)
            } finally {
                if (showLoading) hideLoading()
            }
        }
    }

    protected fun <T> launchDataLoadWithResult(
        execution: suspend () -> NetworkUtils.NetworkResult<T>,
        onSuccess: (T) -> Unit,
        onError: ((NetworkUtils.NetworkResult.Error) -> Unit)? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                if (showLoading) showLoading()
                when (val result = execution()) {
                    is NetworkUtils.NetworkResult.Success -> onSuccess(result.data)
                    is NetworkUtils.NetworkResult.Error -> {
                        onError?.invoke(result) ?: handleNetworkError(result)
                    }
                    is NetworkUtils.NetworkResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in data load")
                handleError(e)
            } finally {
                if (showLoading) hideLoading()
            }
        }
    }

    protected fun showLoading() {
        _isLoading.postValue(true)
    }

    protected fun hideLoading() {
        _isLoading.postValue(false)
    }

    protected fun showError(message: String) {
        _error.postValue(message)
    }

    protected open fun handleError(error: Exception) {
        val message = when (error) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Connection timed out"
            is retrofit2.HttpException -> {
                when (error.code()) {
                    401 -> "Unauthorized access"
                    403 -> "Access forbidden"
                    404 -> "Resource not found"
                    500 -> "Internal server error"
                    else -> "Network error occurred"
                }
            }
            else -> error.message ?: "An unexpected error occurred"
        }
        showError(message)
    }

    protected open fun handleNetworkError(error: NetworkUtils.NetworkResult.Error) {
        showError(error.message)
    }

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(getApplication())
    }

    protected fun isUserLoggedIn(): Boolean {
        return preferenceManager.isLoggedIn()
    }

    protected fun getCurrentUser() = preferenceManager.getUser()

    protected fun getAuthToken() = preferenceManager.getAuthToken()

    protected fun clearSession() {
        preferenceManager.clearSession()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel Cleared: ${javaClass.simpleName}")
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}
