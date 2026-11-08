package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.databinding.FragmentAdminMetricsBinding
import com.ucompensar.technova.databinding.ItemCategoryProgressBinding

/** Indicadores del negocio y participación de ventas por categoría. */
class AdminMetricsFragment : Fragment() {

    private var _binding: FragmentAdminMetricsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminMetricsBinding.inflate(inflater, container, false)
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
