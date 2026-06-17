package kz.tulpartaxi.kandyagash.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.tulpartaxi.kandyagash.data.repository.AuthRepository
import javax.inject.Inject

sealed interface AuthState {
    data object PhoneInput : AuthState
    data object CodeInput : AuthState
    data object Loading : AuthState
    data object Success : AuthState
    data class Error(val message: String) : AuthState
}

data class AuthUiState(
    val phone: String = "",          // полный номер с кодом, отправляется на сервер
    val phoneDigits: String = "",    // только цифры без кода страны (для поля ввода)
    val phoneDialCode: String = "+7",// код страны
    val code: String = "",
    val name: String = "",
    val step: AuthStep = AuthStep.PHONE,
    val isLoading: Boolean = false,
    val error: String? = null,
)

enum class AuthStep { PHONE, CODE }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain.asStateFlow()

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, error = null) }
    }

    fun onPhoneDigitsChange(digits: String, dialCode: String) {
        val full = "${dialCode}${digits}"
        _uiState.update { it.copy(phoneDigits = digits, phoneDialCode = dialCode, phone = full, error = null) }
    }

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value, error = null) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun sendSms() {
        val phone = _uiState.value.phone.trim()
        if (phone.length < 10) {
            _uiState.update { it.copy(error = "Введите корректный номер телефона") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendSms(phone)
                .onSuccess { _uiState.update { it.copy(isLoading = false, step = AuthStep.CODE) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Ошибка сети") } }
        }
    }

    fun verifyCode() {
        val state = _uiState.value
        if (state.code.length < 4) {
            _uiState.update { it.copy(error = "Введите 4-значный код") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.verifyCode(state.phone, state.code, state.name.takeIf { it.isNotBlank() })
                .onSuccess { _navigateToMain.value = true }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Неверный код") } }
        }
    }

    fun backToPhone() {
        _uiState.update { it.copy(step = AuthStep.PHONE, code = "", error = null) }
    }
}
