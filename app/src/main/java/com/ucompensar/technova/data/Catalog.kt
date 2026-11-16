package com.ucompensar.technova.data

import com.ucompensar.technova.R

/**
 * Datos fijos del catálogo: categorías con su ícono, costo de envío por defecto
 * y los productos de ejemplo que se cargan una sola vez cuando la base de datos está vacía.
 */
object Catalog {
    const val DEFAULT_SHIPPING = 12_000L

    /** Nombre de cada categoría con el ícono que la representa. */
    private val categoryIcons = linkedMapOf(
        "Smartwatches" to R.drawable.ic_watch,
        "Audio" to R.drawable.ic_headphones,
        "Celulares" to R.drawable.ic_phone,
        "Accesorios" to R.drawable.ic_battery,
        "Gaming" to R.drawable.ic_gamepad
    )

    /** Construye la lista de categorías contando cuántos productos tiene cada una. */
    fun categories(products: List<Product>): List<Category> = categoryIcons.map { (name, icon) ->
        Category(name, icon, products.count { it.category == name })
    }

    /** Producto de ejemplo con la foto local que se sube a Firestore al sembrar la base de datos. */
    data class Seed(val product: Product, val imageRes: Int)

    /** Catálogo inicial de la tienda (los mismos productos de la entrega del frontend). */
    val seeds = listOf(
        Seed(Product(sku = "SW-PRO-01", name = "Smartwatch Nova Pro", category = "Smartwatches", price = 389_900, stock = 18, rating = 4.7,
            description = "Pantalla AMOLED de 1.9\", GPS, monitor de ritmo cardíaco y batería de 7 días."), R.drawable.prod_smartwatch_pro),
        Seed(Product(sku = "AU-BT-G3", name = "Audífonos Bluetooth G3", category = "Audio", price = 159_900, stock = 25, rating = 4.5,
            description = "Cancelación de ruido activa, 30 horas de batería y carga rápida USB-C."), R.drawable.prod_headphones_g3),
        Seed(Product(sku = "CE-X12-128", name = "Celular X12 128GB", category = "Celulares", price = 1_249_000, stock = 9, rating = 4.6,
            description = "Cámara de 108 MP, pantalla de 6.7\" a 120 Hz y 8 GB de RAM."), R.drawable.prod_phone_x12),
        Seed(Product(sku = "AC-KB-K1", name = "Teclado Mecánico K1", category = "Gaming", price = 289_000, stock = 20, rating = 4.8,
            description = "Switches rojos, iluminación RGB y conexión inalámbrica 2.4 GHz."), R.drawable.prod_keyboard_k1),
        Seed(Product(sku = "GA-CTRL-S", name = "Control Gamer S", category = "Gaming", price = 219_000, stock = 14, rating = 4.4,
            description = "Compatible con PC y celular, vibración dual y batería recargable."), R.drawable.prod_gamepad_s),
        Seed(Product(sku = "AC-PB-20K", name = "Power Bank 20.000 mAh", category = "Accesorios", price = 129_900, stock = 40, rating = 4.3,
            description = "Carga rápida de 22.5 W con dos puertos USB y uno USB-C."), R.drawable.prod_powerbank),
        Seed(Product(sku = "SW-FIT-S", name = "Reloj Inteligente Fit S", category = "Smartwatches", price = 199_900, stock = 30, rating = 4.2,
            description = "Resistente al agua, 100 modos deportivos y notificaciones."), R.drawable.prod_fit_band),
        Seed(Product(sku = "AU-BUDS-M", name = "Earbuds Mini", category = "Audio", price = 99_900, stock = 50, rating = 4.1,
            description = "Audífonos in-ear livianos con estuche de carga."), R.drawable.prod_earbuds_mini)
    )
}
