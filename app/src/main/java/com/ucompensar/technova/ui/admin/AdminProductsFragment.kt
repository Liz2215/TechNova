package com.ucompensar.technova.ui.admin

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
import com.ucompensar.technova.databinding.FragmentAdminProductsBinding
import com.ucompensar.technova.ui.common.ProductRowAdapter
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.seller.ProductFormActivity

/** Productos de toda la tienda para el administrador: permite crear, editar y eliminar cualquier producto (CRUD en Firestore). */
class AdminProductsFragment : Fragment() {
    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductRowAdapter

    /** Crea la vista del fragment a partir de su layout (ViewBinding). */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la lista con las acciones de editar y eliminar, y el botón de agregar. */
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

    /** Recarga los productos al volver del formulario para ver los cambios. */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Lee de Firestore los productos que este usuario puede gestionar. */
    private fun refresh() {
        launchUi { adapter.submit(ProductRepository.getManageable()) }
    }

    /** Pide confirmación y elimina el producto de Firestore. */
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

    /** Libera la referencia a la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
