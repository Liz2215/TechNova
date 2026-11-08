package com.ucompensar.technova.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.FragmentAdminProductsBinding
import com.ucompensar.technova.ui.common.ProductRowAdapter
import com.ucompensar.technova.ui.seller.ProductFormActivity

/** Gestión de productos del administrador: ve, crea, edita y elimina productos de toda la tienda. */
class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductRowAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Reutiliza el mismo adaptador y formulario del vendedor
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
        adapter.notifyDataSetChanged()
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
}
