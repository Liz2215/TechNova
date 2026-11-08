package com.ucompensar.technova.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.FragmentSellerProfileBinding
import com.ucompensar.technova.ui.common.logout
import com.ucompensar.technova.ui.common.toCompactCop

/** Perfil del vendedor: datos de la tienda, indicadores y opciones de cuenta. */
class SellerProfileFragment : Fragment() {

    private var _binding: FragmentSellerProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.statRating) {
            ivStatIcon.setImageResource(R.drawable.ic_star)
            tvStatValue.setText(R.string.seller_rating)
            tvStatLabel.setText(R.string.label_rating_title)
        }
        with(binding.statProducts) {
            ivStatIcon.setImageResource(R.drawable.ic_products)
            tvStatValue.text = FakeRepository.products.size.toString()
            tvStatLabel.setText(R.string.label_products_title)
        }
        with(binding.statSales) {
            ivStatIcon.setImageResource(R.drawable.ic_chart)
            tvStatValue.text = FakeRepository.orders.sumOf { it.total }.toCompactCop()
            tvStatLabel.setText(R.string.label_sales_title)
        }

        val pending = View.OnClickListener {
            Toast.makeText(requireContext(), R.string.msg_not_available, Toast.LENGTH_SHORT).show()
        }
        listOf(binding.optStore, binding.optPayout, binding.optNotifications, binding.optHelp)
            .forEach { it.setOnClickListener(pending) }
        binding.btnLogout.setOnClickListener { requireActivity().logout() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
