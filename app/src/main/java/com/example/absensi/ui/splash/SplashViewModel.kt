package com.example.absensi.ui.splash

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.absensi.base.BaseViewModel
import com.example.absensi.model.User
import com.example.absensi.repository.AuthRepository
import com.example.absensi.util.NetworkUtils
import kotlinx.coroutines.delay

class SplashViewModel(application: Application) : BaseViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _splashState = MutableLiveData<SplashState>()
    val splashState: LiveData<SplashState> = _splashState

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        launchDataLoad(
            execution = {
                // Add artificial delay for splash screen
                delay(SPLASH_DELAY)

                // Check if user is logged in
                if (!authRepository.isLoggedIn()) {
                    return@launchDataLoad SplashState.RequireAuth
                }

                // Try to get user profile
                when (val result = authRepository.getProfile()) {
                    is NetworkUtils.NetworkResult.Success -> {
                        SplashState.Authenticated(result.data)
                    }
                    is NetworkUtils.NetworkResult.Error -> {
                        if (NetworkUtils.isTokenExpired(result)) {
                            // Token expired, try to refresh
                            when (val refreshResult = authRepository.refreshToken()) {
                                is NetworkUtils.NetworkResult.Success -> {
                                    SplashState.Authenticated(refreshResult.data.user)
                                }
                                else -> SplashState.RequireAuth
                            }
                        } else {
                            SplashState.Error(result.message)
                        }
                    }
                    else -> SplashState.Error("Unknown error occurred")
                }
            },
            onSuccess = { state ->
                _splashState.value = state
                handleSplashState(state)
            },
            onError = { error ->
                _splashState.value = SplashState.Error(error.message ?: "Unknown error occurred")
                handleSplashState(_splashState.value!!)
            },
            showLoading = false
        )
    }

    private fun handleSplashState(state: SplashState) {
        when (state) {
            is SplashState.Authenticated -> {
                navigateToMain()
            }
            is SplashState.RequireAuth -> {
                navigateToLogin()
            }
            is SplashState.Error -> {
                if (authRepository.isLoggedIn()) {
                    // If there's an error but user is logged in, try to proceed
                    navigateToMain()
                } else {
                    navigateToLogin()
                }
            }
            is SplashState.Loading -> {
                // Do nothing, wait for actual state
            }
        }
    }

    fun retryInitialization() {
        checkInitialState()
    }

    private fun navigateToMain() {
        _navigationEvent.value = NavigationEvent.ToMain
    }

    private fun navigateToLogin() {
        _navigationEvent.value = NavigationEvent.ToLogin
    }

    sealed class SplashState {
        object Loading : SplashState()
        object RequireAuth : SplashState()
        data class Authenticated(val user: User) : SplashState()
        data class Error(val message: String) : SplashState()
    }

    sealed class NavigationEvent {
        object ToMain : NavigationEvent()
        object ToLogin : NavigationEvent()
    }

    companion object {
        private const val SPLASH_DELAY = 1500L // 1.5 seconds
    }
}
