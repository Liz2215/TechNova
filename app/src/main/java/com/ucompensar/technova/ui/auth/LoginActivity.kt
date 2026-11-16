package com.ucompensar.technova.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ucompensar.technova.R
import com.ucompensar.technova.data.AuthRepository
import com.ucompensar.technova.data.SecureStore
import com.ucompensar.technova.data.User
import com.ucompensar.technova.databinding.ActivityLoginBinding
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.openPanelFor
import com.ucompensar.technova.ui.common.toastLong

/**
 * Inicio de sesión con tres opciones: correo y contraseña, cuenta de Google y huella digital.
 * El panel que se abre depende del rol guardado en Firestore para la cuenta.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    /** Recibe la cuenta elegida en la ventana de Google y entra con ella a Firebase. */
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val token = account.idToken ?: return@registerForActivityResult
            runLogin { AuthRepository.loginWithGoogle(token) }
        } catch (e: ApiException) {
            if (e.statusCode != CANCELED) toastLong(getString(R.string.msg_google_error))
        }
    }

    /** Conecta los botones de la pantalla con sus acciones. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { if (validate()) loginWithEmail() }
        binding.btnGoogle.setOnClickListener { startGoogleSignIn() }
        binding.btnBiometric.setOnClickListener { showBiometricPrompt() }
        binding.btnForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /** Revisa que el correo tenga formato válido y la contraseña al menos 6 caracteres. */
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

    /** Inicia sesión en Firebase y guarda las credenciales cifradas para la huella. */
    private fun loginWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        runLogin {
            AuthRepository.login(email, password).also { SecureStore.saveCredentials(this, email, password) }
        }
    }

    /**
     * Abre la ventana de Google para elegir la cuenta. Requiere que el proyecto de Firebase
     * tenga registrada la huella SHA-1 de la app (si no, el archivo google-services.json
     * no trae el id de cliente web y se avisa al usuario).
     */
    private fun startGoogleSignIn() {
        val clientIdRes = resources.getIdentifier("default_web_client_id", "string", packageName)
        if (clientIdRes == 0) {
            toastLong(getString(R.string.msg_google_not_configured))
            return
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(clientIdRes))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, options)
        client.signOut().addOnCompleteListener { googleLauncher.launch(client.signInIntent) }
    }

    /**
     * Muestra la ventana de huella del sistema (BiometricPrompt). Si el usuario se
     * autentica, entra con las credenciales guardadas cifradas en el último inicio de sesión.
     */
    private fun showBiometricPrompt() {
        val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK
        val manager = BiometricManager.from(this)
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            toastLong(getString(R.string.biometric_unavailable))
            return
        }
        val saved = SecureStore.credentials(this)
        if (saved == null) {
            toastLong(getString(R.string.biometric_need_login))
            return
        }

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                /** Huella reconocida: inicia sesión con las credenciales guardadas. */
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    runLogin { AuthRepository.login(saved.first, saved.second) }
                }

                /** Huella no reconocida: avisa al usuario para que lo intente de nuevo. */
                override fun onAuthenticationFailed() {
                    toastLong(getString(R.string.biometric_failed))
                }
            })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.action_cancel))
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    /** Ejecuta un inicio de sesión mostrando "cargando" y, si funciona, abre el panel del rol. */
    private fun runLogin(action: suspend () -> User) {
        setLoading(true)
        launchUi {
            try {
                val user = action()
                openPanelFor(user.role)
            } catch (e: Exception) {
                setLoading(false)
                toastLong(errorMessage(e))
            }
        }
    }

    /** Desactiva los botones y cambia el texto mientras se inicia sesión. */
    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
        binding.btnBiometric.isEnabled = !loading
        binding.btnLogin.setText(if (loading) R.string.msg_loading else R.string.btn_login)
    }

    companion object {
        private const val CANCELED = 12501
    }
}
