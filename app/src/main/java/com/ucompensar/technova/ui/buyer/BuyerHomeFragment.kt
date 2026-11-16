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
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.databinding.FragmentBuyerHomeBinding
import com.ucompensar.technova.databinding.ItemCategoryTileBinding
import com.ucompensar.technova.ui.common.launchUi

/**
 * Inicio del comprador: saludo con su nombre, banner de promoción, categorías
 * y los productos mejor calificados, leídos desde Firestore.
 */
class BuyerHomeFragment : Fragment() {
    private var _binding: FragmentBuyerHomeBinding? = null
    private val binding get() = _binding!!

    /** Crea la vista del fragment a partir de su layout (ViewBinding). */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Configura los botones y carga los productos del catálogo. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val buyer = activity as? BuyerActivity
        val firstName = Session.current?.name?.substringBefore(" ").orEmpty()
        if (firstName.isNotEmpty()) binding.tvGreeting.text = getString(R.string.home_greeting_name, firstName)

        binding.btnPromo.setOnClickListener { buyer?.openCatalog("Smartwatches") }
        binding.btnSeeAll.setOnClickListener { buyer?.openCatalog() }

        val adapter = ProductGridAdapter(
            onClick = { product -> requireContext().openProductDetail(product) },
            onAddCart = { product -> launchUi { requireContext().addToCart(product) } },
            horizontal = true
        )
        binding.rvBestSellers.adapter = adapter

        launchUi {
            val products = ProductRepository.getAll()
            binding.rvCategories.adapter = CategoryTileAdapter(Catalog.categories(products)) { category ->
                buyer?.openCatalog(category.name)
            }
            adapter.submit(products.sortedByDescending { it.rating }.take(5))
        }
    }

    /** Libera la referencia a la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Lista horizontal de categorías con su ícono; al tocar una abre el catálogo filtrado. */
    class CategoryTileAdapter(
        private val items: List<Category>,
        private val onClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryTileAdapter.Holder>() {
        /** Contenedor de las vistas de una fila de la lista. */
        class Holder(val binding: ItemCategoryTileBinding) : RecyclerView.ViewHolder(binding.root)

        /** Crea la vista de una fila nueva. */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCategoryTileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Cantidad de filas de la lista. */
        override fun getItemCount() = items.size

        /** Llena la fila con los datos del elemento en esa posición. */
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val category = items[position]
            holder.binding.ivIcon.setImageResource(category.iconRes)
            holder.binding.tvName.text = category.name
            holder.binding.root.setOnClickListener { onClick(category) }
        }
    }
}
