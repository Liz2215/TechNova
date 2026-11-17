package com.ucompensar.technova.data

import android.content.Context
import com.ucompensar.technova.ui.common.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Aquí se manejan los productos que se guardan en Firestore */
object ProductRepository {
    private val products get() = Db.store.collection(Db.PRODUCTS)

    /** Trae todos los productos */
    suspend fun getAll(): List<Product> =
        products.get().await().documents.map { Db.toProduct(it) }.sortedBy { it.name.lowercase() }

    /** Trae los productos de un vendedor */
    suspend fun getBySeller(sellerId: String): List<Product> =
        products.whereEqualTo("sellerId", sellerId).get().await()
            .documents.map { Db.toProduct(it) }.sortedBy { it.name.lowercase() }

    /** Trae los productos que el usuario puede manejar según su rol */
    suspend fun getManageable(): List<Product> =
        if (Session.isAdmin) getAll() else getBySeller(Db.uid.orEmpty())

    /** Busca un producto por su id */
    suspend fun get(id: String): Product? {
        val doc = products.document(id).get().await()
        return if (doc.exists()) Db.toProduct(doc) else null
    }

    /** Guarda un producto nuevo o los cambios de uno que ya existe */
    suspend fun save(product: Product): Product {
        return if (product.id.isEmpty()) {
            val ref = products.add(Db.fromProduct(product)).await()
            product.copy(id = ref.id)
        } else {
            products.document(product.id).set(Db.fromProduct(product)).await()
            product
        }
    }

    /** Borra un producto */
    suspend fun delete(id: String) {
        products.document(id).delete().await()
    }

    /** Si no hay productos sube los de ejemplo */
    suspend fun seedIfEmpty(context: Context): Int {
        if (Session.role == Role.BUYER) return 0
        if (!products.limit(1).get().await().isEmpty) return 0
        val me = Session.current ?: return 0
        Catalog.seeds.forEach { seed ->
            val image = withContext(Dispatchers.Default) { ImageUtils.resourceToBase64(context, seed.imageRes) }
            save(seed.product.copy(imageBase64 = image, sellerId = me.id, sellerName = me.name))
        }
        return Catalog.seeds.size
    }
}
