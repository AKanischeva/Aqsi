package com.example.aqsi.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aqsi.State
import com.example.aqsi.db.orders.OrdersEntity

class OrdersViewModel : ViewModel() {

    val _loading = MutableLiveData<Boolean>().apply {
        value = false
    }
    var loading: LiveData<Boolean> = _loading
    val state = MutableLiveData<State>()
    var orders: List<OrdersEntity> = listOf()
    var reloadNeeded = false
    var routeSheetId = ""
}