package com.ucompensar.technova.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.User
import com.ucompensar.technova.databinding.FragmentAdminUsersBinding
import com.ucompensar.technova.databinding.ItemUserBinding

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private var filter: Role? = null

    private val adapter = UserAdapter(
        onEdit = { user -> openForm(user.id) },
        onDelete = { user -> confirmDelete(user) }
    )

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
        binding.fabAdd.setOnClickListener { openForm(null) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        adapter.submit(FakeRepository.users.filter { filter == null || it.role == filter })
    }

    private fun openForm(userId: Int?) {
        val intent = Intent(requireContext(), UserFormActivity::class.java)
        userId?.let { intent.putExtra(UserFormActivity.EXTRA_ID, it) }
        startActivity(intent)
    }

    private fun confirmDelete(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_user_confirm, user.name))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                FakeRepository.users.remove(user)
                refresh()
                Toast.makeText(requireContext(), R.string.msg_user_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class UserAdapter(
        private val onEdit: (User) -> Unit,
        private val onDelete: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.Holder>() {
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
                tvRole.setText(roleLabel(user.role))
                if (user.active) {
                    tvStatus.setText(R.string.status_active)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.tn_status_ok))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_ok)
                } else {
                    tvStatus.setText(R.string.status_suspended)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.tn_status_error))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_error)
                }
                btnEdit.setOnClickListener { onEdit(user) }
                btnDelete.visibility = if (user.role == Role.ADMIN) View.INVISIBLE else View.VISIBLE
                btnDelete.setOnClickListener { onDelete(user) }
            }
        }
    }
}

fun roleLabel(role: Role): Int = when (role) {
    Role.BUYER -> R.string.role_buyer
    Role.SELLER -> R.string.role_seller
    Role.ADMIN -> R.string.role_admin
}
