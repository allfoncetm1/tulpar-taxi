package kz.tulpartaxi.driver.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityDto(
    @Json(name = "id") val id: Int,
    @Json(name = "nameKz") val nameKz: String,
    @Json(name = "nameRu") val nameRu: String,
    @Json(name = "nameEn") val nameEn: String,
    @Json(name = "lat") val lat: String,
    @Json(name = "lng") val lng: String,
    @Json(name = "viewbox") val viewbox: String?,
)

@JsonClass(generateAdapter = true)
data class TariffDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "basePrice") val basePrice: String,
    @Json(name = "pricePerKm") val pricePerKm: String,
    @Json(name = "minPrice") val minPrice: String,
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "name") val name: String?,
    @Json(name = "isDriver") val isDriver: Boolean,
    @Json(name = "cityId") val cityId: Int,
)

@JsonClass(generateAdapter = true)
data class StartDataResponse(
    @Json(name = "user") val user: UserDto,
    @Json(name = "city") val city: CityDto,
    @Json(name = "tariffs") val tariffs: List<TariffDto>,
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    @Json(name = "id") val id: String,
    @Json(name = "clientId") val clientId: String,
    @Json(name = "driverId") val driverId: String?,
    @Json(name = "fromLat") val fromLat: Double,
    @Json(name = "fromLng") val fromLng: Double,
    @Json(name = "fromAddress") val fromAddress: String?,
    @Json(name = "toLat") val toLat: Double,
    @Json(name = "toLng") val toLng: Double,
    @Json(name = "toAddress") val toAddress: String?,
    @Json(name = "estimatedPrice") val estimatedPrice: Double?,
    @Json(name = "status") val status: String,
    @Json(name = "comment") val comment: String?,
    @Json(name = "door") val door: String?,
)

@JsonClass(generateAdapter = true)
data class BalanceResponse(
    @Json(name = "balance") val balance: Int,
    @Json(name = "currency") val currency: String,
)
