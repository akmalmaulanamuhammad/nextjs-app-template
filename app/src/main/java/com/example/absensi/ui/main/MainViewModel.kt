package com.example.absensi.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.absensi.base.BaseViewModel
import com.example.absensi.model.User
import com.example.absensi.repository.AuthRepository
import com.example.absensi.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : BaseViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Attendance)
    val navigationState: StateFlow<NavigationState> = _navigationState

    private val _syncState = MutableLiveData<SyncState>()
    val syncState: LiveData<SyncState> = _syncState

    init {
        loadUserData()
    }

    fun loadUserData() {
        launchDataLoad(
            execution = {
                authRepository.getProfile()
            },
            onSuccess = { user ->
                _user.value = user
            },
            onError = { error ->
                if (NetworkUtils.isTokenExpired(error)) {
                    handleSessionExpired()
                }
            },
            showLoading = false
        )
    }

    fun navigate(state: NavigationState) {
        _navigationState.value = state
    }

    fun syncData() {
        _syncState.value = SyncState.Syncing
        launchDataLoad(
            execution = {
                // Implement data synchronization logic here
                // For example, sync local data with server
                // This is just a placeholder
                kotlinx.coroutines.delay(1000)
                Unit
            },
            onSuccess = {
                _syncState.value = SyncState.Success
            },
            onError = { error ->
                _syncState.value = SyncState.Error(error.message ?: "Sync failed")
            },
            showLoading = false
        )
    }

    fun logout() {
        launchDataLoad(
            execution = {
                authRepository.logout()
            },
            onSuccess = {
                _user.value = null
            },
            onError = { error ->
                showError(error.message ?: "Logout failed")
            }
        )
    }

    fun refreshUserData() {
        loadUserData()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    fun getUser(): User? {
        return _user.value ?: authRepository.getUser()
    }

    private fun handleSessionExpired() {
        authRepository.clearSession()
        _user.value = null
    }

    sealed class NavigationState {
        object Attendance : NavigationState()
        object History : NavigationState()
        object Profile : NavigationState()
        data class Detail(val id: Long) : NavigationState()
    }

    sealed class SyncState {
        object Syncing : SyncState()
        object Success : SyncState()
        data class Error(val message: String) : SyncState()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
