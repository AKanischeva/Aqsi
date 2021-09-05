package com.example.aqsi.ui.routeSheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.R
import com.example.aqsi.State
import com.example.aqsi.Status
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.ui.orders.OrdersActivity
import com.example.aqsi.ui.routeSheet.adapter.RouteSheetAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import steelkiwi.com.library.DotsLoaderView


class RouteSheetFragment : Fragment() {

    private lateinit var routeSheetViewModel: RouteSheetViewModel
    private lateinit var adapter: RouteSheetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        routeSheetViewModel =
            ViewModelProvider(this).get(RouteSheetViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_route_sheet, container, false)
        val progress: DotsLoaderView = root.findViewById(R.id.dotsLoaderView)
        val progressLayout: RelativeLayout = root.findViewById(R.id.progress_layout)
        val editTextSearch: EditText = root.findViewById(R.id.editTextSearch)
        adapter = RouteSheetAdapter { routeSheet ->
            startActivityForResult(OrdersActivity.getIntent(routeSheet.id, requireContext()), REQUEST_CODE_ORDERS)
        }
        editTextSearch.doAfterTextChanged { text ->
            routeSheetViewModel.searchOrderByWord(text.toString())
        }
        routeSheetViewModel.data.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        routeSheetViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is State.Loading -> {
                    progressLayout.visibility = View.VISIBLE
                    progress.show()
                    GlobalScope.launch {
                        if (routeSheetViewModel.routeSheetIdToUpdate.isNotBlank()) {
                            val oldRouteSheet = AppDatabase(root.context).routeSheetDao().getById(routeSheetViewModel.routeSheetIdToUpdate)
                            val newRouteSheet = oldRouteSheet?.apply {
                                this.status = Status.DONE.title
                            }
                            AppDatabase(root.context).routeSheetDao().update(newRouteSheet)
                            routeSheetViewModel.routeSheetIdToUpdate = ""
                        }
                        routeSheetViewModel.orders = AppDatabase(requireContext()).routeSheetDao().getAll()
                        routeSheetViewModel.data.postValue(routeSheetViewModel.orders)
                        routeSheetViewModel.state.postValue(State.Ready())
                    }
                }
                is State.Ready -> {
                    val routeSheet: RecyclerView = root.findViewById(R.id.routeSheet)
                    routeSheet.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    routeSheet.adapter = adapter
                    routeSheet.adapter?.let { adapter ->
                        if (adapter is RouteSheetAdapter) {
                            adapter.setItems(routeSheetViewModel.orders)
                        }
                    }
                    progressLayout.visibility = View.GONE
                    progress.hide()
                }
            }
        })





        routeSheetViewModel.state.postValue(State.Loading())
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == REQUEST_CODE_ORDERS && resultCode == OrdersActivity.RES_CODE_NEED_UPDATE -> {
                routeSheetViewModel.routeSheetIdToUpdate = data?.getStringExtra(OrdersActivity.ROUTE_SHEET_ID) ?: ""
                routeSheetViewModel.state.postValue(State.Loading())
            }
        }
    }


    companion object {
        const val REQUEST_CODE_ORDERS = 15
    }
}