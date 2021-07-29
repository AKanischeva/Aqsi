package com.example.aqsi.ui.routeSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.aqsi.Main2Activity
import com.example.aqsi.R
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.RouteSheetEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RouteSheetFragment : Fragment() {

    private lateinit var routeSheetViewModel: RouteSheetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        routeSheetViewModel =
                ViewModelProvider(this).get(RouteSheetViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_route_sheet, container, false)
        val main = inflater.inflate(R.layout.activity_main2, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        routeSheetViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val toolbar: Toolbar = main.findViewById(R.id.toolbar)
//        (activity as Main2Activity?)?.getSupportActionBar()?.hide()
//
//        (activity as Main2Activity?)?.setSupportActionBar(toolbar)
//        val db = Room.databaseBuilder(
//            textView.context,
//            AppDatabase::class.java, "todo-list.db"
//        ).build()
        val db = AppDatabase(textView.context)

        GlobalScope.launch {
            db.routeSheetDao().insert(RouteSheetEntity("id", "123", "20.07.2021", 1))
            val data = db.routeSheetDao().getAll()

            data?.forEach {
                println(it)
            }
        }

        return root
    }
}