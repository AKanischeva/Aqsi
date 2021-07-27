package com.example.aqsi.ui.addRouteSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aqsi.ui.CaptureAct
import com.google.zxing.integration.android.IntentIntegrator

class ScanRouteSheetViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
//    }
//    val text: LiveData<String> = _text

    fun scanCode(fragment: ScanRouteSheetFragment){
        val integrator = IntentIntegrator.forSupportFragment(fragment)
        integrator.setCaptureActivity(CaptureAct::class.java)
        integrator.setOrientationLocked(true)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scanning code todo")
        integrator.initiateScan()
    }

}