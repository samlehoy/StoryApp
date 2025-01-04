package com.bangkit.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bangkit.storyapp.data.pref.UserModel
import com.bangkit.storyapp.data.repository.AppRepository
import com.bangkit.storyapp.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository // Tambahkan parameter ini
) : ViewModel() {

    private val _storyTrigger = MutableLiveData(Unit)

    // Aliran data Paging yang akan di-refresh
    val story: LiveData<PagingData<ListStoryItem>> = _storyTrigger.switchMap {
        repository.getStories().cachedIn(viewModelScope)
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}