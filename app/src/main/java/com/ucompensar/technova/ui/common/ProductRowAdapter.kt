package com.ucompensar.technova.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.ItemProductRowBinding

/**
 * Lista de productos en filas con botones de editar y eliminar.
 * La usan el vendedor (sus productos) y el administrador (todos los productos).
 */
class ProductRowAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductRowAdapter.Holder>() {
    private var items: List<Product> = emptyList()

    /** Contenedor de las vistas de una fila. */
    class Holder(val binding: ItemProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    /** Reemplaza los productos mostrados y refresca la lista. */
    fun submit(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    /** Crea la vista de una fila nueva. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /** Cantidad de filas de la lista. */
    override fun getItemCount() = items.size

    /** Llena la fila con la foto, nombre, stock y precio del producto. */
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
