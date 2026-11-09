package com.ucompensar.technova.ui.seller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Order
import com.ucompensar.technova.data.OrderStatus
import com.ucompensar.technova.databinding.ItemOrderBinding
import com.ucompensar.technova.ui.common.toCop

class OrderAdapter(
    private val showActions: Boolean,
    private val onStatusChanged: (Order) -> Unit = {}
) : RecyclerView.Adapter<OrderAdapter.Holder>() {
    private var items: List<Order> = emptyList()

    fun submit(list: List<Order>) {
        items = list
        notifyDataSetChanged()
    }

    class Holder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val order = items[position]
        val ctx = holder.itemView.context
        with(holder.binding) {
            tvOrderNumber.text = ctx.getString(R.string.label_order_number, order.id)
            tvCustomer.text = order.customer
            tvDetail.text = ctx.getString(R.string.label_order_items, order.items, order.total.toCop())
            bindStatus(this, order.status)

            btnChangeStatus.visibility = if (showActions) View.VISIBLE else View.GONE
            btnChangeStatus.setOnClickListener { anchor ->
                val popup = PopupMenu(ctx, anchor)
                popup.menuInflater.inflate(R.menu.menu_order_status, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    order.status = when (item.itemId) {
                        R.id.status_shipped -> OrderStatus.SHIPPED
                        R.id.status_delivered -> OrderStatus.DELIVERED
                        else -> OrderStatus.PENDING
                    }
                    onStatusChanged(order)
                    true
                }
                popup.show()
            }
        }
    }

    private fun bindStatus(binding: ItemOrderBinding, status: OrderStatus) {
        val ctx = binding.root.context
        val (text, color, background) = when (status) {
            OrderStatus.PENDING -> Triple(R.string.status_pending, R.color.tn_status_pending, R.drawable.bg_badge_pending)
            OrderStatus.SHIPPED -> Triple(R.string.status_shipped, R.color.tn_indigo, R.drawable.bg_badge_info)
            OrderStatus.DELIVERED -> Triple(R.string.status_delivered, R.color.tn_status_ok, R.drawable.bg_badge_ok)
        }
        binding.tvStatus.setText(text)
        binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, color))
        binding.tvStatus.setBackgroundResource(background)
    }
}

fun OrderStatus.label(): Int = when (this) {
    OrderStatus.PENDING -> R.string.status_pending
    OrderStatus.SHIPPED -> R.string.status_shipped
    OrderStatus.DELIVERED -> R.string.status_delivered
}
