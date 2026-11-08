package com.ucompensar.technova.ui.seller

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucompensar.technova.R
import com.ucompensar.technova.data.FakeRepository
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.databinding.ActivityProductFormBinding

/** Crear o editar un producto. Si recibe un ID, carga sus datos para editarlos. */
class ProductFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductFormBinding
    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val categories = resources.getStringArray(R.array.product_categories)
        binding.actCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, categories))

        product = FakeRepository.findProduct(intent.getIntExtra(EXTRA_ID, -1))
        product?.let { p ->
            binding.toolbar.setTitle(R.string.product_form_edit)
            binding.ivPreview.setImageResource(p.imageRes)
            binding.etName.setText(p.name)
            binding.actCategory.setText(p.category, false)
            binding.etPrice.setText(p.price.toString())
            binding.etStock.setText(p.stock.toString())
            binding.etSku.setText(p.sku)
            binding.etSku.isEnabled = false
            binding.etDescription.setText(p.description)
        }

        binding.btnUploadImage.setOnClickListener {
            Toast.makeText(this, R.string.msg_not_available, Toast.LENGTH_SHORT).show()
        }
        binding.btnSave.setOnClickListener { if (validate()) save() }
    }

    private fun validate(): Boolean {
        val required = getString(R.string.err_required)
        val fields = listOf(
            binding.tilName to binding.etName.text,
            binding.tilCategory to binding.actCategory.text,
            binding.tilPrice to binding.etPrice.text,
            binding.tilStock to binding.etStock.text,
            binding.tilSku to binding.etSku.text
        )
        fields.forEach { (layout, text) -> layout.error = if (text.isNullOrBlank()) required else null }
        return fields.all { it.first.error == null }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val category = binding.actCategory.text.toString()
        val price = binding.etPrice.text.toString().toLong()
        val stock = binding.etStock.text.toString().toInt()
        val description = binding.etDescription.text.toString().trim()

        val current = product
        if (current != null) {
            current.name = name
            current.category = category
            current.price = price
            current.stock = stock
            current.description = description
        } else {
            val newId = (FakeRepository.products.maxOfOrNull { it.id } ?: 0) + 1
            FakeRepository.products.add(
                Product(newId, binding.etSku.text.toString().trim(), name, category, price, stock,
                    0.0, description, FakeRepository.imageForCategory(category))
            )
        }
        Toast.makeText(this, R.string.msg_product_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_ID = "product_id"
    }
}
