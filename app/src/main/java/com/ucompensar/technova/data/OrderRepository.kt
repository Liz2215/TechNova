package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/** Aquí se manejan los pedidos que se guardan en Firestore */
object OrderRepository {
    private val orders get() = Db.store.collection(Db.ORDERS)

    /** Error del pedido con un mensaje para el usuario */
    class OrderError(message: String) : Exception(message)

    /** Crea un número corto para el pedido */
    fun newNumber(): String = (System.currentTimeMillis() / 1000 % 1_000_000).toString().padStart(6, '0')

    /** Revisa que haya stock de todo antes de pagar */
    suspend fun validateStock(items: List<CartItem>) {
        for (item in items) {
            val current = ProductRepository.get(item.product.id)
                ?: throw OrderError("El producto ${item.product.name} ya no está disponible.")
            if (current.stock < item.quantity) {
                throw OrderError("Solo quedan ${current.stock} unidades de ${current.name}.")
            }
        }
    }

    /** Esta función crea el pedido y descuenta el stock al mismo tiempo */
    suspend fun place(order: Order, items: List<CartItem>): Order {
        val ref = orders.document()
        val saved = order.copy(id = ref.id)
        Db.store.runTransaction { tx ->
            val productRefs = items.map { Db.store.collection(Db.PRODUCTS).document(it.product.id) }
            val snapshots = productRefs.map { tx.get(it) }
            snapshots.forEachIndexed { i, snap ->
                val stock = (snap.getLong("stock") ?: 0).toInt()
                if (!snap.exists() || stock < items[i].quantity) {
                    throw OrderError("No hay stock suficiente de ${items[i].product.name}.")
                }
            }
            snapshots.forEachIndexed { i, snap ->
                val stock = (snap.getLong("stock") ?: 0).toInt()
                tx.update(productRefs[i], "stock", stock - items[i].quantity)
            }
            tx.set(ref, Db.fromOrder(saved))
        }.await()
        return saved
    }

    /** Trae los pedidos del comprador */
    suspend fun forBuyer(): List<Order> =
        orders.whereEqualTo("buyerId", Db.uid.orEmpty()).get().await()
            .documents.map { Db.toOrder(it) }.sortedByDescending { it.createdAt }

    /** Trae los pedidos que tienen productos del vendedor */
    suspend fun forSeller(sellerId: String): List<Order> =
        orders.whereArrayContains("sellerIds", sellerId).get().await()
            .documents.map { Db.toOrder(it) }.sortedByDescending { it.createdAt }

    /** Trae todos los pedidos de la tienda */
    suspend fun all(): List<Order> =
        orders.get().await().documents.map { Db.toOrder(it) }.sortedByDescending { it.createdAt }

    /** Trae los pedidos según el rol del usuario */
    suspend fun forCurrentRole(): List<Order> =
        if (Session.isAdmin) all() else forSeller(Db.uid.orEmpty())

    /** Cambia el estado del pedido */
    suspend fun updateStatus(order: Order, status: OrderStatus) {
        orders.document(order.id).update(mapOf("status" to status.name, "updatedAt" to System.currentTimeMillis())).await()
        order.status = status
    }
}
