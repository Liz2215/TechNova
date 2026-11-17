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

/** Pantalla que abre la pasarela de ePayco para pagar */
class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding

    /** Prepara el navegador interno y abre el pago */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { confirmCancel() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            /** Si se presiona atrás pregunta antes de salir */
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
            /** Revisa cada página que se abre para saber cuándo terminó el pago */
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                handleUrl(request.url)

            /** Muestra la barra de carga */
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
            }

            /** Oculta la barra de carga */
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

    /** Cuando ePayco termina toma la referencia del pago y vuelve */
    private fun handleUrl(uri: Uri): Boolean {
        if (!uri.toString().startsWith(EpaycoGateway.RESPONSE_URL)) return false
        val ref = uri.getQueryParameter("ref_payco") ?: uri.getQueryParameter("x_ref_payco")
        val data = Intent().putExtra(RESULT_REF, ref.orEmpty())
        setResult(if (ref.isNullOrEmpty()) Activity.RESULT_CANCELED else Activity.RESULT_OK, data)
        finish()
        return true
    }

    /** Pregunta si de verdad quiere salir del pago */
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
