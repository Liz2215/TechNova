package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.SettingsRepository
import com.ucompensar.technova.data.StoreSettings
import com.ucompensar.technova.databinding.FragmentAdminSettingsBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toast

/** Ajustes generales de la tienda que se guardan en Firestore */
class AdminSettingsFragment : Fragment() {
    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Trae los ajustes guardados y los pone en pantalla */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSave.isEnabled = false
        launchUi {
            val settings = SettingsRepository.get()
            binding.swMaintenance.isChecked = settings.maintenance
            binding.swNotifications.isChecked = settings.notifications
            binding.swNewSellers.isChecked = settings.newSellers
            binding.etShipping.setText(settings.shippingCost.toString())
            binding.cbWompi.isChecked = "epayco" in settings.gateways
            binding.cbPayU.isChecked = "payu" in settings.gateways
            binding.cbMercadoPago.isChecked = "mercadopago" in settings.gateways
            binding.btnSave.isEnabled = true
        }
        binding.btnSave.setOnClickListener { save() }
    }

    /** Revisa el costo de envío y guarda los ajustes */
    private fun save() {
        val shipping = binding.etShipping.text.toString().toLongOrNull()
        binding.tilShipping.error = if (shipping == null) getString(R.string.err_required) else null
        if (shipping == null) return
        val gateways = buildList {
            if (binding.cbWompi.isChecked) add("epayco")
            if (binding.cbPayU.isChecked) add("payu")
            if (binding.cbMercadoPago.isChecked) add("mercadopago")
        }
        val settings = StoreSettings(
            shippingCost = shipping,
            maintenance = binding.swMaintenance.isChecked,
            notifications = binding.swNotifications.isChecked,
            newSellers = binding.swNewSellers.isChecked,
            gateways = gateways
        )
        launchUi {
            SettingsRepository.save(settings)
            requireContext().toast(getString(R.string.msg_settings_saved))
        }
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
