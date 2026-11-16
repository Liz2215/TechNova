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

/**
 * Gestión de cuentas del administrador: lista los usuarios de Firestore con filtro por rol
 * y permite crearlos, editarlos y eliminarlos. Las cuentas de administrador no se pueden eliminar.
 */
class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private var filter: Role? = null
    private var users: List<User> = emptyList()

    private val adapter = UserAdapter(
        onEdit = { user -> openForm(user.id) },
        onDelete = { user -> confirmDelete(user) }
    )

    /** Crea la vista del fragment a partir de su layout (ViewBinding). */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Prepara la lista, los filtros por rol y el botón para crear usuarios. */
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

    /** Recarga los usuarios al volver del formulario. */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Lee todos los usuarios de Firestore. */
    private fun refresh() {
        launchUi {
            users = UserRepository.getAll()
            applyFilter()
        }
    }

    /** Muestra solo los usuarios del rol elegido en los chips. */
    private fun applyFilter() {
        adapter.submit(users.filter { filter == null || it.role == filter })
    }

    /** Abre el formulario para crear (id null) o editar un usuario. */
    private fun openForm(userId: String?) {
        val intent = Intent(requireContext(), UserFormActivity::class.java)
        userId?.let { intent.putExtra(UserFormActivity.EXTRA_ID, it) }
        startActivity(intent)
    }

    /** Pide confirmación y elimina el perfil del usuario en Firestore. */
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

    /** Libera la referencia a la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Filas de usuarios con nombre, correo, rol, estado y botones de editar y eliminar. */
    class UserAdapter(
        private val onEdit: (User) -> Unit,
        private val onDelete: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.Holder>() {
        private var items: List<User> = emptyList()

        /** Reemplaza los usuarios mostrados. */
        fun submit(list: List<User>) {
            items = list
            notifyDataSetChanged()
        }

        /** Contenedor de las vistas de una fila de la lista. */
        class Holder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

        /** Crea la vista de una fila nueva. */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        /** Cantidad de filas de la lista. */
        override fun getItemCount() = items.size

        /** Llena la fila con los datos del usuario y su estado (activo o suspendido). */
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

/** Texto (recurso) que corresponde a cada rol. */
fun roleLabel(role: Role): Int = when (role) {
    Role.BUYER -> R.string.role_buyer
    Role.SELLER -> R.string.role_seller
    Role.ADMIN -> R.string.role_admin
}
