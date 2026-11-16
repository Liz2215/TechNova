package com.ucompensar.technova.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ucompensar.technova.data.AuthRepository
import com.ucompensar.technova.data.Db
import com.ucompensar.technova.databinding.ActivitySplashBinding
import com.ucompensar.technova.ui.common.openPanelFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida. Muestra el logo con una animación y, si ya hay una sesión
 * guardada en Firebase, abre directamente el panel del rol del usuario; si no, va al login.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    /** Inicia la animación del logo y decide a qué pantalla continuar. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivLogo.alpha = 0f
        binding.ivLogo.scaleX = 0.6f
        binding.ivLogo.scaleY = 0.6f
        binding.ivLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700).start()

        lifecycleScope.launch {
            delay(SPLASH_DURATION)
            if (Db.auth.currentUser == null) {
                openLogin()
                return@launch
            }
            runCatching { AuthRepository.loadProfile() }
                .onSuccess { openPanelFor(it.role) }
                .onFailure { openLogin() }
        }
    }

    /** Abre la pantalla de inicio de sesión y cierra la bienvenida. */
    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
        private const val SPLASH_DURATION = 1500L
    }
}
