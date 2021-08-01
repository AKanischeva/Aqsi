package com.example.aqsi.ui.orders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.*
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.orders.OrdersEntity
import com.example.aqsi.ui.orders.adapter.OrdersAdapter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aqsi.commons.models.Cheque
import ru.aqsi.commons.models.orders.OrderStatus
import ru.aqsi.commons.rmk.AqsiOrders
import steelkiwi.com.library.DotsLoaderView
import xdroid.toaster.Toaster
import java.util.*


class OrdersActivity : AppCompatActivity(), BroadcastListener{

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: OrdersViewModel
    private lateinit var adapter: OrdersAdapter
    private var receiver: BroadcastReceiver? = null


    suspend fun getOrdersFromDb(id: String): List<OrdersEntity> = withContext(Dispatchers.IO) {
        AppDatabase(this@OrdersActivity).ordersDao().getByRouteSheetId(id) ?: emptyList()
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
        receiver = ChequeReceiver(this)
        val filter = IntentFilter(AqsiOrders.ACTION_RECEIVE_ORDER_CHECK)
        registerReceiver(receiver, filter)

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

    override fun doSomething(intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            Toaster.toast("Обнаружено сообщение: ")
            val gson = Gson()
            val json = gson.fromJson(intent.getStringExtra(AqsiOrders.ARG_CHECK_BODY), Cheque::class.java)
            json.orderId?.let {
                val dbOrder = AppDatabase(this@OrdersActivity).ordersDao().getById(it)
                dbOrder?.let { order ->
                    val newOrder = order.apply {
                        this.status = OrderStatus.PAID.title
                    }
                    AppDatabase(this@OrdersActivity).ordersDao().update(newOrder)
                    viewModel.orders = getOrdersFromDb(this@OrdersActivity.intent.getStringExtra(ROUTE_SHEET_ID) ?: "")
                    AppDatabase(this@OrdersActivity).routeSheetDao().updateStatus(this@OrdersActivity.intent.getStringExtra(ROUTE_SHEET_ID) ?: "",
                    newOrder.id, newOrder.status)
                    //todo update routeSheetDao order
                    viewModel.state.postValue(State.Ready())
                }
            }
        }
    }
}