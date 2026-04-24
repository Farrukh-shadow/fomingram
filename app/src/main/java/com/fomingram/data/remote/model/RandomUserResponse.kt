package com.fomingram.data.remote.model

import com.google.gson.annotations.SerializedName

data class RandomUserResponse(
    val results: List<RandomUser>,
    val info: Info
)

data class RandomUser(
    val name: Name,
    val location: Location,
    val email: String,
    val phone: String,
    val picture: Picture,
    val login: Login
)

data class Name(
    val title: String,
    val first: String,
    val last: String
) {
    fun fullName() = "$first $last"
}

data class Location(
    val city: String,
    val country: String
)

data class Picture(
    val large: String,
    val medium: String,
    val thumbnail: String
)

data class Login(
    val uuid: String,
    val username: String
)

data class Info(
    val seed: String,
    val results: Int,
    val page: Int,
    val version: String
)
