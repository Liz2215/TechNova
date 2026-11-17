package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.data.CartItem
import com.ucompensar.technova.data.CartRepository
import com.ucompensar.technova.data.SettingsRepository
import com.ucompensar.technova.databinding.ActivityCartBinding
import com.ucompensar.technova.databinding.ItemCartBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showProductImage
import com.ucompensar.technova.ui.common.toCop

/** Pantalla del carrito de compras */
class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private var shippingCost = 0L
    private val adapter = CartAdapter(
        onQuantity = { item, quantity -> launchUi { CartRepository.setQuantity(item.product, quantity); refresh() } },
        onRemove = { item -> launchUi { CartRepository.remove(item.product.id); refresh() } }
    )

    /** Prepara la lista y el botón para pagar */
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

    /** Vuelve a cargar el carrito cada vez que se abre */
    override fun onResume() {
        super.onResume()
        binding.btnCheckout.isEnabled = false
        launchUi {
            shippingCost = SettingsRepository.get().shippingCost
            CartRepository.load()
            refresh()
        }
    }

    /** Actualiza la lista y los totales */
    private fun refresh() {
        adapter.submit(CartRepository.cart.toList())
        val empty = CartRepository.cart.isEmpty()
        binding.layoutEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        binding.btnCheckout.isEnabled = !empty
        val subtotal = CartRepository.subtotal()
        val shipping = if (empty) 0L else shippingCost
        binding.summary.tvSubtotal.text = subtotal.toCop()
        binding.summary.tvShipping.text = shipping.toCop()
        binding.summary.tvTotal.text = (subtotal + shipping).toCop()
    }

    /** Adaptador que pinta cada producto del carrito */
    private class CartAdapter(
        private val onQuantity: (CartItem, Int) -> Unit,
        private val onRemove: (CartItem) -> Unit
    ) : RecyclerView.Adapter<CartAdapter.Holder>() {
        /** Guarda las vistas de una fila */
        class Holder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

        private var items: List<CartItem> = emptyList()

        /** Cambia los productos que se muestran */
        fun submit(list: List<CartItem>) {
            items = list
            notifyDataSetChanged()
        }

        /** Crea una fila nueva */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Dice cuántas filas hay */
        override fun getItemCount() = items.size

        /** Pone el producto en su fila y conecta los botones de más y menos */
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            with(holder.binding) {
                ivProduct.showProductImage(item.product.imageBase64)
                tvName.text = item.product.name
                tvPrice.text = (item.product.price * item.quantity).toCop()
                tvQty.text = item.quantity.toString()
                btnPlus.setOnClickListener {
                    if (item.quantity < item.product.stock) onQuantity(item, item.quantity + 1)
                }
                btnMinus.setOnClickListener {
                    if (item.quantity > 1) onQuantity(item, item.quantity - 1) else onRemove(item)
                }
                btnDelete.setOnClickListener { onRemove(item) }
            }
        }
    }
}
