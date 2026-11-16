package com.ucompensar.technova.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.AuthRepository
import com.ucompensar.technova.databinding.ActivityForgotPasswordBinding
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.common.toastLong

/**
 * Recuperación de contraseña: Firebase envía al correo un enlace para crear una
 * contraseña nueva. Luego se muestra el mensaje "Revisa tu correo" con opción de reenviar.
 */
class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private var email = ""

    /** Conecta los botones de enviar, reenviar y volver al login. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendLink() }
        binding.btnBackLogin.setOnClickListener { finish() }
        binding.btnResend.setOnClickListener { send(resend = true) }
    }

    /** Valida el correo escrito y pide a Firebase enviar el enlace. */
    private fun sendLink() {
        email = binding.etEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_email)
            return
        }
        binding.tilEmail.error = null
        send(resend = false)
    }

    /** Envía (o reenvía) el correo de recuperación y muestra el resultado. */
    private fun send(resend: Boolean) {
        binding.btnSend.isEnabled = false
        launchUi {
            try {
                AuthRepository.sendPasswordReset(email)
                if (resend) {
                    toast(getString(R.string.msg_link_resent))
                } else {
                    binding.tvSentMessage.text = getString(R.string.forgot_sent_msg, email)
                    binding.layoutForm.visibility = View.GONE
                    binding.layoutSent.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                toastLong(errorMessage(e))
            } finally {
                binding.btnSend.isEnabled = true
            }
        }
    }
}
