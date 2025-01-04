package com.bangkit.storyapp.view.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bangkit.storyapp.data.pref.UserModel
import com.bangkit.storyapp.data.repository.AppRepository

class WelcomeViewModel (private val repository: AppRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}