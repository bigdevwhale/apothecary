package app.apothecary.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM items ORDER BY added")
    fun items(): Flow<List<Item>>

    @Query("SELECT * FROM items ORDER BY added")
    suspend fun itemList(): List<Item>

    @Query("SELECT * FROM items WHERE fate IS NULL")
    suspend fun stock(): List<Item>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun item(id: Long): Item?

    @Insert suspend fun insert(i: Item): Long
    @Insert suspend fun insertItems(i: List<Item>)
    @Update suspend fun update(i: Item)
    @Update suspend fun updateItems(i: List<Item>)
    @Delete suspend fun delete(i: Item)

    @Query("SELECT * FROM brews ORDER BY at")
    fun brews(): Flow<List<Brew>>

    @Query("SELECT * FROM brews ORDER BY at")
    suspend fun brewList(): List<Brew>

    @Insert suspend fun insert(b: Brew): Long
    @Insert suspend fun insertBrews(b: List<Brew>)
    @Delete suspend fun delete(b: Brew)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun itemCount(): Int

    @Query("SELECT MAX(jar) FROM items")
    suspend fun lastJar(): Int?
}

@Database(entities = [Item::class, Brew::class], version = 1, exportSchema = true)
abstract class ShopDb : RoomDatabase() {
    abstract fun dao(): ShopDao

    companion object {
        fun create(context: Context): ShopDb = Room.databaseBuilder(context, ShopDb::class.java, "apothecary.db").build()
    }
}
