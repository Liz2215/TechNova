package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.databinding.ActivityProductDetailBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showProductImage
import com.ucompensar.technova.ui.common.toCop

/** Detalle de un producto (foto, precio, stock, SKU y descripción) con botón para agregarlo al carrito. */
class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    /** Lee el producto desde Firestore usando el id recibido y lo muestra. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnAddCart.isEnabled = false

        val id = intent.getStringExtra(EXTRA_ID).orEmpty()
        launchUi {
            val product = ProductRepository.get(id)
            if (product == null) {
                finish()
                return@launchUi
            }
            with(binding) {
                toolbar.title = product.category
                ivProduct.showProductImage(product.imageBase64)
                tvName.text = product.name
                tvRating.text = if (product.rating > 0) getString(R.string.label_rating, product.rating.toString())
                else getString(R.string.label_new)
                tvCategory.text = product.category
                tvPrice.text = product.price.toCop()
                tvStock.text = getString(R.string.label_stock, product.stock)
                tvSku.text = getString(R.string.label_sku, product.sku)
                tvDescription.text = product.description
                btnAddCart.isEnabled = product.stock > 0
                btnAddCart.setOnClickListener { launchUi { addToCart(product) } }
            }
        }
    }

    companion object {
        const val EXTRA_ID = "product_id"
    }
}
