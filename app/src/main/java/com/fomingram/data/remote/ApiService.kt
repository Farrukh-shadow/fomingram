package com.fomingram.data.remote

import com.fomingram.data.remote.model.RandomUserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/")
    suspend fun getRandomUser(
        @Query("results") results: Int = 1,
        @Query("nat") nationality: String = "ru,ua"
    ): RandomUserResponse

    @GET("api/")
    suspend fun getRandomUsers(
        @Query("results") results: Int = 10,
        @Query("nat") nationality: String = "ru,ua"
    ): RandomUserResponse
}
