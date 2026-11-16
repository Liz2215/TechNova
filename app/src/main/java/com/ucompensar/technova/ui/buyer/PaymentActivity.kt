package com.ucompensar.technova.ui.buyer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.EpaycoGateway
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.databinding.ActivityPaymentBinding

/**
 * Pantalla de pago: abre el Checkout de ePayco dentro de un WebView. Cuando ePayco
 * termina y redirige a la URL de respuesta, la app la intercepta, toma la referencia
 * de la transacción (ref_payco) y la devuelve a la pantalla de checkout para validarla.
 */
class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding

    /** Configura el WebView y carga la página con el checkout de ePayco. */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { confirmCancel() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            /** Al presionar atrás, pregunta antes de cancelar el pago. */
            override fun handleOnBackPressed() = confirmCancel()
        })

        val user = Session.current
        if (user == null) {
            finish()
            return
        }

        with(binding.webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
        }
        binding.webView.webViewClient = object : WebViewClient() {
            /** Revisa cada página que se va a abrir para detectar la respuesta de ePayco. */
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                handleUrl(request.url)

            /** Muestra la barra de carga mientras se abre la página. */
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
            }

            /** Oculta la barra de carga cuando la página terminó de cargar. */
            override fun onPageFinished(view: WebView, url: String) {
                binding.progress.visibility = View.GONE
            }
        }

        val html = EpaycoGateway.checkoutHtml(
            invoice = intent.getStringExtra(EXTRA_INVOICE).orEmpty(),
            description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty(),
            amount = intent.getLongExtra(EXTRA_AMOUNT, 0),
            buyer = user,
            address = intent.getStringExtra(EXTRA_ADDRESS).orEmpty()
        )
        binding.webView.loadDataWithBaseURL("https://technova.app/", html, "text/html", "UTF-8", null)
    }

    /**
     * Revisa cada dirección que intenta abrir el WebView. Si es la URL de respuesta de
     * ePayco, termina la pantalla devolviendo la referencia de la transacción.
     */
    private fun handleUrl(uri: Uri): Boolean {
        if (!uri.toString().startsWith(EpaycoGateway.RESPONSE_URL)) return false
        val ref = uri.getQueryParameter("ref_payco") ?: uri.getQueryParameter("x_ref_payco")
        val data = Intent().putExtra(RESULT_REF, ref.orEmpty())
        setResult(if (ref.isNullOrEmpty()) Activity.RESULT_CANCELED else Activity.RESULT_OK, data)
        finish()
        return true
    }

    /** Pregunta antes de salir para no abandonar un pago a medias por error. */
    private fun confirmCancel() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.payment_cancel_title)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.payment_cancel_confirm) { _, _ ->
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            .show()
    }

    companion object {
        const val EXTRA_INVOICE = "invoice"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_ADDRESS = "address"
        const val RESULT_REF = "ref_payco"
    }
}
