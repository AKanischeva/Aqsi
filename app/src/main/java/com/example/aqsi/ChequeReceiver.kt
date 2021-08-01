package com.example.aqsi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.aqsi.db.AppDatabase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.aqsi.commons.models.Cheque
import ru.aqsi.commons.models.orders.OrderStatus
import ru.aqsi.commons.rmk.AqsiOrders
import xdroid.toaster.Toaster

class ChequeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            Toaster.toast("Обнаружено сообщение: ")
            val gson = Gson()
            val json = gson.fromJson(intent.getStringExtra(AqsiOrders.ARG_CHECK_BODY), Cheque::class.java)
            json.orderId?.let {
                val dbOrder = AppDatabase(context).ordersDao().getById(it)
                dbOrder?.let { order ->
                    val newOrder = order.apply {
                        this.status = OrderStatus.PAID.title
                    }
                    AppDatabase(context).ordersDao().update(newOrder)
                }
            }
        }
        val a = 5
    }
}