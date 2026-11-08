package com.ucompensar.technova.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.FragmentSellerProductsBinding
import com.ucompensar.technova.databinding.ItemProductRowBinding
import com.ucompensar.technova.ui.common.toCop

/** Inventario del vendedor: crear, editar y eliminar productos. */
class SellerProductsFragment : Fragment() {

    private var _binding: FragmentSellerProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductRowAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ProductRowAdapter(
            onEdit = { product ->
                val intent = Intent(requireContext(), ProductFormActivity::class.java)
                intent.putExtra(ProductFormActivity.EXTRA_ID, product.id)
                startActivity(intent)
            },
            onDelete = { product -> confirmDelete(product) }
        )
        binding.rvProducts.adapter = adapter
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), ProductFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged() // refresca al volver del formulario
    }

    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_product_confirm, product.name))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                FakeRepository.products.remove(product)
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), R.string.msg_product_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ProductRowAdapter(
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
}
