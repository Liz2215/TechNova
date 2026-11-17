package com.ucompensar.technova.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.UserRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.User
import com.ucompensar.technova.databinding.FragmentAdminUsersBinding
import com.ucompensar.technova.databinding.ItemUserBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toast

/** Pantalla donde el administrador ve y maneja todas las cuentas de usuario */
class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private var filter: Role? = null
    private var users: List<User> = emptyList()

    private val adapter = UserAdapter(
        onEdit = { user -> openForm(user.id) },
        onDelete = { user -> confirmDelete(user) }
    )

    /** Carga el diseño de esta pantalla */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la lista de usuarios con su filtro por rol y el botón para crear */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvUsers.adapter = adapter
        binding.chipRole.setOnCheckedStateChangeListener { _, ids ->
            filter = when (ids.firstOrNull()) {
                R.id.chipBuyers -> Role.BUYER
                R.id.chipSellers -> Role.SELLER
                R.id.chipAdmins -> Role.ADMIN
                else -> null
            }
            applyFilter()
        }
        binding.fabAdd.setOnClickListener { openForm(null) }
    }

    /** Vuelve a cargar los usuarios cuando regresamos del formulario */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Trae todos los usuarios de Firestore */
    private fun refresh() {
        launchUi {
            users = UserRepository.getAll()
            applyFilter()
        }
    }

    /** Muestra solo los usuarios del rol que se eligió */
    private fun applyFilter() {
        adapter.submit(users.filter { filter == null || it.role == filter })
    }

    /** Abre el formulario para crear un usuario nuevo o editar uno que ya existe */
    private fun openForm(userId: String?) {
        val intent = Intent(requireContext(), UserFormActivity::class.java)
        userId?.let { intent.putExtra(UserFormActivity.EXTRA_ID, it) }
        startActivity(intent)
    }

    /** Pregunta si de verdad quiere borrar el usuario y lo borra */
    private fun confirmDelete(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_user_confirm, user.name))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                launchUi {
                    UserRepository.delete(user.id)
                    requireContext().toast(getString(R.string.msg_user_deleted))
                    refresh()
                }
            }
            .show()
    }

    /** Limpia la vista cuando se cierra la pantalla */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Adaptador que pinta cada usuario en la lista */
    class UserAdapter(
        private val onEdit: (User) -> Unit,
        private val onDelete: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.Holder>() {
        private var items: List<User> = emptyList()

        /** Cambia los usuarios que se muestran */
        fun submit(list: List<User>) {
            items = list
            notifyDataSetChanged()
        }

        /** Guarda las vistas de una fila */
        class Holder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

        /** Crea una fila nueva */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Dice cuántas filas hay */
        override fun getItemCount() = items.size

        /** Pone los datos del usuario en su fila */
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

/** Devuelve el nombre del rol para mostrarlo */
fun roleLabel(role: Role): Int = when (role) {
    Role.BUYER -> R.string.role_buyer
    Role.SELLER -> R.string.role_seller
    Role.ADMIN -> R.string.role_admin
}
