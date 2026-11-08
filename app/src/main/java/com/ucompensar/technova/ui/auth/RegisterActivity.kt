package com.ucompensar.technova.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.ActivityRegisterBinding

/** Registro de usuarios con aceptación de tratamiento de datos y términos. */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnRegister.setOnClickListener {
            if (validate()) {
                Toast.makeText(this, R.string.msg_register_ok, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        binding.tilName.error = if (binding.etName.text.isNullOrBlank()) required else null
        binding.tilPhone.error = if (binding.etPhone.text.isNullOrBlank()) required else null
        val email = binding.etEmail.text.toString().trim()
        binding.tilEmail.error =
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) getString(R.string.err_email) else null
        binding.tilPassword.error =
            if (binding.etPassword.text.toString().length < 6) getString(R.string.err_password) else null

        val fieldsOk = listOf(binding.tilName, binding.tilPhone, binding.tilEmail, binding.tilPassword)
            .all { it.error == null }
        val accepted = binding.cbHabeasData.isChecked && binding.cbTerms.isChecked
        if (fieldsOk && !accepted) {
            Toast.makeText(this, R.string.err_accept, Toast.LENGTH_LONG).show()
        }
        return fieldsOk && accepted
    }
}
