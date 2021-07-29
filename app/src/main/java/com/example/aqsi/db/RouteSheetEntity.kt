package com.example.aqsi.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "RouteSheet")
class RouteSheetEntity(
    @PrimaryKey
    var id: String = "",
    @ColumnInfo
    var number: String = "",
    @ColumnInfo
    var date: String = "",
    @ColumnInfo
    var status: Int = 0
)

