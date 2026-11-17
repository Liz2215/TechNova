package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Catalog
import com.ucompensar.technova.data.Category
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.databinding.FragmentBuyerCategoriesBinding
import com.ucompensar.technova.databinding.ItemCategoryBinding
import com.ucompensar.technova.ui.common.launchUi

/** Pantalla con la lista de categorías */
class BuyerCategoriesFragment : Fragment() {
    private var _binding: FragmentBuyerCategoriesBinding? = null
    private val binding get() = _binding!!

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Trae los productos para contar cuántos hay en cada categoría */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        launchUi {
            val categories = Catalog.categories(ProductRepository.getAll())
            binding.rvCategories.adapter = CategoryAdapter(categories) { category ->
                (activity as? BuyerActivity)?.openCatalog(category.name)
            }
        }
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Adaptador que pinta cada categoría */
    class CategoryAdapter(
        private val items: List<Category>,
        private val onClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.Holder>() {
        /** Guarda las vistas de una fila */
        class Holder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

        /** Crea una fila nueva */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Dice cuántas filas hay */
        override fun getItemCount() = items.size

        /** Pone los datos de la categoría en su fila */
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
