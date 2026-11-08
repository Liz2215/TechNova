package com.ucompensar.technova.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.ItemProductRowBinding

/** Fila de producto con botones editar y eliminar. La usan el Vendedor y el Administrador. */
class ProductRowAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductRowAdapter.Holder>() {

    class Holder(val binding: ItemProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = FakeRepository.products.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product = FakeRepository.products[position]
        with(holder.binding) {
            ivProduct.setImageResource(product.imageRes)
            tvName.text = product.name
            tvStock.text = root.context.getString(R.string.label_stock, product.stock)
            tvPrice.text = product.price.toCop()
            btnEdit.setOnClickListener { onEdit(product) }
            btnDelete.setOnClickListener { onDelete(product) }
        }
    }
}
