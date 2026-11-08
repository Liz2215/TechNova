package com.ucompensar.technova.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.databinding.ActivitySplashBinding

/** Pantalla de bienvenida: muestra el logo unos segundos y abre el inicio de sesión. */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val openLogin = Runnable {
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // para que el botón atrás no regrese al splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animación de entrada del logo
        binding.ivLogo.alpha = 0f
        binding.ivLogo.scaleX = 0.6f
        binding.ivLogo.scaleY = 0.6f
        binding.ivLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700).start()

        handler.postDelayed(openLogin, SPLASH_DURATION)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openLogin)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DURATION = 2000L
    }
}
