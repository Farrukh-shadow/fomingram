package com.fomingram.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fomingram.FomingramApp
import com.fomingram.data.remote.model.RandomUser
import com.fomingram.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Fetching : ProfileUiState()
    data class Success(val user: RandomUser, val apiStatus: String = "API: OK") : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

data class ProfileEdits(
    val displayName: String = "",
    val status: String = "В сети",
    val avatarUrl: String = ""
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Fetching)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _edits = MutableStateFlow(ProfileEdits())
    val edits: StateFlow<ProfileEdits> = _edits.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ProfileUiState.Fetching
            try {
                val result = userRepository.getRandomUser()
                result.fold(
                    onSuccess = { user ->
                        // Заполняем дефолтные правки из API при первой загрузке
                        if (_edits.value.displayName.isEmpty()) {
                            _edits.value = _edits.value.copy(
                                displayName = "${user.name.first} ${user.name.last}",
                                avatarUrl = user.picture.large
                            )
                        }
                        _uiState.value = ProfileUiState.Success(user)
                    },
                    onFailure = { e ->
                        _uiState.value = ProfileUiState.Error(
                            e.message ?: "Не удалось загрузить профиль"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Не удалось загрузить профиль")
            }
        }
    }

    fun saveEdits(name: String, status: String, avatarUrl: String) {
        _edits.value = ProfileEdits(
            displayName = name.ifBlank { _edits.value.displayName },
            status = status.ifBlank { "В сети" },
            avatarUrl = avatarUrl.ifBlank { _edits.value.avatarUrl }
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(FomingramApp.instance.userRepository) as T
            }
        }
    }
}
