package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.User
import com.ucompensar.technova.databinding.FragmentAdminUsersBinding
import com.ucompensar.technova.databinding.ItemUserBinding

/** Gestión de cuentas de usuario por rol (RBAC). */
class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private var filter: Role? = null
    private val adapter = UserAdapter { user ->
        user.active = !user.active
        val status = getString(if (user.active) R.string.status_active else R.string.status_suspended)
        Toast.makeText(requireContext(), getString(R.string.msg_user_status, user.name, status), Toast.LENGTH_SHORT).show()
        refresh()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvUsers.adapter = adapter
        binding.chipRole.setOnCheckedStateChangeListener { _, ids ->
            filter = when (ids.firstOrNull()) {
                R.id.chipBuyers -> Role.BUYER
                R.id.chipSellers -> Role.SELLER
                R.id.chipAdmins -> Role.ADMIN
                else -> null
            }
            refresh()
        }
        refresh()
    }

    private fun refresh() {
        adapter.submit(FakeRepository.users.filter { filter == null || it.role == filter })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class UserAdapter(private val onToggle: (User) -> Unit) :
        RecyclerView.Adapter<UserAdapter.Holder>() {

        private var items: List<User> = emptyList()

        fun submit(list: List<User>) {
            items = list
            notifyDataSetChanged()
        }

        class Holder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val user = items[position]
            val ctx = holder.itemView.context
            with(holder.binding) {
                tvName.text = user.name
                tvEmail.text = user.email
                tvRole.setText(
                    when (user.role) {
                        Role.BUYER -> R.string.role_buyer
                        Role.SELLER -> R.string.role_seller
                        Role.ADMIN -> R.string.role_admin
                    }
                )
                if (user.active) {
                    tvStatus.setText(R.string.status_active)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.tn_status_ok))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_ok)
                    btnToggle.setText(R.string.btn_suspend)
                } else {
                    tvStatus.setText(R.string.status_suspended)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.tn_status_error))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_error)
                    btnToggle.setText(R.string.btn_activate)
                }
                // El administrador no puede suspenderse a sí mismo
                btnToggle.visibility = if (user.role == Role.ADMIN) View.GONE else View.VISIBLE
                btnToggle.setOnClickListener { onToggle(user) }
            }
        }
    }
}
