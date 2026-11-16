package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.databinding.ActivityBuyerOrdersBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showOrderDetails
import com.ucompensar.technova.ui.seller.OrderAdapter

/** Historial de pedidos del comprador leído desde Firestore; al tocar uno se ve su detalle. */
class BuyerOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuyerOrdersBinding
    private val adapter = OrderAdapter(showActions = false, onClick = { showOrderDetails(it) })

    /** Prepara la lista y carga los pedidos del usuario. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvOrders.adapter = adapter

        launchUi {
            val orders = OrderRepository.forBuyer()
            adapter.submit(orders)
            binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
