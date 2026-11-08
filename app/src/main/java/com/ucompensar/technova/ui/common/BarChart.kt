package com.ucompensar.technova.ui.common

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.ucompensar.technova.R

/**
 * Dibuja un gráfico de barras sencillo con Views:
 * una barra (drawable bg_chart_bar) por valor, con altura proporcional al valor más alto.
 * Lo usan el panel del Vendedor y el reporte de ventas del Administrador.
 */
fun drawBarChart(context: Context, bars: LinearLayout, labels: LinearLayout, values: List<Int>, names: List<String>) {
    bars.removeAllViews()
    labels.removeAllViews()
    val max = values.max().toFloat()
    val chartHeight = context.resources.getDimensionPixelSize(R.dimen.chart_height)
    val barWidth = context.resources.getDimensionPixelSize(R.dimen.chart_bar_width)

    values.forEachIndexed { index, value ->
        val cell = LinearLayout(context).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }
        val bar = View(context).apply {
            setBackgroundResource(R.drawable.bg_chart_bar)
            layoutParams = LinearLayout.LayoutParams(barWidth, (chartHeight * value / max).toInt())
        }
        cell.addView(bar)
        bars.addView(cell)

        val label = TextView(context).apply {
            text = names[index]
            gravity = Gravity.CENTER
            TextViewCompat.setTextAppearance(this, R.style.Text_TechNova_Caption)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        labels.addView(label)
    }
}
