package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.User
import com.ucompensar.technova.data.UserRepository
import com.ucompensar.technova.databinding.ActivityUserFormBinding
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.common.toastLong

/** Formulario del administrador para crear o editar usuarios */
class UserFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserFormBinding
    private var user: User? = null
    private val roles = Role.values()
    private lateinit var roleNames: Array<String>

    /** Esta función es para crear o editar un usuario y si llega un id carga sus datos */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        roleNames = resources.getStringArray(R.array.user_roles)
        binding.actRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roleNames))
        binding.btnSave.setOnClickListener { if (validate()) save() }

        val id = intent.getStringExtra(EXTRA_ID) ?: return
        binding.btnSave.isEnabled = false
        launchUi {
            user = UserRepository.get(id)
            user?.let { fill(it) }
            binding.btnSave.isEnabled = true
        }
    }

    /** Pone en el formulario los datos del usuario que se va a editar */
    private fun fill(u: User) {
        binding.toolbar.setTitle(R.string.user_form_edit)
        binding.etName.setText(u.name)
        binding.etEmail.setText(u.email)
        binding.etEmail.isEnabled = false
        binding.etPhone.setText(u.phone)
        binding.tilPassword.visibility = View.GONE
        binding.actRole.setText(roleNames[roles.indexOf(u.role)], false)
        binding.swActive.isChecked = u.active
    }

    /** Revisa que los datos del usuario estén bien escritos */
    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        binding.tilName.error = if (binding.etName.text.isNullOrBlank()) required else null
        val email = binding.etEmail.text.toString().trim()
        binding.tilEmail.error =
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) getString(R.string.err_email) else null
        binding.tilRole.error = if (binding.actRole.text.isNullOrBlank()) required else null
        binding.tilPassword.error =
            if (user == null && binding.etPassword.text.toString().length < 6) getString(R.string.err_password) else null
        return listOf(binding.tilName, binding.tilEmail, binding.tilRole, binding.tilPassword).all { it.error == null }
    }

    /** Guarda el usuario nuevo o los cambios en Firestore */
    private fun save() {
        val role = roles[roleNames.indexOf(binding.actRole.text.toString()).coerceAtLeast(0)]
        val data = User(
            id = user?.id.orEmpty(),
            name = binding.etName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            role = role,
            active = binding.swActive.isChecked,
            address = user?.address.orEmpty(),
            city = user?.city.orEmpty(),
            latitude = user?.latitude ?: 0.0,
            longitude = user?.longitude ?: 0.0
        )
        binding.btnSave.isEnabled = false
        launchUi {
            try {
                if (user == null) UserRepository.createByAdmin(data, binding.etPassword.text.toString())
                else UserRepository.update(data)
                toast(getString(R.string.msg_user_saved))
                finish()
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                toastLong(errorMessage(e))
            }
        }
    }

    companion object {
        const val EXTRA_ID = "user_id"
    }
}
