package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/**
 * Pedidos en la colección "orders" de Firestore: creación al confirmar la compra
 * (descontando el stock), historial del comprador, pedidos del vendedor y del administrador,
 * y cambio de estado.
 */
object OrderRepository {
    private val orders get() = Db.store.collection(Db.ORDERS)

    /** Error de negocio con un mensaje claro para el usuario (por ejemplo, sin stock). */
    class OrderError(message: String) : Exception(message)

    /** Genera un número de pedido corto y legible a partir de la hora actual. */
    fun newNumber(): String = (System.currentTimeMillis() / 1000 % 1_000_000).toString().padStart(6, '0')

    /**
     * Revisa en Firestore que cada producto del carrito siga existiendo y tenga stock suficiente.
     * Se llama antes de enviar al usuario a la pasarela de pagos.
     */
    suspend fun validateStock(items: List<CartItem>) {
        for (item in items) {
            val current = ProductRepository.get(item.product.id)
                ?: throw OrderError("El producto ${item.product.name} ya no está disponible.")
            if (current.stock < item.quantity) {
                throw OrderError("Solo quedan ${current.stock} unidades de ${current.name}.")
            }
        }
    }

    /**
     * Crea el pedido dentro de una transacción de Firestore: vuelve a validar el stock,
     * descuenta las unidades vendidas de cada producto y guarda el pedido. Si algo falla,
     * no se guarda nada (todo o nada).
     */
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

    /** Historial de pedidos del comprador actual, del más reciente al más antiguo. */
    suspend fun forBuyer(): List<Order> =
        orders.whereEqualTo("buyerId", Db.uid.orEmpty()).get().await()
            .documents.map { Db.toOrder(it) }.sortedByDescending { it.createdAt }

    /** Pedidos que incluyen productos del vendedor indicado. */
    suspend fun forSeller(sellerId: String): List<Order> =
        orders.whereArrayContains("sellerIds", sellerId).get().await()
            .documents.map { Db.toOrder(it) }.sortedByDescending { it.createdAt }

    /** Todos los pedidos de la tienda (solo administrador). */
    suspend fun all(): List<Order> =
        orders.get().await().documents.map { Db.toOrder(it) }.sortedByDescending { it.createdAt }

    /** Pedidos visibles para el usuario actual según su rol (admin: todos; vendedor: los suyos). */
    suspend fun forCurrentRole(): List<Order> =
        if (Session.isAdmin) all() else forSeller(Db.uid.orEmpty())

    /** Cambia el estado del pedido (pendiente, enviado o entregado). */
    suspend fun updateStatus(order: Order, status: OrderStatus) {
        orders.document(order.id).update(mapOf("status" to status.name, "updatedAt" to System.currentTimeMillis())).await()
        order.status = status
    }
}
