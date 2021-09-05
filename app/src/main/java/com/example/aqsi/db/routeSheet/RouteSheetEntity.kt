package com.example.aqsi.db.routeSheet

import androidx.room.*
import com.example.aqsi.db.orders.OrdersEntity
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


@Entity(tableName = "RouteSheet")
class RouteSheetEntity(
    @PrimaryKey
    var id: String = "",
    @ColumnInfo
    var number: String = "",
    @ColumnInfo
    var date: String = "",
    @ColumnInfo
    var status: String = "",
    @TypeConverters(OrderConverter::class)
    @ColumnInfo
    var ordersList: ArrayList<OrdersEntity> = arrayListOf()
) : Serializable

class OrderConverter {
    private val gson = Gson()
    @TypeConverter
    fun stringToList(data: String?): ArrayList<OrdersEntity> {
        if (data == null) {
            return arrayListOf()
        }

        val listType = object : TypeToken<ArrayList<OrdersEntity>>() {

        }.type

        return gson.fromJson<ArrayList<OrdersEntity>>(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: ArrayList<OrdersEntity>): String {
        return gson.toJson(someObjects)
    }
}

