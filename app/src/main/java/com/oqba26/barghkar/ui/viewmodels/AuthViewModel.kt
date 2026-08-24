package com.oqba26.barghkar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.model.UserProfile
import com.oqba26.barghkar.data.model.UserRole
import com.oqba26.barghkar.data.remote.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(value = null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(value = null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = repository.sessionStatus

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    fetchProfile()
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _userProfile.value = repository.getUserProfile()
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.signUp(email, password)
            } catch (e: Exception) {
                _error.value = e.message ?: "خطا در ثبت‌نام"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.signIn(email, password)
            } catch (e: Exception) {
                _error.value = e.message ?: "خطا در ورود"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
    
    fun clearError() {
        _error.value = null
    }

    fun linkMaster(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.linkMaster(email)
                fetchProfile() // بروزرسانی پروفایل بعد از لینک شدن
            } catch (e: Exception) {
                _error.value = e.message ?: "خطا در اتصال به اوستا"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isMaster(): Boolean = userProfile.value?.role == UserRole.MASTER
}
