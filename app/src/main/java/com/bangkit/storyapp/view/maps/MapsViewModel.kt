package com.bangkit.storyapp.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bangkit.storyapp.data.pref.UserModel
import com.bangkit.storyapp.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers

class MapsViewModel(private val repository: AppRepository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun getStoriesWithLocation() = liveData(Dispatchers.IO) {
        try {
            val response = repository.getStoriesWithLocation()
            emit(Result.success(response.listStory))
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }
}