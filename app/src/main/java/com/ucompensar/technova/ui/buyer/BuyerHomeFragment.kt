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

/** Pantalla de inicio del comprador con promoción categorías y productos destacados */
class BuyerHomeFragment : Fragment() {
    private var _binding: FragmentBuyerHomeBinding? = null
    private val binding get() = _binding!!

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara los botones y carga los productos */
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

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Adaptador de las categorías que se ven de lado */
    class CategoryTileAdapter(
        private val items: List<Category>,
        private val onClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryTileAdapter.Holder>() {
        /** Guarda las vistas de una fila */
        class Holder(val binding: ItemCategoryTileBinding) : RecyclerView.ViewHolder(binding.root)

        /** Crea una fila nueva */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCategoryTileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Dice cuántas filas hay */
        override fun getItemCount() = items.size

        /** Pone el ícono y el nombre de la categoría */
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val category = items[position]
            holder.binding.ivIcon.setImageResource(category.iconRes)
            holder.binding.tvName.text = category.name
            holder.binding.root.setOnClickListener { onClick(category) }
        }
    }
}
