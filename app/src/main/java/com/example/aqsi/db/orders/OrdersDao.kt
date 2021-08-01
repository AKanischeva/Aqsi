package com.example.aqsi.db.orders

import androidx.room.*
import com.example.aqsi.db.routeSheet.RouteSheetEntity


@Dao
interface OrdersDao {
    @Query("SELECT * FROM orders")
    fun getAll(): List<OrdersEntity>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getById(id: String): OrdersEntity?

    @Query("DELETE FROM orders")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(order: OrdersEntity?)

    @Update
    fun update(order: OrdersEntity?)

    @Delete
    fun delete(order: OrdersEntity?)
}