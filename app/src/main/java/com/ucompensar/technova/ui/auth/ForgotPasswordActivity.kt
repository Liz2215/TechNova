package com.ucompensar.technova.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendLink() }
        binding.btnBackLogin.setOnClickListener { finish() }
        binding.btnResend.setOnClickListener {
            Toast.makeText(this, R.string.btn_resend, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendLink() {
        val email = binding.etEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_email)
            return
        }
        binding.tilEmail.error = null
        binding.tvSentMessage.text = getString(R.string.forgot_sent_msg, email)
        binding.layoutForm.visibility = View.GONE
        binding.layoutSent.visibility = View.VISIBLE
    }
}
