package com.example.aqsi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aqsi.State

class Main2ActivityViewModel : ViewModel() {

    val _loading = MutableLiveData<Boolean>().apply {
        value = false
    }
    var loading: LiveData<Boolean> = _loading
    val state = MutableLiveData<State>()
}