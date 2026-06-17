package kz.tulpartaxi.kandyagash.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendSmsRequest(@Json(name = "phone") val phone: String)

@JsonClass(generateAdapter = true)
data class SendSmsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
)

@JsonClass(generateAdapter = true)
data class RegisterV2Request(
    @Json(name = "phone") val phone: String,
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String?,
    @Json(name = "cityId") val cityId: Int = 1,
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "name") val name: String?,
    @Json(name = "phone") val phone: String,
)

@JsonClass(generateAdapter = true)
data class RegisterWithTokenRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "token") val token: String,
)
