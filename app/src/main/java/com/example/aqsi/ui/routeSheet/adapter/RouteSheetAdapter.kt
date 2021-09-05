package com.example.aqsi.ui.routeSheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aqsi.R
import com.example.aqsi.Status
import com.example.aqsi.db.routeSheet.RouteSheetEntity
import com.example.aqsi.utils.FTPUtils
import kotlinx.android.synthetic.main.item_route_sheet.view.*


class RouteSheetAdapter(val onItemClick: (routeSheet: RouteSheetEntity) -> Unit) : RecyclerView.Adapter<RouteSheetAdapter.RouteSheetViewHolder>() {

    private val items: ArrayList<RouteSheetEntity> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteSheetViewHolder {
        return RouteSheetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_route_sheet, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RouteSheetViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[holder.adapterPosition]
        holder.itemView.dateValue.text = FTPUtils.formatDate(item.date)
        holder.itemView.status.text = item.status
        holder.itemView.routeSheetNumber.text = "Маршрутный лист №${item.number}"

        when (item.status) {
            Status.IN_WORK.title -> {
                holder.itemView.status.background = ContextCompat.getDrawable(context, R.drawable.bg_oval_button_green)
            }
            Status.DONE.title -> {
                holder.itemView.status.background = ContextCompat.getDrawable(context, R.drawable.bg_oval_button_violet)
            }
        }
        holder.itemView.setOnClickListener{
            onItemClick(item)
        }
    }

    fun setItems(value: List<RouteSheetEntity>) {
        items.clear()
        items.addAll(value)
        notifyDataSetChanged()
    }

    class RouteSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}