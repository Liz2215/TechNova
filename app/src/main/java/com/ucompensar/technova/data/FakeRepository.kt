package com.ucompensar.technova.data

import com.ucompensar.technova.R

/**
 * Datos de prueba en memoria.
 * En esta entrega (frontend) reemplaza a la API REST que se conectará más adelante.
 */
object FakeRepository {

    const val SHIPPING_COST = 12_000L

    val products = mutableListOf(
        Product(1, "SW-PRO-01", "Smartwatch Nova Pro", "Smartwatches", 389_900, 18, 4.7,
            "Pantalla AMOLED de 1.9\", GPS, monitor de ritmo cardíaco y batería de 7 días.", R.drawable.ic_watch),
        Product(2, "AU-BT-G3", "Audífonos Bluetooth G3", "Audio", 159_900, 25, 4.5,
            "Cancelación de ruido activa, 30 horas de batería y carga rápida USB-C.", R.drawable.ic_headphones),
        Product(3, "CE-X12-128", "Celular X12 128GB", "Celulares", 1_249_000, 9, 4.6,
            "Cámara de 108 MP, pantalla de 6.7\" a 120 Hz y 8 GB de RAM.", R.drawable.ic_phone),
        Product(4, "AC-KB-K1", "Teclado Mecánico K1", "Gaming", 289_000, 20, 4.8,
            "Switches rojos, iluminación RGB y conexión inalámbrica 2.4 GHz.", R.drawable.ic_keyboard),
        Product(5, "GA-CTRL-S", "Control Gamer S", "Gaming", 219_000, 14, 4.4,
            "Compatible con PC y celular, vibración dual y batería recargable.", R.drawable.ic_gamepad),
        Product(6, "AC-PB-20K", "Power Bank 20.000 mAh", "Accesorios", 129_900, 40, 4.3,
            "Carga rápida de 22.5 W con dos puertos USB y uno USB-C.", R.drawable.ic_battery),
        Product(7, "SW-FIT-S", "Reloj Inteligente Fit S", "Smartwatches", 199_900, 30, 4.2,
            "Resistente al agua, 100 modos deportivos y notificaciones.", R.drawable.ic_watch),
        Product(8, "AU-BUDS-M", "Earbuds Mini", "Audio", 99_900, 50, 4.1,
            "Audífonos in-ear livianos con estuche de carga.", R.drawable.ic_headphones)
    )

    val cart = mutableListOf<CartItem>()

    val orders = mutableListOf(
        Order("1045", "Laura Gómez", 2, 549_800, OrderStatus.PENDING),
        Order("1044", "Andrés Ruiz", 1, 1_249_000, OrderStatus.PENDING),
        Order("1043", "Camila Torres", 3, 418_700, OrderStatus.SHIPPED),
        Order("1042", "Julián Pérez", 1, 289_000, OrderStatus.DELIVERED),
        Order("1041", "Sofía Martínez", 2, 359_800, OrderStatus.DELIVERED)
    )

    val users = mutableListOf(
        User(1, "Natalia Romero", "natalia.romero@technova.co", Role.ADMIN, true),
        User(2, "Samuel Ortiz", "samuel.ortiz@technova.co", Role.SELLER, true),
        User(3, "Valentina Ríos", "vale.rios@gmail.com", Role.SELLER, true),
        User(4, "Laura Gómez", "laura.gomez@gmail.com", Role.BUYER, true),
        User(5, "Andrés Ruiz", "andres.ruiz@hotmail.com", Role.BUYER, false),
        User(6, "Camila Torres", "camila.t@gmail.com", Role.BUYER, true)
    )

    /** Ventas de los últimos 7 días (en miles de COP) para el gráfico del vendedor. */
    val weeklySales = listOf(420, 610, 380, 890, 720, 1_150, 960)

    /** Participación (%) de ventas por categoría para el panel del administrador. */
    val salesByCategory = listOf(
        "Smartwatches" to 34, "Audio" to 26, "Celulares" to 21, "Gaming" to 12, "Accesorios" to 7
    )

    fun categories(): List<Category> = listOf(
        Category("Smartwatches", R.drawable.ic_watch, countBy("Smartwatches")),
        Category("Audio", R.drawable.ic_headphones, countBy("Audio")),
        Category("Celulares", R.drawable.ic_phone, countBy("Celulares")),
        Category("Accesorios", R.drawable.ic_battery, countBy("Accesorios")),
        Category("Gaming", R.drawable.ic_gamepad, countBy("Gaming"))
    )

    private fun countBy(category: String) = products.count { it.category == category }

    fun findProduct(id: Int): Product? = products.find { it.id == id }

    fun findUser(id: Int): User? = users.find { it.id == id }

    /** Ventas por período para el reporte del administrador (miles de COP). */
    val salesByPeriod = mapOf(
        "week" to listOf(420, 610, 380, 890, 720, 1_150, 960),
        "month" to listOf(3_900, 4_650, 4_100, 5_300),
        "year" to listOf(41_200, 46_800, 52_300, 61_900)
    )

    /** Pedidos por período (para calcular el ticket promedio). */
    val ordersByPeriod = mapOf("week" to 38, "month" to 154, "year" to 1_720)

    /** Productos más vendidos con sus unidades. */
    val topProducts = listOf(
        "Audífonos Bluetooth G3" to 86, "Smartwatch Nova Pro" to 64,
        "Earbuds Mini" to 51, "Power Bank 20.000 mAh" to 47, "Teclado Mecánico K1" to 33
    )

    fun addToCart(product: Product) {
        val item = cart.find { it.product.id == product.id }
        if (item != null) item.quantity++ else cart.add(CartItem(product, 1))
    }

    fun cartSubtotal(): Long = cart.sumOf { it.product.price * it.quantity }

    fun cartCount(): Int = cart.sumOf { it.quantity }

    fun imageForCategory(category: String): Int = when (category) {
        "Smartwatches" -> R.drawable.ic_watch
        "Audio" -> R.drawable.ic_headphones
        "Celulares" -> R.drawable.ic_phone
        "Gaming" -> R.drawable.ic_gamepad
        else -> R.drawable.ic_battery
    }
}
