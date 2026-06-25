package com.fomingram.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fomingram.data.repository.FirebaseMessage
import com.fomingram.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class FirebaseChatUiState {
    object Loading : FirebaseChatUiState()
    data class Success(val messages: List<FirebaseMessage>) : FirebaseChatUiState()
    data class Error(val message: String) : FirebaseChatUiState()
}

class FirebaseChatViewModel(
    private val repository: FirebaseRepository,
    private val chatId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<FirebaseChatUiState>(FirebaseChatUiState.Loading)
    val uiState: StateFlow<FirebaseChatUiState> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _myName = MutableStateFlow("Я")
    val myName: StateFlow<String> = _myName.asStateFlow()

    init {
        signInAndLoad()
    }

    private fun signInAndLoad() {
        viewModelScope.launch {
            try {
                repository.signInAnonymously()
                loadMessages()
            } catch (e: Exception) {
                _uiState.value = FirebaseChatUiState.Error("Ошибка подключения")
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            repository.getMessages(chatId)
                .catch { e ->
                    _uiState.value = FirebaseChatUiState.Error(e.message ?: "Ошибка")
                }
                .collect { messages ->
                    _uiState.value = FirebaseChatUiState.Success(messages)
                }
        }
    }

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun setMyName(name: String) {
        _myName.value = name
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _inputText.value = ""
            repository.sendMessage(chatId, text, _myName.value)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(chatId, messageId)
        }
    }

    companion object {
        fun factory(chatId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FirebaseChatViewModel(
                        FirebaseRepository(),
                        chatId
                    ) as T
                }
            }
    }
}
