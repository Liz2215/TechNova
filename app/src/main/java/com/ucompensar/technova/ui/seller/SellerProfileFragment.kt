package com.ucompensar.technova.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Db
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.data.SalesStats
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.data.UserRepository
import com.ucompensar.technova.databinding.DialogStoreBinding
import com.ucompensar.technova.databinding.FragmentSellerProfileBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.logout
import com.ucompensar.technova.ui.common.toCompactCop
import com.ucompensar.technova.ui.common.toast
import java.util.Locale

/** Perfil del vendedor con sus datos y opciones */
class SellerProfileFragment : Fragment() {
    private var _binding: FragmentSellerProfileBinding? = null
    private val binding get() = _binding!!

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSellerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Muestra los datos del vendedor y calcula sus números */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showUser()
        binding.statRating.ivStatIcon.setImageResource(R.drawable.ic_star)
        binding.statRating.tvStatLabel.setText(R.string.label_rating_title)
        binding.statProducts.ivStatIcon.setImageResource(R.drawable.ic_products)
        binding.statProducts.tvStatLabel.setText(R.string.label_products_title)
        binding.statSales.ivStatIcon.setImageResource(R.drawable.ic_chart)
        binding.statSales.tvStatLabel.setText(R.string.label_sales_title)

        launchUi {
            val sellerId = Db.uid.orEmpty()
            val products = ProductRepository.getBySeller(sellerId)
            val rated = products.filter { it.rating > 0 }
            binding.statRating.tvStatValue.text =
                if (rated.isEmpty()) "–" else String.format(Locale("es", "CO"), "%.1f", rated.map { it.rating }.average())
            binding.statProducts.tvStatValue.text = products.size.toString()
            binding.statSales.tvStatValue.text =
                OrderRepository.forSeller(sellerId).sumOf { SalesStats.amount(it, sellerId) }.toCompactCop()
        }

        binding.optStore.setOnClickListener { editStore() }
        binding.optPayout.setOnClickListener { info(R.string.seller_payout, R.string.seller_payout_info) }
        binding.optNotifications.setOnClickListener { info(R.string.seller_notifications, R.string.seller_notifications_info) }
        binding.optHelp.setOnClickListener { info(R.string.seller_help, R.string.seller_help_info) }
        binding.btnLogout.setOnClickListener { requireActivity().logout() }
    }

    /** Pone el nombre y el correo del vendedor */
    private fun showUser() {
        binding.tvStoreName.text = Session.current?.name.orEmpty()
        binding.tvStoreEmail.text = Session.current?.email.orEmpty()
    }

    /** Abre una ventana para cambiar el nombre de la tienda y el teléfono */
    private fun editStore() {
        val user = Session.current ?: return
        val dialogBinding = DialogStoreBinding.inflate(layoutInflater)
        dialogBinding.etName.setText(user.name)
        dialogBinding.etPhone.setText(user.phone)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.seller_store_data)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val name = dialogBinding.etName.text.toString().trim().ifEmpty { user.name }
                val phone = dialogBinding.etPhone.text.toString().trim()
                launchUi {
                    UserRepository.update(user.copy(name = name, phone = phone))
                    showUser()
                    requireContext().toast(getString(R.string.msg_store_saved))
                }
            }
            .show()
    }

    /** Abre una ventana con información */
    private fun info(title: Int, message: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.action_close, null)
            .show()
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
