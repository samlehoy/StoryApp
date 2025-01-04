package com.bangkit.storyapp.view.createstory

import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.repository.AppRepository
import com.bangkit.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CreateStoryViewModel(private val repository: AppRepository) : ViewModel(){
    suspend fun getToken(): String {
        return repository.getSession().map { it.token }.first()
    }

    suspend fun uploadImage(
        imageFile: File,
        description: String,
        latitude: Double?,
        longitude: Double?
    ): Result<String> {
        return try {
            val token = getToken()
            val apiService = ApiConfig.getApiService(token)

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            val latBody = latitude?.toString()?.toRequestBody("text/plain".toMediaType())
            val lonBody = longitude?.toString()?.toRequestBody("text/plain".toMediaType())

            val response = apiService.uploadImage(multipartBody, requestBody, latBody, lonBody)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}