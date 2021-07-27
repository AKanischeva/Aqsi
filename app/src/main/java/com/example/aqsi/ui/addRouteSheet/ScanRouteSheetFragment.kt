package com.example.aqsi.ui.addRouteSheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.aqsi.R
import com.example.aqsi.ui.CaptureAct
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_scan_route_sheet.*

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
        val textView: TextView = root.findViewById(R.id.textTip)
//        scanRouteSheetViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        val scanBtn: ImageButton = root.findViewById(R.id.scanBtn)
        scanBtn.setOnClickListener {
            scanRouteSheetViewModel.scanCode(this)
        }

        return root

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