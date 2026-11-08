package com.ucompensar.technova.ui.common

import java.text.NumberFormat
import java.util.Locale

/** Convierte un valor en pesos colombianos con el formato $ 1.249.000 */
fun Long.toCop(): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return "$ " + format.format(this)
}

/** Formato corto para indicadores: $ 2,9 M o $ 850 mil */
fun Long.toCompactCop(): String = when {
    this >= 1_000_000 -> String.format(Locale("es", "CO"), "$ %.1f M", this / 1_000_000.0)
    this >= 1_000 -> "$ ${this / 1_000} mil"
    else -> toCop()
}
