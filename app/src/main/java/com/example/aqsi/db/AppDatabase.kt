package com.example.aqsi.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aqsi.db.orders.OrdersDao
import com.example.aqsi.db.orders.OrdersEntity
import com.example.aqsi.db.routeSheet.OrderConverter
import com.example.aqsi.db.routeSheet.RouteSheetDao
import com.example.aqsi.db.routeSheet.RouteSheetEntity


@Database(entities = arrayOf(RouteSheetEntity::class, OrdersEntity::class), version = 1, exportSchema = false)
@TypeConverters(OrderConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeSheetDao(): RouteSheetDao
    abstract fun ordersDao(): OrdersDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppDatabase::class.java, "database.db")
            .build()
    }
}