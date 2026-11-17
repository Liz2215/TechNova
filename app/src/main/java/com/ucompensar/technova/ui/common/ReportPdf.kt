package com.ucompensar.technova.ui.common

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.ucompensar.technova.data.Order
import com.ucompensar.technova.data.SalesStats
import java.io.File

/** Crea el reporte de ventas en PDF */
object ReportPdf {
    private const val WIDTH = 595
    private const val HEIGHT = 842
    private const val MARGIN = 40f

    /** Arma el PDF y lo guarda para poder compartirlo */
    fun create(context: Context, title: String, orders: List<Order>): Uri {
        val pdf = PdfDocument()
        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, pageNumber).create())
        var canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 18f; typeface = Typeface.DEFAULT_BOLD; color = 0xFF2E2A6B.toInt() }
        val boldPaint = Paint().apply { textSize = 12f; typeface = Typeface.DEFAULT_BOLD }
        val textPaint = Paint().apply { textSize = 11f }
        var y = MARGIN + 10

        /** Escribe una línea y si no cabe pasa a otra página */
        fun line(text: String, paint: Paint = textPaint, gap: Float = 18f) {
            if (y > HEIGHT - MARGIN) {
                pdf.finishPage(page)
                pageNumber++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN + 10
            }
            canvas.drawText(text, MARGIN, y, paint)
            y += gap
        }

        val total = orders.sumOf { it.subtotal }
        line("TechNova — $title", titlePaint, 28f)
        line("Generado: ${System.currentTimeMillis().toDateText()}")
        line("Ventas totales: ${total.toCop()}", boldPaint)
        line("Pedidos: ${orders.size}", boldPaint)
        line("Ticket promedio: ${(if (orders.isEmpty()) 0L else total / orders.size).toCop()}", boldPaint, 28f)

        line("Productos más vendidos", boldPaint)
        SalesStats.topProducts(orders).forEachIndexed { i, (name, units) -> line("${i + 1}. $name — $units unidades") }
        y += 10
        line("Ventas por categoría", boldPaint)
        SalesStats.salesByCategory(orders).forEach { (name, percent) -> line("$name: $percent%") }
        y += 10
        line("Detalle de pedidos", boldPaint)
        orders.forEach { line("#${it.number}  ${it.createdAt.toDateText()}  ${it.customer}  ${it.total.toCop()}") }
        pdf.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "reporte_ventas_technova.pdf")
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }
}
