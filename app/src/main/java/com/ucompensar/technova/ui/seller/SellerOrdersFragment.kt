package com.ucompensar.technova.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.OrderStatus
import com.ucompensar.technova.databinding.FragmentSellerOrdersBinding

/** Pedidos del vendedor con filtro por estado. */
class SellerOrdersFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!
    private var filter: OrderStatus? = null

    private val adapter = OrderAdapter(showActions = true) { order ->
        val status = getString(order.status.label())
        Toast.makeText(requireContext(), getString(R.string.msg_status_changed, order.id, status), Toast.LENGTH_SHORT).show()
        refresh()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvOrders.adapter = adapter
        binding.chipStatus.setOnCheckedStateChangeListener { _, ids ->
            filter = when (ids.firstOrNull()) {
                R.id.chipPending -> OrderStatus.PENDING
                R.id.chipShipped -> OrderStatus.SHIPPED
                R.id.chipDelivered -> OrderStatus.DELIVERED
                else -> null
            }
            refresh()
        }
        refresh()
    }

    private fun refresh() {
        adapter.submit(FakeRepository.orders.filter { filter == null || it.status == filter })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
