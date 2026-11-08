package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.databinding.FragmentAdminDashboardBinding
import com.ucompensar.technova.ui.seller.OrderAdapter

/** Dashboard principal del administrador: indicadores del negocio y pedidos recientes. */
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val users = FakeRepository.users
        with(binding.statSellers) {
            ivStatIcon.setImageResource(R.drawable.ic_users)
            tvStatValue.text = users.count { it.role == Role.SELLER && it.active }.toString()
            tvStatLabel.setText(R.string.stat_active_sellers)
        }
        with(binding.statClients) {
            ivStatIcon.setImageResource(R.drawable.ic_person)
            tvStatValue.text = users.count { it.role == Role.BUYER }.toString()
            tvStatLabel.setText(R.string.stat_registered_clients)
        }
        with(binding.statOrders) {
            ivStatIcon.setImageResource(R.drawable.ic_orders)
            tvStatValue.text = FakeRepository.orders.size.toString()
            tvStatLabel.setText(R.string.stat_monthly_orders)
        }
        with(binding.statAppStatus) {
            ivStatIcon.setImageResource(R.drawable.ic_check_circle)
            ivStatIcon.setBackgroundResource(R.drawable.bg_badge_ok)
            tvStatValue.setText(R.string.value_app_ok)
            tvStatValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.tn_status_ok))
            tvStatLabel.setText(R.string.stat_app_status)
        }

        // Últimos pedidos de toda la tienda (solo lectura)
        val adapter = OrderAdapter(showActions = false)
        adapter.submit(FakeRepository.orders.take(4))
        binding.rvRecentOrders.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
