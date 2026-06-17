package kz.tulpartaxi.kandyagash.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.tulpartaxi.kandyagash.data.api.TulparApi
import kz.tulpartaxi.kandyagash.data.api.model.CreateOrderRequest
import kz.tulpartaxi.kandyagash.data.local.TokenStorage
import kz.tulpartaxi.kandyagash.utils.StaticConfig
import javax.inject.Inject

data class LatLng(val lat: Double, val lng: Double)

data class OrderFormState(
    val fromAddress: String = "",
    val fromLatLng: LatLng? = null,
    val toAddress: String = "",
    val toLatLng: LatLng? = null,
    val offeredPrice: String = "",
    val door: String = "",
    val comment: String = "",
    val isOrdering: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val orderId: String? = null,
    val orderSuccess: Boolean = false,
    val showRating: Boolean = false,
    val ratingOrderId: String? = null,
    // Режим выбора точки на карте: "from", "to", null = не выбираем
    val mapMode: String? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
)

@HiltViewModel
class StartViewModel @Inject constructor(
    private val api: TulparApi,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _formState = MutableStateFlow(
        OrderFormState(userName = tokenStorage.userName)
    )
    val formState: StateFlow<OrderFormState> = _formState.asStateFlow()

    // Вызывается из JS Bridge когда пользователь выбрал точку на карте
    fun onPointSelected(mode: String, lat: Double, lng: Double, address: String) {
        _formState.update { state ->
            if (mode == "from") {
                state.copy(fromAddress = address, fromLatLng = LatLng(lat, lng), mapMode = null)
            } else {
                state.copy(toAddress = address, toLatLng = LatLng(lat, lng), mapMode = null)
            }
        }
    }

    fun openMapForFrom(userLat: Double? = null, userLng: Double? = null) =
        _formState.update { it.copy(mapMode = "from", userLat = userLat, userLng = userLng) }
    fun openMapForTo(userLat: Double? = null, userLng: Double? = null) =
        _formState.update { it.copy(mapMode = "to", userLat = userLat, userLng = userLng) }
    fun closeMap() = _formState.update { it.copy(mapMode = null) }

    fun updateFrom(address: String) = _formState.update { it.copy(fromAddress = address) }
    fun updateTo(address: String) = _formState.update { it.copy(toAddress = address) }
    fun updatePrice(price: String) {
        val digits = price.filter { it.isDigit() }.take(6)
        _formState.update { it.copy(offeredPrice = digits) }
    }
    fun updateDoor(door: String) = _formState.update { it.copy(door = door.take(10)) }
    fun updateComment(comment: String) = _formState.update { it.copy(comment = comment.take(150)) }

    fun createOrder() {
        val state = _formState.value
        if (state.fromAddress.isBlank() || state.toAddress.isBlank() || state.offeredPrice.isBlank()) return
        val from = state.fromLatLng ?: LatLng(StaticConfig.CITY_LAT, StaticConfig.CITY_LNG)
        val to = state.toLatLng ?: LatLng(StaticConfig.CITY_LAT + 0.01, StaticConfig.CITY_LNG + 0.01)

        viewModelScope.launch {
            _formState.update { it.copy(isOrdering = true, error = null) }
            runCatching {
                api.createOrder(
                    CreateOrderRequest(
                        fromLat = from.lat, fromLng = from.lng, fromAddress = state.fromAddress,
                        toLat = to.lat, toLng = to.lng, toAddress = state.toAddress,
                        cityId = StaticConfig.CITY_ID,
                        door = state.door.ifBlank { null },
                        comment = state.comment.ifBlank { null },
                    )
                )
            }.onSuccess { result ->
                _formState.update { it.copy(isOrdering = false, orderId = result.orderId, orderSuccess = true) }
            }.onFailure { e ->
                _formState.update { it.copy(isOrdering = false, error = e.message ?: "Ошибка при создании заказа") }
            }
        }
    }

    fun dismissOrder() {
        val orderId = _formState.value.orderId
        _formState.update {
            it.copy(
                orderSuccess = false, orderId = null,
                fromAddress = "", toAddress = "", offeredPrice = "", door = "", comment = "",
                fromLatLng = null, toLatLng = null,
                showRating = orderId != null, ratingOrderId = orderId,
            )
        }
    }

    fun submitRating(orderId: String, rating: Int) {
        viewModelScope.launch {
            runCatching { api.rateOrder(mapOf("orderId" to orderId, "rating" to rating)) }
            _formState.update { it.copy(showRating = false, ratingOrderId = null) }
        }
    }

    fun skipRating() = _formState.update { it.copy(showRating = false, ratingOrderId = null) }
}
