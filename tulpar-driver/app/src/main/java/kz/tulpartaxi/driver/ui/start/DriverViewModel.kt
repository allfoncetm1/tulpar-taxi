package kz.tulpartaxi.driver.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.tulpartaxi.driver.data.api.TulparApi
import kz.tulpartaxi.driver.data.api.model.OrderDto
import kz.tulpartaxi.driver.data.local.TokenStorage
import javax.inject.Inject

data class DriverState(
    val isOnline: Boolean = false,
    val pendingOrders: List<OrderDto> = emptyList(),
    val activeOrder: OrderDto? = null,
    val balance: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
)

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val api: TulparApi,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(
        DriverState(userName = tokenStorage.userName)
    )
    val state: StateFlow<DriverState> = _state.asStateFlow()

    init {
        startPolling()
        loadBalance()
    }

    fun toggleOnline() {
        _state.update { it.copy(isOnline = !it.isOnline) }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                if (_state.value.isOnline && _state.value.activeOrder == null) {
                    runCatching { api.getNewCustoms() }
                        .onSuccess { orders -> _state.update { it.copy(pendingOrders = orders) } }
                }
                delay(5_000)
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            runCatching { api.getBalance() }
                .onSuccess { resp -> _state.update { it.copy(balance = resp.balance) } }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { api.acceptOrder(mapOf("orderId" to orderId)) }
                .onSuccess {
                    val order = _state.value.pendingOrders.find { it.id == orderId }?.copy(status = "accepted")
                    _state.update { it.copy(activeOrder = order, pendingOrders = emptyList(), isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка") }
                }
        }
    }

    fun setAction(action: String) {
        val orderId = _state.value.activeOrder?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { api.setAction(mapOf("orderId" to orderId, "action" to action)) }
                .onSuccess {
                    if (action == "complete" || action == "cancel") {
                        _state.update { it.copy(activeOrder = null, isLoading = false) }
                        loadBalance()
                    } else {
                        // arrived / start — refresh active order status (для простоты обновляем локально)
                        val newStatus = when (action) {
                            "arrived" -> "arrived"
                            "start" -> "in_progress"
                            else -> _state.value.activeOrder?.status ?: ""
                        }
                        _state.update {
                            it.copy(
                                activeOrder = it.activeOrder?.copy(status = newStatus),
                                isLoading = false,
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
