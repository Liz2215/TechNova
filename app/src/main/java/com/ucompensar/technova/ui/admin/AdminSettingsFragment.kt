package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.FragmentAdminSettingsBinding

class AdminSettingsFragment : Fragment() {
    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etShipping.setText(R.string.default_shipping)
        binding.btnSave.setOnClickListener {
            binding.tilShipping.error =
                if (binding.etShipping.text.isNullOrBlank()) getString(R.string.err_required) else null
            if (binding.tilShipping.error == null) {
                Toast.makeText(requireContext(), R.string.msg_settings_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
