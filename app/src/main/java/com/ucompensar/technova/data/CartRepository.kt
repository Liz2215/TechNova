package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/**
 * CRUD del carrito de compras. Cada producto agregado se guarda en la subcolección
 * users/{uid}/cart de Firestore, así el carrito se conserva aunque se cierre la app.
 * También se mantiene una copia en memoria para mostrar los totales al instante.
 */
object CartRepository {
    private val items = mutableListOf<CartItem>()

    /** Copia de solo lectura de los productos del carrito. */
    val cart: List<CartItem> get() = items

    /** Referencia a la subcolección del carrito del usuario actual en Firestore. */
    private fun cartRef() = Db.store.collection(Db.USERS).document(Db.uid.orEmpty()).collection(Db.CART)

    /**
     * Lee el carrito guardado en Firestore y trae los datos actuales de cada producto
     * (precio, stock y foto). Los productos que ya no existen se quitan del carrito.
     */
    suspend fun load(): List<CartItem> {
        val docs = cartRef().get().await().documents
        val loaded = mutableListOf<CartItem>()
        for (doc in docs) {
            val product = ProductRepository.get(doc.id)
            val quantity = (doc.getLong("quantity") ?: 1).toInt()
            if (product == null) doc.reference.delete().await()
            else loaded.add(CartItem(product, quantity.coerceAtMost(product.stock.coerceAtLeast(1))))
        }
        items.clear()
        items.addAll(loaded)
        return items
    }

    /** Agrega una unidad del producto al carrito (o suma una si ya estaba). */
    suspend fun add(product: Product) {
        val existing = items.find { it.product.id == product.id }
        val quantity = (existing?.quantity ?: 0) + 1
        if (quantity > product.stock) throw IllegalStateException("No hay más unidades disponibles de ${product.name}.")
        setQuantity(product, quantity)
    }

    /** Cambia la cantidad de un producto del carrito y la guarda en Firestore. */
    suspend fun setQuantity(product: Product, quantity: Int) {
        val existing = items.find { it.product.id == product.id }
        if (existing != null) existing.quantity = quantity else items.add(CartItem(product, quantity))
        cartRef().document(product.id)
            .set(mapOf("quantity" to quantity, "updatedAt" to System.currentTimeMillis())).await()
    }

    /** Quita un producto del carrito. */
    suspend fun remove(productId: String) {
        items.removeAll { it.product.id == productId }
        cartRef().document(productId).delete().await()
    }

    /** Vacía el carrito en Firestore y en memoria (se usa al confirmar un pedido). */
    suspend fun clear() {
        cartRef().get().await().documents.forEach { it.reference.delete().await() }
        items.clear()
    }

    /** Vacía solo la copia en memoria (al cerrar sesión). */
    fun clearLocal() = items.clear()

    /** Suma de precio por cantidad de todos los productos del carrito. */
    fun subtotal(): Long = items.sumOf { it.product.price * it.quantity }

    /** Cantidad total de unidades en el carrito. */
    fun count(): Int = items.sumOf { it.quantity }
}
