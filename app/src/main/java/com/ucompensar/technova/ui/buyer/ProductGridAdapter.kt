package com.ucompensar.technova.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.ItemProductGridBinding
import com.ucompensar.technova.ui.common.toCop

/** Adaptador de la cuadrícula de productos del comprador. */
class ProductGridAdapter(
    private val onClick: (Product) -> Unit,
    private val onAddCart: (Product) -> Unit
) : RecyclerView.Adapter<ProductGridAdapter.Holder>() {

    private var items: List<Product> = emptyList()

    fun submit(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemProductGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemProductGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product = items[position]
        val ctx = holder.itemView.context
        with(holder.binding) {
            ivProduct.setImageResource(product.imageRes)
            tvName.text = product.name
            tvRating.text = ctx.getString(R.string.label_rating, product.rating.toString())
            tvPrice.text = product.price.toCop()
            root.setOnClickListener { onClick(product) }
            btnAddCart.setOnClickListener { onAddCart(product) }
        }
    }
}
