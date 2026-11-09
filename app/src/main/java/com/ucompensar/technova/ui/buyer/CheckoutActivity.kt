package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.databinding.ActivityCheckoutBinding
import com.ucompensar.technova.databinding.DialogOrderSuccessBinding
import com.ucompensar.technova.ui.common.toCop

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

        val banks = resources.getStringArray(R.array.banks)
        binding.actBank.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, banks))

        binding.rgPayment.setOnCheckedChangeListener { _, checkedId -> showPaymentForm(checkedId) }
        showPaymentForm(binding.rgPayment.checkedRadioButtonId)

        binding.etCardNumber.doAfterTextChanged { text ->
            binding.tvCardNumber.text = if (text.isNullOrEmpty()) getString(R.string.card_preview_number)
            else text.chunked(4).joinToString(" ")
        }
        binding.etCardName.doAfterTextChanged { text ->
            binding.tvCardName.text = if (text.isNullOrEmpty()) getString(R.string.card_preview_name) else text
        }
        binding.etCardExpiry.doAfterTextChanged { text ->
            binding.tvCardExpiry.text = if (text.isNullOrEmpty()) getString(R.string.card_preview_expiry) else text
        }

        binding.btnPay.setOnClickListener { if (validate()) processPayment() }
    }

    private fun showPaymentForm(checkedId: Int) {
        binding.layoutCard.visibility = if (checkedId == R.id.rbCard) View.VISIBLE else View.GONE
        binding.tilBank.visibility = if (checkedId == R.id.rbPse) View.VISIBLE else View.GONE
        binding.tilNequi.visibility = if (checkedId == R.id.rbNequi) View.VISIBLE else View.GONE
        binding.tvCashInfo.visibility = if (checkedId == R.id.rbCash) View.VISIBLE else View.GONE
    }

    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        fun check(layout: TextInputLayout, ok: Boolean, error: String = required): Boolean {
            layout.error = if (ok) null else error
            return ok
        }

        var valid = check(binding.tilAddress, !binding.etAddress.text.isNullOrBlank())
        valid = check(binding.tilCity, !binding.etCity.text.isNullOrBlank()) && valid

        when (binding.rgPayment.checkedRadioButtonId) {
            R.id.rbCard -> {
                valid = check(binding.tilCardNumber, binding.etCardNumber.text?.length == 16,
                    getString(R.string.err_card_number)) && valid
                valid = check(binding.tilCardName, !binding.etCardName.text.isNullOrBlank()) && valid
                valid = check(binding.tilCardExpiry, binding.etCardExpiry.text?.length == 5) && valid
                valid = check(binding.tilCardCvv, (binding.etCardCvv.text?.length ?: 0) >= 3) && valid
            }
            R.id.rbPse -> valid = check(binding.tilBank, !binding.actBank.text.isNullOrBlank()) && valid
            R.id.rbNequi -> valid = check(binding.tilNequi, binding.etNequi.text?.length == 10) && valid
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, R.string.err_accept, Toast.LENGTH_SHORT).show()
            valid = false
        }
        return valid
    }

    private fun processPayment() {
        binding.btnPay.isEnabled = false
        binding.btnPay.setText(R.string.processing_payment)
        Handler(Looper.getMainLooper()).postDelayed({ showSuccess() }, 1500)
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
