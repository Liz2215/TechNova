package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.ActivityProductDetailBinding
import com.ucompensar.technova.ui.common.toCop

/** Detalle de un producto del catálogo. */
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val product = FakeRepository.findProduct(intent.getIntExtra(EXTRA_ID, -1))
        if (product == null) {
            finish()
            return
        }

        with(binding) {
            toolbar.title = product.category
            ivProduct.setImageResource(product.imageRes)
            tvName.text = product.name
            tvRating.text = getString(R.string.label_rating, product.rating.toString())
            tvCategory.text = product.category
            tvPrice.text = product.price.toCop()
            tvStock.text = getString(R.string.label_stock, product.stock)
            tvSku.text = getString(R.string.label_sku, product.sku)
            tvDescription.text = product.description
            btnAddCart.setOnClickListener {
                FakeRepository.addToCart(product)
                Toast.makeText(this@ProductDetailActivity,
                    getString(R.string.msg_added_cart, product.name), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "product_id"
    }
}
