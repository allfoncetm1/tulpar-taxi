package kz.tulpartaxi.kandyagash.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import kz.tulpartaxi.kandyagash.data.local.db.dao.CityDao
import kz.tulpartaxi.kandyagash.data.local.db.dao.OrderDao
import kz.tulpartaxi.kandyagash.data.local.db.entity.CityEntity
import kz.tulpartaxi.kandyagash.data.local.db.entity.OrderEntity

@Database(
    entities = [CityEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TulparDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun orderDao(): OrderDao
}
