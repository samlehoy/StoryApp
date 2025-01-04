package com.bangkit.storyapp.di

import android.content.Context
import com.bangkit.storyapp.data.database.StoryDatabase
import com.bangkit.storyapp.data.pref.UserPreference
import com.bangkit.storyapp.data.pref.dataStore
import com.bangkit.storyapp.data.repository.AppRepository
import com.bangkit.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): AppRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val storyDatabase = StoryDatabase.getDatabase(context)
        return AppRepository.getInstance(pref, apiService, storyDatabase)
    }
}
