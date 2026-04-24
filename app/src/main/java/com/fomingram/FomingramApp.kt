package com.fomingram

import android.app.Application
import com.fomingram.data.local.AppDatabase
import com.fomingram.data.remote.RetrofitClient
import com.fomingram.data.repository.MessageRepository
import com.fomingram.data.repository.UserRepository

class FomingramApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val messageRepository by lazy { MessageRepository(database.messageDao(), database.contactDao()) }
    val userRepository by lazy { UserRepository(RetrofitClient.apiService) }

    companion object {
        lateinit var instance: FomingramApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
