package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Category
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.FragmentBuyerCategoriesBinding
import com.ucompensar.technova.databinding.ItemCategoryBinding

/** Lista de categorías. Al tocar una se abre el catálogo filtrado. */
class BuyerCategoriesFragment : Fragment() {

    private var _binding: FragmentBuyerCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvCategories.adapter = CategoryAdapter(FakeRepository.categories()) { category ->
            (activity as? BuyerActivity)?.openCategory(category.name)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class CategoryAdapter(
        private val items: List<Category>,
        private val onClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.Holder>() {

        class Holder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val category = items[position]
            with(holder.binding) {
                ivIcon.setImageResource(category.iconRes)
                tvName.text = category.name
                tvCount.text = root.context.resources.getQuantityString(R.plurals.label_products_count, category.count, category.count)
                root.setOnClickListener { onClick(category) }
            }
        }
    }
}
