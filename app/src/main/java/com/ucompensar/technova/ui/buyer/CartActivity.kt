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

/**
 * Carrito de compras guardado en Firestore: permite sumar, restar y quitar productos,
 * y muestra subtotal, costo de envío (configurado por el admin) y total.
 */
class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private var shippingCost = 0L
    private val adapter = CartAdapter(
        onQuantity = { item, quantity -> launchUi { CartRepository.setQuantity(item.product, quantity); refresh() } },
        onRemove = { item -> launchUi { CartRepository.remove(item.product.id); refresh() } }
    )

    /** Prepara la lista y el botón para ir a pagar. */
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

    /** Recarga el carrito desde Firestore cada vez que la pantalla se muestra. */
    override fun onResume() {
        super.onResume()
        binding.btnCheckout.isEnabled = false
        launchUi {
            shippingCost = SettingsRepository.get().shippingCost
            CartRepository.load()
            refresh()
        }
    }

    /** Actualiza la lista, el mensaje de carrito vacío y los totales. */
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

    /** Filas del carrito con foto, nombre, precio y botones de cantidad. */
    private class CartAdapter(
        private val onQuantity: (CartItem, Int) -> Unit,
        private val onRemove: (CartItem) -> Unit
    ) : RecyclerView.Adapter<CartAdapter.Holder>() {
        /** Contenedor de las vistas de una fila de la lista. */
        class Holder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

        private var items: List<CartItem> = emptyList()

        /** Reemplaza los productos mostrados. */
        fun submit(list: List<CartItem>) {
            items = list
            notifyDataSetChanged()
        }

        /** Crea la vista de una fila nueva. */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Cantidad de filas de la lista. */
        override fun getItemCount() = items.size

        /** Muestra el producto y permite subir (hasta el stock), bajar o quitar unidades. */
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
