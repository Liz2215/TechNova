package com.ucompensar.technova.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Db
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.data.OrderStatus
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.data.SalesStats
import com.ucompensar.technova.databinding.FragmentSellerDashboardBinding
import com.ucompensar.technova.ui.common.drawBarChart
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showOrderDetails
import com.ucompensar.technova.ui.common.toCompactCop
import com.ucompensar.technova.ui.common.toastLong

/** Pantalla de inicio del vendedor con sus ventas y sus pedidos recientes */
class SellerDashboardFragment : Fragment() {
    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!
    private val adapter = OrderAdapter(showActions = false, onClick = { context?.showOrderDetails(it) })

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Pone los íconos de las tarjetas y el botón para agregar productos */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statSales.ivStatIcon.setImageResource(R.drawable.ic_chart)
        binding.statSales.tvStatLabel.setText(R.string.stat_sales)
        binding.statClients.ivStatIcon.setImageResource(R.drawable.ic_users)
        binding.statClients.tvStatLabel.setText(R.string.stat_new_clients)
        binding.statToShip.ivStatIcon.setImageResource(R.drawable.ic_shipping)
        binding.statToShip.tvStatLabel.setText(R.string.stat_to_ship)
        binding.rvRecentOrders.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), ProductFormActivity::class.java))
        }
    }

    /** Cada vez que se abre trae los pedidos del vendedor y calcula sus números */
    override fun onResume() {
        super.onResume()
        launchUi {
            val seeded = ProductRepository.seedIfEmpty(requireContext())
            if (seeded > 0) requireContext().toastLong(getString(R.string.msg_seeded, seeded))

            val sellerId = Db.uid.orEmpty()
            val orders = OrderRepository.forSeller(sellerId)
            binding.statSales.tvStatValue.text = orders.sumOf { SalesStats.amount(it, sellerId) }.toCompactCop()
            binding.statClients.tvStatValue.text = orders.map { it.buyerId }.distinct().size.toString()
            binding.statToShip.tvStatValue.text = orders.count { it.status == OrderStatus.PENDING }.toString()

            drawBarChart(requireContext(), binding.chartBars, binding.chartLabels,
                SalesStats.chart(orders, SalesStats.Period.WEEK, sellerId),
                resources.getStringArray(R.array.week_days).toList())
            adapter.submit(orders.take(3))
        }
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
