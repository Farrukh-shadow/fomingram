package com.fomingram.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fomingram.data.local.dao.ContactDao
import com.fomingram.data.local.dao.MessageDao
import com.fomingram.data.local.entity.ContactEntity
import com.fomingram.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MessageEntity::class, ContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fomingram_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.contactDao(), database.messageDao())
                }
            }
        }

        suspend fun populateInitialData(contactDao: ContactDao, messageDao: MessageDao) {
            val now = System.currentTimeMillis()
            val contacts = listOf(
                ContactEntity(
                    id = "1",
                    name = "Мария Иванова",
                    lastMessage = "Привет! Ты уже видел новый проект? 🔥",
                    lastMessageTime = now - 60_000,
                    unreadCount = 2,
                    isOnline = true
                ),
                ContactEntity(
                    id = "2",
                    name = "Дмитрий К.",
                    lastMessage = "✓✓ Окей, договорились!",
                    lastMessageTime = now - 3_600_000,
                    unreadCount = 0,
                    isOnline = false
                ),
                ContactEntity(
                    id = "3",
                    name = "Анна С.",
                    lastMessage = "Скинь файл, пожалуйста",
                    lastMessageTime = now - 7_200_000,
                    unreadCount = 1,
                    isOnline = true
                ),
                ContactEntity(
                    id = "4",
                    name = "Павел Р.",
                    lastMessage = "✓ Увидимся в пятницу",
                    lastMessageTime = now - 86_400_000,
                    unreadCount = 0,
                    isOnline = false
                ),
                ContactEntity(
                    id = "5",
                    name = "Катя В.",
                    lastMessage = "📷 фотография",
                    lastMessageTime = now - 172_800_000,
                    unreadCount = 0,
                    isOnline = false
                ),
                ContactEntity(
                    id = "6",
                    name = "Игорь Д.",
                    lastMessage = "✓✓ Ок, понял",
                    lastMessageTime = now - 259_200_000,
                    unreadCount = 0,
                    isOnline = true
                )
            )
            contacts.forEach { contactDao.insertContact(it) }

            // Seed messages for Мария Иванова chat
            val mariaMessages = listOf(
                MessageEntity(chatId = "1", text = "Привет! Ты уже видел новый проект? 🔥", sender = "Мария Иванова", isFromMe = false, timestamp = now - 300_000),
                MessageEntity(chatId = "1", text = "Да, выглядит круто! Уже изучаю архитектуру 🤩", sender = "me", isFromMe = true, timestamp = now - 270_000),
                MessageEntity(chatId = "1", text = "Fomingram — это вообще бомба! MVVM + Room = 🖊", sender = "Мария Иванова", isFromMe = false, timestamp = now - 240_000),
                MessageEntity(chatId = "1", text = "Согласен, Retrofit тоже хорошо зашёл 👍", sender = "me", isFromMe = true, timestamp = now - 210_000),
                MessageEntity(chatId = "1", text = "Когда покажешь демо? Очень жду! ☺", sender = "Мария Иванова", isFromMe = false, timestamp = now - 180_000, isRead = false),
                MessageEntity(chatId = "1", text = "Скоро, финальные правки делаю ✏", sender = "me", isFromMe = true, timestamp = now - 60_000)
            )
            mariaMessages.forEach { messageDao.insertMessage(it) }
        }
    }
}
