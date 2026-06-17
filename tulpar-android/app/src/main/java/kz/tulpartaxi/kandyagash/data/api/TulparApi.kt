package kz.tulpartaxi.kandyagash.data.api

import kz.tulpartaxi.kandyagash.data.api.model.AuthResponse
import kz.tulpartaxi.kandyagash.data.api.model.CreateOrderRequest
import kz.tulpartaxi.kandyagash.data.api.model.CreateOrderResponse
import kz.tulpartaxi.kandyagash.data.api.model.EstimateRequest
import kz.tulpartaxi.kandyagash.data.api.model.EstimateResponse
import kz.tulpartaxi.kandyagash.data.api.model.RegisterV2Request
import kz.tulpartaxi.kandyagash.data.api.model.RegisterWithTokenRequest
import kz.tulpartaxi.kandyagash.data.api.model.SendSmsRequest
import kz.tulpartaxi.kandyagash.data.api.model.SendSmsResponse
import kz.tulpartaxi.kandyagash.data.api.model.StartDataResponse
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

    @POST("api/services/Estimate")
    suspend fun estimate(@Body request: EstimateRequest): EstimateResponse

    @POST("api/services/createNewCustom")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse

    @POST("api/services/UpdateDeviceId")
    suspend fun updateDeviceId(@Body body: Map<String, String>)

    @POST("api/services/RateOrder")
    suspend fun rateOrder(@Body body: Map<String, Any>)
}
