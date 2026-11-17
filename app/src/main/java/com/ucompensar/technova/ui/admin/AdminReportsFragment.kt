package com.ucompensar.technova.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Order
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.data.SalesStats
import com.ucompensar.technova.databinding.FragmentAdminReportsBinding
import com.ucompensar.technova.databinding.ItemCategoryProgressBinding
import com.ucompensar.technova.databinding.ItemTopProductBinding
import com.ucompensar.technova.ui.common.ReportPdf
import com.ucompensar.technova.ui.common.drawBarChart
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toCompactCop
import com.ucompensar.technova.ui.common.toastLong

/** Reporte de ventas del administrador hecho con los pedidos reales de Firestore */
class AdminReportsFragment : Fragment() {
    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private var orders: List<Order> = emptyList()
    private var period = SalesStats.Period.WEEK

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara las tarjetas y los botones de semana mes y año */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statTotal.ivStatIcon.setImageResource(R.drawable.ic_chart)
        binding.statTotal.tvStatLabel.setText(R.string.stat_total_sales)
        binding.statOrders.ivStatIcon.setImageResource(R.drawable.ic_orders)
        binding.statOrders.tvStatLabel.setText(R.string.stat_orders)
        binding.statTicket.ivStatIcon.setImageResource(R.drawable.ic_credit_card)
        binding.statTicket.tvStatLabel.setText(R.string.stat_avg_ticket)

        binding.chipPeriod.setOnCheckedStateChangeListener { _, ids ->
            period = when (ids.firstOrNull()) {
                R.id.chipMonth -> SalesStats.Period.MONTH
                R.id.chipYear -> SalesStats.Period.YEAR
                else -> SalesStats.Period.WEEK
            }
            showPeriod()
        }
        binding.btnExport.setOnClickListener { exportPdf() }

        launchUi {
            orders = OrderRepository.all()
            showPeriod()
        }
    }

    /** Muestra los números y el gráfico del periodo que se eligió */
    private fun showPeriod() {
        val inPeriod = SalesStats.inPeriod(orders, period)
        val total = inPeriod.sumOf { it.subtotal }
        val labels = when (period) {
            SalesStats.Period.MONTH -> R.array.month_weeks
            SalesStats.Period.YEAR -> R.array.year_quarters
            SalesStats.Period.WEEK -> R.array.week_days
        }
        binding.statTotal.tvStatValue.text = total.toCompactCop()
        binding.statOrders.tvStatValue.text = inPeriod.size.toString()
        binding.statTicket.tvStatValue.text = (if (inPeriod.isEmpty()) 0L else total / inPeriod.size).toCompactCop()
        drawBarChart(requireContext(), binding.chartBars, binding.chartLabels,
            SalesStats.chart(orders, period), resources.getStringArray(labels).toList())
        fillTopProducts(inPeriod)
        fillCategories(inPeriod)
    }

    /** Llena la lista de los productos que más se venden */
    private fun fillTopProducts(list: List<Order>) {
        binding.layoutTopProducts.removeAllViews()
        SalesStats.topProducts(list).forEachIndexed { index, (name, units) ->
            val row = ItemTopProductBinding.inflate(layoutInflater, binding.layoutTopProducts, true)
            row.tvRank.text = (index + 1).toString()
            row.tvName.text = name
            row.tvUnits.text = getString(R.string.label_units_sold, units)
        }
    }

    /** Llena las barras de ventas por categoría */
    private fun fillCategories(list: List<Order>) {
        binding.layoutCategories.removeAllViews()
        SalesStats.salesByCategory(list).forEach { (category, percent) ->
            val row = ItemCategoryProgressBinding.inflate(layoutInflater, binding.layoutCategories, true)
            row.tvCategory.text = category
            row.tvPercent.text = getString(R.string.label_percent, percent)
            val params = row.barFill.layoutParams as LinearLayout.LayoutParams
            params.weight = percent.toFloat().coerceAtLeast(1f)
            row.barFill.layoutParams = params
        }
    }

    /** Crea el PDF del reporte y deja compartirlo o guardarlo */
    private fun exportPdf() {
        try {
            val title = getString(R.string.reports_title) + " · " + when (period) {
                SalesStats.Period.WEEK -> getString(R.string.period_week)
                SalesStats.Period.MONTH -> getString(R.string.period_month)
                SalesStats.Period.YEAR -> getString(R.string.period_year)
            }
            val uri = ReportPdf.create(requireContext(), title, SalesStats.inPeriod(orders, period))
            val share = Intent(Intent.ACTION_SEND).setType("application/pdf")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(share, getString(R.string.btn_export_report)))
        } catch (e: Exception) {
            requireContext().toastLong(requireContext().errorMessage(e))
        }
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
