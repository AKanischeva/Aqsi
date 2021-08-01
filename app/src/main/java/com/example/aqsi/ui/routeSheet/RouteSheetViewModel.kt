package com.example.aqsi.ui.routeSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aqsi.State

class RouteSheetViewModel : ViewModel() {

    val _loading = MutableLiveData<Boolean>().apply {
        value = false
    }
    var loading: LiveData<Boolean> = _loading
    val state = MutableLiveData<State>()
}