package com.bangkit.storyapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.storyapp.data.repository.AppRepository
import com.bangkit.storyapp.data.response.RegisterResponse
import kotlinx.coroutines.launch
import timber.log.Timber

class SignupViewModel(private val repository: AppRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerStatus = MutableLiveData<RegisterResponse>()
    val registerStatus: LiveData<RegisterResponse> = _registerStatus

    private val _isRegister = MutableLiveData<Boolean>()
    val isRegister: LiveData<Boolean> = _isRegister

    private val _isNetworkError = MutableLiveData<Boolean>()
    val isNetworkError: LiveData<Boolean> = _isNetworkError

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        _isNetworkError.value = false
        viewModelScope.launch {
            try {
                val result = repository.register(name, email, password)
                if (result.error == false) {
                    _isRegister.value = true
                    _registerStatus.value = result
                } else if (result.message == "Email is already taken") {
                    _isRegister.value = false
                } else {
                    _isRegister.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Registration failed")
                _isNetworkError.value = true
            } finally {
                _isLoading.value = false // Pastikan ini selalu dipanggil
            }
        }
    }
}
