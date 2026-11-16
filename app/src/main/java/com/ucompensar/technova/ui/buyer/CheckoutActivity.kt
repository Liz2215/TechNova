package com.ucompensar.technova.ui.buyer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.ucompensar.technova.R
import com.ucompensar.technova.data.CartRepository
import com.ucompensar.technova.data.Db
import com.ucompensar.technova.data.EpaycoGateway
import com.ucompensar.technova.data.Order
import com.ucompensar.technova.data.OrderItem
import com.ucompensar.technova.data.OrderRepository
import com.ucompensar.technova.data.PaymentStatus
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.data.SettingsRepository
import com.ucompensar.technova.data.UserRepository
import com.ucompensar.technova.databinding.ActivityCheckoutBinding
import com.ucompensar.technova.databinding.DialogOrderSuccessBinding
import com.ucompensar.technova.ui.common.LocationHelper
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.toCop
import com.ucompensar.technova.ui.common.toastLong
import java.util.Locale

/**
 * Confirmación de la compra: dirección de entrega (con opción de tomarla del GPS),
 * método de pago y resumen. Los pagos en línea se hacen en ePayco; el pago contraentrega
 * crea el pedido directamente. Al confirmar, se guarda el pedido en Firestore,
 * se descuenta el stock y se vacía el carrito.
 */
