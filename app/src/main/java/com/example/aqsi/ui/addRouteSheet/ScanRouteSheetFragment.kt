package com.example.aqsi.ui.addRouteSheet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.aqsi.R
import com.example.aqsi.State
import com.example.aqsi.utils.FTPUtils
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_scan_route_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import steelkiwi.com.library.DotsLoaderView


class ScanRouteSheetFragment : Fragment() {

    private lateinit var scanRouteSheetViewModel: ScanRouteSheetViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        scanRouteSheetViewModel =
            ViewModelProvider(this).get(ScanRouteSheetViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_scan_route_sheet, container, false)
        val progress: DotsLoaderView = root.findViewById(R.id.dotsLoaderView)
        scanRouteSheetViewModel.state.observe(viewLifecycleOwner, Observer {state ->
            when (state) {
                is State.Loading -> {
                    progress.show()
                    CoroutineScope(Dispatchers.IO).launch {
                        FTPUtils.findRouteSheet(root.context, scanField.text.toString())
                        scanRouteSheetViewModel.state.postValue(State.Ready())
                    }
                }
                is State.Ready -> {
                    progress.hide()
                }
            }
        })
        val scanBtn: ImageButton = root.findViewById(R.id.scanBtn)
        scanBtn.setOnClickListener {
            scanRouteSheetViewModel.scanCode(this)
        }

        val scanField: EditText = root.findViewById(R.id.scanField)

        val layout:ConstraintLayout = root.findViewById(R.id.mainLayout)
        layout.setOnTouchListener { v, event ->
            hideSoftKeyboard(activity)
            false
        }


        val loadBtn: Button = root.findViewById(R.id.loadRouteSheet)
        loadBtn.setOnClickListener {
            scanRouteSheetViewModel.state.postValue(State.Loading())
        }

        return root

    }
    fun hideSoftKeyboard(activity: FragmentActivity?) {
        val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        context?.let {
            if (result != null && result.contents != null) {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(result.contents)
                builder.setTitle("TITLE todo")
                builder.setPositiveButton("scan again todo", { dialog, which -> scanRouteSheetViewModel.scanCode(this) })
                builder.setNegativeButton("finish todo", {dialog, which ->
                    dialog.dismiss()
                    scanField.setText(result.contents)})
                val dialog = builder.create()
                dialog.show()
            } else {
                Toast.makeText(it, "Toast todo", Toast.LENGTH_LONG).show()
                super.onActivityResult(requestCode, resultCode, data)
            }
        }



    }
}