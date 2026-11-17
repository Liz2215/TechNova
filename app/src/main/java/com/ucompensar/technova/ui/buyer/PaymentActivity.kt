package com.ucompensar.technova.ui.buyer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
    private var finished = false

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

        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)
        cookies.setAcceptThirdPartyCookies(binding.webView, true)

        with(binding.webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }
        binding.webView.webViewClient = object : WebViewClient() {
            /** Revisa cada página que se abre para saber cuándo terminó el pago */
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                handleUrl(request.url)

            /** Si el banco devuelve la respuesta por otro camino también la atrapa aquí */
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (isResponse(request.url)) {
                    runOnUiThread { handleUrl(request.url) }
                    return WebResourceResponse("text/html", "UTF-8", "".byteInputStream())
                }
                return null
            }

            /** Muestra la barra de carga y revisa si ya llegó la respuesta del pago */
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
                if (isResponse(Uri.parse(url))) handleUrl(Uri.parse(url))
            }

            /** Oculta la barra de carga */
            override fun onPageFinished(view: WebView, url: String) {
                binding.progress.visibility = View.GONE
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            /** La validación del banco a veces abre otra ventana y aquí se carga en la misma pantalla */
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                val popup = WebView(this@PaymentActivity)
                popup.settings.javaScriptEnabled = true
                popup.webViewClient = object : WebViewClient() {
                    /** Pasa la página de la ventana nueva a la pantalla principal */
                    override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean {
                        if (!handleUrl(request.url)) binding.webView.loadUrl(request.url.toString())
                        v.destroy()
                        return true
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = popup
                resultMsg.sendToTarget()
                return true
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

    /** Dice si la dirección es la respuesta de ePayco */
    private fun isResponse(uri: Uri): Boolean = uri.toString().startsWith(EpaycoGateway.RESPONSE_URL)

    /** Cuando ePayco termina toma la referencia del pago y vuelve */
    private fun handleUrl(uri: Uri): Boolean {
        if (!isResponse(uri)) return false
        if (finished) return true
        finished = true
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
