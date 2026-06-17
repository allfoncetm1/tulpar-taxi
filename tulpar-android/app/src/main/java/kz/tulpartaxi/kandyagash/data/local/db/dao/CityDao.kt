package kz.tulpartaxi.kandyagash.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kz.tulpartaxi.kandyagash.data.local.db.entity.CityEntity

@Dao
interface CityDao {
    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getCity(id: Int): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)
}
