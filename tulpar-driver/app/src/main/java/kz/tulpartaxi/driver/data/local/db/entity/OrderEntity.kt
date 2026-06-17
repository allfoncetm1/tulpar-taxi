package kz.tulpartaxi.driver.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "city_id") val cityId: Int,
    @ColumnInfo(name = "from_address") val fromAddress: String?,
    @ColumnInfo(name = "from_lat") val fromLat: String,
    @ColumnInfo(name = "from_lng") val fromLng: String,
    @ColumnInfo(name = "to_address") val toAddress: String?,
    @ColumnInfo(name = "to_lat") val toLat: String,
    @ColumnInfo(name = "to_lng") val toLng: String,
    val price: Double?,
    val status: String,
    val date: String,
)
