package com.ucompensar.technova.data

import androidx.annotation.DrawableRes

/** Roles de la aplicación (RF-04: control de acceso por roles). */
enum class Role { BUYER, SELLER, ADMIN }

/** Estados posibles de un pedido. */
enum class OrderStatus { PENDING, SHIPPED, DELIVERED }

/** Producto del catálogo (RF-02: ID, SKU, categoría, precio, stock e imagen). */
data class Product(
    val id: Int,
    val sku: String,
    var name: String,
    var category: String,
    var price: Long,
    var stock: Int,
    val rating: Double,
    var description: String,
    @DrawableRes val imageRes: Int
)

/** Producto agregado al carrito con su cantidad (RF-03). */
data class CartItem(val product: Product, var quantity: Int)

/** Pedido recibido por el vendedor. */
data class Order(
    val id: String,
    val customer: String,
    val items: Int,
    val total: Long,
    var status: OrderStatus
)

/** Usuario registrado, visible para el administrador. */
data class User(
    val name: String,
    val email: String,
    val role: Role,
    var active: Boolean
)

/** Categoría del catálogo. */
data class Category(
    val name: String,
    @DrawableRes val iconRes: Int,
    val count: Int
)
