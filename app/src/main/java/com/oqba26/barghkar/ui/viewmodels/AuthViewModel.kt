package com.oqba26.barghkar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.model.ApprenticePermission
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

    private val _apprentices = MutableStateFlow<List<UserProfile>>(emptyList())
    val apprentices: StateFlow<List<UserProfile>> = _apprentices.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = repository.sessionStatus

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    // چک کردن تاییدیه ایمیل برای نشست‌های موجود
                    if (!repository.isEmailConfirmed()) {
                        _error.value = "ایمیل شما هنوز تایید نشده است."
                        repository.signOut()
                    } else {
                        fetchProfile()
                        fetchApprentices()
                    }
                } else {
                    _userProfile.value = null
                    _apprentices.value = emptyList()
                }
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _userProfile.value = repository.getUserProfile()
        }
    }

    fun fetchApprentices() {
        viewModelScope.launch {
            _apprentices.value = repository.getApprentices()
        }
    }

    fun updateApprenticePermissions(apprenticeId: String, permissions: List<ApprenticePermission>) {
        viewModelScope.launch {
            try {
                repository.updateApprenticePermissions(apprenticeId, permissions)
                fetchApprentices()
            } catch (_: Exception) {
                _error.value = "خطا در بروزرسانی دسترسی‌ها"
            }
        }
    }

    fun removeApprentice(apprenticeId: String) {
        viewModelScope.launch {
            try {
                repository.removeApprentice(apprenticeId)
                fetchApprentices()
            } catch (_: Exception) {
                _error.value = "خطا در حذف شاگرد"
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val validationError = repository.validateCredentials(email, password)
                if (validationError != null) {
                    _error.value = validationError
                    return@launch
                }
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
                val validationError = repository.validateCredentials(email, password)
                if (validationError != null) {
                    _error.value = validationError
                    return@launch
                }
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

    fun addApprentice(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val validationError = repository.validateApprenticeEmail(email)
                if (validationError != null) {
                    _error.value = validationError
                    return@launch
                }
                repository.addApprenticeByEmail(email)
                fetchApprentices()
            } catch (e: Exception) {
                _error.value = e.message ?: "خطا در افزودن شاگرد"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isMaster(): Boolean = userProfile.value?.role == UserRole.MASTER
}
