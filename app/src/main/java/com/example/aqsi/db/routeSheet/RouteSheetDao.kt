package com.example.aqsi.db.routeSheet

import androidx.room.*
import com.example.aqsi.db.orders.OrdersEntity


@Dao
interface RouteSheetDao {
    @Query("SELECT * FROM routesheet")
    fun getAll(): List<RouteSheetEntity>

    @Query("SELECT * FROM routesheet WHERE id = :id")
    fun getById(id: String): RouteSheetEntity?

    @Query("DELETE FROM routesheet")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routeSheet: RouteSheetEntity?)

    @Update
    fun update(routeSheet: RouteSheetEntity?)

    @Delete
    fun delete(routeSheet: RouteSheetEntity?)

    fun updateStatus(routeSheetId: String, orderId: String, status: String) {
        val routeSheet = getById(routeSheetId)
        val orders: ArrayList<OrdersEntity>? = routeSheet?.ordersList
        orders?.let {
            it.find { it.id == orderId }?.status = status
        }
        update(routeSheet)
    }
}