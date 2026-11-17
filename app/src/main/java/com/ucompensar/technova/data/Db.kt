package com.ucompensar.technova.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/** Conexión con Firebase y funciones para pasar los datos de Firestore a la app */
object Db {
    const val USERS = "users"
    const val PRODUCTS = "products"
    const val ORDERS = "orders"
    const val CART = "cart"
    const val SETTINGS = "settings"
    const val PQRS = "pqrs"

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val store: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /** Id del usuario que inició sesión */
    val uid: String? get() = auth.currentUser?.uid

    /** Pasa un documento de Firestore a usuario */
    fun toUser(doc: DocumentSnapshot) = User(
        id = doc.id,
        name = doc.getString("name").orEmpty(),
        email = doc.getString("email").orEmpty(),
        phone = doc.getString("phone").orEmpty(),
        role = runCatching { Role.valueOf(doc.getString("role") ?: "BUYER") }.getOrDefault(Role.BUYER),
        active = doc.getBoolean("active") ?: true,
        address = doc.getString("address").orEmpty(),
        city = doc.getString("city").orEmpty(),
        latitude = doc.getDouble("latitude") ?: 0.0,
        longitude = doc.getDouble("longitude") ?: 0.0
    )

    /** Pasa un usuario a datos de Firestore */
    fun fromUser(user: User): Map<String, Any> = mapOf(
        "name" to user.name,
        "email" to user.email,
        "phone" to user.phone,
        "role" to user.role.name,
        "active" to user.active,
        "address" to user.address,
        "city" to user.city,
        "latitude" to user.latitude,
        "longitude" to user.longitude
    )

    /** Pasa un documento de Firestore a producto */
    fun toProduct(doc: DocumentSnapshot) = Product(
        id = doc.id,
        sku = doc.getString("sku").orEmpty(),
        name = doc.getString("name").orEmpty(),
        category = doc.getString("category").orEmpty(),
        price = doc.getLong("price") ?: 0,
        stock = (doc.getLong("stock") ?: 0).toInt(),
        rating = doc.getDouble("rating") ?: 0.0,
        description = doc.getString("description").orEmpty(),
        imageBase64 = doc.getString("imageBase64").orEmpty(),
        sellerId = doc.getString("sellerId").orEmpty(),
        sellerName = doc.getString("sellerName").orEmpty()
    )

    /** Pasa un producto a datos de Firestore */
    fun fromProduct(product: Product): Map<String, Any> = mapOf(
        "sku" to product.sku,
        "name" to product.name,
        "category" to product.category,
        "price" to product.price,
        "stock" to product.stock,
        "rating" to product.rating,
        "description" to product.description,
        "imageBase64" to product.imageBase64,
        "sellerId" to product.sellerId,
        "sellerName" to product.sellerName
    )

    /** Pasa un documento de Firestore a pedido */
    @Suppress("UNCHECKED_CAST")
    fun toOrder(doc: DocumentSnapshot): Order {
        val rawItems = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
        val items = rawItems.map { m ->
            OrderItem(
                productId = m["productId"] as? String ?: "",
                name = m["name"] as? String ?: "",
                category = m["category"] as? String ?: "",
                price = (m["price"] as? Number)?.toLong() ?: 0,
                quantity = (m["quantity"] as? Number)?.toInt() ?: 0,
                sellerId = m["sellerId"] as? String ?: ""
            )
        }
        return Order(
            id = doc.id,
            number = doc.getString("number").orEmpty(),
            buyerId = doc.getString("buyerId").orEmpty(),
            customer = doc.getString("customer").orEmpty(),
            items = items,
            sellerIds = doc.get("sellerIds") as? List<String> ?: emptyList(),
            subtotal = doc.getLong("subtotal") ?: 0,
            shipping = doc.getLong("shipping") ?: 0,
            total = doc.getLong("total") ?: 0,
            status = runCatching { OrderStatus.valueOf(doc.getString("status") ?: "PENDING") }.getOrDefault(OrderStatus.PENDING),
            paymentMethod = doc.getString("paymentMethod").orEmpty(),
            paymentStatus = runCatching { PaymentStatus.valueOf(doc.getString("paymentStatus") ?: "PENDING") }
                .getOrDefault(PaymentStatus.PENDING),
            paymentRef = doc.getString("paymentRef").orEmpty(),
            address = doc.getString("address").orEmpty(),
            city = doc.getString("city").orEmpty(),
            latitude = doc.getDouble("latitude") ?: 0.0,
            longitude = doc.getDouble("longitude") ?: 0.0,
            createdAt = doc.getLong("createdAt") ?: 0
        )
    }

    /** Pasa un pedido a datos de Firestore */
    fun fromOrder(order: Order): Map<String, Any> = mapOf(
        "number" to order.number,
        "buyerId" to order.buyerId,
        "customer" to order.customer,
        "items" to order.items.map {
            mapOf(
                "productId" to it.productId, "name" to it.name, "category" to it.category,
                "price" to it.price, "quantity" to it.quantity, "sellerId" to it.sellerId
            )
        },
        "sellerIds" to order.sellerIds,
        "subtotal" to order.subtotal,
        "shipping" to order.shipping,
        "total" to order.total,
        "status" to order.status.name,
        "paymentMethod" to order.paymentMethod,
        "paymentStatus" to order.paymentStatus.name,
        "paymentRef" to order.paymentRef,
        "address" to order.address,
        "city" to order.city,
        "latitude" to order.latitude,
        "longitude" to order.longitude,
        "createdAt" to order.createdAt
    )
}
