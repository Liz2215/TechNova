package com.ucompensar.technova.data

import java.util.Calendar

/** Cálculos de ventas para los gráficos y reportes */
object SalesStats {

    /** Periodos del reporte */
    enum class Period { WEEK, MONTH, YEAR }

    /** Calcula cuánto se vendió en un pedido */
    fun amount(order: Order, sellerId: String? = null): Long =
        if (sellerId == null) order.subtotal
        else order.items.filter { it.sellerId == sellerId }.sumOf { it.price * it.quantity }

    /** Trae los pedidos del periodo elegido */
    fun inPeriod(orders: List<Order>, period: Period): List<Order> {
        val start = periodStart(period)
        return orders.filter { it.createdAt >= start }
    }

    /** Reparte las ventas en las barras del gráfico */
    fun chart(orders: List<Order>, period: Period, sellerId: String? = null): List<Int> {
        val buckets = LongArray(if (period == Period.WEEK) 7 else 4)
        val cal = Calendar.getInstance()
        inPeriod(orders, period).forEach { order ->
            cal.timeInMillis = order.createdAt
            val index = when (period) {
                Period.WEEK -> (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                Period.MONTH -> ((cal.get(Calendar.DAY_OF_MONTH) - 1) / 7).coerceAtMost(3)
                Period.YEAR -> cal.get(Calendar.MONTH) / 3
            }
            buckets[index] += amount(order, sellerId)
        }
        return buckets.map { (it / 1000).toInt() }
    }

    /** Saca los productos más vendidos */
    fun topProducts(orders: List<Order>, limit: Int = 5): List<Pair<String, Int>> =
        orders.flatMap { it.items }.groupBy { it.name }
            .map { (name, items) -> name to items.sumOf { it.quantity } }
            .sortedByDescending { it.second }.take(limit)

    /** Calcula el porcentaje de ventas de cada categoría */
    fun salesByCategory(orders: List<Order>): List<Pair<String, Int>> {
        val byCategory = orders.flatMap { it.items }.groupBy { it.category.ifEmpty { "Otros" } }
            .mapValues { (_, items) -> items.sumOf { it.price * it.quantity } }
        val total = byCategory.values.sum().coerceAtLeast(1)
        return byCategory.map { (name, value) -> name to (value * 100 / total).toInt() }
            .sortedByDescending { it.second }
    }

    /** Calcula desde qué fecha empieza el periodo */
    private fun periodStart(period: Period): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        when (period) {
            Period.WEEK -> {
                val daysFromMonday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            }
            Period.MONTH -> cal.set(Calendar.DAY_OF_MONTH, 1)
            Period.YEAR -> cal.set(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
