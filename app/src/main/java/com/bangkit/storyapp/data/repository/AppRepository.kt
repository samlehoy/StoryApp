package com.bangkit.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.bangkit.storyapp.data.database.StoryDatabase
import com.bangkit.storyapp.data.paging.StoryRemoteMediator
import com.bangkit.storyapp.data.pref.UserModel
import com.bangkit.storyapp.data.pref.UserPreference
import com.bangkit.storyapp.data.response.ListStoryItem
import com.bangkit.storyapp.data.response.LoginResponse
import com.bangkit.storyapp.data.response.RegisterResponse
import com.bangkit.storyapp.data.response.StoryResponse
import com.bangkit.storyapp.data.retrofit.ApiConfig
import com.bangkit.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class AppRepository private constructor(
    private val userPreference: UserPreference,
    private var apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {
    private var apiServices : ApiService
    init {
        apiServices = apiService
    }

    // Fungsi dari UserRepository
    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
        apiServices = runBlocking {
            ApiConfig.getApiService(user.token)
        }
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }

    suspend fun logout() {
        userPreference.logout()
    }

    // Fungsi dari StoryRepository
    suspend fun getStoriesWithLocation(): StoryResponse {
        return withContext(Dispatchers.IO){
            apiServices.getStoriesWithLocation()
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiServices),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStories()
            }
        ).liveData
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            database: StoryDatabase
        ): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(userPreference, apiService, database)
            }.also { instance = it }
    }
}
