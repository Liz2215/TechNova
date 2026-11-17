package com.ucompensar.technova.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.databinding.FragmentSellerProductsBinding
import com.ucompensar.technova.ui.common.ProductRowAdapter
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toast


/** Lista de los productos del vendedor para crearlos editarlos o borrarlos */
class SellerProductsFragment : Fragment() {
    private var _binding: FragmentSellerProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductRowAdapter

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la lista de productos y el botón para agregar uno nuevo */
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

    /** Vuelve a cargar los productos cuando regresamos del formulario */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Trae de Firestore los productos de este vendedor */
    private fun refresh() {
        launchUi { adapter.submit(ProductRepository.getManageable()) }
    }

    /** Pregunta si de verdad quiere borrar el producto y lo borra */
    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_product_confirm, product.name))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                launchUi {
                    ProductRepository.delete(product.id)
                    requireContext().toast(getString(R.string.msg_product_deleted))
                    refresh()
                }
            }
            .show()
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