class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private var shipping = 0L
    private var latitude = 0.0
    private var longitude = 0.0
    private var pendingNumber = ""

    /** Pide el permiso de ubicación y, si se concede, captura la dirección. */
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) captureLocation() else toastLong(getString(R.string.msg_location_denied))
    }

    /** Recibe el resultado de la pantalla de pago de ePayco. */
    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val ref = result.data?.getStringExtra(PaymentActivity.RESULT_REF)
        if (result.resultCode == Activity.RESULT_OK && !ref.isNullOrEmpty()) confirmPayment(ref)
        else {
            setPaying(false)
            toastLong(getString(R.string.msg_payment_canceled))
        }
    }

    /** Llena la dirección guardada, conecta los botones y calcula los totales. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        Session.current?.let { user ->
            binding.etAddress.setText(user.address)
            binding.etCity.setText(user.city)
            latitude = user.latitude
            longitude = user.longitude
            showCoordinates()
        }

        binding.rgPayment.setOnCheckedChangeListener { _, checkedId -> showPaymentInfo(checkedId) }
        showPaymentInfo(binding.rgPayment.checkedRadioButtonId)
        binding.btnLocation.setOnClickListener {
            if (LocationHelper.hasPermission(this)) captureLocation() else locationPermission.launch(LocationHelper.PERMISSIONS)
        }
        binding.tvLocation.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LocationHelper.mapsUrl(latitude, longitude))))
        }
        binding.btnPay.setOnClickListener { if (validate()) startPayment() }

        binding.btnPay.isEnabled = false
        launchUi {
            val settings = SettingsRepository.get()
            shipping = settings.shippingCost
            if (CartRepository.cart.isEmpty()) CartRepository.load()
            updateTotals()
            binding.btnPay.isEnabled = CartRepository.cart.isNotEmpty() && !settings.maintenance
            if (settings.maintenance) toastLong(getString(R.string.msg_maintenance))
        }
    }

    /** Muestra subtotal, envío y total del carrito. */
    private fun updateTotals() {
        val subtotal = CartRepository.subtotal()
        binding.summary.tvSubtotal.text = subtotal.toCop()
        binding.summary.tvShipping.text = shipping.toCop()
        binding.summary.tvTotal.text = (subtotal + shipping).toCop()
    }

    /** Muestra el aviso de pago en línea (ePayco) o el de pago contraentrega. */
    private fun showPaymentInfo(checkedId: Int) {
        val cash = checkedId == R.id.rbCash
        binding.tvCashInfo.visibility = if (cash) View.VISIBLE else View.GONE
        binding.tvOnlineInfo.visibility = if (cash) View.GONE else View.VISIBLE
    }

    /** Obtiene la ubicación con el GPS y llena la dirección y la ciudad. */
    private fun captureLocation() {
        binding.btnLocation.isEnabled = false
        binding.btnLocation.setText(R.string.msg_locating)
        launchUi {
            try {
                val place = LocationHelper.currentPlace(this@CheckoutActivity)
                latitude = place.latitude
                longitude = place.longitude
                if (place.address.isNotBlank()) binding.etAddress.setText(place.address)
                if (place.city.isNotBlank()) binding.etCity.setText(place.city)
                showCoordinates()
            } finally {
                binding.btnLocation.isEnabled = true
                binding.btnLocation.setText(R.string.btn_use_location)
            }
        }
    }

    /** Muestra las coordenadas capturadas; al tocarlas se abren en Google Maps. */
    private fun showCoordinates() {
        if (latitude == 0.0 && longitude == 0.0) return
        binding.tvLocation.visibility = View.VISIBLE
        binding.tvLocation.text = getString(
            R.string.label_location, String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
        )
    }

    /** Revisa la dirección, la ciudad y la aceptación de los términos. */
    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        /** Marca el campo con error si no es válido y devuelve si está bien. */
        fun check(layout: TextInputLayout, ok: Boolean): Boolean {
            layout.error = if (ok) null else required
            return ok
        }

        var valid = check(binding.tilAddress, !binding.etAddress.text.isNullOrBlank())
        valid = check(binding.tilCity, !binding.etCity.text.isNullOrBlank()) && valid
        if (!binding.cbTerms.isChecked) {
            toastLong(getString(R.string.err_accept))
            valid = false
        }
        return valid
    }

    /**
     * Valida el stock en Firestore y, según el método elegido, abre la pasarela de ePayco
     * o crea el pedido contraentrega.
     */
    private fun startPayment() {
        setPaying(true)
        launchUi {
            try {
                OrderRepository.validateStock(CartRepository.cart)
                pendingNumber = OrderRepository.newNumber()
                if (binding.rgPayment.checkedRadioButtonId == R.id.rbCash) {
                    finishOrder(PaymentStatus.PENDING, "")
                } else {
                    val intent = Intent(this@CheckoutActivity, PaymentActivity::class.java)
                        .putExtra(PaymentActivity.EXTRA_INVOICE, "TN-$pendingNumber")
                        .putExtra(PaymentActivity.EXTRA_DESCRIPTION, getString(R.string.payment_description, CartRepository.count()))
                        .putExtra(PaymentActivity.EXTRA_AMOUNT, CartRepository.subtotal() + shipping)
                        .putExtra(PaymentActivity.EXTRA_ADDRESS, "${binding.etAddress.text}, ${binding.etCity.text}")
                    paymentLauncher.launch(intent)
                }
            } catch (e: Exception) {
                setPaying(false)
                toastLong(errorMessage(e))
            }
        }
    }

    /**
     * Consulta en ePayco el resultado real de la transacción. Si fue aceptada (o quedó
     * pendiente) se crea el pedido; si fue rechazada se avisa y se puede intentar de nuevo.
     */
    private fun confirmPayment(ref: String) {
        binding.btnPay.setText(R.string.msg_validating_payment)
        launchUi {
            try {
                val result = EpaycoGateway.validate(ref)
                when {
                    result.approved -> finishOrder(PaymentStatus.APPROVED, result.reference)
                    result.pending -> finishOrder(PaymentStatus.PENDING, result.reference)
                    else -> {
                        setPaying(false)
                        toastLong(getString(R.string.msg_payment_rejected, result.message))
                    }
                }
            } catch (e: Exception) {
                setPaying(false)
                toastLong(errorMessage(e))
            }
        }
    }

    /** Guarda el pedido en Firestore, vacía el carrito, recuerda la dirección y muestra la confirmación. */
    private suspend fun finishOrder(paymentStatus: PaymentStatus, reference: String) {
        val user = Session.current
        val items = CartRepository.cart.toList()
        val subtotal = CartRepository.subtotal()
        val address = binding.etAddress.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val order = Order(
            number = pendingNumber,
            buyerId = Db.uid.orEmpty(),
            customer = user?.name.orEmpty(),
            items = items.map {
                OrderItem(it.product.id, it.product.name, it.product.category, it.product.price, it.quantity, it.product.sellerId)
            },
            sellerIds = items.map { it.product.sellerId }.distinct(),
            subtotal = subtotal,
            shipping = shipping,
            total = subtotal + shipping,
            paymentMethod = selectedMethod(),
            paymentStatus = paymentStatus,
            paymentRef = reference,
            address = address,
            city = city,
            latitude = latitude,
            longitude = longitude,
            createdAt = System.currentTimeMillis()
        )
        OrderRepository.place(order, items)
        CartRepository.clear()
        runCatching { UserRepository.saveAddress(address, city, latitude, longitude) }
        showSuccess(order)
    }

    /** Nombre del método de pago elegido, tal como se guarda en el pedido. */
    private fun selectedMethod(): String = when (binding.rgPayment.checkedRadioButtonId) {
        R.id.rbPse -> getString(R.string.pay_pse)
        R.id.rbNequi -> getString(R.string.pay_nequi)
        R.id.rbCash -> getString(R.string.pay_cash)
        else -> getString(R.string.pay_card)
    }

    /** Bloquea el botón de pagar mientras se procesa el pago. */
    private fun setPaying(paying: Boolean) {
        binding.btnPay.isEnabled = !paying
        binding.btnPay.setText(if (paying) R.string.processing_payment else R.string.btn_pay)
    }

    /** Muestra el diálogo de pedido confirmado y vuelve al panel del comprador. */
    private fun showSuccess(order: Order) {
        val dialogBinding = DialogOrderSuccessBinding.inflate(layoutInflater)
        dialogBinding.tvOrderNumber.text = if (order.paymentRef.isNotEmpty())
            getString(R.string.label_order_with_ref, order.number, order.paymentRef)
        else getString(R.string.label_order_number, order.number)
        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.action_back) { _, _ ->
                val intent = Intent(this, BuyerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .show()
    }
}
