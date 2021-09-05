package com.example.aqsi.ui.routeSheet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aqsi.State
import com.example.aqsi.db.routeSheet.RouteSheetEntity

class RouteSheetViewModel : ViewModel() {

    val state = MutableLiveData<State>()
    var routeSheetIdToUpdate = ""
    var orders = emptyList<RouteSheetEntity>()
    var data = MutableLiveData<List<RouteSheetEntity>>()

    fun searchOrderByWord(word: String) {
        data.postValue(orders.filter { it.number.contains(word) })
    }
}