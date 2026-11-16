package com.ucompensar.technova.data

import android.content.Context
import com.ucompensar.technova.ui.common.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * CRUD de productos en la colección "products" de Firestore.
 * El vendedor gestiona solo sus productos; el administrador gestiona todos.
 */
object ProductRepository {
    private val products get() = Db.store.collection(Db.PRODUCTS)

    /** Devuelve todos los productos del catálogo ordenados por nombre. */
    suspend fun getAll(): List<Product> =
        products.get().await().documents.map { Db.toProduct(it) }.sortedBy { it.name.lowercase() }

    /** Devuelve solo los productos publicados por un vendedor. */
    suspend fun getBySeller(sellerId: String): List<Product> =
        products.whereEqualTo("sellerId", sellerId).get().await()
            .documents.map { Db.toProduct(it) }.sortedBy { it.name.lowercase() }

    /** Productos que puede gestionar el usuario actual: todos si es admin, los suyos si es vendedor. */
    suspend fun getManageable(): List<Product> =
        if (Session.isAdmin) getAll() else getBySeller(Db.uid.orEmpty())

    /** Busca un producto por su id; devuelve null si no existe. */
    suspend fun get(id: String): Product? {
        val doc = products.document(id).get().await()
        return if (doc.exists()) Db.toProduct(doc) else null
    }

    /** Crea el producto si no tiene id o lo actualiza si ya existe. */
    suspend fun save(product: Product): Product {
        return if (product.id.isEmpty()) {
            val ref = products.add(Db.fromProduct(product)).await()
            product.copy(id = ref.id)
        } else {
            products.document(product.id).set(Db.fromProduct(product)).await()
            product
        }
    }

    /** Elimina un producto del catálogo. */
    suspend fun delete(id: String) {
        products.document(id).delete().await()
    }

    /**
     * Si el catálogo está vacío, sube los productos de ejemplo con sus fotos
     * (comprimidas en Base64) a nombre del usuario actual. Solo lo hace un admin o vendedor.
     *
     * @return cantidad de productos creados (0 si el catálogo ya tenía datos).
     */
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
