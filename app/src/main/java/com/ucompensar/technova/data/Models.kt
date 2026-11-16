package com.ucompensar.technova.data

import androidx.annotation.DrawableRes

/**
 * Roles de la aplicación. Se guardan en Firestore como texto (BUYER, SELLER, ADMIN)
 * y deciden qué panel ve cada usuario y qué permisos tiene en las reglas de seguridad.
 */
enum class Role { BUYER, SELLER, ADMIN }

/** Estados por los que pasa un pedido desde que se crea hasta que llega al comprador. */
enum class OrderStatus { PENDING, SHIPPED, DELIVERED }

/** Estado del pago de un pedido según la respuesta de la pasarela (ePayco) o el pago contraentrega. */
enum class PaymentStatus { PENDING, APPROVED, REJECTED }

/**
 * Producto del catálogo (colección "products" de Firestore).
 *
 * @property id identificador del documento en Firestore.
 * @property imageBase64 foto del producto comprimida en JPEG y codificada en Base64.
 * @property sellerId uid del vendedor dueño del producto.
 */
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

/** Producto agregado al carrito con la cantidad elegida (subcolección users/{uid}/cart). */
data class CartItem(val product: Product, var quantity: Int)

/** Línea de un pedido: copia de los datos del producto en el momento de la compra. */
data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val category: String = "",
    val price: Long = 0,
    val quantity: Int = 0,
    val sellerId: String = ""
)

/**
 * Pedido de compra (colección "orders" de Firestore).
 *
 * @property number número corto y legible del pedido que se muestra al usuario.
 * @property sellerIds vendedores con productos en el pedido; permite que cada uno vea sus pedidos.
 * @property latitude latitud capturada con el GPS para la dirección de entrega (0 si no se capturó).
 * @property paymentRef referencia de la transacción devuelta por ePayco (ref_payco).
 */
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
    /** Cantidad total de unidades del pedido. */
    val itemCount: Int get() = items.sumOf { it.quantity }
}

/**
 * Usuario de la tienda (colección "users" de Firestore; el id es el uid de Firebase Authentication).
 */
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

/** Categoría del catálogo con su ícono y la cantidad de productos que tiene. */
data class Category(
    val name: String,
    @DrawableRes val iconRes: Int,
    val count: Int
)

/** Configuración general de la tienda que edita el administrador (documento settings/general). */
data class StoreSettings(
    val shippingCost: Long = Catalog.DEFAULT_SHIPPING,
    val maintenance: Boolean = false,
    val notifications: Boolean = true,
    val newSellers: Boolean = true,
    val gateways: List<String> = listOf("epayco")
)
