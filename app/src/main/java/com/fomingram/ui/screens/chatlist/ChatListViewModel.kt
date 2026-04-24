package com.fomingram.ui.screens.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fomingram.FomingramApp
import com.fomingram.data.local.entity.ContactEntity
import com.fomingram.data.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val chats: List<ContactEntity>) : ChatListUiState()
    object Empty : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}

class ChatListViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.getAllContacts()
                    else repository.searchContacts(query)
                }
                .catch { e ->
                    _uiState.value = ChatListUiState.Error(e.message ?: "Ошибка загрузки")
                }
                .collect { contacts ->
                    _uiState.value = if (contacts.isEmpty()) {
                        ChatListUiState.Empty
                    } else {
                        ChatListUiState.Success(contacts)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun createNewChat(name: String) {
        viewModelScope.launch {
            val contact = ContactEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                isOnline = false
            )
            repository.createChat(contact)
        }
    }

    fun deleteChat(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatListViewModel(FomingramApp.instance.messageRepository) as T
            }
        }
    }
}
