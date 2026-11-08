package com.ucompensar.technova.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.User
import com.ucompensar.technova.databinding.ActivityUserFormBinding

/** Crear o editar un usuario. Si recibe un ID carga sus datos para editarlos. */
class UserFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserFormBinding
    private var user: User? = null
    private val roles = Role.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val roleNames = resources.getStringArray(R.array.user_roles) // mismo orden que Role
        binding.actRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roleNames))

        user = FakeRepository.findUser(intent.getIntExtra(EXTRA_ID, -1))
        user?.let { u ->
            binding.toolbar.setTitle(R.string.user_form_edit)
            binding.etName.setText(u.name)
            binding.etEmail.setText(u.email)
            binding.actRole.setText(roleNames[roles.indexOf(u.role)], false)
            binding.swActive.isChecked = u.active
        }

        binding.btnSave.setOnClickListener { if (validate()) save(roleNames) }
    }

    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        binding.tilName.error = if (binding.etName.text.isNullOrBlank()) required else null
        val email = binding.etEmail.text.toString().trim()
        binding.tilEmail.error =
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) getString(R.string.err_email) else null
        binding.tilRole.error = if (binding.actRole.text.isNullOrBlank()) required else null
        return listOf(binding.tilName, binding.tilEmail, binding.tilRole).all { it.error == null }
    }

    private fun save(roleNames: Array<String>) {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val role = roles[roleNames.indexOf(binding.actRole.text.toString())]
        val active = binding.swActive.isChecked

        val current = user
        if (current != null) {
            current.name = name
            current.email = email
            current.role = role
            current.active = active
        } else {
            val newId = (FakeRepository.users.maxOfOrNull { it.id } ?: 0) + 1
            FakeRepository.users.add(User(newId, name, email, role, active))
        }
        Toast.makeText(this, R.string.msg_user_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_ID = "user_id"
    }
}
