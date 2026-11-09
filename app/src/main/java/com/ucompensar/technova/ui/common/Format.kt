package com.ucompensar.technova.ui.common

import java.text.NumberFormat
import java.util.Locale

fun Long.toCop(): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return "$ " + format.format(this)
}

fun Long.toCompactCop(): String = when {
    this >= 1_000_000 -> String.format(Locale("es", "CO"), "$ %.1f M", this / 1_000_000.0)
    this >= 1_000 -> "$ ${this / 1_000} mil"
    else -> toCop()
}
