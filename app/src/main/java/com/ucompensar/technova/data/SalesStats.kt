package com.ucompensar.technova.data

import java.util.Calendar

/**
 * Cálculos de ventas a partir de los pedidos reales guardados en Firestore,
 * para los indicadores y gráficos del vendedor y del administrador.
 */
object SalesStats {

    /** Períodos del reporte de ventas. */
    enum class Period { WEEK, MONTH, YEAR }

    /**
     * Valor vendido en un pedido. Si se indica un vendedor, solo cuenta sus productos;
     * si no, cuenta el subtotal completo del pedido.
     */
    fun amount(order: Order, sellerId: String? = null): Long =
        if (sellerId == null) order.subtotal
        else order.items.filter { it.sellerId == sellerId }.sumOf { it.price * it.quantity }

    /** Pedidos creados dentro del período actual (esta semana, este mes o este año). */
    fun inPeriod(orders: List<Order>, period: Period): List<Order> {
        val start = periodStart(period)
        return orders.filter { it.createdAt >= start }
    }

    /**
     * Ventas del período repartidas en barras (en miles de pesos):
     * semana = 7 días (lunes a domingo), mes = 4 semanas, año = 4 trimestres.
     */
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

    /** Productos más vendidos (nombre y unidades) ordenados de mayor a menor. */
    fun topProducts(orders: List<Order>, limit: Int = 5): List<Pair<String, Int>> =
        orders.flatMap { it.items }.groupBy { it.name }
            .map { (name, items) -> name to items.sumOf { it.quantity } }
            .sortedByDescending { it.second }.take(limit)

    /** Porcentaje de las ventas que corresponde a cada categoría. */
    fun salesByCategory(orders: List<Order>): List<Pair<String, Int>> {
        val byCategory = orders.flatMap { it.items }.groupBy { it.category.ifEmpty { "Otros" } }
            .mapValues { (_, items) -> items.sumOf { it.price * it.quantity } }
        val total = byCategory.values.sum().coerceAtLeast(1)
        return byCategory.map { (name, value) -> name to (value * 100 / total).toInt() }
            .sortedByDescending { it.second }
    }

    /** Fecha (en milisegundos) en que empieza el período actual. */
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
