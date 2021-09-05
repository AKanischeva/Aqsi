package com.example.aqsi.db.orders

import androidx.room.*
import com.example.aqsi.db.routeSheet.OrderConverter
import java.io.Serializable
import java.math.BigDecimal


@Entity(tableName = "Orders")
class OrdersEntity(
    @PrimaryKey
    var id: String = "",
    @ColumnInfo
    var routeSheetId: String = "",
    @ColumnInfo
    var number: String = "",
    @ColumnInfo
    var date: String = "",
    @ColumnInfo
    var status: String = "",
    @ColumnInfo
    var sum: Double? = null,
    @ColumnInfo
    var recipient: String = "",
    @ColumnInfo
    var address: String = ""
) : Serializable

