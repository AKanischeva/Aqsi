package com.example.aqsi.ui.addRouteSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aqsi.State
import com.example.aqsi.ui.CaptureAct
import com.google.zxing.integration.android.IntentIntegrator

class ScanRouteSheetViewModel : ViewModel() {

    val _loading = MutableLiveData<Boolean>().apply {
        value = false
    }
    var loading: LiveData<Boolean> = _loading
    val state = MutableLiveData<State>()

    fun scanCode(fragment: ScanRouteSheetFragment){
        val integrator = IntentIntegrator.forSupportFragment(fragment)
        integrator.setCaptureActivity(CaptureAct::class.java)
        integrator.setOrientationLocked(true)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scanning code todo")
        integrator.initiateScan()
    }

}