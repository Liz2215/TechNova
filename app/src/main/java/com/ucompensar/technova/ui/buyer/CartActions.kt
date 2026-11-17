package com.ucompensar.technova.ui.buyer

import android.content.Context
import android.content.Intent
import com.ucompensar.technova.R
import com.ucompensar.technova.data.CartRepository
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.common.toastLong

/** Agrega un producto al carrito y le avisa al usuario */
suspend fun Context.addToCart(product: Product) {
    try {
        if (product.stock <= 0) {
            toastLong(getString(R.string.msg_out_of_stock, product.name))
            return
        }
        CartRepository.add(product)
        toast(getString(R.string.msg_added_cart, product.name))
    } catch (e: Exception) {
        toastLong(errorMessage(e))
    }
}

/** Abre el detalle de un producto */
fun Context.openProductDetail(product: Product) {
    val intent = Intent(this, ProductDetailActivity::class.java)
    intent.putExtra(ProductDetailActivity.EXTRA_ID, product.id)
    startActivity(intent)
}
