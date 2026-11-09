package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Category
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.FragmentBuyerHomeBinding
import com.ucompensar.technova.databinding.ItemCategoryTileBinding

class BuyerHomeFragment : Fragment() {
    private var _binding: FragmentBuyerHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val buyer = activity as? BuyerActivity

        binding.btnPromo.setOnClickListener { buyer?.openCatalog("Smartwatches") }
        binding.btnSeeAll.setOnClickListener { buyer?.openCatalog() }

        binding.rvCategories.adapter = CategoryTileAdapter(FakeRepository.categories()) { category ->
            buyer?.openCatalog(category.name)
        }

        val bestSellers = FakeRepository.products.sortedByDescending { it.rating }.take(5)
        val adapter = ProductGridAdapter(
            onClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra(ProductDetailActivity.EXTRA_ID, product.id)
                startActivity(intent)
            },
            onAddCart = { product ->
                FakeRepository.addToCart(product)
                Toast.makeText(requireContext(), getString(R.string.msg_added_cart, product.name), Toast.LENGTH_SHORT).show()
            },
            horizontal = true
        )
        adapter.submit(bestSellers)
        binding.rvBestSellers.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CategoryTileAdapter(
        private val items: List<Category>,
        private val onClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryTileAdapter.Holder>() {
        class Holder(val binding: ItemCategoryTileBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemCategoryTileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val category = items[position]
            holder.binding.ivIcon.setImageResource(category.iconRes)
            holder.binding.tvName.text = category.name
            holder.binding.root.setOnClickListener { onClick(category) }
        }
    }
}
