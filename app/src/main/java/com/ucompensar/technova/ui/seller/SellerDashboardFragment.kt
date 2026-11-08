package com.ucompensar.technova.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.OrderStatus
import com.ucompensar.technova.databinding.FragmentSellerDashboardBinding
import com.ucompensar.technova.ui.common.drawBarChart
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

        drawBarChart(requireContext(), binding.chartBars, binding.chartLabels,
            FakeRepository.weeklySales, resources.getStringArray(R.array.week_days).toList())

        val adapter = OrderAdapter(showActions = false)
        adapter.submit(FakeRepository.orders.take(3))
        binding.rvRecentOrders.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), ProductFormActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
