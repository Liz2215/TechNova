package com.ucompensar.technova.data

import androidx.annotation.DrawableRes

/** Roles que puede tener un usuario */
enum class Role { BUYER, SELLER, ADMIN }

/** Estados de un pedido */
enum class OrderStatus { PENDING, SHIPPED, DELIVERED }

/** Estados del pago de un pedido */
enum class PaymentStatus { PENDING, APPROVED, REJECTED }

/** Datos de un producto */
data class Product(
    val id: String = "",
    val sku: String = "",
    var name: String = "",
    var category: String = "",
    var price: Long = 0,
    var stock: Int = 0,
    val rating: Double = 0.0,
    var description: String = "",
    var imageBase64: String = "",
    val sellerId: String = "",
    val sellerName: String = ""
)

/** Producto que está en el carrito con su cantidad */
data class CartItem(val product: Product, var quantity: Int)

/** Producto dentro de un pedido */
data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val category: String = "",
    val price: Long = 0,
    val quantity: Int = 0,
    val sellerId: String = ""
)

/** Datos de un pedido */
data class Order(
    val id: String = "",
    val number: String = "",
    val buyerId: String = "",
    val customer: String = "",
    val items: List<OrderItem> = emptyList(),
    val sellerIds: List<String> = emptyList(),
    val subtotal: Long = 0,
    val shipping: Long = 0,
    val total: Long = 0,
    var status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: String = "",
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    var paymentRef: String = "",
    val address: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = 0
) {
    /** Cuenta cuántas unidades tiene el pedido */
    val itemCount: Int get() = items.sumOf { it.quantity }
}

/** Datos de un usuario */
data class User(
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var role: Role = Role.BUYER,
    var active: Boolean = true,
    var address: String = "",
    var city: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

/** Datos de una categoría */
data class Category(
    val name: String,
    @DrawableRes val iconRes: Int,
    val count: Int
)

/** Ajustes generales de la tienda */
data class StoreSettings(
    val shippingCost: Long = Catalog.DEFAULT_SHIPPING,
    val maintenance: Boolean = false,
    val notifications: Boolean = true,
    val newSellers: Boolean = true,
    val gateways: List<String> = listOf("epayco")
)
