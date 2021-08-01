package com.example.aqsi.ui.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.R
import com.example.aqsi.Status
import com.example.aqsi.db.orders.OrdersEntity
import com.example.aqsi.utils.FTPUtils
import kotlinx.android.synthetic.main.item_order.view.*
import ru.aqsi.commons.models.orders.OrderStatus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OrdersAdapter(val onItemClick: (order: OrdersEntity) -> Unit) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    private val items: ArrayList<OrdersEntity> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[holder.adapterPosition]

        holder.itemView.dateValue.text = FTPUtils.formatDate(item.date)
        holder.itemView.status.text = item.status
        holder.itemView.orderNumber.text = item.number
        holder.itemView.status.text = item.status
        holder.itemView.addressValue.text = item.address
        holder.itemView.recipientValue.text = item.recipient
        holder.itemView.sum.text = item.sum?.toString()

        when (item.status) {
            OrderStatus.NOT_PAID.title -> {
                holder.itemView.status.background = ContextCompat.getDrawable(context, R.drawable.bg_oval_button_green)
            }
            OrderStatus.PAID.title -> {
                holder.itemView.status.background = ContextCompat.getDrawable(context, R.drawable.bg_oval_button_violet)
            }
        }
        holder.itemView.setOnClickListener{
            onItemClick(item)
        }
    }

    fun setItems(value: List<OrdersEntity>) {
        items.clear()
        items.addAll(value)
        notifyDataSetChanged()
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}