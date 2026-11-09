package com.ucompensar.technova.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.FragmentBuyerProfileBinding
import com.ucompensar.technova.ui.common.logout

class BuyerProfileFragment : Fragment() {
    private var _binding: FragmentBuyerProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentBuyerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvUserName.setText(R.string.demo_user_name)
        binding.tvUserEmail.setText(R.string.demo_user_email)

        val pending = View.OnClickListener {
            Toast.makeText(requireContext(), R.string.msg_not_available, Toast.LENGTH_SHORT).show()
        }
        listOf(binding.optOrders, binding.optAddresses, binding.optPqr, binding.optPrivacy)
            .forEach { it.setOnClickListener(pending) }

        binding.optDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_delete)
                .setMessage(R.string.profile_delete_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete) { _, _ -> requireActivity().logout() }
                .show()
        }
        binding.btnLogout.setOnClickListener { requireActivity().logout() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
