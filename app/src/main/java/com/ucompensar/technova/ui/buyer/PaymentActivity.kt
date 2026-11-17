package com.ucompensar.technova.ui.buyer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.EpaycoGateway
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.databinding.ActivityPaymentBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/** Pantalla que abre la pasarela de ePayco para pagar */
class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private var finished = false
    private var foundRef = ""
    private var polling: Job? = null

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
                watch(request.url.toString())
                if (isResponse(request.url)) {
                    runOnUiThread { handleUrl(request.url) }
                    return WebResourceResponse("text/html", "UTF-8", "".byteInputStream())
                }
                return null
            }

            /** Muestra la barra de carga y revisa si ya llegó la respuesta del pago */
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
                watch(url)
                if (isResponse(Uri.parse(url))) handleUrl(Uri.parse(url))
            }

            /** Oculta la barra de carga */
            override fun onPageFinished(view: WebView, url: String) {
                binding.progress.visibility = View.GONE
                view.evaluateJavascript(REF_SPY, null)
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

        binding.webView.addJavascriptInterface(Bridge(), "TechNovaPay")

        val html = EpaycoGateway.checkoutHtml(
            invoice = intent.getStringExtra(EXTRA_INVOICE).orEmpty(),
            description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty(),
            amount = intent.getLongExtra(EXTRA_AMOUNT, 0),
            buyer = user,
            address = intent.getStringExtra(EXTRA_ADDRESS).orEmpty()
        )
        binding.webView.loadDataWithBaseURL("https://technova.app/", html, "text/html", "UTF-8", null)
    }

    /** Esto recibe la referencia que encuentra el script dentro de la página de ePayco */
    private inner class Bridge {
        /** El script llama esta función cuando ve la referencia del pago */
        @JavascriptInterface
        fun found(ref: String) = runOnUiThread { rememberRef(ref) }
    }

    /** Guarda cada dirección en el registro y busca la referencia del pago en ella */
    private fun watch(url: String) {
        log(url)
        REF_IN_URL.find(url)?.groupValues?.get(1)?.let { ref -> runOnUiThread { rememberRef(ref) } }
    }

    /** Guarda la referencia y empieza a preguntarle a ePayco cómo va el pago */
    private fun rememberRef(ref: String) {
        if (ref.length < 5 || ref == foundRef || finished) return
        foundRef = ref
        log("ref_payco=" + ref)
        polling?.cancel()
        polling = lifecycleScope.launch {
            while (!finished) {
                delay(4_000)
                val result = runCatching { EpaycoGateway.validate(ref) }.getOrNull() ?: continue
                log("estado " + result.code + " " + result.message)
                if (result.code != 0 && !result.pending) {
                    finishWith(ref)
                    return@launch
                }
            }
        }
    }

    /** Escribe en el registro del pago para poder revisar si algo falla */
    private fun log(text: String) {
        Log.d("TechNovaPago", text)
        runCatching { File(getExternalFilesDir(null), "pago_log.txt").appendText(text.take(400) + "\n") }
    }

    /** Cierra el pago y le pasa la referencia al checkout */
    private fun finishWith(ref: String) {
        if (finished) return
        finished = true
        setResult(Activity.RESULT_OK, Intent().putExtra(RESULT_REF, ref))
        finish()
    }

    /** Dice si la dirección es la respuesta de ePayco */
    private fun isResponse(uri: Uri): Boolean = uri.toString().startsWith(EpaycoGateway.RESPONSE_URL)

    /** Cuando ePayco termina toma la referencia del pago y vuelve */
    private fun handleUrl(uri: Uri): Boolean {
        if (!isResponse(uri)) return false
        if (finished) return true
        finished = true
        val ref = uri.getQueryParameter("ref_payco") ?: uri.getQueryParameter("x_ref_payco") ?: foundRef
        if (ref.isEmpty()) {
            finished = true
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else finishWith(ref)
        return true
    }

    /** Pregunta si de verdad quiere salir del pago */
    private fun confirmCancel() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.payment_cancel_title)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.payment_cancel_confirm) { _, _ ->
                if (foundRef.isNotEmpty()) finishWith(foundRef)
                else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_INVOICE = "invoice"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_ADDRESS = "address"
        const val RESULT_REF = "ref_payco"

        /** Busca la referencia del pago dentro de una dirección */
        private val REF_IN_URL = Regex("(?:x_)?ref_payco=(\\d{5,})")

        /** Script que revisa las respuestas de ePayco y el texto de la página para encontrar la referencia del pago */
        private const val REF_SPY = """
            (function(){
              if (window.__tnSpy) return; window.__tnSpy = true;
              var re = /ref_?payco["'\s:=]+"?(\d{5,})/i;
              function check(t){ try { var m = re.exec(String(t)); if (m) TechNovaPay.found(m[1]); } catch(e){} }
              var of = window.fetch;
              if (of) window.fetch = function(){ return of.apply(this, arguments).then(function(r){ try { r.clone().text().then(check); } catch(e){} return r; }); };
              var os = XMLHttpRequest.prototype.send;
              XMLHttpRequest.prototype.send = function(){ this.addEventListener('load', function(){ try { check(this.responseText); } catch(e){} }); return os.apply(this, arguments); };
              setInterval(function(){ check(location.href); check(document.documentElement.innerHTML); }, 2000);
            })();
        """
    }
}
