package com.ucompensar.technova.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Order
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.data.OrderStatus
import com.ucompensar.technova.databinding.FragmentSellerOrdersBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showOrderDetails
import com.ucompensar.technova.ui.common.toast

/** Pedidos del vendedor donde puede filtrar y cambiar el estado */
class SellerOrdersFragment : Fragment() {
    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!
    private var filter: OrderStatus? = null
    private var orders: List<Order> = emptyList()

    private val adapter = OrderAdapter(
        showActions = true,
        onStatusSelected = { order, status -> changeStatus(order, status) },
        onClick = { context?.showOrderDetails(it) }
    )

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la lista y los filtros y carga los pedidos */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvOrders.adapter = adapter
        binding.chipStatus.setOnCheckedStateChangeListener { _, ids ->
            filter = when (ids.firstOrNull()) {
                R.id.chipPending -> OrderStatus.PENDING
                R.id.chipShipped -> OrderStatus.SHIPPED
                R.id.chipDelivered -> OrderStatus.DELIVERED
                else -> null
            }
            applyFilter()
        }
        launchUi {
            orders = OrderRepository.forCurrentRole()
            applyFilter()
        }
    }

    /** Guarda el nuevo estado del pedido */
    private fun changeStatus(order: Order, status: OrderStatus) {
        launchUi {
            OrderRepository.updateStatus(order, status)
            requireContext().toast(getString(R.string.msg_status_changed, order.number, getString(status.label())))
            applyFilter()
        }
    }

    /** Muestra solo los pedidos del estado que se eligió */
    private fun applyFilter() {
        adapter.submit(orders.filter { filter == null || it.status == filter })
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
