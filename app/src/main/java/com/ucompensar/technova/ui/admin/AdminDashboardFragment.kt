package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.SalesStats
import com.ucompensar.technova.data.SettingsRepository
import com.ucompensar.technova.data.UserRepository
import com.ucompensar.technova.databinding.FragmentAdminDashboardBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showOrderDetails
import com.ucompensar.technova.ui.common.toastLong
import com.ucompensar.technova.ui.seller.OrderAdapter

/**
 * Inicio del administrador con datos reales: vendedores activos, clientes registrados,
 * pedidos del mes, estado de la tienda (operando o en mantenimiento) y pedidos recientes.
 * La primera vez que entra con la base vacía, carga los productos de ejemplo.
 */
class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val adapter = OrderAdapter(showActions = false, onClick = { context?.showOrderDetails(it) })

    /** Crea la vista del fragment a partir de su layout (ViewBinding). */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara las tarjetas de indicadores y la lista de pedidos recientes. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statSellers.ivStatIcon.setImageResource(R.drawable.ic_users)
        binding.statSellers.tvStatLabel.setText(R.string.stat_active_sellers)
        binding.statClients.ivStatIcon.setImageResource(R.drawable.ic_person)
        binding.statClients.tvStatLabel.setText(R.string.stat_registered_clients)
        binding.statOrders.ivStatIcon.setImageResource(R.drawable.ic_orders)
        binding.statOrders.tvStatLabel.setText(R.string.stat_monthly_orders)
        binding.statAppStatus.ivStatIcon.setImageResource(R.drawable.ic_check_circle)
        binding.statAppStatus.ivStatIcon.setBackgroundResource(R.drawable.bg_badge_ok)
        binding.statAppStatus.tvStatLabel.setText(R.string.stat_app_status)
        binding.rvRecentOrders.adapter = adapter
    }

    /** Carga usuarios, pedidos y configuración desde Firestore y calcula los indicadores. */
    override fun onResume() {
        super.onResume()
        launchUi {
            val seeded = ProductRepository.seedIfEmpty(requireContext())
            if (seeded > 0) requireContext().toastLong(getString(R.string.msg_seeded, seeded))

            val users = UserRepository.getAll()
            val orders = OrderRepository.all()
            val maintenance = SettingsRepository.get().maintenance
            binding.statSellers.tvStatValue.text = users.count { it.role == Role.SELLER && it.active }.toString()
            binding.statClients.tvStatValue.text = users.count { it.role == Role.BUYER }.toString()
            binding.statOrders.tvStatValue.text = SalesStats.inPeriod(orders, SalesStats.Period.MONTH).size.toString()
            binding.statAppStatus.tvStatValue.setText(if (maintenance) R.string.value_app_maintenance else R.string.value_app_ok)
            binding.statAppStatus.tvStatValue.setTextColor(
                ContextCompat.getColor(requireContext(), if (maintenance) R.color.tn_status_pending else R.color.tn_status_ok)
            )
            adapter.submit(orders.take(4))
        }
    }

    /** Libera la referencia a la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
