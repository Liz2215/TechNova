package com.ucompensar.technova.ui.auth

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.AuthRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.data.SecureStore
import com.ucompensar.technova.databinding.ActivityRegisterBinding
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.openPanelFor
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.common.toastLong

/**
 * Registro de cuentas nuevas como Comprador o Vendedor. Crea el usuario en Firebase
 * Authentication y su perfil en Firestore, y exige aceptar el tratamiento de datos
 * (Ley 1581 de 2012) y los términos (Ley 527 de 1999).
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    /** Prepara la pantalla y el botón de crear cuenta. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { if (validate()) register() }
    }

    /** Revisa los campos obligatorios, el formato del correo, la contraseña y las autorizaciones. */
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
        if (fieldsOk && !accepted) toastLong(getString(R.string.err_accept))
        return fieldsOk && accepted
    }

    /** Crea la cuenta con el rol elegido y entra directamente a su panel. */
    private fun register() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val role = if (binding.rbSeller.isChecked) Role.SELLER else Role.BUYER

        binding.btnRegister.isEnabled = false
        binding.btnRegister.setText(R.string.msg_loading)
        launchUi {
            try {
                val user = AuthRepository.register(name, email, phone, password, role)
                SecureStore.saveCredentials(this@RegisterActivity, email, password)
                toast(getString(R.string.msg_register_ok))
                openPanelFor(user.role)
            } catch (e: Exception) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.setText(R.string.btn_register)
                toastLong(errorMessage(e))
            }
        }
    }
}
