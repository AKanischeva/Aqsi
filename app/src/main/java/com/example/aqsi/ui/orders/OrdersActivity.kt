package com.example.aqsi.ui.orders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.*
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.orders.OrdersEntity
import com.example.aqsi.ui.orders.adapter.OrdersAdapter
import com.example.aqsi.ui.routeSheet.RouteSheetFragment
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aqsi.commons.models.Cheque
import ru.aqsi.commons.models.orders.OrderStatus
import ru.aqsi.commons.rmk.AqsiOrders
import steelkiwi.com.library.DotsLoaderView
import java.util.*


class OrdersActivity : AppCompatActivity(), BroadcastListener{

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: OrdersViewModel
    private lateinit var adapter: OrdersAdapter
    private var receiver: BroadcastReceiver? = null


    suspend fun getOrdersFromDb(id: String): List<OrdersEntity> = withContext(Dispatchers.IO) {
        AppDatabase(this@OrdersActivity).ordersDao().getByRouteSheetId(id) ?: emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = ChequeReceiver(this)
        val filter = IntentFilter(AqsiOrders.ACTION_RECEIVE_ORDER_CHECK)
        registerReceiver(receiver, filter)

        viewModel =
            ViewModelProvider(this).get(OrdersViewModel::class.java)
        setContentView(R.layout.activity_orders)
        viewModel.routeSheetId = intent?.getSerializableExtra(ROUTE_SHEET_ID) as? String ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.state.postValue(State.Loading())
            viewModel.orders = getOrdersFromDb(viewModel.routeSheetId)
            viewModel.state.postValue(State.Ready())
        }
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
        const val ROUTE_SHEET_ID = "ROUTE_SHEET_ID"
        const val RES_CODE_NEED_UPDATE = 3

        fun getIntent(routeSheetId: String, context: Context): Intent {
            return Intent(context, OrdersActivity::class.java).apply {
                putExtra(ROUTE_SHEET_ID, routeSheetId)
            }
        }
    }

    override fun processCheck(intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val gson = Gson()
            val json = gson.fromJson(intent.getStringExtra(AqsiOrders.ARG_CHECK_BODY), Cheque::class.java)
            json.cashierId
            json.orderId?.let {
                val dbOrder = AppDatabase(this@OrdersActivity).ordersDao().getById(it)
                dbOrder?.let { order ->
                    val newOrder = order.apply {
                        this.status = OrderStatus.PAID.title
                    }
                    AppDatabase(this@OrdersActivity).ordersDao().update(newOrder)
                    viewModel.orders = getOrdersFromDb(viewModel.routeSheetId)
                    AppDatabase(this@OrdersActivity).routeSheetDao().updateOrderStatus(this@OrdersActivity.intent.getStringExtra(ROUTE_SHEET_ID) ?: "",
                    newOrder.id, newOrder.status)
                    viewModel.state.postValue(State.Ready())
                }
            }
        }
    }

    override fun onBackPressed() {
        var isRouteSheetDone = true
        viewModel.orders.forEach {
            if(it.status == OrderStatus.NOT_PAID.title){
                isRouteSheetDone = false
            }
        }
        if(isRouteSheetDone) {
            setResult(RES_CODE_NEED_UPDATE, Intent().apply {
                putExtra(ROUTE_SHEET_ID, viewModel.routeSheetId)
            })
        }
        finishActivity(RouteSheetFragment.REQUEST_CODE_ORDERS)
        super.onBackPressed()
    }
}