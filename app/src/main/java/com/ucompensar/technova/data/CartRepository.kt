package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/** Aquí se maneja el carrito de compras que se guarda en Firestore */
object CartRepository {
    private val items = mutableListOf<CartItem>()

    /** Lista de productos del carrito */
    val cart: List<CartItem> get() = items

    /** Ruta del carrito del usuario en Firestore */
    private fun cartRef() = Db.store.collection(Db.USERS).document(Db.uid.orEmpty()).collection(Db.CART)

    /** Trae el carrito guardado con los datos actuales de cada producto */
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

    /** Agrega una unidad del producto al carrito */
    suspend fun add(product: Product) {
        val existing = items.find { it.product.id == product.id }
        val quantity = (existing?.quantity ?: 0) + 1
        if (quantity > product.stock) throw IllegalStateException("No hay más unidades disponibles de ${product.name}.")
        setQuantity(product, quantity)
    }

    /** Cambia la cantidad de un producto */
    suspend fun setQuantity(product: Product, quantity: Int) {
        val existing = items.find { it.product.id == product.id }
        if (existing != null) existing.quantity = quantity else items.add(CartItem(product, quantity))
        cartRef().document(product.id)
            .set(mapOf("quantity" to quantity, "updatedAt" to System.currentTimeMillis())).await()
    }

    /** Quita un producto del carrito */
    suspend fun remove(productId: String) {
        items.removeAll { it.product.id == productId }
        cartRef().document(productId).delete().await()
    }

    /** Vacía el carrito */
    suspend fun clear() {
        cartRef().get().await().documents.forEach { it.reference.delete().await() }
        items.clear()
    }

    /** Vacía el carrito solo en el celular */
    fun clearLocal() = items.clear()

    /** Suma el valor de todo el carrito */
    fun subtotal(): Long = items.sumOf { it.product.price * it.quantity }

    /** Cuenta cuántas unidades hay en el carrito */
    fun count(): Int = items.sumOf { it.quantity }
}
