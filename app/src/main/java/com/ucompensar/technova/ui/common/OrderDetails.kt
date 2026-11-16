package com.ucompensar.technova.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Order
import com.ucompensar.technova.data.PaymentStatus
import com.ucompensar.technova.ui.seller.label

/**
 * Muestra en un diálogo el detalle de un pedido: productos, totales, pago, estado y
 * dirección de entrega. Si el pedido tiene coordenadas GPS, ofrece abrirlas en Google Maps.
 */
fun Context.showOrderDetails(order: Order) {
    val lines = buildString {
        order.items.forEach { appendLine("• ${it.quantity} x ${it.name} — ${(it.price * it.quantity).toCop()}") }
        appendLine()
        appendLine(getString(R.string.detail_subtotal, order.subtotal.toCop()))
        appendLine(getString(R.string.detail_shipping, order.shipping.toCop()))
        appendLine(getString(R.string.detail_total, order.total.toCop()))
        appendLine()
        appendLine(getString(R.string.detail_date, order.createdAt.toDateText()))
        appendLine(getString(R.string.detail_customer, order.customer))
        appendLine(getString(R.string.detail_status, getString(order.status.label())))
        appendLine(getString(R.string.detail_payment, order.paymentMethod, getString(paymentLabel(order.paymentStatus))))
        if (order.paymentRef.isNotEmpty()) appendLine(getString(R.string.detail_payment_ref, order.paymentRef))
        append(getString(R.string.detail_address, order.address, order.city))
    }
    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.label_order_number, order.number))
        .setMessage(lines)
        .setPositiveButton(R.string.action_close, null)
    if (order.latitude != 0.0 || order.longitude != 0.0) {
        dialog.setNeutralButton(R.string.action_view_map) { _, _ ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LocationHelper.mapsUrl(order.latitude, order.longitude))))
        }
    }
    dialog.show()
}

/** Texto (recurso) de cada estado de pago. */
fun paymentLabel(status: PaymentStatus): Int = when (status) {
    PaymentStatus.APPROVED -> R.string.payment_approved
    PaymentStatus.REJECTED -> R.string.payment_rejected
    PaymentStatus.PENDING -> R.string.payment_pending
}
