package com.ucompensar.technova.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.OrderStatus
import com.ucompensar.technova.databinding.FragmentSellerDashboardBinding
import com.ucompensar.technova.ui.common.toCompactCop

/** Resumen del vendedor: indicadores, ventas de la semana y pedidos recientes. */
class SellerDashboardFragment : Fragment() {

    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val totalSales = FakeRepository.orders.sumOf { it.total }
        val toShip = FakeRepository.orders.count { it.status == OrderStatus.PENDING }

        with(binding.statSales) {
            ivStatIcon.setImageResource(R.drawable.ic_chart)
            tvStatValue.text = totalSales.toCompactCop()
            tvStatLabel.setText(R.string.stat_sales)
        }
        with(binding.statClients) {
            ivStatIcon.setImageResource(R.drawable.ic_users)
            tvStatValue.text = "12"
            tvStatLabel.setText(R.string.stat_new_clients)
        }
        with(binding.statToShip) {
            ivStatIcon.setImageResource(R.drawable.ic_shipping)
            tvStatValue.text = toShip.toString()
            tvStatLabel.setText(R.string.stat_to_ship)
        }

        drawChart()

        val adapter = OrderAdapter(showActions = false)
        adapter.submit(FakeRepository.orders.take(3))
        binding.rvRecentOrders.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), ProductFormActivity::class.java))
        }
    }

    /** Dibuja un gráfico de barras simple usando Views y el drawable bg_chart_bar. */
    private fun drawChart() {
        val sales = FakeRepository.weeklySales
        val days = resources.getStringArray(R.array.week_days)
        val max = sales.max().toFloat()
        val chartHeight = resources.getDimensionPixelSize(R.dimen.chart_height)
        val barWidth = resources.getDimensionPixelSize(R.dimen.chart_bar_width)

        sales.forEachIndexed { index, value ->
            val cell = LinearLayout(requireContext()).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            }
            val bar = View(requireContext()).apply {
                setBackgroundResource(R.drawable.bg_chart_bar)
                layoutParams = LinearLayout.LayoutParams(barWidth, (chartHeight * value / max).toInt())
            }
            cell.addView(bar)
            binding.chartBars.addView(cell)

            val label = TextView(requireContext()).apply {
                text = days[index]
                gravity = Gravity.CENTER
                TextViewCompat.setTextAppearance(this, R.style.Text_TechNova_Caption)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            binding.chartLabels.addView(label)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
