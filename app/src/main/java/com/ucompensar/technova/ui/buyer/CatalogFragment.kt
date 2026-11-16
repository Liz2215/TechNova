package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.databinding.FragmentCatalogBinding
import com.ucompensar.technova.ui.common.launchUi

/**
 * Catálogo completo de productos leído desde Firestore, con búsqueda por nombre
 * y filtro por categoría mediante chips.
 */
class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductGridAdapter
    private var selectedCategory: String? = null
    private var products: List<Product> = emptyList()

    /** Crea la vista del fragment a partir de su layout (ViewBinding). */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la cuadrícula, los filtros y carga los productos. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedCategory = arguments?.getString(ARG_CATEGORY)

        adapter = ProductGridAdapter(
            onClick = { product -> requireContext().openProductDetail(product) },
            onAddCart = { product -> launchUi { requireContext().addToCart(product) } }
        )
        binding.rvProducts.adapter = adapter

        buildCategoryChips()
        binding.etSearch.doAfterTextChanged { applyFilters() }
    }

    /** Recarga los productos cada vez que la pantalla vuelve a verse (stock actualizado). */
    override fun onResume() {
        super.onResume()
        launchUi {
            products = ProductRepository.getAll()
            applyFilters()
        }
    }

    /** Crea un chip por cada categoría y marca la que venga seleccionada. */
    private fun buildCategoryChips() {
        val categories = resources.getStringArray(R.array.product_categories)
        categories.forEach { name ->
            val chip = layoutInflater.inflate(R.layout.view_filter_chip, binding.chipCategories, false) as Chip
            chip.id = View.generateViewId()
            chip.text = name
            binding.chipCategories.addView(chip)
            if (name == selectedCategory) chip.isChecked = true
        }
        binding.chipCategories.setOnCheckedStateChangeListener { group, ids ->
            val chip = group.findViewById<Chip>(ids.firstOrNull() ?: R.id.chipAll)
            selectedCategory = if (chip.id == R.id.chipAll) null else chip.text.toString()
            applyFilters()
        }
    }

    /** Muestra solo los productos que coinciden con la búsqueda y la categoría elegida. */
    private fun applyFilters() {
        val b = _binding ?: return
        val query = b.etSearch.text.toString().trim()
        val list = products.filter { product ->
            (selectedCategory == null || product.category == selectedCategory) &&
                product.name.contains(query, ignoreCase = true)
        }
        adapter.submit(list)
    }

    /** Libera la referencia a la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        /** Crea el catálogo filtrado por la categoría indicada (null = todas). */
        fun newInstance(category: String?) = CatalogFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY, category) }
        }
    }
}
