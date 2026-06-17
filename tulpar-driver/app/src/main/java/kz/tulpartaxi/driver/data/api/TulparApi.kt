package kz.tulpartaxi.driver.data.api

import kz.tulpartaxi.driver.data.api.model.AuthResponse
import kz.tulpartaxi.driver.data.api.model.BalanceResponse
import kz.tulpartaxi.driver.data.api.model.OrderDto
import kz.tulpartaxi.driver.data.api.model.RegisterV2Request
import kz.tulpartaxi.driver.data.api.model.RegisterWithTokenRequest
import kz.tulpartaxi.driver.data.api.model.SendSmsRequest
import kz.tulpartaxi.driver.data.api.model.SendSmsResponse
import kz.tulpartaxi.driver.data.api.model.StartDataResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TulparApi {

    @POST("api/account/SendSms")
    suspend fun sendSms(@Body request: SendSmsRequest): SendSmsResponse

    @POST("api/account/RegisterV2")
    suspend fun registerV2(@Body request: RegisterV2Request): AuthResponse

    @POST("api/account/RegisterWithToken")
    suspend fun registerWithToken(@Body request: RegisterWithTokenRequest): AuthResponse

    @POST("api/client/GetStartData")
    suspend fun getStartData(): StartDataResponse

    // Driver-specific endpoints
    @POST("api/services/getNewCustoms")
    suspend fun getNewCustoms(): List<OrderDto>

    @POST("api/services/AcceptOrder")
    suspend fun acceptOrder(@Body body: Map<String, String>)

    @POST("api/services/SetAction")
    suspend fun setAction(@Body body: Map<String, String>)

    @POST("api/services/GetBalance")
    suspend fun getBalance(): BalanceResponse

    @POST("api/services/UpdateDeviceId")
    suspend fun updateDeviceId(@Body body: Map<String, String>)
}
