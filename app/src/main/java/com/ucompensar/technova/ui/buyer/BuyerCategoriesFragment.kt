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

/** Lista de categorías con la cantidad real de productos de cada una (según Firestore). */
class BuyerCategoriesFragment : Fragment() {
    private var _binding: FragmentBuyerCategoriesBinding? = null
    private val binding get() = _binding!!

    /** Crea la vista del fragment a partir de su layout (ViewBinding). */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Carga los productos para contar cuántos hay por categoría. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        launchUi {
            val categories = Catalog.categories(ProductRepository.getAll())
            binding.rvCategories.adapter = CategoryAdapter(categories) { category ->
                (activity as? BuyerActivity)?.openCatalog(category.name)
            }
        }
    }

    /** Libera la referencia a la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Filas de categorías con ícono, nombre y número de productos. */
    class CategoryAdapter(
        private val items: List<Category>,
        private val onClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.Holder>() {
        /** Contenedor de las vistas de una fila de la lista. */
        class Holder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

        /** Crea la vista de una fila nueva. */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Cantidad de filas de la lista. */
        override fun getItemCount() = items.size

        /** Llena la fila con los datos del elemento en esa posición. */
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
