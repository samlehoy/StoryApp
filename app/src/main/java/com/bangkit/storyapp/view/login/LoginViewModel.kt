package com.bangkit.storyapp.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.storyapp.data.pref.UserModel
import com.bangkit.storyapp.data.repository.AppRepository
import com.bangkit.storyapp.data.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AppRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> get() = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                if (!response.error){
                    repository.saveSession(UserModel(email, response.loginResult.token,true))
                }
                _loginResult.value = Result.success(response)
            } catch (e: Exception) {
                e.printStackTrace()
                _loginResult.value = Result.failure(e)
            }
        }
    }
}