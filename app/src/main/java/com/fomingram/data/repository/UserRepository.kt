package com.fomingram.data.repository

import com.fomingram.data.remote.ApiService
import com.fomingram.data.remote.model.RandomUser

class UserRepository(private val apiService: ApiService) {

    suspend fun getRandomUser(): Result<RandomUser> {
        return try {
            val response = apiService.getRandomUser()
            if (response.results.isNotEmpty()) {
                Result.success(response.results.first())
            } else {
                Result.failure(Exception("Нет данных от сервера"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRandomUsers(count: Int = 10): Result<List<RandomUser>> {
        return try {
            val response = apiService.getRandomUsers(results = count)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
