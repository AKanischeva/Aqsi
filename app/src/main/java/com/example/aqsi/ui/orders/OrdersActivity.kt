package com.example.aqsi.ui.orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.Main2ActivityViewModel
import com.example.aqsi.R
import com.example.aqsi.State
import com.example.aqsi.Status
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.orders.OrdersEntity
import com.example.aqsi.db.routeSheet.RouteSheetEntity
import com.example.aqsi.ui.orders.adapter.OrdersAdapter
import com.example.aqsi.ui.routeSheet.adapter.RouteSheetAdapter
import com.example.aqsi.utils.FTPUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aqsi.commons.rmk.AqsiOrders
import steelkiwi.com.library.DotsLoaderView
import xdroid.toaster.Toaster
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


class OrdersActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: OrdersViewModel
    private lateinit var adapter: OrdersAdapter


    suspend fun getOrdersFromDb(id: String): List<OrdersEntity> = withContext(Dispatchers.IO) {
        val sheet = AppDatabase(this@OrdersActivity).routeSheetDao().getById(id)
        sheet?.ordersList ?: arrayListOf()
    }

//    suspend fun observeDb(id: String) = withContext(Dispatchers.IO) {
//        // first paramter in the observe method is your LifeCycle Owner
//        AppDatabase(this@OrdersActivity).routeSheetDao().getByIdLiveDataOrders(id)?.observe(this@OrdersActivity, object : Observer<List<OrdersEntity>> {
//            override fun onChanged(orders: List<OrdersEntity>?) {
//                orders?.let {
//                    adapter.setItems(orders)
//                }
//
////                if(viewModel.reloadNeeded) {
////                    recreate()
////                    viewModel.reloadNeeded = false
////                }
//            }
//        })
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this).get(OrdersViewModel::class.java)
        setContentView(R.layout.activity_orders)
        val routeSheetId = intent?.getSerializableExtra(ROUTE_SHEET_ID) as? String ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.state.postValue(State.Loading())
            viewModel.orders = getOrdersFromDb(routeSheetId)
            viewModel.state.postValue(State.Ready())
//            observeDb(routeSheetId)
        }


//        if(viewModel.orders.isEmpty()) {
//
//        } else {
//
//        }
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)


//        val navView: NavigationView = findViewById(R.id.nav_view)
//        val actionSettings: Button = findViewById(R.id.action_settings)
//        actionSettings.setOnClickListener { Toast.makeText(this, "Toast todo", Toast.LENGTH_LONG).show() }
//        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
        val progress: DotsLoaderView = findViewById(R.id.dotsLoaderView)
        viewModel.state.observe(this, { state ->
            when (state) {
                is State.Loading -> {
                    progress.show()
                }
                is State.Ready -> {
                    val orders: RecyclerView = findViewById(R.id.orders)
                    orders.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    adapter = OrdersAdapter { order ->
                        AqsiOrders.startOrderPayment(this, order.id)
                        viewModel.reloadNeeded = true
//                        Toaster.toast(order.number)
                    }
                    orders.adapter = adapter
                    orders.adapter?.let { adapter ->
                        if (adapter is OrdersAdapter) {
                            adapter.setItems(viewModel.orders)
                        }
                    }
                    progress.hide()
                }
            }
        })
        viewModel.state.postValue(State.Ready())

    }

    companion object {
        private const val ROUTE_SHEET_ID = "ROUTE_SHEET_ID"

        fun getIntent(routeSheetId: String, context: Context): Intent {
            return Intent(context, OrdersActivity::class.java).apply {
                putExtra(ROUTE_SHEET_ID, routeSheetId)
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        if(viewModel.reloadNeeded) {
//            recreate()
//        }
//        viewModel.reloadNeeded = false
//    }
}