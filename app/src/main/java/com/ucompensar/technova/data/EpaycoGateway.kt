package com.ucompensar.technova.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Integración con la pasarela de pagos ePayco en modo de pruebas.
 *
 * El pago se hace en el Checkout estándar de ePayco (página segura de la pasarela abierta
 * dentro de la app), así los datos de la tarjeta nunca pasan por TechNova (PCI-DSS).
 * Al terminar, ePayco devuelve una referencia (ref_payco) y la app consulta el servicio
 * de validación de ePayco para confirmar si la transacción fue aceptada.
 */
object EpaycoGateway {
    /** Llave pública del comercio en ePayco (panel de ePayco > Integraciones > Llaves API). */
    const val PUBLIC_KEY = "45b960805ced5c27ce34b1600b4b9f54"

    /** true = transacciones de prueba (no se cobra dinero real). */
    const val TEST_MODE = true

    /** Dirección a la que ePayco redirige al terminar; la app la intercepta y no se carga. */
    const val RESPONSE_URL = "https://technova.app/epayco/respuesta"

    private const val VALIDATION_URL = "https://secure.epayco.co/validation/v1/reference/"

    /** Resultado de una transacción consultada en ePayco. */
    data class Result(
        val code: Int,
        val message: String,
        val reference: String,
        val invoice: String,
        val amount: Double
    ) {
        val approved get() = code == 1
        val pending get() = code == 3
    }

    /**
     * Arma la página HTML que carga el checkout.js oficial de ePayco y abre el pago
     * con los datos del pedido y del comprador.
     */
    fun checkoutHtml(invoice: String, description: String, amount: Long, buyer: User, address: String): String {
        val data = JSONObject().apply {
            put("name", "Compra TechNova")
            put("description", description)
            put("invoice", invoice)
            put("currency", "cop")
            put("amount", amount.toString())
            put("tax_base", "0")
            put("tax", "0")
            put("country", "co")
            put("lang", "es")
            put("external", "true")
            put("response", RESPONSE_URL)
            put("name_billing", buyer.name)
            put("email_billing", buyer.email)
            put("mobilephone_billing", buyer.phone)
            put("address_billing", address)
        }
        return """
            <!DOCTYPE html>
            <html><head><meta name="viewport" content="width=device-width, initial-scale=1">
            <style>body{font-family:sans-serif;text-align:center;padding-top:40%;color:#2E2A6B}</style></head>
            <body>
              <p>Conectando con la pasarela segura de ePayco…</p>
              <script src="https://checkout.epayco.co/checkout.js"></script>
              <script>
                var handler = ePayco.checkout.configure({ key: '$PUBLIC_KEY', test: $TEST_MODE });
                handler.open($data);
              </script>
            </body></html>
        """.trimIndent()
    }

    /**
     * Consulta en ePayco el estado real de la transacción (servicio de validación).
     * Códigos de respuesta: 1 aceptada, 2 rechazada, 3 pendiente, 4 fallida.
     */
    suspend fun validate(refPayco: String): Result = withContext(Dispatchers.IO) {
        val connection = URL(VALIDATION_URL + refPayco).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val data = json.optJSONObject("data") ?: JSONObject()
            Result(
                code = data.optString("x_cod_response", "0").toIntOrNull() ?: 0,
                message = data.optString("x_respuesta", data.optString("x_response", "Sin respuesta")),
                reference = data.optString("x_ref_payco", refPayco),
                invoice = data.optString("x_id_invoice", ""),
                amount = data.optString("x_amount", "0").toDoubleOrNull() ?: 0.0
            )
        } finally {
            connection.disconnect()
        }
    }
}
