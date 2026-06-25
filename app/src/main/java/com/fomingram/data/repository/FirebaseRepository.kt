package com.fomingram.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FirebaseMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Long = 0,
    val isFromMe: Boolean = false,
    val imageUri: String? = null
)

class FirebaseRepository {

    private val database = FirebaseDatabase
        .getInstance("https://fomingram-default-rtdb.europe-west1.firebasedatabase.app")
    private val auth = FirebaseAuth.getInstance()

    // Текущий userId (анонимный)
    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Войти анонимно
    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: ""
    }

    // Отправить сообщение в Firebase
    suspend fun sendMessage(chatId: String, text: String, senderName: String): Result<Unit> {
        return try {
            val messagesRef = database.getReference("chats/$chatId/messages")
            val messageId = messagesRef.push().key ?: return Result.failure(Exception("Ошибка ID"))

            val message = FirebaseMessage(
                id = messageId,
                text = text,
                senderId = currentUserId,
                senderName = senderName,
                timestamp = System.currentTimeMillis(),
                isFromMe = true
            )

            messagesRef.child(messageId).setValue(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Слушать сообщения в реальном времени
    fun getMessages(chatId: String): Flow<List<FirebaseMessage>> = callbackFlow {
        val messagesRef = database.getReference("chats/$chatId/messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { child ->
                    child.getValue(FirebaseMessage::class.java)?.copy(
                        isFromMe = child.getValue(FirebaseMessage::class.java)
                            ?.senderId == currentUserId
                    )
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    // Удалить сообщение
    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        return try {
            database.getReference("chats/$chatId/messages/$messageId")
                .removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}package com.fomingram.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FirebaseMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Long = 0,
    val isFromMe: Boolean = false,
    val imageUri: String? = null
)

class FirebaseRepository {

    private val database = FirebaseDatabase
        .getInstance("https://fomingram-default-rtdb.europe-west1.firebasedatabase.app")
    private val auth = FirebaseAuth.getInstance()

    // Текущий userId (анонимный)
    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Войти анонимно
    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: ""
    }

    // Отправить сообщение в Firebase
    suspend fun sendMessage(chatId: String, text: String, senderName: String): Result<Unit> {
        return try {
            val messagesRef = database.getReference("chats/$chatId/messages")
            val messageId = messagesRef.push().key ?: return Result.failure(Exception("Ошибка ID"))

            val message = FirebaseMessage(
                id = messageId,
                text = text,
                senderId = currentUserId,
                senderName = senderName,
                timestamp = System.currentTimeMillis(),
                isFromMe = true
            )

            messagesRef.child(messageId).setValue(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(chatId: String): Flow<List<FirebaseMessage>> = callbackFlow {
        val messagesRef = database.getReference("chats/$chatId/messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { child ->
                    child.getValue(FirebaseMessage::class.java)?.copy(
                        isFromMe = child.getValue(FirebaseMessage::class.java)
                            ?.senderId == currentUserId
                    )
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        return try {
            database.getReference("chats/$chatId/messages/$messageId")
                .removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
