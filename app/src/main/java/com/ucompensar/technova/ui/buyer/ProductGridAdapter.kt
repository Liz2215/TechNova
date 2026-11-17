package com.ucompensar.technova.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.ItemProductGridBinding
import com.ucompensar.technova.ui.common.showProductImage
import com.ucompensar.technova.ui.common.toCop

/** Adaptador que muestra los productos en tarjetas */
class ProductGridAdapter(
    private val onClick: (Product) -> Unit,
    private val onAddCart: (Product) -> Unit,
    private val horizontal: Boolean = false
) : RecyclerView.Adapter<ProductGridAdapter.Holder>() {
    private var items: List<Product> = emptyList()

    /** Cambia los productos que se muestran */
    fun submit(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    /** Guarda las vistas de una tarjeta */
    inner class Holder(val binding: ItemProductGridBinding) : RecyclerView.ViewHolder(binding.root)

    /** Crea una tarjeta nueva */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemProductGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (horizontal) {
            binding.root.layoutParams.width = parent.resources.getDimensionPixelSize(R.dimen.product_card_width)
        }
        return Holder(binding)
    }

    /** Dice cuántas tarjetas hay */
    override fun getItemCount() = items.size

    /** Pone los datos del producto en su tarjeta */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product = items[position]
        val ctx = holder.itemView.context
        with(holder.binding) {
            ivProduct.showProductImage(product.imageBase64)
            tvName.text = product.name
            tvRating.text = if (product.rating > 0) ctx.getString(R.string.label_rating, product.rating.toString())
            else ctx.getString(R.string.label_new)
            tvPrice.text = product.price.toCop()
            root.setOnClickListener { onClick(product) }
            btnAddCart.setOnClickListener { onAddCart(product) }
        }
    }
}
