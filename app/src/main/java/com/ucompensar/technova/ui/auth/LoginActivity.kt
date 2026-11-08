package com.ucompensar.technova.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.ActivityLoginBinding
import com.ucompensar.technova.ui.admin.AdminActivity
import com.ucompensar.technova.ui.buyer.BuyerActivity
import com.ucompensar.technova.ui.seller.SellerActivity

/** Inicio de sesión. Según el rol elegido abre el panel correspondiente. */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { if (validate()) openPanel() }
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, R.string.msg_not_available, Toast.LENGTH_SHORT).show()
        }
        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validate(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        binding.tilEmail.error = when {
            email.isEmpty() -> getString(R.string.err_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.err_email)
            else -> null
        }
        binding.tilPassword.error =
            if (password.length < 6) getString(R.string.err_password) else null
        return binding.tilEmail.error == null && binding.tilPassword.error == null
    }

    private fun openPanel() {
        val destination = when (binding.toggleRole.checkedButtonId) {
            R.id.btnRoleSeller -> SellerActivity::class.java
            R.id.btnRoleAdmin -> AdminActivity::class.java
            else -> BuyerActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }
}
