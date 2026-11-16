package com.ucompensar.technova.ui.seller

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ucompensar.technova.R
import com.ucompensar.technova.data.Db
import com.ucompensar.technova.data.Product
import com.ucompensar.technova.data.ProductRepository
import com.ucompensar.technova.data.Session
import com.ucompensar.technova.databinding.ActivityProductFormBinding
import com.ucompensar.technova.ui.common.ImageUtils
import com.ucompensar.technova.ui.common.errorMessage
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.showProductImage
import com.ucompensar.technova.ui.common.toast
import com.ucompensar.technova.ui.common.toastLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Formulario para crear o editar un producto (lo usan el vendedor y el administrador).
 * La foto se toma con la cámara o se elige de la galería, se comprime y se guarda
 * junto con los demás datos en Firestore.
 */
class ProductFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductFormBinding
    private var product: Product? = null
    private var imageBase64 = ""
    private var cameraUri: Uri? = null

    /** Selector de fotos de la galería (no necesita permisos). */
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { loadImage(it) }
    }

    /** Abre la cámara y guarda la foto en un archivo temporal de la app. */
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = cameraUri
        if (saved && uri != null) loadImage(uri)
    }

    /** Prepara el formulario y, si se recibe un id, carga el producto para editarlo. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val categories = resources.getStringArray(R.array.product_categories)
        binding.actCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, categories))

        binding.btnUploadImage.setOnClickListener { chooseImageSource() }
        binding.btnSave.setOnClickListener { if (validate()) save() }

        val id = intent.getStringExtra(EXTRA_ID)
        if (id != null) {
            binding.btnSave.isEnabled = false
            launchUi {
                product = ProductRepository.get(id)
                product?.let { fill(it) }
                binding.btnSave.isEnabled = true
            }
        }
    }

    /** Llena el formulario con los datos del producto que se va a editar. */
    private fun fill(p: Product) {
        binding.toolbar.setTitle(R.string.product_form_edit)
        imageBase64 = p.imageBase64
        showPreview()
        binding.etName.setText(p.name)
        binding.actCategory.setText(p.category, false)
        binding.etPrice.setText(p.price.toString())
        binding.etStock.setText(p.stock.toString())
        binding.etSku.setText(p.sku)
        binding.etSku.isEnabled = false
        binding.etDescription.setText(p.description)
    }

    /** Pregunta si la foto se toma con la cámara o se elige de la galería. */
    private fun chooseImageSource() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.btn_upload_image)
            .setItems(R.array.image_sources) { _, which ->
                if (which == 0) openCamera()
                else galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            .show()
    }

    /** Crea un archivo temporal y abre la cámara para tomar la foto del producto. */
    private fun openCamera() {
        val dir = File(cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "producto_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        cameraUri = uri
        runCatching { cameraLauncher.launch(uri) }.onFailure { toastLong(getString(R.string.msg_no_camera)) }
    }

    /** Comprime la foto elegida en segundo plano y la muestra en la vista previa. */
    private fun loadImage(uri: Uri) {
        launchUi {
            imageBase64 = withContext(Dispatchers.Default) { ImageUtils.uriToBase64(this@ProductFormActivity, uri) }
            showPreview()
        }
    }

    /** Muestra la foto actual del producto ocupando todo el recuadro. */
    private fun showPreview() {
        if (imageBase64.isEmpty()) return
        binding.ivPreview.setPadding(0, 0, 0, 0)
        binding.ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.ivPreview.showProductImage(imageBase64)
    }

    /** Revisa que los campos obligatorios estén llenos y que precio y stock sean números válidos. */
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
        if (binding.tilPrice.error == null && (binding.etPrice.text.toString().toLongOrNull() ?: 0) <= 0) {
            binding.tilPrice.error = getString(R.string.err_number)
        }
        if (binding.tilStock.error == null && binding.etStock.text.toString().toIntOrNull() == null) {
            binding.tilStock.error = getString(R.string.err_number)
        }
        val ok = fields.all { it.first.error == null }
        if (ok && imageBase64.isEmpty()) {
            toastLong(getString(R.string.err_image_required))
            return false
        }
        return ok
    }

    /** Crea o actualiza el producto en Firestore. */
    private fun save() {
        val current = product
        val me = Session.current
        val updated = Product(
            id = current?.id.orEmpty(),
            sku = binding.etSku.text.toString().trim(),
            name = binding.etName.text.toString().trim(),
            category = binding.actCategory.text.toString(),
            price = binding.etPrice.text.toString().toLong(),
            stock = binding.etStock.text.toString().toInt(),
            rating = current?.rating ?: 0.0,
            description = binding.etDescription.text.toString().trim(),
            imageBase64 = imageBase64,
            sellerId = current?.sellerId ?: Db.uid.orEmpty(),
            sellerName = current?.sellerName ?: me?.name.orEmpty()
        )
        binding.btnSave.isEnabled = false
        launchUi {
            try {
                ProductRepository.save(updated)
                toast(getString(R.string.msg_product_saved))
                finish()
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                toastLong(errorMessage(e))
            }
        }
    }

    companion object {
        const val EXTRA_ID = "product_id"
    }
}
