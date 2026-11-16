package com.ucompensar.technova.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.ucompensar.technova.R
import java.io.ByteArrayOutputStream

/**
 * Manejo de las fotos de productos. Como el proyecto usa el plan gratuito de Firebase
 * (sin Cloud Storage), cada foto se reduce a máximo 800 px, se comprime en JPEG y se
 * guarda como texto Base64 dentro del documento del producto en Firestore.
 */
object ImageUtils {
    private const val MAX_SIZE = 800
    private const val QUALITY = 75

    /** Caché en memoria de las fotos ya decodificadas para que las listas no se pongan lentas. */
    private val cache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        /** Tamaño en bytes de cada imagen guardada en la caché. */
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    /** Lee una foto de la galería o la cámara (por su Uri), la reduce y la devuelve en Base64. */
    fun uriToBase64(context: Context, uri: Uri): String {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalArgumentException("No se pudo leer la imagen")
        return bitmapToBase64(bitmap)
    }

    /** Convierte una imagen de los recursos de la app (fotos de ejemplo) a Base64. */
    fun resourceToBase64(context: Context, @DrawableRes res: Int): String =
        bitmapToBase64(BitmapFactory.decodeResource(context.resources, res))

    /** Escala la imagen a máximo [MAX_SIZE] px por lado y la comprime en JPEG Base64. */
    fun bitmapToBase64(source: Bitmap): String {
        val scale = minOf(1f, MAX_SIZE.toFloat() / maxOf(source.width, source.height))
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true)
        } else source
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    /** Convierte el texto Base64 guardado en Firestore de nuevo en imagen (usando la caché). */
    fun decode(base64: String): Bitmap? {
        if (base64.isEmpty()) return null
        val key = base64.length.toString() + base64.takeLast(32)
        cache.get(key)?.let { return it }
        return runCatching {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()?.also { cache.put(key, it) }
    }

    /** Calcula cuánto reducir la foto al leerla para no gastar memoria con fotos muy grandes. */
    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= MAX_SIZE && height / (sample * 2) >= MAX_SIZE) sample *= 2
        return sample
    }
}

/** Muestra en el ImageView la foto del producto o un ícono genérico si no tiene foto. */
fun ImageView.showProductImage(base64: String) {
    val bitmap = ImageUtils.decode(base64)
    if (bitmap != null) setImageBitmap(bitmap) else setImageResource(R.drawable.ic_image)
}
