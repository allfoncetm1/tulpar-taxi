package kz.tulpartaxi.kandyagash.data.repository

import kz.tulpartaxi.kandyagash.data.api.TulparApi
import kz.tulpartaxi.kandyagash.data.api.model.RegisterV2Request
import kz.tulpartaxi.kandyagash.data.api.model.RegisterWithTokenRequest
import kz.tulpartaxi.kandyagash.data.api.model.SendSmsRequest
import kz.tulpartaxi.kandyagash.data.local.TokenStorage
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

// Извлекает читаемое сообщение из тела HTTP ошибки
private fun HttpException.errorMessage(): String {
    return try {
        val body = response()?.errorBody()?.string() ?: return "Ошибка сервера"
        val json = JSONObject(body)
        // NestJS возвращает { "message": "...", "error": "...", "statusCode": N }
        // или { "message": ["validation error 1", ...] }
        when (val msg = json.opt("message")) {
            is String -> msg
            is org.json.JSONArray -> (0 until msg.length()).map { msg.getString(it) }.joinToString(", ")
            else -> json.optString("error", "Ошибка сервера")
        }
    } catch (e: Exception) {
        "Ошибка ${code()}"
    }
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: TulparApi,
    private val tokenStorage: TokenStorage,
) {
    suspend fun sendSms(phone: String): Result<Unit> = runCatching {
        api.sendSms(SendSmsRequest(phone)); Unit
    }.mapError()

    suspend fun verifyCode(phone: String, code: String, name: String?): Result<Unit> = runCatching {
        val response = api.registerV2(RegisterV2Request(phone, code, name))
        tokenStorage.token = response.token
        tokenStorage.phone = response.phone
        tokenStorage.userId = response.userId
        tokenStorage.userName = response.name
    }.mapError()

    private fun <T> Result<T>.mapError(): Result<T> = onFailure { e ->
        if (e is HttpException) throw RuntimeException(e.errorMessage())
    }

    suspend fun tryAutoLogin(): Boolean {
        val token = tokenStorage.token ?: return false
        val phone = tokenStorage.phone ?: return false
        return runCatching {
            val response = api.registerWithToken(RegisterWithTokenRequest(phone, token))
            tokenStorage.token = response.token
            tokenStorage.userName = response.name
        }.isSuccess
    }

    fun isLoggedIn() = tokenStorage.isLoggedIn
    fun getToken() = tokenStorage.token
    fun getUserName() = tokenStorage.userName
    fun logout() = tokenStorage.clear()
}
