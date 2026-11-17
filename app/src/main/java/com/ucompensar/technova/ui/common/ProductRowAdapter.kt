package com.ucompensar.technova.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.ItemProductRowBinding

/** Adaptador que muestra los productos en filas con botones de editar y borrar */
class ProductRowAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductRowAdapter.Holder>() {
    private var items: List<Product> = emptyList()

    /** Guarda las vistas de una fila */
    class Holder(val binding: ItemProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    /** Cambia los productos que se muestran */
    fun submit(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    /** Crea una fila nueva */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /** Dice cuántas filas hay */
    override fun getItemCount() = items.size

    /** Pone los datos del producto en su fila */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product = items[position]
        with(holder.binding) {
            ivProduct.showProductImage(product.imageBase64)
            tvName.text = product.name
            tvStock.text = root.context.getString(R.string.label_stock, product.stock)
            tvPrice.text = product.price.toCop()
            btnEdit.setOnClickListener { onEdit(product) }
            btnDelete.setOnClickListener { onDelete(product) }
        }
    }
}
