package kz.tulpartaxi.driver.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name_kz") val nameKz: String,
    @ColumnInfo(name = "name_ru") val nameRu: String,
    @ColumnInfo(name = "name_en") val nameEn: String,
    val lat: String,
    val lng: String,
    val viewbox: String?,
    val version: Int = 1,
)
