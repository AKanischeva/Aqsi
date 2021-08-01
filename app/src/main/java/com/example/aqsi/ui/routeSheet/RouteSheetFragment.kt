package com.example.aqsi.ui.routeSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.R
import com.example.aqsi.State
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.routeSheet.RouteSheetEntity
import com.example.aqsi.ui.orders.OrdersActivity
import com.example.aqsi.ui.orders.adapter.OrdersAdapter
import com.example.aqsi.ui.routeSheet.adapter.RouteSheetAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import steelkiwi.com.library.DotsLoaderView
import xdroid.toaster.Toaster


class RouteSheetFragment : Fragment() {

    private lateinit var routeSheetViewModel: RouteSheetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        routeSheetViewModel =
                ViewModelProvider(this).get(RouteSheetViewModel::class.java)
//        routeSheetViewModel._loading.postValue(true)
        val root = inflater.inflate(R.layout.fragment_route_sheet, container, false)
        val main = inflater.inflate(R.layout.activity_main2, container, false)
        val progress: DotsLoaderView = root.findViewById(R.id.dotsLoaderView)

//        routeSheetViewModel.loading.observe(viewLifecycleOwner, { loading ->
//            if (loading) {
//                progress.show()
//            } else {
//                progress.hide()
//            }
//        })
//        routeSheetViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

//        val toolbar: Toolbar = main.findViewById(R.id.toolbar)
        var data = emptyList<RouteSheetEntity>()
        routeSheetViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is State.Loading -> {
                    progress.show()
                }
                is State.Ready -> {
                    val routeSheet: RecyclerView = root.findViewById(R.id.routeSheet)
                    routeSheet.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    routeSheet.adapter = RouteSheetAdapter { routeSheet ->
                        startActivity(OrdersActivity.getIntent(routeSheet.id, requireContext()))
                    }
                    routeSheet.adapter?.let { adapter ->
                        if (adapter is RouteSheetAdapter) {
                            adapter.setItems(data)
                        }
                    }
                    routeSheetViewModel._loading.postValue(false)
                    progress.hide()
                }
            }
        })
        routeSheetViewModel.state.postValue(State.Loading())


        GlobalScope.launch {
            data = AppDatabase(requireContext()).routeSheetDao().getAll()
            routeSheetViewModel.state.postValue(State.Ready())
        }
//
//        if(data.isEmpty()){
//            routeSheetViewModel._loading.postValue(true)
//            GlobalScope.launch {
//
//
//                data.forEach {
//                    println(it)
//                }
//                routeSheetViewModel._loading.postValue(true)
//            }
//        }


        return root
    }
}