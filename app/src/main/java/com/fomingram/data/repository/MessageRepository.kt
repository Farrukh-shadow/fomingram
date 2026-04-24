package com.fomingram.data.repository

import com.fomingram.data.local.dao.ContactDao
import com.fomingram.data.local.dao.MessageDao
import com.fomingram.data.local.entity.ContactEntity
import com.fomingram.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val messageDao: MessageDao,
    private val contactDao: ContactDao
) {

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForChat(chatId)

    fun getAllContacts(): Flow<List<ContactEntity>> =
        contactDao.getAllContacts()

    fun searchContacts(query: String): Flow<List<ContactEntity>> =
        contactDao.searchContacts(query)

    suspend fun sendMessage(chatId: String, text: String, contactName: String): Result<Unit> {
        return try {
            val message = MessageEntity(
                chatId = chatId,
                text = text,
                sender = "me",
                isFromMe = true
            )
            messageDao.insertMessage(message)

            // Update contact's last message
            val contact = contactDao.getContactById(chatId)
            contact?.let {
                contactDao.updateContact(
                    it.copy(
                        lastMessage = text,
                        lastMessageTime = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createChat(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    suspend fun getContactById(id: String): ContactEntity? =
        contactDao.getContactById(id)

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
        messageDao.deleteAllMessagesForChat(contact.id)
    }
}
