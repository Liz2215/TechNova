package com.ucompensar.technova.ui.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pone un número en formato de pesos colombianos */
fun Long.toCop(): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return "$ " + format.format(this)
}

/** Pone un número en pesos de forma corta como 1.2 M */
fun Long.toCompactCop(): String = when {
    this >= 1_000_000 -> String.format(Locale("es", "CO"), "$ %.1f M", this / 1_000_000.0)
    this >= 1_000 -> "$ ${this / 1_000} mil"
    else -> toCop()
}

/** Pone una fecha en formato día mes y año */
fun Long.toDateText(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO")).format(Date(this))
