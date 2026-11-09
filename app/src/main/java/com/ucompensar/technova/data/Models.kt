package com.ucompensar.technova.data

import androidx.annotation.DrawableRes

enum class Role { BUYER, SELLER, ADMIN }

enum class OrderStatus { PENDING, SHIPPED, DELIVERED }

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

data class CartItem(val product: Product, var quantity: Int)

data class Order(
    val id: String,
    val customer: String,
    val items: Int,
    val total: Long,
    var status: OrderStatus
)

data class User(
    val id: Int,
    var name: String,
    var email: String,
    var role: Role,
    var active: Boolean
)

data class Category(
    val name: String,
    @DrawableRes val iconRes: Int,
    val count: Int
)
