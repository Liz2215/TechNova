package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.data.CartItem
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.ActivityCartBinding
import com.ucompensar.technova.databinding.ItemCartBinding
import com.ucompensar.technova.ui.common.toCop

/** Carrito de compras: cambiar cantidades, eliminar y ver totales (RF-03). */
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val adapter = CartAdapter { refreshTotals() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvCart.adapter = adapter
        binding.btnCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        refreshTotals()
    }

    private fun refreshTotals() {
        val empty = FakeRepository.cart.isEmpty()
        binding.layoutEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        binding.btnCheckout.isEnabled = !empty
        val subtotal = FakeRepository.cartSubtotal()
        val shipping = if (empty) 0L else FakeRepository.SHIPPING_COST
        binding.summary.tvSubtotal.text = subtotal.toCop()
        binding.summary.tvShipping.text = shipping.toCop()
        binding.summary.tvTotal.text = (subtotal + shipping).toCop()
    }

    private class CartAdapter(private val onChange: () -> Unit) :
        RecyclerView.Adapter<CartAdapter.Holder>() {

        class Holder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

        private val items: MutableList<CartItem> get() = FakeRepository.cart

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            with(holder.binding) {
                ivProduct.setImageResource(item.product.imageRes)
                tvName.text = item.product.name
                tvPrice.text = (item.product.price * item.quantity).toCop()
                tvQty.text = item.quantity.toString()
                btnPlus.setOnClickListener {
                    if (item.quantity < item.product.stock) item.quantity++ // validar stock
                    notifyItemChanged(holder.bindingAdapterPosition)
                    onChange()
                }
                btnMinus.setOnClickListener {
                    if (item.quantity > 1) {
                        item.quantity--
                        notifyItemChanged(holder.bindingAdapterPosition)
                    } else {
                        remove(holder.bindingAdapterPosition)
                    }
                    onChange()
                }
                btnDelete.setOnClickListener {
                    remove(holder.bindingAdapterPosition)
                    onChange()
                }
            }
        }

        private fun remove(position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
