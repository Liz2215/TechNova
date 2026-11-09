package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.FragmentCatalogBinding

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductGridAdapter
    private var selectedCategory: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedCategory = arguments?.getString(ARG_CATEGORY)

        adapter = ProductGridAdapter(
            onClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra(ProductDetailActivity.EXTRA_ID, product.id)
                startActivity(intent)
            },
            onAddCart = { product ->
                FakeRepository.addToCart(product)
                Toast.makeText(requireContext(), getString(R.string.msg_added_cart, product.name), Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvProducts.adapter = adapter

        buildCategoryChips()
        binding.etSearch.doAfterTextChanged { applyFilters() }
        applyFilters()
    }

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

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim()
        val list = FakeRepository.products.filter { product ->
            (selectedCategory == null || product.category == selectedCategory) &&
                product.name.contains(query, ignoreCase = true)
        }
        adapter.submit(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String?) = CatalogFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY, category) }
        }
    }
}
