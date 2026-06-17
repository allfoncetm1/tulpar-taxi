package kz.tulpartaxi.kandyagash.utils

import kz.tulpartaxi.kandyagash.BuildConfig

object StaticConfig {
    const val CITY_ID = BuildConfig.DEFAULT_CITY_ID
    const val API_BASE_URL = BuildConfig.API_BASE_URL
    const val MAP_BASE_URL = BuildConfig.MAP_BASE_URL
    const val GEOCODER_BASE_URL = "https://geocoder.tulpartaxi.kz"
    const val ROUTING_BASE_URL = "https://routing.tulpartaxi.kz"

    const val CITY_NAME_RU = "Кандыагаш"
    const val CITY_NAME_KZ = "Қандыағаш"
    const val CITY_NAME_EN = "Kandyagash"
    const val CITY_LAT = 49.465040
    const val CITY_LNG = 57.410118
    const val CITY_VIEWBOX = "49.42,57.35,49.51,57.47"

    const val APP_VERSION = BuildConfig.VERSION_NAME
    const val SUPPORT_WHATSAPP = "+77000000000"

    fun mapBaseUrl(): String = MAP_BASE_URL

    fun cityMapUrl(userId: String = ""): String =
        "$MAP_BASE_URL/citymap?userId=$userId&cityId=$CITY_ID"

    fun orderMapUrl(orderId: String): String =
        "$MAP_BASE_URL/order?orderId=$orderId"
}
