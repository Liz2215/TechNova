package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.FragmentAdminReportsBinding
import com.ucompensar.technova.databinding.ItemCategoryProgressBinding
import com.ucompensar.technova.databinding.ItemTopProductBinding
import com.ucompensar.technova.ui.common.drawBarChart
import com.ucompensar.technova.ui.common.toCompactCop

class AdminReportsFragment : Fragment() {
    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statTotal.ivStatIcon.setImageResource(R.drawable.ic_chart)
        binding.statTotal.tvStatLabel.setText(R.string.stat_total_sales)
        binding.statOrders.ivStatIcon.setImageResource(R.drawable.ic_orders)
        binding.statOrders.tvStatLabel.setText(R.string.stat_orders)
        binding.statTicket.ivStatIcon.setImageResource(R.drawable.ic_credit_card)
        binding.statTicket.tvStatLabel.setText(R.string.stat_avg_ticket)

        binding.chipPeriod.setOnCheckedStateChangeListener { _, ids ->
            showPeriod(
                when (ids.firstOrNull()) {
                    R.id.chipMonth -> "month"
                    R.id.chipYear -> "year"
                    else -> "week"
                }
            )
        }
        showPeriod("week")
        fillTopProducts()
        fillCategories()

        binding.btnExport.setOnClickListener {
            Toast.makeText(requireContext(), R.string.msg_not_available, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPeriod(period: String) {
        val values = FakeRepository.salesByPeriod.getValue(period)
        val orders = FakeRepository.ordersByPeriod.getValue(period)
        val total = values.sum() * 1_000L
        val labels = when (period) {
            "month" -> R.array.month_weeks
            "year" -> R.array.year_quarters
            else -> R.array.week_days
        }

        binding.statTotal.tvStatValue.text = total.toCompactCop()
        binding.statOrders.tvStatValue.text = orders.toString()
        binding.statTicket.tvStatValue.text = (total / orders).toCompactCop()
        drawBarChart(requireContext(), binding.chartBars, binding.chartLabels,
            values, resources.getStringArray(labels).toList())
    }

    private fun fillTopProducts() {
        FakeRepository.topProducts.forEachIndexed { index, (name, units) ->
            val row = ItemTopProductBinding.inflate(layoutInflater, binding.layoutTopProducts, true)
            row.tvRank.text = (index + 1).toString()
            row.tvName.text = name
            row.tvUnits.text = getString(R.string.label_units_sold, units)
        }
    }

    private fun fillCategories() {
        FakeRepository.salesByCategory.forEach { (category, percent) ->
            val row = ItemCategoryProgressBinding.inflate(layoutInflater, binding.layoutCategories, true)
            row.tvCategory.text = category
            row.tvPercent.text = getString(R.string.label_percent, percent)
            val params = row.barFill.layoutParams as LinearLayout.LayoutParams
            params.weight = percent.toFloat()
            row.barFill.layoutParams = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
