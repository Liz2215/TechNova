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

/** Pantalla del catálogo con búsqueda y filtro por categoría */
class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductGridAdapter
    private var selectedCategory: String? = null
    private var products: List<Product> = emptyList()

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la lista y los filtros */
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

    /** Vuelve a cargar los productos cada vez que se abre */
    override fun onResume() {
        super.onResume()
        launchUi {
            products = ProductRepository.getAll()
            applyFilters()
        }
    }

    /** Crea un botón de filtro por cada categoría */
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

    /** Muestra solo los productos que coinciden con la búsqueda */
    private fun applyFilters() {
        val b = _binding ?: return
        val query = b.etSearch.text.toString().trim()
        val list = products.filter { product ->
            (selectedCategory == null || product.category == selectedCategory) &&
                product.name.contains(query, ignoreCase = true)
        }
        adapter.submit(list)
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        /** Crea el catálogo filtrado por una categoría */
        fun newInstance(category: String?) = CatalogFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY, category) }
        }
    }
}
