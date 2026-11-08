package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.ActivityCheckoutBinding
import com.ucompensar.technova.databinding.DialogOrderSuccessBinding
import com.ucompensar.technova.ui.common.toCop

/** Proceso de pago: dirección, método de pago, resumen y confirmación. */
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val subtotal = FakeRepository.cartSubtotal()
        binding.summary.tvSubtotal.text = subtotal.toCop()
        binding.summary.tvShipping.text = FakeRepository.SHIPPING_COST.toCop()
        binding.summary.tvTotal.text = (subtotal + FakeRepository.SHIPPING_COST).toCop()

        binding.btnPay.setOnClickListener { if (validate()) showSuccess() }
    }

    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        binding.tilAddress.error = if (binding.etAddress.text.isNullOrBlank()) required else null
        binding.tilCity.error = if (binding.etCity.text.isNullOrBlank()) required else null
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, R.string.err_accept, Toast.LENGTH_SHORT).show()
        }
        return binding.tilAddress.error == null && binding.tilCity.error == null && binding.cbTerms.isChecked
    }

    private fun showSuccess() {
        val dialogBinding = DialogOrderSuccessBinding.inflate(layoutInflater)
        dialogBinding.tvOrderNumber.text = getString(R.string.label_order_number, "1046")
        FakeRepository.cart.clear()
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
