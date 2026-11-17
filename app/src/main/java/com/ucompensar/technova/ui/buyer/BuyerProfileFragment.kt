package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.AuthRepository
import com.ucompensar.technova.data.PqrRepository
import com.ucompensar.technova.data.SecureStore
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.data.UserRepository
import com.ucompensar.technova.databinding.DialogAddressBinding
import com.ucompensar.technova.databinding.DialogPqrBinding
import com.ucompensar.technova.databinding.FragmentBuyerProfileBinding
import com.ucompensar.technova.ui.common.LocationHelper
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.logout
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.common.toastLong
import java.util.Locale

/** Perfil del comprador con sus pedidos dirección PQR y opción de borrar la cuenta */
class BuyerProfileFragment : Fragment() {
    private var _binding: FragmentBuyerProfileBinding? = null
    private val binding get() = _binding!!
    private var addressDialog: DialogAddressBinding? = null
    private var latitude = 0.0
    private var longitude = 0.0

    /** Pide permiso de ubicación para llenar la dirección */
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) captureLocation() else context?.toastLong(getString(R.string.msg_location_denied))
    }

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Muestra los datos del usuario y conecta las opciones */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvUserName.text = Session.current?.name.orEmpty()
        binding.tvUserEmail.text = Session.current?.email.orEmpty()

        binding.optOrders.setOnClickListener { startActivity(Intent(requireContext(), BuyerOrdersActivity::class.java)) }
        binding.optAddresses.setOnClickListener { showAddressDialog() }
        binding.optPqr.setOnClickListener { showPqrDialog() }
        binding.optPrivacy.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_privacy)
                .setMessage(R.string.privacy_text)
                .setPositiveButton(R.string.action_close, null)
                .show()
        }
        binding.optDelete.setOnClickListener { confirmDelete() }
        binding.btnLogout.setOnClickListener { requireActivity().logout() }
    }

    /** Abre una ventana para cambiar la dirección de entrega */
    private fun showAddressDialog() {
        val user = Session.current ?: return
        val dialogBinding = DialogAddressBinding.inflate(layoutInflater)
        addressDialog = dialogBinding
        latitude = user.latitude
        longitude = user.longitude
        dialogBinding.etAddress.setText(user.address)
        dialogBinding.etCity.setText(user.city)
        showCoordinates()
        dialogBinding.btnLocation.setOnClickListener {
            if (LocationHelper.hasPermission(requireContext())) captureLocation()
            else locationPermission.launch(LocationHelper.PERMISSIONS)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_addresses)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val address = dialogBinding.etAddress.text.toString().trim()
                val city = dialogBinding.etCity.text.toString().trim()
                launchUi {
                    UserRepository.saveAddress(address, city, latitude, longitude)
                    requireContext().toast(getString(R.string.msg_address_saved))
                }
            }
            .setOnDismissListener { addressDialog = null }
            .show()
    }

    /** Saca la ubicación con el GPS y la pone en la ventana */
    private fun captureLocation() {
        val dialogBinding = addressDialog ?: return
        dialogBinding.btnLocation.isEnabled = false
        dialogBinding.btnLocation.setText(R.string.msg_locating)
        launchUi {
            try {
                val place = LocationHelper.currentPlace(requireContext())
                latitude = place.latitude
                longitude = place.longitude
                if (place.address.isNotBlank()) dialogBinding.etAddress.setText(place.address)
                if (place.city.isNotBlank()) dialogBinding.etCity.setText(place.city)
                showCoordinates()
            } finally {
                dialogBinding.btnLocation.isEnabled = true
                dialogBinding.btnLocation.setText(R.string.btn_use_location)
            }
        }
    }

    /** Muestra las coordenadas que se encontraron */
    private fun showCoordinates() {
        val dialogBinding = addressDialog ?: return
        if (latitude == 0.0 && longitude == 0.0) return
        dialogBinding.tvLocation.visibility = View.VISIBLE
        dialogBinding.tvLocation.text = getString(
            R.string.label_location, String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
        )
    }

    /** Abre el formulario para mandar una PQR */
    private fun showPqrDialog() {
        val dialogBinding = DialogPqrBinding.inflate(layoutInflater)
        val types = resources.getStringArray(R.array.pqr_types)
        dialogBinding.actType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types))
        dialogBinding.actType.setText(types.first(), false)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_pqr)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_send) { _, _ ->
                val message = dialogBinding.etMessage.text.toString().trim()
                if (message.isEmpty()) {
                    requireContext().toastLong(getString(R.string.err_pqr_empty))
                    return@setPositiveButton
                }
                launchUi {
                    val radicado = PqrRepository.create(dialogBinding.actType.text.toString(), message)
                    requireContext().toastLong(getString(R.string.msg_pqr_sent, radicado))
                }
            }
            .show()
    }

    /** Pregunta si de verdad quiere borrar la cuenta y la borra */
    private fun confirmDelete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_delete)
            .setMessage(R.string.profile_delete_confirm)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                launchUi {
                    AuthRepository.deleteAccount()
                    SecureStore.clear(requireContext())
                    requireContext().toastLong(getString(R.string.msg_account_deleted))
                    requireActivity().logout()
                }
            }
            .show()
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
