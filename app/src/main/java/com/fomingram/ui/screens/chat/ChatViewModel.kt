package com.fomingram.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fomingram.FomingramApp
import com.fomingram.data.local.entity.MessageEntity
import com.fomingram.data.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<MessageEntity>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    private val repository: MessageRepository,
    private val chatId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _showEmptyError = MutableStateFlow(false)
    val showEmptyError: StateFlow<Boolean> = _showEmptyError.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            repository.getMessagesForChat(chatId)
                .catch { e ->
                    _uiState.value = ChatUiState.Error(e.message ?: "Ошибка загрузки сообщений")
                }
                .collect { messages ->
                    _uiState.value = ChatUiState.Success(messages)
                }
        }
    }

    fun onInputChange(text: String) {
        _inputText.value = text
        if (text.isNotBlank()) _showEmptyError.value = false
    }

    fun sendMessage(contactName: String) {
        val text = _inputText.value.trim()
        if (text.isBlank()) {
            _showEmptyError.value = true
            return
        }
        viewModelScope.launch {
            _inputText.value = ""
            _showEmptyError.value = false
            repository.sendMessage(chatId, text, contactName)
        }
    }

    companion object {
        fun factory(chatId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(
                        FomingramApp.instance.messageRepository,
                        chatId
                    ) as T
                }
            }
    }
}
