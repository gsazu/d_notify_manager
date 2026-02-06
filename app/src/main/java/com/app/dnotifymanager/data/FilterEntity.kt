package com.app.dnotifymanager.data

import androidx.room.*

@Entity(tableName = "filters")
data class FilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
    val tune: String = ""
)

@Dao
interface FilterDao {
    @Query("SELECT * FROM filters")
    fun getAllFilters(): kotlinx.coroutines.flow.Flow<List<FilterEntity>>

    @Insert
    suspend fun insert(filter: FilterEntity)

//    @Update
//    suspend fun update(filter: FilterEntity)

    @Delete
    suspend fun delete(filter: FilterEntity)
}

@Database(entities = [FilterEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun filterDao(): FilterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "filter-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}