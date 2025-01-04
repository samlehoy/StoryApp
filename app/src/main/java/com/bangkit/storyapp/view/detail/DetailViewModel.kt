package com.bangkit.storyapp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.response.ListStoryItem

class DetailViewModel : ViewModel() {

    private val _story = MutableLiveData<ListStoryItem>()
    val story: LiveData<ListStoryItem> = _story

    // Fungsi untuk menetapkan data story
    fun setStory(story: ListStoryItem) {
        _story.value = story
    }
}