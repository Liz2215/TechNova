package com.ucompensar.technova.ui.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatea un valor en pesos colombianos, por ejemplo "$ 389.900". */
fun Long.toCop(): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return "$ " + format.format(this)
}

/** Formatea un valor en pesos de forma corta para indicadores, por ejemplo "$ 1.2 M" o "$ 450 mil". */
fun Long.toCompactCop(): String = when {
    this >= 1_000_000 -> String.format(Locale("es", "CO"), "$ %.1f M", this / 1_000_000.0)
    this >= 1_000 -> "$ ${this / 1_000} mil"
    else -> toCop()
}

/** Formatea una fecha guardada en milisegundos, por ejemplo "15/11/2026 20:30". */
fun Long.toDateText(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO")).format(Date(this))
